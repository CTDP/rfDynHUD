package net.ctdp.rfdynhud.editor.properties;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.hiergrid.ValueAccessor;


/**
 * 
 * @author Marvin Froehlich
 */
public class EditorTable extends HierarchicalTable
{
    private static final long serialVersionUID = 3238244155847132182L;
    
    private final RFDynHUDEditor editor;
    
    public final RFDynHUDEditor getRFDynHUDEditor()
    {
        return ( editor );
    }
    
    /*
    private final KeyCellRenderer keyRenderer = new KeyCellRenderer();
    private final ReadonlyCellEditor readonlyEditor = new ReadonlyCellEditor();
    private final BooleanCellEditor booleanEditor = new BooleanCellEditor();
    private final IntegerCellEditor integerEditor = new IntegerCellEditor();
    private final FloatCellEditor floatEditor = new FloatCellEditor();
    private final PosSizeCellEditor posSizeEditor = new PosSizeCellEditor();
    private final StringCellEditor stringEditor = new StringCellEditor();
    private final EnumCellEditor enumEditor = new EnumCellEditor();
    private final FontCellEditor fontEditor = new FontCellEditor();
    private final ColorCellEditor colorEditor = new ColorCellEditor();
    private final ImageNameCellEditor imageEditor = new ImageNameCellEditor();
    private final BorderCellEditor borderEditor = new BorderCellEditor();
    */
    
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
    
    /*
    public EditorTable( TableModel model )
    {
        super( model );
        
        this.setTableHeader( null );
    }
    */
    
    private static final boolean needsAreaClear( Property p )
    {
        if ( p.getKey().equals( "x" ) )
            return ( true );
        
        if ( p.getKey().equals( "y" ) )
            return ( true );
        
        if ( p.getKey().equals( "width" ) )
            return ( true );
        
        if ( p.getKey().equals( "height" ) )
            return ( true );
        
        if ( p.getKey().equals( "initialVisibility" ) )
            return ( true );
        
        return ( false );
    }
    
    private static ValueAccessor acc = new ValueAccessor()
    {
        @Override
        public void setValue( JTable table, TableModel model, Object prop, int index, Object newValue )
        {
            Object oldValue = getValue( table, model, prop, index );
            if ( ( oldValue == newValue ) || oldValue.equals( newValue ) )
                return;
            
            if ( EditorTable.needsAreaClear( (Property)prop ) )
                ( (EditorTable)table ).editor.getEditorPanel().clearSelectedWidget();
            
            ( (Property)prop ).setValue( newValue );
            
            ( (EditorTable)table ).editor.getEditorPanel().repaint();
            ( (EditorTable)table ).editor.setDirtyFlag();
        }
        
        @Override
        public Object getValue( JTable table, TableModel model, Object prop, int index )
        {
            if ( index == 0 )
                return ( ( (Property)prop ).getKeyForDisplay() );
            
            return ( ( (Property)prop ).getValue() );
        }
    };
    
    public EditorTable( RFDynHUDEditor editor, FlaggedList data )
    {
        super( data, acc, 2 );
        
        this.editor = editor;
        
        this.setTableHeader( null );
    }
}
