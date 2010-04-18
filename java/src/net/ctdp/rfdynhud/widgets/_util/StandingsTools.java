package net.ctdp.rfdynhud.widgets._util;

import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

public class StandingsTools
{
    // numVehicles schreiben (Rückgabewert)
    // standingsView = getView()
    //if ( getView() == StandingsView.RELATIVE_TO_ME )
    //{
    //    computeRelativeTimesRace( scoringInfo, scoringInfo.getNumVehicles(), scoringInfo.getOwnPlace() );
    //}
    //int ownLaps = scoringInfo.getVehicleScoringInfo( ownPlace - 1 ).getLapsCompleted();
    //float ownLapDistance = scoringInfo.getVehicleScoringInfo( ownPlace - 1 ).getLapDistance();
    
    public static int getDisplayedVSIsForScoring( ScoringInfo scoringInfo, StandingsView standingsView, boolean forceLeaderDisplayed, VehicleScoringInfo[] target )
    {
        final int maxDisplayedDrivers = target.length;
        
        int numVehicles = scoringInfo.getNumVehicles();
        int ownPlace = scoringInfo.getOwnPlace();
        
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
}
