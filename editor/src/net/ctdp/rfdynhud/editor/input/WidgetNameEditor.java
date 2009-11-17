package net.ctdp.rfdynhud.editor.input;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class WidgetNameEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{
    private static final long serialVersionUID = 5993944777559988223L;
    
    private JTextField textField = null;
    
    private void initTextField()
    {
        if ( textField != null )
            return;
        
        textField = new JTextField();
    }
    
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        initTextField();
        
        String str = (String)value;
        
        if ( str == null )
            textField.setText( "" );
        else
            textField.setText( str );
        
        textField.setEnabled( table.getModel().isCellEditable( row, column ) );
        
        return ( textField );
    }
    
    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        initTextField();
        
        String str = (String)value;
        
        if ( str == null )
            textField.setText( "" );
        else
            textField.setText( str );
        
        return ( textField );
    }
    
    public Object getCellEditorValue()
    {
        return ( textField.getText() );
    }
}
