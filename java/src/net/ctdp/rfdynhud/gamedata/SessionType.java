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
public enum SessionType
{
    TEST_DAY, // 0
    PRACTICE1, // 1
    PRACTICE2, // 2
    PRACTICE3, // 3
    PRACTICE4, // 4
    QUALIFYING, // 5
    WARMUP, // 6
    RACE, // 7
    ;
    
    public static final String PRACTICE_WILDCARD = "PRACTICE";
    
    public final boolean isTestDay()
    {
        return ( this == TEST_DAY );
    }
    
    public final boolean isPractice()
    {
        return ( ( this == PRACTICE1 ) || ( this == PRACTICE2 ) || ( this == PRACTICE3 ) || ( this == PRACTICE4 ) );
    }
    
    public final boolean isRace()
    {
        return ( this == RACE );
    }
}
