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
package net.ctdp.rfdynhud.editor.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Cell Renderer for the thumb nail column.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageCellRenderer extends DefaultTableCellRenderer
{
    private static final long serialVersionUID = 8967440958221553904L;
    
    private Image image = null;
    
    @Override
    public void paintComponent( Graphics g )
    {
        //super.paintComponent( g );
        
        g.setColor( getBackground() );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        
        if ( image != null )
        {
            g.drawImage( image, ( getWidth() - image.getWidth( null ) ) / 2, ( getHeight() - image.getHeight( null ) ) / 2, null );
        }
    }
    
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
        
        this.image = (Image)value;
        
        return ( this );
    }
}
