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

import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public interface WidgetsEditorPanelListener extends WidgetSelectionListener
{
    /**
     * Invoked when a {@link Widget}'s position and/or size has changed.
     * 
     * @param widget the changed {@link Widget}
     */
    public void onWidgetPositionSizeChanged( Widget widget );
    
    /**
     * Invoked when a {@link Widget} has been removed.
     * 
     * @param widget the removed {@link Widget}
     * 
     * @return <code>true</code>, if the removal was accepted, <code>false</code> otherwise.
     */
    public boolean onWidgetRemoved( Widget widget );
    
    /**
     * Requests a context menu.
     * 
     * @param selectedWidget the currently selected {@link Widget}.
     */
    public void onContextMenuRequested( Widget selectedWidget );
}
