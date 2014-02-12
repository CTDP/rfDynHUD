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
package net.ctdp.rfdynhud.plugins.datasender;

/**
 * Keeps constants for network commands.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface CommunicatorConstants
{
    public static final int OFFSET = 0xFFFF;
    
    public static final int CONNECTION_REQUEST = OFFSET + 1;
    public static final int REQUEST_PASSWORD = OFFSET + 2;
    public static final int PASSWORD_HASH = OFFSET + 3;
    public static final int PASSWORD_MISMATCH = OFFSET + 4;
    public static final int CONNECTION_REFUSED = OFFSET + 5;
    public static final int CONNECTION_ESTEBLISHED = OFFSET + 6;
    public static final int CONNECTION_CLOSED = OFFSET + 7;
    
    public static final int ON_PITS_ENTERED = OFFSET + 1000;
    public static final int ON_PITS_EXITED = OFFSET + 1001;
    public static final int ON_GARAGE_ENTERED = OFFSET + 1002;
    public static final int ON_GARAGE_EXITED = OFFSET + 1003;
    public static final int ON_VEHICLE_CONTROL_CHANGED = OFFSET + 1004;
    public static final int ON_LAP_STARTED = OFFSET + 1005;
    public static final int ON_SESSION_STARTED = OFFSET + 1006;
    public static final int ON_COCKPIT_ENTERED = OFFSET + 1007;
    public static final int ON_GAME_PAUSE_STATE_CHANGED = OFFSET + 1008;
    public static final int ON_COCKPIT_EXITED = OFFSET + 1009;
    public static final int ON_PLAYER_JOINED = OFFSET + 1010;
    public static final int ON_PLAYER_LEFT = OFFSET + 1011;
    
    public static final int SESSION_TIME = OFFSET + 2000;
}
