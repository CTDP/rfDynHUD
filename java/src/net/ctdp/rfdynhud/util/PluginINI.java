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
    public static final File INI_FILE = new File( RFactorTools.PLUGIN_FOLDER, "rfdynhud.ini" );
    
    private long lastModified = -1;
    
    private String general_language = null;
    private File general_configFolder = null;
    
    private void reset()
    {
        general_language = null;
        general_configFolder = null;
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
    
    private static File getConfigFolder( String configPath )
    {
        if ( !ResourceManager.isJarMode() )
            return ( new File( new File( Helper.stripDotDots( new File( "." ).getAbsolutePath() ), "data" ), "config" ).getAbsoluteFile() );
        
        if ( configPath == null )
            configPath = new File( RFactorTools.PLUGIN_FOLDER, "config" ).getAbsolutePath();
        
        configPath = parsePath( configPath );
        File f = new File( configPath );
        if ( !f.isAbsolute() )
            f = new File( RFactorTools.PLUGIN_FOLDER, configPath );
        
        f = Helper.stripDotDots( f.getAbsolutePath() );
        
        Logger.log( "Using config folder \"" + f.getAbsolutePath() + "\"." );
        
        try
        {
            f.mkdirs();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            Logger.log( "[ERROR] Config folder doesn't exist and couldn't create it." );
        }
        
        return ( f );
    }
    
    private void update()
    {
        if ( INI_FILE.lastModified() <= lastModified )
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
                            general_configFolder = getConfigFolder( value );
                        }
                    }
                    
                    return ( true );
                }
            }.parse( INI_FILE );
            
            lastModified = INI_FILE.lastModified();
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
}
