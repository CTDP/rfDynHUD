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
package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.properties.Property;

public class WidgetPropertyChangeListener implements PropertyChangeListener
{
    private final RFDynHUDEditor editor;
    
    static final boolean needsAreaClear( Property p )
    {
        if ( p.getName().equals( "positioning" ) )
            return ( true );
        
        if ( p.getName().equals( "x" ) )
            return ( true );
        
        if ( p.getName().equals( "y" ) )
            return ( true );
        
        if ( p.getName().equals( "width" ) )
            return ( true );
        
        if ( p.getName().equals( "height" ) )
            return ( true );
        
        if ( p.getName().equals( "initialVisibility" ) )
            return ( true );
        
        return ( false );
    }
    
    @Override
    public void onPropertyChanged( Property property, Object oldValue, Object newValue, int row, int column )
    {
        if ( column == 2 )
        {
            if ( property != null )
            {
                if ( needsAreaClear( property ) )
                    editor.getEditorPanel().clearSelectedWidgetRegion();
                
                //property.setValue( newValue );
                
                editor.repaintEditorPanel();
            }
        }
    }
    
    public WidgetPropertyChangeListener( RFDynHUDEditor editor )
    {
        this.editor = editor;
    }
}
