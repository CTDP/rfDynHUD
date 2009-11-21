package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class StringCellEditor extends JTextField implements TableCellEditor
{
    private static final long serialVersionUID = -4635690306614521152L;
    
    public void addCellEditorListener( CellEditorListener l )
    {
    }
    
    public void removeCellEditorListener( CellEditorListener l )
    {
    }
    
    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        this.setText( String.valueOf( value ) );
        
        return ( this );
    }
    
    public Object getCellEditorValue()
    {
        return ( this.getText() );
    }
    
    public boolean isCellEditable( EventObject anEvent )
    {
        return false;
    }
    
    public boolean shouldSelectCell( EventObject anEvent )
    {
        return false;
    }
    
    public void cancelCellEditing()
    {
    }
    
    public boolean stopCellEditing()
    {
        return false;
    }
}
