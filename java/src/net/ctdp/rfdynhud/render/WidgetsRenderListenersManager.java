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
package net.ctdp.rfdynhud.render;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

/**
 * Manages {@link WidgetsRenderListener}s.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetsRenderListenersManager
{
    private WidgetsRenderListener[] listeners = null;
    
    public void registerListener( WidgetsRenderListener l )
    {
        if ( listeners == null )
        {
            listeners = new WidgetsRenderListener[] { l };
        }
        else
        {
            for ( int i = 0; i < listeners.length; i++ )
            {
                if ( listeners[i] == l )
                    return;
            }
            
            WidgetsRenderListener[] tmp = new WidgetsRenderListener[ listeners.length + 1 ];
            System.arraycopy( listeners, 0, tmp, 0, listeners.length );
            listeners = tmp;
            listeners[listeners.length - 1] = l;
        }
    }
    
    public void unregisterListener( WidgetsRenderListener l )
    {
        if ( listeners == null )
            return;
        
        int index = -1;
        for ( int i = 0; i < listeners.length; i++ )
        {
            if ( listeners[i] == l )
            {
                index = i;
                break;
            }
        }
        
        if ( index < 0 )
            return;
        
        if ( listeners.length == 1 )
        {
            listeners = null;
            return;
        }
        
        WidgetsRenderListener[] tmp = new WidgetsRenderListener[ listeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( listeners, 0, tmp, 0, index );
        if ( index < listeners.length - 1 )
            System.arraycopy( listeners, index + 1, tmp, index, listeners.length - index - 1 );
        listeners = tmp;
    }
    
    void fireBeforeWidgetsConfigurationCleared( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
        if ( listeners != null )
        {
            for ( int i = 0; i < listeners.length; i++ )
            {
                listeners[i].beforeWidgetsConfigurationCleared( gameData, widgetsConfig );
            }
        }
    }
    
    void fireAfterWidgetsConfigurationLoaded( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
        if ( listeners != null )
        {
            for ( int i = 0; i < listeners.length; i++ )
            {
                listeners[i].afterWidgetsConfigurationLoaded( gameData, widgetsConfig );
            }
        }
        
    }
    
    void fireBeforeWidgetsAreRendered( LiveGameData gameData, WidgetsConfiguration widgetsConfig, long sessionTime, long frameCounter )
    {
        if ( listeners != null )
        {
            for ( int i = 0; i < listeners.length; i++ )
            {
                listeners[i].beforeWidgetsAreRendered( gameData, widgetsConfig, sessionTime, frameCounter );
            }
        }
    }
}
