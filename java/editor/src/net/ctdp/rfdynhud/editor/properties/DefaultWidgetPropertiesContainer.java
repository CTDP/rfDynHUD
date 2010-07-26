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

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;

public class DefaultWidgetPropertiesContainer extends WidgetPropertiesContainer
{
    private final FlaggedList root;
    private FlaggedList list;
    private FlaggedList listL2 = null;
    
    @Override
    protected void clearImpl()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void addGroupImpl( String groupName, boolean initiallyExpanded, boolean level2 )
    {
        if ( level2 )
        {
            if ( list == root )
                throw new IllegalStateException( "No group for level 1 defined." );
            
            FlaggedList group = new FlaggedList( groupName, initiallyExpanded );
            list.add( group );
            listL2 = group;
        }
        else
        {
            FlaggedList group = new FlaggedList( groupName, initiallyExpanded );
            root.add( group );
            list = group;
        }
    }
    
    @Override
    protected void popGroupL2Impl()
    {
        if ( listL2 == null )
            throw new IllegalStateException( "No group for level 2 defined/active." );
        
        listL2 = null;
    }
    
    @Override
    protected void addPropertyImpl( Property property )
    {
        if ( listL2 != null )
            listL2.add( property );
        else
            list.add( property );
    }
    
    public DefaultWidgetPropertiesContainer( FlaggedList root )
    {
        this.root = root;
        this.list = root;
    }
}
