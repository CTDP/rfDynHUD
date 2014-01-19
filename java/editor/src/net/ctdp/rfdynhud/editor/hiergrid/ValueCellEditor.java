/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.editor.hiergrid;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 * 
 * @param <P> the proerty type
 * @param <C> the render component type
 * @param <E> the edit component type
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class ValueCellEditor<P extends Object, C extends JComponent, E extends JComponent> extends KeyValueCellRenderer<P, C>
{
    private static final long serialVersionUID = 3705322407163374264L;
    
    private E editComponent = null;
    
    private HierarchicalTable<P> table = null;
    private P property = null;
    
    private Object oldValue = null;
    private int row = 0;
    
    protected void setComponent( C component, E editComponent )
    {
        super.setComponent( component );
        
        this.editComponent = editComponent;
    }
    
    protected final E getEditorComponent()
    {
        return ( editComponent );
    }
    
    protected final HierarchicalTable<P> getTable()
    {
        return ( table );
    }
    
    protected final P getProperty()
    {
        return ( property );
    }
    
    @Override
    protected void prepareComponent( C component, HierarchicalTable<P> table, P property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        this.table = table;
        this.property = property;
        
        this.oldValue = value;
        this.row = row;
    }
    
    protected void onEditingStarted()
    {
        if ( ( editComponent == null ) || !editComponent.isFocusable() )
            return;
        
        editComponent.requestFocus();
        
        if ( editComponent instanceof JTextComponent )
        {
            ( (JTextComponent)editComponent ).setSelectionStart( 0 );
            ( (JTextComponent)editComponent ).setSelectionEnd( ( (JTextComponent)editComponent ).getText().length() );
        }
        else if ( editComponent instanceof JComboBox )
        {
            ( (JComboBox<?>)editComponent ).showPopup();
        }
        else if ( editComponent instanceof AbstractButton )
        {
            ( (AbstractButton)editComponent ).doClick();
        }
    }
    
    protected abstract Object getCellEditorValueImpl() throws Throwable;
    
    protected abstract void applyOldValue( Object oldValue );
    
    @Override
    public final Object getCellEditorValue()
    {
        try
        {
            return ( getCellEditorValueImpl() );
        }
        catch ( Throwable t )
        {
            applyOldValue( oldValue );
            
            return ( oldValue );
        }
    }
    
    protected void finalizeEdit( boolean cancel )
    {
        if ( ( table == null ) || !table.isEditing() )
            return;
        
        int selRow = row; //table.getEditingRow();
        int selCol = table.getEditingColumn();
        //table.editingStopped( null );
        if ( cancel )
        {
            cancelCellEditing();
            table.setValueAt( getCellEditorValue(), selRow, selCol );
        }
        else
        {
            stopCellEditing();
        }
        table.getSelectionModel().setSelectionInterval( selRow, selRow );
    }
    
    public ValueCellEditor( C component )
    {
        super( false, component );
    }
    
    public ValueCellEditor()
    {
        super( false, null );
    }
}
