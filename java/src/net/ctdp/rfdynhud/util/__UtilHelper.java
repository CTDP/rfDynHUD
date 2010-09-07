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

import java.io.File;
import java.io.IOException;

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
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
        
        // fallback in development mode:
        
        File f = new File( "." );
        try
        {
            return ( f.getCanonicalFile().getAbsoluteFile() );
        }
        catch ( IOException e )
        {
            return ( f.getAbsoluteFile() );
        }
    }
    
    public static final File PLUGIN_FOLDER = extractPluginFolder();
    public static final File LOG_FOLDER = new File( PLUGIN_FOLDER, "log" ).getAbsoluteFile();
}
