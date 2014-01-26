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
package net.ctdp.rfdynhud.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import net.ctdp.rfdynhud.util.RFDHLog;

import org.openmali.types.twodee.Rect2i;

/**
 * This class provides a public interface to retrieve the current dirty rectangles.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TextureDirtyRectsManager
{
    public static ByteBuffer createByteBuffer( int maxNumDirtyRects )
    {
        return ( ByteBuffer.allocateDirect( 2 + 2 * 4 * maxNumDirtyRects ).order( ByteOrder.nativeOrder() ) );
    }
    
    private static final void writeShort( short s, ByteBuffer bb )
    {
        bb.put( (byte)( s & 0x00FF ) );
        bb.put( (byte)( ( s & 0xFF00 ) >>> 8 ) );
    }
    
    /**
     * Gets all the current dirty-rects from the given texture.<br>
     * The number of dirty rectangles is written as a short to the first two bytes of the buffer.<br>
     * Then each rectangle is written as four shorts in the order left,top,width,height.<br>
     * After this method finished all dirty rectangles information will be removed from the texture. 
     * 
     * @param texture the texture to handle dirty rectangles of
     * @param buffer the buffer to write the information to
     * @param resetBufferToStart if true, the buffer is set to position zero before anything is written to it
     * 
     * @return the number of dirty rectangles. If the buffer iss too small the number is returned as a negative number.
     */
    public static short getDirtyRects( TextureImage2D texture, ByteBuffer buffer, boolean resetBufferToStart )
    {
        List<Rect2i> dirtyList = texture.getUpdateList();
        int numDirtyRects = dirtyList.size();
        
        if ( buffer != null )
        {
            if ( resetBufferToStart )
                buffer.position( 0 );
            
            buffer.limit( buffer.capacity() );
            
            if ( buffer.limit() - buffer.position() < 2 + 2 * 4 * numDirtyRects )
            {
                if ( buffer.limit() - buffer.position() < 2 + 2 * 4 * 1 )
                {
                    RFDHLog.exception( "WARNING: Cannot write dirty rects to the buffer." );
                }
                else
                {
                    //writeShort( (short)0, buffer );
                    //buffer.flip();
                    
                    writeShort( (short)1, buffer );
                    
                    writeShort( (short)0, buffer );
                    writeShort( (short)0, buffer );
                    writeShort( (short)texture.getWidth(), buffer );
                    writeShort( (short)texture.getHeight(), buffer );
                    
                    buffer.flip();
                    
                    RFDHLog.exception( "WARNING: Cannot write all dirty rects to the buffer. Adding one full size rect. Performance may drop." );
                }
            }
            else
            {
                writeShort( (short)numDirtyRects, buffer );
                
                for ( int i = 0; i < numDirtyRects; i++ )
                {
                    Rect2i r = dirtyList.get( i );
                    
                    writeShort( (short)r.getLeft(), buffer );
                    writeShort( (short)r.getTop(), buffer );
                    writeShort( (short)r.getWidth(), buffer );
                    writeShort( (short)r.getHeight(), buffer );
                }
                
                buffer.flip();
            }
        }
        
        texture.clearUpdateList();
        
        if ( buffer == null )
            return ( (short)-numDirtyRects );
        
        return ( (short)numDirtyRects );
    }
    
    /**
     * This is just for debugging!
     * 
     * @param texture the take dirty rects from and to draw on
     */
    public static void drawDirtyRects( TextureImage2D texture )
    {
        List<Rect2i> dirtyList = texture.getUpdateList();
        int numDirtyRects = dirtyList.size();
        
        //System.out.println( numDirtyRects );
        
        for ( int i = 0; i < numDirtyRects; i++ )
        {
            Rect2i r = dirtyList.get( i );
            System.out.println( r );
            
            texture.fillRectangle( new java.awt.Color( 0, 0, 255, 127 ), r.getLeft(), r.getTop(), r.getWidth(), r.getHeight(), false, null );
        }
    }
}
