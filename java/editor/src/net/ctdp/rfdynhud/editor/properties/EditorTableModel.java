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

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.hiergrid.GridItemsContainer;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.util.Tools;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorTableModel extends HierarchicalTableModel<Property>
{
    private static final long serialVersionUID = -5111097521627270775L;
    
    @Override
    protected void setValueImpl( HierarchicalTable<Property> table, Property property, int index, Object newValue )
    {
        Object oldValue = getValueImpl( table, property, index );
        if ( Tools.objectsEqual( oldValue, newValue ) )
            return;
        
        final RFDynHUDEditor editor = ( (EditorTable)table ).editor;
        
        if ( editor != null )
        {
            if ( WidgetPropertyChangeListener.needsAreaClear( property ) )
                editor.getEditorPanel().clearSelectedWidgetRegion();
        }
        
        property.setValue( newValue );
        ( (EditorTable)table ).propsEditor.invokeChangeListeners( property, oldValue, newValue, table.getSelectedRow(), table.getSelectedColumn() );
        
        if ( ( editor != null ) && ( ( property.getWidget() != null ) || __PropsPrivilegedAccess.isWidgetsConfigProperty( property ) ) )
        {
            editor.onWidgetChanged( property.getWidget(), property.getName() );
            editor.onWidgetSelected( property.getWidget(), false ); // refresh properties editor in case the propertis toggles the display of other properties
        }
    }
    
    @Override
    protected Object getValueImpl( HierarchicalTable<Property> table, Property property, int index )
    {
        if ( index == 0 )
            return ( property.getNameForDisplay() );
        
        return ( property.getValue() );
    }
    
    public EditorTableModel( GridItemsContainer<Property> data, int columnCount )
    {
        super( data, columnCount );
    }
}
