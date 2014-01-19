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

import net.ctdp.rfdynhud.gamedata.GameEventsListener;
import net.ctdp.rfdynhud.gamedata.GameEventsPlugin;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo.ScoringInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.render.WidgetsManager;
import net.ctdp.rfdynhud.render.WidgetsRenderListener;
import net.ctdp.rfdynhud.util.ConfigurationCandidatesIterator;
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
public class DirectorPlugin extends GameEventsPlugin implements GameEventsListener, ScoringInfoUpdateListener, WidgetsRenderListener
{
    private static final String INI_FILENAME = "director.ini";
    private static final String DEFAULT_NO_CONFIG_RECEIVED_MESSAGE = "No configuration received so far.";
    private static final String CONFIGURATION_NAME = "DIRECTOR_CONFIGURATION";
    
    private final File iniFile;
    private boolean isEditorMode = false;
    
    private boolean enabled = false;
    private Communicator communicator = null;
    private String noConfigFoundMessage = "No configuration received so far.";
    private String originalNoConfigFoundMessage = null;
    
    private ConfigurationLoader loader = null;
    private ConfigurationCandidatesIterator defaultCandidatesIterator = null;
    private final NullingConfigurationCandidatesIterator candidatesIterator = new NullingConfigurationCandidatesIterator();
    
    private final HashMap<String, DirectorWidgetController> widgetControllersMap = new HashMap<String, DirectorWidgetController>();
    
    private LiveGameData lastGameData = null;
    private boolean isInRealtimeMode = false;
    
    private byte[] configData = null;
    private int configLength = -1;
    
    public DirectorPlugin( File baseFolder )
    {
        super( "Director", baseFolder );
        
        iniFile = new File( baseFolder, INI_FILENAME );
    }
    
    @Override
    public void onPluginStarted( LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        this.isEditorMode = isEditorMode;
        
        if ( iniFile.exists() )
        {
            String enabled = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", "enabled", null );
            
            if ( enabled != null )
                this.enabled = Boolean.parseBoolean( enabled );
            
            String port = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", isEditorMode ? "offlinePort" : "port", null );
            
            if ( ( port == null ) || ( port.length() == 0 ) )
                this.enabled = false;
            
            String password = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", "password", "" );
            
            try
            {
                this.communicator = new Communicator( this, Integer.parseInt( port ), password );
            }
            catch ( Throwable t )
            {
                log( t );
                this.enabled = false;
            }
            
            String noConfigFoundMessage = AbstractIniParser.parseIniValue( iniFile, "DIRECTOR", "noConfigMessage", DEFAULT_NO_CONFIG_RECEIVED_MESSAGE );
            if ( ( noConfigFoundMessage == null ) || ( noConfigFoundMessage.length() == 0 ) )
                this.noConfigFoundMessage = null;
            else
                this.noConfigFoundMessage = "Director: " + noConfigFoundMessage;
        }
        
        if ( !enabled )
            return;
        
        gameData.registerGameEventsListener( this );
        gameData.registerDataUpdateListener( this );
        gameData.getScoringInfo().registerListener( this );
        widgetsManager.registerListener( this );
        
        this.loader = widgetsManager.getConfigurationLoader();
        this.defaultCandidatesIterator = loader.getCandidatesIterator();
        this.originalNoConfigFoundMessage = loader.getNoConfigFoundMessage();
        
        communicator.connect();
    }
    
    @Override
    public void onPluginShutdown( LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( enabled && ( communicator != null ) )
        {
            communicator.close( false );
        }
    }
    
    public final boolean isInRealtimeMode()
    {
        return ( isInRealtimeMode );
    }
    
    public void onConnectionEsteblished()
    {
        debug( "Connection esteblished" );
        
        loader.setCandidatesIterator( candidatesIterator );
        loader.setNoConfigFoundMessage( noConfigFoundMessage );
        loader.setDefaultLoadingEnabled( false );
        candidatesIterator.setNulling( true );
        
        if ( lastGameData != null )
        {
            sendDriversList( lastGameData );
        }
    }
    
    public void onConnectionClosed()
    {
        candidatesIterator.setNulling( false );
        loader.setDefaultLoadingEnabled( true );
        loader.setNoConfigFoundMessage( originalNoConfigFoundMessage );
        loader.setCandidatesIterator( defaultCandidatesIterator );
    }
    
    @Override
    public void onVehiclePhysicsUpdated( LiveGameData gameData )
    {
    }
    
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, boolean isEditorMode )
    {
    }
    
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData, boolean isEditorMode )
    {
    }
    
    @Override
    public void onPitsEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_PITS_ENTERED );
        }
    }
    
    @Override
    public void onPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_PITS_EXITED );
        }
    }
    
    @Override
    public void onGarageEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_GARAGE_ENTERED );
        }
    }
    
    @Override
    public void onGarageExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_GARAGE_EXITED );
        }
    }
    
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_VEHICLE_CONTROL_CHANGED );
            communicator.writeInt( viewedVSI.getDriverId() );
            communicator.writeByte( viewedVSI.getVehicleControl().ordinal() );
        }
    }
    
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_LAP_STARTED );
            communicator.writeInt( vsi.getDriverId() );
            communicator.writeShort( vsi.getCurrentLap() );
        }
    }
    
    private void sendDriversName( VehicleScoringInfo vsi, boolean andPlace )
    {
        communicator.writeInt( vsi.getDriverId() );
        if ( andPlace )
            communicator.writeShort( vsi.getPlace( false ) );
        communicator.writeByte( vsi.getDriverName().length() );
        communicator.write( vsi.getDriverName().getBytes() );
    }
    
    private void sendDriversList( LiveGameData gameData )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        communicator.writeCommand( DirectorConstants.DRIVERS_LIST );
        
        int n = scoringInfo.getNumVehicles();
        communicator.writeShort( n );
        
        for ( int i = 0; i < n; i++ )
        {
            sendDriversName( scoringInfo.getVehicleScoringInfo( i ), false );
        }
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        lastGameData = gameData;
        
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_SESSION_STARTED );
            communicator.writeByte( gameData.getScoringInfo().getSessionType().ordinal() );
            sendDriversList( gameData );
        }
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        isInRealtimeMode = true;
        
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_REALTIME_ENTERED );
        }
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_GAME_PAUSE_STATE_CHANGED );
            communicator.writeBoolean( isPaused );
        }
    }
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, boolean isEditorMode )
    {
        isInRealtimeMode = false;
        
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_REALTIME_EXITED );
        }
    }
    
    @Override
    public void onPlayerJoined( LiveGameData gameData, VehicleScoringInfo joinedVSI, boolean rejoined )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_PLAYER_JOINED );
            sendDriversName( joinedVSI, true );
        }
    }
    
    @Override
    public void onPlayerLeft( LiveGameData gameData, Integer vsiID )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.ON_PLAYER_LEFT );
            communicator.writeInt( vsiID.intValue() );
        }
    }
    
    private void sendDriversPositions( LiveGameData gameData )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        communicator.writeCommand( DirectorConstants.DRIVERS_POSITIONS );
        
        int n = scoringInfo.getNumVehicles();
        communicator.writeShort( n );
        
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            communicator.writeInt( vsi.getDriverId() );
            //communicator.writeShort( vsi.getPlace( false ) );
        }
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeCommand( DirectorConstants.SESSION_TIME );
            communicator.writeLong( gameData.getScoringInfo().getSessionNanos() );
            
            sendDriversPositions( gameData );
        }
    }
    
    @Override
    public void beforeWidgetsConfigurationCleared( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
    }
    
    public void onWidgetsConfigReceived( byte[] configData, int length )
    {
        if ( isEditorMode )
            return;
        
        //debug( "Widgets config received. length: ", length );
        this.configLength = length;
        this.configData = configData;
    }
    
    @Override
    public void afterWidgetsConfigurationLoaded( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
        if ( isEditorMode )
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
        if ( isEditorMode )
            return;
        
        DirectorWidgetController controller = widgetControllersMap.get( widgetName );
        //debug( "Received WidgetState: ", widgetName, ", ", visibleStart, ", ", visibleEnd, ", ", posX, ", ", posY, ", ", viewedVSIid, ", ", compareVSIId );
        
        if ( controller != null )
        {
            controller.setWidgetState( visibleStart, visibleEnd, posX, posY, viewedVSIid, compareVSIId );
        }
    }
}
