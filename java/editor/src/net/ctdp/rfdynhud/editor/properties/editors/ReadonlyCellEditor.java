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
package net.ctdp.rfdynhud.editor.properties.editors;

import java.awt.Font;
import java.util.EventObject;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.ValueCellEditor;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ReadonlyCellEditor extends ValueCellEditor<Property, JLabel, JLabel>
{
    private static final long serialVersionUID = 7979822630367678241L;
    
    private final JLabel label = new JLabel();
    
    private static Font font = null;
    
    @Override
    protected void prepareComponent( JLabel component, HierarchicalTable<Property> table, Property property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        if ( isSelected )
            label.setBackground( table.getSelectionBackground() );
        else
            label.setBackground( table.getBackground() );
        //label.setForeground( table.getStyle().getValueCellFontColor() );
        label.setForeground( java.awt.Color.LIGHT_GRAY );
        
        if ( font == null )
            font = table.getStyle().getValueCellFont().deriveFont( Font.ITALIC );
        
        label.setFont( font );
        
        label.setText( String.valueOf( value ) );
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
        super();
        
        label.setBorder( new EmptyBorder( 0, 3, 0, 0 ) );
        
        setComponent( label, label );
    }
}
