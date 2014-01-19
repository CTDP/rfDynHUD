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
package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public enum VehicleControl
{
    NOBODY( -1 ),
    LOCAL_PLAYER( 0 ),
    LOCAL_AI( 1 ),
    REMOTE( 2 ),
    REPLAY( 3 ),
    ;
    
    @SuppressWarnings( "unused" )
    private final byte ISI_VALUE;
    
    public final boolean isLocalPlayer()
    {
        return ( this == LOCAL_PLAYER );
    }
    
    private VehicleControl( int isi_value )
    {
        this.ISI_VALUE = (byte)isi_value;
    }
}
