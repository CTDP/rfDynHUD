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

import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits.Convert;

/**
 * This is a model of vehicle physics settings.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class VehiclePhysics
{
    /**
     * Abstraction of a usual physics setting (base_value, step_size, num_steps).
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static class PhysicsSetting
    {
        private static long nextUpdateId = 1;
        
        private long updateId = nextUpdateId++;
        
        private final float factor;
        private final float baseOffset;
        private float baseValue = 0f;
        private float stepSize = 1f;
        private int numSteps = 1;
        
        protected void set( float baseValue, float stepSize, int numSteps )
        {
            this.updateId = nextUpdateId++;
            
            this.baseValue = baseOffset + baseValue;
            this.stepSize = stepSize;
            this.numSteps = numSteps;
        }
        
        protected void set( PhysicsSetting source )
        {
            this.updateId = nextUpdateId++;
            
            this.baseValue = source.baseValue;
            this.stepSize = source.stepSize;
            this.numSteps = source.numSteps;
        }
        
        /**
         * The base value of this physics setting.
         * {@link #getStepSize()} and {@link #getNumSteps()} add to it up to the {@link #getMaxValue()}.
         * 
         * @return base value of this physics setting.
         */
        public final float getBaseValue()
        {
            return ( baseValue * factor );
        }
        
        /**
         * Gets the number size of each step for this setting.
         * 
         * @return the number size of each step for this setting.
         */
        public final float getStepSize()
        {
            return ( stepSize * factor );
        }
        
        /**
         * Gets the number of steps for this setting.
         * 
         * @return the number of steps for this setting.
         */
        public final int getNumSteps()
        {
            return ( numSteps );
        }
        
        /**
         * Gets the minimum value for this physics setting's range.
         * 
         * @return the minimum value for this physics setting's range.
         */
        public final float getMinValue()
        {
            if ( stepSize > 0f )
                return ( baseValue * factor );
            
            return ( ( baseValue + ( numSteps - 1 ) * stepSize ) * factor );
        }
        
        /**
         * Gets the maximum value for this physics setting's range.
         * 
         * @return the maximum value for this physics setting's range.
         */
        public final float getMaxValue()
        {
            if ( stepSize < 0f )
                return ( baseValue * factor );
            
            return ( ( baseValue + ( numSteps - 1 ) * stepSize ) * factor );
        }
        
        /**
         * Gets the value for the given setting.
         * 
         * @param setting the setting to get the value for
         * 
         * @return the value for the given setting.
         */
        public final float getValueForSetting( int setting )
        {
            // There shuold be a range check. But since this cannot be used for cheating, it isn't necessary.
            
            return ( ( baseValue + stepSize * setting ) * factor );
        }
        
        /**
         * Clamps the given value to the range of possible values in this physics setting.
         * 
         * @param value the value to be clamped.
         * 
         * @return the clamped value.
         */
        public final float clampValue( float value )
        {
            return ( Math.max( getMinValue(), Math.min( value, getMaxValue() ) ) );
        }
        
        /**
         * <p>
         * Clamps the given value plus one step increment to the range of possible values in this physics setting.
         * </p>
         * <p>
         * This is equivalent to<br />
         * clampValue( value + getStepSize() )
         * </p>
         * 
         * @param value the value to be clamped.
         * 
         * @return the clamped value.
         */
        public final float clampValuePlusStep( float value )
        {
            return ( clampValue( value + getStepSize() ) );
        }
        
        /**
         * <p>
         * Clamps the given value minus one step increment to the range of possible values in this physics setting.
         * </p>
         * <p>
         * This is equivalent to<br />
         * clampValue( value - getStepSize() )
         * </p>
         * 
         * @param value the value to be clamped.
         * 
         * @return the clamped value.
         */
        public final float clampValueMinusStep( float value )
        {
            return ( clampValue( value - getStepSize() ) );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return ( getClass().getName() + " { baseOffset: " + baseOffset + ", baseValue: " + baseValue + ", stepSize: " + stepSize + ", numSteps: " + numSteps + " }" );
        }
        
        public PhysicsSetting( float factor, float baseOffset )
        {
            this.factor = factor;
            this.baseOffset = baseOffset;
        }
        
        public PhysicsSetting()
        {
            this( 1f, 0f );
        }
    }
    
    private PhysicsSetting fuelRange = new PhysicsSetting( 1f, 0f );
    private long fuelRangeLUpdateId = -1L;
    
    protected final void set( float baseValue, float stepSize, int numSteps, PhysicsSetting target )
    {
        target.set( baseValue, stepSize, numSteps );
    }
    
    protected final void set( PhysicsSetting source, PhysicsSetting target )
    {
        target.set( source );
    }
    
    /**
     * Gets the phyiscs setting for fuel range in liters.
     * 
     * @return the phyiscs setting for fuel range in liters.
     */
    public abstract PhysicsSetting getFuelRangeL();
    
    /**
     * Gets the phyiscs setting for fuel range in the selected units.
     * 
     * @return the phyiscs setting for fuel range in the selected units.
     */
    public final PhysicsSetting getFuelRange()
    {
        if ( fuelRangeLUpdateId != getFuelRangeL().updateId )
        {
            fuelRange.set( getFuelRangeL() );
            fuelRangeLUpdateId = getFuelRangeL().updateId;
        }
        
        return ( fuelRange );
    }
    
    /**
     * Gets the weight of one liter of fuel in kg.
     * 
     * @return the weight of one liter of fuel in kg.
     */
    public abstract float getWeightOfOneLiterOfFuel();
    
    /**
     * Gets the front wing range of settings.
     * 
     * @return the front wing range of settings.
     */
    public abstract PhysicsSetting getFrontWingRange();
    
    /**
     * Abstraction of possible Wheel drive settings.
     * 
     * @author Marvin Froehlich (CTDP)
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
    
    /**
     * Gets the vehicle's {@link WheelDrive}.
     * 
     * @return the vehicle's {@link WheelDrive}
     */
    public abstract WheelDrive getWheelDrive();
    
    /**
     * Gets the number of forward gears.
     * 
     * @return the number of forward gears.
     */
    public abstract short getNumForwardGears();
    
    /**
     * Model of engine physics parameters.
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static abstract class Engine
    {
        protected abstract MeasurementUnits getMeasurementUnits();
        
        protected void reset()
        {
        }
        
        protected void finish()
        {
        }
        
        /**
         * Gets the engine's name.
         * 
         * @return the engine's name.
         */
        public abstract String getName();
        
        /**
         * Gets the average lifetime for the given rance length in seconds.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the average lifetime for the given rance length in seconds.
         */
        public abstract int getLifetimeAverage( double raceLengthMultiplier );
        
        /**
         * Gets the variance of lifetime for the given rance length in seconds.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the variance of lifetime for the given rance length in seconds.
         */
        public abstract int getLifetimeVariance( double raceLengthMultiplier );
        
        /**
         * Gets, whether this engine has a lifetime variance.
         * 
         * @return whether this engine has a lifetime variance.
         */
        public abstract boolean hasLifetimeVariance();
        
        /**
         * Gets the total lifetime in seconds, that the engine will last for sure.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the total lifetime, that the engine will last for sure.
         */
        public abstract int getSafeLifetimeTotal( double raceLengthMultiplier );
        
        /**
         * Gets the total lifetime in seconds, that the engine will most probably hold.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the total lifetime in seconds, that the engine will most probably hold.
         */
        public abstract int getGoodLifetimeTotal( double raceLengthMultiplier );
        
        /**
         * Gets the total lifetime seconds of the barrier, where the engine is in really bad shape.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the total lifetime seconds of the barrier, where the engine is in really bad shape.
         */
        public abstract int getBadLifetimeTotal( double raceLengthMultiplier );
        
        /**
         * Gets the maximum number of lifetime seconds, that the engine can possibly last.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the maximum number of lifetime seconds, that the engine can possibly last.
         */
        public abstract int getMaxLifetimeTotal( double raceLengthMultiplier );
        
        /**
         * Gets the lower bound of lifetime values for the "safe" range (zero).
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the lower bound of lifetime values for the "safe" range (zero).
         */
        public abstract int getLowerSafeLifetimeValue( double raceLengthMultiplier );
        
        /**
         * Gets the lower bound of lifetime values for the "good" range.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the lower bound of lifetime values for the "good" range.
         */
        public abstract int getLowerGoodLifetimeValue( double raceLengthMultiplier );
        
        /**
         * Gets the lower bound of lifetime values for the "bad" range.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the lower bound of lifetime values for the "bad" range.
         */
        public abstract int getLowerBadLifetimeValue( double raceLengthMultiplier );
        
        /**
         * Gets the smalles lifetime value, that your engine can possibly have.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the smalles lifetime value, that your engine can possibly have.
         */
        public abstract int getMinLifetimeValue( double raceLengthMultiplier );
        
        /**
         * Gets the size of the variance rance for engine lifetime in seconds.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the size of the variance rance for engine lifetime in seconds.
         */
        public abstract int getLifetimeVarianceRange( double raceLengthMultiplier );
        
        /**
         * Gets the half size of the variance rance for engine lifetime in seconds.
         * 
         * @param raceLengthMultiplier the fraction of race length
         * 
         * @return the half size of the variance rance for engine lifetime in seconds.
         */
        public abstract int getLifetimeVarianceHalfRange( double raceLengthMultiplier );
        
        /**
         * Gets the base temperature for engine life time in °C.
         * 
         * @return the base temperature for engine life time in °C.
         */
        public abstract float getBaseLifetimeOilTemperatureC();
        
        /**
         * Gets the base temperature for engine life time in °F.
         * 
         * @return the base temperature for engine life time in °F.
         */
        public final float getBaseLifetimeOilTemperatureF()
        {
            return ( Convert.celsius2Fahrehheit( getBaseLifetimeOilTemperatureC() ) );
        }
        
        /**
         * Gets the base temperature for engine life time in the units selected in the PLR.
         * 
         * @return the base temperature for engine life time in the units selected in the PLR.
         */
        public final float getBaseLifetimeOilTemperature()
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getBaseLifetimeOilTemperatureF() );
            
            return ( getBaseLifetimeOilTemperatureC() );
        }
        
        /**
         * Gets the offset to the base oil temperature for engine life time, where life time is halfed in °C.
         * 
         * @return the offset to the base oil temperature for engine life time, where life time is halfed in °C.
         */
        public abstract float getHalfLifetimeOilTempOffsetC();
        
        /**
         * Gets the offset to the base oil temperature for engine life time, where life time is halfed in °F.
         * 
         * @return the offset to the base oil temperature for engine life time, where life time is halfed in °F.
         */
        public final float getHalfLifetimeOilTempOffsetF()
        {
            return ( Convert.celsius2Fahrehheit( getHalfLifetimeOilTempOffsetC() ) );
        }
        
        /**
         * Gets the offset to the base oil temperature for engine life time, where life time is halfed in the units selected in the PLR.
         * 
         * @return the offset to the base oil temperature for engine life time, where life time is halfed in the units selected in the PLR.
         */
        public final float getHalfLifetimeOilTempOffset()
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getHalfLifetimeOilTempOffsetF() );
            
            return ( getHalfLifetimeOilTempOffsetC() );
        }
        
        /**
         * Gets the optimum oil temperature in Celsius. Engine will operatate optimally at this value.
         * 
         * @return the optimum oil temperature.
         */
        public abstract float getOptimumOilTemperatureC();
        
        /**
         * Gets the optimum oil temperature in Fahrenheit. Engine will operatate optimally at this value.
         * 
         * @return the optimum oil temperature.
         */
        public final float getOptimumOilTemperatureF()
        {
            return ( Convert.celsius2Fahrehheit( getOptimumOilTemperatureC() ) );
        }
        
        /**
         * Gets the optimum oil temperature in the units selected in the PLR. Engine will operatate optimally at this value.
         * 
         * @return the optimum oil temperature.
         */
        public final float getOptimumOilTemperature()
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getOptimumOilTemperatureF() );
            
            return ( getOptimumOilTemperatureC() );
        }
        
        /**
         * Gets the temperature value in Celsius at which the engine starts to overheat.
         * This value should serve as a peak level for temperatures during a race.
         * 
         * @return the temperature value at which the engine starts to overheat.
         */
        public abstract float getOverheatingOilTemperatureC();
        
        /**
         * Gets the temperature value in Fahrenheit at which the engine starts to overheat.
         * This value should serve as a peak level for temperatures during a race.
         * 
         * @return the temperature value at which the engine starts to overheat.
         */
        public final float getOverheatingOilTemperatureF()
        {
            return ( Convert.celsius2Fahrehheit( getOverheatingOilTemperatureC() ) );
        }
        
        /**
         * Gets the temperature value in the units selected in the PLR, at which the engine starts to overheat.
         * This value should serve as a peak level for temperatures during a race.
         * 
         * @return the temperature value at which the engine starts to overheat.
         */
        public final float getOverheatingOilTemperature()
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getOverheatingOilTemperatureF() );
            
            return ( getOverheatingOilTemperatureC() );
        }
        
        /**
         * Gets a strong overheating engine temperature in Celsius. At this level the engine will have half of its regular life time.
         * 
         * @return a strong overheating engine temperature.
         */
        public abstract float getStrongOverheatingOilTemperatureC();
        
        /**
         * Gets a strong overheating engine temperature in Fahrenheit. At this level the engine will have half of its regular life time.
         * 
         * @return a strong overheating engine temperature.
         */
        public final float getStrongOverheatingOilTemperatureF()
        {
            return ( Convert.celsius2Fahrehheit( getStrongOverheatingOilTemperatureC() ) );
        }
        
        /**
         * Gets a strong overheating engine temperature in the units selected in the PLR. At this level the engine will have half of its regular life time.
         * 
         * @return a strong overheating engine temperature.
         */
        public final float getStrongOverheatingOilTemperature()
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getStrongOverheatingOilTemperatureF() );
            
            return ( getStrongOverheatingOilTemperatureC() );
        }
        
        /**
         * Gets the engine wear increase per °C obove the optimum temperature.
         * 
         * @return the engine wear increase per °C obove the optimum temperature.
         */
        public abstract float getWearIncreasePerDegreeC();
        
        /**
         * Gets RPM for 'normal' lifetime. No decreased and no increased lifetime.
         * 
         * @see #getHalfLifetimeRPMOffset()
         * 
         * @return RPM for 'normal' lifetime. No decreased and no increased lifetime.
         */
        public abstract float getBaseLifetimeRPM();
        
        /**
         * Gets the offset above the base RPM, where lifetime is halfed.
         * 
         * @see #getBaseLifetimeRPM()
         * 
         * @return the offset above the base RPM, where lifetime is halfed.
         */
        public abstract float getHalfLifetimeRPMOffset();
        
        /**
         * Gets the range of possible max revs.
         * 
         * @see VehicleSetup.Engine#getRevLimitSetting()
         * @see VehicleSetup.Engine#getRevLimit()
         * 
         * @return the range of possible max revs.
         */
        public abstract PhysicsSetting getRevLimitRange();
        
        /**
         * Gets the range of possible boost mappings.
         * 
         * @see VehicleSetup.Engine#getBoostMapping()
         * 
         * @return the range of possible boost mappings.
         */
        public abstract PhysicsSetting getBoostRange();
        
        /**
         * Gets the rev limit increase per boost setting.
         * 
         * @return the rev limit increase per boost setting.
         */
        public abstract float getRPMIncreasePerBoostLevel();
        
        /**
         * Gets the fuel usage increase per boost setting.
         * 
         * @return the fuel usage increase per boost setting.
         */
        public abstract float getFuelUsageIncreasePerBoostLevel();
        
        /**
         * Gets the engine wear increase per boost setting.
         * 
         * @return the engine wear increase per boost setting.
         */
        public abstract float getWearIncreasePerBoostLevel();
        
        /**
         * Gets the engine wear increase per km/h.
         * 
         * @return the engine wear increase per km/h.
         */
        public abstract float getWearIncreasePerVelocity();
        
        /**
         * Gets the maximum RPM at the given boost level.
         * 
         * @param baseMaxRPM maxRPM coming from {@link TelemetryData#getEngineBaseMaxRPM()}
         * @param boostLevel coming from {@link TelemetryData#getEngineBoostMapping()}
         * 
         * @return the maximum RPM at the given boost level.
         */
        public float getMaxRPM( float baseMaxRPM, int boostLevel )
        {
            /*
            if ( rpmIncreasePerBoostSetting <= 0f )
                return ( baseMaxRPM );
            
            return ( baseMaxRPM + ( boostRange.getValueForSetting( boostLevel ) - boostRange.getBaseValue() ) * rpmIncreasePerBoostSetting );
            */
            return ( baseMaxRPM + ( boostLevel - getBoostRange().getBaseValue() ) * getRPMIncreasePerBoostLevel() );
        }
        
        /**
         * Gets the maximum RPM at the highest (valued) boost mapping.
         * 
         * @param baseMaxRPM maxRPM coming from {@link TelemetryData#getEngineBaseMaxRPM()}
         * 
         * @return the maximum RPM at the highest (valued) boost mapping.
         */
        public float getMaxRPM( float baseMaxRPM )
        {
            if ( getRPMIncreasePerBoostLevel() <= 0f )
                return ( baseMaxRPM );
            
            return ( baseMaxRPM + ( getBoostRange().getMaxValue() - getBoostRange().getBaseValue() ) * getRPMIncreasePerBoostLevel() );
        }
        
        /**
         * Gets the boost level with smallest boost.
         * 
         * @return the boost level with smallest boost.
         */
        public int getLowestBoostLevel()
        {
            if ( getRPMIncreasePerBoostLevel() <= 0f )
                return ( (int)getBoostRange().getMaxValue() );
            
            return ( (int)getBoostRange().getMinValue() );
        }
        
        /**
         * Gets the boost level with biggest boost.
         * 
         * @return the boost level with biggest boost.
         */
        public int getHighestBoostLevel()
        {
            if ( getRPMIncreasePerBoostLevel() <= 0f )
                return ( (int)getBoostRange().getMinValue() );
            
            return ( (int)getBoostRange().getMaxValue() );
        }
        
        protected Engine()
        {
        }
    }
    
    /**
     * Get engine related physics parameters.
     * 
     * @return engine related physics parameters.
     */
    public abstract Engine getEngine();
    
    /**
     * Model of brake physics parameters.
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static abstract class Brakes
    {
        /**
         * Gets the range of possible brake distribution values.
         * 
         * @return the range of possible brake distribution values.
         */
        public abstract PhysicsSetting getRearDistributionRange();
        
        /**
         * Gets the range for brake pressure values [0, 1].
         * 
         * @see VehicleSetup.Controls#getBrakePressure()
         * 
         * @return the range for brake pressure values [0, 1].
         */
        // TODO: Provide in IMPERIAL units, too.
        public abstract PhysicsSetting getPressureRange();
        
        /**
         * Brake settings for a single wheel.
         * 
         * @author Marvin Froehlich (CTDP)
         */
        public static abstract class WheelBrake
        {
            private final Wheel wheel;
            
            protected abstract MeasurementUnits getMeasurementUnits();
            
            public final Wheel getWheel()
            {
                return ( wheel );
            }
            
            /**
             * Gets the lower bound of the temperature range in Kelvin, where brakes will operate optimally.
             * 
             * @return the lower bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesLowerBoundK()
            {
                return ( getOptimumTemperaturesLowerBoundC() - Convert.ZERO_KELVIN );
            }
            
            /**
             * Gets the lower bound of the temperature range in Celsius, where brakes will operate optimally.
             * 
             * @return the lower bound of the temperature range, where brakes will operate optimally.
             */
            public abstract float getOptimumTemperaturesLowerBoundC();
            
            /**
             * Gets the lower bound of the temperature range in Fahrenheit, where brakes will operate optimally.
             * 
             * @return the lower bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesLowerBoundF()
            {
                return ( Convert.celsius2Fahrehheit( getOptimumTemperaturesLowerBoundC() ) );
            }
            
            /**
             * Gets the lower bound of the temperature range in the selected units, where brakes will operate optimally.
             * 
             * @return the lower bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesLowerBound()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getOptimumTemperaturesLowerBoundF() );
                
                return ( getOptimumTemperaturesLowerBoundC() );
            }
            
            /**
             * Gets the upper bound of the temperature range in Kelvin, where brakes will operate optimally.
             * 
             * @return the upper bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesUpperBoundK()
            {
                return ( getOptimumTemperaturesUpperBoundC() - Convert.ZERO_KELVIN );
            }
            
            /**
             * Gets the upper bound of the temperature range in Celsius, where brakes will operate optimally.
             * 
             * @return the upper bound of the temperature range, where brakes will operate optimally.
             */
            public abstract float getOptimumTemperaturesUpperBoundC();
            
            /**
             * Gets the upper bound of the temperature range in Fahrenheit, where brakes will operate optimally.
             * 
             * @return the upper bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesUpperBoundF()
            {
                return ( Convert.celsius2Fahrehheit( getOptimumTemperaturesUpperBoundC() ) );
            }
            
            /**
             * Gets the upper bound of the temperature range in the selected units, where brakes will operate optimally.
             * 
             * @return the upper bound of the temperature range, where brakes will operate optimally.
             */
            public final float getOptimumTemperaturesUpperBound()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getOptimumTemperaturesUpperBoundF() );
                
                return ( getOptimumTemperaturesUpperBoundC() );
            }
            
            /**
             * Gets the temperature level in Kelvin under and at which brakes are cold and won't work well.
             * 
             * @return the temperature level under and at which brakes are cold.
             */
            public final float getColdTemperatureK()
            {
                return ( getColdTemperatureC() - Convert.ZERO_KELVIN );
            }
            
            /**
             * Gets the temperature level in Celsius under and at which brakes are cold and won't work well.
             * 
             * @return the temperature level under and at which brakes are cold.
             */
            public abstract float getColdTemperatureC();
            
            /**
             * Gets the temperature level in Fahrenheit under and at which brakes are cold and won't work well.
             * 
             * @return the temperature level under and at which brakes are cold.
             */
            public final float getColdTemperatureF()
            {
                return ( Convert.celsius2Fahrehheit( getColdTemperatureC() ) );
            }
            
            /**
             * Gets the temperature level in the selected units under and at which brakes are cold and won't work well.
             * 
             * @return the temperature level under and at which brakes are cold.
             */
            public final float getColdTemperature()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getColdTemperatureF() );
                
                return ( getColdTemperatureC() );
            }
            
            /**
             * Gets the temperature level in Kelvin above at at which brakes are overheating and won't work well and increase more than regularly.
             * 
             * @return the temperature level above at at which brakes are overheating.
             */
            public final float getOverheatingTemperatureK()
            {
                return ( getOverheatingTemperatureC() - Convert.ZERO_KELVIN );
            }
            
            /**
             * Gets the temperature level in Celsius above at at which brakes are overheating and won't work well and increase more than regularly.
             * 
             * @return the temperature level above at at which brakes are overheating.
             */
            public abstract float getOverheatingTemperatureC();
            
            /**
             * Gets the temperature level in Fahrenheit above at at which brakes are overheating and won't work well and increase more than regularly.
             * 
             * @return the temperature level above at at which brakes are overheating.
             */
            public final float getOverheatingTemperatureF()
            {
                return ( Convert.celsius2Fahrehheit( getOverheatingTemperatureC() ) );
            }
            
            /**
             * Gets the temperature level in the selected units above at at which brakes are overheating and won't work well and increase more than regularly.
             * 
             * @return the temperature level above at at which brakes are overheating.
             */
            public final float getOverheatingTemperature()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getOverheatingTemperatureF() );
                
                return ( getOverheatingTemperatureC() );
            }
            
            /**
             * Gets the engine wear increase per °C over optimum temperature range.
             * 
             * @see #getOptimumTemperaturesUpperBoundC()
             * 
             * @return the engine wear increase per °C over optimum temperature range.
             */
            public abstract float getWearIncreasePerDegreeCOverOptimum();
            
            /**
             * Gets the engine wear increase per °C below optimum temperature range.
             * 
             * @see #getOptimumTemperaturesLowerBoundC()
             * 
             * @return the engine wear increase per °C below optimum temperature range.
             */
            public abstract float getWearDecreasePerDegreeCBelowOptimum();
            
            /**
             * Gets the range, where brakes start to fade.
             * 
             * @return the range, where brakes start to fade.
             */
            public abstract float getBrakeFadeRangeC();
            
            /**
             * Gets the temperature below optimum in °C, where brakes are cold and only produce half of optimum effect.
             * 
             * @see #getOptimumTemperaturesLowerBoundC()
             * 
             * @return the temperature below optimum in °C, where brakes are cold and only produce half of optimum effect.
             */
            public float getBrakeFadeColdTemperatureC()
            {
                return ( getOptimumTemperaturesLowerBound() - getBrakeFadeRangeC() );
            }
            
            /**
             * Gets the temperature above optimum in °C, where brakes are cold and only produce half of optimum effect.
             * 
             * @see #getOptimumTemperaturesUpperBoundC()
             * 
             * @return the temperature above optimum in °C, where brakes are cold and only produce half of optimum effect.
             */
            public float getBrakeFadeHotTemperatureC()
            {
                return ( getOptimumTemperaturesUpperBound() + getBrakeFadeRangeC() );
            }
            
            /**
             * Gets the disc thickness range in meters.
             * 
             * @see VehicleSetup.WheelAndTire#getBrakeDiscThickness()
             * 
             * @return the disc thickness range in meters.
             */
            public abstract PhysicsSetting getDiscRange();
            
            /**
             * Brake disc wear per second in optimum temperature range.
             * 
             * @see #getOptimumTemperaturesLowerBoundC()
             * @see #getOptimumTemperaturesUpperBoundC()
             * 
             * @return Brake disc wear per second in optimum temperature range.
             */
            public abstract double getWearRate();
            
            /**
             * Gets the disc thickness in meters at which it fails.
             * 
             * @return the disc thickness in meters at which it fails.
             */
            public abstract float getDiscFailureAverage();
            
            /**
             * Gets the variance of disc thickness at which it fails.
             * 
             * @return the variance of disc thickness at which it fails.
             */
            public abstract float getDiscFailureVariance();
            
            /**
             * Gets, whether this brake has a variance in fail thickness.
             * 
             * @return whether this brake has a variance in fail thickness.
             */
            public abstract boolean hasDiscFailureVariance();
            
            /**
             * Gets minimum disc thickness, the disc can possibly reach in meters.
             * 
             * @return minimum disc thickness, the disc can possibly reach in meters
             */
            public float getMinDiscFailure()
            {
                return ( getDiscFailureAverage() - getDiscFailureVariance() - getDiscFailureVariance() );
                //return ( getDiscFailureAverage() );
            }
            
            /**
             * Gets the disc thickness in meters, that you can be pretty sure, it will reach.
             * 
             * @return the disc thickness in meters, that you can be pretty sure, it will reach.
             */
            public abstract float getGoodDiscFailure();
            
            /**
             * You shouldn't let your brake disc go to this thickness in meters.
             * 
             * @return the thickness, that you shouldn't let your brakes go to. They will probably not gonna make it.
             */
            public float getBadDiscFailure()
            {
                return ( getDiscFailureAverage() + getDiscFailureVariance() );
            }
            
            /**
             * Gets the disc thickness at which it fails.
             * 
             * @return the disc thickness at which it fails.
             */
            public float getMaxDiscFailure()
            {
                return ( getDiscFailureAverage() + getDiscFailureVariance() + getDiscFailureVariance() );
            }
            
            /**
             * Gets the absolute range of variance in meters.
             * 
             * @return the absolute range of variance in meters.
             */
            public float getDiscFailureVarianceRange()
            {
                return ( getDiscFailureVariance() + getDiscFailureVariance() + getDiscFailureVariance() + getDiscFailureVariance() );
            }
            
            /**
             * Gets half of the absolute range of variance in meters.
             * 
             * @return half of the absolute range of variance in meters.
             */
            public float getDiscFailureVarianceHalfRange()
            {
                return ( getDiscFailureVariance() + getDiscFailureVariance() );
            }
            
            /**
             * Gets brake torque base.
             * 
             * @return brake torque base.
             */
            public abstract float getTorqueBase();
            
            /**
             * Computes the actual brake torque for the given brake temperature.
             * 
             * @param brakeTempK the brake temperature in Kelvin
             * 
             * @return the actual brake torque for the given brake temperature.
             */
            public abstract double computeTorque( float brakeTempK );
            
            protected void reset()
            {
            }
            
            protected void finish()
            {
            }
            
    		protected WheelBrake( Wheel wheel )
    		{
    		    this.wheel = wheel;
    		}
        }
        
        /**
         * Gets the brake model of the given wheel.
         * 
         * @param wheel the requested wheel
         * 
         * @return the brake model of the given wheel.
         */
        public abstract WheelBrake getBrake( Wheel wheel );
        
        protected void reset()
        {
        }
        
        protected void finish()
        {
        }
    }
    
    /**
     * Gets the model for all four brakes.
     * 
     * @return the model for all four brakes.
     */
    public abstract Brakes getBrakes();
    
    /**
     * Model of a tire compound.
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static abstract class TireCompound
    {
        /**
         * Gets the compound's name.
         * 
         * @return the compound's name.
         */
        public abstract String getName();
        
        /**
         * Gets the compound's index in the list. This is what stands in the setup file.
         * 
         * @return the compound's index in the list.
         */
        public abstract int getIndex();
        
        /**
         * Model of one wheel of a compound. There will always be one {@link CompoundWheel} in a {@link TireCompound} for each wheel of the vehicle.
         * 
         * @author Marvin Froehlich (CTDP)
         */
        public static abstract class CompoundWheel
        {
            private final Wheel wheel;
            
            protected abstract MeasurementUnits getMeasurementUnits();
            
            public final Wheel getWheel()
            {
                return ( wheel );
            }
            
            /**
             * Gets the lateral grip value for dry weather.
             * Effective grip will always be a fraction of this value depending on tire wear, temperatures, pressure and load.
             * 
             * @return the lateral grip value for dry weather.
             */
            public abstract float getDryLateralGrip();
            
            /**
             * Gets the longitudinal grip value for dry weather.
             * Effective grip will always be a fraction of this value depending on tire wear, temperatures, pressure and load.
             * 
             * @return the longitudinal grip value for dry weather.
             */
            public abstract float getDryLongitudinalGrip();
            
            /**
             * Gets the lower bound of the temperature window (in Celsius), in which the tire will operate optimally.
             * 
             * @return the lower bound of the temperature window (in Celsius), in which the tire will operate optimally.
             */
            public abstract float getOptimumTemperatureCLowerBound();
            
            /**
             * Gets the upper bound of the temperature window (in Celsius), in which the tire will operate optimally.
             * 
             * @return the upper bound of the temperature window (in Celsius), in which the tire will operate optimally.
             */
            public abstract float getOptimumTemperatureCUpperBound();
            
            /**
             * Gets the temperature value (in Celsius), at which the tire will operate optimally.
             * 
             * @return the temperature value (in Celsius), at which the tire will operate optimally.
             * 
             * @deprecated Replaced by {@link #getOptimumTemperatureCLowerBound()} and {@link #getOptimumTemperatureCUpperBound()}.
             */
            @Deprecated
            public final float getOptimumTemperatureC()
            {
                return ( getOptimumTemperatureCLowerBound() + ( ( getOptimumTemperatureCUpperBound() - getOptimumTemperatureCLowerBound() ) / 2 ) );
            }
            
            /**
             * Gets the lower bound of the temperature window (in Fahrenheit), in which the tire will operate optimally.
             * 
             * @return the lower bound of the temperature window (in Fahrenheit), in which the tire will operate optimally.
             */
            public final float getOptimumTemperatureFLowerBound()
            {
                return ( Convert.celsius2Fahrehheit( getOptimumTemperatureCLowerBound() ) );
            }
            
            /**
             * Gets the upper bound of the temperature window (in Fahrenheit), in which the tire will operate optimally.
             * 
             * @return the upper bound of the temperature window (in Fahrenheit), in which the tire will operate optimally.
             */
            public final float getOptimumTemperatureFUpperBound()
            {
                return ( Convert.celsius2Fahrehheit( getOptimumTemperatureCUpperBound() ) );
            }
            
            /**
             * Gets the temperature value (in Fahrenheit), at which the tire will operate optimally.
             * 
             * @return the temperature value (in Fahrenheit), at which the tire will operate optimally.
             * 
             * @deprecated Replaced by {@link #getOptimumTemperatureFLowerBound()} and {@link #getOptimumTemperatureFUpperBound()}.
             */
            @Deprecated
            public final float getOptimumTemperatureF()
            {
                return ( Convert.celsius2Fahrehheit( getOptimumTemperatureC() ) );
            }
            
            /**
             * Gets the lower bound of the temperature window (in the selected units), in which the tire will operate optimally.
             * 
             * @return the lower bound of the temperature window (in the selected units), in which the tire will operate optimally.
             */
            public final float getOptimumTemperatureLowerBound()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getOptimumTemperatureFLowerBound() );
                
                return ( getOptimumTemperatureCLowerBound() );
            }
            
            /**
             * Gets the upper bound of the temperature window (in the selected units), in which the tire will operate optimally.
             * 
             * @return the upper bound of the temperature window (in the selected units), in which the tire will operate optimally.
             */
            public final float getOptimumTemperatureUpperBound()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getOptimumTemperatureFUpperBound() );
                
                return ( getOptimumTemperatureCUpperBound() );
            }
            
            /**
             * Gets the temperature value (in the selected units), at which the tire will operate optimally.
             * 
             * @return the temperature value (in the selected units), at which the tire will operate optimally.
             * 
             * @deprecated Replaced by {@link #getOptimumTemperatureLowerBound()} and {@link #getOptimumTemperatureUpperBound()}.
             */
            @Deprecated
            public final float getOptimumTemperature()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getOptimumTemperatureF() );
                
                return ( getOptimumTemperatureC() );
            }
            
            /**
             * Gets the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             * 
             * @return the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             */
            public abstract float getGripLossPerDegreeCBelowOptimum();
            
            /**
             * Gets the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             * 
             * @return the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             */
            public final float getGripLossPerDegreeFBelowOptimum()
            {
                return ( Convert.celsius2Fahrehheit( getGripLossPerDegreeCBelowOptimum() ) );
            }
            
            /**
             * Gets the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             * 
             * @return the grip loss (fraction) per degree below {@link #getOptimumTemperature()}.
             */
            public final float getGripLossPerDegreeBelowOptimum()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getGripLossPerDegreeFBelowOptimum() );
                
                return ( getGripLossPerDegreeCBelowOptimum() );
            }
            
            /**
             * Gets the temperature in Celsius, that a tire will have at the given grip fraction value.
             * This function will always return a value below {@link #getOptimumTemperature()}.
             * 
             * @param grip the actual grip fraction
             * 
             * @return the temperature, that a tire will have at the given grip fraction value.
             */
            public abstract float getBelowTemperatureC( float grip );
            
            /**
             * Gets the temperature in Fahrenheit, that a tire will have at the given grip fraction value.
             * This function will always return a value below {@link #getOptimumTemperature()}.
             * 
             * @param grip the actual grip fraction
             * 
             * @return the temperature, that a tire will have at the given grip fraction value.
             */
            public final float getBelowTemperatureF( float grip )
            {
                return ( Convert.celsius2Fahrehheit( getBelowTemperatureC( grip ) ) );
            }
            
            /**
             * Gets the temperature in the selected units, that a tire will have at the given grip fraction value.
             * This function will always return a value below {@link #getOptimumTemperature()}.
             * 
             * @param grip the actual grip fraction
             * 
             * @return the temperature, that a tire will have at the given grip fraction value.
             */
            public final float getBelowTemperature( float grip )
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getBelowTemperatureF( grip ) );
                
                return ( getBelowTemperatureC( grip ) );
            }
            
            /**
             * Gets the temperature in Celsius, that a tire will have at the given grip fraction value.
             * This function will always return a value above {@link #getOptimumTemperature()}.
             * 
             * @param grip the actual grip fraction
             * 
             * @return the temperature, that a tire will have at the given grip fraction value.
             */
            public abstract float getAboveTemperatureC( float grip );
            
            /**
             * Gets the temperature in Fahrenheit, that a tire will have at the given grip fraction value.
             * This function will always return a value above {@link #getOptimumTemperature()}.
             * 
             * @param grip the actual grip fraction
             * 
             * @return the temperature, that a tire will have at the given grip fraction value.
             */
            public final float getAboveTemperatureF( float grip )
            {
                return ( Convert.celsius2Fahrehheit( getAboveTemperatureC( grip ) ) );
            }
            
            /**
             * Gets the temperature in the selected units, that a tire will have at the given grip fraction value.
             * This function will always return a value above {@link #getOptimumTemperature()}.
             * 
             * @param grip the actual grip fraction
             * 
             * @return the temperature, that a tire will have at the given grip fraction value.
             */
            public final float getAboveTemperature( float grip )
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getAboveTemperatureF( grip ) );
                
                return ( getAboveTemperatureC( grip ) );
            }
            
            /**
             * Gets the grip fraction value of the tire at the given average temperature.
             * 
             * @param avgTemperatureC average over outer, mittle and inner temperature
             * 
             * @return the grip fraction value of the tire at the given average temperature.
             */
            public abstract float getGripFactorByTemperatureC( float avgTemperatureC );
            
            /**
             * Gets the optimum tire pressure at the given tire load.
             * 
             * @param load coming from {@link TelemetryData#getTireLoad(Wheel)}
             * 
             * @return the optimum tire pressure at the given tire load.
             */
            public abstract float getOptimumPressure( float load );
            
            /*
             * Computes the optimum tire pressure for the given grip fraction and load.
             * 
             * @param grip the actual grip fraction
             * @param load coming from {@link TelemetryData#getTireLoad(Wheel)}
             * 
             * @return the optimum tire pressure for the given grip fraction and load.
             */
            //public abstract float getPressureForGrip( float grip, float load );
            
            /**
             * Computes the grip fraction of the tire at the given pressure and load.
             * 
             * @param pressure coming from {@link TelemetryData#getTirePressureKPa(Wheel)}
             * @param load coming from {@link TelemetryData#getTireLoadN(Wheel)}
             * 
             * @return the grip fraction of the tire at the given pressure and load.
             */
            public abstract float getGripFactorByPressure( float pressure, float load );
            
            /**
             * Computes the fraction of maximum grip at the given wear, average temperature, pressure and load.
             * 
             * @param wear see {@link TelemetryData#getTireWear(Wheel)} and {@link #getWearGripFactor(float)}
             * @param avgTemperatureC average over outer, mittle and inner temperature
             * @param pressure coming from {@link TelemetryData#getTirePressureKPa(Wheel)}
             * @param load coming from {@link TelemetryData#getTireLoadN(Wheel)}
             * 
             * @return the fraction of maximum grip at the given wear, average temperature, pressure and load.
             */
            public abstract float getGripFraction( float wear, float avgTemperatureC, float pressure, float load );
            
            /**
             * Selects the grip fraction at the given wear level.
             * 
             * @param wear coming from {@link TelemetryData#getTireWear(Wheel)}
             * 
             * @return the grip fraction at the given wear level.
             */
            public abstract float getWearGripFactor( float wear );
            
            /**
             * Gets the grip level at maximum tire wear.
             * 
             * @return the grip level at maximum tire wear.
             */
            public abstract float getMinGrip();
            
            protected CompoundWheel( Wheel wheel )
            {
                this.wheel = wheel;
            }
        }
        
        /**
         * Gets the {@link CompoundWheel} for the given wheel.
         * 
         * @param wheel the requested wheel
         * 
         * @return the {@link CompoundWheel} for the given wheel.
         */
        public abstract CompoundWheel getWheel( Wheel wheel );
        
        protected TireCompound()
        {
        }
    }
    
    /**
     * Gets the number of available {@link TireCompound}s.
     * 
     * @return the number of available {@link TireCompound}s.
     */
    public abstract int getNumTireCompounds();
    
    /**
     * Gets the {@link TireCompound} by the given index.
     * 
     * @param index zero-based
     * 
     * @return the {@link TireCompound} by the given index.
     */
    public abstract TireCompound getTireCompound( int index );
    
    /**
     * Gets the {@link TireCompound} for the given wheel, that has the best grip compared to allother available ones.
     * 
     * @param wheel the requested wheel
     * 
     * @return the {@link TireCompound} for the given wheel, that has the best grip.
     */
    public abstract TireCompound getTireCompoundBestGrip( Wheel wheel );
    
    /**
     * Gets the range of possible values for tire pressure.
     * 
     * @param wheel the requested wheel
     * 
     * @see VehicleSetup.WheelAndTire#getTirePressure()
     * 
     * @return the range of possible values for tire pressure.
     */
    public abstract PhysicsSetting getTirePressureRange( Wheel wheel );
    
    public static abstract class UpgradeIdentifier
    {
        public abstract String getDescription();
    }
    
    /**
     * Gets the list of installed upgrades.
     * 
     * @return the list of installed upgrades or <code>null</code> if no upgrades are installed.
     */
    public abstract UpgradeIdentifier[] getInstalledUpgrades();
    
    protected void reset()
    {
        getEngine().reset();
        getBrakes().reset();
    }
    
    protected void finish()
    {
        getEngine().finish();
        getBrakes().finish();
    }
    
    protected abstract void applyMeasurementUnitsImpl( MeasurementUnits measurementUnits );
    
    protected final void applyMeasurementUnits( MeasurementUnits measurementUnits )
    {
        this.fuelRange = new PhysicsSetting( ( measurementUnits == MeasurementUnits.IMPERIAL ) ? Convert.LITERS_TO_GALONS : 1f, 0f );
        this.fuelRange.set( this.getFuelRangeL() );
        this.fuelRangeLUpdateId = this.getFuelRangeL().updateId;
        
        applyMeasurementUnitsImpl( measurementUnits );
    }
    
    protected VehiclePhysics()
    {
    }
}
