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
        candidates.add( new File( new File( GameFileSystem.CONFIG_FOLDER, modName ), filename ) );
    }
    
    private void addCandidate( String filename )
    {
        candidates.add( new File( GameFileSystem.CONFIG_FOLDER, filename ) );
    }
    
    private void addSmallMonitorCandidates( String modName, String vehicleClass, SessionType sessionType )
    {
        boolean isPractice = sessionType.isPractice();
        
        addCandidate( modName, "overlay_monitor_small_" + vehicleClass + "_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_monitor_small_" + vehicleClass + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_monitor_small_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_monitor_small_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( "overlay_monitor_small_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( "overlay_monitor_small_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_monitor_small_" + vehicleClass + ".ini" );
        addCandidate( modName, "overlay_monitor_small.ini" );
        addCandidate( "overlay_monitor_small.ini" );
    }
    
    private void addBigMonitorCandidates( String modName, String vehicleClass, SessionType sessionType )
    {
        boolean isPractice = sessionType.isPractice();
        
        addCandidate( modName, "overlay_monitor_big_" + vehicleClass + "_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_monitor_big_" + vehicleClass + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_monitor_big_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_monitor_big_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( "overlay_monitor_big_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( "overlay_monitor_big_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_monitor_big_" + vehicleClass + ".ini" );
        addCandidate( modName, "overlay_monitor_big.ini" );
        addCandidate( "overlay_monitor_big.ini" );
    }
    
    private void addMonitorCandidates( String modName, String vehicleClass, SessionType sessionType )
    {
        boolean isPractice = sessionType.isPractice();
        
        addCandidate( modName, "overlay_monitor_" + vehicleClass + "_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_monitor_" + vehicleClass + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_monitor_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_monitor_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( "overlay_monitor_big_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( "overlay_monitor_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_monitor_" + vehicleClass + ".ini" );
        addCandidate( modName, "overlay_monitor.ini" );
        addCandidate( "overlay_monitor.ini" );
    }
    
    private void addGarageCandidates( String modName, String vehicleClass, SessionType sessionType )
    {
        boolean isPractice = sessionType.isPractice();
        
        addCandidate( modName, "overlay_garage_" + vehicleClass + "_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_garage_" + vehicleClass + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_garage_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_garage_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( "overlay_garage_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( "overlay_garage_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_garage_" + vehicleClass + ".ini" );
        addCandidate( modName, "overlay_garage.ini" );
        addCandidate( "overlay_garage.ini" );
    }
    
    private void addRegularCandidates( String modName, String vehicleClass, SessionType sessionType )
    {
        boolean isPractice = sessionType.isPractice();
        
        addCandidate( modName, "overlay_" + vehicleClass + "_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_" + vehicleClass + "_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( modName, "overlay_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( "overlay_" + sessionType.name() + ".ini" );
        if ( isPractice )
            addCandidate( "overlay_" + SessionType.PRACTICE_WILDCARD + ".ini" );
        addCandidate( modName, "overlay_" + vehicleClass + ".ini" );
        addCandidate( modName, "overlay.ini" );
        addCandidate( "overlay.ini" );
    }
    
    public ConfigurationCandidatesIterator( boolean smallMonitor, boolean bigMonitor, boolean isInGarage, String modName, String vehicleClass, SessionType sessionType )
    {
        if ( smallMonitor )
        {
            addSmallMonitorCandidates( modName, vehicleClass, sessionType );
            addMonitorCandidates( modName, vehicleClass, sessionType );
        }
        else if ( bigMonitor )
        {
            addBigMonitorCandidates( modName, vehicleClass, sessionType );
            addMonitorCandidates( modName, vehicleClass, sessionType );
        }
        else
        {
            if ( isInGarage )
            {
                addGarageCandidates( modName, vehicleClass, sessionType );
            }
            
            addRegularCandidates( modName, vehicleClass, sessionType );
        }
    }
}
