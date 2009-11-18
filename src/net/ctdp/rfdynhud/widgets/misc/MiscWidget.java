package net.ctdp.rfdynhud.widgets.misc;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.TopspeedRecorder;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.EnumValue;
import net.ctdp.rfdynhud.widgets._util.FloatValue;
import net.ctdp.rfdynhud.widgets._util.IntValue;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.StringValue;
import net.ctdp.rfdynhud.widgets._util.TimingUtil;
import net.ctdp.rfdynhud.widgets._util.ValidityTest;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
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
    
    private LapDisplayType lapDisplayType = LapDisplayType.CURRENT_LAP;
    private long relTopspeedResetDelay = 5000000000L; // five seconds
    
    private boolean displayScoring = true;
    private boolean displayTiming = true;
    private boolean displayVelocity = true;
    
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
    private final FloatValue fastestLap = new FloatValue( ValidityTest.GREATER_THAN, 0f );
    private final FloatValue sessionTime = new FloatValue( -1f, 0.1f );
    private final EnumValue<GamePhase> gamePhase = new EnumValue<GamePhase>();
    private final IntValue lapsCompleted = new IntValue();
    private float oldLapsRemaining = -1f;
    private float oldAbsTopspeed = -1f;
    private float oldRelTopspeed = -1f;
    private float relTopspeed = -1f;
    private long lastRelTopspeedTime = -1L;
    private int oldVelocity = -1;
    
    public void setLapDisplayType( LapDisplayType lapDisplayType )
    {
        this.lapDisplayType = lapDisplayType;
        
        forceReinitialization();
        setDirtyFlag();
    }
    
    public final LapDisplayType getLapDisplayType()
    {
        return ( lapDisplayType );
    }
    
    public void setRelTopspeedResetDelay( int delay )
    {
        this.relTopspeedResetDelay = delay * 1000000L;
    }
    
    public final int getRelTopspeedResetDelay()
    {
        return ( (int)( relTopspeedResetDelay / 1000000L ) );
    }
    
    public void setDisplayScoring( boolean display )
    {
        this.displayScoring = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayScoring()
    {
        return ( displayScoring );
    }
    
    public void setDisplayTiming( boolean display )
    {
        this.displayTiming = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayTiming()
    {
        return ( displayTiming );
    }
    
    public void setDisplayVelocity( boolean display )
    {
        this.displayVelocity = display;
        
        forceAndSetDirty();
    }
    
    public final boolean getDisplayVelocity()
    {
        return ( displayVelocity );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
        super.onSessionStarted( isEditorMode, sessionType, gameData );
        
        oldAbsTopspeed = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeEntered( isEditorMode, gameData );
        
        oldStintLength = -1;
        
        leader.reset();
        leaderValid = false;
        
        fastestLap.reset();
        
        sessionTime.reset();
        gamePhase.reset();
        lapsCompleted.reset();
        oldLapsRemaining = -1f;
        
        oldRelTopspeed = -1f;
        relTopspeed = -1f;
        lastRelTopspeedTime = -1L;
        oldVelocity = -1;
        
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
        final java.awt.Color fontColor = getFontColor();
        
        final int left = 2;
        final int center = width / 2;
        final int right = width - 2;
        final int top = -2;
        
        if ( getDisplayScoring() )
        {
            scoringString1 = new DrawnString( left, top, Alignment.LEFT, false, font, fontColor, null, null, null );
            scoringString2 = new DrawnString( null, scoringString1, left, 0, Alignment.LEFT, false, font, fontColor, "Fastest Lap: ", null, null );
            scoringString3 = new DrawnString( null, scoringString2, left, 0, Alignment.LEFT, false, font, fontColor, "             ", null, null );
        }
        else
        {
            scoringString1 = null;
            scoringString2 = null;
            scoringString3 = null;
        }
        
        if ( getDisplayTiming() )
        {
            if ( ( getDisplayScoring() && getDisplayVelocity() ) || ( !getDisplayScoring() && !getDisplayVelocity() ) )
            {
                if ( getLapDisplayType() == LapDisplayType.CURRENT_LAP )
                    lapString = new DrawnString( center, top, Alignment.CENTER, false, font, fontColor, "Lap: ", null, null );
                else
                    lapString = new DrawnString( center, top, Alignment.CENTER, false, font, fontColor, "Laps: ", null, null );
                
                stintString = new DrawnString( lapString, lapString, 0, 0, Alignment.CENTER, false, font, fontColor, "Stint: ", null, null );
                sessionTimeString = new DrawnString( lapString, stintString, 0, 0, Alignment.CENTER, false, font, fontColor, "Time: ", null, null );
            }
            else if ( !getDisplayScoring() )
            {
                if ( getLapDisplayType() == LapDisplayType.CURRENT_LAP )
                    lapString = new DrawnString( left, top, Alignment.LEFT, false, font, fontColor, "Lap: ", null, null );
                else
                    lapString = new DrawnString( left, top, Alignment.LEFT, false, font, fontColor, "Laps: ", null, null );
                
                stintString = new DrawnString( lapString, lapString, 0, 0, Alignment.CENTER, false, font, fontColor, "Stint: ", null, null );
                sessionTimeString = new DrawnString( lapString, stintString, left, 0, Alignment.CENTER, false, font, fontColor, "Time: ", null, null );
            }
            else if ( !getDisplayVelocity() )
            {
                if ( getLapDisplayType() == LapDisplayType.CURRENT_LAP )
                    lapString = new DrawnString( right, top, Alignment.RIGHT, false, font, fontColor, "Lap: ", null, null );
                else
                    lapString = new DrawnString( right, top, Alignment.RIGHT, false, font, fontColor, "Laps: ", null, null );
                
                stintString = new DrawnString( lapString, lapString, 0, 0, Alignment.CENTER, false, font, fontColor, "Stint: ", null, null );
                sessionTimeString = new DrawnString( lapString, stintString, left, 0, Alignment.CENTER, false, font, fontColor, "Time: ", null, null );
            }
        }
        else
        {
            sessionTimeString = null;
            lapString = null;
            stintString = null;
        }
        
        if ( getDisplayVelocity() )
        {
            absTopspeedString = new DrawnString( right, top, Alignment.RIGHT, false, font, fontColor, "Abs. Topspeed: ", null, " km/h" );
            relTopspeedString = new DrawnString( null, absTopspeedString, right, 0, Alignment.RIGHT, false, font, fontColor, "Rel. Topspeed: ", null, " km/h" );
            velocityString = new DrawnString( null, relTopspeedString, right, 0, Alignment.RIGHT, false, font, fontColor, "Velocity: ", null, "   km/h" );
        }
        else
        {
            absTopspeedString = null;
            relTopspeedString = null;
            velocityString = null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
        final TextureImage2D image = texCanvas.getImage();
        final java.awt.Color backgroundColor = getBackgroundColor();
        
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( getDisplayScoring() )
        {
            VehicleScoringInfo leaderVSI = scoringInfo.getVehicleScoringInfo( 0 );
            leader.update( leaderVSI.getDriverName() );
            VehicleScoringInfo fastestLapVSI = scoringInfo.getFastestLapVSI();
            String fastestLapper = fastestLapVSI.getDriverName();
            fastestLap.update( fastestLapVSI.getBestLapTime() );
            
            boolean lv = ( leaderVSI.getBestLapTime() > 0f );
            if ( needsCompleteRedraw || leader.hasChanged() || ( lv != leaderValid ) )
            {
                leaderValid = lv;
                
                if ( scoringInfo.getSessionType().isRace() )
                {
                    if ( leaderValid )
                        scoringString1.draw( offsetX, offsetY, "Leader:      " + leader.getValue(), backgroundColor, image );
                    else
                        scoringString1.draw( offsetX, offsetY, "Leader:      N/A", backgroundColor, image );
                }
                else
                {
                    scoringString1.draw( offsetX, offsetY, "", backgroundColor, image );
                }
            }
            
            if ( needsCompleteRedraw || fastestLap.hasValidityChanged() || fastestLap.hasChanged() )
            {
                if ( fastestLap.isValid() )
                {
                    scoringString2.draw( offsetX, offsetY, TimingUtil.getTimeAsString( fastestLap.getValue(), true ), backgroundColor, image );
                    scoringString3.draw( offsetX, offsetY, "(" + fastestLapper + ")", backgroundColor, image );
                }
                else
                {
                    scoringString2.draw( offsetX, offsetY, "--:--.---", backgroundColor, image );
                    scoringString3.draw( offsetX, offsetY, "", backgroundColor, image );
                }
            }
        }
        
        if ( getDisplayTiming() )
        {
            gamePhase.update( scoringInfo.getGamePhase() );
            VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
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
                
                if ( needsCompleteRedraw || ( rounded != oldLapsRemaining ) || gamePhase.hasChanged())
                {
                    oldLapsRemaining = rounded;
                    
                    String string;
                    if ( ( scoringInfo.getSessionType() == SessionType.RACE ) && ( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP ) )
                    {
                        if ( getLapDisplayType() == LapDisplayType.CURRENT_LAP )
                            string = ( lapsCompleted.getValue() + 1 ) + " / " + maxLaps + " / " + (int)Math.ceil( lapsRemaining );
                        else
                            string = lapsCompleted + " / " + maxLaps + " / " + (int)Math.ceil( lapsRemaining );
                    }
                    else
                    {
                        if ( getLapDisplayType() == LapDisplayType.CURRENT_LAP )
                            string = ( lapsCompleted.getValue() + 1 ) + " / " + maxLaps + " / " + NumberUtil.formatFloat( lapsRemaining, 1, true );
                        else
                            string = lapsCompleted + " / " + maxLaps + " / " + NumberUtil.formatFloat( lapsRemaining, 1, true );
                    }
                    lapString.draw( offsetX, offsetY, string, backgroundColor, image );
                }
            }
            else if ( needsCompleteRedraw || lapsCompleted.hasChanged() )
            {
                String string;
                if ( getLapDisplayType() == LapDisplayType.CURRENT_LAP )
                    string = String.valueOf( lapsCompleted.getValue() + 1 );
                else
                    string = String.valueOf( lapsCompleted );
                lapString.draw( offsetX, offsetY, string, backgroundColor, image );
            }
            
            {
                int stintLength = (int)( vsi.getStintLength() * 10f );
                if ( needsCompleteRedraw || ( stintLength != oldStintLength ) )
                {
                    if ( vsi.isInPits() )
                    {
                        if ( oldStintLength < 0 )
                            stintString.draw( offsetX, offsetY, "N/A", backgroundColor, image );
                        else
                            stintString.draw( offsetX, offsetY, String.valueOf( Math.round( oldStintLength / 10f ) ), backgroundColor, image );
                    }
                    else
                    {
                        oldStintLength = stintLength;
                        
                        stintString.draw( offsetX, offsetY, String.valueOf( oldStintLength / 10f ), backgroundColor, image );
                    }
                }
            }
            
            sessionTime.update( gameData.getScoringInfo().getSessionTime() );
            float totalTime = gameData.getScoringInfo().getEndTime();
            if ( needsCompleteRedraw || ( clock1 && ( sessionTime.hasChanged( false ) || gamePhase.hasChanged( false ) ) ) )
            {
                sessionTime.setUnchanged();
                gamePhase.setUnchanged();
                
                if ( scoringInfo.getSessionType().isRace() && ( ( scoringInfo.getGamePhase() == GamePhase.FORMATION_LAP ) || ( totalTime < 0f ) || ( totalTime > 3000000f ) ) )
                {
                    sessionTimeString.draw( offsetX, offsetY, "--:--:--", backgroundColor, image );
                }
                else if ( scoringInfo.getSessionType().isTestDay() || ( totalTime < 0f ) || ( totalTime > 3000000f ) )
                {
                    String string = TimingUtil.getTimeAsString( sessionTime.getValue(), true, false );
                    sessionTimeString.draw( offsetX, offsetY, string, backgroundColor, image );
                }
                else
                {
                    String string = TimingUtil.getTimeAsString( sessionTime.getValue() - totalTime, true, false );
                    sessionTimeString.draw( offsetX, offsetY, string, backgroundColor, image );
                }
            }
        }
        
        if ( getDisplayVelocity() )
        {
            float velocity = gameData.getTelemetryData().getScalarVelocityKPH();
            float topspeed = TopspeedRecorder.MASTER_TOPSPEED_RECORDER.getTopSpeed();
            
            if ( needsCompleteRedraw || ( clock2 && ( topspeed > oldAbsTopspeed ) ) )
            {
                oldAbsTopspeed = topspeed;
                String string = NumberUtil.formatFloat( topspeed, 1, true );
                absTopspeedString.draw( offsetX, offsetY, string, backgroundColor, image );
            }
            
            if ( velocity > relTopspeed )
            {
                relTopspeed = velocity;
                lastRelTopspeedTime = scoringInfo.getSessionNanos();
            }
            else if ( ( lastRelTopspeedTime + relTopspeedResetDelay < scoringInfo.getSessionNanos() ) && ( velocity < relTopspeed - 50f ) )
            {
                relTopspeed = velocity;
                oldRelTopspeed = -1f;
                lastRelTopspeedTime = scoringInfo.getSessionNanos();
            }
            
            if ( needsCompleteRedraw || ( clock2 && ( relTopspeed > oldRelTopspeed ) ) )
            {
                oldRelTopspeed = relTopspeed;
                String string = NumberUtil.formatFloat( oldRelTopspeed, 1, true );
                relTopspeedString.draw( offsetX, offsetY, string, backgroundColor, image );
            }
            
            int intVel = Math.round( velocity );
            if ( needsCompleteRedraw || ( clock2 && ( intVel != oldVelocity ) ) )
            {
                oldVelocity = intVel;
                String string = String.valueOf( intVel );
                velocityString.draw( offsetX, offsetY, string, backgroundColor, image );
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
        
        writer.writeProperty( "displayScoring", getDisplayScoring(), "Display the scoring part of the Widget?" );
        writer.writeProperty( "displayTiming", getDisplayTiming(), "Display the timing part of the Widget?" );
        writer.writeProperty( "displayVelocity", getDisplayVelocity(), "Display the velocity and top speed part of the Widget?" );
        writer.writeProperty( "lapDisplayType", getLapDisplayType(), "The way the laps are displayed. Valid values: CURRENT_LAP, LAPS_DONE." );
        writer.writeProperty( "relTopspeedResetDelay", getRelTopspeedResetDelay(), "The delay after which the relative topspeed is resetted (in milliseconds)." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( key.equals( "displayScoring" ) )
            this.displayScoring = Boolean.parseBoolean( value );
        
        else if ( key.equals( "displayTiming" ) )
            this.displayTiming = Boolean.parseBoolean( value );
        
        else if ( key.equals( "displayVelocity" ) )
            this.displayVelocity = Boolean.parseBoolean( value );
        
        else if ( key.equals( "lapDisplayType" ) )
            this.lapDisplayType = LapDisplayType.valueOf( value );
        
        else if ( key.equals( "relTopspeedResetDelay" ) )
            this.relTopspeedResetDelay = Integer.parseInt( value ) * 1000000L;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( new Property( "displayScoring", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayScoring( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayScoring() );
            }
        } );
        
        props.add( new Property( "displayTiming", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayTiming( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayTiming() );
            }
        } );
        
        props.add( new Property( "displayVelocity", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setDisplayVelocity( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( getDisplayVelocity() );
            }
        } );
        
        props.add( new Property( "lapDisplayType", PropertyEditorType.ENUM )
        {
            @Override
            public void setValue( Object value )
            {
                if ( lapDisplayType == value )
                    return;
                
                setLapDisplayType( (LapDisplayType)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getLapDisplayType() );
            }
        } );
        
        props.add( new Property( "relTopspeedResetDelay", PropertyEditorType.INTEGER )
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
        
        propsList.add( props );
    }
    
    public MiscWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.6625f, Size.PERCENT_OFFSET + 0.0583f );
    }
}
