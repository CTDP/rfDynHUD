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
package net.ctdp.rfdynhud.plugins.datasender;

import java.io.File;

import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.plugins.AbstractDataSenderPlugin;
import net.ctdp.rfdynhud.render.WidgetsManager;

import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DataSenderPlugin extends AbstractDataSenderPlugin
{
    private static final String INI_FILENAME = "datasender.ini";
    
    public DataSenderPlugin( File baseFolder )
    {
        super( "DataSender", baseFolder, new File( baseFolder, INI_FILENAME ) );
    }
    
    @Override
    public void onPluginStarted( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.onPluginStarted( eventsManager, gameData, isEditorMode, widgetsManager );
    }
    
    @Override
    protected void parseIniFile( File iniFile, GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( iniFile.exists() )
        {
            String enabled = AbstractIniParser.parseIniValue( iniFile, "DATASENDER", "enabled", null );
            
            if ( enabled != null )
                setEnabled( Boolean.parseBoolean( enabled ) );
            
            String port = AbstractIniParser.parseIniValue( iniFile, "DATASENDER", isEditorMode ? "offlinePort" : "port", null );
            
            if ( ( port == null ) || ( port.length() == 0 ) )
                setEnabled( false );
            
            String password = AbstractIniParser.parseIniValue( iniFile, "DATASENDER", "password", "" );
            
            try
            {
                this.communicator = new Communicator( this, Integer.parseInt( port ), password );
            }
            catch ( Throwable t )
            {
                log( t );
                setEnabled( false );
            }
        }
    }
    
    @Override
    public void onPluginShutdown( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.onPluginShutdown( eventsManager, gameData, isEditorMode, widgetsManager );
    }
    
    @Override
    protected void onConnectionEsteblished()
    {
        super.onConnectionEsteblished();
    }
    
    @Override
    protected void onConnectionClosed()
    {
        super.onConnectionClosed();
    }
}
