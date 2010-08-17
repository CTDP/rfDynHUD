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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

/**
 * The {@link ImageListModel} is a {@link ListModel}, that keeps all images from the config data folder.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageListModel extends AbstractListModel
{
    private static final long serialVersionUID = 1615729141807367280L;
    
    private final File folder;
    
    private final ArrayList<String> imageNames = new ArrayList<String>();
    
    private static void readFilenames( File folder, String prefix, List<String> filenames )
    {
        for ( File f : folder.listFiles( ImageFileFilter.INSTANCE ) )
        {
            if ( f.isDirectory() )
            {
                if ( prefix == null )
                    readFilenames( f, f.getName(), filenames );
                else
                    readFilenames( f, prefix + "/" + f.getName(), filenames );
            }
            else
            {
                if ( prefix == null )
                    filenames.add( f.getName() );
                else
                    filenames.add( prefix + "/" + f.getName() );
            }
        }
    }
    
    public void update()
    {
        imageNames.clear();
        
        readFilenames( folder, null, imageNames );
        Collections.sort( imageNames );
    }
    
    @Override
    public int getSize()
    {
        return ( imageNames.size() );
    }

    @Override
    public Object getElementAt( int index )
    {
        return ( imageNames.get( index ) );
    }
    
    public final int getIndexOf( String imageName )
    {
        return ( Collections.binarySearch( imageNames, imageName ) );
    }
    
    public ImageListModel( File folder )
    {
        this.folder = folder;
    }
}
