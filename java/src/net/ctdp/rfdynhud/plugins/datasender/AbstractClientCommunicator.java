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

import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleControl;

/**
 * Connects to the editor via a socket and sends/receives data (client side).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractClientCommunicator extends AbstractCommunicator
{
    protected abstract boolean isRunning();
    
    protected abstract boolean checkServerName( byte[] serverName );
    
    /**
     * Attempts to query a password hash.
     * 
     * @return the password hash or <code>null</code>.
     */
    protected abstract byte[] onPasswordRequested();
    
    protected abstract void onConnectionEsteblished( boolean isInCockpit );
    
    protected abstract void onConnectionRefused( String message );
    
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
    
    protected final boolean readInput( DataInputStream in ) throws IOException
    {
        boolean running = this.isRunning();
        
        int code = in.readInt();
        
        //debug( "Received command code: ", code - CommunicatorConstants.OFFSET );
        
        switch ( code )
        {
            case CommunicatorConstants.SERVER_NAME:
                // FIXME: This may block!
                byte[] serverName = new byte[ 32 ];
                in.readFully( serverName );
                
                if ( !checkServerName( serverName ) )
                    close();
                else
                writeSimpleCommand( CommunicatorConstants.CONNECTION_REQUEST2 );
                break;
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
    
    public abstract void connect( String connectionString );
    
    protected abstract void close( boolean restart );
    
    public final void close()
    {
        close( false );
    }
    
    protected abstract void onConnectionClosed();
    
    public AbstractClientCommunicator()
    {
    }
}
