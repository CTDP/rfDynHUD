package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
 */
public enum VehicleControl
{
    NOBODY( -1 ),
    LOCAL_PLAYER( 0 ),
    LOCAL_AI( 1 ),
    REMOTE( 2 ),
    REPLAY( 3 ),
    ;
    
    @SuppressWarnings( "unused" )
    private final byte ISI_VALUE;
    
    public final boolean isLocalPlayer()
    {
        return ( this == LOCAL_PLAYER );
    }
    
    private VehicleControl( int isi_value )
    {
        this.ISI_VALUE = (byte)isi_value;
    }
    
    static final VehicleControl getFromISIValue( byte isi_value )
    {
        switch ( isi_value )
        {
            case -1:
                return ( NOBODY );
            case 0:
                return ( LOCAL_PLAYER );
            case 1:
                return ( LOCAL_AI );
            case 2:
                return ( REMOTE );
            case 3:
                return ( REPLAY );
        }
        
        // Unreachable code!
        return ( null );
    }
}
