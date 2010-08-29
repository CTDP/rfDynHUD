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
package net.ctdp.rfdynhud.input;

/**
 * The {@link InputMapping} keeps information about a single mapped input component to a Widget.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class InputMapping
{
    public static final int MODIFIER_MASK_SHIFT = 1;
    public static final int MODIFIER_MASK_CTRL  = 2;
    public static final int MODIFIER_MASK_LALT  = 4;
    public static final int MODIFIER_MASK_RALT  = 8;
    public static final int MODIFIER_MASK_LMETA = 16;
    public static final int MODIFIER_MASK_RMETA = 32;
    
    private final String widgetName;
    private final InputAction action;
    private final String deviceComponent;
    private final int keyCode;
    private final int modifierMask;
    private final int hitTimes;
    
    public final String getWidgetName()
    {
        return ( widgetName );
    }
    
    public final InputAction getAction()
    {
        return ( action );
    }
    
    public final String getDeviceComponent()
    {
        return ( deviceComponent );
    }
    
    public final int getKeyCode()
    {
        return ( keyCode );
    }
    
    public final int getModifierMask()
    {
        return ( modifierMask );
    }
    
    public final int getHitTimes()
    {
        return ( hitTimes );
    }
    
    public InputMapping( String widgetName, InputAction action, String deviceComponent, int keyCode, int modifierMask, int hitTimes )
    {
        this.widgetName = widgetName;
        this.action = action;
        this.deviceComponent = deviceComponent;
        this.keyCode = keyCode;
        this.modifierMask = modifierMask;
        this.hitTimes = hitTimes;
    }
}
