package net.ctdp.rfdynhud.gamedata;

public class FuelUsageRecorder implements ScoringInfo.ScoringInfoUpdateListener
{
    public static final FuelUsageRecorder MASTER_FUEL_USAGE_RECORDER = new FuelUsageRecorder();
    
    private int oldSessionID = -1;
    private boolean wasInRealTime = false;
    
    private float lastLap = -1f;
    private float average = -1f;
    
    private short oldLapsCompleted = -1;
    private float lapStartFuel = -1f;
    
    private int fuelRelevantLaps = 0;
    private float relevantFuel = 0f;
    
    public final float getLastLap()
    {
        return ( lastLap );
    }
    
    public final float getAverage()
    {
        return ( average );
    }
    
    public final int getFuelRelevantLaps()
    {
        return ( fuelRelevantLaps );
    }
    
    public void reset()
    {
        lastLap = -1f;
        average = -1f;
        
        oldLapsCompleted = -1;
        lapStartFuel = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( scoringInfo.getSessionID() != oldSessionID )
        {
            // new session
            oldSessionID = scoringInfo.getSessionID();
            
            fuelRelevantLaps = 0;
            relevantFuel = 0f;
            average = -1f;
            lastLap = -1f;
        }
        
        if ( scoringInfo.isInRealtimeMode() != wasInRealTime )
        {
            // entered realtime mode (cockpit)
            wasInRealTime = scoringInfo.isInRealtimeMode();
            
            if ( wasInRealTime )
            {
                lapStartFuel = -1f;
                lastLap = -1f;
            }
        }
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        short lapsCompleted = vsi.getLapsCompleted();
        
        if ( lapsCompleted != oldLapsCompleted )
        {
            int stintLength = (int)vsi.getStintLength();
            
            float fuel = gameData.getTelemetryData().getFuel();
            
            if ( stintLength >= 2 )
            {
                fuelRelevantLaps++;
                lastLap = lapStartFuel - fuel;
                relevantFuel += lastLap;
            }
            
            lapStartFuel = fuel;
            
            if ( stintLength >= 2 )
            {
                average = relevantFuel / (float)( fuelRelevantLaps );
            }
            
            oldLapsCompleted = lapsCompleted;
        }
    }
}
