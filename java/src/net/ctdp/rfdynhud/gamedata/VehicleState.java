/**
 * Copyright (C) 2009-2011 Cars and Tracks Development Project (CTDP).
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

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public enum VehicleState
{
    PITTING,
    PITLANE,
    ON_TRACK,
    SLOWER,
    PERS_FASTEST,
    ABS_FASTEST,
    ;
    
    /**
     * The velocity in m/s near zero to consider a vehicle to be standing when below.
     */
    public static final float NEAR_ZERO_VELOCITY = 0.1f;
    
    public final boolean isPitting()
    {
        return ( this == PITTING );
    }
    
    public final boolean isInPitlane()
    {
        return ( ( this == PITTING ) || ( this == PITLANE ) );
    }
    
    public final boolean isOnTrack()
    {
        return ( ( this == ON_TRACK ) || ( this == SLOWER ) || ( this == PERS_FASTEST ) || ( this == ABS_FASTEST ) );
    }
    
    /**
     * Gets the current {@link VehicleState} for the given vehicle.
     * 
     * @param vsi the vehicle to get the state for
     * @param slowerFasterDelay the delay in seconds after lap start to display slower or faster state for
     * @return the current {@link VehicleState} for the given vehicle.
     */
    public static final VehicleState get( VehicleScoringInfo vsi, float slowerFasterDelay )
    {
        VehicleState state = ON_TRACK;
        
        if ( vsi.isInPits() )
        {
            if ( Math.abs( vsi.getScalarVelocityMS() ) < 0.1f )
                state = PITTING;
            else
                state = PITLANE;
        }
        else
        {
            ScoringInfo scoringInfo = vsi.getScoringInfo();
            
            Laptime persFastest = vsi.getFastestLaptime();
            if ( ( persFastest != null ) && persFastest.isFinished() && ( persFastest.getLap() == vsi.getLapsCompleted() ) && ( scoringInfo.getSessionTime() - vsi.getLapStartTime() < slowerFasterDelay ) )
            {
                VehicleScoringInfo absFastestVSI = scoringInfo.getFastestLapVSI();
                
                //if ( ( absFastest != null ) && ( persFastest.getDriverId() == absFastest.getDriverId() ) )
                if ( ( absFastestVSI != null ) && ( absFastestVSI.getDriverId() == vsi.getDriverId() ) )
                    state = ABS_FASTEST;
                else
                    state = PERS_FASTEST;
            }
            else if ( ( persFastest != null ) && persFastest.isFinished() && ( persFastest.getLap() != vsi.getLapsCompleted() ) && ( scoringInfo.getSessionTime() - vsi.getLapStartTime() < slowerFasterDelay ) )
            {
                state = SLOWER;
            }
        }
        
        return ( state );
    }
}
