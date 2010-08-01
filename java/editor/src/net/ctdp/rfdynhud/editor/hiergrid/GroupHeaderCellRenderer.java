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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.SwingConstants;

/**
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class GroupHeaderCellRenderer extends KeyValueCellRenderer< GroupHeaderRenderLabel >
{
    private static final long serialVersionUID = -1986974044855186348L;
    
    private static final Color backgroundColor = new Color( 168, 193, 255 );
    private static Font font = null;
    
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
    protected void prepareComponent( GroupHeaderRenderLabel component, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.prepareComponent( component, table, value, isSelected, hasFocus, row, column );
        
        component.setForeground( Color.BLACK );
        component.setBackground( backgroundColor );
        
        HierarchicalTableModel tm = (HierarchicalTableModel)table.getModel();
        
        component.setLevel( tm.getLevel( row ) );
        component.setLastInGroup( tm.getLastInGroup( row ) );
        component.setExpanded( tm.getValueAt( row, 0 ) == Boolean.TRUE );
        
        if ( font == null )
        {
            font = table.getFont().deriveFont( Font.BOLD );
        }
        
        component.setFont( font );
        
        //component.setText( String.valueOf( value ) );
        if ( value == null )
            component.setText( null );
        else
            component.setText( ( (FlaggedList)value ).getName() );
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
