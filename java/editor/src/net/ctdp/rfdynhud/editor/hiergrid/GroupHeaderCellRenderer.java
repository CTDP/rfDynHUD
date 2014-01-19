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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * @author Marvin Froehlich
 */
public class GroupHeaderCellRenderer extends KeyValueCellRenderer<Object, GroupHeaderRenderComponent>
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
    protected void paintBorder( GroupHeaderRenderComponent c, Graphics g, int x, int y, int width, int height, int row, int column, Color borderColor )
    {
        if ( row > 0 )
        {
            HierarchicalTable<?> table = (HierarchicalTable<?>)c.getParent().getParent();
            int level = Math.min( table.getModel().getLevel( row - 1 ), table.getModel().getLevel( row ) );
            
            int indent = table.getStyle().getLevelIndentation();
            int offsetX = table.getStyle().getIndentHeaders() ? level * indent : 0;
            
            super.paintBorder( c, g, x + offsetX, y, width - offsetX, height, row, column, borderColor );
            
            level = table.getModel().getLevel( row );
            
            offsetX = table.getStyle().getIndentHeaders() ? level * indent : 0;
            
            if ( ( level > 0  ) && table.getStyle().getIndentHeaders() )
            {
                Color oldColor = g.getColor();
                
                g.setColor( borderColor );
                
                g.drawLine( x + offsetX, y, x + offsetX, y + height );
                
                g.setColor( oldColor );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareComponent( GroupHeaderRenderComponent component, HierarchicalTable<Object> table, Object property, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean forEditor )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column, forEditor );
        
        HierarchicalGridStyle style = ( (HierarchicalTable<?>)table ).getStyle();
        
        component.setBackground( style.getGroupHeaderBackgroundColor() );
        component.setForeground( style.getGroupHeaderFontColor() );
        
        HierarchicalTableModel<?> tm = table.getModel();
        
        component.setLevel( tm.getLevel( row ), style.getLevelIndentation() );
        component.setLastInGroup( tm.getLastInGroup( row ) );
        component.setExpanded( tm.getValueAt( row, 0 ) == Boolean.TRUE );
        
        component.setFont( style.getGroupHeaderFont() );
        
        //component.setText( String.valueOf( value ) );
        if ( value == null )
            component.setCaption( null );
        else if ( value instanceof GridItemsContainer<?> )
            component.setCaption( ( (GridItemsContainer<?>)value ).getNameForGrid() );
        else
            component.setCaption( String.valueOf( value ) );
    }
    
    @Override
    public final Object getCellEditorValue()
    {
        return ( null );
    }
    
    public GroupHeaderCellRenderer()
    {
        super( true, new GroupHeaderRenderComponent() );
    }
}
