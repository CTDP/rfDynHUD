package net.ctdp.rfdynhud.render;

import java.awt.geom.AffineTransform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.types.twodee.Rect2i;

/**
 * The {@link TransformableTexture} keeps one {@link TextureImage2D}
 * and transformation parameters.
 * 
 * @author Marvin Froehlich
 */
public class TransformableTexture
{
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
        
        private byte visible = 1;
        
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
    
    private final TextureImage2D texture;
    
    private static final byte TRANSFORM_FLAG_TRANSLATION = 2;
    private static final byte TRANSFORM_FLAG_ROTATION = 4;
    private static final byte TRANSFORM_FLAG_SCALE = 8;
    
    private boolean isDynamic = false;
    
    private final boolean isTransformed;
    private byte transformFlags = 0;
    
    private boolean visible = true;
    
    private final boolean pixelPerfectPositioning;
    private float transX, transY;
    private int rotCenterX, rotCenterY;
    private float rotation;
    private float scaleX, scaleY;
    private int clipRectX = 0, clipRectY = 0, clipRectWidth = 0, clipRectHeight = 0;
    
    private boolean dirty = true;
    
    private Rectangle[] usedRectangles = null;
    
    private final ByteBuffer dirtyRectsBuffer;
    
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
    
    protected void generateSubRectangles( LiveGameData gameData, EditorPresets editorPresets, WidgetsDrawingManager widgetsManager )
    {
        final int n = widgetsManager.getNumWidgets();
        Rectangle[] tmp = new Rectangle[ n ];
        int m = 0;
        for ( int i = 0; i < n; i++ )
        {
            Widget w = widgetsManager.getWidget( i );
            if ( w.hasMasterCanvas( editorPresets != null ) )
                tmp[m++] = new Rectangle( w.getPosition().getEffectiveX(), w.getPosition().getEffectiveY(), w.getMaxWidth( gameData, widgetsManager.getMainTexture() ), w.getMaxHeight( gameData, widgetsManager.getMainTexture() ) );
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
    
    public void setDynamic( boolean dynamic )
    {
        this.isDynamic = dynamic;
    }
    
    public final boolean isDynamic()
    {
        return ( isDynamic );
    }
    
    public final boolean isTransformed()
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
    
    public final int getWidth()
    {
        return ( texture.getUsedWidth() );
    }
    
    public final int getHeight()
    {
        return ( texture.getUsedHeight() );
    }
    
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
    
    public void setRotationCenter( int rotCenterX, int rotCenterY )
    {
        this.rotCenterX = rotCenterX;
        this.rotCenterY = rotCenterY;
        
        this.dirty = true;
    }
    
    public final int getRotCenterX()
    {
        return ( rotCenterX );
    }
    
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
        setRotation( rotDeg * (float)Math.PI / 180f );
    }
    
    public final float getRotation()
    {
        return ( rotation );
    }
    
    public final float getRotationInDegrees()
    {
        return( rotation * 180f / ( (float)Math.PI ) );
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
            buffer.put( OFFSET_TRANSFORMED + index * 1, isTransformed ? transformFlags : (byte)0 );
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
        
        return ( Math.min( rectangleIndex + usedRectangles.length, MAX_TOTAL_NUM_RECTANGLES ) );
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
    
    public void drawInEditor( Texture2DCanvas texCanvas, int offsetX, int offsetY )
    {
        if ( !isVisible() || ( isTransformed() && !isRectangleVisible( 0 ) ) )
            return;
        
        texCanvas.pushClip( -1, -1, Integer.MAX_VALUE, Integer.MAX_VALUE, false );
        
        try
        {
            AffineTransform at = new AffineTransform( texCanvas.getTransform() );
            
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
            
            texCanvas.setTransform( at );
            
            if ( clipRectWidth > 0 && clipRectHeight > 0 )
                texCanvas.drawImage( getTexture().getBufferedImage(), clipRectX, clipRectY, clipRectX + clipRectWidth, clipRectY + clipRectHeight, clipRectX, clipRectY, clipRectX + clipRectWidth, clipRectY + clipRectHeight );
            else
                texCanvas.drawImage( getTexture().getBufferedImage(), 0, 0 );
        }
        finally
        {
            texCanvas.resetTransform();
            texCanvas.popClip();
        }
    }
    
    private static TextureImage2D createTexture( int width, int height )
    {
        //int texInternalWidth = NumberUtil.roundUpPower2( width );
        //int texInternalHeight = NumberUtil.roundUpPower2( height );
        int texInternalWidth = width;
        int texInternalHeight = height;
        
        //ByteBuffer dataBuffer = ByteBuffer.allocateDirect( texInternalWidth * texInternalHeight * 4 ).order( java.nio.ByteOrder.nativeOrder() );
        ByteBuffer dataBuffer = null; // use byte-array for faster offline drawing
        
        return ( TextureImage2D.createDrawTexture( texInternalWidth, texInternalHeight, width, height, true, dataBuffer ) );
    }
    
    /**
     * Creates a (non) {@link TransformableTexture} for the main overlay. Do not use this constructor for sub-textures!
     * 
     * @param dummy
     * @param width
     * @param height
     */
    private TransformableTexture( String dummy, int width, int height )
    {
        this.isDynamic = true;
        this.texture = createTexture( width, height );
        this.isTransformed = false;
        this.transformFlags = 0;
        this.pixelPerfectPositioning = true;
        
        this.dirtyRectsBuffer = TextureDirtyRectsManager.createByteBuffer( 1024 );
    }
    
    /**
     * Never use this method from outside!
     * 
     * @param width
     * @param height
     * @return a main render texture.
     */
    static TransformableTexture createMainTexture( int width, int height )
    {
        return ( new TransformableTexture( "", width, height ) );
    }
    
    public TransformableTexture( int width, int height,
                                 boolean pixelPerfectPositioning, float transX, float transY,
                                 int rotCenterX, int rotCenterY, float rotation,
                                 float scaleX, float scaleY
                               )
    {
        this.texture = createTexture( width, height );
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
        
        this.usedRectangles = new Rectangle[] { new Rectangle( 0, 0, width, height ) };
        
        this.dirtyRectsBuffer = TextureDirtyRectsManager.createByteBuffer( 128 );
    }
    
    public TransformableTexture( int width, int height,
                                 int transX, int transY,
                                 int rotCenterX, int rotCenterY, float rotation,
                                 float scaleX, float scaleY
                               )
    {
        this( width, height, true, transX, transY, rotCenterX, rotCenterY, rotation, scaleX, scaleY );
    }
    
    public TransformableTexture( int width, int height, boolean pixelPerfectPositioning )
    {
        this( width, height, pixelPerfectPositioning, 0f, 0f, 0, 0, 0f, 1.0f, 1.0f );
    }
    
    public TransformableTexture( int width, int height )
    {
        this( width, height, true, 0f, 0f, 0, 0, 0f, 1.0f, 1.0f );
    }
}
