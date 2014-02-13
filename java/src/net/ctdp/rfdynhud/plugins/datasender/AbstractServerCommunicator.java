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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import org.jagatoo.util.strings.MD5Util;

/**
 * Connects to the editor via a socket and sends/receives data (server side).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractServerCommunicator implements Runnable
{
    private final AbstractDataSenderPlugin plugin;
    private final int port;
    private final byte[] passwordHash;
    
    private ServerSocket serverSocket = null;
    
    private volatile boolean running = false;
    private volatile boolean connected = false;
    private volatile boolean restart = true;
    private volatile boolean closeRequested = false;
    
    private volatile boolean waitingForConnection = false;
    
    private final ByteArrayOutputStream eventsBuffer0 = new ByteArrayOutputStream();
    private final DataOutputStream eventsBuffer = new DataOutputStream( eventsBuffer0 );
    
    private boolean commandInProgress = false;
    private int currentCommand = 0;
    
    public final boolean isRunning()
    {
        return ( running );
    }
    
    public final boolean isConnected()
    {
        return ( connected );
    }
    
    public final DataOutputStream getOutputStream()
    {
        if ( !isConnected() )
            return ( null );
        
        return ( eventsBuffer );
    }
    
    public void write( int b )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                //plugin.debug( "Writing byte " + b );
                eventsBuffer.write( b );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing " + b.length + " bytes " );
                eventsBuffer.write( b );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing " + len + " bytes" );
                eventsBuffer.write( b, off, len );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing boolean " + v );
                eventsBuffer.writeBoolean( v );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing byte " + v );
                eventsBuffer.writeByte( v );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing short " + v );
                eventsBuffer.writeShort( v );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing char " + v );
                eventsBuffer.writeChar( v );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing int " + v );
                eventsBuffer.writeInt( v );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                plugin.log( e );
            }
        }
    }
    
    private void _writeCommand( int code ) throws IOException
    {
        //plugin.debug( "Writing command " + code );
        eventsBuffer.writeInt( code );
    }
    
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
    
    public void writeLong( long v )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                //plugin.debug( "Writing long " + v );
                eventsBuffer.writeLong( v );
            }
            catch ( IOException e )
            {
                plugin.log( e );
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
                //plugin.debug( "Writing float " + v );
                eventsBuffer.writeFloat( v );
            }
            catch ( IOException e )
            {
                plugin.log( e );
            }
        }
    }
    
    /**
     * 
     * @param code
     * @param in
     * 
     * @return
     * 
     * @throws IOException
     */
    protected abstract boolean readDatagram( final int code, DataInputStream in ) throws IOException;
    
    private void readInput( DataInputStream in ) throws IOException
    {
        int code = in.readInt();
        
        //plugin.debug( "Received command code: ", code - CommunicatorConstants.OFFSET );
        
        switch ( code )
        {
            case CommunicatorConstants.CONNECTION_REQUEST:
                if ( passwordHash == null )
                {
                    _writeCommand( CommunicatorConstants.CONNECTION_ESTEBLISHED );
                    eventsBuffer.writeBoolean( plugin.isInCockpit() );
                    connected = true;
                    plugin.onConnectionEsteblished();
                }
                else
                {
                    _writeCommand( CommunicatorConstants.REQUEST_PASSWORD );
                }
                break;
            case CommunicatorConstants.PASSWORD_HASH:
                byte[] bytes = new byte[ 16 ];
                in.read( bytes );
                
                if ( Arrays.equals( bytes, passwordHash ) )
                {
                    _writeCommand( CommunicatorConstants.CONNECTION_ESTEBLISHED );
                    eventsBuffer.writeBoolean( plugin.isInCockpit() );
                    connected = true;
                    plugin.onConnectionEsteblished();
                }
                else
                {
                    _writeCommand( CommunicatorConstants.PASSWORD_MISMATCH );
                }
                break;
            case CommunicatorConstants.CONNECTION_CLOSED:
                close( true );
                break;
            default:
                if ( !readDatagram( code, in ) )
                    plugin.log( "WARNING: Unknown command code read: " + code );
        }
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
            if ( serverSocket == null )
            {
                serverSocket = new ServerSocket( port );
                serverSocket.setReuseAddress( true );
            }
            waitingForConnection = true;
            socket = serverSocket.accept();
            waitingForConnection = false;
            in = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
            out = socket.getOutputStream();
        }
        catch ( Throwable t )
        {
            running = false;
            plugin.log( t );
            return;
        }
        
        //connected = true;
        
        while ( running && socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown() )
        {
            synchronized ( eventsBuffer )
            {
                if ( !commandInProgress && ( eventsBuffer0.size() > 0 ) )
                {
                    try
                    {
                        eventsBuffer0.writeTo( out );
                        out.flush(); // Don't know, if that'S necessary.
                        eventsBuffer0.reset();
                    }
                    catch ( SocketException e )
                    {
                        if ( !closeRequested )
                        {
                            plugin.log( "Connection closed unexpectedly" );
                            plugin.log( e );
                            close( true );
                        }
                        break;
                    }
                    catch ( IOException e )
                    {
                        plugin.log( e );
                    }
                }
            }
            
            if ( closeRequested )
            {
                try
                {
                    running = false;
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>> 24 ) & 0xFF );
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>> 16 ) & 0xFF );
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>>  8 ) & 0xFF );
                    out.write( ( CommunicatorConstants.CONNECTION_CLOSED >>>  0 ) & 0xFF );
                }
                catch ( SocketException e )
                {
                    break;
                }
                catch ( IOException e )
                {
                    plugin.log( e );
                }
            }
            
            try
            {
                if ( in.available() >= 4 )
                {
                    //plugin.debug( "in.available: ", in.available() );
                    readInput( in );
                }
            }
            catch ( SocketException e )
            {
                if ( !closeRequested )
                {
                    plugin.log( "Connection closed unexpectedly" );
                    plugin.log( e );
                    close( true );
                }
                break;
            }
            catch ( IOException e )
            {
                plugin.log( e );
            }
            
            try
            {
                Thread.sleep( 10L );
            }
            catch ( InterruptedException e )
            {
                plugin.log( e );
            }
        }
        
        running = false;
        connected = false;
        closeRequested = false;
        
        try
        {
            socket.close();
            socket = null;
        }
        catch ( IOException e )
        {
            plugin.log( e );
        }
        
        synchronized ( eventsBuffer )
        {
            eventsBuffer0.reset();
        }
        
        if ( restart )
        {
            try
            {
                Thread.sleep( 200L );
            }
            catch ( InterruptedException e )
            {
            }
            
            new Thread( this ).start();
        }
    }
    
    public void connect()
    {
        if ( running )
            return;
        
        new Thread( this ).start();
    }
    
    public void close( boolean restart )
    {
        if ( connected )
        {
            plugin.onConnectionClosed();
            plugin.debug( "Connection closed normally" );
            
            closeRequested = true;
        }
        else
        {
            running = false;
        }
        
        this.restart = restart;
        
        if ( waitingForConnection )
        {
            try
            {
                // Create dummy connection to close the waiting socket.
                Socket socket2 = new Socket( "localhost", port );
                socket2.close();
            }
            catch ( IOException e )
            {
                plugin.log( e );
            }
        }
    }
    
    public AbstractServerCommunicator( AbstractDataSenderPlugin plugin, int port, String password )
    {
        this.plugin = plugin;
        this.port = port;
        
        this.passwordHash = ( ( password == null ) || password.equals( "" ) ) ? null : MD5Util.md5Bytes( password );
    }
}
