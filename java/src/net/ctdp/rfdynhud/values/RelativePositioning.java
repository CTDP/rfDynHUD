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
package net.ctdp.rfdynhud.values;

public enum RelativePositioning
{
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER_CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
    ;
    
    public final boolean isTop()
    {
        return ( ( this == TOP_LEFT ) || ( this == TOP_CENTER ) || ( this == TOP_RIGHT ) );
    }
    
    public final boolean isVCenter()
    {
        return ( ( this == CENTER_LEFT ) || ( this == CENTER_CENTER ) || ( this == CENTER_RIGHT ) );
    }
    
    public final boolean isBottom()
    {
        return ( ( this == BOTTOM_LEFT ) || ( this == BOTTOM_CENTER ) || ( this == BOTTOM_RIGHT ) );
    }
    
    public final boolean isLeft()
    {
        return ( ( this == TOP_LEFT ) || ( this == CENTER_LEFT ) || ( this == BOTTOM_LEFT ) );
    }
    
    public final boolean isHCenter()
    {
        return ( ( this == TOP_CENTER ) || ( this == CENTER_CENTER ) || ( this == BOTTOM_CENTER ) );
    }
    
    public final boolean isRight()
    {
        return ( ( this == TOP_RIGHT ) || ( this == CENTER_RIGHT ) || ( this == BOTTOM_RIGHT ) );
    }
    
    public final RelativePositioning deriveLeft()
    {
        if ( isTop() )
            return ( TOP_LEFT );
        
        if ( isVCenter() )
            return ( CENTER_LEFT );
        
        //if ( isBottom() )
            return ( BOTTOM_LEFT );
    }
    
    public final RelativePositioning deriveHCenter()
    {
        if ( isTop() )
            return ( TOP_CENTER );
        
        if ( isVCenter() )
            return ( CENTER_CENTER );
        
        //if ( isBottom() )
            return ( BOTTOM_CENTER );
    }
    
    public final RelativePositioning deriveRight()
    {
        if ( isTop() )
            return ( TOP_RIGHT );
        
        if ( isVCenter() )
            return ( CENTER_RIGHT );
        
        //if ( isBottom() )
            return ( BOTTOM_RIGHT );
    }
    
    public final RelativePositioning deriveTop()
    {
        if ( isLeft() )
            return ( TOP_LEFT );
        
        if ( isHCenter() )
            return ( TOP_CENTER );
        
        //if ( isRight() )
            return ( TOP_RIGHT );
    }
    
    public final RelativePositioning deriveVCenter()
    {
        if ( isLeft() )
            return ( CENTER_LEFT );
        
        if ( isHCenter() )
            return ( CENTER_CENTER );
        
        //if ( isRight() )
            return ( CENTER_RIGHT );
    }
    
    public final RelativePositioning deriveBottom()
    {
        if ( isLeft() )
            return ( BOTTOM_LEFT );
        
        if ( isHCenter() )
            return ( BOTTOM_CENTER );
        
        //if ( isRight() )
            return ( BOTTOM_RIGHT );
    }
}
