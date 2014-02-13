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
package net.ctdp.rfdynhud.editor.live;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import net.ctdp.rfdynhud.editor.RFDynHUDEditor;
import net.ctdp.rfdynhud.gamedata.GameDataStreamSource;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleControl;
import net.ctdp.rfdynhud.plugins.datasender.AbstractClientCommunicator;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.logging.LogLevel;
import org.jagatoo.util.strings.MD5Util;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class LiveCommunicator extends AbstractClientCommunicator implements  java.awt.event.AWTEventListener
{
    private final RFDynHUDEditor editor;
    
    private final GameEventsManager eventsManager;
    @SuppressWarnings( "unused" )
    private final LiveGameData gameData;
    
    private final Object syncMonitor;
    
    private String connectionString = null;
    private JDialog waitingForConnectionDialog = null;
    private boolean nextConnectionAttempt = false;
    
    private String lastEnteredValidPassword = null;
    private String lastEnteredPassword = null;
    
    private volatile boolean rendering = false;
    
    @Override
    protected void log( LogLevel logLevel, Object... message )
    {
        RFDHLog.println( logLevel, message );
    }
    
    @Override
    protected void log( Object... message )
    {
        RFDHLog.println( message );
    }
    
    @Override
    protected void debug( Object... message )
    {
        RFDHLog.debug( message );
    }
    
    private JDialog initWaitingDialog()
    {
        waitingForConnectionDialog = new JDialog( editor.getMainWindow(), true );
        waitingForConnectionDialog.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosed( WindowEvent we )
            {
                if ( waitingForConnectionDialog != null )
                    waitingForConnectionDialog.dispose();
                waitingForConnectionDialog = null;
            }
        } );
        JPanel cp = (JPanel)waitingForConnectionDialog.getContentPane();
        cp.setLayout( new BorderLayout() );
        JLabel message = new JLabel( "Waiting for connection to be esteblished...", JLabel.CENTER );
        message.setBorder( new EmptyBorder( 30, 30, 0, 30 ) );
        cp.add( message, BorderLayout.NORTH );
        ImageIcon waitIcon = null;
        try
        {
            waitIcon = new ImageIcon( this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/wait_48x48.gif" ) );
            JLabel lbl = new JLabel( waitIcon );
            lbl.setBorder( new EmptyBorder( 30, 30, 30, 30 ) );
            cp.add( lbl, BorderLayout.CENTER );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        waitingForConnectionDialog.pack();
        
        return ( waitingForConnectionDialog );
    }
    
    private void waitForConnection( final long delay )
    {
        waitingForConnectionDialog.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowActivated( WindowEvent we )
            {
                waitingForConnectionDialog.removeWindowListener( this );
                
                nextConnectionAttempt = true;
                
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        // If the connection was just closed, we need to take a deep breath, so that we're not accidentally resuing the just closed socket.
                        if ( delay > 0L )
                        {
                            try
                            {
                                Thread.sleep( delay );
                            }
                            catch ( InterruptedException e )
                            {
                            }
                        }
                        
                        while ( ( waitingForConnectionDialog != null ) && waitingForConnectionDialog.isVisible() )
                        {
                            if ( nextConnectionAttempt )
                            {
                                nextConnectionAttempt = false;
                                
                                debug( "connection attempt..." );
                                connect( connectionString );
                                
                                long t = System.nanoTime() + 5000000000L; // 5 seconds
                                while ( ( waitingForConnectionDialog != null ) && waitingForConnectionDialog.isVisible() && ( System.nanoTime() < t ) )
                                {
                                    try
                                    {
                                        Thread.sleep( 100L );
                                    }
                                    catch ( InterruptedException e )
                                    {
                                    }
                                }
                            }
                            else
                            {
                                try
                                {
                                    Thread.sleep( 10L );
                                }
                                catch ( InterruptedException e )
                                {
                                }
                            }
                        }
                        
                        debug( "end." );
                    }
                }.start();
            }
        } );
        waitingForConnectionDialog.setVisible( true );
    }
    
    public boolean startLiveMode( String connectionStrings )
    {
        LiveConnector conn = new LiveConnector( editor.getMainWindow(), connectionStrings );
        conn.setVisible( true );
        
        if ( conn.isCancelled() )
            return ( false );
        
        connectionString = conn.getConnectionString();
        
        initWaitingDialog();
        waitingForConnectionDialog.setLocationRelativeTo( editor.getMainWindow() );
        waitForConnection( 0L );
        
        nextConnectionAttempt = false;
        
        //return ( isConnected() );
        return ( true );
    }
    
    public void stop()
    {
        nextConnectionAttempt = false;
        
        if ( waitingForConnectionDialog != null )
            waitingForConnectionDialog.dispose();
        
        waitingForConnectionDialog = null;
        
        close();
    }
    
    @Override
    protected byte[] onPasswordRequested()
    {
        if ( lastEnteredValidPassword == null )
        {
            JPanel p = new JPanel( new BorderLayout() );
            JLabel msg = new JLabel( "Please enter the password for this connection." );
            msg.setPreferredSize( new Dimension( 200, 22 ) );
            p.add( msg, BorderLayout.NORTH );
            final JPasswordField pwdField = new JPasswordField();
            pwdField.setPreferredSize( new Dimension( 200, 22 ) );
            p.add( pwdField, BorderLayout.CENTER );
            pwdField.requestFocus();
            
            String[] options = { "OK", "Cancel" };
            
            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep( 100L );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                    
                    pwdField.requestFocus();
                }
            }.start();
            
            int option = JOptionPane.showOptionDialog( editor.getMainWindow(), p, "Password requested", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0] );
            
            if ( option == 1 )
                return ( null );
            
            lastEnteredPassword = new String( pwdField.getPassword() );
            
            return ( MD5Util.md5Bytes( lastEnteredPassword ) );
        }
        
        return ( MD5Util.md5Bytes( lastEnteredValidPassword ) );
    }
    
    @Override
    public void eventDispatched( java.awt.AWTEvent ev )
    {
        /*
        if ( ev instanceof java.awt.MouseEvent )
        {
            System.out.println( ev.getSource() );
        }
        */
    }
    
    @Override
    protected void onConnectionEsteblished( boolean isInCockpit )
    {
        debug( "Connection esteblished." );
        
        waitingForConnectionDialog.setVisible( false );
        
        editor.mergeLiveConnectionString( getLastConnectionString() );
        
        lastEnteredValidPassword = lastEnteredPassword;
        
        //java.awt.Toolkit.getDefaultToolkit().addAWTEventListener( this, java.awt.MouseEvent.MOUSE_EVENT_MASK | java.awt.KeyEvent.KEY_EVENT_MASK );
        
        rendering = true;
        editor.getEditorPanel().liveMode = true;
        
        new Thread()
        {
            @Override
            public void run()
            {
                while ( rendering )
                {
                    //synchronized ( syncMonitor )
                    {
                        editor.repaintEditorPanel();
                    }
                    
                    try
                    {
                        Thread.sleep( 500L );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            }
        }.start();
    }
    
    @Override
    protected void onConnectionRefused( String message )
    {
        debug( "Connection refused: " + message );
        
        nextConnectionAttempt = true;
        
        lastEnteredValidPassword = null;
    }
    
    @Override
    protected void onConnectionClosed()
    {
        debug( "Connection closed." );
        
        //java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener( this );
        
        rendering = false;
        editor.getEditorPanel().liveMode = false;
        
        if ( waitingForConnectionDialog != null )
            waitForConnection( 1000L );
    }
    
    @Override
    protected void onSessionStarted( SessionType sessionType )
    {
        eventsManager.onSessionStarted( null );
    }
    
    @Override
    protected void onCockpitEntered()
    {
        eventsManager.onCockpitEntered( null );
    }
    
    @Override
    protected void onCockpitExited()
    {
        eventsManager.onCockpitExited( null );
    }
    
    @Override
    protected void onPitsEntered()
    {
    }
    
    @Override
    protected void onPitsExited()
    {
    }
    
    @Override
    protected void onGarageEntered()
    {
    }
    
    @Override
    protected void onGarageExited()
    {
    }
    
    @Override
    protected void onVehicleControlChanged( int driverID, VehicleControl control )
    {
    }
    
    @Override
    protected void onLapStarted( int driverID, short lap )
    {
    }
    
    @Override
    protected void onGamePauseStateChanged( boolean paused )
    {
    }
    
    @Override
    protected void onPlayerJoined( String name, int id, short place )
    {
    }
    
    @Override
    protected void onPlayerLeft( int id )
    {
    }
    
    @Override
    protected void onSessionTimeReceived( long time )
    {
    }
    
    private static class MyGameDataStreamSource implements GameDataStreamSource
    {
        private InputStream in = null;
        
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
    };
    
    private final MyGameDataStreamSource gdss = new MyGameDataStreamSource();
    
    @SuppressWarnings( "unused" )
    @Override
    protected boolean readDatagram( final int code, DataInputStream in ) throws IOException
    {
        switch ( code )
        {
            case LiveConstants.GRAPHICS_INFO:
                //in.skipBytes( 260 );
                synchronized ( syncMonitor )
                {
                    gdss.in = in;
                    eventsManager.onGraphicsInfoUpdated( gdss );
                    gdss.in = null;
                }
                return ( true );
            case LiveConstants.TELEMETRY_DATA:
                //in.skipBytes( 1888 );
                synchronized ( syncMonitor )
                {
                    gdss.in = in;
                    eventsManager.onTelemetryDataUpdated( gdss );
                    gdss.in = null;
                }
                return ( true );
            case LiveConstants.SCORING_INFO:
                int numVehicles = in.readInt();
                //in.skipBytes( 540 + numVehicles * 584 );
                synchronized ( syncMonitor )
                {
                    gdss.in = in;
                    eventsManager.onScoringInfoUpdated( numVehicles, gdss );
                    gdss.in = null;
                }
                return ( true );
            case LiveConstants.WEATHER_INFO:
                synchronized ( syncMonitor )
                {
                    gdss.in = in;
                    eventsManager.onWeatherInfoUpdated( gdss );
                    gdss.in = null;
                }
                return ( true );
            case LiveConstants.DRIVING_AIDS:
                //in.skipBytes( 40 );
                synchronized ( syncMonitor )
                {
                    gdss.in = in;
                    eventsManager.onDrivingAidsUpdated( gdss );
                    gdss.in = null;
                }
                return ( true );
            case LiveConstants.DRIVING_AIDS_STATE_CHANGED:
                int aidIndex = in.readInt();
                int oldState = in.readInt();
                int newState = in.readInt();
                return ( true );
            case LiveConstants.COMMENTARY_REQUEST_INFO:
                //in.skipBytes( 60 );
                synchronized ( syncMonitor )
                {
                    gdss.in = in;
                    eventsManager.onCommentaryRequestInfoUpdated( gdss );
                    gdss.in = null;
                }
                return ( true );
        }
        
        return ( false );
    }
    
    public LiveCommunicator( RFDynHUDEditor editor, GameEventsManager eventsManager )
    {
        this.editor = editor;
        
        this.eventsManager = eventsManager;
        this.gameData = eventsManager.getGameData();
        
        this.syncMonitor = editor.getEditorPanel().getDrawSyncMonitor();
    }
}
