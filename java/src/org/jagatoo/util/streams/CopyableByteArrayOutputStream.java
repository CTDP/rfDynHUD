/**
 * Copyright (c) 2007-2014, JAGaToo Project Group all rights reserved.
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
package org.jagatoo.util.streams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * {@link ByteArrayInputStream} extended by a copy method.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class CopyableByteArrayOutputStream extends ByteArrayOutputStream
{
    public synchronized void copyTo( byte[] destBuff, int off )
    {
        if ( destBuff == null )
            throw new IllegalArgumentException( "destBuff must not be null." );
        
        if ( off < 0 )
            throw new IllegalArgumentException( "off must not be less than 0." );
        
        if ( destBuff.length - off < size() )
            throw new IllegalArgumentException( "destBuff is shorter than reqired " + size() + "." );
        
        System.arraycopy( buf, 0, destBuff, off, size() );
    }
    
    public final void copyTo( byte[] destBuff )
    {
        copyTo( destBuff, 0 );
    }
    
    public CopyableByteArrayOutputStream( int size )
    {
        super( size );
    }
    
    public CopyableByteArrayOutputStream()
    {
        super();
    }
}
