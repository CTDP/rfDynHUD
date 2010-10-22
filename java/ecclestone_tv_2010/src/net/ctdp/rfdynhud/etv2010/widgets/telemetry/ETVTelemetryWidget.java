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
package net.ctdp.rfdynhud.etv2010.widgets.telemetry;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.SubTextureCollector;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.widgets.revmeter.RevMeterWidget;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * This {@link Widget} attempts to imitate the 2010er TV overlay for F1 telemetry
 * (revs, velocity and throttle/brake).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ETVTelemetryWidget extends RevMeterWidget
{
    public static final int MAX_VELOCITY_LOCAL_Z_INDEX = NEEDLE_LOCAL_Z_INDEX / 2;
    public static final int CONTROLS_LOCAL_Z_INDEX = MAX_VELOCITY_LOCAL_Z_INDEX + 1;
    
    private final FontProperty velocityNumberFont = new FontProperty( this, "velocityNumberFont", "font", ETVUtils.ETV_VELOCITY_FONT, false );
    private final ColorProperty velocityNumberFontColor = new ColorProperty( this, "velocityNumberFontColor", "color", ETVUtils.ETV_CAPTION_FONT_COLOR, false );
    
    private final IntProperty velocityNumber1PosX = new IntProperty( this, "velocityNumber1PosX", "pos1X", 270 );
    private final IntProperty velocityNumber1PosY = new IntProperty( this, "velocityNumber1PosY", "pos1Y", 620 );
    private final IntProperty velocityNumber2PosX = new IntProperty( this, "velocityNumber2PosX", "pos2X", 100 );
    private final IntProperty velocityNumber3PosX = new IntProperty( this, "velocityNumber3PosX", "pos3X", 100 );
    private final IntProperty velocityNumber4PosX = new IntProperty( this, "velocityNumber4PosX", "pos4X", 270 );
    private final IntProperty velocityNumber4PosY = new IntProperty( this, "velocityNumber4PosY", "pos4Y", 50 );
    private final IntProperty velocityUnitsPosX = new IntProperty( this, "velocityUnitsPosX", "unitsPosX", 300 );
    private final IntProperty velocityUnitsPosY = new IntProperty( this, "velocityUnitsPosY", "unitsPosY", 660 );
    
    private final IntProperty maxVelocity = new IntProperty( this, "maxVelocity", 340, 1, 1000 );
    private final IntProperty velocity2 = new IntProperty( this, "velocity2", 110, 1, 1000 );
    private final IntProperty velocity3 = new IntProperty( this, "velocity3", 220, 1, 1000 );
    
    private final ImageProperty maxVelocityOverlay = new ImageProperty( this, "maxVelocityOverlay", "image", "etv2010/max_velocity.png" );
    private TransformableTexture maxVelocityTexture = null;
    private final IntProperty maxVelocityLeftOffset = new IntProperty( this, "maxVelocityLeftOffset", "leftOffset", 25 );
    private final IntProperty maxVelocityTopOffset = new IntProperty( this, "maxVelocityTopOffset", "topOffset", 86 );
    
    private boolean throttleDirty = true;
    
    private final ImageProperty throttleImage = new ImageProperty( this, "throttleImage", null, "etv2010/throttle.png", false, false )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            throttleDirty = true;
        }
    };
    
    private TransformableTexture texThrottle1 = null;
    private TransformableTexture texThrottle2 = null;
    
    private boolean brakeDirty = true;
    
    private final ImageProperty brakeImage = new ImageProperty( this, "brakeImage", null, "etv2010/brake.png", false, false )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            brakeDirty = true;
        }
    };
    
    private TransformableTexture texBrake1 = null;
    private TransformableTexture texBrake2 = null;
    
    private final IntProperty controlsPosX = new IntProperty( this, "controlsPosX", 600 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            throttleDirty = true;
            brakeDirty = true;
        }
    };
    private final IntProperty controlsPosY = new IntProperty( this, "controlsPosY", 400 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            throttleDirty = true;
            brakeDirty = true;
        }
    };
    private final IntProperty controlsWidth = new IntProperty( this, "controlsWidth", 350 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            throttleDirty = true;
            brakeDirty = true;
        }
    };
    private final IntProperty controlsHeight = new IntProperty( this, "controlsHeight", 80 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            throttleDirty = true;
            brakeDirty = true;
        }
    };
    private final IntProperty controlsGap = new IntProperty( this, "controlsGap", "gap", 2 )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            throttleDirty = true;
            brakeDirty = true;
        }
    };
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedFontValue( name );
        
        if ( result != null )
            return ( result );
        
        return ( ETVUtils.getDefaultNamedFontValue( name ) );
    }
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( ETVUtils.WIDGET_PACKAGE );
    }
    
    @Override
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.IMAGE_INDICATOR + "etv2010/background.png" );
    }
    
    @Override
    protected void onBackgroundChanged( float deltaScaleX, float deltaScaleY )
    {
        super.onBackgroundChanged( deltaScaleX, deltaScaleY );
        
        maxVelocityLeftOffset.setIntValue( Math.round( maxVelocityLeftOffset.getIntValue() * deltaScaleX ) );
        maxVelocityTopOffset.setIntValue( Math.round( maxVelocityTopOffset.getIntValue() * deltaScaleY ) );
    }
    
    @Override
    protected String getInitialNeedleImage()
    {
        return ( "etv2010/needle_-154.png" );
    }
    
    @Override
    protected String getInitialPeakNeedleImage()
    {
        return ( "etv2010/peak_needle_-216.png" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Boolean onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        Boolean result = super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        if ( texThrottle1 != null )
            texThrottle1.setVisible( viewedVSI.isPlayer() );
        
        if ( texThrottle2 != null )
            texThrottle2.setVisible( viewedVSI.isPlayer() );
        
        if ( texBrake1 != null )
            texBrake1.setVisible( viewedVSI.isPlayer() );
        
        if ( texBrake2 != null )
            texBrake2.setVisible( viewedVSI.isPlayer() );
        
        return ( result );
    }
    
    private void loadThrottleTexture( boolean isEditorMode, int w, int h )
    {
        if ( ( texThrottle1 == null ) || ( texThrottle1.getWidth() != w ) || ( texThrottle1.getHeight() != h ) || throttleDirty )
        {
            texThrottle1 = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, texThrottle1, isEditorMode );
            texThrottle2 = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, texThrottle2, isEditorMode );
            
            ImageTemplate it = throttleImage.getImage();
            it.drawScaled( 0, 0, it.getBaseWidth(), it.getBaseHeight() / 2, 0, 0, w, h, texThrottle1.getTexture(), true );
            it.drawScaled( 0, it.getBaseHeight() / 2, it.getBaseWidth(), it.getBaseHeight() / 2, 0, 0, w, h, texThrottle2.getTexture(), true );
            
            float x = controlsPosX.getFloatValue() * getBackground().getScaleX();
            float y = controlsPosY.getFloatValue() * getBackground().getScaleY();
            
            texThrottle1.setTranslation( x, y );
            texThrottle2.setTranslation( x, y );
            
            texThrottle1.setLocalZIndex( CONTROLS_LOCAL_Z_INDEX );
            texThrottle2.setLocalZIndex( CONTROLS_LOCAL_Z_INDEX + 1 );
            
            throttleDirty = false;
        }
    }
    
    private void loadBrakeTexture( boolean isEditorMode, int w, int h )
    {
        if ( ( texBrake1 == null ) || ( texBrake1.getWidth() != w ) || ( texBrake1.getHeight() != h ) || brakeDirty )
        {
            texBrake1 = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, texBrake1, isEditorMode );
            texBrake2 = TransformableTexture.getOrCreate( w, h, TransformableTexture.DEFAULT_PIXEL_PERFECT_POSITIONING, texBrake2, isEditorMode );
            
            ImageTemplate it = brakeImage.getImage();
            it.drawScaled( 0, 0, it.getBaseWidth(), it.getBaseHeight() / 2, 0, 0, w, h, texBrake1.getTexture(), true );
            it.drawScaled( 0, it.getBaseHeight() / 2, it.getBaseWidth(), it.getBaseHeight() / 2, 0, 0, w, h, texBrake2.getTexture(), true );
            
            float x = controlsPosX.getFloatValue() * getBackground().getScaleX();
            float y = controlsPosY.getFloatValue() * getBackground().getScaleY() + h + controlsGap.getIntValue();
            
            texBrake1.setTranslation( x, y );
            texBrake2.setTranslation( x, y );
            
            texBrake1.setLocalZIndex( CONTROLS_LOCAL_Z_INDEX );
            texBrake2.setLocalZIndex( CONTROLS_LOCAL_Z_INDEX + 1 );
            
            brakeDirty = false;
        }
    }
    
    private void drawVelocityNumbers( SpeedUnits units, TextureImage2D texture, int offsetX, int offsetY )
    {
        if ( maxVelocityTexture == null )
            return;
        
        float scaleX = getBackground().getScaleX();
        float scaleY = getBackground().getScaleY();
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        texCanvas.setFont( velocityNumberFont.getFont() );
        texCanvas.setAntialiazingEnabled( velocityNumberFont.isAntiAliased() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Color dropShadowColor = markersFontDropShadowColor.getColor();
        float dropShadowOffset = 2.2f; //numberFont.getFont().getSize() * 0.2f;
        boolean drawDropShadow = ( dropShadowColor.getAlpha() > 0 );
        
        int velo = 0;
        String s = String.valueOf( velo );
        Rectangle2D bounds = metrics.getStringBounds( s, texCanvas );
        int x = offsetX + Math.round( velocityNumber1PosX.getFloatValue() * scaleX - (float)( bounds.getWidth() / 2 ) );
        int y = offsetY + Math.round( velocityNumber1PosY.getFloatValue() * scaleY -(float)bounds.getY() - (float)( bounds.getHeight() / 2 ) );
        if ( drawDropShadow )
        {
            texCanvas.setColor( dropShadowColor );
            texCanvas.drawString( s, x + dropShadowOffset, y + dropShadowOffset );
        }
        texCanvas.setColor( velocityNumberFontColor.getColor() );
        texCanvas.drawString( s, x, y, bounds );
        
        velo = velocity2.getIntValue();
        s = String.valueOf( velo );
        bounds = metrics.getStringBounds( s, texCanvas );
        x = offsetX + Math.round( velocityNumber2PosX.getFloatValue() * scaleX - (float)( bounds.getWidth() / 2 ) );
        y = offsetY + Math.round( Math.round( maxVelocityTopOffset.getFloatValue() * scaleY + maxVelocityTexture.getHeight() - velocity2.getFloatValue() * maxVelocityTexture.getHeight() / maxVelocity.getFloatValue() ) -(float)bounds.getY() - (float)( bounds.getHeight() / 2 ) );
        if ( drawDropShadow )
        {
            texCanvas.setColor( dropShadowColor );
            texCanvas.drawString( s, x + dropShadowOffset, y + dropShadowOffset );
        }
        texCanvas.setColor( velocityNumberFontColor.getColor() );
        texCanvas.drawString( s, x, y, bounds );
        
        velo = velocity3.getIntValue();
        s = String.valueOf( velo );
        bounds = metrics.getStringBounds( s, texCanvas );
        x = offsetX + Math.round( velocityNumber3PosX.getFloatValue() * scaleX - (float)( bounds.getWidth() / 2 ) );
        y = offsetY + Math.round( Math.round( maxVelocityTopOffset.getFloatValue() * scaleY + maxVelocityTexture.getHeight() - velocity3.getFloatValue() * maxVelocityTexture.getHeight() / maxVelocity.getFloatValue() ) -(float)bounds.getY() - (float)( bounds.getHeight() / 2 ) );
        if ( drawDropShadow )
        {
            texCanvas.setColor( dropShadowColor );
            texCanvas.drawString( s, x + dropShadowOffset, y + dropShadowOffset );
        }
        texCanvas.setColor( velocityNumberFontColor.getColor() );
        texCanvas.drawString( s, x, y, bounds );
        
        velo = maxVelocity.getIntValue();
        s = String.valueOf( velo );
        bounds = metrics.getStringBounds( s, texCanvas );
        x = offsetX + Math.round( velocityNumber4PosX.getFloatValue() * scaleX - (float)( bounds.getWidth() / 2 ) );
        y = offsetY + Math.round( velocityNumber4PosY.getFloatValue() * scaleY -(float)bounds.getY() - (float)( bounds.getHeight() / 2 ) );
        if ( drawDropShadow )
        {
            texCanvas.setColor( dropShadowColor );
            texCanvas.drawString( s, x + dropShadowOffset, y + dropShadowOffset );
        }
        texCanvas.setColor( velocityNumberFontColor.getColor() );
        texCanvas.drawString( s, x, y, bounds );
        
        s = String.valueOf( units == SpeedUnits.MPH ? Loc.velocity_units_IMPERIAL : Loc.velocity_units_METRIC );
        bounds = metrics.getStringBounds( s, texCanvas );
        x = offsetX + Math.round( velocityUnitsPosX.getFloatValue() * scaleX - (float)( bounds.getWidth() / 2 ) );
        y = offsetY + Math.round( velocityUnitsPosY.getFloatValue() * scaleY -(float)bounds.getY() - (float)( bounds.getHeight() / 2 ) );
        if ( drawDropShadow )
        {
            texCanvas.setColor( dropShadowColor );
            texCanvas.drawString( s, x + dropShadowOffset, y + dropShadowOffset );
        }
        texCanvas.setColor( velocityNumberFontColor.getColor() );
        texCanvas.drawString( s, x, y, bounds );
    }
    
    private boolean loadMaxVelocityTexture( SpeedUnits speedUnits, boolean isEditorMode )
    {
        if ( maxVelocityOverlay.isNoImage() )
        {
            maxVelocityTexture = null;
            return ( false );
        }
        
        try
        {
            ImageTemplate it = maxVelocityOverlay.getImage();
            
            if ( it == null )
            {
                maxVelocityTexture = null;
                return ( false );
            }
            
            int w = Math.round( it.getBaseWidth() * getBackground().getScaleX() );
            int h = Math.round( it.getBaseHeight() * getBackground().getScaleY() );
            TransformableTexture old = maxVelocityTexture;
            int oldW = ( maxVelocityTexture == null ) ? -1 : maxVelocityTexture.getWidth();
            int oldH = ( maxVelocityTexture == null ) ? -1 : maxVelocityTexture.getHeight();
            maxVelocityTexture = it.getScaledTransformableTexture( w, h, maxVelocityTexture, isEditorMode );
            if ( ( maxVelocityTexture == old ) && ( oldW == maxVelocityTexture.getWidth() ) && ( oldH == maxVelocityTexture.getHeight() ) )
                it.drawScaled( 0, 0, maxVelocityTexture.getWidth(), maxVelocityTexture.getHeight(), maxVelocityTexture.getTexture(), true );
            
            drawVelocityNumbers( speedUnits, maxVelocityTexture.getTexture(), -Math.round( maxVelocityLeftOffset.getFloatValue() * getBackground().getScaleX() ), -Math.round( maxVelocityTopOffset.getFloatValue() * getBackground().getScaleY() ) );
            
            maxVelocityTexture.setTranslation( maxVelocityLeftOffset.getFloatValue() * getBackground().getScaleX(), maxVelocityTopOffset.getFloatValue() * getBackground().getScaleY() );
            maxVelocityTexture.setLocalZIndex( MAX_VELOCITY_LOCAL_Z_INDEX );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( false );
        }
        
        return ( true );
    }
    
    @Override
    protected void initSubTextures( LiveGameData gameData, boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight, SubTextureCollector collector )
    {
        super.initSubTextures( gameData, isEditorMode, widgetInnerWidth, widgetInnerHeight, collector );
        
        if ( loadMaxVelocityTexture( gameData.getProfileInfo().getSpeedUnits(), isEditorMode ) )
            collector.add( maxVelocityTexture );
        
        int cw = Math.round( controlsWidth.getFloatValue() * getBackground().getScaleX() );
        int ch = Math.round( controlsHeight.getFloatValue() * getBackground().getScaleY() );
        
        loadThrottleTexture( isEditorMode, cw, ch );
        collector.add( texThrottle1 );
        collector.add( texThrottle2 );
        
        loadBrakeTexture( isEditorMode, cw, ch );
        collector.add( texBrake1 );
        collector.add( texBrake2 );
    }
    
    @Override
    protected void drawBackground( LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        super.drawBackground( gameData, isEditorMode, texture, offsetX, offsetY, width, height, isRoot );
        
        drawVelocityNumbers( gameData.getProfileInfo().getSpeedUnits(), texture, offsetX, offsetY );
    }
    
    @Override
    protected void drawWidget( Clock clock, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        super.drawWidget( clock, needsCompleteRedraw, gameData, isEditorMode, texture, offsetX, offsetY, width, height );
        
        TelemetryData telemData = gameData.getTelemetryData();
        
        if ( maxVelocityTexture != null )
        {
            float normVelo = Math.min( telemData.getScalarVelocityKPH(), maxVelocity.getFloatValue() ) / maxVelocity.getFloatValue();
            int h = Math.round( maxVelocityTexture.getHeight() * normVelo );
            
            maxVelocityTexture.setClipRect( 0, maxVelocityTexture.getHeight() - h, maxVelocityTexture.getWidth(), h, false );
        }
        
        float uBrake = isEditorMode ? 0.2f : telemData.getUnfilteredBrake();
        float uThrottle = isEditorMode ? 0.4f : telemData.getUnfilteredThrottle();
        
        final int w = texThrottle2.getWidth();
        int brake = (int)( w * uBrake );
        int throttle = (int)( w * uThrottle );
        
        texThrottle2.setClipRect( 0, 0, throttle, texThrottle2.getHeight(), true );
        texBrake2.setClipRect( 0, 0, brake, texBrake2.getHeight(), true );
    }
    
    @Override
    protected void saveShiftLightsProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        // We don't need these here!
    }
    
    @Override
    protected void saveBoostProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        // We don't need these here!
    }
    
    @Override
    protected void saveDigiValueProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        // We don't need these here!
    }
    
    @Override
    protected void saveDigiRevsProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        // We don't need these here!
    }
    
    @Override
    protected void getShiftLightsProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        // We don't need these here!
    }
    
    @Override
    protected void getBoostProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        // We don't need these here!
    }
    
    @Override
    protected void getDigiValueProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        // We don't need these here!
    }
    
    @Override
    protected void getDigiRevsProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        // We don't need these here!
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( velocityNumberFont, "The font for the velocity numbers." );
        writer.writeProperty( velocityNumberFontColor, "The font color for the velocity numbers." );
        
        writer.writeProperty( velocityNumber1PosX, "The x-position in background texture space for the first velocity number." );
        writer.writeProperty( velocityNumber1PosY, "The y-position in background texture space for the first velocity number." );
        writer.writeProperty( velocity2, "The second velocity." );
        writer.writeProperty( velocityNumber2PosX, "The x-position in background texture space for the second velocity number." );
        writer.writeProperty( velocity3, "The third velocity." );
        writer.writeProperty( velocityNumber3PosX, "The x-position in background texture space for the third velocity number." );
        writer.writeProperty( velocityNumber4PosX, "The x-position in background texture space for the fourth velocity number." );
        writer.writeProperty( velocityNumber4PosY, "The y-position in background texture space for the fourth velocity number." );
        writer.writeProperty( velocityUnitsPosX, "The x-position in background texture space for te units display." );
        writer.writeProperty( velocityUnitsPosY, "The y-position in background texture space for te units display." );
        
        writer.writeProperty( maxVelocity, "The maximum velocity in km/h." );
        writer.writeProperty( maxVelocityOverlay, "The image name for the max velocity overlay." );
        writer.writeProperty( maxVelocityLeftOffset, "The x-offset in background image space for the max velocity overlay." );
        writer.writeProperty( maxVelocityTopOffset, "The y-offset in background image space for the max velocity overlay." );
        
        writer.writeProperty( throttleImage, "The image for the throttle gauge." );
        writer.writeProperty( brakeImage, "The image for the brake gauge." );
        writer.writeProperty( controlsPosX, "The x-offset in background image space for the controls display." );
        writer.writeProperty( controlsPosY, "The y-offset in background image space for the controls display." );
        writer.writeProperty( controlsWidth, "The width in background image space for the controls display." );
        writer.writeProperty( controlsHeight, "The height in background image space for the controls display." );
        writer.writeProperty( controlsGap, "The gap in pixels between the throttle and brake bars." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( velocityNumberFont ) );
        else if ( loader.loadProperty( velocityNumberFont ) );
        else if ( loader.loadProperty( velocityNumber1PosX ) );
        else if ( loader.loadProperty( velocityNumber1PosY ) );
        else if ( loader.loadProperty( velocity2 ) );
        else if ( loader.loadProperty( velocityNumber2PosX ) );
        else if ( loader.loadProperty( velocity3 ) );
        else if ( loader.loadProperty( velocityNumber3PosX ) );
        else if ( loader.loadProperty( velocityNumber4PosX ) );
        else if ( loader.loadProperty( velocityNumber4PosY ) );
        else if ( loader.loadProperty( velocityUnitsPosX ) );
        else if ( loader.loadProperty( velocityUnitsPosY ) );
        
        else if ( loader.loadProperty( maxVelocity ) );
        else if ( loader.loadProperty( maxVelocityOverlay ) );
        else if ( loader.loadProperty( maxVelocityLeftOffset ) );
        else if ( loader.loadProperty( maxVelocityTopOffset ) );
        
        else if ( loader.loadProperty( throttleImage ) );
        else if ( loader.loadProperty( brakeImage ) );
        else if ( loader.loadProperty( controlsPosX ) );
        else if ( loader.loadProperty( controlsPosY ) );
        else if ( loader.loadProperty( controlsWidth ) );
        else if ( loader.loadProperty( controlsHeight ) );
        else if ( loader.loadProperty( controlsGap ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Velocity Numbers" );
        
        propsCont.addProperty( velocityNumberFont );
        propsCont.addProperty( velocityNumberFontColor );
        propsCont.addProperty( velocityNumber1PosX );
        propsCont.addProperty( velocityNumber1PosY );
        propsCont.addProperty( velocity2 );
        propsCont.addProperty( velocityNumber2PosX );
        propsCont.addProperty( velocity3 );
        propsCont.addProperty( velocityNumber3PosX );
        propsCont.addProperty( velocityNumber4PosX );
        propsCont.addProperty( velocityNumber4PosY );
        propsCont.addProperty( velocityUnitsPosX );
        propsCont.addProperty( velocityUnitsPosY );
        
        propsCont.addGroup( "Max Velocity Overlay" );
        
        propsCont.addProperty( maxVelocity );
        propsCont.addProperty( maxVelocityOverlay );
        propsCont.addProperty( maxVelocityLeftOffset );
        propsCont.addProperty( maxVelocityTopOffset );
        
        propsCont.addGroup( "Controls" );
        
        propsCont.addProperty( throttleImage );
        propsCont.addProperty( brakeImage );
        propsCont.addProperty( controlsPosX );
        propsCont.addProperty( controlsPosY );
        propsCont.addProperty( controlsWidth );
        propsCont.addProperty( controlsHeight );
        propsCont.addProperty( controlsGap );
        
    }
    
    @Override
    protected int getInitialNumberOfShiftLights()
    {
        return ( 0 );
    }
    
    public ETVTelemetryWidget()
    {
        super( 19.6915f, 21.75f );
        
        minValue.setFloatValue( 4000 );
        
        displayMarkers.setBooleanValue( false );
        markersInnerRadius.setIntValue( 170 );
        markersLength.setIntValue( 50 );
        markersOnCircle.setBooleanValue( true );
        markersBigStep.setIntValue( 2000 );
        markersSmallStep.setIntValue( 1000 );
        markersFont.setFont( ETVUtils.ETV_REV_MARKERS_FONT );
        markersFontColor.setColor( "#FFFFFF" );
        markersFontDropShadowColor.setColor( "#000000" );
        markerNumbersCentered.setBooleanValue( true );
        
        needlePivotBottomOffset.setIntValue( -154 );
        peakNeedlePivotBottomOffset.setIntValue( -216 );
        
        needleMountX.setIntValue( 495 );
        needleMountY.setIntValue( 365 );
        
        needleRotationForMinValue.setFloatValue( -180 );
        needleRotationForMaxValue.setFloatValue( +73 );
        
        displayValue.setBooleanValue( false );
        displayGear.setBooleanValue( true );
        gearPosX.setIntValue( 497 );
        gearPosY.setIntValue( 345 );
        gearFont.setFont( ETVUtils.ETV_GEAR_FONT );
        gearFontColor.setColor( "#D9E0EB" );
        
        displayBoostBar.setBooleanValue( false );
        displayBoostNumber.setBooleanValue( false );
        
        displayRPMString1.setBooleanValue( false );
        displayRPMString2.setBooleanValue( false );
    }
}
