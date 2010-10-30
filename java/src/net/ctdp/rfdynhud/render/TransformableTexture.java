/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.AbstractAssembledWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.openmali.types.twodee.Rect2i;

/**
 * The {@link TransformableTexture} keeps one {@link TextureImage2D}
 * and transformation parameters.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TransformableTexture
{
    public static final boolean DEFAULT_PIXEL_PERFECT_POSITIONING = true;
    
    private static final int MAX_RECTANGLE_SIZE = 500;
    
    public static final float PI = (float)Math.PI;
    public static final float TWO_PI = (float)( Math.PI * 2.0 );
    public static final float PI_HALF = (float)( Math.PI / 2.0 );
    
    public static class Rectangle
    {
        private static final byte MADE_INVISIBLE = (byte)-1;
        private static final byte INVISIBLE = (byte)0;
        private static final byte VISIBLE = (byte)1;
        private static final byte MADE_VISIBLE = (byte)+2;
        
        private final short left;
        private final short top;
        private final short width;
        private final short height;
        
        private byte visible = VISIBLE;
        
        private boolean setVisible( boolean visible )
        {
            if ( visible )
            {
                if ( ( this.visible == VISIBLE ) || ( this.visible == MADE_VISIBLE ) )
                {
                    return ( false );
                }
                
                this.visible = MADE_VISIBLE;
                
                return ( true );
            }
            
            if ( ( this.visible == INVISIBLE ) || ( this.visible == MADE_INVISIBLE ) )
            {
                return ( false );
            }
            
            this.visible = MADE_INVISIBLE;
            
            return ( true );
        }
        
        private boolean isVisible()
        {
            return ( ( this.visible == VISIBLE ) || ( this.visible == MADE_VISIBLE ) );
        }
        
        public Rectangle( int left, int top, int width, int height )
        {
            this.left = (short)left;
            this.top = (short)top;
            this.width = (short)width;
            this.height = (short)height;
        }
    }
    
    private static final int SOFT_MAX_NUM_WIDGETS = 48;
    private static final int MAX_TOTAL_NUM_RECTANGLES = 255;
    public static final int MAX_NUM_TEXTURES = MAX_TOTAL_NUM_RECTANGLES - SOFT_MAX_NUM_WIDGETS + 1;
    
    public static final int STRUCT_SIZE = 40 + SOFT_MAX_NUM_WIDGETS * 9;
    
    private static final int OFFSET_VISIBLE = 1;
    private static final int OFFSET_SIZE = OFFSET_VISIBLE + MAX_NUM_TEXTURES * 1;
    private static final int OFFSET_TRANSFORMED = OFFSET_SIZE + MAX_NUM_TEXTURES * 4;
    private static final int OFFSET_TRANSLATION = OFFSET_TRANSFORMED + MAX_NUM_TEXTURES * 1;
    private static final int OFFSET_ROT_CENTER = OFFSET_TRANSLATION + MAX_NUM_TEXTURES * 8;
    private static final int OFFSET_ROTATION = OFFSET_ROT_CENTER + MAX_NUM_TEXTURES * 4;
    private static final int OFFSET_SCALE = OFFSET_ROTATION + MAX_NUM_TEXTURES * 4;
    private static final int OFFSET_CLIP_RECT = OFFSET_SCALE + MAX_NUM_TEXTURES * 8;
    private static final int OFFSET_NUM_RECTANLES = OFFSET_CLIP_RECT + MAX_NUM_TEXTURES * 8;
    private static final int OFFSET_RECT_VISIBLE_FLAGS = OFFSET_NUM_RECTANLES + MAX_NUM_TEXTURES * 1;
    private static final int OFFSET_RECTANGLES = OFFSET_RECT_VISIBLE_FLAGS + MAX_NUM_TEXTURES * SOFT_MAX_NUM_WIDGETS * 1;
    
    private Widget ownerWidget = null;
    
    private final TextureImage2D texture;
    
    private static final byte TRANSFORM_FLAG_TRANSLATION = 2;
    private static final byte TRANSFORM_FLAG_ROTATION = 4;
    private static final byte TRANSFORM_FLAG_SCALE = 8;
    
    private boolean isDynamic = false;
    
    private int localZIndex = 0;
    
    private final boolean isTransformed;
    private byte transformFlags = 0;
    
    private boolean visible = true;
    
    private final boolean pixelPerfectPositioning;
    private float transX = 0.0f, transY = 0.0f;
    private int rotCenterX = 0, rotCenterY = 0;
    private float rotation = 0.0f;
    private float scaleX = 1.0f, scaleY = 1.0f;
    private int clipRectX = 0, clipRectY = 0, clipRectWidth = 0, clipRectHeight = 0;
    
    private boolean dirty = true;
    
    private Rectangle[] usedRectangles = null;
    
    private final ByteBuffer dirtyRectsBuffer;
    
    public final String getName()
    {
        return ( texture.getName() );
    }
    
    void setDirty()
    {
        this.dirty = true;
    }
    
    void setOwnerWidget( Widget ownerWidget )
    {
        this.ownerWidget = ownerWidget;
    }
    
    public final Widget getOwnerWidget()
    {
        return ( ownerWidget );
    }
    
    /**
     * Gets the x-offset relative to the master Widget.
     * 
     * @return the x-offset relative to the master Widget.
     */
    public final int getOffsetXToRootMasterWidget()
    {
        if ( ( ownerWidget == null ) || ( ownerWidget.getMasterWidget() == null ) )
            return ( 0 );
        
        return ( ownerWidget.getBorder().getInnerLeftWidth() + ownerWidget.getPosition().getEffectiveX() + ownerWidget.getMasterWidget().getOffsetXToRootMasterWidget() );
    }
    
    /**
     * Gets the y-offset relative to the master Widget.
     * 
     * @return the y-offset relative to the master Widget.
     */
    public final int getOffsetYToRootMasterWidget()
    {
        if ( ( ownerWidget == null ) || ( ownerWidget.getMasterWidget() == null ) )
            return ( 0 );
        
        return ( ownerWidget.getBorder().getInnerTopHeight() + ownerWidget.getPosition().getEffectiveY() + ownerWidget.getMasterWidget().getOffsetYToRootMasterWidget() );
    }
    
    public static ByteBuffer createByteBuffer()
    {
        return ( ByteBuffer.allocateDirect( 1 + STRUCT_SIZE * MAX_NUM_TEXTURES ).order( ByteOrder.nativeOrder() ) );
    }
    
    public final TextureImage2D getTexture()
    {
        return ( texture );
    }
    
    public final Texture2DCanvas getTextureCanvas()
    {
        return ( texture.getTextureCanvas() );
    }
    
    protected void generateRectanglesForOneBigTexture( LiveGameData gameData, boolean isEditorMode, WidgetsConfiguration widgetsConfig )
    {
        final int n = widgetsConfig.getNumWidgets();
        Rectangle[] tmp = new Rectangle[ n ];
        int m = 0;
        for ( int i = 0; i < n; i++ )
        {
            Widget w = widgetsConfig.getWidget( i );
            if ( w.hasMasterCanvas( isEditorMode ) )
                tmp[m++] = new Rectangle( w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY(), w.getMaxWidth( gameData, isEditorMode ), w.getMaxHeight( gameData, isEditorMode ) );
        }
        
        this.usedRectangles = new Rectangle[ m ];
        System.arraycopy( tmp, 0, usedRectangles, 0, m );
        
        this.dirty = true;
    }
    
    protected final int getNumUsedRectangles()
    {
        return ( usedRectangles.length );
    }
    
    protected void setRectangleVisible( int index, boolean visible )
    {
        if ( usedRectangles[index].setVisible( visible ) )
            this.dirty = true;
    }
    
    protected final boolean isRectangleVisible( int index )
    {
        return ( usedRectangles[index].isVisible() );
    }
    
    /**
     * This flag must be set, if you intend to draw on this texture.
     * 
     * @param dynamic dynamic?
     */
    public void setDynamic( boolean dynamic )
    {
        this.isDynamic = dynamic;
    }
    
    /**
     * This flag must be set, if you intend to draw on this texture.
     * 
     * @return dynamic or not
     */
    public final boolean isDynamic()
    {
        return ( isDynamic );
    }
    
    /**
     * Sets the {@link Widget}-local z-index. The only affects subtextures of a single {@link Widget} or {@link AbstractAssembledWidget}.
     * Higher values make the sub texture be drawn later then those with smaller values.
     * 
     * @param zIndex the new local z-index
     */
    public void setLocalZIndex( int zIndex )
    {
        this.localZIndex = zIndex;
    }
    
    /**
     * Gets the {@link Widget}-local z-index. The only affects subtextures of a single {@link Widget} or {@link AbstractAssembledWidget}.
     * Higher values make the sub texture be drawn later then those with smaller values.
     * 
     * @return the local z-index.
     */
    public final int getLocalZIndex()
    {
        return ( localZIndex );
    }
    
    /**
     * Gets whether this texture is potentially translated, rotated or scaled.
     * 
     * @return whether this texture is potentially translated, rotated or scaled.
     */
    private final boolean isTransformed()
    {
        return ( isTransformed );
    }
    
    public void setVisible( boolean visible )
    {
        if ( this.visible != visible )
            this.dirty = true;
        
        this.visible = visible;
    }
    
    public final boolean isVisible()
    {
        return ( visible );
    }
    
    public final boolean isVisibleInEditor()
    {
        return ( isVisible() && ( !isTransformed() || isRectangleVisible( 0 ) ) );
    }
    
    public final int getWidth()
    {
        return ( texture.getWidth() );
    }
    
    public final int getHeight()
    {
        return ( texture.getHeight() );
    }
    
    /**
     * Set the sub texture's translation. Sub texture's are generally
     * (and additionally to this) translated to the upper left of the host Widget
     * plus its border.
     * 
     * @param transX
     * @param transY
     */
    public void setTranslation( float transX, float transY )
    {
        this.transX = pixelPerfectPositioning ? Math.round( transX ) : transX;
        this.transY = pixelPerfectPositioning ? Math.round( transY ) : transY;
        
        if ( ( this.transX != 0.0f ) || ( this.transY != 0.0f ) )
            this.transformFlags = (byte)( ( transformFlags | TRANSFORM_FLAG_TRANSLATION ) & 0xFF );
        else
            this.transformFlags = (byte)( ( transformFlags & ~TRANSFORM_FLAG_TRANSLATION ) & 0xFF );
        
        this.dirty = true;
    }
    
    public final float getTransX()
    {
        return ( transX );
    }
    
    public final float getTransY()
    {
        return ( transY );
    }
    
    /**
     * Sets the center location for rotation. This is relative to the sub texture's upper left.
     * 
     * @param rotCenterX
     * @param rotCenterY
     */
    public void setRotationCenter( int rotCenterX, int rotCenterY )
    {
        this.rotCenterX = rotCenterX;
        this.rotCenterY = rotCenterY;
        
        this.dirty = true;
    }
    
    /**
     * Gets the center location for rotation. This is relative to the sub texture's upper left.
     * 
     * @return the center location for rotation.
     */
    public final int getRotCenterX()
    {
        return ( rotCenterX );
    }
    
    /**
     * Gets the center location for rotation. This is relative to the sub texture's upper left.
     * 
     * @return the center location for rotation.
     */
    public final int getRotCenterY()
    {
        return ( rotCenterY );
    }
    
    public void setRotation( float rotation )
    {
        this.rotation = rotation;
        
        if ( rotation != 0.0f )
            this.transformFlags = (byte)( ( transformFlags | TRANSFORM_FLAG_ROTATION ) & 0xFF );
        else
            this.transformFlags = (byte)( ( transformFlags & ~TRANSFORM_FLAG_ROTATION ) & 0xFF );
        
        this.dirty = true;
    }
    
    public final void setRotationInDegrees( float rotDeg )
    {
        setRotation( rotDeg * PI / 180f );
    }
    
    public final float getRotation()
    {
        return ( rotation );
    }
    
    public final float getRotationInDegrees()
    {
        return( rotation * 180f / PI );
    }
    
    public void setScale( float scaleX, float scaleY )
    {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        
        if ( ( scaleX != 1.0f ) || ( scaleY != 1.0f ) )
            this.transformFlags = (byte)( ( transformFlags | TRANSFORM_FLAG_SCALE ) & 0xFF );
        else
            this.transformFlags = (byte)( ( transformFlags & ~TRANSFORM_FLAG_SCALE ) & 0xFF );
        
        this.dirty = true;
    }
    
    public final float getScaleX()
    {
        return ( scaleX );
    }
    
    public final float getScaleY()
    {
        return ( scaleY );
    }
    
    public void setClipRect( int x, int y, int width, int height, boolean toggleVisibilityBySize )
    {
        this.clipRectX = x;
        this.clipRectY = y;
        this.clipRectWidth = width;
        this.clipRectHeight = height;
        
        if ( toggleVisibilityBySize )
        {
            setVisible( width > 0 && height > 0 );
        }
        
        this.dirty = true;
    }
    
    public final Rect2i getClipRect( Rect2i r )
    {
        r.set( clipRectX, clipRectY, clipRectWidth, clipRectHeight );
        
        return ( r );
    }
    
    public final ByteBuffer getDirtyRectsBuffer()
    {
        return ( dirtyRectsBuffer );
    }
    
    public final byte[] getTextureData()
    {
        return ( texture.getData() );
    }
    
    protected int fillBuffer( boolean widgetVisibility, int offsetX, int offsetY, int index, int rectangleIndex, ByteBuffer buffer )
    {
        if ( ( index >= MAX_NUM_TEXTURES ) || ( rectangleIndex >= MAX_TOTAL_NUM_RECTANGLES ) )
            return ( rectangleIndex );
        
        buffer.put( OFFSET_VISIBLE + index * 1, ( widgetVisibility && visible ) ? (byte)1 : (byte)0 );
        
        if ( dirty )
        {
            buffer.putShort( OFFSET_SIZE + index * 4 + 0, (short)texture.getWidth() );
            buffer.putShort( OFFSET_SIZE + index * 4 + 2, (short)texture.getHeight() );
            buffer.put( OFFSET_TRANSFORMED + index * 1, isTransformed() ? transformFlags : (byte)0 );
            buffer.putFloat( OFFSET_TRANSLATION + index * 8 + 0, offsetX + transX );
            buffer.putFloat( OFFSET_TRANSLATION + index * 8 + 4, offsetY + transY );
            buffer.putShort( OFFSET_ROT_CENTER + index * 4 + 0, (short)rotCenterX );
            buffer.putShort( OFFSET_ROT_CENTER + index * 4 + 2, (short)rotCenterY );
            buffer.putFloat( OFFSET_ROTATION + index * 4, rotation );
            buffer.putFloat( OFFSET_SCALE + index * 8 + 0, scaleX );
            buffer.putFloat( OFFSET_SCALE + index * 8 + 4, scaleY );
            buffer.putShort( OFFSET_CLIP_RECT + index * 8 + 0, (short)clipRectX );
            buffer.putShort( OFFSET_CLIP_RECT + index * 8 + 2, (short)clipRectY );
            buffer.putShort( OFFSET_CLIP_RECT + index * 8 + 4, (short)clipRectWidth );
            buffer.putShort( OFFSET_CLIP_RECT + index * 8 + 6, (short)clipRectHeight );
            
            if ( ( usedRectangles == null ) || ( usedRectangles.length == 0 ) )
            {
                buffer.put( OFFSET_NUM_RECTANLES + index * 1, (byte)0 );
            }
            else
            {
                int n = Math.min( usedRectangles.length, SOFT_MAX_NUM_WIDGETS );
                buffer.put( OFFSET_NUM_RECTANLES + index * 1, (byte)n );
                buffer.position( OFFSET_RECT_VISIBLE_FLAGS + rectangleIndex );
                for ( int i = 0; i < n; i++ )
                    buffer.put( usedRectangles[i].visible );
                buffer.position( OFFSET_RECTANGLES + rectangleIndex * 8 );
                for ( int i = 0; i < n; i++ )
                {
                    buffer.putShort( usedRectangles[i].left );
                    buffer.putShort( usedRectangles[i].top );
                    buffer.putShort( usedRectangles[i].width );
                    buffer.putShort( usedRectangles[i].height );
                }
            }
            
            this.dirty = false;
        }
        
        return ( Math.min( rectangleIndex + ( usedRectangles == null ? 0 : usedRectangles.length ), MAX_TOTAL_NUM_RECTANGLES ) );
    }
    
    /*
     * Updates the visible flags of all rectangles covered by a widget on the primary texture.
     * 
     * @param widgetsManager
     * @param buffer
     */
    /*
    protected void updateRectangleVisibleFlags( WidgetsDrawingManager widgetsManager, ByteBuffer buffer )
    {
        buffer.position( OFFSET_RECT_VISIBLE_FLAGS );
        
        final int n = widgetsManager.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget w = widgetsManager.getWidget( i );
            usedRectangles[i].setVisible( w.isVisible() );
            buffer.put( usedRectangles[i].visible );
        }
    }
    */
    
    /*
     * Updates the visible flag of the rectangle of a secondary texture.
     * 
     * @param rectangleIndex
     * @param visible
     * @param buffer
     */
    /*
    protected void updateRectangleVisibleFlag( int rectangleIndex, boolean visible, ByteBuffer buffer )
    {
        buffer.position( OFFSET_RECT_VISIBLE_FLAGS + rectangleIndex );
        buffer.put( visible ? (byte)1 : (byte)0 );
    }
    */
    
    public AffineTransform getTransformForEditor( int offsetX, int offsetY )
    {
        AffineTransform at = new AffineTransform();
        
        AffineTransform atTrans = AffineTransform.getTranslateInstance( offsetX + getTransX(), offsetY + getTransY() );
        AffineTransform atRotCenter1 = AffineTransform.getTranslateInstance( +getRotCenterX(), +getRotCenterY() );
        AffineTransform atRot = AffineTransform.getRotateInstance( getRotation() );
        AffineTransform atRotCenter2 = AffineTransform.getTranslateInstance( -getRotCenterX(), -getRotCenterY() );
        AffineTransform atScale = AffineTransform.getScaleInstance( getScaleX(), getScaleY() );
        
        at.concatenate( atTrans );
        at.concatenate( atRotCenter1 );
        at.concatenate( atRot );
        at.concatenate( atRotCenter2 );
        at.concatenate( atScale );
        
        return ( at );
    }
    
    private final java.awt.geom.Point2D.Float pUL = new java.awt.geom.Point2D.Float();
    private final java.awt.geom.Point2D.Float pUR = new java.awt.geom.Point2D.Float();
    private final java.awt.geom.Point2D.Float pLL = new java.awt.geom.Point2D.Float();
    private final java.awt.geom.Point2D.Float pLR = new java.awt.geom.Point2D.Float();
    
    public Rect2i getTransformedRectForEditor( AffineTransform at )
    {
        if ( !isVisibleInEditor() )
            return ( null );
        
        if ( ( clipRectWidth > 0 ) && ( clipRectHeight > 0 ) )
        {
            pUL.setLocation( clipRectX, clipRectY );
            pUR.setLocation( clipRectX + clipRectWidth - 1, clipRectY );
            pLL.setLocation( clipRectX, clipRectY + clipRectHeight - 1 );
            pLR.setLocation( clipRectX + clipRectWidth - 1, clipRectY + clipRectHeight - 1 );
        }
        else
        {
            pUL.setLocation( 0, 0 );
            pUR.setLocation( getWidth() - 1, 0 );
            pLL.setLocation( 0, getHeight() - 1 );
            pLR.setLocation( getWidth() - 1, getHeight() - 1 );
        }
        
        at.transform( pUL, pUL );
        at.transform( pUR, pUR );
        at.transform( pLL, pLL );
        at.transform( pLR, pLR );
        
        int x = Math.round( Math.min( Math.min( Math.min( pUL.x, pUR.x ), pLL.x ), pLR.x ) );
        int y = Math.round( Math.min( Math.min( Math.min( pUL.y, pUR.y ), pLL.y ), pLR.y ) );
        int x1 = Math.round( Math.max( Math.max( Math.max( pUL.x, pUR.x ), pLL.x ), pLR.x ) );
        int y1 = Math.round( Math.max( Math.max( Math.max( pUL.y, pUR.y ), pLL.y ), pLR.y ) );
        int w = x1 - x + 1;
        int h = y1 - y + 1;
        
        return ( new Rect2i( x, y, w, h ) );
    }
    
    public void drawInEditor( Graphics2D texCanvas, AffineTransform at, Rect2i transformedRect )
    {
        if ( !isVisibleInEditor() )
            return;
        
        AffineTransform at0 = new AffineTransform( texCanvas.getTransform() );
        AffineTransform tmp = new AffineTransform( at0 );
        tmp.concatenate( at );
        at = tmp;
        
        Rect2i r = transformedRect;
        
        //texCanvas.pushClip( r.getLeft(), r.getTop(), r.getWidth(), r.getHeight(), false );
        Shape oldClip = texCanvas.getClip();
        texCanvas.setClip( r.getLeft(), r.getTop(), r.getWidth(), r.getHeight() );
        
        texCanvas.setTransform( at );
        
        Object oldAA = texCanvas.getRenderingHint( RenderingHints.KEY_ANTIALIASING );
        texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        Object oldInter = texCanvas.getRenderingHint( RenderingHints.KEY_INTERPOLATION );
        texCanvas.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
        
        try
        {
            if ( ( clipRectWidth > 0 ) && ( clipRectHeight > 0 ) )
                texCanvas.drawImage( getTexture().getBufferedImage(), clipRectX, clipRectY, clipRectX + clipRectWidth, clipRectY + clipRectHeight, clipRectX, clipRectY, clipRectX + clipRectWidth, clipRectY + clipRectHeight, null );
            else
                texCanvas.drawImage( getTexture().getBufferedImage(), 0, 0, null );
        }
        finally
        {
            texCanvas.setRenderingHint( RenderingHints.KEY_INTERPOLATION, ( oldInter == null ) ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : oldInter );
            texCanvas.setRenderingHint( RenderingHints.KEY_ANTIALIASING, oldAA );
            
            //texCanvas.resetTransform();
            texCanvas.setTransform( at0 );
            //texCanvas.popClip();
            texCanvas.setClip( oldClip );
        }
        
        /*
        texCanvas.setClip( (org.openmali.types.twodee.Rect2i)null );
        texCanvas.setColor( java.awt.Color.GREEN );
        texCanvas.drawRect( r.getLeft(), r.getTop(), r.getWidth(), r.getHeight() );
        */
    }
    
    private static TextureImage2D createTexture( int width, int height, boolean usePowerOfTwoSizes )
    {
        int width2 = usePowerOfTwoSizes ? NumberUtil.roundUpPower2( width ) : width;
        int height2 = usePowerOfTwoSizes ? NumberUtil.roundUpPower2( height ) : height;
        
        return ( TextureImage2D.createOnlineTexture( width2, height2, width, height, true ) );
    }
    
    private static Rectangle[] generateRectangles( int width, int height )
    {
        if ( ( width <= MAX_RECTANGLE_SIZE ) && ( height <= MAX_RECTANGLE_SIZE ) )
            return ( new Rectangle[] { new Rectangle( 0, 0, width, height ) } );
        
        Rectangle[] rects = new Rectangle[ (int)Math.ceil( (double)width / (double)MAX_RECTANGLE_SIZE ) * (int)Math.ceil( (double)height / (double)MAX_RECTANGLE_SIZE ) ];
        
        int offX = 0;
        int offY = 0;
        int w = width;
        int h = height;
        int i = 0;
        
        while ( h > 0 )
        {
            int h2 = Math.min( h, MAX_RECTANGLE_SIZE );
            
            while ( w > 0 )
            {
                int w2 = Math.min( w, MAX_RECTANGLE_SIZE );
                
                rects[i++] = new Rectangle( offX, offY, w2, h2 );
                
                offX += w2;
                w -= w2;
            }
            
            offX = 0;
            offY += h2;
            w = width;
            h -= h2;
        }
        
        return ( rects );
    }
    
    /**
     * @param dummy
     * @param width
     * @param height
     * @param transformable
     */
    private TransformableTexture( String dummy, int width, int height, boolean transformable )
    {
        this.isDynamic = true;
        this.texture = createTexture( width, height, false );
        this.isTransformed = transformable;
        this.transformFlags = (byte)( transformable ? 1 : 0 );
        this.pixelPerfectPositioning = true;
        
        this.dirtyRectsBuffer = TextureDirtyRectsManager.createByteBuffer( transformable ? 128 : 1024 );
        
        if ( transformable )
            this.usedRectangles = generateRectangles( width, height );
    }
    
    /**
     * Never use this method from outside!
     * 
     * @param width
     * @param height
     * @param transformable
     * 
     * @return a main render texture.
     */
    static TransformableTexture createMainTexture( int width, int height, boolean transformable )
    {
        return ( new TransformableTexture( "", width, height, transformable ) );
    }
    
    public TransformableTexture( int width, int height,
                                 boolean pixelPerfectPositioning, float transX, float transY,
                                 int rotCenterX, int rotCenterY, float rotation,
                                 float scaleX, float scaleY,
                                 boolean usePowerOfTwoSizes
                               )
    {
        this.texture = createTexture( width, height, usePowerOfTwoSizes );
        this.isTransformed = true;
        this.transformFlags = 1;
        this.pixelPerfectPositioning = pixelPerfectPositioning;
        this.transX = pixelPerfectPositioning ? Math.round( transX ) : transX;
        this.transY = pixelPerfectPositioning ? Math.round( transY ) : transY;
        if ( ( this.transX != 0.0f ) || ( this.transY != 0.0f ) )
            this.transformFlags = (byte)( ( transformFlags | TRANSFORM_FLAG_TRANSLATION ) & 0xFF );
        this.rotCenterX = rotCenterX;
        this.rotCenterY = rotCenterY;
        this.rotation = rotation;
        if ( rotation != 0.0f )
            this.transformFlags = (byte)( ( transformFlags | TRANSFORM_FLAG_ROTATION ) & 0xFF );
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        if ( ( scaleX != 1.0f ) || ( scaleY != 1.0f ) )
            this.transformFlags = (byte)( ( transformFlags | TRANSFORM_FLAG_SCALE ) & 0xFF );
        
        this.usedRectangles = generateRectangles( width, height );
        
        this.dirtyRectsBuffer = TextureDirtyRectsManager.createByteBuffer( 128 );
    }
    
    public TransformableTexture( int width, int height,
                                 int transX, int transY,
                                 int rotCenterX, int rotCenterY, float rotation,
                                 float scaleX, float scaleY,
                                 boolean usePowerOfTwoSizes
                               )
    {
        this( width, height, DEFAULT_PIXEL_PERFECT_POSITIONING, transX, transY, rotCenterX, rotCenterY, rotation, scaleX, scaleY, usePowerOfTwoSizes );
    }
    
    public TransformableTexture( int width, int height, boolean pixelPerfectPositioning, boolean usePowerOfTwoSizes )
    {
        this( width, height, pixelPerfectPositioning, 0f, 0f, 0, 0, 0f, 1.0f, 1.0f, usePowerOfTwoSizes );
    }
    
    public TransformableTexture( int width, int height )
    {
        this( width, height, DEFAULT_PIXEL_PERFECT_POSITIONING, 0f, 0f, 0, 0, 0f, 1.0f, 1.0f, false );
    }
    
    /**
     * Gets a {@link TransformableTexture} with this image drawn onto it.
     * If the possibleResult is non null and has the correct size, it is returned.
     * 
     * @param width the desired width
     * @param height the desired height
     * @param pixelPerfectPositioning prepare for pixel perfect positioning
     * @param possibleResult this instance is possibly retured, if it matches the parameters
     * @param tryToResize if true, the passed in texture is resized to the given size, if the max size is sufficient.
     *                    This is useful in editor mode avoid constant recreations.
     * 
     * @return a {@link TransformableTexture} with this image drawn onto it.
     */
    public static TransformableTexture getOrCreate( int width, int height, boolean pixelPerfectPositioning, TransformableTexture possibleResult, boolean tryToResize )
    {
        final boolean usePowerOfTwoSizes = tryToResize;
        int width2 = usePowerOfTwoSizes ? NumberUtil.roundUpPower2( width ) : width;
        int height2 = usePowerOfTwoSizes ? NumberUtil.roundUpPower2( height ) : height;
        
        if ( possibleResult != null )
        {
            if ( usePowerOfTwoSizes )
            {
                if ( ( width2 == possibleResult.getTexture().getMaxWidth() ) && ( height2 == possibleResult.getTexture().getMaxHeight() ) )
                {
                    if ( ( width != possibleResult.getTexture().getWidth() ) || ( height != possibleResult.getTexture().getHeight() ) )
                    {
                        possibleResult.getTexture().clear( false, null );
                        possibleResult.getTexture().resize( width, height );
                        possibleResult.getTexture().clearUpdateList();
                    }
                    
                    return ( possibleResult );
                }
            }
            else if ( ( width == possibleResult.getTexture().getWidth() ) || ( height == possibleResult.getTexture().getHeight() ) )
            {
                //possibleResult.getTexture().clearUpdateList();
                
                return ( possibleResult );
            }
        }
        
        return ( new TransformableTexture( width, height, pixelPerfectPositioning, usePowerOfTwoSizes ) );
    }
}
