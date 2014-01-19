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

import java.awt.Image;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * 
 * @author Marvin Froehlich
 *
 * @param <P>
 */
public class HierarchicalTableColumnModel<P extends Object> extends DefaultTableColumnModel
{
    private static final long serialVersionUID = 5238687005871052922L;
    
    private final HierarchicalTable<P> table;
    
    public void init()
    {
        HierarchicalTableModel<P> tableModel = table.getModel();
        
        this.tableColumns.clear();
        
        if ( tableModel == null )
            return;
        
        TableColumn column;
        
        int c = 0;
        
        if ( tableModel.hasExpandableItems() )
        {
            Image minusImage = table.getStyle().getExpandedImage();
            Image plusImage = table.getStyle().getCollapsedImage();
            
            int w = minusImage.getWidth( null );
            column = new TableColumn( c++, w + 2, new ExpanderColumnCellRenderer( minusImage, plusImage ), null );
            column.setMinWidth( w + 2 );
            column.setMaxWidth( w + 2 );
            column.setHeaderValue( tableModel.getColumnName( 0 ) );
            column.setResizable( false );
            
            addColumn( column );
        }
        
        if ( tableModel.getKeyColumnWidth() >= 0 )
        {
            int kcw = tableModel.getKeyColumnWidth();
            column = new TableColumn( c++, kcw, new KeyCellRenderer<P>(), null );
            column.setMinWidth( kcw );
            column.setMaxWidth( kcw );
            column.setResizable( false );
            column.setHeaderValue( tableModel.getColumnName( 1 ) );
            addColumn( column );
        }
        else
        {
            column = new TableColumn( c++, 20000, new KeyCellRenderer<P>(), null );
            //column = new TableColumn( c++, 20000, null, null );
            column.setHeaderValue( tableModel.getColumnName( 1 ) );
            addColumn( column );
        }
        
        //column = new TableColumn( c++, 20000, new StringKeyValueCellRenderer(), null );
        column = new TableColumn( c++, 20000, null, null );
        column.setHeaderValue( tableModel.getColumnName( 2 ) );
        addColumn( column );
    }
    
    public HierarchicalTableColumnModel( HierarchicalTable<P> table )
    {
        super();
        
        this.table = table;
        
        init();
    }
}
