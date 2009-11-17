package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
 */
public enum SurfaceType
{
    DRY, // 0
    WET, // 1
    GRASS, // 2
    DIRTY, // 3
    GRAVEL, // 4
    RUMBLESTRIP, // 5
    ;
    
    public static final SurfaceType getFromIndex( final short index )
    {
        switch ( index )
        {
            case 0:
                return ( DRY );
            case 1:
                return ( WET );
            case 2:
                return ( GRASS );
            case 3:
                return ( DIRTY );
            case 4:
                return ( GRAVEL );
            case 5:
                return ( RUMBLESTRIP );
        }
        
        // Unreachable code!
        return ( null );
    }
}
