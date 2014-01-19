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
package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.gamedata.Track;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

public enum LapState
{
    OUTLAP( "OL" ),
    SOMEWHERE( "SW" ),
    AFTER_SECTOR1_START( "AS1S" ),
    BEFORE_SECTOR1_END( "BS1E" ),
    AFTER_SECTOR2_START( "AS2S" ),
    BEFORE_SECTOR2_END( "BS2E" ),
    AFTER_SECTOR3_START( "AS3S" ),
    BEFORE_SECTOR3_END( "BS3E" ),
    ;
    
    public final String SHORT;
    
    public final boolean isAfterSectorStart()
    {
        return ( ( this == AFTER_SECTOR1_START ) || ( this == AFTER_SECTOR2_START ) || ( this == AFTER_SECTOR3_START ) );
    }
    
    public final boolean isBeforeSectorEnd()
    {
        return ( ( this == BEFORE_SECTOR1_END ) || ( this == BEFORE_SECTOR2_END ) || ( this == BEFORE_SECTOR3_END ) );
    }
    
    private LapState( String SHORT )
    {
        this.SHORT = SHORT;
    }
    
    public static LapState getLapState( Track track, VehicleScoringInfo vsi, float beforeSectorDistance, float afterSectorDistance )
    {
        if ( vsi.getStintLength() <= 1.0f )
            return ( OUTLAP );
        
        float lapDistance = vsi.getLapDistance();
        
        switch ( vsi.getSector() )
        {
            case 1:
                // Sometimes lapDistance and sector lenghts/track length don't match!
                if ( lapDistance > vsi.getScoringInfo().getTrackLength() / 2 )
                    return ( AFTER_SECTOR1_START );
                
                if ( lapDistance < afterSectorDistance )
                    return ( AFTER_SECTOR1_START );
                
                if ( ( track == null ) || ( lapDistance < track.getSector1Length() - beforeSectorDistance ) )
                    return ( SOMEWHERE );
                
                return ( BEFORE_SECTOR1_END );
                
            case 2:
                if ( track == null )
                    return ( SOMEWHERE );
                
                if ( lapDistance - track.getSector1Length() < afterSectorDistance )
                    return ( AFTER_SECTOR2_START );
                
                if ( lapDistance < track.getSector2Length( true ) - afterSectorDistance )
                    return ( SOMEWHERE );
                
                return ( BEFORE_SECTOR2_END );
                
            case 3:
                // Sometimes lapDistance and sector lenghts/track length don't match!
                if ( lapDistance < vsi.getScoringInfo().getTrackLength() / 2 )
                    return ( BEFORE_SECTOR3_END );
                
                if ( track == null )
                {
                    if ( lapDistance >= vsi.getScoringInfo().getTrackLength() - beforeSectorDistance )
                        return ( BEFORE_SECTOR3_END );
                    
                    return ( SOMEWHERE );
                }
                
                if ( lapDistance - track.getSector2Length( true ) < afterSectorDistance )
                    return ( AFTER_SECTOR3_START );
                
                if ( lapDistance - track.getSector2Length( true ) < track.getSector3Length() - afterSectorDistance )
                    return ( SOMEWHERE );
                
                return ( BEFORE_SECTOR3_END );
        }
        
        // Should be unreachable!
        return ( SOMEWHERE );
    }
    
    public static LapState getLapState( Track track, VehicleScoringInfo vsi )
    {
        return ( getLapState( track, vsi, 400f, 400f ) );
    }
}
