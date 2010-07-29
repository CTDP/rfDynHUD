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
package net.ctdp.rfdynhud.render;

/**
 * The manager provides static final fields for the used byte order.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ByteOrderManager
{
    public static final int RED = ByteOrderInitializer.offsetRed;
    public static final int GREEN = ByteOrderInitializer.offsetGreen;
    public static final int BLUE = ByteOrderInitializer.offsetBlue;
    public static final int ALPHA = ByteOrderInitializer.offsetAlpha;
    
    public static void dump()
    {
        System.out.println( "Byte order: RED = " + RED + ", GREEN = " + GREEN + ", BLUE = " + BLUE + ", ALPHA = " + ALPHA );
    }
}
