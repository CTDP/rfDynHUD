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
import net.ctdp.rfdynhud.util.Tools;


/**
 * 
 * @author Marvin Froehlich
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
        
        switch ( editorType )
        {
            case BOOLEAN:
                //return ( booleanEditor );
                return ( new BooleanCellEditor() );
            case INTEGER:
                //return ( integerEditor );
                return ( new IntegerCellEditor() );
            case FLOAT:
                //return ( floatEditor );
                return ( new FloatCellEditor() );
            case POS_SIZE:
                //return ( posSizeEditor );
                return ( new PosSizeCellEditor() );
            case STRING:
                //return ( stringEditor );
                return ( new StringCellEditor() );
            case ENUM:
                //return ( enumEditor );
                return ( new EnumCellEditor() );
            case ARRAY:
                //return ( arrayEditor );
                return ( new ArrayCellEditor() );
            case LIST:
                //return ( listEditor );
                return ( new ListCellEditor() );
            case FONT:
                //return ( fontEditor );
                return ( new FontCellEditor() );
            case COLOR:
                //return ( colorEditor );
                return ( new ColorCellEditor() );
            case IMAGE:
                //return ( imageEditor );
                return ( new ImageNameCellEditor() );
            case BORDER:
                //return ( borderEditor );
                return ( new BorderCellEditor() );
        }
        
        return ( super.getCellRenderer( row, column ) );
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
        
        switch ( editorType )
        {
            case BOOLEAN:
                //return ( booleanEditor );
                return ( new BooleanCellEditor() );
            case INTEGER:
                //return ( integerEditor );
                return ( new IntegerCellEditor() );
            case FLOAT:
                //return ( floatEditor );
                return ( new FloatCellEditor() );
            case POS_SIZE:
                //return ( posSizeEditor );
                return ( new PosSizeCellEditor() );
            case STRING:
                //return ( stringEditor );
                return ( new StringCellEditor() );
            case ENUM:
                //return ( enumEditor );
                return ( new EnumCellEditor() );
            case ARRAY:
                //return ( arrayEditor );
                return ( new ArrayCellEditor() );
            case LIST:
                //return ( listEditor );
                return ( new ListCellEditor() );
            case FONT:
                //return ( fontEditor );
                return ( new FontCellEditor() );
            case COLOR:
                //return ( colorEditor );
                return ( new ColorCellEditor() );
            case IMAGE:
                //return ( imageEditor );
                return ( new ImageNameCellEditor() );
            case BORDER:
                //return ( borderEditor );
                return ( new BorderCellEditor() );
        }
        
        return ( super.getCellEditor( row, column ) );
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
            
            if ( ( editor != null ) && ( property.getWidget() != null ) )
            {
                editor.onWidgetChanged( property.getWidget(), property.getPropertyName() );
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
