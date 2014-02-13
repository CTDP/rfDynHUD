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

import net.ctdp.rfdynhud.gamedata.GameEventsListener;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo.ScoringInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.plugins.GameEventsPlugin;
import net.ctdp.rfdynhud.render.WidgetsManager;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractDataSenderPlugin extends GameEventsPlugin implements GameEventsListener, ScoringInfoUpdateListener
{
    private final File iniFile;
    
    private boolean isEditorMode = false;
    private boolean isInCockpit = false;
    
    private boolean enabled = false;
    protected AbstractServerCommunicator communicator = null;
    
    //private GameEventsManager eventsManager = null;
    
    //private LiveGameData lastGameData = null;
    //private boolean isInCockpit = false;
    
    protected AbstractDataSenderPlugin( String name, File baseFolder, File iniFile )
    {
        super( name, baseFolder );
        
        this.iniFile = iniFile;
    }
    
    public final boolean isEditorMode()
    {
        return ( isEditorMode );
    }
    
    protected void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }
    
    public final boolean isEnabled()
    {
        return ( enabled );
    }
    
    protected abstract void parseIniFile( File iniFile, GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager );
    
    /**
     * 
     * @param eventsManager
     * @param gameData
     * @param isEditorMode
     * @param widgetsManager
     */
    protected void registerListeners( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        gameData.registerGameEventsListener( this );
        gameData.registerDataUpdateListener( this );
        gameData.getScoringInfo().registerListener( this );
    }
    
    @Override
    public void onPluginStarted( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        this.isEditorMode = isEditorMode;
        
        parseIniFile( iniFile, eventsManager, gameData, isEditorMode, widgetsManager );
        
        if ( !enabled )
            return;
        
        if ( communicator == null )
        {
            this.enabled = false;
            log( "ERROR: " + getClass().getName() + " didn't initialize the communicator." );
            return;
        }
        
        registerListeners( eventsManager, gameData, isEditorMode, widgetsManager );
        
        communicator.connect();
    }
    
    /**
     * 
     * @param eventsManager
     * @param gameData
     * @param isEditorMode
     * @param widgetsManager
     */
    protected void unregisterListeners( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        gameData.unregisterGameEventsListener( this );
        gameData.unregisterDataUpdateListener( this );
        gameData.getScoringInfo().unregisterListener( this );
    }
    
    @Override
    public void onPluginShutdown( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( enabled && ( communicator != null ) )
        {
            communicator.close( false );
        }
        
        unregisterListeners( eventsManager, gameData, isEditorMode, widgetsManager );
    }
    
    protected void onConnectionEsteblished()
    {
        debug( "Connection esteblished" );
    }
    
    protected void onConnectionClosed()
    {
        debug( "Connection closed" );
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
            communicator.writeSimpleCommand( CommunicatorConstants.ON_PITS_ENTERED );
        }
    }
    
    @Override
    public void onPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeSimpleCommand( CommunicatorConstants.ON_PITS_EXITED );
        }
    }
    
    @Override
    public void onGarageEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeSimpleCommand( CommunicatorConstants.ON_GARAGE_ENTERED );
        }
    }
    
    @Override
    public void onGarageExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.writeSimpleCommand( CommunicatorConstants.ON_GARAGE_EXITED );
        }
    }
    
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( CommunicatorConstants.ON_VEHICLE_CONTROL_CHANGED );
            communicator.writeInt( viewedVSI.getDriverId() );
            communicator.writeByte( viewedVSI.getVehicleControl().ordinal() );
            communicator.endCommand();
        }
    }
    
    protected void sendDriversName( VehicleScoringInfo vsi, boolean andPlace )
    {
        communicator.writeInt( vsi.getDriverId() );
        if ( andPlace )
            communicator.writeShort( vsi.getPlace( false ) );
        communicator.writeByte( vsi.getDriverName().length() );
        communicator.write( vsi.getDriverName().getBytes() );
    }
    
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( CommunicatorConstants.ON_LAP_STARTED );
            communicator.writeInt( vsi.getDriverId() );
            communicator.writeShort( vsi.getCurrentLap() );
            communicator.endCommand();
        }
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( CommunicatorConstants.ON_SESSION_STARTED );
            communicator.writeByte( gameData.getScoringInfo().getSessionType().ordinal() );
            communicator.endCommand();
        }
    }
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        isInCockpit = true;
        
        if ( communicator.isConnected() )
        {
            communicator.writeSimpleCommand( CommunicatorConstants.ON_COCKPIT_ENTERED );
        }
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( CommunicatorConstants.ON_GAME_PAUSE_STATE_CHANGED );
            communicator.writeBoolean( isPaused );
            communicator.endCommand();
        }
    }
    
    @Override
    public void onCockpitExited( LiveGameData gameData, boolean isEditorMode )
    {
        isInCockpit = false;
        
        if ( communicator.isConnected() )
        {
            communicator.writeSimpleCommand( CommunicatorConstants.ON_COCKPIT_EXITED );
        }
    }
    
    public final boolean isInCockpit()
    {
        return ( isInCockpit );
    }
    
    @Override
    public void onPlayerJoined( LiveGameData gameData, VehicleScoringInfo joinedVSI, boolean rejoined )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( CommunicatorConstants.ON_PLAYER_JOINED );
            sendDriversName( joinedVSI, true );
            communicator.endCommand();
        }
    }
    
    @Override
    public void onPlayerLeft( LiveGameData gameData, Integer vsiID )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( CommunicatorConstants.ON_PLAYER_LEFT );
            communicator.writeInt( vsiID.intValue() );
            communicator.endCommand();
        }
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( CommunicatorConstants.SESSION_TIME );
            communicator.writeLong( gameData.getScoringInfo().getSessionNanos() );
            communicator.endCommand();
        }
    }
}
