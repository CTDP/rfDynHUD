package net.ctdp.rfdynhud.editor.input;

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
    
    private final byte[] buffer = new byte[ 1024 ];
    private PollingListener listener = null;
    
    public final boolean isPolling()
    {
        return ( listener != null );
    }
    
    private native void initDirectInputAndStartPolling( byte[] buffer, int titleLength, int bufferLength );
    
    public void startInputPolling( final String windowTitle, final PollingListener listener )
    {
        if ( !libLoaded )
        {
            System.loadLibrary( "direct_input_connection" );
            libLoaded = true;
        }
        
        if ( isPolling() )
            return;
        
        for ( int i = 0; i < windowTitle.length(); i++ )
        {
            buffer[i] = (byte)windowTitle.charAt( i );
        }
        
        buffer[windowTitle.length()] = 0;
        
        new Thread()
        {
            @Override
            public void run()
            {
                DirectInputConnection.this.listener = listener;
                
                //try { Thread.sleep( 50L ); } catch ( Throwable t ) {}
                
                initDirectInputAndStartPolling( buffer, windowTitle.length(), buffer.length );
                
                DirectInputConnection.this.listener = null;
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
}
