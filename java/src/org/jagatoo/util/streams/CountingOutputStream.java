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
package org.jagatoo.util.streams;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Counts all written bytes and if the unterlying {@link OutputStream} is not <code>null</code>, writes them to this stream.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class CountingOutputStream extends OutputStream
{
    private final OutputStream out;
    
    private long count = 0L;
    
    /**
     * Gets the number of written bytes.
     * 
     * @return the number of written bytes.
     */
    public final long getCount()
    {
        return ( count );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write( int b ) throws IOException
    {
        if ( out != null )
            out.write( b );
        
        count++;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write( byte[] b, int off, int len ) throws IOException
    {
        if ( b == null )
            throw new NullPointerException( "b must not be null" );
        
        if ( off < 0 )
            throw new ArrayIndexOutOfBoundsException( "off must be >= 0." );
        
        if ( len < 0 )
            throw new ArrayIndexOutOfBoundsException( "len must be >= 0." );
        
        if ( b.length < off + len )
            throw new ArrayIndexOutOfBoundsException( "off+len must be <= b.length." );
        
        if ( out != null )
            out.write( b, off, len );
        
        count += len;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write( byte[] b ) throws IOException
    {
        if ( b == null )
            throw new NullPointerException( "b must not be null" );
        
        if ( out != null )
            out.write( b );
        
        count += b.length;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException
    {
        if ( out != null )
            out.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        if ( out != null )
            out.close();
    }
    
    public CountingOutputStream( OutputStream out )
    {
        this.out = out;
    }
    
    public CountingOutputStream()
    {
        this( null );
    }
}
