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

import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Brakes.WheelBrake;

class LifetimeManager implements TelemetryData.TelemetryDataUpdateListener
{
    public static final LifetimeManager INSTANCE = new LifetimeManager();
    
    private static final double FLT_BIG = 1000000000.0;
    private static final float PI = (float)Math.PI;
    
    private int lastSessionID = -1;
    private int lastEnteredRealtimeID = -1;
    
    private float lastOilTemperature = -1f;
    private float lastEngineRevs = -1f;
    private int lastEngineBoost = -1;
    private float lastVelocity = -1f;
    
    private double engineLifetime = -1.0;
    
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
    
    private double brakeDiscThicknessFL = 0.0;
    private double brakeDiscThicknessFR = 0.0;
    private double brakeDiscThicknessRL = 0.0;
    private double brakeDiscThicknessRR = 0.0;
    
    private final double computeTorque( WheelBrake brake, float brakeTemp )
    {
        float brakeFadeTemp = brake.getBrakeFadeRangeC(); // straight from the HDV (or Float.MAX_VALUE/2, if not found)
        float brakeFadeColdTemp = brake.getBrakeFadeColdTemperatureC(); // optimum lower bound minus BrakeFadeRange
        float brakeFadeHotTemp = brake.getBrakeFadeHotTemperatureC(); // optimum upper bound plus BrakeFadeRange
        
        if ( brakeFadeTemp < FLT_BIG )
            brakeFadeColdTemp += MeasurementUnits.Convert.ZERO_KELVIN;
        
        if ( brakeFadeTemp < FLT_BIG )
            brakeFadeHotTemp += MeasurementUnits.Convert.ZERO_KELVIN;
        
        double torque = brake.getTorque();
        
        if ( brakeTemp < brake.getOptimumTemperaturesLowerBoundC() )
        {
            if ( brakeTemp < brakeFadeColdTemp )
            {
                torque *= 0.5;
            }
            else
            {
                final float coldRange = brakeFadeColdTemp - brake.getOptimumTemperaturesLowerBoundC();
                final float brakeFadeColdMult = ( coldRange < 0.0f ) ? ( PI / coldRange ) : 0.0f;
                
                torque = torque * ( 0.75 + ( 0.25 * Math.cos( ( brakeTemp - brake.getOptimumTemperaturesLowerBoundC() ) * brakeFadeColdMult ) ) );
            }
        }
        else if ( brakeTemp > brakeFadeHotTemp )
        {
            torque *= 0.5;
        }
        else if ( brakeTemp > brake.getOptimumTemperaturesUpperBoundC() )
        {
            final float hotRange = brakeFadeHotTemp - brake.getOptimumTemperaturesUpperBoundC();
            final float brakeFadeHotMult = ( hotRange < 0.0f ) ? ( PI / hotRange ) : 0.0f;
            
            torque = torque * ( 0.75 + ( 0.25 * Math.cos( ( brakeTemp - brake.getOptimumTemperaturesUpperBoundC() ) * brakeFadeHotMult ) ) );
        }
        
        return ( torque );
    }
    
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode ) {}
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode ) {}
    
    public void applyActualLifetime( LiveGameData gameData, double oldRaceLengthPercentage, double raceLengthPercentage )
    {
        final VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        final VehicleSetup setup = gameData.getSetup();
        
        if ( !setup.isUpdatedInTimeScope() )
            return;
        
        double engineLifetime1 = engine.getSafeLifetimeTotal( oldRaceLengthPercentage );
        double engineLifetimeP = engine.getSafeLifetimeTotal( raceLengthPercentage );
        double d = engineLifetime1 - this.engineLifetime;
        this.engineLifetime = engineLifetimeP - d;
        
        double factor = oldRaceLengthPercentage / raceLengthPercentage;
        
        double brakeDiscThicknessFL1 = setup.getWheelAndTire( Wheel.FRONT_LEFT ).getBrakeDiscThickness();
        d = brakeDiscThicknessFL1 - this.brakeDiscThicknessFL;
        this.brakeDiscThicknessFL = brakeDiscThicknessFL1 - d * factor;
        double brakeDiscThicknessFR1 = setup.getWheelAndTire( Wheel.FRONT_RIGHT ).getBrakeDiscThickness();
        d = brakeDiscThicknessFR1 - this.brakeDiscThicknessFR;
        this.brakeDiscThicknessFR = brakeDiscThicknessFR1 - d * factor;
        double brakeDiscThicknessRL1 = setup.getWheelAndTire( Wheel.REAR_LEFT ).getBrakeDiscThickness();
        d = brakeDiscThicknessRL1 - this.brakeDiscThicknessRL;
        this.brakeDiscThicknessRL = brakeDiscThicknessRL1 - d * factor;
        double brakeDiscThicknessRR1 = setup.getWheelAndTire( Wheel.REAR_RIGHT ).getBrakeDiscThickness();
        d = brakeDiscThicknessRR1 - this.brakeDiscThicknessRR;
        this.brakeDiscThicknessRR = brakeDiscThicknessRR1 - d * factor;
    }
    
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
        final double raceLengthPercentage = scoringInfo.getRaceLengthPercentage();
        
        if ( !setup.isUpdatedInTimeScope() )
            return;
        
        if ( ( scoringInfo.getSessionId() > lastSessionID ) || ( scoringInfo.getRealtimeEntredId() > lastEnteredRealtimeID ) )
        {
            lastSessionID = scoringInfo.getSessionId();
            lastEnteredRealtimeID = scoringInfo.getRealtimeEntredId();
            engineLifetime = engine.getSafeLifetimeTotal( raceLengthPercentage );
            
            brakeDiscThicknessFL = setup.getWheelAndTire( Wheel.FRONT_LEFT ).getBrakeDiscThickness();
            brakeDiscThicknessFR = setup.getWheelAndTire( Wheel.FRONT_RIGHT ).getBrakeDiscThickness();
            brakeDiscThicknessRL = setup.getWheelAndTire( Wheel.REAR_LEFT ).getBrakeDiscThickness();
            brakeDiscThicknessRR = setup.getWheelAndTire( Wheel.REAR_RIGHT ).getBrakeDiscThickness();
        }
        else
        {
            if ( !gameData.isGamePaused() )
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
                    
                    engineLifetime -= deltaTime * ( ( factorOilTemp + factorRPM ) / 2.0 ) * factorBoost * factorVelocity;
                }
                
                // brakes lifetime
                {
                    float currBrakeApplication = telemData.getUnfilteredBrake();
                    float avgBrakeApplication = ( lastBrakeApplication + currBrakeApplication ) / 2.0f;
                    float brakePressure = setup.getControls().getBrakePressure();
                    
                    //brake bias for that end * brake application * brake pressure * ((brakewear rate * temperature (Kelvin) ^ 3 ) / ( ( brake response curve value 2 + brake response curve value 3 + 271.15 *2 ) / 2 ) ^ 3)
                    
                    Wheel wheel = Wheel.FRONT_LEFT;
                    float lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundC();
                    float upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundC();
                    float wearRate = brakes.getBrake( wheel ).getWearRate();
                    /*
                    float torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureFL < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureFL ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureFL > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureFL - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    */
                    double torque = computeTorque( brakes.getBrake( wheel ), lastBrakeTemperatureFL );
                    double brakeWearFL = torque * ( 1.0f - lastBrakeBias ) * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationFL ) * ( wearRate * Math.pow( lastBrakeTemperatureFL, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( MeasurementUnits.Convert.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    wheel = Wheel.FRONT_RIGHT;
                    lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundC();
                    upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundC();
                    wearRate = brakes.getBrake( wheel ).getWearRate();
                    /*
                    torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureFR < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureFR ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureFR > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureFR - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    */
                    torque = computeTorque( brakes.getBrake( wheel ), lastBrakeTemperatureFR );
                    double brakeWearFR = torque * ( 1.0f - lastBrakeBias ) * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationFR ) * ( wearRate * Math.pow( lastBrakeTemperatureFR, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( MeasurementUnits.Convert.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    wheel = Wheel.REAR_LEFT;
                    lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundC();
                    upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundC();
                    wearRate = brakes.getBrake( wheel ).getWearRate();
                    /*
                    torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureRL < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureRL ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureRL > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureRL - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    */
                    torque = computeTorque( brakes.getBrake( wheel ), lastBrakeTemperatureRL );
                    double brakeWearRL = torque * lastBrakeBias * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationRL ) * ( wearRate * Math.pow( lastBrakeTemperatureRL, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( MeasurementUnits.Convert.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    wheel = Wheel.REAR_RIGHT;
                    lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBoundC();
                    upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBoundC();
                    wearRate = brakes.getBrake( wheel ).getWearRate();
                    /*
                    torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureRR < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureRR ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureRR > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureFL - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    */
                    torque = computeTorque( brakes.getBrake( wheel ), lastBrakeTemperatureRR );
                    double brakeWearRR = torque * lastBrakeBias * avgBrakeApplication * brakePressure * Math.abs( lastWheelRotationRR ) * ( wearRate * Math.pow( lastBrakeTemperatureRR, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( MeasurementUnits.Convert.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    brakeDiscThicknessFL -= deltaTime * brakeWearFL / raceLengthPercentage;
                    brakeDiscThicknessFR -= deltaTime * brakeWearFR / raceLengthPercentage;
                    brakeDiscThicknessRL -= deltaTime * brakeWearRL / raceLengthPercentage;
                    brakeDiscThicknessRR -= deltaTime * brakeWearRR / raceLengthPercentage;
                }
            }
        }
        
        telemData.engineLifetime = (float)engineLifetime;
        
        telemData.brakeDiscThicknessFL = (float)brakeDiscThicknessFL;
        telemData.brakeDiscThicknessFR = (float)brakeDiscThicknessFR;
        telemData.brakeDiscThicknessRL = (float)brakeDiscThicknessRL;
        telemData.brakeDiscThicknessRR = (float)brakeDiscThicknessRR;
        
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
