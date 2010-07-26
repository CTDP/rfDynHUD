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
package net.ctdp.rfdynhud.properties;

public abstract class WidgetPropertiesContainer
{
    private int numGroups = 0;
    private int numProperties = 0;
    
    protected abstract void clearImpl();
    
    public final void clear()
    {
        clearImpl();
        
        numGroups = 0;
        numProperties = 0;
    }
    
    protected abstract void addGroupImpl( String groupName, boolean initiallyExpanded, boolean level2 );
    
    /**
     * Creates a new property group.
     * 
     * @param groupName
     * @param initiallyExpanded
     */
    public final void addGroup( String groupName, boolean initiallyExpanded )
    {
        addGroupImpl( groupName, initiallyExpanded, false );
        
        numGroups++;
    }
    
    /**
     * Creates a new property group (initially expanded).
     * 
     * @param groupName
     */
    public final void addGroup( String groupName )
    {
        addGroup( groupName, true );
    }
    
    /**
     * Creates a new property group.
     * 
     * @param groupName
     * @param initiallyExpanded
     */
    public final void addGroupL2( String groupName, boolean initiallyExpanded )
    {
        addGroupImpl( groupName, initiallyExpanded, true );
        
        numGroups++;
    }
    
    /**
     * Creates a new property group (initially expanded).
     * 
     * @param groupName
     */
    public final void addGroupL2( String groupName )
    {
        addGroupL2( groupName, true );
    }
    
    protected abstract void popGroupL2Impl();
    
    public final void popGroupL2()
    {
        popGroupL2Impl();
    }
    
    protected abstract void addPropertyImpl( Property property );
    
    /**
     * Adds the property to the container under the last created group.
     * 
     * @param property
     */
    public final void addProperty( Property property )
    {
        addPropertyImpl( property );
        
        numProperties++;
    }
}
