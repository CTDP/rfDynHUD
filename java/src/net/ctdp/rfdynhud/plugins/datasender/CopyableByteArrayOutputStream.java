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
package net.ctdp.rfdynhud.plugins.datasender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * {@link ByteArrayInputStream} extended by a copy method.
 * 
 * @author Marvin Froehlich (CTDP)
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
