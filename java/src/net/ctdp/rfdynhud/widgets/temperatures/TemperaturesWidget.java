package net.ctdp.rfdynhud.widgets.temperatures;

import java.awt.Color;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.editor.properties.BooleanProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.WheelPart;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound.CompoundWheel;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.ByteOrderManager;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link TemperaturesWidget} displays temperature information for the engine, tires and brakes.
 * 
 * @author Marvin Froehlich
 */
public class TemperaturesWidget extends Widget
{
    private final FontProperty font2 = new FontProperty( this, "font2", "SmallerFont" );
    
    private final BooleanProperty displayEngine = new BooleanProperty( this, "displayEngine", true );
    private final BooleanProperty displayWaterTemp = new BooleanProperty( this, "displayWaterTemp", true );
    private final BooleanProperty displayTires = new BooleanProperty( this, "displayTires", true );
    private final BooleanProperty displayBrakes = new BooleanProperty( this, "displayBrakes", true );
    
    private final Size engineHeight;
    
    private final Size tireSize;
    private final Size brakeSize;
    
    private long brakeTempsPeekDelay = 3000000000L; // three seconds
    
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
    public String getWidgetPackage()
    {
        return ( "" );
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
    
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        if ( !engineHeight.isWidthPercentageValue() )
            engineHeight.flipWidthPercentagePx();
        
        if ( !engineHeight.isHeightPercentageValue() )
            engineHeight.flipHeightPercentagePx();
        
        if ( !tireSize.isWidthPercentageValue() )
            tireSize.flipWidthPercentagePx();
        
        if ( !tireSize.isHeightPercentageValue() )
            tireSize.flipHeightPercentagePx();
        
        if ( !brakeSize.isWidthPercentageValue() )
            brakeSize.flipWidthPercentagePx();
        
        if ( !brakeSize.isHeightPercentageValue() )
            brakeSize.flipHeightPercentagePx();
    }
    
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        if ( engineHeight.isWidthPercentageValue() )
            engineHeight.flipWidthPercentagePx();
        
        if ( engineHeight.isHeightPercentageValue() )
            engineHeight.flipHeightPercentagePx();
        
        if ( tireSize.isWidthPercentageValue() )
            tireSize.flipWidthPercentagePx();
        
        if ( tireSize.isHeightPercentageValue() )
            tireSize.flipHeightPercentagePx();
        
        if ( brakeSize.isWidthPercentageValue() )
            brakeSize.flipWidthPercentagePx();
        
        if ( brakeSize.isHeightPercentageValue() )
            brakeSize.flipHeightPercentagePx();
    }
    
    public void setBrakeTempsPeekDelay( int delay )
    {
        this.brakeTempsPeekDelay = delay * 1000000L;
    }
    
    public final int getBrakeTempsPeekDelay()
    {
        return ( (int)( brakeTempsPeekDelay / 1000000L ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeEntered( isEditorMode, gameData );
        
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
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize(boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
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
        
        if ( displayEngine.getBooleanValue() )
        {
            engineHeaderString = new DrawnString( left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Engine: ", null );
            if ( displayWaterTemp.getBooleanValue() )
            {
                engineWaterTempString = new DrawnString( null, engineHeaderString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(W:", "°C)" );
                engineOilTempString = new DrawnString( null, engineWaterTempString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font, font2AntiAliased, fontColor, null, "°C" );
            }
            else
            {
                engineWaterTempString = null;
                engineOilTempString = new DrawnString( null, engineHeaderString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "°C" );
            }
            
            relY = engineHeaderString;
            top = engineHeight.getEffectiveHeight() + 10;
        }
        
        int imgWidth = displayTires.getBooleanValue() && displayBrakes.getBooleanValue() ? Math.max( tireWidth, brakeWidth ) : ( displayTires.getBooleanValue() ? tireWidth : brakeWidth );
        
        if ( displayTires.getBooleanValue() )
        {
            tiresHeaderString = new DrawnString( null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Tires: ", null );
            tireTempFLString = new DrawnString( null, tiresHeaderString, center - 7 - imgWidth, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "°C" );
            tireTempFRString = new DrawnString( null, tiresHeaderString, center + 7 + imgWidth, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "°C" );
            tireTempRLString = new DrawnString( null, tiresHeaderString, center - 7 - imgWidth, 0 + tireHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "°C" );
            tireTempRRString = new DrawnString( null, tiresHeaderString, center + 7 + imgWidth, 0 + tireHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "°C" );
            tireTempFLString2 = new DrawnString( null, tiresHeaderString, center - 7 - imgWidth, 0 + tireHeight - 2, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", "°C)" );
            tireTempFRString2 = new DrawnString( null, tiresHeaderString, center + 7 + imgWidth, 0 + tireHeight - 2, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", "°C)" );
            tireTempRLString2 = new DrawnString( null, tiresHeaderString, center - 7 - imgWidth, 0 + tireHeight - 2 + tireHeight + 7, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", "°C)" );
            tireTempRRString2 = new DrawnString( null, tiresHeaderString, center + 7 + imgWidth, 0 + tireHeight - 2 + tireHeight + 7, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", "°C)" );
            
            relY = tiresHeaderString;
            top = tireHeight * 2 + 15;
        }
        
        if ( displayBrakes.getBooleanValue() )
        {
            brakesHeaderString = new DrawnString( null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            brakeTempFLString = new DrawnString( null, brakesHeaderString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "°C" );
            brakeTempFRString = new DrawnString( null, brakesHeaderString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "°C" );
            brakeTempRLString = new DrawnString( null, brakesHeaderString, center - 7 - imgWidth, 2 + brakeHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "°C" );
            brakeTempRRString = new DrawnString( null, brakesHeaderString, center + 7 + imgWidth, 2 + brakeHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "°C" );
        }
    }
    
    private static void interpolateColor( byte[] color0, byte[] color1, float alpha, byte[] result )
    {
        final float beta = 1f - alpha;
        
        result[ByteOrderManager.RED] = (byte)( (float)( color0[ByteOrderManager.RED] & 0xFF ) * beta + (float)( color1[ByteOrderManager.RED] & 0xFF ) * alpha );
        result[ByteOrderManager.GREEN] = (byte)( (float)( color0[ByteOrderManager.GREEN] & 0xFF ) * beta + (float)( color1[ByteOrderManager.GREEN] & 0xFF ) * alpha );
        result[ByteOrderManager.BLUE] = (byte)( (float)( color0[ByteOrderManager.BLUE] & 0xFF ) * beta + (float)( color1[ByteOrderManager.BLUE] & 0xFF ) * alpha );
    }
    
    private void drawEngine( float temp, VehiclePhysics.Engine engine, TextureImage2D image, int x, int y, int width )
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
            image.clear( Color.BLACK, x + barWidth, y, w - barWidth, h, false, null );
        }
        
        Color awtColor = new Color( color[ByteOrderManager.RED] & 0xFF, color[ByteOrderManager.GREEN] & 0xFF, color[ByteOrderManager.BLUE] & 0xFF );
        image.clear( awtColor, x, y, barWidth, h, false, null );
        
        image.getTextureCanvas().setColor( Color.GREEN );
        image.getTextureCanvas().drawLine( x + (int)( ( optimum - min ) * w / range ), y, x + (int)( ( optimum - min ) * w / range ), y + h - 1 );
        
        image.getTextureCanvas().setColor( Color.DARK_GRAY );
        image.getTextureCanvas().drawLine( x + (int)( ( upperOptimum - min ) * w / range ), y, x + (int)( ( upperOptimum - min ) * w / range ), y + h - 1 );
        
        image.getTextureCanvas().setColor( Color.YELLOW );
        image.getTextureCanvas().drawLine( x + (int)( ( overheating - min ) * w / range ), y, x + (int)( ( overheating - min ) * w / range ), y + h - 1 );
        
        image.getTextureCanvas().setColor( new Color( 255, 102, 0 ) );
        image.getTextureCanvas().drawLine( x + (int)( ( strongOverheating - min ) * w / range ), y, x + (int)( ( strongOverheating - min ) * w / range ), y + h - 1 );
        
        image.markDirty( x, y, w, h );
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
    
    private void drawTire( TelemetryData telemData, Wheel wheel, CompoundWheel compoundWheel, TextureImage2D image, int x, int y )
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
        
        image.markDirty( x, y, w, h );
        
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
            image.clearPixelLine( pixels, x, y + j, w, false, null );
        }
        
        for ( int j = hh + 1; j < h; j++ )
        {
            image.clearPixelLine( pixels, x, y + j, w, false, null );
        }
        
        color[0] = (byte)0;
        color[1] = (byte)255;
        color[2] = (byte)0;
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( color, 0, pixels, i * 4, 4 );
        }
        
        image.clearPixelLine( pixels, x, y + hh, w, false, null );
        
        Texture2DCanvas texCanvas = image.getTextureCanvas();
        
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
    
    private void drawBrake( float temp, Wheel wheel, VehiclePhysics.Brakes.WheelBrake brake, TextureImage2D image, int x, int y )
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
            image.clear( Color.BLACK, x, y, w, h - barHeight, false, null );
        }
        
        image.clear( awtColor, x, y + h - barHeight, w, barHeight, false, null );
        
        byte[] pixels = new byte[ w * 4 ];
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( colorHot, 0, pixels, i * 4, 4 );
        }
        
        image.clearPixelLine( pixels, x, y + (int)( ( range - brake.getOverheatingTemperature() ) * h / range ), w, false, null );
        
        for ( int i = 0; i < w; i++ )
        {
            //System.arraycopy( colorOpt, 0, pixels, i * 4, 4 );
            pixels[i * 4 + ByteOrderManager.RED] = (byte)0;
            pixels[i * 4 + ByteOrderManager.GREEN] = (byte)255;
            pixels[i * 4 + ByteOrderManager.BLUE] = (byte)0;
            pixels[i * 4 + ByteOrderManager.ALPHA] = (byte)255;
        }
        
        image.clearPixelLine( pixels, x, y + (int)( ( range - brake.getOptimumTemperaturesUpperBound() ) * h / range ), w, false, null );
        image.clearPixelLine( pixels, x, y + (int)( ( range - brake.getOptimumTemperaturesLowerBound() ) * h / range ), w, false, null );
        
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( colorCold, 0, pixels, i * 4, 4 );
        }
        
        image.clearPixelLine( pixels, x, y + (int)( ( range - brake.getColdTemperature() ) * h / range ), w, false, null );
        
        image.markDirty( x, y, w, h );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final TextureImage2D image = texCanvas.getImage();
        final Color backgroundColor = getBackgroundColor();
        
        final TelemetryData telemData = gameData.getTelemetryData();
        final VehiclePhysics physics = gameData.getPhysics();
        final VehicleSetup setup = gameData.getSetup();
        
        if ( needsCompleteRedraw )
        {
            if ( displayEngine.getBooleanValue() )
                engineHeaderString.draw( offsetX, offsetY, "(" + NumberUtil.formatFloat( gameData.getPhysics().getEngine().getOptimumOilTemperature(), 1, true ) + "°C)", backgroundColor, image );
            TireCompound tireCompound = setup.getGeneral().getFrontTireCompound();
            if ( displayTires.getBooleanValue() )
                tiresHeaderString.draw( offsetX, offsetY, tireCompound.getName() + " (" + NumberUtil.formatFloat( tireCompound.getWheel( Wheel.FRONT_LEFT ).getOptimumTemperature(), 1, true ) + "°C)", backgroundColor, image );
            if ( displayBrakes.getBooleanValue() )
                brakesHeaderString.draw( offsetX, offsetY, "Brakes:", backgroundColor, image );
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
                    engineWaterTempString.draw( offsetX, offsetY, string, backgroundColor, image );
                }
            }
            
            int oilTemp = Math.round( telemData.getEngineOilTemperature() * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( oilTemp != oldOilTemp ) ) )
            {
                oldOilTemp = oilTemp;
                
                String string = String.valueOf( oilTemp / 10f );
                engineOilTempString.draw( offsetX, offsetY, string, backgroundColor, image );
                
                int engineWidth;
                if ( displayWaterTemp.getBooleanValue() )
                    engineWidth = Math.max( 70, Math.max( engineWaterTempString.getLastWidth(), engineOilTempString.getLastWidth() ) );
                else
                    engineWidth = Math.max( 70, engineOilTempString.getLastWidth() );
                engineWidth = engineOilTempString.getAbsX() - engineWidth - 5;
                
                int engineTop = ( engineWaterTempString != null ) ? engineWaterTempString.getAbsY() + 3 : engineOilTempString.getAbsY() + 3;
                
                drawEngine( oilTemp / 10f, physics.getEngine(), image, offsetX + engineHeaderString.getAbsX(), offsetY + engineTop, engineWidth );
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
                tireTempFLString.draw( offsetX, offsetY, string, backgroundColor, image );
                float diff = Math.round( tireTempFL - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempFLString2.draw( offsetX, offsetY, string, backgroundColor, image );
                
                
                drawTire( telemData, Wheel.FRONT_LEFT, wheel, image, offsetX + tireTempFLString.getAbsX() + 3, offsetY + tireTempFLString.getAbsY() + 2 );
            }
            
            int tireTempFR = Math.round( telemData.getTireTemperature( Wheel.FRONT_RIGHT ) * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( tireTempFR != oldTireTemps[1] ) ) )
            {
                oldTireTemps[1] = tireTempFR;
                
                CompoundWheel wheel = setup.getGeneral().getFrontTireCompound().getWheel( Wheel.FRONT_RIGHT );
                
                float temp = tireTempFR / 10f;
                String string = String.valueOf( temp );
                tireTempFRString.draw( offsetX, offsetY, string, backgroundColor, image );
                float diff = Math.round( tireTempFR - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempFRString2.draw( offsetX, offsetY, string, backgroundColor, image );
                
                drawTire( telemData, Wheel.FRONT_RIGHT, wheel, image, offsetX + tireTempFRString.getAbsX() - tireWidth - 3, offsetY + tireTempFRString.getAbsY() + 2 );
            }
            
            int tireTempRL = Math.round( telemData.getTireTemperature( Wheel.REAR_LEFT ) * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( tireTempRL != oldTireTemps[2] ) ) )
            {
                oldTireTemps[2] = tireTempRL;
                
                CompoundWheel wheel = setup.getGeneral().getRearTireCompound().getWheel( Wheel.REAR_LEFT );
                
                float temp = tireTempRL / 10f;
                String string = String.valueOf( temp );
                tireTempRLString.draw( offsetX, offsetY, string, backgroundColor, image );
                float diff = Math.round( tireTempRL - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempRLString2.draw( offsetX, offsetY, string, backgroundColor, image );
                
                drawTire( telemData, Wheel.REAR_LEFT, wheel, image, offsetX + tireTempRLString.getAbsX() + 3, offsetY + tireTempRLString.getAbsY() + 2 );
            }
            
            int tireTempRR = Math.round( telemData.getTireTemperature( Wheel.REAR_RIGHT ) * 10f );
            if ( needsCompleteRedraw || ( clock1 && ( tireTempRR != oldTireTemps[3] ) ) )
            {
                oldTireTemps[3] = tireTempRR;
                
                CompoundWheel wheel = setup.getGeneral().getRearTireCompound().getWheel( Wheel.REAR_RIGHT );
                
                float temp = tireTempRR / 10f;
                String string = String.valueOf( temp );
                tireTempRRString.draw( offsetX, offsetY, string, backgroundColor, image );
                float diff = Math.round( tireTempRR - wheel.getOptimumTemperature() * 10f ) / 10f;
                string = ( diff >= 0f ? "+" : "" ) + diff;
                tireTempRRString2.draw( offsetX, offsetY, string, backgroundColor, image );
                
                drawTire( telemData, Wheel.REAR_RIGHT, wheel, image, offsetX + tireTempRRString.getAbsX() - tireWidth - 3, offsetY + tireTempRRString.getAbsY() + 2 );
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
            else if ( lastBrakeTempTime + brakeTempsPeekDelay < gameData.getScoringInfo().getSessionNanos() )
            {
                brakesUpdateAllowed = true;
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempFL != oldBrakeTemps[0] ) ) )
            {
                oldBrakeTemps[0] = brakeTempFL;
                
                String string = String.valueOf( brakeTempFL );
                brakeTempFLString.draw( offsetX, offsetY, string, backgroundColor, image );
                
                drawBrake( brakeTempFL, Wheel.FRONT_LEFT, physics.getBrakes().getBrake( Wheel.FRONT_LEFT ), image, offsetX + brakeTempFLString.getAbsX() + 3, offsetY + brakeTempFLString.getAbsY() );
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempFR != oldBrakeTemps[1] ) ) )
            {
                oldBrakeTemps[1] = brakeTempFR;
                
                String string = String.valueOf( brakeTempFR );
                brakeTempFRString.draw( offsetX, offsetY, string, backgroundColor, image );
                
                drawBrake( brakeTempFR, Wheel.FRONT_RIGHT, physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ), image, offsetX + brakeTempFRString.getAbsX() - brakeWidth - 3, offsetY + brakeTempFRString.getAbsY() );
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempRL != oldBrakeTemps[2] ) ) )
            {
                oldBrakeTemps[2] = brakeTempRL;
                
                String string = String.valueOf( brakeTempRL );
                brakeTempRLString.draw( offsetX, offsetY, string, backgroundColor, image );
                
                drawBrake( brakeTempRL, Wheel.REAR_LEFT, physics.getBrakes().getBrake( Wheel.REAR_LEFT ), image, offsetX + brakeTempRLString.getAbsX() + 3, offsetY + brakeTempRLString.getAbsY() );
            }
            
            if ( needsCompleteRedraw || ( clock1 && brakesUpdateAllowed && ( brakeTempRR != oldBrakeTemps[3] ) ) )
            {
                oldBrakeTemps[3] = brakeTempRR;
                
                String string = String.valueOf( brakeTempRR );
                brakeTempRRString.draw( offsetX, offsetY, string, backgroundColor, image );
                
                drawBrake( brakeTempRR, Wheel.REAR_RIGHT, physics.getBrakes().getBrake( Wheel.REAR_RIGHT ), image, offsetX + brakeTempRRString.getAbsX() - brakeWidth - 3, offsetY + brakeTempRRString.getAbsY() );
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
        writer.writeProperty( "engineHeight", Size.unparseValue( engineHeight.getHeight() ), "The height of the engine bar." );
        writer.writeProperty( displayTires, "Display the tire part of the Widget?" );
        writer.writeProperty( "tireWidth", Size.unparseValue( tireSize.getWidth() ), "The width of a tire image." );
        writer.writeProperty( "tireHeight", Size.unparseValue( tireSize.getHeight() ), "The height of a tire image." );
        writer.writeProperty( displayBrakes, "Display the brakes of the Widget?" );
        writer.writeProperty( "brakeWidth", Size.unparseValue( brakeSize.getWidth() ), "The width of a brake image." );
        writer.writeProperty( "brakeHeight", Size.unparseValue( brakeSize.getHeight() ), "The height of a brake image." );
        writer.writeProperty( "brakeTempsPeekDelay", getBrakeTempsPeekDelay(), "(in milliseconds) If greater than 0, the brake temperatures will stay on their peek values after a turn for the chosen amount of milliseconds." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( font2.loadProperty( key, value ) );
        else if ( displayEngine.loadProperty( key, value ) );
        else if ( displayWaterTemp.loadProperty( key, value ) );
        else if ( key.equals( "engineHeight" ) );
        else if ( displayTires.loadProperty( key, value ) );
        else if ( tireSize.loadProperty( key, value, "tireWidth", "tireHeight" ) );
        else if ( displayBrakes.loadProperty( key, value ) );
        else if ( brakeSize.loadProperty( key, value, "brakeWidth", "brakeHeight" ) );
        else if ( key.equals( "brakeTempsPeekDelay" ) )
            this.brakeTempsPeekDelay = Integer.parseInt( value ) * 1000000L;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont )
    {
        super.getProperties( propsCont );
        
        propsCont.addProperty( font2 );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( displayEngine );
        propsCont.addProperty( displayWaterTemp );
        propsCont.addProperty( engineHeight.createHeightProperty( "engineHeight" ) );
        propsCont.addProperty( displayTires );
        propsCont.addProperty( tireSize.createWidthProperty( "tireWidth" ) );
        propsCont.addProperty( tireSize.createHeightProperty( "tireHeight" ) );
        propsCont.addProperty( displayBrakes );
        propsCont.addProperty( brakeSize.createWidthProperty( "brakeWidth" ) );
        propsCont.addProperty( brakeSize.createHeightProperty( "brakeHeight" ) );
        
        propsCont.addProperty( new Property( "brakeTempsPeekDelay", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setBrakeTempsPeekDelay( ( (Number)value ).intValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getBrakeTempsPeekDelay() );
            }
        } );
    }
    
    public TemperaturesWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.178125f, Size.PERCENT_OFFSET + 0.30416667f );
        
        this.engineHeight = new Size( Size.PERCENT_OFFSET + 0, Size.PERCENT_OFFSET + 0.1f, this );
        this.tireSize = new Size( Size.PERCENT_OFFSET + 0.1f, Size.PERCENT_OFFSET + 0.1f, this );
        this.brakeSize = new Size( Size.PERCENT_OFFSET + 0.07f, Size.PERCENT_OFFSET + 0.2f, this );
    }
}
