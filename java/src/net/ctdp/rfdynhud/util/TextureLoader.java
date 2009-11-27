package net.ctdp.rfdynhud.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;

/**
 * ImageIO image loading is pretty slow. This is a simple but fast texture loading implementation.
 * 
 * @author Marvin Froehlich
 */
public class TextureLoader
{
    public static final File IMAGES_FOLDER = new File( RFactorTools.CONFIG_PATH + File.separator + "data" + File.separator + "images" );
    
    private static final HashMap<String, ImageTemplate> cache = new HashMap<String, ImageTemplate>();
    
    /*
    private static final HashMap<String, TextureImage2D> cache = new HashMap<String, TextureImage2D>();
    private static final HashMap<String, BufferedImage> biCache = new HashMap<String, BufferedImage>();
    
    public static TextureImage2D getTexture( String name, boolean useCache )
    {
        if ( File.separatorChar != '/' )
            name = name.replace( '/', File.separatorChar );
        if ( File.separatorChar != '\\' )
            name = name.replace( '\\', File.separatorChar );
        
        TextureImage2D texture = useCache ? cache.get( name ) : null;
        
        //System.out.println( ( ( texture != null ) ? "found in cache" : "not found in cache" ) );
        
        if ( texture != null )
            return ( texture );
        
        BufferedImage bi = null;
        try
        {
            bi = ImageIO.read( new File( IMAGES_FOLDER, name ) );
        }
        catch ( IOException e )
        {
            //Logger.log( e );
            return ( null );
        }
        
        ByteInterleavedRaster raster = (ByteInterleavedRaster)bi.getData();
        int[] byteOffsets = raster.getDataOffsets();
        byte[] srcBytes = raster.getDataStorage();
        byte[] data;
        
        if ( ( ByteOrderManager.RED == byteOffsets[0] ) && ( ByteOrderManager.GREEN == byteOffsets[1] ) && ( ByteOrderManager.BLUE == byteOffsets[2] ) && ( ByteOrderManager.ALPHA == byteOffsets[3] ) )
        {
            texture = TextureImage2D.createOfflineTexture( bi.getWidth(), bi.getHeight(), bi.getColorModel().hasAlpha(), srcBytes );
        }
        else
        {
            int pixelStride = ( bi.getColorModel().hasAlpha() ? 4 : 3 );
            data = new byte[ bi.getWidth() * bi.getHeight() * pixelStride ];
            
            int offset = 0;
            for ( int j = 0; j < bi.getHeight(); j++ )
            {
                for ( int i = 0; i < bi.getWidth(); i++ )
                {
                    data[offset + ByteOrderManager.RED] = srcBytes[offset + byteOffsets[0]];
                    data[offset + ByteOrderManager.GREEN] = srcBytes[offset + byteOffsets[1]];
                    data[offset + ByteOrderManager.BLUE] = srcBytes[offset + byteOffsets[2]];
                    if ( pixelStride == 4 )
                        data[offset + ByteOrderManager.ALPHA] = srcBytes[offset + byteOffsets[3]];
                    offset += pixelStride;
                }
            }
            
            texture = TextureImage2D.createOfflineTexture( bi.getWidth(), bi.getHeight(), bi.getColorModel().hasAlpha(), data );
        }
        
        if ( useCache )
            cache.put( name, texture );
        
        return ( texture );
    }
    
    public static TextureImage2D getTexture( String name )
    {
        return ( getTexture( name, true ) );
    }
    
    public static BufferedImage getImage( String name, boolean useCache )
    {
        if ( File.separatorChar != '/' )
            name = name.replace( '/', File.separatorChar );
        if ( File.separatorChar != '\\' )
            name = name.replace( '\\', File.separatorChar );
        
        BufferedImage image = useCache ? biCache.get( name ) : null;
        
        //System.out.println( ( ( image != null ) ? "found in cache" : "not found in cache" ) );
        
        if ( image != null )
            return ( image );
        
        File f = new File( IMAGES_FOLDER, name );
        
        if ( !f.exists() )
        {
            Logger.log( "[ERROR] Unable to read input file \"" + f.getAbsolutePath() + "\"." );
            return ( null );
        }
        
        try
        {
            image = ImageIO.read( f );
        }
        catch ( IOException e )
        {
            Logger.log( "[ERROR] Unable to read input file \"" + f.getAbsolutePath() + "\"." );
            return ( null );
        }
        
        if ( useCache )
            biCache.put( name, image );
        
        return ( image );
    }
    
    public static BufferedImage getImage( String name )
    {
        return ( getImage( name, true ) );
    }
    */
    
    private static final boolean checkImage( File file, ImageTemplate it )
    {
        if ( file.lastModified() != __RenderPrivilegedAccess.getLastModified( it ) )
            return ( false );
        
        if ( file.length() != __RenderPrivilegedAccess.getFileSize( it ) )
            return ( false );
        
        return ( true );
    }
    
    public static ImageTemplate getImage( String name, boolean useCache )
    {
        if ( File.separatorChar != '/' )
            name = name.replace( '/', File.separatorChar );
        if ( File.separatorChar != '\\' )
            name = name.replace( '\\', File.separatorChar );
        
        File f = new File( IMAGES_FOLDER, name );
        
        if ( !f.exists() )
        {
            cache.remove( name );
            Logger.log( "[ERROR] Unable to read input file \"" + f.getAbsolutePath() + "\"." );
            return ( null );
        }
        
        ImageTemplate template = useCache ? cache.get( name ) : null;
        
        //System.out.println( ( ( template != null ) ? "found in cache" : "not found in cache" ) );
        
        if ( ( template != null ) && checkImage( f, template ) )
            return ( template );
        
        BufferedImage image = null;
        
        try
        {
            image = ImageIO.read( f );
        }
        catch ( IOException e )
        {
            Logger.log( "[ERROR] Unable to read input file \"" + f.getAbsolutePath() + "\"." );
            return ( null );
        }
        
        template = new ImageTemplate( image );
        
        if ( useCache )
            cache.put( name, template );
        
        __RenderPrivilegedAccess.setLastModified( f.lastModified(), template );
        __RenderPrivilegedAccess.setFileSize( f.length(), template );
        
        return ( template );
    }
    
    public static ImageTemplate getImage( String name )
    {
        return ( getImage( name, true ) );
    }
    
    public static void removeImageFromCache( String name )
    {
        if ( File.separatorChar != '/' )
            name = name.replace( '/', File.separatorChar );
        if ( File.separatorChar != '\\' )
            name = name.replace( '\\', File.separatorChar );
        
        cache.remove( name );
        //biCache.remove( name );
    }
}
