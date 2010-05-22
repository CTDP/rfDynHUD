package net.ctdp.rfdynhud.gamedata;

/**
 * 
 * @author Marvin Froehlich
 */
public enum FinishStatus
{
    NONE, // 0
    FINISHED, // 1
    DNF, // 2
    DQ, // 3
    ;
    
    public final boolean isNone()
    {
        return ( this == NONE );
    }
    
    public final boolean isOut()
    {
        return ( ( this == DNF ) || ( this == DQ ) );
    }
    
    public final boolean isFinished()
    {
        return ( this == FINISHED );
    }
}
