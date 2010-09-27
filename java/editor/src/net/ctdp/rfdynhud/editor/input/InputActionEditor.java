/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.editor.input;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
    
    @Override
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
    
    @Override
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
        
        combo.addPopupMenuListener( new PopupMenuListener()
        {
            @Override
            public void popupMenuWillBecomeVisible( PopupMenuEvent e )
            {
            }
            
            @Override
            public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
            {
                stopCellEditing();
            }
            
            @Override
            public void popupMenuCanceled( PopupMenuEvent e )
            {
            }
        } );
        
        return ( combo );
    }
    
    @Override
    public Object getCellEditorValue()
    {
        return ( combo.getSelectedItem() );
    }
    
    public InputActionEditor( InputBindingsGUI gui )
    {
        this.gui = gui;
    }
}
