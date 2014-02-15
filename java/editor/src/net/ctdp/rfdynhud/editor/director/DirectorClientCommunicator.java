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
package net.ctdp.rfdynhud.editor.director;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import net.ctdp.rfdynhud.editor.director.widgetstate.WidgetState.VisibleType;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleControl;
import net.ctdp.rfdynhud.plugins.datasender.AbstractTCPClientCommunicator;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

import org.jagatoo.logging.LogLevel;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectorClientCommunicator extends AbstractTCPClientCommunicator implements DirectorConstants
{
    private static final byte[] SERVER_IDENTIFIER = createServerName( "Director".getBytes() );
    
    private final DirectorManager manager;
    
    @Override
    protected void log( LogLevel logLevel, Object... message )
    {
        manager.debug( message );
    }
    
    @Override
    protected void log( Object... message )
    {
        manager.log( message );
    }
    
    @Override
    protected void debug( Object... message )
    {
        manager.debug( message );
    }
    
    public void sendWidgetsConfiguration( WidgetsConfiguration widgetsConfig, GameResolution res )
    {
        if ( !isRunning() )
            return;
        
        DataOutputStream os = getOutputStream();
        
        synchronized ( os )
        {
            try
            {
                startCommand( WIDGETS_CONFIGURATION );
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ConfigurationSaver.saveConfiguration( widgetsConfig, res.getResolutionString(), 0, 0, 0, 0, baos, true );
                
                os.writeInt( baos.size() );
                
                //baos.writeTo( eventsBuffer0 );
                baos.writeTo( os );
                
                endCommand();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
    
    public void sendWidgetState( String widgetName, EffectiveWidgetState ws )
    {
        if ( !isRunning() )
            return;
        
        DataOutputStream os = getOutputStream();
        
        synchronized ( os )
        {
            try
            {
                startCommand( WIDGET_STATE );
                
                os.writeByte( widgetName.length() );
                os.write( widgetName.getBytes() );
                if ( ws.getVisibleType() == VisibleType.AUTO )
                {
                    os.writeLong( Long.MAX_VALUE );
                    os.writeLong( Long.MIN_VALUE );
                }
                else if ( ws.getVisibleType() == VisibleType.NEVER )
                {
                    os.writeLong( Long.MIN_VALUE );
                    os.writeLong( Long.MIN_VALUE + 1 );
                }
                else
                {
                    if ( manager.isTimeDecreasing() )
                    {
                        os.writeLong( ws.getVisibleStart() - ws.getVisibleTime() );
                        os.writeLong( ws.getVisibleStart() );
                    }
                    else
                    {
                        os.writeLong( ws.getVisibleStart() );
                        os.writeLong( ws.getVisibleStart() + ws.getVisibleTime() );
                    }
                }
                os.writeShort( ws.getPosX() );
                os.writeShort( ws.getPosY() );
                os.writeInt( ws.getForDriverID() );
                os.writeInt( ws.getCompareDriverID() );
                
                endCommand();
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
    
    @Override
    protected boolean checkServerName( byte[] serverName )
    {
        return ( Arrays.equals( SERVER_IDENTIFIER, serverName ) );
    }
    
    @Override
    protected byte[] onPasswordRequested()
    {
        return ( manager.onPasswordRequested() );
    }
    
    @Override
    protected void onConnectionEsteblished( boolean isInCockpit )
    {
        manager.onConnectionEsteblished( isInCockpit );
    }
    
    @Override
    protected void onConnectionRefused( String message )
    {
        manager.onConnectionRefused( message );
    }
    
    @Override
    protected void onConnectionClosed()
    {
        manager.onConnectionClosed();
    }
    
    @Override
    protected void onSessionStarted( SessionType sessionType )
    {
        manager.onSessionStarted( sessionType );
    }
    
    @Override
    protected void onCockpitEntered()
    {
        manager.onCockpitEntered();
    }
    
    @Override
    protected void onCockpitExited()
    {
        manager.onCockpitExited();
    }
    
    @Override
    protected void onPitsEntered()
    {
        manager.onPitsEntered();
    }
    
    @Override
    protected void onPitsExited()
    {
        manager.onPitsExited();
    }
    
    @Override
    protected void onGarageEntered()
    {
        manager.onGarageEntered();
    }
    
    @Override
    protected void onGarageExited()
    {
        manager.onGarageExited();
    }
    
    @Override
    protected void onVehicleControlChanged( int driverID, VehicleControl control )
    {
        manager.onVehicleControlChanged( driverID, control );
    }
    
    @Override
    protected void onLapStarted( int driverID, short lap )
    {
        manager.onLapStarted( driverID, lap );
    }
    
    @Override
    protected void onGamePauseStateChanged( boolean paused )
    {
        manager.onGamePauseStateChanged( paused );
    }
    
    @Override
    protected void onPlayerJoined( String name, int id, short place )
    {
        manager.onPlayerJoined( new DriverCapsule( name, id ), place );
    }
    
    @Override
    protected void onPlayerLeft( int id )
    {
        manager.onPlayerLeft( id );
    }
    
    @Override
    protected void onSessionTimeReceived( long time )
    {
        manager.onSessionTimeReceived( time );
    }
    
    @Override
    protected boolean readDatagram( final short code, DataInputStream in ) throws IOException
    {
        switch ( code )
        {
            case DirectorConstants.DRIVERS_LIST:
                readDriversList( in );
                return ( true );
            case DirectorConstants.DRIVERS_POSITIONS:
                readDriversPositions( in );
                return ( true );
        }
        
        return ( false );
    }
    
    public DirectorClientCommunicator( DirectorManager manager )
    {
        super();
        
        this.manager = manager;
    }
}
