package net.ctdp.rfdynhud.widgets.wear;

import java.awt.Color;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.editor.properties.BooleanProperty;
import net.ctdp.rfdynhud.editor.properties.EnumProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound.CompoundWheel;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.ByteOrderManager;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.FloatValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link WearWidget} displays wear information for the tires.
 * 
 * @author Marvin Froehlich
 */
public class WearWidget extends Widget
{
    private static enum HundredPercentBase
    {
        SAFE_RANGE,
        GOOD_RANGE,
        BAD_RANGE,
        MAX_RANGE,
        ;
    }
    
    private final FontProperty font2 = new FontProperty( this, "font2", "SmallerFont" );
    
    private final Size engineHeight;
    
    private final Size tireSize;
    private final Size brakeSize;
    
    private final BooleanProperty displayEngine = new BooleanProperty( this, "displayEngine", true );
    private final BooleanProperty displayTires = new BooleanProperty( this, "displayTires", true );
    private final BooleanProperty displayBrakes = new BooleanProperty( this, "displayBrakes", true );
    
    private final EnumProperty<HundredPercentBase> hundredPercentBase = new EnumProperty<HundredPercentBase>( this, "hundredPercentBase", HundredPercentBase.SAFE_RANGE );
    
    private final BooleanProperty displayWearPercent = new BooleanProperty( this, "displayWearPercent", true );
    private final BooleanProperty displayCompoundName = new BooleanProperty( this, "displayCompoundName", true );
    
    private DrawnString engineHeaderString = null;
    private DrawnString engineWearString = null;
    private DrawnString engineVarianceString = null;
    
    private DrawnString tiresHeaderString = null;
    private DrawnString tireWearFLString = null;
    private DrawnString tireWearFRString = null;
    private DrawnString tireWearRLString = null;
    private DrawnString tireWearRRString = null;
    private DrawnString tireGripFLString = null;
    private DrawnString tireGripFRString = null;
    private DrawnString tireGripRLString = null;
    private DrawnString tireGripRRString = null;
    
    private DrawnString brakesHeaderString = null;
    private DrawnString brakeWearFLString = null;
    private DrawnString brakeWearFRString = null;
    private DrawnString brakeWearRLString = null;
    private DrawnString brakeWearRRString = null;
    private DrawnString brakeWearVarianceFLString = null;
    private DrawnString brakeWearVarianceFRString = null;
    private DrawnString brakeWearVarianceRLString = null;
    private DrawnString brakeWearVarianceRRString = null;
    
    private final FloatValue engineLifetime = new FloatValue( -1f, 0.001f );
    private final FloatValue brakeDiscWearFL = new FloatValue( -1f, 0.001f );
    private final FloatValue brakeDiscWearFR = new FloatValue( -1f, 0.001f );
    private final FloatValue brakeDiscWearRL = new FloatValue( -1f, 0.001f );
    private final FloatValue brakeDiscWearRR = new FloatValue( -1f, 0.001f );
    
    private static final float gripGood = 0.95f;
    private static final float gripBad = 0.85f;
    
    private static final byte[] colorGood = new byte[ 4 ];
    private static final byte[] colorOk = new byte[ 4 ];
    private static final byte[] colorBad = new byte[ 4 ];
    static
    {
        colorGood[ByteOrderManager.RED] = (byte)0;
        colorGood[ByteOrderManager.GREEN] = (byte)255;
        colorGood[ByteOrderManager.BLUE] = (byte)0;
        colorGood[ByteOrderManager.ALPHA] = (byte)255;
        
        colorOk[ByteOrderManager.RED] = (byte)255;
        colorOk[ByteOrderManager.GREEN] = (byte)255;
        colorOk[ByteOrderManager.BLUE] = (byte)0;
        colorOk[ByteOrderManager.ALPHA] = (byte)255;
        
        colorBad[ByteOrderManager.RED] = (byte)255;
        colorBad[ByteOrderManager.GREEN] = (byte)0;
        colorBad[ByteOrderManager.BLUE] = (byte)0;
        colorBad[ByteOrderManager.ALPHA] = (byte)255;
    }
    
    private static final Color YELLOW2 = new Color( 234, 190, 37 );
    private static final Color GREEN2 = new Color( 152, 234, 13 );
    
    private int[] oldTireWear = { -1, -1, -1, -1 };
    
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
    
    public final boolean getDisplayWearPercent()
    {
        return ( displayWearPercent.getBooleanValue() );
    }
    
    private final boolean getDisplayWearPercent_engine()
    {
        return ( getDisplayWearPercent() );
    }
    
    private final boolean getDisplayWearPercent_tires()
    {
        return ( true );
    }
    
    private final boolean getDisplayWearPercent_brakes()
    {
        return ( true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeEntered( isEditorMode, gameData );
        
        for ( int i = 0; i < oldTireWear.length; i++ )
            oldTireWear[i] = -1;
        
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
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
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
            engineHeaderString = new DrawnString( left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            if ( getDisplayWearPercent_engine() )
            {
                engineWearString = new DrawnString( null, engineHeaderString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                engineVarianceString = new DrawnString( null, engineWearString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
            }
            else
            {
                engineWearString = null;
                engineVarianceString = null;
            }
            
            relY = engineHeaderString;
            top = engineHeight.getEffectiveHeight() + 10;
        }
        
        int imgWidth = displayTires.getBooleanValue() && displayBrakes.getBooleanValue() ? Math.max( tireWidth, brakeWidth ) : ( displayTires.getBooleanValue() ? tireWidth : brakeWidth );
        
        if ( displayTires.getBooleanValue() )
        {
            tiresHeaderString = new DrawnString( null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Tire wear/grip", null );
            if ( getDisplayWearPercent_tires() )
            {
                tireWearFLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireWearFRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireWearRLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireWearRRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireGripFLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight - 2, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
                tireGripFRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight - 2, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
                tireGripRLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight - 2 + tireHeight + 7, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
                tireGripRRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight - 2 + tireHeight + 7, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
            }
            else
            {
                tireWearFLString = null;
                tireWearFRString = null;
                tireWearRLString = null;
                tireWearRRString = null;
                tireGripFLString = null;
                tireGripFRString = null;
                tireGripRLString = null;
                tireGripRRString = null;
            }
            
            relY = tiresHeaderString;
            top = tireHeight * 2 + 15;
        }
        
        if ( displayBrakes.getBooleanValue() )
        {
            brakesHeaderString = new DrawnString( null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            if ( getDisplayWearPercent_brakes() )
            {
                brakeWearFLString = new DrawnString( null, brakesHeaderString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearFRString = new DrawnString( null, brakesHeaderString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearRLString = new DrawnString( null, brakesHeaderString, center - 7 - imgWidth, 2 + brakeHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearRRString = new DrawnString( null, brakesHeaderString, center + 7 + imgWidth, 2 + brakeHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearVarianceFLString = new DrawnString( null, brakeWearFLString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
                brakeWearVarianceFRString = new DrawnString( null, brakeWearFRString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
                brakeWearVarianceRLString = new DrawnString( null, brakeWearRLString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
                brakeWearVarianceRRString = new DrawnString( null, brakeWearRRString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
            }
            else
            {
                brakeWearFLString = null;
                brakeWearFRString = null;
                brakeWearRLString = null;
                brakeWearRRString = null;
                brakeWearVarianceFLString = null;
                brakeWearVarianceFRString = null;
                brakeWearVarianceRLString = null;
                brakeWearVarianceRRString = null;
            }
        }
    }
    
    private static void interpolateColor( byte[] color0, byte[] color1, float alpha, byte[] result )
    {
        final float beta = 1f - alpha;
        
        result[ByteOrderManager.RED] = (byte)( (float)( color0[ByteOrderManager.RED] & 0xFF ) * beta + (float)( color1[ByteOrderManager.RED] & 0xFF ) * alpha );
        result[ByteOrderManager.GREEN] = (byte)( (float)( color0[ByteOrderManager.GREEN] & 0xFF ) * beta + (float)( color1[ByteOrderManager.GREEN] & 0xFF ) * alpha );
        result[ByteOrderManager.BLUE] = (byte)( (float)( color0[ByteOrderManager.BLUE] & 0xFF ) * beta + (float)( color1[ByteOrderManager.BLUE] & 0xFF ) * alpha );
    }
    
    private void drawEngine( float lifetime, double raceLengthMultiplier, VehiclePhysics.Engine engine, TextureImage2D image, int x, int y, int width )
    {
        final int w = width;
        final int h = engineHeight.getEffectiveHeight();
        
        final int lowerSafeLifetime = engine.getLowerSafeLifetimeValue( raceLengthMultiplier );
        final int lowerGoodLifetime = engine.getLowerGoodLifetimeValue( raceLengthMultiplier );
        final int lowerBadLifetime = engine.getLowerBadLifetimeValue( raceLengthMultiplier );
        final int minLifetime = engine.getMinLifetimeValue( raceLengthMultiplier );
        final int safeLifetimeTotal = engine.getSafeLifetimeTotal( raceLengthMultiplier );
        final int goodLifetimeTotal = engine.getGoodLifetimeTotal( raceLengthMultiplier );
        final int badLifetimeTotal = engine.getBadLifetimeTotal( raceLengthMultiplier );
        final int maxLifetimeTotal = engine.getMaxLifetimeTotal( raceLengthMultiplier );
        final boolean hasVariance = engine.hasLifetimeVariance();
        
        if ( hasVariance )
        {
            int x0 = x;
            int x1 = -1;
            int x2 = -1;
            int x3 = -1;
            
            if ( lifetime >= lowerBadLifetime )
            {
                int w2 = ( lowerBadLifetime - minLifetime ) * w / maxLifetimeTotal;
                image.clear( Color.RED, x0, y, w2, h, false, null );
                x0 += w2;
            }
            else
            {
                int w2 = (int)( ( lifetime - minLifetime ) * w / maxLifetimeTotal );
                image.clear( Color.RED, x0, y, w2, h, false, null );
                x0 += w2;
                x1 = x + ( maxLifetimeTotal - badLifetimeTotal ) * w / maxLifetimeTotal;
            }
            
            if ( lifetime >= lowerGoodLifetime )
            {
                int w2 = ( lowerGoodLifetime - lowerBadLifetime ) * w / maxLifetimeTotal;
                image.clear( YELLOW2, x0, y, w2, h, false, null );
                x0 += w2;
            }
            else
            {
                if ( lifetime >= lowerBadLifetime )
                {
                    int w2 = (int)( ( lifetime - lowerBadLifetime ) * w / maxLifetimeTotal );
                    image.clear( YELLOW2, x0, y, w2, h, false, null );
                    x0 += w2;
                }
                
                x2 = x + ( maxLifetimeTotal - goodLifetimeTotal ) * w / maxLifetimeTotal;
            }
            
            if ( lifetime >= lowerSafeLifetime )
            {
                int w2 = ( lowerSafeLifetime - lowerGoodLifetime ) * w / maxLifetimeTotal;
                image.clear( GREEN2, x0, y, w2, h, false, null );
                x0 += w2;
                
                int w3 = (int)( ( lifetime - lowerSafeLifetime ) * w / maxLifetimeTotal );
                image.clear( Color.GREEN, x0, y, w3, h, false, null );
                x0 += w3;
            }
            else
            {
                if ( lifetime >= lowerGoodLifetime )
                {
                    int w2 = (int)( ( lifetime - lowerGoodLifetime ) * w / maxLifetimeTotal );
                    image.clear( GREEN2, x0, y, w2, h, false, null );
                    x0 += w2;
                }
                
                x3 = x + ( maxLifetimeTotal - safeLifetimeTotal ) * w / maxLifetimeTotal;
            }
            
            int w_ = w - x0 + x;
            if ( w_ > 0 )
            {
                image.clear( Color.BLACK, x0, y, w_, h, false, null );
            }
            
            if ( x1 > 0 )
            {
                image.getTextureCanvas().setColor( Color.RED );
                image.getTextureCanvas().drawLine( x1, y, x1, y + h - 1 );
            }
            
            if ( x2 > 0 )
            {
                image.getTextureCanvas().setColor( YELLOW2 );
                image.getTextureCanvas().drawLine( x2, y, x2, y + h - 1 );
            }
            
            if ( x3 > 0 )
            {
                image.getTextureCanvas().setColor( GREEN2 );
                image.getTextureCanvas().drawLine( x3, y, x3, y + h - 1 );
            }
        }
        else
        {
            int w2 = (int)( lifetime * w / minLifetime );
            image.clear( Color.GREEN, x, y, w2, h, false, null );
            
            int w3 = w - w2;
            if ( w3 > 0 )
                image.clear( Color.BLACK, x + w2, y, w3, h, false, null );
        }
        
        image.markDirty( x, y, w, h );
    }
    
    private void drawTire( float wear, float grip, CompoundWheel compoundWheel, TextureImage2D image, int x, int y )
    {
        int w = tireSize.getEffectiveWidth();
        int h = tireSize.getEffectiveHeight();
        
        final float minGrip = compoundWheel.getMinGrip();
        float normGrip = ( grip - minGrip ) / ( 1.0f - minGrip );
        float normGripGood = ( gripGood - minGrip ) / ( 1.0f - minGrip );
        float normGripBad = ( gripBad - minGrip ) / ( 1.0f - minGrip );
        
        byte[] color = new byte[4];
        color[ByteOrderManager.ALPHA] = (byte)255;
        if ( normGrip <= normGripBad )
        {
            System.arraycopy( colorBad, 0, color, 0, 3 );
        }
        else if ( normGrip < normGripGood )
        {
            float alpha = ( normGrip - normGripBad ) / ( normGripGood - normGripBad );
            interpolateColor( colorBad, colorOk, alpha, color );
        }
        else
        {
            System.arraycopy( colorGood, 0, color, 0, 3 );
        }
        
        Color awtColor = new Color( color[ByteOrderManager.RED] & 0xFF, color[ByteOrderManager.GREEN] & 0xFF, color[ByteOrderManager.BLUE] & 0xFF );
        
        int barHeight = Math.min( (int)( h * normGrip ), h );
        
        if ( normGrip > 0.0f )
        {
            image.clear( Color.BLACK, x, y, w, h - barHeight, false, null );
        }
        
        image.clear( awtColor, x, y + h - barHeight, w, barHeight, false, null );
        
        byte[] pixels = new byte[ w * 4 ];
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( colorBad, 0, pixels, i * 4, 4 );
        }
        
        image.clearPixelLine( pixels, x, y + h - (int)( h * wear ), w, false, null );
        
        image.markDirty( x, y, w, h );
    }
    
    private void drawBrake( float discThickness, Wheel wheel, VehiclePhysics.Brakes.WheelBrake brake, VehicleSetup setup, TextureImage2D image, int x, int y )
    {
        int w = brakeSize.getEffectiveWidth();
        int h = brakeSize.getEffectiveHeight();
        
        final float minDiscFailure = brake.getMinDiscFailure();
        final float maxDiscFailure = brake.getMaxDiscFailure();
        final float startDiscThickness = setup.getWheelAndTire( wheel ).getBrakeDiscThickness();
        final float variance = brake.getDiscFailureVarianceRange();
        discThickness = Math.max( minDiscFailure, discThickness );
        final float positiveRange = startDiscThickness - maxDiscFailure;
        final float maxRange = startDiscThickness - minDiscFailure;
        final boolean hasVariance = brake.hasDiscFailureVariance();
        
        if ( hasVariance )
        {
            if ( discThickness >= maxDiscFailure )
            {
                int h2 = (int)( variance * h / maxRange );
                image.clear( Color.RED, x, y + h - h2, w, h2, false, null );
                
                int h3 = (int)( ( discThickness - maxDiscFailure ) * ( h - h2 ) / positiveRange );
                image.clear( Color.GREEN, x, y + h - h2 - h3, w, h3, false, null );
                
                int h4 = h - h3 - h2;
                if ( h4 > 0 )
                    image.clear( Color.BLACK, x, y, w, h4, false, null );
            }
            else
            {
                int h2 = (int)( ( discThickness - minDiscFailure ) * h / maxRange );
                image.clear( Color.RED, x, y + h - h2, w, h2, false, null );
                
                int y2 = h - (int)( variance * h / maxRange );
                
                int h3 = h - h2;
                if ( h3 > 0 )
                    image.clear( Color.BLACK, x, y, w, h3, false, null );
                
                image.getTextureCanvas().setColor( Color.RED );
                image.getTextureCanvas().drawLine( x, y + y2, x + w - 1, y + y2 );
            }
        }
        else
        {
            int h2 = (int)( ( discThickness - minDiscFailure ) * h / maxRange );
            image.clear( Color.GREEN, x, y + h - h2, w, h2, false, null );
            
            int h3 = h - h2;
            if ( h3 > 0 )
                image.clear( Color.BLACK, x, y, w, h3, false, null );
        }
        
        image.markDirty( x, y, w, h );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final TextureImage2D image = texCanvas.getImage();
        final Color backgroundColor = getBackgroundColor();
        
        final VehiclePhysics physics = gameData.getPhysics();
        final TelemetryData telemData = gameData.getTelemetryData();
        final VehicleSetup setup = gameData.getSetup();
        
        //final int center = width / 2;
        
        final boolean isEditorMode = ( editorPresets != null );
        
        if ( needsCompleteRedraw )
        {
            if ( displayEngine.getBooleanValue() )
                engineHeaderString.draw( offsetX, offsetY, "Engine:", backgroundColor, image );
            if ( displayTires.getBooleanValue() )
            {
                if ( displayCompoundName.getBooleanValue() )
                    tiresHeaderString.draw( offsetX, offsetY, ": " + setup.getGeneral().getFrontTireCompound().getName(), backgroundColor, image );
                else
                    tiresHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
            }
            if ( displayBrakes.getBooleanValue() )
                brakesHeaderString.draw( offsetX, offsetY, "Brakes:", backgroundColor, image );
        }
        
        if ( displayEngine.getBooleanValue() )
        {
            final double raceLengthPercentage = gameData.getScoringInfo().getRaceLengthPercentage();
            float lifetime = isEditorMode ? editorPresets.getEngineLifetime() : gameData.getTelemetryData().getEngineLifetime();
            final int hundredPercentBase;
            switch ( this.hundredPercentBase.getEnumValue() )
            {
                case SAFE_RANGE:
                default:
                    hundredPercentBase = physics.getEngine().getSafeLifetimeTotal( raceLengthPercentage );
                    engineLifetime.update( lifetime / hundredPercentBase );
                    break;
                case GOOD_RANGE:
                    hundredPercentBase = physics.getEngine().getGoodLifetimeTotal( raceLengthPercentage );
                    engineLifetime.update( ( lifetime + hundredPercentBase - physics.getEngine().getSafeLifetimeTotal( raceLengthPercentage ) ) / hundredPercentBase );
                    break;
                case BAD_RANGE:
                    hundredPercentBase = physics.getEngine().getBadLifetimeTotal( raceLengthPercentage );
                    engineLifetime.update( ( lifetime + hundredPercentBase - physics.getEngine().getSafeLifetimeTotal( raceLengthPercentage ) ) / hundredPercentBase );
                    break;
                case MAX_RANGE:
                    hundredPercentBase = physics.getEngine().getMaxLifetimeTotal( raceLengthPercentage );
                    engineLifetime.update( ( lifetime + hundredPercentBase - physics.getEngine().getSafeLifetimeTotal( raceLengthPercentage ) ) / hundredPercentBase );
                    break;
            }
            lifetime = Math.max( -physics.getEngine().getLifetimeVarianceRange( raceLengthPercentage ), lifetime );
            
            if ( needsCompleteRedraw || engineLifetime.hasChanged() )
            {
                int engineWidth;
                if ( getDisplayWearPercent_engine() )
                {
                    engineWearString.draw( offsetX, offsetY, NumberUtil.formatFloat( engineLifetime.getValue() * 100f, 1, true ), backgroundColor, image );
                    final float variancePercent;
                    switch ( this.hundredPercentBase.getEnumValue() )
                    {
                        case SAFE_RANGE:
                        default:
                            variancePercent = ( physics.getEngine().getMaxLifetimeTotal( raceLengthPercentage ) - physics.getEngine().getSafeLifetimeTotal( raceLengthPercentage ) ) * 100f / hundredPercentBase;
                            break;
                        case GOOD_RANGE:
                            variancePercent = ( physics.getEngine().getMaxLifetimeTotal( raceLengthPercentage ) - physics.getEngine().getGoodLifetimeTotal( raceLengthPercentage ) ) * 100f / hundredPercentBase;
                            break;
                        case BAD_RANGE:
                            variancePercent = ( physics.getEngine().getMaxLifetimeTotal( raceLengthPercentage ) - physics.getEngine().getBadLifetimeTotal( raceLengthPercentage ) ) * 100f / hundredPercentBase;
                            break;
                        case MAX_RANGE:
                            variancePercent = 0.0f;
                            break;
                    }
                    if ( variancePercent > 0.001f )
                        engineVarianceString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, image );
                    
                    engineWidth = Math.max( engineVarianceString.getLastWidth(), engineWearString.getLastWidth() );
                    engineWidth = engineWearString.getAbsX() - engineWidth - 5;
                }
                else
                {
                    engineWidth = width - ( engineHeaderString.getAbsX() * 2 );
                }
                
                drawEngine( lifetime, raceLengthPercentage, physics.getEngine(), image, offsetX + engineHeaderString.getAbsX(), offsetY + engineHeaderString.getAbsY() + engineHeaderString.getMaxHeight( false ) + 3, engineWidth );
            }
        }
        
        if ( displayTires.getBooleanValue() )
        {
            final int tireWidth = tireSize.getEffectiveWidth();
            
            float tireWearFLf = telemData.getTireWear( Wheel.FRONT_LEFT );
            int tireWearFL = Math.round( tireWearFLf * 100f );
            if ( needsCompleteRedraw || ( clock1 && ( tireWearFL != oldTireWear[0] ) ) )
            {
                oldTireWear[0] = tireWearFL;
                
                CompoundWheel wheel = setup.getGeneral().getFrontTireCompound().getWheel( Wheel.FRONT_LEFT );
                
                float gripf = wheel.getWearGripFactor( tireWearFLf );
                int grip = Math.round( gripf * 100f );
                
                int top = 0, left = 0;
                if ( getDisplayWearPercent_tires() )
                {
                    String string = String.valueOf( tireWearFL );
                    tireWearFLString.draw( offsetX, offsetY, string, backgroundColor, image );
                    string = String.valueOf( grip );
                    tireGripFLString.draw( offsetX, offsetY, string, backgroundColor, image );
                    
                    left = tireWearFLString.getAbsX() + 3;
                    top = tireWearFLString.getAbsY() + 2;
                }
                
                drawTire( tireWearFLf, gripf, wheel, image, offsetX + left, offsetY + top );
            }
            
            float tireWearFRf = telemData.getTireWear( Wheel.FRONT_RIGHT );
            int tireWearFR = Math.round( tireWearFRf * 100f );
            if ( needsCompleteRedraw || ( clock1 && ( tireWearFR != oldTireWear[1] ) ) )
            {
                oldTireWear[1] = tireWearFR;
                
                CompoundWheel wheel = setup.getGeneral().getFrontTireCompound().getWheel( Wheel.FRONT_RIGHT );
                
                float gripf = wheel.getWearGripFactor( tireWearFRf );
                int grip = Math.round( gripf * 100f );
                
                int top = 0, left = 0;
                if ( getDisplayWearPercent_tires() )
                {
                    String string = String.valueOf( tireWearFR );
                    tireWearFRString.draw( offsetX, offsetY, string, backgroundColor, image );
                    string = String.valueOf( grip );
                    tireGripFRString.draw( offsetX, offsetY, string, backgroundColor, image );
                    
                    left = tireWearFRString.getAbsX() - tireWidth - 3;
                    top = tireWearFRString.getAbsY() + 2;
                }
                
                drawTire( tireWearFRf, gripf, wheel, image, offsetX + left, offsetY + top );
            }
            
            float tireWearRLf = telemData.getTireWear( Wheel.REAR_LEFT );
            int tireWearRL = Math.round( tireWearRLf * 100f );
            if ( needsCompleteRedraw || ( clock1 && ( tireWearRL != oldTireWear[2] ) ) )
            {
                oldTireWear[2] = tireWearRL;
                
                CompoundWheel wheel = setup.getGeneral().getRearTireCompound().getWheel( Wheel.REAR_LEFT );
                
                float gripf = wheel.getWearGripFactor( tireWearRLf );
                int grip = Math.round( gripf * 100f );
                
                int top = 0, left = 0;
                if ( getDisplayWearPercent_tires() )
                {
                    String string = String.valueOf( tireWearRL );
                    tireWearRLString.draw( offsetX, offsetY, string, backgroundColor, image );
                    string = String.valueOf( grip );
                    tireGripRLString.draw( offsetX, offsetY, string, backgroundColor, image );
                    
                    left = tireWearRLString.getAbsX() + 3;
                    top = tireWearRLString.getAbsY() + 2;
                }
                
                drawTire( tireWearRLf, gripf, wheel, image, offsetX + left, offsetY + top );
            }
            
            float tireWearRRf = telemData.getTireWear( Wheel.REAR_RIGHT );
            int tireWearRR = Math.round( tireWearRRf * 100f );
            if ( needsCompleteRedraw || ( clock1 && ( tireWearRR != oldTireWear[3] ) ) )
            {
                oldTireWear[3] = tireWearRR;
                
                CompoundWheel wheel = setup.getGeneral().getRearTireCompound().getWheel( Wheel.REAR_RIGHT );
                
                float gripf = wheel.getWearGripFactor( tireWearRRf );
                int grip = Math.round( gripf * 100f );
                
                int top = 0, left = 0;
                if ( getDisplayWearPercent_tires() )
                {
                    String string = String.valueOf( tireWearRR );
                    tireWearRRString.draw( offsetX, offsetY, string, backgroundColor, image );
                    string = String.valueOf( grip );
                    tireGripRRString.draw( offsetX, offsetY, string, backgroundColor, image );
                    
                    left = tireWearRRString.getAbsX() - tireWidth - 3;
                    top = tireWearRRString.getAbsY() + 2;
                }
                
                drawTire( tireWearRRf, gripf, wheel, image, offsetX + left, offsetY + top );
            }
        }
        
        if ( displayBrakes.getBooleanValue() )
        {
            final int brakeWidth = brakeSize.getEffectiveWidth();
            
            Wheel wheel = Wheel.FRONT_LEFT;
            VehiclePhysics.Brakes.WheelBrake brake = physics.getBrakes().getBrake( wheel );
            float brakeDiscThickness = isEditorMode ? editorPresets.getBrakeDiscThicknessFL() : telemData.getBrakeDiscThickness( wheel );
            brakeDiscWearFL.update( ( brakeDiscThickness - brake.getMaxDiscFailure() ) / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() ) );
            
            if ( needsCompleteRedraw || ( clock1 && brakeDiscWearFL.hasChanged( false ) ) )
            {
                brakeDiscWearFL.setUnchanged();
                
                int left = 0, top = 0;
                if ( getDisplayWearPercent_brakes() )
                {
                    String string = NumberUtil.formatFloat( brakeDiscWearFL.getValue() * 100f, 1, true );
                    brakeWearFLString.draw( offsetX, offsetY, string, backgroundColor, image );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceFLString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, image );
                    
                    left = brakeWearFLString.getAbsX() + 3;
                    top = brakeWearFLString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, image, offsetX + left, offsetY + top );
            }
            
            wheel = Wheel.FRONT_RIGHT;
            brake = physics.getBrakes().getBrake( wheel );
            brakeDiscThickness = isEditorMode ? editorPresets.getBrakeDiscThicknessFR() : telemData.getBrakeDiscThickness( wheel );
            brakeDiscWearFR.update( ( brakeDiscThickness - brake.getMaxDiscFailure() ) / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() ) );
            
            if ( needsCompleteRedraw || ( clock1 && brakeDiscWearFR.hasChanged( false ) ) )
            {
                brakeDiscWearFR.setUnchanged();
                
                int top = 0, left = 0;
                if ( getDisplayWearPercent_brakes() )
                {
                    String string = NumberUtil.formatFloat( brakeDiscWearFR.getValue() * 100f, 1, true );
                    brakeWearFRString.draw( offsetX, offsetY, string, backgroundColor, image );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceFRString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, image );
                    
                    left = brakeWearFRString.getAbsX() - brakeWidth - 3;
                    top = brakeWearFRString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, image, offsetX + left, offsetY + top );
            }
            
            wheel = Wheel.REAR_LEFT;
            brake = physics.getBrakes().getBrake( wheel );
            brakeDiscThickness = isEditorMode ? editorPresets.getBrakeDiscThicknessRL() : telemData.getBrakeDiscThickness( wheel );
            brakeDiscWearRL.update( ( brakeDiscThickness - brake.getMaxDiscFailure() ) / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() ) );
            
            if ( needsCompleteRedraw || ( clock1 && brakeDiscWearRL.hasChanged( false ) ) )
            {
                brakeDiscWearRL.setUnchanged();
                
                int top = 0, left = 0;
                if ( getDisplayWearPercent_brakes() )
                {
                    String string = NumberUtil.formatFloat( brakeDiscWearRL.getValue() * 100f, 1, true );
                    brakeWearRLString.draw( offsetX, offsetY, string, backgroundColor, image );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceRLString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, image );
                    
                    left = brakeWearRLString.getAbsX() + 3;
                    top = brakeWearRLString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, image, offsetX + left, offsetY + top );
            }
            
            wheel = Wheel.REAR_RIGHT;
            brake = physics.getBrakes().getBrake( wheel );
            brakeDiscThickness = isEditorMode ? editorPresets.getBrakeDiscThicknessRR() : telemData.getBrakeDiscThickness( wheel );
            brakeDiscWearRR.update( ( brakeDiscThickness - brake.getMaxDiscFailure() ) / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() ) );
            
            if ( needsCompleteRedraw || ( clock1 && brakeDiscWearRR.hasChanged( false ) ) )
            {
                brakeDiscWearRR.setUnchanged();
                
                int top = 0, left = 0;
                if ( getDisplayWearPercent_brakes() )
                {
                    String string = NumberUtil.formatFloat( brakeDiscWearRR.getValue() * 100f, 1, true );
                    brakeWearRRString.draw( offsetX, offsetY, string, backgroundColor, image );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceRRString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, image );
                    
                    left = brakeWearRRString.getAbsX() - brakeWidth - 3;
                    top = brakeWearRRString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, image, offsetX + left, offsetY + top );
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
        writer.writeProperty( "engineHeight", Size.unparseValue( engineHeight.getHeight() ), "The height of the engine bar." );
        writer.writeProperty( hundredPercentBase, "The value range to be used as 100% base." );
        writer.writeProperty( displayTires, "Display the tire part of the Widget?" );
        writer.writeProperty( displayWearPercent, "Display wear in percentage numbers?" );
        writer.writeProperty( displayCompoundName, "Display the tire compound name in the header?" );
        writer.writeProperty( "tireWidth", Size.unparseValue( tireSize.getWidth() ), "The width of a tire image." );
        writer.writeProperty( "tireHeight", Size.unparseValue( tireSize.getHeight() ), "The height of a tire image." );
        writer.writeProperty( displayBrakes, "Display the brakes of the Widget?" );
        writer.writeProperty( "brakeWidth", Size.unparseValue( brakeSize.getWidth() ), "The width of a brake image." );
        writer.writeProperty( "brakeHeight", Size.unparseValue( brakeSize.getHeight() ), "The height of a brake image." );
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
        else if ( key.equals( "engineHeight" ) )
            this.engineHeight.setHeight( Size.parseValue( value ) );
        else if ( hundredPercentBase.loadProperty( key, value ) );
        else if ( displayTires.loadProperty( key, value ) );
        else if ( displayWearPercent.loadProperty( key, value ) );
        else if ( displayCompoundName.loadProperty( key, value ) );
        else if ( tireSize.loadProperty( key, value, "tireWidth", "tireHeight" ) );
        else if ( displayBrakes.loadProperty( key, value ) );
        else if ( brakeSize.loadProperty( key, value, "brakeWidth", "brakeHeight" ) );
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
        propsCont.addProperty( engineHeight.createHeightProperty( "engineHeight" ) );
        propsCont.addProperty( hundredPercentBase );
        
        propsCont.addProperty( displayTires );
        propsCont.addProperty( displayWearPercent );
        propsCont.addProperty( displayCompoundName );
        propsCont.addProperty( tireSize.createWidthProperty( "tireWidth" ) );
        propsCont.addProperty( tireSize.createHeightProperty( "tireHeight" ) );
        
        propsCont.addProperty( displayBrakes );
        propsCont.addProperty( brakeSize.createWidthProperty( "brakeWidth" ) );
        propsCont.addProperty( brakeSize.createHeightProperty( "brakeHeight" ) );
    }
    
    public WearWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.178125f, Size.PERCENT_OFFSET + 0.30416667f );
        
        this.engineHeight = new Size( Size.PERCENT_OFFSET + 0, Size.PERCENT_OFFSET + 0.1f, this );
        this.tireSize = new Size( Size.PERCENT_OFFSET + 0.1f, Size.PERCENT_OFFSET + 0.1f, this );
        this.brakeSize = new Size( Size.PERCENT_OFFSET + 0.07f, Size.PERCENT_OFFSET + 0.2f, this );
    }
}
