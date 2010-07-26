/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
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
                return ( ResourceManager.class.getClassLoader().getResource( "data/" + name ) );
            
            return ( new URL( getDataDirectory(), name ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            return ( null );
        }
    }
}
