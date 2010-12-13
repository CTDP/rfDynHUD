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

import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Brakes.WheelBrake;


class LifetimeManager implements TelemetryData.TelemetryDataUpdateListener
{
    public static final LifetimeManager INSTANCE = new LifetimeManager();
    
    private int lastSessionID = -1;
    private int lastEnteredRealtimeID = -1;
    
    private float lastOilTemperatureC = -1f;
    private float lastEngineRPM = -1f;
    private float lastVelocityMPS = -1f;
    
    private double engineLifetime100Percent = -1.0;
    private double engineLifetimeLoss = -1.0;
    
    private float lastBrakeApplication = 0.0f;
    private float lastBrakeTemperatureFL = 1.0f;
    private float lastBrakeTemperatureFR = 1.0f;
    private float lastBrakeTemperatureRL = 1.0f;
    private float lastBrakeTemperatureRR = 1.0f;
    private float lastWheelRotationFL = 0.0f;
    private float lastWheelRotationFR = 0.0f;
    private float lastWheelRotationRL = 0.0f;
    private float lastWheelRotationRR = 0.0f;
    
    private double startBrakeDiscThicknessFL = 0.0;
    private double startBrakeDiscThicknessFR = 0.0;
    private double startBrakeDiscThicknessRL = 0.0;
    private double startBrakeDiscThicknessRR = 0.0;
    private double brakeDiscThicknessFLLoss = 0.0;
    private double brakeDiscThicknessFRLoss = 0.0;
    private double brakeDiscThicknessRLLoss = 0.0;
    private double brakeDiscThicknessRRLoss = 0.0;
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode ) {}
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode ) {}
    
    /**
     * Reads initial values for engine lifetime and brake discs' thicknesses.
     * 
     * @param engine the vehicle physics engine
     * @param setup the vehicle setup
     */
    private void readInitialValues( VehiclePhysics.Engine engine, VehicleSetup setup )
    {
        engineLifetime100Percent = engine.getSafeLifetimeTotal( 1.0 );
        engineLifetimeLoss = 0.0f;
        
        startBrakeDiscThicknessFL = setup.getWheelAndTire( Wheel.FRONT_LEFT ).getBrakeDiscThickness();
        startBrakeDiscThicknessFR = setup.getWheelAndTire( Wheel.FRONT_RIGHT ).getBrakeDiscThickness();
        startBrakeDiscThicknessRL = setup.getWheelAndTire( Wheel.REAR_LEFT ).getBrakeDiscThickness();
        startBrakeDiscThicknessRR = setup.getWheelAndTire( Wheel.REAR_RIGHT ).getBrakeDiscThickness();
        brakeDiscThicknessFLLoss = 0.0;
        brakeDiscThicknessFRLoss = 0.0;
        brakeDiscThicknessRLLoss = 0.0;
        brakeDiscThicknessRRLoss = 0.0;
    }
    
    /**
     * Records current relevant telemetry data.
     * 
     * @param telemData the telemetry data interface
     * @param viewedVSI VehicleScoringInfo for the currently viewed vehicle
     */
    private void recordTelemetryData( TelemetryData telemData, VehicleScoringInfo viewedVSI )
    {
        if ( ( viewedVSI == null ) || viewedVSI.isPlayer() )
        {
            // Read relevant data for engine wear
            lastOilTemperatureC = telemData.getEngineOilTemperatureC();
            lastEngineRPM = telemData.getEngineRPM();
            lastVelocityMPS = telemData.getScalarVelocityMPS();
            
            // Read current (unfiltered) brake application [0..1]
            lastBrakeApplication = telemData.getUnfilteredBrake();
            
            if ( ( viewedVSI == null ) || viewedVSI.getVehicleControl().isLocalPlayer() )
            {
                // Read current brake disc temperatures in Kelvin
                lastBrakeTemperatureFL = telemData.getBrakeTemperatureK( Wheel.FRONT_LEFT );
                lastBrakeTemperatureFR = telemData.getBrakeTemperatureK( Wheel.FRONT_RIGHT );
                lastBrakeTemperatureRL = telemData.getBrakeTemperatureK( Wheel.REAR_LEFT );
                lastBrakeTemperatureRR = telemData.getBrakeTemperatureK( Wheel.REAR_RIGHT );
            }
            
            // Read current wheel rotations in m/s
            lastWheelRotationFL = telemData.getWheelRotation( Wheel.FRONT_LEFT );
            lastWheelRotationFR = telemData.getWheelRotation( Wheel.FRONT_RIGHT );
            lastWheelRotationRL = telemData.getWheelRotation( Wheel.REAR_LEFT );
            lastWheelRotationRR = telemData.getWheelRotation( Wheel.REAR_RIGHT );
        }
    }
    
    /**
     * Computes the engine wear.
     * The value needs to be multiplied with time slice in seconds.
     * 
     * @param lastOilTemperatureC the oil temperature in Celsius (at the beginning of time slice)
     * @param lastEngineRPM the engine RPM (at the beginning of time slice)
     * @param lastVelocityMPS the velocity in m/s (at the beginning of time slice)
     * @param engine the vehicle physics engine interface
     * @param telemData the telemetry data interface
     * 
     * @return the wear (needs to be multiplied by time slice in seconds).
     */
    private static final double computeEngineWear( float lastOilTemperatureC, float lastEngineRPM, float lastVelocityMPS, VehiclePhysics.Engine engine, TelemetryData telemData )
    {
        double factorOilTemp = Math.pow( 2.0, ( lastOilTemperatureC - engine.getBaseLifetimeOilTemperatureC() ) / engine.getHalfLifetimeOilTempOffsetC() );
        
        float rpm = ( lastEngineRPM + telemData.getEngineRPM() ) / 2.0f; // average engine rounds per minute over the last telem-data update period (ca. 11ms)
        double factorRPM = Math.pow( 2.0, ( rpm - engine.getBaseLifetimeRPM() ) / engine.getHalfLifetimeRPMOffset() );
        
        double factorBoost = 1.0 + ( telemData.getEffectiveEngineBoostMapping() - engine.getBoostRange().getMinValue() ) * engine.getWearIncreasePerBoostLevel();
        
        float velocityMPS = ( lastVelocityMPS + telemData.getScalarVelocityMPS() ) / 2.0f; // average velocity over the last telem-data update period (ca. 11ms) in m/s
        double factorVelocity = 1.0 + velocityMPS * engine.getWearIncreasePerVelocity();
        
        double engineWear = ( ( factorOilTemp + factorRPM ) / 2.0 ) * factorBoost * factorVelocity;
        
        return ( engineWear );
    }
    
    /**
     * Computes brake disc wear.
     * The value needs to be multiplied with time slice in seconds.
     * 
     * @param brakeApplication the current (unfiltered) brake application [0..1]
     * @param brakePressure the brake pressure from setup [0..1]
     * @param brake the {@link WheelBrake} from vehicle physics
     * @param rearBrakeBias the brake bias for the rear end
     * @param lastWheelRotation wheel rotation in radians/sec (at the beginning of time slice)
     * @param lastBrakeTemperatureK brake disc temperature in Kelvin (at the beginning of time slice)
     * @param telemData the telemetry data
     * 
     * @return the wear (needs to be multiplied by time slice in seconds).
     */
    private static final double computeBrakeWear( float brakeApplication, float brakePressure, WheelBrake brake, float rearBrakeBias, float lastWheelRotation, float lastBrakeTemperatureK, TelemetryData telemData )
    {
        Wheel wheel = brake.getWheel();
        float brakeBias = wheel.isFront() ? ( 1.0f - rearBrakeBias ) : rearBrakeBias;
        float wheelRotation = ( telemData.getWheelRotation( wheel ) + lastWheelRotation ) / 2.0f; // average wheel rotation over the last telem-data update period (ca. 11ms) in radians/sec
        double avgOptTemp = ( brake.getOptimumTemperaturesLowerBoundK() + brake.getOptimumTemperaturesUpperBoundK() ) / 2.0;
        double wearRate = brake.getWearRate();
        float brakeTemperature = ( telemData.getBrakeTemperatureK( wheel ) + lastBrakeTemperatureK ) / 2.0f; // average disc temperature over the last telem-data update period (ca. 11ms) in Kelvin
        double torque = brake.computeTorque( brakeTemperature );
        //torque *= telemData.getBrakeDiscThickness( wheel ) / brakeDiscThicknessFL100Percent;
        
        // brake bias for that end * brake application * brake pressure * ( ( brakewear rate * temperature (Kelvin) ^ 3 ) / ( ( brake response curve value 2 + brake response curve value 3 + 271.15 *2 ) / 2 ) ^ 3 )
        
        double brakeWear = brakeBias * brakeApplication * brakePressure * Math.abs( wheelRotation ) * wearRate * torque * Math.pow( brakeTemperature, 3.0 ) / Math.pow( avgOptTemp, 3.0 );
        
        return ( brakeWear );
    }
    
    /**
     * Applies the computed wear values to telemetry data.
     * 
     * @param telemData the telemetry data interface
     * @param scoringInfo the scoring info interface
     */
    private void applyWearValues( TelemetryData telemData, ScoringInfo scoringInfo )
    {
        final double raceLengthPercentage = scoringInfo.getRaceLengthPercentage();
        final double recipRaceLengthPercentage = 1.0 / raceLengthPercentage;
        
        telemData.engineLifetime = (float)( ( engineLifetime100Percent * raceLengthPercentage ) - engineLifetimeLoss );
        
        telemData.brakeDiscThicknessFL = (float)( startBrakeDiscThicknessFL - ( brakeDiscThicknessFLLoss * recipRaceLengthPercentage ) );
        telemData.brakeDiscThicknessFR = (float)( startBrakeDiscThicknessFR - ( brakeDiscThicknessFRLoss * recipRaceLengthPercentage ) );
        telemData.brakeDiscThicknessRL = (float)( startBrakeDiscThicknessRL - ( brakeDiscThicknessRLLoss * recipRaceLengthPercentage ) );
        telemData.brakeDiscThicknessRR = (float)( startBrakeDiscThicknessRR - ( brakeDiscThicknessRRLoss * recipRaceLengthPercentage ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        // This method is executed at 90Hz (ca. 11ms) and calls all the other implemented methods in this class.
        
        final VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        final VehiclePhysics.Brakes brakes = gameData.getPhysics().getBrakes();
        final TelemetryData telemData = gameData.getTelemetryData();
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        final VehicleSetup setup = gameData.getSetup();
        
        if ( !setup.isUpdatedInTimeScope() )
            return;
        
        if ( ( scoringInfo.getSessionId() > lastSessionID ) || ( scoringInfo.getRealtimeEntredId() > lastEnteredRealtimeID ) )
        {
            // A new session has been started or you entered the cockit.
            // We need to reset everything.
            
            lastSessionID = scoringInfo.getSessionId();
            lastEnteredRealtimeID = scoringInfo.getRealtimeEntredId();
            
            readInitialValues( engine, setup );
        }
        else if ( !gameData.isGamePaused() )
        {
            // Read time in seconds between the last and the current telemetry data update
            double deltaTime = telemData.getDeltaUpdateTime() / 1000000000.0;
            
            // Compute engine lifetime loss for the last time slice
            //if ( telemData.getEngineRPM() > 1.00f )
            {
                double engineWear = computeEngineWear( lastOilTemperatureC, lastEngineRPM, lastVelocityMPS, engine, telemData );
                engineLifetimeLoss += deltaTime * engineWear;
            }
            
            // Compute brake discs' wear for the last time slice
            float brakeApplication = ( lastBrakeApplication + telemData.getUnfilteredBrake() ) / 2.0f; // average brake application over the last telem-data update period (ca. 11ms)
            if ( brakeApplication > 0.0f );
            {
                float brakePressure = setup.getControls().getBrakePressure();
                float rearBrakeBias = setup.getControls().getRearBrakeBalance(); // TODO: We need to make this dynamic!
                
                double brakeWearFL = computeBrakeWear( brakeApplication, brakePressure, brakes.getBrake( Wheel.FRONT_LEFT ), rearBrakeBias, lastWheelRotationFL, lastBrakeTemperatureFL, telemData );
                brakeDiscThicknessFLLoss += deltaTime * brakeWearFL;
                
                double brakeWearFR = computeBrakeWear( brakeApplication, brakePressure, brakes.getBrake( Wheel.FRONT_RIGHT ), rearBrakeBias, lastWheelRotationFR, lastBrakeTemperatureFR, telemData );
                brakeDiscThicknessFRLoss += deltaTime * brakeWearFR;
                
                double brakeWearRL = computeBrakeWear( brakeApplication, brakePressure, brakes.getBrake( Wheel.REAR_LEFT ), rearBrakeBias, lastWheelRotationRL, lastBrakeTemperatureRL, telemData );
                brakeDiscThicknessRLLoss += deltaTime * brakeWearRL;
                
                double brakeWearRR = computeBrakeWear( brakeApplication, brakePressure, brakes.getBrake( Wheel.REAR_RIGHT ), rearBrakeBias, lastWheelRotationRR, lastBrakeTemperatureRR, telemData );
                brakeDiscThicknessRRLoss += deltaTime * brakeWearRR;
            }
        }
        
        // Now we need to apply the current engine and brake wear to telemetry data...
        applyWearValues( telemData, scoringInfo );
        
        // Record current data for the next cycle
        recordTelemetryData( telemData, scoringInfo.getViewedVehicleScoringInfo() );
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused ) {}
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, boolean isEditorMode ) {}
    
    LifetimeManager()
    {
    }
}
