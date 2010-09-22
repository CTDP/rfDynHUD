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
package net.ctdp.rfdynhud.editor.util;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.HashMap;

public class AvailableDisplayModes
{
    private static final HashMap<String, java.awt.DisplayMode> displayModes = new HashMap<String, java.awt.DisplayMode>();
    static
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for ( GraphicsDevice gd : ge.getScreenDevices() )
        {
            int n = 0;
            for ( DisplayMode dm : gd.getDisplayModes() )
            {
                if ( ( dm.getWidth() >= 800 ) && ( dm.getWidth() >= 600 ) && ( dm.getBitDepth() == 32 ) )
                {
                    String key = dm.getWidth() + "x" + dm.getHeight();
                    
                    DisplayMode dm2 = displayModes.get( key );
                    if ( dm2 == null )
                    {
                        displayModes.put( key, dm );
                    }
                    else if ( dm.getRefreshRate() > dm2.getRefreshRate() )
                    {
                        displayModes.remove( key );
                        displayModes.put( key, dm );
                    }
                    
                    n++;
                }
            }
            
            if ( n == 0 )
            {
                DisplayMode dm = gd.getDisplayMode();
                
                String key = dm.getWidth() + "x" + dm.getHeight();
                
                DisplayMode dm2 = displayModes.get( key );
                if ( dm2 == null )
                {
                    displayModes.put( key, dm );
                }
                else if ( dm.getRefreshRate() > dm2.getRefreshRate() )
                {
                    displayModes.remove( key );
                    displayModes.put( key, dm );
                }
            }
        }
    }
    
    public static final java.awt.DisplayMode getDisplayMode( String resString )
    {
        return ( displayModes.get( resString ) );
    }
    
    public static final Collection<java.awt.DisplayMode> getAll()
    {
        return ( displayModes.values() );
    }
}
