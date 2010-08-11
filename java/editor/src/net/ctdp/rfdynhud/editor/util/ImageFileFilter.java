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
package net.ctdp.rfdynhud.editor.util;

import java.io.File;
import java.io.FileFilter;

public class ImageFileFilter implements FileFilter
{
    @Override
    public boolean accept( File file )
    {
        String name = file.getName();
        
        if ( file.isDirectory() )
            return ( !name.equalsIgnoreCase( ".svn" ) );
        
        int pos = name.lastIndexOf( '.' );
        if ( pos < 0 )
            return ( false );
        String ext = name.substring( pos ).toLowerCase();
        
        if ( ext.endsWith( ".png" ) )
            return ( true );
        
        if ( ext.endsWith( ".jpg" ) )
            return ( true );
        
        if ( ext.endsWith( ".gif" ) )
            return ( true );
        
        return ( false );
    }
    
    protected ImageFileFilter()
    {
    }
    
    public static final ImageFileFilter INSTANCE = new ImageFileFilter(); 
}
