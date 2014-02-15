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

import java.io.DataOutputStream;
import java.io.IOException;

import org.jagatoo.logging.LogLevel;

/**
 * Sends/receives data.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractCommunicator implements CommunicatorConstants
{
    protected final CopyableByteArrayOutputStream eventsBuffer0 = new CopyableByteArrayOutputStream();
    protected final DataOutputStream eventsBuffer = new DataOutputStream( eventsBuffer0 );
    
    public static final byte[] createServerName( byte[] bytes )
    {
        if ( bytes.length > 32 )
            throw new IllegalArgumentException( "bytes must not be larger than 32." );
        
        byte[] result = new byte[ 32 ];
        
        System.arraycopy( bytes, 0, result, 0, bytes.length );
        
        for ( int i = bytes.length; i < result.length; i++ )
            result[i] = (byte)0;
        
        return ( result );
    }
    
    protected abstract void log( LogLevel logLevel, Object... message );
    
    protected abstract void log( Object... message );
    
    protected abstract void debug( Object... message );
    
    public abstract boolean isConnected();
    
    public final DataOutputStream getOutputStream()
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        return ( eventsBuffer );
    }
    
    public void write( int b )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.write( b );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    protected void writeImpl( byte[] b )
    {
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.write( b );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public final void write( byte[] b )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        writeImpl( b );
    }

    public void write( byte[] b, int off, int len )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.write( b, off, len );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeBoolean( boolean v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeBoolean( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeByte( int v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeByte( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeShort( int v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeShort( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeChar( int v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeChar( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeInt( int v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeInt( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }
    
    public void writeLong( long v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeLong( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeFloat( float v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeFloat( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }

    public void writeDouble( double v )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        synchronized ( eventsBuffer )
        {
            try
            {
                eventsBuffer.writeDouble( v );
            }
            catch ( IOException e )
            {
                log( e );
            }
        }
    }
    
    protected abstract void startCommandImpl( short code );
    
    public final void startCommand( short code )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        startCommandImpl( code );
    }
    
    protected abstract void endCommandImpl();
    
    public final void endCommand()
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        endCommandImpl();
    }
    
    protected final void writeSimpleCommandImpl( short code )
    {
        startCommandImpl( code );
        endCommandImpl();
    }
    
    public final void writeSimpleCommand( short code )
    {
        if ( !isConnected() )
            throw new IllegalStateException( "Connection not yet esteblished." );
        
        writeSimpleCommandImpl( code );
    }
    
    public AbstractCommunicator()
    {
    }
}
