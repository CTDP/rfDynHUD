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
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * @param <P> the proerty type
 * @param <C> the render component type
 * 
 * @author Marvin Froehlich
 */
public abstract class KeyValueCellRenderer<P extends Object, C extends JComponent> extends AbstractCellEditor implements Border, TableCellRenderer
{
    private static final long serialVersionUID = 6279484353820779292L;
    
    private final boolean isKeyRenderer;
    private C component;
    
    private Color gridColor = Color.BLACK;
    private int level = 0;
    private int row = 0;
    
    private Object oldValue = null;
    
    public void setComponent( C component )
    {
        this.component = component;
        if ( component != null )
            this.component.setOpaque( true );
    }
    
    public final C getComponent()
    {
        return ( component );
    }
    
    @Override
    public boolean isBorderOpaque()
    {
        return( false );
    }
    
    @Override
    public Insets getBorderInsets( Component c )
    {
        if ( isKeyRenderer && ( level > 0 ) )
            return ( new Insets( ( row == 0 ) ? 0 : 1, 14 * level, 0, 0 ) );
        
        return ( new Insets( ( row == 0 ) ? 0 : 1, 2, 0, 0 ) );
    }
    
    @Override
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height )
    {
        Color oldColor = g.getColor();
        
        g.setColor( gridColor );
        
        if ( row > 0 )
            g.drawLine( x, y, x + width, y );
        
        g.setColor( oldColor );
    }
    
    /**
     * 
     * @param component
     * @param table
     * @param property
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     */
    protected void prepareComponent( C component, HierarchicalTable<P> table, P property, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        HierarchicalGridStyle style = table.getStyle();
        
        component.setBorder( this );
        
        if ( isSelected )
        {
            component.setForeground( table.getSelectionForeground() );
            component.setBackground( table.getSelectionBackground() );
        }
        else
        {
            component.setForeground( table.getForeground() );
            component.setBackground( table.getBackground() );
        }
        
        component.setFont( style.getValueCellFont() );
        
        oldValue = value;
    }
    
    @SuppressWarnings( "unchecked" )
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        HierarchicalTableModel<?> model = (HierarchicalTableModel<?>)table.getModel();
        
        this.level = model.getLevel( row );
        this.row = row;
        this.gridColor = table.getGridColor();
        
        if ( model.isDataRow( row ) )
            prepareComponent( component, (HierarchicalTable<P>)table, (P)model.getRowAt( row ), value, isSelected, hasFocus, row, column );
        else
            prepareComponent( component, (HierarchicalTable<P>)table, null, value, isSelected, hasFocus, row, column );
        
        return ( component );
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
    
    protected void finalizeEdit( JTable table )
    {
        if ( table == null )
            return;
        
        int selRow = row; //table.getEditingRow();
        int selCol = table.getEditingColumn();
        table.editingStopped( null );
        table.setValueAt( getCellEditorValue(), selRow, selCol );
        table.getSelectionModel().setSelectionInterval( selRow, selRow );
    }
    
    protected KeyValueCellRenderer( boolean isKeyRenderer, C component )
    {
        this.isKeyRenderer = isKeyRenderer;
        
        setComponent( component );
    }
    
    protected KeyValueCellRenderer( C component )
    {
        this( false, component );
    }
}
