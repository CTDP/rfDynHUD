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
package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class _GraphicsInfoCapsule
{
    private long updateId = 0L;
    
    public abstract byte[] getBuffer();
    
    /**
     * This is incremented every time the info is updated.
     *  
     * @return the current update id.
     */
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    /**
     * Increments the update ID.
     */
    protected void onDataUpdated()
    {
        this.updateId++;
    }
    
    public abstract void loadFromStream( InputStream in ) throws IOException;
    
    public abstract void writeToStream( OutputStream out ) throws IOException;
    
    /**
     * camera position in meters
     * 
     * @param position output buffer
     */
    public abstract void getCameraPosition( TelemVect3 position );
    
    /**
     * @return camera position in meters
     */
    public abstract float getCameraPositionX();
    
    /**
     * @return camera position in meters
     */
    public abstract float getCameraPositionY();
    
    /**
     * @return camera position in meters
     */
    public abstract float getCameraPositionZ();
    
    /**
     * camera orientation
     * 
     * @param orientation output buffer
     */
    public abstract void getCameraOrientation( TelemVect3 orientation );
    
    /**
     * @return the ambient color
     */
    public abstract java.awt.Color getAmbientColor();
    
    /**
     * Gets the currently viewed vehicle.
     * 
     * @return the currently viewed vehicle or <code>null</code>, if N/A.
     */
    public abstract VehicleScoringInfo getViewedVehicleScoringInfo();
    
    protected _GraphicsInfoCapsule()
    {
    }
}
