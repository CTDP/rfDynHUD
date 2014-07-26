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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.jagatoo.util.streams.StreamUtils;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.gamedata.GameDataStreamSource;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;

/**
 * Capable of playing back recorded data events simulations.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SimulationPlayer
{
    public static interface PlaybackControl
    {
        /**
         * Gets a time scale factor. A value <= 0 will make timing get totally ignored.
         * 
         * @return a time scale factor.
         */
        public float getTimeScale();
        
        public void update();
        
        public boolean isCancelled();
    }
    
    public static class SimDataStreamSource implements GameDataStreamSource
    {
        private final InputStream in;
        
        @Override
        public InputStream getInputStreamForGraphicsInfo()
        {
            return ( in );
        }
        
        @Override
        public InputStream getInputStreamForTelemetryData()
        {
            return ( in );
        }
        
        @Override
        public InputStream getInputStreamForScoringInfo()
        {
            return ( in );
        }
        
        @Override
        public InputStream getInputStreamForDrivingAids()
        {
            return ( in );
        }
        
        @Override
        public InputStream getInputStreamForCommentaryRequestInfo()
        {
            return ( in );
        }
        
        public SimDataStreamSource( InputStream in )
        {
            this.in = in;
        }
    };
    
    private static long waitForTimeCode( long t0, long t, PlaybackControl control ) throws InterruptedException
    {
        if ( ( control != null ) && control.isCancelled() )
            return ( t );
        
        if ( ( ( control == null ) || ( control.getTimeScale() > 0f ) ) && ( t0 != -1L ) )
        {
            long waitTime = t - t0;
            if ( control != null )
            {
                float timeScale = control.getTimeScale();
                if ( timeScale != 1.0f )
                    waitTime = (long)( waitTime / timeScale );
            }
            waitTime = Math.min( waitTime, 1000L );
            
            while ( waitTime > 0L )
            {
                if ( ( control != null ) && control.isCancelled() )
                    break;
                
                if ( waitTime > 100L )
                {
                    Thread.sleep( 100L );
                    waitTime -= 100L;
                }
                else
                {
                    Thread.sleep( waitTime );
                    waitTime = 0L;
                }
            }
        }
        
        return ( t );
    }
    
    public static void playback( GameEventsManager eventsManager, Object syncMonitor, File file, PlaybackControl control ) throws Throwable
    {
        boolean oldSimMode = __GDPrivilegedAccess.simulationMode;
        __GDPrivilegedAccess.simulationMode = true;
        
        if ( syncMonitor == null )
            syncMonitor = new Object();
        
        DataInputStream in = null;
        
        try
        {
            in = new DataInputStream( new BufferedInputStream( new GZIPInputStream( new FileInputStream( file ) ) ) );
            SimDataStreamSource simUserObject = new SimDataStreamSource( in );
            
            //LiveGameData gameData = eventsManager.getGameData();
            
            boolean firstSessionStart = true;
            
            boolean isGraphicsReady = false;
            boolean isTelementryReady = false;
            boolean isScoringReady = false;
            boolean isWeatherReady = false;
            
            long t0 = -1L;
            
            int len = 0;
            
            int code = 0;
            while ( ( code = in.read() ) != -1 )
            {
                if ( ( control != null ) && control.isCancelled() )
                    break;
                
                //System.out.println( "code: " + code + " ('" + (char)code + "')" );
                //System.out.println( "waiting for data: " + eventsManager.getWaitingForData( true ) );
                
                switch ( code )
                {
                    case SimulationConstants.ON_SESSION_STARTED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            if ( firstSessionStart )
                                eventsManager.onSessionEnded( simUserObject );
                            firstSessionStart = false;
                            eventsManager.onSessionStarted( simUserObject );
                        }
                        break;
                    case SimulationConstants.ON_SESSION_ENDED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            eventsManager.onSessionEnded( simUserObject );
                        }
                        break;
                    case SimulationConstants.ON_PHYSICS:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        break;
                    case SimulationConstants.ON_SETUP:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        break;
                    case SimulationConstants.ON_TRACK:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        len = in.readShort();
                        for ( int i = 0; i < len; i++ )
                            in.readChar();
                        break;
                    case SimulationConstants.ON_COCKPIT_ENTERED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        t0 = -1L; // Avoid mysterious wait time!
                        break;
                    case SimulationConstants.ON_COCKPIT_EXITED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        break;
                    case SimulationConstants.ON_GARAGE_ENTERED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        break;
                    case SimulationConstants.ON_GARAGE_EXITED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        break;
                    case SimulationConstants.ON_PITS_ENTERED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        break;
                    case SimulationConstants.ON_PITS_EXITED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        break;
                    case SimulationConstants.ON_CONTROL:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        in.readShort();
                        break;
                    case SimulationConstants.ON_LAP:
                        //System.out.println( "lap" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        in.readShort();
                        break;
                    case SimulationConstants.ON_DATA_UPDATED_DRIVING_AIDS:
                        //System.out.println( "driving_aids" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            eventsManager.onDrivingAidsUpdated( simUserObject );
                        }
                        break;
                    case SimulationConstants.ON_DATA_UPDATED_GRAPHICS:
                        //System.out.println( "graphics" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            eventsManager.onGraphicsInfoUpdated( simUserObject );
                        }
                        isGraphicsReady = true;
                        break;
                    case SimulationConstants.ON_DATA_UPDATED_TELEMETRY:
                        //System.out.println( "telemetry" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            eventsManager.onTelemetryDataUpdated( simUserObject );
                        }
                        isTelementryReady = true;
                        break;
                    case SimulationConstants.ON_DATA_UPDATED_SCORING:
                        //System.out.println( "scoring" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        int numVehicles = in.readInt();
                        synchronized ( syncMonitor )
                        {
                            eventsManager.onScoringInfoUpdated( numVehicles, simUserObject );
                        }
                        isScoringReady = true;
                        break;
                    case SimulationConstants.ON_DATA_UPDATED_WEATHER:
                        //System.out.println( "weather" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            eventsManager.onWeatherInfoUpdated( simUserObject );
                        }
                        isWeatherReady = true;
                        break;
                    case SimulationConstants.ON_DATA_UPDATED_COMMENTARY:
                        //System.out.println( "commentary" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            eventsManager.onCommentaryRequestInfoUpdated( simUserObject );
                        }
                        break;
                    case SimulationConstants.ON_VIEWPORT_CHANGED:
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        in.readShort();
                        in.readShort();
                        in.readShort();
                        in.readShort();
                        break;
                    case SimulationConstants.BEFORE_RENDERED:
                        //System.out.println( "render" );
                        t0 = waitForTimeCode( t0, in.readLong(), control );
                        synchronized ( syncMonitor )
                        {
                            eventsManager.beforeRender( (short)0, (short)0, (short)1920, (short)1080 );
                        }
                        if ( isGraphicsReady && isTelementryReady && isScoringReady && isWeatherReady )
                        {
                            if ( control != null )
                                control.update();
                        }
                        break;
                    default:
                        throw new IllegalStateException( "Unexpected command value: " + code + " ('" + (char)code + "')" );
                }
            }
            
            if ( !firstSessionStart )
                eventsManager.onSessionEnded( simUserObject );
        }
        finally
        {
            __GDPrivilegedAccess.simulationMode = oldSimMode;
            StreamUtils.closeStream( in );
        }
    }
    
    public static void main( String[] args ) throws Throwable
    {
        boolean oldSimMode = __GDPrivilegedAccess.simulationMode;
        __GDPrivilegedAccess.simulationMode = true;
        
        try
        {
            final RFDynHUD rfDynHUD = RFDynHUD.createInstance( new net.ctdp.rfdynhud.gamedata.rfactor2._rf2_LiveGameDataObjectsFactory(), 1920, 1080 );
            final GameEventsManager eventsManager = rfDynHUD.getEventsManager();
            
            final long now = System.nanoTime();
            eventsManager.onStartup( now );
            eventsManager.onSessionStarted( now );
            eventsManager.onCockpitEntered( now );
            eventsManager.onCommentaryRequestInfoUpdated( null );
            eventsManager.onGraphicsInfoUpdated( null );
            eventsManager.beforeRender( (short)0, (short)0, (short)1920, (short)1080 );
            
            //String file = "D:\\rfdynhud_data";
            String file = "c:\\Spiele\\rFactor2\\Plugins\\rfDynHUD\\plugins\\simulation\\simdata";
            
            SimulationPlayer.PlaybackControl control = new SimulationPlayer.PlaybackControl()
            {
                @Override
                public float getTimeScale()
                {
                    return ( 0f );
                }
                
                private boolean firstUpdate = true;
                
                @Override
                public void update()
                {
                    if ( firstUpdate )
                    {
                        //javax.swing.JOptionPane.showMessageDialog( null, "start" );
                        
                        firstUpdate = false;
                    }
                    
                    rfDynHUD.update();
                }
                
                @Override
                public boolean isCancelled()
                {
                    return ( false );
                }
            };
            
            SimulationPlayer.playback( eventsManager, null, new File( file ), control );
            
            eventsManager.onShutdown( System.nanoTime() );
        }
        finally
        {
            __GDPrivilegedAccess.simulationMode = oldSimMode;
        }
    }
}
