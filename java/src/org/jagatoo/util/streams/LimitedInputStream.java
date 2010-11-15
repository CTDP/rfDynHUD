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
package org.jagatoo.util.streams;

import java.io.IOException;
import java.io.InputStream;

/**
 * Streams a limited number of bytes.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class LimitedInputStream extends InputStream
{
    private final InputStream in;
    private long numRemainingBytes;
    
    @Override
    public int available() throws IOException
    {
        return ( in.available() );
    }
    
    @Override
    public int read() throws IOException
    {
        int result = -1;
        
        if ( numRemainingBytes > 0L )
        {
            result = in.read();
            
            if ( result >= 0 )
                numRemainingBytes--;
        }
        
        return ( result );
    }
    
    @Override
    public int read( byte[] b, int off, int len ) throws IOException
    {
        if ( numRemainingBytes == 0L )
            return ( -1 );
        
        if ( len > numRemainingBytes )
            len = (int)numRemainingBytes;
        
        int result = in.read( b, off, len );
        
        if ( result > 0 )
            numRemainingBytes -= result;
        
        return ( result );
    }
    
    @Override
    public long skip( long n ) throws IOException
    {
        long toSkip = Math.min( n, numRemainingBytes );
        
        long result = super.skip( toSkip );
        
        if ( result > 0L )
            numRemainingBytes -= result;
        
        return ( result );
    }
    
    @Override
    public void close() throws IOException
    {
        in.close();
    }
    
    public LimitedInputStream( InputStream in, long limit )
    {
        this.in = in;
        this.numRemainingBytes = limit;
    }
}
