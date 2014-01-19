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
package net.ctdp.rfdynhud.editor.input;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class InputBindingsTable extends JTable
{
    private static final long serialVersionUID = -2871793061426881191L;
    
    private final InputBindingsGUI gui;
    
    @Override
    public TableCellEditor getCellEditor( int row, int column )
    {
        if ( column == 0 )
            return ( new WidgetNameEditor() );
        
        if ( column == 1 )
            return ( new InputActionEditor( gui ) );
        
        if ( column == 2 )
            return ( new DeviceComponentEditor() );
        
        if ( column == 3 )
            return ( new HitTimesEditor() );
        
        return ( null );
    }
    
    public InputBindingsTable( TableModel model, TableColumnModel columnModel, ListSelectionModel selectionModel, InputBindingsGUI gui )
    {
        super( model, columnModel, selectionModel );
        
        this.gui = gui;
    }
}
