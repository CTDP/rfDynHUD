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
package net.ctdp.rfdynhud.util;

/**
 * Provides static methods to deal with timing.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TimingUtil
{
    private static final String pad2( int i )
    {
        if ( i >= 10 )
            return ( String.valueOf( i ) );
        
        return ( "0" + String.valueOf( i ) );
    }
    
    private static final String pad3( int i )
    {
        if ( i >= 100 )
            return ( String.valueOf( i ) );
        
        if ( i >= 10 )
            return ( "0" + String.valueOf( i ) );
        
        return ( "00" + String.valueOf( i ) );
    }
    
    /**
     * Gets a formatted String from the given seconds.
     * 
     * @param seconds the seconds to format to a time string
     * @param forceAllFields show hours or minutes, even, if they are all zero?
     * @param padHighest if true, the highest displayed field (i.e. hours or minuts) will be padded with a zero
     * @param showMillis show milli seconds?
     * 
     * @return a formatted String from the given seconds.
     */
    public static String getTimeAsString( float seconds, boolean forceAllFields, boolean padHighest, boolean showMillis )
    {
        if ( showMillis )
            seconds = Math.round( seconds * 1000f ) / 1000f;
        else
            seconds = Math.round( seconds );
        
        String str = "";
        
        if ( seconds < 0 )
        {
            str += "-";
            seconds *= -1f;
        }
        
        int hours = (int)( seconds / 3600f );
        
        if ( ( hours > 0 ) || forceAllFields )
        {
            if ( padHighest )
                str += pad2( hours ) + ":";
            else
                str += String.valueOf( hours ) + ":";
        }
        
        int minutes = (int)( ( seconds / 60f ) % 60f );
        
        if ( ( hours > 0 ) || ( minutes > 0 ) || forceAllFields )
        {
            if ( ( hours > 0 ) || padHighest )
                str += pad2( minutes ) + ":";
            else
                str += minutes + ":";
        }
        
        int restSeconds = showMillis ? (int)( seconds % 60f ) : Math.round( seconds % 60f );
        
        if ( ( hours > 0 ) || ( minutes > 0 ) || padHighest )
            str += pad2( restSeconds );
        else
            str += String.valueOf( restSeconds );
        
        if ( showMillis )
        {
            int millis = Math.round( ( seconds * 1000f ) % 1000f );
            
            str += "." + pad3( millis );
        }
        
        return ( str );
    }
    
    /**
     * Gets a formatted String from the given seconds.
     * 
     * @param seconds the seconds to format to a time string
     * @param forceAllFields show hours or minutes, even, if they are all zero?
     * @param showMillis show milli seconds?
     * 
     * @return a formatted String from the given seconds.
     */
    public static String getTimeAsString( float seconds, boolean forceAllFields, boolean showMillis )
    {
        return ( getTimeAsString( seconds, forceAllFields, true, showMillis ) );
    }
    
    /**
     * Gets a formatted String from the given seconds.
     * 
     * @param seconds the seconds to format to a time string
     * @param showMillis show milli seconds?
     * 
     * @return a formatted String from the given seconds.
     */
    public static String getTimeAsString( float seconds, boolean showMillis )
    {
        return ( getTimeAsString( seconds, false, showMillis ) );
    }
    
    /**
     * Gets a formatted String from the given seconds.
     * 
     * @param seconds the seconds to format to a time string
     * 
     * @return a formatted String from the given seconds.
     */
    public static String getTimeAsLaptimeString( float seconds )
    {
        return ( getTimeAsString( seconds, false, false, true ) );
    }
    
    public static String getTimeAsGapString( float seconds )
    {
        if ( seconds >= 0f )
            return ( "+" + getTimeAsString( seconds, false, false, true ) );
        
        return ( getTimeAsString( seconds, false, false, true ) );
    }
}
