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
package net.ctdp.rfdynhud.editor.properties;

import java.util.EventObject;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.KeyValueCellRenderer;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ReadonlyCellEditor extends KeyValueCellRenderer<Property, JLabel> implements TableCellEditor
{
    private static final long serialVersionUID = 7979822630367678241L;
    
    private final JLabel label = new JLabel();
    
    @Override
    //public JLabel getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    protected void prepareComponent( JLabel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        setComponent( label );
        
        super.prepareComponent( label, table, property, value, isSelected, hasFocus, row, column );
        
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
    
    @Override
    public JLabel getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        //Property property = (Property)( (EditorTableModel)table.getModel() ).getRowAt( row );
        Property property = null;
        
        //JLabel label = getTableCellRendererComponent( table, value, isSelected, true, row, column );
        prepareComponent( label, (EditorTable)table, property, value, isSelected, true, row, column );
        
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
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( label.getText() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public ReadonlyCellEditor()
    {
        super( false, null );
    }
}
