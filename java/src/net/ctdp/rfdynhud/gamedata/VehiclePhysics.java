package net.ctdp.rfdynhud.gamedata;

import java.io.File;

import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.RFactorTools;


/**
 * This is a model of vehicle physics settings.
 * 
 * @author Marvin Froehlich
 */
public class VehiclePhysics
{
    /**
     * Abstraction of a usual physics setting (base_value, step_size, num_steps).
     * 
     * @author Marvin Froehlich
     */
    public static class PhysicsSetting
    {
        private final float baseOffset;
        private float baseValue = 0f;
        private float stepSize = 1f;
        private int numSteps = 1;
        
        void set( float baseValue, float stepSize, int numSteps )
        {
            this.baseValue = baseOffset + baseValue;
            this.stepSize = stepSize;
            this.numSteps = numSteps;
        }
        
        public final float getBaseValue()
        {
            return ( baseValue );
        }
        
        public final float getStepSize()
        {
            return ( stepSize );
        }
        
        public final int getNumSteps()
        {
            return ( numSteps );
        }
        
        public final float getMinValue()
        {
            if ( stepSize > 0f )
                return ( baseValue );
            
            return ( baseValue + ( numSteps - 1 ) * stepSize );
        }
        
        public final float getMaxValue()
        {
            if ( stepSize < 0f )
                return ( baseValue );
            
            return ( baseValue + ( numSteps - 1 ) * stepSize );
        }
        
        public final float getValueForSetting( int setting )
        {
            // There shuold be a range check. But since this cannot be used for cheating, it isn't necessary.
            
            return ( baseValue + stepSize * setting );
        }
        
        PhysicsSetting( float baseOffset )
        {
            this.baseOffset = baseOffset;
        }
        
        PhysicsSetting()
        {
            this( 0f );
        }
    }
    
    private final PhysicsSetting fuelRange = new PhysicsSetting();
    private float weightOfOneLiter = 0.742f; // weight of one liter of fuel in kg
    private final PhysicsSetting frontWingRange = new PhysicsSetting();
    
    /**
     * Gets the phyiscs setting for fule range.
     * 
     * @return the phyiscs setting for fule range.
     */
    public final PhysicsSetting getFuelRange()
    {
        return ( fuelRange );
    }
    
    /**
     * Gets the weight of one liter of fuel in kg.
     * 
     * @return the weight of one liter of fuel in kg.
     */
    public final float getWeightOfOneLiterOfFuel()
    {
        return ( weightOfOneLiter );
    }
    
    /**
     * Gets the front wing range of settings.
     * 
     * @return the front wing range of settings.
     */
    public final PhysicsSetting getFrontWingRange()
    {
        return ( frontWingRange );
    }
    
    /**
     * Abstraction of possible Wheel drive settings.
     * 
     * @author Marvin Froehlich
     */
    public static enum WheelDrive
    {
        FRONT,
        REAR,
        FOUR,
        ;
        
        public final boolean includesFront()
        {
            return ( ( this == FRONT ) || ( this == FOUR ) );
        }
        
        public final boolean includesRear()
        {
            return ( ( this == REAR ) || ( this == FOUR ) );
        }
    }
    
    WheelDrive wheelDrive;
    
    /**
     * Gets the vehicle's {@link WheelDrive}.
     * 
     * @return the vehicle's {@link WheelDrive}
     */
    public final WheelDrive getWheelDrive()
    {
        return ( wheelDrive );
    }
    
    /**
     * Model of engine physics parameters.
     * 
     * @author Marvin Froehlich
     */
    public static class Engine
    {
        String name = "N/A";
        float lifetimeAverage;
        float lifetimeVariance;
        float baseLifetimeOilTemperature;
        float halfLifetimeOilTempOffset;
        float optimumOilTemperature;
        float wearIncreasePerDegree;
        float baseLifetimeRPM;
        float halfLifetimeRPMOffset;
        private final PhysicsSetting boostRange = new PhysicsSetting( 1f );
        float rpmIncreasePerBoostSetting;
        float fuelUsageIncreasePerBoostSetting;
        float wearIncreasePerBoostSetting;
        float wearIncreasePerVelocity;
        
        /**
         * Gets the engine's name.
         * 
         * @return the engine's name.
         */
        public final String getName()
        {
            return ( name );
        }
        
        public final float getLifetimeAverage( double raceLengthMultiplier )
        {
            return ( (float)( lifetimeAverage * raceLengthMultiplier ) );
        }
        
        public final float getLifetimeVariance( double raceLengthMultiplier )
        {
            return ( (float)( lifetimeVariance * raceLengthMultiplier ) );
        }
        
        public final boolean hasLifetimeVariance()
        {
            return ( ( lifetimeVariance < -0.02f ) || ( lifetimeVariance > +0.02f ) );
        }
        
        public final float getMinLifetime( double raceLengthMultiplier )
        {
            return ( (float)( ( lifetimeAverage - lifetimeVariance - lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        public final float getRedLifetime( double raceLengthMultiplier )
        {
            return ( (float)( ( lifetimeAverage + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        public final float getMaxLifetime( double raceLengthMultiplier )
        {
            return ( (float)( ( lifetimeAverage + lifetimeVariance + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        public final float getLifetimeVarianceRange( double raceLengthMultiplier )
        {
            return ( (float)( ( lifetimeVariance + lifetimeVariance + lifetimeVariance + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        public final float getLifetimeVarianceHalfRange( double raceLengthMultiplier )
        {
            return ( (float)( ( lifetimeVariance + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        public final float getBaseLifetimeOilTemperature()
        {
            return ( baseLifetimeOilTemperature );
        }
        
        public final float getHalfLifetimeOilTempOffset()
        {
            return ( halfLifetimeOilTempOffset );
        }
        
        /**
         * Gets the optimum oil temperature. Engine will operatate optimally at this value.
         * 
         * @return the optimum oil temperature.
         */
        public final float getOptimumOilTemperature()
        {
            return ( optimumOilTemperature );
        }
        
        /**
         * Gets the temperature value at which the engine starts to overheat.
         * This value should serve as a peak level for temperatures during a race.
         * 
         * @return the temperature value at which the engine starts to overheat.
         */
        public final float getOverheatingOilTemperature()
        {
            return ( baseLifetimeOilTemperature );
        }
        
        /**
         * Gets a strong overheating engine temperature. At this level the engine will have half of its regular life time.
         * 
         * @return a strong overheating engine temperature.
         */
        public final float getStrongOverheatingOilTemperature()
        {
            return ( baseLifetimeOilTemperature + halfLifetimeOilTempOffset );
        }
        
        public final float getWearIncreasePerDegree()
        {
            return ( wearIncreasePerDegree );
        }
        
        /**
         * Gets RPM for 'normal' lifetime. No decreased and no increased lifetime.
         * 
         * @return RPM for 'normal' lifetime. No decreased and no increased lifetime.
         */
        public final float getBaseLifetimeRPM()
        {
            return ( baseLifetimeRPM );
        }
        
        public final float getHalfLifetimeRPMOffset()
        {
            return ( halfLifetimeRPMOffset );
        }
        
        /**
         * Gets the range of possible boost mappings.
         * 
         * @return the range of possible boost mappings.
         */
        public final PhysicsSetting getBoostRange()
        {
            return ( boostRange );
        }
        
        public final float getRPMIncreasePerBoostLevel()
        {
            return ( rpmIncreasePerBoostSetting );
        }
        
        public final float getFuelUsageIncreasePerBoostLevel()
        {
            return ( fuelUsageIncreasePerBoostSetting );
        }
        
        public final float getWearIncreasePerBoostLevel()
        {
            return ( wearIncreasePerBoostSetting );
        }
        
        public final float getWearIncreasePerVelocity()
        {
            return ( wearIncreasePerVelocity );
        }
        
        /**
         * Gets the maximum RPM at the given boost level.
         * 
         * @param baseMaxRPM maxRPM coming from {@link TelemetryData#getEngineMaxRPM()}
         * @param boostLevel coming from {@link TelemetryData#getEngineBoostMapping()}
         * 
         * @return the maximum RPM at the given boost level.
         */
        public final float getMaxRPM( float baseMaxRPM, int boostLevel )
        {
            /*
            if ( rpmIncreasePerBoostSetting <= 0f )
                return ( baseMaxRPM );
            
            return ( baseMaxRPM + ( boostRange.getValueForSetting( boostLevel ) - boostRange.getBaseValue() ) * rpmIncreasePerBoostSetting );
            */
            return ( baseMaxRPM + ( boostLevel - boostRange.getBaseValue() ) * rpmIncreasePerBoostSetting );
        }
        
        /**
         * Gets the maximum RPM at the highest (valued) boost mapping.
         * 
         * @param baseMaxRPM maxRPM coming from {@link TelemetryData#getEngineMaxRPM()}
         * 
         * @return the maximum RPM at the highest (valued) boost mapping.
         */
        public final float getMaxRPM( float baseMaxRPM )
        {
            if ( rpmIncreasePerBoostSetting <= 0f )
                return ( baseMaxRPM );
            
            return ( baseMaxRPM + ( boostRange.getMaxValue() - boostRange.getBaseValue() ) * rpmIncreasePerBoostSetting );
        }
        
        Engine()
        {
        }
    }
    
    private final Engine engine = new Engine();
    
    /**
     * Get engine related physics parameters.
     * 
     * @return engine related physics parameters.
     */
    public final Engine getEngine()
    {
        return ( engine );
    }
    
    /**
     * Model of brake physics parameters.
     * 
     * @author Marvin Froehlich
     */
    public static class Brakes
    {
        private final PhysicsSetting balance = new PhysicsSetting();
        
        /**
         * Gets the range of possible brake distribution values.
         * 
         * @return the range of possible brake distribution values.
         */
        public final PhysicsSetting getRearDistributionRange()
        {
            return ( balance );
        }
        
        private final PhysicsSetting pressureRange = new PhysicsSetting();
        
        /**
         * Gets the range for brake pressure values [0, 1].
         * 
         * @return the range for brake pressure values [0, 1].
         */
        public final PhysicsSetting getPressureRange()
        {
            return ( pressureRange );
        }
        
        public class WheelBrake
        {
            public static final float DEFAULT_BRAKE_FADE_RANGE = Float.MAX_VALUE / 2f;
            
            private float optimumTemperaturesLowerBound;
            private float optimumTemperaturesUpperBound;
            private float coldTemperature;
            private float overheatingTemperature;
            private float wearincreasePerDegreeOverOptimum;
            private float weardecreasePerDegreeBelowOptimum;
            float brakeFadeRange = DEFAULT_BRAKE_FADE_RANGE;
            
            private final PhysicsSetting discRange = new PhysicsSetting();
            float wearRate;
            float discFailureAverage;
            float discFailureVariance;
            float torque;
            
            /**
             * Gets the lower bound of the temperature range, where brakes will operate optimally.
             * 
             * @return the lower bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesLowerBound()
            {
                return ( optimumTemperaturesLowerBound );
            }
            
            /**
             * Gets the upper bound of the temperature range, where brakes will operate optimally.
             * 
             * @return the upper bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesUpperBound()
            {
                return ( optimumTemperaturesUpperBound );
            }
            
            /**
             * Gets the temperature level under and at which brakes are cold and won't work well.
             * 
             * @return the temperature level under and at which brakes are cold.
             */
            public final float getColdTemperature()
            {
                return ( coldTemperature );
            }
            
            /**
             * Gets the temperature level above at at which brakes are overheating and won't work well and increase more than regularly.
             * 
             * @return the temperature level above at at which brakes are overheating.
             */
            public final float getOverheatingTemperature()
            {
                return ( overheatingTemperature );
            }
            
            public final float getWearincreasePerDegreeOverOptimum()
            {
                return ( wearincreasePerDegreeOverOptimum );
            }
            
            public final float getWeardecreasePerDegreeBelowOptimum()
            {
                return ( weardecreasePerDegreeBelowOptimum );
            }
    		
            void setTemperatures( float coldTemperature, float optimumTemperaturesLowerBound, float optimumTemperaturesUpperBound, float overheatingTemperature )
            {
                this.coldTemperature = coldTemperature;
                this.optimumTemperaturesLowerBound = optimumTemperaturesLowerBound;
                this.optimumTemperaturesUpperBound = optimumTemperaturesUpperBound;
                this.overheatingTemperature = overheatingTemperature;
            }
            
            public final float getBrakeFadeRange()
            {
                return ( brakeFadeRange );
            }
            
            public final float getBrakeFadeColdTemperature()
            {
                return ( optimumTemperaturesLowerBound - brakeFadeRange );
            }
            
            public final float getBrakeFadeHotTemperature()
            {
                return ( optimumTemperaturesUpperBound + brakeFadeRange );
            }
            
            /**
             * Gets the disc thickness range in meters.
             * 
             * @return the disc thickness range in meters.
             */
            public final PhysicsSetting getDiscRange()
            {
                return ( discRange );
            }
            
            /**
             * Brake disc wear per second at optimum temperature.
             * 
             * @return Brake disc wear per second at optimum temperature
             */
            public final float getWearRate()
            {
                return ( wearRate );
            }
            
            /**
             * Gets the disc thickness at which it fails.
             * 
             * @return the disc thickness at which it fails.
             */
            public final float getDiscFailureAverage()
            {
                return ( discFailureAverage );
            }
            
            /**
             * Gets the disc thickness at which it fails.
             * 
             * @return the disc thickness at which it fails.
             */
            public final float getDiscFailureVariance()
            {
                return ( discFailureVariance );
            }
            
            public final boolean hasDiscFailureVariance()
            {
                return ( ( discFailureVariance < -0.0000001f ) || ( discFailureVariance > +0.0000001f ) );
            }
            
            /**
             * Gets the disc thickness at which it fails.
             * 
             * @return the disc thickness at which it fails.
             */
            public final float getMinDiscFailure()
            {
                return ( discFailureAverage - discFailureVariance - discFailureVariance );
                //return ( discFailureAverage );
            }
            
            /**
             * Gets the disc thickness at which it fails.
             * 
             * @return the disc thickness at which it fails.
             */
            public final float getRedDiscFailure()
            {
                return ( discFailureAverage + discFailureVariance );
            }
            
            /**
             * Gets the disc thickness at which it fails.
             * 
             * @return the disc thickness at which it fails.
             */
            public final float getMaxDiscFailure()
            {
                return ( discFailureAverage + discFailureVariance + discFailureVariance );
            }
            
            public final float getDiscFailureVarianceRange()
            {
                return ( discFailureVariance + discFailureVariance + discFailureVariance + discFailureVariance );
            }
            
            public final float getDiscFailureVarianceHalfRange()
            {
                return ( discFailureVariance + discFailureVariance );
            }
            
            /**
             * Gets brake torque.
             * 
             * @return brake torque.
             */
            public final float getTorque()
            {
                return ( torque );
            }
            
    		WheelBrake()
    		{
    		}
        }
        
        private final WheelBrake brakeFrontLeft = new WheelBrake();
        private final WheelBrake brakeFrontRight = new WheelBrake();
        private final WheelBrake brakeRearLeft = new WheelBrake();
        private final WheelBrake brakeRearRight = new WheelBrake();
        
        /**
         * Gets the brake model of the given wheel.
         * 
         * @param wheel
         * 
         * @return the brake model of the given wheel.
         */
        public final WheelBrake getBrake( Wheel wheel )
        {
            switch ( wheel )
            {
                case FRONT_LEFT:
                    return ( brakeFrontLeft );
                case FRONT_RIGHT:
                    return ( brakeFrontRight );
                case REAR_LEFT:
                    return ( brakeRearLeft );
                case REAR_RIGHT:
                    return ( brakeRearRight );
            }
            
            // Unreachable code!
            return ( null );
        }
    }
    
    private final Brakes brakes = new Brakes();
    
    public final Brakes getBrakes()
    {
        return ( brakes );
    }
    
    /**
     * Model of a tire slip curve.
     * 
     * @author Marvin Froehlich
     */
    public static final class SlipCurve
    {
        String name = null;
        float step = -1f;
        float dropoffFunction = -1f;
        float[] data = new float[ 512 ];
        int dataLength = 0;
        
        /**
         * Gets the slip curve's name.
         * 
         * @return the slip curve's name.
         */
        public final String getName()
        {
            return ( name );
        }
        
        public final float getStep()
        {
            return ( step );
        }
        
        public final float getDropoffFunction()
        {
            return ( dropoffFunction );
        }
        
        public final float[] getData()
        {
            return ( data );
        }
        
        public final int getDataLength()
        {
            return ( dataLength );
        }
        
        SlipCurve()
        {
        }
    }
    
    File usedTBCFile;
    
    public final File getUsedTBCFile()
    {
        return ( usedTBCFile );
    }
    
    /**
     * Model of a tire compound.
     * 
     * @author Marvin Froehlich
     */
    public static class TireCompound
    {
        String name = "N/A";
        int index = -1;
        SlipCurve frontLatitudeSlipCurve = null;
        SlipCurve frontBrakingSlipCurve = null;
        SlipCurve frontTractiveSlipCurve = null;
        SlipCurve rearLatitudeSlipCurve = null;
        SlipCurve rearBrakingSlipCurve = null;
        SlipCurve rearTractiveSlipCurve = null;
        
        /**
         * Gets the compound's name.
         * 
         * @return the compound's name.
         */
        public final String getName()
        {
            return ( name );
        }
        
        /**
         * Gets the compound's index in the list. This is what stands in the setup file.
         * 
         * @return the compound's index in the list.
         */
        public final int getIndex()
        {
            return ( index );
        }
        
        public final SlipCurve getFrontLatitudeSlipCurve()
        {
            return ( frontLatitudeSlipCurve );
        }
        
        public final SlipCurve getFrontBrakingSlipCurve()
        {
            return ( frontBrakingSlipCurve );
        }
        
        public final SlipCurve getFrontTractiveSlipCurve()
        {
            return ( frontTractiveSlipCurve );
        }
        
        public final SlipCurve getRearLatitudeSlipCurve()
        {
            return ( rearLatitudeSlipCurve );
        }
        
        public final SlipCurve getRearBrakingSlipCurve()
        {
            return ( rearBrakingSlipCurve );
        }
        
        public final SlipCurve getRearTractiveSlipCurve()
        {
            return ( rearTractiveSlipCurve );
        }
        
        /**
         * Model of one wheel of a compound. There will always be one {@link CompoundWheel} in a {@link TireCompound} for each wheel of the vehicle.
         * 
         * @author Marvin Froehlich
         */
        public class CompoundWheel
        {
            private float dryLateralGrip;
            private float dryLongitudinalGrip;
            private float optimumTemperature;
            private float gripLossPerDegreeBelowOptimum;
            private float gripLossPerDegreeAboveOptimum;
            private float optPress;
            private float optPressMult;
            private float offPressure;
            float[] gripFactorPerWear;
            
            void setDryGrip( float laterial, float longitudinal )
            {
                this.dryLateralGrip = laterial;
                this.dryLongitudinalGrip = longitudinal;
            }
            
            /**
             * Gets the lateral grip value for dry weather.
             * Effective grip will always be a fraction of this value depending on tire wear, temperatures, pressure and load.
             * 
             * @return the lateral grip value for dry weather.
             */
            public final float getDryLateralGrip()
            {
                return ( dryLateralGrip );
            }
            
            /**
             * Gets the longitudinal grip value for dry weather.
             * Effective grip will always be a fraction of this value depending on tire wear, temperatures, pressure and load.
             * 
             * @return the longitudinal grip value for dry weather.
             */
            public final float getDryLongitudinalGrip()
            {
                return ( dryLongitudinalGrip );
            }
            
            void setOptimumTemperature( float optTemp )
            {
                this.optimumTemperature = optTemp;
            }
            
            /**
             * Gets the temperature value (in celsius), at which the tire will operate optimally.
             * 
             * @return the temperature value (in celsius), at which the tire will operate optimally.
             */
            public final float getOptimumTemperature()
            {
                return ( optimumTemperature );
            }
            
            /**
             * @param belowTemp TBC "GripTempPress" value 1
             * @param aboveTemp TBC "GripTempPress" value 2
             * @param offPress TBC "GripTempPress" value 3
             */
            void setAboveAndBelowTempsAndPressures( float belowTemp, float aboveTemp, float offPress )
            {
                float recipOptimumTemperature = ( optimumTemperature != 0.0f ) ? ( 1.0f / optimumTemperature ) : 0.0f;
                
                this.gripLossPerDegreeBelowOptimum = belowTemp * recipOptimumTemperature;
                this.gripLossPerDegreeAboveOptimum = aboveTemp * recipOptimumTemperature;
                
                this.offPressure = offPress;
            }
            
            /**
             * Gets the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             * 
             * @return the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             */
            public final float getGripLossPerDegreeBelowOptimum()
            {
                return ( gripLossPerDegreeBelowOptimum );
            }
            
            /**
             * Gets the grip loss (fraction) per degree above {@link #getOptimumTemperature()}.
             * 
             * @return the grip loss (fraction) per degree above {@link #getOptimumTemperature()}.
             */
            public final float getGripLossPerDegreeAboveOptimum()
            {
                return ( gripLossPerDegreeAboveOptimum );
            }
            
            /**
             * Gets the temperature, that a tire will have at the given grip fraction value.
             * This function will always return a value below {@link #getOptimumTemperature()}.
             * 
             * @param grip
             * 
             * @return the temperature, that a tire will have at thegiven grip fraction value.
             */
            public final float getBelowTemperature( float grip )
            {
                return ( optimumTemperature - ( grip / gripLossPerDegreeBelowOptimum ) );
            }
            
            /**
             * Gets the temperature, that a tire will have at the given grip fraction value.
             * This function will always return a value above {@link #getOptimumTemperature()}.
             * 
             * @param grip
             * 
             * @return the temperature, that a tire will have at thegiven grip fraction value.
             */
            public final float getAboveTemperature( float grip )
            {
                return ( optimumTemperature + ( grip / gripLossPerDegreeAboveOptimum ) );
            }
            
            /**
             * Gets the grip fraction value of the tire at the given average temperature.
             * 
             * @param avgTemperature average over outer, mittle and inner temperature
             * 
             * @return the grip fraction value of the tire at the given average temperature.
             */
            public final float getGripFactorByTemperature( float avgTemperature )
            {
                float diffTemp = avgTemperature - optimumTemperature;
                
                return ( ( diffTemp < 0.0f ) ? ( gripLossPerDegreeBelowOptimum * -diffTemp ) : ( gripLossPerDegreeAboveOptimum * diffTemp ) );
            }
            
            /**
             * @param optPress TBC "OptimuPressure" field 1
             * @param mult TBC "OptimuPressure" field 2
             */
            void setOptimumPressure( float optPress, float mult )
            {
                this.optPress = optPress;
                this.optPressMult = mult;
            }
            
            /**
             * Gets the optimum tire pressure at the given tire load.
             * 
             * @param load coming from {@link TelemetryData#getTireLoad(Wheel)}
             * 
             * @return the optimum tire pressure at the given tire load.
             */
            public final float getOptimumPressure( float load )
            {
                return ( optPress + ( optPressMult * load ) );
            }
            
            /**
             * Computes the optimum tire pressure for the given grip fraction and load.
             * 
             * @param grip
             * @param load coming from {@link TelemetryData#getTireLoad(Wheel)}
             * 
             * @return the optimum tire pressure for the given grip fraction and load.
             */
            public final float getPressureForGrip( float grip, float load )
            {
                if ( grip <= 0.0f )
                    return ( 0.0f );
                
                float optPressLoad = getOptimumPressure( load );
                float recipOptPress = ( optPressLoad != 0.0f ) ? ( 1.0f / optPressLoad ) : 0.0f;
                
                float offPressureGrip = offPressure * recipOptPress;
                
                return ( Math.abs( optPressLoad + grip / offPressureGrip ) );
            }
            
            /**
             * Computes the grip fraction of the tire at the given pressure and load.
             * 
             * @param pressure coming from {@link TelemetryData#getTirePressure(Wheel)}
             * @param load coming from {@link TelemetryData#getTireLoad(Wheel)}
             * 
             * @return the grip fraction of the tire at the given pressure and load.
             */
            public final float getGripFactorByPressure( float pressure, float load )
            {
                if ( optPress <= 0.0f )
                    return ( 0.0f );
                
                float optPressLoad = getOptimumPressure( load );
                float recipOptPress = ( optPressLoad != 0.0f ) ? ( 1.0f / optPressLoad ) : 0.0f;
                
                float offPressureGrip = offPressure * recipOptPress;
                
                return ( offPressureGrip * Math.abs( pressure - optPressLoad ) );
            }
            
            /**
             * Computes the fraction of maximum grip at the given wear, average temperature, pressure and load.
             * 
             * @param wear see {@link TelemetryData#getTireWear(Wheel)} and {@link #getWearGripFactor(float)}
             * @param avgTemperature average over outer, mittle and inner temperature
             * @param pressure coming from {@link TelemetryData#getTirePressure(Wheel)}
             * @param load coming from {@link TelemetryData#getTireLoad(Wheel)}
             * 
             * @return the fraction of maximum grip at the given wear, average temperature, pressure and load.
             */
            public final float getGripFraction( float wear, float avgTemperature, float pressure, float load )
            {
                if ( optPress <= 0.0f )
                    return ( 0.0f );
                
                float gfTemp = getGripFactorByTemperature( avgTemperature );
                float gfPress = getGripFactorByPressure( pressure, load );
                float gfTotal = Math.min( gfTemp + gfPress, 1.0f );
                
                return ( getWearGripFactor( wear ) * ( 1.0f - ( 0.5f * gfTotal * gfTotal ) ) );
            }
            
            private static final float WEAR0  = 0.00f;
            private static final float WEAR1  = 0.06f;
            private static final float WEAR2  = 0.13f;
            private static final float WEAR3  = 0.19f;
            private static final float WEAR4  = 0.25f;
            private static final float WEAR5  = 0.31f;
            private static final float WEAR6  = 0.38f;
            private static final float WEAR7  = 0.44f;
            private static final float WEAR8  = 0.50f;
            private static final float WEAR9  = 0.56f;
            private static final float WEAR10 = 0.63f;
            private static final float WEAR11 = 0.69f;
            private static final float WEAR12 = 0.75f;
            private static final float WEAR13 = 0.81f;
            private static final float WEAR14 = 0.88f;
            private static final float WEAR15 = 0.94f;
            private static final float WEAR16 = 1.00f;
            
            /**
             * Selects the grip fraction at the given wear level.
             * 
             * @param wear coming from {@link TelemetryData#getTireWear(Wheel)}
             * 
             * @return the grip fraction at the given wear level.
             */
            public final float getWearGripFactor( float wear )
            {
                if ( gripFactorPerWear == null )
                    return ( 1.0f );
                
                final float[] w = gripFactorPerWear;
                
                // w[0] will always be set to 1.0.
                
                // WearGrip1=(0.989,0.981,0.9745,0.9715,0.969,0.967,0.9655,0.9645) // Grip at 6/13/19/25/31/38/44/50% wear (defaults to 0.980->0.844), grip is 1.0 at 0% wear
                // WearGrip2=(0.964,0.9638,0.963,0.961,0.9535,0.936,0.850,0.775) // Grip at 56/63/69/75/81/88/94/100% wear (defaults to 0.824->0.688), tire bursts at 100% wear
                
                wear = 1.0f - wear;
                
                if ( wear <= WEAR1 )
                    return ( w[0] - ( w[0] - w[1] ) * ( wear - WEAR0 ) / ( WEAR1 - WEAR0 ) );
                
                if ( wear <= WEAR2 )
                    return ( w[1] - ( w[1] - w[2] ) * ( wear - WEAR1 ) / ( WEAR2 - WEAR1 ) );
                
                if ( wear <= WEAR3 )
                    return ( w[2] - ( w[2] - w[3] ) * ( wear - WEAR2 ) / ( WEAR3 - WEAR2 ) );
                
                if ( wear <= WEAR4 )
                    return ( w[3] - ( w[3] - w[4] ) * ( wear - WEAR3 ) / ( WEAR4 - WEAR3 ) );
                
                if ( wear <= WEAR5 )
                    return ( w[4] - ( w[4] - w[5] ) * ( wear - WEAR4 ) / ( WEAR5 - WEAR4 ) );
                
                if ( wear <= WEAR6 )
                    return ( w[5] - ( w[5] - w[6] ) * ( wear - WEAR5 ) / ( WEAR6 - WEAR5 ) );
                
                if ( wear <= WEAR7 )
                    return ( w[6] - ( w[6] - w[7] ) * ( wear - WEAR6 ) / ( WEAR7 - WEAR6 ) );
                
                if ( wear <= WEAR8 )
                    return ( w[7] - ( w[7] - w[8] ) * ( wear - WEAR7 ) / ( WEAR8 - WEAR7 ) );
                
                if ( wear <= WEAR9 )
                    return ( w[8] - ( w[8] - w[9] ) * ( wear - WEAR8 ) / ( WEAR9 - WEAR8 ) );
                
                if ( wear <= WEAR10 )
                    return ( w[9] - ( w[9] - w[10] ) * ( wear - WEAR9 ) / ( WEAR10 - WEAR9 ) );
                
                if ( wear <= WEAR11 )
                    return ( w[10] - ( w[10] - w[11] ) * ( wear - WEAR10 ) / ( WEAR11 - WEAR10 ) );
                
                if ( wear <= WEAR12 )
                    return ( w[11] - ( w[11] - w[12] ) * ( wear - WEAR11 ) / ( WEAR12 - WEAR11 ) );
                
                if ( wear <= WEAR13 )
                    return ( w[12] - ( w[12] - w[13] ) * ( wear - WEAR12 ) / ( WEAR13 - WEAR12 ) );
                
                if ( wear <= WEAR14 )
                    return ( w[13] - ( w[13] - w[14] ) * ( wear - WEAR13 ) / ( WEAR14 - WEAR13 ) );
                
                if ( wear <= WEAR15 )
                    return ( w[14] - ( w[14] - w[15] ) * ( wear - WEAR14 ) / ( WEAR15 - WEAR14 ) );
                
                if ( wear < WEAR16 )
                    return ( w[15] - ( w[15] - w[16] ) * ( wear - WEAR15 ) / ( WEAR16 - WEAR15 ) );
                
                return ( 0.0f );
            }
            
            public final float getMinGrip()
            {
                if ( gripFactorPerWear == null )
                    return ( 1.0f );
                
                return ( gripFactorPerWear[gripFactorPerWear.length - 1] );
            }
            
            CompoundWheel()
            {
            }
        }
        
        private final CompoundWheel frontLeft = new CompoundWheel();
        private final CompoundWheel frontRight = new CompoundWheel();
        private final CompoundWheel rearLeft = new CompoundWheel();
        private final CompoundWheel rearRight = new CompoundWheel();
        
        /**
         * Gets the {@link CompoundWheel} for the given wheel.
         * 
         * @param wheel
         * 
         * @return the {@link CompoundWheel} for the given wheel.
         */
        public final CompoundWheel getWheel( Wheel wheel )
        {
            switch ( wheel )
            {
                case FRONT_LEFT:
                    return ( frontLeft );
                case FRONT_RIGHT:
                    return ( frontRight );
                case REAR_LEFT:
                    return ( rearLeft );
                case REAR_RIGHT:
                    return ( rearRight );
            }
            
            // Unreachable code!
            return ( null );
        }
        
        void setOptimumTempForAll4( float optimumTemp )
        {
            frontLeft.optimumTemperature = optimumTemp;
            frontRight.optimumTemperature = optimumTemp;
            rearLeft.optimumTemperature = optimumTemp;
            rearRight.optimumTemperature = optimumTemp;
        }
        
        TireCompound()
        {
        }
    }
    
    private TireCompound[] tireCompounds;
    private TireCompound tcBestGripFrontLeft = null;
    private TireCompound tcBestGripFrontRight = null;
    private TireCompound tcBestGripRearLeft = null;
    private TireCompound tcBestGripRearRight = null;
    
    void setTireCompounds( TireCompound[] tireCompounds )
    {
        this.tireCompounds = tireCompounds;
        
        this.tcBestGripFrontLeft = null;
        this.tcBestGripFrontRight = null;
        this.tcBestGripRearLeft = null;
        this.tcBestGripRearRight = null;
    }
    
    /**
     * Gets the number of available {@link TireCompound}s.
     * 
     * @return the number of available {@link TireCompound}s.
     */
    public final int getNumTireCompounds()
    {
        if ( tireCompounds == null )
            return ( 0 );
        
        return ( tireCompounds.length );
    }
    
    /**
     * Gets the {@link TireCompound} by the given index.
     * 
     * @param index zero-based
     * 
     * @return the {@link TireCompound} by the given index.
     */
    public final TireCompound getTireCompound( int index )
    {
        if ( index >= tireCompounds.length )
        {
            Logger.log( "WARNING: Unknown tire compound index " + index + ". Using closest one." );
            index = tireCompounds.length - 1;
        }
        
        return ( tireCompounds[index] );
    }
    
    /**
     * Gets the {@link TireCompound} for the given wheel, that has the best grip compared to allother available ones.
     * 
     * @param wheel
     * 
     * @return the {@link TireCompound} for the given wheel, that has the best grip.
     */
    public final TireCompound getTireCompoundBestGrip( Wheel wheel )
    {
        if ( tireCompounds == null )
            return ( null );
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                if ( tcBestGripFrontLeft != null )
                    return ( tcBestGripFrontLeft );
                break;
            case FRONT_RIGHT:
                if ( tcBestGripFrontRight != null )
                    return ( tcBestGripFrontRight );
                break;
            case REAR_LEFT:
                if ( tcBestGripRearLeft != null )
                    return ( tcBestGripRearLeft );
                break;
            case REAR_RIGHT:
                if ( tcBestGripRearRight != null )
                    return ( tcBestGripRearRight );
                break;
        }
        
        float maxGrip = 0.0f;
        TireCompound maxGripCompound = null;
        
        for ( int i = 0; i < tireCompounds.length; i++ )
        {
            TireCompound.CompoundWheel tcw = tireCompounds[i].getWheel( wheel );
            float grip = ( tcw.getDryLateralGrip() + tcw.getDryLongitudinalGrip() ) / 2.0f;
            
            if ( grip > maxGrip )
            {
                maxGrip = grip;
                maxGripCompound = tireCompounds[i];
            }
        }
        
        switch ( wheel )
        {
            case FRONT_LEFT:
                this.tcBestGripFrontLeft = maxGripCompound;
                return ( tcBestGripFrontLeft );
            case FRONT_RIGHT:
                this.tcBestGripFrontRight = maxGripCompound;
                return ( tcBestGripFrontRight );
            case REAR_LEFT:
                this.tcBestGripRearLeft = maxGripCompound;
                return ( tcBestGripRearLeft );
            case REAR_RIGHT:
                this.tcBestGripRearRight = maxGripCompound;
                return ( tcBestGripRearRight );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    private final PhysicsSetting tirePressureRangeFL = new PhysicsSetting();
    private final PhysicsSetting tirePressureRangeFR = new PhysicsSetting();
    private final PhysicsSetting tirePressureRangeRL = new PhysicsSetting();
    private final PhysicsSetting tirePressureRangeRR = new PhysicsSetting();
    
    /**
     * Gets the range of possible values for tire pressure.
     */
    public final PhysicsSetting getTirePressureRange( Wheel wheel )
    {
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( tirePressureRangeFL );
            case FRONT_RIGHT:
                return ( tirePressureRangeFR );
            case REAR_LEFT:
                return ( tirePressureRangeRL );
            case REAR_RIGHT:
                return ( tirePressureRangeRR );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    public static class UpgradeIdentifier
    {
        private final String upgradeType;
        private final String upgradeLevel;
        private final String description;
        
        public final String getUpgradeType()
        {
            return ( upgradeType );
        }
        
        public final String getUpgradeLevel()
        {
            return ( upgradeLevel );
        }
        
        public final String getDescription()
        {
            return ( description );
        }
        
        UpgradeIdentifier( String upgradeType, String upgradeLevel, String description )
        {
            this.upgradeType = upgradeType;
            this.upgradeLevel = upgradeLevel;
            this.description = description;
        }
    }
    
    UpgradeIdentifier[] installedUpgrades = null;
    
    /**
     * Gets the list of installed upgrades.
     * 
     * @return the list of installed upgrades or <code>null</code> if no upgrades are installed.
     */
    public final UpgradeIdentifier[] getInstalledUpgrades()
    {
        return ( installedUpgrades );
    }
    
    private static TireCompound createCompound( String name, int index, float optTemp )
    {
        float optPress = 64.5f;
        float optPressMult = 0.018841f;
        
        TireCompound tc = new TireCompound();
        
        tc.name = name;
        tc.index = index;
        tc.frontLeft.setDryGrip( 2.19570f, 2.30307f );
        tc.frontLeft.optimumTemperature = optTemp;
        tc.frontLeft.gripFactorPerWear = new float[] { 1.0f, 0.9400f, 0.9371f, 0.9344f, 0.9321f, 0.9301f, 0.9282f, 0.9264f, 0.9248f, 0.9233f, 0.9219f, 0.9205f, 0.9191f, 0.9177f, 0.9161f, 0.9066f, 0.7588f };
        tc.frontLeft.setOptimumPressure( optPress, optPressMult );
        tc.frontLeft.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        tc.frontRight.setDryGrip( 2.19570f, 2.30307f );
        tc.frontRight.optimumTemperature = tc.frontLeft.optimumTemperature;
        tc.frontRight.gripFactorPerWear = tc.frontLeft.gripFactorPerWear;
        tc.frontRight.setOptimumPressure( optPress, optPressMult );
        tc.frontRight.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        tc.rearLeft.setDryGrip( 2.19570f, 2.30307f );
        tc.rearLeft.optimumTemperature = tc.frontLeft.optimumTemperature;
        tc.rearLeft.gripFactorPerWear = tc.frontLeft.gripFactorPerWear;
        tc.rearLeft.setOptimumPressure( optPress, optPressMult );
        tc.rearLeft.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        tc.rearRight.setDryGrip( 2.19570f, 2.30307f );
        tc.rearRight.optimumTemperature = tc.frontLeft.optimumTemperature;
        tc.rearRight.gripFactorPerWear = tc.frontLeft.gripFactorPerWear;
        tc.rearRight.setOptimumPressure( optPress, optPressMult );
        tc.rearRight.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        
        return ( tc );
    }
    
    void loadEditorDefaults()
    {
    	try
    	{
    	    wheelDrive = WheelDrive.REAR;
    	    
	        fuelRange.set( 6.0f, 1.0f, 127 );
	        frontWingRange.set( 14.0f, 0.25f, 65 );
	        
	        engine.boostRange.set( 0f, 1.0f, 9 );
            engine.rpmIncreasePerBoostSetting = -200.0f;
            engine.fuelUsageIncreasePerBoostSetting = -0.001f;
            engine.wearIncreasePerBoostSetting = -0.001f;
            engine.wearIncreasePerVelocity = 3.00e-5f;
	        engine.optimumOilTemperature = 109.0f;
            engine.lifetimeAverage = 6890;
            engine.lifetimeVariance = 1600;
	        engine.baseLifetimeOilTemperature = 126.2f; //114.7f;
            engine.halfLifetimeOilTempOffset = 4.15f;
            engine.baseLifetimeRPM = 16680.0f;
            engine.halfLifetimeRPMOffset = 510.0f;
	        
	        brakes.brakeFrontLeft.setTemperatures( 200f, 450f, 750f, 1050f );
	        brakes.brakeFrontRight.setTemperatures( 200f, 450f, 750f, 1050f );
	        brakes.brakeRearLeft.setTemperatures( 200f, 450f, 750f, 1050f );
	        brakes.brakeRearRight.setTemperatures( 200f, 450f, 750f, 1050f );
            
	        brakes.brakeFrontLeft.brakeFadeRange = Brakes.WheelBrake.DEFAULT_BRAKE_FADE_RANGE;
            brakes.brakeFrontRight.brakeFadeRange = Brakes.WheelBrake.DEFAULT_BRAKE_FADE_RANGE;
            brakes.brakeRearLeft.brakeFadeRange = Brakes.WheelBrake.DEFAULT_BRAKE_FADE_RANGE;
            brakes.brakeRearRight.brakeFadeRange = Brakes.WheelBrake.DEFAULT_BRAKE_FADE_RANGE;
	        
	        brakes.brakeFrontLeft.discRange.set( 0.023f, 0.001f, 6 );
            brakes.brakeFrontLeft.wearRate = 5.770e-011f;
            brakes.brakeFrontLeft.discFailureAverage = 1.45e-02f;
            brakes.brakeFrontLeft.discFailureVariance = 7.00e-04f;
            brakes.brakeFrontLeft.torque = 4100.0f;
            
            brakes.brakeFrontRight.discRange.set( 0.023f, 0.001f, 6 );
            brakes.brakeFrontRight.wearRate = 5.770e-011f;
            brakes.brakeFrontRight.discFailureAverage = 1.45e-02f;
            brakes.brakeFrontRight.discFailureVariance = 7.00e-04f;
            brakes.brakeFrontRight.torque = 4100.0f;
            
            brakes.brakeRearLeft.discRange.set( 0.023f, 0.001f, 6 );
            brakes.brakeRearLeft.wearRate = 5.770e-011f;
            brakes.brakeRearLeft.discFailureAverage = 1.45e-02f;
            brakes.brakeRearLeft.discFailureVariance = 7.00e-04f;
            brakes.brakeRearLeft.torque = 4100.0f;
            
            brakes.brakeRearRight.discRange.set( 0.023f, 0.001f, 6 );
            brakes.brakeRearRight.wearRate = 5.770e-011f;
            brakes.brakeRearRight.discFailureAverage = 1.45e-02f;
            brakes.brakeRearRight.discFailureVariance = 7.00e-04f;
            brakes.brakeRearRight.torque = 4100.0f;
            
	        
	        brakes.balance.set( 0.3f, 0.002f, 151 );
	        
	        usedTBCFile = null;
	        
	        TireCompound[] tireCompounds = new TireCompound[ 20 ];
	        
	        tireCompounds[ 0] = createCompound( "01-Cold",  0, 90.00f );
            tireCompounds[ 1] = createCompound( "01-Hot",   1, 93.00f );
            tireCompounds[ 2] = createCompound( "02-Cold",  2, 90.67f );
            tireCompounds[ 3] = createCompound( "02-Hot",   3, 93.67f );
            tireCompounds[ 4] = createCompound( "03-Cold",  4, 91.33f );
            tireCompounds[ 5] = createCompound( "03-Hot",   5, 94.33f );
            tireCompounds[ 6] = createCompound( "04-Cold",  6, 92.00f );
            tireCompounds[ 7] = createCompound( "04-Hot",   7, 95.00f );
            tireCompounds[ 8] = createCompound( "05-Cold",  8, 92.67f );
            tireCompounds[ 9] = createCompound( "05-Hot",   9, 95.67f );
            tireCompounds[10] = createCompound( "06-Cold", 10, 93.33f );
            tireCompounds[11] = createCompound( "06-Hot",  11, 96.33f );
            tireCompounds[12] = createCompound( "07-Cold", 12, 94.00f );
            tireCompounds[13] = createCompound( "07-Hot",  13, 97.00f );
            tireCompounds[14] = createCompound( "08-Cold", 14, 94.67f );
            tireCompounds[15] = createCompound( "08-Hot",  15, 97.67f );
            tireCompounds[16] = createCompound( "09-Cold", 16, 95.33f );
            tireCompounds[17] = createCompound( "09-Hot",  17, 98.33f );
            tireCompounds[18] = createCompound( "10-Cold", 18, 96.00f );
            tireCompounds[19] = createCompound( "10-Hot",  19, 99.00f );
            
            setTireCompounds( tireCompounds );
	        
	        tirePressureRangeFL.set( 95.0f, 1.0f, 66 );
            tirePressureRangeFR.set( 95.0f, 1.0f, 66 );
            tirePressureRangeRL.set( 95.0f, 1.0f, 66 );
            tirePressureRangeRR.set( 95.0f, 1.0f, 66 );
		}
    	catch ( Throwable t )
    	{
    		Logger.log( t );
		}
    }
    
    void loadFromPhysicsFiles( LiveGameData gameData )
    {
        File profileFolder = RFactorTools.getProfileFolder();
        File cchFile = RFactorTools.getCCHFile( profileFolder );
    	String playerVEHFile = RFactorTools.getPlayerVEHFile( profileFolder );
    	
    	try
    	{
    	    long t0 = System.currentTimeMillis();
    	    
			VehiclePhysicsParser.parsePhysicsFiles( cchFile, RFactorTools.RFACTOR_FOLDER, playerVEHFile, gameData.getScoringInfo().getTrackName(), this );
			
			Logger.log( "Successfully parsed physics files. (Took " + ( System.currentTimeMillis() - t0 ) + "ms.)" );
			
			if ( getInstalledUpgrades() == null )
			{
			    Logger.log( "No upgrades installed." );
			}
			else
			{
			    Logger.log( "Installed upgrades:" );
			    for ( UpgradeIdentifier ui : getInstalledUpgrades() )
			        Logger.log( "  " + ui.getUpgradeType() + ", " + ui.getUpgradeLevel()/* + ", " + ui.getDescription()*/ );
			}
			
			if ( getUsedTBCFile() != null )
			    Logger.log( "Used TBC file: " + getUsedTBCFile().getAbsolutePath() );
		}
    	catch ( Throwable t )
    	{
    		Logger.log( t );
		}
    }
    
    public VehiclePhysics()
    {
        // We initialize with factory defaults, so that in case of parsing errors we at least have "some" values.
        loadEditorDefaults();
    }
}
