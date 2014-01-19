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
package net.ctdp.rfdynhud.editor.util;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Cell renderer with a small padding.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class PaddingCellRenderer extends DefaultTableCellRenderer
{
    private static final long serialVersionUID = -3522834495326860299L;
    
    public static final int PADDING = 3;
    private Font font = null;
    
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
        
        setBorder( new EmptyBorder( 0, PADDING, 0, PADDING ) );
        if ( font == null )
            font = getFont().deriveFont( Font.BOLD );
        setFont( font );
        
        return ( this );
    }
}
