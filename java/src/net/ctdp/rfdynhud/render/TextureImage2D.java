/**
 * Copyright (c) 2003-2009, Xith3D Project Group all rights reserved.
 * 
 * Portions based on the Java3D interface, Copyright by Sun Microsystems.
 * Many thanks to the developers of Java3D and Sun Microsystems for their
 * innovation and design.
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
package net.ctdp.rfdynhud.render;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.util.NumberUtil;

import org.jagatoo.image.DirectBufferedImage;
import org.openmali.types.twodee.Rect2i;
import org.openmali.vecmath2.util.ColorUtils;

/**
 * This class provides a direct interface to draw on a Texture.
 * 
 * Most of the code is borrowed from TextureImage2D from the Xith3D project. (http://xith.org/)
 * 
 * @author David Yazel
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class TextureImage2D
{
    private static final int BYTE_OFFSET3_RED = ByteOrderManager.RED;
    private static final int BYTE_OFFSET3_GREEN = ByteOrderManager.GREEN;
    private static final int BYTE_OFFSET3_BLUE = ByteOrderManager.BLUE;
    
    private static final int BYTE_OFFSET4_RED = ByteOrderManager.RED;
    private static final int BYTE_OFFSET4_GREEN = ByteOrderManager.GREEN;
    private static final int BYTE_OFFSET4_BLUE = ByteOrderManager.BLUE;
    private static final int BYTE_OFFSET4_ALPHA = ByteOrderManager.ALPHA;
    
    @SuppressWarnings( "unused" )
    private final boolean isOffline;
    
    private ByteBuffer dataBuffer = null;
    private byte[] data = null;
    
    private byte[] pixelRow1 = null;
    private byte[] pixelRow2 = null;
    
    private final int pixelBytes;
    private final int pixelSize;
    
    private final ArrayList<Rect2i> updateList;
    
    private final Rect2i userClipRect = new Rect2i( 0, 0, 128, 128 );
    private final Rect2i clipRect = new Rect2i( 0, 0, 128, 128 );
    
    private boolean yUp = false;
    
    private int width;
    private int height;
    
    private int usedWidth;
    private int usedHeight;
    
    private BufferedImage bufferedImage = null;
    
    private Texture2DCanvas textureCanvas = null;
    private boolean hasTextureCanvas = false;
    
    /**
     * Gets the physical width of the texture.
     * 
     * @return the physical width of the texture.
     */
    public final int getMaxWidth()
    {
        return ( width );
    }
    
    /**
     * Gets the physical height of the texture.
     * 
     * @return the physical height of the texture.
     */
    public final int getMaxHeight()
    {
        return ( height );
    }
    
    /**
     * Gets the width-part of the texture, that is actually used by the application.
     * Since textures always need to be power-of-two-sized, the physical size can be larger than the used part.
     * If we need a texture of width 800, the physical width of this texture will be 1024.
     * 
     * @return the used part of the texture width.
     */
    public final int getWidth()
    {
        return ( usedWidth );
    }
    
    /**
     * Gets the height-part of the texture, that is actually used by the application.
     * Since textures always need to be power-of-two-sized, the physical size can be larger than the used part.
     * If we need a texture of height 600, the physical height of this texture will be 1024.
     * 
     * @return the used part of the texture height.
     */
    public final int getHeight()
    {
        return ( usedHeight );
    }
    
    /**
     * Resizes this texture to a size in range [1,getMaxWidth()],[1,getMaxHeight()].
     * 
     * @param width the width to resize to
     * @param height the height to resize to
     */
    public void resize( int width, int height )
    {
        if ( ( width < 1 ) || ( width > getMaxWidth() ) )
            throw new IllegalArgumentException( "width out of range (" + width + " / " + getMaxWidth() + ")" );
        
        if ( ( height < 1 ) || ( height > getMaxHeight() ) )
            throw new IllegalArgumentException( "height out of range (" + height + " / " + getMaxHeight() + ")" );
        
        this.usedWidth = width;
        this.usedHeight = height;
    }
    
    /**
     * Gets the number of bytes per pixel (3 or 4).
     * 
     * @return the number of bytes per pixel.
     */
    public final int getPixelBytes()
    {
        return ( pixelBytes );
    }
    
    /**
     * Gets the number of bits per pixel (24 or 32).
     * 
     * @return the number of bits per pixel.
     */
    public final int getPixelSize()
    {
        return ( pixelSize );
    }
    
    /**
     * Gets whether this texture has an alpha channel.
     * 
     * @return whether this texture has an alpha channel.
     */
    public final boolean hasAlphaChannel()
    {
        return ( pixelBytes == 4 );
    }
    
    /**
     * Gets the backing byte array.
     * 
     * @return null, if this texture is backed by a ByteBuffer.
     */
    public final byte[] getData()
    {
        return ( data );
    }
    
    /**
     * Gets whether this texture has an attached Texture2DCanvas instance.
     * 
     * @return whether this texture has an attached Texture2DCanvas instance.
     */
    public final boolean hasTextureCanvas()
    {
        return ( hasTextureCanvas );
    }
    
    /**
     * Gets the attached Texture2DCanvas. If it doesn't currently have one, it will be created.
     * 
     * @return the attached Texture2DCanvas. (never null)
     */
    public Texture2DCanvas getTextureCanvas()
    {
        if ( textureCanvas == null )
        {
            textureCanvas = new Texture2DCanvas( this );
            hasTextureCanvas = true;
        }
        
        return ( textureCanvas );
    }
    
    private void addDirtyRect( Rect2i rect, int testListStart, int testListSize )
    {
        if ( updateList == null )
            return;
        
        for ( int i = testListStart; i < Math.min( testListSize, updateList.size() ); i++ )
        {
            Rect2i r = updateList.get( i );
            
            if ( rect.isCoveredBy( r ) )
            {
                Rect2i.toPool( rect );
                
                return;
            }
            
            if ( rect.covers( r ) )
            {
                updateList.remove( i );
                Rect2i.toPool( r );
                
                i--;
                testListSize--;
                
                continue;
            }
            
            int rectRight = rect.getLeft() + rect.getWidth() - 1;
            int rectBottom = rect.getTop() + rect.getHeight() - 1;
            int rRight = r.getLeft() + r.getWidth() - 1;
            int rBottom = r.getTop() + r.getHeight() - 1;
            
            if ( ( rectRight >= r.getLeft() ) && ( rectBottom >= r.getTop() ) && ( rect.getLeft() <= rRight ) && ( rect.getTop() <= rBottom ) )
            {
                if ( rect.getLeft() >= r.getLeft() )
                {
                    if ( rectRight <= rRight )
                    {
                        if ( rect.getTop() < r.getTop() )
                        {
                            if ( rectBottom <= rBottom )
                            {
                                /*
                                
                                  /------\
                                  | rect |
                                /-|------|-\
                                | \------/ |
                                |    r     |
                                \----------/
                                
                                */
                                
                                rect.setHeight( r.getTop() - rect.getTop() );
                            }
                            else
                            {
                                /*
                                
                                  /------\
                                  | rect |
                                /-|------|-\
                                | |      | |
                                | |  r   | |
                                \-|------|-/
                                  |      |
                                  \------/
                                
                                */
                                
                                Rect2i bottomPart = Rect2i.fromPool();
                                bottomPart.set( rect.getLeft(), rBottom + 1, rect.getWidth(), rectBottom - rBottom );
                                
                                addDirtyRect( bottomPart, i + 1, testListSize );
                                
                                rect.setHeight( r.getTop() - rect.getTop() );
                            }
                        }
                        else
                        {
                            /*
                            
                            /----------\
                            |    r     |
                            | /------\ |
                            \-|------|-/
                              | rect |
                              \------/
                            
                            */
                            
                            rect.set( rect.getLeft(), rBottom + 1, rect.getWidth(), rectBottom - rBottom );
                        }
                    }
                    else
                    {
                        if ( rectBottom <= rBottom )
                        {
                            if ( rect.getTop() < r.getTop() )
                            {
                                /*
                                
                                  /----------\
                                  | rect     |
                                /-|--------\ |
                                | \--------|-/
                                |    r     |
                                \----------/
                                
                                */
                                
                                Rect2i bottomRightPart = Rect2i.fromPool();
                                bottomRightPart.set( rRight + 1, r.getTop(), rectRight - rRight, rectBottom - r.getTop() + 1 );
                                
                                addDirtyRect( bottomRightPart, i + 1, testListSize );
                                
                                rect.setHeight( r.getTop() - rect.getTop() );
                            }
                            else
                            {
                                /*
                                
                                /----------\
                                |   /------|------\
                                | r |      | rect |
                                |   \------|------/
                                \----------/
                                
                                */
                                
                                rect.set( rRight + 1, rect.getTop(), rectRight - rRight, rect.getHeight() );
                            }
                        }
                        else
                        {
                            if ( rect.getTop() < r.getTop() )
                            {
                                /*
                                
                                    /------\
                                    | rect |
                                /---|---\  |
                                |   |   |  |
                                | r |   |  |
                                \---|---/  |
                                    |      |
                                    \------/
                                
                                */
                                
                                Rect2i topPart = Rect2i.fromPool();
                                topPart.set( rect.getLeft(), rect.getTop(), rect.getWidth(), r.getTop() - rect.getTop() );
                                
                                addDirtyRect( topPart, i + 1, testListSize );
                                
                                Rect2i bottomPart = Rect2i.fromPool();
                                bottomPart.set( rect.getLeft(), rBottom + 1, rect.getWidth(), rectBottom - rBottom );
                                
                                addDirtyRect( bottomPart, i + 1, testListSize );
                                
                                rect.set( rRight + 1, r.getTop(), rectRight - rRight, r.getHeight() );
                            }
                            else
                            {
                                /*
                                
                                /--------\
                                |   r    |
                                | /------|-\
                                \-|------/ |
                                  | rect   |
                                  \--------/
                                
                                */
                                
                                Rect2i topRightPart = Rect2i.fromPool();
                                topRightPart.set( rRight + 1, rect.getTop(), rectRight - rRight, rBottom - rect.getTop() + 1 );
                                
                                addDirtyRect( topRightPart, i + 1, testListSize );
                                
                                rect.set( rect.getLeft(), rBottom + 1, rect.getWidth(), rectBottom - rBottom );
                            }
                        }
                    }
                }
                else
                {
                    if ( rect.getTop() < r.getTop() )
                    {
                        if ( rectBottom <= rBottom )
                        {
                            if ( rectRight > rRight )
                            {
                                /*
                                
                                /---------\
                                |  rect   |
                                | /-----\ |
                                \-|-----|-/
                                  |  r  |
                                  \-----/
                                
                                */
                                
                                Rect2i bottomLeftPart = Rect2i.fromPool();
                                bottomLeftPart.set( rect.getLeft(), r.getTop(), r.getLeft() - rect.getLeft(), rectBottom - r.getTop() + 1 );
                                
                                addDirtyRect( bottomLeftPart, i + 1, testListSize );
                                
                                Rect2i bottomRightPart = Rect2i.fromPool();
                                bottomRightPart.set( rRight + 1, r.getTop(), rectRight - rRight, rectBottom - r.getTop() + 1 );
                                
                                addDirtyRect( bottomRightPart, i + 1, testListSize );
                                
                                rect.setHeight( r.getTop() - rect.getTop() );
                            }
                            else
                            {
                                /*
                                
                                /------\
                                | rect |
                                | /----|-----\
                                \-|----/     |
                                  |       r  |
                                  \----------/
                                
                                */
                                
                                Rect2i bottomLeftPart = Rect2i.fromPool();
                                bottomLeftPart.set( rect.getLeft(), r.getTop(), r.getLeft() - rect.getLeft(), rectBottom - r.getTop() + 1 );
                                
                                addDirtyRect( bottomLeftPart, i + 1, testListSize );
                                
                                rect.setHeight( r.getTop() - rect.getTop() );
                            }
                        }
                        else
                        {
                            /*
                            
                            /------\
                            | rect |
                            | /----|-----\
                            | |    |     |
                            | |    |  r  |
                            | \----|-----/
                            |      |
                            \------/
                            
                            */
                            
                            Rect2i topPart = Rect2i.fromPool();
                            topPart.set( rect.getLeft(), rect.getTop(), rect.getWidth(), r.getTop() - rect.getTop() );
                            
                            addDirtyRect( topPart, i + 1, testListSize );
                            
                            Rect2i bottomPart = Rect2i.fromPool();
                            bottomPart.set( rect.getLeft(), rBottom + 1, rect.getWidth(), rectBottom - rBottom );
                            
                            addDirtyRect( bottomPart, i + 1, testListSize );
                            
                            rect.set( rect.getLeft(), r.getTop(), r.getLeft() - rect.getLeft(), r.getHeight() );
                        }
                    }
                    else
                    {
                        if ( rectBottom <= rBottom )
                        {
                            if ( rectRight > rRight )
                            {
                                /*
                                
                                       /----------\
                                /------|----------|---\
                                | rect |          |   |
                                \------|----------|---/
                                       |    r     |
                                       \----------/
                                
                                */
                                
                                Rect2i rightPart = Rect2i.fromPool();
                                rightPart.set( rRight + 1, rect.getTop(), rectRight - rRight, rect.getHeight() );
                                
                                addDirtyRect( rightPart, i + 1, testListSize );
                                
                                rect.setWidth( r.getLeft() - rect.getLeft() );
                            }
                            else
                            {
                                /*
                                
                                       /----------\
                                /------|------\   |
                                | rect |      | r |
                                \------|------/   |
                                       \----------/
                                
                                */
                                
                                rect.setWidth( r.getLeft() - rect.getLeft() );
                            }
                        }
                        else
                        {
                            if ( rectRight > rRight )
                            {
                                /*
                                
                                  /----------\
                                  |    r     |
                                /-|----------|-\
                                | \----------/ |
                                |     rect     |
                                \--------------/
                                
                                */
                                
                                Rect2i topLeftPart = Rect2i.fromPool();
                                topLeftPart.set( rect.getLeft(), rect.getTop(), r.getLeft() - rect.getLeft(), rBottom - rect.getTop() + 1 );
                                
                                addDirtyRect( topLeftPart, i + 1, testListSize );
                                
                                Rect2i topRightPart = Rect2i.fromPool();
                                topRightPart.set( rRight + 1, rect.getTop(), rectRight - rRight, rBottom - rect.getTop() + 1 );
                                
                                addDirtyRect( topRightPart, i + 1, testListSize );
                                
                                rect.set( rect.getLeft(), rBottom + 1, rect.getWidth(), rectBottom - rBottom );
                            }
                            else
                            {
                                /*
                                
                                  /----------\
                                  |    r     |
                                /-|------\   |
                                | \------|---/
                                |   rect |
                                \--------/
                                
                                */
                                
                                Rect2i topLeftPart = Rect2i.fromPool();
                                topLeftPart.set( rect.getLeft(), rect.getTop(), r.getLeft() - rect.getLeft(), rBottom - rect.getTop() + 1 );
                                
                                addDirtyRect( topLeftPart, i + 1, testListSize );
                                
                                rect.set( rect.getLeft(), rBottom + 1, rect.getWidth(), rectBottom - rBottom );
                            }
                        }
                    }
                }
            }
        }
        
        if ( ( rect.getWidth() > 0 ) && ( rect.getHeight() > 0 ) )
            updateList.add( rect );
    }
    
    void addDirtyRect( Rect2i rect )
    {
        if ( updateList == null )
            return;
        
        addDirtyRect( rect, 0, updateList.size() );
    }
    
    protected void markDirty( int x, int y, int width, int height, boolean clampClip, boolean validate )
    {
        if ( clampClip )
        {
            Rect2i clip = getEffectiveClipRect();
            
            int clipX = clip.getLeft();
            int clipY = clip.getTop();
            int clipW = clip.getWidth();
            int clipH = clip.getHeight();
            
            x = Math.max( clipX, x );
            y = Math.max( clipY, y );
            x = Math.min( x, clipX + clipW - 1 );
            y = Math.min( y, clipY + clipH - 1 );
            width = Math.min( width, clipX + clipW - x );
            height = Math.min( height, clipY + clipH - y );
        }
        
        if ( validate )
        {
            if ( ( x < 0 ) || ( y < 0 ) || ( x >= this.getMaxWidth() ) || ( y >= this.getMaxHeight() ) || ( width > this.getMaxWidth() ) || ( height > this.getMaxHeight() ) || ( ( x + width ) > this.getMaxWidth() ) || ( ( y + height ) > this.getMaxHeight() ) )
            {
                throw new IllegalArgumentException( "Rectangle outside of image (" + x + ", " + y + ", " + width + ", " + height + ")" );
            }
        }
        
        if ( ( width > 0 ) && ( height > 0 ) )
        {
            Rect2i rect = Rect2i.fromPool();
            if ( yUp )
                rect.set( x, getHeight() - height - y, width, height );
            else
                //rect.set( x, getMaxHeight() - height - y, width, height );
                rect.set( x, y, width, height );
            
            addDirtyRect( rect );
        }
    }
    
    /**
     * Marks a portion of the image component as dirty.
     * The region will be pushed to the graphics card on the next frame.
     * 
     * @param x the left coordinate
     * @param y the top coordinate
     * @param width the width
     * @param height the height
     */
    public final void markDirty( int x, int y, int width, int height )
    {
        markDirty( x, y, width, height, true, true );
    }
    
    /**
     * Marks a portion of the image component as dirty.
     * The region will be pushed to the graphics card on the next frame.
     * 
     * @param r the rectangle
     */
    public final void markDirty( Rect2i r )
    {
        markDirty( r.getLeft(), r.getTop(), r.getWidth(), r.getHeight() );
    }
    
    final ArrayList<Rect2i> getUpdateList()
    {
        return ( updateList );
    }
    
    final void clearUpdateList()
    {
        if ( updateList == null )
            return;
        
        for ( int i = updateList.size() - 1; i >= 0; i-- )
        {
            Rect2i.toPool( updateList.get( i ) );
        }
        
        updateList.clear();
    }
    
    /**
     * Gets a BufferedImagebacked by this texture's data.
     * 
     * @return a BufferedImagebacked by this texture's data.
     */
    public BufferedImage getBufferedImage()
    {
        if ( bufferedImage == null )
        {
            int[] pixelOffsets;
            if ( getPixelBytes() == 4 )
                pixelOffsets = new int[] { BYTE_OFFSET4_RED, BYTE_OFFSET4_GREEN, BYTE_OFFSET4_BLUE, BYTE_OFFSET4_ALPHA };
            else
                pixelOffsets = new int[] { BYTE_OFFSET3_RED, BYTE_OFFSET3_GREEN, BYTE_OFFSET3_BLUE };
            
            if ( dataBuffer == null )
            {
                DataBufferByte dbb = new DataBufferByte( data, getMaxWidth() * getMaxHeight() * getPixelBytes() );
                bufferedImage = new BufferedImage( new ComponentColorModel( ColorSpace.getInstance( ColorSpace.CS_sRGB ), new int[] { 8, 8, 8, 8 }, ( getPixelBytes() == 4 ), false, Transparency.TRANSLUCENT, 0 ), Raster.createInterleavedRaster( dbb, getMaxWidth(), getMaxHeight(), getMaxWidth() * getPixelBytes(), getPixelBytes(), pixelOffsets, new java.awt.Point( 0, 0 ) ), false, null );
            }
            else
            {
                bufferedImage = DirectBufferedImage.makeDirectImageRGBA( getMaxWidth(), getMaxHeight(), pixelOffsets, dataBuffer );
            }
        }
        
        return ( bufferedImage );
    }
    
    protected Graphics2D createGraphics2D()
    {
        BufferedImage bi = getBufferedImage();
        
        return ( bi.createGraphics() );
    }
    
    private void clampClipRect()
    {
        clipRect.set( userClipRect );
        clipRect.clamp( 0, 0, getMaxWidth(), getMaxHeight() );
    }
    
    /**
     * Sets the clip-rect, that will be clipping the drawn pixels.
     * 
     * @param x the left coordinate
     * @param y the top coordinate
     * @param width the width
     * @param height the height
     */
    public void setClipRect( int x, int y, int width, int height )
    {
        userClipRect.set( x, y, width, height );
        
        clampClipRect();
    }
    
    /**
     * Sets the clip-rect, that will be clipping the drawn pixels.
     * 
     * @param clipRect the clip rectangle
     */
    public final void setClipRect( Rect2i clipRect )
    {
        setClipRect( clipRect.getLeft(), clipRect.getTop(), clipRect.getWidth(), clipRect.getHeight() );
    }
    
    /**
     * Gets the currently used clip-rect.
     * 
     * @param rect where the rectangle data will be written to.
     * 
     * @param <Rect2i_> the return and parameter type restriction
     * 
     * @return the passed-in rectangle back again.
     */
    public final <Rect2i_ extends Rect2i> Rect2i_ getClipRect( Rect2i_ rect )
    {
        rect.set( userClipRect );
        
        return ( rect );
    }
    
    /**
     * Clamps the given rect to the current effective clip rect.
     * 
     * @param rect the rectangle
     * 
     * @param <Rect2i_> the return and parameter type restriction
     * 
     * @return the passed-in rectangle back again.
     */
    public final <Rect2i_ extends Rect2i> Rect2i_ clampToClipRect( Rect2i_ rect )
    {
        rect.clamp( clipRect );
        
        return ( rect );
    }
    
    final Rect2i getEffectiveClipRect()
    {
        return ( clipRect );
    }
    
    /*
    protected final void setImageData( byte[] data, int dataLength, boolean useBuffer )
    {
        clampClipRect( getMaxWidth(), getMaxHeight() );
        
        if ( useBuffer )
        {
            if ( ( this.dataBuffer == null ) || ( this.dataBuffer.capacity() < dataLength ) )
            {
                this.dataBuffer = ByteBuffer.allocateDirect( dataLength ).order( ByteOrder.nativeOrder() );
            }
            
            this.data = null;
        }
        else
        {
            if ( ( this.data == null ) || ( this.data.length < dataLength ) )
            {
                this.data = new byte[ dataLength ];
            }
            
            this.dataBuffer = null;
        }
        
        this.bufferedImage = null;
        
        if ( data == null )
        {
            return;
        }
        
        if ( useBuffer )
        {
            dataBuffer.position( 0 );
            dataBuffer.put( data, 0, dataLength );
            dataBuffer.flip();
        }
        else
        {
            switch ( getPixelBytes() )
            {
                case 4:
                    for ( int i = 0; i < dataLength; i += 4 )
                    {
                        this.data[ i + BYTE_OFFSET_RED ] = data[ i + 0 ];
                        this.data[ i + BYTE_OFFSET_GREEN ] = data[ i + 1 ];
                        this.data[ i + BYTE_OFFSET_BLUE ] = data[ i + 2 ];
                        this.data[ i + BYTE_OFFSET_ALPHA ] = data[ i + 3 ];
                    }
                    break;
                case 3:
                    for ( int i = 0; i < dataLength; i += 3 )
                    {
                        this.data[ i + BYTE_OFFSET_RED ] = data[ i + 0 ];
                        this.data[ i + BYTE_OFFSET_GREEN ] = data[ i + 1 ];
                        this.data[ i + BYTE_OFFSET_BLUE ] = data[ i + 2 ];
                    }
                    break;
            }
        }
    }
    
    public final void setImageData( byte[] data, int dataLength )
    {
        setImageData( data, dataLength, true );
    }
    
    protected final void setImageData( byte[] data, boolean useBuffer )
    {
        setImageData( data, ( data != null ) ? data.length : 0, useBuffer );
    }
    
    public final void setImageData( byte[] data )
    {
        setImageData( data, true );
    }
    
    protected void setImageData( BufferedImage image, boolean useBuffer )
    {
        int orgWidth = image.getMaxWidth();
        int orgHeight = image.getMaxWidth();
        
        int width = roundUpPower2( orgWidth );
        int height = roundUpPower2( orgHeight );
        
        this.setSize( width, height );
        this.setOriginalSize( orgWidth, orgHeight );
        texCoordUR.set( (float)orgWidth / (float)width, (float)orgHeight / (float)height );
        
        if ( useBuffer )
            this.data = null;
        else
            this.dataBuffer = null;
        
        this.pixelSize = getFormat().getPixelSize();
        
        if ( useBuffer )
        {
            this.dataBuffer = BufferUtils.createByteBuffer( width * height * pixelSize );
            
            Raster raster = image.getRaster();
            ColorModel cm = image.getColorModel();
            Object o = null;
            
            int i = 0;
            for ( int x = 0; x < width; x++ )
            {
                for ( int y = 0; y < height; y++ )
                {
                    o = raster.getDataElements( x, y, o );
                    
                    switch ( pixelSize )
                    {
                        case 4:
                        {
                            final byte r = (byte)cm.getRed( o );
                            final byte g = (byte)cm.getGreen( o );
                            final byte b = (byte)cm.getBlue( o );
                            final byte a = (byte)cm.getAlpha( o );
                            
                            dataBuffer.put( i++, r );
                            dataBuffer.put( i++, g );
                            dataBuffer.put( i++, b );
                            dataBuffer.put( i++, a );
                            break;
                        }
                        case 3:
                        {
                            final byte r = (byte)cm.getRed( o );
                            final byte g = (byte)cm.getGreen( o );
                            final byte b = (byte)cm.getBlue( o );
                            
                            dataBuffer.put( i++, r );
                            dataBuffer.put( i++, g );
                            dataBuffer.put( i++, b );
                            break;
                        }
                        case 2:
                        {
                            final byte r = (byte)cm.getRed( o );
                            final byte g = (byte)cm.getGreen( o );
                            
                            dataBuffer.put( i++, r );
                            dataBuffer.put( i++, g );
                            break;
                        }
                        case 1:
                        {
                            final byte r = (byte)cm.getRed( o );
                            
                            dataBuffer.put( i++, r );
                            break;
                        }
                    }
                }
            }
            
            //dataBuffer.flip();
            dataBuffer.position( 0 );
            dataBuffer.limit( dataBuffer.capacity() );
        }
        else
        {
            this.data = new byte[ width * height * pixelSize ];
            
            int i = getDataOffset( 0, 0 );
            for ( int x = 0; x < width; x++ )
            {
                for ( int y = 0; y < height; y++ )
                {
                    int argb = image.getRGB( x, y );
                    
                    if ( pixelSize == 4 )
                    {
                        data[ i++ ] = (byte)( ( argb & 0xFF000000 ) >> 24 );
                        data[ i++ ] = (byte)( ( argb & 0x00FF0000 ) >> 16 );
                        data[ i++ ] = (byte)( ( argb & 0x0000FF00 ) >> 8 );
                        data[ i++ ] = (byte)( argb & 0x000000FF );
                    }
                    else if ( pixelSize == 3 )
                    {
                        data[ i++ ] = (byte)( ( argb & 0x00FF0000 ) >> 16 );
                        data[ i++ ] = (byte)( ( argb & 0x0000FF00 ) >> 8 );
                        data[ i++ ] = (byte)( argb & 0x000000FF );
                    }
                }
            }
        }
        
        setHasData( true );
    }
    
    public void setImageData( BufferedImage image )
    {
        setImageData( image, true );
    }
    */
    
    private final int getDataOffset( int x, int y )
    {
        if ( yUp )
            return ( ( ( getMaxHeight() - y - 1 ) * getMaxWidth() * getPixelBytes() ) + ( x * getPixelBytes() ) );
        
        return ( ( y * getMaxWidth() * getPixelBytes() ) + ( x * getPixelBytes() ) );
    }
    
    private static final int getDataOffset( int x, int y, int imgWidth, int pixelBytes )
    {
        return ( ( y * imgWidth * pixelBytes ) + ( x * pixelBytes ) );
    }
    
    protected final void setPixel( int offset, byte[] data )
    {
        if ( this.data == null )
        {
            this.dataBuffer.position( offset );
            this.dataBuffer.put( data, 0, getPixelBytes() );
            this.dataBuffer.position( 0 );
        }
        else
        {
            System.arraycopy( data, 0, this.data, offset, getPixelBytes() );
        }
    }
    
    /**
     * Sets one pixel.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param data the source pixel data
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void setPixel( int x, int y, byte[] data, boolean markDirty, Rect2i dirtyRect )
    {
        if ( ( x < 0 ) || ( x >= getMaxWidth() ) || ( y < 0 ) || ( y >= getMaxHeight() ) )
            return;
        
        setPixel( getDataOffset( x, y ), data );
        
        if ( markDirty )
        {
            markDirty( x, y, 1, 1 );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x, y, 1, 1 );
        }
    }
    
    private final byte[] getPixel( int offset, byte[] data )
    {
        if ( this.data == null )
        {
            this.dataBuffer.position( offset );
            this.dataBuffer.get( data, 0, getPixelBytes() );
            this.dataBuffer.position( 0 );
        }
        else
        {
            System.arraycopy( this.data, offset, data, 0, getPixelBytes() );
        }
        
        return ( data );
    }
    
    /**
     * Gets one pixel.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param data the target pixel data
     * 
     * @return the pixel data back again.
     */
    public final byte[] getPixel( int x, int y, byte[] data )
    {
        return ( getPixel( getDataOffset( x, y ), data ) );
    }
    
    private final void setPixelLine( int trgByteOffset, int length, byte[] data, int srcByteOffset )
    {
        if ( this.data == null )
        {
            this.dataBuffer.position( trgByteOffset );
            this.dataBuffer.put( data, srcByteOffset, length * getPixelBytes() );
            this.dataBuffer.position( 0 );
        }
        else
        {
            System.arraycopy( data, srcByteOffset, this.data, trgByteOffset, length * getPixelBytes() );
        }
    }
    
    /**
     * Sets one (part of a) pixel line.
     * 
     * @param x the x-cordinate of the starting location
     * @param y the y-cordinate of the starting location
     * @param length the number of pixels to write
     * @param data the source pixel data
     * @param srcOffset the offset in the source array
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void setPixelLine( int x, int y, int length, byte[] data, int srcOffset, boolean markDirty, Rect2i dirtyRect )
    {
        setPixelLine( getDataOffset( x, y ), length, data, srcOffset );
        
        if ( markDirty )
        {
            markDirty( x, y, length, 1 );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x, y, length, 1 );
        }
    }
    
    private final byte[] getPixelLine( int offset, int length, byte[] data )
    {
        if ( this.data == null )
        {
            this.dataBuffer.position( offset );
            this.dataBuffer.get( data, 0, length * getPixelBytes() );
            this.dataBuffer.position( 0 );
        }
        else
        {
            System.arraycopy( this.data, offset, data, 0, length * getPixelBytes() );
        }
        
        return ( data );
    }
    
    /**
     * Gets one (part of a) pixel line.
     * 
     * @param x the x-cordinate of the starting location
     * @param y the y-cordinate of the starting location
     * @param data the target pixel data
     * @param length the number of pixels to read
     * 
     * @return the pixel data
     */
    public final byte[] getPixelLine( int x, int y, byte[] data, int length )
    {
        return ( getPixelLine( getDataOffset( x, y ), length, data ) );
    }
    
    private static final byte[] getPixelLine( byte[] srcData, int offset, final int length, byte[] data )
    {
        if ( ( ByteOrderManager.RED == 3 ) && ( ByteOrderManager.GREEN == 2 ) && ( ByteOrderManager.BLUE == 1 ) && ( ByteOrderManager.ALPHA == 0 ) )
        {
            System.arraycopy( srcData, offset, data, 0, length * 4 );
        }
        else
        {
            for ( int i = 0; i < length * 4; i += 4 )
            {
                data[i + ByteOrderManager.RED] = srcData[ offset + i + 3 ];
                data[i + ByteOrderManager.GREEN] = srcData[ offset + i + 2 ];
                data[i + ByteOrderManager.BLUE] = srcData[ offset + i + 1 ];
                data[i + ByteOrderManager.ALPHA] = srcData[ offset + i + 0 ];
            }
        }
        
        return ( data );
    }
    
    private static final byte[] getPixelLine( byte[] srcData, int srcImageWidth, int x, int y, byte[] data, int length )
    {
        return ( getPixelLine( srcData, getDataOffset( x, y, srcImageWidth, 4 ), length, data ) );
    }
    
    //private static final int V255_255 = 255 * 255;
    //private static final int V255_255_255 = 255 * 255 * 255;
    
    private static final byte[] combinePixels( final byte[] src,
                                               final int srcByteOffset,
                                               final int srcPixelSize,
                                               final TextureImage2D trgIC, final int trgPixelSize,
                                               final byte[] trg, final int trgByteOffset,
                                               final int numPixels,
                                               final boolean overwrite
                                             )
    {
        if ( srcPixelSize == 3 )
        {
            if ( trgPixelSize == 3 )
            {
                return ( src );
            }
            
            // target has size 4
            
            //trgIC.getPixelLine( trgByteOffset, numPixels, trg );
            
            int j = srcByteOffset;
            int k = 0;
            for ( int i = 0; i < numPixels; i++ )
            {
                trg[ k + ByteOrderManager.RED ] = src[ j + ByteOrderManager.RED ];
                trg[ k + ByteOrderManager.GREEN ] = src[ j + ByteOrderManager.GREEN ];
                trg[ k + ByteOrderManager.BLUE ] = src[ j + ByteOrderManager.BLUE ];
                trg[ k + ByteOrderManager.ALPHA ] = (byte)255;
                
                j += srcPixelSize;
                k += trgPixelSize;
            }
        }
        else if ( srcPixelSize == 4 )
        {
            if ( trgPixelSize == 3 )
            {
                if ( !overwrite )
                    trgIC.getPixelLine( trgByteOffset, numPixels, trg );
                
                int j = srcByteOffset;
                int k = 0;
                for ( int i = 0; i < numPixels; i++ )
                {
                    final int srcR = src[ j + ByteOrderManager.RED ] & 0xFF;
                    final int srcG = src[ j + ByteOrderManager.GREEN ] & 0xFF;
                    final int srcB = src[ j + ByteOrderManager.BLUE ] & 0xFF;
                    final int srcA = src[ j + ByteOrderManager.ALPHA ] & 0xFF;
                    
                    if ( overwrite )
                    {
                        trg[ k + ByteOrderManager.RED ] = (byte)( srcR * srcA / 255 );
                        trg[ k + ByteOrderManager.GREEN ] = (byte)( srcG * srcA / 255 );
                        trg[ k + ByteOrderManager.BLUE ] = (byte)( srcB * srcA / 255 );
                    }
                    else
                    {
                        final int trgR = trg[ k + ByteOrderManager.RED ] & 0xFF;
                        final int trgG = trg[ k + ByteOrderManager.GREEN ] & 0xFF;
                        final int trgB = trg[ k + ByteOrderManager.BLUE ] & 0xFF;
                        
                        trg[ k + ByteOrderManager.RED ] = (byte)( ( srcR * srcA / 255 ) + ( trgR * ( 255 - srcA ) / 255 ) );
                        trg[ k + ByteOrderManager.GREEN ] = (byte)( ( srcG * srcA / 255 ) + ( trgG * ( 255 - srcA ) / 255 ) );
                        trg[ k + ByteOrderManager.BLUE ] = (byte)( ( srcB * srcA / 255 ) + ( trgB * ( 255 - srcA ) / 255 ) );
                    }
                    
                    j += srcPixelSize;
                    k += trgPixelSize;
                }
            }
            else if ( trgPixelSize == 4 )
            {
                if ( overwrite )
                    return ( src );
                
                trgIC.getPixelLine( trgByteOffset, numPixels, trg );
                
                int j = srcByteOffset;
                int k = 0;
                for ( int i = 0; i < numPixels; i++ )
                {
                    final int srcR = src[ j + ByteOrderManager.RED ] & 0xFF;
                    final int srcG = src[ j + ByteOrderManager.GREEN ] & 0xFF;
                    final int srcB = src[ j + ByteOrderManager.BLUE ] & 0xFF;
                    final int srcA = src[ j + ByteOrderManager.ALPHA ] & 0xFF;
                    
                    final int trgR = trg[ k + ByteOrderManager.RED ] & 0xFF;
                    final int trgG = trg[ k + ByteOrderManager.GREEN ] & 0xFF;
                    final int trgB = trg[ k + ByteOrderManager.BLUE ] & 0xFF;
                    final int trgA = trg[ k + ByteOrderManager.ALPHA ] & 0xFF;
                    
                    final float rs = srcR / 255f;
                    final float gs = srcG / 255f;
                    final float bs = srcB / 255f;
                    final float as = srcA / 255f;
                    
                    final float rd = trgR / 255f;
                    final float gd = trgG / 255f;
                    final float bd = trgB / 255f;
                    final float ad = trgA / 255f;
                    
                    final float rr = rs * as + rd * ad * ( 1.0f - as );
                    final float gr = gs * as + gd * ad * ( 1.0f - as );
                    final float br = bs * as + bd * ad * ( 1.0f - as );
                    final float ar = as + ad * ( 1.0f - as );
                    
                    trg[ k + ByteOrderManager.RED ] = (byte)( Math.min( rr, 1.0f ) * 255 );
                    trg[ k + ByteOrderManager.GREEN ] = (byte)( Math.min( gr, 1.0f ) * 255 );
                    trg[ k + ByteOrderManager.BLUE ] = (byte)( Math.min( br, 1.0f ) * 255 );
                    trg[ k + ByteOrderManager.ALPHA ] = (byte)( Math.min( ar, 1.0f ) * 255 );
                    
                    /*
                    trg[ k + ByteOrderManager.RED ] = (byte)( ( 255 * srcR * srcA + ( 255 - srcA ) * trgR * trgA ) / V255_255_255 );
                    trg[ k + ByteOrderManager.GREEN ] = (byte)( ( 255 * srcG * srcA + ( 255 - srcA ) * trgG * trgA ) / V255_255_255 );
                    trg[ k + ByteOrderManager.BLUE ] = (byte)( ( 255 * srcB * srcA + ( 255 - srcA ) * trgB * trgA ) / V255_255_255 );
                    trg[ k + ByteOrderManager.ALPHA ] = (byte)( ( 255 * ( srcA + trgA ) - srcA * trgA ) / V255_255 );
                    */
                    
                    j += srcPixelSize;
                    k += trgPixelSize;
                }
            }
        }
        
        return ( trg );
    }
    
    private final byte[] getPixelLineBuffer1( int size )
    {
        if ( ( pixelRow1 == null ) || ( pixelRow1.length < size ) )
            pixelRow1 = new byte[ Math.min( size, getMaxWidth() * 4 ) ];
        
        return ( pixelRow1 );
    }
    
    private final byte[] getPixelLineBuffer2( int size )
    {
        if ( ( pixelRow2 == null ) || ( pixelRow2.length < size ) )
            pixelRow2 = new byte[ Math.min( size, getMaxWidth() * 4 ) ];
        
        return ( pixelRow2 );
    }
    
    public void copyImageDataFrom( BufferedImage srcImage, int srcX, int srcY, int srcWidth, int srcHeight, int trgX, int trgY, int trgWidth, int trgHeight, boolean overwrite, boolean markDirty, Rect2i dirtyRect )
    {
        if ( ( srcImage.getType() != BufferedImage.TYPE_3BYTE_BGR ) && ( srcImage.getType() != BufferedImage.TYPE_4BYTE_ABGR ) )
            throw new IllegalArgumentException( "Only TYPE_3BYTE_BGR and TYPE_4BYTE_ABGR images are supported." );
        
        if ( ( srcX >= srcImage.getWidth() ) || ( srcY >= srcImage.getHeight() ) )
            return;
        
        if ( ( trgX + trgWidth < clipRect.getLeft() ) ||
             ( trgY + trgHeight < clipRect.getTop() ) ||
             ( trgX >= clipRect.getLeft() + clipRect.getWidth() ) ||
             ( trgY >= clipRect.getTop() + clipRect.getHeight() ) )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            return;
        }
        
        srcWidth = Math.min( srcImage.getWidth() - srcX, srcWidth );
        srcHeight = Math.min( srcImage.getHeight() - srcY, srcHeight );
        
        if ( trgX < clipRect.getLeft() )
        {
            int oldTrgX = trgX;
            trgX = clipRect.getLeft() - ( ( clipRect.getLeft() - trgX ) % srcWidth );
            trgWidth -= trgX - oldTrgX;
        }
        
        if ( trgY < clipRect.getTop() )
        {
            int oldTrgY = trgY;
            trgY = clipRect.getTop() - ( ( clipRect.getTop() - trgY ) % srcHeight );
            trgHeight -= trgY - oldTrgY;
        }
        
        if ( trgX + trgWidth > clipRect.getLeft() + clipRect.getWidth() )
        {
            trgWidth = (int)Math.ceil( (double)( clipRect.getLeft() + clipRect.getWidth() - trgX ) / (double)srcWidth ) * srcWidth;
        }
        
        /*
        if ( trgY + trgHeight > clipRect.getTop() + clipRect.getHeight() )
        {
            trgHeight = (int)Math.ceil( (double)( clipRect.getTop() + clipRect.getHeight() - trgY ) / (double)srcHeight ) * srcHeight;
        }
        */
        
        final byte[] srcData = ( (DataBufferByte)srcImage.getRaster().getDataBuffer() ).getData();
        final int srcImageWidth = srcImage.getWidth();
        
        final int srcPixelBytes = ( srcImage.getType() == BufferedImage.TYPE_3BYTE_BGR ) ? 3 : 4;
        final int trgPixelBytes = this.getPixelBytes();
        
        byte[] srcBuffer = getPixelLineBuffer1( srcWidth * srcPixelBytes );
        byte[] trgBuffer = getPixelLineBuffer2( srcWidth * trgPixelBytes );
        
        final int y_ = yUp ? ( getMaxHeight() - getHeight() ) : 0;
        
        final int x0 = Math.max( clipRect.getLeft(), trgX );
        final int x1 = Math.min( clipRect.getLeft() + clipRect.getWidth(), trgX + trgWidth );
        final int y0 = Math.max( clipRect.getTop(), trgY );
        final int y1 = Math.min( clipRect.getTop() + clipRect.getHeight(), trgY + trgHeight );
        
        int srcJ = srcY;
        for ( int j = y0; j < y1; j++ )
        {
            //int srcJ = srcY + ( ( j - trgY ) % srcHeight );
            
            getPixelLine( srcData, srcImageWidth/*, srcPixelSize*/, srcX, srcJ, srcBuffer, srcWidth );
            
            int trgX_ = trgX;
            int trgWidth_ = trgWidth;
            int trgLength_ = srcWidth;
            while ( trgX_ < trgX + trgWidth )
            {
                if ( trgWidth_ < srcWidth )
                    trgLength_ = trgWidth_;
                
                if ( trgX_ + trgLength_ >= x1 )
                    trgLength_ = x1 - trgX_;
                
                int trgX__ = Math.max( x0, trgX_ );
                int trgByteOffset = this.getDataOffset( trgX__, y_ + j );
                int srcPixelOffset = ( trgX__ - trgX_ );
                
                byte[] pixels = combinePixels( srcBuffer, srcPixelOffset * srcPixelBytes, srcPixelBytes, this, trgPixelBytes, trgBuffer, trgByteOffset, trgLength_ - srcPixelOffset, overwrite );
                
                if ( ( srcPixelBytes == trgPixelBytes ) && overwrite )
                    this.setPixelLine( trgByteOffset, trgLength_ - srcPixelOffset, pixels, srcPixelOffset * srcPixelBytes );
                else
                    this.setPixelLine( trgByteOffset, trgLength_ - srcPixelOffset, pixels, 0 );
                
                trgX_ += srcWidth;
                trgWidth_ -= srcWidth;
                
                srcJ++;
            }
        }
        
        if ( markDirty )
        {
            markDirty( x0, y0, x1 - x0 + 1, y1 - y0 + 1 );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x0, y0, x1 - x0 + 1, y1 - y0 + 1 );
        }
    }
    
    /**
     * Copies the image data from the given {@link TextureImage2D}
     * and writes them to this image at the given position.
     * 
     * @param srcTI source image
     * @param srcX the rectangle's left to copy from the source {@link TextureImage2D}.
     * @param srcY the rectangle's top to copy from the source {@link TextureImage2D}.
     * @param srcWidth the rectangle's width to copy from the source {@link TextureImage2D}.
     * @param srcHeight the rectangle's height to copy from the source {@link TextureImage2D}.
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param trgWidth the targetWidth (tiled or clipped if necessary)
     * @param trgHeight the targetHeight (tiled or clipped if necessary)
     * @param overwrite
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    private void copyImageDataFrom( TextureImage2D srcTI, int srcX, int srcY, int srcWidth, int srcHeight, int trgX, int trgY, int trgWidth, int trgHeight, boolean overwrite, boolean markDirty, Rect2i dirtyRect )
    {
        if ( ( srcX >= srcTI.getWidth() ) || ( srcY >= srcTI.getHeight() ) )
            return;
        
        if ( ( trgX + trgWidth < clipRect.getLeft() ) ||
             ( trgY + trgHeight < clipRect.getTop() ) ||
             ( trgX >= clipRect.getLeft() + clipRect.getWidth() ) ||
             ( trgY >= clipRect.getTop() + clipRect.getHeight() ) )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            return;
        }
        
        srcWidth = Math.min( srcTI.getWidth() - srcX, srcWidth );
        srcHeight = Math.min( srcTI.getHeight() - srcY, srcHeight );
        
        if ( trgX < clipRect.getLeft() )
        {
            int oldTrgX = trgX;
            trgX = clipRect.getLeft() - ( ( clipRect.getLeft() - trgX ) % srcWidth );
            trgWidth -= trgX - oldTrgX;
        }
        
        if ( trgY < clipRect.getTop() )
        {
            int oldTrgY = trgY;
            trgY = clipRect.getTop() - ( ( clipRect.getTop() - trgY ) % srcHeight );
            trgHeight -= trgY - oldTrgY;
        }
        
        if ( trgX + trgWidth > clipRect.getLeft() + clipRect.getWidth() )
        {
            trgWidth = (int)Math.ceil( (double)( clipRect.getLeft() + clipRect.getWidth() - trgX ) / (double)srcWidth ) * srcWidth;
        }
        
        /*
        if ( trgY + trgHeight > clipRect.getTop() + clipRect.getHeight() )
        {
            trgHeight = (int)Math.ceil( (double)( clipRect.getTop() + clipRect.getHeight() - trgY ) / (double)srcHeight ) * srcHeight;
        }
        */
        
        final int srcPixelSize = srcTI.getPixelBytes();
        final int trgPixelSize = this.getPixelBytes();
        
        byte[] srcBuffer = srcTI.getPixelLineBuffer1( srcWidth * srcPixelSize );
        byte[] trgBuffer = this.getPixelLineBuffer2( srcWidth * trgPixelSize );
        
        final int y_ = yUp ? ( getMaxHeight() - getHeight() ) : 0;
        
        final int x0 = Math.max( clipRect.getLeft(), trgX );
        final int x1 = Math.min( clipRect.getLeft() + clipRect.getWidth() - 1, trgX + trgWidth - 1 );
        final int y0 = Math.max( clipRect.getTop(), trgY );
        final int y1 = Math.min( clipRect.getTop() + clipRect.getHeight() - 1, trgY + trgHeight - 1 );
        
        for ( int j = y0; j <= y1; j++ )
        {
            int srcJ = srcY + ( ( j - trgY ) % srcHeight );
            
            srcTI.getPixelLine( srcX, srcJ, srcBuffer, srcWidth );
            
            int trgX_ = trgX;
            int trgWidth_ = trgWidth;
            int trgLength_ = srcWidth;
            while ( trgX_ < trgX + trgWidth )
            {
                if ( trgWidth_ < srcWidth )
                    trgLength_ = trgWidth_;
                
                if ( trgX_ + trgLength_ >= x1 )
                    trgLength_ = x1 - trgX_ + 1;
                
                int trgX__ = Math.max( x0, trgX_ );
                int trgByteOffset = this.getDataOffset( trgX__, y_ + j );
                int srcPixelOffset = ( trgX__ - trgX_ );
                
                byte[] pixels = combinePixels( srcBuffer, srcPixelOffset * srcPixelSize, srcPixelSize, this, trgPixelSize, trgBuffer, trgByteOffset, trgLength_ - srcPixelOffset, overwrite );
                
                if ( ( srcPixelSize == trgPixelSize ) && overwrite )
                    this.setPixelLine( trgByteOffset, trgLength_ - srcPixelOffset, pixels, srcPixelOffset * srcPixelSize );
                else
                    this.setPixelLine( trgByteOffset, trgLength_ - srcPixelOffset, pixels, 0 );
                
                trgX_ += srcWidth;
                trgWidth_ -= srcWidth;
            }
        }
        
        if ( markDirty )
        {
            markDirty( x0, y0, x1 - x0 + 1, y1 - y0 + 1 );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x0, y0, x1 - x0 + 1, y1 - y0 + 1 );
        }
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and honors the alpha channels (if any).
     * 
     * @param srcTI source image
     * @param srcX the rectangle's left to copy from the source {@link TextureImage2D}.
     * @param srcY the rectangle's top to copy from the source {@link TextureImage2D}.
     * @param srcWidth the rectangle's width to copy from the source {@link TextureImage2D}.
     * @param srcHeight the rectangle's height to copy from the source {@link TextureImage2D}.
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param trgWidth the targetWidth (tiled or clipped if necessary)
     * @param trgHeight the targetHeight (tiled or clipped if necessary)
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void drawImage( TextureImage2D srcTI, int srcX, int srcY, int srcWidth, int srcHeight, int trgX, int trgY, int trgWidth, int trgHeight, boolean markDirty, Rect2i dirtyRect )
    {
        copyImageDataFrom( srcTI, srcX, srcY, srcWidth, srcHeight, trgX, trgY, trgWidth, trgHeight, false, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and honors the alpha channels (if any).
     * 
     * @param srcTI source image
     * @param srcX the rectangle's left to copy from the source {@link TextureImage2D}.
     * @param srcY the rectangle's top to copy from the source {@link TextureImage2D}.
     * @param srcWidth the rectangle's width to copy from the source {@link TextureImage2D}.
     * @param srcHeight the rectangle's height to copy from the source {@link TextureImage2D}.
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void drawImage( TextureImage2D srcTI, int srcX, int srcY, int srcWidth, int srcHeight, int trgX, int trgY, boolean markDirty, Rect2i dirtyRect )
    {
        drawImage( srcTI, srcX, srcY, srcWidth, srcHeight, trgX, trgY, srcWidth, srcHeight, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and honors the alpha channels (if any).
     * 
     * @param srcTI source image
     * @param srcRect the rectangle to copy from the source {@link TextureImage2D}.
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void drawImage( TextureImage2D srcTI, Rect2i srcRect, int trgX, int trgY, boolean markDirty, Rect2i dirtyRect )
    {
        drawImage( srcTI, srcRect.getLeft(), srcRect.getTop(), srcRect.getWidth(), srcRect.getHeight(), trgX, trgY, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and honors the alpha channels (if any).
     * 
     * @param srcTI source image
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void drawImage( TextureImage2D srcTI, int trgX, int trgY, boolean markDirty, Rect2i dirtyRect )
    {
        //drawImage( srcTI, 0, 0, srcTI.getWidth(), srcTI.getHeight(), trgX, trgY, markDirty, dirtyRect );
        drawImage( srcTI, 0, 0, srcTI.getWidth(), srcTI.getHeight(), trgX, trgY, markDirty, dirtyRect );
    }
    
    /**
     * Fills the given rectangle with the specified color and combines alpha channels if necessary.
     * 
     * @param color the color to fill with
     * @param offsetX the destination x coordinate
     * @param offsetY the destination y coordinate
     * @param width the destination area width
     * @param height the destination area height
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public void fillRectangle( java.awt.Color color, int offsetX, int offsetY, int width, int height, boolean markDirty, Rect2i dirtyRect )
    {
        //if ( !color.hasAlpha() )
        if ( color.getAlpha() == 255 )
        {
            clear( color, offsetX, offsetY, width, height, markDirty, dirtyRect );
            return;
        }
        
        int srcPixelSize = 4;
        byte[] pixel = getPixelLineBuffer1( srcPixelSize );
        
        pixel[ByteOrderManager.RED] = (byte)color.getRed();
        pixel[ByteOrderManager.GREEN] = (byte)color.getGreen();
        pixel[ByteOrderManager.BLUE] = (byte)color.getBlue();
        pixel[ByteOrderManager.ALPHA] = (byte)color.getAlpha(); //( (byte)255 - color.getAlphaByte() );
        
        int trgPixelSize = this.getPixelBytes();
        byte[] trgBuffer = getPixelLineBuffer2( trgPixelSize );
        
        final int x0 = Math.max( clipRect.getLeft(), offsetX );
        final int x1 = Math.min( clipRect.getLeft() + clipRect.getWidth(), offsetX + width );
        final int y0 = Math.max( clipRect.getTop(), offsetY );
        final int y1 = Math.min( clipRect.getTop() + clipRect.getHeight(), offsetY + height );
        final int y_ = yUp ? ( getMaxHeight() - getHeight() ) : 0;
        
        for ( int j = y0; j < y1; j++ )
        {
            for ( int i = x0; i < x1; i++ )
            {
                int trgOffset = this.getDataOffset( i, y_ + j );
                byte[] newPixel = combinePixels( pixel, 0, getPixelBytes(), this, getPixelBytes(), trgBuffer, trgOffset, 1, false );
                this.setPixel( trgOffset, newPixel );
            }
        }
        
        if ( markDirty )
        {
            markDirty( x0, y0, x1 - x0 + 1, y1 - y0 + 1 );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x0, y0, x1 - x0 + 1, y1 - y0 + 1 );
        }
    }
    
    /**
     * Fills the complete (used part of the) texture with the specified color and combines alpha channels if necessary.
     * 
     * @param color the color to fill with
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void fillFullRectangle( java.awt.Color color, boolean markDirty, Rect2i dirtyRect )
    {
        //fillRectangle( color, 0, 0, getMaxWidth(), getMaxHeight(), markDirty, dirtyRect );
        fillRectangle( color, 0, 0, getWidth(), getHeight(), markDirty, dirtyRect );
    }
    
    /**
     * Draws a horizontal line of pixels and combines alpha channels if necessary.
     * 
     * @param pixels the pixel data array
     * @param startX the x-coordinate of the starting location
     * @param startY the y-coordinate of the starting location
     * @param length the number of pixels to draw
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public void drawPixelLine( byte[] pixels, int startX, int startY, int length, boolean markDirty, Rect2i dirtyRect )
    {
        if ( ( clipRect.getLeft() > startX + length - 1 ) || ( clipRect.getLeft() + clipRect.getWidth() - 1 < startX ) )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            return;
        }
        
        if ( ( clipRect.getTop() > startY ) || ( clipRect.getTop() + clipRect.getHeight() - 1 < startY ) )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            return;
        }
        
        int trgPixelSize = this.getPixelBytes();
        byte[] trgBuffer = getPixelLineBuffer2( trgPixelSize );
        
        final int x0 = Math.max( clipRect.getLeft(), startX );
        final int x1 = Math.min( clipRect.getLeft() + clipRect.getWidth() - 1, startX + length - 1 );
        length = x1 - x0 + 1;
        final int y_ = yUp ? ( getMaxHeight() - getHeight() ) : 0;
        
        //int srcByteOffset = ( x0 - startX ) * pixelSize;
        int srcByteOffset = ( x0 - startX ) * this.pixelBytes;
        int trgByteOffset = this.getDataOffset( x0, y_ + startY );
        byte[] newPixels = combinePixels( pixels, srcByteOffset, pixelSize, this, trgPixelSize, trgBuffer, trgByteOffset, length, false );
        this.setPixelLine( trgByteOffset, length, newPixels, srcByteOffset );
        
        if ( markDirty )
        {
            markDirty( x0, startY, x1 - x0 + 1, 1 );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x0, startY, x1 - x0 + 1, 1 );
        }
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and simply overwrites anything.
     * 
     * @param srcTI source image
     * @param srcX the rectangle's left to copy from the source {@link TextureImage2D}.
     * @param srcY the rectangle's top to copy from the source {@link TextureImage2D}.
     * @param srcWidth the rectangle's width to copy from the source {@link TextureImage2D}.
     * @param srcHeight the rectangle's height to copy from the source {@link TextureImage2D}.
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param trgWidth the targetWidth (tiled or clipped if necessary)
     * @param trgHeight the targetHeight (tiled or clipped if necessary)
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( TextureImage2D srcTI, int srcX, int srcY, int srcWidth, int srcHeight, int trgX, int trgY, int trgWidth, int trgHeight, boolean markDirty, Rect2i dirtyRect )
    {
        copyImageDataFrom( srcTI, srcX, srcY, srcWidth, srcHeight, trgX, trgY, trgWidth, trgHeight, true, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and simply overwrites anything.
     * 
     * @param srcTI source image
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param trgWidth the targetWidth (tiled or clipped if necessary)
     * @param trgHeight the targetHeight (tiled or clipped if necessary)
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( TextureImage2D srcTI, int trgX, int trgY, int trgWidth, int trgHeight, boolean markDirty, Rect2i dirtyRect )
    {
        copyImageDataFrom( srcTI, 0, 0, srcTI.getWidth(), srcTI.getHeight(), trgX, trgY, trgWidth, trgHeight, true, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and simply overwrites anything.
     * 
     * @param srcTI source image
     * @param srcX the rectangle's left to copy from the source {@link TextureImage2D}.
     * @param srcY the rectangle's top to copy from the source {@link TextureImage2D}.
     * @param srcWidth the rectangle's width to copy from the source {@link TextureImage2D}.
     * @param srcHeight the rectangle's height to copy from the source {@link TextureImage2D}.
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( TextureImage2D srcTI, int srcX, int srcY, int srcWidth, int srcHeight, int trgX, int trgY, boolean markDirty, Rect2i dirtyRect )
    {
        clear( srcTI, srcX, srcY, srcWidth, srcHeight, trgX, trgY, srcWidth, srcHeight, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and simply overwrites anything.
     * 
     * @param srcTI source image
     * @param srcRect the rectangle to copy from the source {@link TextureImage2D}.
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( TextureImage2D srcTI, Rect2i srcRect, int trgX, int trgY, boolean markDirty, Rect2i dirtyRect )
    {
        clear( srcTI, srcRect.getLeft(), srcRect.getTop(), srcRect.getWidth(), srcRect.getHeight(), trgX, trgY, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and simply overwrites anything.
     * 
     * @param srcTI source image
     * @param trgX target x-coordinate
     * @param trgY target y-coordinate
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( TextureImage2D srcTI, int trgX, int trgY, boolean markDirty, Rect2i dirtyRect )
    {
        clear( srcTI, 0, 0, srcTI.getWidth(), srcTI.getHeight(), trgX, trgY, markDirty, dirtyRect );
    }
    
    /**
     * Draws the given {@link TextureImage2D} onto this one and simply overwrites anything.
     * 
     * @param srcTI source image
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( TextureImage2D srcTI, boolean markDirty, Rect2i dirtyRect )
    {
        clear( srcTI, 0, 0, srcTI.getWidth(), srcTI.getHeight(), 0, 0, markDirty, dirtyRect );
    }
    
    /**
     * Clears the given rectangle's outline with the specified color.
     * 
     * @param color the color to draw with
     * @param offsetX the destination x coordinate
     * @param offsetY the destination y coordinate
     * @param width the destination area width
     * @param height the destination area height
     * @param lineWidth the width of the outline
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public void clearOutline( java.awt.Color color, int offsetX, int offsetY, final int width, int height, int lineWidth, boolean markDirty, Rect2i dirtyRect )
    {
        final int x0 = Math.max( clipRect.getLeft(), offsetX );
        final int x1 = Math.min( clipRect.getLeft() + clipRect.getWidth(), offsetX + width - 1 );
        final int w = x1 - x0 + 1;
        final int y0 = Math.max( clipRect.getTop(), offsetY );
        final int y1 = Math.min( clipRect.getTop() + clipRect.getHeight(), offsetY + height - 1 );
        final int h = y1 - y0 + 1;
        
        final byte[] pixel = getPixelLineBuffer1( Math.max( 1, w ) * this.getPixelBytes() );
        //final byte[] pixel = getPixelLineBuffer1( this.getPixelBytes() );
        
        switch ( this.getPixelBytes() )
        {
            case 4:
                pixel[ByteOrderManager.RED] = (byte)color.getRed();
                pixel[ByteOrderManager.GREEN] = (byte)color.getGreen();
                pixel[ByteOrderManager.BLUE] = (byte)color.getBlue();
                pixel[ByteOrderManager.ALPHA] = (byte)color.getAlpha(); //( (byte)255 - color.getAlphaByte() );
                for ( int i = 4; i < w * 4; i += 4 )
                {
                    System.arraycopy( pixel, 0, pixel, i, 4 );
                }
                break;
            case 3:
                pixel[ByteOrderManager.RED] = (byte)color.getRed();
                pixel[ByteOrderManager.GREEN] = (byte)color.getGreen();
                pixel[ByteOrderManager.BLUE] = (byte)color.getBlue();
                for ( int i = 3; i < w * 3; i += 3 )
                {
                    System.arraycopy( pixel, 0, pixel, i, 3 );
                }
                break;
        }
        
        final int y_ = yUp ? ( getMaxHeight() - getHeight() ) : 0;
        
        final int stride = getMaxWidth() * getPixelBytes();
        int dataOffset = getDataOffset( x0, y_ + y0 );
        
        for ( int j = 0; j < lineWidth; j++ )
        {
            setPixelLine( dataOffset, w, pixel, 0 );
            dataOffset += stride;
        }
        
        for ( int j = lineWidth * 2; j < h; j++ )
        {
            setPixelLine( dataOffset, lineWidth, pixel, 0 );
            setPixelLine( dataOffset + ( w - lineWidth ) * getPixelBytes(), lineWidth, pixel, 0 );
            dataOffset += stride;
        }
        
        for ( int j = 0; j < lineWidth; j++ )
        {
            setPixelLine( dataOffset, w, pixel, 0 );
            dataOffset += stride;
        }
        
        if ( markDirty )
        {
            dirtyRect.set( x0, y0, w, lineWidth );
            dirtyRect.set( x0, y0 + lineWidth, lineWidth, h - lineWidth - lineWidth );
            dirtyRect.set( x1 - lineWidth + 1, y0 + lineWidth, lineWidth, h - lineWidth - lineWidth );
            dirtyRect.set( x0, y1 - lineWidth + 1, w, lineWidth );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x0, y0, w, h );
        }
    }
    
    /**
     * Clears the given rectangle with the specified color.
     * 
     * @param color the color to clear with
     * @param offsetX the destination x coordinate
     * @param offsetY the destination y coordinate
     * @param width the destination area width
     * @param height the destination area height
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public void clear( java.awt.Color color, int offsetX, int offsetY, final int width, int height, boolean markDirty, Rect2i dirtyRect )
    {
        final int x0 = Math.max( clipRect.getLeft(), offsetX );
        final int x1 = Math.min( clipRect.getLeft() + clipRect.getWidth() - 1, offsetX + width - 1 );
        final int w = x1 - x0 + 1;
        final int y0 = Math.max( clipRect.getTop(), offsetY );
        final int y1 = Math.min( clipRect.getTop() + clipRect.getHeight() - 1, offsetY + height - 1 );
        final int h = y1 - y0 + 1;
        
        final byte[] pixel = getPixelLineBuffer1( Math.max( 1, w ) * this.getPixelBytes() );
        //final byte[] pixel = getPixelLineBuffer1( this.getPixelBytes() );
        
        switch ( this.getPixelBytes() )
        {
            case 4:
                pixel[ByteOrderManager.RED] = (byte)color.getRed();
                pixel[ByteOrderManager.GREEN] = (byte)color.getGreen();
                pixel[ByteOrderManager.BLUE] = (byte)color.getBlue();
                pixel[ByteOrderManager.ALPHA] = (byte)color.getAlpha(); //( (byte)255 - color.getAlphaByte() );
                for ( int i = 4; i < w * 4; i += 4 )
                {
                    System.arraycopy( pixel, 0, pixel, i, 4 );
                }
                break;
            case 3:
                pixel[ByteOrderManager.RED] = (byte)color.getRed();
                pixel[ByteOrderManager.GREEN] = (byte)color.getGreen();
                pixel[ByteOrderManager.BLUE] = (byte)color.getBlue();
                for ( int i = 3; i < w * 3; i += 3 )
                {
                    System.arraycopy( pixel, 0, pixel, i, 3 );
                }
                break;
        }
        
        if ( ( w <= 0 ) || ( h <= 0 ) )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            return;
        }
        
        final int y_ = yUp ? ( getMaxHeight() - getHeight() ) : 0;
        
        final int stride = getMaxWidth() * getPixelBytes();
        int dataOffset = getDataOffset( x0, y_ + y0 );
        
        for ( int j = y0; j <= y1; j++ )
        {
            setPixelLine( dataOffset, w, pixel, 0 );
            dataOffset += stride;
        }
        
        if ( markDirty )
        {
            markDirty( x0, y0, w, h );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x0, y0, w, h );
        }
    }
    
    /**
     * Clears the whole (used part of the) texture with the specified color.
     * 
     * @param color the color to clear with
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( java.awt.Color color, boolean markDirty, Rect2i dirtyRect )
    {
        clear( color, 0, 0, getWidth(), getHeight(), markDirty, dirtyRect );
    }
    
    /**
     * Clears the given rectangle with a black-transparent color.
     * 
     * @param offsetX the destination x coordinate
     * @param offsetY the destination y coordinate
     * @param width the destination area width
     * @param height the destination area height
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( int offsetX, int offsetY, int width, int height, boolean markDirty, Rect2i dirtyRect )
    {
        clear( ColorUtils.BLACK_TRANSPARENT, offsetX, offsetY, width, height, markDirty, dirtyRect );
    }
    
    /**
     * Clears the whole (used part of the) texture with a black-transparent color.
     * 
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public final void clear( boolean markDirty, Rect2i dirtyRect )
    {
        clear( ColorUtils.BLACK_TRANSPARENT, 0, 0, getWidth(), getHeight(), markDirty, dirtyRect );
    }
    
    /**
     * Clears a horizontal line of pixels.
     * 
     * @param pixels the pixel data
     * @param startX the x-coordinate of the starting location
     * @param startY the y-coordinate of the starting location
     * @param length the number of pixels to clear
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public void clearPixelLine( byte[] pixels, int startX, int startY, int length, boolean markDirty, Rect2i dirtyRect )
    {
        if ( ( clipRect.getLeft() > startX + length - 1 ) || ( clipRect.getLeft() + clipRect.getWidth() - 1 < startX ) )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            return;
        }
        
        if ( ( clipRect.getTop() > startY ) || ( clipRect.getTop() + clipRect.getHeight() - 1 < startY ) )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            return;
        }
        
        final int x0 = Math.max( clipRect.getLeft(), startX );
        final int x1 = Math.min( clipRect.getLeft() + clipRect.getWidth() - 1, startX + length - 1 );
        length = x1 - x0 + 1;
        final int y_ = yUp ? ( getMaxHeight() - getHeight() ) : 0;
        
        //int srcByteOffset = ( x0 - startX ) * pixelBytes;
        int srcByteOffset = ( x0 - startX ) * this.pixelBytes;
        int trgByteOffset = this.getDataOffset( x0, y_ + startY );
        this.setPixelLine( trgByteOffset, length, pixels, srcByteOffset );
        
        if ( markDirty )
        {
            markDirty( x0, startY, x1 - x0 + 1, 1 );
        }
        
        if ( dirtyRect != null )
        {
            dirtyRect.set( x0, startY, x1 - x0 + 1, 1 );
        }
    }
    
    private static BufferedImage textImage = new BufferedImage( 256, 64, BufferedImage.TYPE_4BYTE_ABGR );
    private static Graphics2D textGraphics = textImage.createGraphics();
    private static FontMetrics fontMetrics = textGraphics.getFontMetrics();
    /*
    private static int textImageLineByteLength = textImage.getWidth() * 4;
    private static byte[] clearLine = null;
    
    private static byte[] getClearLine()
    {
        if ( ( clearLine == null ) || ( clearLine.length < textImage.getWidth() * 4 ) )
        {
            clearLine = new byte[ textImageLineByteLength ];
            
            clearLine[0] = (byte)ColorUtils.BLACK_TRANSPARENT.getAlpha();
            clearLine[1] = (byte)ColorUtils.BLACK_TRANSPARENT.getBlue();
            clearLine[2] = (byte)ColorUtils.BLACK_TRANSPARENT.getGreen();
            clearLine[3] = (byte)ColorUtils.BLACK_TRANSPARENT.getRed();
            for ( int i = 4; i < textImageLineByteLength; i += 4 )
            {
                System.arraycopy( clearLine, 0, clearLine, i, 4 );
            }
        }
        
        return ( clearLine );
    }
    */
    
    public static final java.awt.geom.Rectangle2D getStringBounds( String s, java.awt.Font font, boolean antiAliased )
    {
        if ( !textGraphics.getFont().equals( font ) )
        {
            textGraphics.setFont( font );
            textGraphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antiAliased ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF );
            
            fontMetrics = textGraphics.getFontMetrics();
        }
        
        return ( fontMetrics.getStringBounds( s, textGraphics ) );
    }
    
    public static final java.awt.geom.Rectangle2D getStringBounds( String s, FontProperty font )
    {
        return ( getStringBounds( s, font.getFont(), font.isAntiAliased() ) );
    }
    
    public static final int getStringWidth( String s, java.awt.Font font, boolean antiAliased )
    {
        return ( (int)getStringBounds( s, font, antiAliased ).getWidth() );
    }
    
    public static final int getStringWidth( String s, FontProperty font )
    {
        return ( (int)getStringBounds( s, font.getFont(), font.isAntiAliased() ).getWidth() );
    }
    
    public static final int getStringHeight( String s, java.awt.Font font, boolean antiAliased )
    {
        return ( (int)getStringBounds( s, font, antiAliased ).getHeight() );
    }
    
    public static final int getStringHeight( String s, FontProperty font )
    {
        return ( (int)getStringBounds( s, font.getFont(), font.isAntiAliased() ).getHeight() );
    }
    
    public static final int getFontAscent( java.awt.Font font )
    {
        if ( !textGraphics.getFont().equals( font ) )
        {
            textGraphics.setFont( font );
            
            fontMetrics = textGraphics.getFontMetrics();
        }
        
        return ( fontMetrics.getAscent() );
    }
    
    public static final int getFontDescent( java.awt.Font font )
    {
        if ( !textGraphics.getFont().equals( font ) )
        {
            textGraphics.setFont( font );
            
            fontMetrics = textGraphics.getFontMetrics();
        }
        
        return ( fontMetrics.getDescent() );
    }
    
    /**
     * Draws a String at the specified location.
     * 
     * @param s the String to draw
     * @param x the x-position
     * @param y the y-position of the String's baseline
     * @param bounds the String's bounds. If null, bounds will be created temporarily
     * @param font the Font to use
     * @param antiAliased anti aliased font?
     * @param color the Color to use
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     */
    public void drawString( String s, int x, int y, java.awt.geom.Rectangle2D bounds, java.awt.Font font, boolean antiAliased, java.awt.Color color, boolean markDirty, Rect2i dirtyRect )
    {
        /*
        if ( !textGraphics.getFont().equals( font ) )
        {
            textGraphics.setFont( font );
            
            fontMetrics = textGraphics.getFontMetrics();
        }
        
        if ( !textGraphics.getColor().equals( color ) )
        {
            textGraphics.setColor( color );
        }
        
        if ( bounds == null )
            bounds = fontMetrics.getStringBounds( s, textGraphics );
        
        int w = (int)bounds.getWidth();
        //int h = (int)( bounds.getHeight() + fontMetrics.getDescent() );
        int h = (int)bounds.getHeight();
        
        if ( ( textImage.getWidth() < w ) || ( textImage.getHeight() < h ) )
        {
            textImage = new BufferedImage( NumberUtil.roundUpPower2( w ), NumberUtil.roundUpPower2( h ), BufferedImage.TYPE_4BYTE_ABGR );
            textGraphics = textImage.createGraphics();
            textGraphics.setFont( font );
            textGraphics.setColor( color );
            fontMetrics = textGraphics.getFontMetrics();
            textImageLineByteLength = textImage.getWidth() * 4;
        }
        
        byte[] textData = ( (DataBufferByte)textImage.getRaster().getDataBuffer() ).getData();
        byte[] clearLine = getClearLine();
        
        // clear the used area
        int offset = 0;
        for ( int j = 0; j < h; j++ )
        {
            System.arraycopy( clearLine, 0, textData, offset, w * 4 );
            offset += textImageLineByteLength;
        }
        
        // draw the string
        textGraphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antiAliased ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF );
        textGraphics.drawString( s, 0, (int)-bounds.getY() );
        
        // copy the data to this TextureImage
        copyImageDataFrom( textImage, 0, 0, w, h, x, y + (int)bounds.getY(), w, h, false, markDirty, dirtyRect );
        */
        
        if ( bounds == null )
            bounds = getStringBounds( s, font, antiAliased );
        
        getTextureCanvas().drawString( s, x, y, bounds, font, antiAliased, color, markDirty, dirtyRect );
    }
    
    private TextureImage2D( int maxWidth, int maxHeight, int usedWidth, int usedHeight, boolean alpha, ByteBuffer dataBuffer, byte[] data, boolean isOffline )
    {
        this.width = Math.max( 1, maxWidth );
        this.height = Math.max( 1, maxHeight );
        
        this.usedWidth = Math.max( 1, usedWidth );
        this.usedHeight = Math.max( 1, usedHeight );
        
        this.pixelBytes = alpha ? 4 : 3;
        this.pixelSize = alpha ? 32 : 24;
        
        setClipRect( 0, 0, this.width, this.height );
        
        this.dataBuffer = dataBuffer;
        
        if ( dataBuffer == null )
        {
            if ( data == null )
            {
                this.data = new byte[ this.width * this.height * pixelBytes ];
            }
            else
            {
                if ( data.length < ( this.width * this.height * pixelBytes ) )
                    throw new IllegalArgumentException( "The data array is too small." );
                
                this.data = data;
            }
        }
        else
        {
            if ( dataBuffer.capacity() < ( this.width * this.height * pixelBytes ) )
                throw new IllegalArgumentException( "The dataBuffer is too small." );
            
            this.data = null;
        }
        
        this.isOffline = isOffline;
        
        this.updateList = isOffline ? null : new ArrayList<Rect2i>();
    }
    
    static TextureImage2D createOnlineTexture( int maxWidth, int maxHeight, int usedWidth, int usedHeight, boolean alpha )
    {
        return ( new TextureImage2D( maxWidth, maxHeight, usedWidth, usedHeight, alpha, null, null, false ) );
    }
    
    public static TextureImage2D createDrawTexture( int maxWidth, int maxHeight, int usedWidth, int usedHeight, boolean alpha )
    {
        return ( new TextureImage2D( maxWidth, maxHeight, usedWidth, usedHeight, alpha, null, null, true ) );
    }
    
    static TextureImage2D createOnlineTexture( int width, int height, boolean alpha )
    {
        return ( new TextureImage2D( width, height, width, height, alpha, null, null, false ) );
    }
    
    public static TextureImage2D createDrawTexture( int width, int height, boolean alpha )
    {
        return ( new TextureImage2D( width, height, width, height, alpha, null, null, true ) );
    }
    
    private static TextureImage2D getOrCreateDrawTexture( int width, int height, boolean alpha, TextureImage2D possibleResult, boolean usePowerOfTwoSizes, boolean isOffline )
    {
        int width2 = usePowerOfTwoSizes ? NumberUtil.roundUpPower2( width ) : width;
        int height2 = usePowerOfTwoSizes ? NumberUtil.roundUpPower2( height ) : height;
        
        if ( possibleResult != null )
        {
            if ( usePowerOfTwoSizes )
            {
                if ( ( width2 == possibleResult.getMaxWidth() ) && ( height2 == possibleResult.getMaxHeight() ) )
                {
                    if ( ( width != possibleResult.getWidth() ) || ( height != possibleResult.getHeight() ) )
                    {
                        possibleResult.clear( false, null );
                        possibleResult.resize( width, height );
                        
                        possibleResult.clearUpdateList();
                    }
                    
                    return ( possibleResult );
                }
            }
            else if ( ( width == possibleResult.getWidth() ) || ( height == possibleResult.getHeight() ) )
            {
                //possibleResult.clearUpdateList();
                
                return ( possibleResult );
            }
        }
        
        return ( new TextureImage2D( width2, height2, width, height, alpha, null, null, isOffline ) );
    }
    
    static TextureImage2D getOrCreateOnlineTexture( int width, int height, boolean alpha, TextureImage2D possibleResult, boolean usePowerOfTwoSizes )
    {
        return ( getOrCreateDrawTexture( width, height, alpha, possibleResult, usePowerOfTwoSizes, false ) );
    }
    
    public static TextureImage2D getOrCreateDrawTexture( int width, int height, boolean alpha, TextureImage2D possibleResult, boolean usePowerOfTwoSizes )
    {
        return ( getOrCreateDrawTexture( width, height, alpha, possibleResult, usePowerOfTwoSizes, true ) );
    }
}
