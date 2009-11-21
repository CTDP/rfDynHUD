package net.ctdp.rfdynhud.gamedata;

public class TopspeedRecorder implements TelemetryData.TelemetryDataUpdateListener
{
    public static final TopspeedRecorder MASTER_TOPSPEED_RECORDER = new TopspeedRecorder();
    
    private float value = 0f;
    
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
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData )
    {
        float velocity = gameData.getTelemetryData().getScalarVelocityKPH();
        
        if ( velocity > value )
            value = velocity;
    }
}
