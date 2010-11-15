/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.widgets.standard.startinglight;

import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.base.widget.__WPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

/**
 * The {@link StartingLightWidget} displays a starting light for the race.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StartingLightWidget extends Widget
{
    private static final int MAX_LIGHTS = 5;
    
    private TextureImage2D offImage = null;
    private final ImageProperty offImageProp = new ImageProperty( this, "offImageName", "standard/starting_light_off.png" )
    {
        @Override
        public void setValue( Object value )
        {
            super.setValue( value );
            
            offImage = null;
            onImage = null;
        }
    };
    
    private TextureImage2D onImage = null;
    private final ImageProperty onImageProp = new ImageProperty( this, "onImageName", "standard/starting_light_on.png" )
    {
        @Override
        public void setValue( Object value )
        {
            super.setValue( value );
            
            offImage = null;
            onImage = null;
        }
    };
    
    private final IntProperty numRows = new IntProperty( this, "numRows", 2, 1, 4 );
    
    private final FloatProperty visibleTimeAfterLightsOff = new FloatProperty( this, "visibleTimeAfterLightsOff", "visTimeAfterOff", 4.0f, 0f, 60f, false );
    
    private final EnumValue<GamePhase> gamePhase = new EnumValue<GamePhase>();
    private final IntValue numLights = new IntValue();
    private float visibleTime = -1f;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE_TIMING );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        gamePhase.reset();
        numLights.reset();
        visibleTime = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
    }
    
    @Override
    protected Boolean updateVisibility( LiveGameData gameData, boolean isEditorMode )
    {
        /*Boolean result = */super.updateVisibility( gameData, isEditorMode );
        
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( !scoringInfo.getSessionType().isRace() )
        {
            return ( false );
        }
        
        boolean visible = true;
        
        gamePhase.update( scoringInfo.getGamePhase() );
        float sessionTime = scoringInfo.getSessionTime();
        
        if ( gamePhase.hasChanged( false ) )
        {
            if ( gamePhase.getValue() == GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN )
            {
                visible = true;
            }
            else
            {
                if ( gamePhase.getOldValue() == GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN )
                    visibleTime = sessionTime + visibleTimeAfterLightsOff.getFloatValue();
                
                visible = ( sessionTime <= visibleTime );
            }
        }
        else if ( gamePhase.getValue() != GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN )
        {
            visible = ( sessionTime <= visibleTime );
        }
        
        gamePhase.setUnchanged();
        if ( sessionTime > visibleTime )
            visibleTime = -1f;
        
        return ( visible );
    }
    
    private void loadImages( boolean isEditorMode, int innerHeight )
    {
        final int imageHeight = innerHeight / numRows.getIntValue();
        
        if ( ( offImage == null ) || ( offImage.getHeight() != imageHeight ) )
        {
            try
            {
                ImageTemplate it = offImageProp.getImage();
                
                float scale = (float)imageHeight / (float)it.getBaseHeight();
                int imageWidth = (int)( it.getBaseWidth() * scale );
                
                offImage = it.getScaledTextureImage( imageWidth, imageHeight, offImage, isEditorMode );
                onImage = onImageProp.getImage().getScaledTextureImage( imageWidth, imageHeight, onImage, isEditorMode );
            }
            catch ( Throwable t )
            {
                log( t );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
        loadImages( isEditorMode, height );
    }
    
    @Override
    public int getMaxWidth( LiveGameData gameData, boolean isEditorMode )
    {
        loadImages( isEditorMode, getInnerSize().getEffectiveHeight() );
        
        return ( offImage.getWidth() * MAX_LIGHTS + getEffectiveWidth() - getInnerSize().getEffectiveWidth() );
    }
    
    @Override
    protected boolean checkForChanges( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int width, int height )
    {
        if ( !isEditorMode && ( offImage != null ) )
        {
            int newWidth = offImage.getWidth() * Math.min( gameData.getScoringInfo().getNumRedLights(), MAX_LIGHTS );
            
            if ( newWidth != width )
            {
                getSize().setEffectiveSize( getEffectiveWidth() - getInnerSize().getEffectiveWidth() + newWidth, getEffectiveHeight() );
                
                return ( true );
            }
        }
        
        return ( false );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        int m = scoringInfo.getNumRedLights();
        int numIgnoredLights = ( m <= MAX_LIGHTS ) ? 0 : m - MAX_LIGHTS;
        m = Math.min( m, MAX_LIGHTS );
        
        numLights.update( scoringInfo.getStartLightFrame() - numIgnoredLights );
        
        if ( needsCompleteRedraw || numLights.hasChanged() )
        {
            int n = numLights.getValue();
            
            if ( isEditorMode )
                n = m / 2;
            
            if ( n > m )
                n = 0;
            
            int offX2 = 0;
            TextureImage2D img;
            
            for ( int i = 0; i < m; i++ )
            {
                if ( i < n )
                    img = onImage;
                else
                    img = offImage;
                
                clearBackgroundRegion( texture, offsetX, offsetY, offX2, 0, img.getWidth(), height, true, null );
                
                for ( int j = 0; j < numRows.getIntValue(); j++ )
                    texture.drawImage( img, offsetX + offX2, offsetY + j * img.getHeight(), false, null );
                
                offX2 += img.getWidth();
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( PropertyWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( offImageProp, "The image name for the off-lights." );
        writer.writeProperty( onImageProp, "The image name for the on-lights." );
        writer.writeProperty( numRows, "The number of light rows." );
        writer.writeProperty( visibleTimeAfterLightsOff, "Amount of seconds, the Widget stays visible after all lights have gone off." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.getSourceVersion().getBuild() < 70 )
        {
            if ( loader.getCurrentKey().equals( "initialVisibility" ) )
                __WPrivilegedAccess.setInputVisible( this, true );
        }
        
        if ( loader.loadProperty( offImageProp ) );
        else if ( loader.loadProperty( onImageProp ) );
        else if ( loader.loadProperty( numRows ) );
        else if ( loader.loadProperty( visibleTimeAfterLightsOff ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( offImageProp );
        propsCont.addProperty( onImageProp );
        propsCont.addProperty( numRows );
        propsCont.addProperty( visibleTimeAfterLightsOff );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    public StartingLightWidget()
    {
        super( 11.328125f, true, 8.3984375f, true );
    }
}
