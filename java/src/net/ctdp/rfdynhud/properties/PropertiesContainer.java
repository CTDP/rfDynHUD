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
package net.ctdp.rfdynhud.properties;

import java.io.PrintStream;

import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * A container for {@link Widget} {@link Property}s.
 * 
 * @author Marvin Froehlich
 */
public abstract class PropertiesContainer
{
    private int level = 0;
    
    @SuppressWarnings( "unused" )
    private int numGroups = 0;
    @SuppressWarnings( "unused" )
    private int numProperties = 0;
    
    /**
     * Gets the current group level.
     * 
     * @return the current group level.
     */
    public final int getLevel()
    {
        return ( level );
    }
    
    /**
     * Clears the container.
     */
    protected abstract void clearImpl();
    
    /**
     * Clears the container.
     */
    public final void clear()
    {
        clearImpl();
        
        level = 0;
        
        numGroups = 0;
        numProperties = 0;
    }
    
    /**
     * Adds a new group to the container.
     * 
     * @param groupName the new group's name
     * @param initiallyExpanded initially expanded?
     * @param pushed pushed one level down?
     */
    protected abstract void addGroupImpl( Object groupName, boolean initiallyExpanded, boolean pushed );
    
    /**
     * Creates a new property group inside the current group (pushed one level down).
     * 
     * @param groupName the new group's name
     * @param initiallyExpanded initially expanded?
     */
    public final void pushGroup( Object groupName, boolean initiallyExpanded )
    {
        level++;
        
        addGroupImpl( groupName, initiallyExpanded, true );
        
        numGroups++;
    }
    
    /**
     * Creates a new property group inside the current group (pushed one level down).
     * 
     * @param groupName the new group's name
     */
    public final void pushGroup( Object groupName )
    {
        pushGroup( groupName, true );
    }
    
    /**
     * Pop one level up.
     */
    protected abstract void popGroupImpl();
    
    /**
     * Finishes the current group and moves one level up.
     */
    public final void popGroup()
    {
        if ( level == 0 )
            throw new IllegalStateException( "No group to pop." );
        
        popGroupImpl();
        
        level--;
    }
    
    /**
     * Creates a new property group on the same level.
     * 
     * @param groupName the new group's name
     * @param initiallyExpanded initially expanded?
     */
    public final void addGroup( Object groupName, boolean initiallyExpanded )
    {
        addGroupImpl( groupName, initiallyExpanded, false );
        
        numGroups++;
    }
    
    /**
     * Creates a new property group on the same level (initially expanded).
     * 
     * @param groupName the new group's name
     */
    public final void addGroup( Object groupName )
    {
        addGroup( groupName, true );
    }
    
    protected abstract void addPropertyImpl( Property property );
    
    /**
     * Adds the property to the container under the last created group.
     * 
     * @param property the property
     */
    public final void addProperty( Property property )
    {
        addPropertyImpl( property );
        
        numProperties++;
    }
    
    /**
     * Dumps this container to the given {@link PrintStream}.
     * 
     * @param ps the stream to print to
     */
    public abstract void dump( PrintStream ps );
    
    /**
     * Dumps this container to stdout.
     */
    public final void dump()
    {
        dump( System.out );
    }
    
    protected PropertiesContainer()
    {
    }
}
