/**
 * Copyright (c) 2007-2009, OpenMaLi Project Group all rights reserved.
 * 
 * Portions based on the Sun's javax.vecmath interface, Copyright by Sun
 * Microsystems or Kenji Hiranabe's alternative GC-cheap implementation.
 * Many thanks to the developers.
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
 * Neither the name of the 'OpenMaLi Project Group' nor the names of its
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
package org.openmali.vecmath2.util;

import java.awt.Color;

/**
 * Methods to convert from hex to colors and vice versa.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public final class ColorUtils
{
    public static final java.awt.Color BLACK_TRANSPARENT = new java.awt.Color( 0, 0, 0, 0 );
    public static final int ALPHA_OPAQUE = 255;
    
    /**
     * Convertes the char interpreded as hex to its corresponding int. Returns
     * -1, if the char is not a hex char.
     * 
     * @param ch
     * 
     * @return the corresponding int or -1
     */
    public static final int hexToInt( char ch )
    {
        if ( ( ch >= 'A' ) && ( ch <= 'Z' ) )
        {
            return ( (int)ch - 55 );
        }
        
        if ( ( ch >= 'a' ) && ( ch <= 'z' ) )
        {
            return ( (int)ch - 87 );
        }
        
        if ( ( ch >= '0' ) && ( ch <= '9' ) )
        {
            return ( (int)ch - 48 );
        }
        
        return ( -1 );
    }
    
    private static final int calcColorComp( String str, int start, boolean throwException )
    {
        final int h = hexToInt( str.charAt( start + 0 ) );
        final int l = hexToInt( str.charAt( start + 1 ) );
        
        if ( ( h == -1 ) || ( l == -1 ) )
        {
            if ( throwException )
                throw new IllegalArgumentException( "given hex string is not valid" );
            
            return ( -1 );
        }
        
        return ( ( h * 16 ) + l );
    }
    
    /**
     * Checks the hex-string for validity.
     * 
     * @param hexStr
     * 
     * @return 0 for RGB hex-string without leading #,
     *         1 for RGB hex-string with leading #,
     *         2 for RGBA hex-string without leading #,
     *         3 for RGBA hex-string with leading #
     */
    public static final int checkHexString( String hexStr, boolean throwException )
    {
        if ( hexStr == null )
        {
            if ( throwException )
                throw new NullPointerException( "hexStr" );
            
            return ( -1 );
        }
        
        int type = -1;
        
        if ( hexStr.startsWith( "#" ) )
        {
            if ( hexStr.length() == 7 )
                type = 1;
            else if ( hexStr.length() == 9 )
                type = 3;
            else if ( throwException )
                throw new IllegalArgumentException( "Illegal hex-string." );
            else
                return ( -2 );
        }
        else
        {
            if ( hexStr.length() == 6 )
                type = 0;
            else if ( hexStr.length() == 8 )
                type = 2;
            else if ( throwException )
                throw new IllegalArgumentException( "Illegal hex-string (" + hexStr + ")." );
            else
                return ( -2 );
        }
        
        return ( type );
    }
    
    /**
     * Checks the hex-string for validity.
     * 
     * @param hexStr
     * 
     * @return 0 for RGB hex-string without leading #,
     *         1 for RGB hex-string with leading #,
     *         2 for RGBA hex-string without leading #,
     *         3 for RGBA hex-string with leading #
     */
    public static final int checkHexString( String hexStr )
    {
        return ( checkHexString( hexStr, true ) );
    }
    
    public static final boolean hexToColor( String hexStr, boolean throwException, int[] buffer )
    {
        int type = checkHexString( hexStr, throwException );
        if ( type < 0 )
            return ( false );
        int start = ( ( type & 1 ) != 0 ) ? 1 : 0;
        
        if ( ( ( type & 2 ) != 0 ) && buffer.length < 4 )
        {
            if ( throwException )
                throw new IllegalArgumentException( "buffer too small" );
            
            return ( false );
        }
        
        int j = 0;
        for ( int i = start; i < hexStr.length(); i += 2, j++ )
        {
            int c = calcColorComp( hexStr, start + j * 2, throwException );
            if ( c < 0 )
                return ( false );
            
            buffer[j] = c;
        }
        
        if ( ( type & 2 ) != 0 ) // has alpha?
        {
            return ( true );
        }
        
        if ( buffer.length >= 4 )
            buffer[3] = ALPHA_OPAQUE;
        
        return ( true );
    }
    
    public static final boolean hexToColor( String hexStr, int[] buffer )
    {
        return ( hexToColor( hexStr, true, buffer ) );
    }
    
    /*
    public static final Color hexToColor( String hexStr, Color out )
    {
        int type = checkHexString( hexStr );
        int start = ( ( type & 1 ) != 0 ) ? 1 : 0;
        
        out.setRed( calcColorComp( hexStr, start + 0 ) / 255f );
        out.setGreen( calcColorComp( hexStr, start + 2 ) / 255f );
        out.setBlue( calcColorComp( hexStr, start + 4 ) / 255f );
        if ( ( type & 2 ) != 0 )
            out.setAlpha( calcColorComp( hexStr, start + 6 ) / 255f );
        else
            out.setAlpha( -1f );
        
        return ( out );
    }
    */
    
    public static final Color hexToColor( String hexStr, boolean throwException )
    {
        int[] values = new int[ 4 ];
        
        values[ 3 ] = ALPHA_OPAQUE;
        
        if ( !hexToColor( hexStr, throwException, values ) )
            return ( null );
        
        if ( values[ 3 ] != ALPHA_OPAQUE )
            return ( new Color( values[ 0 ], values[ 1 ], values[ 2 ], values[ 3 ] ) );
        
        return ( new Color( values[ 0 ], values[ 1 ], values[ 2 ] ) );
    }
    
    public static final Color hexToColor( String hexStr )
    {
        return ( hexToColor( hexStr, true ) );
    }
    
    private static final String pad2( String s )
    {
        if ( s.length() >= 2 )
            return ( s );
        
        for ( int i = s.length(); i < 2; i++ )
        {
            s = "0" + s;
        }
        
        return ( s );
    }
    
    public static final String colorToHex( int red, int green, int blue, int alpha )
    {
        String value = "#";
        value += pad2( Integer.toHexString( red ).toUpperCase() );
        value += pad2( Integer.toHexString( green ).toUpperCase() );
        value += pad2( Integer.toHexString( blue ).toUpperCase() );
        
        if ( alpha != ALPHA_OPAQUE )
            value += pad2( Integer.toHexString( alpha ).toUpperCase() );
        
        return ( value );
    }
    
    public static final String colorToHex( int red, int green, int blue )
    {
        return ( colorToHex( red, green, blue, ALPHA_OPAQUE ) );
    }
    
    public static final String colorToHex( Color color )
    {
        if ( color == null )
            throw new NullPointerException( "color" );
        
        return ( colorToHex( color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() ) );
    }
    
    private ColorUtils()
    {
    }
}
