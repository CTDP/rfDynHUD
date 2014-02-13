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
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.CommentaryRequestInfo;
import net.ctdp.rfdynhud.gamedata.DrivingAids;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GraphicsInfo;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.WeatherInfo;
import net.ctdp.rfdynhud.render.WidgetsManager;

import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DataSenderPlugin extends AbstractDataSenderPlugin implements GraphicsInfo.GraphicsInfoUpdateListener, TelemetryData.TelemetryDataUpdateListener, WeatherInfo.WeatherInfoUpdateListener, DrivingAids.DrivingAidStateChangeListener, CommentaryRequestInfo.CommentaryInfoUpdateListener
{
    private static final String INI_FILENAME = "datasender.ini";
    
    private LiveGameData startGameData = null;
    
    public DataSenderPlugin( File baseFolder )
    {
        super( "DataSender", baseFolder, new File( baseFolder, INI_FILENAME ) );
    }
    
    @Override
    public void onPluginStarted( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.onPluginStarted( eventsManager, gameData, isEditorMode, widgetsManager );
        
        this.startGameData = gameData;
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
    protected void registerListeners( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.registerListeners( eventsManager, gameData, isEditorMode, widgetsManager );
        
        gameData.getGraphicsInfo().registerListener( this );
        gameData.getTelemetryData().registerListener( this );
        gameData.getScoringInfo().registerListener( this );
        gameData.getWeatherInfo().registerListener( this );
        gameData.getDrivingAids().registerListener( this );
        gameData.getCommentaryRequestInfo().registerListener( this );
    }
    
    @Override
    protected void unregisterListeners( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.unregisterListeners( eventsManager, gameData, isEditorMode, widgetsManager );
        
        gameData.getGraphicsInfo().unregisterListener( this );
        gameData.getTelemetryData().unregisterListener( this );
        gameData.getScoringInfo().unregisterListener( this );
        gameData.getWeatherInfo().unregisterListener( this );
        gameData.getDrivingAids().unregisterListener( this );
        gameData.getCommentaryRequestInfo().unregisterListener( this );
    }
    
    @Override
    public void onPluginShutdown( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        super.onPluginShutdown( eventsManager, gameData, isEditorMode, widgetsManager );
        
        this.startGameData = null;
    }
    
    @Override
    protected void onConnectionEsteblished()
    {
        super.onConnectionEsteblished();
        
        if ( startGameData.getGraphicsInfo().isUpdatedInTimeScope() )
            sendGraphicsInfo( startGameData.getGraphicsInfo() );
        
        if ( startGameData.getTelemetryData().isUpdatedInTimeScope() )
            sendTelemetryData( startGameData.getTelemetryData() );
        
        if ( startGameData.getScoringInfo().isUpdatedInTimeScope() )
            sendScoringInfo( startGameData.getScoringInfo() );
        
        if ( startGameData.getDrivingAids().isUpdatedInTimeScope() )
            sendDrivingAids( startGameData.getDrivingAids() );
        
        if ( startGameData.getCommentaryRequestInfo().isUpdatedInTimeScope() )
            sendCommentaryRequestInfo( startGameData.getCommentaryRequestInfo() );
    }
    
    @Override
    protected void onConnectionClosed()
    {
        super.onConnectionClosed();
    }
    
    private void sendGraphicsInfo( GraphicsInfo graphicsInfo )
    {
        communicator.startCommand( DataSenderConstants.GRAPHICS_INFO );
        
        try
        {
            graphicsInfo.writeToStream( communicator.getOutputStream() );
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        communicator.endCommand();
    }
    
    private long nextGraphicsInfoSendTime = -( Long.MAX_VALUE / 2L );
    //private int graphicsInfoCounter = 0;
    
    @Override
    public void onGraphicsInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            long now = System.nanoTime();
            if ( now >= nextGraphicsInfoSendTime )
            {
                sendGraphicsInfo( gameData.getGraphicsInfo() );
                
                nextGraphicsInfoSendTime = now + 500000000L;
            }
        }
    }
    
    @Override
    public void onViewportChanged( LiveGameData gameData, int viewportX, int viewportY, int viewportWidth, int viewportHeight )
    {
    }
    
    private void sendTelemetryData( TelemetryData telemData )
    {
        communicator.startCommand( DataSenderConstants.TELEMETRY_DATA );
        
        try
        {
            telemData.writeToStream( communicator.getOutputStream() );
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        communicator.endCommand();
    }
    
    @SuppressWarnings( "unused" )
    private long nextTelemDataSendTime = -( Long.MAX_VALUE / 2L );
    @SuppressWarnings( "unused" )
    private int telemDataCounter = 0;
    
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            long now = System.nanoTime();
            //if ( ( ( telemDataCounter % 5 ) == 0 ) || ( now >= nextTelemDataSendTime ) )
            {
                sendTelemetryData( gameData.getTelemetryData() );
                
                telemDataCounter = 1;
                
                nextTelemDataSendTime = now + 100000000L;
            }
            /*
            else
            {
                telemDataCounter++;
            }
            */
        }
    }
    
    private void sendScoringInfo( ScoringInfo scoringInfo )
    {
        communicator.startCommand( DataSenderConstants.SCORING_INFO );
        
        communicator.writeInt( scoringInfo.getNumVehicles() );
        
        try
        {
            scoringInfo.writeToStream( communicator.getOutputStream() );
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        communicator.endCommand();
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        super.onScoringInfoUpdated( gameData, isEditorMode );
        
        if ( communicator.isConnected() )
        {
            sendScoringInfo( gameData.getScoringInfo() );
        }
    }
    
    private void sendWeatherInfo( WeatherInfo weatherInfo )
    {
        communicator.startCommand( DataSenderConstants.SCORING_INFO );
        
        try
        {
            weatherInfo.writeToStream( communicator.getOutputStream() );
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        communicator.endCommand();
    }
    
    @Override
    public void onWeatherInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            sendWeatherInfo( gameData.getWeatherInfo() );
        }
    }
    
    private void sendDrivingAids( DrivingAids drivingAids )
    {
        communicator.startCommand( DataSenderConstants.DRIVING_AIDS );
        
        try
        {
            drivingAids.writeToStream( communicator.getOutputStream() );
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        communicator.endCommand();
    }
    
    @Override
    public void onDrivingAidsUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            sendDrivingAids( gameData.getDrivingAids() );
        }
    }
    
    @Override
    public void onDrivingAidStateChanged( LiveGameData gameData, int aidIndex, int oldState, int newState )
    {
        if ( communicator.isConnected() )
        {
            communicator.startCommand( DataSenderConstants.DRIVING_AIDS_STATE_CHANGED );
            communicator.writeInt( aidIndex );
            communicator.writeInt( oldState );
            communicator.writeInt( newState );
            communicator.endCommand();
        }
    }
    
    private void sendCommentaryRequestInfo( CommentaryRequestInfo commentaryRequestInfo )
    {
        communicator.startCommand( DataSenderConstants.COMMENTARY_REQUEST_INFO );
        
        try
        {
            commentaryRequestInfo.writeToStream( communicator.getOutputStream() );
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        communicator.endCommand();
    }
    
    @Override
    public void onCommentaryInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( communicator.isConnected() )
        {
            sendCommentaryRequestInfo( gameData.getCommentaryRequestInfo() );
        }
    }
}
