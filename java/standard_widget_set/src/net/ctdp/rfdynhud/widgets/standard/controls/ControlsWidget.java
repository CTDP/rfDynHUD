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
package net.ctdp.rfdynhud.widgets.standard.controls;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TextureImage2D.TextDirection;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetPackage;
import net.ctdp.rfdynhud.widgets.standard._util.StandardWidgetSet;

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
    
    private final BooleanProperty swapThrottleAndBrake = new BooleanProperty( this, "swapThrottleAndBrake", "swapThrottle/Brake", false );
    private boolean oldSwapTB = swapThrottleAndBrake.getBooleanValue();
    
    private boolean clutchDirty = true;
    
    private final BooleanProperty displayClutch = new BooleanProperty( this, "displayClutch", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            clutchDirty = true;
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
    
    private TextureImage2D scaledClutchTexture = null;
    private TransformableTexture texClutch = null;
    
    private boolean brakeDirty = true;
    
    private final BooleanProperty displayBrake = new BooleanProperty( this, "displayBrake", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            brakeDirty = true;
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
    
    private TextureImage2D scaledBrakeTexture = null;
    private TransformableTexture texBrake = null;
    
    private boolean throttleDirty = true;
    
    private final BooleanProperty displayThrottle = new BooleanProperty( this, "displayThrottle", true )
    {
        @Override
        protected void onValueChanged( boolean newValue )
        {
            throttleDirty = true;
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
    
    private TextureImage2D scaledThrottleTexture = null;
    private TransformableTexture texThrottle = null;
    
    private final IntProperty gap = new IntProperty( this, "gap", 5 );
    
    private final IntProperty labelOffset = new IntProperty( this, "labelOffset", 20 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            clutchDirty = true;
            brakeDirty = true;
            throttleDirty = true;
        }
    };
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        return ( viewedVSI.isPlayer() );
    }
    
    private void drawBarLabel( String label, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Rectangle2D bounds = TextureImage2D.getStringBounds( label, getFontProperty() );
        
        if ( labelOffset.getIntValue() > -bounds.getWidth() )
        {
            if ( horizontalBars.getBooleanValue() )
            {
                if ( labelOffset.getIntValue() < width )
                    texture.drawString( label, offsetX + labelOffset.getIntValue(), offsetY + ( height - (int)bounds.getHeight() ) / 2 - (int)bounds.getY(), bounds, getFont(), isFontAntiAliased(), getFontColor(), true, null );
            }
            else
            {
                if ( labelOffset.getIntValue() < height )
                    texture.drawString( label, offsetX + width / 2 - (int)bounds.getHeight() / 2 - (int)bounds.getY(), offsetY + height - labelOffset.getIntValue(), bounds, getFont(), isFontAntiAliased(), getFontColor(), TextDirection.UP, true, null );
                    //texture.drawString( label, offsetX + width / 2 + (int)bounds.getY() + (int)bounds.getHeight() / 2, offsetY + labelOffset.getIntValue(), bounds, getFont(), isFontAntiAliased(), getFontColor(), TextDirection.DOWN, true, null );
            }
        }
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
                
                drawBarLabel( Loc.clutch_label, texClutch.getTexture(), 0, 0, texClutch.getWidth(), texClutch.getHeight() );
                
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
                
                drawBarLabel( Loc.brake_label, texBrake.getTexture(), 0, 0, texBrake.getWidth(), texBrake.getHeight() );
                
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
                
                drawBarLabel( Loc.throttle_label, texThrottle.getTexture(), 0, 0, texThrottle.getWidth(), texThrottle.getHeight() );
                
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
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        int numBars = initSubTextures( isEditorMode, widgetInnerWidth, widgetInnerHeight );
        
        if ( numBars == 0 )
            return;
        
        if ( displayClutch.getBooleanValue() )
            collector.add( texClutch );
        if ( swapThrottleAndBrake.getBooleanValue() )
        {
            if ( displayThrottle.getBooleanValue() )
                collector.add( texThrottle );
            if ( displayBrake.getBooleanValue() )
                collector.add( texBrake );
        }
        else
        {
            if ( displayBrake.getBooleanValue() )
                collector.add( texBrake );
            if ( displayThrottle.getBooleanValue() )
                collector.add( texThrottle );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int width, int height )
    {
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final TelemetryData telemData = gameData.getTelemetryData();
        float uClutch = isEditorMode ? 1.0f : telemData.getUnfilteredClutch();
        float uBrake = isEditorMode ? 0.2f : telemData.getUnfilteredBrake();
        float uThrottle = isEditorMode ? 0.4f : telemData.getUnfilteredThrottle();
        
        if ( needsCompleteRedraw )
        {
            if ( displayClutch.getBooleanValue() && ( scaledClutchTexture != null ) )
                texture.drawImage( scaledClutchTexture, 0, 0, texClutch.getWidth(), texClutch.getHeight(), offsetX + (int)texClutch.getTransX(), offsetY + (int)texClutch.getTransY(), texClutch.getWidth(), texClutch.getHeight(), true, null );
            if ( displayClutch.getBooleanValue() )
                drawBarLabel( Loc.clutch_label, texture, offsetX + (int)texClutch.getTransX(), offsetY + (int)texClutch.getTransY(), texClutch.getWidth(), texClutch.getHeight() );
            if ( displayBrake.getBooleanValue() && ( scaledBrakeTexture != null ) )
                texture.drawImage( scaledBrakeTexture, 0, 0, texBrake.getWidth(), texBrake.getHeight(), offsetX + (int)texBrake.getTransX(), offsetY + (int)texBrake.getTransY(), texBrake.getWidth(), texBrake.getHeight(), true, null );
            if ( displayBrake.getBooleanValue() )
                drawBarLabel( Loc.brake_label, texture, offsetX + (int)texBrake.getTransX(), offsetY + (int)texBrake.getTransY(), texBrake.getWidth(), texBrake.getHeight() );
            if ( displayThrottle.getBooleanValue() && ( scaledThrottleTexture != null ) )
                texture.drawImage( scaledThrottleTexture, 0, 0, texThrottle.getWidth(), texThrottle.getHeight(), offsetX + (int)texThrottle.getTransX(), offsetY + (int)texThrottle.getTransY(), texThrottle.getWidth(), texThrottle.getHeight(), true, null );
            if ( displayThrottle.getBooleanValue() )
                drawBarLabel( Loc.throttle_label, texture, offsetX + (int)texThrottle.getTransX(), offsetY + (int)texThrottle.getTransY(), texThrottle.getWidth(), texThrottle.getHeight() );
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
    public void saveProperties( PropertyWriter writer ) throws IOException
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
        writer.writeProperty( labelOffset, "The offset for bar text from the left or bottom boundary of the bar." );
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
        else if ( loader.loadProperty( labelOffset ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( PropertiesContainer propsCont, boolean forceAll )
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
        propsCont.addProperty( labelOffset );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareForMenuItem()
    {
        super.prepareForMenuItem();
        
        getFontProperty().setFont( "Dialog", Font.PLAIN, 5, false, true );
        labelOffset.setIntValue( 3 );
    }
    
    public ControlsWidget()
    {
        super( 9.9f, 16.5f );
        
        setPadding( 3, 3, 3, 3 );
        
        getFontProperty().setFont( "DokChampa", Font.BOLD, 22, true, true );
        getFontColorProperty().setColor( "#FFFFFF" );
    }
}
