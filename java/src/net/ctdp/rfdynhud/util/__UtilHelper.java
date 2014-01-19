/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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

import org.jagatoo.util.io.FileUtils;

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class __UtilHelper
{
    private static File extractPluginFolder()
    {
        String[] classPath = System.getProperty( "java.class.path" ).split( File.pathSeparator );
        
        for ( String s : classPath )
        {
            if ( s.contains( "rfdynhud.jar" ) )
                return ( FileUtils.getCanonicalFile( s ).getParentFile() );
            
            if ( s.contains( "rfdynhud_editor.jar" ) )
                return ( FileUtils.getCanonicalFile( s ).getParentFile().getParentFile().getAbsoluteFile() );
        }
        
        // fallback in development mode:
        
        return ( FileUtils.getCanonicalFile( "." ) );
    }
    
    public static final File PLUGIN_FOLDER = extractPluginFolder();
    public static final PluginINI PLUGIN_INI = new PluginINI( PLUGIN_FOLDER );
    public static final File LOG_FOLDER = PLUGIN_INI.getGeneralLogFolder();
    
    public static File configFolder = null;
    public static File bordersBolder = null;
    public static File imagesFolder = null;
    public static String editorPropertyDisplayNameGeneratorClass;// GameFileSystem.INSTANCE.getPluginINI().getEditorPropertyDisplayNameGeneratorClass()
    //__UtilPrivilegedAccess.updateLocalizationsManager
}
