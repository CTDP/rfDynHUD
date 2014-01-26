/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputActionConsumer;
import net.ctdp.rfdynhud.input.__InpPrivilegedAccess;

/**
 * Records fuel usages per lap.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class FuelUsageRecorder implements ScoringInfo.ScoringInfoUpdateListener
{
    private static final class MasterFuelUsageRecorder extends FuelUsageRecorder implements InputActionConsumer
    {
        @Override
        protected void onValuesUpdated( LiveGameData gameData, int fuelRelevantLaps, float relevantFuel, float lastLap, float average )
        {
            gameData.getTelemetryData().fuelUsageLastLap = lastLap;
            gameData.getTelemetryData().fuelUsageAverage = average;
        }
        
        @Override
        public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
        {
            if ( action == INPUT_ACTION_RESET_FUEL_CONSUMPTION )
            {
                liveReset( gameData );
            }
        }
        
        @Override
        public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
        {
            super.onSessionStarted( gameData, isEditorMode );
            
            Float cached = DataCache.INSTANCE.getFuelUsage( gameData.getProfileInfo().getTeamName() );
            if ( cached != null )
            {
                super.fuelRelevantLaps = 1;
                super.relevantFuel = -1f;
                super.average = cached.floatValue();
                
                onValuesUpdated( gameData, super.fuelRelevantLaps, super.relevantFuel, super.lastLap, super.average );
            }
        }
    }
    
    static final FuelUsageRecorder MASTER_FUEL_USAGE_RECORDER = new MasterFuelUsageRecorder();
    static final InputAction INPUT_ACTION_RESET_FUEL_CONSUMPTION = __InpPrivilegedAccess.createInputAction( "ResetFuelConsumption", true, false, (InputActionConsumer)MASTER_FUEL_USAGE_RECORDER, FuelUsageRecorder.class.getClassLoader().getResource( FuelUsageRecorder.class.getPackage().getName().replace( '.', '/' ) + "/doc/ResetFuelConsumption.html" ) );
    
    private boolean setByEditor = false;
    
    private float lastLap = -1f;
    private float average = -1f;
    
    private short oldLapsCompleted = -1;
    private float lapStartFuel = -1f;
    
    private int fuelRelevantLaps = 0;
    private float relevantFuel = -1f;
    
    /**
     * Gets the fuel usage of the last (timed) lap.
     * 
     * @return the fuel usage of the last (timed) lap.
     */
    public final float getLastLap()
    {
        return ( lastLap );
    }
    
    /**
     * Gets the average fuel usage of all recorded (timed) laps.
     * 
     * @return the average fuel usage of all recorded (timed) laps.
     */
    public final float getAverage()
    {
        return ( average );
    }
    
    /**
     * Gets the number of recorded (timed) laps.
     * 
     * @return the number of recorded (timed) laps.
     */
    public final int getFuelRelevantLaps()
    {
        return ( fuelRelevantLaps );
    }
    
    /**
     * This event is invoked when the key values have been updated.
     * 
     * @param gameData the game data
     * @param fuelRelevantLaps the fuel relevant laps
     * @param relevantFuel the relevant fuel
     * @param lastLap the last lap's fuel usage
     * @param average the average fuel usage
     */
    protected void onValuesUpdated( LiveGameData gameData, int fuelRelevantLaps, float relevantFuel, float lastLap, float average )
    {
    }
    
    /**
     * Call this to reset the whole thing.
     * 
     * @param gameData the game data
     */
    public void reset( LiveGameData gameData )
    {
        if ( setByEditor )
            return;
        
        lastLap = -1f;
        average = -1f;
        
        oldLapsCompleted = -1;
        lapStartFuel = -1f;
        
        fuelRelevantLaps = 0;
        relevantFuel = -1f;
        
        onValuesUpdated( gameData, fuelRelevantLaps, relevantFuel, lastLap, average );
    }
    
    /**
     * Call this to reset the recorder while in cockpit.
     * 
     * @param gameData the game data
     */
    public void liveReset( LiveGameData gameData )
    {
        if ( setByEditor )
            return;
        
        oldLapsCompleted = -1;
        
        //lastLap = -1f;
        average = -1f;
        
        fuelRelevantLaps = 0;
        relevantFuel = -1f;
        
        onValuesUpdated( gameData, fuelRelevantLaps, relevantFuel, lastLap, average );
    }
    
    void applyEditorPresets( LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
        
        lastLap = 3.456f;
        average = 3.397f;
        
        oldLapsCompleted = -1;
        lapStartFuel = -1f;
        
        fuelRelevantLaps = 4;
        relevantFuel = fuelRelevantLaps * average;
        
        setByEditor = true;
        
        onValuesUpdated( gameData, fuelRelevantLaps, relevantFuel, lastLap, average );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        reset( gameData );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCockpitEntered( LiveGameData gameData, boolean isEditorMode )
    {
        oldLapsCompleted = -1;
        lapStartFuel = -1f;
        //lastLap = -1f;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        //if ( !scoringInfo.isInCockpit() )
        if ( !gameData.isInCockpit() )
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
                lastLap = lapStartFuel - fuel;
                
                if ( relevantFuel < 0f )
                {
                    fuelRelevantLaps = 1;
                    relevantFuel = lastLap;
                }
                else
                {
                    fuelRelevantLaps++;
                    relevantFuel += lastLap;
                }
            }
            
            lapStartFuel = fuel;
            
            if ( stintLength >= 2 )
            {
                average = relevantFuel / fuelRelevantLaps;
                
                onValuesUpdated( gameData, fuelRelevantLaps, relevantFuel, lastLap, average );
            }
            
            oldLapsCompleted = lapsCompleted;
        }
    }
    
    @Override
    public void onPlayerJoined( LiveGameData gameData, VehicleScoringInfo joinedVSI, boolean rejoined ) {}
    
    @Override
    public void onPlayerLeft( LiveGameData gameData, Integer vsiID ) {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused ) {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCockpitExited( LiveGameData gameData, boolean isEditorMode ) {}
}
