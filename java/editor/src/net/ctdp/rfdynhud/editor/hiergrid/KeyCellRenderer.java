package net.ctdp.rfdynhud.editor.hiergrid;

import javax.swing.JTable;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class KeyCellRenderer extends KeyValueCellRenderer< KeyRenderLabel >
{
    private static final long serialVersionUID = 663331747917701155L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareComponent( KeyRenderLabel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.prepareComponent( component, table, value, isSelected, hasFocus, row, column );
        
        component.setLevel( ( (HierarchicalTableModel)table.getModel() ).getLevel( row ) );
        
        component.setText( String.valueOf( value ) );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( getComponent().toString() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public KeyCellRenderer()
    {
        super( true, new KeyRenderLabel() );
    }
}
