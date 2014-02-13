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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleControl;

import org.jagatoo.logging.LogLevel;

/**
 * Connects to the editor via a socket and sends/receives data (client side).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractClientCommunicator implements Runnable
{
    private String connectionString = null;
    private String host = null;
    private int port = 0;
    
    private volatile boolean running = false;
    private volatile boolean connected = false;
    private volatile boolean closeRequested = false;
    
    private volatile boolean waitingForConnection = false;
    
    private final ByteArrayOutputStream eventsBuffer0 = new ByteArrayOutputStream();
    private final DataOutputStream eventsBuffer = new DataOutputStream( eventsBuffer0 );
    
    private boolean commandInProgress = false;
    private int currentCommand = 0;
    
    public final DataOutputStream getOutputStream()
    {
        if ( !isConnected() )
            return ( null );
        
        return ( eventsBuffer );
    }
    
    protected abstract void log( LogLevel logLevel, Object... message );
    
    protected abstract void log( Object... message );
    
    protected abstract void debug( Object... message );
    
    public final String getLastConnectionString()
    {
        return ( connectionString );
    }
    
    public final boolean isRunning()
    {
        return ( running );
    }
    
    public final boolean isConnected()
    {
        return ( connected );
    }
    
    public void write( int b )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.write( b );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void write( byte[] b )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.write( b );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void write( byte[] b, int off, int len )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.write( b, off, len );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeBoolean( boolean v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeBoolean( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeByte( int v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeByte( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeShort( int v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeShort( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeChar( int v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeChar( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeInt( int v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeInt( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeLong( long v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeLong( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeFloat( float v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeFloat( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }
    
    public void startCommand( int code )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            if ( commandInProgress )
                throw new IllegalStateException( "Another command (" + ( currentCommand - DataSenderConstants.OFFSET ) + ") has been started, but not ended." );
            
            currentCommand = code;
            commandInProgress = true;
            
            try
            {
                //plugin.debug( "Writing command " + code );
                eventsBuffer.writeInt( code );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }
    
    /*
    private void _writeCommand( int code ) throws IOException
    {
        //plugin.debug( "Writing command " + code );
        eventsBuffer.writeInt( code );
    }
    */
    
    public void endCommand()
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            if ( !commandInProgress )
                throw new IllegalStateException( "No command had been started." );
            
            currentCommand = 0;
            commandInProgress = false;
        }
    }
    
    public void writeSimpleCommand( int code )
    {
        startCommand( code );
        endCommand();
    }
    
    /**
     * Attempts to query a password hash.
     * 
     * @return the password hash or <code>null</code>.
     */
    protected abstract byte[] onPasswordRequested();
    
    protected abstract void onConnectionEsteblished( boolean isInCockpit );
    
    protected abstract void onConnectionRefused( String message );
    
    protected abstract void onConnectionClosed();
    
    protected abstract void onSessionStarted( SessionType sessionType );
    
    protected abstract void onCockpitEntered();
    
    protected abstract void onCockpitExited();
    
    protected abstract void onPitsEntered();
    
    protected abstract void onPitsExited();
    
    protected abstract void onGarageEntered();
    
    protected abstract void onGarageExited();
    
    protected abstract void onVehicleControlChanged( int driverID, VehicleControl control );
    
    protected abstract void onLapStarted( int driverID, short lap );
    
    protected abstract void onGamePauseStateChanged( boolean paused );
    
    protected abstract void onPlayerJoined( String name, int id, short place );
    
    private void readJoinedDriver( DataInputStream in ) throws IOException
    {
        int id = in.readInt();
        short place = in.readShort();
        int nameLength = in.readByte() & 0xFF;
        byte[] name = new byte[ nameLength ];
        in.readFully( name );
        
        onPlayerJoined( new String( name ), id, place );
    }
    
    protected abstract void onPlayerLeft( int id );
    
    protected abstract void onSessionTimeReceived( long time );
    
    protected abstract boolean readDatagram( final int code, DataInputStream in ) throws IOException;
    
    private boolean readInput( DataInputStream in ) throws IOException
    {
        boolean running = this.running;
        
        int code = in.readInt();
        
        //debug( "Received command code: ", code - CommunicatorConstants.OFFSET );
        
        switch ( code )
        {
            case CommunicatorConstants.REQUEST_PASSWORD:
                byte[] passwordHash = onPasswordRequested();
                
                if ( passwordHash == null )
                {
                    close();
                    return ( running );
                }
                
                startCommand( CommunicatorConstants.PASSWORD_HASH );
                write( passwordHash );
                endCommand();
                break;
            case CommunicatorConstants.PASSWORD_MISMATCH:
                byte[] passwordHash2 = onPasswordRequested();
                
                if ( passwordHash2 == null )
                {
                    close();
                    return ( running );
                }
                
                startCommand( CommunicatorConstants.PASSWORD_HASH );
                write( passwordHash2 );
                endCommand();
                break;
            case CommunicatorConstants.CONNECTION_ESTEBLISHED:
                onConnectionEsteblished( in.readBoolean() );
                break;
            case CommunicatorConstants.CONNECTION_REFUSED:
                onConnectionRefused( "Connection refused" );
                close();
                break;
            case CommunicatorConstants.CONNECTION_CLOSED:
                close();
                running = false;
                break;
            case CommunicatorConstants.ON_SESSION_STARTED:
                SessionType sessionType = SessionType.values()[in.readByte() & 0xFF];
                onSessionStarted( sessionType );
                break;
            case CommunicatorConstants.ON_PITS_ENTERED:
                onPitsEntered();
                break;
            case CommunicatorConstants.ON_PITS_EXITED:
                onPitsExited();
                break;
            case CommunicatorConstants.ON_GARAGE_ENTERED:
                onGarageEntered();
                break;
            case CommunicatorConstants.ON_GARAGE_EXITED:
                onGarageExited();
                break;
            case CommunicatorConstants.ON_VEHICLE_CONTROL_CHANGED:
                int driverID1 = in.readInt();
                VehicleControl control = VehicleControl.values()[in.readByte() & 0xFF];
                onVehicleControlChanged( driverID1, control );
                break;
            case CommunicatorConstants.ON_LAP_STARTED:
                int driverID2 = in.readInt();
                short lap = in.readShort();
                onLapStarted( driverID2, lap );
                break;
            case CommunicatorConstants.ON_COCKPIT_ENTERED:
                onCockpitEntered();
                break;
            case CommunicatorConstants.ON_COCKPIT_EXITED:
                onCockpitExited();
                break;
            case CommunicatorConstants.ON_GAME_PAUSE_STATE_CHANGED:
                onGamePauseStateChanged( in.readBoolean() );
                break;
            case CommunicatorConstants.ON_PLAYER_JOINED:
                readJoinedDriver( in );
                break;
            case CommunicatorConstants.ON_PLAYER_LEFT:
                onPlayerLeft( in.readInt() );
                break;
            case CommunicatorConstants.SESSION_TIME:
                onSessionTimeReceived( in.readLong() );
                break;
            default:
                if ( !readDatagram( code, in ) )
                    log( "WARNING: Unknown command code read: " + code );
        }
        
        return ( running );
    }
    
    @Override
    public void run()
    {
        running = true;
        closeRequested = false;
        
        Socket socket = null;
        DataInputStream in = null;
        OutputStream out = null;
        
        try
        {
            waitingForConnection = true;
            socket = new Socket( host, port );
            waitingForConnection = false;
            in = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
            out = socket.getOutputStream();
            
            out.write( ( CommunicatorConstants.CONNECTION_REQUEST >>> 24 ) & 0xFF );
            out.write( ( CommunicatorConstants.CONNECTION_REQUEST >>> 16 ) & 0xFF );
            out.write( ( CommunicatorConstants.CONNECTION_REQUEST >>>  8 ) & 0xFF );
            out.write( ( CommunicatorConstants.CONNECTION_REQUEST >>>  0 ) & 0xFF );
        }
        catch ( UnknownHostException e )
        {
            onConnectionRefused( "Connection refused (unknown host)" );
            running = false;
            return;
        }
        catch ( IOException e )
        {
            onConnectionRefused( "Connection refused" );
            running = false;
            return;
        }
        catch ( Throwable t )
        {
            log( t );
            running = false;
            return;
        }
        
        connected = true;
        
        while ( running && socket.isConnected() )
        {
            synchronized ( eventsBuffer )
            {
                if ( !commandInProgress && ( eventsBuffer0.size() > 0 ) )
                {
                    try
                    {
                        //System.out.println( "Sending " + eventsBuffer0.size() + " bytes." );
                        eventsBuffer0.writeTo( out );
                        out.flush(); // Don't know, if that'S necessary.
                        eventsBuffer0.reset();
                    }
                    catch ( IOException e )
                    {
                        log( e );
                    }
                }
            }
            
            if ( closeRequested )
            {
                try
                {
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>> 24 ) & 0xFF );
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>> 16 ) & 0xFF );
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>>  8 ) & 0xFF );
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>>  0 ) & 0xFF );
                    running = false;
                }
                catch ( IOException e )
                {
                    running = false;
                    log( e );
                }
            }
            
            try
            {
                if ( in.available() >= 4 )
                {
                    running = readInput( in ) && running;
                }
            }
            catch ( IOException e )
            {
                log( e );
            }
            
            try
            {
                Thread.sleep( 10L );
            }
            catch ( InterruptedException e )
            {
                log( e );
            }
        }
        
        running = false;
        connected = false;
        
        try
        {
            in.close();
            out.close();
            socket.close();
        }
        catch ( IOException e )
        {
            log( e );
        }
        
        synchronized ( eventsBuffer )
        {
            eventsBuffer0.reset();
        }
        
        onConnectionClosed();
    }
    
    public static Object[] parseConnectionString( String connectionString )
    {
        String host = connectionString;
        int port = 9876;
        int p = host.indexOf( ':' );
        if ( p >= 0 )
        {
            port = Integer.parseInt( host.substring( p + 1 ) );
            host = host.substring( 0, p );
        }
        
        return ( new Object[] { host, port } );
    }
    
    public void connect( String connectionString )
    {
        Object[] parsed = parseConnectionString( connectionString );
        this.connectionString = connectionString;
        this.host = (String)parsed[0];
        this.port = (Integer)parsed[1];
        
        if ( !running )
            new Thread( this ).start();
        
        /*
        try
        {
            Thread.sleep( 100L );
        }
        catch ( InterruptedException e )
        {
        }
        
        writeInt( CommunicatorConstants.CONNECTION_REQUEST );
        */
    }
    
    public void close()
    {
        if ( connected )
        {
            closeRequested = true;
        }
        
        if ( waitingForConnection )
        {
            /*
            try
            {
                // Create dummy connection to close the waiting socket.
                Socket socket2 = new Socket( "localhost", port );
                socket2.close();
            }
            catch ( IOException e )
            {
                manager.log( e );
            }
            */
        }
    }
    
    public AbstractClientCommunicator()
    {
    }
}
