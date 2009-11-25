package net.ctdp.rfdynhud.widgets.standings;

import java.io.IOException;
import java.util.Arrays;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.BooleanProperty;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.EnumProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.FinishStatus;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.TimingUtil;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets._util.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link StandingsWidget} displays engine information.
 * 
 * @author Marvin Froehlich
 */
public class StandingsWidget extends Widget
{
    public static enum StandingsView
    {
        RELATIVE_TO_LEADER,
        RELATIVE_TO_ME,
        ABSOLUTE_TIMES,
        ;
    }
    
    private static enum NameDisplayType
    {
        FULL_NAME,
        SHORT_FORM,
        THREE_LETTER_CODE,
        ;
    }
    
    private static final InputAction INPUT_ACTION_CYCLE_VIEW = new InputAction( "CycleStandingsViewAction" );
    
    private final ColorProperty fontColor_me = new ColorProperty( this, "fontColor_me", "#7A7727" );
    private final ColorProperty fontColor_out = new ColorProperty( this, "fontColor_out", "#646464" );
    private final ColorProperty fontColor_finished = new ColorProperty( this, "fontColor_finished", "#00FF00" );
    
    private final BooleanProperty useAutoWidth = new BooleanProperty( this, "useAutoWidth", false );
    
    private final BooleanProperty allowRelToLeaderView = new BooleanProperty( this, "allowRelToLeaderView", true );
    private final BooleanProperty allowRelToMeView = new BooleanProperty( this, "allowRelToMeView", true );
    private final BooleanProperty allowAbsTimesView = new BooleanProperty( this, "allowAbsTimesView", true );
    
    private final BooleanProperty forceLeaderDisplayed = new BooleanProperty( this, "allowRelToLeaderView", true );
    private final EnumProperty<NameDisplayType> nameDisplayType = new EnumProperty<NameDisplayType>( this, "nameDisplayType", NameDisplayType.FULL_NAME );
    private final BooleanProperty abbreviate = new BooleanProperty( this, "abbreviate", false );
    
    private DrawnString[] positionStrings = null;
    private int maxDisplayedDrivers = 100;
    
    private String[][] currPosStrings = null;
    private final int[] colWidths = { 0, 0, 0, 0 };
    private final Alignment[] aligns = { Alignment.RIGHT, Alignment.LEFT, Alignment.RIGHT, Alignment.RIGHT };
    @SuppressWarnings( "unused" )
    private int playerPosStringIndex = -1;
    private int[] vsiIndices = null;
    private int numVehicles = -1;
    
    private int oldNumVehicles = -1;
    private String[][] oldPosStirngs = null;
    
    private float[] relTimes = null;
    
    private SessionType lastKnownSessionType = SessionType.PRACTICE1;
    
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
    
    private static final int[] ensureCapacity( int[] array, int minCapacity, boolean preserveValues )
    {
        if ( array == null )
        {
            array = new int[ minCapacity ];
        }
        else if ( array.length < minCapacity )
        {
            int[] tmp = new int[ minCapacity ];
            if ( preserveValues )
                System.arraycopy( array, 0, tmp, 0, array.length );
            array = tmp;
        }
        
        return ( array );
    }
    
    private static final float[] ensureCapacity( float[] array, int minCapacity, boolean preserveValues )
    {
        if ( array == null )
        {
            array = new float[ minCapacity ];
        }
        else if ( array.length < minCapacity )
        {
            float[] tmp = new float[ minCapacity ];
            if ( preserveValues )
                System.arraycopy( array, 0, tmp, 0, array.length );
            array = tmp;
        }
        
        return ( array );
    }
    
    public void setView( StandingsView view )
    {
        LocalStore store = (LocalStore)getLocalStore();
        store.view = view;
        
        forceCompleteRedraw();
        setDirtyFlag();
    }
    
    public final StandingsView getView()
    {
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
    
    public final boolean isAbsTimesViewAllowed( SessionType sessionType )
    {
        return ( !sessionType.isRace() && allowAbsTimesView.getBooleanValue() );
    }
    
    private final boolean checkView( StandingsView view, SessionType sessionType )
    {
        switch ( view )
        {
            case RELATIVE_TO_LEADER:
                return ( isRelToLeaderViewAllowed() );
            case RELATIVE_TO_ME:
                return ( isRelToMeViewAllowed() );
            case ABSOLUTE_TIMES:
                return ( isAbsTimesViewAllowed( sessionType ) );
        }
        
        // Unreachable code!
        return ( true );
    }
    
    public StandingsView cycleView( SessionType sessionType, boolean includeVisibility )
    {
        final StandingsView[] views = StandingsView.values();
        
        if ( includeVisibility )
        {
            //StandingsView oldView = getView();
            if ( !isVisible() )
            {
                for ( int i = 0; i < views.length; i++ )
                {
                    if ( checkView( views[i], sessionType ) )
                    {
                        setVisible( true );
                        setView( views[i] );
                        
                        return ( getView() );
                    }
                }
                
                setVisible( true );
                return ( null );
            }
            
            for ( int i = ( (LocalStore)getLocalStore() ).view.ordinal() + 1; i < views.length; i++ )
            {
                if ( checkView( views[i], sessionType ) )
                {
                    setView( views[i] );
                    
                    return ( getView() );
                }
            }
            
            //Logger.log( "cycleView(): " + sessionType + ", " + includeVisibility + ", " + oldView + ", " + getView() );
            
            setVisible( false );
            return ( null );
        }
        
        setVisible( true );
        
        int offset = ( (LocalStore)getLocalStore() ).view.ordinal();
        for ( int i = 1; i < views.length; i++ )
        {
            if ( checkView( views[( offset + i ) % views.length], sessionType ) )
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
    public void onRealtimeEntered( boolean isEditorMode, LiveGameData gameData )
    {
        super.onRealtimeEntered( isEditorMode, gameData );
        
        lastKnownSessionType = gameData.getScoringInfo().getSessionType();
        
        if ( lastKnownSessionType.isRace() && ( getView() == StandingsView.ABSOLUTE_TIMES ) )
            cycleView( lastKnownSessionType, false );
        
        oldNumVehicles = -1;
        if ( oldPosStirngs != null )
        {
            for ( int i = 0; i < oldPosStirngs.length; i++ )
                oldPosStirngs[i] = null;
        }
        
        //forceReinitialization();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
        if ( action == INPUT_ACTION_CYCLE_VIEW )
        {
            cycleView( lastKnownSessionType, true );
        }
    }
    
    private final String getDisplayedDriverName( String driverName )
    {
        switch ( nameDisplayType.getEnumValue() )
        {
            case FULL_NAME:
                return ( driverName );
            case SHORT_FORM:
                return ( ThreeLetterCodeManager.getShortForm( driverName ) );
            case THREE_LETTER_CODE:
                return ( ThreeLetterCodeManager.getThreeLetterCode( driverName ) );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    private void computeRelativeTimesRace( ScoringInfo scoringInfo, int numVehicles, int ownPlace )
    {
        relTimes = ensureCapacity( relTimes, numVehicles, false );
        
        relTimes[ownPlace - 1] = 0f;
        
        for ( int i = ownPlace - 2; i >= 0; i-- )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i + 1 );
            
            relTimes[i] = relTimes[i + 1] + vsi.getTimeBehindNextInFront();
        }
        
        for ( int i = ownPlace; i < numVehicles; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            relTimes[i] = relTimes[i - 1] + -vsi.getTimeBehindNextInFront();
        }
    }
    
    private String[] getPositionStringRaceRelToLeader( int ownPlace, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 4 ];
        
        ss[0] = ( ( place < 10 ) ? " " : "" ) + place + ".";
        
        ss[1] = getDisplayedDriverName( vsi.getDriverName() );
        
        FinishStatus finishStatus = vsi.getFinishStatus();
        if ( finishStatus == FinishStatus.NONE )
        {
            if ( place > 1 )
            {
                int lbl = vsi.getLapsBehindLeader();
                if ( lbl > 0 )
                {
                    ss[2] = "(+" + vsi.getLapsBehindLeader() + ( ( lbl == 1 ) ? " Lap)" : " Laps)" );
                }
                else
                {
                    float sbl = -vsi.getTimeBehindLeader();
                    ss[2] = "(" + TimingUtil.getTimeAsGapString( sbl ) + ")";
                }
            }
            else
            {
                ss[2] = null;
            }
            
            int stops = vsi.getNumPitstopsMade();
            if ( abbreviate.getBooleanValue() )
                ss[3] = stops + "S";
            else if ( stops == 1 )
                ss[3] = stops + " Stop";
            else
                ss[3] = stops + " Stops";
        }
        else if ( finishStatus == FinishStatus.FINISHED )
        {
            ss[2] = "(" + finishStatus.toString() + ")";
            ss[3] = null;
        }
        else
        {
            ss[2] = "Out! (" + finishStatus.toString() + ")";
            ss[3] = null;
        }
        
        return ( ss );
    }
    
    private String[] getPositionStringRaceRelToMe( int ownPlace, int ownLaps, float ownLapDistance, float relTime, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 4 ];
        ss[0] = ( ( place < 10 ) ? " " : "" ) + place + ".";
        
        ss[1] = getDisplayedDriverName( vsi.getDriverName() );
        
        FinishStatus finishStatus = vsi.getFinishStatus();
        if ( finishStatus == FinishStatus.NONE )
        {
            if ( place != ownPlace )
            {
                int lapDiff = ownLaps - vsi.getLapsCompleted();
                if ( ( lapDiff > 0 ) && ( ownLapDistance < vsi.getLapDistance() ) )
                    lapDiff--;
                else if ( ( lapDiff < 0 ) && ( ownLapDistance > vsi.getLapDistance() ) )
                    lapDiff++;
                
                if ( lapDiff < 0 )
                {
                    if ( abbreviate.getBooleanValue() )
                        ss[2] = "(" + lapDiff + "L" + ")";
                    else
                        ss[2] = "(" + lapDiff + " Lap" + ( lapDiff < -1 ? "s" : "" ) + ")";
                }
                else if ( lapDiff > 0 )
                {
                    if ( abbreviate.getBooleanValue() )
                        ss[2] = "(+" + lapDiff + "L" + ")";
                    else
                        ss[2] = "(+" + lapDiff + " Lap" + ( lapDiff > 1 ? "s" : "" ) + ")";
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
            
            int stops = vsi.getNumPitstopsMade();
            if ( abbreviate.getBooleanValue() )
                ss[3] = stops + "S";
            else if ( stops == 1 )
                ss[3] = stops + " Stop";
            else
                ss[3] = stops + " Stops";
        }
        else if ( finishStatus == FinishStatus.FINISHED )
        {
            ss[2] = "(" + finishStatus.toString() + ")";
            ss[3] = null;
        }
        else
        {
            ss[2] = "Out! (" + finishStatus.toString() + ")";
            ss[3] = null;
        }
        
        return ( ss );
    }
    
    private String[] getPosStringRace( int i, VehicleScoringInfo vsi, int ownPlace, int ownLaps, float ownLapDistance )
    {
        switch ( getView() )
        {
            case RELATIVE_TO_LEADER:
                return ( getPositionStringRaceRelToLeader( ownPlace, vsi ) );
            case RELATIVE_TO_ME:
            case ABSOLUTE_TIMES:
                return ( getPositionStringRaceRelToMe( ownPlace, ownLaps, ownLapDistance, relTimes[i], vsi ) );
        }
        
        // unreachable code
        return ( null );
    }
    
    private int initPosStringsRace( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        numVehicles = scoringInfo.getNumVehicles();
        int ownPlace = scoringInfo.getOwnPlace();
        int ownLaps = scoringInfo.getVehicleScoringInfo( ownPlace - 1 ).getLapsCompleted();
        float ownLapDistance = scoringInfo.getVehicleScoringInfo( ownPlace - 1 ).getLapDistance();
        
        if ( getView() == StandingsView.RELATIVE_TO_ME )
        {
            computeRelativeTimesRace( scoringInfo, numVehicles, ownPlace );
        }
        
        currPosStrings = ensureCapacity( currPosStrings, numVehicles, false );
        vsiIndices = ensureCapacity( vsiIndices, numVehicles, false );
        
        int i0 = 0;
        int j = 0;
        if ( maxDisplayedDrivers < numVehicles )
        {
            numVehicles = maxDisplayedDrivers;
            i0 = Math.max( 0, ownPlace - (int)Math.ceil( ( numVehicles + 1 ) / 2f ) );
            
            if ( i0 + numVehicles > scoringInfo.getNumVehicles() )
            {
                i0 -= i0 + numVehicles - scoringInfo.getNumVehicles();
                
                if ( i0 < 0 )
                {
                    numVehicles += i0;
                    i0 = 0;
                }
            }
            
            if ( ( i0 > 0 ) && forceLeaderDisplayed.getBooleanValue() )
            {
                i0++;
                
                currPosStrings[j] = getPosStringRace( 0, scoringInfo.getVehicleScoringInfo( 0 ), ownPlace, ownLaps, ownLapDistance );
                vsiIndices[j] = 0;
                j++;
                if ( scoringInfo.getVehicleScoringInfo( 0 ).isPlayer() )
                    playerPosStringIndex = 0;
            }
        }
        
        int n = numVehicles - j;
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i0 + i );
            
            currPosStrings[j] = getPosStringRace( i0 + i, vsi, ownPlace, ownLaps, ownLapDistance );
            vsiIndices[j] = i0 + i;
            j++;
            if ( vsi.isPlayer() )
                playerPosStringIndex = 0;
        }
        
        return ( numVehicles );
    }
    
    private String[] getPositionStringNonRaceRelToLeader( int firstVisiblePlace, float bestTime, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 4 ];
        ss[0] = ( ( place < 10 ) ? " " : "" ) + place + ".";
        
        ss[1] = getDisplayedDriverName( vsi.getDriverName() );
        
        float t = vsi.getBestLapTime();
        
        if ( t > 0f )
        {
            if ( place == firstVisiblePlace )
                ss[2] = "(" + TimingUtil.getTimeAsString( t, false, false, true ) + ")";
            else
                ss[2] = "(" + TimingUtil.getTimeAsGapString( t - bestTime ) + ")";
        }
        else
        {
            ss[2] = null;
        }
        
        if ( vsi.getLapsCompleted() == 1 )
            ss[3] = vsi.getLapsCompleted() + " Lap";
        else
            ss[3] = vsi.getLapsCompleted() + " Laps";
        
        return ( ss );
    }
    
    private String[] getPositionStringNonRaceRelToMe( int ownPlace, float ownTime, VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 4 ];
        ss[0] = ( ( place < 10 ) ? " " : "" ) + place + ". ";
        
        ss[1] = getDisplayedDriverName( vsi.getDriverName() );
        
        float t = vsi.getBestLapTime();
        
        if ( t > 0f )
        {
            if ( place == ownPlace )
                ss[2] = "(" + TimingUtil.getTimeAsString( t, false, false, true ) + ")";
            else
                ss[2] = "(" + TimingUtil.getTimeAsGapString( t - ownTime ) + ")";
        }
        else
        {
            ss[2] = null;
        }
        
        if ( vsi.getLapsCompleted() == 1 )
            ss[3] = vsi.getLapsCompleted() + " Lap";
        else
            ss[3] = vsi.getLapsCompleted() + " Laps";
        
        return ( ss );
    }
    
    private String[] getPositionStringNonRaceAbsTimes( VehicleScoringInfo vsi )
    {
        short place = vsi.getPlace();
        
        String[] ss = new String[ 4 ];
        ss[0] = ( ( place < 10 ) ? " " : "" ) + place + ". ";
        
        ss[1] = getDisplayedDriverName( vsi.getDriverName() );
        
        float t = vsi.getBestLapTime();
        
        if ( t > 0f )
        {
            ss[2] = TimingUtil.getTimeAsString( t, false, false, true );
        }
        else
        {
            ss[2] = null;
        }
        
        if ( abbreviate.getBooleanValue() )
            ss[3] = vsi.getLapsCompleted() + "L";
        else if ( vsi.getLapsCompleted() == 1 )
            ss[3] = vsi.getLapsCompleted() + " Lap";
        else
            ss[3] = vsi.getLapsCompleted() + " Laps";
        
        return ( ss );
    }
    
    private String[] getPosStringNonRace( int i, VehicleScoringInfo vsi, int firstVisiblePlace, int ownPlace, float ownTime, float bestTime )
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
        
        numVehicles = scoringInfo.getNumVehicles();
        int ownPlace = scoringInfo.getOwnPlace();
        
        currPosStrings = ensureCapacity( currPosStrings, numVehicles, false );
        vsiIndices = ensureCapacity( vsiIndices, numVehicles, false );
        
        float bestTime = -1f;
        float ownTime = scoringInfo.getVehicleScoringInfo( ownPlace - 1 ).getBestLapTime();
        
        int i0 = 0;
        int j = 0;
        if ( maxDisplayedDrivers < numVehicles )
        {
            numVehicles = maxDisplayedDrivers;
            i0 = Math.max( 0, ownPlace - (int)Math.ceil( ( numVehicles + 1 ) / 2f ) );
            
            if ( i0 + numVehicles > scoringInfo.getNumVehicles() )
            {
                i0 -= i0 + numVehicles - scoringInfo.getNumVehicles();
                
                if ( i0 < 0 )
                {
                    numVehicles += i0;
                    i0 = 0;
                }
            }
            
            if ( ( i0 > 0 ) && forceLeaderDisplayed.getBooleanValue() )
            {
                i0++;
                
                bestTime = scoringInfo.getVehicleScoringInfo( 0 ).getBestLapTime();
                
                currPosStrings[j] = getPosStringNonRace( 0, scoringInfo.getVehicleScoringInfo( 0 ), 1, ownPlace, ownTime, bestTime );
                vsiIndices[j] = 0;
                j++;
                if ( scoringInfo.getVehicleScoringInfo( 0 ).isPlayer() )
                    playerPosStringIndex = 0;
            }
        }
        
        if ( j == 0 )
            bestTime = scoringInfo.getVehicleScoringInfo( i0 ).getBestLapTime();
        
        int n = numVehicles - j;
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i0 + i );
            
            currPosStrings[j] = getPosStringNonRace( i, vsi, i0 + 1, ownPlace, ownTime, bestTime );
            vsiIndices[j] = i0 + i;
            j++;
            if ( vsi.isPlayer() )
                playerPosStringIndex = 0;
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
        
        DrawnString ds = new DrawnString( 10, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        String[] strs = { "99.", ( nameDisplayType.getEnumValue() == NameDisplayType.THREE_LETTER_CODE ) ? "AAAA" : ( ( nameDisplayType.getEnumValue() == NameDisplayType.SHORT_FORM ) ? "G. Fisichella___" : "Giancarlo Fisichella___" ), "-1:99:99.999", "99" + ( abbreviate.getBooleanValue() ? "S" : " Stops" ) };
        
        ds.getMinColWidths( strs, 10, texCanvas.getImage(), colWidths );
        
        int total = 0;
        for ( int i = 0; i < colWidths.length; i++ )
            total += colWidths[i];
        
        return ( total );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( gameData.getScoringInfo().getSessionType() == SessionType.RACE )
            initPosStringsRace( gameData );
        else
            initPosStringsNonRace( gameData );
        
        if ( ( positionStrings == null ) || ( numVehicles > positionStrings.length ) )
            initPositionStrings( gameData );
        
        for ( int i = 0; i < colWidths.length; i++ )
            colWidths[i] = 0;
        
        int minWidth = 0;
        for ( int i = 0; i < numVehicles; i++ )
        {
            int w = positionStrings[i].getMaxColWidths( currPosStrings[i], 10, texCanvas.getImage(), colWidths );
            if ( w > minWidth )
                minWidth = w;
        }
        
        if ( !useAutoWidth.getBooleanValue() )
            return ( false );
        
        //int padding = 2 * 8;
        int padding = 0;
        minWidth += padding;
        
        if ( ( isEditorMode && ( Math.abs( ( width + padding ) - minWidth ) > 1 ) ) || ( width + padding != minWidth ) )
        {
            clearRegion( isEditorMode, texCanvas.getImage() );
            getSize().setEffectiveSize( getBorder().getWidgetWidth( minWidth ), getBorder().getWidgetHeight( height ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    private void initPositionStrings( LiveGameData gameData )
    {
        int n = gameData.getScoringInfo().getNumVehicles();
        
        positionStrings = new DrawnString[ n ];
        
        for ( int i = 0; i < n; i++ )
        {
            if ( i == 0 )
                positionStrings[i] = new DrawnString( 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
            else
                positionStrings[i] = new DrawnString( null, positionStrings[i - 1], 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        initPositionStrings( gameData );
        
        int h = height + getBorder().getInnerBottomHeight() - getBorder().getOpaqueBottomHeight();
        int rowHeight = positionStrings[0].getMaxHeight( texCanvas.getImage(), false );
        maxDisplayedDrivers = Math.max( 1, h / rowHeight );
        
        currPosStrings = null;
        playerPosStringIndex = -1;
        vsiIndices = null;
        numVehicles = -1;
        oldPosStirngs = null;
    }
    
    private void drawPosition( int i, VehicleScoringInfo vsi, boolean needsCompleteRedraw, TextureImage2D image, int offsetX, int offsetY, int width )
    {
        //image.getTextureCanvas().pushClip( offsetX + positionStrings[i].getAbsX(), clipRect.getTop(), width - positionStrings[i].getAbsX() - 13, clipRect.getHeight() );
        
        String[] ss = currPosStrings[i];
        
        if ( needsCompleteRedraw || !Arrays.equals( ss, oldPosStirngs[i] ) )
        {
            java.awt.Color fc = null;
            
            switch ( vsi.getFinishStatus() )
            {
                case NONE:
                    //if ( playerPosStringIndex == i - i0 )
                    if ( vsi.isPlayer() )
                        fc = fontColor_me.getColor();
                    else
                        fc = null;
                    break;
                case DNF:
                case DQ:
                    fc = fontColor_out.getColor();
                    break;
                case FINISHED:
                    fc = fontColor_finished.getColor();
                    break;
            }
            
            positionStrings[i].drawColumns( offsetX, offsetY, ss, aligns, 10, colWidths, getBackgroundColor(), fc, image );
            
            oldPosStirngs[i] = ss;
        }
        
        //image.getTextureCanvas().popClip();
    }
    
    @Override
    protected void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        lastKnownSessionType = scoringInfo.getSessionType();
        
        if ( currPosStrings == null )
        {
            if ( gameData.getScoringInfo().getSessionType() == SessionType.RACE )
                initPosStringsRace( gameData );
            else
                initPosStringsNonRace( gameData );
        }
        
        if ( oldNumVehicles != numVehicles )
        {
            oldNumVehicles = numVehicles;
            needsCompleteRedraw = true;
        }
        
        TextureImage2D image = texCanvas.getImage();
        
        oldPosStirngs = ensureCapacity( oldPosStirngs, numVehicles, true );
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            drawPosition( i, scoringInfo.getVehicleScoringInfo( vsiIndices[i] ), needsCompleteRedraw, image, offsetX, offsetY, width );
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
        writer.writeProperty( "initialView", getView(), "the initial kind of standings view. Valid values: RELATIVE_TO_LEADER, RELATIVE_TO_ME." );
        writer.writeProperty( forceLeaderDisplayed, "Display leader regardless of maximum displayed drivers setting?" );
        writer.writeProperty( nameDisplayType, "How to display driver names." );
        writer.writeProperty( abbreviate, "Whether to abbreviate \"Stops\", or not." );
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
        else if ( key.equals( "initialView" ) )
        {
            try
            {
                ( (LocalStore)getLocalStore() ).view = StandingsView.valueOf( value );
            }
            catch ( Throwable t )
            {
                // Ignore and keep default!
            }
        }
        else if ( forceLeaderDisplayed.loadProperty( key, value ) );
        else if ( nameDisplayType.loadProperty( key, value ) );
        else if ( abbreviate.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( FlaggedList propsList )
    {
        super.getProperties( propsList );
        
        FlaggedList props = new FlaggedList( "Specific", true );
        
        props.add( fontColor_me );
        props.add( fontColor_out );
        props.add( fontColor_finished );
        props.add( useAutoWidth );
        props.add( new Property( "initialView", PropertyEditorType.ENUM )
        {
            @Override
            public void setValue( Object value )
            {
                if ( ( (LocalStore)getLocalStore() ).view == value )
                    return;
                
                setView( (StandingsView)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( getView() );
            }
        } );
        
        props.add( forceLeaderDisplayed );
        props.add( nameDisplayType );
        props.add( abbreviate );
        
        propsList.add(  props );
    }
    
    public StandingsWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.2825f, Size.PERCENT_OFFSET + 0.14916667f );
        
        getFontProperty().setFont( "BiggerFont" );
    }
}
