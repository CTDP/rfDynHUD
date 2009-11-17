package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
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
    
    final int getArrayIndexFL()
    {
        return ( arrayIndexFL );
    }
    
    final int getArrayIndexFR()
    {
        return ( arrayIndexFR );
    }
    
    final int getArrayIndexRL()
    {
        return ( arrayIndexRL );
    }
    
    final int getArrayIndexRR()
    {
        return ( arrayIndexRR );
    }
    
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
