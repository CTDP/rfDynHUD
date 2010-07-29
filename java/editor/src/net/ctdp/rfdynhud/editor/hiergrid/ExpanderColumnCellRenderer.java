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
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class ExpanderColumnCellRenderer extends JPanel implements TableCellRenderer
{
    private static final long serialVersionUID = 2232413953910659262L;
    
    private final Image minusImage;
    private final Image plusImage;
    
    private Boolean value = null;
    
    @Override
    protected void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        
        if ( value != null )
        {
            if ( value.booleanValue() )
            {
                int x = ( this.getWidth() - minusImage.getWidth( this ) ) / 2;
                int y = ( this.getHeight() - minusImage.getHeight( this ) ) / 2;
                
                g.drawImage( minusImage, x, y, this );
            }
            else
            {
                int x = ( this.getWidth() - plusImage.getWidth( this ) ) / 2;
                int y = ( this.getHeight() - plusImage.getHeight( this ) ) / 2;
                
                g.drawImage( plusImage, x, y, this );
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
    {
        if ( value == null )
        {
            this.value = null;
        }
        else
        {
            this.value = (Boolean)value;
        }
        
        return( this );
    }
    
    public ExpanderColumnCellRenderer( Image minusImage, Image plusImage )
    {
        this.minusImage = minusImage;
        this.plusImage = plusImage;
        
        setOpaque( true );
    }
}
