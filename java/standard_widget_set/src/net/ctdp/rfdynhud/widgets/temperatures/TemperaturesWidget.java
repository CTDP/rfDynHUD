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
package net.ctdp.rfdynhud.widgets.temperatures;

import java.awt.Color;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound.CompoundWheel;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.WheelPart;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.DelayProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.ByteOrderManager;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * The {@link TemperaturesWidget} displays temperature information for the engine, tires and brakes.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TemperaturesWidget extends Widget
{
    private final FontProperty font2 = new FontProperty( this, "font2", FontProperty.SMALLER_FONT_NAME );
    
    private final BooleanProperty displayEngine = new BooleanProperty( this, "displayEngine", true );
    private final BooleanProperty displayWaterTemp = new BooleanProperty( this, "displayWaterTemp", true );
    private final BooleanProperty displayTires = new BooleanProperty( this, "displayTires", true );
    private final BooleanProperty displayBrakes = new BooleanProperty( this, "displayBrakes", true );
    
    private final Size engineHeight = Size.newLocalSize( this, 0f, true, 10.0f, true );
    
    private final Size tireSize = Size.newLocalSize( this, 10.0f, true, 10.0f, true );
    private final Size brakeSize = Size.newLocalSize( this, 7.0f, true, 20.0f, true );
    
    private final DelayProperty brakeTempsPeekDelay = new DelayProperty( this, "brakeTempsPeekDelay", DelayProperty.DisplayUnits.MILLISECONDS, 7000, 0, 20000 );
    
    private DrawnString engineHeaderString = null;
    private DrawnString engineWaterTempString = null;
    private DrawnString engineOilTempString = null;
    
    private DrawnString tiresHeaderString = null;
    private DrawnString tireTempFLString = null;
    private DrawnString tireTempFRString = null;
    private DrawnString tireTempRLString = null;
    private DrawnString tireTempRRString = null;
    private DrawnString tireTempFLString2 = null;
    private DrawnString tireTempFRString2 = null;
    private DrawnString tireTempRLString2 = null;
    private DrawnString tireTempRRString2 = null;
    
    private DrawnString brakesHeaderString = null;
    private DrawnString brakeTempFLString = null;
    private DrawnString brakeTempFRString = null;
    private DrawnString brakeTempRLString = null;
    private DrawnString brakeTempRRString = null;
    
    private static final byte[] colorCold = new byte[ 4 ];
    private static final byte[] colorOpt = new byte[ 4 ];
    private static final byte[] colorHot = new byte[ 4 ];
    static
    {
        colorCold[ByteOrderManager.RED] = (byte)0;
        colorCold[ByteOrderManager.GREEN] = (byte)0;
        colorCold[ByteOrderManager.BLUE] = (byte)255;
        colorCold[ByteOrderManager.ALPHA] = (byte)255;
        
        colorOpt[ByteOrderManager.RED] = (byte)255;
        colorOpt[ByteOrderManager.GREEN] = (byte)255;
        colorOpt[ByteOrderManager.BLUE] = (byte)0;
        colorOpt[ByteOrderManager.ALPHA] = (byte)255;
        
        colorHot[ByteOrderManager.RED] = (byte)255;
        colorHot[ByteOrderManager.GREEN] = (byte)0;
        colorHot[ByteOrderManager.BLUE] = (byte)0;
        colorHot[ByteOrderManager.ALPHA] = (byte)255;
    }
    
    private int oldWaterTemp = -1;
    private int oldOilTemp = -1;
    private int[] oldTireTemps = { -1, -1, -1, -1 };
    private int[] oldBrakeTemps = { -1, -1, -1, -1 };
    private long lastBrakeTempTime = -1L;
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        engineHeight.bake();
        tireSize.bake();
        brakeSize.bake();
    }
    
    @Override
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        engineHeight.setWidthToPercents();
        engineHeight.setHeightToPercents();
        
        tireSize.setWidthToPercents();
        tireSize.setHeightToPercents();
        
        brakeSize.setWidthToPercents();
        brakeSize.setHeightToPercents();
    }
    
    @Override
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        engineHeight.setWidthToPixels();
        engineHeight.setHeightToPixels();
        
        tireSize.setWidthToPixels();
        tireSize.setHeightToPixels();
        
        brakeSize.setWidthToPixels();
        brakeSize.setHeightToPixels();
    }
    
    @Override
    public int getNeededData()
    {
        return ( Widget.NEEDED_DATA_SCORING );
    }
    
    private void setControlVisibility( VehicleScoringInfo viewedVSI )
    {
        setUserVisible1( viewedVSI.isPlayer() && viewedVSI.getVehicleControl().isLocalPlayer() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, isEditorMode );
        
        setControlVisibility( gameData.getScoringInfo().getViewedVehicleScoringInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onRealtimeEntered( gameData, isEditorMode );
        
        oldWaterTemp = -1;
        oldOilTemp = -1;
        
        for ( int i = 0; i < oldTireTemps.length; i++ )
            oldTireTemps[i] = -1;
        
        for ( int i = 0; i < oldBrakeTemps.length; i++ )
            oldBrakeTemps[i] = -1;
        
        lastBrakeTempTime = -1L;
        
        //forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        
        setControlVisibility( viewedVSI );
    }
    
    private static final String getTempUnits( MeasurementUnits measurementUnits )
    {
        if ( measurementUnits == MeasurementUnits.IMPERIAL )
            return ( Loc.temperature_units_IMPERIAL );
        
        return ( Loc.temperature_units_METRIC );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(boolean clock1, boolean clock2, LiveGameData gameData, boolean isEditorMode, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Font font = getFont();
        final boolean fontAntiAliased = isFontAntiAliased();
        final java.awt.Font font2 = this.font2.getFont();
        final boolean font2AntiAliased = this.font2.isAntiAliased();
        final java.awt.Color fontColor = getFontColor();
        
        final int tireWidth = tireSize.getEffectiveWidth();
        final int tireHeight = tireSize.getEffectiveHeight();
        final int brakeWidth = brakeSize.getEffectiveWidth();
        final int brakeHeight = brakeSize.getEffectiveHeight();
        
        int left = 2;
        int center = width / 2;
        int top = -2;
        DrawnString relY = null;
        
        final MeasurementUnits measurementUnits = gameData.getProfileInfo().getMeasurementUnits();
        
        if ( displayEngine.getBooleanValue() )
        {
            engineHeaderString = dsf.newDrawnString( "engineHeaderString", left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.engine_header_prefix + ": ", null );
            if ( displayWaterTemp.getBooleanValue() )
            {
                engineWaterTempString = dsf.newDrawnString( "engineWaterTempString", null, engineHeaderString, width, 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(" + Loc.engine_watertemp_prefix + ":", getTempUnits( measurementUnits ) + ")" );
                engineOilTempString = dsf.newDrawnString( "engineOilTempString", null, engineWaterTempString, width, 2, Alignment.RIGHT, false, font, font2AntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            }
            else
            {
                engineWaterTempString = null;
                engineOilTempString = dsf.newDrawnString( "engineOilTempString", null, engineHeaderString, width, 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            }
            
            relY = engineHeaderString;
            top = engineHeight.getEffectiveHeight() + 10;
        }
        
        int imgWidth = displayTires.getBooleanValue() && displayBrakes.getBooleanValue() ? Math.max( tireWidth, brakeWidth ) : ( displayTires.getBooleanValue() ? tireWidth : brakeWidth );
        
        if ( displayTires.getBooleanValue() )
        {
            tiresHeaderString = dsf.newDrawnString( "tiresHeaderString", null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Tires: ", null );
            tireTempFLString = dsf.newDrawnString( "tireTempFLString", null, tiresHeaderString, center - 7 - imgWidth, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            tireTempFRString = dsf.newDrawnString( "tireTempFRString", null, tiresHeaderString, center + 7 + imgWidth, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            tireTempRLString = dsf.newDrawnString( "tireTempRLString", null, tiresHeaderString, center - 7 - imgWidth, 0 + tireHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            tireTempRRString = dsf.newDrawnString( "tireTempRRString", null, tiresHeaderString, center + 7 + imgWidth, 0 + tireHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            tireTempFLString2 = dsf.newDrawnString( "tireTempFLString2", null, tiresHeaderString, center - 7 - imgWidth, 0 + tireHeight - 2, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", getTempUnits( measurementUnits ) + ")" );
            tireTempFRString2 = dsf.newDrawnString( "tireTempFRString2", null, tiresHeaderString, center + 7 + imgWidth, 0 + tireHeight - 2, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", getTempUnits( measurementUnits ) + ")" );
            tireTempRLString2 = dsf.newDrawnString( "tireTempRLString2", null, tiresHeaderString, center - 7 - imgWidth, 0 + tireHeight - 2 + tireHeight + 7, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", getTempUnits( measurementUnits ) + ")" );
            tireTempRRString2 = dsf.newDrawnString( "tireTempRRString2", null, tiresHeaderString, center + 7 + imgWidth, 0 + tireHeight - 2 + tireHeight + 7, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", getTempUnits( measurementUnits ) + ")" );
            
            relY = tiresHeaderString;
            top = tireHeight * 2 + 15;
        }
        
        if ( displayBrakes.getBooleanValue() )
        {
            brakesHeaderString = dsf.newDrawnString( "brakesHeaderString", null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            brakeTempFLString = dsf.newDrawnString( "brakeTempFLString", null, brakesHeaderString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            brakeTempFRString = dsf.newDrawnString( "brakeTempFRString", null, brakesHeaderString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            brakeTempRLString = dsf.newDrawnString( "brakeTempRLString", null, brakesHeaderString, center - 7 - imgWidth, 2 + brakeHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
            brakeTempRRString = dsf.newDrawnString( "brakeTempRRString", null, brakesHeaderString, center + 7 + imgWidth, 2 + brakeHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, getTempUnits( measurementUnits ) );
        }
    }
    
    private static void interpolateColor( byte[] color0, byte[] color1, float alpha, byte[] result )
    {
        final float beta = 1f - alpha;
        
        result[ByteOrderManager.RED] = (byte)( (float)( color0[ByteOrderManager.RED] & 0xFF ) * beta + (float)( color1[ByteOrderManager.RED] & 0xFF ) * alpha );
        result[ByteOrderManager.GREEN] = (byte)( (float)( color0[ByteOrderManager.GREEN] & 0xFF ) * beta + (float)( color1[ByteOrderManager.GREEN] & 0xFF ) * alpha );
        result[ByteOrderManager.BLUE] = (byte)( (float)( color0[ByteOrderManager.BLUE] & 0xFF ) * beta + (float)( color1[ByteOrderManager.BLUE] & 0xFF ) * alpha );
    }
    
    private void drawEngine( float temp, VehiclePhysics.Engine engine, TextureImage2D texture, int x, int y, int width )
    {
        final int w = width;
        final int h = engineHeight.getEffectiveHeight();
        
        final float optimum = engine.getOptimumOilTemperature();
        final float lowerOptimum = optimum - 15f;
        final float overheating = engine.getOverheatingOilTemperature();
        final float upperOptimum = optimum + ( overheating - optimum ) / 3f;
        final float strongOverheating = engine.getStrongOverheatingOilTemperature();
        
        final float min = optimum - 15f;
        final float max = strongOverheating + ( strongOverheating - overheating ) * 3f;
        final float range = max - min;
        final float dispTemp = Math.max( min, Math.min( temp, max ) );
        
        byte[] color = new byte[4];
        color[ByteOrderManager.ALPHA] = (byte)255;
        if ( temp < lowerOptimum )
        {
            System.arraycopy( colorCold, 0, color, 0, 3 );
        }
        else if ( temp < optimum )
        {
            //float alpha = ( dispTemp - min ) / ( optimum - min );
            float alpha = ( dispTemp - lowerOptimum ) / ( optimum - lowerOptimum );
            interpolateColor( colorCold, colorOpt, alpha, color );
        }
        else if ( temp < overheating )
        {
            float alpha = ( dispTemp - optimum ) / ( overheating - optimum );
            interpolateColor( colorOpt, colorHot, alpha, color );
        }
        else// if ( temp >= overheating )
        {
            System.arraycopy( colorHot, 0, color, 0, 3 );
        }
        
        int barWidth = Math.min( (int)( w * ( dispTemp - min ) / range ), w );
        
        if ( temp < max )
        {
            texture.clear( Color.BLACK, x + barWidth, y, w - barWidth, h, false, null );
        }
        
        Color awtColor = new Color( color[ByteOrderManager.RED] & 0xFF, color[ByteOrderManager.GREEN] & 0xFF, color[ByteOrderManager.BLUE] & 0xFF );
        texture.clear( awtColor, x, y, barWidth, h, false, null );
        
        texture.getTextureCanvas().setColor( Color.GREEN );
        texture.getTextureCanvas().drawLine( x + (int)( ( optimum - min ) * w / range ), y, x + (int)( ( optimum - min ) * w / range ), y + h - 1 );
        
        texture.getTextureCanvas().setColor( Color.DARK_GRAY );
        texture.getTextureCanvas().drawLine( x + (int)( ( upperOptimum - min ) * w / range ), y, x + (int)( ( upperOptimum - min ) * w / range ), y + h - 1 );
        
        texture.getTextureCanvas().setColor( Color.YELLOW );
        texture.getTextureCanvas().drawLine( x + (int)( ( overheating - min ) * w / range ), y, x + (int)( ( overheating - min ) * w / range ), y + h - 1 );
        
        texture.getTextureCanvas().setColor( new Color( 255, 102, 0 ) );
        texture.getTextureCanvas().drawLine( x + (int)( ( strongOverheating - min ) * w / range ), y, x + (int)( ( strongOverheating - min ) * w / range ), y + h - 1 );
        
        texture.markDirty( x, y, w, h );
    }
    
    private byte[] getTireColor( float actualTemp, float optimumTemp, float coldTemp, float overheatingTemp )
    {
        if ( actualTemp == optimumTemp )
            return ( colorOpt );
        
        if ( actualTemp <= coldTemp )
            return ( colorCold );
        
        if ( actualTemp >= overheatingTemp )
            return ( colorHot );
        
        byte[] color = new byte[4];
        color[ByteOrderManager.ALPHA] = (byte)255;
        
        if ( actualTemp < optimumTemp )
        {
            float alpha = ( actualTemp - coldTemp ) / ( optimumTemp - coldTemp );
            
            interpolateColor( colorCold, colorOpt, alpha, color );
        }
        else// if ( actualTemp > optimumTemp )
        {
            float alpha = ( overheatingTemp - actualTemp ) / ( overheatingTemp - optimumTemp );
            
            interpolateColor( colorHot, colorOpt, alpha, color );
        }
        
        return ( color );
    }
    
    private void drawTire( TelemetryData telemData, Wheel wheel, CompoundWheel compoundWheel, TextureImage2D texture, int x, int y )
    {
        WheelPart wpLeft = WheelPart.getLeftPart( wheel );
        WheelPart wpRight = WheelPart.getRightPart( wheel );
        
        final float optimumTemp = compoundWheel.getOptimumTemperature();
        final float coldTemp = compoundWheel.getBelowTemperature( 0.33f );
        final float overheatingTemp = compoundWheel.getAboveTemperature( 0.33f );
        
        float tempLeft = telemData.getTireTemperature( wheel, wpLeft );
        float tempCenter = telemData.getTireTemperature( wheel, WheelPart.CENTER );
        float tempRight = telemData.getTireTemperature( wheel, wpRight );
        byte[] colorLeft = getTireColor( tempLeft, optimumTemp, coldTemp, overheatingTemp );
        byte[] colorCenter = getTireColor( tempCenter, optimumTemp, coldTemp, overheatingTemp );
        byte[] colorRight = getTireColor( tempRight, optimumTemp, coldTemp, overheatingTemp );
        
        byte[] color = new byte[4];
        color[ByteOrderManager.ALPHA] = (byte)255;
        
        int w = tireSize.getEffectiveWidth();
        int hw = w / 2;
        int h = tireSize.getEffectiveHeight();
        int hh = h / 2;
        
        texture.markDirty( x, y, w, h );
        
        byte[] pixels = new byte[ w * 4 ];
        
        for ( int i = 0; i < hw; i++ )
        {
            interpolateColor( colorLeft, colorCenter, (float)i / (float)hw, color );
            System.arraycopy( color, 0, pixels, i * 4, 4 );
        }
        
        for ( int i = hw; i < w; i++ )
        {
            interpolateColor( colorCenter, colorRight, (float)( i - hw ) / (float)hw, color );
            System.arraycopy( color, 0, pixels, i * 4, 4 );
        }
        
        for ( int j = 0; j < hh; j++ )
        {
            texture.clearPixelLine( pixels, x, y + j, w, false, null );
        }
        
        for ( int j = hh + 1; j < h; j++ )
        {
            texture.clearPixelLine( pixels, x, y + j, w, false, null );
        }
        
        color[0] = (byte)0;
        color[1] = (byte)255;
        color[2] = (byte)0;
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( color, 0, pixels, i * 4, 4 );
        }
        
        texture.clearPixelLine( pixels, x, y + hh, w, false, null );
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        final float coldTemp2 = compoundWheel.getBelowTemperature( 0.5f );
        final float overheatingTemp2 = compoundWheel.getAboveTemperature( 0.5f );
        
        int[] xPoints = { x, x + hw, x + w };
        int[] yPoints = new int[ 3 ];
        if ( tempLeft >= optimumTemp )
            yPoints[0] = y + hh - (int)( ( tempLeft - optimumTemp ) * hh / ( overheatingTemp2 - optimumTemp ) );
        else
            yPoints[0] = y + hh + (int)( ( optimumTemp - tempLeft ) * hh / ( optimumTemp - coldTemp2 ) );
        if ( tempCenter >= optimumTemp )
            yPoints[1] = y + hh - (int)( ( tempCenter - optimumTemp ) * hh / ( overheatingTemp2 - optimumTemp ) );
        else
            yPoints[1] = y + hh + (int)( ( optimumTemp - tempCenter ) * hh / ( optimumTemp - coldTemp2 ) );
        if ( tempRight >= optimumTemp )
            yPoints[2] = y + hh - (int)( ( tempRight - optimumTemp ) * hh / ( overheatingTemp2 - optimumTemp ) );
        else
            yPoints[2] = y + hh + (int)( ( optimumTemp - tempRight ) * hh / ( optimumTemp - coldTemp2 ) );
        
        for ( int i = 0; i < 3; i++ )
        {
            yPoints[i] = Math.min( y + h - 1, Math.max( yPoints[i], y ) );
        }
        
        texCanvas.setColor( java.awt.Color.BLACK );
        boolean wasAntialiasingEnabled = texCanvas.isAntialiazingEnabled();
        texCanvas.setAntialiazingEnabled( true );
        texCanvas.drawPolyline( xPoints, yPoints, 3 );
        texCanvas.setAntialiazingEnabled( wasAntialiasingEnabled );
    }
    
    private void drawBrake( float temp, VehiclePhysics.Brakes.WheelBrake brake, TextureImage2D texture, int x, int y )
    {
        int w = brakeSize.getEffectiveWidth();
        int h = brakeSize.getEffectiveHeight();
        
        final float range = brake.getOverheatingTemperature() + 200f;
        
        byte[] color = new byte[4];
        color[ByteOrderManager.ALPHA] = (byte)255;
        if ( temp <= brake.getColdTemperature() )
        {
            System.arraycopy( colorCold, 0, color, 0, 3 );
        }
        else if ( temp < brake.getOptimumTemperaturesLowerBound() )
        {
            float alpha = ( temp - brake.getColdTemperature() ) / ( brake.getOptimumTemperaturesLowerBound() - brake.getColdTemperature() );
            interpolateColor( colorCold, colorOpt, alpha, color );
        }
        else if ( temp <= brake.getOptimumTemperaturesUpperBound() )
        {
            System.arraycopy( colorOpt, 0, color, 0, 3 );
        }
        else if ( temp < brake.getOverheatingTemperature() )
        {
            float alpha = ( temp - brake.getOptimumTemperaturesUpperBound() ) / ( brake.getOverheatingTemperature() - brake.getOptimumTemperaturesUpperBound() );
            interpolateColor( colorOpt, colorHot, alpha, color );
        }
        else// if ( temp >= brakes.getOverheatingTemperature() )
        {
            System.arraycopy( colorHot, 0, color, 0, 3 );
        }
        
        Color awtColor = new Color( color[ByteOrderManager.RED] & 0xFF, color[ByteOrderManager.GREEN] & 0xFF, color[ByteOrderManager.BLUE] & 0xFF );
        
        int barHeight = Math.min( (int)( h * temp / range ), h );
        
        if ( temp < range )
        {
            texture.clear( Color.BLACK, x, y, w, h - barHeight, false, null );
        }
        
        texture.clear( awtColor, x, y + h - barHeight, w, barHeight, false, null );
        
        byte[] pixels = new byte[ w * 4 ];
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( colorHot, 0, pixels, i * 4, 4 );
        }
        
        texture.clearPixelLine( pixels, x, y + (int)( ( range - brake.getOverheatingTemperature() ) * h / range ), w, false, null );
        
        for ( int i = 0; i < w; i++ )
        {
            //System.arraycopy( colorOpt, 0, pixels, i * 4, 4 );
            pixels[i * 4 + ByteOrderManager.RED] = (byte)0;
            pixels[i * 4 + ByteOrderManager.GREEN] = (byte)255;
            pixels[i * 4 + ByteOrderManager.BLUE] = (byte)0;
            pixels[i * 4 + ByteOrderManager.ALPHA] = (byte)255;
        }
        
        texture.clearPixelLine( pixels, x, y + (int)( ( range - brake.getOptimumTemperaturesUpperBound() ) * h / range ), w, false, null );
        texture.clearPixelLine( pixels, x, y + (int)( ( range - brake.getOptimumTemperaturesLowerBound() ) * h / range ), w, false, null );
        
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( colorCold, 0, pixels, i * 4, 4 );
        }
        
        texture.clearPixelLine( pixels, x, y + (int)( ( range - brake.getColdTemperature() ) * h / range ), w, false, null );
        
        texture.markDirty( x, y, w, h );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, boolean isEditorMode, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final TelemetryData telemData = gameData.getTelemetryData();
        final VehiclePhysics physics = gameData.getPhysics();
        final VehicleSetup setup = gameData.getSetup();
        
        if ( needsCompleteRedraw )
        {
            if ( displayEngine.getBooleanValue() )
                engineHeaderString.draw( offsetX, offsetY, "(" + NumberUtil.formatFloat( gameData.getPhysics().getEngine().getOptimumOilTemperature(), 1, true ) + getTempUnits( gameData.getProfileInfo().getMeasurementUnits() ) + ")", texture );
            
            // TODO: Try to find a way to detect tire compound change on pit stop!
            TireCompound tireCompound = setup.getGeneral().getFrontTireCompound();
            if ( displayTires.getBooleanValue() )
                tiresHeaderString.draw( offsetX, offsetY, tireCompound.getName() + " (" + NumberUtil.formatFloat( tireCompound.getWheel( Wheel.FRONT_LEFT ).getOptimumTemperature(), 1, true ) + getTempUnits( gameData.getProfileInfo().getMeasurementUnits() ) + ")", texture );
            if ( displayBrakes.getBooleanValue() )
                brakesHeaderString.draw( offsetX, offsetY, Loc.brakes_header_prefix + ":", texture );
        }
        
        if ( displayEngine.getBooleanValue() )
        {
            if ( displayWaterTemp.getBooleanValue() )
            {
                int waterTemp = Math.round( telemData.getEngineWaterTemperature() * 10f );
                if ( needsCompleteRedraw || ( clock1 && ( waterTemp != oldWaterTemp ) ) )
                {
                    oldWaterTemp = waterTemp;
                    
                    String string = String.valueOf( waterTemp / 10f );
                    engineWaterTempString.draw( offsetX, offsetY, string, texture );
                }
            }
            
            int oilTemp = Math.round( telemData.getEngineOilTemperature() * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( oilTemp != oldOilTemp ) ) )
            {
                oldOilTemp = oilTemp;
                
                String string = String.valueOf( oilTemp / 10f );
                engineOilTempString.draw( offsetX, offsetY, string, texture );
                
                int engineWidth;
                if ( displayWaterTemp.getBooleanValue() )
                    engineWidth = Math.max( 70, Math.max( engineWaterTempString.getLastWidth(), engineOilTempString.getLastWidth() ) );
                else
                    engineWidth = Math.max( 70, engineOilTempString.getLastWidth() );
                engineWidth = engineOilTempString.getAbsX() - engineWidth - 5;
                
                int engineTop = ( engineWaterTempString != null ) ? engineWaterTempString.getAbsY() + 3 : engineOilTempString.getAbsY() + 3;
                
                drawEngine( oilTemp / 10f, physics.getEngine(), texture, offsetX + engineHeaderString.getAbsX(), offsetY + engineTop, engineWidth );
            }
        }
        
        if ( displayTires.getBooleanValue() )
        {
            final int tireWidth = tireSize.getEffectiveWidth();
            
            int tireTempFL = Math.round( telemData.getTireTemperature( Wheel.FRONT_LEFT ) * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( tireTempFL != oldTireTemps[0] ) ) )
            {
                oldTireTemps[0] = tireTempFL;
                
                CompoundWheel wheel = setup.getGeneral().getFrontTireCompound().getWheel( Wheel.FRONT_LEFT );
                
                float temp = tireTempFL / 10f;
                String string = String.valueOf( temp );
                tireTempFLString.draw( offsetX, offsetY, string, texture );
                float diff = Math.round( tireTempFL - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempFLString2.draw( offsetX, offsetY, string, texture );
                
                drawTire( telemData, Wheel.FRONT_LEFT, wheel, texture, offsetX + tireTempFLString.getAbsX() + 3, offsetY + tireTempFLString.getAbsY() + 2 );
            }
            
            int tireTempFR = Math.round( telemData.getTireTemperature( Wheel.FRONT_RIGHT ) * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( tireTempFR != oldTireTemps[1] ) ) )
            {
                oldTireTemps[1] = tireTempFR;
                
                CompoundWheel wheel = setup.getGeneral().getFrontTireCompound().getWheel( Wheel.FRONT_RIGHT );
                
                float temp = tireTempFR / 10f;
                String string = String.valueOf( temp );
                tireTempFRString.draw( offsetX, offsetY, string, texture );
                float diff = Math.round( tireTempFR - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempFRString2.draw( offsetX, offsetY, string, texture );
                
                drawTire( telemData, Wheel.FRONT_RIGHT, wheel, texture, offsetX + tireTempFRString.getAbsX() - tireWidth - 3, offsetY + tireTempFRString.getAbsY() + 2 );
            }
            
            int tireTempRL = Math.round( telemData.getTireTemperature( Wheel.REAR_LEFT ) * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( tireTempRL != oldTireTemps[2] ) ) )
            {
                oldTireTemps[2] = tireTempRL;
                
                CompoundWheel wheel = setup.getGeneral().getRearTireCompound().getWheel( Wheel.REAR_LEFT );
                
                float temp = tireTempRL / 10f;
                String string = String.valueOf( temp );
                tireTempRLString.draw( offsetX, offsetY, string, texture );
                float diff = Math.round( tireTempRL - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempRLString2.draw( offsetX, offsetY, string, texture );
                
                drawTire( telemData, Wheel.REAR_LEFT, wheel, texture, offsetX + tireTempRLString.getAbsX() + 3, offsetY + tireTempRLString.getAbsY() + 2 );
            }
            
            int tireTempRR = Math.round( telemData.getTireTemperature( Wheel.REAR_RIGHT ) * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( tireTempRR != oldTireTemps[3] ) ) )
            {
                oldTireTemps[3] = tireTempRR;
                
                CompoundWheel wheel = setup.getGeneral().getRearTireCompound().getWheel( Wheel.REAR_RIGHT );
                
                float temp = tireTempRR / 10f;
                String string = String.valueOf( temp );
                tireTempRRString.draw( offsetX, offsetY, string, texture );
                float diff = Math.round( tireTempRR - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempRRString2.draw( offsetX, offsetY, string, texture );
                
                drawTire( telemData, Wheel.REAR_RIGHT, wheel, texture, offsetX + tireTempRRString.getAbsX() - tireWidth - 3, offsetY + tireTempRRString.getAbsY() + 2 );
            }
        }
        
        if ( displayBrakes.getBooleanValue() )
        {
            final int brakeWidth = brakeSize.getEffectiveWidth();
            
            boolean brakesUpdateAllowed = false;
            int brakeTempFL = Math.round( telemData.getBrakeTemperature( Wheel.FRONT_LEFT ) );
            int brakeTempFR = Math.round( telemData.getBrakeTemperature( Wheel.FRONT_RIGHT ) );
            int brakeTempRL = Math.round( telemData.getBrakeTemperature( Wheel.REAR_LEFT ) );
            int brakeTempRR = Math.round( telemData.getBrakeTemperature( Wheel.REAR_RIGHT ) );
            if ( ( brakeTempFL > oldBrakeTemps[0] ) || ( brakeTempFR > oldBrakeTemps[1] ) || ( brakeTempRL > oldBrakeTemps[2] ) || ( brakeTempRR > oldBrakeTemps[3] ) )
            {
                brakesUpdateAllowed = true;
                lastBrakeTempTime = gameData.getScoringInfo().getSessionNanos();
            }
            else if ( lastBrakeTempTime + brakeTempsPeekDelay.getDelay() < gameData.getScoringInfo().getSessionNanos() )
            {
                brakesUpdateAllowed = true;
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempFL != oldBrakeTemps[0] ) ) )
            {
                oldBrakeTemps[0] = brakeTempFL;
                
                String string = String.valueOf( brakeTempFL );
                brakeTempFLString.draw( offsetX, offsetY, string, texture );
                
                drawBrake( brakeTempFL, physics.getBrakes().getBrake( Wheel.FRONT_LEFT ), texture, offsetX + brakeTempFLString.getAbsX() + 3, offsetY + brakeTempFLString.getAbsY() );
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempFR != oldBrakeTemps[1] ) ) )
            {
                oldBrakeTemps[1] = brakeTempFR;
                
                String string = String.valueOf( brakeTempFR );
                brakeTempFRString.draw( offsetX, offsetY, string, texture );
                
                drawBrake( brakeTempFR, physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ), texture, offsetX + brakeTempFRString.getAbsX() - brakeWidth - 3, offsetY + brakeTempFRString.getAbsY() );
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempRL != oldBrakeTemps[2] ) ) )
            {
                oldBrakeTemps[2] = brakeTempRL;
                
                String string = String.valueOf( brakeTempRL );
                brakeTempRLString.draw( offsetX, offsetY, string, texture );
                
                drawBrake( brakeTempRL, physics.getBrakes().getBrake( Wheel.REAR_LEFT ), texture, offsetX + brakeTempRLString.getAbsX() + 3, offsetY + brakeTempRLString.getAbsY() );
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempRR != oldBrakeTemps[3] ) ) )
            {
                oldBrakeTemps[3] = brakeTempRR;
                
                String string = String.valueOf( brakeTempRR );
                brakeTempRRString.draw( offsetX, offsetY, string, texture );
                
                drawBrake( brakeTempRR, physics.getBrakes().getBrake( Wheel.REAR_RIGHT ), texture, offsetX + brakeTempRRString.getAbsX() - brakeWidth - 3, offsetY + brakeTempRRString.getAbsY() );
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( font2, "The used (smaller) font." );
        writer.writeProperty( displayEngine, "Display the engine part of the Widget?" );
        writer.writeProperty( displayWaterTemp, "Display water temperature?" );
        writer.writeProperty( engineHeight.getHeightProperty( "engineHeight" ), "The height of the engine bar." );
        writer.writeProperty( displayTires, "Display the tire part of the Widget?" );
        writer.writeProperty( tireSize.getWidthProperty( "tireWidth" ), "The width of a tire image." );
        writer.writeProperty( tireSize.getHeightProperty( "tireHeight" ), "The height of a tire image." );
        writer.writeProperty( displayBrakes, "Display the brakes of the Widget?" );
        writer.writeProperty( brakeSize.getWidthProperty( "brakeWidth" ), "The width of a brake image." );
        writer.writeProperty( brakeSize.getHeightProperty( "brakeHeight" ), "The height of a brake image." );
        writer.writeProperty( brakeTempsPeekDelay, "(in milliseconds) If greater than 0, the brake temperatures will stay on their peek values after a turn for the chosen amount of milliseconds." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( PropertyLoader loader )
    {
        super.loadProperty( loader );
        
        if ( loader.loadProperty( font2 ) );
        else if ( loader.loadProperty( displayEngine ) );
        else if ( loader.loadProperty( displayWaterTemp ) );
        else if ( loader.loadProperty( engineHeight.getHeightProperty( "engineHeight" ) ) );
        else if ( loader.loadProperty( displayTires ) );
        else if ( loader.loadProperty( tireSize.getWidthProperty( "tireWidth" ) ) );
        else if ( loader.loadProperty( tireSize.getHeightProperty( "tireHeight" ) ) );
        else if ( loader.loadProperty( displayBrakes ) );
        else if ( loader.loadProperty( brakeSize.getWidthProperty( "brakeWidth" ) ) );
        else if ( loader.loadProperty( brakeSize.getHeightProperty( "brakeHeight" ) ) );
        else if ( loader.loadProperty( brakeTempsPeekDelay ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void addFontPropertiesToContainer( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.addFontPropertiesToContainer( propsCont, forceAll );
        
        propsCont.addProperty( font2 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( displayEngine );
        propsCont.addProperty( displayWaterTemp );
        propsCont.addProperty( engineHeight.getHeightProperty( "engineHeight" ) );
        propsCont.addProperty( displayTires );
        propsCont.addProperty( tireSize.getWidthProperty( "tireWidth" ) );
        propsCont.addProperty( tireSize.getHeightProperty( "tireHeight" ) );
        propsCont.addProperty( displayBrakes );
        propsCont.addProperty( brakeSize.getWidthProperty( "brakeWidth" ) );
        propsCont.addProperty( brakeSize.getHeightProperty( "brakeHeight" ) );
        propsCont.addProperty( brakeTempsPeekDelay );
    }
    
    public TemperaturesWidget( String name )
    {
        super( name, 17.8125f, 30.416667f );
    }
}
