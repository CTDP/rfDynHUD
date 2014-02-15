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
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Connects to the editor via a socket and sends/receives data (client side).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractTCPClientCommunicator extends AbstractClientCommunicator implements Runnable
{
    private String connectionString = null;
    private String host = null;
    private int port = 0;
    
    private volatile boolean running = false;
    private volatile boolean connected = false;
    private volatile boolean closeRequested = false;
    
    private volatile boolean waitingForConnection = false;
    
    private boolean commandInProgress = false;
    private short currentCommand = 0;
    private boolean commandEnded = false;
    
    public final String getLastConnectionString()
    {
        return ( connectionString );
    }
    
    @Override
    public final boolean isRunning()
    {
        return ( running );
    }
    
    @Override
    public final boolean isConnected()
    {
        return ( connected );
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
            waitingForConnection = true;
            socket = new Socket( host, port );
            waitingForConnection = false;
            in = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
            out = socket.getOutputStream();
            
            out.write( ( CONNECTION_REQUEST >>>  8 ) & 0xFF );
            out.write( ( CONNECTION_REQUEST >>>  0 ) & 0xFF );
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
                if ( ( eventsBuffer0.size() > 0 ) )
                {
                    try
                    {
                        //System.out.println( "Sending " + eventsBuffer0.size() + " bytes." );
                        eventsBuffer0.writeTo( out );
                        if ( commandEnded )
                            out.flush();
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
                    out.write( ( CONNECTION_CLOSED >>>  8 ) & 0xFF );
                    out.write( ( CONNECTION_CLOSED >>>  0 ) & 0xFF );
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
                if ( in.available() >= 2 )
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
    
    @Override
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
        
        writeShort( CONNECTION_REQUEST );
        */
    }
    
    @Override
    protected void close( boolean restart )
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
    
    public AbstractTCPClientCommunicator()
    {
    }
}
