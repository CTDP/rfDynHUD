package net.ctdp.rfdynhud.widgets.wear;

import java.awt.Color;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound.CompoundWheel;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.ByteOrderManager;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
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
    
    private final FontProperty font2 = new FontProperty( this, "font2", FontProperty.SMALLER_FONT_NAME );
    
    private final Size engineHeight;
    
    private final Size tireSize;
    private final Size brakeSize;
    
    private final BooleanProperty displayEngine = new BooleanProperty( this, "displayEngine", true );
    private final BooleanProperty displayTires = new BooleanProperty( this, "displayTires", true );
    private final BooleanProperty displayBrakes = new BooleanProperty( this, "displayBrakes", true );
    
    private Boolean displayBrakes2 = null;
    
    private final EnumProperty<HundredPercentBase> hundredPercentBase = new EnumProperty<HundredPercentBase>( this, "hundredPercentBase", HundredPercentBase.SAFE_RANGE );
    
    private final ImageProperty estimationImageName = new ImageProperty( this, "engineEstimationImage", "estimationImage", "start_finish.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            estimationTexture = null;
        }
    };
    private TextureImage2D estimationTexture = null;
    
    private final BooleanProperty displayWearPercent = new BooleanProperty( this, "displayWearPercent", true );
    private final BooleanProperty displayCompoundName = new BooleanProperty( this, "displayCompoundName", true );
    
    private DrawnString engineHeaderString = null;
    private DrawnString engineWearString = null;
    private DrawnString engineVarianceString = null;
    
    private final BooleanProperty swapTireWearGripMeaning = new BooleanProperty( this, "swapTireWearGripMeaning", "swapTireWearGrip", false );
    
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
    
    private static final float wearGood = 0.5f;
    private static final float wearBad = 0.25f;
    
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
    
    private float engineLifetimeAtLapStart = -1f;
    private float engineLifetimeLossPerLap = -1f;
    
    @Override
    public String getWidgetPackage()
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
    
    private void setControlVisibility( VehicleScoringInfo viewedVSI )
    {
        setUserVisible1( viewedVSI.isPlayer() );
        
        displayBrakes2 = displayBrakes.getBooleanValue() && viewedVSI.getVehicleControl().isLocalPlayer();
        forceReinitialization();
        forceCompleteRedraw();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        
        setControlVisibility( gameData.getScoringInfo().getViewedVehicleScoringInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        for ( int i = 0; i < oldTireWear.length; i++ )
            oldTireWear[i] = -1;
        
        engineLifetimeAtLapStart = -1f;
        engineLifetimeLossPerLap = -1f;
        
        //forceReinitialization();
        
        displayBrakes2 = null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, editorPresets );
        
        setControlVisibility( viewedVSI );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onLapStarted( vsi, gameData, editorPresets );
        
        VehicleScoringInfo pvsi = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
        
        if ( vsi.equals( pvsi ) && ( vsi.getLapsCompleted() >= 1 ) )
        {
            if ( engineLifetimeAtLapStart < 0f )
            {
                engineLifetimeAtLapStart = gameData.getTelemetryData().getEngineLifetime();
                engineLifetimeLossPerLap = -1f;
            }
            else
            {
                engineLifetimeLossPerLap = engineLifetimeAtLapStart - gameData.getTelemetryData().getEngineLifetime();
                engineLifetimeAtLapStart = gameData.getTelemetryData().getEngineLifetime();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
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
            engineHeaderString = dsf.newDrawnString( "engineHeaderString", left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            if ( getDisplayWearPercent_engine() )
            {
                engineWearString = dsf.newDrawnString( "engineWearString", null, engineHeaderString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                engineVarianceString = dsf.newDrawnString( "engineVarianceString", null, engineWearString, width - getBorder().getPaddingRight(), 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
            }
            else
            {
                engineWearString = null;
                engineVarianceString = null;
            }
            
            relY = engineHeaderString;
            top = engineHeight.getEffectiveHeight() + 10;
        }
        
        boolean db = ( displayBrakes2 == null ) ? displayBrakes.getBooleanValue() : displayBrakes2.booleanValue();
        
        int imgWidth = displayTires.getBooleanValue() && db ? Math.max( tireWidth, brakeWidth ) : ( displayTires.getBooleanValue() ? tireWidth : brakeWidth );
        
        if ( displayTires.getBooleanValue() )
        {
            tiresHeaderString = dsf.newDrawnString( "tiresHeaderString", null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.tires_header_prefix + ":", null );
            if ( getDisplayWearPercent_tires() )
            {
                tireWearFLString = dsf.newDrawnString( "tireWearFLString", null, tiresHeaderString, center - 7 - tireWidth, 3, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireWearFRString = dsf.newDrawnString( "tireWearFRString", null, tiresHeaderString, center + 7 + tireWidth, 3, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireWearRLString = dsf.newDrawnString( "tireWearRLString", null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireWearRRString = dsf.newDrawnString( "tireWearRRString", null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                tireGripFLString = dsf.newDrawnString( "tireGripFLString", null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight - 2, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
                tireGripFRString = dsf.newDrawnString( "tireGripFRString", null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight - 2, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
                tireGripRLString = dsf.newDrawnString( "tireGripRLString", null, tiresHeaderString, center - 7 - tireWidth, 3 + tireHeight - 2 + tireHeight + 7, Alignment.RIGHT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
                tireGripRRString = dsf.newDrawnString( "tireGripRRString", null, tiresHeaderString, center + 7 + tireWidth, 3 + tireHeight - 2 + tireHeight + 7, Alignment.LEFT, true, font2, font2AntiAliased, fontColor, "(", "%)" );
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
        
        if ( db )
        {
            brakesHeaderString = dsf.newDrawnString( "brakesHeaderString", null, relY, left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            if ( getDisplayWearPercent_brakes() )
            {
                brakeWearFLString = dsf.newDrawnString( "brakeWearFLString", null, brakesHeaderString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearFRString = dsf.newDrawnString( "brakeWearFRString", null, brakesHeaderString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearRLString = dsf.newDrawnString( "brakeWearRLString", null, brakesHeaderString, center - 7 - imgWidth, 2 + brakeHeight + 7, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearRRString = dsf.newDrawnString( "brakeWearRRString", null, brakesHeaderString, center + 7 + imgWidth, 2 + brakeHeight + 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, null, "%" );
                brakeWearVarianceFLString = dsf.newDrawnString( "brakeWearVarianceFLString", null, brakeWearFLString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
                brakeWearVarianceFRString = dsf.newDrawnString( "brakeWearVarianceFRString", null, brakeWearFRString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
                brakeWearVarianceRLString = dsf.newDrawnString( "brakeWearVarianceRLString", null, brakeWearRLString, center - 7 - imgWidth, 2, Alignment.RIGHT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
                brakeWearVarianceRRString = dsf.newDrawnString( "brakeWearVarianceRRString", null, brakeWearRRString, center + 7 + imgWidth, 2, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, "(", "%)" );
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
    
    private TextureImage2D loadExplodeImage( int height )
    {
        if ( estimationImageName.isNoImage() )
        {
            estimationTexture = null;
            
            return ( estimationTexture );
        }
        
        if ( ( estimationTexture == null ) || ( estimationTexture.getHeight() != height ) )
        {
            ImageTemplate it = estimationImageName.getImage();
            
            int width = Math.round( height * it.getBaseAspect() );
            
            estimationTexture = it.getScaledTextureImage( width, height );
        }
        
        return ( estimationTexture );
    }
    
    private void drawEngine( ScoringInfo scoringInfo, boolean isEditorMode, float lifetime, double raceLengthMultiplier, VehiclePhysics.Engine engine, TextureImage2D texture, final int x, final int y, final int width )
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
                texture.clear( Color.RED, x0, y, w2, h, false, null );
                x0 += w2;
            }
            else
            {
                int w2 = (int)( ( lifetime - minLifetime ) * w / maxLifetimeTotal );
                texture.clear( Color.RED, x0, y, w2, h, false, null );
                x0 += w2;
                x1 = x + ( maxLifetimeTotal - badLifetimeTotal ) * w / maxLifetimeTotal;
            }
            
            if ( lifetime >= lowerGoodLifetime )
            {
                int w2 = ( lowerGoodLifetime - lowerBadLifetime ) * w / maxLifetimeTotal;
                texture.clear( YELLOW2, x0, y, w2, h, false, null );
                x0 += w2;
            }
            else
            {
                if ( lifetime >= lowerBadLifetime )
                {
                    int w2 = (int)( ( lifetime - lowerBadLifetime ) * w / maxLifetimeTotal );
                    texture.clear( YELLOW2, x0, y, w2, h, false, null );
                    x0 += w2;
                }
                
                x2 = x + ( maxLifetimeTotal - goodLifetimeTotal ) * w / maxLifetimeTotal;
            }
            
            if ( lifetime >= lowerSafeLifetime )
            {
                int w2 = ( lowerSafeLifetime - lowerGoodLifetime ) * w / maxLifetimeTotal;
                texture.clear( GREEN2, x0, y, w2, h, false, null );
                x0 += w2;
                
                int w3 = (int)( ( lifetime - lowerSafeLifetime ) * w / maxLifetimeTotal );
                texture.clear( Color.GREEN, x0, y, w3, h, false, null );
                x0 += w3;
            }
            else
            {
                if ( lifetime >= lowerGoodLifetime )
                {
                    int w2 = (int)( ( lifetime - lowerGoodLifetime ) * w / maxLifetimeTotal );
                    texture.clear( GREEN2, x0, y, w2, h, false, null );
                    x0 += w2;
                }
                
                x3 = x + ( maxLifetimeTotal - safeLifetimeTotal ) * w / maxLifetimeTotal;
            }
            
            int w_ = w - x0 + x;
            if ( w_ > 0 )
            {
                texture.clear( Color.BLACK, x0, y, w_, h, false, null );
            }
            
            if ( x1 > 0 )
            {
                texture.getTextureCanvas().setColor( Color.RED );
                texture.getTextureCanvas().drawLine( x1, y, x1, y + h - 1 );
            }
            
            if ( x2 > 0 )
            {
                texture.getTextureCanvas().setColor( YELLOW2 );
                texture.getTextureCanvas().drawLine( x2, y, x2, y + h - 1 );
            }
            
            if ( x3 > 0 )
            {
                texture.getTextureCanvas().setColor( GREEN2 );
                texture.getTextureCanvas().drawLine( x3, y, x3, y + h - 1 );
            }
        }
        else
        {
            int w2 = (int)( lifetime * w / minLifetime );
            texture.clear( Color.GREEN, x, y, w2, h, false, null );
            
            int w3 = w - w2;
            if ( w3 > 0 )
                texture.clear( Color.BLACK, x + w2, y, w3, h, false, null );
        }
        
        TextureImage2D explodeTexture = loadExplodeImage( h );
        if ( explodeTexture != null )
        {
            if ( isEditorMode )
            {
                texture.drawImage( explodeTexture, x + 10, y, false, null );
            }
            else if ( scoringInfo.getSessionType().isRace() && ( engineLifetimeLossPerLap > 0f ) )
            {
                int lapsRemaining = scoringInfo.getMaxLaps() - scoringInfo.getPlayersVehicleScoringInfo().getLapsCompleted();
                int x2 = (int)( ( engineLifetimeAtLapStart - ( engineLifetimeLossPerLap * lapsRemaining ) + maxLifetimeTotal - safeLifetimeTotal ) * w / maxLifetimeTotal );
                x2 -= explodeTexture.getWidth() / 2;
                x2 = Math.max( 0, x2 );
                
                texture.drawImage( explodeTexture, x + x2, y, false, null );
            }
        }
        
        texture.markDirty( x, y, w, h );
    }
    
    private void drawTire( float wear, float grip, CompoundWheel compoundWheel, TextureImage2D texture, int x, int y )
    {
        int w = tireSize.getEffectiveWidth();
        int h = tireSize.getEffectiveHeight();
        
        float barValue;
        float barGood;
        float barBad;
        float lineValue;
        
        if ( swapTireWearGripMeaning.getBooleanValue() )
        {
            barValue = wear;
            barGood = wearGood;
            barBad = wearBad;
            
            final float minGrip = compoundWheel.getMinGrip();
            float barRange = 1.0f - minGrip;
            lineValue = ( grip - minGrip ) / barRange;
        }
        else
        {
            final float minGrip = compoundWheel.getMinGrip();
            float barRange = 1.0f - minGrip;
            barValue = ( grip - minGrip ) / barRange;
            barGood = ( gripGood - minGrip ) / barRange;
            barBad = ( gripBad - minGrip ) / barRange;
            
            lineValue = wear;
        }
        
        byte[] color = new byte[4];
        color[ByteOrderManager.ALPHA] = (byte)255;
        if ( barValue <= barBad )
        {
            System.arraycopy( colorBad, 0, color, 0, 3 );
        }
        else if ( barValue < barGood )
        {
            float alpha = ( barValue - barBad ) / ( barGood - barBad );
            interpolateColor( colorBad, colorOk, alpha, color );
        }
        else
        {
            System.arraycopy( colorGood, 0, color, 0, 3 );
        }
        
        Color awtColor = new Color( color[ByteOrderManager.RED] & 0xFF, color[ByteOrderManager.GREEN] & 0xFF, color[ByteOrderManager.BLUE] & 0xFF );
        
        int barHeight = Math.min( (int)( h * barValue ), h );
        
        if ( barValue > 0.0f )
        {
            texture.clear( Color.BLACK, x, y, w, h - barHeight, false, null );
        }
        
        texture.clear( awtColor, x, y + h - barHeight, w, barHeight, false, null );
        
        byte[] pixels = new byte[ w * 4 ];
        for ( int i = 0; i < w; i++ )
        {
            System.arraycopy( colorBad, 0, pixels, i * 4, 4 );
        }
        
        texture.clearPixelLine( pixels, x, y + h - (int)( h * lineValue ), w, false, null );
        
        texture.markDirty( x, y, w, h );
    }
    
    private void drawBrake( float discThickness, Wheel wheel, VehiclePhysics.Brakes.WheelBrake brake, VehicleSetup setup, TextureImage2D texture, int x, int y )
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
                texture.clear( Color.RED, x, y + h - h2, w, h2, false, null );
                
                int h3 = (int)( ( discThickness - maxDiscFailure ) * ( h - h2 ) / positiveRange );
                texture.clear( Color.GREEN, x, y + h - h2 - h3, w, h3, false, null );
                
                int h4 = h - h3 - h2;
                if ( h4 > 0 )
                    texture.clear( Color.BLACK, x, y, w, h4, false, null );
            }
            else
            {
                int h2 = (int)( ( discThickness - minDiscFailure ) * h / maxRange );
                texture.clear( Color.RED, x, y + h - h2, w, h2, false, null );
                
                int y2 = h - (int)( variance * h / maxRange );
                
                int h3 = h - h2;
                if ( h3 > 0 )
                    texture.clear( Color.BLACK, x, y, w, h3, false, null );
                
                texture.getTextureCanvas().setColor( Color.RED );
                texture.getTextureCanvas().drawLine( x, y + y2, x + w - 1, y + y2 );
            }
        }
        else
        {
            int h2 = (int)( ( discThickness - minDiscFailure ) * h / maxRange );
            texture.clear( Color.GREEN, x, y + h - h2, w, h2, false, null );
            
            int h3 = h - h2;
            if ( h3 > 0 )
                texture.clear( Color.BLACK, x, y, w, h3, false, null );
        }
        
        texture.markDirty( x, y, w, h );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final Color backgroundColor = getBackgroundColor();
        
        final VehiclePhysics physics = gameData.getPhysics();
        final TelemetryData telemData = gameData.getTelemetryData();
        final VehicleSetup setup = gameData.getSetup();
        
        //final int center = width / 2;
        
        final boolean isEditorMode = ( editorPresets != null );
        
        boolean db = ( displayBrakes2 == null ) ? displayBrakes.getBooleanValue() : displayBrakes2.booleanValue();
        
        if ( needsCompleteRedraw )
        {
            if ( displayEngine.getBooleanValue() )
                engineHeaderString.draw( offsetX, offsetY, Loc.engine_header_prefix + ":", backgroundColor, texture );
            if ( displayTires.getBooleanValue() )
            {
                if ( displayCompoundName.getBooleanValue() )
                    tiresHeaderString.draw( offsetX, offsetY, " " + setup.getGeneral().getFrontTireCompound().getName(), backgroundColor, texture );
                else
                    tiresHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
            }
            if ( db )
                brakesHeaderString.draw( offsetX, offsetY, Loc.brakes_header_prefix + ":", backgroundColor, texture );
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
                    engineWearString.draw( offsetX, offsetY, NumberUtil.formatFloat( engineLifetime.getValue() * 100f, 1, true ), backgroundColor, texture );
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
                        engineVarianceString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, texture );
                    
                    engineWidth = Math.max( engineVarianceString.getLastWidth(), engineWearString.getLastWidth() );
                    engineWidth = engineWearString.getAbsX() - engineWidth - 5;
                }
                else
                {
                    engineWidth = width - ( engineHeaderString.getAbsX() * 2 );
                }
                
                drawEngine( gameData.getScoringInfo(), isEditorMode, lifetime, raceLengthPercentage, physics.getEngine(), texture, offsetX + engineHeaderString.getAbsX(), offsetY + engineHeaderString.getAbsY() + engineHeaderString.getMaxHeight( false ) + 3, engineWidth );
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
                    tireWearFLString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    string = String.valueOf( grip );
                    tireGripFLString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    
                    left = tireWearFLString.getAbsX() + 3;
                    top = tireWearFLString.getAbsY() + 2;
                }
                
                drawTire( tireWearFLf, gripf, wheel, texture, offsetX + left, offsetY + top );
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
                    tireWearFRString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    string = String.valueOf( grip );
                    tireGripFRString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    
                    left = tireWearFRString.getAbsX() - tireWidth - 3;
                    top = tireWearFRString.getAbsY() + 2;
                }
                
                drawTire( tireWearFRf, gripf, wheel, texture, offsetX + left, offsetY + top );
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
                    tireWearRLString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    string = String.valueOf( grip );
                    tireGripRLString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    
                    left = tireWearRLString.getAbsX() + 3;
                    top = tireWearRLString.getAbsY() + 2;
                }
                
                drawTire( tireWearRLf, gripf, wheel, texture, offsetX + left, offsetY + top );
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
                    tireWearRRString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    string = String.valueOf( grip );
                    tireGripRRString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    
                    left = tireWearRRString.getAbsX() - tireWidth - 3;
                    top = tireWearRRString.getAbsY() + 2;
                }
                
                drawTire( tireWearRRf, gripf, wheel, texture, offsetX + left, offsetY + top );
            }
        }
        
        if ( db )
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
                    brakeWearFLString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceFLString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, texture );
                    
                    left = brakeWearFLString.getAbsX() + 3;
                    top = brakeWearFLString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, texture, offsetX + left, offsetY + top );
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
                    brakeWearFRString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceFRString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, texture );
                    
                    left = brakeWearFRString.getAbsX() - brakeWidth - 3;
                    top = brakeWearFRString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, texture, offsetX + left, offsetY + top );
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
                    brakeWearRLString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceRLString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, texture );
                    
                    left = brakeWearRLString.getAbsX() + 3;
                    top = brakeWearRLString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, texture, offsetX + left, offsetY + top );
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
                    brakeWearRRString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    float variancePercent = brake.getDiscFailureVariance() * 200f / ( setup.getWheelAndTire( wheel ).getBrakeDiscThickness() - brake.getMaxDiscFailure() );
                    if ( variancePercent > 0.000001f )
                        brakeWearVarianceRRString.draw( offsetX, offsetY, NumberUtil.formatFloat( -variancePercent, 1, true ), backgroundColor, texture );
                    
                    left = brakeWearRRString.getAbsX() - brakeWidth - 3;
                    top = brakeWearRRString.getAbsY();
                }
                
                drawBrake( brakeDiscThickness, wheel, brake, setup, texture, offsetX + left, offsetY + top );
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
        engineHeight.saveHeightProperty( "engineHeight", "The height of the engine bar.", writer );
        writer.writeProperty( hundredPercentBase, "The value range to be used as 100% base." );
        writer.writeProperty( displayWearPercent, "Display wear in percentage numbers?" );
        writer.writeProperty( estimationImageName, "Image to display where the engine is expected to explode." );
        
        writer.writeProperty( displayTires, "Display the tire part of the Widget?" );
        writer.writeProperty( displayCompoundName, "Display the tire compound name in the header?" );
        tireSize.saveWidthProperty( "tireWidth", "The width of a tire image.", writer );
        tireSize.saveHeightProperty( "tireHeight", "The height of a tire image.", writer );
        writer.writeProperty( swapTireWearGripMeaning, "Swap bar and line display for wear and grip?" );
        
        writer.writeProperty( displayBrakes, "Display the brakes of the Widget?" );
        brakeSize.saveWidthProperty( "brakeWidth", "The width of a brake image.", writer );
        brakeSize.saveHeightProperty( "brakeHeight", "The height of a brake image.", writer );
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
        else if ( engineHeight.loadProperty( key, value, null, "engineHeight" ) );
        else if ( hundredPercentBase.loadProperty( key, value ) );
        else if ( displayWearPercent.loadProperty( key, value ) );
        else if ( estimationImageName.loadProperty( key, value ) );
        
        else if ( displayTires.loadProperty( key, value ) );
        else if ( displayCompoundName.loadProperty( key, value ) );
        else if ( tireSize.loadProperty( key, value, "tireWidth", "tireHeight" ) );
        else if ( swapTireWearGripMeaning.loadProperty( key, value ) );
        
        else if ( displayBrakes.loadProperty( key, value ) );
        else if ( brakeSize.loadProperty( key, value, "brakeWidth", "brakeHeight" ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addProperty( font2 );
        
        propsCont.addGroup( "Engine" );
        
        propsCont.addProperty( displayEngine );
        propsCont.addProperty( engineHeight.createHeightProperty( "engineHeight" ) );
        propsCont.addProperty( hundredPercentBase );
        propsCont.addProperty( displayWearPercent );
        propsCont.addProperty( estimationImageName );
        
        propsCont.addGroup( "Tires" );
        
        propsCont.addProperty( displayTires );
        propsCont.addProperty( displayCompoundName );
        propsCont.addProperty( tireSize.createWidthProperty( "tireWidth" ) );
        propsCont.addProperty( tireSize.createHeightProperty( "tireHeight" ) );
        propsCont.addProperty( swapTireWearGripMeaning );
        
        propsCont.addGroup( "Brakes" );
        
        propsCont.addProperty( displayBrakes );
        propsCont.addProperty( brakeSize.createWidthProperty( "brakeWidth" ) );
        propsCont.addProperty( brakeSize.createHeightProperty( "brakeHeight" ) );
    }
    
    public WearWidget( String name )
    {
        super( name, 17.8125f, 30.416667f );
        
        this.engineHeight = new Size( 0f, true, 10.0f, true, this );
        this.tireSize = new Size( 10.0f, true, 10.0f, true, this );
        this.brakeSize = new Size( 7.0f, true, 20.0f, true, this );
    }
}
