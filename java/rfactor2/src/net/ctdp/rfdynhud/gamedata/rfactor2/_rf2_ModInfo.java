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
package net.ctdp.rfdynhud.gamedata.rfactor2;

import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

/**
 * Model of mod information
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_ModInfo extends ModInfo
{
    private final _rf2_VehicleRegistry vehicleRegistry;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateImpl()
    {
        maxOpponents = 32; // 256
        raceDuration = 120;//-1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleInfo getVehicleInfoForDriver( VehicleScoringInfo vsi )
    {
        return ( vehicleRegistry.getVehicleForDriver( vsi.getVehicleName() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getInstalledModNames()
    {
        // TODO: Find a way to read the installed mods.
        
        return ( new String[ 0 ] );
    }
    
    /**
     * Creates a new ModInfo instance.
     * 
     * @param profileInfo
     */
    public _rf2_ModInfo( ProfileInfo profileInfo )
    {
        super( profileInfo );
        
        this.vehicleRegistry = new _rf2_VehicleRegistry();
    }
}
