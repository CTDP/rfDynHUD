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
package net.ctdp.rfdynhud.test.optimization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.gamedata.GameEventsListener;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GameEventsPlugin;
import net.ctdp.rfdynhud.gamedata.GraphicsInfo.GraphicsInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo.ScoringInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.SupportedGames;
import net.ctdp.rfdynhud.gamedata.TelemetryData.TelemetryDataUpdateListener;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.render.WidgetsRenderListener;
import net.ctdp.rfdynhud.render.WidgetsRenderListenersManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SimulationRecorderPlugin implements GameEventsPlugin, GameEventsListener, GraphicsInfoUpdateListener, TelemetryDataUpdateListener, ScoringInfoUpdateListener, WidgetsRenderListener
{
    private DataOutputStream os = null;
    
    public static final char ON_PHYSICS = 'p';
    public static final char ON_SETUP = 'u';
    public static final char ON_TRACK = 't';
    public static final char ON_PITS_ENTERED = 'B';
    public static final char ON_PITS_EXITED = 'b';
    public static final char ON_GARAGE_ENTERED = 'G';
    public static final char ON_GARAGE_EXITED = 'g';
    public static final char ON_CONTROL = 'c';
    public static final char ON_LAP = 'l';
    public static final char ON_SESSION_STARTED = 'S';
    public static final char ON_SESSION_ENDED = 's';
    public static final char ON_REALTIME_ENTERED = 'R';
    public static final char ON_REALTIME_EXITED = 'r';
    public static final char ON_VIEWPORT_CHANGED = 'v';
    public static final char ON_DATA_UPDATED = 'D';
    public static final char ON_DATA_UPDATED_TELEMETRY = 'T';
    public static final char ON_DATA_SCORING = 'S';
    public static final char BEFORE_RENDERED = 'W';
    
    /**
     * 
     * @param baseFolder
     */
    public SimulationRecorderPlugin( File baseFolder )
    {
    }
    
    @Override
    public void onPluginStarted( LiveGameData gameData, boolean isEditorMode, WidgetsRenderListenersManager renderListenerManager )
    {
        if ( isEditorMode )
            return;
        
        try
        {
            os = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( "D:\\rfdynhud_data" ) ) );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        
        gameData.registerGameEventsListener( this );
        gameData.registerDataUpdateListener( this );
        //gameData.getCommentaryRequestInfo().registerListener( this );
        gameData.getGraphicsInfo().registerListener( this );
        gameData.getTelemetryData().registerListener( this );
        gameData.getScoringInfo().registerListener( this );
        renderListenerManager.registerListener( this );
    }
    
    @Override
    public void onPluginShutdown( LiveGameData gameData, boolean isEditorMode, WidgetsRenderListenersManager renderListenerManager )
    {
        if ( isEditorMode )
            return;
        
        try
        {
            os.close();
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onVehiclePhysicsUpdated( LiveGameData gameData )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_PHYSICS );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onVehicleSetupUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_SETUP );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onTrackChanged( String trackname, LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_TRACK );
            os.writeShort( trackname.length() );
            os.writeChars( trackname );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onPitsEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_PITS_ENTERED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_PITS_EXITED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onGarageEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_GARAGE_ENTERED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onGarageExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_GARAGE_EXITED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
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
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_CONTROL );
            os.writeShort( getVSIIndex( viewedVSI, gameData.getScoringInfo() ) );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        */
    }
    
    @Override
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_LAP );
            os.writeShort( getVSIIndex( vsi, gameData.getScoringInfo() ) );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_SESSION_STARTED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_REALTIME_ENTERED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused )
    {
    }
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_REALTIME_EXITED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onViewportChanged( LiveGameData gameData, int viewportX, int viewportY, int viewportWidth, int viewportHeight )
    {
        /*
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_VIEWPORT_CHANGED );
            os.writeShort( viewportX );
            os.writeShort( viewportY );
            os.writeShort( viewportWidth );
            os.writeShort( viewportHeight );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        */
    }
    
    @Override
    public void onGraphicsInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
    }
    
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_DATA_UPDATED );
            os.write( ON_DATA_UPDATED_TELEMETRY );
            gameData.getTelemetryData().writeToStream( os );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( ON_DATA_UPDATED );
            os.write( ON_DATA_SCORING );
            gameData.getScoringInfo().writeToStream( os );
        }
        catch ( IOException e )
        {
            Logger.log( e );
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
        if ( !gameData.isInRealtimeMode() )
            return;
        
        try
        {
            os.write( BEFORE_RENDERED );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
    }
    
    public static void main( String[] args ) throws Throwable
    {
        DataInputStream in = new DataInputStream( new BufferedInputStream( new FileInputStream( "D:\\rfdynhud_data2" ) ) );
        
        RFDynHUD rfDynHUD = new RFDynHUD( SupportedGames.rFactor.name(), 1920, 1200 );
        LiveGameData gameData = rfDynHUD.getGameData();
        //_LiveGameData_CPP_Adapter gdcpp = rfDynHUD.getGameData_CPP_Adapter();
        GameEventsManager eventsManager = rfDynHUD.getEventsManager();
        GameEventsManager.simulationMode = true;
        
        eventsManager.onStartup();
        eventsManager.onSessionStarted();
        eventsManager.onRealtimeEntered();
        eventsManager.onGraphicsInfoUpdated( (short)0, (short)0, (short)1920, (short)1200 );
        
        boolean isReady1 = false;
        boolean isReady2 = false;
        boolean isFirstReady = true;
        
        try
        {
            int len = 0;
            int type = 0;
            
            int b = 0;
            while ( ( b = in.read() ) != -1 )
            {
                switch ( b )
                {
                    case ON_PHYSICS:
                        break;
                    case ON_SETUP:
                        break;
                    case ON_TRACK:
                        len = in.readShort();
                        for ( int i = 0; i < len; i++ )
                            in.readChar();
                        break;
                    case ON_PITS_ENTERED:
                        break;
                    case ON_PITS_EXITED:
                        break;
                    case ON_GARAGE_ENTERED:
                        break;
                    case ON_GARAGE_EXITED:
                        break;
                    case ON_CONTROL:
                        in.readShort();
                        break;
                    case ON_LAP:
                        //System.out.println( "lap" );
                        in.readShort();
                        break;
                    case ON_SESSION_STARTED:
                        break;
                    case ON_SESSION_ENDED:
                        break;
                    case ON_REALTIME_ENTERED:
                        break;
                    case ON_REALTIME_EXITED:
                        break;
                    case ON_VIEWPORT_CHANGED:
                        in.readShort();
                        in.readShort();
                        in.readShort();
                        in.readShort();
                        break;
                    case ON_DATA_UPDATED:
                        type = in.read();
                        switch ( type )
                        {
                            case ON_DATA_UPDATED_TELEMETRY:
                                //System.out.println( "telemetry" );
                                gameData.getTelemetryData().readFromStream( in );
                                eventsManager.onTelemetryDataUpdated();
                                isReady1 = true;
                                break;
                            case ON_DATA_SCORING:
                                //System.out.println( "scoring" );
                                gameData.getScoringInfo().readFromStream( in );
                                eventsManager.onScoringInfoUpdated();
                                isReady2 = true;
                                break;
                        }
                        break;
                    case BEFORE_RENDERED:
                        //System.out.println( "render" );
                        if ( isReady1 && isReady2 )
                        {
                            if ( isFirstReady )
                            {
                                JOptionPane.showMessageDialog( null, "start" );
                                
                                isFirstReady = false;
                            }
                            
                            rfDynHUD.update();
                        }
                        break;
                }
            }
        }
        finally
        {
            in.close();
        }
        
        eventsManager.onShutdown();
    }
}
