package net.ctdp.rfdynhud.gamedata;

import java.util.HashMap;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputActionConsumer;
import net.ctdp.rfdynhud.input.__InpPrivilegedAccess;

class TopspeedRecorder implements TelemetryData.TelemetryDataUpdateListener, ScoringInfo.ScoringInfoUpdateListener
{
    private static final class MasterTopspeedRecorder extends TopspeedRecorder implements InputActionConsumer
    {
        private long firstResetStrokeTime = -1L;
        private int resetStrokes = 0;
        
        @Override
        public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
        {
            if ( action == INPUT_ACTION_RESET_TOPSPEEDS )
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
    
    static final TopspeedRecorder MASTER_TOPSPEED_RECORDER = new MasterTopspeedRecorder();
    static final InputAction INPUT_ACTION_RESET_TOPSPEEDS = __InpPrivilegedAccess.createInputAction( "ResetTopSpeeds", true, false, (InputActionConsumer)MASTER_TOPSPEED_RECORDER );
    
    private static final class FloatContainer
    {
        public float value = 0f;
    }
    
    private final HashMap<String, FloatContainer> store = new HashMap<String, FloatContainer>();
    
    private long firstValidTime = Long.MAX_VALUE;
    
    /**
     * Call this to reset the recorder while in cockpit.
     */
    public void liveReset()
    {
        store.clear();
    }
    
    public void reset()
    {
        liveReset();
        
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
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( scoringInfo.getSessionNanos() < firstValidTime )
            return;
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        float velocity = gameData.getTelemetryData().getScalarVelocityKPH();
        
        String driverName = vsi.getDriverName();
        FloatContainer fc = store.get( driverName );
        if ( fc == null )
        {
            fc = new FloatContainer();
            fc.value = velocity;
            store.put( driverName, fc );
        }
        else if ( velocity > fc.value )
        {
            fc.value = velocity;
        }
        
        vsi.topspeed = fc.value;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( scoringInfo.getSessionNanos() < firstValidTime )
            return;
        
        final int n = scoringInfo.getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            float velocity = vsi.getScalarVelocityKPH();
            
            String driverName = vsi.getDriverName();
            FloatContainer fc = store.get( driverName );
            if ( fc == null )
            {
                fc = new FloatContainer();
                fc.value = velocity;
                store.put( driverName, fc );
            }
            else if ( velocity > fc.value )
            {
                fc.value = velocity;
            }
            
            vsi.topspeed = fc.value;
        }
    }
    
    public void onRealtimeExited( LiveGameData gameData ) {}
}
