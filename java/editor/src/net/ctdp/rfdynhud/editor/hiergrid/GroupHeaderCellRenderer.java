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
package net.ctdp.rfdynhud.editor.hiergrid;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.SwingConstants;

/**
 * @author Marvin Froehlich
 */
public class GroupHeaderCellRenderer extends KeyValueCellRenderer<Object, GroupHeaderRenderLabel>
{
    private static final long serialVersionUID = -1986974044855186348L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Insets getBorderInsets( Component c )
    {
        Insets insets = super.getBorderInsets( c );
        
        insets.left = 2;
        
        return( insets );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareComponent( GroupHeaderRenderLabel component, HierarchicalTable<Object> table, Object property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        HierarchicalGridStyle style = ( (HierarchicalTable<?>)table ).getStyle();
        
        component.setBackground( style.getGroupHeaderBackgroundColor() );
        component.setForeground( style.getGroupHeaderFontColor() );
        
        HierarchicalTableModel<?> tm = (HierarchicalTableModel<?>)table.getModel();
        
        component.setLevel( tm.getLevel( row ), style.getLevelIndentation() );
        component.setLastInGroup( tm.getLastInGroup( row ) );
        component.setExpanded( tm.getValueAt( row, 0 ) == Boolean.TRUE );
        
        component.setFont( style.getGroupHeaderFont() );
        
        //component.setText( String.valueOf( value ) );
        if ( value == null )
            component.setText( null );
        else if ( value instanceof GridItemsContainer<?> )
            component.setText( ( (GridItemsContainer<?>)value ).getNameForGrid() );
        else
            component.setText( String.valueOf( value ) );
        component.setHorizontalAlignment( SwingConstants.CENTER );
    }
    
    @Override
    protected Object getCellEditorValueImpl() throws Throwable
    {
        return ( getComponent().toString() );
    }
    
    @Override
    protected void applyOldValue( Object oldValue )
    {
    }
    
    public GroupHeaderCellRenderer()
    {
        super( true, new GroupHeaderRenderLabel() );
    }
}
