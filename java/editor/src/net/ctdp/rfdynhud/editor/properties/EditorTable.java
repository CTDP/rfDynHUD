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

import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableColumnModel;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.ValueAccessor;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.util.Tools;


/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorTable extends HierarchicalTable
{
    private static final long serialVersionUID = 3238244155847132182L;
    
    private final RFDynHUDEditor editor;
    private final PropertiesEditor propsEditor;
    
    private final ArrayList<PropertySelectionListener> selectionListeners = new ArrayList<PropertySelectionListener>();
    
    public final RFDynHUDEditor getRFDynHUDEditor()
    {
        return ( editor );
    }
    
    //private final KeyCellRenderer keyRenderer = new KeyCellRenderer();
    //private final ReadonlyCellEditor readonlyEditor = new ReadonlyCellEditor();
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
    
    public void addPropertySelectionListener( PropertySelectionListener l )
    {
        selectionListeners.add( l );
    }
    
    public void removePropertySelectionListener( PropertySelectionListener l )
    {
        selectionListeners.remove( l );
    }
    
    @Override
    public TableCellRenderer getDataCellRenderer( int row, int column )
    {
        if ( column == ( (HierarchicalTableModel)getModel() ).getFirstNonExpanderColumn() )
            //return ( keyRenderer );
            return ( new KeyCellRenderer() );
        
        //Property property = ( (PropertiesEditor)getModel() ).getProperty( row );
        Property property = (Property)( (HierarchicalTableModel)getModel() ).getRowAt( row );
        
        if ( property.isReadOnly() )
            //return ( readonlyEditor );
            return ( new ReadonlyCellEditor() );
        
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
                default:
                    result = super.getCellRenderer( row, column );
                    break;
            }
            
            __PropsPrivilegedAccess.setCellRenderer( result, property );
        }
        
        return ( result );
    }
    
    @Override
    public TableCellEditor getDataCellEditor( int row, int column )
    {
        if ( column == ( (HierarchicalTableModel)getModel() ).getFirstNonExpanderColumn() )
            //return ( keyRenderer );
            return ( new KeyCellRenderer() );
        
        //Property property = ( (PropertiesEditor)getModel() ).getProperty( row );
        Property property = (Property)( (HierarchicalTableModel)getModel() ).getRowAt( row );
        
        if ( property.isReadOnly() )
            //return ( readonlyEditor );
            return ( new ReadonlyCellEditor() );
        
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
                default:
                    result = super.getCellEditor( row, column );
                    break;
            }
            
            __PropsPrivilegedAccess.setCellEditor( result, property );
        }
        
        return ( result );
    }
    
    public void apply()
    {
        ( (HierarchicalTableModel)this.getModel() ).apply( null, (HierarchicalTableColumnModel)this.getColumnModel() );
    }
    
    private static ValueAccessor acc = new ValueAccessor()
    {
        @Override
        public void setValue( JTable table, TableModel model, Object prop, int index, Object newValue )
        {
            Property property = (Property)prop;
            
            Object oldValue = getValue( table, model, prop, index );
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
            }
        }
        
        @Override
        public Object getValue( JTable table, TableModel model, Object prop, int index )
        {
            if ( index == 0 )
                return ( ( (Property)prop ).getNameForDisplay() );
            
            return ( ( (Property)prop ).getValue() );
        }
    };
    
    public JScrollPane createScrollPane()
    {
        JScrollPane scrollPane = new JScrollPane( this );
        scrollPane.getViewport().setBackground( java.awt.Color.WHITE );
        scrollPane.getVerticalScrollBar().setUnitIncrement( 10 );
        
        return ( scrollPane );
    }
    
    public EditorTable( RFDynHUDEditor editor, PropertiesEditor propsEditor )
    {
        super( propsEditor.getPropertiesList(), acc, 2 );
        
        this.editor = editor;
        this.propsEditor = propsEditor;
        
        this.setTableHeader( null );
        this.setRowHeight( 20 );
        
        getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            @Override
            public void valueChanged( ListSelectionEvent e )
            {
                if ( !e.getValueIsAdjusting() && ( selectionListeners != null ) )
                {
                    final EditorTable editorTable = EditorTable.this;
                    
                    HierarchicalTableModel m = (HierarchicalTableModel)editorTable.getModel();
                    
                    final Property property;
                    if ( m.isDataRow( editorTable.getSelectedRow() ) )
                        property = (Property)m.getRowAt( editorTable.getSelectedRow() );
                    else
                        property = null;
                    
                    for ( int i = 0; i < selectionListeners.size(); i++ )
                    {
                        selectionListeners.get( i ).onPropertySelected( property, editorTable.getSelectedRow() );
                    }
                }
            }
        } );

    }
}
