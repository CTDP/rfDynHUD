package net.ctdp.rfdynhud.gamedata;

public class Laptime
{
    int lap;
    float sector1 = -1f;
    float sector2 = -1f;
    float sector3 = -1f;
    float laptime = -1f;
    
    boolean isOutLap = false;
    Boolean isInLap = null;
    boolean finished = false;
    
    public final int getLap()
    {
        return ( lap );
    }
    
    void updateLaptimeFromSectors()
    {
        if ( ( sector1 < 0f ) || ( sector2 < 0f ) || ( sector3 < 0f ) )
            laptime = -1f;
        else
            laptime = sector1 + sector2 + sector3;
    }
    
    public final float getSector1()
    {
        return ( sector1 );
    }
    
    public final float getSector2()
    {
        return ( sector2 );
    }
    
    public final float getSector1And2()
    {
        return ( sector1 + sector2 );
    }
    
    public final float getSector2( boolean includingSector1 )
    {
        if ( includingSector1 )
            return ( getSector1And2() );
        
        return ( getSector2() );
    }
    
    public final float getSector3()
    {
        return ( sector3 );
    }
    
    public final float getLapTime()
    {
        return ( laptime );
    }
    
    /**
     * Gets whether this lap is an outlap. If this information is not yet available, null is returned.
     * 
     * @return whether this lap is an outlap. If this information is not yet available, null is returned.
     */
    public final Boolean isOutlap()
    {
        return ( isOutLap );
    }
    
    /**
     * Gets whether this lap is an inlap. If this information is not yet available, null is returned.
     * 
     * @return whether this lap is an inlap. If this information is not yet available, null is returned.
     */
    public final Boolean isInlap()
    {
        return ( isInLap );
    }
    
    public final boolean isFinished()
    {
        return ( finished );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( "lap: " + getLap() + ", sec1: " + getSector1() + ", sec2: " + getSector2() + ", sec3: " + getSector3() + ", laptime: " + getLapTime() + ( isOutlap() == Boolean.TRUE ? ", OUTLAP" : ( isOutlap() == null ? ", UNKNOWN" : ", REGULAR" ) ) + ( isInlap() == Boolean.TRUE ? ", INLAP" : ( isInlap() == null ? ", UNKNOWN" : ", REGULAR" ) ) );
    }
    
    public Laptime( int lap )
    {
        this.lap = lap;
    }
    
    public Laptime( int lap, float sector1, float sector2, float sector3, boolean isOutLap, boolean isInLap, boolean finished )
    {
        this.lap = lap;
        this.sector1 = sector1;
        this.sector2 = sector2;
        this.sector3 = sector3;
        this.isOutLap = isOutLap;
        this.isInLap = isInLap;
        this.finished = finished;
        
        updateLaptimeFromSectors();
    }
}
