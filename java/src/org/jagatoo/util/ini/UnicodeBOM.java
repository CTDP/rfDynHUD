/**
 * Copyright (c) 2007-2009, JAGaToo Project Group all rights reserved.
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
package org.jagatoo.util.ini;

import java.io.IOException;
import java.io.InputStream;

/**
 * Unicode BOM model.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public enum UnicodeBOM
{
    UTF_8( 0xEFBBBF, 3 ),
    UTF_16_BE( 0xFEFF, 2 ),
    UTF_16_LE( 0xFFFE, 2 ),
    UTF_32_BE( 0x0000FEFF, 4 ),
    UTF_32_LE( 0xFEFF0000, 4 ),
    UTF_7a( 0x2B2F7638, 4 ),
    UTF_7b( 0x2B2F7639, 4 ),
    UTF_7c( 0x2B2F762B, 4 ),
    UTF_7d( 0x2B2F762F, 4 ),
    UTF_1( 0xF7644C, 3 ),
    UTF_EBCDIC( 0xDD736673, 4 ),
    SUSU( 0x0EFEFF, 3 ),
    BOCU_1( 0xFBEE28, 3 ), // optional FF, 4
    GB_18030( 0x84319533, 4 ),
    ;
    
    private final int bom;
    private final int length;
    
    /**
     * Gets the BOM code.
     * 
     * @return the BOM code.
     */
    public final int getBOM()
    {
        return ( bom );
    }
    
    /**
     * The theoretical byte length. Could be more for {@value #BOCU_1}.
     * 
     * @return theoretical length. Could be more for {@value #BOCU_1}.
     */
    public final int getLength()
    {
        return ( length );
    }
    
    /**
     * The actual byte length of the given BOM.
     * 
     * @param bom the 4 byte bom (first 4 bytes of the file)
     * 
     * @return the actual byte length of the given BOM.
     */
    public final int getLength( int bom )
    {
        if ( this == BOCU_1 )
        {
            if ( ( bom & 0xFF ) == 0xFF )
                return ( 4 );
            
            return ( 3 );
        }
        
        return ( getLength() );
    }
    
    private UnicodeBOM( int bom, int length )
    {
        this.bom = bom;
        this.length = length;
    }
    
    /**
     * Attempts to recognize the passed unicode BOM. If it can't be recognized, <code>null</code> is returned.
     * 
     * @param bom the 4 byte bom (first 4 bytes of the file)
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     */
    public static UnicodeBOM recognize( int bom )
    {
        if ( bom == UTF_32_BE.getBOM() )
            return ( UTF_32_BE );
        
        if ( bom == UTF_32_BE.getBOM() )
            return ( UTF_32_BE );
        
        if ( bom == UTF_32_LE.getBOM() )
            return ( UTF_32_LE );
        
        if ( bom == UTF_7a.getBOM() )
            return ( UTF_7a );
        
        if ( bom == UTF_7b.getBOM() )
            return ( UTF_7b );
        
        if ( bom == UTF_7c.getBOM() )
            return ( UTF_7c );
        
        if ( bom == UTF_7d.getBOM() )
            return ( UTF_7d );
        
        if ( bom == UTF_EBCDIC.getBOM() )
            return ( UTF_EBCDIC );
        
        if ( bom == GB_18030.getBOM() )
            return ( GB_18030 );
        
        int bom3 = ( bom & 0xFFFFFF00 ) >>> 8;
        
        if ( bom3 == BOCU_1.getBOM() )
        {
            //if ( ( bom & 0xFF ) == 0xFF )
            
            return ( BOCU_1 );
        }
        
        if ( bom3 == SUSU.getBOM() )
            return ( SUSU );
        
        if ( bom3 == UTF_1.getBOM() )
            return ( UTF_1 );
        
        if ( bom3 == UTF_8.getBOM() )
            return ( UTF_8 );
        
        int bom2 = ( bom & 0xFFFF00 ) >>> 16;
        
        if ( bom2 == UTF_16_BE.getBOM() )
            return ( UTF_16_BE );
        
        if ( bom2 == UTF_16_LE.getBOM() )
            return ( UTF_16_LE );
        
        return ( null );
    }
    
    /**
     * Skips the bytes, covered by a possible unicode BOM, or, if not recognized, does nothing.
     * The passed {@link InputStream} needs to support mark/reset.
     * 
     * @param in
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     * 
     * @throws IOException if anything went wrong.
     */
    public static UnicodeBOM skipBOM( InputStream in ) throws IOException
    {
        //if ( !in.markSupported() )
        //    return ( null );
        
        in.mark( 16 );
        
        int bom = 0;
        int b = in.read();
        bom |= ( ( b << 24 ) & 0xFF000000 );
        b = in.read();
        bom |= ( ( b << 16 ) & 0x00FF0000 );
        b = in.read();
        bom |= ( ( b << 8 ) & 0x0000FF00 );
        b = in.read();
        bom |= ( b & 0x000000FF );
        
        in.reset();
        
        UnicodeBOM uniBOM = UnicodeBOM.recognize( bom );
        
        if ( uniBOM != null )
        {
            in.skip( uniBOM.getLength( bom ) );
        }
        
        return ( uniBOM );
    }
}
