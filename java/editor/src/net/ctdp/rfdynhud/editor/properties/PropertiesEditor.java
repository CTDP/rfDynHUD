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
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class PropertiesEditor
{
    private static final long serialVersionUID = -1723298567515621091L;
    
    private GridItemsContainer<Property> properties;
    
    private final ArrayList<PropertyChangeListener> changeListeners = new ArrayList<PropertyChangeListener>();
    
    public void addChangeListener( PropertyChangeListener l )
    {
        changeListeners.add( l );
    }
    
    public void removeChangeListener( PropertyChangeListener l )
    {
        changeListeners.remove( l );
    }
    
    void invokeChangeListeners( Property property, Object oldValue, Object newValue, int row, int column )
    {
        for ( int i = 0; i < changeListeners.size(); i++ )
        {
            changeListeners.get( i ).onPropertyChanged( property, oldValue, newValue, row, column );
        }
    }
    
    public void clear()
    {
        properties.clear();
    }
    
    public final GridItemsContainer<Property> getPropertiesList()
    {
        return ( properties );
    }
    
    public void addProperty( Property p )
    {
        properties.addProperty( p );
    }
    
    public void addGroup( GridItemsContainer<Property> group )
    {
        properties.addGroup( group );
    }
    
    /*
    public Property getProperty( int row )
    {
        Object obj = properties.get( row );
        
        if ( obj instanceof Property )
            return ( (Property)obj );
        
        return ( null );
    }
    */
    
    public PropertiesEditor()
    {
        super();
        
        this.properties = new GridItemsContainerImpl( "properties::" );
    }
}
