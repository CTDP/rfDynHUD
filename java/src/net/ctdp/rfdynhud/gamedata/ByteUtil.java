package net.ctdp.rfdynhud.gamedata;

/**
 * Static methods to read primitive types from a byte array.
 * 
 * @author Marvin Froehlich
 */
public class ByteUtil
{
    public static final int SIZE_BOOL = 1;
    public static final int SIZE_CHAR = 1;
    public static final int SIZE_SHORT = 2;
    public static final int SIZE_INT = 4;
    public static final int SIZE_LONG = 4;
    public static final int SIZE_FLOAT = 4;
    public static final int SIZE_DOUBLE = 8;
    public static final int SIZE_POINTER = 4; // 4 or 8 ???
    public static final int SIZE_VECTOR3 = 3 * SIZE_FLOAT;
    
    public static final int SHIFT4_HH = 0;
    public static final int SHIFT4_HL = 8;
    public static final int SHIFT4_LH = 16;
    public static final int SHIFT4_LL = 24;
    
    public static final int SHIFT2_H = 0;
    public static final int SHIFT2_L = 8;
    
    public static final int SHIFT_HHH = 24 + 32;
    public static final int SHIFT_HHL = 16 + 32;
    public static final int SHIFT_HLH = 8 + 32;
    public static final int SHIFT_HLL = 0 + 32;
    
    public static final byte readByte( final byte[] buffer, final int offset )
    {
        return ( buffer[ offset ] );
    }
    
    public static final short readUnsignedByte( final byte[] buffer, final int offset )
    {
        return ( (short)( buffer[ offset ] & 0xFF ) );
    }
    
    public static final boolean readBoolean( final byte[] buffer, final int offset )
    {
        return ( ( buffer[ offset ] & 0xFF ) != 0 );
    }
    
    public static final short readShort( final byte[] buffer, final int offset )
    {
        return ( (short)( ( ( buffer[ offset + 0 ] & 0xFF ) << ByteUtil.SHIFT2_H ) | ( ( buffer[ offset + 1 ] & 0xFF ) << ByteUtil.SHIFT2_L ) ) );
    }
    
    public static final int readUnsignedShort( final byte[] buffer, final int offset )
    {
        return ( ( ( buffer[ offset + 0 ] & 0xFF ) << ByteUtil.SHIFT2_H ) | ( ( buffer[ offset + 1 ] & 0xFF ) << ByteUtil.SHIFT2_L ) );
    }
    
    public static final int readInt( final byte[] buffer, final int offset )
    {
        return ( ( ( buffer[ offset + 0 ] & 0xFF ) << ByteUtil.SHIFT4_HH ) | ( ( buffer[ offset + 1 ] & 0xFF ) << ByteUtil.SHIFT4_HL ) | ( ( buffer[ offset + 2 ] & 0xFF ) << ByteUtil.SHIFT4_LH ) | ( ( buffer[ offset + 3 ] & 0xFF ) << ByteUtil.SHIFT4_LL ) );
    }
    
    public static final long readUnsignedInt( final byte[] buffer, final int offset )
    {
        return ( readInt( buffer, offset ) );
    }
    
    public static final long readLong( final byte[] buffer, final int offset )
    {
        /*
        int high = readInt( buffer, offset );
        int low = readInt( buffer, offset + 4 );
        
        return ( ( (long)high << 32 ) | low );
        */
        
        return ( readInt( buffer, offset ) );
    }
    
    public static final float readFloat( final byte[] buffer, final int offset )
    {
        int i = readInt( buffer, offset );
        
        return ( Float.intBitsToFloat( i ) );
    }
    
    public static final double readDouble( final byte[] buffer, final int offset )
    {
        //long l = readLong( buffer, offset );
        
        int high = readInt( buffer, offset );
        int low = readInt( buffer, offset + 4 );
        long l = ( (long)high << 32 ) | low;
        
        return ( Double.longBitsToDouble( l ) );
    }
    
    public static final void readVector( final byte[] buffer, final int offset, TelemVect3 vector )
    {
        vector.x = ByteUtil.readFloat( buffer, offset + 0 * ByteUtil.SIZE_FLOAT );
        vector.y = ByteUtil.readFloat( buffer, offset + 1 * ByteUtil.SIZE_FLOAT );
        vector.z = ByteUtil.readFloat( buffer, offset + 2 * ByteUtil.SIZE_FLOAT );
    }
    
    public static final String readString( final byte[] buffer, final int offset, final int maxLength )
    {
        int length = maxLength;
        for ( int i = 0; i < maxLength; i++ )
        {
            if ( buffer[offset + i] == (byte)0 )
            {
                length = i;
                break;
            }
        }
        
        return ( new String( buffer, offset, length ) );
        
    }
}
