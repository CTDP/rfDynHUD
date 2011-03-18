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

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ResourceManager
{
    private static boolean needsUpdate = true;
    
    private static boolean ideMode = true;
    private static File rfdynhud_jar = null;
    private static File rfdynhud_editor_jar = null;
    
    public static final void update()
    {
        if ( needsUpdate )
        {
            // reset defaults
            ideMode = false;
            rfdynhud_jar = null;
            rfdynhud_editor_jar = null;
            
            String classpath = System.getProperty( "java.class.path" );
            String separator = System.getProperty( "path.separator" );
            
            for ( String s : classpath.split( separator ) )
            {
                File f = new File( s );
                
                if ( f.isDirectory() )
                {
                    // We're obviously running inside of an IDE.
                    ideMode = true;
                }
                else if ( f.getName().equalsIgnoreCase( "rfdynhud.jar" ) )
                {
                    rfdynhud_jar = f;
                }
                else if ( f.getName().equalsIgnoreCase( "rfdynhud_editor.jar" ) )
                {
                    rfdynhud_editor_jar = f;
                }
            }
            
            needsUpdate = false;
        }
    }
    
    public static final boolean isCompleteIDEMode()
    {
        update();
        
        return ( ideMode && ( rfdynhud_jar == null ) );
    }
    
    public static final boolean isIDEMode()
    {
        update();
        
        return ( ideMode );
    }
    
    public static final File getRFDynHUD_Jar()
    {
        update();
        
        return ( rfdynhud_jar );
    }
    
    public static final File getRFDynHUDEditor_Jar()
    {
        update();
        
        return ( rfdynhud_editor_jar );
    }
}
