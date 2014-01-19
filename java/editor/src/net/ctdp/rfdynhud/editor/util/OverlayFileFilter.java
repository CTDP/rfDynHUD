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
package net.ctdp.rfdynhud.editor.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class OverlayFileFilter extends FileFilter implements java.io.FileFilter
{
    @Override
    public boolean accept( File f )
    {
        String name = f.getName().toLowerCase();
        
        if ( !f.isFile() )
        {
            if ( f.isDirectory() )
            {
                if ( name.equals( ".svn" ) )
                    return ( false );
                else if ( f.getParentFile().equals( RFDynHUDEditor.FILESYSTEM.getConfigFolder() ) && name.equals( "data" ) )
                    return ( false );
            }
            else
            {
                return ( false );
            }
        }
        else if ( !name.contains( "overlay" ) || !name.endsWith( ".ini" ) )
        {
            return ( false );
        }
        
        /*
        if ( name.equals( "overlay.ini" ) )
            return ( true );
        
        if ( !name.startsWith( "overlay_" ) )
            return ( false );
        
        if ( !name.endsWith( ".ini" ) )
            return ( false );
        */
        
        return ( true );
    }
    
    @Override
    public String getDescription()
    {
        return ( "Overlay Configurations (*overlay*.ini)" );
    }
}
