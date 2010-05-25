package net.ctdp.rfdynhud.util;

import java.io.File;
import java.io.IOException;

public class __UtilHelper
{
    public static final File stripDotDots( String pathname )
    {
        try
        {
            return ( new File( pathname ).getCanonicalFile().getAbsoluteFile() );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        
        return ( new File( pathname ).getAbsoluteFile() );
    }
    
    private static File extractPluginFolder()
    {
        String[] classPath = System.getProperty( "java.class.path" ).split( File.pathSeparator );
        
        for ( String s : classPath )
        {
            if ( s.contains( "rfdynhud.jar" ) )
                return ( stripDotDots( s ).getParentFile() );
            
            if ( s.contains( "rfdynhud_editor.jar" ) )
                return ( stripDotDots( s ).getParentFile().getParentFile().getAbsoluteFile() );
        }
        
        File f = new File( "." );
        try
        {
            return ( f.getCanonicalFile().getAbsoluteFile() );
        }
        catch ( IOException e )
        {
            return ( new File( "." ).getAbsoluteFile() );
        }
    }
    
    public static final File PLUGIN_FOLDER = extractPluginFolder();
    public static final File LOG_FOLDER = new File( PLUGIN_FOLDER, "log" ).getAbsoluteFile();
}
