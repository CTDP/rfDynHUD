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
package net.ctdp.rfdynhud.widgets.base.widget;

import net.ctdp.rfdynhud.widgets._util.StandardWidgetSet;

public class AssembledWidget extends AbstractAssembledWidget
{
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( StandardWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    protected void arrangeParts( Widget[] parts )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Widget[] initParts( float width, boolean widthPercent, float height, boolean heightPercent )
    {
        return ( new Widget[ 0 ] );
    }
    
    /**
     * 
     * @param initParts
     */
    public AssembledWidget( boolean initParts )
    {
        super( 33.6f, true, 23.0f, true, false );
    }
    
    public AssembledWidget()
    {
        this( false );
    }
}
