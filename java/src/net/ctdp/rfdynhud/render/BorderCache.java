package net.ctdp.rfdynhud.render;

import java.io.File;
import java.util.HashMap;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.TextureManager;

/**
 * The {@link BorderCache} is used to load borders only once.
 * 
 * @author Marvin Froehlich
 */
public class BorderCache
{
    private static final HashMap<String, BorderWrapper> CACHE = new HashMap<String, BorderWrapper>();
    
    private static String parseTypeFromIni( File iniFile )
    {
        final String[] type = { null };
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( "General".equals( group ) )
                    {
                        if ( key.equals( "Type" ) )
                        {
                            type[0] = value;
                            
                            return ( false );
                        }
                    }
                    
                    return ( true );
                }
            }.parse( iniFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( type[0] );
        }
        
        return ( type[0] );
    }
    
    private static String parseImageFromIni( File iniFile )
    {
        final String[] image = { null };
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( "General".equals( group ) )
                    {
                        if ( key.equals( "Image" ) )
                        {
                            image[0] = value;
                            
                            return ( false );
                        }
                    }
                    
                    return ( true );
                }
            }.parse( iniFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( image[0] );
        }
        
        return ( image[0] );
    }
    
    private static String parseRendererFromIni( File iniFile )
    {
        final String[] renderer = { null };
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( "General".equals( group ) )
                    {
                        if ( key.equals( "RendererClass" ) )
                        {
                            renderer[0] = value;
                            
                            return ( false );
                        }
                    }
                    
                    return ( true );
                }
            }.parse( iniFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( renderer[0] );
        }
        
        return ( renderer[0] );
    }
    
    private static BorderMeasures parseMeasuresFromIni( File iniFile )
    {
        final BorderMeasures measures = new BorderMeasures();
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( "Measures".equals( group ) )
                    {
                        if ( key.equals( "LeftWidth" ) )
                            measures.setLeftWidth( Integer.parseInt( value ) );
                        else if ( key.equals( "RightWidth" ) )
                            measures.setRightWidth( Integer.parseInt( value ) );
                        else if ( key.equals( "TopHeight" ) )
                            measures.setTopHeight( Integer.parseInt( value ) );
                        else if ( key.equals( "BottomHeight" ) )
                            measures.setBottomHeight( Integer.parseInt( value ) );
                        
                        else if ( key.equals( "InnerLeftWidth" ) )
                            measures.setInnerLeftWidth( Integer.parseInt( value ) );
                        else if ( key.equals( "InnerRightWidth" ) )
                            measures.setInnerRightWidth( Integer.parseInt( value ) );
                        else if ( key.equals( "InnerTopHeight" ) )
                            measures.setInnerTopHeight( Integer.parseInt( value ) );
                        else if ( key.equals( "InnerBottomHeight" ) )
                            measures.setInnerBottomHeight( Integer.parseInt( value ) );
                        
                        else if ( key.equals( "OpaqueLeftWidth" ) )
                            measures.setOpaqueLeftWidth( Integer.parseInt( value ) );
                        else if ( key.equals( "OpaqueRightWidth" ) )
                            measures.setOpaqueRightWidth( Integer.parseInt( value ) );
                        else if ( key.equals( "OpaqueTopHeight" ) )
                            measures.setOpaqueTopHeight( Integer.parseInt( value ) );
                        else if ( key.equals( "InnerBottomHeight" ) )
                            measures.setOpaqueBottomHeight( Integer.parseInt( value ) );
                    }
                    
                    return ( true );
                }
            }.parse( iniFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( measures );
        }
        
        return ( measures );
    }
    
    private static BorderWrapper getFallback( String iniFilename )
    {
        BorderWrapper bw = new BorderWrapper( null, null );
        
        CACHE.put( iniFilename, bw );
        
        return ( bw );
    }
    
    /**
     * Gets or creates a TexturedBorder with the given side widths.
     * 
     * @param texture
     */
    public static BorderWrapper getBorder( String iniFilename )
    {
        if ( iniFilename == null )
            return ( null );
        
        BorderWrapper border = CACHE.get( iniFilename );
        
        if ( border != null )
            return ( border );
        
        if ( File.separatorChar != '/' )
            iniFilename = iniFilename.replace( '/', File.separatorChar );
        if ( File.separatorChar != '\\' )
            iniFilename = iniFilename.replace( '\\', File.separatorChar );
        
        File iniFile = new File( new File( TextureManager.IMAGES_FOLDER, "borders" ), iniFilename );
        
        if ( !iniFile.exists() || !iniFile.isFile() || !iniFile.getName().toLowerCase().endsWith( ".ini" ) )
        {
            Logger.log( "[Error] Border ini file invalid \"" + iniFilename + "\"." );
            
            return ( getFallback( iniFilename ) );
        }
        
        String type = parseTypeFromIni( iniFile );
        if ( type == null )
        {
            Logger.log( "[Error] No \"Type\" setting found in \"" + iniFile.getAbsolutePath() + "\"." );
            
            return ( getFallback( iniFilename ) );
        }
        
        if ( type.equals( "Image" ) )
        {
            String textureName = parseImageFromIni( iniFile );
            
            if ( textureName == null )
            {
                Logger.log( "[Error] No \"Image\" setting found in \"" + iniFile.getAbsolutePath() + "\"." );
                
                return ( getFallback( iniFilename ) );
            }
            
            TextureImage2D texture = TextureManager.getImage( "borders" + File.separator + textureName, false ).getTextureImage();
            
            BorderWrapper bw = new BorderWrapper( new ImageBorderRenderer( textureName, texture ), parseMeasuresFromIni( iniFile ) );
            
            CACHE.put( iniFilename, bw );
            
            return ( bw );
        }
        
        if ( type.equals( "Renderer" ) )
        {
            String rendererClass = parseRendererFromIni( iniFile );
            
            if ( rendererClass == null )
            {
                Logger.log( "[Error] No \"RendererClass\" setting found in \"" + iniFile.getAbsolutePath() + "\"." );
                
                return ( getFallback( iniFilename ) );
            }
            
            Class<?> clazz = null;
            try
            {
                clazz = (Class<?>)Class.forName( rendererClass );
            }
            catch ( Throwable t )
            {
                Logger.log( "[Error] Unable to load BorderRenderer class \"" + rendererClass + "\"." );
                
                return ( getFallback( iniFilename ) );
            }
            
            if ( !BorderRenderer.class.isAssignableFrom( clazz ) )
            {
                Logger.log( "[Error] \"" + rendererClass + "\" is not a subclass of " + BorderRenderer.class.getName() + "." );
                
                return ( getFallback( iniFilename ) );
            }
            
            BorderRenderer br = null;
            try
            {
                br = (BorderRenderer)clazz.newInstance();
            }
            catch ( Throwable t )
            {
                Logger.log( "[Error] Unable to instantiate " + clazz.getName() + " using default constructor." );
                
                return ( getFallback( iniFilename ) );
            }
            
            BorderWrapper bw = new BorderWrapper( br, parseMeasuresFromIni( iniFile ) );
            
            CACHE.put( iniFilename, bw );
            
            return ( bw );
        }
        
        Logger.log( "[Error] Unknown border type \"" + type + "\" in border ini file \"" + iniFile.getAbsolutePath() + "\"." );
        
        return ( getFallback( iniFilename ) );
    }
}
