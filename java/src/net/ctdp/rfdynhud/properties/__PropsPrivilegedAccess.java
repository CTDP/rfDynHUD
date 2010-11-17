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

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class __PropsPrivilegedAccess
{
    public static final void setKeeper( Property property, PropertiesKeeper keeper, boolean force )
    {
        property.setKeeper( keeper, force );
    }
    
    public static final void attachKeeper( PropertiesKeeper keeper, boolean force )
    {
        FlatPropertiesContainer pc = new FlatPropertiesContainer();
        
        keeper.getProperties( pc, true );
        
        for ( int i = 0; i < pc.getList().size(); i++ )
        {
            pc.getList().get( i ).setKeeper( keeper, force );
        }
    }
    
    public static final void detachKeeper( PropertiesKeeper keeper )
    {
        FlatPropertiesContainer pc = new FlatPropertiesContainer();
        
        keeper.getProperties( pc, true );
        
        for ( int i = 0; i < pc.getList().size(); i++ )
        {
            pc.getList().get( i ).setKeeper( null, false );
        }
    }
    
    public static final void setCellRenderer( Object renderer, Property property )
    {
        property.cellRenderer = renderer;
    }
    
    public static final Object getCellRenderer( Property property )
    {
        return ( property.cellRenderer );
    }
    
    public static final void setCellEditor( Object editor, Property property )
    {
        property.cellEditor = editor;
    }
    
    public static final Object getCellEditor( Property property )
    {
        return ( property.cellEditor );
    }
}
