package net.ctdp.rfdynhud.editor.properties;

import java.util.EventObject;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;

/**
 * 
 * @author Marvin Froehlich
 */
public class ReadonlyCellEditor extends KeyValueCellRenderer<JLabel> implements TableCellRenderer, TableCellEditor
{
    private static final long serialVersionUID = 7979822630367678241L;
    
    private final JLabel label = new JLabel();
    
    @Override
    //public JLabel getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JLabel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( label );
        
        super.prepareComponent( label, table, value, isSelected, hasFocus, row, column );
        
        if ( isSelected )
            label.setBackground( table.getSelectionBackground() );
        else
            label.setBackground( table.getBackground() );
        //label.setForeground( table.getForeground() );
        label.setForeground( java.awt.Color.LIGHT_GRAY );
        label.setFont( table.getFont().deriveFont( java.awt.Font.ITALIC ) );
        
        label.setText( String.valueOf( value ) );
        
        //return ( label );
    }
    
    public JLabel getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        //JLabel label = getTableCellRendererComponent( table, value, isSelected, true, row, column );
        prepareComponent( label, table, value, isSelected, true, row, column );
        
        if ( isSelected )
            label.setBackground( table.getSelectionBackground() );
        else
            label.setBackground( table.getBackground() );
        //label.setForeground( table.getSelectionForeground() );
        label.setForeground( java.awt.Color.LIGHT_GRAY );
        
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
    
    public ReadonlyCellEditor()
    {
        super( false, null );
    }
}
