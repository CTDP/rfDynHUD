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
package net.ctdp.rfdynhud.widgets;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.input.InputMappings;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationClearListener;
import net.ctdp.rfdynhud.widgets.widget.Widget;

public class __WCPrivilegedAccess
{
    public static final void addWidget( WidgetsConfiguration config, Widget widget, boolean isLoading )
    {
        config.addWidget( widget, isLoading );
    }
    
    public static final void removeWidget( WidgetsConfiguration config, Widget widget )
    {
        config.removeWidget( widget );
    }
    
    public static final void clear( WidgetsConfiguration config, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener )
    {
        config.clear( gameData, editorPresets, clearListener );
    }
    
    public static final void setInputMappings( WidgetsConfiguration config, InputMappings inputMappings )
    {
        config.setInputMappings( inputMappings );
    }
    
    public static final void sortWidgets( WidgetsConfiguration config )
    {
        config.sortWidgets();
    }
    
    public static final void setValid( WidgetsConfiguration config, boolean valid )
    {
        config.setValid( valid );
    }
    
    public static final void setJustLoaded( WidgetsConfiguration config, LiveGameData gameData, EditorPresets editorPresets )
    {
        config.setJustLoaded( gameData, editorPresets );
    }
    
    public static final boolean setViewport( int x, int y, int w, int h, WidgetsConfiguration widgetsConfig )
    {
        return ( widgetsConfig.setViewport( x, y, w, h ) );
    }
}
