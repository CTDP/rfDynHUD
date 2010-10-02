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
public final class GameResolution
{
    private int resX = 1280;
    private int resY = 1024;
    
    private int vpX = 0;
    private int vpY = 0;
    private int vpW = 1280;
    private int vpH = 1024;
    
    boolean setResolution( int resX, int resY )
    {
        if ( ( resX != this.resX ) || ( resY != this.resY ) )
        {
            this.resX = resX;
            this.resY = resY;
            
            return ( true );
        }
        
        return ( false );
    }
    
    public final int getResX()
    {
        return ( resX );
    }
    
    public final int getResY()
    {
        return ( resY );
    }
    
    boolean setViewport( int x, int y, int w, int h )
    {
        if ( ( x != this.vpX ) || ( y != this.vpY ) || ( w != this.vpW ) || ( h != this.vpH ) )
        {
            this.vpX = x;
            this.vpY = y;
            this.vpW = w;
            this.vpH = h;
            
            return ( true );
        }
        
        return ( false );
    }
    
    public final int getViewportX()
    {
        return ( vpX );
    }
    
    public final int getViewportY()
    {
        return ( vpY );
    }
    
    public final int getViewportWidth()
    {
        return ( vpW );
    }
    
    public final int getViewportHeight()
    {
        return ( vpH );
    }
    
    public final String getResolutionString()
    {
        return ( resX + "x" + resY );
    }
    
    public final String getViewportString()
    {
        return ( vpX + ", " + vpY + ", " + vpW + "x" + vpH );
    }
    
    public GameResolution( int resX, int resY )
    {
        this.resX = resX;
        this.resY = resY;
        
        setViewport( 0, 0, resX, resY );
    }
    
    public GameResolution()
    {
    }
}
