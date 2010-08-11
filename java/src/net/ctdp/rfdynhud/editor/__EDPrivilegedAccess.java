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
package net.ctdp.rfdynhud.editor;

import java.io.IOException;

import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class __EDPrivilegedAccess
{
    public static boolean isEditorMode = false;
    
    public static final void getEditorPresetsProperties( EditorPresets editorPresets, WidgetPropertiesContainer propsCont )
    {
        editorPresets.getProperties( propsCont );
    }
    
    public static final void saveProperties( EditorPresets editorPresets, WidgetsConfigurationWriter writer ) throws IOException
    {
        editorPresets.saveProperties( writer );
    }
    
    public static final void loadProperty( EditorPresets editorPresets, String key, String value )
    {
        editorPresets.loadProperty( key, value );
    }
}
