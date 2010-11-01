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
package net.ctdp.rfdynhud.util;

import java.util.Comparator;

import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * {@link Comparator} implementation to sort {@link Widget}s by z-index first, then by y-coordinate and then by x-coordinate.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetZYXComparator implements Comparator<Widget>
{
    @Override
    public int compare( Widget w1, Widget w2 )
    {
        int z1 = w1.getZIndex();
        int z2 = w2.getZIndex();
        
        if ( z1 < z2 )
            return ( -1 );
        
        if ( z1 > z2 )
            return ( +1 );
        
        int y1 = w1.getPosition().getEffectiveY();
        int y2 = w2.getPosition().getEffectiveY();
        
        if ( y1 < y2 )
            return ( -1 );
        
        if ( y1 > y2 )
            return ( +1 );
        
        int x1 = w1.getPosition().getEffectiveX();
        int x2 = w2.getPosition().getEffectiveX();
        
        if ( x1 < x2 )
            return ( -1 );
        
        if ( x1 > x2 )
            return ( +1 );
        
        return ( 0 );
    }
    
    public static final WidgetZYXComparator INSTANCE = new WidgetZYXComparator();
};
