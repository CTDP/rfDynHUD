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
package net.ctdp.rfdynhud.test.optimization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.gamedata.CommentaryRequestInfo.CommentaryInfoUpdateListener;
import net.ctdp.rfdynhud.gamedata.DrivingAids.DrivingAidsUpdateListener;
import net.ctdp.rfdynhud.gamedata.GameDataStreamSource;
import net.ctdp.rfdynhud.gamedata.GameEventsListener;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
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

import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SimulationRecorderPlugin extends GameEventsPlugin implements GameEventsListener, GraphicsInfoUpdateListener, TelemetryDataUpdateListener, ScoringInfoUpdateListener, DrivingAidsUpdateListener, CommentaryInfoUpdateListener, WidgetsRenderListener
{
    public static final String INI_FILENAME = "sim_recorder.ini";
    
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
    public static final char ON_COCKPIT_ENTERED = 'R';
    public static final char ON_COCKPIT_EXITED = 'r';
    public static final char ON_VIEWPORT_CHANGED = 'v';
    public static final char ON_DATA_UPDATED_GRAPHICS = 'I';
    public static final char ON_DATA_UPDATED_TELEMETRY = 'T';
    public static final char ON_DATA_UPDATED_SCORING = 'D';
    public static final char ON_DATA_UPDATED_DRIVING_AIDS = 'A';
    public static final char ON_DATA_UPDATED_COMMENTARY = 'O';
    public static final char BEFORE_RENDERED = 'W';
    
    private final File iniFile;
    
    private boolean enabled = false;
    
    private boolean onlyRecordInCockpit = true;
    
    private DataOutputStream os = null;
    
    private long t0 = -1L;
    
    /**
     * 
     * @param baseFolder
     */
    public SimulationRecorderPlugin( File baseFolder )
    {
        super( "Simulation", baseFolder );
        
        iniFile = new File( baseFolder, INI_FILENAME );
    }
    
    @Override
    public void onPluginStarted( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( isEditorMode )
            return;
        
        File dataFile = null;
        
        if ( iniFile.exists() )
        {
            String enabled = AbstractIniParser.parseIniValue( iniFile, "SIMRECORDER", "enabled", null );
            
            if ( enabled != null )
                this.enabled = Boolean.parseBoolean( enabled );
            
            if ( !this.enabled )
                return;
            
            this.onlyRecordInCockpit = Boolean.parseBoolean( AbstractIniParser.parseIniValue( iniFile, "SIMRECORDER", "onlyRecordInCockpit", "true" ) );
            
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
            os = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( dataFile ) ) );
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        gameData.registerGameEventsListener( this );
        gameData.registerDataUpdateListener( this );
        gameData.getCommentaryRequestInfo().registerListener( this );
        gameData.getGraphicsInfo().registerListener( this );
        gameData.getTelemetryData().registerListener( this );
        gameData.getScoringInfo().registerListener( this );
        gameData.getDrivingAids().registerListener( this );
        widgetsManager.registerListener( this );
    }
    
    @Override
    public void onPluginShutdown( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager )
    {
        if ( isEditorMode || !enabled )
            return;
        
        gameData.unregisterGameEventsListener( this );
        gameData.unregisterDataUpdateListener( this );
        gameData.getCommentaryRequestInfo().unregisterListener( this );
        gameData.getGraphicsInfo().unregisterListener( this );
        gameData.getTelemetryData().unregisterListener( this );
        gameData.getScoringInfo().unregisterListener( this );
        gameData.getDrivingAids().unregisterListener( this );
        widgetsManager.unregisterListener( this );
        
        try
        {
            if ( os != null )
                os.close();
        }
        catch ( IOException e )
        {
            log( e );
        }
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
            os.write( ON_PHYSICS );
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
            os.write( ON_SETUP );
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
            os.write( ON_TRACK );
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
            os.write( ON_PITS_ENTERED );
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
            os.write( ON_PITS_EXITED );
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
            os.write( ON_GARAGE_ENTERED );
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
            os.write( ON_GARAGE_EXITED );
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
            os.write( ON_CONTROL );
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
            os.write( ON_LAP );
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
            os.write( ON_SESSION_STARTED );
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
            os.write( ON_COCKPIT_ENTERED );
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
            os.write( ON_COCKPIT_EXITED );
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
            os.write( ON_VIEWPORT_CHANGED );
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
            os.write( ON_DATA_UPDATED_GRAPHICS );
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
            os.write( ON_DATA_UPDATED_TELEMETRY );
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
            os.write( ON_DATA_UPDATED_SCORING );
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
            os.write( ON_DATA_UPDATED_DRIVING_AIDS );
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
            os.write( ON_DATA_UPDATED_COMMENTARY );
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
            os.write( BEFORE_RENDERED );
            writeTimecode();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    private static long waitForTimeCode( long t0, long t, boolean ignoreTiming ) throws InterruptedException
    {
        if ( !ignoreTiming && ( t0 != -1L ) )
        {
            Thread.sleep( t - t0 );
        }
        
        return ( t );
    }
    
    private static interface PlaybackUpdateInterface
    {
        public void update( boolean firstTime );
    }
    
    public static class SimUserObject implements GameDataStreamSource
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
        
        public SimUserObject( InputStream in )
        {
            this.in = in;
        }
    };
    
    public static void playback( GameEventsManager eventsManager, File file, boolean ignoreTiming, PlaybackUpdateInterface updateInterface ) throws Throwable
    {
        boolean oldSimMode = GameEventsManager.simulationMode;
        GameEventsManager.simulationMode = true;
        
        try
        {
            DataInputStream in = new DataInputStream( new BufferedInputStream( new FileInputStream( file ) ) );
            SimUserObject simUserObject = new SimUserObject( in );
            
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
                    switch ( b )
                    {
                        case ON_PHYSICS:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_SETUP:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_TRACK:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            len = in.readShort();
                            for ( int i = 0; i < len; i++ )
                                in.readChar();
                            break;
                        case ON_PITS_ENTERED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_PITS_EXITED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_GARAGE_ENTERED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_GARAGE_EXITED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_CONTROL:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            in.readShort();
                            break;
                        case ON_LAP:
                            //System.out.println( "lap" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            in.readShort();
                            in.readShort(); // TODO: remove
                            break;
                        case ON_SESSION_STARTED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_SESSION_ENDED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_COCKPIT_ENTERED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            t0 = -1L; // Avoid mysterious wait time!
                            break;
                        case ON_COCKPIT_EXITED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            break;
                        case ON_VIEWPORT_CHANGED:
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            in.readShort();
                            in.readShort();
                            in.readShort();
                            in.readShort();
                            break;
                        case ON_DATA_UPDATED_GRAPHICS:
                            //System.out.println( "graphics" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            //gameData.getGraphicsInfo().readFromStream( in, null );
                            eventsManager.onGraphicsInfoUpdated( simUserObject );
                            isGraphicsReady = true;
                            break;
                        case ON_DATA_UPDATED_TELEMETRY:
                            //System.out.println( "telemetry" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            //gameData.getTelemetryData().readFromStream( in, null );
                            eventsManager.onTelemetryDataUpdated( simUserObject );
                            isTelementryReady = true;
                            break;
                        case ON_DATA_UPDATED_SCORING:
                            //System.out.println( "scoring" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            int numVehicles = in.readInt();
                            //gameData.getScoringInfo().readFromStream( in, null );
                            eventsManager.onScoringInfoUpdated( numVehicles, simUserObject );
                            isScoringReady = true;
                            break;
                        case ON_DATA_UPDATED_DRIVING_AIDS:
                            //System.out.println( "driving_aids" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            //gameData.getDrivingAids().readFromStream( in, null );
                            eventsManager.onDrivingAidsUpdated( simUserObject );
                            break;
                        case ON_DATA_UPDATED_COMMENTARY:
                            //System.out.println( "commentary" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            //gameData.getCommentaryRequestInfo().readFromStream( in, null );
                            eventsManager.onCommentaryRequestInfoUpdated( simUserObject );
                            break;
                        case BEFORE_RENDERED:
                            //System.out.println( "render" );
                            t0 = waitForTimeCode( t0, in.readLong(), ignoreTiming );
                            if ( isGraphicsReady && isTelementryReady && isScoringReady )
                            {
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
            
            PlaybackUpdateInterface updateInterfce = new PlaybackUpdateInterface()
            {
                @Override
                public void update( boolean firstTime )
                {
                    if ( firstTime )
                        //javax.swing.JOptionPane.showMessageDialog( null, "start" );
                    
                    rfDynHUD.update();
                }
            };
            
            playback( eventsManager, new File( file ), true, updateInterfce );
            
            eventsManager.onShutdown( System.nanoTime() );
        }
        finally
        {
            GameEventsManager.simulationMode = oldSimMode;
        }
    }
}
