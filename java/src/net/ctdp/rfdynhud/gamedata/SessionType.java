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
public enum SessionType
{
    TEST_DAY,
    PRACTICE1,
    PRACTICE2,
    PRACTICE3,
    PRACTICE4,
    QUALIFYING1,
    QUALIFYING2,
    QUALIFYING3,
    QUALIFYING4,
    WARMUP,
    RACE1,
    RACE2,
    RACE3,
    RACE4,
    ;
    
    public static final String PRACTICE_WILDCARD = "PRACTICE";
    public static final String QUALIFYING_WILDCARD = "QUALIFYING";
    public static final String RACE_WILDCARD = "RACE";
    
    public final boolean isTestDay()
    {
        return ( this == TEST_DAY );
    }
    
    public final boolean isPractice()
    {
        return ( ( this == PRACTICE1 ) || ( this == PRACTICE2 ) || ( this == PRACTICE3 ) || ( this == PRACTICE4 ) );
    }
    
    public final boolean isQualifying()
    {
        return ( ( this == QUALIFYING1 ) || ( this == QUALIFYING2 ) || ( this == QUALIFYING3 ) || ( this == QUALIFYING4 ) );
    }
    
    public final boolean isRace()
    {
        return ( ( this == RACE1 ) || ( this == RACE2 ) || ( this == RACE3 ) || ( this == RACE4 ) );
    }
}
