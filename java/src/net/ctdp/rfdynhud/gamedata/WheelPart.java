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
 * 
 * @author Marvin Froehlich (CTDP)
 */
public enum WheelPart
{
    INSIDE( 2, 0, 2, 0 ),
    CENTER( 1, 1, 1, 1 ),
    OUTSIDE( 0, 2, 0, 2 ),
    ;
    
    private final int arrayIndexFL;
    private final int arrayIndexFR;
    private final int arrayIndexRL;
    private final int arrayIndexRR;
    
    public final int getArrayIndexFL()
    {
        return ( arrayIndexFL );
    }
    
    public final int getArrayIndexFR()
    {
        return ( arrayIndexFR );
    }
    
    public final int getArrayIndexRL()
    {
        return ( arrayIndexRL );
    }
    
    public final int getArrayIndexRR()
    {
        return ( arrayIndexRR );
    }
    
    /**
     * Gets {@value #OUTSIDE} for the left wheels and {@value #INSIDE} for the right wheels.
     * 
     * @param wheel
     * 
     * @return {@value #OUTSIDE} for the left wheels and {@value #INSIDE} for the right wheels.
     */
    public static final WheelPart getLeftPart( Wheel wheel )
    {
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( WheelPart.OUTSIDE );
            case REAR_LEFT:
                return ( WheelPart.OUTSIDE );
            case FRONT_RIGHT:
                return ( WheelPart.INSIDE );
            case REAR_RIGHT:
                return ( WheelPart.INSIDE );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    /**
     * Gets {@value #INSIDE} for the left wheels and {@value #OUTSIDE} for the right wheels.
     * 
     * @param wheel
     * 
     * @return {@value #INSIDE} for the left wheels and {@value #OUTSIDE} for the right wheels.
     */
    public static final WheelPart getRightPart( Wheel wheel )
    {
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( WheelPart.INSIDE );
            case REAR_LEFT:
                return ( WheelPart.INSIDE );
            case FRONT_RIGHT:
                return ( WheelPart.OUTSIDE );
            case REAR_RIGHT:
                return ( WheelPart.OUTSIDE );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    private WheelPart( int arrayIndexFL, int arrayIndexFR, int arrayIndexRL, int arrayIndexRR )
    {
        this.arrayIndexFL = arrayIndexFL;
        this.arrayIndexFR = arrayIndexFR;
        this.arrayIndexRL = arrayIndexRL;
        this.arrayIndexRR = arrayIndexRR;
    }
}
