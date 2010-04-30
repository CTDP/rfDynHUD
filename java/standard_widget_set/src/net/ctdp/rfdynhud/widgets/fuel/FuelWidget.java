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
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.ByteOrderManager;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.ValidityTest;
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
    
    private final FontProperty font2 = new FontProperty( this, "font2", FontProperty.SMALLER_FONT3_NAME );
    
    private final FontProperty fuelFont = new FontProperty( this, "fuelFont", FontProperty.STANDARD_FONT_NAME );
    
    private DrawnString fuelHeaderString = null;
    
    private final IntProperty fuelBarLeftOffset = new IntProperty( this, "fuelBarLeftOffset", 4 );
    private final Size fuelBarWidth;
    
    private final ColorProperty fuelFontColor = new ColorProperty( this, "fuelFontColor", "#2828FF" );
    
    private final BooleanProperty roundUpRemainingLaps = new BooleanProperty( this, "roundUpRemainingLaps", false );
    private final IntProperty fuelSafetyPlanning = new IntProperty( this, "fuelSafetyPlanning", 2);
    
    private DrawnString fuelLoadString1 = null;
    private DrawnString fuelLoadString2 = null;
    private DrawnString fuelLoadString3 = null;
    
    private DrawnString fuelUsageHeaderString = null;
    private DrawnString fuelUsageLastLapHeaderString = null;
    private DrawnString fuelUsageAvgHeaderString = null;
    
    private DrawnString fuelUsageOneLapString = null;
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
    private final IntValue stintLength = new IntValue( ValidityTest.GREATER_THAN, 0 );
    
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
        
        fuelBarWidth.bake();
    }
    
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        if ( !fuelBarWidth.isWidthPercentageValue() )
            fuelBarWidth.flipWidthPercentagePx();
        
        if ( !fuelBarWidth.isHeightPercentageValue() )
            fuelBarWidth.flipHeightPercentagePx();
    }
    
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        this.nextPitstopLapCorrection = 0;
        this.nextPitstopFuelLapsCorrection = 0;
        this.oldNextPitstopLapCorrection = ( ( nextPitstopLapCorrection + Short.MAX_VALUE / 2 ) << 16 ) | ( nextPitstopFuelLapsCorrection + Short.MAX_VALUE / 2 );
        this.pitstopFuel.reset();
        
        this.stintLength.reset();
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
        
        if ( stintLength.getValue() < 1 )
        {
            this.nextPitstopLapCorrection = 0;
            this.nextPitstopFuelLapsCorrection = 0;
        }
        
        this.stintLength.reset();
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
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
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
        
        fuelHeaderString = new DrawnString( left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Fuel: (", ")" );
        
        int fuelBarWidth = this.fuelBarWidth.getEffectiveWidth();
        int fuelBarCenter = left + fuelBarLeftOffset.getIntValue() + ( fuelBarWidth / 2 );
        
        fuelLoadString1 = new DrawnString( fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontAntiAliased, fuelFontColor, null, "L" );
        fuelLoadString2 = new DrawnString( null, fuelLoadString1, fuelBarCenter, 0, Alignment.CENTER, false, fuelFont, fuelFontAntiAliased, fuelFontColor, null, "kg" );
        fuelLoadString3 = new DrawnString( null, fuelLoadString2, fuelBarCenter, 0, Alignment.CENTER, false, font2, font2AntiAliased, fuelFontColor, null, null );
        
        int rightLeft = left + fuelBarLeftOffset.getIntValue() + fuelBarWidth + 2;
        int lastToAvgSpacing = 75; // 85
        
        fuelUsageHeaderString = new DrawnString( null, fuelHeaderString, rightLeft, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Usage:", null );
        fuelUsageLastLapHeaderString = new DrawnString( null, fuelUsageHeaderString, rightLeft + 50, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Last lap", null );
        fuelUsageAvgHeaderString = new DrawnString( null, fuelUsageHeaderString, rightLeft + 50 + lastToAvgSpacing, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor, String.valueOf( (char)216 ), null );
        
        fuelUsageOneLapString = new DrawnString( null, fuelUsageLastLapHeaderString, rightLeft + 50, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
        fuelUsageAvgString = new DrawnString( null, fuelUsageAvgHeaderString, rightLeft + 50 + lastToAvgSpacing, 2, Alignment.CENTER, false, font, fontAntiAliased, fontColor );
        
        nextPitstopHeaderString = new DrawnString( null, fuelUsageOneLapString, rightLeft, 7, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Next Pitstop:", null );
        nextPitstopLapString = new DrawnString( null, nextPitstopHeaderString, rightLeft + 10, 2, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, "Lap: ", null );
        nextPitstopFuelString = new DrawnString( null, nextPitstopLapString, rightLeft + 10, 0, Alignment.LEFT, false, font2, font2AntiAliased, fontColor, "Fuel: ", null );
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
            fuelHeaderString.draw( offsetX, offsetY, String.valueOf( tankSize ) + " Liters max", backgroundColor, texture );
            fuelUsageHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
            fuelUsageLastLapHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
            fuelUsageAvgHeaderString.draw( offsetX, offsetY, "", backgroundColor, texture );
            //nextPitstopHeaderString.draw( offsetX, offsetY, "", backgroundColor, image );
        }
        
        float fuel = isEditorMode ? ( tankSize * 3f / 4f ) : telemData.getFuel();
        float avgFuelUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getAverage();
        
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
            string = NumberUtil.formatFloat( fuel * gameData.getPhysics().getWeightOfOneLiterOfFuel(), 1, true );
            fuelLoadString2.draw( offsetX, offsetY + fuelY, string, (Color)null, texture );
            
            if ( !isEditorMode && ( avgFuelUsage > 0f ) )
            {
                if ( roundUpRemainingLaps.getBooleanValue() )
                    string = NumberUtil.formatFloat( ( fuel / avgFuelUsage ) + ( vsi.getStintLength() - (int)vsi.getStintLength() ), 1, true ) + "Laps";
                else
                    string = NumberUtil.formatFloat( fuel / avgFuelUsage, 1, true ) + "Laps";
            }
            else
            {
                string = "N/A";
            }
            fuelLoadString3.draw( offsetX, offsetY + fuelY, string, (Color)null, texture );
        }
        
        stintLength.update( (int)vsi.getStintLength() );
        int fuelRelevantLaps = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getFuelRelevantLaps();
        
        if ( isEditorMode || ( fuelRelevantLaps == 0 ) )
        {
            if ( isEditorMode || ( fuelRelevantLaps != oldFuelRelevantLaps ) )
            {
                fuelUsageOneLapString.draw( offsetX, offsetY, "N/A", backgroundColor, texture );
                fuelUsageAvgString.draw( offsetX, offsetY, "N/A", backgroundColor, texture );
            }
        }
        else if ( needsCompleteRedraw || stintLength.hasChanged() )
        {
            float lastFuelUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getLastLap();
            
            String string;
            if ( lastFuelUsage > 0f )
                string = NumberUtil.formatFloat( lastFuelUsage, 3, true ) + "L";
            else
                string = "N/A";
            fuelUsageOneLapString.draw( offsetX, offsetY, string, backgroundColor, texture );
            
            string = NumberUtil.formatFloat( avgFuelUsage, 3, true ) + "L";
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
            
            int remainingFuelLaps = (int)Math.floor( ( fuel / avgFuelUsage ) + ( vsi.getStintLength() - (int)vsi.getStintLength() ) );
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
                    
                    string = String.valueOf( pitstopFuel.getValue() + fuelSafetyPlanning.getIntValue() ) + "L (" + ( pitstopLaps + nextPitstopFuelLapsCorrection ) + "Laps," + NumberUtil.delta( nextPitstopFuelLapsCorrection ) + ")";
                    nextPitstopFuelString.draw( offsetX, offsetY, string, backgroundColor, texture );
                }
                else
                {
                    nextPitstopLapString.draw( offsetX, offsetY, "-", backgroundColor, texture );
                    nextPitstopFuelString.draw( offsetX, offsetY, "-", backgroundColor, texture );
                }
            }
            else
            {
                nextPitstopLapString.draw( offsetX, offsetY, "N/A (" + NumberUtil.delta( nextPitstopLapCorrection ) + ")", backgroundColor, texture );
                nextPitstopFuelString.draw( offsetX, offsetY, "N/A (" + NumberUtil.delta( nextPitstopFuelLapsCorrection ) + " Laps)", backgroundColor, texture );
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
    }
    
    public FuelWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.178f, Size.PERCENT_OFFSET + 0.135f );
        
        this.fuelBarWidth = new Size( Size.PERCENT_OFFSET + 0.26f, 0, this );
    }
}
