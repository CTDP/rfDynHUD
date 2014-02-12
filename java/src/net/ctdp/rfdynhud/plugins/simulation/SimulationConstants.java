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
package net.ctdp.rfdynhud.plugins.simulation;

/**
 * Constants for a simulation of data events.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface SimulationConstants
{
    public static final char ON_PHYSICS = 'p';
    public static final char ON_SETUP = 'u';
    public static final char ON_TRACK = 't';
    public static final char ON_PITS_ENTERED = 'B';
    public static final char ON_PITS_EXITED = 'b';
    public static final char ON_GARAGE_ENTERED = 'G';
    public static final char ON_GARAGE_EXITED = 'g';
    public static final char ON_CONTROL = 'c';
    public static final char ON_LAP = 'l';
    public static final char ON_SESSION_STARTED = 'S';
    public static final char ON_SESSION_ENDED = 's';
    public static final char ON_COCKPIT_ENTERED = 'R';
    public static final char ON_COCKPIT_EXITED = 'r';
    public static final char ON_VIEWPORT_CHANGED = 'v';
    public static final char ON_DATA_UPDATED_GRAPHICS = 'I';
    public static final char ON_DATA_UPDATED_TELEMETRY = 'T';
    public static final char ON_DATA_UPDATED_SCORING = 'D';
    public static final char ON_DATA_UPDATED_DRIVING_AIDS = 'A';
    public static final char ON_DATA_UPDATED_COMMENTARY = 'O';
    public static final char BEFORE_RENDERED = 'W';
}
