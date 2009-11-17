package net.ctdp.rfdynhud.widgets._util;

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
