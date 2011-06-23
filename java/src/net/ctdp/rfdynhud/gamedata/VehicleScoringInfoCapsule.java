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
abstract class VehicleScoringInfoCapsule
{
    protected abstract String getOriginalName();
    
    protected abstract void resetHash();
    
    public abstract byte[] getBuffer();
    
    protected abstract Integer refreshID( boolean storeOriginalName );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean equals( Object o );
    
    public abstract void loadFromStream( InputStream in ) throws IOException;
    
    public abstract void writeToStream( OutputStream out ) throws IOException;
    
    protected abstract void setDriverName( String drivername );
    
    protected abstract int postfixDriverName( String postfix, int pos );
    
    public abstract String getDriverName();
    
    /**
     * @return vehicle name
     */
    public abstract String getVehicleName();
    
    /**
     * @return laps completed
     */
    public abstract short getLapsCompleted();
    
    /**
     * @return sector
     */
    public abstract byte getSector();
    
    /**
     * @return finish status
     */
    public abstract FinishStatus getFinishStatus();
    
    /**
     * @return current distance around track
     */
    public abstract float getLapDistance();
    
    /**
     * @return lateral position with respect to *very approximate* "center" path
     */
    public abstract float getPathLateral();
    
    /**
     * @return track edge (w.r.t. "center" path) on same side of track as vehicle
     */
    public abstract float getTrackEdge();
    
    /**
     * @return best sector 1
     */
    public abstract float getBestSector1();
    
    /**
     * @return best sector 2
     */
    public abstract float getBestSector2();
    
    /**
     * @return best lap time
     */
    public abstract float getBestLapTime();
    
    /**
     * @return last sector 1
     */
    public abstract float getLastSector1();
    
    /**
     * @return last sector 2
     */
    public abstract float getLastSector2();
    
    /**
     * @return last lap time
     */
    public abstract float getLastLapTime();
    
    /**
     * @return current sector 1 (if valid)
     */
    public abstract float getCurrentSector1();
    
    /**
     * @return current sector 2
     */
    public abstract float getCurrentSector2();
    
    /**
     * @return number of pitstops made
     */
    public abstract short getNumPitstopsMade();
    
    /**
     * @return number of outstanding penalties
     */
    public abstract short getNumOutstandingPenalties();
    
    /**
     * @return is this the player's vehicle?
     */
    public abstract boolean isPlayer();
    
    /**
     * @return who's in control?
     */
    public abstract VehicleControl getVehicleControl();
    
    /**
     * between pit entrance and pit exit (not always accurate for remote vehicles)
     * 
     * @return is this vehicle in the pit lane?
     */
    public abstract boolean isInPits();
    
    /**
     * @return 1-based position
     */
    public abstract short getPlace();
    
    /**
     * @return vehicle class
     */
    public abstract String getVehicleClass();
    
    /**
     * @return time behind vehicle in next higher place
     */
    public abstract float getTimeBehindNextInFront();
    
    /**
     * @return laps behind vehicle in next higher place
     */
    public abstract int getLapsBehindNextInFront();
    
    /**
     * @return time behind leader
     */
    public abstract float getTimeBehindLeader();
    
    /**
     * @return laps behind leader
     */
    public abstract int getLapsBehindLeader();
    
    /**
     * @return time this lap was started at
     */
    public abstract float getLapStartTime();
    
    /**
     * world position in meters
     * 
     * @param position output buffer
     */
    public abstract void getWorldPosition( TelemVect3 position );
    
    /**
     * @return world position in meters
     */
    public abstract float getWorldPositionX();
    
    /**
     * @return world position in meters
     */
    public abstract float getWorldPositionY();
    
    /**
     * @return world position in meters
     */
    public abstract float getWorldPositionZ();
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel output buffer
     */
    public abstract void getLocalVelocity( TelemVect3 localVel );
    
    /**
     * @return velocity (meters/sec) in local vehicle coordinates
     */
    public abstract float getScalarVelocity();
    
    /**
     * acceleration (meters/sec^2) in local vehicle coordinates
     * 
     * @param localAccel output buffer
     */
    public abstract void getLocalAcceleration( TelemVect3 localAccel );
    
    /**
     * top row of orientation matrix (also converts local vehicle vectors into world X using dot product)
     * 
     * @param oriX output buffer
     */
    public abstract void getOrientationX( TelemVect3 oriX );
    
    /**
     * mid row of orientation matrix (also converts local vehicle vectors into world Y using dot product)
     * 
     * @param oriY output buffer
     */
    public abstract void getOrientationY( TelemVect3 oriY );
    
    /**
     * bot row of orientation matrix (also converts local vehicle vectors into world Z using dot product)
     * 
     * @param oriZ output buffer
     */
    public abstract void getOrientationZ( TelemVect3 oriZ );
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot output buffer
     */
    public abstract void getLocalRotation( TelemVect3 localRot );
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel output buffer
     */
    public abstract void getLocalRotationalAcceleration( TelemVect3 localRotAccel );
    
    protected VehicleScoringInfoCapsule()
    {
    }
}
