package net.ctdp.rfdynhud.editor.input;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class DeviceComponentEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{
    private static final long serialVersionUID = 8648826631392438892L;
    
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        String str = ( value == null ) ? "" : String.valueOf( value );
        
        JLabel label = new JLabel( str );
        
        return ( label );
    }
    
    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        return ( null );
    }
    
    public Object getCellEditorValue()
    {
        return ( null );
    }
    
    @Override
    public boolean isCellEditable( EventObject e )
    {
        return ( false );
    }
}
