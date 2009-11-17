package net.ctdp.rfdynhud.editor.input;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.KnownInputActions;

public class InputActionEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{
    private static final long serialVersionUID = 5993944777559988223L;
    
    private static InputAction[] knownActions = KnownInputActions.getAll();
    
    private JComboBox combo = null;
    
    private void initCombo()
    {
        if ( combo != null )
            return;
        
        combo = new JComboBox();
        
        for ( InputAction action : knownActions )
        {
            combo.addItem( action );
        }
    }
    
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        initCombo();
        
        InputAction action = (InputAction)value;
        
        combo.setSelectedIndex( -1 );
        
        if ( action != null )
        {
            for ( int i = 0; i < knownActions.length; i++ )
            {
                if ( action == knownActions[i] )
                {
                    combo.setSelectedIndex( i );
                    break;
                }
            }
        }
        
        return ( combo );
    }
    
    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        initCombo();
        
        InputAction action = (InputAction)value;
        
        combo.setSelectedIndex( -1 );
        
        if ( action != null )
        {
            for ( int i = 0; i < knownActions.length; i++ )
            {
                if ( action == knownActions[i] )
                {
                    combo.setSelectedIndex( i );
                    break;
                }
            }
        }
        
        return ( combo );
    }
    
    public Object getCellEditorValue()
    {
        return ( combo.getSelectedItem() );
    }
}
