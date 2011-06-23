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

import java.io.File;
import java.util.ArrayList;

/**
 * Model of mod information
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class ModInfo
{
    protected String modName = null;
    protected File rfmFile = null;
    protected File vehiclesDir = null;
    protected String[] vehicleFilter = null;
    protected int maxOpponents = -1;
    protected float raceDuration = -1f;
    
    protected abstract void updateImpl();
    
    protected final void update()
    {
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
     * Gets the mod's RFM file.
     * 
     * @return the mod's RFM file.
     */
    public final File getRFMFile()
    {
        return ( rfmFile );
    }
    
    /**
     * Gets the vehicle filter.
     * 
     * @return the vehicle filter.
     */
    public final String[] getVehicleFilter()
    {
        return ( vehicleFilter );
    }
    
    /**
     * Gets the folder, where to search for .VEH files.
     * 
     * @return the folder, where to search for .VEH files.
     */
    public final File getVehiclesFolder()
    {
        return ( vehiclesDir );
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
     * Creates a new ModInfo instance.
     */
    protected ModInfo()
    {
    }
    
    /**
     * Gets the RFM filenames of all installed mods.
     * 
     * @param fileSystem
     * 
     * @return the RFM filenames of all installed mods.
     */
    public static String[] getInstalledModNames( GameFileSystem fileSystem )
    {
        File[] rfms = new File( fileSystem.getGameFolder(), "rfm" ).listFiles();
        
        if ( rfms == null )
            return ( null );
        
        ArrayList<String> names = new ArrayList<String>();
        
        for ( File rfm : rfms )
        {
            String name = rfm.getName();
            if ( rfm.isFile() && name.toLowerCase().endsWith( ".rfm" ) )
            {
                if ( name.length() == 4 )
                    names.add( "" );
                else
                    names.add( name.substring( 0, name.length() - 4 ) );
            }
        }
        
        return ( names.toArray( new String[ names.size() ] ) );
    }
}
