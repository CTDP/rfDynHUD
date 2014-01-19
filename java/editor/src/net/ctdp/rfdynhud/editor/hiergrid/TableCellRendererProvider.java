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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * @param <P> the property type
 * 
 * @author Marvin Froehlich
 */
public interface TableCellRendererProvider<P>
{
    /**
     * Gets the {@link TableCellRenderer} for a property value cell.
     * 
     * @param table the table
     * @param row the row
     * @param index the column index (0 is key)
     * @param property the proerty
     * 
     * @return the {@link TableCellRenderer} for a property value cell or <code>null</code> to use the table's default.
     */
    public TableCellRenderer getDataCellRenderer( HierarchicalTable<P> table, int row, int index, P property );
    
    /**
     * Gets the {@link TableCellEditor} for a property value cell.
     * 
     * @param table the table
     * @param row the row
     * @param index the column index (0 is key)
     * @param property the proerty
     * 
     * @return the {@link TableCellEditor} for a property value cell or <code>null</code> to use the table's default.
     */
    public TableCellEditor getDataCellEditor( HierarchicalTable<P> table, int row, int index, P property );
}
