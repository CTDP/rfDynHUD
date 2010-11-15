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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FlatPropertiesContainer extends PropertiesContainer
{
    private final ArrayList<Property> properties = new ArrayList<Property>();
    
    /**
     * Gets the properties list.
     * 
     * @return the properties list.
     */
    public final List<Property> getList()
    {
        return ( properties );
    }
    
    @Override
    protected void clearImpl()
    {
        properties.clear();
    }
    
    @Override
    protected void addGroupImpl( Object groupName, boolean initiallyExpanded, boolean pushed )
    {
    }
    
    @Override
    protected void popGroupImpl()
    {
    }
    
    @Override
    protected void addPropertyImpl( Property property )
    {
        properties.add( property );
    }
    
    @Override
    public void dump( PrintStream ps )
    {
        for ( int i = 0; i < properties.size(); i++ )
        {
            ps.println( properties.get( i ) );
        }
    }
}
