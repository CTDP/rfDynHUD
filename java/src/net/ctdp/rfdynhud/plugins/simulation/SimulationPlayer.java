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

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.gamedata.GameDataStreamSource;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;

/**
 * Capable of playing back recorded data events simulations.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SimulationPlayer
{
    public static interface PlaybackUpdateInterface
    {
        public void update( boolean firstTime );
        
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
    
    private static long waitForTimeCode( long t0, long t, boolean ignoreTiming, PlaybackUpdateInterface updateInterface ) throws InterruptedException
    {
        if ( !ignoreTiming && ( t0 != -1L ) )
        {
            long waitTime = Math.min( t - t0, 1000L );
            
            while ( waitTime > 0L )
            {
                if ( ( updateInterface != null ) && updateInterface.isCancelled() )
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
    
    public static void playback( GameEventsManager eventsManager, Object syncMonitor, File file, boolean ignoreTiming, PlaybackUpdateInterface updateInterface ) throws Throwable
    {
        boolean oldSimMode = GameEventsManager.simulationMode;
        GameEventsManager.simulationMode = true;
        
        if ( syncMonitor == null )
            syncMonitor = new Object();
        
        try
        {
            DataInputStream in = new DataInputStream( new BufferedInputStream( new FileInputStream( file ) ) );
            SimDataStreamSource simUserObject = new SimDataStreamSource( in );
            
            //LiveGameData gameData = eventsManager.getGameData();
            
            boolean isGraphicsReady = false;
            boolean isTelementryReady = false;
            boolean isScoringReady = false;
            boolean isFirstReady = true;
            
            long t0 = -1L;
            
            try
            {
                int len = 0;
                
                int b = 0;
                while ( ( b = in.read() ) != -1 )
                {
                    if ( ( updateInterface != null ) && updateInterface.isCancelled() )
                        break;
                    
                    switch ( b )
                    {
                        case SimulationConstants.ON_PHYSICS:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_SETUP:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_TRACK:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            len = in.readShort();
                            for ( int i = 0; i < len; i++ )
                                in.readChar();
                            break;
                        case SimulationConstants.ON_PITS_ENTERED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_PITS_EXITED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_GARAGE_ENTERED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_GARAGE_EXITED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_CONTROL:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            in.readShort();
                            break;
                        case SimulationConstants.ON_LAP:
                            //System.out.println( "lap" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            in.readShort();
                            in.readShort(); // TODO: remove
                            break;
                        case SimulationConstants.ON_SESSION_STARTED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_SESSION_ENDED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_COCKPIT_ENTERED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            t0 = -1L; // Avoid mysterious wait time!
                            break;
                        case SimulationConstants.ON_COCKPIT_EXITED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            break;
                        case SimulationConstants.ON_VIEWPORT_CHANGED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            in.readShort();
                            in.readShort();
                            in.readShort();
                            in.readShort();
                            break;
                        case SimulationConstants.ON_DATA_UPDATED_GRAPHICS:
                            //System.out.println( "graphics" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            synchronized ( syncMonitor )
                            {
                                eventsManager.onGraphicsInfoUpdated( simUserObject );
                            }
                            isGraphicsReady = true;
                            break;
                        case SimulationConstants.ON_DATA_UPDATED_TELEMETRY:
                            //System.out.println( "telemetry" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            synchronized ( syncMonitor )
                            {
                                eventsManager.onTelemetryDataUpdated( simUserObject );
                            }
                            isTelementryReady = true;
                            break;
                        case SimulationConstants.ON_DATA_UPDATED_SCORING:
                            //System.out.println( "scoring" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            int numVehicles = in.readInt();
                            synchronized ( syncMonitor )
                            {
                                eventsManager.onScoringInfoUpdated( numVehicles, simUserObject );
                            }
                            isScoringReady = true;
                            break;
                        case SimulationConstants.ON_DATA_UPDATED_DRIVING_AIDS:
                            //System.out.println( "driving_aids" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            synchronized ( syncMonitor )
                            {
                                eventsManager.onDrivingAidsUpdated( simUserObject );
                            }
                            break;
                        case SimulationConstants.ON_DATA_UPDATED_COMMENTARY:
                            //System.out.println( "commentary" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            synchronized ( syncMonitor )
                            {
                                eventsManager.onCommentaryRequestInfoUpdated( simUserObject );
                            }
                            break;
                        case SimulationConstants.BEFORE_RENDERED:
                            //System.out.println( "render" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming, updateInterface );
                            if ( isGraphicsReady && isTelementryReady && isScoringReady )
                            {
                                if ( updateInterface != null )
                                    updateInterface.update( isFirstReady );
                                
                                isFirstReady = false;
                            }
                            break;
                    }
                }
            }
            finally
            {
                in.close();
            }
        }
        finally
        {
            GameEventsManager.simulationMode = oldSimMode;
        }
    }
    
    public static void main( String[] args ) throws Throwable
    {
        boolean oldSimMode = GameEventsManager.simulationMode;
        GameEventsManager.simulationMode = true;
        
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
            
            SimulationPlayer.PlaybackUpdateInterface updateInterfce = new SimulationPlayer.PlaybackUpdateInterface()
            {
                @Override
                public void update( boolean firstTime )
                {
                    if ( firstTime )
                        //javax.swing.JOptionPane.showMessageDialog( null, "start" );
                    
                    rfDynHUD.update();
                }
                
                @Override
                public boolean isCancelled()
                {
                    return ( false );
                }
            };
            
            SimulationPlayer.playback( eventsManager, null, new File( file ), true, updateInterfce );
            
            eventsManager.onShutdown( System.nanoTime() );
        }
        finally
        {
            GameEventsManager.simulationMode = oldSimMode;
        }
    }
}
