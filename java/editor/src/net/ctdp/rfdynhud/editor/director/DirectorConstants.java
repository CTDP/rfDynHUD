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
package net.ctdp.rfdynhud.editor.director;

/**
 * Keeps constants for network commands.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorConstants extends net.ctdp.rfdynhud.plugins.CommunicatorConstants
{
    public static final int WIDGETS_CONFIGURATION = OFFSET + 10000;
    public static final int RESET_WIDGET_STATES = OFFSET + 10001;
    public static final int WIDGET_STATE = OFFSET + 10002;
    public static final int DRIVERS_LIST = OFFSET + 10003;
    public static final int DRIVERS_POSITIONS = OFFSET + 10004;
}
