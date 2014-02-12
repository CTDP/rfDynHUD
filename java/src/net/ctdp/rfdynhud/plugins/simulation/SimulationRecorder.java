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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.CommentaryRequestInfo.CommentaryInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.DrivingAids.DrivingAidsUpdateListener;
import net.ctdp.rfdynhud.gamedata.GameEventsListener;
import net.ctdp.rfdynhud.gamedata.GraphicsInfo.GraphicsInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo.ScoringInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.TelemetryData.TelemetryDataUpdateListener;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.plugins.GameEventsPlugin;
import net.ctdp.rfdynhud.render.WidgetsManager;
import net.ctdp.rfdynhud.render.WidgetsRenderListener;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

import org.jagatoo.logging.Log;
import org.jagatoo.util.streams.StreamUtils;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SimulationRecorder implements GameEventsListener, GraphicsInfoUpdateListener, TelemetryDataUpdateListener, ScoringInfoUpdateListener, DrivingAidsUpdateListener, CommentaryInfoUpdateListener, WidgetsRenderListener
{
    private final String logPrefix;
    
    private final boolean onlyRecordInCockpit;
    
    private DataOutputStream os = null;
    
    private long t0 = -1L;
    
    public void log( Object... message )
    {
        if ( ( message != null ) && ( message.length > 0 ) )
        {
            if ( ( message.length == 1 ) && ( message[0] instanceof Throwable ) )
            {
                Log.println( GameEventsPlugin.LOG_CHANNEL, message );
            }
            else
            {
                Object[] message2 = new Object[ message.length + 1 ];
                message2[0] = logPrefix;
                System.arraycopy( message, 0, message2, 1, message.length );
                Log.println( GameEventsPlugin.LOG_CHANNEL, message2 );
            }
        }
    }
    
    public void wireListeners( LiveGameData gameData, WidgetsManager widgetsManager )
    {
        gameData.registerGameEventsListener( this );
        gameData.registerDataUpdateListener( this );
        gameData.getCommentaryRequestInfo().registerListener( this );
        gameData.getGraphicsInfo().registerListener( this );
        gameData.getTelemetryData().registerListener( this );
        gameData.getScoringInfo().registerListener( this );
        gameData.getDrivingAids().registerListener( this );
        widgetsManager.registerListener( this );
    }
    
    public void unwireListeners( LiveGameData gameData, WidgetsManager widgetsManager )
    {
        gameData.unregisterGameEventsListener( this );
        gameData.unregisterDataUpdateListener( this );
        gameData.getCommentaryRequestInfo().unregisterListener( this );
        gameData.getGraphicsInfo().unregisterListener( this );
        gameData.getTelemetryData().unregisterListener( this );
        gameData.getScoringInfo().unregisterListener( this );
        gameData.getDrivingAids().unregisterListener( this );
        widgetsManager.unregisterListener( this );
    }
    
    private void writeTimecode() throws IOException
    {
        if ( t0 == -1L )
            t0 = System.nanoTime() / 1000000L;
        
        long t = ( System.nanoTime() / 1000000L ) - t0;
        
        os.writeLong( t );
    }
    
    @Override
    public void onVehiclePhysicsUpdated( LiveGameData gameData )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_PHYSICS );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_SETUP );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_TRACK );
            writeTimecode();
            os.writeShort( trackname.length() );
            os.writeChars( trackname );
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onPitsEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_PITS_ENTERED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_PITS_EXITED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onGarageEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_GARAGE_ENTERED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onGarageExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_GARAGE_EXITED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    private static final int getVSIIndex( VehicleScoringInfo vsi, ScoringInfo scoringInfo )
    {
        for ( int i = 0; i < scoringInfo.getNumVehicles(); i++ )
        {
            if ( scoringInfo.getVehicleScoringInfo( i ) == vsi )
            {
                return ( i );
            }
        }
        
        return ( -1 );
    }
    
    @Override
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        /*
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_CONTROL );
            writeTimecode();
            os.writeShort( getVSIIndex( viewedVSI, gameData.getScoringInfo() ) );
        }
        catch ( IOException e )
        {
            log( e );
        }
        */
    }
    
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_LAP );
            writeTimecode();
            os.writeShort( getVSIIndex( vsi, gameData.getScoringInfo() ) );
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        //if ( onlyRecordInCockpit && !gameData.isInCockpit() )
        //    return;
        
        try
        {
            os.write( SimulationConstants.ON_SESSION_STARTED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        try
        {
            os.write( SimulationConstants.ON_COCKPIT_ENTERED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused )
    {
    }
    
    @Override
    public void onCockpitExited( LiveGameData gameData, boolean isEditorMode )
    {
        try
        {
            os.write( SimulationConstants.ON_COCKPIT_EXITED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onViewportChanged( LiveGameData gameData, int viewportX, int viewportY, int viewportWidth, int viewportHeight )
    {
        /*
        if ( onlyRecordInCockpit && !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_VIEWPORT_CHANGED );
            writeTimecode();
            os.writeShort( viewportX );
            os.writeShort( viewportY );
            os.writeShort( viewportWidth );
            os.writeShort( viewportHeight );
        }
        catch ( IOException e )
        {
            log( e );
        }
        */
    }
    
    @Override
    public void onGraphicsInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_DATA_UPDATED_GRAPHICS );
            writeTimecode();
            gameData.getGraphicsInfo().writeToStream( os );
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_DATA_UPDATED_TELEMETRY );
            writeTimecode();
            gameData.getTelemetryData().writeToStream( os );
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onPlayerJoined( LiveGameData gameData, VehicleScoringInfo joinedVSI, boolean rejoined )
    {
    }
    
    @Override
    public void onPlayerLeft( LiveGameData gameData, Integer vsiID )
    {
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        if ( gameData.getScoringInfo().getNumVehicles() == 0 )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_DATA_UPDATED_SCORING );
            writeTimecode();
            os.writeInt( gameData.getScoringInfo().getNumVehicles() );
            gameData.getScoringInfo().writeToStream( os );
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onDrivingAidsUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_DATA_UPDATED_DRIVING_AIDS );
            writeTimecode();
            gameData.getDrivingAids().writeToStream( os );
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void onCommentaryInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.ON_DATA_UPDATED_COMMENTARY );
            writeTimecode();
            gameData.getCommentaryRequestInfo().writeToStream( os );
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    public void beforeWidgetsConfigurationCleared( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
    }
    
    @Override
    public void afterWidgetsConfigurationLoaded( LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
    }
    
    @Override
    public void beforeWidgetsAreRendered( LiveGameData gameData, WidgetsConfiguration widgetsConfig, long sessionTime, long frameCounter )
    {
        if ( onlyRecordInCockpit && !gameData.isInCockpit() )
            return;
        
        try
        {
            os.write( SimulationConstants.BEFORE_RENDERED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    public void close()
    {
        StreamUtils.closeStream( os );
    }
    
    public SimulationRecorder( File file, boolean onlyRecordInCockpit, String logPrefix ) throws IOException
    {
        this.logPrefix = logPrefix;
        this.onlyRecordInCockpit = onlyRecordInCockpit;
        
        this.os = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( file ) ) );
    }
    
    public SimulationRecorder( File file, String logPrefix ) throws IOException
    {
        this( file, true, logPrefix );
    }
}
