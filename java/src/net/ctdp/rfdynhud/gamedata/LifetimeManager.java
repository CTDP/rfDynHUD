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

class LifetimeManager implements TelemetryData.TelemetryDataUpdateListener
{
    public static final LifetimeManager INSTANCE = new LifetimeManager();
    
    private int lastSessionID = -1;
    private int lastEnteredRealtimeID = -1;
    
    private float lastOilTemperature = -1f;
    private float lastEngineRevs = -1f;
    private int lastEngineBoost = -1;
    private float lastVelocity = -1f;
    
    private double engineLifetime100Percent = -1.0;
    private double engineLifetimeLoss = -1.0;
    
    private float lastBrakeBias = 0.5f;
    private float lastBrakeApplication = 0.0f;
    private float lastBrakeTemperatureFL = 1.0f;
    private float lastBrakeTemperatureFR = 1.0f;
    private float lastBrakeTemperatureRL = 1.0f;
    private float lastBrakeTemperatureRR = 1.0f;
    private float lastWheelRotationFL = 0.0f;
    private float lastWheelRotationFR = 0.0f;
    private float lastWheelRotationRL = 0.0f;
    private float lastWheelRotationRR = 0.0f;
    
    private double brakeDiscThicknessFL100Percent = 0.0;
    private double brakeDiscThicknessFR100Percent = 0.0;
    private double brakeDiscThicknessRL100Percent = 0.0;
    private double brakeDiscThicknessRR100Percent = 0.0;
    private double brakeDiscThicknessFLLoss = 0.0;
    private double brakeDiscThicknessFRLoss = 0.0;
    private double brakeDiscThicknessRLLoss = 0.0;
    private double brakeDiscThicknessRRLoss = 0.0;
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode ) {}
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode ) {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onTelemetryDataUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        final VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        final VehiclePhysics.Brakes brakes = gameData.getPhysics().getBrakes();
        final TelemetryData telemData = gameData.getTelemetryData();
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        final VehicleSetup setup = gameData.getSetup();
        
        if ( !setup.isUpdatedInTimeScope() )
            return;
        
        if ( ( scoringInfo.getSessionId() > lastSessionID ) || ( scoringInfo.getRealtimeEntredId() > lastEnteredRealtimeID ) )
        {
            lastSessionID = scoringInfo.getSessionId();
            lastEnteredRealtimeID = scoringInfo.getRealtimeEntredId();
            engineLifetime100Percent = engine.getSafeLifetimeTotal( 1.0 );
            engineLifetimeLoss = 0.0f;
            
            brakeDiscThicknessFL100Percent = setup.getWheelAndTire( Wheel.FRONT_LEFT ).getBrakeDiscThickness();
            brakeDiscThicknessFR100Percent = setup.getWheelAndTire( Wheel.FRONT_RIGHT ).getBrakeDiscThickness();
            brakeDiscThicknessRL100Percent = setup.getWheelAndTire( Wheel.REAR_LEFT ).getBrakeDiscThickness();
            brakeDiscThicknessRR100Percent = setup.getWheelAndTire( Wheel.REAR_RIGHT ).getBrakeDiscThickness();
            brakeDiscThicknessFLLoss = 0.0;
            brakeDiscThicknessFRLoss = 0.0;
            brakeDiscThicknessRLLoss = 0.0;
            brakeDiscThicknessRRLoss = 0.0;
        }
        else if ( !gameData.isGamePaused() )
        {
            double deltaTime = telemData.getDeltaUpdateTime() / 1000000000.0;
            
            // engine lifetime
            //if ( telemData.getEngineRPM() > 1.00f )
            {
                double factorOilTemp = Math.pow( 2.0, ( lastOilTemperature - engine.getBaseLifetimeOilTemperatureC() ) / engine.getHalfLifetimeOilTempOffsetC() );
                
                float avgRevs = ( telemData.getEngineRPM() + lastEngineRevs ) / 2.0f;
                double factorRPM = Math.pow( 2.0, ( avgRevs - engine.getBaseLifetimeRPM() ) / engine.getHalfLifetimeRPMOffset() );
                
                double factorBoost = 1.0 + ( lastEngineBoost - engine.getBoostRange().getMinValue() ) * engine.getWearIncreasePerBoostLevel();
                
                double factorVelocity = 1.0 + lastVelocity * engine.getWearIncreasePerVelocity();
                
                engineLifetimeLoss += deltaTime * ( ( factorOilTemp + factorRPM ) / 2.0 ) * factorBoost * factorVelocity;
            }
            
            // brakes lifetime
            {
                float currBrakeApplication = telemData.getUnfilteredBrake();
                float avgBrakeApplication = ( lastBrakeApplication + currBrakeApplication ) / 2.0f;
                float brakePressure = setup.getControls().getBrakePressure();
                
                //brake bias for that end * brake application * brake pressure * ((brakewear rate * temperature (Kelvin) ^ 3 ) / ( ( brake response curve value 2 + brake response curve value 3 + 271.15 *2 ) / 2 ) ^ 3)
                
                Wheel wheel = Wheel.FRONT_LEFT;
                float lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundK();
                float upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundK();
                double avgOptTemp = ( lowerOptTemp + upperOptTemp ) / 2.0;
                float avgBrakeTemperatureFL = ( telemData.getBrakeTemperatureK( Wheel.FRONT_LEFT ) + lastBrakeTemperatureFL ) / 2.0f;
                float wearRate = brakes.getBrake( wheel ).getWearRate();
                double torque = brakes.getBrake( wheel ).computeTorque( avgBrakeTemperatureFL );
                double brakeWearFL = torque * ( 1.0f - lastBrakeBias ) * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationFL ) * wearRate * Math.pow( avgBrakeTemperatureFL, 3.0 ) / Math.pow( avgOptTemp, 3.0 );
                
                brakeDiscThicknessFLLoss += deltaTime * brakeWearFL;
                
                wheel = Wheel.FRONT_RIGHT;
                lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundK();
                upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundK();
                avgOptTemp = ( lowerOptTemp + upperOptTemp ) / 2.0;
                float avgBrakeTemperatureFR = ( telemData.getBrakeTemperatureK( Wheel.FRONT_RIGHT ) + lastBrakeTemperatureFR ) / 2.0f;
                wearRate = brakes.getBrake( wheel ).getWearRate();
                torque = brakes.getBrake( wheel ).computeTorque( avgBrakeTemperatureFR );
                double brakeWearFR = torque * ( 1.0f - lastBrakeBias ) * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationFR ) * wearRate * Math.pow( avgBrakeTemperatureFR, 3.0 ) / Math.pow( avgOptTemp, 3.0 );
                
                brakeDiscThicknessFRLoss += deltaTime * brakeWearFR;
                
                wheel = Wheel.REAR_LEFT;
                lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundK();
                upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundK();
                avgOptTemp = ( lowerOptTemp + upperOptTemp ) / 2.0;
                float avgBrakeTemperatureRL = ( telemData.getBrakeTemperatureK( Wheel.REAR_LEFT ) + lastBrakeTemperatureRL ) / 2.0f;
                wearRate = brakes.getBrake( wheel ).getWearRate();
                torque = brakes.getBrake( wheel ).computeTorque( avgBrakeTemperatureRL );
                double brakeWearRL = torque * lastBrakeBias * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationRL ) * wearRate * Math.pow( avgBrakeTemperatureRL, 3.0 ) / Math.pow( avgOptTemp, 3.0 );
                
                brakeDiscThicknessRLLoss += deltaTime * brakeWearRL;
                
                wheel = Wheel.REAR_RIGHT;
                lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundK();
                upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundK();
                avgOptTemp = ( lowerOptTemp + upperOptTemp ) / 2.0;
                float avgBrakeTemperatureRR = ( telemData.getBrakeTemperatureK( Wheel.REAR_RIGHT ) + lastBrakeTemperatureRR ) / 2.0f;
                wearRate = brakes.getBrake( wheel ).getWearRate();
                torque = brakes.getBrake( wheel ).computeTorque( avgBrakeTemperatureRR );
                double brakeWearRR = torque * lastBrakeBias * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationRR ) * wearRate * Math.pow( avgBrakeTemperatureRR, 3.0 ) / Math.pow( avgOptTemp, 3.0 );
                
                brakeDiscThicknessRRLoss += deltaTime * brakeWearRR;
            }
        }
        
        final double raceLengthPercentage = scoringInfo.getRaceLengthPercentage();
        final double recipRaceLengthPercentage = 1.0 / raceLengthPercentage;
        
        telemData.engineLifetime = (float)( ( engineLifetime100Percent * raceLengthPercentage ) - engineLifetimeLoss );
        
        telemData.brakeDiscThicknessFL = (float)( brakeDiscThicknessFL100Percent - ( brakeDiscThicknessFLLoss * recipRaceLengthPercentage ) );
        telemData.brakeDiscThicknessFR = (float)( brakeDiscThicknessFR100Percent - ( brakeDiscThicknessFRLoss * recipRaceLengthPercentage ) );
        telemData.brakeDiscThicknessRL = (float)( brakeDiscThicknessRL100Percent - ( brakeDiscThicknessRLLoss * recipRaceLengthPercentage ) );
        telemData.brakeDiscThicknessRR = (float)( brakeDiscThicknessRR100Percent - ( brakeDiscThicknessRRLoss * recipRaceLengthPercentage ) );
        
        lastOilTemperature = telemData.getEngineOilTemperatureC();
        lastEngineRevs = telemData.getEngineRPM();
        lastEngineBoost = telemData.getEffectiveEngineBoostMapping();
        lastVelocity = telemData.getScalarVelocityMPS();
        
        lastBrakeBias = setup.getControls().getRearBrakeBalance();
        lastBrakeApplication = telemData.getUnfilteredBrake();
        if ( ( scoringInfo.getViewedVehicleScoringInfo() == null ) || ( scoringInfo.getViewedVehicleScoringInfo().isPlayer() && scoringInfo.getViewedVehicleScoringInfo().getVehicleControl().isLocalPlayer() ) )
        {
            lastBrakeTemperatureFL = telemData.getBrakeTemperatureK( Wheel.FRONT_LEFT );
            lastBrakeTemperatureFR = telemData.getBrakeTemperatureK( Wheel.FRONT_RIGHT );
            lastBrakeTemperatureRL = telemData.getBrakeTemperatureK( Wheel.REAR_LEFT );
            lastBrakeTemperatureRR = telemData.getBrakeTemperatureK( Wheel.REAR_RIGHT );
        }
        lastWheelRotationFL = telemData.getWheelRotation( Wheel.FRONT_LEFT );
        lastWheelRotationFR = telemData.getWheelRotation( Wheel.FRONT_RIGHT );
        lastWheelRotationRL = telemData.getWheelRotation( Wheel.REAR_LEFT );
        lastWheelRotationRR = telemData.getWheelRotation( Wheel.REAR_RIGHT );
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused ) {}
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, boolean isEditorMode ) {}
    
    LifetimeManager()
    {
    }
}
