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
package net.ctdp.rfdynhud.gamedata.rfactor2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ctdp.rfdynhud.gamedata.ByteUtil;
import net.ctdp.rfdynhud.gamedata.FinishStatus;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.PitState;
import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.StatusFlag;
import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.VehicleControl;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleUpgradePack;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_VehicleScoringInfo extends VehicleScoringInfo
{
    private static final int OFFSET_SLOT_ID = 0;
    private static final int OFFSET_DRIVER_NAME = OFFSET_SLOT_ID + ByteUtil.SIZE_LONG;
    private static final int MAX_DRIVER_NAME_LENGTH = 32;
    private static final int OFFSET_VEHICLE_NAME = OFFSET_DRIVER_NAME + MAX_DRIVER_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int MAX_VEHICLE_NAME_LENGTH = 64;
    
    private static final int OFFSET_TOTAL_LAPS = OFFSET_VEHICLE_NAME + MAX_VEHICLE_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_SECTOR = OFFSET_TOTAL_LAPS + ByteUtil.SIZE_SHORT;
    private static final int OFFSET_FINISH_STATUS = OFFSET_SECTOR + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAP_DISTANCE = OFFSET_FINISH_STATUS + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_PATH_LATERAL = OFFSET_LAP_DISTANCE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_TRACK_EDGE = OFFSET_PATH_LATERAL + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_BEST_SECTOR_1 = OFFSET_TRACK_EDGE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_BEST_SECTOR_2 = OFFSET_BEST_SECTOR_1 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_BEST_LAP_TIME = OFFSET_BEST_SECTOR_2 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LAST_SECTOR_1 = OFFSET_BEST_LAP_TIME + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LAST_SECTOR_2 = OFFSET_LAST_SECTOR_1 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LAST_LAP_TIME = OFFSET_LAST_SECTOR_2 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_CURR_SECTOR_1 = OFFSET_LAST_LAP_TIME + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_CURR_SECTOR_2 = OFFSET_CURR_SECTOR_1 + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_NUM_PITSTOPS = OFFSET_CURR_SECTOR_2 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_NUM_PENALTIES = OFFSET_NUM_PITSTOPS + ByteUtil.SIZE_SHORT;
    
    private static final int OFFSET_IS_PLAYER = OFFSET_NUM_PENALTIES + ByteUtil.SIZE_SHORT;
    private static final int OFFSET_CONTROL = OFFSET_IS_PLAYER + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_IN_PITS = OFFSET_CONTROL + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_PLACE = OFFSET_IN_PITS + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_VEHICLE_CLASS = OFFSET_PLACE + ByteUtil.SIZE_CHAR;
    private static final int MAX_VEHICLE_CLASS_LENGTH = 32;
    
    private static final int OFFSET_TIME_BEHIND_NEXT = OFFSET_VEHICLE_CLASS + MAX_VEHICLE_CLASS_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAPS_BEHIND_NEXT = OFFSET_TIME_BEHIND_NEXT + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_TIME_BEHIND_LEADER = OFFSET_LAPS_BEHIND_NEXT + ByteUtil.SIZE_LONG;
    private static final int OFFSET_LAPS_BEHIND_LEADER = OFFSET_TIME_BEHIND_LEADER + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LAP_START_TIME = OFFSET_LAPS_BEHIND_LEADER + ByteUtil.SIZE_LONG;
    
    private static final int OFFSET_POSITION = OFFSET_LAP_START_TIME + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LOCAL_VELOCITY = OFFSET_POSITION + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_LOCAL_ACCELERATION = OFFSET_LOCAL_VELOCITY + ByteUtil.SIZE_VECTOR3D;
    
    private static final int OFFSET_ORIENTATION_X = OFFSET_LOCAL_ACCELERATION + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_ORIENTATION_Y = OFFSET_ORIENTATION_X + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_ORIENTATION_Z = OFFSET_ORIENTATION_Y + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_LOCAL_ROTATION = OFFSET_ORIENTATION_Z + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_LOCAL_ROTATION_ACCELERATION = OFFSET_LOCAL_ROTATION + ByteUtil.SIZE_VECTOR3D;
    
    private static final int OFFSET_HEADLIGHTS = OFFSET_LOCAL_ROTATION_ACCELERATION + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_PIT_STATE = OFFSET_HEADLIGHTS + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_SERVER_SCORED = OFFSET_PIT_STATE + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_INDIVIDUAL_PHASE = OFFSET_SERVER_SCORED + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_QUALIFICATION = OFFSET_INDIVIDUAL_PHASE + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_TIME_INTO_LAP = OFFSET_QUALIFICATION + ByteUtil.SIZE_LONG;
    private static final int OFFSET_ESTIMATED_LAP_TIME = OFFSET_TIME_INTO_LAP + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_PIT_GROUP = OFFSET_ESTIMATED_LAP_TIME + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_FLAG = OFFSET_PIT_GROUP + ByteUtil.SIZE_CHAR * 24;
    
    private static final int OFFSET_UNUSED1 = OFFSET_FLAG + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_UNUSED2 = OFFSET_UNUSED1 + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_UNUSED3 = OFFSET_UNUSED2 + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_UPGRADE_PACK = OFFSET_UNUSED3 + ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_EXPANSION = OFFSET_UPGRADE_PACK + ByteUtil.SIZE_CHAR * 16;
    
    static final int BUFFER_SIZE = OFFSET_EXPANSION + 60 * ByteUtil.SIZE_CHAR; // 584
    
    final byte[] buffer;
    private final int buffOff;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer refreshIDImpl( int index )
    {
        // Unfortunately the slot-ID is not really unique, since it gets reused after some leaves according to the API.
        
        return ( getSlotId() );
    }
    
    private void readFromStreamImpl( InputStream in ) throws IOException
    {
        int offset = 0;
        int bytesToRead = BUFFER_SIZE;
        
        while ( bytesToRead > 0 )
        {
            int n = in.read( buffer, buffOff + offset, bytesToRead );
            
            if ( n < 0 )
                throw new IOException();
            
            offset += n;
            bytesToRead -= n;
        }
    }
    
    public void readFromStream( InputStream in ) throws IOException
    {
        readFromStreamImpl( in );
        
        onDataUpdated( System.nanoTime() );
    }
    
    public void writeToStream( OutputStream out ) throws IOException
    {
        out.write( buffer, buffOff, BUFFER_SIZE );
    }
    
    /*
     * ################################
     * VehicleScoringV01
     * ################################
     */
    
    /**
     * @return slot ID (note that it can be re-used in multiplayer after someone leaves)
     */
    public final int getSlotId()
    {
        // float mID
        
        return ( (int)ByteUtil.readLong( buffer, buffOff + OFFSET_SLOT_ID ) );
    }
    
    /*
     * ################################
     * VehicleScoringInfo
     * ################################
     */
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final String getDriverNameImpl()
    {
        // char mDriverName[32]
        
        return ( ByteUtil.readString( buffer, buffOff + OFFSET_DRIVER_NAME, MAX_DRIVER_NAME_LENGTH ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final String getVehicleNameImpl()
    {
        // char mVehicleName[64]
        
        return ( ByteUtil.readString( buffer, buffOff + OFFSET_VEHICLE_NAME, MAX_VEHICLE_NAME_LENGTH ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final short getLapsCompleted()
    {
        // short mTotalLaps
        
        return ( ByteUtil.readShort( buffer, buffOff + OFFSET_TOTAL_LAPS ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final byte getSector()
    {
        // signed char mSector
        
        byte sector = (byte)( ByteUtil.readByte( buffer, buffOff + OFFSET_SECTOR ) + 1 );
        
        if ( sector == 1 )
            return ( 3 );
        
        return ( (byte)( sector - 1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final FinishStatus getFinishStatus()
    {
        // signed char mFinishStatus
        
        short state = ByteUtil.readByte( buffer, buffOff + OFFSET_FINISH_STATUS );
        
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
     * {@inheritDoc}
     */
    @Override
    protected final float getLastKnownLapDistance()
    {
        // double mLapDist
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_LAP_DISTANCE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getPathLateral()
    {
        // double mPathLateral
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_PATH_LATERAL ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTrackEdge()
    {
        // double mTrackEdge
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_TRACK_EDGE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getBestSector1Impl()
    {
        // double mBestSector1
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_BEST_SECTOR_1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getBestSector2Impl()
    {
        // double mBestSector2
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_BEST_SECTOR_2 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getBestLapTimeImpl()
    {
        // double mBestLapTime
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_BEST_LAP_TIME ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getLastSector1Impl()
    {
        // double mLastSector1
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_LAST_SECTOR_1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getLastSector2Impl()
    {
        // double mLastSector2
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_LAST_SECTOR_2 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getLastLapTimeImpl()
    {
        // double mLastLapTime
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_LAST_LAP_TIME ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getCurrentSector1Impl()
    {
        // double mCurSector1
        
        // TODO: Check result, if sector1 is invalid
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_CURR_SECTOR_1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getCurrentSector2Impl()
    {
        // double mCurSector2
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_CURR_SECTOR_2 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final short getNumPitstopsMade()
    {
        // short mNumPitstops
        
        return ( ByteUtil.readShort( buffer, buffOff + OFFSET_NUM_PITSTOPS ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final short getNumOutstandingPenalties()
    {
        // short mNumPenalties
        
        return ( ByteUtil.readShort( buffer, buffOff + OFFSET_NUM_PENALTIES ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isPlayer()
    {
        // bool mIsPlayer
        
        return ( ByteUtil.readBoolean( buffer, buffOff + OFFSET_IS_PLAYER ) );
    }
    
    private static final VehicleControl convertVehicleControl( byte isi_value )
    {
        switch ( isi_value )
        {
            case -1:
                return ( VehicleControl.NOBODY );
            case 0:
                return ( VehicleControl.LOCAL_PLAYER );
            case 1:
                return ( VehicleControl.LOCAL_AI );
            case 2:
                return ( VehicleControl.REMOTE );
            case 3:
                return ( VehicleControl.REPLAY );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final VehicleControl getVehicleControl()
    {
        // signed char mControl
        
        byte control = ByteUtil.readByte( buffer, buffOff + OFFSET_CONTROL );
        VehicleControl vc = convertVehicleControl( control );
        
        if ( vc == null )
            throw new Error( "Unknown control id read (" + control + ")." );
        
        return ( vc );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isInPits()
    {
        // bool mInPits
        
        return ( ByteUtil.readBoolean( buffer, buffOff + OFFSET_IN_PITS ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final short getPlaceImpl()
    {
        // unsigned char mPlace
        
        return ( ByteUtil.readUnsignedByte( buffer, buffOff + OFFSET_PLACE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final String getVehicleClassImpl()
    {
        // char mVehicleClass[32]
        
        return ( ByteUtil.readString( buffer, buffOff + OFFSET_VEHICLE_CLASS, MAX_VEHICLE_CLASS_LENGTH ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getTimeBehindNextInFrontImpl()
    {
        // double mTimeBehindNext
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_TIME_BEHIND_NEXT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final int getLapsBehindNextInFrontImpl()
    {
        // long mLapsBehindNext
        
        return ( (int)ByteUtil.readLong( buffer, buffOff + OFFSET_LAPS_BEHIND_NEXT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getTimeBehindLeaderImpl()
    {
        // double mTimeBehindLeader
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_TIME_BEHIND_LEADER ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final int getLapsBehindLeaderImpl()
    {
        // long mLapsBehindLeader
        
        return ( (int)ByteUtil.readLong( buffer, buffOff + OFFSET_LAPS_BEHIND_LEADER ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getLapStartTime()
    {
        // double mLapStartET
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_LAP_START_TIME ) );
    }
    
    // Position and derivatives
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getWorldPosition( TelemVect3 position )
    {
        // TelemVect3 mPos
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_POSITION, position );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWorldPositionX()
    {
        // TelemVect3 mPos
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_POSITION + 0 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWorldPositionY()
    {
        // TelemVect3 mPos
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_POSITION + 1 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWorldPositionZ()
    {
        // TelemVect3 mPos
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_POSITION + 2 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalVelocity( TelemVect3 localVel )
    {
        // TelemVect3 mLocalVel
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_LOCAL_VELOCITY, localVel );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getScalarVelocityMS()
    {
        double vecX = ByteUtil.readDouble( buffer, buffOff + OFFSET_LOCAL_VELOCITY + 0 * ByteUtil.SIZE_DOUBLE );
        double vecY = ByteUtil.readDouble( buffer, buffOff + OFFSET_LOCAL_VELOCITY + 1 * ByteUtil.SIZE_DOUBLE );
        double vecZ = ByteUtil.readDouble( buffer, buffOff + OFFSET_LOCAL_VELOCITY + 2 * ByteUtil.SIZE_DOUBLE );
        
        return ( (float)Math.sqrt( vecX * vecX + vecY * vecY + vecZ * vecZ ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalAcceleration( TelemVect3 localAccel )
    {
        // TelemVect3 mLocalAccel
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_LOCAL_ACCELERATION, localAccel );
    }
    
    // Orientation and derivatives
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getOrientationX( TelemVect3 oriX )
    {
        // TelemVect3 mOriX
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_ORIENTATION_X, oriX );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getOrientationY( TelemVect3 oriY )
    {
        // TelemVect3 mOriY
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_ORIENTATION_Y, oriY );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getOrientationZ( TelemVect3 oriZ )
    {
        // TelemVect3 mOriZ
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_ORIENTATION_Z, oriZ );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalRotation( TelemVect3 localRot )
    {
        // TelemVect3 mLocalRot
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_LOCAL_ROTATION, localRot );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        // TelemVect3 mLocalRotAccel
        
        ByteUtil.readVectorD( buffer, buffOff + OFFSET_LOCAL_ROTATION_ACCELERATION, localRotAccel );
    }
    
    
    
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final boolean getHeadlightsState()
    {
        // unsigned char mHeadlights;     // status of headlights
        
        return ( ByteUtil.readBoolean( buffer, buffOff + OFFSET_HEADLIGHTS ) );
    }
    
    /*
     * {@inheritDoc}
     */
    @Override
    public final PitState getPitState()
    {
        // unsigned char mPitState;
        
        byte value = ByteUtil.readByte( buffer, buffOff + OFFSET_PIT_STATE );
        
        switch ( value )
        {
            case 0:
                return ( PitState.NONE );
            case 1:
                return ( PitState.REQUESTED );
            case 2:
                return ( PitState.ENTERING );
            case 3:
                return ( PitState.STOPPED );
            case 4:
                return ( PitState.EXITING );
        }
        
        return ( null );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final boolean isServerScored()
    {
        // unsigned char mServerScored;   // whether this vehicle is being scored by server (could be off in qualifying or racing heats)
        
        return ( ByteUtil.readBoolean( buffer, buffOff + OFFSET_SERVER_SCORED ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final byte isIndividualPhase()
    {
        // unsigned char mIndividualPhase;// game phases (described below) plus 9=after formation, 10=under yellow, 11=under blue (not used)
        
        return ( ByteUtil.readByte( buffer, buffOff + OFFSET_INDIVIDUAL_PHASE ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final long getQualification()
    {
        // long mQualification;           // 1-based, can be -1 when invalid
        
        return ( ByteUtil.readLong( buffer, buffOff + OFFSET_QUALIFICATION ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final float getTimeIntoLap()
    {
        // double mTimeIntoLap;           // estimated time into lap
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_TIME_INTO_LAP ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final float getEstimatedLapTime()
    {
        // double mEstimatedLapTime;      // estimated laptime used for 'time behind' and 'time into lap' (note: this may changed based on vehicle and setup!?)
        
        return ( (float)ByteUtil.readDouble( buffer, buffOff + OFFSET_ESTIMATED_LAP_TIME ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final String getPitGroup()
    {
        // char mPitGroup[24];            // pit group (same as team name unless pit is shared)
        
        return ( ByteUtil.readString( buffer, buffOff + OFFSET_PIT_GROUP, 24 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final StatusFlag getStatusFlag()
    {
        // unsigned char mFlag;           // primary flag being shown to vehicle (currently only 0=green or 6=blue)
        
        byte value = ByteUtil.readByte( buffer, buffOff + OFFSET_FLAG );
        
        switch ( value )
        {
            case 0:
                return ( StatusFlag.GREEN );
            case 6:
                return ( StatusFlag.BLUE );
        }
        
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final VehicleUpgradePack getUpgradePack()
    {
        // unsigned char mUpgradePack[16];  // Coded upgrades
        
        final String upgrade_name = ByteUtil.readString( buffer, buffOff + OFFSET_PIT_GROUP, 16 );
        
        return ( new VehicleUpgradePack()
        {
            @Override
            public String getName()
            {
                return ( upgrade_name );
            }
        } );
    }
    
    _rf2_VehicleScoringInfo( ScoringInfo scoringInfo, ProfileInfo profileInfo, LiveGameData gameData, byte[] buffer, int buffOff )
    {
        super( scoringInfo, profileInfo, gameData );
        
        this.buffer = buffer;
        this.buffOff = buffOff;
    }
}
