package net.ctdp.rfdynhud.widgets.misc;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.StringValue;
import net.ctdp.rfdynhud.values.ValidityTest;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link MiscWidget} displays miscellaneous information like fastest lap, session time, top speed, etc..
 * 
 * @author Marvin Froehlich
 */
public class MiscWidget extends Widget
{
    public static enum LapDisplayType
    {
        CURRENT_LAP,
        LAPS_DONE,
        ;
    }
    
    private final EnumProperty<LapDisplayType> lapDisplayType = new EnumProperty<LapDisplayType>( this, "lapDisplayType", LapDisplayType.CURRENT_LAP );
    private long relTopspeedResetDelay = 10000000000L; // ten seconds
    
    private final BooleanProperty displayScoring = new BooleanProperty( this, "displayScoring", true );
    private final BooleanProperty displayTiming = new BooleanProperty( this, "displayTiming", true );
    private final BooleanProperty displayVelocity = new BooleanProperty( this, "displayVelocity", true );
    
    private DrawnString scoringString1 = null;
    private DrawnString scoringString2 = null;
    private DrawnString scoringString3 = null;
    
    private DrawnString sessionTimeString = null;
    private DrawnString lapString = null;
    private DrawnString stintString = null;
    
    private DrawnString absTopspeedString = null;
    private DrawnString relTopspeedString = null;
    private DrawnString velocityString = null;
    
    private int oldStintLength = -1;
    private final StringValue leader = new StringValue();
    private boolean leaderValid = false;
    private final IntValue place = new IntValue( ValidityTest.GREATER_THAN, 0 );
    private final FloatValue fastestLap = new FloatValue( ValidityTest.GREATER_THAN, 0f );
    private final FloatValue sessionTime = new FloatValue( -1f, 0.1f );
    private final EnumValue<GamePhase> gamePhase1 = new EnumValue<GamePhase>();
    private final EnumValue<GamePhase> gamePhase2 = new EnumValue<GamePhase>();
    private final IntValue lapsCompleted = new IntValue();
    private float oldLapsRemaining = -1f;
    private float oldAbsTopspeed = -1f;
    private float oldRelTopspeed = -1f;
    private float relTopspeed = -1f;
    private int oldVelocity = -1;
    private boolean updateAbs = false;
    
    private int[] scoringColWidths = new int[ 2 ];
    private int[] velocityColWidths = new int[ 3 ];
    private final Alignment[] scoringAlignment = new Alignment[] { Alignment.RIGHT, Alignment.LEFT };
    private final Alignment[] velocityAlignment = new Alignment[] { Alignment.RIGHT, Alignment.LEFT, Alignment.LEFT };
    private static final int padding = 4;
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createLocalStore()
    {
        return ( new LocalStore() );
    }
    
    public void setRelTopspeedResetDelay( int delay )
    {
        this.relTopspeedResetDelay = delay * 1000000L;
    }
    
    public final int getRelTopspeedResetDelay()
    {
        return ( (int)( relTopspeedResetDelay / 1000000L ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        oldAbsTopspeed = -1f;
        
        ( (LocalStore)getLocalStore() ).lastDisplayedAbsTopspeed = 0f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        oldStintLength = -1;
        
        leader.reset();
        leaderValid = false;
        
        place.reset();
        fastestLap.reset();
        
        sessionTime.reset();
        gamePhase1.reset();
        gamePhase2.reset();
        lapsCompleted.reset();
        oldLapsRemaining = -1f;
        
        oldRelTopspeed = -1f;
        relTopspeed = -1f;
        ( (LocalStore)getLocalStore() ).lastRelTopspeedTime = -1L;
        oldVelocity = -1;
        
        updateAbs = false;
        
        //forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Font font = getFont();
        final boolean fontAntiAliased = isFontAntiAliased();
        final java.awt.Color fontColor = getFontColor();
        
        final int left = 2;
        final int center = width / 2;
        final int right = width - 2;
        final int top = -2;
        
        if ( displayScoring.getBooleanValue() )
        {
            scoringString1 = new DrawnString( left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            scoringString2 = new DrawnString( null, scoringString1, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
            scoringString3 = new DrawnString( null, scoringString2, left, 0, Alignment.LEFT, false, font, fontAntiAliased, fontColor );
        }
        else
        {
            scoringString1 = null;
            scoringString2 = null;
            scoringString3 = null;
        }
        
        if ( displayTiming.getBooleanValue() )
        {
            if ( ( displayScoring.getBooleanValue() && displayVelocity.getBooleanValue() ) || ( !displayScoring.getBooleanValue() && !displayVelocity.getBooleanValue() ) )
            {
                if ( lapDisplayType.getEnumValue() == LapDisplayType.CURRENT_LAP )
                    lapString = new DrawnString( center, top, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Lap: ", null );
                else
                    lapString = new DrawnString( center, top, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Laps: ", null );
                
                stintString = new DrawnString( lapString, lapString, 0, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Stint: ", null );
                sessionTimeString = new DrawnString( lapString, stintString, 0, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Time: ", null );
            }
            else if ( !displayScoring.getBooleanValue() )
            {
                if ( lapDisplayType.getEnumValue() == LapDisplayType.CURRENT_LAP )
                    lapString = new DrawnString( left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Lap: ", null );
                else
                    lapString = new DrawnString( left, top, Alignment.LEFT, false, font, fontAntiAliased, fontColor, "Laps: ", null );
                
                stintString = new DrawnString( lapString, lapString, 0, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Stint: ", null );
                sessionTimeString = new DrawnString( lapString, stintString, left, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Time: ", null );
            }
            else if ( !displayVelocity.getBooleanValue() )
            {
                if ( lapDisplayType.getEnumValue() == LapDisplayType.CURRENT_LAP )
                    lapString = new DrawnString( right, top, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, "Lap: ", null );
                else
                    lapString = new DrawnString( right, top, Alignment.RIGHT, false, font, fontAntiAliased, fontColor, "Laps: ", null );
                
                stintString = new DrawnString( lapString, lapString, 0, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Stint: ", null );
                sessionTimeString = new DrawnString( lapString, stintString, left, 0, Alignment.CENTER, false, font, fontAntiAliased, fontColor, "Time: ", null );
            }
        }
        else
        {
            sessionTimeString = null;
            lapString = null;
            stintString = null;
        }
        
        if ( displayVelocity.getBooleanValue() )
        {
            absTopspeedString = new DrawnString( right, top, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
            relTopspeedString = new DrawnString( null, absTopspeedString, right, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
            velocityString = new DrawnString( null, relTopspeedString, right, 0, Alignment.RIGHT, false, font, fontAntiAliased, fontColor );
            
            velocityColWidths[0] = 0;
            velocityColWidths[1] = 0;
            velocityColWidths[2] = 0;
            
            absTopspeedString.getMaxColWidths( new String[] { "Topspeed1:", "000.0", "km/h" }, velocityAlignment, padding, texture, velocityColWidths );
            relTopspeedString.getMaxColWidths( new String[] { "Topspeed2:", "000.0", "km/h" }, velocityAlignment, padding, texture, velocityColWidths );
            velocityString.getMaxColWidths( new String[] { "Velocity:", "000", "km/h" }, velocityAlignment, padding, texture, velocityColWidths );
        }
        else
        {
            absTopspeedString = null;
            relTopspeedString = null;
            velocityString = null;
        }
    }
    
    private void updateScoringColWidths( ScoringInfo scoringInfo, TextureImage2D texture )
    {
        String fastestLapper = scoringInfo.getFastestLapVSI().getDriverName();
        
        scoringColWidths[0] = 0;
        scoringColWidths[1] = 0;
        
        scoringString1.getMaxColWidths( new String[] { "Leader:", leader.getValue() }, scoringAlignment, padding, texture, scoringColWidths );
        if ( place.isValid() )
            scoringString2.getMaxColWidths( new String[] { "Place:", place.getValueAsString() + "/" + scoringInfo.getNumVehicles() }, scoringAlignment, padding, texture, scoringColWidths );
        else
            scoringString2.getMaxColWidths( new String[] { "Place:", "N/A" }, scoringAlignment, padding, texture, scoringColWidths );
        
        if ( fastestLap.isValid() )
            scoringString3.getMaxColWidths( new String[] { "Fst. Lap:", TimingUtil.getTimeAsString( fastestLap.getValue(), true ) + " (" + ThreeLetterCodeManager.getShortForm( fastestLapper ) + ")" }, scoringAlignment, padding, texture, scoringColWidths );
        else
            scoringString3.getMaxColWidths( new String[] { "Fst. Lap:", "--:--.---" }, scoringAlignment, padding, texture, scoringColWidths );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final java.awt.Color backgroundColor = getBackgroundColor();
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        if ( displayScoring.getBooleanValue() )
        {
            VehicleScoringInfo leaderVSI = scoringInfo.getVehicleScoringInfo( 0 );
            leader.update( leaderVSI.getDriverName() );
            place.update( scoringInfo.getOwnPlace() );
            VehicleScoringInfo fastestLapVSI = scoringInfo.getFastestLapVSI();
            String fastestLapper = fastestLapVSI.getDriverName();
            fastestLap.update( fastestLapVSI.getBestLapTime() );
            
            boolean colWidthsUpdated = false;
            
            boolean lv = ( leaderVSI.getBestLapTime() > 0f );
            if ( needsCompleteRedraw || leader.hasChanged() || ( lv != leaderValid ) )
            {
                leaderValid = lv;
                
                if ( !colWidthsUpdated )
                {
                    updateScoringColWidths( scoringInfo, texture );
                    colWidthsUpdated = true;
                }
                
                if ( scoringInfo.getSessionType().isRace() )
                {
                    if ( leaderValid )
                        scoringString1.drawColumns( offsetX, offsetY, new String[] { "Leader:", ThreeLetterCodeManager.getShortForm( leader.getValue() ) }, scoringAlignment, padding, scoringColWidths, backgroundColor, texture );
                    else
                        scoringString1.drawColumns( offsetX, offsetY, new String[] { "Leader:", "N/A" }, scoringAlignment, padding, scoringColWidths, backgroundColor, texture );
                }
                else
                {
                    scoringString1.draw( offsetX, offsetY, "", backgroundColor, texture );
                }
            }
            
            if ( needsCompleteRedraw || place.hasValidityChanged() || place.hasChanged() )
            {
                if ( !colWidthsUpdated )
                {
                    updateScoringColWidths( scoringInfo, texture );
                    colWidthsUpdated = true;
                }
                
                if ( place.isValid() )
                    scoringString2.drawColumns( offsetX, offsetY, new String[] { "Place:", place.getValueAsString() + "/" + scoringInfo.getNumVehicles() }, scoringAlignment, padding, scoringColWidths, backgroundColor, texture );
                else
                    scoringString2.drawColumns( offsetX, offsetY, new String[] { "Place:", "N/A" }, scoringAlignment, padding, scoringColWidths, backgroundColor, texture );
            }
            
            if ( needsCompleteRedraw || fastestLap.hasValidityChanged() || fastestLap.hasChanged() )
            {
                if ( !colWidthsUpdated )
                {
                    updateScoringColWidths( scoringInfo, texture );
                    colWidthsUpdated = true;
                }
                
                if ( fastestLap.isValid() )
                    scoringString3.drawColumns( offsetX, offsetY, new String[] { "Fst. Lap:", TimingUtil.getTimeAsLaptimeString( fastestLap.getValue() ) + " (" + ThreeLetterCodeManager.getShortForm( fastestLapper ) + ")" }, scoringAlignment, padding, scoringColWidths, backgroundColor, texture );
                else
                    scoringString3.drawColumns( offsetX, offsetY, new String[] { "Fst. Lap:", "--:--.---" }, scoringAlignment, padding, scoringColWidths, backgroundColor, texture );
            }
        }
        
        if ( displayTiming.getBooleanValue() )
        {
            lapsCompleted.update( vsi.getLapsCompleted() );
            final int maxLaps = scoringInfo.getMaxLaps();
            if ( maxLaps < Integer.MAX_VALUE / 2 )
            {
                float lapsRemaining = maxLaps - lapsCompleted.getValue();
                
                if ( true )
                {
                    lapsRemaining -= 1f;
                    lapsRemaining += 1f - vsi.getLapDistance() / scoringInfo.getTrackLength();
                }
                
                float rounded = Math.round( lapsRemaining * 10f );
                gamePhase1.update( scoringInfo.getGamePhase() );
                
                if ( needsCompleteRedraw || ( rounded != oldLapsRemaining ) || gamePhase1.hasChanged())
                {
                    oldLapsRemaining = rounded;
                    
                    String string;
                    if ( scoringInfo.getSessionType().isRace() && ( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP ) )
                    {
                        string = "0 / " + maxLaps + " / " + maxLaps;
                    }
                    else
                    {
                        if ( lapDisplayType.getEnumValue() == LapDisplayType.CURRENT_LAP )
                            string = ( lapsCompleted.getValue() + 1 ) + " / " + maxLaps + " / " + NumberUtil.formatFloat( lapsRemaining, 1, true );
                        else
                            string = lapsCompleted + " / " + maxLaps + " / " + NumberUtil.formatFloat( lapsRemaining, 1, true );
                    }
                    lapString.draw( offsetX, offsetY, string, backgroundColor, texture );
                }
            }
            else if ( needsCompleteRedraw || lapsCompleted.hasChanged() )
            {
                String string;
                if ( lapDisplayType.getEnumValue() == LapDisplayType.CURRENT_LAP )
                    string = String.valueOf( lapsCompleted.getValue() + 1 );
                else
                    string = String.valueOf( lapsCompleted );
                lapString.draw( offsetX, offsetY, string, backgroundColor, texture );
            }
            
            {
                int stintLength = (int)( vsi.getStintLength() * 10f );
                if ( needsCompleteRedraw || ( stintLength != oldStintLength ) )
                {
                    if ( vsi.isInPits() )
                    {
                        if ( oldStintLength < 0 )
                            stintString.draw( offsetX, offsetY, "N/A", backgroundColor, texture );
                        else
                            stintString.draw( offsetX, offsetY, String.valueOf( Math.round( oldStintLength / 10f ) ), backgroundColor, texture );
                    }
                    else
                    {
                        oldStintLength = stintLength;
                        
                        stintString.draw( offsetX, offsetY, String.valueOf( oldStintLength / 10f ), backgroundColor, texture );
                    }
                }
            }
            
            sessionTime.update( gameData.getScoringInfo().getSessionTime() );
            gamePhase2.update( scoringInfo.getGamePhase() );
            float totalTime = gameData.getScoringInfo().getEndTime();
            if ( needsCompleteRedraw || ( clock1 && ( sessionTime.hasChanged( false ) || gamePhase2.hasChanged( false ) ) ) )
            {
                sessionTime.setUnchanged();
                gamePhase2.setUnchanged();
                
                if ( scoringInfo.getSessionType().isRace() && ( ( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP ) || ( totalTime < 0f ) || ( totalTime > 3000000f ) ) )
                    sessionTimeString.draw( offsetX, offsetY, "--:--:--", backgroundColor, texture );
                else if ( scoringInfo.getSessionType().isTestDay() || ( totalTime < 0f ) || ( totalTime > 3000000f ) )
                    sessionTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( sessionTime.getValue(), true, false ), backgroundColor, texture );
                else
                    sessionTimeString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( sessionTime.getValue() - totalTime, true, false ), backgroundColor, texture );
            }
        }
        
        if ( displayVelocity.getBooleanValue() )
        {
            float floatVelocity = gameData.getTelemetryData().getScalarVelocityKPH();
            int velocity = Math.round( floatVelocity );
            
            if ( floatVelocity >= relTopspeed )
            {
                relTopspeed = floatVelocity;
                ( (LocalStore)getLocalStore() ).lastRelTopspeedTime = scoringInfo.getSessionNanos();
            }
            else if ( ( ( (LocalStore)getLocalStore() ).lastRelTopspeedTime + relTopspeedResetDelay < scoringInfo.getSessionNanos() ) && ( floatVelocity < relTopspeed - 50f ) )
            {
                relTopspeed = floatVelocity;
                oldRelTopspeed = -1f;
                //lastRelTopspeedTime = scoringInfo.getSessionNanos();
                
                updateAbs = true;
            }
            
            boolean forceUpdateAbs = false;
            float topspeed = vsi.getTopspeed();
            if ( topspeed < ( (LocalStore)getLocalStore() ).lastDisplayedAbsTopspeed )
            {
                ( (LocalStore)getLocalStore() ).lastDisplayedAbsTopspeed = 0f;
                oldAbsTopspeed = -1f;
                topspeed = 0f;
                updateAbs = true;
                forceUpdateAbs = true;
            }
            
            if ( needsCompleteRedraw || ( ( clock1 || forceUpdateAbs ) && updateAbs && ( topspeed > oldAbsTopspeed ) ) )
            {
                if ( !needsCompleteRedraw )
                    ( (LocalStore)getLocalStore() ).lastDisplayedAbsTopspeed = topspeed;
                
                updateAbs = false;
                oldAbsTopspeed = topspeed;
                absTopspeedString.drawColumns( offsetX, offsetY, new String[] { "Topspeed1:", NumberUtil.formatFloat( ( (LocalStore)getLocalStore() ).lastDisplayedAbsTopspeed, 1, true ), "km/h" }, velocityAlignment, padding, velocityColWidths, backgroundColor, texture );
            }
            
            if ( needsCompleteRedraw || ( clock1 && ( relTopspeed > oldRelTopspeed ) ) )
            {
                oldRelTopspeed = relTopspeed;
                relTopspeedString.drawColumns( offsetX, offsetY, new String[] { "Topspeed2:", NumberUtil.formatFloat( oldRelTopspeed, 1, true ), "km/h" }, velocityAlignment, padding, velocityColWidths, backgroundColor, texture );
            }
            
            if ( needsCompleteRedraw || ( clock1 && ( velocity != oldVelocity ) ) )
            {
                oldVelocity = velocity;
                velocityString.drawColumns( offsetX, offsetY, new String[] { "Velocity:", String.valueOf( velocity ), "km/h" }, velocityAlignment, padding, velocityColWidths, backgroundColor, texture );
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
        
        writer.writeProperty( displayScoring, "Display the scoring part of the Widget?" );
        writer.writeProperty( displayTiming, "Display the timing part of the Widget?" );
        writer.writeProperty( lapDisplayType, "The way the laps are displayed. Valid values: CURRENT_LAP, LAPS_DONE." );
        writer.writeProperty( displayVelocity, "Display the velocity and top speed part of the Widget?" );
        writer.writeProperty( "relTopspeedResetDelay", getRelTopspeedResetDelay(), "The delay after which the relative topspeed is resetted (in milliseconds)." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( displayScoring.loadProperty( key, value ) );
        else if ( displayTiming.loadProperty( key, value ) );
        else if ( lapDisplayType.loadProperty( key, value ) );
        else if ( displayVelocity.loadProperty( key, value ) );
        else if ( key.equals( "relTopspeedResetDelay" ) )
            this.relTopspeedResetDelay = Integer.parseInt( value ) * 1000000L;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( displayScoring );
        propsCont.addProperty( displayTiming );
        propsCont.addProperty( lapDisplayType );
        propsCont.addProperty( displayVelocity );
        
        propsCont.addProperty( new Property( this, "relTopspeedResetDelay", PropertyEditorType.INTEGER )
        {
            @Override
            public void setValue( Object value )
            {
                setRelTopspeedResetDelay( ( (Number)value ).intValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getRelTopspeedResetDelay() );
            }
        } );
    }
    
    public MiscWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.6625f, Size.PERCENT_OFFSET + 0.0583f );
    }
}
