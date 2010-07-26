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
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 * @author Marvin Froehlich (aka Qudus)
 */
public class StringCellEditor extends JTextField implements TableCellEditor
{
    private static final long serialVersionUID = -4635690306614521152L;
    
    public void addCellEditorListener( CellEditorListener l )
    {
    }
    
    public void removeCellEditorListener( CellEditorListener l )
    {
    }
    
    public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
    {
        this.setText( String.valueOf( value ) );
        
        return ( this );
    }
    
    public Object getCellEditorValue()
    {
        return ( this.getText() );
    }
    
    public boolean isCellEditable( EventObject anEvent )
    {
        return false;
    }
    
    public boolean shouldSelectCell( EventObject anEvent )
    {
        return false;
    }
    
    public void cancelCellEditing()
    {
    }
    
    public boolean stopCellEditing()
    {
        return false;
    }
}
