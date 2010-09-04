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
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
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
    private final BooleanProperty horizontalBars = new BooleanProperty( this, "horizontalBars", false )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            clutchDirty = true;
            brakeDirty = true;
            throttleDirty = true;
        }
    };
    
    private final BooleanProperty swapThrottleAndBrake = new BooleanProperty( this, "swapThrottleAndBrake", false );
    private boolean oldSwapTB = swapThrottleAndBrake.getBooleanValue();
    
    private final BooleanProperty displayClutch = new BooleanProperty( this, "displayClutch", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            clutchDirty = true;
        }
    };
    private final BooleanProperty displayBrake = new BooleanProperty( this, "displayBrake", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            brakeDirty = true;
        }
    };
    private final BooleanProperty displayThrottle = new BooleanProperty( this, "displayThrottle", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            throttleDirty = true;
        }
    };
    
    private final ImageProperty clutchImage = new ImageProperty( this, "clutchImage", null, "", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            clutchDirty = true;
        }
    };
    private final ColorProperty clutchColor = new ColorProperty( this, "clutchColor", "#0000FF" )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            clutchDirty = true;
        }
    };
    
    private final ImageProperty brakeImage = new ImageProperty( this, "brakeImage", null, "", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            brakeDirty = true;
        }
    };
    private final ColorProperty brakeColor = new ColorProperty( this, "brakeColor", "#FF0000" )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            brakeDirty = true;
        }
    };
    
    private final ImageProperty throttleImage = new ImageProperty( this, "throttleImage", null, "", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            throttleDirty = true;
        }
    };
    private final ColorProperty throttleColor = new ColorProperty( this, "throttleColor", "#00FF00" )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            throttleDirty = true;
        }
    };
    
    private TextureImage2D scaledClutchTexture = null;
    private TextureImage2D scaledBrakeTexture = null;
    private TextureImage2D scaledThrottleTexture = null;
    
    private TransformableTexture texClutch = null;
    private TransformableTexture texBrake = null;
    private TransformableTexture texThrottle = null;
    
    private boolean clutchDirty = true;
    private boolean brakeDirty = true;
    private boolean throttleDirty = true;
    
    private final IntProperty gap = new IntProperty( this, "gap", 5 );
    
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
    
    private int initClutchTexture( boolean isEditorMode, int offset, int w, int h, int gap )
    {
        if ( displayClutch.getBooleanValue() )
        {
            if ( ( texClutch == null ) || ( texClutch.getWidth() != w ) || ( texClutch.getHeight() != h ) || clutchDirty )
            {
                texClutch = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, texClutch, isEditorMode );
                
                ImageTemplate it = clutchImage.getImage();
                if ( it == null )
                {
                    scaledClutchTexture = null;
                    texClutch.getTexture().clear( clutchColor.getColor(), true, null );
                }
                else
                {
                    if ( horizontalBars.getBooleanValue() )
                    {
                        scaledClutchTexture = it.getScaledTextureImage( w, h * 2, scaledClutchTexture, isEditorMode );
                        texClutch.getTexture().clear( scaledClutchTexture, 0, h, w, h, 0, 0, w, h, true, null );
                    }
                    else
                    {
                        scaledClutchTexture = it.getScaledTextureImage( w * 2, h, scaledClutchTexture, isEditorMode );
                        texClutch.getTexture().clear( scaledClutchTexture, w, 0, w, h, 0, 0, w, h, true, null );
                    }
                }
                
                if ( horizontalBars.getBooleanValue() )
                    texClutch.setTranslation( 0, offset );
                else
                    texClutch.setTranslation( offset, 0 );
                
                clutchDirty = false;
            }
            
            if ( horizontalBars.getBooleanValue() )
                offset += h + gap;
            else
                offset += w + gap;
        }
        
        return ( offset );
    }
    
    private int initBrakeTexture( boolean isEditorMode, int offset, int w, int h, int gap )
    {
        if ( displayBrake.getBooleanValue() )
        {
            if ( ( texBrake == null ) || ( texBrake.getWidth() != w ) || ( texBrake.getHeight() != h ) || brakeDirty || ( swapThrottleAndBrake.getBooleanValue() != oldSwapTB ) )
            {
                texBrake = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, texBrake, isEditorMode );
                
                ImageTemplate it = brakeImage.getImage();
                if ( it == null )
                {
                    scaledBrakeTexture = null;
                    texBrake.getTexture().clear( brakeColor.getColor(), true, null );
                }
                else
                {
                    if ( horizontalBars.getBooleanValue() )
                    {
                        scaledBrakeTexture = it.getScaledTextureImage( w, h * 2, scaledBrakeTexture, isEditorMode );
                        texBrake.getTexture().clear( scaledBrakeTexture, 0, h, w, h, 0, 0, w, h, true, null );
                    }
                    else
                    {
                        scaledBrakeTexture = it.getScaledTextureImage( w * 2, h, scaledBrakeTexture, isEditorMode );
                        texBrake.getTexture().clear( scaledBrakeTexture, w, 0, w, h, 0, 0, w, h, true, null );
                    }
                }
                
                if ( horizontalBars.getBooleanValue() )
                    texBrake.setTranslation( 0, offset );
                else
                    texBrake.setTranslation( offset, 0 );
                
                brakeDirty = false;
            }
            
            if ( horizontalBars.getBooleanValue() )
                offset += h + gap;
            else
                offset += w + gap;
        }
        
        return ( offset );
    }
    
    private int initThrottleTexture( boolean isEditorMode, int offset, int w, int h, int gap )
    {
        if ( displayThrottle.getBooleanValue() )
        {
            if ( ( texThrottle == null ) || ( texThrottle.getWidth() != w ) || ( texThrottle.getHeight() != h ) || throttleDirty || ( swapThrottleAndBrake.getBooleanValue() != oldSwapTB ) )
            {
                texThrottle = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, texThrottle, isEditorMode );
                
                ImageTemplate it = throttleImage.getImage();
                if ( it == null )
                {
                    scaledThrottleTexture = null;
                    texThrottle.getTexture().clear( throttleColor.getColor(), true, null );
                }
                else
                {
                    if ( horizontalBars.getBooleanValue() )
                    {
                        scaledThrottleTexture = it.getScaledTextureImage( w, h * 2, scaledThrottleTexture, isEditorMode );
                        texThrottle.getTexture().clear( scaledThrottleTexture, 0, h, w, h, 0, 0, w, h, true, null );
                    }
                    else
                    {
                        scaledThrottleTexture = it.getScaledTextureImage( w * 2, h, scaledThrottleTexture, isEditorMode );
                        texThrottle.getTexture().clear( scaledThrottleTexture, w, 0, w, h, 0, 0, w, h, true, null );
                    }
                }
                
                if ( horizontalBars.getBooleanValue() )
                    texThrottle.setTranslation( 0, offset );
                else
                    texThrottle.setTranslation( offset, 0 );
                
                throttleDirty = false;
            }
            
            if ( horizontalBars.getBooleanValue() )
                offset += h + gap;
            else
                offset += w + gap;
        }
        
        return ( offset );
    }
    
    private int initSubTextures( boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
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
        final int w = horizontalBars.getBooleanValue() ? widgetInnerWidth : ( widgetInnerWidth + gap ) / numBars - gap;
        final int h = horizontalBars.getBooleanValue() ? ( widgetInnerHeight + gap ) / numBars - gap : widgetInnerHeight;
        
        int offset = 0;
        
        offset = initClutchTexture( isEditorMode, offset, w, h, gap );
        
        if ( swapThrottleAndBrake.getBooleanValue() )
        {
            offset = initThrottleTexture( isEditorMode, offset, w, h, gap );
            offset = initBrakeTexture( isEditorMode, offset, w, h, gap );
        }
        else
        {
            offset = initBrakeTexture( isEditorMode, offset, w, h, gap );
            offset = initThrottleTexture( isEditorMode, offset, w, h, gap );
        }
        
        oldSwapTB = swapThrottleAndBrake.getBooleanValue();
        
        return ( numBars );
    }
    
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        int numBars = initSubTextures( editorPresets != null, widgetInnerWidth, widgetInnerHeight );
        
        if ( numBars == 0 )
            return ( null );
        
        TransformableTexture[] texs = new TransformableTexture[ numBars ];
        
        int i = 0;
        if ( displayClutch.getBooleanValue() )
            texs[i++] = texClutch;
        if ( swapThrottleAndBrake.getBooleanValue() )
        {
            if ( displayThrottle.getBooleanValue() )
                texs[i++] = texThrottle;
            if ( displayBrake.getBooleanValue() )
                texs[i++] = texBrake;
        }
        else
        {
            if ( displayBrake.getBooleanValue() )
                texs[i++] = texBrake;
            if ( displayThrottle.getBooleanValue() )
                texs[i++] = texThrottle;
        }
        
        return ( texs );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        initSubTextures( editorPresets != null, width, height );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        final TelemetryData telemData = gameData.getTelemetryData();
        float uClutch = isEditorMode ? 1.0f : telemData.getUnfilteredClutch();
        float uBrake = isEditorMode ? 0.2f : telemData.getUnfilteredBrake();
        float uThrottle = isEditorMode ? 0.4f : telemData.getUnfilteredThrottle();
        
        if ( needsCompleteRedraw )
        {
            if ( displayClutch.getBooleanValue() && ( scaledClutchTexture != null ) )
                texture.drawImage( scaledClutchTexture, 0, 0, texClutch.getWidth(), texClutch.getHeight(), offsetX + (int)texClutch.getTransX(), offsetY + (int)texClutch.getTransY(), texClutch.getWidth(), texClutch.getHeight(), true, null );
            if ( displayBrake.getBooleanValue() && ( scaledBrakeTexture != null ) )
                texture.drawImage( scaledBrakeTexture, 0, 0, texBrake.getWidth(), texBrake.getHeight(), offsetX + (int)texBrake.getTransX(), offsetY + (int)texBrake.getTransY(), texBrake.getWidth(), texBrake.getHeight(), true, null );
            if ( displayThrottle.getBooleanValue() && ( scaledThrottleTexture != null ) )
                texture.drawImage( scaledThrottleTexture, 0, 0, texThrottle.getWidth(), texThrottle.getHeight(), offsetX + (int)texThrottle.getTransX(), offsetY + (int)texThrottle.getTransY(), texThrottle.getWidth(), texThrottle.getHeight(), true, null );
        }
        
        if ( horizontalBars.getBooleanValue() )
        {
            final int w = displayThrottle.getBooleanValue() ? texThrottle.getWidth() : ( displayBrake.getBooleanValue() ? texBrake.getWidth() : ( displayClutch.getBooleanValue() ? texClutch.getWidth() : 0 ) );
            int clutch = (int)( w * uClutch );
            int brake = (int)( w * uBrake );
            int throttle = (int)( w * uThrottle );
            
            if ( displayClutch.getBooleanValue() )
                texClutch.setClipRect( 0, 0, clutch, texClutch.getHeight(), true );
            if ( displayBrake.getBooleanValue() )
                texBrake.setClipRect( 0, 0, brake, texBrake.getHeight(), true );
            if ( displayThrottle.getBooleanValue() )
                texThrottle.setClipRect( 0, 0, throttle, texThrottle.getHeight(), true );
        }
        else
        {
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
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( horizontalBars, "Extend the bars horizontally instead of vertically?" );
        writer.writeProperty( swapThrottleAndBrake, "Swap throttle and brake order?" );
        writer.writeProperty( displayClutch, "Display the clutch bar?" );
        writer.writeProperty( clutchImage, "The image for the clutch bar. (overrules the color)" );
        writer.writeProperty( clutchColor, "The color used for the clutch bar in the format #RRGGBB (hex)." );
        writer.writeProperty( displayBrake, "Display the brake bar?" );
        writer.writeProperty( brakeImage, "The image for the brake bar. (overrules the color)" );
        writer.writeProperty( brakeColor, "The color used for the brake bar in the format #RRGGBB (hex)." );
        writer.writeProperty( displayThrottle, "Display the throttle bar?" );
        writer.writeProperty( throttleImage, "The image for the throttle bar. (overrules the color)" );
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
        
        if ( loader.loadProperty( horizontalBars ) );
        else if ( loader.loadProperty( swapThrottleAndBrake ) );
        else if ( loader.loadProperty( displayClutch ) );
        else if ( loader.loadProperty( clutchImage ) );
        else if ( loader.loadProperty( clutchColor ) );
        else if ( loader.loadProperty( displayBrake ) );
        else if ( loader.loadProperty( brakeImage ) );
        else if ( loader.loadProperty( brakeColor ) );
        else if ( loader.loadProperty( displayThrottle ) );
        else if ( loader.loadProperty( throttleImage ) );
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
        
        propsCont.addProperty( horizontalBars );
        propsCont.addProperty( swapThrottleAndBrake );
        propsCont.addProperty( displayClutch );
        propsCont.addProperty( clutchImage );
        propsCont.addProperty( clutchColor );
        propsCont.addProperty( displayBrake );
        propsCont.addProperty( brakeImage );
        propsCont.addProperty( brakeColor );
        propsCont.addProperty( displayThrottle );
        propsCont.addProperty( throttleImage );
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
        
        setPadding( 3, 3, 3, 3 );
    }
}
