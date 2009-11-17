package net.ctdp.rfdynhud.util;

import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author Marvin Froehlich
 */
public class ResourceManager
{
    private static Boolean jarMode = null;
    private static URL dataDir = null;
    
    public static final boolean isJarMode()
    {
        if ( jarMode == null )
        {
            String classpath = System.getProperty( "java.class.path" );
            jarMode = classpath.contains( "rfdynhud.jar" ) || classpath.contains( "rfdynhud_editor.jar" );
        }
        
        return ( jarMode.booleanValue() );
    }
    
    public static URL getDataDirectory()
    {
        if ( dataDir == null )
        {
            try
            {
                if ( isJarMode() )
                {
                    dataDir = ResourceManager.class.getClassLoader().getResource( "data/" );
                }
                else
                {
                    java.io.File dir = new java.io.File( System.getProperty( "java.class.path" ) );
                    dir = dir.getParentFile();
                    dir = new java.io.File( dir, "data" );
                    
                    dataDir = dir.toURI().toURL();
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
        
        return ( dataDir );
    }
    
    public static URL getDataResource( String name )
    {
        try
        {
            if ( isJarMode() )
            {
                return ( ResourceManager.class.getClassLoader().getResource( "data/" + name ) );
            }
            else
            {
                return ( new URL( getDataDirectory(), name ) );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return ( null );
        }
    }
}
