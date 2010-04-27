package net.ctdp.rfdynhud.editor.input;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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
    
    private final InputBindingsGUI gui;
    
    private static InputAction[] knownActions = KnownInputActions.getAll();
    
    private JTable lastTable = null;
    private JComboBox combo = null;
    
    private void initCombo()
    {
        if ( combo != null )
            return;
        
        combo = new JComboBox();
        
        combo.addItem( "[No Action]" );
        
        for ( InputAction action : knownActions )
        {
            combo.addItem( action );
        }
    }
    
    public JComboBox getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        initCombo();
        
        combo.setSelectedIndex( 0 );
        
        if ( value instanceof InputAction )
        {
            InputAction action = (InputAction)value;
            
            for ( int i = 0; i < knownActions.length; i++ )
            {
                if ( action == knownActions[i] )
                {
                    combo.setSelectedIndex( i + 1 );
                    break;
                }
            }
        }
        
        this.lastTable = table;
        
        return ( combo );
    }
    
    private void showInputActionDoc( Object value )
    {
        if ( value instanceof InputAction )
        {
            gui.showInputActionDoc( (InputAction)value );
        }
        else
        {
            gui.showInputActionDoc( null );
        }
    }
    
    public JComboBox getTableCellEditorComponent( JTable table, Object value, boolean isSelected, final int row, final int column )
    {
        showInputActionDoc( value );
        
        final JComboBox combo = getTableCellRendererComponent( table, value, isSelected, false, row, column );
        
        combo.addItemListener( new ItemListener()
        {
            @Override
            public void itemStateChanged( ItemEvent e )
            {
                if ( e.getStateChange() == ItemEvent.SELECTED )
                {
                    lastTable.getModel().setValueAt( e.getItem(), row, column );
                    stopCellEditing();
                    combo.removeItemListener( this );
                    
                    showInputActionDoc( e.getItem() );
                }
            }
        } );
        
        return ( combo );
    }
    
    public Object getCellEditorValue()
    {
        return ( combo.getSelectedItem() );
    }
    
    public InputActionEditor( InputBindingsGUI gui )
    {
        this.gui = gui;
    }
}
