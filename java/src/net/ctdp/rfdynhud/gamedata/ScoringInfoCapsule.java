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

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
class ScoringInfoCapsule
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
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    final byte[] getBuffer()
    {
        return ( buffer );
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
    }
    
    void writeToStream( OutputStream out ) throws IOException
    {
        out.write( buffer, 0, BUFFER_SIZE );
    }
    
    /*
     * ################################
     * ScoringInfoBase
     * ################################
     */
    
    /**
     * @return current track name
     */
    public final String getTrackName()
    {
        // char mTrackName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_TRACK_NAME, MAX_TRACK_NAME_LENGTH ) );
    }
    
    /**
     * @return current session
     */
    public final SessionType getSessionType()
    {
        // long mSession
        
        int st = (int)ByteUtil.readLong( buffer, OFFSET_SESSION_TYPE );
        
        return ( SessionType.values()[st] );
    }
    
    /**
     * @return current session time
     */
    public final float getSessionTime()
    {
        // float mCurrentET
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CURRENT_TIME ) );
    }
    
    /**
     * @return session ending time
     */
    public final float getEndTime()
    {
        // float mEndET
        
        return ( ByteUtil.readFloat( buffer, OFFSET_END_TIME ) );
    }
    
    /**
     * @return maximum laps
     */
    public final int getMaxLaps()
    {
        // long mMaxLaps
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_MAX_LAPS ) );
    }
    
    /**
     * @return distance around track
     */
    public final float getTrackLength()
    {
        // float mLapDist
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAP_DISTANCE ) );
    }
    
    //char *mResultsStream;          // results stream additions since last update (newline-delimited and NULL-terminated)
    
    /**
     * @return current number of vehicles
     */
    public final int getNumVehicles()
    {
        // long mNumVehicles
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_NUM_VEHICLES ) );
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
     * @return Game phases
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
     * @return Yellow flag states (applies to full-course only)
     */
    public final YellowFlagState getYellowFlagState()
    {
        // signed char mYellowFlagState
        
        short state = ByteUtil.readByte( buffer, OFFSET_YELLOW_FLAG_STATE );
        
        switch ( state )
        {
            case -1:
                throw new Error( "Invalid state detected." );
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
     * @return whether there are any local yellows at the moment in each sector
     * 
     * @param sector the sector in question
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
     * @return start light frame (number depends on track)
     */
    public final int getStartLightFrame()
    {
        // unsigned char mStartLight
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_STARTING_LIGHT_FRAME ) );
    }
    
    /**
     * @return number of red lights in start sequence
     */
    public final int getNumRedLights()
    {
        // unsigned char mNumRedLights
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_NUM_RED_LIGHTS ) );
    }
    
    /**
     * @return in realtime as opposed to at the monitor
     */
    public final boolean isInRealtimeMode()
    {
        // bool mInRealtime
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_IN_REALTIME ) );
    }
    
    /**
     * @return player name (including possible multiplayer override)
     */
    public final String getPlayerName()
    {
        // char mPlayerName[32]
        
        return ( ByteUtil.readString( buffer, OFFSET_PLAYER_NAME, MAX_PLAYER_NAME_LENGTH ) );
    }
    
    /**
     * @return may be encoded to be a legal filename
     */
    public final String getPlayerFilename()
    {
        // char mPlrFileName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_PLAYER_FILENAME, MAX_PLAYER_FILENAME_LENGTH ) );
    }
    
    // weather
    
    /**
     * @return cloud darkness? 0.0-1.0
     */
    public final float getCloudDarkness()
    {
        // float mDarkCloud
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CLOUD_DARKNESS ) );
    }
    
    /**
     * @return raining severity 0.0-1.0
     */
    public final float getRainingSeverity()
    {
        // float mRaining
        
        return ( ByteUtil.readFloat( buffer, OFFSET_RAINING_SEVERITIY ) );
    }
    
    /**
     * @return temperature (Celsius)
     */
    public final float getAmbientTemperature()
    {
        // float mAmbientTemp
        
        return ( ByteUtil.readFloat( buffer, OFFSET_AMBIENT_TEMPERATURE ) );
    }
    
    /**
     * @return temperature (Celsius)
     */
    public final float getTrackTemperature()
    {
        // float mTrackTemp
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TRACK_TEMPERATURE ) );
    }
    
    /**
     * wind speed
     * 
     * @param speed output buffer
     */
    public final void getWindSpeed( TelemVect3 speed )
    {
        // TelemVect3 mWind
        
        ByteUtil.readVector( buffer, OFFSET_WIND_SPEED, speed );
    }
    
    /**
     * @return on main path 0.0-1.0
     */
    public final float getOnPathWetness()
    {
        // float mOnPathWetness
        
        return ( ByteUtil.readFloat( buffer, OFFSET_ON_PATH_WETNESS ) );
    }
    
    /**
     * @return off main path 0.0-1.0
     */
    public final float getOffPathWetness()
    {
        // float mOffPathWetness
        
        return ( ByteUtil.readFloat( buffer, OFFSET_OFF_PATH_WETNESS ) );
    }
    
    // Future use
    //unsigned char mExpansion[256];
    
    /*
     * array of vehicle scoring info's
     * 
     * @param i
     * 
     * @see #getNumVehicles()
     */
    /*
    public final VehicleScoringInfo getVehicleScoringInfo( int i )
    {
        // VehicleScoringInfoV2* mVehicle
        
        if ( i >= getNumVehicles() )
            throw new IllegalArgumentException( "There is no vehicle with the index " + i + ". There are only " + getNumVehicles() + " vehicles." );
        
        return ( vehicleScoringInfo2[i] );
    }
    */
    
    ScoringInfoCapsule()
    {
    }
}
