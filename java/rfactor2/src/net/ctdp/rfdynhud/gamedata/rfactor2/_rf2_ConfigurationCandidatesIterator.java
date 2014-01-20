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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.ctdp.rfdynhud.gamedata.SessionType;

/**
 * Loads a configuration and searches for the file in the following order:
 * <ul>
 *   <li>CONFIGURATION_FOLDER/MOD_FOLDER/overlay_VEHICLE_CLASS.ini</li>
 *   <li>CONFIGURATION_FOLDER/MOD_FOLDER/overlay.ini</li>
 *   <li>CONFIGURATION_FOLDER/config.ini</li>
 * </ul>
 */
class _rf2_ConfigurationCandidatesIterator implements Iterator<File>
{
    private final File configFolder;
    private final List<File> candidates = new ArrayList<File>();
    private int pos = 0;
    
    public final File getConfigFolder()
    {
        return ( configFolder );
    }
    
    public void reset()
    {
        candidates.clear();
        pos = 0;
    }
    
    @Override
    public boolean hasNext()
    {
        return ( pos < candidates.size() );
    }
    
    @Override
    public File next()
    {
        if ( !hasNext() )
            return ( null );
        
        return ( candidates.get( pos++ ) );
    }
    
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    
    private void addCandidate( String modName, String filename )
    {
        candidates.add( new File( new File( configFolder, modName ), filename ) );
    }
    
    private void addCandidate( String filename )
    {
        candidates.add( new File( configFolder, filename ) );
    }
    
    private void addSpecificCandidates( String modName, String prefix, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        boolean isPractice = sessionType.isPractice();
        boolean isQualifying = sessionType.isQualifying();
        boolean isRace = sessionType.isRace();
        
        String prefix2 = prefix + "_";
        
        if ( vehicleName != null )
            addCandidate( modName, prefix2 + vehicleName + "_" + sessionType.name() + ".ini" );
        addCandidate( modName, prefix2 + vehicleClass + "_" + sessionType.name() + ".ini" );
        if ( isPractice )
        {
            if ( vehicleName != null )
                addCandidate( modName, prefix2 + vehicleName + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
            addCandidate( modName, prefix2 + vehicleClass + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        }
        if ( isQualifying )
        {
            if ( vehicleName != null )
                addCandidate( modName, prefix2 + vehicleName + "_" + SessionType.QUALIFYING_WILDCARD + ".ini" );
            addCandidate( modName, prefix2 + vehicleClass + "_" + SessionType.QUALIFYING_WILDCARD + ".ini" );
        }
        if ( isRace )
        {
            if ( vehicleName != null )
                addCandidate( modName, prefix2 + vehicleName + "_" + SessionType.RACE_WILDCARD + ".ini" );
            addCandidate( modName, prefix2 + vehicleClass + "_" + SessionType.RACE_WILDCARD + ".ini" );
        }
        addCandidate( modName, prefix2 + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, prefix2 + SessionType.PRACTICE_WILDCARD + ".ini" );
        if ( isQualifying )
            addCandidate( modName, prefix2 + SessionType.QUALIFYING_WILDCARD + ".ini" );
        if ( isRace )
            addCandidate( modName, prefix2 + SessionType.RACE_WILDCARD + ".ini" );
        addCandidate( prefix2 + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( prefix2 + SessionType.PRACTICE_WILDCARD + ".ini" );
        if ( isQualifying )
            addCandidate( prefix2 + SessionType.QUALIFYING_WILDCARD + ".ini" );
        if ( isRace )
            addCandidate( prefix2 + SessionType.RACE_WILDCARD + ".ini" );
        if ( vehicleName != null )
            addCandidate( modName, prefix2 + vehicleName + ".ini" );
        addCandidate( modName, prefix2 + vehicleClass + ".ini" );
        addCandidate( modName, prefix + ".ini" );
        addCandidate( prefix + ".ini" );
    }
    
    private void addSmallMonitorCandidates( String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( modName, "overlay_monitor_small", vehicleClass, vehicleName, sessionType );
    }
    
    private void addBigMonitorCandidates( String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( modName, "overlay_monitor_big", vehicleClass, vehicleName, sessionType );
    }
    
    private void addMonitorCandidates( String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( modName, "overlay_monitor", vehicleClass, vehicleName, sessionType );
    }
    
    private void addGarageCandidates( String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( modName, "overlay_garage", vehicleClass, vehicleName, sessionType );
    }
    
    private void addRegularCandidates( String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( modName, "overlay", vehicleClass, vehicleName, sessionType );
    }
    
    public void collectCandidates( boolean smallMonitor, boolean bigMonitor, boolean isInGarage, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        if ( smallMonitor )
        {
            addSmallMonitorCandidates( modName, vehicleClass, vehicleName, sessionType );
            addMonitorCandidates( modName, vehicleClass, vehicleName, sessionType );
        }
        else if ( bigMonitor )
        {
            addBigMonitorCandidates( modName, vehicleClass, vehicleName, sessionType );
            addMonitorCandidates( modName, vehicleClass, vehicleName, sessionType );
        }
        else
        {
            if ( isInGarage )
            {
                addGarageCandidates( modName, vehicleClass, vehicleName, sessionType );
            }
            
            addRegularCandidates( modName, vehicleClass, vehicleName, sessionType );
        }
    }
    
    public _rf2_ConfigurationCandidatesIterator( File configFolder )
    {
        this.configFolder = configFolder;
    }
}
