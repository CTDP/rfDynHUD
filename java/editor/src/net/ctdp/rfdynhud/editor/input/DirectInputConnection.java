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
package net.ctdp.rfdynhud.editor.input;

import net.ctdp.rfdynhud.input.InputDeviceManager;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * This class interfaces native DirectInput.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class DirectInputConnection
{
    public static interface PollingListener
    {
        public void onPollingFinished( boolean canceled, String deviceComponent, int keyCode, int modifierMask );
    }
    
    private static boolean libLoaded = false;
    private final InputDeviceManager inputDeviceManager = new InputDeviceManager();
    
    private final byte[] buffer = new byte[ 1024 ];
    private PollingListener listener = null;
    
    private static boolean loadLib()
    {
        if ( !libLoaded )
        {
            try
            {
                RFDHLog.printlnEx( "Loading library direct_input_connection.dll..." );
                
                System.loadLibrary( "direct_input_connection" );
                libLoaded = true;
                
                RFDHLog.printlnEx( "Library loaded." );
            }
            catch ( UnsatisfiedLinkError e )
            {
                RFDHLog.error( "[ERROR] Couldn't find direct_input_connection.dll" );
            }
            catch ( Throwable t )
            {
                RFDHLog.exception( t );
            }
        }
        
        return ( libLoaded );
    }
    
    private void fillTitleBuffer( final String windowTitle )
    {
        for ( int i = 0; i < windowTitle.length(); i++ )
        {
            buffer[i] = (byte)windowTitle.charAt( i );
        }
        
        buffer[windowTitle.length()] = 0;
    }
    
    private native void nativeInitInputDeviceManager( byte[] buffer, int titleLength, int bufferLength );
    
    private void initInputDeviceManager( String windowTitle )
    {
        fillTitleBuffer( windowTitle );
        
        if ( loadLib() )
        {
            RFDHLog.printlnEx( "Initializing input device and component names..." );
            nativeInitInputDeviceManager( buffer, windowTitle.length(), buffer.length );
            RFDHLog.printlnEx( "Done initializing input device and component names." );
        }
    }
    
    public final InputDeviceManager getInputDeviceManager()
    {
        return ( inputDeviceManager );
    }
    
    public void initInput( byte[] deviceData )
    {
        try
        {
            RFDHLog.printlnEx( "Executing request from native library to initialize device and component names..." );
            inputDeviceManager.decodeData( deviceData );
            RFDHLog.printlnEx( "Native request executed." );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    public final boolean isPolling()
    {
        return ( listener != null );
    }
    
    private native void initDirectInputAndStartPolling( byte[] buffer, int titleLength, int bufferLength );
    
    public void startInputPolling( final String windowTitle, final PollingListener listener )
    {
        if ( !loadLib() )
            return;
        
        if ( isPolling() )
            return;
        
        fillTitleBuffer( windowTitle );
        
        new Thread()
        {
            @Override
            public void run()
            {
                DirectInputConnection.this.listener = listener;
                
                try
                {
                    //try { Thread.sleep( 50L ); } catch ( Throwable t ) {}
                    
                    RFDHLog.printlnEx( "Polling for a key or button to be bound..." );
                    
                    initDirectInputAndStartPolling( buffer, windowTitle.length(), buffer.length );
                }
                finally
                {
                    DirectInputConnection.this.listener = null;
                }
            }
        }.start();
    }
    
    private native void nativeInterruptPolling();
    
    public void interruptPolling()
    {
        if ( !isPolling() )
            return;
        
        RFDHLog.printlnEx( "Interrupting input polling." );
        
        nativeInterruptPolling();
        
        listener = null;
    }
    
    public void onInputEventReceived( int keyCode, byte[] buffer, int stringLength, int modifierMask )
    {
        if ( stringLength == 0 )
        {
            RFDHLog.printlnEx( "Input polling cancelled." );
            
            listener.onPollingFinished( true, null, -1, 0 );
        }
        else
        {
            String string = new String( buffer, 0, stringLength );
            
            RFDHLog.printlnEx( "Caught input to be bound: " + string );
            
            listener.onPollingFinished( false, string, keyCode, modifierMask );
        }
    }
    
    public DirectInputConnection( String windowTitle )
    {
        initInputDeviceManager( windowTitle );
    }
}
