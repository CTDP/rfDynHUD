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
package net.ctdp.rfdynhud.editor.properties;

import java.util.ArrayList;


import net.ctdp.rfdynhud.editor.hiergrid.GridItemsContainer;
import net.ctdp.rfdynhud.properties.Property;

/**
 * Default implementation of the {@link GridItemsContainer}.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class GridItemsContainerImpl extends ArrayList< Object > implements GridItemsContainer<Property>
{
    private static final long serialVersionUID = -2178071328264336996L;
    
    private final Object name;
    private boolean expandFlag;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getNameForGrid()
    {
        return ( String.valueOf( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setExpandFlag( boolean flag )
    {
        this.expandFlag = flag;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean getExpandFlag()
    {
        return ( expandFlag );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNumberOfItems()
    {
        return ( size() );
    }
    
    /*
     * {@inheritDoc}
     */
    //@Override
    public void addGroup( GridItemsContainer<Property> group )
    {
        add( group );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addProperty( Property property )
    {
        add( property );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getItem( int index )
    {
        return ( get( index ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getClass().getSimpleName() + " \"" + getNameForGrid() + "\"" );
    }
    
    public GridItemsContainerImpl( Object name, boolean expandFlag, int initialCapacity )
    {
        super( initialCapacity );
        
        this.name = name;
        this.expandFlag = expandFlag;
    }
    
    public GridItemsContainerImpl( Object name, boolean expandFlag )
    {
        this( name, expandFlag, 16 );
    }
    
    public GridItemsContainerImpl( Object name, int initialCapacity )
    {
        this( name, false, initialCapacity );
    }
    
    public GridItemsContainerImpl( Object name )
    {
        this( name, false );
    }
}
