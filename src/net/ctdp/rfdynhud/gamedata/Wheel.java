package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
 */
public enum Wheel
{
    FRONT_LEFT,
    FRONT_RIGHT,
    REAR_LEFT,
    REAR_RIGHT,
    ;
    
    public final boolean isFront()
    {
        return ( ( this == FRONT_LEFT ) || ( this == FRONT_RIGHT ) );
    }
    
    public final boolean isRear()
    {
        return ( ( this == REAR_LEFT ) || ( this == REAR_RIGHT ) );
    }
    
    public final boolean isLeft()
    {
        return ( ( this == FRONT_LEFT ) || ( this == REAR_LEFT ) );
    }
    
    public final boolean isRight()
    {
        return ( ( this == FRONT_RIGHT ) || ( this == REAR_RIGHT ) );
    }
}
