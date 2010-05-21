package net.ctdp.rfdynhud.widgets.standings;

import java.io.IOException;
import java.util.Arrays;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.FinishStatus;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.StandingsTools;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.LongValue;
import net.ctdp.rfdynhud.values.NameDisplayType;
import net.ctdp.rfdynhud.values.StandingsView;
import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link StandingsWidget} displays engine information.
 * 
 * @author Marvin Froehlich
 */
public class StandingsWidget extends Widget
{
    private static final InputAction INPUT_ACTION_CYCLE_VIEW = new InputAction( "CycleStandingsViewAction" );
    
    private final ColorProperty fontColor_me = new ColorProperty( this, "fontColor_me", "#367727" );
    private final ColorProperty fontColor_out = new ColorProperty( this, "fontColor_out", "#646464" );
    private final ColorProperty fontColor_finished = new ColorProperty( this, "fontColor_finished", "#00FF00" );
    
    private final BooleanProperty useAutoWidth = new BooleanProperty( this, "useAutoWidth", false );
    
    private final EnumProperty<StandingsView> initialView = new EnumProperty<StandingsView>( this, "initialView", StandingsView.RELATIVE_TO_ME )
    {
        @Override
        protected void onValueChanged( StandingsView oldValue, StandingsView newValue )
        {
            ( (LocalStore)getLocalStore() ).view = null;
        }
    };
    private final BooleanProperty allowRelToLeaderView = new BooleanProperty( this, "allowRelToLeaderView", true );
    private final BooleanProperty allowRelToMeView = new BooleanProperty( this, "allowRelToMeView", true );
    private final BooleanProperty allowAbsTimesView = new BooleanProperty( this, "allowAbsTimesView", true );
    
    private final BooleanProperty forceLeaderDisplayed = new BooleanProperty( this, "forceLeaderDisplayed", true );
    private final EnumProperty<NameDisplayType> nameDisplayType = new EnumProperty<NameDisplayType>( this, "nameDisplayType", NameDisplayType.FULL_NAME );
    private final BooleanProperty showLapsOrStops = new BooleanProperty( this, "showLapsOrStops", true );
    private final BooleanProperty abbreviate = new BooleanProperty( this, "abbreviate", false );
    private final BooleanProperty showTopspeeds = new BooleanProperty( this, "showTopspeeds", true );
    
    private final LongValue lastScoringUpdateId = new LongValue();
    
    private DrawnString[] positionStrings = null;
    private int maxDisplayedDrivers = 100;
    
    private String[][] currPosStrings = null;
    private final int[] oldColWidths = { 0, 0, 0, 0, 0, 0, 0 };
    private final int[] colWidths = { 0, 0, 0, 0, 0, 0, 0 };
    private final Alignment[] colAligns = { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT, Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT, Alignment.LEFT };
    private final int colPadding = 8;
    //private int[] vsiIndices = null;
    private int numVehicles = -1;
    private int maxNumVehicles = -1;
    private VehicleScoringInfo[] vehicleScoringInfos = null;
    
    private int oldNumVehicles = -1;
    private String[][] oldPosStrings = null;
    
    private final float[] relTimes = new float[ 64 ];
    
    @Override
    public String getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Object createLocalStore()
    {
        return ( new LocalStore() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public InputAction[] getInputActions()
    {
        return ( new InputAction[] { INPUT_ACTION_CYCLE_VIEW } );
    }
    
    private static final String[][] ensureCapacity( String[][] array, int minCapacity, boolean preserveValues )
    {
        if ( array == null )
        {
            array = new String[ minCapacity ][];
        }
        else if ( array.length < minCapacity )
        {
            String[][] tmp = new String[ minCapacity ][];
            if ( preserveValues )
                System.arraycopy( array, 0, tmp, 0, array.length );
            array = tmp;
        }
        
        return ( array );
    }
    
    public void setView( StandingsView view )
    {
        ( (LocalStore)getLocalStore() ).view = view;
        
        lastScoringUpdateId.reset( true );
        forceCompleteRedraw();
        setDirtyFlag();
    }
    
    public final StandingsView getView()
    {
        if ( ( (LocalStore)getLocalStore() ).view == null )
        {
            ( (LocalStore)getLocalStore() ).view = initialView.getEnumValue();
        }
        
        return ( ( (LocalStore)getLocalStore() ).view );
    }
    
    public void allowViews( boolean relToLeader, boolean relToMe, boolean absTimes )
    {
        this.allowRelToLeaderView.setBooleanValue( relToLeader );
        this.allowRelToMeView.setBooleanValue( relToMe );
        this.allowAbsTimesView.setBooleanValue( absTimes );
    }
    
    public final boolean isRelToLeaderViewAllowed()
    {
        return ( allowRelToLeaderView.getBooleanValue() );
    }
    
    public final boolean isRelToMeViewAllowed()
    {
        return ( allowRelToMeView.getBooleanValue() );
    }
    
    public final boolean isAbsTimesViewAllowed( boolean isEditorMode, SessionType sessionType )
    {
        return ( ( isEditorMode || !sessionType.isRace() ) && allowAbsTimesView.getBooleanValue() );
    }
    
    private final boolean checkView( boolean isEditorMode, StandingsView view, SessionType sessionType )
    {
        switch ( view )
        {
            case RELATIVE_TO_LEADER:
                return ( isRelToLeaderViewAllowed() );
            case RELATIVE_TO_ME:
                return ( isRelToMeViewAllowed() );
            case ABSOLUTE_TIMES:
                return ( isAbsTimesViewAllowed( isEditorMode, sessionType ) );
        }
        
        // Unreachable code!
        return ( true );
    }
    
    private StandingsView cycleView( boolean isEditorMode, SessionType sessionType, boolean includeVisibility )
    {
        final StandingsView[] views = StandingsView.values();
        
        if ( includeVisibility )
        {
            //StandingsView oldView = getView();
            if ( !isInputVisible() )
            {
                for ( int i = 0; i < views.length; i++ )
                {
                    if ( checkView( isEditorMode, views[i], sessionType ) )
                    {
                        setInputVisible( true );
                        setView( views[i] );
                        
                        return ( getView() );
                    }
                }
                
                setInputVisible( true );
                return ( null );
            }
            
            int v0 = getView().ordinal();
            for ( int i = 1; i < views.length; i++ )
            {
                StandingsView sv = views[( v0 + i ) % views.length];
                if ( checkView( isEditorMode, sv, sessionType ) )
                {
                    setView( sv );
                    
                    return ( getView() );
                }
            }
            
            setInputVisible( false );
            return ( null );
        }
        
        setInputVisible( true );
        
        int offset = getView().ordinal();
        for ( int i = 1; i < views.length; i++ )
        {
            if ( checkView( isEditorMode, views[( offset + i ) % views.length], sessionType ) )
            {
                setView( views[( offset + i ) % views.length] );
                
                return ( getView() );
            }
        }
        
        return ( getView() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        lastScoringUpdateId.reset();
        
        SessionType sessionType = gameData.getScoringInfo().getSessionType();
        
        if ( ( editorPresets == null ) && sessionType.isRace() && ( getView() == StandingsView.ABSOLUTE_TIMES ) )
            cycleView( false, sessionType, false );
        
        if ( oldPosStrings != null )
            Arrays.fill( oldPosStrings, null );
        
        Arrays.fill( oldColWidths, -1 );
        
        this.oldNumVehicles = -1;
        
        if ( editorPresets != null )
            this.maxNumVehicles = 23;
        else
            this.maxNumVehicles = RFactorTools.getMaxOpponents() + 1;
        
        //forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onVehicleControlChanged( viewedVSI, gameData, editorPresets );
        
        lastScoringUpdateId.reset();
        forceCompleteRedraw();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( action == INPUT_ACTION_CYCLE_VIEW )
        {
            cycleView( ( editorPresets != null ), gameData.getScoringInfo().getSessionType(), true );
        }
    }
    
    private final String getDisplayedDriverName( VehicleScoringInfo vsi )
    {
        switch ( nameDisplayType.getEnumValue() )
        {
            case FULL_NAME:
                return ( vsi.getDriverName() );
            case SHORT_FORM:
                return ( vsi.getDriverNameShort() );
            case THREE_LETTER_CODE:
                return ( vsi.getDriverNameTLC() );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    private static final String getSpeedUnits()
    {
        switch ( RFactorTools.getSpeedUnits() )
        {
            case MPH:
                return ( Loc.column_topspeed_units_IMPERIAL );
            case KPH:
            default:
                return ( Loc.column_topspeed_units_METRIC );
        }
    }
    
    private String[] getPositionStringRaceRelToLeader( GamePhase gamePhase, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 7 ];
        
        ss[0] = place + ".";
        
        ss[1] = getDisplayedDriverName( vsi );
        
        FinishStatus finishStatus = vsi.getFinishStatus();
        if ( finishStatus.isNone() )
        {
            if ( ( gamePhase == GamePhase.RECONNAISSANCE_LAPS ) || ( gamePhase == GamePhase.FORMATION_LAP ) )
            {
                ss[2] = null;
            }
            else
            {
                int lbl = vsi.getLapsBehindLeader();
                if ( lbl > 0 )
                {
                    ss[2] = "(+" + vsi.getLapsBehindLeader() + ( abbreviate.getBooleanValue() ? Loc.column_time_gap_laps_short : ( ( lbl == 1 ) ? " " + Loc.column_time_gap_laps_singular + ")" : " " + Loc.column_time_gap_laps_plural + ")" ) );
                }
                else
                {
                    float sbl = -vsi.getTimeBehindLeader();
                    ss[2] = "(" + TimingUtil.getTimeAsGapString( sbl ) + ")";
                }
            }
            
            if ( showLapsOrStops.getBooleanValue() )
            {
                int stops = vsi.getNumPitstopsMade();
                if ( abbreviate.getBooleanValue() )
                {
                    ss[3] = String.valueOf( stops ) + Loc.column_stops_short;
                    ss[4] = null;
                }
                else if ( stops == 1 )
                {
                    ss[3] = String.valueOf( stops );
                    ss[4] = Loc.column_stops_singular;
                }
                else
                {
                    ss[3] = String.valueOf( stops );
                    ss[4] = Loc.column_stops_plural;
                }
            }
            else
            {
                ss[3] = null;
                ss[4] = null;
            }
            
            if ( showTopspeeds.getBooleanValue() )
            {
                ss[5] = NumberUtil.formatFloat( vsi.getTopspeed(), 1, true );
                ss[6] = getSpeedUnits();
            }
            else
            {
                ss[5] = null;
                ss[6] = null;
            }
        }
        else if ( finishStatus == FinishStatus.FINISHED )
        {
            ss[2] = "(" + Loc.finishsstatus_FINISHED + ")";
            ss[3] = null;
            ss[4] = null;
            ss[5] = null;
            ss[6] = null;
        }
        else
        {
            switch ( finishStatus )
            {
                case DNF:
                    ss[2] = Loc.out + " (" + Loc.finishsstatus_DNF + ")";
                    break;
                case DQ:
                    ss[2] = Loc.out + " (" + Loc.finishsstatus_DQ + ")";
                    break;
            }
            
            ss[3] = null;
            ss[4] = null;
            ss[5] = null;
            ss[6] = null;
        }
        
        return ( ss );
    }
    
    private String[] getPositionStringRaceRelToMe( int ownPlace, int ownLaps, float ownLapDistance, float relTime, GamePhase gamePhase, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 7 ];
        ss[0] = place + ".";
        
        ss[1] = getDisplayedDriverName( vsi );
        
        FinishStatus finishStatus = vsi.getFinishStatus();
        if ( finishStatus.isNone() )
        {
            if ( ( gamePhase != GamePhase.FORMATION_LAP ) && ( gamePhase != GamePhase.RECONNAISSANCE_LAPS ) && ( place != ownPlace ) )
            {
                int lapDiff = ownLaps - vsi.getLapsCompleted();
                if ( ( lapDiff > 0 ) && ( ownLapDistance < vsi.getLapDistance() ) )
                    lapDiff--;
                else if ( ( lapDiff < 0 ) && ( ownLapDistance > vsi.getLapDistance() ) )
                    lapDiff++;
                
                if ( lapDiff < 0 )
                {
                    if ( abbreviate.getBooleanValue() )
                        ss[2] = "(" + lapDiff + Loc.column_time_gap_laps_short + ")";
                    else
                        ss[2] = "(" + lapDiff + " " + ( ( lapDiff < -1 ) ? Loc.column_time_gap_laps_plural : Loc.column_time_gap_laps_singular ) + ")";
                }
                else if ( lapDiff > 0 )
                {
                    if ( abbreviate.getBooleanValue() )
                        ss[2] = "(+" + lapDiff + Loc.column_time_gap_laps_short + ")";
                    else
                        ss[2] = "(+" + lapDiff + " " + ( ( lapDiff > 1 ) ? Loc.column_time_gap_laps_plural : Loc.column_time_gap_laps_singular ) + ")";
                }
                else
                {
                    ss[2] = "(" + TimingUtil.getTimeAsGapString( relTime ) + ")";
                }
            }
            else
            {
                ss[2] = null;
            }
            
            if ( showLapsOrStops.getBooleanValue() )
            {
                int stops = vsi.getNumPitstopsMade();
                if ( abbreviate.getBooleanValue() )
                {
                    ss[3] = String.valueOf( stops ) + Loc.column_stops_short;
                    ss[4] = null;
                }
                else if ( stops == 1 )
                {
                    ss[3] = String.valueOf( stops );
                    ss[4] = Loc.column_stops_singular;
                }
                else
                {
                    ss[3] = String.valueOf( stops );
                    ss[4] = Loc.column_stops_plural;
                }
            }
            else
            {
                ss[3] = null;
                ss[4] = null;
            }
            
            if ( showTopspeeds.getBooleanValue() )
            {
                ss[5] = NumberUtil.formatFloat( vsi.getTopspeed(), 1, true );
                ss[6] = getSpeedUnits();
            }
            else
            {
                ss[5] = null;
                ss[6] = null;
            }
        }
        else if ( finishStatus == FinishStatus.FINISHED )
        {
            ss[2] = "(" + Loc.finishsstatus_FINISHED + ")";
            ss[3] = null;
            ss[4] = null;
            ss[5] = null;
            ss[6] = null;
        }
        else
        {
            switch ( finishStatus )
            {
                case DNF:
                    ss[2] = Loc.out + " (" + Loc.finishsstatus_DNF + ")";
                    break;
                case DQ:
                    ss[2] = Loc.out + " (" + Loc.finishsstatus_DQ + ")";
                    break;
            }
            
            ss[3] = null;
            ss[4] = null;
            ss[5] = null;
            ss[6] = null;
        }
        
        return ( ss );
    }
    
    private String[] getPosStringRace( int ownPlace, int ownLaps, float ownLapDistance, GamePhase gamePhase, VehicleScoringInfo vsi )
    {
        switch ( getView() )
        {
            case RELATIVE_TO_LEADER:
                return ( getPositionStringRaceRelToLeader( gamePhase, vsi ) );
            case RELATIVE_TO_ME:
                return ( getPositionStringRaceRelToMe( ownPlace, ownLaps, ownLapDistance, relTimes[vsi.getPlace() - 1], gamePhase, vsi ) );
            case ABSOLUTE_TIMES: // Only possible in the editor!
                return ( getPositionStringNonRaceAbsTimes( vsi ) );
        }
        
        // unreachable code
        return ( null );
    }
    
    private int initPosStringsRace( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        GamePhase gamePhase = scoringInfo.getGamePhase();
        VehicleScoringInfo myVSI = scoringInfo.getViewedVehicleScoringInfo();
        
        numVehicles = StandingsTools.getDisplayedVSIsForScoring( scoringInfo, myVSI, getView(), forceLeaderDisplayed.getBooleanValue(), vehicleScoringInfos );
        
        int ownPlace = myVSI.getPlace();
        int ownLaps = myVSI.getLapsCompleted();
        float ownLapDistance = myVSI.getLapDistance();
        
        if ( getView() == StandingsView.RELATIVE_TO_ME )
        {
            StandingsTools.computeRelativeTimesRace( scoringInfo, myVSI, relTimes );
        }
        
        currPosStrings = ensureCapacity( currPosStrings, numVehicles, false );
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            currPosStrings[i] = getPosStringRace( ownPlace, ownLaps, ownLapDistance, gamePhase, vehicleScoringInfos[i] );
        }
        
        return ( numVehicles );
    }
    
    private String[] getPositionStringNonRaceRelToLeader( int firstVisiblePlace, float bestTime, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 7 ];
        ss[0] = place + ".";
        
        ss[1] = getDisplayedDriverName( vsi );
        
        float t = vsi.getBestLapTime();
        
        if ( t > 0f )
        {
            if ( place == firstVisiblePlace )
                ss[2] = "(" + TimingUtil.getTimeAsLaptimeString( t ) + ")";
            else
                ss[2] = "(" + TimingUtil.getTimeAsGapString( t - bestTime ) + ")";
        }
        else
        {
            ss[2] = null;
        }
        
        if ( showLapsOrStops.getBooleanValue() )
        {
            int lapsCompleted = vsi.getLapsCompleted();
            if ( abbreviate.getBooleanValue() )
            {
                ss[3] = String.valueOf( lapsCompleted ) + Loc.column_laps_short;
                ss[4] = null;
            }
            else if ( lapsCompleted == 1 )
            {
                ss[3] = String.valueOf( lapsCompleted );
                ss[4] = Loc.column_laps_singular;
            }
            else
            {
                ss[3] = String.valueOf( lapsCompleted );
                ss[4] = Loc.column_laps_plural;
            }
        }        
        else
        {
            ss[3] = null;
            ss[4] = null;
        }
        
        if ( showTopspeeds.getBooleanValue() )
        {
            ss[5] = NumberUtil.formatFloat( vsi.getTopspeed(), 1, true );
            ss[6] = getSpeedUnits();
        }
        else
        {
            ss[5] = null;
            ss[6] = null;
        }
        
        return ( ss );
    }
    
    private String[] getPositionStringNonRaceRelToMe( int ownPlace, float ownTime, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 7 ];
        ss[0] = place + ".";
        
        ss[1] = getDisplayedDriverName( vsi );
        
        float t = vsi.getBestLapTime();
        
        if ( t > 0f )
        {
            if ( place == ownPlace )
                ss[2] = "(" + TimingUtil.getTimeAsLaptimeString( t ) + ")";
            else
                ss[2] = "(" + TimingUtil.getTimeAsGapString( t - ownTime ) + ")";
        }
        else
        {
            ss[2] = null;
        }
        
        if ( showLapsOrStops.getBooleanValue() )
        {
            int lapsCompleted = vsi.getLapsCompleted();
            if ( abbreviate.getBooleanValue() )
            {
                ss[3] = String.valueOf( lapsCompleted ) + Loc.column_laps_short;
                ss[4] = null;
            }
            else if ( lapsCompleted == 1 )
            {
                ss[3] = String.valueOf( lapsCompleted );
                ss[4] = Loc.column_laps_singular;
            }
            else
            {
                ss[3] = String.valueOf( lapsCompleted );
                ss[4] = Loc.column_laps_plural;
            }
        }        
        else
        {
            ss[3] = null;
            ss[4] = null;
        }
        
        if ( showTopspeeds.getBooleanValue() )
        {
            ss[5] = NumberUtil.formatFloat( vsi.getTopspeed(), 1, true );
            ss[6] = getSpeedUnits();
        }
        else
        {
            ss[5] = null;
            ss[6] = null;
        }
        
        return ( ss );
    }
    
    private String[] getPositionStringNonRaceAbsTimes( VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 7 ];
        ss[0] = place + ".";
        
        ss[1] = getDisplayedDriverName( vsi );
        
        float t = vsi.getBestLapTime();
        
        if ( t > 0f )
        {
            ss[2] = TimingUtil.getTimeAsLaptimeString( t );
        }
        else
        {
            ss[2] = null;
        }
        
        if ( showLapsOrStops.getBooleanValue() )
        {
            int lapsCompleted = vsi.getLapsCompleted();
            if ( abbreviate.getBooleanValue() )
            {
                ss[3] = String.valueOf( lapsCompleted ) + Loc.column_laps_short;
                ss[4] = null;
            }
            else if ( lapsCompleted == 1 )
            {
                ss[3] = String.valueOf( lapsCompleted );
                ss[4] = Loc.column_laps_singular;
            }
            else
            {
                ss[3] = String.valueOf( lapsCompleted );
                ss[4] = Loc.column_laps_plural;
            }
        }
        else
        {
            ss[3] = null;
            ss[4] = null;
        }
        
        if ( showTopspeeds.getBooleanValue() )
        {
            ss[5] = NumberUtil.formatFloat( vsi.getTopspeed(), 1, true );
            ss[6] = getSpeedUnits();
        }
        else
        {
            ss[5] = null;
            ss[6] = null;
        }
        
        return ( ss );
    }
    
    private String[] getPosStringNonRace( VehicleScoringInfo vsi, int firstVisiblePlace, int ownPlace, float ownTime, float bestTime )
    {
        if ( getView() == StandingsView.ABSOLUTE_TIMES )
            return ( getPositionStringNonRaceAbsTimes( vsi ) );
        
        if ( ( getView() == StandingsView.RELATIVE_TO_LEADER ) || ( ownTime <= 0f ) )
            return ( getPositionStringNonRaceRelToLeader( firstVisiblePlace, bestTime, vsi ) );
        
        return ( getPositionStringNonRaceRelToMe( ownPlace, ownTime, vsi ) );
    }
    
    private int initPosStringsNonRace( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        VehicleScoringInfo ownVSI = scoringInfo.getViewedVehicleScoringInfo();
        
        numVehicles = StandingsTools.getDisplayedVSIsForScoring( scoringInfo, ownVSI, getView(), forceLeaderDisplayed.getBooleanValue(), vehicleScoringInfos );
        
        int firstVisiblePlace = vehicleScoringInfos[0].getPlace();
        float bestTime = vehicleScoringInfos[0].getBestLapTime();
        int ownPlace = ownVSI.getPlace();
        float ownTime = ownVSI.getBestLapTime();
        
        currPosStrings = ensureCapacity( currPosStrings, numVehicles, false );
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            VehicleScoringInfo vsi = vehicleScoringInfos[i];
            
            currPosStrings[i] = getPosStringNonRace( vsi, firstVisiblePlace, ownPlace, ownTime, bestTime );
        }
        
        return ( numVehicles );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWidth( LiveGameData gameData, Texture2DCanvas texCanvas )
    {
        if ( !useAutoWidth.getBooleanValue() )
            return ( super.getMaxWidth( gameData, texCanvas ) );
        
        DrawnString ds = getDrawnStringFactory().newDrawnString( null, 10, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        String[] strs = { "99.", ( nameDisplayType.getEnumValue() == NameDisplayType.THREE_LETTER_CODE ) ? "AAAA" : ( ( nameDisplayType.getEnumValue() == NameDisplayType.SHORT_FORM ) ? "G. Fisichella___" : "Giancarlo Fisichella___" ), "-1:99:99.999", "99" + ( abbreviate.getBooleanValue() ? "S" : " Stops" ) };
        
        int total = ds.getMinColWidths( strs, colAligns, colPadding, texCanvas.getImage(), colWidths );
        
        return ( total );
    }
    
    private void initPositionStrings( int numVehicles )
    {
        DrawnStringFactory dsf = getDrawnStringFactory();
        
        if ( ( positionStrings == null ) || ( positionStrings.length < maxNumVehicles ) )
        {
            positionStrings = new DrawnString[ numVehicles ];
            
            for ( int i = 0; i < numVehicles; i++ )
            {
                if ( i == 0 )
                    positionStrings[i] = dsf.newDrawnString( "positionStrings" + i, 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
                else
                    positionStrings[i] = dsf.newDrawnString( "positionStrings" + i, null, positionStrings[i - 1], 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
            }
        }
    }
    
    private int updateColumnWidths( TextureImage2D texture, int widgetWidth )
    {
        Arrays.fill( colWidths, 0 );
        
        int minWidth = 0;
        for ( int i = 0; i < numVehicles; i++ )
        {
            int w = positionStrings[i].getMaxColWidths( currPosStrings[i], colAligns, colPadding, texture, colWidths );
            if ( w > minWidth )
                minWidth = w;
        }
        
        if ( colWidths[5] > 0 )
        {
            colWidths[5] += 15;
            minWidth += 15;
        }
        
        if ( !useAutoWidth.getBooleanValue() )
        {
            if ( minWidth < widgetWidth )
                colWidths[1] += widgetWidth - minWidth;
        }
        
        return ( minWidth );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        lastScoringUpdateId.reset( true );
        
        initPositionStrings( gameData.getScoringInfo().getNumVehicles() );
        
        int h = height + getBorder().getInnerBottomHeight() - getBorder().getOpaqueBottomHeight();
        int rowHeight = positionStrings[0].getMaxHeight( texture, false );
        maxDisplayedDrivers = Math.max( 1, h / rowHeight );
        
        vehicleScoringInfos = new VehicleScoringInfo[ maxDisplayedDrivers ];
        
        numVehicles = -1;
        oldPosStrings = null;
        currPosStrings = null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        final boolean isEditorMode = ( editorPresets != null );
        
        lastScoringUpdateId.update( scoringInfo.getUpdateId() );
        
        if ( !lastScoringUpdateId.hasChanged() )
            return ( false );
        
        if ( gameData.getScoringInfo().getSessionType().isRace() )
            initPosStringsRace( gameData );
        else
            initPosStringsNonRace( gameData );
        
        boolean result = false;
        
        if ( oldNumVehicles != numVehicles )
        {
            oldNumVehicles = numVehicles;
            result = true;
        }
        
        int minWidth = updateColumnWidths( texture, width );
        
        if ( !useAutoWidth.getBooleanValue() )
            return ( result );
        
        //int padding = 2 * 8;
        int padding = 0;
        minWidth += padding;
        
        if ( ( isEditorMode && ( Math.abs( ( width + padding ) - minWidth ) > 1 ) ) || ( width + padding != minWidth ) )
        {
            clearRegion( isEditorMode, texture );
            getSize().setEffectiveSize( getBorder().getWidgetWidth( minWidth ), getBorder().getWidgetHeight( height ) );
            
            result = true;
        }
        
        return ( result );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        oldPosStrings = ensureCapacity( oldPosStrings, numVehicles, true );
        
        if ( clock2 && !Arrays.equals( oldColWidths, colWidths ) )
        {
            needsCompleteRedraw = true;
            System.arraycopy( colWidths, 0, oldColWidths, 0, colWidths.length );
        }
        
        VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            if ( needsCompleteRedraw || ( clock2 && !Arrays.equals( currPosStrings[i], oldPosStrings[i] ) ) )
            {
                java.awt.Color fc = null;
                
                switch ( vehicleScoringInfos[i].getFinishStatus() )
                {
                    case NONE:
                        if ( vehicleScoringInfos[i].equals( viewedVSI ) )
                            fc = fontColor_me.getColor();
                        break;
                    case DNF:
                    case DQ:
                        fc = fontColor_out.getColor();
                        break;
                    case FINISHED:
                        fc = fontColor_finished.getColor();
                        break;
                }
                
                positionStrings[i].drawColumns( offsetX, offsetY, currPosStrings[i], colAligns, colPadding, colWidths, getBackgroundColor(), fc, texture );
                
                oldPosStrings[i] = currPosStrings[i];
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
        
        writer.writeProperty( fontColor_me, "The font color used for myself in the format #RRGGBB (hex)." );
        writer.writeProperty( fontColor_out, "The font color used for retired drivers in the format #RRGGBB (hex)." );
        writer.writeProperty( fontColor_finished, "The font color used for finished drivers in the format #RRGGBB (hex)." );
        writer.writeProperty( useAutoWidth, "Automatically compute and display the width?" );
        writer.writeProperty( initialView, "the initial kind of standings view. Valid values: RELATIVE_TO_LEADER, RELATIVE_TO_ME." );
        //writer.writeProperty( allowAbsTimesView, "" );
        //writer.writeProperty( allowRelToLeaderView, "" );
        //writer.writeProperty( allowRelToMeView, "" );
        writer.writeProperty( forceLeaderDisplayed, "Display leader regardless of maximum displayed drivers setting?" );
        writer.writeProperty( nameDisplayType, "How to display driver names." );
        writer.writeProperty( showLapsOrStops, "Whether to show the number of laps or stops done or not." );
        writer.writeProperty( abbreviate, "Whether to abbreviate \"Stops\", or not." );
        writer.writeProperty( showTopspeeds, "Whether to show a topspeeds column or not." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( fontColor_me.loadProperty( key, value ) );
        else if ( fontColor_out.loadProperty( key, value ) );
        else if ( fontColor_finished.loadProperty( key, value ) );
        else if ( useAutoWidth.loadProperty( key, value ) );
        else if ( initialView.loadProperty( key, value ) );
        //else if ( allowAbsTimesView.loadProperty( key, value ) );
        //else if ( allowRelToLeaderView.loadProperty( key, value ) );
        //else if ( allowRelToMeView.loadProperty( key, value ) );
        else if ( forceLeaderDisplayed.loadProperty( key, value ) );
        else if ( nameDisplayType.loadProperty( key, value ) );
        else if ( showLapsOrStops.loadProperty( key, value ) );
        else if ( abbreviate.loadProperty( key, value ) );
        else if ( showTopspeeds.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( fontColor_me );
        propsCont.addProperty( fontColor_out );
        propsCont.addProperty( fontColor_finished );
        propsCont.addProperty( useAutoWidth );
        
        propsCont.addProperty( initialView );
        //propsCont.addProperty( allowAbsTimesView );
        //propsCont.addProperty( allowRelToLeaderView );
        //propsCont.addProperty( allowRelToMeView );
        
        propsCont.addProperty( forceLeaderDisplayed );
        propsCont.addProperty( nameDisplayType );
        propsCont.addProperty( showLapsOrStops );
        if ( forceAll || showLapsOrStops.getBooleanValue() )
            propsCont.addProperty( abbreviate );
        propsCont.addProperty( showTopspeeds );
    }
    
    public StandingsWidget( String name )
    {
        super( name, 36.328125f, 14.916667f );
        
        getFontProperty().setFont( "BiggerFont" );
    }
}
