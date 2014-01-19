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
package net.ctdp.rfdynhud.plugins.director;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetController;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorWidgetController extends WidgetController
{
    private long visibleStart = Long.MAX_VALUE;
    private long visibleEnd = Long.MIN_VALUE;
    private short posX = -10000;
    private short posY = -10000;
    
    public void reset()
    {
        visibleStart = Long.MAX_VALUE;
        visibleEnd = Long.MIN_VALUE;
        posX = -10000;
        posY = -10000;
        
        setViewedVehicle( null );
    }
    
    public void setWidgetState( long visibleStart, long visibleEnd, short x, short y, int viewedVSIid, int compareVSIid )
    {
        this.visibleStart = visibleStart;
        this.visibleEnd = visibleEnd;
        this.posX = x;
        this.posY = y;
        
        setViewedVehicle( ( viewedVSIid < 1 ) ? null : viewedVSIid );
        setCompareVehicle( ( compareVSIid < 1 ) ? null : compareVSIid );
    }
    
    @Override
    protected void updateImpl( Widget widget, LiveGameData gameData )
    {
        long sessionTime = gameData.getScoringInfo().getSessionNanos();
        
        if ( visibleEnd < visibleStart )
            setWidgetVisible( null );
        else
            setWidgetVisible( ( visibleStart <= sessionTime ) && ( sessionTime <= visibleEnd ) );
        
        boolean posChanged = ( ( posX > -10000 ) && ( posY > -10000 ) && !widget.getPosition().equalsEffective( posX, posY ) );
        
        if ( posChanged )
            widget.getPosition().setEffectivePosition( posX, posY );
    }
}
