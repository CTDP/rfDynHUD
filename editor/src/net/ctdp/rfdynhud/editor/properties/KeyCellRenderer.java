package net.ctdp.rfdynhud.editor.properties;

import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * 
 * @author Marvin Froehlich
 */
public class KeyCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
{
    private static final long serialVersionUID = 7979822630367678241L;
    
    private static EmptyBorder border = new EmptyBorder( 2, 2, 2, 2 );
    
    private final JLabel label = new JLabel();
    
    public JLabel getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        label.setBackground( table.getBackground() );
        label.setForeground( table.getForeground() );
        label.setFont( table.getFont().deriveFont( java.awt.Font.BOLD ) );
        
        label.setText( String.valueOf( value ) );
        
        label.setBorder( border );
        
        return ( label );
    }
    
    public JLabel getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        JLabel label = getTableCellRendererComponent( table, value, isSelected, true, row, column );
        
        label.setBackground( table.getSelectionBackground() );
        label.setForeground( table.getSelectionForeground() );
        
        return ( label );
    }
    
    @Override
    public boolean isCellEditable( EventObject e )
    {
        return ( false );
    }
    
    public Object getCellEditorValue()
    {
        return ( label.getText() );
    }
    
    public KeyCellRenderer()
    {
        super();
    }
}
