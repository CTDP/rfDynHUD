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
package net.ctdp.rfdynhud.plugins.simulation;

import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.plugins.GameEventsPlugin;
import net.ctdp.rfdynhud.render.WidgetsManager;

import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SimulationRecorderPlugin extends GameEventsPlugin
{
    public static final String INI_FILENAME = "sim_recorder.ini";
    
    private boolean enabled = false;
    
    private SimulationRecorder recorder = null;
    
    /**
     * 
     * @param baseFolder
     */
    public SimulationRecorderPlugin( File baseFolder )
    {
        super( "Simulation", baseFolder );
    }
    
    @Override
    public void onPluginStarted( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( isEditorMode )
            return;
        
        File dataFile = null;
        boolean onlyRecordInCockpit = true;
        boolean resetWhenEnteringCockpit = false;
        
        File iniFile = new File( getBaseFolder(), INI_FILENAME );
        
        if ( iniFile.exists() )
        {
            String enabled = AbstractIniParser.parseIniValue( iniFile, "SIMRECORDER", "enabled", null );
            
            if ( enabled != null )
                this.enabled = Boolean.parseBoolean( enabled );
            
            if ( !this.enabled )
                return;
            
            onlyRecordInCockpit = Boolean.parseBoolean( AbstractIniParser.parseIniValue( iniFile, "SIMRECORDER", "onlyRecordInCockpit", "true" ) );
            
            resetWhenEnteringCockpit = Boolean.parseBoolean( AbstractIniParser.parseIniValue( iniFile, "SIMRECORDER", "resetWhenEnteringCockpit", "false" ) );
            
            String file = AbstractIniParser.parseIniValue( iniFile, "SIMRECORDER", "file", "D:\\rfdynhud_data" );
            
            dataFile = new File( file );
        }
        else
        {
            dataFile = new File( "D:\\rfdynhud_data" );
        }
        
        if ( !dataFile.isAbsolute() )
            dataFile = new File( iniFile.getParentFile(), dataFile.getPath() );
        
        try
        {
            this.recorder = new SimulationRecorder( dataFile, onlyRecordInCockpit, resetWhenEnteringCockpit, getLogPrefix() );
        }
        catch ( IOException e )
        {
            log( e );
            
            this.enabled = false;
            return;
        }
        
        recorder.wireListeners( gameData, widgetsManager );
    }
    
    @Override
    public void onPluginShutdown( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( isEditorMode || !enabled )
            return;
        
        recorder.unwireListeners( gameData, widgetsManager );
        recorder.close();
    }
}
