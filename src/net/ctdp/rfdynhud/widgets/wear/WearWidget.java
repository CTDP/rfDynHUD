package net.ctdp.rfdynhud.widgets.wear;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
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
import net.ctdp.rfdynhud.widgets._util.FontUtils;
import net.ctdp.rfdynhud.widgets._util.Size;
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
    private String fontKey2 = "SmallerFont";
    private Font font2 = null;
    
    private final Size engineHeight;
    
    private final Size tireSize;
    private final Size brakeSize;
    
    private boolean displayEngine = true;
    private boolean displayTires = true;
    private boolean displayBrakes = true;
    
    private boolean displayWearPercent = true;
    private boolean displayCompoundName = true;
    
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
    
    private int[] oldTireWear = { -1, -1, -1, -1 };
    
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
    
    public void setFont2( String font )
    {
        this.fontKey2 = font;
        this.font2 = null;
        
        forceAndSetDirty();
    }
    
    public final void setFont2( Font font, boolean virtual )
    {
        setFont2( FontUtils.getFontString( font, virtual ) );
    }
    
    public final Font getFont2()
    {
        font2 = FontProperty.getFontFromFontKey( fontKey2, font2, getConfiguration() );
        
        return ( font2 );
    }
    
    public void setDisplayEngine( boolean display )
    {
        this.displayEngine = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayEngine()
    {
        return ( displayEngine );
    }
    
    public void setDisplayTires( boolean display )
    {
        this.displayTires = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayTires()
    {
        return ( displayTires );
    }
    
    public void setDisplayBrakes( boolean display )
    {
        this.displayBrakes = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayBrakes()
    {
        return ( displayBrakes );
    }
    
    public void setDisplayWearPercent( boolean display )
    {
        this.displayWearPercent = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayWearPercent()
    {
        return ( displayWearPercent );
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
    
    public void setDisplayCompoundName( boolean display )
    {
        this.displayCompoundName = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayCompoundName()
    {
        return ( displayCompoundName );
    }
    
    public void setEngineHeight( float height )
    {
        this.engineHeight.setHeight( height );
    }
    
    public final float getEngineHeight()
    {
        return ( engineHeight.getHeight() );
    }
    
    public void setTireWidth( float width )
    {
        this.tireSize.setWidth( width );
    }
    
    public final float getTireWidth()
    {
        return ( tireSize.getWidth() );
    }
    
    public void setTireHeight( float height )
    {
        this.tireSize.setHeight( height );
    }
    
    public final float getTireHeight()
    {
        return ( tireSize.getHeight() );
    }
    
    public void setBrakeWidth( float width )
    {
        this.brakeSize.setWidth( width );
    }
    
    public final float getBrakeWidth()
    {
        return ( brakeSize.getWidth() );
    }
    
    public void setBrakeHeight( float height )
    {
        this.brakeSize.setHeight( height );
    }
    
    public final float getBrakeHeight()
    {
        return ( brakeSize.getHeight() );
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
    protected boolean checkForChanges( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Font font = getFont();
        final java.awt.Font font2 = getFont2();
        final java.awt.Color fontColor = getFontColor();
        
        final int tireWidth = tireSize.getEffectiveWidth();
        final int tireHeight = tireSize.getEffectiveHeight();
        final int brakeWidth = brakeSize.getEffectiveWidth();
        final int brakeHeight = brakeSize.getEffectiveHeight();
        
        int left = 2;
        int center = width / 2;
        int top = -2;
        DrawnString relY = null;
        
        if ( getDisplayEngine() )
        {
            engineHeaderString = new DrawnString( left, top, Alignment.LEFT, false, font, fontColor, "Engine: ", null, null );
            if ( getDisplayWearPercent_engine() )
            {
                engineWearString = new DrawnString( null, engineHeaderString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font, fontColor, null, null, "%" );
                engineVarianceString = new DrawnString( null, engineWearString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font2, fontColor, "(", null, "%)" );
            }
            else
            {
                engineWearString = null;
                engineVarianceString = null;
            }
            
            relY = engineHeaderString;
            top = engineHeight.getEffectiveHeight() + 10;
        }
        
        int imgWidth = getDisplayTires() && getDisplayBrakes() ? Math.max( tireWidth, brakeWidth ) : ( getDisplayTires() ? tireWidth : brakeWidth );
        
        if ( getDisplayTires() )
        {
            tiresHeaderString = new DrawnString( null, relY, left, top, Alignment.LEFT, false, font, fontColor, "Tire wear/grip", null, null );
            if ( getDisplayWearPercent_tires() )
            {
                tireWearFLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3, Alignment.RIGHT, false, font, fontColor, null, null, "%" );
                tireWearFRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3, Alignment.LEFT, false, font, fontColor, null, null, "%" );
                tireWearRLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight + 7, Alignment.RIGHT, false, font, fontColor, null, null, "%" );
                tireWearRRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight + 7, Alignment.LEFT, false, font, fontColor, null, null, "%" );
                tireGripFLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight - 2, Alignment.RIGHT, true, font2, fontColor, "(", null, "%)" );
                tireGripFRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight - 2, Alignment.LEFT, true, font2, fontColor, "(", null, "%)" );
                tireGripRLString = new DrawnString( null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight - 2 + tireHeight + 7, Alignment.RIGHT, true, font2, fontColor, "(", null, "%)" );
                tireGripRRString = new DrawnString( null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight - 2 + tireHeight + 7, Alignment.LEFT, true, font2, fontColor, "(", null, "%)" );
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
        
        if ( getDisplayBrakes() )
        {
            brakesHeaderString = new DrawnString( null, relY, left, top, Alignment.LEFT, false, font, fontColor, "Brakes:" );
            if ( getDisplayWearPercent_brakes() )
            {
                brakeWearFLString = new DrawnString( null, brakesHeaderString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font, fontColor, null, null, "%" );
                brakeWearFRString = new DrawnString( null, brakesHeaderString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font, fontColor, null, null, "%" );
                brakeWearRLString = new DrawnString( null, brakesHeaderString, center - 7 - imgWidth, 2 + brakeHeight + 7, Alignment.RIGHT, false, font, fontColor, null, null, "%" );
                brakeWearRRString = new DrawnString( null, brakesHeaderString, center + 7 + imgWidth, 2 + brakeHeight + 7, Alignment.LEFT, false, font, fontColor, null, null, "%" );
                brakeWearVarianceFLString = new DrawnString( null, brakeWearFLString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font2, fontColor, "(", null, "%)" );
                brakeWearVarianceFRString = new DrawnString( null, brakeWearFRString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font2, fontColor, "(", null, "%)" );
                brakeWearVarianceRLString = new DrawnString( null, brakeWearRLString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font2, fontColor, "(", null, "%)" );
                brakeWearVarianceRRString = new DrawnString( null, brakeWearRRString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font2, fontColor, "(", null, "%)" );
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
    
    private void drawEngine( float wear, float lifetime, double raceLengthMultiplier, VehiclePhysics.Engine engine, TextureImage2D image, int x, int y, int width )
    {
        final int w = width;
        final int h = engineHeight.getEffectiveHeight();
        
        final float minLifetime = engine.getMinLifetime( raceLengthMultiplier );
        final float maxLifetime = engine.getMaxLifetime( raceLengthMultiplier );
        final float variance = engine.getLifetimeVariance( raceLengthMultiplier );
        final boolean hasVariance = ( Math.abs( engine.getLifetimeVariance( 1.0 ) ) >= 0.02f );
        
        lifetime = Math.max( -variance * 2f, lifetime );
        
        if ( hasVariance )
        {
            if ( lifetime >= 0.0f )
            {
                int w2 = (int)( ( variance * 2 ) * w / maxLifetime );
                image.clear( Color.RED, x, y, w2, h, false, null );
                
                int w3 = (int)( lifetime * w / maxLifetime );
                image.clear( Color.GREEN, x + w2, y, w3, h, false, null );
                
                int w4 = w - w3 - w2;
                if ( w4 > 0 )
                    image.clear( Color.BLACK, x + w2 + w3, y, w4, h, false, null );
            }
            else
            {
                int w2 = (int)( ( lifetime + variance * 2 ) * w / maxLifetime );
                image.clear( Color.RED, x, y, w2, h, false, null );
                
                int x2 = (int)( ( variance * 2 ) * w / maxLifetime );
                
                int w3 = w - w2;
                if ( w3 > 0 )
                    image.clear( Color.BLACK, x + w2, y, w3, h, false, null );
                
                image.getTextureCanvas().setColor( Color.RED );
                image.getTextureCanvas().drawLine( x + x2, y, x + x2, y + h - 1 );
            }
        }
        else
        {
            float lifetime_ = Math.max( 0f, lifetime );
            
            int w2 = (int)( lifetime_ * w / minLifetime );
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
        final float variance = brake.getDiscFailureVariance();
        discThickness = Math.max( minDiscFailure, discThickness );
        final float positiveRange = startDiscThickness - maxDiscFailure;
        final float maxRange = startDiscThickness - minDiscFailure;
        final boolean hasVariance = ( Math.abs( brake.getDiscFailureVariance() ) >= 0.0000001f );
        
        if ( hasVariance )
        {
            if ( discThickness >= maxDiscFailure )
            {
                int h2 = (int)( ( variance * 2 ) * h / maxRange );
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
                
                int y2 = h - (int)( ( variance * 2 ) * h / maxRange );
                
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
    protected void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
        final TextureImage2D image = texCanvas.getImage();
        final Color backgroundColor = getBackgroundColor();
        
        final VehiclePhysics physics = gameData.getPhysics();
        final TelemetryData telemData = gameData.getTelemetryData();
        final VehicleSetup setup = gameData.getSetup();
        
        //final int center = width / 2;
        
        if ( needsCompleteRedraw )
        {
            if ( getDisplayEngine() )
                engineHeaderString.draw( offsetX, offsetY, "(" + NumberUtil.formatFloat( physics.getEngine().getOptimumOilTemperature(), 1, true ) + "°C)", backgroundColor, image );
            if ( getDisplayTires() )
            {
                if ( getDisplayCompoundName() )
                    tiresHeaderString.draw( offsetX, offsetY, ": " + setup.getGeneral().getFrontTireCompound().getName(), backgroundColor, image );
                else
                    tiresHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
            }
            if ( getDisplayBrakes() )
                brakesHeaderString.draw( offsetX, offsetY, "Brakes:", backgroundColor, image );
        }
        
        if ( getDisplayEngine() )
        {
            float lifetime = isEditorMode ? 1000f : gameData.getTelemetryData().getEngineLifetime();
            engineLifetime.update( lifetime / physics.getEngine().getMinLifetime( gameData.getScoringInfo().getRaceLengthPercentage() ) );
            
            if ( needsCompleteRedraw || engineLifetime.hasChanged() )
            {
                int engineWidth;
                if ( getDisplayWearPercent_engine() )
                {
                    engineWearString.draw( offsetX, offsetY, NumberUtil.formatFloat( engineLifetime.getValue() * 100f, 1, true ), backgroundColor, image );
                    float variancePercent = physics.getEngine().getLifetimeVariance( gameData.getScoringInfo().getRaceLengthPercentage() ) * 200f / physics.getEngine().getMinLifetime( gameData.getScoringInfo().getRaceLengthPercentage() );
                    if ( variancePercent > 0.001f )
                        engineVarianceString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, image );
                    
                    engineWidth = Math.max( engineVarianceString.getLastWidth(), engineWearString.getLastWidth() );
                    engineWidth = engineWearString.getAbsX() - engineWidth - 5;
                }
                else
                {
                    engineWidth = width - ( engineHeaderString.getAbsX() * 2 );
                }
                
                drawEngine( engineLifetime.getValue(), lifetime, gameData.getScoringInfo().getRaceLengthPercentage(), physics.getEngine(), image, offsetX + engineHeaderString.getAbsX(), offsetY + engineHeaderString.getAbsY() + engineHeaderString.getMaxHeight( false ) + 3, engineWidth );
            }
        }
        
        if ( getDisplayTires() )
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
        
        if ( getDisplayBrakes() )
        {
            final int brakeWidth = brakeSize.getEffectiveWidth();
            
            Wheel wheel = Wheel.FRONT_LEFT;
            VehiclePhysics.Brakes.WheelBrake brake = physics.getBrakes().getBrake( wheel );
            float brakeDiscThickness = isEditorMode ? 0.021f : telemData.getBrakeDiscThickness( wheel );
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
            brakeDiscThickness = isEditorMode ? 0.0145f : telemData.getBrakeDiscThickness( wheel );
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
            brakeDiscThickness = isEditorMode ? 0.018f : telemData.getBrakeDiscThickness( wheel );
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
            brakeDiscThickness = isEditorMode ? 0.022f : telemData.getBrakeDiscThickness( wheel );
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
        
        writer.writeProperty( "font2", fontKey2, "The used (smaller) font." );
        writer.writeProperty( "displayEngine", getDisplayEngine(), "Display the engine part of the Widget?" );
        writer.writeProperty( "engineHeight", Size.unparseValue( engineHeight.getHeight() ), "The height of the engine bar." );
        writer.writeProperty( "displayTires", getDisplayTires(), "Display the tire part of the Widget?" );
        writer.writeProperty( "displayWearPercent", getDisplayWearPercent(), "Display wear in percentage numbers?" );
        writer.writeProperty( "displayCompoundName", getDisplayCompoundName(), "Display the tire compound name in the header?" );
        writer.writeProperty( "tireWidth", Size.unparseValue( tireSize.getWidth() ), "The width of a tire image." );
        writer.writeProperty( "tireHeight", Size.unparseValue( tireSize.getHeight() ), "The height of a tire image." );
        writer.writeProperty( "displayBrakes", getDisplayBrakes(), "Display the brakes of the Widget?" );
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
        
        if ( key.equals( "font2" ) )
            this.fontKey2 = value;
        
        else if ( key.equals( "displayEngine" ) )
            this.displayEngine = Boolean.parseBoolean( value );
        
        else if ( key.equals( "engineHeight" ) )
            this.engineHeight.setHeight( Size.parseValue( value ) );
        
        else if ( key.equals( "displayTires" ) )
            this.displayTires = Boolean.parseBoolean( value );
        
        else if ( key.equals( "displayWearPercent" ) )
            this.displayWearPercent = Boolean.parseBoolean( value );
        
        else if ( key.equals( "displayCompoundName" ) )
            this.displayCompoundName = Boolean.parseBoolean( value );
        
        else if ( key.equals( "tireWidth" ) )
            this.tireSize.setWidth( Size.parseValue( value ) );
        
        else if ( key.equals( "tireHeight" ) )
            this.tireSize.setHeight( Size.parseValue( value ) );
        
        else if ( key.equals( "displayBrakes" ) )
            this.displayBrakes = Boolean.parseBoolean( value );
        
        else if ( key.equals( "brakeWidth" ) )
            this.brakeSize.setWidth( Size.parseValue( value ) );
        
        else if ( key.equals( "brakeHeight" ) )
            this.brakeSize.setHeight( Size.parseValue( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList superProps = (FlaggedList)propsList.get( propsList.size() - 1 );
        
        superProps.add( new FontProperty( "font2", getConfiguration() )
        {
            @Override
            public void setValue( Object value )
            {
                setFont2( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( fontKey2 );
            }
        } );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( new Property( "displayEngine", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayEngine( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayEngine() );
            }
        } );
        
        props.add( engineHeight.createHeightProperty( "engineHeight" ) );
        
        props.add( new Property( "displayTires", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayTires( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayTires() );
            }
        } );
        
        props.add( new Property( "displayWearPercent", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayWearPercent( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayWearPercent() );
            }
        } );
        
        props.add( new Property( "displayCompoundName", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayCompoundName( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayCompoundName() );
            }
        } );
        
        props.add( tireSize.createWidthProperty( "tireWidth" ) );
        
        props.add( tireSize.createHeightProperty( "tireHeight" ) );
        
        props.add( new Property( "displayBrakes", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayBrakes( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayBrakes() );
            }
        } );
        
        props.add( brakeSize.createWidthProperty( "brakeWidth" ) );
        
        props.add( brakeSize.createHeightProperty( "brakeHeight" ) );
        
        propsList.add( props );
    }
    
    public WearWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.178125f, Size.PERCENT_OFFSET + 0.30416667f );
        
        this.engineHeight = new Size( Size.PERCENT_OFFSET + 0, Size.PERCENT_OFFSET + 0.1f, this );
        this.tireSize = new Size( Size.PERCENT_OFFSET + 0.1f, Size.PERCENT_OFFSET + 0.1f, this );
        this.brakeSize = new Size( Size.PERCENT_OFFSET + 0.07f, Size.PERCENT_OFFSET + 0.2f, this );
    }
}
