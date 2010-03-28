package net.ctdp.rfdynhud.editor.input;

import net.ctdp.rfdynhud.input.InputDeviceManager;
import net.ctdp.rfdynhud.util.Logger;

/**
 * This class interfaces native DirectInput.
 * 
 * @author Marvin Froehlich
 */
public class DirectInputConnection
{
    public static interface PollingListener
    {
        public void onPollingFinished( boolean canceled, String deviceComponent );
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
                Logger.log( "Loading library direct_input_connection.dll..." );
                
                System.loadLibrary( "direct_input_connection" );
                libLoaded = true;
                
                Logger.log( "Library loaded." );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
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
            Logger.log( "Initializing input device and component names..." );
            nativeInitInputDeviceManager( buffer, windowTitle.length(), buffer.length );
            Logger.log( "Done initializing input device and component names." );
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
            Logger.log( "Executing request from native library to initialize device and component names..." );
            inputDeviceManager.decodeData( deviceData );
            Logger.log( "Native request executed." );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
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
                    
                    Logger.log( "Polling for a key or button to be bound..." );
                    
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
        
        Logger.log( "Interrupting input polling." );
        
        nativeInterruptPolling();
        
        listener = null;
    }
    
    public void onInputEventReceived( byte[] buffer, int stringLength )
    {
        if ( stringLength == 0 )
        {
            Logger.log( "Input polling cancelled." );
            
            listener.onPollingFinished( true, null );
        }
        else
        {
            String string = new String( buffer, 0, stringLength );
            
            Logger.log( "Caught input to be bound: " + string );
            
            listener.onPollingFinished( false, string );
        }
    }
    
    public DirectInputConnection( String windowTitle )
    {
        initInputDeviceManager( windowTitle );
    }
}
