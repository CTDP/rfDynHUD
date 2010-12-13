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

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound;

/**
 * This class models all possible settings of a car setup.
 * 
 * @author Marvin Froehlich (CTDP)
 */
@SuppressWarnings( "unused" )
public class VehicleSetup
{
    boolean updatedInTimeScope = false;
    
    /**
     * Gets, whether the last update of these data has been done while in running session resp. realtime mode.
     * @return whether the last update of these data has been done while in running session resp. realtime mode.
     */
    public final boolean isUpdatedInTimeScope()
    {
        return ( updatedInTimeScope );
    }
    
    void onSessionStarted()
    {
        this.updatedInTimeScope = false;
    }
    
    void onSessionEnded()
    {
        this.updatedInTimeScope = false;
    }
    
    void onRealtimeEntered()
    {
        this.updatedInTimeScope = false;
    }
    
    void onRealtimeExited()
    {
        this.updatedInTimeScope = false;
    }
    
    //private boolean symmetric; // GENERAL::Symmetric=1
    
    /*
     * GENERAL::Symmetric=1
     * 
     * @return whether the setup is symmetric.
     *
    public final boolean isSymmetric()
    {
        return ( symmetric );
    }
    */
    
    /**
     * Model of the general part of the setup
     * 
     * @author Marvin Froehlich (CTDP)
     */
    public static class General
    {
        private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
        
        /*
        float weightDistributionLeft; // GENERAL::CGRightSetting=8//50.0:50.0
        float weightDistributionFront; // GENERAL::CGRearSetting=12//46.3:53.7
        
        float wedge; // GENERAL::WedgeSetting=0//0.00 turns
        */
        
        TireCompound frontTireCompound; // GENERAL::FrontTireCompoundSetting=7//04-Hot
        TireCompound rearTireCompound; // GENERAL::RearTireCompoundSetting=7//04-Hot
        
        int numPitstops; // GENERAL::NumPitstopsSetting=2//2
        float[] fuel; // GENERAL::FuelSetting=94//100L (13laps)
                      // GENERAL::Pitstop1Setting=71//75L (10laps)
                      // GENERAL::Pitstop2Setting=60//64L (8laps)
                      // GENERAL::Pitstop3Setting=48//N/A
        
        /*
        float leftFenderFlare; // LEFTFENDER::FenderFlareSetting=0//1
        float rightFenderFlare; // RIGHTFENDER::FenderFlareSetting=0//1
        */
        
        float frontWing; // FRONTWING::FWSetting=64//65
        //private float rearWing; // REARWING::RWSetting=62//63
        
        /*
        int radiatorSize; // BODYAERO::RadiatorSetting=3//4
        int brakeDuctSize; // BODYAERO::BrakeDuctSetting=2//3
        */
        
        /*
         * GENERAL::CGRightSetting=8//50.0:50.0
         * 
         * @return the fraction of weight, distributed to the left.
         *
        public final float getWeightDistributionLeft()
        {
            return ( weightDistributionLeft );
        }
        
        /*
         * GENERAL::CGRearSetting=12//46.3:53.7
         * 
         * @return the fraction of weight, distributed to the front.
         *
        public final float getWeightDistributionFront()
        {
            return ( weightDistributionFront );
        }
        
        /*
         * GENERAL::WedgeSetting=0//0.00 turns
         * 
         * @return the wedge turns.
         *
        public final float getWedge()
        {
            return ( wedge );
        }
        */
        
        /**
         * GENERAL::FrontTireCompoundSetting=7//04-Hot
         * 
         * @return the front tire compound.
         */
        public final TireCompound getFrontTireCompound()
        {
            return ( frontTireCompound );
        }
        
        /**
         * GENERAL::RearTireCompoundSetting=7//04-Hot
         * 
         * @return the rear tire compound.
         */
        public final TireCompound getRearTireCompound()
        {
            return ( rearTireCompound );
        }
        
        /**
         * GENERAL::NumPitstopsSetting=2//2
         * 
         * @return the number of planned pit stops.
         */
        public final int getNumPitstops()
        {
            return ( numPitstops );
        }
        
        void applyFuel( float fuel, int pitstop )
        {
            if ( this.fuel == null )
            {
                this.fuel = new float[ pitstop + 1 ];
            }
            else if ( this.fuel.length < pitstop + 1 )
            {
                float[] tmp = new float[ pitstop + 1 ];
                System.arraycopy( this.fuel, 0, tmp, 0, this.fuel.length );
                this.fuel = tmp;
            }
            
            this.fuel[pitstop] = fuel;
        }
        
        /**
         * Get fuel in liters.
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
        public final float getFuelL( int pitstop )
        {
            return ( fuel[pitstop] );
        }
        
        /**
         * Get fuel in galons.
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
            return ( getFuelL( pitstop ) * MeasurementUnits.Convert.LITERS_TO_GALONS );
        }
        
        /**
         * Get fuel in the units selected in the PLR.
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
            if ( measurementUnits == MeasurementUnits.IMPERIAL )
                return ( getFuelGal( pitstop ) );
            
            return ( getFuelL( pitstop ) );
        }
        
        /*
         * LEFTFENDER::FenderFlareSetting=0//1
         * 
         * @return the left fender flare.
         *
        public final float getLeftFenderFlare()
        {
            return ( leftFenderFlare );
        }
        
        /*
         * RIGHTFENDER::FenderFlareSetting=0//1
         * 
         * @return the right fender flare.
         *
        public final float getRightFenderFlare()
        {
            return ( rightFenderFlare );
        }
        */
        
        /**
         * FRONTWING::FWSetting=64//65
         * 
         * @return the front wing level.
         */
        public final float getFrontWing()
        {
            return ( frontWing );
        }
        
        /*
         * REARWING::RWSetting=62//63
         * 
         * @return the rear wing level.
         *
        public final float getRearWing()
        {
            return ( rearWing );
        }
        
        /*
         * BODYAERO::RadiatorSetting=3//4
         * 
         * @return the size of the main radiator.
         *
        public final int getRadiatorSize()
        {
            return ( radiatorSize );
        }
        
        /*
         * BODYAERO::BrakeDuctSetting=2//3
         * 
         * @return the size of the brake duct.
         *
        public final int getBrakeDuctSize()
        {
            return ( brakeDuctSize );
        }
        */
    }
    
    final General general = new General();
    
    /**
     * Gets an interface to the general part of the setup.
     * 
     * @return an interface to the general part of the setup.
     */
    public final General getGeneral()
    {
        return ( general );
    }
    
    private static class Suspension
    {
        private int frontAntiSwayBar; // SUSPENSION::FrontAntiSwaySetting=40//86 N/mm
        private int rearAntiSwayBar; // SUSPENSION::FrontAntiSwaySetting=40//86 N/mm
        
        private float frontToeIn; // SUSPENSION::FrontToeInSetting=36//-0.10 deg
        private float rearToeIn; // SUSPENSION::RearToeInSetting=14//0.10 deg
        
        private float leftCaster; // SUSPENSION::LeftCasterSetting=25//3.5 deg
        private float rightCaster; // SUSPENSION::RightCasterSetting=25//3.5 deg
        
        private float leftTrackBar; // SUSPENSION::LeftTrackBarSetting=0//0.0 cm
        private float rightTrackBar; // SUSPENSION::RightTrackBarSetting=0//0.0 cm
        
        /**
         * SUSPENSION::FrontAntiSwaySetting=40//86 N/mm
         * 
         * @return the setting for the front anti sway bar.
         */
        public final int getFrontAntiSwayBar()
        {
            return ( frontAntiSwayBar );
        }
        
        /**
         * SUSPENSION::FrontAntiSwaySetting=40//86 N/mm
         * 
         * @return the setting for the rear anti sway bar.
         */
        public final int getRearAntiSwayBar()
        {
            return ( rearAntiSwayBar );
        }
        
        /**
         * SUSPENSION::FrontToeInSetting=36//-0.10 deg
         * 
         * @return the settings for the front toe in in degrees.
         */
        public final float getFrontToeIn()
        {
            return ( frontToeIn );
        }
        
        /**
         * SUSPENSION::RearToeInSetting=14//0.10 deg
         * 
         * @return the settings for the rear toe in in degrees.
         */
        public final float getRearToeIn()
        {
            return ( rearToeIn );
        }
        
        /**
         * SUSPENSION::LeftCasterSetting=25//3.5 deg
         * 
         * @return the setting for the left caster in degrees.
         */
        public final float getLeftCaster()
        {
            return ( leftCaster );
        }
        
        /**
         * SUSPENSION::RightCasterSetting=25//3.5 deg
         * 
         * @return the setting for the right caster in degrees.
         */
        public final float getRightCaster()
        {
            return ( rightCaster );
        }
        
        /**
         * SUSPENSION::LeftTrackBarSetting=0//0.0 cm
         * 
         * @return the setting for the left track bar in cm.
         */
        public final float getLeftTrackBar()
        {
            return ( leftTrackBar );
        }
        
        /**
         * SUSPENSION::RightTrackBarSetting=0//0.0 cm
         * 
         * @return the setting for the right track bar in cm.
         */
        public final float getRightTrackBar()
        {
            return ( rightTrackBar );
        }
        
        public static class ThirdSpring
        {
            private float packer; // SUSPENSION::Front3rdPackerSetting=9//1.0 cm
            private int springRate; // SUSPENSION::Front3rdSpringSetting=30//60 N/mm
            private int slowBump; // SUSPENSION::Front3rdSlowBumpSetting=15//2062 N/m/s
            private int fastBump; // SUSPENSION::Front3rdFastBumpSetting=15//1031 N/m/s
            private int slowRebound; // SUSPENSION::Front3rdSlowReboundSetting=13//4062 N/m/s
            private int fastRebound; // SUSPENSION::Front3rdFastReboundSetting=15//2344 N/m/s
            
            /**
             * SUSPENSION::Front3rdPackerSetting=9//1.0 cm
             * SUSPENSION::Rear3rdPackerSetting=19//2.0 cm
             * 
             * @return the packer size in cm.
             */
            public final float getPacker()
            {
                return ( packer );
            }
            
            /**
             * SUSPENSION::Front3rdSpringSetting=30//60 N/mm
             * SUSPENSION::Rear3rdSpringSetting=25//50 N/mm
             * 
             * @return the spring rate in N/mm
             */
            public final int getSpringRate()
            {
                return ( springRate );
            }
            
            /**
             * SUSPENSION::Front3rdSlowBumpSetting=15//2062 N/m/s
             * SUSPENSION::Rear3rdSlowBumpSetting=15//2062 N/m/s
             * 
             * @return the slow bump settings in N/m/s
             */
            public final int getSlowBump()
            {
                return ( slowBump );
            }
            
            /**
             * SUSPENSION::Front3rdFastBumpSetting=15//1031 N/m/s
             * SUSPENSION::Rear3rdFastBumpSetting=15//1031 N/m/s
             * 
             * @return the fast bump settings in N/m/s
             */
            public final int getFastBump()
            {
                return ( fastBump );
            }
            
            /**
             * SUSPENSION::Front3rdSlowReboundSetting=13//4062 N/m/s
             * SUSPENSION::Rear3rdSlowReboundSetting=15//4688 N/m/s
             * 
             * @return the slow rebound settings in N/m/s
             */
            public final int getSlowRebound()
            {
                return ( slowRebound );
            }
            
            /**
             * SUSPENSION::Front3rdFastReboundSetting=15//2344 N/m/s
             * SUSPENSION::Rear3rdFastReboundSetting=15//2344 N/m/s
             * 
             * @return the fast rebound settings in N/m/s
             */
            public final int getFastRebound()
            {
                return ( fastRebound );
            }
        }
        
        private final ThirdSpring front3rdSpring = new ThirdSpring();
        private final ThirdSpring rear3rdSpring = new ThirdSpring();
        
        /**
         * Gets an interface to the settings for the front third spring.
         * 
         * @return an interface to the settings for the front third spring.
         */
        public final ThirdSpring getFront3dSpring()
        {
            return ( front3rdSpring );
        }
        
        /**
         * Gets an interface to the settings for the rear third spring.
         * 
         * @return an interface to the settings for the rear third spring.
         */
        public final ThirdSpring getRear3dSpring()
        {
            return ( rear3rdSpring );
        }
    }
    
    //private final Suspension suspension = new Suspension();
    
    /*
     * @return an interface to the settings for the suspension.
     * 
    public final Suspension getSuspension()
    {
        return ( suspension );
    }
    */
    
    /**
     * Model of the controls part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static class Controls
    {
        //float steeringLock; // CONTROLS::SteerLockSetting=23//17.5 deg
        float rearBrakeBalance; // CONTROLS::RearBrakeSetting=85//55.0:45.0
        float brakePressure; // CONTROLS::BrakePressureSetting=40//100%
        //float handbrakePressure; // CONTROLS::HandbrakePressSetting=0//0%
        
        /*
         * CONTROLS::SteerLockSetting=23//17.5 deg
         * 
         * @return the steering lock in degrees.
         *
        public final float getSteeringLock()
        {
            return ( steeringLock );
        }
        */
        
        /**
         * CONTROLS::RearBrakeSetting=85//55.0:45.0
         * 
         * @return the fraction of brake power weighted to the front.
         */
        public final float getRearBrakeBalance()
        {
            return ( rearBrakeBalance );
        }
        
        /**
         * CONTROLS::BrakePressureSetting=40//100%
         * 
         * @see VehiclePhysics.Brakes#getPressureRange()
         * 
         * @return the fraction of brake pressure.
         */
        public final float getBrakePressure()
        {
            return ( brakePressure );
        }
        
        /*
         * CONTROLS::HandbrakePressSetting=0//0%
         * 
         * @return the fraction of hand brake pressure.
         *
        public final float getHandbrakePressure()
        {
            return ( handbrakePressure );
        }
        */
    }
    
    final Controls controls = new Controls();
    
    /**
     * Gets an interface to the settings of the control part of the setup.
     * 
     * @return an interface to the settings of the control part of the setup.
     */
    public final Controls getControls()
    {
        return ( controls );
    }
    
    /**
     * Model of the engine part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static class Engine
    {
        int revLimitSetting; // ENGINE::RevLimitSetting=0//20,000
        float revLimit; // ENGINE::RevLimitSetting=0//20,000
        int boostMapping; // ENGINE::EngineBoostSetting=4//5
        //int engineBrakeMap; // ENGINE::EngineBrakingMapSetting=4//4
        
        /**
         * Gets the rev limit setting.
         * 
         * @see VehiclePhysics.Engine#getRevLimitRange()
         * 
         * @return the rev limit setting.
         */
        public final int getRevLimitSetting()
        {
            return ( revLimitSetting );
        }
        
        /**
         * ENGINE::RevLimitSetting=0//20,000
         * 
         * @return the absolute rev limit.
         */
        public final float getRevLimit()
        {
            return ( revLimit );
        }
        
        /**
         * ENGINE::EngineBoostSetting=4//5
         * 
         * @return the engine boost mapping. See {@link VehiclePhysics.Engine#getBoostRange()} for the range.
         */
        public final int getBoostMapping()
        {
            return ( boostMapping );
        }
        
        /*
         * ENGINE::EngineBrakingMapSetting=4//4
         * 
         * @return the setting for the engine brake map.
         *
        public final int getEngineBrakeMap()
        {
            return ( engineBrakeMap );
        }
        */
    }
    
    final Engine engine = new Engine();
    
    /**
     * Gets an interface to the settings of the engine part of the setup.
     * 
     * @return an interface to the settings of the engine part of the setup.
     */
    public final Engine getEngine()
    {
        return ( engine );
    }
    
    private static class GearBox
    {
        private float finalDrive; // DRIVELINE::FinalDriveSetting=5//10/60 (bevel 18/20)
        private float reverseGear; // DRIVELINE::ReverseSetting=2//11/38 (23.030)
        private int numGears;
        private float[] gears; // DRIVELINE::Gear1Setting=21//11/29 (17.576)
                               // DRIVELINE::Gear2Setting=41//14/30 (14.286)
                               // DRIVELINE::Gear3Setting=52//15/27 (12.000)
                               // DRIVELINE::Gear4Setting=64//18/28 (10.370)
                               // DRIVELINE::Gear5Setting=72//18/25 (9.259)
                               // DRIVELINE::Gear6Setting=80//20/25 (8.333)
                               // DRIVELINE::Gear7Setting=90//21/24 (7.619)
        
        /**
         * DRIVELINE::FinalDriveSetting=5//10/60 (bevel 18/20)
         * 
         * @return the 'final drive' setting.
         */
        public final float getFinalDrive()
        {
            return ( finalDrive );
        }
        
        /**
         * DRIVELINE::ReverseSetting=2//11/38 (23.030)
         * 
         * @return the setting for the reverse gear.
         */
        public final float getReverseGear()
        {
            return ( reverseGear );
        }
        
        /**
         * Gets the number of gears.
         * 
         * @return the number of gears.
         */
        public final int getNumGears()
        {
            return ( numGears );
        }
        
        private void applyGear( float gearValue, int gear )
        {
            if ( this.gears == null )
            {
                this.gears = new float[ gear ];
            }
            else if ( this.gears.length < gear )
            {
                float[] tmp = new float[ gear ];
                System.arraycopy( this.gears, 0, tmp, 0, this.gears.length );
                this.gears = tmp;
            }
            
            this.gears[gear - 1] = gearValue;
        }
        
        /**
         * DRIVELINE::Gear1Setting=21//11/29 (17.576)
         * DRIVELINE::Gear2Setting=41//14/30 (14.286)
         * DRIVELINE::Gear3Setting=52//15/27 (12.000)
         * DRIVELINE::Gear4Setting=64//18/28 (10.370)
         * DRIVELINE::Gear5Setting=72//18/25 (9.259)
         * DRIVELINE::Gear6Setting=80//20/25 (8.333)
         * DRIVELINE::Gear7Setting=90//21/24 (7.619)
         * DRIVELINE::Gear8Setting=
         * DRIVELINE::Gear9Setting=
         * 
         * @param gear one-based gear index
         * 
         * @return the setting for the passed gear.
         */
        public final float getGear( int gear )
        {
            return ( gears[gear - 1] );
        }
    }
    
    //private final GearBox gearBox = new GearBox();
    
    /*
     * 
     * @return an interface to the settings of the gear box.
     *
    public final GearBox getGearBox()
    {
        return ( gearBox );
    }
    */
    
    private static class Differential
    {
        private float pump; // DRIVELINE::DiffPumpSetting=30//30%
        private float power; // DRIVELINE::DiffPowerSetting=15//15%
        private float coast; // DRIVELINE::DiffCoastSetting=20//20%
        private int preload; // DRIVELINE::DiffPreloadSetting=5//6
        private float rearSplit; // DRIVELINE::RearSplitSetting=0// 0.0:100.0
        
        /**
         * DRIVELINE::DiffPumpSetting=30//30%
         * 
         * @return the pump setting as a fraction.
         */
        public final float getPump()
        {
            return ( pump );
        }
        
        /**
         * DRIVELINE::DiffPowerSetting=15//15%
         * 
         * @return the power setting as a fraction.
         */
        public final float getPower()
        {
            return ( power );
        }
        
        /**
         * DRIVELINE::DiffCoastSetting=20//20%
         * 
         * @return the coast setting as a fraction.
         */
        public final float getCoast()
        {
            return ( coast );
        }
        
        /**
         * DRIVELINE::DiffPreloadSetting=5//6
         * 
         * @return the setting for the preload.
         */
        public final int getPreload()
        {
            return ( preload );
        }
        
        /**
         * DRIVELINE::RearSplitSetting=0// 0.0:100.0
         * 
         * @return the weighted 'dingsbums' as a fraction... what ever... I don't know.
         */
        public final float getRearSplit()
        {
            return ( rearSplit );
        }
    }
    
    //private final Differential differential = new Differential();
    
    /*
     * @return an interface to the settings of the differential.
     *
    public final Differential getDifferential()
    {
        return ( differential );
    }
    */
    
    /**
     * Model of the wheel and tire part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static class WheelAndTire
    {
        private final Wheel wheel;
        
        //private float camber; // FRONTLEFT::CamberSetting=24//-3.3 deg
        private int tirePressure; // FRONTLEFT::PressureSetting=25//120 kPa
        /*
        float packer; // FRONTLEFT::PackerSetting=21//2.5 cm
        int springRate; // FRONTLEFT::SpringSetting=16//100 N/mm
        int springRubber; // FRONTLEFT::SpringRubberSetting=0//Detached
        float rideHeight; // FRONTLEFT::RideHeightSetting=11//3.0 cm
        
        int slowBump; // FRONTLEFT::SlowBumpSetting=15//4590 N/m/s
        int fastBump; // FRONTLEFT::FastBumpSetting=15//2295 N/m/s
        int slowRebound; // FRONTLEFT::SlowReboundSetting=14//9840 N/m/s
        int fastRebound; // FRONTLEFT::FastReboundSetting=15//5100 N/m/s
        */
        float brakeDiscThickness; // FRONTLEFT::BrakeDiscSetting=5//2.8 cm
        /*
        int brakePad; // FRONTLEFT::BrakePadSetting=2//3
        */
        
        public final Wheel getWheel()
        {
            return ( wheel );
        }
        
        /*
         * FRONTLEFT::CamberSetting=24//-3.3 deg
         *
        public final float getCamber()
        {
            return ( camber );
        }
        */
        
        /**
         * FRONTLEFT::PressureSetting=25//120 kPa
         * 
         * @see VehiclePhysics#getTirePressureRange(Wheel)
         * 
         * @return the initial tire pressure at mount time and room temperature in kPa
         */
        public final int getTirePressure()
        {
            // TODO: Provide in IMPERIAL units, too.
            return ( tirePressure );
        }
        
        /*
         * FRONTLEFT::PackerSetting=21//2.5 cm
         * 
         * @return the size of the packer in cm.
         *
        public final float getPacker()
        {
            return ( packer );
        }
        
        /*
         * FRONTLEFT::SpringSetting=16//100 N/mm
         * 
         * @return the spring rate in N/mm.
         *
        public final int getSpringRate()
        {
            return ( springRate );
        }
        
        /*
         * FRONTLEFT::SpringRubberSetting=0//Detached
         * 
         * @return the spring rubber setting.
         *
        public final int getSpringRubber()
        {
            return ( springRubber );
        }
        
        /*
         * FRONTLEFT::RideHeightSetting=11//3.0 cm
         * 
         * @return the ride height setting in cm.
         *
        public final float getRideHeight()
        {
            return ( rideHeight );
        }
        
        /*
         * FRONTLEFT::SlowBumpSetting=15//4590 N/m/s
         * 
         * @return the setting for the slow bump in N/m/s.
         *
        public final int getSlowBump()
        {
            return ( slowBump );
        }
        
        /*
         * FRONTLEFT::FastBumpSetting=15//2295 N/m/s
         * 
         * @return the setting for the fast bump in N/m/s.
         *
        public final int getFastBump()
        {
            return ( fastBump );
        }
        
        /*
         * FRONTLEFT::SlowReboundSetting=14//9840 N/m/s
         * 
         * @return the setting for the slow rebound in N/m/s.
         *
        public final int getSlowRebound()
        {
            return ( slowRebound );
        }
        
        /*
         * FRONTLEFT::FastReboundSetting=15//5100 N/m/s
         * 
         * @return the setting for the fast rebound in N/m/s.
         *
        public final int getFastRebound()
        {
            return ( fastRebound );
        }
        */
        
        /**
         * FRONTLEFT::BrakeDiscSetting=5//2.8 cm
         * 
         * @see VehiclePhysics.Brakes.WheelBrake#getDiscRange()
         * 
         * @return the thickness of the brake disc in meters.
         */
        public final float getBrakeDiscThickness()
        {
            return ( brakeDiscThickness );
        }
        
        /*
         * FRONTLEFT::BrakePadSetting=2//3
         * 
         * @return the brake pad setting.
         *
        public final int getBrakePad()
        {
            return ( brakePad );
        }
        */
        
        WheelAndTire( Wheel wheel )
        {
            this.wheel = wheel;
        }
    }
    
    final WheelAndTire flWheelAndTire = new WheelAndTire( Wheel.FRONT_LEFT );
    final WheelAndTire frWheelAndTire = new WheelAndTire( Wheel.FRONT_RIGHT );
    final WheelAndTire rlWheelAndTire = new WheelAndTire( Wheel.REAR_LEFT );
    final WheelAndTire rrWheelAndTire = new WheelAndTire( Wheel.REAR_RIGHT );
    
    /**
     * Gets an interface to the settings of the wheel and tire, suspension and brake disc for the passed wheel.
     * 
     * @param wheel the requested wheel
     * 
     * @return an interface to the settings of the wheel and tire, suspension and brake disc for the passed tire.
     */
    public final WheelAndTire getWheelAndTire( Wheel wheel )
    {
        switch ( wheel )
        {
            case FRONT_LEFT:
                return ( flWheelAndTire );
            case FRONT_RIGHT:
                return ( frWheelAndTire );
            case REAR_LEFT:
                return ( rlWheelAndTire );
            case REAR_RIGHT:
                return ( rrWheelAndTire );
        }
        
        // Unreachable code!
        return ( null );
    }
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
    }
    
    void applyMeasurementUnits( MeasurementUnits measurementUnits )
    {
        this.general.measurementUnits = measurementUnits;
    }
    
    VehicleSetup()
    {
    }
}
