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

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Table implementation for the image selector.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageTable extends JTable
{
    private static final long serialVersionUID = -7773827142361441481L;
    
    private final ImageCellRenderer imageCellRenderer = new ImageCellRenderer();
    private final PaddingCellRenderer nameRenderer = new PaddingCellRenderer();
    private final PaddingCellRenderer sizeRenderer = new PaddingCellRenderer();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ImageTableModel getModel()
    {
        return ( (ImageTableModel)super.getModel() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TableCellRenderer getCellRenderer( int row, int column )
    {
        if ( column == 0 )
            return ( imageCellRenderer );
        
        if ( column == 1 )
            return ( nameRenderer );
        
        if ( column == 2 )
            return ( sizeRenderer );
        
        return ( super.getCellRenderer( row, column ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable( int row, int column )
    {
        return ( false );
    }
    
    public ImageTable( ImageTableModel model )
    {
        super( model );
    }
}
