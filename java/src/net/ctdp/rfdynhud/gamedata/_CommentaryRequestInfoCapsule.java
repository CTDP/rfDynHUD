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
public abstract class _CommentaryRequestInfoCapsule
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
     * @return one of the event names in the commentary INI file
     */
    public abstract String getName();
    
    /**
     * @return first value to pass in (if any)
     */
    public abstract double getInput1();
    
    /**
     * @return second value to pass in (if any)
     */
    public abstract double getInput2();
    
    /**
     * @return third value to pass in (if any)
     */
    public abstract double getInput3();
    
    /**
     * @return ignores commentary detail and random probability of event
     */
    public abstract boolean getSkipChecks();
}
