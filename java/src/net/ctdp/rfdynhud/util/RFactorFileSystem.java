package net.ctdp.rfdynhud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

public class RFactorFileSystem
{
    private static boolean isRoot( File folder )
    {
        return ( folder.getParent() == null );
    }
    
    private static File readDevRFactorFolder()
    {
        try
        {
            BufferedReader br = new BufferedReader( new FileReader( new File( "rfactor_folder.txt" ) ) );
            String line = br.readLine();
            br.close();
            
            return ( new File( line ).getAbsoluteFile() );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( null );
        }
    }
    
    private static File findRFactorFolder( File pluginFolder )
    {
        if ( !ResourceManager.isJarMode() )
            return ( readDevRFactorFolder() );
        
        File f = pluginFolder.getParentFile();
        
        while ( !new File( f, "rFactor.exe" ).exists() && !isRoot( f ) )
            f = f.getParentFile();
        
        if ( new File( f, "rFactor.exe" ).exists() )
            return ( f.getAbsoluteFile() );
        
        if ( isRoot( pluginFolder ) )
            return ( pluginFolder.getAbsoluteFile() );
        
        return ( pluginFolder.getParentFile().getAbsoluteFile() );
    }
    
    public static final File PLUGIN_FOLDER = Helper.PLUGIN_FOLDER;
    public static final String PLUGIN_PATH = PLUGIN_FOLDER.getAbsolutePath();
    public static final PluginINI PLUGIN_INI = new PluginINI();
    public static final File RFACTOR_FOLDER = findRFactorFolder( PLUGIN_FOLDER );
    public static final String RFACTOR_PATH = RFACTOR_FOLDER.getAbsolutePath();
    public static final File CONFIG_FOLDER = PLUGIN_INI.getGeneralConfigFolder();
    public static final String CONFIG_PATH = CONFIG_FOLDER.getAbsolutePath();
    public static final File IMAGES_FOLDER = new File( new File( CONFIG_FOLDER, "data" ), "images" ).getAbsoluteFile();
    public static final File EDITOR_FOLDER = ResourceManager.isJarMode() ? new File( PLUGIN_FOLDER, "editor" ).getAbsoluteFile() : new File( Helper.stripDotDots( new File( "." ).getAbsolutePath() ), "data" ).getAbsoluteFile();
    public static final String EDITOR_PATH = EDITOR_FOLDER.getAbsolutePath();
    public static final File SCREENSHOTS_FOLDER = RFactorFileSystem.getPathFromRFConfigINI( RFactorFileSystem.RFACTOR_FOLDER, "ScreenShotsDir", "UserData" + File.separator + "ScreenShots" );
    
    private static File getFallbackRFConfigINIPath( String fallback )
    {
        File fallback0 = new File( RFACTOR_FOLDER, fallback ).getAbsoluteFile();
        
        try
        {
            return ( fallback0.getCanonicalFile() );
        }
        catch ( IOException e )
        {
            return ( fallback0 );
        }
    }
    
    public static File getPathFromRFConfigINI( File rFactorFolder, final String setting, String fallback )
    {
        File config = new File( RFACTOR_FOLDER, "config.ini" );
        
        if ( !config.exists() )
        {
            return ( getFallbackRFConfigINIPath( fallback ) );
        }
        
        final String[] result = { null };
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( "COMPONENTS".equalsIgnoreCase( group ) && setting.equalsIgnoreCase( key ) )
                    {
                        result[0] = value;
                        
                        return ( false );
                    }
                    
                    return ( true );
                }
            }.parse( config );
        }
        catch ( ParsingException e1 )
        {
            return ( getFallbackRFConfigINIPath( fallback ) );
        }
        catch ( IOException e1 )
        {
            return ( getFallbackRFConfigINIPath( fallback ) );
        }
        
        if ( result[0] == null )
            return ( getFallbackRFConfigINIPath( fallback ) );
        
        File f = new File( result[0] );
        if ( f.isAbsolute() )
            return ( f );
        
        f = new File( rFactorFolder, result[0] );
        try
        {
            return ( f.getCanonicalFile() );
        }
        catch ( IOException e )
        {
            return ( f.getAbsoluteFile() );
        }
    }
}
