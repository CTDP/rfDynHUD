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
package net.ctdp.rfdynhud.gamedata.rfactor1;

import java.io.File;

import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits.Convert;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * This is a model of vehicle physics settings.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class _rf1_VehiclePhysics extends VehiclePhysics
{
    private final PhysicsSetting fuelRangeL = new PhysicsSetting( 1f, 0f );
    private PhysicsSetting fuelRange = new PhysicsSetting( 1f, 0f );;
    private float weightOfOneLiter = 0.742f; // weight of one liter of fuel in kg
    private final PhysicsSetting frontWingRange = new PhysicsSetting();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final PhysicsSetting getFuelRangeL()
    {
        return ( fuelRangeL );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final PhysicsSetting getFuelRange()
    {
        return ( fuelRange );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getWeightOfOneLiterOfFuel()
    {
        return ( weightOfOneLiter );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final PhysicsSetting getFrontWingRange()
    {
        return ( frontWingRange );
    }
    
    WheelDrive wheelDrive;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final WheelDrive getWheelDrive()
    {
        return ( wheelDrive );
    }
    
    short numForwardGears;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final short getNumForwardGears()
    {
        return ( numForwardGears );
    }
    
    /**
     * Model of engine physics parameters.
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static class _rf1_Engine extends Engine
    {
        String name = "N/A";
        int lifetimeAverage;
        int lifetimeVariance;
        float baseLifetimeOilTemperature;
        float halfLifetimeOilTempOffset;
        float optimumOilTemperature;
        float wearIncreasePerDegree;
        float baseLifetimeRPM;
        float halfLifetimeRPMOffset;
        private final PhysicsSetting revLimitRange = new PhysicsSetting();
        private final PhysicsSetting boostRange = new PhysicsSetting( 1f, 1f );
        float rpmIncreasePerBoostSetting;
        float fuelUsageIncreasePerBoostSetting;
        float wearIncreasePerBoostSetting;
        float wearIncreasePerVelocity;
        
        private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected MeasurementUnits getMeasurementUnits()
        {
            return ( measurementUnits );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected void reset()
        {
            super.reset();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected void finish()
        {
            super.finish();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final String getName()
        {
            return ( name );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getLifetimeAverage( double raceLengthMultiplier )
        {
            return ( (int)Math.round( lifetimeAverage * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getLifetimeVariance( double raceLengthMultiplier )
        {
            return ( (int)Math.round( lifetimeVariance * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean hasLifetimeVariance()
        {
            return ( lifetimeVariance != 0 );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getSafeLifetimeTotal( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( lifetimeAverage - lifetimeVariance - lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getGoodLifetimeTotal( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( lifetimeAverage - lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getBadLifetimeTotal( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( lifetimeAverage + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getMaxLifetimeTotal( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( lifetimeAverage + lifetimeVariance + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getLowerSafeLifetimeValue( double raceLengthMultiplier )
        {
            return ( 0 );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getLowerGoodLifetimeValue( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( - lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getLowerBadLifetimeValue( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( - lifetimeVariance - lifetimeVariance - lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getMinLifetimeValue( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( - lifetimeVariance - lifetimeVariance - lifetimeVariance - lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getLifetimeVarianceRange( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( lifetimeVariance + lifetimeVariance + lifetimeVariance + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getLifetimeVarianceHalfRange( double raceLengthMultiplier )
        {
            return ( (int)Math.round( ( lifetimeVariance + lifetimeVariance ) * raceLengthMultiplier ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getBaseLifetimeOilTemperatureC()
        {
            return ( baseLifetimeOilTemperature );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getHalfLifetimeOilTempOffsetC()
        {
            return ( halfLifetimeOilTempOffset );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getOptimumOilTemperatureC()
        {
            return ( optimumOilTemperature );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getOverheatingOilTemperatureC()
        {
            return ( baseLifetimeOilTemperature );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getStrongOverheatingOilTemperatureC()
        {
            return ( baseLifetimeOilTemperature + halfLifetimeOilTempOffset );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getWearIncreasePerDegreeC()
        {
            return ( wearIncreasePerDegree );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getBaseLifetimeRPM()
        {
            return ( baseLifetimeRPM );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getHalfLifetimeRPMOffset()
        {
            return ( halfLifetimeRPMOffset );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final PhysicsSetting getRevLimitRange()
        {
            return ( revLimitRange );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final PhysicsSetting getBoostRange()
        {
            return ( boostRange );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getRPMIncreasePerBoostLevel()
        {
            return ( rpmIncreasePerBoostSetting );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getFuelUsageIncreasePerBoostLevel()
        {
            return ( fuelUsageIncreasePerBoostSetting );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getWearIncreasePerBoostLevel()
        {
            return ( wearIncreasePerBoostSetting );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getWearIncreasePerVelocity()
        {
            return ( wearIncreasePerVelocity );
        }
        
        _rf1_Engine()
        {
        }
    }
    
    private final _rf1_Engine engine = new _rf1_Engine();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf1_Engine getEngine()
    {
        return ( engine );
    }
    
    /**
     * Model of brake physics parameters.
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static class _rf1_Brakes extends Brakes
    {
        private final PhysicsSetting balance = new PhysicsSetting();
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final PhysicsSetting getRearDistributionRange()
        {
            return ( balance );
        }
        
        private final PhysicsSetting pressureRange = new PhysicsSetting();
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final PhysicsSetting getPressureRange()
        {
            return ( pressureRange );
        }
        
        /**
         * Brake settings for a single wheel.
         * 
         * @author Marvin Froehlich (CTDP)
         */
        public static class _rf1_WheelBrake extends WheelBrake
        {
            private boolean brakeResponseCurveSet = false;
            private float optimumTemperaturesLowerBound;
            private float optimumTemperaturesUpperBound;
            private float coldTemperature;
            private float overheatingTemperature;
            private float wearIncreasePerDegreeOverOptimum;
            private float wearDecreasePerDegreeBelowOptimum;
            float brakeOptimumTemp = -Float.MAX_VALUE / 2;
            float brakeFadeRange = 0f;
            
            private final PhysicsSetting discRange = new PhysicsSetting();
            double wearRate;
            float discFailureAverage;
            float discFailureVariance;
            float torqueBase;
            
            private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected MeasurementUnits getMeasurementUnits()
            {
                return ( measurementUnits );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getOptimumTemperaturesLowerBoundC()
            {
                return ( optimumTemperaturesLowerBound );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getOptimumTemperaturesUpperBoundC()
            {
                return ( optimumTemperaturesUpperBound );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getColdTemperatureC()
            {
                return ( coldTemperature );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getOverheatingTemperatureC()
            {
                return ( overheatingTemperature );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getWearIncreasePerDegreeCOverOptimum()
            {
                return ( wearIncreasePerDegreeOverOptimum );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getWearDecreasePerDegreeCBelowOptimum()
            {
                return ( wearDecreasePerDegreeBelowOptimum );
            }
    		
            void setTemperatures( float coldTemperature, float optimumTemperaturesLowerBound, float optimumTemperaturesUpperBound, float overheatingTemperature )
            {
                this.coldTemperature = coldTemperature;
                this.optimumTemperaturesLowerBound = optimumTemperaturesLowerBound;
                this.optimumTemperaturesUpperBound = optimumTemperaturesUpperBound;
                this.overheatingTemperature = overheatingTemperature;
                this.brakeResponseCurveSet = true;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getBrakeFadeRangeC()
            {
                return ( brakeFadeRange );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getBrakeFadeColdTemperatureC()
            {
                return ( optimumTemperaturesLowerBound - brakeFadeRange );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getBrakeFadeHotTemperatureC()
            {
                return ( optimumTemperaturesUpperBound + brakeFadeRange );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final PhysicsSetting getDiscRange()
            {
                return ( discRange );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final double getWearRate()
            {
                return ( wearRate );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getDiscFailureAverage()
            {
                return ( discFailureAverage );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getDiscFailureVariance()
            {
                return ( discFailureVariance );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final boolean hasDiscFailureVariance()
            {
                return ( ( discFailureVariance < -0.0000001f ) || ( discFailureVariance > +0.0000001f ) );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getGoodDiscFailure()
            {
                return ( discFailureAverage );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getTorqueBase()
            {
                return ( torqueBase );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final double computeTorque( float brakeTempK )
            {
                // Get base brake torque from HDV.
                double torque = getTorqueBase();
                
                //RFDHLog.debugCS( brakeTempK, getColdTemperatureK(), getOptimumTemperaturesLowerBoundK(), getOptimumTemperaturesUpperBoundK(), getOverheatingTemperatureK() );
                
                // Compare current temperature to the values from BrakeResponseCurve (converted to Kelvin).
                
                if ( brakeTempK < getColdTemperatureK() )
                {
                    // Brake torque is halfed when brakes are cold.
                    torque *= 0.5;
                }
                else if ( brakeTempK < getOptimumTemperaturesLowerBoundK() )
                {
                    // Brake temperature is between cold and lower optimum temp.
                    
                    final double coldRange = getOptimumTemperaturesLowerBoundK() - getColdTemperatureK();
                    final double brakeFadeColdMult = ( coldRange > 0.0 ) ? ( Math.PI / coldRange ) : 0.0;
                    
                    torque *= ( 0.75 + ( 0.25 * Math.cos( ( brakeTempK - getOptimumTemperaturesLowerBoundK() ) * brakeFadeColdMult ) ) );
                }
                else if ( brakeTempK > getOverheatingTemperatureK() )
                {
                    // Brake torque is halfed when brakes are overheated.
                    torque *= 0.5;
                }
                else if ( brakeTempK > getOptimumTemperaturesUpperBoundK() )
                {
                    // Brake temperature is between upper optimum and overheating temp.
                    
                    final double hotRange = getOverheatingTemperatureK() - getOptimumTemperaturesUpperBoundK();
                    final double brakeFadeHotMult = ( hotRange > 0.0 ) ? ( Math.PI / hotRange ) : 0.0;
                    
                    torque *= ( 0.75 + ( 0.25 * Math.cos( ( brakeTempK - getOptimumTemperaturesUpperBoundK() ) * brakeFadeHotMult ) ) );
                }
                
                // Brake temperature is in optimum range.
                
                return ( torque );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected void reset()
            {
                super.reset();
                
                brakeOptimumTemp = -Float.MAX_VALUE / 2f;
                brakeFadeRange = 0f;
                brakeResponseCurveSet = false;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected void finish()
            {
                super.finish();
                
                if ( !brakeResponseCurveSet )
                {
                    this.coldTemperature = brakeOptimumTemp - brakeFadeRange;
                    this.optimumTemperaturesLowerBound = brakeOptimumTemp;
                    this.optimumTemperaturesUpperBound = brakeOptimumTemp;
                    this.overheatingTemperature = brakeOptimumTemp + brakeFadeRange;
                }
            }
            
    		_rf1_WheelBrake( Wheel wheel )
    		{
    		    super( wheel );
    		}
        }
        
        private final _rf1_WheelBrake brakeFrontLeft = new _rf1_WheelBrake( Wheel.FRONT_LEFT );
        private final _rf1_WheelBrake brakeFrontRight = new _rf1_WheelBrake( Wheel.FRONT_RIGHT );
        private final _rf1_WheelBrake brakeRearLeft = new _rf1_WheelBrake( Wheel.REAR_LEFT );
        private final _rf1_WheelBrake brakeRearRight = new _rf1_WheelBrake( Wheel.REAR_RIGHT );
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final _rf1_WheelBrake getBrake( Wheel wheel )
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected void reset()
        {
            super.reset();
            
            brakeFrontLeft.reset();
            brakeFrontRight.reset();
            brakeRearLeft.reset();
            brakeRearRight.reset();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected void finish()
        {
            super.finish();
            
            brakeFrontLeft.finish();
            brakeFrontRight.finish();
            brakeRearLeft.finish();
            brakeRearRight.finish();
        }
    }
    
    private final _rf1_Brakes brakes = new _rf1_Brakes();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf1_Brakes getBrakes()
    {
        return ( brakes );
    }
    
    /**
     * Model of a tire slip curve.
     * 
     * @author Marvin Froehlich (CTDP)
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
        
        public final float getDropOffFunction()
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
    
    /**
     * Gets TBC tire compound file, currently in use. (may depend on upgrades)
     * 
     * @return TBC tire compound file, currently in use.
     */
    public final File getUsedTBCFile()
    {
        return ( usedTBCFile );
    }
    
    /**
     * Model of a tire compound.
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static class _rf1_TireCompound extends TireCompound
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
         * {@inheritDoc}
         */
        @Override
        public final String getName()
        {
            return ( name );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
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
         * Model of one wheel of a compound. There will always be one {@link _rf1_CompoundWheel} in a {@link _rf1_TireCompound} for each wheel of the vehicle.
         * 
         * @author Marvin Froehlich (CTDP)
         */
        public static class _rf1_CompoundWheel extends CompoundWheel
        {
            private float dryLateralGrip;
            private float dryLongitudinalGrip;
            private float gripTempPress1;
            private float gripTempPress2;
            private float optimumTemperatureK;
            private float optimumTemperatureC;
            private float gripLossPerDegreeBelowOptimum;
            private float gripLossPerDegreeAboveOptimum;
            private float optPress;
            private float optPressMult;
            private float offPressure;
            float[] gripFactorPerWear;
            
            //WearGrip1=(0.980,0.961,0.941,0.922,0.902,0.883,0.863,0.844) // Grip at 6/13/19/25/31/38/44/50% wear (defaults to 0.980->0.844), grip is 1.0 at 0% wear
            //WearGrip2=(0.824,0.805,0.785,0.766,0.746,0.727,0.707,0.688) // Grip at 56/63/69/75/81/88/94/100% wear (defaults to 0.824->0.688), tire bursts at 100% wear
            private static final float[] DEFAULT_WEAR_GRIP = { 1.0f, 0.980f, 0.961f, 0.941f, 0.922f, 0.902f, 0.883f, 0.863f, 0.844f, 0.824f, 0.805f, 0.785f, 0.766f, 0.746f, 0.727f, 0.707f, 0.688f };
            
            private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
            
            /**
             * {@inheritDoc}
             */
            @Override
            protected MeasurementUnits getMeasurementUnits()
            {
                return ( measurementUnits );
            }
            
            void setDryGrip( float laterial, float longitudinal )
            {
                this.dryLateralGrip = laterial;
                this.dryLongitudinalGrip = longitudinal;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getDryLateralGrip()
            {
                return ( dryLateralGrip );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getDryLongitudinalGrip()
            {
                return ( dryLongitudinalGrip );
            }
            
            void setOptimumTemperatureC( float optTemp )
            {
                this.optimumTemperatureC = optTemp;
                this.optimumTemperatureK = optTemp - Convert.ZERO_KELVIN;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getOptimumTemperatureCLowerBound()
            {
                return ( optimumTemperatureC );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getOptimumTemperatureCUpperBound()
            {
                return ( optimumTemperatureC );
            }
            
            /**
             * @param belowTempC TBC "GripTempPress" value 1
             * @param aboveTempC TBC "GripTempPress" value 2
             * @param offPress TBC "GripTempPress" value 3
             */
            void setAboveAndBelowTempsAndPressures( float belowTempC, float aboveTempC, float offPress )
            {
                this.gripTempPress1 = belowTempC;
                this.gripTempPress2 = aboveTempC;
                
                float recipOptimumTemperature = ( optimumTemperatureK != 0.0f ) ? ( 1.0f / optimumTemperatureK ) : 0.0f;
                
                this.gripLossPerDegreeBelowOptimum = belowTempC * recipOptimumTemperature;
                this.gripLossPerDegreeAboveOptimum = aboveTempC * recipOptimumTemperature;
                
                this.offPressure = offPress;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getGripLossPerDegreeCBelowOptimum()
            {
                return ( gripLossPerDegreeBelowOptimum );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getBelowTemperatureC( float grip )
            {
                //return ( optimumTemperatureC - ( grip / gripLossPerDegreeBelowOptimum ) );
                
                float dt = (float)Math.sqrt( ( grip - 1.0f ) / -0.5f ) * optimumTemperatureK / gripTempPress1;
                
                return ( optimumTemperatureC - dt );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getAboveTemperatureC( float grip )
            {
                //return ( optimumTemperatureC + ( grip / gripLossPerDegreeAboveOptimum ) );
                
                float dt = (float)Math.sqrt( ( grip - 1.0f ) / -0.5f ) * optimumTemperatureK / gripTempPress2;
                
                return ( optimumTemperatureC + dt );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getGripFactorByTemperatureC( float avgTemperatureC )
            {
                float diffTemp = avgTemperatureC - optimumTemperatureC;
                float gripLossTimesDiffTemp = ( diffTemp < 0.0f ) ? ( gripLossPerDegreeBelowOptimum * -diffTemp ) : ( gripLossPerDegreeAboveOptimum * diffTemp );
                
                return ( 1.0f - 0.5f * gripLossTimesDiffTemp * gripLossTimesDiffTemp );
            }
            
            /**
             * @param optPress TBC "OptimumPressure" field 1
             * @param mult TBC "OptimumPressure" field 2
             */
            void setOptimumPressure( float optPress, float mult )
            {
                this.optPress = optPress;
                this.optPressMult = mult;
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getOptimumPressure( float load )
            {
                return ( optPress + ( optPressMult * load ) );
            }
            
            /*
             * {@inheritDoc}
             */
            /*
            @Override
            public final float getPressureForGrip( float grip, float load )
            {
                if ( grip <= 0.0f )
                    return ( 0.0f );
                
                float optPressLoad = getOptimumPressure( load );
                float recipOptPress = ( optPressLoad != 0.0f ) ? ( 1.0f / optPressLoad ) : 0.0f;
                
                float offPressureGrip = offPressure * recipOptPress;
                
                return ( Math.abs( optPressLoad + grip / offPressureGrip ) );
            }
            */
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getGripFactorByPressure( float pressure, float load )
            {
                if ( optPress <= 0.0f )
                    return ( 0.0f );
                
                float optPressLoad = getOptimumPressure( load );
                float recipOptPress = ( optPressLoad != 0.0f ) ? ( 1.0f / optPressLoad ) : 0.0f;
                
                float offPressGrip = Math.abs( pressure - optPressLoad ) * offPressure * recipOptPress;
                
                return ( 1.0f - 0.5f * offPressGrip * offPressGrip  );
            }
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getGripFraction( float wear, float avgTemperatureC, float pressure, float load )
            {
                if ( optPress <= 0.0f )
                    return ( 0.0f );
                
                float gfTemp = getGripFactorByTemperatureC( avgTemperatureC );
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
             * {@inheritDoc}
             */
            @Override
            public final float getWearGripFactor( float wear )
            {
                final float[] w = ( gripFactorPerWear == null ) ? DEFAULT_WEAR_GRIP : gripFactorPerWear;
                
                // w[0] will always be set to 1.0.
                
                // WearGrip1=(0.989,0.981,0.9745,0.9715,0.969,0.967,0.9655,0.9645) // Grip at 6/13/19/25/31/38/44/50% wear (defaults to 0.980->0.844), grip is 1.0 at 0% wear
                // WearGrip2=(0.964,0.9638,0.963,0.961,0.9535,0.936,0.850,0.775) // Grip at 56/63/69/75/81/88/94/100% wear (defaults to 0.824->0.688), tire bursts at 100% wear
                
                wear = 1.0f - wear;
                
                // TODO: Use binary search!
                
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
            
            /**
             * {@inheritDoc}
             */
            @Override
            public final float getMinGrip()
            {
                final float[] w = ( gripFactorPerWear == null ) ? DEFAULT_WEAR_GRIP : gripFactorPerWear;
                
                return ( w[w.length - 1] );
            }
            
            _rf1_CompoundWheel( Wheel wheel )
            {
                super( wheel );
            }
        }
        
        private final _rf1_CompoundWheel frontLeft = new _rf1_CompoundWheel( Wheel.FRONT_LEFT );
        private final _rf1_CompoundWheel frontRight = new _rf1_CompoundWheel( Wheel.FRONT_RIGHT );
        private final _rf1_CompoundWheel rearLeft = new _rf1_CompoundWheel( Wheel.REAR_LEFT );
        private final _rf1_CompoundWheel rearRight = new _rf1_CompoundWheel( Wheel.REAR_RIGHT );
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final _rf1_CompoundWheel getWheel( Wheel wheel )
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
        
        void setOptimumTempForAll4( float optimumTempC )
        {
            frontLeft.optimumTemperatureC = optimumTempC;
            frontRight.optimumTemperatureC = optimumTempC;
            rearLeft.optimumTemperatureC = optimumTempC;
            rearRight.optimumTemperatureC = optimumTempC;
        }
        
        _rf1_TireCompound()
        {
            super();
        }
    }
    
    private _rf1_TireCompound[] tireCompounds;
    private _rf1_TireCompound tcBestGripFrontLeft = null;
    private _rf1_TireCompound tcBestGripFrontRight = null;
    private _rf1_TireCompound tcBestGripRearLeft = null;
    private _rf1_TireCompound tcBestGripRearRight = null;
    
    void setTireCompounds( _rf1_TireCompound[] tireCompounds )
    {
        this.tireCompounds = tireCompounds;
        
        this.tcBestGripFrontLeft = null;
        this.tcBestGripFrontRight = null;
        this.tcBestGripRearLeft = null;
        this.tcBestGripRearRight = null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNumTireCompounds()
    {
        if ( tireCompounds == null )
            return ( 0 );
        
        return ( tireCompounds.length );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf1_TireCompound getTireCompound( int index )
    {
        if ( tireCompounds == null )
        {
            RFDHLog.exception( "WARNING: No tire compounds known." );
            
            return ( FALLBACK_COMPOUND );
        }
        
        if ( index >= tireCompounds.length )
        {
            RFDHLog.exception( "WARNING: Unknown tire compound index " + index + ". Using closest one." );
            index = tireCompounds.length - 1;
        }
        
        return ( tireCompounds[index] );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf1_TireCompound getTireCompoundBestGrip( Wheel wheel )
    {
        if ( tireCompounds == null )
            return ( FALLBACK_COMPOUND );
        
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
        _rf1_TireCompound maxGripCompound = null;
        
        for ( int i = 0; i < tireCompounds.length; i++ )
        {
            _rf1_TireCompound._rf1_CompoundWheel tcw = tireCompounds[i].getWheel( wheel );
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
     * {@inheritDoc}
     */
    @Override
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
    
    public static class _rf1_UpgradeIdentifier extends UpgradeIdentifier
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final String getDescription()
        {
            return ( description );
        }
        
        _rf1_UpgradeIdentifier( String upgradeType, String upgradeLevel, String description )
        {
            this.upgradeType = upgradeType;
            this.upgradeLevel = upgradeLevel;
            this.description = description;
        }
    }
    
    _rf1_UpgradeIdentifier[] installedUpgrades = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf1_UpgradeIdentifier[] getInstalledUpgrades()
    {
        return ( installedUpgrades );
    }
    
    private static _rf1_TireCompound createDefaultCompound( String name, int index, float optTemp )
    {
        float optPress = 64.5f;
        float optPressMult = 0.018841f;
        
        _rf1_TireCompound tc = new _rf1_TireCompound();
        
        tc.name = name;
        tc.index = index;
        tc.frontLeft.setDryGrip( 2.19570f, 2.30307f );
        tc.frontLeft.optimumTemperatureC = optTemp;
        tc.frontLeft.gripFactorPerWear = new float[] { 1.0f, 0.9400f, 0.9371f, 0.9344f, 0.9321f, 0.9301f, 0.9282f, 0.9264f, 0.9248f, 0.9233f, 0.9219f, 0.9205f, 0.9191f, 0.9177f, 0.9161f, 0.9066f, 0.7588f };
        tc.frontLeft.setOptimumPressure( optPress, optPressMult );
        tc.frontLeft.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        tc.frontRight.setDryGrip( 2.19570f, 2.30307f );
        tc.frontRight.optimumTemperatureC = tc.frontLeft.optimumTemperatureC;
        tc.frontRight.gripFactorPerWear = tc.frontLeft.gripFactorPerWear;
        tc.frontRight.setOptimumPressure( optPress, optPressMult );
        tc.frontRight.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        tc.rearLeft.setDryGrip( 2.19570f, 2.30307f );
        tc.rearLeft.optimumTemperatureC = tc.frontLeft.optimumTemperatureC;
        tc.rearLeft.gripFactorPerWear = tc.frontLeft.gripFactorPerWear;
        tc.rearLeft.setOptimumPressure( optPress, optPressMult );
        tc.rearLeft.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        tc.rearRight.setDryGrip( 2.19570f, 2.30307f );
        tc.rearRight.optimumTemperatureC = tc.frontLeft.optimumTemperatureC;
        tc.rearRight.gripFactorPerWear = tc.frontLeft.gripFactorPerWear;
        tc.rearRight.setOptimumPressure( optPress, optPressMult );
        tc.rearRight.setAboveAndBelowTempsAndPressures( 3.870f, 2.269f, 0.814f );
        
        return ( tc );
    }
    
    public static final _rf1_TireCompound FALLBACK_COMPOUND = createDefaultCompound( "DUMMY", 0, 96.5f );
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset()
    {
        super.reset();
        
        tireCompounds = null;
        tcBestGripFrontLeft = null;
        tcBestGripFrontRight = null;
        tcBestGripRearLeft = null;
        tcBestGripRearRight = null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void finish()
    {
        super.finish();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyMeasurementUnits( MeasurementUnits measurementUnits )
    {
        this.engine.measurementUnits = measurementUnits;
        this.brakes.brakeFrontLeft.measurementUnits = measurementUnits;
        this.brakes.brakeFrontRight.measurementUnits = measurementUnits;
        this.brakes.brakeRearLeft.measurementUnits = measurementUnits;
        this.brakes.brakeRearRight.measurementUnits = measurementUnits;
        
        if ( tireCompounds != null )
        {
            for ( int i = 0; i < tireCompounds.length; i++ )
            {
                tireCompounds[i].frontLeft.measurementUnits = measurementUnits;
                tireCompounds[i].frontRight.measurementUnits = measurementUnits;
                tireCompounds[i].rearLeft.measurementUnits = measurementUnits;
                tireCompounds[i].rearRight.measurementUnits = measurementUnits;
            }
        }
        
        this.fuelRange = new PhysicsSetting( ( measurementUnits == MeasurementUnits.IMPERIAL ) ? Convert.LITERS_TO_GALONS : 1f, 0f );
        set( this.fuelRangeL, this.fuelRange );
    }
    
    void loadDefaults()
    {
        try
        {
            wheelDrive = WheelDrive.REAR;
            numForwardGears = 7;
            
            set( 6.0f, 1.0f, 127, fuelRangeL );
            set( 6.0f, 1.0f, 127, fuelRange );
            set( 14.0f, 0.25f, 65, frontWingRange );
            
            set( 20000.0f, -250f, 9, engine.revLimitRange );
            set( 0f, 1.0f, 9, engine.boostRange );
            engine.rpmIncreasePerBoostSetting = -200.0f;
            engine.fuelUsageIncreasePerBoostSetting = -0.001f;
            engine.wearIncreasePerBoostSetting = -0.001f;
            engine.wearIncreasePerVelocity = 3.00e-5f;
            engine.optimumOilTemperature = 109.0f;
            engine.lifetimeAverage = 5000;// 6890;
            engine.lifetimeVariance = 1000;//1600;
            engine.baseLifetimeOilTemperature = 126.2f; //114.7f;
            engine.halfLifetimeOilTempOffset = 4.15f;
            engine.baseLifetimeRPM = 16661.3f;
            engine.halfLifetimeRPMOffset = 594.0f;
            
            brakes.brakeFrontLeft.setTemperatures( 200f, 450f, 750f, 1050f );
            brakes.brakeFrontRight.setTemperatures( 200f, 450f, 750f, 1050f );
            brakes.brakeRearLeft.setTemperatures( 200f, 450f, 750f, 1050f );
            brakes.brakeRearRight.setTemperatures( 200f, 450f, 750f, 1050f );
            
            set( 0.023f, 0.001f, 6, brakes.brakeFrontLeft.discRange );
            brakes.brakeFrontLeft.wearRate = 5.770e-011f;
            brakes.brakeFrontLeft.discFailureAverage = 1.45e-02f;
            brakes.brakeFrontLeft.discFailureVariance = 7.00e-04f;
            brakes.brakeFrontLeft.torqueBase = 4100.0f;
            
            set( 0.023f, 0.001f, 6, brakes.brakeFrontRight.discRange );
            brakes.brakeFrontRight.wearRate = 5.770e-011f;
            brakes.brakeFrontRight.discFailureAverage = 1.45e-02f;
            brakes.brakeFrontRight.discFailureVariance = 7.00e-04f;
            brakes.brakeFrontRight.torqueBase = 4100.0f;
            
            set( 0.023f, 0.001f, 6, brakes.brakeRearLeft.discRange );
            brakes.brakeRearLeft.wearRate = 5.770e-011f;
            brakes.brakeRearLeft.discFailureAverage = 1.45e-02f;
            brakes.brakeRearLeft.discFailureVariance = 7.00e-04f;
            brakes.brakeRearLeft.torqueBase = 4100.0f;
            
            set( 0.023f, 0.001f, 6, brakes.brakeRearRight.discRange );
            brakes.brakeRearRight.wearRate = 5.770e-011f;
            brakes.brakeRearRight.discFailureAverage = 1.45e-02f;
            brakes.brakeRearRight.discFailureVariance = 7.00e-04f;
            brakes.brakeRearRight.torqueBase = 4100.0f;
            
            
            set( 0.3f, 0.002f, 151, brakes.balance );
            
            usedTBCFile = null;
            
            _rf1_TireCompound[] tireCompounds = new _rf1_TireCompound[ 20 ];
            
            tireCompounds[ 0] = createDefaultCompound( "01-Cold",  0, 90.00f );
            tireCompounds[ 1] = createDefaultCompound( "01-Hot",   1, 93.00f );
            tireCompounds[ 2] = createDefaultCompound( "02-Cold",  2, 90.67f );
            tireCompounds[ 3] = createDefaultCompound( "02-Hot",   3, 93.67f );
            tireCompounds[ 4] = createDefaultCompound( "03-Cold",  4, 91.33f );
            tireCompounds[ 5] = createDefaultCompound( "03-Hot",   5, 94.33f );
            tireCompounds[ 6] = createDefaultCompound( "04-Cold",  6, 92.00f );
            tireCompounds[ 7] = createDefaultCompound( "04-Hot",   7, 95.00f );
            tireCompounds[ 8] = createDefaultCompound( "05-Cold",  8, 92.67f );
            tireCompounds[ 9] = createDefaultCompound( "05-Hot",   9, 95.67f );
            tireCompounds[10] = createDefaultCompound( "06-Cold", 10, 93.33f );
            tireCompounds[11] = createDefaultCompound( "06-Hot",  11, 96.33f );
            tireCompounds[12] = createDefaultCompound( "07-Cold", 12, 94.00f );
            tireCompounds[13] = createDefaultCompound( "07-Hot",  13, 97.00f );
            tireCompounds[14] = createDefaultCompound( "08-Cold", 14, 94.67f );
            tireCompounds[15] = createDefaultCompound( "08-Hot",  15, 97.67f );
            tireCompounds[16] = createDefaultCompound( "09-Cold", 16, 95.33f );
            tireCompounds[17] = createDefaultCompound( "09-Hot",  17, 98.33f );
            tireCompounds[18] = createDefaultCompound( "10-Cold", 18, 96.00f );
            tireCompounds[19] = createDefaultCompound( "10-Hot",  19, 99.00f );
            
            setTireCompounds( tireCompounds );
            
            set( 95.0f, 1.0f, 66, tirePressureRangeFL );
            set( 95.0f, 1.0f, 66, tirePressureRangeFR );
            set( 95.0f, 1.0f, 66, tirePressureRangeRL );
            set( 95.0f, 1.0f, 66, tirePressureRangeRR );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    public _rf1_VehiclePhysics()
    {
        // We initialize with factory defaults, so that in case of parsing errors we at least have "some" values.
        loadDefaults();
    }
    
    /*
    public static void main( String[] args )
    {
        VehiclePhysics vp = new VehiclePhysics();
        TireCompound tc = vp.getTireCompound( 0 );
        TireCompound.CompoundWheel cw = tc.getWheel( Wheel.FRONT_LEFT );
        
        cw.setOptimumTemperatureC( 100.0f );
        cw.setAboveAndBelowTempsAndPressures( 1.524f, 3.523f, 0.845f );
        System.out.println( "FL: temp: opt-temp: " + cw.getOptimumTemperatureC() + ", grip-loss/°above: " + cw.gripLossPerDegreeAboveOptimum );
        System.out.println( tc.getWheel( Wheel.FRONT_LEFT ).getGripFactorByTemperatureC( 111.05894f ) );
    }
    */
}
