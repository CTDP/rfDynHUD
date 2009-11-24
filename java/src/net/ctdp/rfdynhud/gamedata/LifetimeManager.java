package net.ctdp.rfdynhud.gamedata;

class LifetimeManager implements TelemetryData.TelemetryDataUpdateListener
{
    public static final LifetimeManager INSTANCE = new LifetimeManager();
    
    private int lastEnteredRealtimeID = -1;
    private long lastTimestamp = -2L;
    
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
    
    /**
     * {@inheritDoc}
     */
    public void onTelemetryDataUpdated( LiveGameData gameData )
    {
        final VehiclePhysics.Engine engine = gameData.getPhysics().getEngine();
        final VehiclePhysics.Brakes brakes = gameData.getPhysics().getBrakes();
        final TelemetryData telemData = gameData.getTelemetryData();
        
        long timestamp = System.nanoTime();
        
        if ( gameData.getScoringInfo().getRealtimeEntredID() > lastEnteredRealtimeID )
        {
            lastEnteredRealtimeID = gameData.getScoringInfo().getRealtimeEntredID();
            engineLifetime = engine.getMinLifetime( gameData.getScoringInfo().getRaceLengthPercentage() );
            
            brakeDiscThicknessFL = gameData.getSetup().getWheelAndTire( Wheel.FRONT_LEFT ).getBrakeDiscThickness();
            brakeDiscThicknessFR = gameData.getSetup().getWheelAndTire( Wheel.FRONT_RIGHT ).getBrakeDiscThickness();
            brakeDiscThicknessRL = gameData.getSetup().getWheelAndTire( Wheel.REAR_LEFT ).getBrakeDiscThickness();
            brakeDiscThicknessRR = gameData.getSetup().getWheelAndTire( Wheel.REAR_RIGHT ).getBrakeDiscThickness();
        }
        else
        {
            double deltaTime = ( timestamp - lastTimestamp ) / 1000000000.0;
            
            if ( deltaTime < 0.15 ) // Game paused?
            {
                // engine lifetime
                //if ( telemData.getEngineRPM() > 1.00f )
                {
                    double factorOilTemp = Math.pow( 2.0, ( lastOilTemperature - engine.getBaseLifetimeOilTemperature() ) / engine.getHalfLifetimeOilTempOffset() );
                    
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
                    float brakePressure = gameData.getSetup().getControls().getBrakePressure();
                    
                    //brake bias for that end * brake application * brake pressure * ((brakewear rate * temperature (Kelvin) ^ 3 ) / ( ( brake response curve value 2 + brake response curve value 3 + 271.15 *2 ) / 2 ) ^ 3)
                    
                    Wheel wheel = Wheel.FRONT_LEFT;
                    float lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBound();
                    float upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBound();
                    float wearRate = brakes.getBrake( wheel ).getWearRate();
                    float torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureFL < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureFL ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureFL > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureFL - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    double brakeWearFL = torque * ( 1.0f - lastBrakeBias ) * avgBrakeApplication * brakePressure * -lastWheelRotationFL * ( wearRate * Math.pow( lastBrakeTemperatureFL, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( TelemetryData.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    wheel = Wheel.FRONT_RIGHT;
                    lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBound();
                    upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBound();
                    wearRate = brakes.getBrake( wheel ).getWearRate();
                    torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureFR < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureFR ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureFR > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureFR - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    double brakeWearFR = torque * ( 1.0f - lastBrakeBias ) * avgBrakeApplication * brakePressure * -lastWheelRotationFR * ( wearRate * Math.pow( lastBrakeTemperatureFR, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( TelemetryData.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    wheel = Wheel.REAR_LEFT;
                    lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBound();
                    upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBound();
                    wearRate = brakes.getBrake( wheel ).getWearRate();
                    torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureRL < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureRL ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureRL > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureRL - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    double brakeWearRL = torque * lastBrakeBias * avgBrakeApplication * brakePressure * -lastWheelRotationRL * ( wearRate * Math.pow( lastBrakeTemperatureRL, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( TelemetryData.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    wheel = Wheel.REAR_RIGHT;
                    lowerOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesLowerBound();
                    upperOptTemp = brakes.getBrake( wheel ).getOptimumTemperaturesUpperBound();
                    wearRate = brakes.getBrake( wheel ).getWearRate();
                    torque = brakes.getBrake( wheel ).getTorque();
                    if ( lastBrakeTemperatureRR < lowerOptTemp )
                        torque /= Math.pow( 2.0, ( lowerOptTemp - lastBrakeTemperatureRR ) / ( lowerOptTemp - brakes.getBrake( wheel ).getColdTemperature() ) );
                    else if ( lastBrakeTemperatureRR > upperOptTemp )
                        torque /= Math.pow( 2.0, ( lastBrakeTemperatureFL - upperOptTemp ) / ( brakes.getBrake( wheel ).getOverheatingTemperature() - upperOptTemp ) );
                    double brakeWearRR = torque * lastBrakeBias * avgBrakeApplication * brakePressure * -lastWheelRotationRR * ( wearRate * Math.pow( lastBrakeTemperatureRR, 3.0 ) ) / Math.pow( ( ( lowerOptTemp + upperOptTemp - ( TelemetryData.ZERO_KELVIN * 2.0f ) ) / 2.0 ), 3.0 );
                    
                    brakeDiscThicknessFL -= deltaTime * brakeWearFL / gameData.getScoringInfo().getRaceLengthPercentage();
                    brakeDiscThicknessFR -= deltaTime * brakeWearFR / gameData.getScoringInfo().getRaceLengthPercentage();
                    brakeDiscThicknessRL -= deltaTime * brakeWearRL / gameData.getScoringInfo().getRaceLengthPercentage();
                    brakeDiscThicknessRR -= deltaTime * brakeWearRR / gameData.getScoringInfo().getRaceLengthPercentage();
                }
            }
        }
        
        telemData.engineLifetime = (float)engineLifetime;
        
        telemData.brakeDiscThicknessFL = (float)brakeDiscThicknessFL;
        telemData.brakeDiscThicknessFR = (float)brakeDiscThicknessFR;
        telemData.brakeDiscThicknessRL = (float)brakeDiscThicknessRL;
        telemData.brakeDiscThicknessRR = (float)brakeDiscThicknessRR;
        
        lastOilTemperature = telemData.getEngineOilTemperature();
        lastEngineRevs = telemData.getEngineRPM();
        lastEngineBoost = telemData.getEffectiveEngineBoostMapping();
        lastVelocity = telemData.getScalarVelocity();
        
        lastBrakeBias = gameData.getSetup().getControls().getRearBrakeBalance();
        lastBrakeApplication = telemData.getUnfilteredBrake();
        lastBrakeTemperatureFL = telemData.getBrakeTemperatureKelvin( Wheel.FRONT_LEFT );
        lastBrakeTemperatureFR = telemData.getBrakeTemperatureKelvin( Wheel.FRONT_RIGHT );
        lastBrakeTemperatureRL = telemData.getBrakeTemperatureKelvin( Wheel.REAR_LEFT );
        lastBrakeTemperatureRR = telemData.getBrakeTemperatureKelvin( Wheel.REAR_RIGHT );
        lastWheelRotationFL = telemData.getWheelRotation( Wheel.FRONT_LEFT );
        lastWheelRotationFR = telemData.getWheelRotation( Wheel.FRONT_RIGHT );
        lastWheelRotationRL = telemData.getWheelRotation( Wheel.REAR_LEFT );
        lastWheelRotationRR = telemData.getWheelRotation( Wheel.REAR_RIGHT );
        
        lastTimestamp = timestamp;
    }
    
    LifetimeManager()
    {
    }
}
