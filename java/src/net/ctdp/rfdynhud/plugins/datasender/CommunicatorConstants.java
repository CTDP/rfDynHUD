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
    public static final short CONNECTION_REQUEST = 10;
    public static final short CONNECTION_REQUEST2 = 11;
    public static final short SERVER_NAME = 20;
    public static final short REQUEST_PASSWORD = 30;
    public static final short PASSWORD_HASH = 40;
    public static final short PASSWORD_MISMATCH = 50;
    public static final short CONNECTION_REFUSED = 60;
    public static final short CONNECTION_ESTEBLISHED = 70;
    public static final short CONNECTION_CLOSED = 80;
    
    public static final short ON_PITS_ENTERED = 1000;
    public static final short ON_PITS_EXITED = 1001;
    public static final short ON_GARAGE_ENTERED = 1002;
    public static final short ON_GARAGE_EXITED = 1003;
    public static final short ON_VEHICLE_CONTROL_CHANGED = 1004;
    public static final short ON_LAP_STARTED = 1005;
    public static final short ON_SESSION_STARTED = 1006;
    public static final short ON_COCKPIT_ENTERED = 1007;
    public static final short ON_GAME_PAUSE_STATE_CHANGED = 1008;
    public static final short ON_COCKPIT_EXITED = 1009;
    public static final short ON_PLAYER_JOINED = 1010;
    public static final short ON_PLAYER_LEFT = 1011;
    
    public static final short SESSION_TIME = 2000;
}
