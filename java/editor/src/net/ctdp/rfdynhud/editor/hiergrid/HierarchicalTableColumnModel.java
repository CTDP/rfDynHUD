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

import java.awt.Image;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * @author Marvin Froehlich (CTDP) (aka Qudus)
 */
public class HierarchicalTableColumnModel extends DefaultTableColumnModel
{
    private static final long serialVersionUID = 5238687005871052922L;
    
    private HierarchicalTableModel tableModel;
    
    private Image minusImage;
    private Image plusImage;
    
    public void init()
    {
        for ( int i = getColumnCount() - 1; i >= 0; i-- )
            removeColumn( getColumn( i ) );
        
        TableColumn column;
        
        if ( tableModel.hasExpandableItems() )
        {
            column = new TableColumn( 0, 18, new ExpanderColumnCellRenderer( minusImage, plusImage ), null );
            column.setMinWidth( 18 );
            column.setMaxWidth( 18 );
            column.setHeaderValue( tableModel.getColumnName( 0 ) );
            column.setResizable( false );
            
            addColumn( column );
        }
        
        column = new TableColumn( 1, 20000, new KeyCellRenderer(), null );
        //column = new TableColumn( 1, 20000, null, null );
        column.setHeaderValue( tableModel.getColumnName( 1 ) );
        addColumn( column );
        
        //column = new TableColumn( 2, 20000, new StringKeyValueCellRenderer(), null );
        column = new TableColumn( 2, 20000, null, null );
        column.setHeaderValue( tableModel.getColumnName( 2 ) );
        addColumn( column );
    }
    
    public HierarchicalTableColumnModel( HierarchicalTableModel tableModel, Image minusImage, Image plusImage )
    {
        super();
        
        this.tableModel = tableModel;
        this.minusImage = minusImage;
        this.plusImage = plusImage;
        
        init();
    }
}
