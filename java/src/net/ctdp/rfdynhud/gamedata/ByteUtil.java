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
package net.ctdp.rfdynhud.gamedata;

/**
 * Static methods to read primitive types from a byte array.
 * 
 * @author Marvin Froehlich (CTDP)
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
    public static final int SIZE_VECTOR3F = 3 * SIZE_FLOAT;
    public static final int SIZE_VECTOR3D = 3 * SIZE_DOUBLE;
    
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
    
    public static final void writeByte( final byte value, byte[] buffer, final int offset )
    {
        buffer[ offset ] = value;
    }
    
    public static final byte readByte( final byte[] buffer, final int offset )
    {
        return ( buffer[ offset ] );
    }
    
    public static final void writeUnsignedByte( final short value, byte[] buffer, final int offset )
    {
        buffer[ offset ] = (byte)value;
    }
    
    public static final short readUnsignedByte( final byte[] buffer, final int offset )
    {
        return ( (short)( buffer[ offset ] & 0xFF ) );
    }
    
    public static final void writeBoolean( final boolean value, byte[] buffer, final int offset )
    {
        buffer[ offset ] = value ? (byte)1 : (byte)0;
    }
    
    public static final boolean readBoolean( final byte[] buffer, final int offset )
    {
        return ( ( buffer[ offset ] & 0xFF ) != 0 );
    }
    
    public static final void writeShort( final int value, byte[] buffer, final int offset )
    {
        buffer[ offset + 0 ] = (byte)( ( value >> ByteUtil.SHIFT2_H ) & 0xFF );
        buffer[ offset + 1 ] = (byte)( ( value >> ByteUtil.SHIFT2_L ) & 0xFF );
    }
    
    public static final short readShort( final byte[] buffer, final int offset )
    {
        return ( (short)( ( ( buffer[ offset + 0 ] & 0xFF ) << ByteUtil.SHIFT2_H ) | ( ( buffer[ offset + 1 ] & 0xFF ) << ByteUtil.SHIFT2_L ) ) );
    }
    
    public static final int readUnsignedShort( final byte[] buffer, final int offset )
    {
        return ( ( ( buffer[ offset + 0 ] & 0xFF ) << ByteUtil.SHIFT2_H ) | ( ( buffer[ offset + 1 ] & 0xFF ) << ByteUtil.SHIFT2_L ) );
    }
    
    public static final void writeInt( final int value, byte[] buffer, final int offset )
    {
        buffer[ offset + 0 ] = (byte)( ( value >> ByteUtil.SHIFT4_HH ) & 0xFF );
        buffer[ offset + 1 ] = (byte)( ( value >> ByteUtil.SHIFT4_HL ) & 0xFF );
        buffer[ offset + 2 ] = (byte)( ( value >> ByteUtil.SHIFT4_LH ) & 0xFF );
        buffer[ offset + 3 ] = (byte)( ( value >> ByteUtil.SHIFT4_LL ) & 0xFF );
    }
    
    public static final int readInt( final byte[] buffer, final int offset )
    {
        return ( ( ( buffer[ offset + 0 ] & 0xFF ) << ByteUtil.SHIFT4_HH ) | ( ( buffer[ offset + 1 ] & 0xFF ) << ByteUtil.SHIFT4_HL ) | ( ( buffer[ offset + 2 ] & 0xFF ) << ByteUtil.SHIFT4_LH ) | ( ( buffer[ offset + 3 ] & 0xFF ) << ByteUtil.SHIFT4_LL ) );
    }
    
    public static final void writeUnsignedInt( final long value, byte[] buffer, final int offset )
    {
        writeInt( (int)value, buffer, offset );
    }
    
    public static final long readUnsignedInt( final byte[] buffer, final int offset )
    {
        return ( readInt( buffer, offset ) );
    }
    
    public static final void writeLong( final long value, byte[] buffer, final int offset )
    {
        writeInt( (int)value, buffer, offset );
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
    
    public static final void writeFloat( final float value, byte[] buffer, final int offset )
    {
        int i = Float.floatToIntBits( value );
        
        writeInt( i, buffer, offset );
    }
    
    public static final float readFloat( final byte[] buffer, final int offset )
    {
        int i = readInt( buffer, offset );
        
        return ( Float.intBitsToFloat( i ) );
    }
    
    public static final void writeDouble( final double value, byte[] buffer, final int offset )
    {
        long l = Double.doubleToLongBits( value );
        
        buffer[offset + 7] = (byte)( ( l >> 56 ) & 0xFF );
        buffer[offset + 6] = (byte)( ( l >> 48 ) & 0xFF );
        buffer[offset + 5] = (byte)( ( l >> 40 ) & 0xFF );
        buffer[offset + 4] = (byte)( ( l >> 32 ) & 0xFF );
        buffer[offset + 3] = (byte)( ( l >> 24 ) & 0xFF );
        buffer[offset + 2] = (byte)( ( l >> 16 ) & 0xFF );
        buffer[offset + 1] = (byte)( ( l >> 8 ) & 0xFF );
        buffer[offset + 0] = (byte)( ( l >> 0 ) & 0xFF );
    }
    
    public static final double readDouble( final byte[] buffer, final int offset )
    {
        //long l = readLong( buffer, offset );
        
        /*
        int low = readInt( buffer, offset );
        int high = readInt( buffer, offset + 4 );
        long l = ( (long)high << 32 ) | low;
        */
        
        /*
        long l = (
                   ( (long)buffer[offset + 0] << 56 ) +
                   ( (long)( buffer[offset + 1] & 255 ) << 48 ) +
                   ( (long)( buffer[offset + 2] & 255 ) << 40 ) +
                   ( (long)( buffer[offset + 3] & 255 ) << 32 ) +
                   ( (long)( buffer[offset + 4] & 255 ) << 24 ) +
                   ( ( buffer[offset + 5] & 255 ) << 16 ) +
                   ( ( buffer[offset + 6] & 255 ) <<  8 ) +
                   ( ( buffer[offset + 7] & 255 ) <<  0 )
                 );        
        */
        
        long l = (
                   ( (long)( buffer[offset + 7] & 0xFF ) << 56 ) |
                   ( (long)( buffer[offset + 6] & 0xFF ) << 48 ) |
                   ( (long)( buffer[offset + 5] & 0xFF ) << 40 ) |
                   ( (long)( buffer[offset + 4] & 0xFF ) << 32 ) |
                   ( (long)( buffer[offset + 3] & 0xFF ) << 24 ) |
                   ( (long)( buffer[offset + 2] & 0xFF ) << 16 ) |
                   ( (long)( buffer[offset + 1] & 0xFF ) <<  8 ) |
                   ( ( buffer[offset + 0] & 0xFF ) )
                 );
        
        
        
        return ( Double.longBitsToDouble( l ) );
    }
    
    public static final void readVectorF( final byte[] buffer, final int offset, TelemVect3 vector )
    {
        vector.x = ByteUtil.readFloat( buffer, offset + 0 * ByteUtil.SIZE_FLOAT );
        vector.y = ByteUtil.readFloat( buffer, offset + 1 * ByteUtil.SIZE_FLOAT );
        vector.z = ByteUtil.readFloat( buffer, offset + 2 * ByteUtil.SIZE_FLOAT );
    }
    
    public static final void readVectorD( final byte[] buffer, final int offset, TelemVect3 vector )
    {
        vector.x = (float)ByteUtil.readDouble( buffer, offset + 0 * ByteUtil.SIZE_DOUBLE );
        vector.y = (float)ByteUtil.readDouble( buffer, offset + 1 * ByteUtil.SIZE_DOUBLE );
        vector.z = (float)ByteUtil.readDouble( buffer, offset + 2 * ByteUtil.SIZE_DOUBLE );
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
