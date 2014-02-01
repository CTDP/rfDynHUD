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
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SurfaceType;
import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.WheelPart;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;

import org.jagatoo.util.streams.StreamUtils;

/**
 * Our world coordinate system is left-handed, with +y pointing up.
 * The local vehicle coordinate system is as follows:
 *   +x points out the left side of the car (from the driver's perspective)
 *   +y points out the roof
 *   +z points out the back of the car
 * Rotations are as follows:
 *   +x pitches up
 *   +y yaws to the right
 *   +z rolls to the right
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_TelemetryData extends TelemetryData
{
    private static final int OFFSET_SLOT_ID = 0;
    private static final int OFFSET_DELTA_TIME = OFFSET_SLOT_ID + ByteUtil.SIZE_LONG;
    private static final int OFFSET_ELAPSED_TIME = OFFSET_DELTA_TIME + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LAP_NUMBER = OFFSET_ELAPSED_TIME + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LAP_START_TIME = OFFSET_LAP_NUMBER + ByteUtil.SIZE_LONG;
    private static final int OFFSET_VEHICLE_NAME = OFFSET_LAP_START_TIME + ByteUtil.SIZE_DOUBLE;
    private static final int MAX_VEHICLE_NAME_LENGTH = 64;
    private static final int OFFSET_TRACK_NAME = OFFSET_VEHICLE_NAME + MAX_VEHICLE_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int MAX_TRACK_NAME_LENGTH = 64;
    
    private static final int OFFSET_POSITION = OFFSET_TRACK_NAME + MAX_TRACK_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LOCAL_VELOCITY = OFFSET_POSITION + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_LOCAL_ACCELERATION = OFFSET_LOCAL_VELOCITY + ByteUtil.SIZE_VECTOR3D;
    
    private static final int OFFSET_ORIENTATION_X = OFFSET_LOCAL_ACCELERATION + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_ORIENTATION_Y = OFFSET_ORIENTATION_X + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_ORIENTATION_Z = OFFSET_ORIENTATION_Y + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_LOCAL_ROTATION = OFFSET_ORIENTATION_Z + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_LOCAL_ROTATION_ACCELERATION = OFFSET_LOCAL_ROTATION + ByteUtil.SIZE_VECTOR3D;
    
    private static final int OFFSET_GEAR = OFFSET_LOCAL_ROTATION_ACCELERATION + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_ENGINE_RPM = OFFSET_GEAR + ByteUtil.SIZE_LONG;
    private static final int OFFSET_ENGINE_WATER_TEMP = OFFSET_ENGINE_RPM + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_ENGINE_OIL_TEMP = OFFSET_ENGINE_WATER_TEMP + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_CLUTCH_RPM = OFFSET_ENGINE_OIL_TEMP + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_UNFILTERED_THROTTLE = OFFSET_CLUTCH_RPM + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_UNFILTERED_BRAKE = OFFSET_UNFILTERED_THROTTLE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_UNFILTERED_STEERING = OFFSET_UNFILTERED_BRAKE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_UNFILTERED_CLUTCH = OFFSET_UNFILTERED_STEERING + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_FILTERED_THROTTLE = OFFSET_UNFILTERED_CLUTCH + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_FILTERED_BRAKE = OFFSET_FILTERED_THROTTLE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_FILTERED_STEERING = OFFSET_FILTERED_BRAKE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_FILTERED_CLUTCH = OFFSET_FILTERED_STEERING + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_STEERING_ARM_FORCE = OFFSET_FILTERED_CLUTCH + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_FRONT_3RD_SPRING_DEFLECTION = OFFSET_STEERING_ARM_FORCE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_REAR_3RD_SPRING_DEFLECTION = OFFSET_FRONT_3RD_SPRING_DEFLECTION + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_FRONT_WING_HEIGHT = OFFSET_REAR_3RD_SPRING_DEFLECTION + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_FRONT_RIDE_HEIGHT = OFFSET_FRONT_WING_HEIGHT + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_REAR_RIDE_HEIGHT = OFFSET_FRONT_RIDE_HEIGHT + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_DRAG = OFFSET_REAR_RIDE_HEIGHT + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_FRONT_DOWNFORCE = OFFSET_DRAG + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_REAR_DOWNFORCE = OFFSET_FRONT_DOWNFORCE + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_FUEL = OFFSET_REAR_DOWNFORCE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_ENGINE_MAX_RPM = OFFSET_FUEL + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_SCHEDULED_STOPS = OFFSET_ENGINE_MAX_RPM + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_OVERHEATING = OFFSET_SCHEDULED_STOPS + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_DETACHED = OFFSET_OVERHEATING + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_HEADLIGHTS = OFFSET_DETACHED + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_DENT_SEVERITY = OFFSET_HEADLIGHTS + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_LAST_IMPACT_TIME = OFFSET_DENT_SEVERITY + 8 * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAST_IMPACT_MAGNITUDE = OFFSET_LAST_IMPACT_TIME + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LAST_IMPACT_POSITION = OFFSET_LAST_IMPACT_MAGNITUDE + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_EXPANSION = OFFSET_LAST_IMPACT_POSITION + ByteUtil.SIZE_VECTOR3D;
    
    /*
    double mEngineTq;              // current engine torque (including additive torque)
    long mCurrentSector;           // the current sector (zero-based) with the pitlane stored in the sign bit (example: entering pits from third sector gives 0x80000002)
    unsigned char mSpeedLimiter;   // whether speed limiter is on
    unsigned char mMaxGears;       // maximum forward gears
    unsigned char mFrontTireCompoundIndex;   // index within brand
    unsigned char mRearTireCompoundIndex;    // index within brand
    double mFuelCapacity;          // capacity in liters
    unsigned char mFrontFlapActivated;       // whether front flap is activated
    unsigned char mRearFlapActivated;        // whether rear flap is activated
    unsigned char mRearFlapLegalStatus;      // 0=disallowed, 1=criteria detected but not allowed quite yet, 2=allowed
    unsigned char mIgnitionStarter;          // 0=off 1=ignition 2=ignition+starter
    char mFrontTireCompoundName[18];         // name of front tire compound
    char mRearTireCompoundName[18];          // name of rear tire compound
    */
    
    private static final int OFFSET_WHEEL_DATA = OFFSET_EXPANSION + 256 * ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_WHEEL_SUSPENSION_DEFLECTION = 1;
    private static final int OFFSET_RIDE_HEIGHT = OFFSET_WHEEL_SUSPENSION_DEFLECTION + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_WHEEL_SUSPENSION_FORCE = OFFSET_RIDE_HEIGHT + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_BRAKE_TEMP = OFFSET_WHEEL_SUSPENSION_FORCE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_BRAKE_PRESSURE = OFFSET_BRAKE_TEMP + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_WHEEL_ROTATION = OFFSET_BRAKE_PRESSURE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LATERAL_PATCH_VEL = OFFSET_WHEEL_ROTATION + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LONGITUDINAL_PATCH_VEL = OFFSET_LATERAL_PATCH_VEL + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LATERAL_GROUND_VEL = OFFSET_LONGITUDINAL_PATCH_VEL + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LONGITUDINAL_GROUND_VEL = OFFSET_LATERAL_GROUND_VEL + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_CAMBER = OFFSET_LONGITUDINAL_GROUND_VEL + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LATERAL_FORCE = OFFSET_CAMBER + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_LONGITUDINAL_FORCE = OFFSET_LATERAL_FORCE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_TIRE_LOAD = OFFSET_LONGITUDINAL_FORCE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_GRIP_FRACTION = OFFSET_TIRE_LOAD + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_TIRE_PRESSURE = OFFSET_GRIP_FRACTION + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_TIRE_TEMPERATURES = OFFSET_TIRE_PRESSURE + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_TIRE_WEAR = OFFSET_TIRE_TEMPERATURES + 3 * ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_TERRAIN_NAME = OFFSET_TIRE_WEAR + ByteUtil.SIZE_DOUBLE;
    private static final int MAX_TERRAIN_NAME_LENGTH = 16;
    private static final int OFFSET_SURFACE_TYPE = OFFSET_TERRAIN_NAME + MAX_TERRAIN_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_IS_WHEEL_FLAT = OFFSET_SURFACE_TYPE + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_IS_WHEEL_DETACHED = OFFSET_IS_WHEEL_FLAT + ByteUtil.SIZE_BOOL;
    
    private static final int OFFSET_VERTICAL_TIRE_DEFLECTION = OFFSET_IS_WHEEL_DETACHED + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_WHEEL_Y_LOCATION = OFFSET_VERTICAL_TIRE_DEFLECTION + ByteUtil.SIZE_DOUBLE;
    
    private static final int OFFSET_WHEEL_DATA_EXPENSION = OFFSET_WHEEL_Y_LOCATION + ByteUtil.SIZE_DOUBLE;
    
    private static final int WHEEL_DATA_SIZE = OFFSET_WHEEL_DATA_EXPENSION + 64 * ByteUtil.SIZE_CHAR;
    
    private static final int BUFFER_SIZE = OFFSET_WHEEL_DATA + ( 4 * WHEEL_DATA_SIZE );
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private static native void fetchData( final long sourceBufferAddress, final int sourceBufferSize, final byte[] targetBuffer );
    
    @Override
    protected void updateDataImpl( Object userObject, long timestamp )
    {
        _rf2_DataAddressKeeper ak = (_rf2_DataAddressKeeper)userObject;
        
        fetchData( ak.getBufferAddress(), ak.getBufferSize(), buffer );
    }
    
    @Override
    protected float clampEngineMaxRPMByPhysics( float maxRPM, VehiclePhysics.Engine engine )
    {
        // TODO: Remove this once vehicle physics are known!
        __GDPrivilegedAccess.set( maxRPM, 0, 1, engine.getRevLimitRange() );
        
        //return ( engine.getRevLimitRange().clampValue( maxRPM ) );
        
        return ( maxRPM );
    }
    
    private void readFromStreamImpl( InputStream in ) throws IOException
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
    
    @Override
    public void readFromStream( InputStream in, boolean isEditorMode ) throws IOException
    {
        final long now = System.nanoTime();
        
        prepareDataUpdate( null, now );
        
        readFromStreamImpl( in );
        
        onDataUpdated( null, now, isEditorMode );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void readDefaultValues( boolean isEditorMode ) throws IOException
    {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream( this.getClass().getPackage().getName().replace( '.', '/' ) + "/data/game_data/telemetry_data" );
        
        try
        {
            readFromStream( in, isEditorMode );
        }
        finally
        {
            if ( in != null )
                StreamUtils.closeStream( in );
        }
    }
    
    @Override
    public void writeToStream( OutputStream out ) throws IOException
    {
        out.write( buffer, 0, BUFFER_SIZE );
    }
    
    /*
     * ################################
     * TelemInfoV01
     * ################################
     */
    
    // Time
    
    /**
     * @return slot ID (note that it can be re-used in multiplayer after someone leaves)
     * TODO: [API] Add to public interface!
     */
    //@Override
    public final int getSlotId()
    {
        // long mID
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_SLOT_ID ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getDeltaTime()
    {
        // double mDeltaTime
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_DELTA_TIME ) );
    }
    
    /**
     * @return game session time
     * 
     * TODO: [API] Add to public interface!
     */
    //@Override
    public final float getElapsedTime()
    {
        // double mElapsedTime
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_ELAPSED_TIME ) );
    }
    
    //@Override
    public final int getCurrentLapNumber()
    {
        // long mLapNumber
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_LAP_NUMBER ) );
    }
    
    //@Override
    public final float getLapStartET()
    {
        // double mLapStartET
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_LAP_START_TIME ) );
    }
    
    //@Override
    public final String getVehicleName()
    {
        // char mVehicleName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_VEHICLE_NAME, MAX_VEHICLE_NAME_LENGTH ) );
    }
    
    //@Override
    public final String getTrackName()
    {
        // char mTrackName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_TRACK_NAME, MAX_TRACK_NAME_LENGTH ) );
    }
    
    // Position and derivatives
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getPosition( TelemVect3 position )
    {
        // TelemVect3 mPos
        
        ByteUtil.readVectorD( buffer, OFFSET_POSITION, position );
        
        return ( position );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getPositionX()
    {
        // TelemVect3 mPos
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_POSITION + 0 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getPositionY()
    {
        // TelemVect3 mPos
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_POSITION + 1 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getPositionZ()
    {
        // TelemVect3 mPos
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_POSITION + 2 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getLocalVelocity( TelemVect3 localVel )
    {
        // TelemVect3 mLocalVel
        
        ByteUtil.readVectorD( buffer, OFFSET_LOCAL_VELOCITY, localVel );
        
        return ( localVel );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getScalarVelocityMS()
    {
        double vecX = ByteUtil.readDouble( buffer, OFFSET_LOCAL_VELOCITY + 0 * ByteUtil.SIZE_DOUBLE );
        double vecY = ByteUtil.readDouble( buffer, OFFSET_LOCAL_VELOCITY + 1 * ByteUtil.SIZE_DOUBLE );
        double vecZ = ByteUtil.readDouble( buffer, OFFSET_LOCAL_VELOCITY + 2 * ByteUtil.SIZE_DOUBLE );
        
        return ( (float)Math.sqrt( vecX * vecX + vecY * vecY + vecZ * vecZ ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getLocalAcceleration( TelemVect3 localAccel )
    {
        // TelemVect3 mLocalAccel
        
        ByteUtil.readVectorD( buffer, OFFSET_LOCAL_ACCELERATION, localAccel );
        
        return ( localAccel );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getLongitudinalAcceleration()
    {
        // TelemVect3 mLocalAccel
        
        return ( -(float)ByteUtil.readDouble( buffer, OFFSET_LOCAL_ACCELERATION + 2 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getLateralAcceleration()
    {
        // TelemVect3 mLocalAccel
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_LOCAL_ACCELERATION + 0 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    // Orientation and derivatives
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getOrientationX( TelemVect3 oriX )
    {
        // TelemVect3 mOriX
        
        ByteUtil.readVectorD( buffer, OFFSET_ORIENTATION_X, oriX );
        
        return ( oriX );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getOrientationY( TelemVect3 oriY )
    {
        // TelemVect3 mOriY
        
        ByteUtil.readVectorD( buffer, OFFSET_ORIENTATION_Y, oriY );
        
        return ( oriY );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getOrientationZ( TelemVect3 oriZ )
    {
        // TelemVect3 mOriZ
        
        ByteUtil.readVectorD( buffer, OFFSET_ORIENTATION_Z, oriZ );
        
        return ( oriZ );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getLocalRotation( TelemVect3 localRot )
    {
        // TelemVect3 mLocalRot
        
        ByteUtil.readVectorD( buffer, OFFSET_LOCAL_ROTATION, localRot );
        
        return ( localRot );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        // TelemVect3 mLocalRotAccel
        
        ByteUtil.readVectorD( buffer, OFFSET_LOCAL_ROTATION_ACCELERATION, localRotAccel );
        
        return ( localRotAccel );
    }
    
    // Vehicle status
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final short getCurrentGear()
    {
        // long mGear
        
        return ( (short)ByteUtil.readLong( buffer, OFFSET_GEAR ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getEngineRPMImpl()
    {
        // double mEngineRPM
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_ENGINE_RPM ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getEngineWaterTemperatureC()
    {
        // double mEngineWaterTemp
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_ENGINE_WATER_TEMP ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getEngineOilTemperatureC()
    {
        // double mEngineOilTemp
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_ENGINE_OIL_TEMP ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getClutchRPM()
    {
        // double mClutchRPM
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_CLUTCH_RPM ) );
    }
    
    // Driver input
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getUnfilteredThrottle()
    {
        // double mUnfilteredThrottle
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_UNFILTERED_THROTTLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getUnfilteredBrake()
    {
        // double mUnfilteredBrake
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_UNFILTERED_BRAKE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getUnfilteredClutch()
    {
        // double mUnfilteredClutch
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_UNFILTERED_CLUTCH ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getUnfilteredSteering()
    {
        // double mUnfilteredSteering
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_UNFILTERED_STEERING ) );
    }
    
    // Filtered input (various adjustments for rev or speed limiting, TC, ABS?, speed sensitive steering, clutch work for semi-automatic shifting, etc.)
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final float getFilteredThrottle()
    {
        // double mFilteredThrottle
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FILTERED_THROTTLE ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final float getFilteredBrake()
    {
        // double mFfilteredBrake
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FILTERED_BRAKE ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final float getFilteredClutch()
    {
        // double mFilteredClutch
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FILTERED_CLUTCH ) );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public final float getFilteredSteering()
    {
        // double mFilteredSteering
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FILTERED_STEERING ) );
    }
    
    // Misc
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getSteeringArmForce()
    {
        // double mSteeringArmForce
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_STEERING_ARM_FORCE ) );
    }
    
    /**
     * @return deflection at front 3rd spring
     */
    //@Override
    public final float getFront3rdSpringDeflection()
    {
        // double mFront3rdDeflection
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FRONT_3RD_SPRING_DEFLECTION ) );
    }
    
    /**
     * @return deflection at rear 3rd spring
     */
    //@Override
    public final float getRear3rdSpringDeflection()
    {
        // double mRear3rdDeflection
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_REAR_3RD_SPRING_DEFLECTION ) );
    }
    
    // Aerodynamics
    
    /**
     * @return front wing height
     */
    //@Override
    public final float getFrontWingHeight()
    {
        // double mFrontWingHeight
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FRONT_WING_HEIGHT ) );
    }
    
    /**
     * @return front ride height
     */
    //@Override
    public final float getFrontRideHeight()
    {
        // double mFrontRideHeight
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FRONT_RIDE_HEIGHT ) );
    }
    
    /**
     * @return rear ride height
     */
    //@Override
    public final float getRearRideHeight()
    {
        // double mRearRideHeight
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_REAR_RIDE_HEIGHT ) );
    }
    
    /**
     * @return drag
     */
    //@Override
    public final float getDrag()
    {
        // double mDrag
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_DRAG ) );
    }
    
    /**
     * @return front downforce
     */
    //@Override
    public final float getFrontDownforce()
    {
        // double mFrontDownforce
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FRONT_DOWNFORCE ) );
    }
    
    /**
     * @return rear downforce
     */
    //@Override
    public final float getRearDownforce()
    {
        // double mRearDownforce
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_REAR_DOWNFORCE ) );
    }
    
    // state/damage info
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getFuelImpl()
    {
        // double mFuel
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_FUEL ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final float getEngineMaxRPMImpl()
    {
        // double mEngineMaxRPM
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_ENGINE_MAX_RPM ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final short getNumberOfScheduledPitstops()
    {
        // unsigned char mScheduledStops
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_SCHEDULED_STOPS ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isOverheating()
    {
        // bool mOverheating
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_OVERHEATING ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAnythingDetached()
    {
        // bool mDetached
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_DETACHED ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final short[] getDentSevirity()
    {
        // unsigned char mDentSeverity[8]
        
        short[] result = new short[ 8 ];
        
        for ( int i = 0; i < result.length; i++ )
        {
            result[i] = ByteUtil.readUnsignedByte( buffer, OFFSET_DENT_SEVERITY + i * ByteUtil.SIZE_CHAR );
        }
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getLastImpactTime()
    {
        // double mLastImpactET
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_LAST_IMPACT_TIME ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getLastImpactMagnitude()
    {
        // double mLastImpactMagnitude
        
        return ( (float)ByteUtil.readDouble( buffer, OFFSET_LAST_IMPACT_MAGNITUDE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final TelemVect3 getLastImpactPosition( TelemVect3 lastImpactPos )
    {
        // TelemVect3 mLastImpactPos
        
        ByteUtil.readVectorD( buffer, OFFSET_LAST_IMPACT_POSITION, lastImpactPos );
        
        return ( lastImpactPos );
    }
    
    /*
     * ################################
     * TelemWheelV01
     * ################################
     */
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWheelRotation( Wheel wheel )
    {
        // double mRotation
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_WHEEL_ROTATION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWheelSuspensionDeflectionM( Wheel wheel )
    {
        // double mSuspensionDeflection
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_DEFLECTION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getRideHeightM( Wheel wheel )
    {
        // double mRideHeight
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_RIDE_HEIGHT ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return pushrod load in Newtons
     * 
     * @param wheel
     */
    //@Override
    public final float getSuspensionForce( Wheel wheel )
    {
        // double mSuspForce
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_FORCE ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_FORCE ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_FORCE ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_WHEEL_SUSPENSION_FORCE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTireLoadN( Wheel wheel )
    {
        // double mTireLoad
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_LOAD ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getLateralForce( Wheel wheel )
    {
        // double mLateralForce
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_LATERAL_FORCE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return Newtons
     * 
     * @param wheel
     */
    //@Override
    public final float getLongitudinalForce( Wheel wheel )
    {
        // double mLongitudinalForce
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_FORCE ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_FORCE ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_FORCE ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_FORCE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getGripFraction( Wheel wheel )
    {
        // double mGripFract
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_GRIP_FRACTION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getBrakeTemperatureK( Wheel wheel )
    {
        // double mBrakeTemp
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_BRAKE_TEMP ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return currently 0.0-1.0, depending on driver input and brake balance; will convert to true brake pressure (kPa) in future
     * 
     * @param wheel
     */
    //@Override
    public final float getBrakePressure( Wheel wheel )
    {
        // double mBrakePressure
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_BRAKE_PRESSURE ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_BRAKE_PRESSURE ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_BRAKE_PRESSURE ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_BRAKE_PRESSURE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return lateral velocity at contact patch
     * 
     * @param wheel
     */
    //@Override
    public final float getLateralPatchVelocity( Wheel wheel )
    {
        // double mLateralPatchVel
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_LATERAL_PATCH_VEL ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_LATERAL_PATCH_VEL ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_LATERAL_PATCH_VEL ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_LATERAL_PATCH_VEL ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return longitudinal velocity at contact patch
     * 
     * @param wheel
     */
    //@Override
    public final float getLongitudinalPatchVelocity( Wheel wheel )
    {
        // double mLongitudinalPatchVel
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_PATCH_VEL ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_PATCH_VEL ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_PATCH_VEL ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_PATCH_VEL ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return lateral velocity at ground
     * 
     * @param wheel
     */
    //@Override
    public final float getLateralGroundVelocity( Wheel wheel )
    {
        // double mLateralGroundVel
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_LATERAL_GROUND_VEL ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_LATERAL_GROUND_VEL ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_LATERAL_GROUND_VEL ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_LATERAL_GROUND_VEL ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return longitudinal velocity at ground
     * 
     * @param wheel
     */
    //@Override
    public final float getLongitudinalGroundVelocity( Wheel wheel )
    {
        // double mLongitudinalGroundVel
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_GROUND_VEL ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_GROUND_VEL ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_GROUND_VEL ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_LONGITUDINAL_GROUND_VEL ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return radians (positive is left for left-side wheels, right for right-side wheels)
     * 
     * @param wheel
     */
    //@Override
    public final float getCamber( Wheel wheel )
    {
        // double mCamber
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_CAMBER ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_CAMBER ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_CAMBER ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_CAMBER ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTirePressureKPa( Wheel wheel )
    {
        // double mPressure
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_PRESSURE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTireTemperatureC( Wheel wheel, WheelPart part )
    {
        // double mTemperature[3], left/center/right (not to be confused with inside/center/outside!)
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexFL() * ByteUtil.SIZE_DOUBLE ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexFR() * ByteUtil.SIZE_DOUBLE ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexRL() * ByteUtil.SIZE_DOUBLE ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_TEMPERATURES + part.getArrayIndexRR() * ByteUtil.SIZE_DOUBLE ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTireWear( Wheel wheel )
    {
        // double mWear
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TIRE_WEAR ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTerrainName( Wheel wheel )
    {
        // char mTerrainName[16]
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
            case REAR_LEFT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
            case REAR_RIGHT:
                return ( ByteUtil.readString( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_TERRAIN_NAME, MAX_TERRAIN_NAME_LENGTH ) );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final SurfaceType getSurfaceType( Wheel wheel )
    {
        // unsigned char mSurfaceType
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
            case FRONT_RIGHT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
            case REAR_LEFT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
            case REAR_RIGHT:
                return ( SurfaceType.getFromIndex( ByteUtil.readUnsignedByte( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_SURFACE_TYPE ) ) );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isWheelFlat( Wheel wheel )
    {
        // bool mFlat
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
            case REAR_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
            case REAR_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_FLAT ) );
        }
        
        // Unreachable code!
        return ( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isWheelDetached( Wheel wheel )
    {
        // bool mDetached
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
            case FRONT_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
            case REAR_LEFT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
            case REAR_RIGHT:
                return ( ByteUtil.readBoolean( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_IS_WHEEL_DETACHED ) );
        }
        
        // Unreachable code!
        return ( false );
    }
    
    /**
     * @return how much is tire deflected from its (speed-sensitive) radius
     * 
     * @param wheel
     */
    //@Override
    public final float getVerticalTireDeflection( Wheel wheel )
    {
        // double mVerticalTireDeflection
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_VERTICAL_TIRE_DEFLECTION ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_VERTICAL_TIRE_DEFLECTION ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_VERTICAL_TIRE_DEFLECTION ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_VERTICAL_TIRE_DEFLECTION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    /**
     * @return wheel's y location relative to vehicle y location
     * 
     * @param wheel
     */
    //@Override
    public final float getWheelYLocation( Wheel wheel )
    {
        // double mWheelYLocation
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 0 * WHEEL_DATA_SIZE + OFFSET_WHEEL_Y_LOCATION ) );
            case FRONT_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 1 * WHEEL_DATA_SIZE + OFFSET_WHEEL_Y_LOCATION ) );
            case REAR_LEFT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 2 * WHEEL_DATA_SIZE + OFFSET_WHEEL_Y_LOCATION ) );
            case REAR_RIGHT:
                return ( (float)ByteUtil.readDouble( buffer, OFFSET_WHEEL_DATA + 3 * WHEEL_DATA_SIZE + OFFSET_WHEEL_Y_LOCATION ) );
        }
        
        // Unreachable code!
        return ( 0f );
    }
    
    _rf2_TelemetryData( LiveGameData gameData )
    {
        super( gameData );
    }
}
