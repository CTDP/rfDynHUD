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
package net.ctdp.rfdynhud.gamedata.rfactor2;

import net.ctdp.rfdynhud.gamedata.ByteUtil;
import net.ctdp.rfdynhud.gamedata.CommentaryRequestInfo;
import net.ctdp.rfdynhud.gamedata.LiveGameData;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_CommentaryRequestInfo extends CommentaryRequestInfo
{
    private static final int OFFSET_NAME = 0;
    private static final int OFFSET_INPUT1 = OFFSET_NAME + 32 * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_INPUT2 = OFFSET_INPUT1 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_INPUT3 = OFFSET_INPUT2 + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_SKIP_CHECKS = OFFSET_INPUT3 + ByteUtil.SIZE_DOUBLE;
    
    private static final int BUFFER_SIZE = 60;//OFFSET_SKIP_CHECKS + ByteUtil.SIZE_BOOL;
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private native void fetchData( final long sourceBufferAddress, final int sourceBufferSize, final byte[] targetBuffer );
    
    @Override
    protected void updateDataImpl( Object userObject, long timestamp )
    {
        _rf2_DataAddressKeeper ak = (_rf2_DataAddressKeeper)userObject;
        
        fetchData( ak.getBufferAddress(), ak.getBufferSize(), buffer );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName()
    {
        // char mName[32]
        
        return ( ByteUtil.readString( buffer, OFFSET_NAME, 32 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final double getInput1()
    {
        // double mInput1
        
        return ( ByteUtil.readDouble( buffer, OFFSET_INPUT1 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final double getInput2()
    {
        // double mInput2
        
        return ( ByteUtil.readDouble( buffer, OFFSET_INPUT2 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final double getInput3()
    {
        // double mInput3
        
        return ( ByteUtil.readDouble( buffer, OFFSET_INPUT3 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean getSkipChecks()
    {
        // bool mSkipChecks
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_SKIP_CHECKS ) );
    }
    
    _rf2_CommentaryRequestInfo( LiveGameData gameData )
    {
        super( gameData );
        
        //mName[0] = 0; mInput1 = 0.0; mInput2 = 0.0; mInput3 = 0.0; mSkipChecks = false;
    }
}
