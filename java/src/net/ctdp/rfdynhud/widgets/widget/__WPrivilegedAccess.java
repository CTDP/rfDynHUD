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
package net.ctdp.rfdynhud.widgets.widget;

import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class __WPrivilegedAccess
{
    public static final void setConfiguration( WidgetsConfiguration config, Widget widget, boolean loading )
    {
        widget.setConfiguration( config );
        
        if ( !loading && ( config != null ) && ( widget instanceof AssembledWidget ) )
            ( (AssembledWidget)widget ).arrangeParts();
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public static final void setLocalStore( Object localStore, StatefulWidget widget )
    {
        widget.setLocalStore( localStore );
    }
    
    @SuppressWarnings( "rawtypes" )
    public static final boolean hasLocalStore( StatefulWidget widget )
    {
        return ( widget.hasLocalStore() );
    }
    
    public static final boolean isInitialized( Widget widget )
    {
        return ( widget.isInitialized() );
    }
    
    public static final void onPropertyChanged( Property property, Object oldValue, Object newValue, Widget widget )
    {
        widget.onPropertyChanged( property, oldValue, newValue );
    }
    
    public static final void onPositionChanged( RelativePositioning oldPositioning, int oldX, int oldY, RelativePositioning newPositioning, int newX, int newY, Widget widget )
    {
        widget.onPositionChanged( oldPositioning, oldX, oldY, newPositioning, newX, newY );
    }
    
    public static final void onSizeChanged( int oldWidth, int oldHeight, int newWidth, int newHeight, Widget widget )
    {
        widget.onSizeChanged( oldWidth, oldHeight, newWidth, newHeight );
    }
    
    public static final BorderProperty getBorderProperty( Widget widget )
    {
        return ( widget.getBorderProperty() );
    }
    
    public static final boolean needsCompleteClear( Widget widget )
    {
        return ( widget.needsCompleteClear() );
    }
}
