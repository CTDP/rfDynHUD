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
import java.util.HashMap;

import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.strings.StringUtils;

/**
 * Provides {@link VehicleInfo} instances.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class VehicleRegistry
{
    private final ArrayList<VehicleInfo> vehicles = new ArrayList<VehicleInfo>();
    private final HashMap<String, VehicleInfo> driverVehileMap = new HashMap<String, VehicleInfo>();
    
    private void findVehicleFiles( String[] vehicleFilter, File folder )
    {
        for ( File file : folder.listFiles() )
        {
            if ( file.isDirectory() )
            {
                if ( !file.getName().equals( ".svn" ) )
                {
                    findVehicleFiles( vehicleFilter, file );
                }
            }
            else if ( StringUtils.endsWithIgnoreCase( file.getName(), ".veh" ) )
            {
                VehicleInfo vi = new VehicleInfo();
                try
                {
                    new VehicleInfoParser( file.getName(), vi ).parse( file );
                    
                    if ( vehicleFilter == null )
                    {
                        this.vehicles.add( vi );
                    }
                    else if ( vi.getClasses() != null )
                    {
                        String[] classes = vi.getClasses().split( "," );
                        boolean found = false;
                        
                        for ( int i = 0; i < classes.length && !found; i++ )
                        {
                            String c = classes[i].trim().toLowerCase();
                            for ( int j = 0; j < vehicleFilter.length && !found; j++ )
                            {
                                if ( c.equals( vehicleFilter[j] ) )
                                    found = true;
                            }
                        }
                        
                        if ( found )
                            this.vehicles.add( vi );
                    }
                }
                catch ( Throwable t )
                {
                    RFDHLog.exception( t );
                }
            }
        }
    }
    
    public void update( String[] vehicleFilter, File vehiclesFolder )
    {
        vehicles.clear();
        driverVehileMap.clear();
        findVehicleFiles( vehicleFilter, vehiclesFolder );
        
        for ( int i = 0; i < vehicles.size(); i++ )
        {
            driverVehileMap.put( vehicles.get( i ).getDriverDescription(), vehicles.get( i ) );
        }
    }
    
    public final VehicleInfo getVehicleForDriver( String vehicleName )
    {
        return ( driverVehileMap.get( vehicleName ) );
    }
    
    public VehicleRegistry()
    {
    }
}
