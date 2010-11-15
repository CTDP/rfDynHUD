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
package net.ctdp.rfdynhud.widgets.base.widget;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

/**
 * Controls {@link Widget}s' visibility and position.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class WidgetController
{
    private Boolean widgetVisible = null;
    
    private Integer viewedVSIid = -1;
    private VehicleScoringInfo viewedVSI = null;
    private VehicleScoringInfo lastViewedVSI = null;
    
    private Integer compareVSIid = -1;
    private VehicleScoringInfo compareVSI = null;
    
    public void setWidgetVisible( Boolean visible )
    {
        this.widgetVisible = visible;
    }
    
    public final Boolean isWidgetVisible()
    {
        return ( widgetVisible );
    }
    
    public void setViewedVehicle( Integer vsiID )
    {
        this.viewedVSIid = vsiID;
    }
    
    protected final VehicleScoringInfo getViewedVSI()
    {
        return ( viewedVSI );
    }
    
    public void setCompareVehicle( Integer vsiID )
    {
        this.compareVSIid = vsiID;
    }
    
    protected final VehicleScoringInfo getCompareVSI()
    {
        return ( compareVSI );
    }
    
    private static VehicleScoringInfo getVSIById( ScoringInfo scoringInfo, int id )
    {
        int n = scoringInfo.getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            if ( scoringInfo.getVehicleScoringInfo( i ).getDriverId() == id )
                return ( scoringInfo.getVehicleScoringInfo( i ) );
        }
        
        return ( null );
    }
    
    protected abstract void updateImpl( Widget widget, LiveGameData gameData );
    
    public final void update( Widget widget, LiveGameData gameData )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( ( viewedVSIid == null ) || ( viewedVSIid.intValue() < 1 ) )
            viewedVSI = null;
        else if ( ( viewedVSI == null ) || !viewedVSI.isValid() || ( viewedVSI.getDriverId() != viewedVSIid ) )
            viewedVSI = getVSIById( scoringInfo, viewedVSIid );
        
        if ( viewedVSI == null )
        {
            if ( lastViewedVSI != null )
                widget._onVehicleControlChanged( scoringInfo.getViewedVehicleScoringInfo(), gameData, false );
        }
        else if ( !viewedVSI.equals( scoringInfo.getViewedVehicleScoringInfo() ) )
        {
            widget._onVehicleControlChanged( viewedVSI, gameData, false );
        }
        
        lastViewedVSI = viewedVSI;
        
        if ( ( compareVSIid == null ) || ( compareVSIid.intValue() < 1 ) )
            compareVSI = null;
        else if ( ( compareVSI == null ) || !compareVSI.isValid() || ( compareVSI.getDriverId() != compareVSIid ) )
            compareVSI = getVSIById( scoringInfo, compareVSIid );
        
        updateImpl( widget, gameData );
    }
}
