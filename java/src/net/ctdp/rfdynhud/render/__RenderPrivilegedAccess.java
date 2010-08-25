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
package net.ctdp.rfdynhud.render;

import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class __RenderPrivilegedAccess
{
    public static TransformableTexture createMainTexture( int width, int height )
    {
        return ( TransformableTexture.createMainTexture( width, height ) );
    }
    
    public static void setOwnerWidget( Widget ownerWidget, TransformableTexture tt )
    {
        tt.setOwnerWidget( ownerWidget );
    }
    
    public static final void onWidgetCleared( DrawnStringFactory dsf )
    {
        dsf.onWidgetCleared();
    }
    
    public static final void setLastModified( long lastModified, ImageTemplate it )
    {
        it.lastModified = lastModified;
    }
    
    public static final long getLastModified( ImageTemplate it )
    {
        return ( it.lastModified );
    }
    
    public static final void setFileSize( long fileSize, ImageTemplate it )
    {
        it.fileSize = fileSize;
    }
    
    public static final long getFileSize( ImageTemplate it )
    {
        return ( it.fileSize );
    }
}
