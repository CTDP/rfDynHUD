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
package net.ctdp.rfdynhud.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * The {@link NumberUtil} keeps static methods to format numbers.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class NumberUtil
{
    private static final NumberFormat formatter = DecimalFormat.getNumberInstance( Locale.US );
    
    public static final String pad2( int number )
    {
        if ( ( number > 9 ) || ( number < 0 ) )
            return ( String.valueOf( number ) );
        
        return ( "0" + String.valueOf( number ) );
    }
    
    /**
     * Formats the given float to a String with the specified number of decimal places.
     * 
     * @param f the number to format
     * @param numDecPlaces the maximum number of decimal places
     * @param forceFractions always format with maximum number of fractions?
     * @param forceSign always show the sign?
     * 
     * @return the formatted String.
     */
    public static final String formatFloat( float f, int numDecPlaces, boolean forceFractions, boolean forceSign )
    {
        if ( numDecPlaces == 0 )
        {
            if ( forceSign )
            {
                if ( f == 0f )
                    return ( "+0" );
                
                if ( f > 0f )
                    return ( "+" + String.valueOf( Math.round( f ) ) );
                
                return ( String.valueOf( Math.round( f ) ) );
            }
            
            return ( String.valueOf( Math.round( f ) ) );
        }
        
        /*
        int p = (int)Math.pow( 10, numDecPlaces );
        
        int i = (int)f;
        String s = String.valueOf( i ) + ".";
        
        f -= i;
        f *= p;
        i = Math.round( f );
        
        s += String.valueOf( i );
        
        return ( s );
        */
        
        formatter.setMaximumFractionDigits( numDecPlaces );
        formatter.setMinimumFractionDigits( forceFractions ? numDecPlaces : 0 );
        
        if ( forceSign && ( f >= 0f ) )
        {
            return ( "+" + formatter.format( f ) );
        }
        
        return ( formatter.format( f ) );
    }
    
    /**
     * Formats the given float to a String with the specified number of decimal places.
     * 
     * @param f the number to format
     * @param numDecPlaces the maximum number of decimal places
     * @param forceFractions always format with maximum number of fractions?
     * 
     * @return the formatted String.
     */
    public static final String formatFloat( float f, int numDecPlaces, boolean forceFractions )
    {
        return ( formatFloat( f, numDecPlaces, forceFractions, false ) );
    }
    
    /**
     * Gets the next greater power-of-two for the given number.
     * 
     * @param v the value to round up
     * 
     * @return the next greater power-of-two for the given number.
     */
    public static final int roundUpPower2( int v )
    {
        switch ( Integer.bitCount( v ) )
        {
            case 0:
                return ( 1 );
            case 1:
                return ( v );
            default:
                return ( Integer.highestOneBit( v ) << 1 );
        }
    }
    
    public static final String delta( int i )
    {
        if ( i == 0 )
            return ( "+0" );
        
        if ( i > 0 )
            return ( "+" + i );
        
        return ( String.valueOf( i ) );
    }
    
    public static final String delta( float i )
    {
        if ( i == 0f )
            return ( "+0" );
        
        if ( i > 0f )
            return ( "+" + i );
        
        return ( String.valueOf( i ) );
    }
}
