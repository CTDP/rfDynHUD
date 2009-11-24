package net.ctdp.rfdynhud.render;

import java.io.File;
import java.util.HashMap;

import net.ctdp.rfdynhud.util.TextureLoader;

/**
 * The {@link BorderCache} is used to load borders only once.
 * 
 * @author Marvin Froehlich
 */
public class BorderCache
{
    private static final HashMap<String, TexturedBorder> CACHE = new HashMap<String, TexturedBorder>();
    
    /**
     * Gets or creates a TexturedBorder with the given side widths.
     * 
     * @param texture
     */
    public static TexturedBorder getTexturedBorder( String textureName )
    {
        if ( textureName == null )
            return ( null );
        
        TexturedBorder border = CACHE.get( textureName );
        
        if ( border != null )
            return ( border );
        
        TextureImage2D texture = TextureLoader.getImage( "borders" + File.separator + textureName, false ).getTextureImage();
        
        if ( texture == null )
            return ( null );
        
        int px0x = ( texture.getUsedWidth() % 2 ) == 0 ? texture.getUsedWidth() / 2 - 1 : (int)( texture.getUsedWidth() / 2 );
        int px0y = ( texture.getUsedHeight() % 2 ) == 0 ? texture.getUsedHeight() / 2 - 1 : (int)( texture.getUsedHeight() / 2 );
        //System.out.println( px0x + ", " + px0y );
        byte[] pixel = new byte[ 4 ];
        
        texture.getPixel( px0x + 0, px0y + 0, pixel );
        int leftWidth = pixel[ByteOrderManager.RED] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 0, pixel );
        int topHeight = pixel[ByteOrderManager.RED] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 0, pixel );
        int rightWidth = pixel[ByteOrderManager.RED] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 1, pixel );
        int bottomHeight = pixel[ByteOrderManager.RED] & 0xFF;
        
        texture.getPixel( px0x + 0, px0y + 0, pixel );
        int innerLeftWidth = pixel[ByteOrderManager.GREEN] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 0, pixel );
        int innerTopHeight = pixel[ByteOrderManager.GREEN] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 0, pixel );
        int innerRightWidth = pixel[ByteOrderManager.GREEN] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 1, pixel );
        int innerBottomHeight = pixel[ByteOrderManager.GREEN] & 0xFF;
        
        texture.getPixel( px0x + 0, px0y + 0, pixel );
        int opaqueLeftWidth = pixel[ByteOrderManager.BLUE] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 0, pixel );
        int opaqueTopHeight = pixel[ByteOrderManager.BLUE] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 0, pixel );
        int opaqueRightWidth = pixel[ByteOrderManager.BLUE] & 0xFF;
        texture.getPixel( px0x + 1, px0y + 1, pixel );
        int opaqueBottomHeight = pixel[ByteOrderManager.BLUE] & 0xFF;
        
        //System.out.println( leftWidth + ", " + topHeight + ", " + rightWidth + ", " + bottomHeight );
        //System.out.println( opaqueLeftWidth + ", " + opaqueTopHeight + ", " + opaqueRightWidth + ", " + opaqueBottomHeight );
        
        border = new TexturedBorder( texture, bottomHeight, rightWidth, topHeight, leftWidth );
        
        border.setOpaqueBottomHeight( opaqueBottomHeight );
        border.setOpaqueRightWidth( opaqueRightWidth );
        border.setOpaqueTopHeight( opaqueTopHeight );
        border.setOpaqueLeftWidth( opaqueLeftWidth );
        
        border.setInnerBottomHeight( innerBottomHeight );
        border.setInnerRightWidth( innerRightWidth );
        border.setInnerTopHeight( innerTopHeight );
        border.setInnerLeftWidth( innerLeftWidth );
        
        CACHE.put( textureName, border );
        
        return ( border );
    }
}
