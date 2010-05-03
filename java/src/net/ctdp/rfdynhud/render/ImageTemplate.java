package net.ctdp.rfdynhud.render;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import sun.awt.image.ByteInterleavedRaster;

public class ImageTemplate
{
    long lastModified = -1L;
    long fileSize = -1L;
    
    private final BufferedImage bufferedImage;
    
    public final int getBaseWidth()
    {
        return ( bufferedImage.getWidth() );
    }
    
    public final int getBaseHeight()
    {
        return ( bufferedImage.getHeight() );
    }
    
    public final float getBaseAspect()
    {
        return ( getBaseWidth() / (float)getBaseHeight() );
    }
    
    private void copyPixels( TextureImage2D texture )
    {
        ByteInterleavedRaster raster = (ByteInterleavedRaster)bufferedImage.getData();
        int[] byteOffsets = raster.getDataOffsets();
        byte[] srcBytes = raster.getDataStorage();
        byte[] data;
        
        /*
        if ( ( ByteOrderManager.RED == byteOffsets[0] ) && ( ByteOrderManager.GREEN == byteOffsets[1] ) && ( ByteOrderManager.BLUE == byteOffsets[2] ) && ( ByteOrderManager.ALPHA == byteOffsets[3] ) )
        {
            texture = TextureImage2D.createOfflineTexture( bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getColorModel().hasAlpha(), srcBytes );
        }
        else
        */
        {
            int pixelStride = ( bufferedImage.getColorModel().hasAlpha() ? 4 : 3 );
            //data = new byte[ bufferedImage.getWidth() * bufferedImage.getHeight() * pixelStride ];
            data = texture.getData();
            
            int offset = 0;
            for ( int j = 0; j < bufferedImage.getHeight(); j++ )
            {
                for ( int i = 0; i < bufferedImage.getWidth(); i++ )
                {
                    data[offset + ByteOrderManager.RED] = srcBytes[offset + byteOffsets[0]];
                    data[offset + ByteOrderManager.GREEN] = srcBytes[offset + byteOffsets[1]];
                    data[offset + ByteOrderManager.BLUE] = srcBytes[offset + byteOffsets[2]];
                    if ( pixelStride == 4 )
                        data[offset + ByteOrderManager.ALPHA] = srcBytes[offset + byteOffsets[3]];
                    offset += pixelStride;
                }
            }
            
            //texture = TextureImage2D.createOfflineTexture( bi.getWidth(), bi.getHeight(), bi.getColorModel().hasAlpha(), data );
        }
    }
    
    public void drawScaled( int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, TextureImage2D texture, boolean clearBefore )
    {
        if ( ( bufferedImage.getData() instanceof ByteInterleavedRaster ) && ( bufferedImage.getColorModel().hasAlpha() == texture.hasAlphaChannel() ) && ( sx == 0 ) && ( sy == 0 ) && ( sw == getBaseWidth() ) && ( sh == getBaseHeight() ) && ( dx == 0 ) && ( dy == 0 ) && ( dw == getBaseWidth() ) && ( dh == getBaseHeight() ) && clearBefore )
        {
            copyPixels( texture );
        }
        else
        {
            if ( clearBefore )
                texture.clear( dx, dy, dw, dh, false, null );
            
            Texture2DCanvas texCanvas = texture.getTextureCanvas();
            
            Object oldInterpolation = texCanvas.getRenderingHint( RenderingHints.KEY_INTERPOLATION );
            texCanvas.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
            
            texCanvas.drawImage( bufferedImage, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh );
            
            if ( oldInterpolation == null )
                texCanvas.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
            else
                texCanvas.setRenderingHint( RenderingHints.KEY_INTERPOLATION, oldInterpolation );
        }
    }
    
    public void drawScaled( int x, int y, int width, int height, TextureImage2D texture, boolean clearBefore )
    {
        drawScaled( 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), x, y, width, height, texture, clearBefore );
    }
    
    public TextureImage2D getScaledTextureImage( int width, int height )
    {
        TextureImage2D texture = TextureImage2D.createDrawTexture( width, height, width, height, bufferedImage.getColorModel().hasAlpha(), null );
        
        drawScaled( 0, 0, width, height, texture, true );
        
        return ( texture );
    }
    
    public TextureImage2D getTextureImage()
    {
        return ( getScaledTextureImage( getBaseWidth(), getBaseHeight() ) );
    }
    
    public TransformableTexture getScaledTransformableTexture( int width, int height )
    {
        TransformableTexture texture = new TransformableTexture( width, height, 0, 0, 0, 0, 0f, 1f, 1f );
        
        drawScaled( 0, 0, width, height, texture.getTexture(), true );
        
        return ( texture );
    }
    
    public TransformableTexture getTransformableTexture()
    {
        return ( getScaledTransformableTexture( getBaseWidth(), getBaseHeight() ) );
    }
    
    public ImageTemplate( BufferedImage bufferedImage )
    {
        this.bufferedImage = bufferedImage;
    }
}
