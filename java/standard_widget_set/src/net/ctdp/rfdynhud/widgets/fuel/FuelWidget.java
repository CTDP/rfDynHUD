package net.ctdp.rfdynhud.widgets.fuel;

import java.awt.Color;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.FuelUsageRecorder;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.ImageProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.ByteOrderManager;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.util.RFactorTools.MeasurementUnits;
import net.ctdp.rfdynhud.values.AbstractSize;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Position;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.ValidityTest;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link FuelWidget} displays fuel information like current fuel load, fuel usage per lap,
 * and computed fuel for scheduled pitstops.
 * 
 * @author Marvin Froehlich
 */
public class FuelWidget extends Widget
{
    private static final InputAction INPUT_ACTION_INC_PITSTOP = new InputAction( "IncPitstopAction" );
    private static final InputAction INPUT_ACTION_DEC_PITSTOP = new InputAction( "DecPitstopAction" );
    
    private final FontProperty font2 = new FontProperty( this, "font2", FontProperty.STANDARD_FONT3_NAME );
    
    private final FontProperty fuelFont = new FontProperty( this, "fuelFont", FontProperty.STANDARD_FONT_NAME );
    
    private DrawnString fuelHeaderString = null;
    
    private final IntProperty fuelBarLeftOffset = new IntProperty( this, "fuelBarLeftOffset", 4 );
    private final Size fuelBarWidth;
    
    private final ColorProperty fuelFontColor = new ColorProperty( this, "fuelFontColor", "#FFFFFFCD" );
    
    private final BooleanProperty roundUpRemainingLaps = new BooleanProperty( this, "roundUpRemainingLaps", false );
    private final IntProperty fuelSafetyPlanning = new IntProperty( this, "fuelSafetyPlanning", 2);
    
    private final ImageProperty lowFuelWarningImageNameOff = new ImageProperty( this, "lowFuelWarningImageOff", "imageOff", "shiftlight_off.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            lowFuelWarningImageOff = null;
        }
    };
    private TransformableTexture lowFuelWarningImageOff = null;
    
    private final ImageProperty lowFuelWarningImageNameOn = new ImageProperty( this, "lowFuelWarningImageOn", "imageOn", "shiftlight_on_red.png", false, true )
    {
        @Override
        protected void onValueChanged( String oldValue, String newValue )
        {
            lowFuelWarningImageOn = null;
        }
    };
    private TransformableTexture lowFuelWarningImageOn = null;
    
    private final AbstractSize lowFuelWarnImgSize = new AbstractSize()
    {
        @Override
        public int getEffectiveWidth()
        {
            loadLowFuelWarningImages( getEffectiveInnerWidth(), getEffectiveInnerHeight() );
            
            if ( lowFuelWarningImageOn == null )
                return ( 0 );
            
            return ( lowFuelWarningImageOn.getWidth() );
        }
        
        @Override
        public int getEffectiveHeight()
        {
            loadLowFuelWarningImages( getEffectiveInnerWidth(), getEffectiveInnerHeight() );
            
            if ( lowFuelWarningImageOn == null )
                return ( 0 );
            
            return ( lowFuelWarningImageOn.getHeight() );
        }
    };
    
    private final Position lowFuelWarningImagePosition = new Position( RelativePositioning.TOP_RIGHT, 4.0f, false, 4.0f, false, lowFuelWarnImgSize, this )
    {
        @Override
        protected void onPositioningPropertySet( RelativePositioning positioning )
        {
            forceReinitialization();
        }
        
        @Override
        protected void onXPropertySet( float x )
        {
            forceReinitialization();
        }
        
        @Override
        protected void onYPropertySet( float y )
        {
            forceReinitialization();
        }
    };
    
    private final Size lowFuelWarningImageSize = new Size( 20.0f, true, 20.0f, true, this )
    {
        @Override
        protected void onWidthPropertySet( float width )
        {
            forceReinitialization();
        }
        
        @Override
        protected void onHeightPropertySet( float height )
        {
            forceReinitialization();
        }
    };
    
    private final IntProperty lowFuelBlinkTime = new IntProperty( this, "lowFuelBlinkTime", "blinkTime", 500, 0, 5000, false )
    {
        @Override
        protected void onValueChanged( int oldValue, int newValue )
        {
            lowFuelBlinkNanos = newValue * 1000000L;
        }
    };
    private long lowFuelBlinkNanos = lowFuelBlinkTime.getIntValue() * 1000000L;
    private long nextBlinkTime = -1L;
    private boolean blinkState = false;
    
    private DrawnString fuelLoadString1 = null;
    private DrawnString fuelLoadString2 = null;
    private DrawnString fuelLoadString3 = null;
    
    private DrawnString fuelUsageHeaderString = null;
    private DrawnString fuelUsageLastLapHeaderString = null;
    private DrawnString fuelUsageAvgHeaderString = null;
    
    private DrawnString fuelUsageLastLapString = null;
    private DrawnString fuelUsageAvgString = null;
    
    private DrawnString nextPitstopHeaderString = null;
    private DrawnString nextPitstopLapString = null;
    private DrawnString nextPitstopFuelString = null;
    
    private int oldFuelRelevantLaps = -1;
    
    private static final byte[] colorFuel = new byte[ 4 ];
    static
    {
        colorFuel[ByteOrderManager.RED] = (byte)84;
        colorFuel[ByteOrderManager.GREEN] = (byte)118;
        colorFuel[ByteOrderManager.BLUE] = (byte)11;
        colorFuel[ByteOrderManager.ALPHA] = (byte)255;
    }
    
    /*
    private static final class StintFuel
    {
        public final float stintLength;
        public final float fuelConsumption;
        
        public StintFuel( float stintLength, float fuelConsumption )
        {
            this.stintLength = stintLength;
            this.fuelConsumption = fuelConsumption;
        }
    }
    */
    
    private int oldNextPitstopLapCorrection = -1;
    private int nextPitstopLapCorrection = 0;
    private int nextPitstopFuelLapsCorrection = 0;
    private final IntValue pitstopFuel = new IntValue( ValidityTest.GREATER_THAN, 0 );
    private final IntValue stintLengthV = new IntValue( ValidityTest.GREATER_THAN, 0 );
    
    private int oldFuel = -1;
    private float oldAverage = -1f;
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputAction[] getInputActions()
    {
        return ( new InputAction[] { INPUT_ACTION_INC_PITSTOP, INPUT_ACTION_DEC_PITSTOP } );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        lowFuelWarningImagePosition.bake();
        fuelBarWidth.bake();
    }
    
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        if ( !lowFuelWarningImagePosition.isXPercentageValue() )
            lowFuelWarningImagePosition.flipXPercentagePx();
        
        if ( !lowFuelWarningImagePosition.isYPercentageValue() )
            lowFuelWarningImagePosition.flipYPercentagePx();
        
        if ( !fuelBarWidth.isWidthPercentageValue() )
            fuelBarWidth.flipWidthPercentagePx();
        
        if ( !fuelBarWidth.isHeightPercentageValue() )
            fuelBarWidth.flipHeightPercentagePx();
    }
    
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        if ( lowFuelWarningImagePosition.isXPercentageValue() )
            lowFuelWarningImagePosition.flipXPercentagePx();
        
        if ( lowFuelWarningImagePosition.isYPercentageValue() )
            lowFuelWarningImagePosition.flipYPercentagePx();
        
        if ( fuelBarWidth.isWidthPercentageValue() )
            fuelBarWidth.flipWidthPercentagePx();
        
        if ( fuelBarWidth.isHeightPercentageValue() )
            fuelBarWidth.flipHeightPercentagePx();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        this.oldFuelRelevantLaps = -1;
    }
    
    private final boolean isLowFuelWaningUsed()
    {
        return ( ( lowFuelBlinkTime.getIntValue() > 0 ) && !lowFuelWarningImageNameOn.isNoImage() );
    }
    
    private void resetBlink( boolean isEditorMode )
    {
        this.nextBlinkTime = -1L;
        this.blinkState = false;
        
        if ( lowFuelWarningImageOff != null )
        {
            lowFuelWarningImageOff.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOff.setVisible( false );
        }
        
        if ( lowFuelWarningImageOn != null )
        {
            lowFuelWarningImageOn.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOn.setVisible( isEditorMode );
        }
    }
    
    private void loadLowFuelWarningImages( int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( !isLowFuelWaningUsed() )
        {
            lowFuelWarningImageOff = null;
            lowFuelWarningImageOn = null;
            
            return;
        }
        
        boolean offVisible = ( lowFuelWarningImageOff == null ) ? false : lowFuelWarningImageOff.isVisible();
        boolean onVisible = ( lowFuelWarningImageOn == null ) ? false : lowFuelWarningImageOn.isVisible();
        
        boolean offReloaded = false;
        
        if ( !lowFuelWarningImageNameOff.isNoImage() )
        {
            ImageTemplate it = lowFuelWarningImageNameOff.getImage();
            
            int h = lowFuelWarningImageSize.getEffectiveHeight();
            int w = Math.round( h * it.getBaseAspect() );
            
            if ( ( lowFuelWarningImageOff == null ) || ( lowFuelWarningImageOff.getWidth() != w ) || ( lowFuelWarningImageOff.getHeight() != h ) )
            {
                lowFuelWarningImageOff = it.getScaledTransformableTexture( w, h );
                
                offReloaded = true;
            }
        }
        
        boolean onReloaded = false;
        
        if ( !lowFuelWarningImageNameOn.isNoImage() )
        {
            ImageTemplate it = lowFuelWarningImageNameOn.getImage();
            
            int h = lowFuelWarningImageSize.getEffectiveHeight();
            int w = Math.round( h * it.getBaseAspect() );
            
            if ( ( lowFuelWarningImageOn == null ) || ( lowFuelWarningImageOn.getWidth() != w ) || ( lowFuelWarningImageOn.getHeight() != h ) )
            {
                lowFuelWarningImageOn = it.getScaledTransformableTexture( w, h );
                
                onReloaded = true;
            }
        }
        
        if ( offReloaded && ( lowFuelWarningImageOff != null ) )
        {
            lowFuelWarningImageOff.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOff.setVisible( offVisible );
        }
        
        if ( onReloaded && ( lowFuelWarningImageOn != null ) )
        {
            lowFuelWarningImageOn.setTranslation( lowFuelWarningImagePosition.getEffectiveX(), lowFuelWarningImagePosition.getEffectiveY() );
            lowFuelWarningImageOn.setVisible( onVisible );
        }
    }
    
    private void setControlVisibility( VehicleScoringInfo viewedVSI )
    {
        setVisible2( viewedVSI.isPlayer() && viewedVSI.getVehicleControl().isLocalPlayer() );
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
        
        loadLowFuelWarningImages( getEffectiveInnerWidth(), getEffectiveInnerHeight() );
        resetBlink( editorPresets != null );
        
        this.nextPitstopLapCorrection = 0;
        this.nextPitstopFuelLapsCorrection = 0;
        this.oldNextPitstopLapCorrection = ( ( nextPitstopLapCorrection + Short.MAX_VALUE / 2 ) << 16 ) | ( nextPitstopFuelLapsCorrection + Short.MAX_VALUE / 2 );
        this.pitstopFuel.reset();
        
        this.stintLengthV.reset();
        this.oldFuel = -1;
        this.oldAverage = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPitsExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onPitsExited( gameData, editorPresets );
        
        if ( stintLengthV.getValue() < 1 )
        {
            this.nextPitstopLapCorrection = 0;
            this.nextPitstopFuelLapsCorrection = 0;
        }
        
        this.stintLengthV.reset();
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
    public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( action == INPUT_ACTION_INC_PITSTOP )
        {
            if ( ( modifierMask & InputAction.MODIFIER_MASK_SHIFT ) == 0 )
            {
                if ( nextPitstopLapCorrection < 0 )
                {
                    this.nextPitstopLapCorrection++;
                    this.nextPitstopFuelLapsCorrection--;
                }
            }
            else
            {
                this.nextPitstopFuelLapsCorrection++;
            }
        }
        else if ( action == INPUT_ACTION_DEC_PITSTOP )
        {
            if ( ( modifierMask & InputAction.MODIFIER_MASK_SHIFT ) == 0 )
            {
                this.nextPitstopLapCorrection--;
                this.nextPitstopFuelLapsCorrection++;
            }
            else
            {
                this.nextPitstopFuelLapsCorrection--;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( !isLowFuelWaningUsed() )
            return ( null );
        
        loadLowFuelWarningImages( widgetInnerWidth, widgetInnerHeight );
        
        TransformableTexture[] tts;
        if ( lowFuelWarningImageNameOff.isNoImage() || lowFuelWarningImageNameOn.isNoImage() )
            tts = new TransformableTexture[ 1 ];
        else
            tts = new TransformableTexture[ 2 ];
        
        int i = 0;
        
        if ( lowFuelWarningImageOff != null )
        {
            tts[i++] = lowFuelWarningImageOff;
        }
        
        if ( lowFuelWarningImageOn != null )
        {
            tts[i++] = lowFuelWarningImageOn;
        }
        
        return ( tts );
    }
    
    private static final String getFuelUnits()
    {
        switch ( RFactorTools.getMeasurementUnits() )
        {
            case IMPERIAL:
                return ( Loc.fuel_units_IMPERIAL );
            case METRIC:
            default:
                return ( Loc.fuel_units_METRIC );
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
        final java.awt.Font fuelFont = this.fuelFont.getFont();
        final boolean fuelFontAntiAliased = this.fuelFont.isAntiAliased();
        final java.awt.Color fuelFontColor = this.fuelFontColor.getColor();
        
        int left = 2;
        int top = -2;
        
        fuelHeaderString = dsf.newDrawnString( "fuelHeaderString", left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.fuelHeader_prefix + ": (", ")" );
        
        int fuelBarWidth = this.fuelBarWidth.getEffectiveWidth();
        int fuelBarCenter = left + fuelBarLeftOffset.getIntValue() + ( fuelBarWidth / 2 );
        
        fuelLoadString1 = dsf.newDrawnString( "fuelLoadString1", fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontAntiAliased, fuelFontColor, null, getFuelUnits() );
        fuelLoadString2 = dsf.newDrawnString( "fuelLoadString2", null, fuelLoadString1, fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontAntiAliased, fuelFontColor, null, Loc.fuelLoad2_postfix );
        fuelLoadString3 = dsf.newDrawnString( "fuelLoadString3", null, fuelLoadString2, fuelBarCenter, 0, Alignment.CENTER, false, font2, font2AntiAliased, fuelFontColor, null, null );
        
        int rightLeft = left + fuelBarLeftOffset.getIntValue() + fuelBarWidth + 2;
        int lastToAvgSpacing = 75; // 85
        
        fuelUsageHeaderString = dsf.newDrawnString( "fuelUsageHeaderString", null, fuelHeaderString, rightLeft, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.fuelUsageHeader + ":", null );
        fuelUsageLastLapHeaderString = dsf.newDrawnString( "fuelUsageLastLapHeaderString", null, fuelUsageHeaderString, rightLeft + 50, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor, Loc.fuelUsageLastLapHeader, null );
        fuelUsageAvgHeaderString = dsf.newDrawnString( "fuelUsageAvgHeaderString", null, fuelUsageHeaderString, rightLeft + 50 + lastToAvgSpacing, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor, Loc.fuelUsageAvgHeader, null );
        
        fuelUsageLastLapString = dsf.newDrawnString( "fuelUsageOneLapString", null, fuelUsageLastLapHeaderString, rightLeft + 50, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
        fuelUsageAvgString = dsf.newDrawnString( "fuelUsageAvgString", null, fuelUsageAvgHeaderString, rightLeft + 50 + lastToAvgSpacing, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
        
        nextPitstopHeaderString = dsf.newDrawnString( "nextPitstopHeaderString", null, fuelUsageLastLapString, rightLeft, 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, Loc.nextPitstopHeader + ":", null );
        nextPitstopLapString = dsf.newDrawnString( "nextPitstopLapString", null, nextPitstopHeaderString, rightLeft + 10, 2, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, Loc.nextPitstopLap_prefix + ": ", null );
        nextPitstopFuelString = dsf.newDrawnString( "nextPitstopFuelString", null, nextPitstopLapString, rightLeft + 10, 0, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, Loc.nextPitstopFuel_prefix + ": ", null );
        
        loadLowFuelWarningImages( width, height );
        resetBlink( editorPresets != null );
    }
    
    private void drawFuel( float fuel, int tankSize, TextureImage2D texture, int x, int y, int height )
    {
        int w = fuelBarWidth.getEffectiveWidth();
        int h = height;
        
        float normFuel = fuel / tankSize;
        
        Color awtColor = new Color( colorFuel[ByteOrderManager.RED] & 0xFF, colorFuel[ByteOrderManager.GREEN] & 0xFF, colorFuel[ByteOrderManager.BLUE] & 0xFF );
        
        int barHeight = Math.min( (int)( h * normFuel ), h );
        
        if ( normFuel < 1.0f )
        {
            texture.clear( Color.BLACK, x, y, w, h - barHeight, false, null );
        }
        
        texture.clear( awtColor, x, y + h - barHeight, w, barHeight, false, null );
        
        texture.markDirty( x, y, w, h );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        final java.awt.Color backgroundColor = getBackgroundColor();
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        TelemetryData telemData = gameData.getTelemetryData();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        final int tankSize = (int)gameData.getPhysics().getFuelRange().getMaxValue();
        
        if ( needsCompleteRedraw )
        {
            fuelHeaderString.draw( offsetX, offsetY, String.valueOf( tankSize ) + " " + ( RFactorTools.getMeasurementUnits() == MeasurementUnits.IMPERIAL ? Loc.fuelHeader_postfix_IMPERIAL : Loc.fuelHeader_postfix_METRIC ), backgroundColor, texture );
            fuelUsageHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
            fuelUsageLastLapHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
            fuelUsageAvgHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
            //nextPitstopHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
        }
        
        float fuel = isEditorMode ? ( tankSize * 3f / 4f ) : telemData.getFuel();
        float fuelL = isEditorMode ? ( tankSize * 3f / 4f ) : telemData.getFuelL();
        float avgFuelUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getAverage();
        float stintLength = ( editorPresets == null ) ? vsi.getStintLength() : 5.2f;
        
        if ( isEditorMode )
        {
            if ( lowFuelWarningImageOn != null )
            {
                lowFuelWarningImageOn.setVisible( true );
                
                if ( lowFuelWarningImageOff != null )
                {
                    lowFuelWarningImageOff.setVisible( false );
                }
            }
            else if ( lowFuelWarningImageOff != null )
            {
                lowFuelWarningImageOff.setVisible( false );
            }
        }
        else if ( ( this.lowFuelBlinkNanos > 0L ) && ( lowFuelWarningImageOn != null ) )
        {
            float lapsForFuel = ( fuel - 0.5f ) / avgFuelUsage;
            float restLapLength = 1.0f - ( stintLength % 1f );
            
            int lapsRemaining = scoringInfo.getMaxLaps() - vsi.getLapsCompleted() - 1;
            
            if ( ( avgFuelUsage > 0f ) && ( lapsForFuel - restLapLength < 1.0f ) && ( lapsRemaining > 0 ) )
            {
                if ( nextBlinkTime < 0L )
                {
                    nextBlinkTime = scoringInfo.getSessionNanos() + lowFuelBlinkNanos;
                    blinkState = true;
                }
                else if ( scoringInfo.getSessionNanos() >= nextBlinkTime )
                {
                    nextBlinkTime = scoringInfo.getSessionNanos() + lowFuelBlinkNanos;
                    blinkState = !blinkState;
                }
            }
            else
            {
                nextBlinkTime = -1L;
                blinkState = false;
            }
            
            if ( lowFuelWarningImageOff != null )
                lowFuelWarningImageOff.setVisible( !blinkState );
            
            lowFuelWarningImageOn.setVisible( blinkState );
        }
        
        int fuel_ = Math.round( fuel * 10f );
        if ( needsCompleteRedraw || ( clock1 && ( ( fuel_ != oldFuel ) || ( avgFuelUsage != oldAverage ) ) ) )
        {
            oldFuel = fuel_;
            oldAverage = avgFuelUsage;
            
            int fuelY = fuelHeaderString.getAbsY() + fuelHeaderString.getMaxHeight( true ) + 0;
            int fuelHeight = height - fuelY - 4;
            drawFuel( fuel, tankSize, texture, offsetX + fuelBarLeftOffset.getIntValue(), offsetY + fuelY, fuelHeight );
            
            String string = NumberUtil.formatFloat( fuel, 1, true );
            fuelLoadString1.draw( offsetX, offsetY + fuelY, string, (Color)null, texture );
            string = NumberUtil.formatFloat( fuelL * gameData.getPhysics().getWeightOfOneLiterOfFuel(), 1, true );
            fuelLoadString2.draw( offsetX, offsetY + fuelY, string, (Color)null, texture );
            
            if ( avgFuelUsage > 0f )
            {
                if ( roundUpRemainingLaps.getBooleanValue() )
                    string = NumberUtil.formatFloat( ( fuel / avgFuelUsage ) + ( stintLength - (int)stintLength ), 1, true ) + Loc.fuelLoad3_postfix;
                else
                    string = NumberUtil.formatFloat( fuel / avgFuelUsage, 1, true ) + Loc.fuelLoad3_postfix;
            }
            else
            {
                string = Loc.fuelLoad3_na;
            }
            fuelLoadString3.draw( offsetX, offsetY + fuelY, string, (Color)null, texture );
        }
        
        stintLengthV.update( (int)stintLength );
        int fuelRelevantLaps = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getFuelRelevantLaps();
        
        if ( fuelRelevantLaps == 0 )
        {
            if ( fuelRelevantLaps != oldFuelRelevantLaps )
            {
                fuelUsageLastLapString.draw( offsetX, offsetY, Loc.fuelUsageLastLap_na, backgroundColor, texture );
                fuelUsageAvgString.draw( offsetX, offsetY, Loc.fuelUsageAvg_na, backgroundColor, texture );
            }
        }
        else if ( needsCompleteRedraw || stintLengthV.hasChanged() )
        {
            float lastFuelUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getLastLap();
            
            String string;
            if ( lastFuelUsage > 0f )
                string = NumberUtil.formatFloat( lastFuelUsage, 2, true ) + getFuelUnits();
            else
                string = Loc.fuelUsageLastLap_na;
            fuelUsageLastLapString.draw( offsetX, offsetY, string, backgroundColor, texture );
            
            string = NumberUtil.formatFloat( avgFuelUsage, 2, true ) + getFuelUnits();
            fuelUsageAvgString.draw( offsetX, offsetY, string, backgroundColor, texture );
        }
        
        this.oldFuelRelevantLaps = fuelRelevantLaps;
        
        if ( needsCompleteRedraw )
        {
            nextPitstopHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
        }
        
        int nextPitstopLap = -1;
        int pitstopFuel_ = -1;
        int pitstopLaps = -1;
        if ( !isEditorMode && ( avgFuelUsage > 0f ) )
        {
            int currLap = vsi.getCurrentLap();
            boolean isRace = scoringInfo.getSessionType().isRace();
            int totalLaps = scoringInfo.getMaxLaps(); // compute for time based races
            
            int remainingFuelLaps = (int)Math.floor( ( fuel / avgFuelUsage ) + ( stintLength - (int)stintLength ) );
            nextPitstopLap = vsi.getLapsCompleted() + remainingFuelLaps + nextPitstopLapCorrection;
            
            if ( nextPitstopLap < currLap )
            {
                int delta = currLap - nextPitstopLap;
                
                nextPitstopLapCorrection += delta;
                nextPitstopFuelLapsCorrection -= delta;
                nextPitstopLap = vsi.getLapsCompleted() + remainingFuelLaps + nextPitstopLapCorrection;
            }
            
            int nextPitstopIndex = Math.min( gameData.getScoringInfo().getPlayersVehicleScoringInfo().getNumPitstopsMade() + 1, gameData.getSetup().getGeneral().getNumPitstops() );
            float pitstopFuel0 = gameData.getSetup().getGeneral().getFuel( nextPitstopIndex );
            pitstopLaps = (int)Math.floor( pitstopFuel0 / avgFuelUsage );
            pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
            
            while ( pitstopFuel_ < avgFuelUsage )
            {
                nextPitstopFuelLapsCorrection++;
                pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
            }
            
            while ( ( pitstopFuel_ > tankSize ) || ( isRace && ( nextPitstopLap + pitstopLaps + nextPitstopFuelLapsCorrection > totalLaps ) ) )
            {
                nextPitstopFuelLapsCorrection--;
                pitstopFuel_ = (int)Math.ceil( ( pitstopLaps + nextPitstopFuelLapsCorrection ) * avgFuelUsage );
            }
        }
        else if ( isEditorMode )
        {
            nextPitstopLap = 31;
            pitstopLaps = (int)( 72 / avgFuelUsage );
        }
        
        if ( isEditorMode )
            pitstopFuel.update( 72 );
        else
            pitstopFuel.update( pitstopFuel_ );
        
        int tmp = ( ( nextPitstopLapCorrection + Short.MAX_VALUE / 2 ) << 16 ) | ( nextPitstopFuelLapsCorrection + Short.MAX_VALUE / 2 );
        
        if ( needsCompleteRedraw || ( tmp != oldNextPitstopLapCorrection ) || pitstopFuel.hasChanged() )
        {
            oldNextPitstopLapCorrection = tmp;
            
            if ( avgFuelUsage > 0f )
            {
                if ( pitstopFuel.isValid() )
                {
                    String string = String.valueOf( nextPitstopLap ) + " (" + NumberUtil.delta( nextPitstopLapCorrection ) + ")";
                    nextPitstopLapString.draw( offsetX, offsetY, string, backgroundColor, texture );
                    
                    string = String.valueOf( pitstopFuel.getValue() + fuelSafetyPlanning.getIntValue() ) + getFuelUnits() + " (" + ( pitstopLaps + nextPitstopFuelLapsCorrection ) + Loc.nextPitstopFuel_laps + "," + NumberUtil.delta( nextPitstopFuelLapsCorrection ) + ")";
                    nextPitstopFuelString.draw( offsetX, offsetY, string, backgroundColor, texture );
                }
                else
                {
                    nextPitstopLapString.draw( offsetX, offsetY, Loc.nextPitstopLap_enough, backgroundColor, texture );
                    nextPitstopFuelString.draw( offsetX, offsetY, Loc.nextPitstopFuel_enough, backgroundColor, texture );
                }
            }
            else
            {
                nextPitstopLapString.draw( offsetX, offsetY, Loc.nextPitstopLap_na + " (" + NumberUtil.delta( nextPitstopLapCorrection ) + ")", backgroundColor, texture );
                nextPitstopFuelString.draw( offsetX, offsetY, Loc.nextPitstopFuel_na + " (" + NumberUtil.delta( nextPitstopFuelLapsCorrection ) + " " + Loc.nextPitstopFuel_laps + ")", backgroundColor, texture );
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
        writer.writeProperty( fuelFont, "The used font for fuel load." );
        writer.writeProperty( fuelFontColor, "The color to use for fuel load in the format #RRGGBB (hex)." );
        writer.writeProperty( roundUpRemainingLaps, "Round up remaining fuel laps to include the current lap?" );
        lowFuelWarningImagePosition.savePositioningProperty( "lowFuelWarningImagePositioning", "Positioning type for the low-fuel-warning image.", writer );
        lowFuelWarningImagePosition.saveXProperty( "lowFuelWarningImagePositionX", "X-position for the low-fuel-warning image.", writer );
        lowFuelWarningImagePosition.saveYProperty( "lowFuelWarningImagePositionY", "Y-position for the low-fuel-warning image.", writer );
        //lowFuelWarningImageSize.saveWidthProperty( "lowFuelWarningImageWidth", "Width for the low-fuel-warning image.", writer );
        lowFuelWarningImageSize.saveHeightProperty( "lowFuelWarningImageHeight", "Height for the low-fuel-warning image.", writer );
        writer.writeProperty( lowFuelBlinkTime, "Blink time in milli seconds for low fuel warning (0 to disable)." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( font2.loadProperty( key, value ) );
        else if ( fuelFont.loadProperty( key, value ) );
        else if ( fuelFontColor.loadProperty( key, value ) );
        else if ( roundUpRemainingLaps.loadProperty( key, value ) );
        else if ( lowFuelWarningImageNameOff.loadProperty( key, value ) );
        else if ( lowFuelWarningImageNameOn.loadProperty( key, value ) );
        else if ( lowFuelWarningImagePosition.loadProperty( key, value, "lowFuelWarningImagePositioning", "lowFuelWarningImagePositionX", "lowFuelWarningImagePositionY" ) );
        else if ( lowFuelWarningImageSize.loadProperty( key, value, "lowFuelWarningImageWidth", "lowFuelWarningImageHeight" ) );
        else if ( lowFuelBlinkTime.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addProperty( font2 );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( fuelFont );
        propsCont.addProperty( fuelFontColor );
        propsCont.addProperty( roundUpRemainingLaps );
        
        propsCont.addGroup( "Low Fuel Warning" );
        
        propsCont.addProperty( lowFuelWarningImageNameOff );
        propsCont.addProperty( lowFuelWarningImageNameOn );
        propsCont.addProperty( lowFuelWarningImagePosition.createPositioningProperty( "lowFuelWarningImagePositioning", "imagePositioning" ) );
        propsCont.addProperty( lowFuelWarningImagePosition.createXProperty( "lowFuelWarningImagePositionX", "imagePosX" ) );
        propsCont.addProperty( lowFuelWarningImagePosition.createYProperty( "lowFuelWarningImagePositionY", "imagePosY" ) );
        //propsCont.addProperty( lowFuelWarningImageSize.createWidthProperty( "lowFuelWarningImageWidth", "imageWidth" ) );
        propsCont.addProperty( lowFuelWarningImageSize.createHeightProperty( "lowFuelWarningImageHeight", "imageHeight" ) );
        propsCont.addProperty( lowFuelBlinkTime );
    }
    
    public FuelWidget( String name )
    {
        super( name, 17.8f, true, 13.5f, true );
        
        this.fuelBarWidth = new Size( 26.f, true, 0f, true, this );
        
        getFontProperty().setFont( "StandardFont2" );
    }
}
