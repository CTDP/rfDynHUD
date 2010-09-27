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
package net.ctdp.rfdynhud.editor.hiergrid;

/**
 * The {@link GridItemsContainer} keeps items for the hierarchical grid (properties and groups, which are {@link GridItemsContainer}s themselfes).
 * 
 * @param <P> the property type
 * 
 * @author Marvin Froehlich
 */
public interface GridItemsContainer<P extends Object>
{
    /**
     * Gets the name of this container.
     * 
     * @return the name of this container.
     */
    public String getNameForGrid();
    
    /**
     * Sets the expand flag for this container.
     * 
     * @param flag
     */
    public void setExpandFlag( boolean flag );
    
    /**
     * Gets the expand flag for this container.
     * 
     * @return the expand flag for this container.
     */
    public boolean getExpandFlag();
    
    public int getNumberOfItems();
    
    //public void addGroup( GridItemsContainer<P> group );
    
    public void addProperty( P property );
    
    public Object getItem( int index );
    
    public void clear();
}
