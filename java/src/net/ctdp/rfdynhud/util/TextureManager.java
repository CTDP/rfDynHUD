package net.ctdp.rfdynhud.util;

import java.awt.Color;
import java.awt.Graphics2D;
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
public class TextureManager
{
    public static final File IMAGES_FOLDER = new File( RFactorTools.CONFIG_PATH + File.separator + "data" + File.separator + "images" );
    
    private static ImageTemplate MISSING_IMAGE = null;
    
    public static BufferedImage createMissingImage( int width, int height )
    {
        BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
        
        Graphics2D g = bi.createGraphics();
        
        final int sx = 5;
        final int sy = 5;
        final int sx2 = sx + sx;
        final int sy2 = sy + sy;
        
        for ( int y = 0; y < height; y += sy )
        {
            for ( int x = 0; x < width; x += 5 )
            {
                if ( ( y % sy2 ) == 0 )
                {
                    if ( ( x % sx2 ) == 0 )
                        g.setColor( Color.LIGHT_GRAY );
                    else
                        g.setColor( Color.WHITE );
                }
                else
                {
                    if ( ( x % sx2 ) == 0 )
                        g.setColor( Color.WHITE );
                    else
                        g.setColor( Color.LIGHT_GRAY );
                }
                
                g.fillRect( x, y, 5, 5 );
            }
        }
        
        return ( bi );
    }
    
    private static final ImageTemplate getMissingImage()
    {
        if ( MISSING_IMAGE == null )
        {
            MISSING_IMAGE = new ImageTemplate( createMissingImage( 128, 128 ) );
        }
        
        return ( MISSING_IMAGE );
    }
    
    private static final HashMap<String, ImageTemplate> cache = new HashMap<String, ImageTemplate>();
    
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
        
        ImageTemplate template = null;
        
        if ( !f.exists() )
        {
            if ( useCache )
            {
                template = cache.get( name );
                
                if ( template != getMissingImage() )
                {
                    Logger.log( "[ERROR] Unable to read input file \"" + f.getAbsolutePath() + "\"." );
                    //Logger.log( new Exception() );
                    
                    template = getMissingImage();
                    
                    cache.remove( name );
                    cache.put( name, template );
                }
            }
            else
            {
                Logger.log( "[ERROR] Unable to read input file \"" + f.getAbsolutePath() + "\"." );
                //Logger.log( new Exception() );
                
                template = getMissingImage();
            }
            
            return ( template );
        }
        
        template = useCache ? cache.get( name ) : null;
        
        //System.out.println( ( ( template != null ) ? "found in cache" : "not found in cache" ) );
        
        if ( template != null )
        {
            if ( template == getMissingImage() )
            {
                if ( useCache )
                    cache.remove( name );
                
                template = null;
            }
            else if ( checkImage( f, template ) )
            {
                return ( template );
            }
        }
        
        BufferedImage image = null;
        
        try
        {
            image = ImageIO.read( f );
        }
        catch ( IOException e )
        {
            Logger.log( "[ERROR] Unable to read input file \"" + f.getAbsolutePath() + "\"." );
            return ( getMissingImage() );
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
