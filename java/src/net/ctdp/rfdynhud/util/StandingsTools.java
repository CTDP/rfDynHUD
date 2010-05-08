package net.ctdp.rfdynhud.util;

import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.values.StandingsView;

public class StandingsTools
{
    /**
     * Fills the target array with {@link VehicleScoringInfo}s for all visible drivers.
     * 
     * @param scoringInfo
     * @param viewedVSI
     * @param standingsView
     * @param forceLeaderDisplayed
     * @param target
     * 
     * @return the actual number of displayed drivers.
     */
    public static int getDisplayedVSIsForScoring( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, StandingsView standingsView, boolean forceLeaderDisplayed, VehicleScoringInfo[] target )
    {
        final int maxDisplayedDrivers = target.length;
        
        int numVehicles = scoringInfo.getNumVehicles();
        int ownPlace = viewedVSI.getPlace();
        
        int i0 = 0;
        int j = 0;
        if ( maxDisplayedDrivers < numVehicles )
        {
            numVehicles = maxDisplayedDrivers;
            i0 = Math.max( 0, ownPlace - (int)Math.ceil( ( numVehicles + 1 ) / 2f ) );
            
            if ( i0 + numVehicles > scoringInfo.getNumVehicles() )
            {
                i0 -= i0 + numVehicles - scoringInfo.getNumVehicles();
                
                if ( i0 < 0 )
                {
                    numVehicles += i0;
                    i0 = 0;
                }
            }
            
            if ( ( i0 > 0 ) && forceLeaderDisplayed )
            {
                i0++;
                
                target[j++] = scoringInfo.getVehicleScoringInfo( 0 );
            }
        }
        
        int n = numVehicles - j;
        for ( int i = 0; i < n; i++ )
        {
            target[j++] = scoringInfo.getVehicleScoringInfo( i0 + i );
        }
        
        return ( numVehicles );
    }
    
    /**
     * Computes gaps for all drivers to the player.
     * 
     * @param scoringInfo
     * @param viewedVSI
     * @param relTimes target array
     */
    public static void computeRelativeTimesRace( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, float[] relTimes )
    {
        final int numVehicles = scoringInfo.getNumVehicles();
        final int ownPlace = viewedVSI.getPlace();
        relTimes[ownPlace - 1] = 0f;
        
        for ( int i = ownPlace - 2; i >= 0; i-- )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i + 1 );
            
            relTimes[i] = relTimes[i + 1] + vsi.getTimeBehindNextInFront();
        }
        
        for ( int i = ownPlace; i < numVehicles; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            relTimes[i] = relTimes[i - 1] + -vsi.getTimeBehindNextInFront();
        }
    }
}
