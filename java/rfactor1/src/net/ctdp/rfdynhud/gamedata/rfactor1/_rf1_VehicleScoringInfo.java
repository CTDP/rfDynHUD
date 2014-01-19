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
package net.ctdp.rfdynhud.gamedata.rfactor1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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
public class _rf1_VehicleScoringInfo extends VehicleScoringInfo
{
    private static final int OFFSET_DRIVER_NAME = 0;
    private static final int MAX_DRIVER_NAME_LENGTH = 32;
    private static final int OFFSET_VEHICLE_NAME = OFFSET_DRIVER_NAME + MAX_DRIVER_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int MAX_VEHICLE_NAME_LENGTH = 64;
    
    private static final int OFFSET_TOTAL_LAPS = OFFSET_VEHICLE_NAME + MAX_VEHICLE_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    
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
    private static final int MAX_VEHICLE_CLASS_LENGTH = 32;
    
    private static final int OFFSET_TIME_BEHIND_NEXT = OFFSET_VEHICLE_CLASS + MAX_VEHICLE_CLASS_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAPS_BEHIND_NEXT = OFFSET_TIME_BEHIND_NEXT + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TIME_BEHIND_LEADER = OFFSET_LAPS_BEHIND_NEXT + ByteUtil.SIZE_LONG;
    private static final int OFFSET_LAPS_BEHIND_LEADER = OFFSET_TIME_BEHIND_LEADER + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAP_START_TIME = OFFSET_LAPS_BEHIND_LEADER + ByteUtil.SIZE_LONG;
    
    private static final int OFFSET_POSITION = OFFSET_LAP_START_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LOCAL_VELOCITY = OFFSET_POSITION + ByteUtil.SIZE_VECTOR3F;
    private static final int OFFSET_LOCAL_ACCELERATION = OFFSET_LOCAL_VELOCITY + ByteUtil.SIZE_VECTOR3F;
    
    private static final int OFFSET_ORIENTATION_X = OFFSET_LOCAL_ACCELERATION + ByteUtil.SIZE_VECTOR3F;
    private static final int OFFSET_ORIENTATION_Y = OFFSET_ORIENTATION_X + ByteUtil.SIZE_VECTOR3F;
    private static final int OFFSET_ORIENTATION_Z = OFFSET_ORIENTATION_Y + ByteUtil.SIZE_VECTOR3F;
    private static final int OFFSET_LOCAL_ROTATION = OFFSET_ORIENTATION_Z + ByteUtil.SIZE_VECTOR3F;
    private static final int OFFSET_LOCAL_ROTATION_ACCELERATION = OFFSET_LOCAL_ROTATION + ByteUtil.SIZE_VECTOR3F;
    
    private static final int OFFSET_EXPANSION = OFFSET_LOCAL_ROTATION_ACCELERATION + ByteUtil.SIZE_VECTOR3F;
    
    static final int BUFFER_SIZE = OFFSET_EXPANSION + 128 * ByteUtil.SIZE_CHAR;
    
    final byte[] buffer;
    private final int buffOff;
    
    private static final Map<Object, Integer> idMap = new HashMap<Object, Integer>();
    private static int nextId = 1;
    
    private class NameHashItem
    {
        private int hash = 0;
        private boolean hasHash = false;
        
        @Override
        public int hashCode()
        {
            if ( !hasHash )
            {
                hash = 0;
                
                for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
                {
                    int ch = buffer[buffOff + OFFSET_DRIVER_NAME + i] & 0xFF;
                    if ( ch == 0 )
                        break;
                    
                    hash = 31 * hash + ch;
                }
                
                hasHash = true;
            }
            
            return ( hash );
        }
        
        private byte[] getBuffer()
        {
            return ( _rf1_VehicleScoringInfo.this.buffer );
        }
        
        private int getBufferOffset()
        {
            return ( _rf1_VehicleScoringInfo.this.buffOff );
        }
        
        @Override
        public boolean equals( Object o )
        {
            if ( !( o instanceof NameHashItem ) )
                return ( false );
            
            NameHashItem that = (NameHashItem)o;
            
            byte[] thisBuff = this.getBuffer();
            byte[] thatBuff = that.getBuffer();
            int thisBuffOff = this.getBufferOffset();
            int thatBuffOff = that.getBufferOffset();
            
            for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
            {
                byte ch1 = thisBuff[thisBuffOff + i];
                byte ch2 = thatBuff[thatBuffOff + i];
                
                if ( ch1 != ch2 )
                    return ( false );
                
                if ( ch1 == (byte)0 )
                    break;
            }
            
            return ( true );
        }
    }
    
    private final NameHashItem nameHashItem = new NameHashItem();
    
    private Integer refreshID()
    {
        Integer id = idMap.get( nameHashItem );
        if ( id == null )
        {
            id = nextId++;
            idMap.put( nameHashItem, id );
        }
        
        return ( id );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Integer refreshIDImpl( int index )
    {
        Integer id = refreshID();
        
        if ( index == 0 )
        {
            ( (_rf1_ScoringInfo)scoringInfo ).idVSIMap.clear();
            ( (_rf1_ScoringInfo)scoringInfo ).nameDuplicatesMap.clear();
        }
        
        int orgNameLength = getDriverNameLength();
        
        while ( ( (_rf1_ScoringInfo)scoringInfo ).idVSIMap.containsKey( id ) )
        {
            Integer pf = ( (_rf1_ScoringInfo)scoringInfo ).nameDuplicatesMap.get( id );
            if ( pf == null )
                pf = 2;
            postfixDriverName( String.valueOf( pf ), orgNameLength );
            //Logger.log( getDriverNameImpl() );
            ( (_rf1_ScoringInfo)scoringInfo ).nameDuplicatesMap.put( id, pf.intValue() + 1 );
            id = refreshID();
        }
        
        return ( id );
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
    
    private void writeToStreamImpl( OutputStream out ) throws IOException
    {
        out.write( buffer, buffOff, BUFFER_SIZE );
    }
    
    public void writeToStream( OutputStream out ) throws IOException
    {
        writeToStreamImpl( out );
    }
    
    /*
     * {@inheritDoc}
     */
    /*
    @Override
    protected void setDriverName( String drivername )
    {
        byte[] bytes = drivername.getBytes();
        System.arraycopy( bytes, 0, buffer, buffOff + OFFSET_DRIVER_NAME, bytes.length );
        buffer[buffOff + OFFSET_DRIVER_NAME + bytes.length] = (byte)0;
    }
    */
    
    /*
     * {@inheritDoc}
     */
    //@Override
    private int postfixDriverName( String postfix, int pos )
    {
        if ( pos < 0 )
        {
            for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
            {
                if ( buffer[buffOff + OFFSET_DRIVER_NAME + i] == (byte)0 )
                {
                    pos = i;
                    
                    break;
                }
            }
        }
        
        if ( pos >= 0 )
        {
            byte[] bytes = postfix.getBytes();
            System.arraycopy( bytes, 0, buffer, buffOff + OFFSET_DRIVER_NAME + pos, bytes.length );
            buffer[buffOff + OFFSET_DRIVER_NAME + pos + postfix.length()] = (byte)0;
        }
        
        return ( pos );
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
    
    /*
     * {@inheritDoc}
     */
    //@Override
    private final int getDriverNameLength()
    {
        for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
        {
            if ( buffer[buffOff + i] == (byte)0 )
                return ( i );
        }
        
        return ( MAX_DRIVER_NAME_LENGTH );
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
        // float mLapDist
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_LAP_DISTANCE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getPathLateral()
    {
        // float mPathLateral
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_PATH_LATERAL ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTrackEdge()
    {
        // float mTrackEdge
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_TRACK_EDGE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getBestSector1Impl()
    {
        // float mBestSector1
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_BEST_SECTOR_1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getBestSector2Impl()
    {
        // float mBestSector2
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_BEST_SECTOR_2 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getBestLapTimeImpl()
    {
        // float mBestLapTime
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_BEST_LAP_TIME ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getLastSector1Impl()
    {
        // float mLastSector1
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_LAST_SECTOR_1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getLastSector2Impl()
    {
        // float mLastSector2
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_LAST_SECTOR_2 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getLastLapTimeImpl()
    {
        // float mLastLapTime
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_LAST_LAP_TIME ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getCurrentSector1Impl()
    {
        // float mCurSector1
        
        // TODO: Check result, if sector1 is invalid
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_CURR_SECTOR_1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getCurrentSector2Impl()
    {
        // float mCurSector2
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_CURR_SECTOR_2 ) );
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
    
    /*
     * ################################
     * VehicleScoringInfoV2
     * ################################
     */
    
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
        // float mTimeBehindNext
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_TIME_BEHIND_NEXT ) );
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
        // float mTimeBehindLeader
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_TIME_BEHIND_LEADER ) );
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
        // float mLapStartET
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_LAP_START_TIME ) );
    }
    
    // Position and derivatives
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getWorldPosition( TelemVect3 position )
    {
        // TelemVect3 mPos
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_POSITION, position );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWorldPositionX()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_POSITION + 0 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWorldPositionY()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_POSITION + 1 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWorldPositionZ()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, buffOff + OFFSET_POSITION + 2 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalVelocity( TelemVect3 localVel )
    {
        // TelemVect3 mLocalVel
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_LOCAL_VELOCITY, localVel );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getScalarVelocityMS()
    {
        float vecX = ByteUtil.readFloat( buffer, buffOff + OFFSET_LOCAL_VELOCITY + 0 * ByteUtil.SIZE_FLOAT );
        float vecY = ByteUtil.readFloat( buffer, buffOff + OFFSET_LOCAL_VELOCITY + 1 * ByteUtil.SIZE_FLOAT );
        float vecZ = ByteUtil.readFloat( buffer, buffOff + OFFSET_LOCAL_VELOCITY + 2 * ByteUtil.SIZE_FLOAT );
        
        return ( (float)Math.sqrt( vecX * vecX + vecY * vecY + vecZ * vecZ ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalAcceleration( TelemVect3 localAccel )
    {
        // TelemVect3 mLocalAccel
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_LOCAL_ACCELERATION, localAccel );
    }
    
    // Orientation and derivatives
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getOrientationX( TelemVect3 oriX )
    {
        // TelemVect3 mOriX
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_ORIENTATION_X, oriX );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getOrientationY( TelemVect3 oriY )
    {
        // TelemVect3 mOriY
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_ORIENTATION_Y, oriY );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getOrientationZ( TelemVect3 oriZ )
    {
        // TelemVect3 mOriZ
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_ORIENTATION_Z, oriZ );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalRotation( TelemVect3 localRot )
    {
        // TelemVect3 mLocalRot
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_LOCAL_ROTATION, localRot );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        // TelemVect3 mLocalRotAccel
        
        ByteUtil.readVectorF( buffer, buffOff + OFFSET_LOCAL_ROTATION_ACCELERATION, localRotAccel );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PitState getPitState()
    {
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public StatusFlag getStatusFlag()
    {
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleUpgradePack getUpgradePack()
    {
        return ( null );
    }
    
    _rf1_VehicleScoringInfo( ScoringInfo scoringInfo, ProfileInfo profileInfo, LiveGameData gameData, byte[] buffer, int buffOff )
    {
        super( scoringInfo, profileInfo, gameData );
        
        this.buffer = buffer;
        this.buffOff = buffOff;
    }
}
