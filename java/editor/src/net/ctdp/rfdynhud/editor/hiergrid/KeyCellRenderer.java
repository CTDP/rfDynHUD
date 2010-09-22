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

/**
 * @param <P> the property type
 * 
 * @author Marvin Froehlich
 */
public class KeyCellRenderer<P extends Object> extends KeyValueCellRenderer<P, KeyRenderLabel>
{
    private static final long serialVersionUID = 663331747917701155L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareComponent( KeyRenderLabel component, HierarchicalTable<P> table, P property, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.prepareComponent( component, table, property, value, isSelected, hasFocus, row, column );
        
        HierarchicalGridStyle style = table.getStyle();
        HierarchicalTableModel<P> tm = table.getModel();
        
        component.setLevel( tm.getLevel( row ) );
        component.setLastInGroup( tm.getLastInGroup( row ) );
        
        component.setForeground( style.getKeyCellFontColor() );
        //if ( !component.getFont().isBold() )
        //    component.setFont( component.getFont().deriveFont( component.getFont().getStyle() | java.awt.Font.BOLD ) );
        component.setFont( style.getKeyCellFont() );
        
        component.setText( String.valueOf( value ) );
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
    
    public KeyCellRenderer()
    {
        super( true, new KeyRenderLabel() );
    }
}
