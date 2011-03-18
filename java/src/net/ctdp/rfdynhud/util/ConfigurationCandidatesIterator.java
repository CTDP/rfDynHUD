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
package net.ctdp.rfdynhud.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.SessionType;

public class ConfigurationCandidatesIterator implements Iterator<File>
{
    private final ArrayList<File> candidates = new ArrayList<File>();
    private int pos = 0;
    
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
    
    protected void addCandidate( File configFolder, String modName, String filename )
    {
        candidates.add( new File( new File( configFolder, modName ), filename ) );
    }
    
    protected void addCandidate( File configFolder, String filename )
    {
        candidates.add( new File( configFolder, filename ) );
    }
    
    protected void addSpecificCandidates( File configFolder, String modName, String prefix, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        boolean isPractice = sessionType.isPractice();
        
        String prefix2 = prefix + "_";
        
        if ( vehicleName != null )
            addCandidate( configFolder, modName, prefix2 + vehicleName + "_" + sessionType.name() + ".ini" );
        addCandidate( configFolder, modName, prefix2 + vehicleClass + "_" + sessionType.name() + ".ini" );
        if ( isPractice )
        {
            if ( vehicleName != null )
                addCandidate( configFolder, modName, prefix2 + vehicleName + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
            addCandidate( configFolder, modName, prefix2 + vehicleClass + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        }
        addCandidate( configFolder, modName, prefix2 + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( configFolder, modName, prefix2 + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( configFolder, prefix2 + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( configFolder, prefix2 + SessionType.PRACTICE_WILDCARD + ".ini" );
        if ( vehicleName != null )
            addCandidate( configFolder, modName, prefix2 + vehicleName + ".ini" );
        addCandidate( configFolder, modName, prefix2 + vehicleClass + ".ini" );
        addCandidate( configFolder, modName, prefix + ".ini" );
        addCandidate( configFolder, prefix + ".ini" );
    }
    
    protected void addSmallMonitorCandidates( File configFolder, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( configFolder, modName, "overlay_monitor_small", vehicleClass, vehicleName, sessionType );
    }
    
    protected void addBigMonitorCandidates( File configFolder, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( configFolder, modName, "overlay_monitor_big", vehicleClass, vehicleName, sessionType );
    }
    
    protected void addMonitorCandidates( File configFolder, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( configFolder, modName, "overlay_monitor", vehicleClass, vehicleName, sessionType );
    }
    
    protected void addGarageCandidates( File configFolder, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( configFolder, modName, "overlay_garage", vehicleClass, vehicleName, sessionType );
    }
    
    protected void addRegularCandidates( File configFolder, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        addSpecificCandidates( configFolder, modName, "overlay", vehicleClass, vehicleName, sessionType );
    }
    
    public void collectCandidates( boolean smallMonitor, boolean bigMonitor, boolean isInGarage, String modName, String vehicleClass, String vehicleName, SessionType sessionType )
    {
        final File configFolder = GameFileSystem.INSTANCE.getConfigFolder();
        
        if ( smallMonitor )
        {
            addSmallMonitorCandidates( configFolder, modName, vehicleClass, vehicleName, sessionType );
            addMonitorCandidates( configFolder, modName, vehicleClass, vehicleName, sessionType );
        }
        else if ( bigMonitor )
        {
            addBigMonitorCandidates( configFolder, modName, vehicleClass, vehicleName, sessionType );
            addMonitorCandidates( configFolder, modName, vehicleClass, vehicleName, sessionType );
        }
        else
        {
            if ( isInGarage )
            {
                addGarageCandidates( configFolder, modName, vehicleClass, vehicleName, sessionType );
            }
            
            addRegularCandidates( configFolder, modName, vehicleClass, vehicleName, sessionType );
        }
    }
    
    public ConfigurationCandidatesIterator()
    {
    }
}
