package net.ctdp.rfdynhud.gamedata;

public class TopspeedRecorder implements TelemetryData.TelemetryDataUpdateListener
{
    public static final TopspeedRecorder MASTER_TOPSPEED_RECORDER = new TopspeedRecorder();
    
    private float value = 0f;
    
    private long firstValidTime = Long.MAX_VALUE;
    
    /**
     * Gets the recorded top speed as km/h (kph).
     * 
     * @return the recorded top speed.
     */
    public final float getTopSpeed()
    {
        return ( value );
    }
    
    public void reset()
    {
        value = 0f;
        firstValidTime = Long.MAX_VALUE;
    }
    
    public void onSessionStarted( LiveGameData gameData )
    {
        reset();
    }
    
    public void onRealtimeEntered( LiveGameData gameData )
    {
        firstValidTime = gameData.getScoringInfo().getSessionNanos() + 1L * 1000000000L;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData )
    {
        if ( gameData.getScoringInfo().getSessionNanos() >= firstValidTime )
        {
            float velocity = gameData.getTelemetryData().getScalarVelocityKPH();
            
            if ( velocity > value )
                value = velocity;
        }
    }
    
    public void onRealtimeExited( LiveGameData gameData ) {}
}
