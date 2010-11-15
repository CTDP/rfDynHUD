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
package net.ctdp.rfdynhud.gamedata;

/**
 * <p>
 * Controls values in {@link LiveGameData}.
 * </p>
 * 
 * <p>
 * Currently it only controls the currently viewed vehicle and engine RPM, boost and gear.
 * </p>
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class LiveGameDataController
{
    private int viewedVSIId = -1;
    
    public void setViewedVSI( int vsiId )
    {
        this.viewedVSIId = vsiId;
    }
    
    public final int getViewedVSIId()
    {
        return ( viewedVSIId );
    }
    
    public void setEngineRPM( VehicleScoringInfo vsi, float engineRPM, float engineMaxRPM )
    {
        vsi.engineMaxRPM = engineRPM;
        vsi.engineMaxRPM = engineMaxRPM;
    }
    
    public void setEngineBoostMapping( VehicleScoringInfo vsi, int engineBoostMapping )
    {
        vsi.engineBoostMapping = engineBoostMapping;
    }
    
    public void setCurrentGear( VehicleScoringInfo vsi, int gear )
    {
        vsi.gear = gear;
    }
    
    public LiveGameDataController()
    {
    }
}
