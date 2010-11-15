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
package net.ctdp.rfdynhud.editor.director;

import net.ctdp.rfdynhud.editor.director.widgetstate.WidgetState.VisibleType;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * Describes a {@link Widget}'s state to be sent to the game.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EffectiveWidgetState
{
    private String widgetName = null;
    private VisibleType visibleType = null;
    private long visibleStart = -Long.MAX_VALUE;
    private long visibleTime = Long.MAX_VALUE;
    private DriverCapsule forDriver = null;
    private DriverCapsule compareDriver = null;
    private int posX = -10000;
    private int posY = -10000;
    
    public void setWidgetName( String widgetName )
    {
        this.widgetName = widgetName;
    }
    
    public final String getWidgetName()
    {
        return ( widgetName );
    }
    
    public void setVisibleType( VisibleType visibleType )
    {
        this.visibleType = visibleType;
    }
    
    public final VisibleType getVisibleType()
    {
        return ( visibleType );
    }
    
    public void setVisibleStart( long visibleStart )
    {
        this.visibleStart = visibleStart;
    }
    
    public final long getVisibleStart()
    {
        return ( visibleStart );
    }
    
    public void setVisibleTime( long visibleTime )
    {
        this.visibleTime = visibleTime;
    }
    
    public final long getVisibleTime()
    {
        return ( visibleTime );
    }
    
    public void setForDriver( DriverCapsule driver )
    {
        this.forDriver = driver;
    }
    
    public final DriverCapsule getForDriver()
    {
        return ( forDriver );
    }
    
    public final int getForDriverID()
    {
        if ( forDriver == null )
            return ( -1 );
        
        return ( forDriver.getId() );
    }
    
    public void setCompareDriver( DriverCapsule driver )
    {
        this.compareDriver = driver;
    }
    
    public final DriverCapsule getCompareDriver()
    {
        return ( compareDriver );
    }
    
    public final int getCompareDriverID()
    {
        if ( compareDriver == null )
            return ( -1 );
        
        return ( compareDriver.getId() );
    }
    
    public void setPosX( int posX )
    {
        this.posX = posX;
    }
    
    public final int getPosX()
    {
        return ( posX );
    }
    
    public void setPosY( int posY )
    {
        this.posY = posY;
    }
    
    public final int getPosY()
    {
        return ( posY );
    }
    
    public EffectiveWidgetState()
    {
    }
}
