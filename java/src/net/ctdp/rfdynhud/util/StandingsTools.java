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
     * @param useClassScoring
     * @param standingsView
     * @param forceLeaderDisplayed
     * @param target
     * 
     * @return the actual number of displayed drivers.
     */
    public static int getDisplayedVSIsForScoring( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, boolean useClassScoring, StandingsView standingsView, boolean forceLeaderDisplayed, VehicleScoringInfo[] target )
    {
        final int maxDisplayedDrivers = target.length;
        
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
     * @param scoringInfo
     * @param viewedVSI
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
