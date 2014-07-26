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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.jagatoo.util.strings.MD5Util;

/**
 * Connects via a socket using TCP and sends/receives data (server side).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractServerCommunicator extends AbstractCommunicator
{
    private final byte[] passwordHash;
    
    private boolean connected = false;
    
    /**
     * Gets the server's name for use in connection requests.
     * 
     * @return a 32 bytes array.
     */
    protected abstract byte[] getServerName();
    
    protected abstract boolean isRunning();
    
    @Override
    public final boolean isConnected()
    {
        return ( connected );
    }
    
    protected abstract boolean isInCockpit();
    
    protected abstract void onConnectionEsteblished();
    
    /**
     * 
     * @param code
     * @param in
     * 
     * @return <code>true</code>, if the datagram was recognized, <code>false</code> otherwise.
     * 
     * @throws IOException
     */
    protected abstract boolean readDatagram( final short code, DataInputStream in ) throws IOException;
    
    protected final void readInput( DataInputStream in ) throws IOException
    {
        short code = in.readShort();
        
        //plugin.debug( "Received command code: ", code - CommunicatorConstants.OFFSET );
        
        switch ( code )
        {
            case CONNECTION_REQUEST:
                byte[] serverName = getServerName();
                if ( serverName == null )
                    throw new Error( "Wrong implementation: getServerName() returned a null value." );
                if ( serverName.length != 32 )
                    throw new Error( "Wrong implementation: getServerName() returned an array of length " + serverName.length + ". Must be 32." );
                startCommandImpl( SERVER_NAME );
                writeImpl( serverName );
                endCommandImpl();
                break;
            case CONNECTION_REQUEST2:
                if ( passwordHash == null )
                {
                    startCommandImpl( CONNECTION_ESTEBLISHED );
                    eventsBuffer.writeBoolean( isInCockpit() );
                    endCommandImpl();
                    connected = true;
                    onConnectionEsteblished();
                }
                else
                {
                    writeSimpleCommandImpl( REQUEST_PASSWORD );
                }
                break;
            case PASSWORD_HASH:
                byte[] bytes = new byte[ 16 ];
                in.read( bytes );
                
                if ( Arrays.equals( bytes, passwordHash ) )
                {
                    startCommandImpl( CONNECTION_ESTEBLISHED );
                    eventsBuffer.writeBoolean( isInCockpit() );
                    endCommandImpl();
                    connected = true;
                    onConnectionEsteblished();
                }
                else
                {
                    writeSimpleCommandImpl( PASSWORD_MISMATCH );
                }
                break;
            case CONNECTION_CLOSED:
                close( true );
                break;
            default:
                if ( !readDatagram( code, in ) )
                    log( "WARNING: Unknown command code read: " + code );
        }
    }
    
    public abstract void connect();
    
    protected abstract void close( boolean restart );
    
    public final void close()
    {
        close( false );
    }
    
    protected abstract void onConnectionClosed();
    
    public AbstractServerCommunicator( String password )
    {
        this.passwordHash = ( ( password == null ) || password.equals( "" ) ) ? null : MD5Util.md5Bytes( password );
    }
}
