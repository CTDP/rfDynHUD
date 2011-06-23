/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.editor.director;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import net.ctdp.rfdynhud.editor.director.widgetstate.WidgetState.VisibleType;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleControl;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorCommunicator implements Runnable
{
    private final DirectorManager manager;
    
    private String connectionString = null;
    private String host = null;
    private int port = 0;
    
    private boolean running = false;
    private boolean connected = false;
    private boolean closeRequested = false;
    
    private boolean waitingForConnection = false;
    
    private final ByteArrayOutputStream eventsBuffer0 = new ByteArrayOutputStream();
    private final DataOutputStream eventsBuffer = new DataOutputStream( eventsBuffer0 );
    
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
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
                manager.log( e );
            }
        }
    }
    
    public void sendWidgetsConfiguration( WidgetsConfiguration widgetsConfig, GameResolution res )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeInt( DirectorConstants.WIDGETS_CONFIGURATION );
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ConfigurationSaver.saveConfiguration( widgetsConfig, res.getResolutionString(), 0, 0, 0, 0, baos, true );
                
                eventsBuffer.writeInt( baos.size() );
                
                baos.writeTo( eventsBuffer0 );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
    
    public void sendWidgetState( String widgetName, EffectiveWidgetState ws )
    {
        if ( !running )
            return;
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeInt( DirectorConstants.WIDGET_STATE );
                
                eventsBuffer.writeByte( widgetName.length() );
                eventsBuffer.write( widgetName.getBytes() );
                if ( ws.getVisibleType() == VisibleType.AUTO )
                {
                    eventsBuffer.writeLong( Long.MAX_VALUE );
                    eventsBuffer.writeLong( Long.MIN_VALUE );
                }
                else if ( ws.getVisibleType() == VisibleType.NEVER )
                {
                    eventsBuffer.writeLong( Long.MIN_VALUE );
                    eventsBuffer.writeLong( Long.MIN_VALUE + 1 );
                }
                else
                {
                    if ( manager.isTimeDecreasing() )
                    {
                        eventsBuffer.writeLong( ws.getVisibleStart() - ws.getVisibleTime() );
                        eventsBuffer.writeLong( ws.getVisibleStart() );
                    }
                    else
                    {
                        eventsBuffer.writeLong( ws.getVisibleStart() );
                        eventsBuffer.writeLong( ws.getVisibleStart() + ws.getVisibleTime() );
                    }
                }
                eventsBuffer.writeShort( ws.getPosX() );
                eventsBuffer.writeShort( ws.getPosY() );
                eventsBuffer.writeInt( ws.getForDriverID() );
                eventsBuffer.writeInt( ws.getCompareDriverID() );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
    
    private void readDriversList( DataInputStream in ) throws IOException
    {
        short numVehicles = in.readShort();
        
        DriverCapsule[] dcs = new DriverCapsule[ numVehicles ];
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            int id = in.readInt();
            int nameLength = in.readByte() & 0xFF;
            byte[] name_ = new byte[ nameLength ];
            in.readFully( name_ );
            String name = new String( name_ );
            dcs[i] = new DriverCapsule( name, id );
        }
        
        manager.onDriversListReceived( dcs );
    }
    
    private void readDriversPositions( DataInputStream in ) throws IOException
    {
        short numVehicles = in.readShort();
        
        int[] ids = new int[ numVehicles ];
        
        for ( int i = 0; i < numVehicles; i++ )
        {
            ids[i] = in.readInt();
        }
        
        manager.onDriversPositionsReceived( ids );
    }
    
    private void readJoinedDriver( DataInputStream in ) throws IOException
    {
        int id = in.readInt();
        short place = in.readShort();
        int nameLength = in.readByte() & 0xFF;
        byte[] name = new byte[ nameLength ];
        in.readFully( name );
        
        DriverCapsule dc = new DriverCapsule( new String( name ), id );
        
        manager.onPlayerJoined( dc, place );
    }
    
    private boolean readInput( DataInputStream in ) throws IOException
    {
        boolean running = this.running;
        
        int code = in.readInt();
        
        //manager.debug( "Received command code: ", code - DirectorConstants.OFFSET );
        
        int driverID;
        
        switch ( code )
        {
            case DirectorConstants.REQUEST_PASSWORD:
                byte[] passwordHash = manager.onPasswordRequested();
                
                if ( passwordHash == null )
                {
                    close();
                    return ( running );
                }
                
                writeInt( DirectorConstants.PASSWORD_HASH );
                write( passwordHash );
                break;
            case DirectorConstants.PASSWORD_MISMATCH:
                byte[] passwordHash2 = manager.onPasswordRequested();
                
                if ( passwordHash2 == null )
                {
                    close();
                    return ( running );
                }
                
                writeInt( DirectorConstants.PASSWORD_HASH );
                write( passwordHash2 );
                break;
            case DirectorConstants.CONNECTION_ESTEBLISHED:
                manager.onConnectionEsteblished( in.readBoolean() );
                break;
            case DirectorConstants.CONNECTION_REFUSED:
                manager.onConnectionRefused( "Connection refused" );
                close();
                break;
            case DirectorConstants.CONNECTION_CLOSED:
                close();
                running = false;
                break;
            case DirectorConstants.ON_SESSION_STARTED:
                SessionType sessionType = SessionType.values()[in.readByte() & 0xFF];
                manager.onSessionStarted( sessionType );
                break;
            case DirectorConstants.DRIVERS_LIST:
                readDriversList( in );
                break;
            case DirectorConstants.DRIVERS_POSITIONS:
                readDriversPositions( in );
                break;
            case DirectorConstants.ON_PITS_ENTERED:
                manager.onPitsEntered();
                break;
            case DirectorConstants.ON_PITS_EXITED:
                manager.onPitsExited();
                break;
            case DirectorConstants.ON_GARAGE_ENTERED:
                manager.onGarageEntered();
                break;
            case DirectorConstants.ON_GARAGE_EXITED:
                manager.onGarageExited();
                break;
            case DirectorConstants.ON_VEHICLE_CONTROL_CHANGED:
                driverID = in.readInt();
                VehicleControl control = VehicleControl.values()[in.readByte() & 0xFF];
                manager.onVehicleControlChanged( driverID, control );
                break;
            case DirectorConstants.ON_LAP_STARTED:
                driverID = in.readInt();
                short lap = in.readShort();
                manager.onLapStarted( driverID, lap );
                break;
            case DirectorConstants.ON_REALTIME_ENTERED:
                manager.onRealtimeEntered();
                break;
            case DirectorConstants.ON_REALTIME_EXITED:
                manager.onRealtimeExited();
                break;
            case DirectorConstants.ON_GAME_PAUSE_STATE_CHANGED:
                manager.onGamePauseStateChanged( in.readBoolean() );
                break;
            case DirectorConstants.ON_PLAYER_JOINED:
                readJoinedDriver( in );
                break;
            case DirectorConstants.ON_PLAYER_LEFT:
                manager.onPlayerLeft( in.readInt() );
                break;
            case DirectorConstants.SESSION_TIME:
                manager.onSessionTimeReceived( in.readLong() );
                break;
            default:
                manager.log( "WARNING: Unknown command code read: " + code );
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
        }
        catch ( UnknownHostException e )
        {
            manager.onConnectionRefused( "Connection refused (unknown host)" );
            running = false;
            return;
        }
        catch ( IOException e )
        {
            manager.onConnectionRefused( "Connection refused" );
            running = false;
            return;
        }
        catch ( Throwable t )
        {
            manager.log( t );
            running = false;
            return;
        }
        
        connected = true;
        
        while ( running && socket.isConnected() )
        {
            synchronized ( eventsBuffer )
            {
                if ( eventsBuffer0.size() > 0 )
                {
                    try
                    {
                        //System.out.println( "Sending " + eventsBuffer0.size() + " bytes." );
                        eventsBuffer0.writeTo( out );
                        eventsBuffer0.reset();
                    }
                    catch ( IOException e )
                    {
                        manager.log( e );
                    }
                }
            }
            
            if ( closeRequested )
            {
                try
                {
                    out.write( ( DirectorConstants.CONNECTION_CLOSED >>> 24 ) & 0xFF );
                    out.write( ( DirectorConstants.CONNECTION_CLOSED >>> 16 ) & 0xFF );
                    out.write( ( DirectorConstants.CONNECTION_CLOSED >>>  8 ) & 0xFF );
                    out.write( ( DirectorConstants.CONNECTION_CLOSED >>>  0 ) & 0xFF );
                    running = false;
                }
                catch ( IOException e )
                {
                    running = false;
                    manager.log( e );
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
                manager.log( e );
            }
            
            try
            {
                Thread.sleep( 10L );
            }
            catch ( InterruptedException e )
            {
                manager.log( e );
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
            manager.log( e );
        }
        
        synchronized ( eventsBuffer )
        {
            eventsBuffer0.reset();
        }
        
        manager.onConnectionClosed();
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
        
        if ( running )
            return;
        
        new Thread( this ).start();
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
    
    public DirectorCommunicator( DirectorManager manager )
    {
        this.manager = manager;
    }
}
