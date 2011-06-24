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

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TelemVect3
{
    float x, y, z;
    
    public void invert()
    {
        this.x *= -1f;
        this.y *= -1f;
        this.z *= -1f;
    }
    
    public final float getX()
    {
        return ( x );
    }
    
    public final float getY()
    {
        return ( y );
    }
    
    public final float getZ()
    {
        return ( z );
    }
    
    public final float getLengthSquared()
    {
        return ( x * x + y * y + z * z );
    }
    
    public final float getLength()
    {
        return ( (float)Math.sqrt( getLengthSquared() ) );
    }
    
    public final float getDistanceToSquared( TelemVect3 pos2 )
    {
        float d = 0.0f;
        float tmp = this.x - pos2.x;
        d += tmp * tmp;
        tmp = this.y - pos2.y;
        d += tmp * tmp;
        tmp = this.z - pos2.z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceTo( TelemVect3 pos2 )
    {
        return ( (float)Math.sqrt( getDistanceToSquared( pos2 ) ) );
    }
    
    public final float getDistanceToSquared( float x, float y, float z )
    {
        float d = 0.0f;
        float tmp = this.x - x;
        d += tmp * tmp;
        tmp = this.y - y;
        d += tmp * tmp;
        tmp = this.z - z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceTo( float x, float y, float z )
    {
        return ( (float)Math.sqrt( getDistanceToSquared( x, y, z ) ) );
    }
    
    public final float getDistanceXZToSquared( TelemVect3 pos2 )
    {
        float d = 0.0f;
        float tmp = this.x - pos2.x;
        d += tmp * tmp;
        tmp = this.z - pos2.z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceXZTo( TelemVect3 pos2 )
    {
        return ( (float)Math.sqrt( getDistanceXZToSquared( pos2 ) ) );
    }
    
    public final float getDistanceXZToSquared( float x, float z )
    {
        float d = 0.0f;
        float tmp = this.x - x;
        d += tmp * tmp;
        tmp = this.z - z;
        d += tmp * tmp;
        
        return ( d );
    }
    
    public final float getDistanceXZTo( float x, float z )
    {
        return ( (float)Math.sqrt( getDistanceXZToSquared( x, z ) ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( this.getClass().getSimpleName() + "( " + getX() + ", " + getY() + ", " + getZ() + " )" );
    }
    
    public TelemVect3()
    {
    }
}
