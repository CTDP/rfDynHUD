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
    
    private static void loadLib()
    {
        if ( !libLoaded )
        {
            System.loadLibrary( "direct_input_connection" );
            libLoaded = true;
        }
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
        
        loadLib();
        
        nativeInitInputDeviceManager( buffer, windowTitle.length(), buffer.length );
    }
    
    public final InputDeviceManager getInputDeviceManager()
    {
        return ( inputDeviceManager );
    }
    
    public void initInput( byte[] deviceData )
    {
        try
        {
            inputDeviceManager.decodeData( deviceData );
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
        loadLib();
        
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
        
        nativeInterruptPolling();
        
        listener = null;
    }
    
    public void onInputEventReceived( byte[] buffer, int stringLength )
    {
        if ( stringLength == 0 )
            listener.onPollingFinished( true, null );
        else
            listener.onPollingFinished( false, new String( buffer, 0, stringLength ) );
    }
    
    public DirectInputConnection( String windowTitle )
    {
        initInputDeviceManager( windowTitle );
    }
}
