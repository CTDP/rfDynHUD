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
package net.ctdp.rfdynhud.editor.live;

/**
 * Keeps constants for network commands.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface LiveConstants extends net.ctdp.rfdynhud.plugins.datasender.CommunicatorConstants
{
    public static final short GRAPHICS_INFO = 2100;
    public static final short TELEMETRY_DATA = 2200;
    public static final short SCORING_INFO = 2300;
    public static final short WEATHER_INFO = 2400;
    public static final short DRIVING_AIDS = 2500;
    public static final short DRIVING_AIDS_STATE_CHANGED = 2510;
    public static final short COMMENTARY_REQUEST_INFO = 2600;
}
