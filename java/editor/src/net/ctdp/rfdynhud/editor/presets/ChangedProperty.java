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
package net.ctdp.rfdynhud.editor.presets;

import net.ctdp.rfdynhud.properties.Property;

public class ChangedProperty implements Comparable<ChangedProperty>
{
    private final Property property;
    private final Object oldValue;
    private int changeId;
    
    public final Property getProperty()
    {
        return ( property );
    }
    
    public final Object getOldValue()
    {
        return ( oldValue );
    }
    
    public void resetValue()
    {
        property.setValue( oldValue );
    }
    
    public void setChangeId( int changeId )
    {
        this.changeId = changeId;
    }
    
    public final int getChangeId()
    {
        return ( changeId );
    }
    
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof ChangedProperty ) )
            return ( false );
        
        return ( property.getName().equals( ( (ChangedProperty)o ).property.getName() ) );
    }
    
    @Override
    public int hashCode()
    {
        return ( property.getName().hashCode() );
    }
    
    @Override
    public int compareTo( ChangedProperty o )
    {
        if ( this.changeId < o.changeId )
            return ( -1 );
        
        if ( this.changeId > o.changeId )
            return ( +1 );
        
        return ( 0 );
    }
    
    public ChangedProperty( Property property, Object oldValue, int changeId )
    {
        this.property = property;
        this.oldValue = oldValue;
        this.changeId = changeId;
    }
}
