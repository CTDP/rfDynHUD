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

/**
 * 
 * @author Marvin Froehlich
 */
public enum SurfaceType
{
    DRY, // 0
    WET, // 1
    GRASS, // 2
    DIRTY, // 3
    GRAVEL, // 4
    RUMBLESTRIP, // 5
    ;
    
    public static final SurfaceType getFromIndex( final short index )
    {
        switch ( index )
        {
            case 0:
                return ( DRY );
            case 1:
                return ( WET );
            case 2:
                return ( GRASS );
            case 3:
                return ( DIRTY );
            case 4:
                return ( GRAVEL );
            case 5:
                return ( RUMBLESTRIP );
        }
        
        // Unreachable code!
        return ( null );
    }
}
