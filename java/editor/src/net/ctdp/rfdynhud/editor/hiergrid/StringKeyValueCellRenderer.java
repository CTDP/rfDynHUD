package net.ctdp.rfdynhud.editor.hiergrid;

import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class StringKeyValueCellRenderer extends KeyValueCellRenderer< JLabel >
{
    private static final long serialVersionUID = -7542592787633532420L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareComponent( JLabel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.prepareComponent( component, table, value, isSelected, hasFocus, row, column );
        
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
    
    public StringKeyValueCellRenderer()
    {
        super( new JLabel() );
    }
}
