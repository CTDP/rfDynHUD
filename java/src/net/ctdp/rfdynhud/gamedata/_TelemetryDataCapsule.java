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
public abstract class _TelemetryDataCapsule
{
    public abstract byte[] getBuffer();
    
    public abstract void loadFromStream( InputStream in ) throws IOException;
    
    public abstract void writeToStream( OutputStream out ) throws IOException;
    
    /**
     * @return time since last update (seconds)
     */
    public abstract float getDeltaTime();
    
    /**
     * @return current lap number
     */
    public abstract int getCurrentLapNumber();
    
    /**
     * @return time this lap was started
     */
    public abstract float getLapStartET();
    
    /**
     * @return current vehicle name
     */
    public abstract String getVehicleName();
    
    /**
     * @return current track name
     */
    public abstract String getTrackName();
    
    /**
     * world position in meters
     * 
     * @param position output buffer
     * 
     * @return the outbut buffer back again.
     */
    public abstract TelemVect3 getPosition( TelemVect3 position );
    
    /**
     * @return world position in meters
     */
    public abstract float getPositionX();
    
    /**
     * @return world position in meters
     */
    public abstract float getPositionY();
    
    /**
     * @return world position in meters
     */
    public abstract float getPositionZ();
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel output buffer
     * 
     * @see #getScalarVelocity()
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getLocalVelocity( TelemVect3 localVel );
    
    /**
     * @return velocity (meters/sec)
     * 
     * @see #getLocalVelocity(TelemVect3)
     */
    public abstract float getScalarVelocity();
    
    /**
     * acceleration (meters/sec^2) in local vehicle coordinates
     * 
     * @param localAccel output buffer
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getLocalAcceleration( TelemVect3 localAccel );
    
    /**
     * @return longitudinal acceleration (meters/sec^2)
     */
    public abstract float getLongitudinalAcceleration();
    
    /**
     * @return lateral acceleration (meters/sec^2)
     */
    public abstract float getLateralAcceleration();
    
    /**
     * top row of orientation matrix
     * 
     * (also converts local vehicle vectors into world X using dot product)
     * 
     * @param oriX output buffer
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getOrientationX( TelemVect3 oriX );
    
    /**
     * mid row of orientation matrix
     * 
     * (also converts local vehicle vectors into world Y using dot product)
     * 
     * @param oriY output buffer
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getOrientationY( TelemVect3 oriY );
    
    /**
     * bot row of orientation matrix
     * 
     * (also converts local vehicle vectors into world Z using dot product)
     * 
     * @param oriZ output buffer
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getOrientationZ( TelemVect3 oriZ );
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot output buffer
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getLocalRotation( TelemVect3 localRot );
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel output buffer
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getLocalRotationalAcceleration( TelemVect3 localRotAccel );
    
    /**
     * @return -1=reverse, 0=neutral, 1+=forward gears
     */
    public abstract short getCurrentGear();
    
    /**
     * @return engine RPM
     */
    public abstract float getEngineRPM();
    
    /**
     * @return Celsius
     */
    public abstract float getEngineWaterTemperature();
    
    /**
     * @return Celsius
     */
    public abstract float getEngineOilTemperature();
    
    /**
     * @return clutch RPM
     */
    public abstract float getClutchRPM();
    
    /**
     * @return ranges  0.0-1.0
     */
    public abstract float getUnfilteredThrottle();
    
    /**
     * @return ranges  0.0-1.0
     */
    public abstract float getUnfilteredBrake();
    
    /**
     * @return ranges  0.0-1.0
     */
    public abstract float getUnfilteredClutch();
    
    /**
     * @return ranges -1.0-1.0 (left to right)
     */
    public abstract float getUnfilteredSteering();
    
    /**
     * @return force on steering arms
     */
    public abstract float getSteeringArmForce();
    
    /**
     * @return amount of fuel (liters)
     */
    public abstract float getFuel();
    
    /**
     * @return rev limit
     */
    public abstract float getEngineMaxRPM();
    
    /**
     * @return number of scheduled pitstops
     */
    public abstract short getNumberOfScheduledPitstops();
    
    /**
     * @return whether overheating icon is shown
     */
    public abstract boolean isOverheating();
    
    /**
     * @return whether any parts (besides wheels) have been detached
     */
    public abstract boolean isAnythingDetached();
    
    /**
     * @return dent severity at 8 locations around the car (0=none, 1=some, 2=more)
     */
    public abstract short[] getDentSevirity();
    
    /**
     * @return time of last impact
     */
    public abstract float getLastImpactTime();
    
    /**
     * @return magnitude of last impact
     */
    public abstract float getLastImpactMagnitude();
    
    /**
     * location of last impact
     * 
     * @param lastImpactPos output buffer
     * 
     * @return the output buffer back again.
     */
    public abstract TelemVect3 getLastImpactPosition( TelemVect3 lastImpactPos );
    
    /**
     * @return radians/sec
     * 
     * @param wheel the requested wheel
     */
    public abstract float getWheelRotation( Wheel wheel );
    
    /**
     * @return meters
     * 
     * @param wheel the requested wheel
     */
    public abstract float getWheelSuspensionDeflection( Wheel wheel );
    
    /**
     * @return meters
     * 
     * @param wheel the requested wheel
     */
    public abstract float getRideHeight( Wheel wheel );
    
    /**
     * @return Newtons
     * 
     * @param wheel the requested wheel
     */
    public abstract float getTireLoad( Wheel wheel );
    
    /**
     * @return Newtons
     * 
     * @param wheel the requested wheel
     */
    public abstract float getLateralForce( Wheel wheel );
    
    /**
     * @return an approximation of what fraction of the contact patch is sliding
     * 
     * @param wheel the requested wheel
     */
    public abstract float getGripFraction( Wheel wheel );
    
    /**
     * @return Kelvin
     * 
     * @param wheel the requested wheel
     */
    public abstract float getBrakeTemperature( Wheel wheel );
    
    /**
     * @return kPa
     * 
     * @param wheel the requested wheel
     */
    public abstract float getTirePressure( Wheel wheel );
    
    /**
     * @return Kelvin
     * 
     * @param wheel the requested wheel
     * @param part the requested wheel part
     */
    public abstract float getTireTemperature( Wheel wheel, WheelPart part );
    
    /**
     * @return wear (0.0-1.0, fraction of maximum) ... this is not necessarily proportional with grip loss
     * 
     * @param wheel the requested wheel
     */
    public abstract float getTireWear( Wheel wheel );
    
    /**
     * @return the material prefixes from the TDF file
     * 
     * @param wheel the requested wheel
     */
    public abstract String getTerrainName( Wheel wheel );
    
    /**
     * @return surface under the wheel
     * 
     * @param wheel the requested wheel
     */
    public abstract SurfaceType getSurfaceType( Wheel wheel );
    
    /**
     * @return whether tire is flat
     * 
     * @param wheel the requested wheel
     */
    public abstract boolean isWheelFlat( Wheel wheel );
    
    /**
     * @return whether wheel is detached
     * 
     * @param wheel the requested wheel
     */
    public abstract boolean isWheelDetached( Wheel wheel );
    
    protected _TelemetryDataCapsule()
    {
    }
}
