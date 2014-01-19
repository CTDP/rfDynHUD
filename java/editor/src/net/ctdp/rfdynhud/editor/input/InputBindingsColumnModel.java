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

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class InputBindingsColumnModel extends DefaultTableColumnModel
{
    private static final long serialVersionUID = -8316456120753822033L;
    
    public void init( InputBindingsGUI gui )
    {
        TableColumn column = new TableColumn( 0, 250, new WidgetNameEditor(), new WidgetNameEditor() );
        column.setHeaderValue( "Widget Name" );
        addColumn( column );
        
        column = new TableColumn( 1, 250, new InputActionEditor( gui ), new InputActionEditor( gui ) );
        column.setHeaderValue( "Action" );
        addColumn( column );
        
        column = new TableColumn( 2, 350, new DeviceComponentEditor(), new DeviceComponentEditor() );
        column.setHeaderValue( "Input Component" );
        addColumn( column );
        
        column = new TableColumn( 3, 100, new HitTimesEditor(), new HitTimesEditor() );
        column.setHeaderValue( "Hit Times" );
        addColumn( column );
    }
    
    public InputBindingsColumnModel( InputBindingsGUI gui )
    {
        init( gui );
    }
}
