package net.ctdp.rfdynhud.gamedata;

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
                long t = when;
                
                if ( t - firstResetStrokeTime > 1000000000L )
                {
                    resetStrokes = 1;
                    firstResetStrokeTime = t;
                }
                else if ( ++resetStrokes >= 3 )
                {
                    liveReset( gameData.getScoringInfo() );
                    
                    resetStrokes = 0;
                }
            }
        }
    }
    
    static final TopspeedRecorder MASTER_TOPSPEED_RECORDER = new MasterTopspeedRecorder();
    static final InputAction INPUT_ACTION_RESET_TOPSPEEDS = __InpPrivilegedAccess.createInputAction( "ResetTopSpeeds", true, false, (InputActionConsumer)MASTER_TOPSPEED_RECORDER, FuelUsageRecorder.class.getClassLoader().getResource( FuelUsageRecorder.class.getPackage().getName().replace( '.', '/' ) + "/doc/ResetTopSpeeds.html" ) );
    
    private long firstValidTime = Long.MAX_VALUE;
    
    /**
     * Call this to reset the recorder while in cockpit.
     */
    public void liveReset( ScoringInfo scoringInfo )
    {
        int n = scoringInfo.getNumVehicles();
        for ( int i = 0; i < n; i++ )
            scoringInfo.getVehicleScoringInfo( i ).topspeed = 0f;
    }
    
    public void reset( ScoringInfo scoringInfo )
    {
        liveReset( scoringInfo );
        
        firstValidTime = scoringInfo.getSessionNanos() + 3L * 1000000000L;
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, EditorPresets editorPresets )
    {
        reset( gameData.getScoringInfo() );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        firstValidTime = gameData.getScoringInfo().getSessionNanos() + 3L * 1000000000L;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( !gameData.isInRealtimeMode() || ( scoringInfo.getSessionNanos() < firstValidTime ) )
            return;
        
        VehicleScoringInfo vsi = scoringInfo.getPlayersVehicleScoringInfo();
        
        float velocity = gameData.getTelemetryData().getScalarVelocity();
        
        if ( velocity > vsi.topspeed )
            vsi.topspeed = velocity;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        final int n = scoringInfo.getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            if ( !vsi.isPlayer() || ( gameData.isInRealtimeMode() && ( scoringInfo.getSessionNanos() >= firstValidTime ) ) )
            {
                float velocity = vsi.getScalarVelocity();
                
                if ( velocity > vsi.topspeed )
                    vsi.topspeed = velocity;
            }
        }
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, EditorPresets editorPresets, boolean isPaused ) {}
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets ) {}
}
