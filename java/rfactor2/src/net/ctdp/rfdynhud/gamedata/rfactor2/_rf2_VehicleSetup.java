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
package net.ctdp.rfdynhud.gamedata.rfactor2;

import java.io.File;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits.Convert;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.rfactor2._rf2_VehiclePhysics._rf2_TireCompound;

/**
 * This class models all possible settings of a car setup.
 * 
 * @author Marvin Froehlich (CTDP)
 */
@SuppressWarnings( "unused" )
class _rf2_VehicleSetup extends VehicleSetup
{
    private static final float convert_N_m_s_to_LBS_in_s( int value )
    {
        return ( value * Convert.N_TO_LBS / Convert.MM_TO_INCH );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUpdatedInTimeScope()
    {
        super.setUpdatedInTimeScope();
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
    public static class _rf2_General extends General
    {
        private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
        
        /*
        float weightDistributionLeft; // GENERAL::CGRightSetting=8//50.0:50.0
        float weightDistributionFront; // GENERAL::CGRearSetting=12//46.3:53.7
        
        float wedge; // GENERAL::WedgeSetting=0//0.00 turns
        */
        
        _rf2_TireCompound frontTireCompound; // GENERAL::FrontTireCompoundSetting=7//04-Hot
        _rf2_TireCompound rearTireCompound; // GENERAL::RearTireCompoundSetting=7//04-Hot
        
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected MeasurementUnits getMeasurementUnits()
        {
            return ( measurementUnits );
        }
        
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
         * {@inheritDoc}
         */
        @Override
        public final _rf2_TireCompound getFrontTireCompound()
        {
            return ( frontTireCompound );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final _rf2_TireCompound getRearTireCompound()
        {
            return ( rearTireCompound );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
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
         * {@inheritDoc}
         */
        @Override
        public final float getFuelL( int pitstop )
        {
            return ( fuel[pitstop] );
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
         * {@inheritDoc}
         */
        @Override
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
        
        _rf2_General()
        {
            super();
        }
    }
    
    final _rf2_General general = new _rf2_General();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf2_General getGeneral()
    {
        return ( general );
    }
    
    private static class _rf2_Suspension
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
        
        public static class _rf2_ThirdSpring
        {
            private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
            
            private float packer; // SUSPENSION::Front3rdPackerSetting=9//1.0 cm
            private int springRate; // SUSPENSION::Front3rdSpringSetting=30//60 N/mm
            private int slowBump; // SUSPENSION::Front3rdSlowBumpSetting=15//2062 N/m/s
            private int slowRebound; // SUSPENSION::Front3rdSlowReboundSetting=13//4062 N/m/s
            private int fastBump; // SUSPENSION::Front3rdFastBumpSetting=15//1031 N/m/s
            private int fastRebound; // SUSPENSION::Front3rdFastReboundSetting=15//2344 N/m/s
            
            protected MeasurementUnits getMeasurementUnits()
            {
                return ( measurementUnits );
            }
            
            /**
             * Gets the packer size in cm.
             * 
             * SUSPENSION::Front3rdPackerSetting=9//1.0 cm
             * SUSPENSION::Rear3rdPackerSetting=19//2.0 cm
             * 
             * @return the packer size in cm.
             */
            public final float getPackerCM()
            {
                return ( packer );
            }
            
            /**
             * Gets the packer in inch.
             * 
             * SUSPENSION::Front3rdPackerSetting=9//1.0 cm
             * SUSPENSION::Rear3rdPackerSetting=19//2.0 cm
             * 
             * @return the packer size in inch.
             */
            public final float getPackerIN()
            {
                return ( packer * Convert.CM_TO_INCH );
            }
            
            /**
             * Gets packer in the units selected in the PLR.
             * 
             * SUSPENSION::Front3rdPackerSetting=9//1.0 cm
             * SUSPENSION::Rear3rdPackerSetting=19//2.0 cm
             * 
             * @return the packer size in the selected units.
             */
            public final float getPacker()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getPackerIN() );
                
                return ( getPackerCM() );
            }
            
            /**
             * spring rate in N/mm
             * 
             * SUSPENSION::Front3rdSpringSetting=30//60 N/mm
             * SUSPENSION::Rear3rdSpringSetting=25//50 N/mm
             * 
             * @return the spring rate in N/mm
             */
            public final int getSpringRateNmm()
            {
                return ( springRate );
            }
            
            /**
             * spring rate in lbs/in
             * 
             * SUSPENSION::Front3rdSpringSetting=30//60 N/mm
             * SUSPENSION::Rear3rdSpringSetting=25//50 N/mm
             * 
             * @return the spring rate in lbs/in
             */
            public final int getSpringRateLBSin()
            {
                return ( (int)( springRate * Convert.N_TO_LBS / Convert.MM_TO_INCH ) );
            }
            
            /**
             * Gets spring rate in the units selected in the PLR.
             * 
             * SUSPENSION::Front3rdSpringSetting=30//60 N/mm
             * SUSPENSION::Rear3rdSpringSetting=25//50 N/mm
             * 
             * @return the spring rate in the selected units.
             */
            public final int getSpringRate()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getSpringRateLBSin() );
                
                return ( getSpringRateNmm() );
            }
            
            /**
             * slow bump in N/m/s
             * 
             * SUSPENSION::Front3rdSlowBumpSetting=15//2062 N/m/s
             * SUSPENSION::Rear3rdSlowBumpSetting=15//2062 N/m/s
             * 
             * @return the slow bump settings in N/m/s.
             */
            public final int getSlowBumpNms()
            {
                return ( slowBump );
            }
            
            /**
             * slow bump in LBS/in/s
             * 
             * SUSPENSION::Front3rdSlowBumpSetting=15//2062 N/m/s
             * SUSPENSION::Rear3rdSlowBumpSetting=15//2062 N/m/s
             * 
             * @return the slow bump settings in LBS/in/s.
             */
            public final float getSlowBumpLBSIns()
            {
                return ( convert_N_m_s_to_LBS_in_s( slowBump ) );
            }
            
            /**
             * slow bump in the units selected in the PLR.
             * 
             * SUSPENSION::Front3rdSlowBumpSetting=15//2062 N/m/s
             * SUSPENSION::Rear3rdSlowBumpSetting=15//2062 N/m/s
             * 
             * @return the slow bump settings in the selected units.
             */
            public final float getSlowBump()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getSlowBumpLBSIns() );
                
                return ( getSlowBumpNms() );
            }
            
            /**
             * slow rebound in N/m/s
             * 
             * SUSPENSION::Front3rdSlowReboundSetting=13//4062 N/m/s
             * SUSPENSION::Rear3rdSlowReboundSetting=15//4688 N/m/s
             * 
             * @return the slow rebound settings in N/m/s.
             */
            public final int getSlowReboundNms()
            {
                return ( slowRebound );
            }
            
            /**
             * slow rebound in LBS/in/s
             * 
             * SUSPENSION::Front3rdSlowReboundSetting=13//4062 N/m/s
             * SUSPENSION::Rear3rdSlowReboundSetting=15//4688 N/m/s
             * 
             * @return the slow rebound settings in LBS/in/s.
             */
            public final float getSlowReboundLBSIns()
            {
                return ( convert_N_m_s_to_LBS_in_s( slowBump ) );
            }
            
            /**
             * slow rebound in the units selected in the PLR.
             * 
             * SUSPENSION::Front3rdSlowReboundSetting=13//4062 N/m/s
             * SUSPENSION::Rear3rdSlowReboundSetting=15//4688 N/m/s
             * 
             * @return the slow rebound settings in the selected units.
             */
            public final float getSlowRebound()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getSlowReboundLBSIns() );
                
                return ( getSlowReboundNms() );
            }
            
            /**
             * fast bump in N/m/s
             * 
             * SUSPENSION::Front3rdFastBumpSetting=15//1031 N/m/s
             * SUSPENSION::Rear3rdFastBumpSetting=15//1031 N/m/s
             * 
             * @return the fast bump settings in N/m/s.
             */
            public final int getFastBumpNms()
            {
                return ( fastBump );
            }
            
            /**
             * fast bump in LBS/in/s
             * 
             * SUSPENSION::Front3rdFastBumpSetting=15//1031 N/m/s
             * SUSPENSION::Rear3rdFastBumpSetting=15//1031 N/m/s
             * 
             * @return the fast bump settings in LBS/in/s.
             */
            public final float getFastBumpLBSIns()
            {
                return ( convert_N_m_s_to_LBS_in_s( fastBump ) );
            }
            
            /**
             * fast bump in the units selected in the PLR.
             * 
             * SUSPENSION::Front3rdFastBumpSetting=15//1031 N/m/s
             * SUSPENSION::Rear3rdFastBumpSetting=15//1031 N/m/s
             * 
             * @return the fast bump settings the selected units.
             */
            public final float getFastBump()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getFastBumpLBSIns() );
                
                return ( getFastBumpNms() );
            }
            
            /**
             * fast rebound in N/m/s
             * 
             * SUSPENSION::Front3rdFastReboundSetting=15//2344 N/m/s
             * SUSPENSION::Rear3rdFastReboundSetting=15//2344 N/m/s
             * 
             * @return the fast rebound settings in N/m/s.
             */
            public final int getFastReboundNms()
            {
                return ( fastRebound );
            }
            
            /**
             * fast rebound in LBS/in/s
             * 
             * SUSPENSION::Front3rdFastReboundSetting=15//2344 N/m/s
             * SUSPENSION::Rear3rdFastReboundSetting=15//2344 N/m/s
             * 
             * @return the fast rebound settings in LBS/in/s.
             */
            public final float getFastReboundLBSIns()
            {
                return ( convert_N_m_s_to_LBS_in_s( fastRebound ) );
            }
            
            /**
             * fast rebound in the units selected in the PLR.
             * 
             * SUSPENSION::Front3rdFastReboundSetting=15//2344 N/m/s
             * SUSPENSION::Rear3rdFastReboundSetting=15//2344 N/m/s
             * 
             * @return the fast rebound settings in the selected units.
             */
            public final float getFastRebound()
            {
                if ( getMeasurementUnits() == MeasurementUnits.IMPERIAL )
                    return ( getFastReboundLBSIns() );
                
                return ( getFastReboundNms() );
            }
            
            _rf2_ThirdSpring()
            {
                super();
            }
        }
        
        private final _rf2_ThirdSpring front3rdSpring = new _rf2_ThirdSpring();
        private final _rf2_ThirdSpring rear3rdSpring = new _rf2_ThirdSpring();
        
        /**
         * Gets an interface to the settings for the front third spring.
         * 
         * @return an interface to the settings for the front third spring.
         */
        public final _rf2_ThirdSpring getFront3dSpring()
        {
            return ( front3rdSpring );
        }
        
        /**
         * Gets an interface to the settings for the rear third spring.
         * 
         * @return an interface to the settings for the rear third spring.
         */
        public final _rf2_ThirdSpring getRear3dSpring()
        {
            return ( rear3rdSpring );
        }
        
        _rf2_Suspension()
        {
            super();
        }
    }
    
    private final _rf2_Suspension suspension = new _rf2_Suspension();
    
    /*
     * {@inheritDoc}
     *
    @Override
    public final _rf2_Suspension getSuspension()
    {
        return ( suspension );
    }
    */
    
    /**
     * Model of the controls part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static class _rf2_Controls extends Controls
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
         * {@inheritDoc}
         */
        @Override
        public final float getRearBrakeBalance()
        {
            return ( rearBrakeBalance );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
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
    
    final _rf2_Controls controls = new _rf2_Controls();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf2_Controls getControls()
    {
        return ( controls );
    }
    
    /**
     * Model of the engine part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static class _rf2_Engine extends Engine
    {
        int revLimitSetting; // ENGINE::RevLimitSetting=0//20,000
        float revLimit; // ENGINE::RevLimitSetting=0//20,000
        int boostMapping; // ENGINE::EngineBoostSetting=4//5
        //int engineBrakeMap; // ENGINE::EngineBrakingMapSetting=4//4
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final int getRevLimitSetting()
        {
            return ( revLimitSetting );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public final float getRevLimit()
        {
            return ( revLimit );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
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
    
    final _rf2_Engine engine = new _rf2_Engine();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf2_Engine getEngine()
    {
        return ( engine );
    }
    
    private static class _rf2_GearBox
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
    
    //private final _rf2_GearBox gearBox = new _rf2_GearBox();
    
    /*
     * 
     * @return an interface to the settings of the gear box.
     *
    public final _rf2_GearBox getGearBox()
    {
        return ( gearBox );
    }
    */
    
    private static class _rf2_Differential
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
    
    //private final _rf2_Differential differential = new _rf2_Differential();
    
    /*
     * @return an interface to the settings of the differential.
     *
    public final _rf2_Differential getDifferential()
    {
        return ( differential );
    }
    */
    
    /**
     * Model of the wheel and tire part of the setup
     * 
     * @author Marvin Froehlich
     */
    public static class _rf2_WheelAndTire extends WheelAndTire
    {
        private MeasurementUnits measurementUnits = MeasurementUnits.METRIC;
        
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
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected MeasurementUnits getMeasurementUnits()
        {
            return ( measurementUnits );
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
         * {@inheritDoc}
         */
        @Override
        public final float getTirePressureKPa()
        {
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
         * {@inheritDoc}
         */
        @Override
        public final float getBrakeDiscThicknessM()
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
        
        _rf2_WheelAndTire( Wheel wheel )
        {
            super( wheel );
        }
    }
    
    final _rf2_WheelAndTire flWheelAndTire = new _rf2_WheelAndTire( Wheel.FRONT_LEFT );
    final _rf2_WheelAndTire frWheelAndTire = new _rf2_WheelAndTire( Wheel.FRONT_RIGHT );
    final _rf2_WheelAndTire rlWheelAndTire = new _rf2_WheelAndTire( Wheel.REAR_LEFT );
    final _rf2_WheelAndTire rrWheelAndTire = new _rf2_WheelAndTire( Wheel.REAR_RIGHT );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final _rf2_WheelAndTire getWheelAndTire( Wheel wheel )
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyEditorPresets( EditorPresets editorPresets )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyMeasurementUnits( MeasurementUnits measurementUnits )
    {
        this.general.measurementUnits = measurementUnits;
        this.suspension.front3rdSpring.measurementUnits = measurementUnits;
        this.suspension.rear3rdSpring.measurementUnits = measurementUnits;
        this.flWheelAndTire.measurementUnits = measurementUnits;
        this.frWheelAndTire.measurementUnits = measurementUnits;
        this.rlWheelAndTire.measurementUnits = measurementUnits;
        this.rrWheelAndTire.measurementUnits = measurementUnits;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkLoadPhysicsAndSetupOnSessionStarted( LiveGameData gameData, boolean isEditorMode )
    {
        boolean loadPhysicsAndSetup = true;
        
        // TODO
        /*
        if ( isEditorMode )
        {
            File cchFile = ( (_rf2_ProfileInfo)gameData.getProfileInfo() ).getCCHFile();
            File playerVEHFile = ( (_rf2_ProfileInfo)gameData.getProfileInfo() ).getVehicleFile();
            String trackName = gameData.getTrackInfo().getTrackName();
            File setupFile = gameData.getFileSystem().locateSetupFile( gameData );
            
            if ( ( cchFile == null ) || !cchFile.exists() )
                loadPhysicsAndSetup = false;
            
            if ( ( playerVEHFile == null ) || !playerVEHFile.exists() )
                loadPhysicsAndSetup = false;
            
            if ( ( trackName == null ) || trackName.equals( "" ) )
                loadPhysicsAndSetup = false;
            
            if ( ( setupFile == null ) || !setupFile.exists() )
                loadPhysicsAndSetup = false;
        }
        */
        
        return ( loadPhysicsAndSetup );
    }
    
    _rf2_VehicleSetup()
    {
        super();
    }
}
