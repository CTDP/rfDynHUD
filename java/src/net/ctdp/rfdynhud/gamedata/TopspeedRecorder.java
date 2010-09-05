/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.gamedata;

import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputActionConsumer;
import net.ctdp.rfdynhud.input.__InpPrivilegedAccess;

class TopspeedRecorder implements TelemetryData.TelemetryDataUpdateListener, ScoringInfo.ScoringInfoUpdateListener
{
    private static final class MasterTopspeedRecorder extends TopspeedRecorder implements InputActionConsumer
    {
        @Override
        public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
        {
            if ( action == INPUT_ACTION_RESET_TOPSPEEDS )
            {
                liveReset( gameData.getScoringInfo() );
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
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        reset( gameData.getScoringInfo() );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        firstValidTime = gameData.getScoringInfo().getSessionNanos() + 3L * 1000000000L;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData, boolean isEditorMode )
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
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
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
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused ) {}
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, boolean isEditorMode ) {}
}
