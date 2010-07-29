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

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class KeyCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
{
    private static final long serialVersionUID = 7979822630367678241L;
    
    private static EmptyBorder border = new EmptyBorder( 2, 2, 2, 2 );
    
    private final JLabel label = new JLabel();
    
    public JLabel getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        label.setBackground( table.getBackground() );
        label.setForeground( table.getForeground() );
        label.setFont( table.getFont().deriveFont( java.awt.Font.BOLD ) );
        
        label.setText( String.valueOf( value ) );
        
        label.setBorder( border );
        
        return ( label );
    }
    
    public JLabel getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        JLabel label = getTableCellRendererComponent( table, value, isSelected, true, row, column );
        
        label.setBackground( table.getSelectionBackground() );
        label.setForeground( table.getSelectionForeground() );
        
        return ( label );
    }
    
    @Override
    public boolean isCellEditable( EventObject e )
    {
        return ( false );
    }
    
    public Object getCellEditorValue()
    {
        return ( label.getText() );
    }
    
    public KeyCellRenderer()
    {
        super();
    }
}
