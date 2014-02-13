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
package net.ctdp.rfdynhud.plugins.director;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.plugins.datasender.AbstractDataSenderPlugin;
import net.ctdp.rfdynhud.render.WidgetsManager;
import net.ctdp.rfdynhud.render.WidgetsRenderListener;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.streams.LimitedInputStream;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorPlugin extends AbstractDataSenderPlugin implements WidgetsRenderListener
{
    private static final String INI_FILENAME = "director.ini";
    private static final String DEFAULT_NO_CONFIG_RECEIVED_MESSAGE = "No configuration received so far.";
    private static final String CONFIGURATION_NAME = "DIRECTOR_CONFIGURATION";
    
    private String noConfigFoundMessage = "No configuration received so far.";
    private String originalNoConfigFoundMessage = null;
    
    private GameEventsManager eventsManager = null;
    
    private ConfigurationLoader loader = null;
    private Iterator<File> defaultCandidatesIterator = null;
    
    private final Map<String, DirectorWidgetController> widgetControllersMap = new HashMap<String, DirectorWidgetController>();
    
    private LiveGameData lastGameData = null;
    
    private byte[] configData = null;
    private int configLength = -1;
    
    public DirectorPlugin( File baseFolder )
    {
        super( "Director", baseFolder, new File( baseFolder, INI_FILENAME ) );
    }
    
    @Override
    public void onPluginStarted( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        this.eventsManager = eventsManager;
        
        super.onPluginStarted( eventsManager, gameData, isEditorMode, widgetsManager );
        
        if ( !isEnabled() )
            return;
    }
    
    @Override
    protected void parseIniFile( File iniFile, GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( iniFile.exists() )
        {
            String enabled = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", "enabled", null );
            
            if ( enabled != null )
                setEnabled( Boolean.parseBoolean( enabled ) );
            
            String port = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", isEditorMode ? "offlinePort" : "port", null );
            
            if ( ( port == null ) || ( port.length() == 0 ) )
                setEnabled( false );
            
            String password = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", "password", "" );
            
            try
            {
                this.communicator = new Communicator( this, Integer.parseInt( port ), password );
            }
            catch ( Throwable t )
            {
                log( t );
                setEnabled( false );
            }
            
            String noConfigFoundMessage = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", "noConfigMessage", DEFAULT_NO_CONFIG_RECEIVED_MESSAGE );
            if ( ( noConfigFoundMessage == null ) || ( noConfigFoundMessage.length() == 0 ) )
                this.noConfigFoundMessage = null;
            else
                this.noConfigFoundMessage = "Director: " + noConfigFoundMessage;
        }
    }
    
    @Override
    protected void registerListeners( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.registerListeners( eventsManager, gameData, isEditorMode, widgetsManager );
        
        widgetsManager.registerListener( this );
        
        this.loader = widgetsManager.getConfigurationLoader();
        this.defaultCandidatesIterator = eventsManager.getConfigurationCandidatesIterator();
        this.originalNoConfigFoundMessage = loader.getNoConfigFoundMessage();
    }
    
    @Override
    protected void unregisterListeners( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.unregisterListeners( eventsManager, gameData, isEditorMode, widgetsManager );
        
        widgetsManager.unregisterListener( this );
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
        
        eventsManager.setConfigurationCandidatesIterator( null );
        loader.setNoConfigFoundMessage( noConfigFoundMessage );
        
        if ( lastGameData != null )
        {
            sendDriversList( lastGameData );
        }
    }
    
    @Override
    protected void onConnectionClosed()
    {
        super.onConnectionClosed();
        
        loader.setNoConfigFoundMessage( originalNoConfigFoundMessage );
        eventsManager.setConfigurationCandidatesIterator( defaultCandidatesIterator );
    }
    
    private void sendDriversList( LiveGameData gameData )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        communicator.startCommand( DirectorConstants.DRIVERS_LIST );
        
        int n = scoringInfo.getNumVehicles();
        communicator.writeShort( n );
        
        for ( int i = 0; i < n; i++ )
        {
            sendDriversName( scoringInfo.getVehicleScoringInfo( i ), false );
        }
        
        communicator.endCommand();
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        lastGameData = gameData;
        
        super.onSessionStarted( gameData, isEditorMode );
        
        if ( communicator.isConnected() )
        {
            sendDriversList( gameData );
        }
    }
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitEntered( gameData, isEditorMode );
    }
    
    @Override
    public void onCockpitExited( LiveGameData gameData, boolean isEditorMode )
    {
        super.onCockpitExited( gameData, isEditorMode );
    }
    
    private void sendDriversPositions( LiveGameData gameData )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        communicator.startCommand( DirectorConstants.DRIVERS_POSITIONS );
        
        int n = scoringInfo.getNumVehicles();
        communicator.writeShort( n );
        
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            communicator.writeInt( vsi.getDriverId() );
            //communicator.writeShort( vsi.getPlace( false ) );
        }
        
        communicator.endCommand();
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        super.onScoringInfoUpdated( gameData, isEditorMode );
        
        if ( communicator.isConnected() )
        {
            sendDriversPositions( gameData );
        }
    }
    
    @Override
    public void beforeWidgetsConfigurationCleared( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
    }
    
    public void onWidgetsConfigReceived( byte[] configData, int length )
    {
        if ( isEditorMode() )
            return;
        
        //debug( "Widgets config received. length: ", length );
        this.configLength = length;
        this.configData = configData;
    }
    
    @Override
    public void afterWidgetsConfigurationLoaded( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
        if ( isEditorMode() )
            return;
        
        if ( widgetsConfig.getName() == CONFIGURATION_NAME )
        {
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
            {
                Widget widget = widgetsConfig.getWidget( i );
                
                DirectorWidgetController controller = widgetControllersMap.get( widget.getName() );
                if ( controller == null )
                    controller = new DirectorWidgetController();
                
                widget.setWidgetController( controller );
            }
            
            widgetControllersMap.clear();
            
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
            {
                Widget widget = widgetsConfig.getWidget( i );
                
                DirectorWidgetController controller = (DirectorWidgetController)widget.getWidgetController();
                
                widgetControllersMap.put( widget.getName(), controller );
            }
        }
    }
    
    @Override
    public void beforeWidgetsAreRendered( LiveGameData gameData, WidgetsConfiguration widgetsConfig, long sessionTime, long frameCounter )
    {
        if ( configData != null )
        {
            LimitedInputStream lin = new LimitedInputStream( new ByteArrayInputStream( configData ), configLength );
            
            try
            {
                //debug( "Loading received WidgetsConfiguration..." );
                loader.loadConfiguration( lin, CONFIGURATION_NAME, widgetsConfig, gameData, false );
            }
            catch ( IOException e )
            {
                log( e );
            }
            
            configData = null;
            configLength = -1;
        }
    }
    
    public void onWidgetStatesReset()
    {
        for ( DirectorWidgetController controller : widgetControllersMap.values() )
            controller.reset();
    }
    
    public void onWidgetStateReceived( String widgetName, long visibleStart, long visibleEnd, short posX, short posY, int viewedVSIid, int compareVSIId )
    {
        if ( isEditorMode() )
            return;
        
        DirectorWidgetController controller = widgetControllersMap.get( widgetName );
        //debug( "Received WidgetState: ", widgetName, ", ", visibleStart, ", ", visibleEnd, ", ", posX, ", ", posY, ", ", viewedVSIid, ", ", compareVSIId );
        
        if ( controller != null )
        {
            controller.setWidgetState( visibleStart, visibleEnd, posX, posY, viewedVSIid, compareVSIId );
        }
    }
}
