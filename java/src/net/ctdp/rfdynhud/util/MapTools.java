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
package net.ctdp.rfdynhud.util;

import java.util.Arrays;
import java.util.Comparator;

import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

public class MapTools
{
    private static final class LapDistanceComparator implements Comparator<VehicleScoringInfo>
    {
        private VehicleScoringInfo viewedVSI = null;
        private float trackLength = 0f;
        
        @Override
        public int compare( VehicleScoringInfo vsi1, VehicleScoringInfo vsi2 )
        {
            float ld0 = viewedVSI.getLapDistance();
            float ld1 = vsi1.getLapDistance();
            float ld2 = vsi2.getLapDistance();
            
            float d1, d2;
            
            if ( ld1 > ld0 )
                d1 = Math.min( ld1 - ld0, trackLength - ld1 + ld0 );
            else
                d1 = Math.min( ld0 - ld1, trackLength - ld0 + ld1 );
            
            if ( ld2 > ld0 )
                d2 = Math.min( ld2 - ld0, trackLength - ld2 + ld0 );
            else
                d2 = Math.min( ld0 - ld2, trackLength - ld0 + ld2 );
            
            if ( d1 < d2 )
                return ( -1 );
            
            if ( d1 > d2 )
                return ( +1 );
            
            return ( 0 );
        }
    }
    
    private static final LapDistanceComparator LAP_DISTANCE_COMPARATOR = new LapDistanceComparator();
    
    private static final boolean vsiConsumed( VehicleScoringInfo vsi, VehicleScoringInfo viewedVSI, boolean useClassScoring, boolean forceLeaderDisplayed )
    {
        if ( vsi.equals( viewedVSI ) )
        {
            // already consumed above
        }
        else if ( vsi.equals( viewedVSI.getNextInFront( useClassScoring ) ) )
        {
            // already consumed above
        }
        else if ( vsi.equals( viewedVSI.getNextBehind( useClassScoring ) ) )
        {
            // already consumed above
        }
        else if ( forceLeaderDisplayed && ( vsi.getPlace( useClassScoring ) == 1 ) && ( !useClassScoring || ( vsi.getVehicleClassId() == viewedVSI.getVehicleClassId() ) ) )
        {
            // already consumed above
        }
        else// if ( /*!vsi.isInPits() &&*/ ( vsi.getFinishStatus().isNone() || vsi.getFinishStatus().isFinished() ) )
        {
            return ( false );
        }
        
        return ( true );
    }
    
    private static final boolean finishStatusOk( final VehicleScoringInfo vsi )
    {
        return ( vsi.getFinishStatus().isNone() || vsi.getFinishStatus().isFinished() );
    }
    
    /*
    private static int getDisplayedVSIsForMap_all( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, boolean forceLeaderDisplayed, VehicleScoringInfo[] target )
    {
        final short ownPlace = viewedVSI.getPlace( false );
        
        int n = Math.min( scoringInfo.getNumVehicles(), target.length );
        int result = n;
        
        int i0 = Math.max( 0, ownPlace - (int)Math.ceil( ( n + 1 ) / 2f ) );
        //int i1 = Math.min( i0 + n - 1, n - 1 );
        int i1 = i0 + n - 1;
        
        target[--n] = viewedVSI;
        
        VehicleScoringInfo nif = viewedVSI.getNextInFront( false );
        if ( nif != null )
        {
            target[--n] = nif;
        }
        
        VehicleScoringInfo nb = viewedVSI.getNextBehind( false );
        if ( nb != null )
        {
            target[--n] = nb;
        }
        
        if ( ( ownPlace > 1 ) && ( ( i0 == 0 ) || forceLeaderDisplayed ) )
        {
            target[--n] = scoringInfo.getLeadersVehicleScoringInfo();
            if ( i0 == 0 )
                i0++;
        }
        
        int n2 = Math.max( ( ownPlace - 1 ) - i0, i1 - ( ownPlace - 1 ) );
        for ( int i = 0; i < n2; i++ )
        {
            int j = ownPlace - 1 - i - 1;
            if ( ( n > 0 ) && ( j >= i0 ) )
            {
                VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( j );
                
                if ( !vsiConsumed( vsi, viewedVSI, false ) )
                {
                    target[--n] = vsi;
                }
            }
            
            j = ownPlace - 1 + i + 1;
            if ( ( n > 0 ) && ( j <= i1 ) )
            {
                VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( j );
                
                if ( !vsiConsumed( vsi, viewedVSI, false ) )
                {
                    target[--n] = vsi;
                }
            }
        }
        
        // Should never do anything. But just in case...
        for ( int i = i1 + 1; i < n; i++ )
        {
            target[i] = null;
        }
        
        return ( result );
    }
    */
    
    private static VehicleScoringInfo[] vsis = new VehicleScoringInfo[ 32 ];
    
    public static int getDisplayedVSIsForMap( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, boolean useClassScoring, boolean forceLeaderDisplayed, VehicleScoringInfo[] target )
    {
        if ( target == null )
            throw new NullPointerException( "target array must not be null." );
        
        if ( !forceLeaderDisplayed && ( target.length < 3 ) )
            throw new ArrayIndexOutOfBoundsException( "target array must be at least of size 3." );
        
        if ( forceLeaderDisplayed && ( target.length < 4 ) )
            throw new ArrayIndexOutOfBoundsException( "target array must be at least of size 4." );
        
        int n = Math.min( scoringInfo.getNumVehicles(), target.length );
        int result = n;
        
        if ( n > 0 )
        {
            target[--n] = viewedVSI;
        }
        
        VehicleScoringInfo nif = viewedVSI.getNextInFront( useClassScoring );
        if ( ( n > 0 ) && ( nif != null ) && finishStatusOk( nif ) )
        {
            target[--n] = nif;
        }
        
        VehicleScoringInfo nb = viewedVSI.getNextBehind( useClassScoring );
        if ( ( n > 0 ) && ( nb != null ) && finishStatusOk( nb ) )
        {
            target[--n] = nb;
        }
        
        int leaderIndex = n - 1;
        if ( forceLeaderDisplayed )
        {
            VehicleScoringInfo leader = useClassScoring ? viewedVSI.getLeaderByClass() : scoringInfo.getLeadersVehicleScoringInfo();
            if ( ( n > 0 ) && ( leader != null ) && finishStatusOk( leader ) )
            {
                target[--n] = leader;
            }
        }
        
        if ( ( vsis == null ) || ( vsis.length != scoringInfo.getNumVehicles() ) )
        {
            vsis = new VehicleScoringInfo[ scoringInfo.getNumVehicles() ];
        }
        
        scoringInfo.getVehicleScoringInfos( vsis );
        LAP_DISTANCE_COMPARATOR.trackLength = scoringInfo.getTrackLength();
        LAP_DISTANCE_COMPARATOR.viewedVSI = viewedVSI;
        Arrays.sort( vsis, LAP_DISTANCE_COMPARATOR );
        
        for ( int i = 0; ( i < vsis.length ) && ( n > 0 ); i++ )
        {
            VehicleScoringInfo vsi = vsis[i];
            
            if ( !vsiConsumed( vsi, viewedVSI, useClassScoring, forceLeaderDisplayed ) && finishStatusOk( vsi ) )
            {
                if ( vsi.getPlace( useClassScoring ) == 1 )
                {
                    System.arraycopy( target, n, target, n - 1, leaderIndex - n + 1 );
                    target[leaderIndex] = vsi;
                    n--;
                }
                else
                {
                    target[--n] = vsi;
                }
            }
        }
        
        // Should never do anything. But just in case...
        for ( int i = 0; i < n; i++ )
        {
            target[i] = null;
        }
        
        return ( result );
    }
}
