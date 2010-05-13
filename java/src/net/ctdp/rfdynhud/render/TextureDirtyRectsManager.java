package net.ctdp.rfdynhud.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.openmali.types.twodee.Rect2i;

/**
 * This class provides a public interface to retrieve the current dirty rectangles.
 * 
 * @author Marvin Froehlich
 */
public class TextureDirtyRectsManager
{
    private static boolean needsCompleteDirtyRect = true;
    private static long completeRedrawFrameIndex = -1L;
    
    public static void forceCompleteRedraw()
    {
        needsCompleteDirtyRect = true;
    }
    
    public static boolean isCompleteRedrawForced()
    {
        return ( needsCompleteDirtyRect );
    }
    
    public static ByteBuffer createByteBuffer( int maxNumDirtyRects )
    {
        return ( ByteBuffer.allocateDirect( 2 + 2 * maxNumDirtyRects ).order( ByteOrder.nativeOrder() ) );
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
     * @param frameIndex
     * 
     * @return the number of dirty rectangles. If the buffer iss too small the number is returned as a negative number.
     */
    public static short getDirtyRects( long frameIndex, TextureImage2D texture, ByteBuffer buffer, boolean resetBufferToStart )
    {
        ArrayList<Rect2i> dirtyList = texture.getUpdateList();
        int numDirtyRects = dirtyList.size();
        
        if ( buffer != null )
        {
            if ( resetBufferToStart )
                buffer.position( 0 );
            
            buffer.limit( buffer.capacity() );
            
            if ( buffer.limit() - buffer.position() < 2 + 2 * numDirtyRects )
            {
                writeShort( (short)0, buffer );
                buffer.flip();
                
                return ( (short)-numDirtyRects );
            }
            
            boolean completeDirtyRect = needsCompleteDirtyRect;
            needsCompleteDirtyRect = false;
            if ( completeDirtyRect )
                completeRedrawFrameIndex = frameIndex;
            else if ( completeRedrawFrameIndex == frameIndex )
                completeDirtyRect = true;
            else
                completeRedrawFrameIndex = -1L;
            
            if ( completeDirtyRect )
            {
                writeShort( (short)1, buffer );
                
                writeShort( (short)0, buffer );
                writeShort( (short)0, buffer );
                writeShort( (short)texture.getUsedWidth(), buffer );
                writeShort( (short)texture.getUsedHeight(), buffer );
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
            }
            
            buffer.flip();
        }
        
        texture.clearUpdateList();
        
        return ( (short)numDirtyRects );
    }
    
    /**
     * This is just for debugging!
     * 
     * @param texture
     */
    public static void drawDirtyRects( TextureImage2D texture )
    {
        ArrayList<Rect2i> dirtyList = texture.getUpdateList();
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
