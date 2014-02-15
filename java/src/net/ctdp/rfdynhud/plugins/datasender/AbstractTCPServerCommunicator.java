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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Connects via a socket using TCP and sends/receives data (server side).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractTCPServerCommunicator extends AbstractServerCommunicator implements Runnable
{
    private final int port;
    
    private ServerSocket serverSocket = null;
    
    private volatile boolean running = false;
    private volatile boolean connected = false;
    private volatile boolean restart = true;
    private volatile boolean closeRequested = false;
    
    private volatile boolean waitingForConnection = false;
    
    private boolean commandInProgress = false;
    private short currentCommand = 0;
    private boolean commandEnded = false;
    
    @Override
    public final boolean isRunning()
    {
        return ( running );
    }
    
    @Override
    protected void startCommandImpl( short code )
    {
        synchronized ( eventsBuffer )
        {
            if ( commandInProgress )
                throw new IllegalStateException( "Another command (" + currentCommand + ") has been started, but not ended." );
            
            currentCommand = code;
            commandInProgress = true;
            
            try
            {
                eventsBuffer.writeShort( code );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }
    
    @Override
    protected void endCommandImpl()
    {
        synchronized ( eventsBuffer )
        {
            if ( !commandInProgress )
                throw new IllegalStateException( "No command had been started." );
            
            currentCommand = 0;
            commandInProgress = false;
            commandEnded = true;
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
            log( t );
            return;
        }
        
        //connected = true;
        
        while ( running && socket.isConnected() && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown() )
        {
            synchronized ( eventsBuffer )
            {
                if ( eventsBuffer0.size() > 0 )
                {
                    try
                    {
                        eventsBuffer0.writeTo( out );
                        if ( commandEnded )
                            out.flush();
                        eventsBuffer0.reset();
                    }
                    catch ( SocketException e )
                    {
                        if ( !closeRequested )
                        {
                            log( "Connection closed unexpectedly" );
                            log( e );
                            close( true );
                        }
                        break;
                    }
                    catch ( IOException e )
                    {
                        log( e );
                    }
                }
                else if ( commandEnded )
                {
                    try
                    {
                        out.flush();
                    }
                    catch ( SocketException e )
                    {
                        if ( !closeRequested )
                        {
                            log( "Connection closed unexpectedly" );
                            log( e );
                            close( true );
                        }
                        break;
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
                    running = false;
                    out.write( ( CONNECTION_CLOSED >>>  8 ) & 0xFF );
                    out.write( ( CONNECTION_CLOSED >>>  0 ) & 0xFF );
                }
                catch ( SocketException e )
                {
                    break;
                }
                catch ( IOException e )
                {
                    log( e );
                }
            }
            
            try
            {
                if ( in.available() >= 2 )
                {
                    //plugin.debug( "in.available: ", in.available() );
                    readInput( in );
                }
            }
            catch ( SocketException e )
            {
                if ( !closeRequested )
                {
                    log( "Connection closed unexpectedly" );
                    log( e );
                    close( true );
                }
                break;
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
        closeRequested = false;
        
        try
        {
            socket.close();
            socket = null;
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
    
    @Override
    public void connect()
    {
        if ( running )
            return;
        
        new Thread( this ).start();
    }
    
    protected void beforeClosed()
    {
    }
    
    protected void closeWaitingSocket()
    {
        try
        {
            // Create dummy connection to close the waiting socket.
            Socket socket2 = new Socket( "localhost", port );
            socket2.close();
        }
        catch ( IOException e )
        {
            log( e );
        }
    }
    
    @Override
    protected final void close( boolean restart )
    {
        if ( connected )
        {
            beforeClosed();
            
            closeRequested = true;
        }
        else
        {
            running = false;
        }
        
        this.restart = restart;
        
        if ( waitingForConnection )
        {
            closeWaitingSocket();
        }
    }
    
    public AbstractTCPServerCommunicator( int port, String password )
    {
        super( password );
        
        this.port = port;
    }
}
