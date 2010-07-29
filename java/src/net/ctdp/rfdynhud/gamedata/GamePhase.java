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
 * @author Marvin Froehlich (CTDP)
 */
public enum GamePhase
{
    BEFORE_SESSION_HAS_BEGUN, // 0
    RECONNAISSANCE_LAPS, // 1 (race only)
    GRID_WALK_THROUGH, // 2 (race only)
    FORMATION_LAP, // 3 (race only)
    STARTING_LIGHT_COUNTDOWN_HAS_BEGUN, // 4 (race only)
    GREEN_FLAG, // 5
    FULL_COURSE_YELLOW, // 6 (safety car)
    SESSION_STOPPED, // 7
    SESSION_OVER, // 8
    ;
}
