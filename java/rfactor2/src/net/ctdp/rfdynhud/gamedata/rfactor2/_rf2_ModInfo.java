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
package net.ctdp.rfdynhud.gamedata.rfactor2;

import java.io.File;
import java.util.ArrayList;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

/**
 * Model of mod information
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class _rf2_ModInfo extends ModInfo
{
    //private final _LiveGameDataObjectsFactory gdFactory;
    private final GameFileSystem fileSystem;
    
    private final _rf2_VehicleRegistry vehicleRegistry;
    
    /**
     * 
     * @param rfmFile
     */
    private void parseRFM( File rfmFile )
    {
        maxOpponents = Integer.MAX_VALUE;
        raceDuration = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateImpl()
    {
        this.rfmFile = new File( new File( fileSystem.getGameFolder(), "rfm" ), getName() + ".rfm" );
        this.vehiclesDir = new File( new File( fileSystem.getGameFolder(), "GameData" ), "Vehicles" );
        this.vehicleFilter = null;
        
        parseRFM( rfmFile );
        
        if ( vehiclesDir != null )
            vehicleRegistry.update( vehicleFilter, vehiclesDir );
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
     * Creates a new ModInfo instance.
     * 
     * @param fileSystem
     * @param profileInfo
     */
    public _rf2_ModInfo( GameFileSystem fileSystem, ProfileInfo profileInfo )
    {
        super( profileInfo );
        
        this.fileSystem = fileSystem;
        
        this.vehicleRegistry = new _rf2_VehicleRegistry();
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
