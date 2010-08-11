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

import java.util.ArrayList;

import net.ctdp.rfdynhud.values.Position;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.__ValPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * <p>
 * This class can be used to push it into a Property constructor
 * to be able to instantiate a Property with the field declaration as follows.
 * </p>
 * <p>
 * {@link WidgetToPropertyForwarder} w2pf = new {@link WidgetToPropertyForwarder}();
 * 
 * BooleanProperty boolProp = new BooleanProperty( w2pf, "boolProp", true );
 * 
 * public MyWidgetComponent( Widget widget )
 * {
 *     w2pf.finish( widget );
 *     w2pf = null;
 * }
 * </p>
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetToPropertyForwarder
{
    private ArrayList<Property> properties = new ArrayList<Property>();
    private ArrayList<Position> positions = new ArrayList<Position>();
    private ArrayList<Size> sizes = new ArrayList<Size>();
    
    void addProperty( Property property )
    {
        properties.add( property );
    }
    
    void addPosition( Position position )
    {
        positions.add( position );
    }
    
    void addSize( Size size )
    {
        sizes.add( size );
    }
    
    /**
     * Sets the {@link Widget} in the added Properties.
     * 
     * @param widget
     */
    public void finish( Widget widget )
    {
        if ( properties == null )
            return;
        
        for ( int i = 0; i < properties.size(); i++ )
        {
            properties.get( i ).widget = widget;
        }
        
        properties.clear();
        properties = null;
        
        for ( int i = 0; i < positions.size(); i++ )
        {
            __ValPrivilegedAccess.setWidget(  positions.get( i ), widget );
        }
        
        positions.clear();
        positions = null;
        
        for ( int i = 0; i < sizes.size(); i++ )
        {
            __ValPrivilegedAccess.setWidget( sizes.get( i ), widget );
        }
        
        sizes.clear();
        sizes = null;
    }
    
    public WidgetToPropertyForwarder()
    {
    }
}
