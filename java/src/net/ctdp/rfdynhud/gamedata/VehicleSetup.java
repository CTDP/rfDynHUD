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
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits.Convert;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound;

/**
 * This class models all possible settings of a car setup.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class VehicleSetup
{
    boolean updatedInTimeScope = false;
    
    protected void setUpdatedInTimeScope()
    {
        this.updatedInTimeScope = true;
    }
    
    /**
     * Gets, whether the last update of these data has been done while in running session resp. realtime mode.
     * @return whether the last update of these data has been done while in running session resp. realtime mode.
     */
    public final boolean isUpdatedInTimeScope()
    {
        return ( updatedInTimeScope );
    }
    
    /**
     * 
     * @param timestamp
     */
    void onSessionStarted( long timestamp )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * 
     * @param timestamp
     */
    void onSessionEnded( long timestamp )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * 
     * @param timestamp
     */
    void onCockpitEntered( long timestamp )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * 
     * @param timestamp
     */
    void onCockpitExited( long timestamp )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * Model of the general part of the setup
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static abstract class General
    {
        protected abstract MeasurementUnits getMeasurementUnits();
        
        /**
         * GENERAL::FrontTireCompoundSetting=7//04-Hot
         * 
         * @return the front tire compound.
         */
        public abstract TireCompound getFrontTireCompound();
        
        /**
         * GENERAL::RearTireCompoundSetting=7//04-Hot
         * 
         * @return the rear tire compound.
         */
        public abstract TireCompound getRearTireCompound();
        
        /**
         * GENERAL::NumPitstopsSetting=2//2
         * 
         * @return the number of planned pit stops.
         */
        public abstract int getNumPitstops();
        
        /**
         * Gets fuel in liters.
         * 
         * GENERAL::FuelSetting=94//100L (13laps)
         * GENERAL::Pitstop1Setting=71//75L (10laps)
         * GENERAL::Pitstop2Setting=60//64L (8laps)
         * GENERAL::Pitstop3Setting=48//N/A
         * 
         * @param pitstop pitstop number. (0 for starting fuel).
         * 
         * @return the fuel setting for the given pitstop number.
         * 
         * @see #getNumPitstops()
         */
        public abstract float getFuelL( int pitstop );
        
        /**
         * Gets fuel in galons.
         * 
         * GENERAL::FuelSetting=94//100L (13laps)
         * GENERAL::Pitstop1Setting=71//75L (10laps)
         * GENERAL::Pitstop2Setting=60//64L (8laps)
         * GENERAL::Pitstop3Setting=48//N/A
         * 
         * @param pitstop pitstop number. (0 for starting fuel).
         * 
         * @return the fuel setting for the given pitstop number.
         * 
         * @see #getNumPitstops()
         */
        public final float getFuelGal( int pitstop )
        {
            return ( getFuelL( pitstop ) * Convert.LITERS_TO_GALONS );
        }
        
        /**
         * Gets fuel in the units selected in the PLR.
         * 
         * GENERAL::FuelSetting=94//100L (13laps)
         * GENERAL::Pitstop1Setting=71//75L (10laps)
         * GENERAL::Pitstop2Setting=60//64L (8laps)
         * GENERAL::Pitstop3Setting=48//N/A
         * 
         * @param pitstop pitstop number. (0 for starting fuel).
         * 
         * @return the fuel setting for the given pitstop number.
         * 
         * @see #getNumPitstops()
         */
        public final float getFuel( int pitstop )
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getFuelGal( pitstop ) );
            
            return ( getFuelL( pitstop ) );
        }
        
        /**
         * FRONTWING::FWSetting=64//65
         * 
         * @return the front wing level.
         */
        public abstract float getFrontWing();
        
        protected General()
        {
        }
    }
    
    /**
     * Gets an interface to the general part of the setup.
     * 
     * @return an interface to the general part of the setup.
     */
    public abstract General getGeneral();
    
    /**
     * Model of the controls part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static abstract class Controls
    {
        /**
         * CONTROLS::RearBrakeSetting=85//55.0:45.0
         * 
         * @return the fraction of brake power weighted to the front.
         */
        public abstract float getRearBrakeBalance();
        
        /**
         * CONTROLS::BrakePressureSetting=40//100%
         * 
         * @see VehiclePhysics.Brakes#getPressureRange()
         * 
         * @return the fraction of brake pressure.
         */
        public abstract float getBrakePressure();
    }
    
    /**
     * Gets an interface to the settings of the control part of the setup.
     * 
     * @return an interface to the settings of the control part of the setup.
     */
    public abstract Controls getControls();
    
    /**
     * Model of the engine part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static abstract class Engine
    {
        /**
         * Gets the rev limit setting.
         * 
         * @see VehiclePhysics.Engine#getRevLimitRange()
         * 
         * @return the rev limit setting.
         */
        public abstract int getRevLimitSetting();
        
        /**
         * ENGINE::RevLimitSetting=0//20,000
         * 
         * @return the absolute rev limit.
         */
        public abstract float getRevLimit();
        
        /**
         * ENGINE::EngineBoostSetting=4//5
         * 
         * @return the engine boost mapping. See {@link VehiclePhysics.Engine#getBoostRange()} for the range.
         */
        public abstract int getBoostMapping();
    }
    
    /**
     * Gets an interface to the settings of the engine part of the setup.
     * 
     * @return an interface to the settings of the engine part of the setup.
     */
    public abstract Engine getEngine();
    
    /**
     * Model of the wheel and tire part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static abstract class WheelAndTire
    {
        private final Wheel wheel;
        
        public final Wheel getWheel()
        {
            return ( wheel );
        }
        
        protected abstract MeasurementUnits getMeasurementUnits();
        
        /**
         * tire pressure in kPa
         * 
         * FRONTLEFT::PressureSetting=25//120 kPa
         * 
         * @see VehiclePhysics#getTirePressureRange(Wheel)
         * 
         * @return the initial tire pressure at mount time and room temperature in kPa.
         */
        public abstract float getTirePressureKPa();
        
        /**
         * tire pressure in PSI.
         * 
         * FRONTLEFT::PressureSetting=25//120 kPa
         * 
         * @see VehiclePhysics#getTirePressureRange(Wheel)
         * 
         * @return the initial tire pressure at mount time and room temperature in PSI.
         */
        public final float getTirePressurePSI()
        {
            return ( getTirePressureKPa() * Convert.KPA_TO_PSI );
        }
        
        /**
         * Gets initial tire pressure in the units selected in the PLR.
         * 
         * FRONTLEFT::PressureSetting=25//120 kPa
         * 
         * @see VehiclePhysics#getTirePressureRange(Wheel)
         * 
         * @return the initial tire pressure at mount time and room temperature in the selected units.
         */
        public final float getTirePressure()
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getTirePressurePSI() );
            
            return ( getTirePressureKPa() );
        }
        
        /**
         * brake disc thickness in meters
         * 
         * FRONTLEFT::BrakeDiscSetting=5//2.8 cm
         * 
         * @see VehiclePhysics.Brakes.WheelBrake#getDiscRange()
         * 
         * @return the thickness of the brake disc in meters.
         */
        public abstract float getBrakeDiscThicknessM();
        
        /**
         * brake disc thickness in inch
         * 
         * FRONTLEFT::BrakeDiscSetting=5//2.8 cm
         * 
         * @see VehiclePhysics.Brakes.WheelBrake#getDiscRange()
         * 
         * @return the thickness of the brake disc in inch.
         */
        public final float getBrakeDiscThicknessIn()
        {
            return ( getBrakeDiscThicknessM() * Convert.M_TO_INCH );
        }
        
        /**
         * brake disc thickness in the units selected in the PLR.
         * 
         * FRONTLEFT::BrakeDiscSetting=5//2.8 cm
         * 
         * @see VehiclePhysics.Brakes.WheelBrake#getDiscRange()
         * 
         * @return the thickness of the brake disc in the selected units.
         */
        public final float getBrakeDiscThickness()
        {
            if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                return ( getBrakeDiscThicknessIn() );
            
            return ( getBrakeDiscThicknessM() );
        }
        
        protected WheelAndTire( Wheel wheel )
        {
            this.wheel = wheel;
        }
    }
    
    /**
     * Gets an interface to the settings of the wheel and tire, suspension and brake disc for the passed wheel.
     * 
     * @param wheel the requested wheel
     * 
     * @return an interface to the settings of the wheel and tire, suspension and brake disc for the passed tire.
     */
    public abstract WheelAndTire getWheelAndTire( Wheel wheel );
    
    protected abstract void applyEditorPresets( EditorPresets editorPresets );
    
    protected abstract void applyMeasurementUnits( MeasurementUnits measurementUnits );
    
    protected abstract boolean checkLoadPhysicsAndSetupOnSessionStarted( LiveGameData gameData, boolean isEditorMode );
    
    protected VehicleSetup()
    {
    }
}
