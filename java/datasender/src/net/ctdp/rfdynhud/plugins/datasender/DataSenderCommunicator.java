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

import org.jagatoo.logging.LogLevel;

/**
 * Connects to the editor via a socket and sends/receives data.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DataSenderCommunicator extends net.ctdp.rfdynhud.plugins.datasender.AbstractUDPServerCommunicator
{
    private final DataSenderPlugin plugin;
    
    private static final byte[] SERVER_IDENTIFIER = createServerName( "DataSender".getBytes() );
    
    @Override
    protected byte[] getServerName()
    {
        return ( SERVER_IDENTIFIER );
    }
    
    @Override
    protected void log( LogLevel logLevel, Object... message )
    {
        plugin.log( logLevel, message );
    }
    
    @Override
    protected void log( Object... message )
    {
        plugin.log( message );
    }
    
    @Override
    protected void debug( Object... message )
    {
        plugin.debug( message );
    }
    
    @Override
    protected boolean isInCockpit()
    {
        return ( plugin.isInCockpit() );
    }
    
    @Override
    protected void onConnectionEsteblished()
    {
        plugin.onConnectionEsteblished();
    }
    
    @Override
    protected void onConnectionClosed()
    {
        plugin.onConnectionClosed();
        plugin.debug( "Connection closed normally" );
    }
    
    @Override
    protected boolean readDatagram( final short code, DataInputStream in ) throws IOException
    {
        return ( false );
    }
    
    public DataSenderCommunicator( DataSenderPlugin plugin, int port, String password )
    {
        super( port, password );
        
        this.plugin = plugin;
    }
}
