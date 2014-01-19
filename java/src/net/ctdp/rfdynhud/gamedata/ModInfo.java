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
package net.ctdp.rfdynhud.gamedata;

/**
 * Model of mod information
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class ModInfo
{
    private final ProfileInfo profileInfo;
    
    private String modName = null;
    protected int maxOpponents = -1;
    protected float raceDuration = -1f;
    
    protected abstract void updateImpl();
    
    protected final void update()
    {
        this.modName = profileInfo.getModName();
        
        updateImpl();
    }
    
    /**
     * Gets the current mod's name.
     * 
     * @return the current mod's name.
     */
    public final String getName()
    {
        return ( modName );
    }
    
    /**
     * Gets the VehicleInfo corresponding to the given driver.
     * 
     * @param vsi
     * 
     * @return the VehicleInfo corresponding to the given driver.
     */
    public abstract VehicleInfo getVehicleInfoForDriver( VehicleScoringInfo vsi );
    
    /**
     * Gets the 'max opponents' setting from the mod's RFM.
     * 
     * @return the 'max opponents' setting from the mod's RFM.
     */
    public final int getMaxOpponents()
    {
        return ( maxOpponents );
    }
    
    /**
     * Gets the race duration in seconds.
     * 
     * @return the race duration in seconds.
     */
    public final float getRaceDuration()
    {
        return ( raceDuration );
    }
    
    /**
     * Gets the filenames of all installed mods.
     * 
     * @return the filenames of all installed mods.
     */
    public abstract String[] getInstalledModNames();
    
    /**
     * Creates a new ModInfo instance.
     * 
     * @param profileInfo
     */
    protected ModInfo( ProfileInfo profileInfo )
    {
        this.profileInfo = profileInfo;
    }
}
