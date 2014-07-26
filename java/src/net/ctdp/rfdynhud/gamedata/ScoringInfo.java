/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.util.AbstractThreeLetterCodeGenerator;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.ThreeLetterCodeGenerator;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.util.TimingUtil;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class ScoringInfo
{
    protected final LiveGameData gameData;
    protected final _LiveGameDataObjectsFactory gdFactory;
    
    private boolean updatedInTimeScope = false;
    private long updateId = 0L;
    private long updateTimestamp = -1L;
    private int sessionId = 0;
    private boolean sessionRunning = false;
    private int sessionJustStarted = 0;
    
    private long sessionStartTimestamp = -1L;
    private long cockpitEnteredTimestamp = -1L;
    private int cockpitEnteredId = 0;
    
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
    
    public abstract void readFromStream( InputStream in, EditorPresets editorPresets ) throws IOException;
    
    /**
     * Read default values. This is usually done in editor mode.
     * 
     * @param editorPresets <code>null</code> in non editor mode
     */
    public abstract void loadDefaultValues( EditorPresets editorPresets );
    
    public abstract void writeToStream( OutputStream out ) throws IOException;
    
    private VehicleScoringInfo[] vehicleScoringInfoCache = null;
    private VehicleScoringInfo[] vehicleScoringInfo = null;
    private int numVehicles = -1;
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
    
    private final Map<Integer, VehicleScoringInfo> oldIdVSIMap = new HashMap<Integer, VehicleScoringInfo>();
    
    /**
     * Gets the i-th (unsorted) {@link VehicleScoringInfo} instance in this {@link ScoringInfo}.
     * 
     * @param i
     * 
     * @return the i-th (unsorted) {@link VehicleScoringInfo} instance in this {@link ScoringInfo}.
     */
    protected final VehicleScoringInfo getCachedVehicleScoringInfo( int i )
    {
        if ( ( vehicleScoringInfoCache == null ) || ( vehicleScoringInfoCache.length == 0 ) )
            return ( null );
        
        return ( vehicleScoringInfoCache[i] );
    }
    
    private void initVehicleScoringInfo( int numVehicles )
    {
        if ( sessionJustStarted == 1 )
            this.sessionJustStarted = 2;
        
        this.numVehicles = numVehicles;
        
        if ( ( vehicleScoringInfoCache == null ) || ( vehicleScoringInfoCache.length < numVehicles ) )
        {
            vehicleScoringInfoCache = gdFactory.newVehicleScoringInfos( gameData, (int)( numVehicles * 1.5 ) + 1, vehicleScoringInfoCache );
        }
        
        if ( ( vehicleScoringInfo == null ) || ( vehicleScoringInfo.length != numVehicles ) )
        {
            vehicleScoringInfo = new VehicleScoringInfo[ numVehicles ];
            
            System.arraycopy( vehicleScoringInfoCache, 0, vehicleScoringInfo, 0, numVehicles );
        }
    }
    
    /**
     * 
     * @param numVehicles
     * @param userObject
     */
    private void checkVSIs( int numVehicles, Object userObject )
    {
        final int n = numVehicles;
        
        try
        {
            for ( int i = 0; i < n; i++ )
            {
                VehicleScoringInfo vsi = vehicleScoringInfo[i];
                Integer id = vsi.refreshID( i );
                
                //Logger.log( "Found data for " + vsi.getDriverName() + ", assigned id " + id + "." );
                
                // Detect joined drivers...
                
                if ( oldIdVSIMap.remove( id ) == null )
                {
                    RFDHLog.debug( "[DEBUG]: Player joined: ", vsi.getDriverName(), ", id = ", id, ", index = ", i, ", fastest lap: " + TimingUtil.getTimeAsLaptimeString( vsi.getBestLapTime() ) );
                    
                    //if ( vsi.getBestLapTime() > 0f )
                    {
                        Laptime lt = new Laptime( vsi.getDriverId(), 0, -1f, -1f, -1f, false, false, true );
                        lt.laptime = vsi.getBestLapTime();
                        vsi.setFastestLaptime( lt );
                        
                        // TODO: Only for rejoins actually.
                        for ( int j = 0; j < vsi.laptimes.size(); j++ )
                        {
                            lt = vsi.laptimes.get( j );
                            if ( lt != null )
                            {
                                lt.sector1 = -1f;
                                lt.sector2 = -1f;
                                lt.sector3 = -1f;
                                lt.laptime = -1f;
                            }
                        }
                    }
                    
                    //changedVSIs.add( new Object[] { +1, vsi } );
                }
            }
            
            // Detect left drivers...
            
            for ( Map.Entry<Integer, VehicleScoringInfo> leftVSI : oldIdVSIMap.entrySet() )
            {
                RFDHLog.debug( "[DEBUG]: ", leftVSI.getValue().getOldDriverName(), " left the game." );
                
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onPlayerLeft( gameData, leftVSI.getKey() );
                    }
                    catch ( Throwable t )
                    {
                        RFDHLog.exception( t );
                    }
                }
                
                //changedVSIs.add( new Object[] { -1, leftVSI.getValue().getDriverID() } );
            }
            
            for ( int i = 0; i < n; i++ )
            {
                VehicleScoringInfo vsi = vehicleScoringInfo[i];
                
                oldIdVSIMap.put( vsi.getDriverID(), vsi );
            }
            
            
            /*
            int firstFree = 0;
            
            if ( idCapsuleMap.size() > 0 )
            {
                for ( Integer joinedID : idCapsuleMap.keySet() )
                {
                    // Player joined
                    
                    _VehicleScoringInfoCapsule data = idCapsuleMap.get( joinedID );
                    
                    VehicleScoringInfo vsi = leftVSICache.remove( joinedID );
                    
                    if ( vsi == null )
                    {
                        vsi = vehicleScoringInfoCache[firstFree++];
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
                        vehicleScoringInfoCache[firstFree++] = vsi;
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
            */
            
            /*
            idCapsuleMap.clear();
            
            // Check for errors and do a workaround...
            int somethingWrong = 0;
            for ( int i = 0; i < n; i++ )
            {
                if ( vehicleScoringInfoCache[i] == null )
                {
                    somethingWrong++;
                    vehicleScoringInfoCache[i] = new VehicleScoringInfo( this, gameData.getProfileInfo(), gameData );
                    vehicleScoringInfoCache[i].data = gdFactory.newVehicleScoringInfoCapsule( gameData );
                }
                else if ( vehicleScoringInfoCache[i].data == null )
                {
                    somethingWrong++;
                    vehicleScoringInfoCache[i].data = gdFactory.newVehicleScoringInfoCapsule( gameData );
                }
            }
            
            if ( somethingWrong > 0 )
            {
                RFDHLog.printlnEx( "WARNING: Something went wrong when initializing VehicleScoringInfos. Had to add " + somethingWrong + " instances. numVehicles = " + n + ", old = " + oldNumVehicles );
            }
            */
            
            /*
            System.arraycopy( vehicleScoringInfoCache, 0, vehicleScoringInfo, 0, n );
            
            //for ( int i = n; i < vehicleScoringInfo.length; i++ )
            for ( int i = n; i < oldNumVehicles; i++ )
                vehicleScoringInfoCache[i].data = null;
            */
            
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
        
        /*
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
        */
    }
    
    protected void resetDerivateData()
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
        
        if ( vehicleScoringInfoCache != null )
        {
            for ( int i = 0; i < vehicleScoringInfoCache.length; i++ )
            {
                if ( vehicleScoringInfoCache[i] != null )
                {
                    vehicleScoringInfoCache[i].resetDerivateData();
                }
            }
        }
    }
    
    /**
     * 
     * @param numVehicles
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected void prepareDataUpdate( int numVehicles, Object userObject, long timestamp )
    {
        lastUpdateTimestamp = timestamp;
        
        initVehicleScoringInfo( numVehicles );
    }
    
    /**
     * 
     * @param numVehicles
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected abstract void updateDataImpl( int numVehicles, Object userObject, long timestamp );
    
    protected void applyEditorPresets( EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
        
        final int n = getNumVehicles();
        for ( int i = 0; i < n; i++ )
            getVehicleScoringInfo( i ).applyEditorPresets( i, editorPresets );
        
        FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.applyEditorPresets( gameData, editorPresets );
    }
    
    private void updateRaceLengthPercentage( boolean isEditorMode )
    {
        if ( ( getSessionType() != null ) && getSessionType().isRace() )
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
    
    protected void executeOnVSIDataUpdated( long timestamp )
    {
        for ( int i = 0; i < numVehicles; i++ )
            vehicleScoringInfo[i].onDataUpdated( timestamp );
    }
    
    /**
     * @param numVehicles
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected void onDataUpdatedImpl( int numVehicles, Object userObject, long timestamp )
    {
    }
    
    /**
     * @param numVehicles
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected final void onDataUpdated( int numVehicles, Object userObject, long timestamp )
    {
        try
        {
            resetDerivateData();
            
            executeOnVSIDataUpdated( timestamp );
            
            checkVSIs( numVehicles, userObject );
            
            Arrays.sort( vehicleScoringInfo, VehicleScoringInfo.VSIPlaceComparator.INSTANCE );
            
            this.updatedInTimeScope = true;
            this.updateId++;
            this.updateTimestamp = timestamp;
            this.gamePausedCache = gameData.isGamePaused();
            
            this.sessionBaseNanos = Math.round( getSessionTimeImpl() * 1000000000.0 );
            updateSessionTime( updateTimestamp );
            
            for ( int i = 0; i < numVehicles; i++ )
                vehicleScoringInfo[i].updateSomeData();
            
            boolean rlpUpdated = false;
            
            if ( sessionJustStarted > 0 )
            {
                updateRaceLengthPercentage( userObject instanceof EditorPresets );
                rlpUpdated = true;
                
                for ( int i = 0; i < numVehicles; i++ )
                    vehicleScoringInfo[i].onSessionStarted();
                
                this.sessionJustStarted = 0;
                lastGamePhase = null;
            }
            
            if ( userObject instanceof EditorPresets )
                applyEditorPresets( (EditorPresets)userObject );
            
            if ( ( numVehicles > 0 ) && getLeadersVehicleScoringInfo().isLapJustStarted() && !rlpUpdated )
            {
                updateRaceLengthPercentage( userObject instanceof EditorPresets );
                rlpUpdated = true;
            }
            
            GamePhase gamePhase = getGamePhase();
            if ( ( gamePhase != lastGamePhase ) && !rlpUpdated )
            {
                updateRaceLengthPercentage( userObject instanceof EditorPresets );
                rlpUpdated = true;
            }
            lastGamePhase = gamePhase;
            
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onScoringInfoUpdated( gameData, userObject instanceof EditorPresets );
                    }
                    catch ( Throwable t )
                    {
                        RFDHLog.exception( t );
                    }
                }
            }
            
            onDataUpdatedImpl( numVehicles, userObject, timestamp );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    protected void updateData( int numVehicles, Object userObject, long timestamp )
    {
        if ( gameData.getProfileInfo().isValid() )
        {
            if ( userObject instanceof EditorPresets )
                numVehicles = 64; // Just to have enough.
            
            prepareDataUpdate( numVehicles, userObject, timestamp );
            
            updateDataImpl( numVehicles, userObject, timestamp );
            
            if ( userObject instanceof EditorPresets )
            {
                numVehicles = getNumVehiclesImpl();
                
                prepareDataUpdate( numVehicles, userObject, timestamp );
                
                updateDataImpl( numVehicles, userObject, timestamp );
            }
            
            onDataUpdated( numVehicles, userObject, timestamp );
        }
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
    
    /**
     * @param timestamp
     * @param isEditorMode
     */
    protected void onSessionStarted( long timestamp, boolean isEditorMode )
    {
        this.sessionId++;
        this.sessionStartTimestamp = timestamp;
        this.sessionBaseNanos = 0L;
        this.sessionRunning = true;
        this.sessionJustStarted = 1;
        this.updatedInTimeScope = false;
        
        this.raceLengthPercentage = 1.0; // We don't know it better now!
    }
    
    /**
     * 
     * @param timestamp
     */
    protected void onSessionEnded( long timestamp )
    {
        this.sessionRunning = false;
        this.updatedInTimeScope = false;
        
        if ( vehicleScoringInfoCache != null )
        {
            for ( int i = 0; i < vehicleScoringInfoCache.length; i++ )
            {
                vehicleScoringInfoCache[i].onSessionEnded();
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
    
    protected void onCockpitEntered( long timestamp )
    {
        this.cockpitEnteredTimestamp = timestamp;
        this.cockpitEnteredId++;
        this.updatedInTimeScope = true;
    }
    
    /**
     * 
     * @param timestamp
     */
    protected void onCockpitExited( long timestamp )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * Gets the system timestamp in nanoseconds, at which the player entered the cockpit.
     * 
     * @return the system timestamp in nanoseconds, at which the player entered the cockpit.
     */
    public final long getCockpitEnteredTimestamp()
    {
        return ( cockpitEnteredTimestamp );
    }
    
    /**
     * This ID is incremented each time, the player enters realtime mode.
     * 
     * @return the ID of realtime enter actions.
     */
    public final int getRealtimeEntredId()
    {
        return ( cockpitEnteredId );
    }
    
    /**
     * Gets, whether the last update of these data has been done while in running session resp. cockpit mode.
     * @return whether the last update of these data has been done while in running session resp. cockpit mode.
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
     * Gets the system nano time for the last data update.
     * 
     * @return the system nano time for the last data update.
     */
    public final long getUpdateTimestamp()
    {
        return ( updateTimestamp );
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
    
    /**
     * Gets a multiplier in range [0, 1] for the race distance.
     * 
     * @return a multiplier in range [0, 1] for the race distance.
     */
    public final double getRaceLengthPercentage()
    {
        return ( raceLengthPercentage );
    }
    
    private final Set<Integer> handledClassIDs = new HashSet<Integer>();
    
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
    protected abstract String getTrackNameImpl();
    
    /**
     * Gets the current track name.
     * 
     * @return the current track name.
     */
    public final String getTrackName()
    {
        if ( trackName == null )
        {
            trackName = getTrackNameImpl();
        }
        
        return ( trackName );
    }
    
    /**
     * Gets current session type.
     * 
     * @return current session type.
     */
    public abstract SessionType getSessionType();
    
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
     * Gets current session time in seconds.
     * 
     * @return current session time in seconds.
     */
    protected abstract float getSessionTimeImpl();
    
    /**
     * Gets current session time in seconds.
     * 
     * @return current session time in seconds.
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
    public abstract float getEndTime();
    
    /**
     * Gets maximum laps.
     * 
     * @return maximum laps.
     */
    public abstract int getMaxLaps();
    
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
        if ( ( getSessionType() != null ) && getSessionType().isRace() )
            return ( getLeadersVehicleScoringInfo().getEstimatedMaxLaps() );
        
        return ( vsi.getEstimatedMaxLaps() );
    }
    
    /**
     * Gets the distance around track.
     * 
     * @return the distance around track.
     */
    protected abstract float getTrackLengthImpl();
    
    /**
     * Gets the distance around track.
     * 
     * @return the distance around track.
     */
    public final float getTrackLength()
    {
        if ( trackLength < 0f )
        {
            trackLength = getTrackLengthImpl();
        }
        
        return ( trackLength );
    }
    
    /**
     * Gets the current number of vehicles.
     * 
     * @return the current number of vehicles.
     */
    protected abstract int getNumVehiclesImpl();
    
    /**
     * Gets the current number of vehicles.
     * 
     * @return the current number of vehicles.
     */
    public final int getNumVehicles()
    {
        if ( sessionJustStarted == 1 )
            return ( 0 );
        
        //if ( numVehicles == -1 )
        //    numVehicles = getNumVehiclesImpl();
        
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
        int nc = 0;
        
        final int n = getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            if ( getVehicleScoringInfo( i ).getVehicleClassId() == vsi.getVehicleClassId() )
                nc++;
        }
        
        return ( nc );
    }
    
    /**
     * Gets the current game phase.
     * 
     * @return the current game phase.
     */
    public abstract GamePhase getGamePhase();
    
    /**
     * Gets the current yellow flag state (applies to full-course only).
     * 
     * @return the current yellow flag state.
     */
    public abstract YellowFlagState getYellowFlagState();
    
    /**
     * Gets whether there are any local yellows at the moment in the sector.
     * 
     * @param sector the queried sector (1,2,3)
     * 
     * @return whether there are any local yellows at the moment in the sector
     */
    public abstract boolean getSectorYellowFlag( int sector );
    
    /**
     * Gets the current start light frame (number depends on track).
     * 
     * @see #getNumStartingLights()
     * 
     * @return the current start light frame.
     */
    public abstract int getStartLightFrame();
    
    /**
     * Gets the number of lights in start sequence.
     * 
     * @see #getStartLightFrame()
     * 
     * @return the number of lights in start sequence.
     */
    public abstract int getNumStartingLights();
    
    /**
     * Gets the number of red lights in start sequence.
     * 
     * @deprecated replaced by {@link #getNumStartingLights()}
     * 
     * @return the number of red lights in start sequence.
     */
    @Deprecated
    public final int getNumRedLights()
    {
        return ( getNumStartingLights() );
    }
    
    /**
     * Gets whether we're in realtime as opposed to at the monitor.
     * 
     * @return whether we're in realtime as opposed to at the monitor.
     * 
     * @deprecated replaced by {@link LiveGameData#isInCockpit()}
     */
    @Deprecated
    public final boolean isInRealtimeMode()
    {
        return ( gameData.isInCockpit() );
    }
    
    /**
     * Gets the player name (including possible multiplayer override).
     * 
     * @return the player name.
     */
    protected abstract String getPlayerNameImpl();
    
    /**
     * Gets the player name (including possible multiplayer override).
     * 
     * @return the player name.
     */
    public final String getPlayerName()
    {
        if ( playerName == null )
        {
            playerName = getPlayerNameImpl();
        }
        
        return ( playerName );
    }
    
    /**
     * Gets the player's filename (PLR) (may be encoded to be a legal filename).
     * 
     * @return the player's filename.
     */
    protected abstract String getPlayerFilenameImpl();
    
    /**
     * Gets the player's filename (PLR) (may be encoded to be a legal filename).
     * 
     * @return the player's filename.
     */
    public final String getPlayerFilename()
    {
        if ( playerFilename == null )
        {
            playerFilename = getPlayerFilenameImpl();
        }
        
        return ( playerFilename );
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
        
        return ( vehicleScoringInfo[i] );
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
        
        System.arraycopy( vehicleScoringInfo, 0, vsis, 0, n );
        
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
                if ( vehicleScoringInfo[i].isPlayer() )
                {
                    playerVSI = vehicleScoringInfo[i];
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
    
    /**
     * Sets the viewed vehicle (updated on the next frame).<br />
     * This operation can be ignored, if the underlying sim doesn't support it.
     * 
     * @param vsi the next viewed vehicle
     * @param cameraType the new camera type
     * 
     * @see GraphicsInfo#CAMERA_TYPE_COCKPIT
     * @see GraphicsInfo#CAMERA_TYPE_TV_COCKPIT
     * @see GraphicsInfo#CAMERA_TYPE_NOSECAM
     * @see GraphicsInfo#CAMERA_TYPE_SWINGMAN
     * @see GraphicsInfo#CAMERA_TYPE_TRACKSIDE
     */
    public abstract void setViewedVehicleScoringInfo( VehicleScoringInfo vsi, int cameraType );
    
    void setControlledViewedVSI( VehicleScoringInfo controlledViewedVSI )
    {
        this.controlledViewedVSI = controlledViewedVSI;
    }
    
    protected abstract VehicleScoringInfo getViewedVehicleScoringInfoImpl();
    
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
                    if ( vehicleScoringInfo[i].getDriverId() == controlledId )
                    {
                        viewedVSI = vehicleScoringInfo[i];
                        return ( viewedVSI );
                    }
                }
            }
        }
        
        // Not found! Search the regular way...
        
        if ( fixedViewedVSI )
            return ( getPlayersVehicleScoringInfo() );
        
        return ( getViewedVehicleScoringInfoImpl() );
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
            fastestSector1VSI = vehicleScoringInfo[0];
            float fs = fastestSector1VSI.getBestSector1();
            
            for ( int i = 1; i < vehicleScoringInfo.length; i++ )
            {
                float fs_ = vehicleScoringInfo[i].getBestLapTime();
                if ( ( fs_ > 0f ) && ( fs_ < fs ) )
                {
                    fastestSector1VSI = vehicleScoringInfo[i];
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
            fastestSector2VSI = vehicleScoringInfo[0];
            float fs = fastestSector2VSI.getBestSector2( false );
            
            for ( int i = 1; i < vehicleScoringInfo.length; i++ )
            {
                float fs_ = vehicleScoringInfo[i].getBestSector2( false );
                if ( ( fs_ > 0f ) && ( fs_ < fs ) )
                {
                    fastestSector2VSI = vehicleScoringInfo[i];
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
            fastestSector3VSI = vehicleScoringInfo[0];
            float fs = fastestSector3VSI.getBestSector3();
            
            for ( int i = 1; i < vehicleScoringInfo.length; i++ )
            {
                float fs_ = vehicleScoringInfo[i].getBestSector3();
                if ( ( fs_ > 0f ) && ( fs_ < fs ) )
                {
                    fastestSector3VSI = vehicleScoringInfo[i];
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
            
            if ( ( getSessionType() != null ) && !getSessionType().isRace() )
            {
                // VehicleScoringInfos are sorted by place, which is the same as by laptime in non-race sessions.
                
                fastestLapVSI = vehicleScoringInfo[0];
                
                //if ( ( vehicleScoringInfo2.length > 1 ) && ( vehicleScoringInfo2[1].getBestLapTime() > 0f ) )
                if ( ( vehicleScoringInfo.length > 1 ) && ( vehicleScoringInfo[1].getFastestLaptime() != null ) )
                {
                    secondFastestLapVSI = vehicleScoringInfo[1];
                }
                
                //RFDHLog.debug( TimingUtil.getTimeAsLaptimeString( getSessionTime() ) + ", " + fastestLapVSI.getLapsCompleted() + ": " + fastestLapVSI + ", " + fastestLapVSI.getFastestLaptime() );
                return ( fastestLapVSI );
            }
            
            int i0;
            for ( i0 = 0; i0 < vehicleScoringInfo.length; i0++ )
            {
                Laptime lt_ = vehicleScoringInfo[i0].getFastestLaptime();
                if ( ( lt_ != null ) && ( lt_.getLapTime() > 0f ) && lt_.isFinished() )
                    break;
            }
            
            if ( i0 == vehicleScoringInfo.length )
            {
                fastestLapVSI = vehicleScoringInfo[0];
                
                if ( vehicleScoringInfo.length > 1 )
                    secondFastestLapVSI = vehicleScoringInfo[1];
            }
            else
            {
                fastestLapVSI = vehicleScoringInfo[i0];
                Laptime lt = fastestLapVSI.getFastestLaptime();
                
                for ( int i = i0 + 1; i < vehicleScoringInfo.length; i++ )
                {
                    Laptime lt_ = vehicleScoringInfo[i].getFastestLaptime();
                    if ( ( lt_ != null ) && ( lt_.getLapTime() < lt .getLapTime() ) )
                    {
                        secondFastestLapVSI = fastestLapVSI;
                        fastestLapVSI = vehicleScoringInfo[i];
                        lt = lt_;
                    }
                }
                
                if ( ( secondFastestLapVSI == null ) && ( vehicleScoringInfo.length > i0 ) )
                {
                    Laptime lt2 = null;
                    
                    for ( int i = i0 + 1; i < vehicleScoringInfo.length; i++ )
                    {
                        Laptime lt_ = vehicleScoringInfo[i].getFastestLaptime();
                        if ( lt_ != null )
                        {
                            if ( secondFastestLapVSI == null )
                            {
                                secondFastestLapVSI = vehicleScoringInfo[i];
                                lt2 = secondFastestLapVSI.getFastestLaptime();
                            }
                            else if ( ( lt2 == null ) || ( lt_.getLapTime() < lt2.getLapTime() ) )
                            {
                                secondFastestLapVSI = vehicleScoringInfo[i];
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
    
    /**
     * @deprecated use {@link WeatherInfo#getCloudDarkness()}
     * 
     * @return the cloud darkness.
     */
    @Deprecated
    public final float getCloudDarkness()
    {
        return ( gameData.getWeatherInfo().getCloudDarkness() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getRainingSeverity()}
     * 
     * @return the rain severity.
     */
    @Deprecated
    public final float getRainingSeverity()
    {
        return ( gameData.getWeatherInfo().getRainingSeverity() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getAmbientTemperatureK()}
     * 
     * @return the ambient temperature in °K.
     */
    @Deprecated
    public final float getAmbientTemperatureK()
    {
        return ( gameData.getWeatherInfo().getAmbientTemperatureK() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getAmbientTemperatureC()}
     * 
     * @return the ambient temperature in °C.
     */
    @Deprecated
    public final float getAmbientTemperatureC()
    {
        return ( gameData.getWeatherInfo().getAmbientTemperatureC() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getAmbientTemperatureF()}
     * 
     * @return the ambient temperature in °F.
     */
    @Deprecated
    public final float getAmbientTemperatureF()
    {
        return ( gameData.getWeatherInfo().getAmbientTemperatureF() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getAmbientTemperature()}
     * 
     * @return the ambient temperature.
     */
    @Deprecated
    public final float getAmbientTemperature()
    {
        return ( gameData.getWeatherInfo().getAmbientTemperature() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getTrackTemperatureK()}
     * 
     * @return the track temperature in °K.
     */
    @Deprecated
    public final float getTrackTemperatureK()
    {
        return ( gameData.getWeatherInfo().getTrackTemperatureK() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getTrackTemperatureC()}
     * 
     * @return the track temperature in °C.
     */
    @Deprecated
    public final float getTrackTemperatureC()
    {
        return ( gameData.getWeatherInfo().getTrackTemperatureC() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getTrackTemperatureF()}
     * 
     * @return the track temperature in °F.
     */
    @Deprecated
    public final float getTrackTemperatureF()
    {
        return ( gameData.getWeatherInfo().getTrackTemperatureF() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getTrackTemperature()}
     * 
     * @return the track temperature.
     */
    @Deprecated
    public final float getTrackTemperature()
    {
        return ( gameData.getWeatherInfo().getTrackTemperature() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getWindSpeedMS(TelemVect3)}
     * 
     * @param speed
     */
    @Deprecated
    public final void getWindSpeedMS( TelemVect3 speed )
    {
        gameData.getWeatherInfo().getWindSpeedMS( speed );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getWindSpeedKmh(TelemVect3)}
     * 
     * @param speed
     */
    @Deprecated
    public final void getWindSpeedKph( TelemVect3 speed )
    {
        gameData.getWeatherInfo().getWindSpeedKmh( speed );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getWindSpeedMih(TelemVect3)}
     * 
     * @param speed
     */
    @Deprecated
    public final void getWindSpeedMph( TelemVect3 speed )
    {
        gameData.getWeatherInfo().getWindSpeedMih( speed );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getWindSpeed(TelemVect3)}
     * 
     * @param speed
     */
    @Deprecated
    public final void getWindSpeed( TelemVect3 speed )
    {
        gameData.getWeatherInfo().getWindSpeed( speed );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getOnPathWetness()}
     * 
     * @return the on path wetness.
     */
    @Deprecated
    public final float getOnPathWetness()
    {
        return ( gameData.getWeatherInfo().getOnPathWetness() );
    }
    
    /**
     * @deprecated use {@link WeatherInfo#getOffPathWetness()}
     * 
     * @return the off path wetness.
     */
    @Deprecated
    public final float getOffPathWetness()
    {
        return ( gameData.getWeatherInfo().getOffPathWetness() );
    }
    
    protected ScoringInfo( LiveGameData gameData )
    {
        this.gameData = gameData;
        this.gdFactory = gameData.getGameDataObjectsFactory();
//gdFactory.newVehicleScoringInfo( gameData ); // We need to call this to initialize the capsule class early to trick the invoked JVM.
        
        this.tlcGenerator = AbstractThreeLetterCodeGenerator.initThreeLetterCodeGenerator( gameData.getFileSystem().getPluginINI().getGeneralThreeLetterCodeGeneratorClass() );
        
        registerListener( LaptimesRecorder.INSTANCE );
        registerListener( FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER );
        registerListener( TopspeedRecorder.MASTER_TOPSPEED_RECORDER );
    }
}
