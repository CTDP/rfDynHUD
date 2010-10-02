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
package net.ctdp.rfdynhud.util;

import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.values.StandingsView;

public class StandingsTools
{
    /**
     * Fills the target array with {@link VehicleScoringInfo}s for all visible drivers.
     * 
     * @param scoringInfo the scoring info
     * @param viewedVSI the currently viewed vehicle
     * @param useClassScoring use class relative scoring?
     * @param standingsView the standings view
     * @param forceLeaderDisplayed force leader displayed, even if far away?
     * @param target the target array
     * 
     * @return the actual number of displayed drivers.
     */
    public static int getDisplayedVSIsForScoring( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, boolean useClassScoring, StandingsView standingsView, boolean forceLeaderDisplayed, VehicleScoringInfo[] target )
    {
        final int maxDisplayedDrivers = target.length;
        
        if ( maxDisplayedDrivers == 0 )
            return ( 0 );
        
        int numDispVehicles = scoringInfo.getNumVehicles();
        if ( useClassScoring )
        {
            int n = numDispVehicles;
            numDispVehicles = 0;
            for ( int i = 0; i < n; i++ )
            {
                if ( scoringInfo.getVehicleScoringInfo( i ).getVehicleClassId() == viewedVSI.getVehicleClassId() )
                    numDispVehicles++;
            }
        }
        int numClassVehicles = numDispVehicles;
        
        VehicleScoringInfo[] vsis = new VehicleScoringInfo[ numClassVehicles ];
        int j = 0;
        for ( int i = 0; i < scoringInfo.getNumVehicles(); i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            if ( !useClassScoring || ( vsi.getVehicleClassId() == viewedVSI.getVehicleClassId() ) )
                vsis[j++] = vsi;
        }
        
        int i0 = 0;
        j = 0;
        if ( maxDisplayedDrivers < numDispVehicles )
        {
            numDispVehicles = maxDisplayedDrivers;
            i0 = Math.max( 0, viewedVSI.getPlace( useClassScoring ) - (int)Math.ceil( ( numDispVehicles + 1 ) / 2f ) );
            
            if ( i0 + numDispVehicles > numClassVehicles )
            {
                i0 -= i0 + numDispVehicles - numClassVehicles;
                
                if ( i0 < 0 )
                {
                    numDispVehicles += i0;
                    i0 = 0;
                }
            }
            
            if ( ( i0 > 0 ) && forceLeaderDisplayed )
            {
                i0++;
                
                target[j++] = vsis[0];
            }
        }
        
        int n = numDispVehicles - j;
        for ( int i = 0; i < n; i++ )
        {
            target[j++] = vsis[i0 + i];
        }
        
        return ( numDispVehicles );
    }
    
    /**
     * Computes gaps for all drivers to the player.
     * 
     * @param scoringInfo the scoring info
     * @param viewedVSI the currently viewed vehicle
     * @param relTimes target array
     */
    public static void computeRaceGapsRelativeToPosition( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, float[] relTimes )
    {
        final int numVehicles = scoringInfo.getNumVehicles();
        final int ownPlace = viewedVSI.getPlace( false );
        relTimes[ownPlace - 1] = 0f;
        
        for ( int i = ownPlace - 2; i >= 0; i-- )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i + 1 );
            
            relTimes[i] = relTimes[i + 1] + vsi.getTimeBehindNextInFront( false );
        }
        
        for ( int i = ownPlace; i < numVehicles; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            relTimes[i] = relTimes[i - 1] + -vsi.getTimeBehindNextInFront( false );
        }
    }
}
