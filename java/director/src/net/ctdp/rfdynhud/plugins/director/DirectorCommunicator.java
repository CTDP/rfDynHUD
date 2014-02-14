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
package net.ctdp.rfdynhud.plugins.director;

import java.io.DataInputStream;
import java.io.IOException;

import org.jagatoo.logging.LogLevel;

import net.ctdp.rfdynhud.editor.director.DirectorConstants;

/**
 * Connects to the editor via a socket and sends/receives data.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorCommunicator extends net.ctdp.rfdynhud.plugins.datasender.AbstractTCPServerCommunicator
{
    private final DirectorPlugin plugin;
    
    private byte[] configData = null;
    
    private static final byte[] SERVER_NAME = createServerName( "Director".getBytes() );
    
    @Override
    protected byte[] getServerName()
    {
        return ( SERVER_NAME );
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
    
    private void readWidgetsConfig( DataInputStream in ) throws IOException
    {
        int length = in.readInt();
        
        if ( ( configData == null ) || ( configData.length < length ) )
            configData = new byte[ length ];
        
        int configLength = length;
        
        int off = 0;
        while ( length > 0 )
        {
            int read = in.read( configData, off, length );
            
            if ( read < 0 )
            {
                length = -1;
                configLength = -1;
            }
            else
            {
                length -= read;
                off += read;
            }
        }
        
        if ( configLength > 0 )
            plugin.onWidgetsConfigReceived( configData, configLength );
    }
    
    private void readWidgetState( DataInputStream in ) throws IOException
    {
        int widgetNameLength = ( in.readByte() & 0xFF );
        byte[] bytes = new byte[ widgetNameLength ];
        in.read( bytes );
        String widgetName = new String( bytes );
        long visibleStart = in.readLong();
        long visibleEnd = in.readLong();
        short posX = in.readShort();
        short posY = in.readShort();
        int viewedVSIid = in.readInt();
        int compareVSIId = in.readInt();
        
        plugin.onWidgetStateReceived( widgetName, visibleStart, visibleEnd, posX, posY, viewedVSIid, compareVSIId );
    }
    
    @Override
    protected boolean readDatagram( final int code, DataInputStream in ) throws IOException
    {
        switch ( code )
        {
            case DirectorConstants.WIDGETS_CONFIGURATION:
                readWidgetsConfig( in );
                return ( true );
            case DirectorConstants.RESET_WIDGET_STATES:
                plugin.onWidgetStatesReset();
                return ( true );
            case DirectorConstants.WIDGET_STATE:
                readWidgetState( in );
                return ( true );
        }
        
        return ( false );
    }
    
    public DirectorCommunicator( DirectorPlugin plugin, int port, String password )
    {
        super( port, password );
        
        this.plugin = plugin;
    }
}
