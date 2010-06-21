package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Marvin Froehlich
 */
class CommentaryRequestInfoCapsule
{
    private static final int OFFSET_NAME = 0;
    private static final int OFFSET_INPUT1 = OFFSET_NAME + 32 * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_INPUT2 = OFFSET_INPUT1 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_INPUT3 = OFFSET_INPUT2 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_SKIP_CHECKS = OFFSET_INPUT3 + ByteUtil.SIZE_DOUBLE;
    
    private static final int BUFFER_SIZE = OFFSET_SKIP_CHECKS + ByteUtil.SIZE_BOOL;
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    final byte[] getBuffer()
    {
        return ( buffer );
    }
    
    void loadFromStream( InputStream in ) throws IOException
    {
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
    
    CommentaryRequestInfoCapsule()
    {
        //mName[0] = 0; mInput1 = 0.0; mInput2 = 0.0; mInput3 = 0.0; mSkipChecks = false;
    }
}
