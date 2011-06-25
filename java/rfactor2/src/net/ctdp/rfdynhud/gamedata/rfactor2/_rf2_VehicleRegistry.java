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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * Provides {@link VehicleInfo} instances.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_VehicleRegistry
{
    private final ArrayList<VehicleInfo> vehicles = new ArrayList<VehicleInfo>();
    private final HashMap<String, VehicleInfo> driverVehileMap = new HashMap<String, VehicleInfo>();
    
    @SuppressWarnings( "unused" )
    private void loadFactoryDefaultVehicles()
    {
        String[] files = new String[]
        {
            "BMWF106_NH16.veh",
            "BMWF106_RK17.veh",
            "F248_FM06.veh",
            "F248_MS05.veh",
            "FW28_MW09.veh",
            "FW28_NR10.veh",
            "M16_CA19.veh",
            "M16_TM18.veh",
            "MP4-21_DLR04.veh",
            "MP4-21_KR03.veh",
            "R26_FA01.veh",
            "R26_GF02.veh",
            "RA106_JB12.veh",
            "RA106_RB11.veh",
            "RB2_DC14.veh",
            "RB2_RD15.veh",
            "SA05_SY23.veh",
            "SA05_TS22.veh",
            "TF106_JT08.veh",
            "TF106_RS07.veh",
            "TR1_SS21.veh",
            "TR1_VL20.veh",
        };
        
        for ( String filename : files )
        {
            _rf2_VehicleInfo vi = new _rf2_VehicleInfo();
            try
            {
                URL file = __EDPrivilegedAccess.editorClassLoader.getResource( "data/game_data/vehicles/" + filename );
                
                new _rf2_VehicleInfoParser( filename, vi ).parse( file );
                
                this.vehicles.add( vi );
            }
            catch ( Throwable t )
            {
                RFDHLog.exception( t );
            }
        }
    }
    
    /**
     * 
     * @param vehicleFilter
     * @param vehiclesFolder
     */
    public void update( String[] vehicleFilter, File vehiclesFolder )
    {
        vehicles.clear();
        driverVehileMap.clear();
        
        //if ( __EDPrivilegedAccess.editorClassLoader == null )
        //    findVehicleFiles( vehicleFilter, vehiclesFolder );
        //else
        //    loadFactoryDefaultVehicles();
        
        for ( int i = 0; i < vehicles.size(); i++ )
        {
            driverVehileMap.put( vehicles.get( i ).getDriverDescription(), vehicles.get( i ) );
        }
    }
    
    public final VehicleInfo getVehicleForDriver( String vehicleName )
    {
        return ( driverVehileMap.get( vehicleName ) );
    }
    
    public _rf2_VehicleRegistry()
    {
    }
}
