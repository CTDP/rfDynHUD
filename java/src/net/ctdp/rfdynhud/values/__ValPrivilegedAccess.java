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
package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public class __ValPrivilegedAccess
{
    public static final Position newWidgetPosition( RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, Size size, Widget widget )
    {
        return ( new Position( positioning, x, xPercent, y, yPercent, size, widget, true ) );
    }
    
    public static final Size newWidgetSize( float width, boolean widthPercent, float height, boolean heightPercent, Widget widget )
    {
        return ( new Size( width, widthPercent, height, heightPercent, widget, true ) );
    }
}
