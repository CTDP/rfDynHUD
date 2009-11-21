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

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import org.openmali.types.twodee.Rect2i;

/**
 * This is an adapter for pixel-perfect drawing onto a Texture2D.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class Texture2DCanvas extends Graphics2D
{
    private final TextureImage2D texImg;
    
    private Graphics2D graphics;
    private final AffineTransform baseAffineTransform;
    
    private Rect2i currentUpdateRect = null;
    private int currentlyAppliedUpdateRects = 0;
    
    private final Rect2i[] clipStack = new Rect2i[ 32 ];
    private int clipStackSize = 0;
    
    public final TextureImage2D getImage()
    {
        return ( texImg );
    }
    
    private final void markDirty( int x, int y, int width, int height )
    {
        if ( texImg == null )
            return;
        
        if ( currentUpdateRect != null )
        {
            if ( ( x >= currentUpdateRect.getLeft() ) &&
                 ( y >= currentUpdateRect.getTop() ) &&
                 ( x + width <= currentUpdateRect.getLeft() + currentUpdateRect.getWidth() ) &&
                 ( y + height <= currentUpdateRect.getTop() + currentUpdateRect.getHeight() )
               )
            {
                return;
            }
        }
        
        texImg.markDirty( x, y, width, height, true, true );
    }
    
    private final void markDirty()
    {
        markDirty( 0, 0, texImg.getUsedWidth(), texImg.getUsedHeight() );
    }
    
    public final void beginUpdateRegion( int x, int y, int width, int height )
    {
        if ( currentUpdateRect != null )
        {
            currentUpdateRect.combine( x, y, width, height );
        }
        else
        {
            currentUpdateRect = Rect2i.fromPool( x, y, width, height );
        }
        
        currentlyAppliedUpdateRects++;
    }
    
    public final void beginUpdateRegionComplete()
    {
        beginUpdateRegion( 0, 0, texImg.getUsedWidth(), texImg.getUsedHeight() );
    }
    
    public final void finishUpdateRegion()
    {
        if ( currentUpdateRect == null )
            return;
        
        currentlyAppliedUpdateRects--;
        
        if ( currentlyAppliedUpdateRects == 0 )
        {
            int l = currentUpdateRect.getLeft();
            int t = currentUpdateRect.getTop();
            int w = currentUpdateRect.getWidth();
            int h = currentUpdateRect.getHeight();
            Rect2i.toPool( currentUpdateRect );
            currentUpdateRect = null;
            markDirty( l, t, w, h );
        }
    }
    
    /*
    public final java.awt.geom.AffineTransform getBaseAffineTransform()
    {
        return ( baseAffineTransform );
    }
    */
    
    @Override
    public GraphicsConfiguration getDeviceConfiguration()
    {
        return ( graphics.getDeviceConfiguration() );
    }
    
    @Override
    public final void clearRect( int x, int y, int width, int height )
    {
        graphics.clearRect( x, y, width, height );
        
        markDirty( x, y, width, height );
    }
    
    public final void clearRect( Rect2i rect )
    {
        clearRect( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() );
    }
    
    @Override
    public void clipRect( int x, int y, int width, int height )
    {
        graphics.clipRect( x, y, width, height );        
    }
    
    public final void clip( Rect2i rect )
    {
        clipRect( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() );
    }
    
    @Override
    public final void clip( Shape shape )
    {
        graphics.clip( shape );
    }
    
    @Override
    public final void copyArea( int x, int y, int width, int height, int dx, int dy )
    {
        graphics.copyArea( x, y, width, height, dx, dy );
        
        markDirty( dx, dy, width, height );
    }
    
    public final void copyArea( Rect2i rect, int dx, int dy )
    {
        copyArea( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight(), dx, dy );
    }
    
    @Override
    public final void draw( Shape shape )
    {
        graphics.draw( shape );
        
        java.awt.Rectangle rect = shape.getBounds();
        
        markDirty( rect.x, rect.y, rect.width, rect.height );
    }
    
    @Override
    public final void drawArc( int x, int y, int width, int height, int startAngle, int arcAngle )
    {
        graphics.drawArc( x, y, width, height, startAngle, arcAngle );
        
        markDirty( x, y, width, height );
    }
    
    public final void drawCircle( int x, int y, int radius )
    {
        drawArc( x - radius, y - radius, radius + radius, radius + radius, 0, 360 );
    }
    
    @Override
    public final void drawBytes( byte[] data, int offset, int length, int x, int y )
    {
        graphics.drawBytes( data, offset, length, x, y );
        
        markDirty( x, y, texImg.getUsedWidth() - x, texImg.getUsedHeight() - y );
    }
    
    @Override
    public final void drawChars( char[] data, int offset, int length, int x, int y )
    {
        graphics.drawChars( data, offset, length, x, y );
        
        markDirty( x, y, texImg.getUsedWidth() - x, texImg.getUsedHeight() - y );
    }
    
    @Override
    public final void drawGlyphVector( GlyphVector g, float x, float y )
    {
        graphics.drawGlyphVector( g, x, y );
        
        markDirty( (int)x, (int)y, texImg.getUsedWidth() - (int)x, texImg.getUsedHeight() - (int)y );
    }
    
    @Override
    public final boolean drawImage( Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver imgOb )
    {
        if ( ( dx2 < 0 ) || ( dy2 < 0 ) || ( dx1 >= texImg.getUsedWidth() ) || ( dy1 >= texImg.getUsedHeight() ) )
            return ( true );
        
        final boolean result = graphics.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, imgOb );
        
        markDirty( dx1, dy1, dx2 - dx1 + 1, dy2 - dy1 + 1 );
        
        return ( result );
    }
    
    public final void drawImage( Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2 )
    {
        drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, (ImageObserver)null );
    }
    
    @Override
    public final boolean drawImage( Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgColor, ImageObserver imgOb )
    {
        final boolean result = graphics.drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgColor, imgOb );
        
        markDirty( dx1, dy1, dx2 - dx1 + 1, dy2 - dy1 + 1 );
        
        return ( result );
    }
    
    public final void drawImage( Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgColor )
    {
        drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgColor, (ImageObserver)null );
    }
    
    @Override
    public final boolean drawImage( Image img, AffineTransform xform, ImageObserver imgOb )
    {
        final boolean result = graphics.drawImage( img, xform, imgOb );
        
        markDirty();
        
        return ( result );
    }
    
    @Override
    public final void drawImage( BufferedImage img, BufferedImageOp op, int x, int y )
    {
        graphics.drawImage( img, op, x, y );
        
        markDirty( x, y, img.getWidth() - x, img.getHeight() - y );
    }
    
    @Override
    public final boolean drawImage( Image img, int x, int y, ImageObserver imgOb )
    {
        final boolean result = graphics.drawImage( img, x, y, imgOb );
        
        markDirty( x, y, img.getWidth( null ) - x, img.getHeight( null ) - y );
        
        return ( result );
    }
    
    public final void drawImage( Image img, int x, int y )
    {
        drawImage( img, x, y, (ImageObserver)null );
    }
    
    @Override
    public final boolean drawImage( Image img, int x, int y, Color bgColor, ImageObserver imgOb )
    {
        final boolean result = graphics.drawImage( img, x, y, bgColor, imgOb );
        
        markDirty( x, y, img.getWidth( null ) - x, img.getHeight( null ) - y );
        
        return ( result );
    }
    
    public final void drawImage( Image img, int x, int y, Color bgColor )
    {
        drawImage( img, x, y, bgColor, (ImageObserver)null );
    }
    
    @Override
    public final boolean drawImage( Image img, int x, int y, int width, int height, ImageObserver imgOb )
    {
        final boolean result = graphics.drawImage( img, x, y, width, height, null );
        
        markDirty( x, y, width, height );
        
        return ( result );
    }
    
    public final void drawImage( Image img, int x, int y, int width, int height )
    {
        drawImage( img, x, y, width, height, (ImageObserver)null );
    }
    
    @Override
    public final boolean drawImage( Image img, int x, int y, int width, int height, Color bgColor, ImageObserver imgOb )
    {
        final boolean result = graphics.drawImage( img, x, y, width, height, bgColor, imgOb );
        
        markDirty( x, y, width, height );
        
        return ( result );
    }
    
    public final void drawImage( Image img, int x, int y, int width, int height, Color bgColor )
    {
        drawImage( img, x, y, width, height, bgColor, (ImageObserver)null );
    }
    
    @Override
    public void drawRenderableImage( RenderableImage img, AffineTransform xform )
    {
        graphics.drawRenderableImage( img, xform );
        
        markDirty();
    }
    
    @Override
    public void drawRenderedImage( RenderedImage img, AffineTransform xform )
    {
        graphics.drawRenderedImage( img, xform );
        
        markDirty();
    }
    
    @Override
    public final void drawLine( int x1, int y1, int x2, int y2 )
    {
        //graphics.drawLine( x1, yy( y1 ), x2, yy( y2 ) );
        graphics.drawLine( x1, y1, x2, y2 );
        
        markDirty( x1, y1, x2 - x1, y2 - y1 );
    }
    
    public final void drawLineOffset( int x, int y, int dx, int dy )
    {
        drawLine( x, y, x + dx, y + dy );
    }
    
    @Override
    public final void drawOval( int x, int y, int width, int height )
    {
        graphics.drawOval( x, y, width, height );
        
        markDirty( x, y, width, height );
    }
    
    @Override
    public final void drawPolygon( Polygon polygon )
    {
        graphics.drawPolygon( polygon );
        
        java.awt.Rectangle rect = polygon.getBounds();
        
        markDirty( rect.x, rect.y, rect.width, rect.height );
    }
    
    @Override
    public final void drawPolygon( int[] xPoints, int[] yPoints, int nPoints )
    {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        for ( int i = 0; i < nPoints; i++ )
        {
            if ( xPoints[i] < minX )
                minX = xPoints[i];
            
            if ( xPoints[i] > maxX )
                maxX = xPoints[i];
        }
        
        for ( int i = 0; i < nPoints; i++ )
        {
            if ( yPoints[i] < minY )
                minY = yPoints[i];
            
            if ( yPoints[i] > maxY )
                maxY = yPoints[i];
        }
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        
        if ( ( width > 0 ) && ( height > 0 ) )
        {
            graphics.drawPolygon( xPoints, yPoints, nPoints );
            
            markDirty( minX, minY, width, height );
        }
    }
    
    @Override
    public final void drawPolyline( int[] xPoints, int[] yPoints, int nPoints )
    {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        for ( int i = 0; i < nPoints; i++ )
        {
            if ( xPoints[i] < minX )
                minX = xPoints[i];
            
            if ( xPoints[i] > maxX )
                maxX = xPoints[i];
        }
        
        for ( int i = 0; i < nPoints; i++ )
        {
            if ( yPoints[i] < minY )
                minY = yPoints[i];
            
            if ( yPoints[i] > maxY )
                maxY = yPoints[i];
        }
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        
        if ( ( width > 0 ) && ( height > 0 ) )
        {
            graphics.drawPolyline( xPoints, yPoints, nPoints );
            
            markDirty( minX, minY, width, height );
        }
    }
    
    @Override
    public final void drawRect( int x, int y, int width, int height )
    {
        graphics.drawRect( x, y, width, height );
        
        markDirty( x, y, width, height );
    }
    
    public final void drawRect( Rect2i rect )
    {
        drawRect( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() );
    }
    
    @Override
    public final void drawRoundRect( int x, int y, int width, int height, int arcWidth, int arcHeight )
    {
        graphics.drawRoundRect( x, y, width, height, arcWidth, arcHeight );
        
        markDirty( x, y, width, height );
    }
    
    public final void drawRoundRect( Rect2i rect, int arcWidth, int arcHeight )
    {
        drawRoundRect( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight(), arcWidth, arcHeight );
    }
    
    @Override
    public final void drawString( AttributedCharacterIterator iterator, float x, float y )
    {
        graphics.drawString( iterator, x, y );
        
        markDirty( (int)x, (int)y, texImg.getUsedWidth() - (int)x, texImg.getUsedHeight() - (int)y );
    }
    
    @Override
    public final void drawString( AttributedCharacterIterator iterator, int x, int y )
    {
        graphics.drawString( iterator, x, y );
        
        markDirty( x, y, texImg.getUsedWidth() - x, texImg.getUsedHeight() - y );
    }
    
    @Override
    public final void drawString( String s, float x, float y )
    {
        graphics.drawString( s, x, y );
        
        markDirty( (int)x, (int)y, texImg.getUsedWidth() - (int)x, texImg.getUsedHeight() - (int)y );
    }
    
    public final void drawString( String s, int x, int y, int baselineOffset, int boundsWidth, int boundsHeight )
    {
        graphics.drawString( s, x, y );
        
        markDirty( x, y - baselineOffset, boundsWidth, boundsHeight );
    }
    
    public final void drawString( String s, int x, int y, Rectangle2D bounds )
    {
        graphics.drawString( s, x, y );
        
        markDirty( x, y + (int)bounds.getY(), (int)bounds.getWidth(), (int)bounds.getHeight() );
    }
    
    @Override
    public final void drawString( String s, int x, int y )
    {
        drawString( s, x, y, graphics.getFontMetrics().getStringBounds( s, graphics ) );
    }
    
    @Override
    public final void fill( Shape shape )
    {
        graphics.fill( shape );
        
        java.awt.Rectangle rect = shape.getBounds();
        
        markDirty( rect.x, rect.y, rect.width, rect.height );
    }
    
    @Override
    public final void fillArc( int x, int y, int width, int height, int startAngle, int arcAngle )
    {
        graphics.fillArc( x, y, width, height, startAngle, arcAngle );
        
        markDirty( x, y, width, height );
    }
    
    public final void fillCircle( int x, int y, int radius )
    {
        fillArc( x - radius, y - radius, radius + radius, radius + radius, 0, 360 );
    }
    
    @Override
    public final void fillOval( int x, int y, int width, int height )
    {
        graphics.fillOval( x, y, width, height );
        
        markDirty( x, y, width, height );
    }
    
    @Override
    public final void fillPolygon( int[] xPoints, int[] yPoints, int nPoints )
    {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        for ( int i = 0; i < nPoints; i++ )
        {
            if ( xPoints[i] < minX )
                minX = xPoints[i];
            
            if ( xPoints[i] > maxX )
                maxX = xPoints[i];
        }
        
        for ( int i = 0; i < nPoints; i++ )
        {
            if ( yPoints[i] < minY )
                minY = yPoints[i];
            
            if ( yPoints[i] > maxY )
                maxY = yPoints[i];
        }
        
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        
        if ( ( width > 0 ) && ( height > 0 ) )
        {
            graphics.fillPolygon( xPoints, yPoints, nPoints );
            
            markDirty( minX, minY, width, height );
        }
    }
    
    @Override
    public final void fillRect( int x, int y, int width, int height )
    {
        graphics.fillRect( x, y, width, height );
        
        markDirty( x + 1, y + 1, width - 2, height - 2 );
    }
    
    public final void fillRect( Rect2i rect )
    {
        fillRect( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() );
    }
    
    @Override
    public final void fillRoundRect( int x, int y, int width, int height, int arcWidth, int arcHeight )
    {
        graphics.fillRoundRect( x, y, width, height, arcWidth, arcHeight );
        
        markDirty( x, y, width, height );
    }
    
    public final void fillRoundRect( Rect2i rect, int arcWidth, int arcHeight )
    {
        fillRoundRect( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight(), arcWidth, arcHeight );
    }
    
    @Override
    public Color getBackground()
    {
        return ( graphics.getBackground() );
    }
    
    @Override
    public final java.awt.Shape getClip()
    {
        return ( graphics.getClip() );
    }
    
    public final Rect2i getClipRect2i()
    {
        java.awt.Rectangle awtRect = graphics.getClipBounds();
        
        if ( awtRect == null )
            return ( new Rect2i( 0, 0, getImage().getWidth(), getImage().getHeight() ) );
        
        return ( new Rect2i( awtRect.x, awtRect.y, awtRect.width, awtRect.height ) );
    }
    
    @Override
    public final Rectangle getClipBounds()
    {
        return ( graphics.getClipBounds() );
    }
    
    public final Rect2i getClipBounds( Rect2i rect )
    {
        java.awt.Rectangle awtRect = new java.awt.Rectangle( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() );
        
        graphics.getClipBounds( awtRect );
        
        return ( rect );
    }
    
    @Override
    public final Color getColor()
    {
        return ( graphics.getColor() );
    }
    
    @Override
    public final java.awt.Font getFont()
    {
        return ( graphics.getFont() );
    }
    
    @Override
    public final java.awt.FontMetrics getFontMetrics()
    {
        return ( graphics.getFontMetrics() );
    }
    
    @Override
    public FontMetrics getFontMetrics( Font font )
    {
        return ( graphics.getFontMetrics( font ) );
    }
    
    @Override
    public final java.awt.font.FontRenderContext getFontRenderContext()
    {
        return ( graphics.getFontRenderContext() );
    }
    
    @Override
    public final java.awt.Paint getPaint()
    {
        return ( graphics.getPaint() );
    }
    
    @Override
    public final Object getRenderingHint( java.awt.RenderingHints.Key hintKey )
    {
        return ( graphics.getRenderingHint( hintKey ) );
    }
    
    @Override
    public final java.awt.RenderingHints getRenderingHints()
    {
        return ( graphics.getRenderingHints() );
    }
    
    @Override
    public final java.awt.Stroke getStroke()
    {
        return ( graphics.getStroke() );
    }
    
    @Override
    public final java.awt.geom.AffineTransform getTransform()
    {
        return ( graphics.getTransform() );
    }
    
    @Override
    public final boolean hitClip( int x, int y, int width, int height )
    {
        return ( graphics.hitClip( x, y, width, height ) );
    }
    
    @Override
    public boolean hit( Rectangle rect, Shape s, boolean onStroke )
    {
        return ( graphics.hit( rect, s, onStroke ) );
    }
    
    @Override
    public final void rotate( double theta )
    {
        graphics.rotate( theta );
    }
    
    @Override
    public final void rotate( double theta, double x, double y )
    {
        graphics.rotate( theta, x, y );
    }
    
    @Override
    public final void scale( double sx, double sy )
    {
        graphics.scale( sx, sy );
    }
    
    @Override
    public final void setBackground( Color color )
    {
        graphics.setBackground( color );
    }
    
    @Override
    public final void setClip( java.awt.Shape clip )
    {
        graphics.setClip( clip );
    }
    
    @Override
    public final void setClip( int x, int y, int width, int height )
    {
        int right = Math.min( x + width - 1, getImage().getWidth() - 1 );
        int bottom = Math.min( y + height - 1, getImage().getHeight() - 1 );
        graphics.setClip( Math.max( 0, x ), Math.max( 0, y ), right - x + 1, bottom - y + 1 );
        
        getImage().setClipRect( x, y, width, height );
    }
    
    public final void setClip( Rect2i rect )
    {
        if ( rect == null )
            setClip( 0, 0, getImage().getWidth(), getImage().getHeight() );
        else
            setClip( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight() );
    }
    
    public final <Rect2i_ extends Rect2i> Rect2i_ getClip( Rect2i_ rect )
    {
        return ( getImage().getClipRect( rect ) );
    }
    
    public final void pushClip( int x, int y, int width, int height, boolean intersectWithCurrent )
    {
        getClip( clipStack[clipStackSize - 1] );
        if ( clipStack[clipStackSize] == null )
            clipStack[clipStackSize] = new Rect2i();
        clipStack[clipStackSize].set( x, y, width, height );
        
        if ( intersectWithCurrent )
        {
            clipStack[clipStackSize].clamp( clipStack[clipStackSize - 1] );
            setClip( clipStack[clipStackSize] );
        }
        else
        {
            setClip( x, y, width, height );
        }
        
        clipStackSize++;
    }
    
    public final void pushClip( int x, int y, int width, int height )
    {
        pushClip( x, y, width, height, false );
    }
    
    public final void pushClip( Rect2i rect, boolean intersectWithCurrent )
    {
        pushClip( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight(), intersectWithCurrent );
    }
    
    public final void pushClip( Rect2i rect )
    {
        pushClip( rect.getLeft(), rect.getTop(), rect.getWidth(), rect.getHeight(), false );
    }
    
    public final void popClip()
    {
        clipStackSize--;
        setClip( clipStack[clipStackSize - 1] );
    }
    
    @Override
    public void setColor( Color color )
    {
        graphics.setColor( color );
    }
    
    @Override
    public final void setFont( java.awt.Font font )
    {
        graphics.setFont( font );
    }
    
    @Override
    public final void setPaint( java.awt.Paint paint )
    {
        graphics.setPaint( paint );
    }
    
    @Override
    public final void setPaintMode()
    {
        graphics.setPaintMode();
    }
    
    @Override
    public final void setRenderingHint( java.awt.RenderingHints.Key hintKey, Object hintValue )
    {
        graphics.setRenderingHint( hintKey, hintValue );
    }
    
    public final void setRenderingHints( java.awt.RenderingHints hints )
    {
        graphics.setRenderingHints( hints );
    }
    
    @Override
    public void setRenderingHints( Map< ?, ? > hints )
    {
        graphics.setRenderingHints( hints );
    }
    
    @Override
    public void addRenderingHints( Map< ?, ? > hints )
    {
        graphics.addRenderingHints( hints );
    }
    
    public final void setAntialiazingEnabled( boolean enabled )
    {
        Object value = enabled ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
        
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, value );
    }
    
    public final boolean isAntialiazingEnabled()
    {
        Object value = graphics.getRenderingHint( RenderingHints.KEY_ANTIALIASING );
        
        return ( value == RenderingHints.VALUE_ANTIALIAS_ON );
    }
    
    @Override
    public final void setStroke( java.awt.Stroke stroke )
    {
        graphics.setStroke( stroke );
    }
    
    @Override
    public final void setTransform( java.awt.geom.AffineTransform Tx )
    {
        graphics.setTransform( Tx );
    }
    
    @Override
    public final void setXORMode( Color color )
    {
        graphics.setXORMode( color );
    }
    
    @Override
    public final void shear( double shx, double shy )
    {
        graphics.shear( shx, shy );
    }
    
    @Override
    public final void transform( java.awt.geom.AffineTransform Tx )
    {
        graphics.transform( Tx );
    }
    
    @Override
    public final void translate( double tx, double ty )
    {
        graphics.translate( tx, ty );
    }
    
    @Override
    public final void translate( int tx, int ty )
    {
        graphics.translate( tx, ty );
    }
    
    private void updateAffineTransform()
    {
        baseAffineTransform.setToIdentity();
        
        /*
        //if ( texImg.getYUp() )
        {
            baseAffineTransform.concatenate( new AffineTransform( 1f, 0f, 0f, -1f, 0f, 0f ) );
            baseAffineTransform.concatenate( new AffineTransform( 1f, 0f, 0f, 1f, 0f, -texImg.getUsedHeight() ) );
        }
        */
        
        this.graphics.setTransform( baseAffineTransform );
    }
    
    /*
    public final void notifyImagesizeChanged( int imgWidth, int imgHeight, Graphics2D graphics )
    {
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        
        if ( ( graphics != null ) && ( graphics != this.graphics ) )
        {
            graphics.setBackground( this.graphics.getBackground() );
            graphics.setClip( this.graphics.getClip() );
            graphics.setColor( this.graphics.getColor() );
            graphics.setComposite( this.graphics.getComposite() );
            graphics.setFont( this.graphics.getFont() );
            graphics.setPaint( this.graphics.getPaint() );
            graphics.setRenderingHints( this.graphics.getRenderingHints() );
            graphics.setStroke( this.graphics.getStroke() );
            //graphics.setXORMode( this.graphics.getXORMode() );
            
            this.graphics = graphics;
        }
        
        updateAffineTransform();
    }
    */
    
    @Override
    public void setComposite( Composite comp )
    {
        graphics.setComposite( comp );
    }
    
    @Override
    public Composite getComposite()
    {
        return ( graphics.getComposite() );
    }
    
    @Override
    public Graphics create()
    {
        throw new Error( "create() is not supported for this kind of Graphics2D." );
    }
    
    @Override
    public void dispose()
    {
        graphics.dispose();
    }
    
    protected Texture2DCanvas( TextureImage2D ti, Graphics2D graphics )
    {
        if ( graphics == null )
            throw new IllegalArgumentException( "graphics must not be null." );
        
        this.texImg = ti;
        
        this.graphics = graphics;
        
        this.baseAffineTransform = new AffineTransform( 1f, 0f, 0f, 1f, 0f, 0f );
        
        updateAffineTransform();
        
        clipStack[0] = getClipRect2i();
        clipStackSize++;
    }
    
    protected Texture2DCanvas( TextureImage2D ti )
    {
        this( ti, ti.createGraphics2D() );
    }
}
