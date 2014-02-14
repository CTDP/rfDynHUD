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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Connects to the editor via a socket and sends/receives data (client side).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractUDPClientCommunicator extends AbstractClientCommunicator
{
    private String connectionString = null;
    
    private DatagramSocket socket = null;
    private InetAddress serverAddress = null;
    private int serverPort = -1;
    
    private long datagramOrdinal = 0L;
    
    private volatile boolean running = false;
    private volatile boolean connected = false;
    private volatile boolean restartSender = true;
    private volatile boolean restartReceiver = true;
    private volatile boolean closeRequested = false;
    
    private final Stack<DatagramPacket> datagrams = new Stack<DatagramPacket>();
    
    private boolean commandInProgress = false;
    private int currentCommand = 0;
    
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
    protected void startCommandImpl( int code )
    {
        synchronized ( eventsBuffer )
        {
            if ( commandInProgress )
                throw new IllegalStateException( "Another command (" + ( currentCommand - CommunicatorConstants.OFFSET ) + ") has been started, but not ended." );
            
            currentCommand = code;
            commandInProgress = true;
            
            try
            {
                eventsBuffer.writeLong( datagramOrdinal++ );
                eventsBuffer.writeInt( code );
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
        byte[] buffer = null;
        int usedBufferLength = -1;
        
        synchronized ( eventsBuffer )
        {
            if ( !commandInProgress )
                throw new IllegalStateException( "No command had been started." );
            
            buffer = eventsBuffer0.toByteArray(); // TODO: Optimize by reusing byte arrays!
            eventsBuffer0.reset();
            usedBufferLength = buffer.length;
            
            currentCommand = 0;
            commandInProgress = false;
        }
        
        //if ( buffer != null )
        {
            DatagramPacket datagram = new DatagramPacket( buffer, usedBufferLength );
            
            synchronized ( datagrams )
            {
                datagrams.push( datagram );
            }
        }
    }
    
    
    private final Runnable sender = new Runnable()
    {
        private final List<DatagramPacket> datagramsCopy = new ArrayList<DatagramPacket>();
        
        @Override
        public void run()
        {
            while ( running )
            {
                DatagramPacket closeDatagram = null;
                
                synchronized ( datagrams )
                {
                    // If close requested, send a close datagram.
                    if ( closeRequested )
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream( baos );
                        
                        try
                        {
                            dos.writeLong( datagramOrdinal++ );
                            dos.writeInt( CommunicatorConstants.CONNECTION_CLOSED );
                            dos.close();
                            
                            closeDatagram = new DatagramPacket( baos.toByteArray(), baos.size(), serverAddress, serverPort );
                            
                            running = false;
                            closeRequested = false;
                            
                            // Forgit pending datagrams.
                            datagrams.clear();
                            
                            datagramsCopy.add( closeDatagram );
                        }
                        catch ( IOException e )
                        {
                            log( e );
                        }
                    }
                    else if ( !datagrams.isEmpty() )
                    {
                        // Copy pending datagrams to a local buffer...
                        for ( int i = 0; i < datagrams.size(); i++ )
                            datagramsCopy.add( datagrams.get( i ) );
                        
                        datagrams.clear();
                    }
                }
                
                // Send all pending datagrams...
                if ( !datagramsCopy.isEmpty() )
                {
                    for ( int i = 0; i < datagramsCopy.size(); i++ )
                    {
                        DatagramPacket datagram = datagramsCopy.get( i );
                        
                        datagram.setAddress( serverAddress );
                        datagram.setPort( serverPort );
                        
                        try
                        {
                            socket.send( datagram );
                        }
                        catch ( SocketException e )
                        {
                            if ( !closeRequested )
                            {
                                log( "Connection closed unexpectedly" );
                                log( e );
                                running = false;
                                close( true );
                            }
                            break;
                        }
                        catch ( IOException e )
                        {
                            log( e );
                        }
                        
                        if ( datagram != closeDatagram )
                        {
                            // TODO: push used datagrams to a pool to reuse them.
                        }
                    }
                    
                    datagramsCopy.clear();
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
            
            if ( socket != null )
            {
                socket.close();
                socket = null;
            }
            
            synchronized ( eventsBuffer )
            {
                eventsBuffer0.reset();
            }
            
            onConnectionClosed();
            
            if ( restartSender )
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
    };
    
    private final Runnable receiver = new Runnable()
    {
        @Override
        public void run()
        {
            byte[] buffer = new byte[ 1024 * 1024 ];
            ByteArrayInputStream bais = new ByteArrayInputStream( buffer );
            DataInputStream din = new DataInputStream( bais );
            DatagramPacket datagram = new DatagramPacket( buffer, buffer.length );
            
            while ( running )
            {
                try
                {
                    socket.receive( datagram );
                    
                    bais.reset();
                    
                    while ( din.available() >= 4 )
                    {
                        //plugin.debug( "in.available: ", din.available() );
                        readInput( din );
                    }
                }
                catch ( SocketException e )
                {
                    if ( !closeRequested )
                    {
                        log( "Connection closed unexpectedly" );
                        log( e );
                        //close( true );
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
            
            if ( socket != null )
            {
                socket.close();
                socket = null;
            }
            
            synchronized ( eventsBuffer )
            {
                eventsBuffer0.reset();
            }
            
            if ( restartReceiver )
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
    };
    
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
        if ( running )
            return;
        
        Object[] parsed = parseConnectionString( connectionString );
        this.connectionString = connectionString;
        try
        {
            this.serverAddress = InetAddress.getByName( (String)parsed[0] );
        }
        catch ( UnknownHostException e )
        {
            throw new Error( e );
        }
        this.serverPort = (Integer)parsed[1];
        
        try
        {
            if ( socket == null )
            {
                socket = new DatagramSocket( serverPort );
                socket.setReuseAddress( true );
            }
            
            datagramOrdinal = 0L;
        }
        catch ( Throwable t )
        {
            running = false;
            log( t );
            return;
        }
        
        
        running = true;
        closeRequested = false;
        
        new Thread( sender ).start();
        new Thread( receiver ).start();
    }
    
    protected void beforeClosed()
    {
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
        
        this.restartSender = restart;
        this.restartReceiver = restart;
        
        if ( socket != null )
        {
            socket.close();
            socket = null;
        }
    }
    
    public AbstractUDPClientCommunicator()
    {
    }
}
