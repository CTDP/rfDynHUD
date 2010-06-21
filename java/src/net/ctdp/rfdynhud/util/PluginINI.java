package net.ctdp.rfdynhud.util;

import java.io.File;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Abstraction of the main ini file for the plugin.
 * 
 * @author Marvin Froehlich
 */
public class PluginINI
{
    private static final File IDE_DATA_FOLDER = ResourceManager.isJarMode() ? null : new File( __UtilHelper.stripDotDots( new File( "." ).getAbsolutePath() ), "data" ).getAbsoluteFile();
    
    private final File pluginFolder;
    private final File iniFile;
    
    private long lastModified = -1;
    
    private String general_language = null;
    private File general_configFolder = null;
    private File general_cacheFolder = null;
    
    private void reset()
    {
        general_language = null;
        general_configFolder = null;
        general_cacheFolder = null;
    }
    
    private static String parsePath( String path )
    {
        int p;
        while ( ( p = path.indexOf( "${" ) ) >= 0 )
        {
            int p2 = path.indexOf( '}', p + 1 );
            if ( p2 < 0 )
                break;
            
            String val = System.getenv( path.substring( p + 2, p2 ) );
            if ( val == null )
                val = "";
            
            if ( p == 0 )
            {
                if ( p2 == path.length() - 1 )
                {
                    path = val;
                }
                else
                {
                    path = val + path.substring( p2 + 1 );
                }
            }
            else if ( p2 == path.length() - 1 )
            {
                path = path.substring( 0, p ) + val;
            }
            else
            {
                path = path.substring( 0, p ) + val + path.substring( p2 + 1 );
            }
        }
        
        return ( path );
    }
    
    private File getFolder( String name, String configPath, String fallback )
    {
        if ( !ResourceManager.isJarMode() )
            return ( new File( IDE_DATA_FOLDER, fallback ).getAbsoluteFile() );
        
        if ( configPath == null )
            configPath = new File( pluginFolder, fallback ).getAbsolutePath();
        
        configPath = parsePath( configPath );
        File f = new File( configPath );
        if ( !f.isAbsolute() )
            f = new File( pluginFolder, configPath );
        
        f = __UtilHelper.stripDotDots( f.getAbsolutePath() );
        
        Logger.log( "Using " + name + " folder \"" + f.getAbsolutePath() + "\"." );
        
        try
        {
            f.mkdirs();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            Logger.log( "[ERROR] The " + name + " folder doesn't exist and couldn't be created." );
        }
        
        return ( f );
    }
    
    private void update()
    {
        if ( iniFile.lastModified() <= lastModified )
            return;
        
        reset();
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( group == null )
                    {
                    }
                    else if ( group.equals( "GENERAL" ) )
                    {
                        if ( key.equals( "language" ) )
                        {
                            general_language = value;
                        }
                        else if ( key.equals( "configFolder" ) )
                        {
                            general_configFolder = getFolder( "config", value, "config" );
                        }
                        else if ( key.equals( "cacheFolder" ) )
                        {
                            if ( ( value == null ) || value.equals( "" ) )
                                general_cacheFolder = null;
                            else
                                general_cacheFolder = getFolder( "cache", value, "cache" );
                        }
                    }
                    
                    return ( true );
                }
            }.parse( iniFile );
            
            lastModified = iniFile.lastModified();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    /**
     * Gets the language setting from GENERAL group.
     * 
     * @return the language setting from GENERAL group.
     */
    public final String getGeneralLanguage()
    {
        update();
        
        return ( general_language );
    }
    
    /**
     * Gets the configFolder setting from GENERAL group.
     * 
     * @return the configFolder setting from GENERAL group.
     */
    public final File getGeneralConfigFolder()
    {
        update();
        
        return ( general_configFolder );
    }
    
    /**
     * Gets the cacheFolder setting from GENERAL group.
     * 
     * @return the cacheFolder setting from GENERAL group.
     */
    public final File getGeneralCacheFolder()
    {
        update();
        
        return ( general_cacheFolder );
    }
    
    public PluginINI( File pluginFolder )
    {
        this.pluginFolder = pluginFolder;
        this.iniFile = new File( pluginFolder, "rfdynhud.ini" );
    }
}
