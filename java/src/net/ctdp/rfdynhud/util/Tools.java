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

public class Tools
{
    public static final Number getNumber( String string )
    {
        try
        {
            return ( Integer.parseInt( string ) );
        }
        catch ( NumberFormatException e )
        {
            try
            {
                return ( Double.parseDouble( string ) );
            }
            catch ( NumberFormatException e2 )
            {
                return ( null );
            }
        }
    }
    
    public static final boolean objectsEqual( Object o1, Object o2 )
    {
        if ( o1 == o2 )
            return ( true );
        
        if ( ( o1 == null ) && ( o2 != null ) )
            return ( false );
        
        if ( ( o1 != null ) && ( o2 == null ) )
            return ( false );
        
        return ( o1.equals( o2 ) );
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public static final int compareObjects( Comparable o1, Comparable o2 )
    {
        if ( o1 == o2 )
            return ( 0 );
        
        if ( o1 == null )
            return ( -1 );
        
        if ( o2 == null )
            return ( +1 );
        
        return ( o1.compareTo( o2 ) );
    }
    
    public static final String padLeft( int number, int length, String padStr )
    {
        String s = String.valueOf( number );
        
        while ( s.length() < length )
            s = padStr + s;
        
        return ( s );
    }
}
