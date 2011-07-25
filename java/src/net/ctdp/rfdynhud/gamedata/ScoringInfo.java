/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits.Convert;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.util.AbstractThreeLetterCodeGenerator;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.ThreeLetterCodeGenerator;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.util.TimingUtil;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ScoringInfo
{
    private final LiveGameData gameData;
    private final _LiveGameDataObjectsFactory gdFactory;
    
    final _ScoringInfoCapsule data;
    
    private boolean updatedInTimeScope = false;
    private long updateId = 0L;
    private long updateTimestamp = -1L;
    private int sessionId = 0;
    private boolean sessionRunning = false;
    private int sessionJustStarted = 0;
    
    private long sessionStartTimestamp = -1L;
    private long realtimeEnteredTimestamp = -1L;
    private int realtimeEnteredId = 0;
    
    private boolean gamePausedCache = false;
    private long lastUpdateTimestamp = -1L;
    private long sessionBaseNanos = -1L;
    private long extrapolationNanos = 0L;
    private float extrapolationTime = 0.0f;
    private long sessionNanos = -1L;
    private float sessionTime = 0.0f;
    
    private float trackLength = -1f;
    
    private double raceLengthPercentage = 1.0;
    
    private boolean classScoringCalculated = false;
    
    public static interface ScoringInfoUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode );
        
        public void onPlayerJoined( LiveGameData gameData, VehicleScoringInfo joinedVSI, boolean rejoined );
        
        public void onPlayerLeft( LiveGameData gameData, Integer vsiID );
    }
    
    private ScoringInfoUpdateListener[] updateListeners = null;
    
    public void registerListener( ScoringInfoUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new ScoringInfoUpdateListener[] { l };
        }
        else
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] == l )
                    return;
            }
            
            ScoringInfoUpdateListener[] tmp = new ScoringInfoUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
        
        gameData.registerDataUpdateListener( l );
    }
    
    public void unregisterListener( ScoringInfoUpdateListener l )
    {
        if ( updateListeners == null )
            return;
        
        int index = -1;
        for ( int i = 0; i < updateListeners.length; i++ )
        {
            if ( updateListeners[i] == l )
            {
                index = i;
                break;
            }
        }
        
        if ( index < 0 )
            return;
        
        if ( updateListeners.length == 1 )
        {
            updateListeners = null;
            return;
        }
        
        ScoringInfoUpdateListener[] tmp = new ScoringInfoUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
        
        gameData.unregisterDataUpdateListener( l );
    }
    
    private final GameEventsManager eventsManager;
    
    private VehicleScoringInfo[] vehicleScoringInfo = null;
    private VehicleScoringInfo[] vehicleScoringInfo2 = null;
    _VehicleScoringInfoCapsule[] vehicleScoringInfoCapsules = null;
    private int numVehicles = -1;
    private int oldNumVehicles = -1;
    private boolean fixedViewedVSI = false;
    private VehicleScoringInfo playerVSI = null;
    private VehicleScoringInfo viewedVSI = null;
    private VehicleScoringInfo controlledViewedVSI = null;
    private VehicleScoringInfo fastestLapVSI = null;
    private VehicleScoringInfo secondFastestLapVSI = null;
    private VehicleScoringInfo fastestSector1VSI = null;
    private VehicleScoringInfo fastestSector2VSI = null;
    private VehicleScoringInfo fastestSector3VSI = null;
    private VehicleScoringInfo controlledCompareVSI = null;
    
    private String playerName = null;
    private String playerFilename = null;
    private String trackName = null;
    
    private ThreeLetterCodeGenerator tlcGenerator;
    
    private final HashMap<Integer, _VehicleScoringInfoCapsule> idCapsuleMap = new HashMap<Integer, _VehicleScoringInfoCapsule>();
    private final HashMap<Integer, VehicleScoringInfo> leftVSICache = new HashMap<Integer, VehicleScoringInfo>();
    private final HashMap<Integer, Integer> nameDuplicatesMap = new HashMap<Integer, Integer>();
    
    private final ArrayList<Object[]> changedVSIs = new ArrayList<Object[]>();
    
    void initVehicleScoringInfo()
    {
        if ( sessionJustStarted == 1 )
            this.sessionJustStarted = 2;
        
        oldNumVehicles = numVehicles;
        numVehicles = -1;
        numVehicles = getNumVehicles();
        
        if ( ( vehicleScoringInfoCapsules == null ) || ( vehicleScoringInfoCapsules.length < numVehicles ) )
        {
            _VehicleScoringInfoCapsule[] tmp = new _VehicleScoringInfoCapsule[ numVehicles ];
            
            int oldCount;
            if ( vehicleScoringInfoCapsules == null )
            {
                oldCount = 0;
            }
            else
            {
                oldCount = vehicleScoringInfoCapsules.length;
                System.arraycopy( vehicleScoringInfoCapsules, 0, tmp, 0, oldCount );
            }
            
            for ( int i = oldCount; i < numVehicles; i++ )
            {
                tmp[i] = gdFactory.newVehicleScoringInfoCapsule( gameData );
            }
            
            vehicleScoringInfoCapsules = tmp;
        }
        
        if ( ( vehicleScoringInfo == null ) || ( vehicleScoringInfo.length < numVehicles ) )
        {
            VehicleScoringInfo[] tmp = new VehicleScoringInfo[ numVehicles ];
            
            int oldCount;
            if ( vehicleScoringInfo == null )
            {
                oldCount = 0;
            }
            else
            {
                oldCount = vehicleScoringInfo.length;
                System.arraycopy( vehicleScoringInfo, 0, tmp, 0, oldCount );
            }
            
            for ( int i = oldCount; i < numVehicles; i++ )
            {
                tmp[i] = new VehicleScoringInfo( this, gameData.getProfileInfo(), gameData );
            }
            
            vehicleScoringInfo = tmp;
        }
        
        if ( ( vehicleScoringInfo2 == null ) || ( vehicleScoringInfo2.length != numVehicles ) )
        {
            vehicleScoringInfo2 = new VehicleScoringInfo[ numVehicles ];
        }
    }
    
    void assignVSICapsules()
    {
        try
        {
            //Logger.log( "################ numVehicles = " + getNumVehicles() + ", oldNumVehicles = " + oldNumVehicles );
            
            idCapsuleMap.clear();
            nameDuplicatesMap.clear();
            
            int n = getNumVehicles();
            for ( int i = 0; i < n; i++ )
            {
                //idCapsuleMap.put( vehicleScoringInfoCapsules[i].refreshID(), vehicleScoringInfoCapsules[i] );
                
                Integer id = vehicleScoringInfoCapsules[i].refreshID( true );
                
                if ( idCapsuleMap.containsKey( id ) )
                {
                    Integer pf = nameDuplicatesMap.get( id );
                    if ( pf == null )
                        pf = 2;
                    vehicleScoringInfoCapsules[i].postfixDriverName( String.valueOf( pf ), -1 );
                    //Logger.log( vehicleScoringInfoCapsules[i].getDriverName() );
                    nameDuplicatesMap.put( id, pf.intValue() + 1 );
                    id = vehicleScoringInfoCapsules[i].refreshID( false );
                }
                
                //Logger.log( "Found data for " + vehicleScoringInfoCapsules[i].getDriverName() + ", assigned id " + id + "." );
                idCapsuleMap.put( id, vehicleScoringInfoCapsules[i] );
            }
            
            int firstFree = 0;
            
            if ( oldNumVehicles > 0 )
            {
                int j = oldNumVehicles - 1;
                int n2 = Math.max( n, oldNumVehicles );
                for ( int i = 0; i < n2; i++ )
                {
                    VehicleScoringInfo vsi = vehicleScoringInfo[i];
                    if ( vsi.getDriverId() > 0 )
                    {
                        vsi.data = idCapsuleMap.remove( vsi.getDriverID() );
                        
                        if ( vsi.data == null )
                        {
                            // player left
                            
                            changedVSIs.add( new Object[] { -1, vsi.getDriverID() } );
                            
                            RFDHLog.debug( "[DEBUG]: ", vsi.getDriverName(), " left the game." );
                            
                            leftVSICache.put( vsi.getDriverID(), vsi );
                            if ( j > 0 ) // Shouldn't be possible, but...
                            {
                                vehicleScoringInfo[i] = vehicleScoringInfo[j];
                                vehicleScoringInfo[j] = new VehicleScoringInfo( this, gameData.getProfileInfo(), gameData );
                                i--;
                                j--;
                            }
                        }
                        else
                        {
                            //Logger.log( "Assigned data for " + vsi.data.getDriverName() + " to index " + i + "." );
                        }
                    }
                }
                
                firstFree = j + 1;
            }
            
            if ( idCapsuleMap.size() > 0 )
            {
                for ( Integer joinedID : idCapsuleMap.keySet() )
                {
                    // Player joined
                    
                    _VehicleScoringInfoCapsule data = idCapsuleMap.get( joinedID );
                    
                    VehicleScoringInfo vsi = leftVSICache.remove( joinedID );
                    
                    if ( vsi == null )
                    {
                        vsi = vehicleScoringInfo[firstFree++];
                        vsi.data = data;
                        vsi.setDriverName( data.getOriginalName(), data.getDriverName(), joinedID );
                        
                        RFDHLog.debug( "[DEBUG]: Player joined: ", vsi.getDriverName(), ", id = ", joinedID, ", index = ", ( firstFree - 1 ), ", fastest lap: " + TimingUtil.getTimeAsLaptimeString( vsi.getBestLapTime() ) );
                        
                        //if ( vsi.getBestLapTime() > 0f )
                        {
                            Laptime lt = new Laptime( vsi.getDriverId(), 0, -1f, -1f, -1f, false, false, true );
                            lt.laptime = vsi.getBestLapTime();
                            vsi.setFastestLaptime( lt );
                        }
                        
                        changedVSIs.add( new Object[] { +1, vsi } );
                    }
                    else
                    {
                        vehicleScoringInfo[firstFree++] = vsi;
                        vsi.data = data;
                        
                        RFDHLog.debug( "[DEBUG]: Player rejoined: ", vsi.getDriverName(), ", id = ", joinedID, ", index = ", ( firstFree - 1 ), ", fastest lap: " + TimingUtil.getTimeAsLaptimeString( vsi.getBestLapTime() ) );
                        
                        if ( ( vsi._getFastestLaptime() == null ) && ( vsi.getBestLapTime() > 0f ) )
                        {
                            Laptime lt = new Laptime( vsi.getDriverId(), 0, -1f, -1f, -1f, false, false, true );
                            lt.laptime = vsi.getBestLapTime();
                            vsi.setFastestLaptime( lt );
                        }
                        else if ( vsi.getBestLapTime() < 0f )
                        {
                            // Player seems to have changed his unique ID.
                            
                            Laptime lt = new Laptime( vsi.getDriverId(), 0, -1f, -1f, -1f, false, false, true );
                            lt.laptime = vsi.getBestLapTime();
                            vsi.setFastestLaptime( lt );
                            
                            for ( int i = 0; i < vsi.laptimes.size(); i++ )
                            {
                                lt = vsi.laptimes.get( i );
                                lt.sector1 = -1f;
                                lt.sector2 = -1f;
                                lt.sector3 = -1f;
                                lt.laptime = -1f;
                            }
                        }
                        
                        changedVSIs.add( new Object[] { +2, vsi } );
                    }
                }
            }
            
            idCapsuleMap.clear();
            
            // Check for errors and do a workaround...
            int somethingWrong = 0;
            for ( int i = 0; i < n; i++ )
            {
                if ( vehicleScoringInfo[i] == null )
                {
                    somethingWrong++;
                    vehicleScoringInfo[i] = new VehicleScoringInfo( this, gameData.getProfileInfo(), gameData );
                    vehicleScoringInfo[i].data = gdFactory.newVehicleScoringInfoCapsule( gameData );
                }
                else if ( vehicleScoringInfo[i].data == null )
                {
                    somethingWrong++;
                    vehicleScoringInfo[i].data = gdFactory.newVehicleScoringInfoCapsule( gameData );
                }
            }
            
            if ( somethingWrong > 0 )
            {
                RFDHLog.printlnEx( "WARNING: Something went wrong when initializing VehicleScoringInfos. Had to add " + somethingWrong + " instances. numVehicles = " + n + ", old = " + oldNumVehicles );
            }
            
            System.arraycopy( vehicleScoringInfo, 0, vehicleScoringInfo2, 0, n );
            
            //for ( int i = n; i < vehicleScoringInfo.length; i++ )
            for ( int i = n; i < oldNumVehicles; i++ )
                vehicleScoringInfo[i].data = null;
            
            /*
            int nn = 0;
            Logger.log( "VSIs without data: " );
            for ( int i = 0; i < n; i++ )
            {
                if ( vehicleScoringInfo[i].data == null )
                {
                    Logger.log( i + ": " + vehicleScoringInfo[i].getDriverName() );
                    nn++;
                }
            }
            Logger.log( nn );
            */
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        if ( ( changedVSIs.size() > 0 ) && ( updateListeners != null ) )
        {
            for ( int j = 0; j < changedVSIs.size(); j++ )
            {
                Object[] change = changedVSIs.get( j );
                
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        int changeId = ( (Integer)change[0] ).intValue();
                        
                        if ( changeId == -1 )
                            updateListeners[i].onPlayerLeft( gameData, (Integer)change[1] );
                        else if ( changeId == +1 )
                            updateListeners[i].onPlayerJoined( gameData, (VehicleScoringInfo)change[1], false );
                        else if ( changeId == +2 )
                            updateListeners[i].onPlayerJoined( gameData, (VehicleScoringInfo)change[1], true );
                    }
                    catch ( Throwable t )
                    {
                        RFDHLog.exception( t );
                    }
                }
            }
        }
        
        changedVSIs.clear();
    }
    
    private void resetDerivateData()
    {
        playerName = null;
        playerFilename = null;
        trackName = null;
        playerVSI = null;
        viewedVSI = null;
        controlledViewedVSI = null;
        
        fastestLapVSI = null;
        secondFastestLapVSI = null;
        fastestSector1VSI = null;
        fastestSector2VSI = null;
        fastestSector3VSI = null;
        controlledCompareVSI = null;
        
        trackLength = -1f;
        
        classScoringCalculated = false;
        
        if ( vehicleScoringInfo != null )
        {
            for ( int i = 0; i < vehicleScoringInfo.length; i++ )
            {
                if ( vehicleScoringInfo[i] != null )
                {
                    vehicleScoringInfo[i].classLeaderVSI = null;
                    vehicleScoringInfo[i].classNextInFrontVSI = null;
                    vehicleScoringInfo[i].classNextBehindVSI = null;
                }
            }
        }
    }
    
    void prepareDataUpdate()
    {
        lastUpdateTimestamp = System.nanoTime();
    }
    
    /**
     * Sets the generator to use to generate three-letter-codes and short forms from driver names.
     * 
     * @param tlcGenerator
     */
    public void setThreeLetterCodeGenerator( ThreeLetterCodeGenerator tlcGenerator )
    {
        if ( tlcGenerator == null )
            throw new IllegalArgumentException( "tlcGenerator must not be null." );
        
        this.tlcGenerator = tlcGenerator;
        ThreeLetterCodeManager.resetMaps();
    }
    
    /**
     * Gets the generator to use to generate three-letter-codes and short forms from driver names.
     * 
     * @return the generator to use to generate three-letter-codes and short forms from driver names.
     */
    public final ThreeLetterCodeGenerator getThreeLetterCodeGenerator()
    {
        return ( tlcGenerator );
    }
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
        
        for ( int i = 0; i < getNumVehicles(); i++ )
            getVehicleScoringInfo( i ).applyEditorPresets( editorPresets );
        
        FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.applyEditorPresets( gameData, editorPresets );
    }
    
    private void updateRaceLengthPercentage( boolean isEditorMode )
    {
        if ( getSessionType().isRace() )
        {
            double trackRaceLaps = gameData.getTrackInfo().getRaceLaps();
            if ( isEditorMode )
                trackRaceLaps = 70;
            else if ( trackRaceLaps < 0.0 ) // corrupt GDB file?
            {
                RFDHLog.exception( "WARNING: \"RaceLaps\" not found in GDB. Using rFactor default value 50." );
                trackRaceLaps = 50; // rFactor standard value, if this is missing.
            }
            
            VehicleScoringInfo leader = getLeadersVehicleScoringInfo();
            SessionLimit sessionLimit = leader.getSessionLimit();
            
            if ( sessionLimit == SessionLimit.TIME )
            {
                if ( gameData.getModInfo().getRaceDuration() < 0f )
                {
                    // fall back to lap limited to at least have something.
                    
                    RFDHLog.exception( "WARNING: No \"RaceTime\" found in RFM." );
                    sessionLimit = SessionLimit.LAPS;
                }
            }
            
            if ( sessionLimit == SessionLimit.TIME )
            {
                double modRaceSeconds = gameData.getModInfo().getRaceDuration();
                double raceSeconds = getEndTime();
                
                if ( raceSeconds > 0.0 )
                {
                    // Time limit is always in minutes without fractions. rFactor adds some seconds to the end time.
                    raceSeconds = Math.floor( raceSeconds / 60.0 ) * 60.0;
                    
                    //raceLengthPercentage = ( raceSeconds + 150.0 ) / modRaceSeconds;
                    raceLengthPercentage = ( raceSeconds + 150.0 * ( ( modRaceSeconds - raceSeconds ) / modRaceSeconds ) ) / modRaceSeconds;
                }
                else
                {
                    raceLengthPercentage = 1.0;
                }
            }
            else
            {
                double raceLaps = getEstimatedMaxLaps( leader );
                
                //raceLengthPercentage = ( raceLaps + 1 ) / trackRaceLaps;
                raceLengthPercentage = ( raceLaps + ( ( trackRaceLaps - raceLaps ) / trackRaceLaps ) ) / trackRaceLaps;
            }
        }
        else
        {
            raceLengthPercentage = 1.0;
        }
    }
    
    private GamePhase lastGamePhase = null;
    
    void onDataUpdated( EditorPresets editorPresets )
    {
        try
        {
            this.updatedInTimeScope = true;
            this.updateId++;
            this.updateTimestamp = System.nanoTime();
            this.gamePausedCache = gameData.isGamePaused();
            
            this.sessionBaseNanos = Math.round( data.getSessionTime() * 1000000000.0 );
            updateSessionTime( updateTimestamp );
            
            Arrays.sort( vehicleScoringInfo2, VehicleScoringInfo.VSIPlaceComparator.INSTANCE );
            
            resetDerivateData();
            
            int n = getNumVehicles();
            for ( int i = 0; i < n; i++ )
            {
                getVehicleScoringInfo( i ).updateSomeData();
            }
            
            applyEditorPresets( editorPresets );
            
            boolean rlpUpdated = false;
            
            if ( sessionJustStarted > 0 )
            {
                updateRaceLengthPercentage( editorPresets != null );
                rlpUpdated = true;
                
                for ( int i = 0; i < n; i++ )
                {
                    getVehicleScoringInfo( i ).onSessionStarted();
                }
                
                this.sessionJustStarted = 0;
                lastGamePhase = null;
            }
            
            if ( ( getNumVehicles() > 0 ) && getLeadersVehicleScoringInfo().isLapJustStarted() && !rlpUpdated )
            {
                updateRaceLengthPercentage( editorPresets != null );
                rlpUpdated = true;
            }
            
            GamePhase gamePhase = getGamePhase();
            if ( ( gamePhase != lastGamePhase ) && !rlpUpdated )
            {
                updateRaceLengthPercentage( editorPresets != null );
                rlpUpdated = true;
            }
            lastGamePhase = gamePhase;
            
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onScoringInfoUpdated( gameData, editorPresets != null );
                    }
                    catch ( Throwable t )
                    {
                        RFDHLog.exception( t );
                    }
                }
            }
            
            if ( eventsManager != null )
            {
                eventsManager.checkRaceRestart( updateTimestamp );
                eventsManager.checkAndFireOnLapStarted( editorPresets != null );
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    /**
     * 
     * @param isEditorMode
     */
    final void onSessionStarted( boolean isEditorMode )
    {
        this.sessionId++;
        this.sessionStartTimestamp = System.nanoTime();
        this.sessionBaseNanos = 0L;
        this.sessionRunning = true;
        this.sessionJustStarted = 1;
        this.updatedInTimeScope = false;
        
        this.raceLengthPercentage = 1.0; // We don't know it better now!
    }
    
    final void onSessionEnded()
    {
        this.sessionRunning = false;
        this.updatedInTimeScope = false;
        
        if ( vehicleScoringInfo != null )
        {
            for ( int i = 0; i < vehicleScoringInfo.length; i++ )
            {
                vehicleScoringInfo[i].onSessionEnded();
            }
        }
    }
    
    /**
     * Gets whether a session is currently running or not.
     * 
     * @return whether a session is currently running or not.
     */
    public final boolean isSessionRunning()
    {
        return ( sessionRunning );
    }
    
    /**
     * Gets the system timestamp in nanoseconds, at which the current session was started.
     * 
     * @return the system timestamp in nanoseconds, at which the current session was started.
     */
    public final long getSessionStartTimestamp()
    {
        return ( sessionStartTimestamp );
    }
    
    final void onRealtimeEntered()
    {
        this.realtimeEnteredTimestamp = System.nanoTime();
        this.realtimeEnteredId++;
        this.updatedInTimeScope = true;
    }
    
    final void onRealtimeExited()
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * Gets the system timestamp in nanoseconds, at which the player entered realtime mode.
     * 
     * @return the system timestamp in nanoseconds, at which the player entered realtime mode.
     */
    public final long getRealtimeEnteredTimestamp()
    {
        return ( realtimeEnteredTimestamp );
    }
    
    /**
     * This ID is incremented each time, the player enters realtime mode.
     * 
     * @return the ID of realtime enter actions.
     */
    public final int getRealtimeEntredId()
    {
        return ( realtimeEnteredId );
    }
    
    /**
     * Gets, whether the last update of these data has been done while in running session resp. realtime mode.
     * @return whether the last update of these data has been done while in running session resp. realtime mode.
     */
    public final boolean isUpdatedInTimeScope()
    {
        return ( updatedInTimeScope );
    }
    
    /**
     * Gets whether this data has been updated in the current session.
     * 
     * @return whether this data has been updated in the current session.
     */
    public final boolean isValid()
    {
        return ( sessionJustStarted == 0 );
    }
    
    /**
     * Gets an ID, that in incremented every time, this {@link ScoringInfo} object is filled with new data from the game.
     * 
     * @return an ID, that in incremented every time, this {@link ScoringInfo} object is filled with new data from the game.
     */
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    /**
     * This Session ID is incremented every time, a new session is started.
     * 
     * @return a session ID unique for each started session.
     */
    public final int getSessionId()
    {
        return ( sessionId );
    }
    
    void loadFromStream( InputStream in, EditorPresets editorPresets ) throws IOException
    {
        prepareDataUpdate();
        
        data.loadFromStream( in );
        
        initVehicleScoringInfo();
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            vehicleScoringInfoCapsules[i].loadFromStream( in );
        }
        
        assignVSICapsules();
        
        applyEditorPresets( editorPresets );
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            vehicleScoringInfo2[i].onDataUpdated();
        }
        
        onDataUpdated( editorPresets );
        
        // Add postfixes to some vehicles' classes to get valid class-scoring in the editor.
        String classA = "F1 2006";
        String classB = "F1 2006B";
        vehicleScoringInfo2[0].setVehClass( classA );
        vehicleScoringInfo2[1].setVehClass( classB );
        vehicleScoringInfo2[2].setVehClass( classA );
        vehicleScoringInfo2[3].setVehClass( classB );
        vehicleScoringInfo2[4].setVehClass( classA );
        vehicleScoringInfo2[5].setVehClass( classA );
        vehicleScoringInfo2[6].setVehClass( classB );
        vehicleScoringInfo2[7].setVehClass( classB );
        vehicleScoringInfo2[8].setVehClass( classA );
        vehicleScoringInfo2[9].setVehClass( classA );
        vehicleScoringInfo2[10].setVehClass( classA );
        vehicleScoringInfo2[11].setVehClass( classA );
        vehicleScoringInfo2[12].setVehClass( classA );
        vehicleScoringInfo2[13].setVehClass( classB );
        vehicleScoringInfo2[14].setVehClass( classA );
        vehicleScoringInfo2[15].setVehClass( classA );
        vehicleScoringInfo2[16].setVehClass( classA );
        vehicleScoringInfo2[17].setVehClass( classA );
        vehicleScoringInfo2[18].setVehClass( classB );
        vehicleScoringInfo2[19].setVehClass( classA );
        vehicleScoringInfo2[20].setVehClass( classA );
        vehicleScoringInfo2[21].setVehClass( classA );
    }
    
    public void readFromStream( InputStream in ) throws IOException
    {
        prepareDataUpdate();
        
        data.loadFromStream( in );
        
        initVehicleScoringInfo();
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            vehicleScoringInfoCapsules[i].loadFromStream( in );
        }
        
        assignVSICapsules();
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            vehicleScoringInfo2[i].onDataUpdated();
        }
        
        onDataUpdated( null );
    }
    
    public void writeToStream( OutputStream out ) throws IOException
    {
        data.writeToStream( out );
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            vehicleScoringInfoCapsules[i].writeToStream( out );
        }
    }
    
    /**
     * Gets a multiplier in range [0, 1] for the race distance.
     * 
     * @return a multiplier in range [0, 1] for the race distance.
     */
    public final double getRaceLengthPercentage()
    {
        return ( raceLengthPercentage );
    }
    
    private final HashSet<Integer> handledClassIDs = new HashSet<Integer>();
    
    final void updateClassScoring()
    {
        if ( classScoringCalculated )
            return;
        
        handledClassIDs.clear();
        
        final int n = getNumVehicles();
        
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi0 = getVehicleScoringInfo( i );
            
            if ( handledClassIDs.add( vsi0.getVehicleClassID() ) )
            {
                short p = 1;
                int numVehiclesInClass = 1;
                float tbn = 0f;
                int lbn = 0;
                float tbl = 0f;
                int lbl = 0;
                
                vsi0.placeByClass = p++;
                vsi0.timeBehindNextByClass = tbn;
                vsi0.lapsBehindNextByClass = lbn;
                vsi0.timeBehindLeaderByClass = tbl;
                vsi0.lapsBehindLeaderByClass = lbl;
                vsi0.classLeaderVSI = vsi0;
                vsi0.classNextInFrontVSI = null;
                
                for ( int j = vsi0.getPlace( false ) - 0; j < n; j++ )
                {
                    VehicleScoringInfo vsi1 = getVehicleScoringInfo( j );
                    
                    tbn += vsi1.getTimeBehindNextInFront( false );
                    lbn += vsi1.getLapsBehindNextInFront( false );
                    tbl += vsi1.getTimeBehindNextInFront( false );
                    lbl += vsi1.getLapsBehindNextInFront( false );
                    
                    if ( vsi1.getVehicleClassId() == vsi0.getVehicleClassId() )
                    {
                        vsi1.placeByClass = p++;
                        vsi1.timeBehindNextByClass = tbn;
                        vsi1.lapsBehindNextByClass = lbn;
                        vsi1.timeBehindLeaderByClass = tbl;
                        vsi1.lapsBehindLeaderByClass = lbl;
                        vsi1.classLeaderVSI = vsi0.classLeaderVSI;
                        vsi1.classNextInFrontVSI = vsi0;
                        vsi0.classNextBehindVSI = vsi1;
                        
                        tbn = 0f;
                        lbn = 0;
                        vsi0 = vsi1;
                        
                        numVehiclesInClass++;
                    }
                }
                
                vsi0.classNextBehindVSI = null;
                
                for ( int j = vsi0.getPlace( false ) - 1; j >= 0; j-- )
                {
                    VehicleScoringInfo vsi1 = getVehicleScoringInfo( j );
                    
                    if ( vsi1.getVehicleClassId() == vsi0.getVehicleClassId() )
                        vsi1.numVehiclesInClass = numVehiclesInClass;
                }
            }
        }
        
        classScoringCalculated = true;
    }
    
    /**
     * Gets the current track name.
     * 
     * @return the current track name.
     */
    public final String getTrackName()
    {
        if ( trackName == null )
        {
            trackName = data.getTrackName();
        }
        
        return ( trackName );
    }
    
    /**
     * Gets current session type.
     * 
     * @return current session type.
     */
    public final SessionType getSessionType()
    {
        return ( data.getSessionType() );
    }
    
    final void updateSessionTime( long timestamp )
    {
        extrapolationNanos = timestamp - lastUpdateTimestamp;
        
        gamePausedCache = gamePausedCache || gameData.isGamePaused();
        
        if ( gamePausedCache )
        {
            extrapolationNanos = 0L;
        }
        
        extrapolationTime = extrapolationNanos / 1000000000.0f;
        
        sessionNanos = sessionBaseNanos + extrapolationNanos;
        sessionTime = sessionNanos / 1000000000.0f;
        
        int n = getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            getVehicleScoringInfo( i ).resetExtrapolatedValues();
        }
    }
    
    /**
     * Gets the nano seconds, the current session is running.
     * 
     * @return the nano seconds, the current session is running.
     */
    public final long getSessionNanos()
    {
        return ( sessionNanos );
    }
    
    /**
     * Returns the nano seconds since the last ScoringInfo update.
     * 
     * @return the nano seconds since the last ScoringInfo update.
     */
    public final long getExtrapolationNanos()
    {
        return ( extrapolationNanos );
    }
    
    /**
     * Returns the seconds since the last ScoringInfo update.
     * 
     * @return the seconds since the last ScoringInfo update.
     */
    public final float getExtrapolationTime()
    {
        return ( extrapolationTime );
    }
    
    /**
     * Gets current session time.
     * 
     * @return current session time.
     */
    public final float getSessionTime()
    {
        if ( getGamePhase() == GamePhase.SESSION_OVER )
            return ( 0f );
        
        return ( sessionTime );
    }
    
    /**
     * Gets session ending time.
     * 
     * @return session ending time.
     */
    public final float getEndTime()
    {
        return ( data.getEndTime() );
    }
    
    /**
     * Gets maximum laps.
     * 
     * @return maximum laps.
     */
    public final int getMaxLaps()
    {
        return ( data.getMaxLaps() );
    }
    
    /**
     * Gets the estimated max laps based on the session end time and average lap time.
     * If the {@link SessionLimit} is defined to be LAPS, then max laps is known and returned.
     * If the current session is a race, the estimated max laps of the leader are returned.
     * 
     * @param vsi the vehicle (should be the leader)
     * 
     * @return the estimated max laps.
     */
    public final int getEstimatedMaxLaps( VehicleScoringInfo vsi )
    {
        if ( getSessionType().isRace() )
            return ( getLeadersVehicleScoringInfo().getEstimatedMaxLaps() );
        
        return ( vsi.getEstimatedMaxLaps() );
    }
    
    /**
     * Gets the distance around track.
     * 
     * @return the distance around track.
     */
    public final float getTrackLength()
    {
        if ( trackLength < 0f )
        {
            trackLength = data.getTrackLength();
        }
        
        return ( trackLength );
    }
    
    /**
     * Gets the current number of vehicles.
     * 
     * @return the current number of vehicles.
     */
    public final int getNumVehicles()
    {
        if ( sessionJustStarted == 1 )
            return ( 0 );
        
        if ( numVehicles == -1 )
            numVehicles = data.getNumVehicles();
        
        return ( numVehicles );
    }
    
    /**
     * Gets the number of vehicles in the same vehicle class as the given one. This method counts on every call.
     * 
     * @param vsi the vehicle
     * 
     * @return the number of vehicles in the same vehicle class as the given one.
     */
    public final int getNumVehiclesInSameClass( VehicleScoringInfo vsi )
    {
        int n = 0;
        
        for ( int i = 0; i < getNumVehicles(); i++ )
        {
            if ( getVehicleScoringInfo( i ).getVehicleClassId() == vsi.getVehicleClassId() )
                n++;
        }
        
        return ( n );
    }
    
    /**
     * Gets the current game phase.
     * 
     * @return the current game phase.
     */
    public final GamePhase getGamePhase()
    {
        return ( data.getGamePhase() );
    }
    
    /**
     * Gets the current yellow flag state (applies to full-course only).
     * 
     * @return the current yellow flag state.
     */
    public final YellowFlagState getYellowFlagState()
    {
        return ( data.getYellowFlagState() );
    }
    
    /**
     * Gets whether there are any local yellows at the moment in the sector.
     * 
     * @param sector the queried sector (1,2,3)
     * 
     * @return whether there are any local yellows at the moment in the sector
     */
    public final boolean getSectorYellowFlag( int sector )
    {
        return ( data.getSectorYellowFlag( sector ) );
    }
    
    /**
     * Gets the current start light frame (number depends on track).
     * 
     * @return the current start light frame.
     */
    public final int getStartLightFrame()
    {
        return ( data.getStartLightFrame() );
    }
    
    /**
     * Gets the number of red lights in start sequence.
     * 
     * @return the number of red lights in start sequence.
     */
    public final int getNumRedLights()
    {
        return ( data.getNumRedLights() );
    }
    
    /**
     * Gets whether we're in realtime as opposed to at the monitor.
     * 
     * @return whether we're in realtime as opposed to at the monitor.
     */
    public final boolean isInRealtimeMode()
    {
        return ( data.isInRealtimeMode() );
    }
    
    /**
     * Gets the player name (including possible multiplayer override).
     * 
     * @return the player name.
     */
    public final String getPlayerName()
    {
        if ( playerName == null )
        {
            playerName = data.getPlayerName();
        }
        
        return ( playerName );
    }
    
    /**
     * Gets the player's filename (PLR) (may be encoded to be a legal filename).
     * 
     * @return the player's filename.
     */
    public final String getPlayerFilename()
    {
        if ( playerFilename == null )
        {
            playerFilename = data.getPlayerFilename();
        }
        
        return ( playerFilename );
    }
    
    /**
     * Gets cloud darkness? 0.0-1.0
     * 
     * @return cloud darkness? 0.0-1.0
     */
    public final float getCloudDarkness()
    {
        return ( data.getCloudDarkness() );
    }
    
    /**
     * Gets raining severity 0.0-1.0
     * 
     * @return raining severity 0.0-1.0
     */
    public final float getRainingSeverity()
    {
        return ( data.getRainingSeverity() );
    }
    
    /**
     * Gets ambient temperature (Celsius)
     * 
     * @return ambient temperature (Celsius)
     */
    public final float getAmbientTemperatureK()
    {
        return ( data.getAmbientTemperature() + Convert.ZERO_KELVIN );
    }
    
    /**
     * Gets ambient temperature (Celsius)
     * 
     * @return ambient temperature (Celsius)
     */
    public final float getAmbientTemperatureC()
    {
        return ( data.getAmbientTemperature() );
    }
    
    /**
     * Gets ambient temperature (Fahrenheit)
     * 
     * @return ambient temperature (Fahrenheit)
     */
    public final float getAmbientTemperatureF()
    {
        return ( Convert.celsius2Fahrehheit( data.getAmbientTemperature() ) );
    }
    
    /**
     * Gets ambient temperature (PLR selected units)
     * 
     * @return ambient temperature (PLR selected units)
     */
    public final float getAmbientTemperature()
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getAmbientTemperatureF() );
        
        return ( getAmbientTemperatureC() );
    }
    
    /**
     * Gets track temperature (Celsius)
     * 
     * @return track temperature (Celsius)
     */
    public final float getTrackTemperatureK()
    {
        return ( data.getTrackTemperature() + Convert.ZERO_KELVIN );
    }
    
    /**
     * Gets track temperature (Celsius)
     * 
     * @return track temperature (Celsius)
     */
    public final float getTrackTemperatureC()
    {
        return ( data.getTrackTemperature() );
    }
    
    /**
     * Gets track temperature (Fahrenheit)
     * 
     * @return track temperature (Fahrenheit)
     */
    public final float getTrackTemperatureF()
    {
        return ( Convert.celsius2Fahrehheit( data.getTrackTemperature() ) );
    }
    
    /**
     * Gets track temperature (PLR selected units)
     * 
     * @return track temperature (PLR selected units)
     */
    public final float getTrackTemperature()
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getTrackTemperatureF() );
        
        return ( getTrackTemperatureC() );
    }
    
    /**
     * Gets wind speed in m/sec.
     * 
     * @param speed output buffer
     */
    public final void getWindSpeedMS( TelemVect3 speed )
    {
        data.getWindSpeed( speed );
    }
    
    /**
     * Gets wind speed in km/h.
     * 
     * @param speed output buffer
     */
    public final void getWindSpeedKph( TelemVect3 speed )
    {
        data.getWindSpeed( speed );
        
        speed.x *= SpeedUnits.Convert.MPS_TO_KPH;
        speed.y *= SpeedUnits.Convert.MPS_TO_KPH;
        speed.z *= SpeedUnits.Convert.MPS_TO_KPH;
    }
    
    /**
     * Gets wind speed in mi/h.
     * 
     * @param speed output buffer
     */
    public final void getWindSpeedMph( TelemVect3 speed )
    {
        data.getWindSpeed( speed );
        
        speed.x *= SpeedUnits.Convert.MPS_TO_MPH;
        speed.y *= SpeedUnits.Convert.MPS_TO_MPH;
        speed.z *= SpeedUnits.Convert.MPS_TO_MPH;
    }
    
    /**
     * Gets wind speed in km/h or mph depending on PLR settings.
     * 
     * @param speed output buffer
     */
    public final void getWindSpeed( TelemVect3 speed )
    {
        if ( gameData.getProfileInfo().getSpeedUnits() == SpeedUnits.MPH )
            getWindSpeedMph( speed );
        else
            getWindSpeedKph( speed );
    }
    
    /**
     * Gets wetness on main path 0.0-1.0
     * 
     * @return wetness on main path 0.0-1.0
     */
    public final float getOnPathWetness()
    {
        return ( data.getOnPathWetness() );
    }
    
    /**
     * Gets wetness off main path 0.0-1.0
     * 
     * @return wetness off main path 0.0-1.0
     */
    public final float getOffPathWetness()
    {
        return ( data.getOffPathWetness() );
    }
    
    /**
     * Gets the i-th vehicle scoring info.
     * 
     * @param i the index
     * 
     * @see #getNumVehicles()
     * 
     * @return the i-th vehicle scoring info.
     */
    public final VehicleScoringInfo getVehicleScoringInfo( int i )
    {
        // VehicleScoringInfoV2* mVehicle
        
        if ( i >= getNumVehicles() )
            throw new IllegalArgumentException( "There is no vehicle with the index " + i + ". There are only " + getNumVehicles() + " vehicles." );
        
        return ( vehicleScoringInfo2[i] );
    }
    
    /**
     * Gets all the current {@link VehicleScoringInfo}s and writes them into the given array.
     * 
     * @param vsis the target array (must be of at least {@link #getNumVehicles()} size.
     * 
     * @return the number of {@link VehicleScoringInfo}s.
     */
    public final int getVehicleScoringInfos( VehicleScoringInfo[] vsis )
    {
        if ( vsis == null )
            throw new NullPointerException( "vsis parameter is null" );
        
        int n = getNumVehicles();
        
        if ( vsis.length < n )
            throw new ArrayIndexOutOfBoundsException( "vsis array too small (" + vsis.length + " < " + n + ")." );
        
        System.arraycopy( vehicleScoringInfo2, 0, vsis, 0, n );
        
        return ( n );
    }
    
    /**
     * Gets the leader's {@link VehicleScoringInfo}.
     * This is equivalent to getVehicleScoringInfo( 0 ).
     * 
     * @return the leader's {@link VehicleScoringInfo}.
     */
    public final VehicleScoringInfo getLeadersVehicleScoringInfo()
    {
        return ( getVehicleScoringInfo( 0 ) );
    }
    
    /**
     * Gets the player's VehicleScroingInfo.
     * 
     * @see #getOwnPlace(boolean)
     * 
     * @return the player's VehicleScroingInfo.
     */
    public final VehicleScoringInfo getPlayersVehicleScoringInfo()
    {
        if ( playerVSI == null )
        {
            int n = getNumVehicles();
            for ( short i = 0; i < n; i++ )
            {
                if ( vehicleScoringInfo2[i].isPlayer() )
                {
                    playerVSI = vehicleScoringInfo2[i];
                    break;
                }
            }
        }
        
        return ( playerVSI );
    }
    
    void toggleFixedViewedVSI()
    {
        this.fixedViewedVSI = !fixedViewedVSI;
        this.viewedVSI = null;
    }
    
    void setControlledViewedVSI( VehicleScoringInfo controlledViewedVSI )
    {
        this.controlledViewedVSI = controlledViewedVSI;
    }
    
    /**
     * Gets the viewed's VehicleScroingInfo (this is just a guess, but should be correct).
     * 
     * @return the viewed's VehicleScroingInfo.
     */
    public final VehicleScoringInfo getViewedVehicleScoringInfo()
    {
        if ( controlledViewedVSI != null )
            return ( controlledViewedVSI );
        
        if ( viewedVSI == null )
        {
            LiveGameDataController controller = gameData.getLiveGameDataController();
            int controlledId = ( controller == null ) ? -1 : controller.getViewedVSIId();
            
            int n = getNumVehicles();
            
            if ( controlledId >= 0 )
            {
                for ( short i = 0; i < n; i++ )
                {
                    if ( vehicleScoringInfo2[i].getDriverId() == controlledId )
                    {
                        viewedVSI = vehicleScoringInfo2[i];
                        return ( viewedVSI );
                    }
                }
            }
        }
        
        // Not found! Search the regular way...
        
        if ( fixedViewedVSI )
            return ( getPlayersVehicleScoringInfo() );
        
        GraphicsInfo gi = gameData.getGraphicsInfo();
        
        //if ( !gi.isUpdatedInRealtimeMode() )
        //    return ( getPlayersVehicleScoringInfo() );
        
        if ( viewedVSI == null )
        {
            viewedVSI = gi.getViewedVehicleScoringInfo();
        }
        
        return ( viewedVSI );
    }
    
    /**
     * Gets the position of the player.
     * 
     * @param byClass only consider vehicles in the same class
     * 
     * @return the position of the player.
     */
    public final short getOwnPlace( boolean byClass )
    {
        return ( getPlayersVehicleScoringInfo().getPlace( byClass ) );
    }
    
    /**
     * Gets the VehicleScoringInfo for the fastest sector1.
     * 
     * @return the VehicleScoringInfo for the fastest sector1.
     */
    public final VehicleScoringInfo getFastestSector1VSI()
    {
        if ( fastestSector1VSI == null )
        {
            fastestSector1VSI = vehicleScoringInfo2[0];
            float fs = fastestSector1VSI.getBestSector1();
            
            for ( int i = 1; i < vehicleScoringInfo2.length; i++ )
            {
                float fs_ = vehicleScoringInfo2[i].getBestLapTime();
                if ( ( fs_ > 0f ) && ( fs_ < fs ) )
                {
                    fastestSector1VSI = vehicleScoringInfo2[i];
                    fs = fs_;
                }
            }
        }
        
        return ( fastestSector1VSI );
    }
    
    /**
     * Gets the VehicleScoringInfo for the fastest sector2.
     * 
     * @return the VehicleScoringInfo for the fastest sector2.
     */
    public final VehicleScoringInfo getFastestSector2VSI()
    {
        if ( fastestSector2VSI == null )
        {
            fastestSector2VSI = vehicleScoringInfo2[0];
            float fs = fastestSector2VSI.getBestSector2( false );
            
            for ( int i = 1; i < vehicleScoringInfo2.length; i++ )
            {
                float fs_ = vehicleScoringInfo2[i].getBestSector2( false );
                if ( ( fs_ > 0f ) && ( fs_ < fs ) )
                {
                    fastestSector2VSI = vehicleScoringInfo2[i];
                    fs = fs_;
                }
            }
        }
        
        return ( fastestSector2VSI );
    }
    
    /**
     * Gets the VehicleScoringInfo for the fastest sector3.
     * 
     * @return the VehicleScoringInfo for the fastest sector3.
     */
    public final VehicleScoringInfo getFastestSector3VSI()
    {
        if ( fastestSector3VSI == null )
        {
            fastestSector3VSI = vehicleScoringInfo2[0];
            float fs = fastestSector3VSI.getBestSector3();
            
            for ( int i = 1; i < vehicleScoringInfo2.length; i++ )
            {
                float fs_ = vehicleScoringInfo2[i].getBestSector3();
                if ( ( fs_ > 0f ) && ( fs_ < fs ) )
                {
                    fastestSector3VSI = vehicleScoringInfo2[i];
                    fs = fs_;
                }
            }
        }
        
        return ( fastestSector3VSI );
    }
    
    /**
     * Gets the VehicleScoringInfo for the fastest sector i.
     * 
     * @param sector the queried sector
     * 
     * @return the VehicleScoringInfo for the fastest sector i.
     */
    public final VehicleScoringInfo getFastestSectorVSI( int sector )
    {
        if ( sector == 1 )
            return ( getFastestSector1VSI() );
        
        if ( sector == 2 )
            return ( getFastestSector2VSI() );
        
        if ( sector == 3 )
            return ( getFastestSector3VSI() );
        
        throw new IllegalArgumentException( "sector must be between 1 and 3." );
    }
    
    /**
     * Gets the VehicleScoringInfo for the fastest lap.
     * 
     * @return the VehicleScoringInfo for the fastest lap.
     */
    public final VehicleScoringInfo getFastestLapVSI()
    {
        if ( fastestLapVSI == null )
        {
            secondFastestLapVSI = null;
            
            if ( !getSessionType().isRace() )
            {
                // VehicleScoringInfos are sorted by place, which is the same as by laptime in non-race sessions.
                
                fastestLapVSI = vehicleScoringInfo2[0];
                
                //if ( ( vehicleScoringInfo2.length > 1 ) && ( vehicleScoringInfo2[1].getBestLapTime() > 0f ) )
                if ( ( vehicleScoringInfo2.length > 1 ) && ( vehicleScoringInfo2[1].getFastestLaptime() != null ) )
                {
                    secondFastestLapVSI = vehicleScoringInfo2[1];
                }
                
                //RFDHLog.debug( TimingUtil.getTimeAsLaptimeString( getSessionTime() ) + ", " + fastestLapVSI.getLapsCompleted() + ": " + fastestLapVSI + ", " + fastestLapVSI.getFastestLaptime() );
                return ( fastestLapVSI );
            }
            
            int i0;
            for ( i0 = 0; i0 < vehicleScoringInfo2.length; i0++ )
            {
                Laptime lt_ = vehicleScoringInfo2[i0].getFastestLaptime();
                if ( ( lt_ != null ) && ( lt_.getLapTime() > 0f ) && lt_.isFinished() )
                    break;
            }
            
            if ( i0 == vehicleScoringInfo2.length )
            {
                fastestLapVSI = vehicleScoringInfo2[0];
                
                if ( vehicleScoringInfo2.length > 1 )
                    secondFastestLapVSI = vehicleScoringInfo2[1];
            }
            else
            {
                fastestLapVSI = vehicleScoringInfo2[i0];
                Laptime lt = fastestLapVSI.getFastestLaptime();
                
                for ( int i = i0 + 1; i < vehicleScoringInfo2.length; i++ )
                {
                    Laptime lt_ = vehicleScoringInfo2[i].getFastestLaptime();
                    if ( ( lt_ != null ) && ( lt_.getLapTime() < lt .getLapTime() ) )
                    {
                        secondFastestLapVSI = fastestLapVSI;
                        fastestLapVSI = vehicleScoringInfo2[i];
                        lt = lt_;
                    }
                }
                
                if ( ( secondFastestLapVSI == null ) && ( vehicleScoringInfo2.length > i0 ) )
                {
                    Laptime lt2 = null;
                    
                    for ( int i = i0 + 1; i < vehicleScoringInfo2.length; i++ )
                    {
                        Laptime lt_ = vehicleScoringInfo2[i].getFastestLaptime();
                        if ( lt_ != null )
                        {
                            if ( secondFastestLapVSI == null )
                            {
                                secondFastestLapVSI = vehicleScoringInfo2[i];
                                lt2 = secondFastestLapVSI.getFastestLaptime();
                            }
                            else if ( ( lt2 == null ) || ( lt_.getLapTime() < lt2.getLapTime() ) )
                            {
                                secondFastestLapVSI = vehicleScoringInfo2[i];
                                lt2 = lt_;
                            }
                        }
                    }
                }
            }
        }
        
        return ( fastestLapVSI );
    }
    
    /**
     * Gets the VehicleScoringInfo for the second fastest lap (or <code>null</code>).
     * 
     * @return the VehicleScoringInfo for the second fastest lap (or <code>null</code>).
     */
    public final VehicleScoringInfo getSecondFastestLapVSI()
    {
        getFastestLapVSI();
        
        return ( secondFastestLapVSI );
    }
    
    /**
     * Gets the absolute fastes lap time.
     * 
     * @return the absolute fastest lap time.
     */
    public final Laptime getFastestLaptime()
    {
        return ( getFastestLapVSI().getFastestLaptime() );
    }
    
    void setControlledCompareVSI( VehicleScoringInfo controlledCompareVSI )
    {
        this.controlledCompareVSI = controlledCompareVSI;
    }
    
    /**
     * <p>
     * Gets the {@link VehicleScoringInfo} to compare against.
     * </p>
     * 
     * <p>
     * By default this is <code>null</code>, which leads to default behavior. But a plugin can override this.
     * </p>
     * 
     * @return the {@link VehicleScoringInfo} to compare laptimes against.
     */
    public final VehicleScoringInfo getCompareVSI()
    {
        return ( controlledCompareVSI );
    }
    
    ScoringInfo( LiveGameData gameData, _LiveGameDataObjectsFactory gdFactory, GameEventsManager eventsManager )
    {
        this.gameData = gameData;
        this.gdFactory = gdFactory;
        this.data = gdFactory.newScoringInfoCapsule( gameData );
        this.eventsManager = eventsManager;
        
        this.tlcGenerator = AbstractThreeLetterCodeGenerator.initThreeLetterCodeGenerator( gameData.getFileSystem().getPluginINI().getGeneralThreeLetterCodeGeneratorClass() );
        
        registerListener( LaptimesRecorder.INSTANCE );
        registerListener( FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER );
        registerListener( TopspeedRecorder.MASTER_TOPSPEED_RECORDER );
    }
}
