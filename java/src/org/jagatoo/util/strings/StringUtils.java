/**
 * Copyright (c) 2007-2011, JAGaToo Project Group all rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of the 'Xith3D Project Group' nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 */
package org.jagatoo.util.strings;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Utility methods for Strings.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public final class StringUtils
{
    /**
     * Checks, whether the given String argument is intepretable as numeric value.
     * 
     * @param str
     * 
     * @return true or false
     */
    public static final boolean isNumeric( String str )
    {
        if ( ( str == null ) || ( str.length() == 0 ) )
            return ( false );
        
        for ( int i = 0; i < str.length(); i++ )
        {
            final char ch = str.charAt( i );
            
            if ( ( !Character.isDigit( ch ) ) && ( ch != '.' ) )
            {
                if ( ( i > 0 ) && ( ch != '-' ) && ( ch != '+' ) )
                    return ( false );
            }
        }
        
        return ( true );
    }
    
    /**
     * Checks, whether the given String argument is intepretable as boolean value.
     * 
     * @param str
     * 
     * @return true or false
     */
    public static final boolean isBoolean( String str )
    {
        if ( ( str == null ) || ( str.length() == 0 ) )
            return ( false );
        
        return ( str.equalsIgnoreCase( "true" ) || str.equalsIgnoreCase( "false" ) );
    }
    
    public static final String padLeft( int number, int length, String padStr )
    {
        String s = String.valueOf( number );
        
        while ( s.length() < length )
            s = padStr + s;
        
        return ( s );
    }
    
    public static final boolean startsWithIgnoreCase( String s, String test, int offset )
    {
        int l = test.length();
        if ( l > s.length() - offset )
            return ( false );
        
        for ( int i = 0; i < l; i++ )
        {
            if ( Character.toLowerCase( s.charAt( offset + i ) ) != Character.toLowerCase( test.charAt( i ) ) )
                return ( false );
        }
        
        return ( true );
    }
    
    public static final boolean startsWithIgnoreCase( String s, String test )
    {
        return ( startsWithIgnoreCase( s, test, 0 ) );
    }
    
    public static final boolean endsWithIgnoreCase( String s, String test )
    {
        int l = test.length();
        if ( l > s.length() )
            return ( false );
        
        int offset = s.length() - test.length();
        for ( int i = 0; i < l; i++ )
        {
            if ( Character.toLowerCase( s.charAt( offset + i ) ) != Character.toLowerCase( test.charAt( i ) ) )
                return ( false );
        }
        
        return ( true );
    }
    
    public static final int findFirstNonWhitespace( String s, int offset )
    {
        for ( int i = offset; i < s.length(); i++ )
        {
            if ( !Character.isWhitespace( s.charAt( i ) ) )
                return ( i );
        }
        
        return ( -1 );
    }
    
    public static final int findFirstNonWhitespace( String s )
    {
        return ( findFirstNonWhitespace( s, 0 ) );
    }
    
    public static final int findLastNonWhitespace( String s )
    {
        for ( int i = s.length() - 1; i >= 0; i-- )
        {
            if ( !Character.isWhitespace( s.charAt( i ) ) )
                return ( i );
        }
        
        return ( -1 );
    }
    
    public static final String unquoteString( String s )
    {
        if ( s.charAt( 0 ) == '\"' )
        {
            if ( s.charAt( s.length() - 1 ) == '\"' )
            {
                return ( s.substring( 1, s.length() - 1 ) );
            }
            
            return ( s.substring( 1, s.length() - 0 ) );
        }
        
        if ( s.charAt( s.length() - 1 ) == '\"' )
        {
            return ( s.substring( 1, s.length() - 1 ) );
        }
        
        return ( s );
    }
    
    public static final String loadString( URL url )
    {
        if ( url == null )
            return ( "" );
        
        StringBuilder sb = new StringBuilder();
        
        BufferedInputStream in = null;
        try
        {
            int c;
            in = new BufferedInputStream( url.openStream() );
            while ( ( c = in.read() ) >= 0 )
            {
                sb.append( (char)c );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( in != null )
                try { in.close(); } catch ( IOException e2 ) {}
        }
        
        return ( sb.toString() );
    }
    
    private StringUtils()
    {
    }
}
