/**
 * Copyright (c) 2007-2010, JAGaToo Project Group all rights reserved.
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Encrypts Strings using MD5.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class MD5Util
{
    private static String hex( byte[] array )
    {
        StringBuilder sb = new StringBuilder();
        
        for ( int i = 0; i < array.length; ++i )
        {
            String hexString = Integer.toHexString( ( array[i] & 0xFF ) | 0x100 );
            
            for ( int j = 1; j < 3; j++ )
                sb.append( Character.toUpperCase( hexString.charAt( j ) ) );
        }
        
        return ( sb.toString() );
    }
    
    /**
     * Encrypt the passed String using MD5.
     * 
     * @param message the message to encrypt
     * 
     * @return the MD5 hash.
     */
    public static String md5( String message )
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            
            return ( hex( md.digest( message.getBytes() ) ) );
        }
        catch ( NoSuchAlgorithmException e )
        {
        }
        
        return ( null );
    }
    
    /**
     * Encrypt the passed String using MD5.
     * 
     * @param message the message to encrypt
     * 
     * @return the MD5 hash as 16 bytes.
     */
    public static byte[] md5Bytes( String message )
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            
            return ( md.digest( message.getBytes() ) );
        }
        catch ( NoSuchAlgorithmException e )
        {
        }
        
        return ( null );
    }
}
