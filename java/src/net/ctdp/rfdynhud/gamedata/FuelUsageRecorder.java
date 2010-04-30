package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputActionConsumer;
import net.ctdp.rfdynhud.input.__InpPrivilegedAccess;
import net.ctdp.rfdynhud.util.Logger;

public class FuelUsageRecorder implements ScoringInfo.ScoringInfoUpdateListener
{
    public static final File FUEL_USAGE_FILE = new File( Logger.FOLDER, "fuel_consumption" );
    
    private static final class MasterFuelUsageRecorder extends FuelUsageRecorder implements InputActionConsumer
    {
        private long firstResetStrokeTime = -1L;
        private int resetStrokes = 0;
        
        @Override
        public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
        {
            if ( action == INPUT_ACTION_RESET_FUEL_CONSUMPTION )
            {
                long t = System.nanoTime();
                
                if ( t - firstResetStrokeTime > 1000000000L )
                {
                    resetStrokes = 1;
                    firstResetStrokeTime = t;
                }
                else if ( ++resetStrokes >= 3 )
                {
                    liveReset();
                }
            }
        }
    }
    
    public static final FuelUsageRecorder MASTER_FUEL_USAGE_RECORDER = new MasterFuelUsageRecorder();
    static final InputAction INPUT_ACTION_RESET_FUEL_CONSUMPTION = __InpPrivilegedAccess.createInputAction( "ResetFuelConsumption", true, false, (InputActionConsumer)MASTER_FUEL_USAGE_RECORDER, FuelUsageRecorder.class.getClassLoader().getResource( FuelUsageRecorder.class.getPackage().getName().replace( '.', '/' ) + "/doc/ResetFuelConsumption.html" ) );
    
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
        
        fuelRelevantLaps = 0;
        relevantFuel = 0f;
    }
    
    /**
     * Call this to reset the recorder while in cockpit.
     */
    public void liveReset()
    {
        oldLapsCompleted = -1;
        
        lastLap = -1f;
        average = -1f;
        
        fuelRelevantLaps = 0;
        relevantFuel = 0f;
    }
    
    public void onSessionStarted( LiveGameData gameData )
    {
        reset();
    }
    
    public void onRealtimeEntered( LiveGameData gameData )
    {
        oldLapsCompleted = -1;
        lapStartFuel = -1f;
        //lastLap = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( !scoringInfo.isInRealtimeMode() )
            return;
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        short lapsCompleted = vsi.getLapsCompleted();
        
        if ( oldLapsCompleted == -1 )
        {
            oldLapsCompleted = lapsCompleted;
        }
        
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
    
    public void onRealtimeExited( LiveGameData gameData )
    {
        float avgUsage = FuelUsageRecorder.MASTER_FUEL_USAGE_RECORDER.getAverage();
        
        if ( avgUsage > 0f )
        {
            try
            {
                Writer w = new FileWriter( FUEL_USAGE_FILE );
                w.write( String.valueOf( avgUsage ) );
                w.close();
            }
            catch ( Throwable t )
            {
            }
        }
    }
}
