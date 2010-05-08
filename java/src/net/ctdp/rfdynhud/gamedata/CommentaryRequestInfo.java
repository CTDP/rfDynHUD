package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Marvin Froehlich
 */
public class CommentaryRequestInfo
{
    private static final int OFFSET_NAME = 0;
    private static final int OFFSET_INPUT1 = OFFSET_NAME + 32 * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_INPUT2 = OFFSET_INPUT1 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_INPUT3 = OFFSET_INPUT2 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_SKIP_CHECKS = OFFSET_INPUT3 + ByteUtil.SIZE_DOUBLE;
    
    private static final int BUFFER_SIZE = OFFSET_SKIP_CHECKS + ByteUtil.SIZE_BOOL;
    
    final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private final LiveGameData gameData;
    
    private long updateId = 0L;
    
    public static interface CommentaryInfoUpdateListener
    {
        public void onCommentaryInfoUpdated( LiveGameData gameData );
    }
    
    private CommentaryInfoUpdateListener[] updateListeners = null;
    
    public void registerListener( CommentaryInfoUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new CommentaryInfoUpdateListener[] { l };
        }
        else
        {
            CommentaryInfoUpdateListener[] tmp = new CommentaryInfoUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
    }
    
    public void unregisterListener( CommentaryInfoUpdateListener l )
    {
        if ( updateListeners == null )
            return;
        
        int index = -1;
        for ( int i = 0; i < updateListeners.length; i++ )
        {
            if ( updateListeners[i] == l )
            {
                index = i;
                break;
            }
        }
        
        if ( index < 0 )
            return;
        
        if ( updateListeners.length == 1 )
        {
            updateListeners = null;
            return;
        }
        
        CommentaryInfoUpdateListener[] tmp = new CommentaryInfoUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
    }
    
    void prepareDataUpdate()
    {
    }
    
    void onDataUpdated()
    {
        this.updateId++;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onCommentaryInfoUpdated( gameData );
        }
    }
    
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    void loadFromStream( InputStream in ) throws IOException
    {
        prepareDataUpdate();
        
        int offset = 0;
        int bytesToRead = BUFFER_SIZE;
        
        while ( bytesToRead > 0 )
        {
            int n = in.read( buffer, offset, bytesToRead );
            
            if ( n < 0 )
                throw new IOException();
            
            offset += n;
            bytesToRead -= n;
        }
        
        onDataUpdated();
    }
    
    /**
     * one of the event names in the commentary INI file
     */
    public final String getName()
    {
        // char mName[32]
        
        return ( ByteUtil.readString( buffer, OFFSET_NAME, 32 ) );
    }
    
    /**
     * first value to pass in (if any)
     */
    public final double getInput1()
    {
        // double mInput1
        
        return ( ByteUtil.readDouble( buffer, OFFSET_INPUT1 ) );
    }
    
    /**
     * first value to pass in (if any)
     */
    public final double getInput2()
    {
        // double mInput2
        
        return ( ByteUtil.readDouble( buffer, OFFSET_INPUT2 ) );
    }
    
    /**
     * first value to pass in (if any)
     */
    public final double getInput3()
    {
        // double mInput3
        
        return ( ByteUtil.readDouble( buffer, OFFSET_INPUT3 ) );
    }
    
    /**
     * ignores commentary detail and random probability of event
     */
    public final boolean getSkipChecks()
    {
        // bool mSkipChecks
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_SKIP_CHECKS ) );
    }
    
    CommentaryRequestInfo( LiveGameData gameData )
    {
        this.gameData = gameData;
        
        //mName[0] = 0; mInput1 = 0.0; mInput2 = 0.0; mInput3 = 0.0; mSkipChecks = false;
    }
}
