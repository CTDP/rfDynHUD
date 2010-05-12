package net.ctdp.rfdynhud.values;

public enum LapDisplayType
{
    CURRENT_LAP,
    LAPS_DONE,
    ;
    
    public final boolean isCurrentLap()
    {
        return ( this == CURRENT_LAP );
    }
    
    public final boolean isLapsDone()
    {
        return ( this == LAPS_DONE );
    }
}
