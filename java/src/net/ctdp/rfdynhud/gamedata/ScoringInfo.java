package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.util.Logger;

/**
 * 
 * @author Marvin Froehlich
 */
public class ScoringInfo
{
    private static final int OFFSET_TRACK_NAME = 0;
    private static final int MAX_TRACK_NAME_LENGTH = 64;
    private static final int OFFSET_SESSION_TYPE = OFFSET_TRACK_NAME + MAX_TRACK_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_CURRENT_TIME = OFFSET_SESSION_TYPE + ByteUtil.SIZE_LONG;
    private static final int OFFSET_END_TIME = OFFSET_CURRENT_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_MAX_LAPS = OFFSET_END_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAP_DISTANCE = OFFSET_MAX_LAPS + ByteUtil.SIZE_LONG;
    
    private static final int OFFSET_RESULTS_STREAM = OFFSET_LAP_DISTANCE + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_NUM_VEHICLES = OFFSET_RESULTS_STREAM + ByteUtil.SIZE_POINTER; // Is it just the pointer to be offsetted or the whole stream???
    
    private static final int OFFSET_GAME_PHASE = OFFSET_NUM_VEHICLES + ByteUtil.SIZE_LONG;
    private static final int OFFSET_YELLOW_FLAG_STATE = OFFSET_GAME_PHASE + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_SECTOR_FLAGS = OFFSET_YELLOW_FLAG_STATE + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_STARTING_LIGHT_FRAME = OFFSET_SECTOR_FLAGS + 3 * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_NUM_RED_LIGHTS = OFFSET_STARTING_LIGHT_FRAME + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_IN_REALTIME = OFFSET_NUM_RED_LIGHTS + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_PLAYER_NAME = OFFSET_IN_REALTIME + ByteUtil.SIZE_BOOL;
    private static final int MAX_PLAYER_NAME_LENGTH = 32;
    private static final int OFFSET_PLAYER_FILENAME = OFFSET_PLAYER_NAME + MAX_PLAYER_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int MAX_PLAYER_FILENAME_LENGTH = 64;
    
    private static final int OFFSET_CLOUD_DARKNESS = OFFSET_PLAYER_FILENAME + MAX_PLAYER_FILENAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_RAINING_SEVERITIY = OFFSET_CLOUD_DARKNESS + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_AMBIENT_TEMPERATURE = OFFSET_RAINING_SEVERITIY + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TRACK_TEMPERATURE = OFFSET_AMBIENT_TEMPERATURE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_WIND_SPEED = OFFSET_TRACK_TEMPERATURE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_ON_PATH_WETNESS = OFFSET_WIND_SPEED + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_OFF_PATH_WETNESS = OFFSET_ON_PATH_WETNESS + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_EXPANSION = OFFSET_OFF_PATH_WETNESS + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_VEHICLES = OFFSET_EXPANSION + ( 256 * ByteUtil.SIZE_CHAR );
    
    private static final int BUFFER_SIZE = OFFSET_VEHICLES + ByteUtil.SIZE_FLOAT /*+ ( 256 * ByteUtil.SIZE_CHAR )*/; // + ( 1 * VehicleScoringInfo.BUFFER_SIZE ); // How many vehicles?
    
    final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    //private final HashSet<String> lastKnownDriverNames = new HashSet<String>();
    
    private final LiveGameData gameData;
    
    private boolean updatedInTimeScope = false;
    private long updateId = 0L;
    private long updateTimestamp = -1L;
    private int sessionId = 0;
    private boolean sessionRunning = false;
    private int sessionJustStarted = 0;
    
    private long sessionStartTimestamp = -1L;
    private long realtimeEnteredTimestamp = -1L;
    private int realtimeEnteredId = 0;
    
    private long lastUpdateTimestamp = -1L;
    private long sessionBaseNanos = -1L;
    private long extrapolationNanos = 0L;
    private float extrapolationTime = 0.0f;
    private long sessionNanos = -1L;
    private float sessionTime = 0.0f;
    
    private double raceLengthPercentage = 1.0;
    
    private boolean classScoringCalculated = false;
    
    public static interface ScoringInfoUpdateListener
    {
        public void onSessionStarted( LiveGameData gameData, EditorPresets editorPresets );
        public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets );
        public void onScoringInfoUpdated( LiveGameData gameData, EditorPresets editorPresets );
        public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets );
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
            ScoringInfoUpdateListener[] tmp = new ScoringInfoUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
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
    }
    
    private final LaptimesRecorder laptimesRecorder = new LaptimesRecorder();
    private final RFactorEventsManager eventsManager;
    
    private VehicleScoringInfo[] vehicleScoringInfo = null;
    private VehicleScoringInfo[] vehicleScoringInfo2 = null;
    VehicleScoringInfoCapsule[] vehicleScoringInfoCapsules = null;
    private int numVehicles = -1;
    private int oldNumVehicles = -1;
    private VehicleScoringInfo playerVSI = null;
    private VehicleScoringInfo viewedVSI = null;
    
    private String playerName = null;
    private String playerFilename = null;
    private String trackName = null;
    
    private VehicleScoringInfo fastestLapVSI = null;
    private VehicleScoringInfo secondFastestLapVSI = null;
    private VehicleScoringInfo fastestSector1VSI = null;
    private VehicleScoringInfo fastestSector2VSI = null;
    private VehicleScoringInfo fastestSector3VSI = null;
    
    //private Laptime fastestLaptime = null;
    
    private final HashMap<Integer, VehicleScoringInfoCapsule> idCapsuleMap = new HashMap<Integer, VehicleScoringInfoCapsule>();
    private final HashMap<Integer, VehicleScoringInfo> leftVSICache = new HashMap<Integer, VehicleScoringInfo>();
    private final HashMap<Integer, Integer> nameDuplicatesMap = new HashMap<Integer, Integer>();
    
    void initVehicleScoringInfo()
    {
        if ( sessionJustStarted == 1 )
            this.sessionJustStarted = 2;
        
        oldNumVehicles = numVehicles;
        numVehicles = -1;
        numVehicles = getNumVehicles();
        
        if ( ( vehicleScoringInfoCapsules == null ) || ( vehicleScoringInfoCapsules.length < numVehicles ) )
        {
            VehicleScoringInfoCapsule[] tmp = new VehicleScoringInfoCapsule[ numVehicles ];
            
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
                tmp[i] = new VehicleScoringInfoCapsule();
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
                tmp[i] = new VehicleScoringInfo( this, gameData.getProfileInfo() );
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
                
                Integer id = vehicleScoringInfoCapsules[i].refreshID();
                
                if ( idCapsuleMap.containsKey( id ) )
                {
                    Integer pf = nameDuplicatesMap.get( id );
                    if ( pf == null )
                        pf = 2;
                    vehicleScoringInfoCapsules[i].postfixDriverName( String.valueOf( pf ), -1 );
                    //Logger.log( vehicleScoringInfoCapsules[i].getDriverName() );
                    nameDuplicatesMap.put( id, pf.intValue() + 1 );
                    id = vehicleScoringInfoCapsules[i].refreshID();
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
                            
                            //Logger.log( "Player " + vsi.getDriverName() + " left the game." );
                            
                            leftVSICache.put( vsi.getDriverID(), vsi );
                            vehicleScoringInfo[i] = vehicleScoringInfo[j];
                            vehicleScoringInfo[j] = new VehicleScoringInfo( this, gameData.getProfileInfo() );
                            i--;
                            j--;
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
                    
                    VehicleScoringInfoCapsule data = idCapsuleMap.get( joinedID );
                    
                    VehicleScoringInfo vsi = leftVSICache.remove( joinedID );
                    if ( vsi == null )
                    {
                        //Logger.log( "Player joined: " + data.getDriverName() + ", id = " + joinedID + ", index = " + firstFree );
                        
                        vsi = vehicleScoringInfo[firstFree++];
                        vsi.data = data;
                        vsi.setDriverName( data.getDriverName(), joinedID );
                    }
                    else
                    {
                        //Logger.log( "Player rejoined: " + data.getDriverName() + ", id = " + joinedID + ", index = " + firstFree );
                        
                        vehicleScoringInfo[firstFree++] = vsi;
                        vsi.data = data;
                    }
                }
            }
            
            idCapsuleMap.clear();
            
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
            Logger.log( t );
        }
    }
    
    private void resetDerivateData()
    {
        playerName = null;
        playerFilename = null;
        trackName = null;
        playerVSI = null;
        viewedVSI = null;
        
        fastestLapVSI = null;
        secondFastestLapVSI = null;
        fastestSector1VSI = null;
        fastestSector2VSI = null;
        fastestSector3VSI = null;
        
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
    
    final void updateSessionTime( long timestamp )
    {
        extrapolationNanos = timestamp - lastUpdateTimestamp;
        
        if ( extrapolationNanos > 600000000000L )
        {
            // The game seems to be paused.
            extrapolationNanos = 0L;
        }
        
        extrapolationTime = extrapolationNanos / 1000000000.0f;
        
        sessionNanos = sessionBaseNanos + extrapolationNanos;
        sessionTime = sessionNanos / 1000000000.0f;
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
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
        
        for ( int i = 0; i < getNumVehicles(); i++ )
            getVehicleScoringInfo( i ).applyEditorPresets( editorPresets );
        
        FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.applyEditorPresets( editorPresets );
    }
    
    private void updateRaceLengthPercentage()
    {
        if ( getSessionType().isRace() )
        {
            double trackRaceLaps = gameData.getTrackInfo().getRaceLaps();
            if ( trackRaceLaps < 0.0 )
            {
                // We seem to be in editor mode
                trackRaceLaps = 70;
            }
            
            VehicleScoringInfo leader = getLeadersVehicleScoringInfo();
            
            if ( leader.getSessionLimit() == SessionLimit.TIME )
            {
                int raceLaps = getEstimatedMaxLaps( leader );
                
                if ( raceLaps > 0 )
                {
                    double oldRLP = raceLengthPercentage;
                    raceLengthPercentage = ( raceLaps + 1 ) / trackRaceLaps;
                    
                    if ( raceLengthPercentage != oldRLP )
                    {
                        LifetimeManager.INSTANCE.applyActualLifetime( gameData, oldRLP, raceLengthPercentage );
                    }
                }
                else
                {
                    raceLengthPercentage = 1.0;
                }
            }
            else
            {
                double raceLaps = getEstimatedMaxLaps( leader );
                
                raceLengthPercentage = ( raceLaps + 1 ) / trackRaceLaps;
            }
        }
        else
        {
            raceLengthPercentage = 1.0;
        }
    }
    
    void onDataUpdated( EditorPresets editorPresets )
    {
        try
        {
            this.updatedInTimeScope = true;
            this.updateId++;
            this.updateTimestamp = System.nanoTime();
            
            this.sessionBaseNanos = Math.round( ByteUtil.readFloat( buffer, OFFSET_CURRENT_TIME ) * 1000000000.0 );
            updateSessionTime( updateTimestamp );
            
            Arrays.sort( vehicleScoringInfo2, VehicleScoringInfo.VSIPlaceComparator.INSTANCE );
            
            resetDerivateData();
            
            int n = getNumVehicles();
            for ( int i = 0; i < n; i++ )
            {
                getVehicleScoringInfo( i ).updateStintLength();
            }
            
            applyEditorPresets( editorPresets );
            
            if ( sessionJustStarted > 0 )
            {
                updateRaceLengthPercentage();
                
                for ( int i = 0; i < n; i++ )
                {
                    getVehicleScoringInfo( i ).onSessionStarted();
                }
                
                if ( updateListeners != null )
                {
                    for ( int i = 0; i < updateListeners.length; i++ )
                    {
                        try
                        {
                            updateListeners[i].onSessionStarted( gameData, editorPresets );
                        }
                        catch ( Throwable t )
                        {
                            Logger.log( t );
                        }
                    }
                }
                
                this.sessionJustStarted = 0;
            }
            
            if ( getLeadersVehicleScoringInfo().isLapJustStarted() )
            {
                updateRaceLengthPercentage();
            }
            
            
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onScoringInfoUpdated( gameData, editorPresets );
                    }
                    catch ( Throwable t )
                    {
                        Logger.log( t );
                    }
                }
            }
            
            if ( eventsManager != null )
            {
                eventsManager.checkRaceRestart( updateTimestamp );
                eventsManager.checkAndFireOnLapStarted( null );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    final LaptimesRecorder getLaptimesRecorder()
    {
        return ( laptimesRecorder );
    }
    
    /**
     * 
     * @param editorPresets
     */
    final void onSessionStarted( EditorPresets editorPresets )
    {
        this.sessionId++;
        this.sessionStartTimestamp = System.nanoTime();
        this.sessionRunning = true;
        this.sessionJustStarted = 1;
        this.updatedInTimeScope = false;
        
        this.raceLengthPercentage = 1.0; // We don't know it better now!
    }
    
    final void onSessionEnded()
    {
        this.sessionRunning = false;
        this.updatedInTimeScope = false;
        
        for ( int i = 0; i < vehicleScoringInfo.length; i++ )
        {
            vehicleScoringInfo[i].onSessionEnded();
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
    
    final void onRealtimeEntered( EditorPresets editorPresets )
    {
        this.realtimeEnteredTimestamp = System.nanoTime();
        this.realtimeEnteredId++;
        this.updatedInTimeScope = true;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                try
                {
                    updateListeners[i].onRealtimeEntered( gameData, editorPresets );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
    }
    
    final void onRealtimeExited( EditorPresets editorPresets )
    {
        this.updatedInTimeScope = false;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                try
                {
                    updateListeners[i].onRealtimeExited( gameData, editorPresets );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
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
        
        int offset = 0;
        int bytesToRead = BUFFER_SIZE;
        
        while ( bytesToRead > 0 )
        {
            int n = in.read( buffer, offset, bytesToRead );
            
            if ( n < 0 )
                throw new IOException();
            
            offset += n;
            bytesToRead -= n;
        }
        
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
        
        // Add postfixes to some vehicle's classes to get valid class-scoring in the editor.
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
    
    /*
     * ################################
     * ScoringInfoBase
     * ################################
     */
    
    /**
     * current track name
     */
    public final String getTrackName()
    {
        // char mTrackName[64]
        
        if ( trackName == null )
        {
            trackName = ByteUtil.readString( buffer, OFFSET_TRACK_NAME, MAX_TRACK_NAME_LENGTH );
        }
        
        return ( trackName );
    }
    
    /**
     * current session
     */
    public final SessionType getSessionType()
    {
        // long mSession
        
        int st = (int)ByteUtil.readLong( buffer, OFFSET_SESSION_TYPE );
        
        return ( SessionType.values()[st] );
    }
    
    /**
     * current session time
     */
    public final float getSessionTime()
    {
        if ( getGamePhase() == GamePhase.SESSION_OVER )
            return ( 0f );
        
        // float mCurrentET
        
        //return ( ByteUtil.readFloat( buffer, OFFSET_CURRENT_TIME ) );
        return ( sessionTime );
    }
    
    /**
     * session ending time
     */
    public final float getEndTime()
    {
        // float mEndET
        
        return ( ByteUtil.readFloat( buffer, OFFSET_END_TIME ) );
    }
    
    /**
     * maximum laps
     */
    public final int getMaxLaps()
    {
        // long mMaxLaps
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_MAX_LAPS ) );
    }
    
    /**
     * Gets the estimated max laps based on the session end time and average lap time.
     * If the {@link SessionLimit} is defined to be LAPS, then max laps is known and returned.
     * If the current session is a race, the estimated max laps of the leader are returned.
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
     * distance around track
     */
    public final float getTrackLength()
    {
        // float mLapDist
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAP_DISTANCE ) );
    }
    
    //char *mResultsStream;          // results stream additions since last update (newline-delimited and NULL-terminated)
    
    /**
     * current number of vehicles
     */
    public final int getNumVehicles()
    {
        // long mNumVehicles
        
        if ( sessionJustStarted == 1 )
            return ( 0 );
        
        if ( numVehicles == -1 )
            numVehicles = (int)ByteUtil.readLong( buffer, OFFSET_NUM_VEHICLES );
        
        return ( numVehicles );
    }
    
    /**
     * Gets the number of vehicles in the same vehicle class as the given one.
     * 
     * @param vsi
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
    
    /*
     * ################################
     * ScoringInfo
     * ################################
     */
    
    /*
     * array of vehicle scoring info's
     * 
     * @see #getNumVehicles()
     */
    // We shouldn't need this, because VehicleScoringInfoV2 is at the end of the buffer.
    /*
    public final void getVehicleScoringInfos( VehicleScoringInfo[] vsi )
    {
        // VehicleScoringInfo *mVehicle
    }
    */
    
    /*
     * ################################
     * ScoringInfoV2
     * ################################
     */
    
    /**
     * Game phases
     */
    public final GamePhase getGamePhase()
    {
        // unsigned char mGamePhase
        
        short state = ByteUtil.readUnsignedByte( buffer, OFFSET_GAME_PHASE );
        
        switch ( state )
        {
            case 0:
                return ( GamePhase.BEFORE_SESSION_HAS_BEGUN );
            case 1:
                return ( GamePhase.RECONNAISSANCE_LAPS );
            case 2:
                return ( GamePhase.GRID_WALK_THROUGH );
            case 3:
                return ( GamePhase.FORMATION_LAP );
            case 4:
                return ( GamePhase.STARTING_LIGHT_COUNTDOWN_HAS_BEGUN );
            case 5:
                return ( GamePhase.GREEN_FLAG );
            case 6:
                return ( GamePhase.FULL_COURSE_YELLOW );
            case 7:
                return ( GamePhase.SESSION_STOPPED );
            case 8:
                return ( GamePhase.SESSION_OVER );
        }
        
        throw new Error( "Unknown game state read (" + state + ")." );
    }
    
    /**
     * Yellow flag states (applies to full-course only)
     */
    public final YellowFlagState getYellowFlagState()
    {
        // signed char mYellowFlagState
        
        short state = ByteUtil.readByte( buffer, OFFSET_YELLOW_FLAG_STATE );
        
        switch ( state )
        {
            case -1:
                throw new Error( "Invald state detected." );
            case 0:
                return ( YellowFlagState.NONE );
            case 1:
                return ( YellowFlagState.PENDING );
            case 2:
                return ( YellowFlagState.PITS_CLOSED );
            case 3:
                return ( YellowFlagState.PIT_LEAD_LAP );
            case 4:
                return ( YellowFlagState.PITS_OPEN );
            case 5:
                return ( YellowFlagState.LAST_LAP );
            case 6:
                return ( YellowFlagState.RESUME );
            case 7:
                return ( YellowFlagState.RACE_HALT );
        }
        
        throw new Error( "Unknown game state read (" + state + ")." );
    }
    
    /**
     * whether there are any local yellows at the moment in each sector
     * 
     * @param sector
     */
    public final boolean getSectorYellowFlag( int sector )
    {
        // signed char mSectorFlag[3]
        
        if ( ( sector < 1 ) || ( sector > 3 ) )
            throw new IllegalArgumentException( "Sector must be in range [1, 3]" );
        
        short flag = ByteUtil.readByte( buffer, OFFSET_SECTOR_FLAGS + ( sector % 3 ) );
        
        return ( flag != 0 );
    }
    
    /**
     * start light frame (number depends on track)
     */
    public final int getStartLightFrame()
    {
        // unsigned char mStartLight
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_STARTING_LIGHT_FRAME ) );
    }
    
    /**
     * number of red lights in start sequence
     */
    public final int getNumRedLights()
    {
        // unsigned char mNumRedLights
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_NUM_RED_LIGHTS ) );
    }
    
    /**
     * in realtime as opposed to at the monitor
     */
    public final boolean isInRealtimeMode()
    {
        // bool mInRealtime
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_IN_REALTIME ) );
    }
    
    /**
     * player name (including possible multiplayer override)
     */
    public final String getPlayerName()
    {
        // char mPlayerName[32]
        
        if ( playerName == null )
        {
            playerName = ByteUtil.readString( buffer, OFFSET_PLAYER_NAME, MAX_PLAYER_NAME_LENGTH );
        }
        
        return ( playerName );
    }
    
    /**
     * may be encoded to be a legal filename
     */
    public final String getPlayerFilename()
    {
        // char mPlrFileName[64]
        
        if ( playerFilename == null )
        {
            playerFilename = ByteUtil.readString( buffer, OFFSET_PLAYER_FILENAME, MAX_PLAYER_FILENAME_LENGTH );
        }
        
        return ( playerFilename );
    }
    
    // weather
    
    /**
     * cloud darkness? 0.0-1.0
     */
    public final float getCloudDarkness()
    {
        // float mDarkCloud
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CLOUD_DARKNESS ) );
    }
    
    /**
     * raining severity 0.0-1.0
     */
    public final float getRainingSeverity()
    {
        // float mRaining
        
        return ( ByteUtil.readFloat( buffer, OFFSET_RAINING_SEVERITIY ) );
    }
    
    /**
     * temperature (Celsius)
     */
    public final float getAmbientTemperature()
    {
        // float mAmbientTemp
        
        return ( ByteUtil.readFloat( buffer, OFFSET_AMBIENT_TEMPERATURE ) );
    }
    
    /**
     * temperature (Celsius)
     */
    public final float getTrackTemperature()
    {
        // float mTrackTemp
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TRACK_TEMPERATURE ) );
    }
    
    /**
     * wind speed
     */
    public final void getWindSpeed( TelemVect3 speed )
    {
        // TelemVect3 mWind
        
        ByteUtil.readVector( buffer, OFFSET_WIND_SPEED, speed );
    }
    
    /**
     * on main path 0.0-1.0
     */
    public final float getOnPathWetness()
    {
        // float mOnPathWetness
        
        return ( ByteUtil.readFloat( buffer, OFFSET_ON_PATH_WETNESS ) );
    }
    
    /**
     * on main path 0.0-1.0
     */
    public final float getOffPathWetness()
    {
        // float mOffPathWetness
        
        return ( ByteUtil.readFloat( buffer, OFFSET_OFF_PATH_WETNESS ) );
    }
    
    // Future use
    //unsigned char mExpansion[256];
    
    /**
     * array of vehicle scoring info's
     * 
     * @param i
     * 
     * @see #getNumVehicles()
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
     * the player's VehicleScroingInfo
     * 
     * @see #getOwnPlace()
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
    
    private final TelemVect3 camPos = new TelemVect3();
    private final TelemVect3 carPos = new TelemVect3();
    
    /**
     * the viewed's VehicleScroingInfo (this is just a guess, but should be correct).
     */
    public final VehicleScoringInfo getViewedVehicleScoringInfo()
    {
        GraphicsInfo gi = gameData.getGraphicsInfo();
        
        //if ( !gi.isUpdatedInRealtimeMode() )
        //    return ( getPlayersVehicleScoringInfo() );
        
        if ( viewedVSI == null )
        {
            gi.getCameraPosition( camPos );
            camPos.x *= -1f;
            camPos.y *= -1f;
            camPos.z *= -1f;
            
            float closestDist = Float.MAX_VALUE;
            
            int n = getNumVehicles();
            for ( short i = 0; i < n; i++ )
            {
                vehicleScoringInfo2[i].getWorldPosition( carPos );
                
                float dist = carPos.getDistanceToSquared( camPos );
                
                if ( dist < closestDist )
                {
                    closestDist = dist;
                    viewedVSI = vehicleScoringInfo2[i];
                }
            }
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
        return ( playerVSI.getPlace( byClass ) );
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
                if ( fs_ < fs )
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
                if ( fs_ < fs )
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
                if ( fs_ < fs )
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
     * @param sector
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
                
                if ( ( vehicleScoringInfo2.length > 1 ) && ( vehicleScoringInfo2[1].getBestLapTime() > 0f ) )
                {
                    secondFastestLapVSI = vehicleScoringInfo2[1];
                }
                
                return ( fastestLapVSI );
            }
            
            int i0;
            for ( i0 = 0; i0 < vehicleScoringInfo2.length; i0++ )
            {
                float fl_ = vehicleScoringInfo2[i0].getBestLapTime();
                if ( fl_ > 0f )
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
                float fl = fastestLapVSI.getBestLapTime();
                
                for ( int i = i0 + 1; i < vehicleScoringInfo2.length; i++ )
                {
                    float fl_ = vehicleScoringInfo2[i].getBestLapTime();
                    if ( ( fl_ > 0f ) && ( fl_ < fl ) )
                    {
                        secondFastestLapVSI = fastestLapVSI;
                        fastestLapVSI = vehicleScoringInfo2[i];
                        fl = fl_;
                    }
                }
                
                if ( ( secondFastestLapVSI == null ) && ( vehicleScoringInfo2.length > i0 ) )
                {
                    float fl2 = 0f;
                    
                    for ( int i = i0 + 1; i < vehicleScoringInfo2.length; i++ )
                    {
                        float fl_ = vehicleScoringInfo2[i].getBestLapTime();
                        if ( fl_ > 0f )
                        {
                            if ( secondFastestLapVSI == null )
                            {
                                secondFastestLapVSI = vehicleScoringInfo2[i];
                                fl2 = secondFastestLapVSI.getBestLapTime();
                            }
                            else if ( fl_ < fl2 )
                            {
                                secondFastestLapVSI = vehicleScoringInfo2[i];
                                fl2 = fl_;
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
    
    public final Laptime getFastestLaptime()
    {
        //return ( fastestLaptime );
        return ( getFastestLapVSI().getFastestLaptime() );
    }
    
    ScoringInfo( LiveGameData gameData, RFactorEventsManager eventsManager )
    {
        this.gameData = gameData;
        this.eventsManager = eventsManager;
        
        registerListener( laptimesRecorder );
        registerListener( FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER );
        registerListener( TopspeedRecorder.MASTER_TOPSPEED_RECORDER );
    }
}
