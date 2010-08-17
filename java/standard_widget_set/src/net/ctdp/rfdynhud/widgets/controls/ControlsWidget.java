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
package net.ctdp.rfdynhud.widgets.controls;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link ControlsWidget} displays clutch, brake and throttle.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ControlsWidget extends Widget
{
    private final BooleanProperty displayClutch = new BooleanProperty( this, "displayClutch", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            resetTransTexs();
        }
    };
    private final BooleanProperty displayBrake = new BooleanProperty( this, "displayBrake", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            resetTransTexs();
        }
    };
    private final BooleanProperty displayThrottle = new BooleanProperty( this, "displayThrottle", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            resetTransTexs();
        }
    };
    
    private final ColorProperty clutchColor = new ColorProperty( this, "clutchColor", "#0000FF" );
    private final ColorProperty brakeColor = new ColorProperty( this, "brakeColor", "#FF0000" );
    private final ColorProperty throttleColor = new ColorProperty( this, "throttleColor", "#00FF00" );
    
    private TransformableTexture texClutch = null;
    private TransformableTexture texBrake = null;
    private TransformableTexture texThrottle = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion()
    {
        return ( composeVersion( 1, 0, 0 ) );
    }
    
    private void resetTransTexs()
    {
        texClutch = null;
        texBrake = null;
        texThrottle = null;
    }
    
    private final IntProperty gap = new IntProperty( this, "gap", 10 );
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        
        setUserVisible1( gameData.getScoringInfo().getViewedVehicleScoringInfo().isPlayer() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, editorPresets );
        
        setUserVisible1( viewedVSI.isPlayer() );
    }
    
    private int initSubTextures( int widgetInnerWidth, int widgetInnerHeight )
    {
        int numBars = 0;
        if ( displayClutch.getBooleanValue() )
            numBars++;
        if ( displayBrake.getBooleanValue() )
            numBars++;
        if ( displayThrottle.getBooleanValue() )
            numBars++;
        
        if ( numBars == 0 )
        {
            texClutch = null;
            texBrake = null;
            texThrottle = null;
            
            return ( 0 );
        }
        
        final int gap = this.gap.getIntValue();
        final int w = ( widgetInnerWidth - 6 + gap ) / numBars - gap;
        final int h = widgetInnerHeight - 6;
        
        int left = 3;
        if ( displayClutch.getBooleanValue() && ( ( texClutch == null ) || ( texClutch.getWidth() != w ) || ( texClutch.getHeight() != h ) ) )
        {
            texClutch = new TransformableTexture( w, h, left, 3, 0, 0, 0f, 1f, 1f );
            left += w + gap;
        }
        
        if ( displayBrake.getBooleanValue() && ( ( texBrake == null ) || ( texBrake.getWidth() != w ) || ( texBrake.getHeight() != h ) ) )
        {
            texBrake = new TransformableTexture( w, h, left, 3, 0, 0, 0f, 1f, 1f );
            left += w + gap;
        }
        
        if ( displayThrottle.getBooleanValue() && ( ( texThrottle == null ) || ( texThrottle.getWidth() != w ) || ( texThrottle.getHeight() != h ) ) )
        {
            texThrottle = new TransformableTexture( w, h, left, 3, 0, 0, 0f, 1f, 1f );
            left += w + gap;
        }
        
        return ( numBars );
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        int numBars = initSubTextures( widgetInnerWidth, widgetInnerHeight );
        
        if ( numBars == 0 )
            return ( null );
        
        TransformableTexture[] texs = new TransformableTexture[ numBars ];
        
        int i = 0;
        if ( displayClutch.getBooleanValue() )
            texs[i++] = texClutch;
        if ( displayBrake.getBooleanValue() )
            texs[i++] = texBrake;
        if ( displayThrottle.getBooleanValue() )
            texs[i++] = texThrottle;
        
        return ( texs );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        initSubTextures( width, height );
        
        if ( displayClutch.getBooleanValue() )
            texClutch.getTexture().clear( clutchColor.getColor(), true, null );
        
        if ( displayBrake.getBooleanValue() )
            texBrake.getTexture().clear( brakeColor.getColor(), true, null );
        
        if ( displayThrottle.getBooleanValue() )
            texThrottle.getTexture().clear( throttleColor.getColor(), true, null );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        final TelemetryData telemData = gameData.getTelemetryData();
        float uClutch = isEditorMode ? 0.5f : telemData.getUnfilteredClutch();
        float uBrake = isEditorMode ? 0.2f : telemData.getUnfilteredBrake();
        float uThrottle = telemData.getUnfilteredThrottle();
        
        final int h = displayThrottle.getBooleanValue() ? texThrottle.getHeight() : ( displayBrake.getBooleanValue() ? texBrake.getHeight() : ( displayClutch.getBooleanValue() ? texClutch.getHeight() : 0 ) );
        int clutch = (int)( h * uClutch );
        int brake = (int)( h * uBrake );
        int throttle = (int)( h * uThrottle );
        
        if ( displayClutch.getBooleanValue() )
            texClutch.setClipRect( 0, h - clutch, texClutch.getWidth(), clutch, true );
        if ( displayBrake.getBooleanValue() )
            texBrake.setClipRect( 0, h - brake, texBrake.getWidth(), brake, true );
        if ( displayThrottle.getBooleanValue() )
            texThrottle.setClipRect( 0, h - throttle, texThrottle.getWidth(), throttle, true );
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( displayClutch, "Display the clutch bar?" );
        writer.writeProperty( clutchColor, "The color used for the clutch bar in the format #RRGGBB (hex)." );
        writer.writeProperty( displayBrake, "Display the brake bar?" );
        writer.writeProperty( brakeColor, "The color used for the brake bar in the format #RRGGBB (hex)." );
        writer.writeProperty( displayThrottle, "Display the throttle bar?" );
        writer.writeProperty( throttleColor, "The color used for the throttle bar in the format #RRGGBB (hex)." );
        writer.writeProperty( gap, "Gap between the bars" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( displayClutch ) );
        else if ( loader.loadProperty( clutchColor ) );
        else if ( loader.loadProperty( displayBrake ) );
        else if ( loader.loadProperty( brakeColor ) );
        else if ( loader.loadProperty( displayThrottle ) );
        else if ( loader.loadProperty( throttleColor ) );
        else if ( loader.loadProperty( gap ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( displayClutch );
        propsCont.addProperty( clutchColor );
        propsCont.addProperty( displayBrake );
        propsCont.addProperty( brakeColor );
        propsCont.addProperty( displayThrottle );
        propsCont.addProperty( throttleColor );
        propsCont.addProperty( gap );
    }
    
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    
    public ControlsWidget( String name )
    {
        super( name, 9.9f, 16.5f );
    }
}
