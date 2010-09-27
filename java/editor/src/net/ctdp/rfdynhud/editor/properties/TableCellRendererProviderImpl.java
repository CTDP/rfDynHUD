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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.TableCellRendererProvider;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TableCellRendererProviderImpl implements TableCellRendererProvider<Property>
{
    //private final KeyCellRenderer keyRenderer = new KeyCellRenderer();
    private final ReadonlyCellEditor readonlyEditor = new ReadonlyCellEditor();
    //private final BooleanCellEditor booleanEditor = new BooleanCellEditor();
    //private final IntegerCellEditor integerEditor = new IntegerCellEditor();
    //private final FloatCellEditor floatEditor = new FloatCellEditor();
    //private final PosSizeCellEditor posSizeEditor = new PosSizeCellEditor();
    //private final StringCellEditor stringEditor = new StringCellEditor();
    //private final EnumCellEditor enumEditor = new EnumCellEditor();
    //private final ArrayCellEditor arrayEditor = new ArrayCellEditor();
    //private final ListCellEditor listEditor = new ListCellEditor();
    //private final FontCellEditor fontEditor = new FontCellEditor();
    //private final ColorCellEditor colorEditor = new ColorCellEditor();
    //private final ImageNameCellEditor imageEditor = new ImageNameCellEditor();
    //private final BorderCellEditor borderEditor = new BorderCellEditor();
    //private final BackgroundCellEditor backgroundEditor = new BackgroundCellEditor();
    
    @Override
    public TableCellRenderer getDataCellRenderer( HierarchicalTable<Property> table, int row, int index, Property property )
    {
        //if ( column == ( (EditorTableModel)getModel() ).getFirstNonExpanderColumn() )
        //    return ( super.getDataCellRenderer( row, column ) );
        
        if ( property.isReadOnly() )
            return ( readonlyEditor );
            //return ( new ReadonlyCellEditor() );
        
        PropertyEditorType editorType = property.getEditorType();
        
        TableCellRenderer result = (TableCellRenderer)__PropsPrivilegedAccess.getCellRenderer( property );
        
        if ( result == null )
        {
            switch ( editorType )
            {
                case BOOLEAN:
                    //result = booleanEditor;
                    result = new BooleanCellEditor();
                    break;
                case INTEGER:
                    //result = integerEditor;
                    result = new IntegerCellEditor();
                    break;
                case FLOAT:
                    //result = floatEditor;
                    result = new FloatCellEditor();
                    break;
                case POS_SIZE:
                    //result = posSizeEditor;
                    result = new PosSizeCellEditor();
                    break;
                case STRING:
                    //result = stringEditor;
                    result = new StringCellEditor();
                    break;
                case ENUM:
                    //result = enumEditor;
                    result = new EnumCellEditor();
                    break;
                case ARRAY:
                    //result = arrayEditor;
                    result = new ArrayCellEditor();
                    break;
                case LIST:
                    //result = listEditor;
                    result = new ListCellEditor();
                    break;
                case FONT:
                    //result = fontEditor;
                    result = new FontCellEditor();
                    break;
                case COLOR:
                    //result = colorEditor;
                    result = new ColorCellEditor();
                    break;
                case IMAGE:
                    //result = imageEditor;
                    result = new ImageNameCellEditor();
                    break;
                case BORDER:
                    //result = borderEditor;
                    result = new BorderCellEditor();
                    break;
                case BACKGROUND:
                    //result = backgroundEditor;
                    result = new BackgroundCellEditor();
                    break;
            }
            
            __PropsPrivilegedAccess.setCellRenderer( result, property );
        }
        
        return ( result );
    }
    
    @Override
    public TableCellEditor getDataCellEditor( HierarchicalTable<Property> table, int row, int index, Property property )
    {
        //if ( column == ( (EditorTableModel)getModel() ).getFirstNonExpanderColumn() )
        //    return ( super.getDataCellEditor( row, column ) );
        
        if ( property.isReadOnly() )
            return ( readonlyEditor );
            //return ( new ReadonlyCellEditor() );
        
        PropertyEditorType editorType = property.getEditorType();
        
        TableCellEditor result = (TableCellEditor)__PropsPrivilegedAccess.getCellEditor( property );
        
        if ( result == null )
        {
            switch ( editorType )
            {
                case BOOLEAN:
                    //result = booleanEditor;
                    result = new BooleanCellEditor();
                    break;
                case INTEGER:
                    //result = integerEditor;
                    result = new IntegerCellEditor();
                    break;
                case FLOAT:
                    //result = floatEditor;
                    result = new FloatCellEditor();
                    break;
                case POS_SIZE:
                    //result = posSizeEditor;
                    result = new PosSizeCellEditor();
                    break;
                case STRING:
                    //result = stringEditor;
                    result = new StringCellEditor();
                    break;
                case ENUM:
                    //result = enumEditor;
                    result = new EnumCellEditor();
                    break;
                case ARRAY:
                    //result = arrayEditor;
                    result = new ArrayCellEditor();
                    break;
                case LIST:
                    //result = listEditor;
                    result = new ListCellEditor();
                    break;
                case FONT:
                    //result = fontEditor;
                    result = new FontCellEditor();
                    break;
                case COLOR:
                    //result = colorEditor;
                    result = new ColorCellEditor();
                    break;
                case IMAGE:
                    //result = imageEditor;
                    result = new ImageNameCellEditor();
                    break;
                case BORDER:
                    //result = borderEditor;
                    result = new BorderCellEditor();
                    break;
                case BACKGROUND:
                    //result = backgroundEditor;
                    result = new BackgroundCellEditor();
                    break;
            }
            
            __PropsPrivilegedAccess.setCellEditor( result, property );
        }
        
        return ( result );
    }
}
