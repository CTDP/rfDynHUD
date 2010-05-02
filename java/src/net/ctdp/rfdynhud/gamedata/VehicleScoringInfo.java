package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;

/**
 * 
 * @author Marvin Froehlich
 */
public class VehicleScoringInfo
{
    private static final int OFFSET_DRIVER_NAME = 0;
    private static final int OFFSET_VEHICLE_NAME = OFFSET_DRIVER_NAME + 32 * ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_TOTAL_LAPS = OFFSET_VEHICLE_NAME + 64 * ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_SECTOR = OFFSET_TOTAL_LAPS + ByteUtil.SIZE_SHORT;
    private static final int OFFSET_FINISH_STATUS = OFFSET_SECTOR + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAP_DISTANCE = OFFSET_FINISH_STATUS + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_PATH_LATERAL = OFFSET_LAP_DISTANCE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TRACK_EDGE = OFFSET_PATH_LATERAL + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_BEST_SECTOR_1 = OFFSET_TRACK_EDGE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_BEST_SECTOR_2 = OFFSET_BEST_SECTOR_1 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_BEST_LAP_TIME = OFFSET_BEST_SECTOR_2 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAST_SECTOR_1 = OFFSET_BEST_LAP_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAST_SECTOR_2 = OFFSET_LAST_SECTOR_1 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAST_LAP_TIME = OFFSET_LAST_SECTOR_2 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_CURR_SECTOR_1 = OFFSET_LAST_LAP_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_CURR_SECTOR_2 = OFFSET_CURR_SECTOR_1 + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_NUM_PITSTOPS = OFFSET_CURR_SECTOR_2 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_NUM_PENALTIES = OFFSET_NUM_PITSTOPS + ByteUtil.SIZE_SHORT;
    
    private static final int OFFSET_IS_PLAYER = OFFSET_NUM_PENALTIES + ByteUtil.SIZE_SHORT;
    private static final int OFFSET_CONTROL = OFFSET_IS_PLAYER + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_IN_PITS = OFFSET_CONTROL + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_PLACE = OFFSET_IN_PITS + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_VEHICLE_CLASS = OFFSET_PLACE + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_TIME_BEHIND_NEXT = OFFSET_VEHICLE_CLASS + 32 * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAPS_BEHIND_NEXT = OFFSET_TIME_BEHIND_NEXT + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TIME_BEHIND_LEADER = OFFSET_LAPS_BEHIND_NEXT + ByteUtil.SIZE_LONG;
    private static final int OFFSET_LAPS_BEHIND_LEADER = OFFSET_TIME_BEHIND_LEADER + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAP_START_TIME = OFFSET_LAPS_BEHIND_LEADER + ByteUtil.SIZE_LONG;
    
    private static final int OFFSET_POSITION = OFFSET_LAP_START_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LOCAL_VELOCITY = OFFSET_POSITION + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ACCELERATION = OFFSET_LOCAL_VELOCITY + ByteUtil.SIZE_VECTOR3;
    
    private static final int OFFSET_ORIENTATION_X = OFFSET_LOCAL_ACCELERATION + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_ORIENTATION_Y = OFFSET_ORIENTATION_X + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_ORIENTATION_Z = OFFSET_ORIENTATION_Y + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ROTATION = OFFSET_ORIENTATION_Z + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ROTATION_ACCELERATION = OFFSET_LOCAL_ROTATION + ByteUtil.SIZE_VECTOR3;
    
    private static final int OFFSET_EXPANSION = OFFSET_LOCAL_ROTATION_ACCELERATION + ByteUtil.SIZE_VECTOR3;
    
    static final int BUFFER_SIZE = OFFSET_EXPANSION + 128 * ByteUtil.SIZE_CHAR;
    
    final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private final ScoringInfo scoringInfo;
    
    private String name = null;
    private static int nextId = 1;
    private int id = 0;
    private Integer id2 = null;
    
    private int stintStartLap = -1;
    private float stintLength = 0f;
    
    ArrayList<Laptime> laptimes = null;
    Laptime fastestLaptime = null;
    
    float topspeed = 0f;
    
    private static final HashMap<String, Integer> nameToIDMap = new HashMap<String, Integer>();
    
    private void updateID()
    {
        String name = getDriverName();
        
        Integer id = nameToIDMap.get( name );
        if ( id == null )
        {
            id = nextId++;
            nameToIDMap.put( name, id );
        }
        
        this.id = id.intValue();
        this.id2 = id;
    }
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
        
        if ( isPlayer() )
            name = editorPresets.getDriverName();
        
        updateID();
        
        topspeed = editorPresets.getTopSpeed( getPlace() - 1 );
    }
    
    void onDataUpdated()
    {
        name = null;
        id = 0;
        id2 = null;
    }
    
    void loadFromStream( InputStream in ) throws IOException
    {
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
        
        onDataUpdated();
    }
    
    /*
     * ################################
     * VehicleScoringInfo
     * ################################
     */
    
    public final String getDriverName()
    {
        // char mDriverName[32]
        
        if ( name == null )
        {
            name = ByteUtil.readString( buffer, OFFSET_DRIVER_NAME, 32 );
        }
        
        return ( name );
    }
    
    /**
     * driver name (three letter code)
     */
    public final String getDriverNameTLC()
    {
        return ( ThreeLetterCodeManager.getThreeLetterCode( getDriverName() ) );
    }
    
    /**
     * driver name (short form)
     */
    public final String getDriverNameShort()
    {
        return ( ThreeLetterCodeManager.getShortForm( getDriverName() ) );
    }
    
    public final int getDriverId()
    {
        if ( id <= 0 )
        {
            updateID();
        }
        
        return ( id );
    }
    
    public final Integer getDriverID()
    {
        if ( id2 == null )
        {
            updateID();
        }
        
        return ( id2 );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( getDriverId() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof VehicleScoringInfo ) )
            return ( false );
        
        return ( this.getDriverId() == ( (VehicleScoringInfo)o ).getDriverId() );
    }
    
    /**
     * vehicle name
     */
    public final String getVehicleName()
    {
        // char mVehicleName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_VEHICLE_NAME, 64 ) );
    }
    
    /**
     * laps completed
     */
    public final short getLapsCompleted()
    {
        // short mTotalLaps
        
        return ( ByteUtil.readShort( buffer, OFFSET_TOTAL_LAPS ) );
    }
    
    public final short getCurrentLap()
    {
        if ( isInPits() && ( getStintLength() < 0.5f ) )
            return ( getLapsCompleted() );
        
        return ( (short)( getLapsCompleted() + 1 ) );
    }
    
    /**
     * sector
     */
    public final short getSector()
    {
        // signed char mSector
        
        short sector = (short)( ByteUtil.readByte( buffer, OFFSET_SECTOR ) + 1 );
        
        if ( sector == 1 )
            return ( 3 );
        
        return ( (short)( sector - 1 ) );
    }
    
    /**
     * finish status
     */
    public final FinishStatus getFinishStatus()
    {
        // signed char mFinishStatus
        
        short state = ByteUtil.readByte( buffer, OFFSET_FINISH_STATUS );
        
        switch ( state )
        {
            case 0:
                return ( FinishStatus.NONE );
            case 1:
                return ( FinishStatus.FINISHED );
            case 2:
                return ( FinishStatus.DNF );
            case 3:
                return ( FinishStatus.DQ );
        }
        
        throw new Error( "Unknown finish status read (" + state + ")." );
    }
    
    /**
     * current distance around track
     */
    public final float getLapDistance()
    {
        // float mLapDist
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAP_DISTANCE ) );
    }
    
    void setStintLength( int stintStartLap, float stintLength )
    {
        this.stintStartLap = stintStartLap;
        this.stintLength = stintLength;
    }
    
    public final int getStintStartLap()
    {
        return ( stintStartLap );
    }
    
    public final float getStintLength()
    {
        return ( stintLength );
    }
    
    /**
     * Gets the Laptime object for the given lap.
     * 
     * @param lap
     * 
     * @return the Laptime object for the given lap.
     */
    public final Laptime getLaptime( int lap )
    {
        if ( ( lap < 1 ) || ( laptimes == null ) || ( lap > laptimes.size() ) )
            return ( null );
        
        return ( laptimes.get( lap - 1 ) );
    }
    
    /**
     * Gets this driver's fastest {@link Laptime}.
     * 
     * @return this driver's fastest {@link Laptime}.
     */
    public final Laptime getFastestLaptime()
    {
        return ( fastestLaptime );
    }
    
    /**
     * lateral position with respect to *very approximate* "center" path
     */
    public final float getPathLateral()
    {
        // float mPathLateral
        
        return ( ByteUtil.readFloat( buffer, OFFSET_PATH_LATERAL ) );
    }
    
    /**
     * track edge (w.r.t. "center" path) on same side of track as vehicle
     */
    public final float getTrackEdge()
    {
        // float mTrackEdge
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TRACK_EDGE ) );
    }
    
    /**
     * best sector 1
     */
    public final float getBestSector1()
    {
        // float mBestSector1
        
        return ( ByteUtil.readFloat( buffer, OFFSET_BEST_SECTOR_1 ) );
    }
    
    /**
     * best sector 2
     * 
     * @param includingSector1
     */
    public final float getBestSector2( boolean includingSector1 )
    {
        // float mBestSector2
        
        float sec2 = ByteUtil.readFloat( buffer, OFFSET_BEST_SECTOR_2 );
        
        if ( !includingSector1 )
            sec2 -= getBestSector1();
        
        return ( sec2 );
    }
    
    /**
     * best lap time
     */
    public final float getBestLapTime()
    {
        // float mBestLapTime
        
        return ( ByteUtil.readFloat( buffer, OFFSET_BEST_LAP_TIME ) );
    }
    
    /**
     * best sector 3
     */
    public final float getBestSector3()
    {
        return ( getBestLapTime() - getBestSector2( true ) );
    }
    
    /**
     * last sector 1
     */
    public final float getLastSector1()
    {
        // float mLastSector1
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAST_SECTOR_1 ) );
    }
    
    /**
     * last sector 2
     * 
     * @param includingSector1
     */
    public final float getLastSector2( boolean includingSector1 )
    {
        // float mLastSector2
        
        float sec2 = ByteUtil.readFloat( buffer, OFFSET_LAST_SECTOR_2 );
        
        if ( !includingSector1 )
            sec2 -= getLastSector1();
        
        return ( sec2 );
    }
    
    /**
     * last lap time
     */
    public final float getLastLapTime()
    {
        // float mLastLapTime
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAST_LAP_TIME ) );
    }
    
    /**
     * last sector 3
     */
    public final float getLastSector3()
    {
        return ( getLastLapTime() - getLastSector2( true ) );
    }
    
    /**
     * current sector 1 (if valid)
     */
    public final float getCurrentSector1()
    {
        // float mCurSector1
        
        // TODO: Check result, if sector1 is invalid
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CURR_SECTOR_1 ) );
    }
    
    /**
     * current sector 2
     * 
     * @param includingSector1 only affects result if sector1 is valid
     */
    public final float getCurrentSector2( boolean includingSector1 )
    {
        // float mCurSector2
        
        float sec2 = ByteUtil.readFloat( buffer, OFFSET_CURR_SECTOR_2 );
        
        if ( !includingSector1 && ( sec2 > 0f ) )
            sec2 -= getCurrentSector1();
        
        return ( sec2 );
    }
    
    /**
     * The current laptime (may be incomplete).
     * 
     * @return current laptime.
     */
    public final float getCurrentLaptime()
    {
        if ( getStintLength() < 1.0f )
            return ( -1f );
        
        if ( !scoringInfo.getSessionType().isRace() && ( getStintLength() < 1.0f ) )
            return ( -1f );
        
        return ( scoringInfo.getSessionTime() - getLapStartTime() );
    }
    
    /**
     * number of pitstops made
     */
    public final short getNumPitstopsMade()
    {
        // short mNumPitstops
        
        return ( ByteUtil.readShort( buffer, OFFSET_NUM_PITSTOPS ) );
    }
    
    /**
     * number of outstanding penalties
     */
    public final short getNumOutstandingPenalties()
    {
        // short mNumPenalties
        
        return ( ByteUtil.readShort( buffer, OFFSET_NUM_PENALTIES ) );
    }
    
    /*
     * ################################
     * VehicleScoringInfoV2
     * ################################
     */
    
    /**
     * is this the player's vehicle?
     */
    public final boolean isPlayer()
    {
        // bool mIsPlayer
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_IS_PLAYER ) );
    }
    
    /**
     * who's in control?
     */
    public final VehicleControl getVehicleControl()
    {
        // signed char mControl
        
        short control = ByteUtil.readByte( buffer, OFFSET_CONTROL );
        
        switch ( control )
        {
            case -1: // (shouldn't get this)
                return ( VehicleControl.NOBODY );
            case 0:
                return ( VehicleControl.LOCAL_PLAYER );
            case 1:
                return ( VehicleControl.LOCAL_AI );
            case 2:
                return ( VehicleControl.REMOTE );
            case 3: // (shouldn't get this)
                return ( VehicleControl.REPLAY );
        }
        
        throw new Error( "Unknown control id read (" + control + ")." );
    }
    
    /**
     * between pit entrance and pit exit (not always accurate for remote vehicles)
     */
    public final boolean isInPits()
    {
        // bool mInPits
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_IN_PITS ) );
    }
    
    /**
     * 1-based position
     */
    public final short getPlace()
    {
        // unsigned char mPlace
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_PLACE ) );
    }
    
    /**
     * vehicle class
     */
    public final String getVehicleClass()
    {
        // char mVehicleClass[32]
        
        return ( ByteUtil.readString( buffer, OFFSET_VEHICLE_CLASS, 32 ) );
    }
    
    /**
     * time behind vehicle in next higher place
     */
    public final float getTimeBehindNextInFront()
    {
        // float mTimeBehindNext
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TIME_BEHIND_NEXT ) );
    }
    
    /**
     * laps behind vehicle in next higher place
     */
    public final int getLapsBehindNextInFront()
    {
        // long mLapsBehindNext
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_LAPS_BEHIND_NEXT ) );
    }
    
    /**
     * time behind leader
     */
    public final float getTimeBehindLeader()
    {
        // float mTimeBehindLeader
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TIME_BEHIND_LEADER ) );
    }
    
    /**
     * laps behind leader
     */
    public final int getLapsBehindLeader()
    {
        // long mLapsBehindLeader
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_LAPS_BEHIND_LEADER ) );
    }
    
    /**
     * time this lap was started
     */
    public final float getLapStartTime()
    {
        // float mLapStartET
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAP_START_TIME ) );
    }
    
    // Position and derivatives
    
    /**
     * world position in meters
     * 
     * @param position
     */
    public final void getWorldPosition( TelemVect3 position )
    {
        // TelemVect3 mPos
        
        ByteUtil.readVector( buffer, OFFSET_POSITION, position );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionX()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 0 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionY()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 1 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionZ()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 2 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel
     */
    public final void getLocalVelocity( TelemVect3 localVel )
    {
        // TelemVect3 mLocalVel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_VELOCITY, localVel );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     */
    public final float getScalarVelocity()
    {
        float vecX = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 0 * ByteUtil.SIZE_FLOAT );
        float vecY = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 1 * ByteUtil.SIZE_FLOAT );
        float vecZ = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 2 * ByteUtil.SIZE_FLOAT );
        
        return ( (float)Math.sqrt( vecX * vecX + vecY * vecY + vecZ * vecZ ) );
    }
    
    /**
     * velocity (km/h) in local vehicle coordinates
     */
    public final float getScalarVelocityKPH()
    {
        float mps = getScalarVelocity();
        
        //return ( mps * 3600f / 1000f );
        return ( mps * 3.6f );
    }
    
    /**
     * topspeed in km/h
     */
    public final float getTopspeed()
    {
        return ( topspeed );
    }
    
    /**
     * acceleration (meters/sec^2) in local vehicle coordinates
     * 
     * @param localAccel
     */
    public final void getLocalAcceleration( TelemVect3 localAccel )
    {
        // TelemVect3 mLocalAccel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ACCELERATION, localAccel );
    }
    
    // Orientation and derivatives
    
    /**
     * top row of orientation matrix (also converts local vehicle vectors into world X using dot product)
     * 
     * @param oriX
     */
    public final void getOrientationX( TelemVect3 oriX )
    {
        // TelemVect3 mOriX
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_X, oriX );
    }
    
    /**
     * mid row of orientation matrix (also converts local vehicle vectors into world Y using dot product)
     * 
     * @param oriY
     */
    public final void getOrientationY( TelemVect3 oriY )
    {
        // TelemVect3 mOriY
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_Y, oriY );
    }
    
    /**
     * bot row of orientation matrix (also converts local vehicle vectors into world Z using dot product)
     * 
     * @param oriZ
     */
    public final void getOrientationZ( TelemVect3 oriZ )
    {
        // TelemVect3 mOriZ
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_Z, oriZ );
    }
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot
     */
    public final void getLocalRotation( TelemVect3 localRot )
    {
        // TelemVect3 mLocalRot
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ROTATION, localRot );
    }
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel
     */
    public final void getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        // TelemVect3 mLocalRotAccel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ROTATION_ACCELERATION, localRotAccel );
    }
    
    // Future use
    //unsigned char mExpansion[128];
    
    VehicleScoringInfo( ScoringInfo scoringInfo )
    {
        this.scoringInfo = scoringInfo;
    }
    
    public static final class VSIPlaceComparator implements Comparator<VehicleScoringInfo>
    {
        public static final VSIPlaceComparator INSTANCE = new VSIPlaceComparator();
        
        public int compare( VehicleScoringInfo vsi1, VehicleScoringInfo vsi2 )
        {
            if ( vsi1.getPlace() < vsi2.getPlace() )
                return ( -1 );
            
            if ( vsi1.getPlace() > vsi2.getPlace() )
                return ( +1 );
            
            return ( 0 );
        }
        
        private VSIPlaceComparator()
        {
        }
    }
}
