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
package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorTable extends HierarchicalTable<Property>
{
    private static final long serialVersionUID = 3238244155847132182L;
    
    final RFDynHUDEditor editor;
    final PropertiesEditor propsEditor;
    
    public final RFDynHUDEditor getRFDynHUDEditor()
    {
        return ( editor );
    }
    
    public EditorTable( RFDynHUDEditor editor, PropertiesEditor propsEditor )
    {
        super( new EditorTableModel( propsEditor.getPropertiesList(), 2 ), new TableCellRendererProviderImpl() );
        
        this.editor = editor;
        this.propsEditor = propsEditor;
        
        this.setTableHeader( null );
    }
}
