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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ctdp.rfdynhud.gamedata.ByteUtil;
import net.ctdp.rfdynhud.gamedata.GraphicsInfo;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.TelemVect3;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf2_GraphicsInfo extends GraphicsInfo
{
    private static final int OFFSET_CAM_POS = 0;
    private static final int OFFSET_CAM_ORI = OFFSET_CAM_POS + ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_HWND = OFFSET_CAM_ORI + 3 * ByteUtil.SIZE_VECTOR3D;
    private static final int OFFSET_AMBIENT_RED = OFFSET_HWND + ByteUtil.SIZE_LONG;
    private static final int OFFSET_AMBIENT_GREEN = OFFSET_AMBIENT_RED + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_AMBIENT_BLUE = OFFSET_AMBIENT_GREEN + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_SLOT_ID = OFFSET_AMBIENT_BLUE + ByteUtil.SIZE_DOUBLE;
    private static final int OFFSET_CAMERA_TYPE = OFFSET_SLOT_ID + ByteUtil.SIZE_LONG;
    
    private static final int EXPANSION_SIZE = 128 * ByteUtil.SIZE_CHAR;
    
    private static final int BUFFER_SIZE = OFFSET_CAMERA_TYPE + ByteUtil.SIZE_LONG + EXPANSION_SIZE;
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private final ScoringInfo scoringInfo;
    
    private native void fetchData( final long sourceBufferAddress, final int sourceBufferSize, final byte[] targetBuffer );
    
    @Override
    protected void updateDataImpl( Object userObject, long timestamp )
    {
        _rf2_DataAddressKeeper ak = (_rf2_DataAddressKeeper)userObject;
        
        fetchData( ak.getBufferAddress(), ak.getBufferSize(), buffer );
    }
    
    private void readFromStreamImpl( InputStream in ) throws IOException
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
    
    @Override
    public void readFromStream( InputStream in, boolean isEditorMode ) throws IOException
    {
        final long now = System.nanoTime();
        
        prepareDataUpdate( null, now );
        
        readFromStreamImpl( in );
        
        onDataUpdated( null, now, isEditorMode );
    }
    
    @Override
    public void writeToStream( OutputStream out ) throws IOException
    {
        out.write( buffer, 0, BUFFER_SIZE );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getCameraPosition( TelemVect3 position )
    {
        // TelemVect3 mCamPos
        
        ByteUtil.readVectorD( buffer, OFFSET_CAM_POS, position );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getCameraPositionX()
    {
        // TelemVect3 mCamPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CAM_POS + 0 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getCameraPositionY()
    {
        // TelemVect3 mCamPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CAM_POS + 1 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getCameraPositionZ()
    {
        // TelemVect3 mCamPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CAM_POS + 2 * ByteUtil.SIZE_DOUBLE ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getCameraOrientation( TelemVect3 orientation )
    {
        // TelemVect3 mCamOri
        
        // TODO: Convert from orientation matrix (three vectors)!
        ByteUtil.readVectorD( buffer, OFFSET_CAM_ORI, orientation );
    }
    
    // HWND mHWND;                    // app handle
    
    // GraphicsInfoV2
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final java.awt.Color getAmbientColor()
    {
        // float mAmbientRed
        // float mAmbientGreen
        // float mAmbientBlue
        
        float red = ByteUtil.readFloat( buffer, OFFSET_AMBIENT_RED );
        float green = ByteUtil.readFloat( buffer, OFFSET_AMBIENT_GREEN );
        float blue = ByteUtil.readFloat( buffer, OFFSET_AMBIENT_BLUE );
        
        return ( new java.awt.Color( (int)( red * 255f ), (int)( green * 255f ), (int)( blue * 255f ) ) );
    }
    
    /**
     * Gets the currently viewed vehicle slot (-1 if invalid).
     * 
     * @return the currently viewed vehicle slot (-1 if invalid).
     */
    public final int getSlotID()
    {
        return ( (int)ByteUtil.readLong( buffer, OFFSET_SLOT_ID ) );
    }
    
    /**
     * <p>
     * Gets the currently used camera type.
     * </p>
     * <p>
     * Possible values:
     * </p>
     * <ul>
     *   <li>0 = TV cockpit</li>
     *   <li>1 = cockpit</li>
     *   <li>2 = nosecam</li>
     *   <li>3 = swingman</li>
     *   <li>4 = trackside (nearest)</li>
     *   <li>5..1004 = onboardXXX</li>
     *   <li>1005+ = (currently unsupported, in the future may be able to set/get specific trackside camera)</li>
     * </ul>
     * 
     * @return the currently viewed vehicle slot (-1 if invalid).
     * TODO: [API] Add to public interface!
     */
    public final int getCameraType()
    {
        return ( (int)ByteUtil.readLong( buffer, OFFSET_CAMERA_TYPE ) );
    }
    
    _rf2_GraphicsInfo( LiveGameData gameData )
    {
        super( gameData );
        
        this.scoringInfo = gameData.getScoringInfo();
    }
}
