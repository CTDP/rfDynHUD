/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.gamedata.rfactor1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ctdp.rfdynhud.gamedata.ByteUtil;
import net.ctdp.rfdynhud.gamedata._GraphicsInfoCapsule;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf1_GraphicsInfoCapsule extends _GraphicsInfoCapsule
{
    private static final int OFFSET_CAM_POS = 0;
    private static final int OFFSET_CAM_ORI = OFFSET_CAM_POS + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_HWND = OFFSET_CAM_ORI + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_AMBIENT_RED = OFFSET_HWND + ByteUtil.SIZE_LONG;
    private static final int OFFSET_AMBIENT_GREEN = OFFSET_AMBIENT_RED + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_AMBIENT_BLUE = OFFSET_AMBIENT_GREEN + ByteUtil.SIZE_FLOAT;
    
    private static final int BUFFER_SIZE = OFFSET_AMBIENT_BLUE + ByteUtil.SIZE_FLOAT;
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private final ScoringInfo scoringInfo;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final byte[] getBuffer()
    {
        return ( buffer );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromStream( InputStream in ) throws IOException
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
     * {@inheritDoc}
     */
    @Override
    public void writeToStream( OutputStream out ) throws IOException
    {
        out.write( buffer, 0, BUFFER_SIZE );
    }
    
    // GraphicsInfo
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getCameraPosition( TelemVect3 position )
    {
        // TelemVect3 mCamPos
        
        ByteUtil.readVector( buffer, OFFSET_CAM_POS, position );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getCameraPositionX()
    {
        // TelemVect3 mCamPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CAM_POS + 0 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getCameraPositionY()
    {
        // TelemVect3 mCamPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CAM_POS + 1 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getCameraPositionZ()
    {
        // TelemVect3 mCamPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CAM_POS + 2 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void getCameraOrientation( TelemVect3 orientation )
    {
        // TelemVect3 mCamOri
        
        ByteUtil.readVector( buffer, OFFSET_CAM_ORI, orientation );
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
    
    private final TelemVect3 camPos = new TelemVect3();
    private final TelemVect3 carPos = new TelemVect3();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final VehicleScoringInfo getViewedVehicleScoringInfo()
    {
        //if ( !isUpdatedInRealtimeMode() )
        //    return ( null );
        
        VehicleScoringInfo viewedVSI = null;
        
        getCameraPosition( camPos );
        camPos.invert();
        
        float closestDist = Float.MAX_VALUE;
        
        int n = scoringInfo.getNumVehicles();
        
        for ( short i = 0; i < n; i++ )
        {
            scoringInfo.getVehicleScoringInfo( i ).getWorldPosition( carPos );
            
            float dist = carPos.getDistanceToSquared( camPos );
            
            if ( dist < closestDist )
            {
                closestDist = dist;
                viewedVSI = scoringInfo.getVehicleScoringInfo( i );
            }
        }
        
        return ( viewedVSI );
    }
    
    _rf1_GraphicsInfoCapsule( ScoringInfo scoringInfo )
    {
        this.scoringInfo = scoringInfo;
    }
}
