package net.ctdp.rfdynhud.gamedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.TireCompound;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.RFactorTools;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * This class models all possible settings of a car setup.
 * 
 * @author Marvin Froehlich
 */
@SuppressWarnings( "unused" )
public class VehicleSetup
{
    //private boolean symmetric; // GENERAL::Symmetric=1
    
    /*
     * GENERAL::Symmetric=1
     *
    public final boolean isSymmetric()
    {
        return ( symmetric );
    }
    */
    
    public class General
    {
        /*
        private float weightDistributionLeft; // GENERAL::CGRightSetting=8//50.0:50.0
        private float weightDistributionFront; // GENERAL::CGRearSetting=12//46.3:53.7
        
        private float wedge; // GENERAL::WedgeSetting=0//0.00 turns
        */
        
        private TireCompound frontTireCompound; // GENERAL::FrontTireCompoundSetting=7//04-Hot
        private TireCompound rearTireCompound; // GENERAL::RearTireCompoundSetting=7//04-Hot
        
        private int numPitstops; // GENERAL::NumPitstopsSetting=2//2
        private float[] fuel; // GENERAL::FuelSetting=94//100L (13laps)
                              // GENERAL::Pitstop1Setting=71//75L (10laps)
                              // GENERAL::Pitstop2Setting=60//64L (8laps)
                              // GENERAL::Pitstop3Setting=48//N/A
        
        /*
        private float leftFenderFlare; // LEFTFENDER::FenderFlareSetting=0//1
        private float rightFenderFlare; // RIGHTFENDER::FenderFlareSetting=0//1
        */
        
        private float frontWing; // FRONTWING::FWSetting=64//65
        //private float rearWing; // REARWING::RWSetting=62//63
        
        /*
        private int radiatorSize; // BODYAERO::RadiatorSetting=3//4
        private int brakeDuctSize; // BODYAERO::BrakeDuctSetting=2//3
        */
        
        /*
         * GENERAL::CGRightSetting=8//50.0:50.0
         *
        public final float getWeightDistributionLeft()
        {
            return ( weightDistributionLeft );
        }
        
        /*
         * GENERAL::CGRearSetting=12//46.3:53.7
         *
        public final float getWeightDistributionFront()
        {
            return ( weightDistributionFront );
        }
        
        /*
         * GENERAL::WedgeSetting=0//0.00 turns
         *
        public final float getWedge()
        {
            return ( wedge );
        }
        */
        
        /**
         * GENERAL::FrontTireCompoundSetting=7//04-Hot
         */
        public final TireCompound getFrontTireCompound()
        {
            return ( frontTireCompound );
        }
        
        /**
         * GENERAL::RearTireCompoundSetting=7//04-Hot
         */
        public final TireCompound getRearTireCompound()
        {
            return ( rearTireCompound );
        }
        
        /**
         * GENERAL::NumPitstopsSetting=2//2
         */
        public final int getNumPitstops()
        {
            return ( numPitstops );
        }
        
        private void applyFuel( float fuel, int pitstop )
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
            return ( getFuelL( pitstop ) * TelemetryData.LITERS_TO_GALONS );
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
            switch ( RFactorTools.getMeasurementUnits() )
            {
                case IMPERIAL:
                    return ( getFuelGal( pitstop ) );
                case METRIC:
                default:
                    return ( getFuelL( pitstop ) );
            }
        }
        
        /*
         * LEFTFENDER::FenderFlareSetting=0//1
         *
        public final float getLeftFenderFlare()
        {
            return ( leftFenderFlare );
        }
        
        /*
         * RIGHTFENDER::FenderFlareSetting=0//1
         *
        public final float getRightFenderFlare()
        {
            return ( rightFenderFlare );
        }
        */
        
        /**
         * FRONTWING::FWSetting=64//65
         */
        public final float getFrontWing()
        {
            return ( frontWing );
        }
        
        /*
         * REARWING::RWSetting=62//63
         *
        public final float getRearWing()
        {
            return ( rearWing );
        }
        
        /*
         * BODYAERO::RadiatorSetting=3//4
         *
        public final int getRadiatorSize()
        {
            return ( radiatorSize );
        }
        
        /*
         * BODYAERO::BrakeDuctSetting=2//3
         *
        public final int getBrakeDuctSize()
        {
            return ( brakeDuctSize );
        }
        */
    }
    
    private final General general = new General();
    
    public final General getGeneral()
    {
        return ( general );
    }
    
    private class Suspension
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
         */
        public final int getFrontAntiSwayBar()
        {
            return ( frontAntiSwayBar );
        }
        
        /**
         * SUSPENSION::FrontAntiSwaySetting=40//86 N/mm
         */
        public final int getRearAntiSwayBar()
        {
            return ( rearAntiSwayBar );
        }
        
        /**
         * SUSPENSION::FrontToeInSetting=36//-0.10 deg
         */
        public final float getFrontToeIn()
        {
            return ( frontToeIn );
        }
        
        /**
         * SUSPENSION::RearToeInSetting=14//0.10 deg
         */
        public final float getRearToeIn()
        {
            return ( rearToeIn );
        }
        
        /**
         * SUSPENSION::LeftCasterSetting=25//3.5 deg
         */
        public final float getLeftCaster()
        {
            return ( leftCaster );
        }
        
        /**
         * SUSPENSION::RightCasterSetting=25//3.5 deg
         */
        public final float getRightCaster()
        {
            return ( rightCaster );
        }
        
        /**
         * SUSPENSION::LeftTrackBarSetting=0//0.0 cm
         */
        public final float getLeftTrackBar()
        {
            return ( leftTrackBar );
        }
        
        /**
         * SUSPENSION::RightTrackBarSetting=0//0.0 cm
         */
        public final float getRightTrackBar()
        {
            return ( rightTrackBar );
        }
        
        public class ThirdSpring
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
             */
            public final float getPacker()
            {
                return ( packer );
            }
            
            /**
             * SUSPENSION::Front3rdSpringSetting=30//60 N/mm
             * SUSPENSION::Rear3rdSpringSetting=25//50 N/mm
             */
            public final int getSpringRate()
            {
                return ( springRate );
            }
            
            /**
             * SUSPENSION::Front3rdSlowBumpSetting=15//2062 N/m/s
             * SUSPENSION::Rear3rdSlowBumpSetting=15//2062 N/m/s
             */
            public final int getSlowBump()
            {
                return ( slowBump );
            }
            
            /**
             * SUSPENSION::Front3rdFastBumpSetting=15//1031 N/m/s
             * SUSPENSION::Rear3rdFastBumpSetting=15//1031 N/m/s
             */
            public final int getFastBump()
            {
                return ( fastBump );
            }
            
            /**
             * SUSPENSION::Front3rdSlowReboundSetting=13//4062 N/m/s
             * SUSPENSION::Rear3rdSlowReboundSetting=15//4688 N/m/s
             */
            public final int getSlowRebound()
            {
                return ( slowRebound );
            }
            
            /**
             * SUSPENSION::Front3rdFastReboundSetting=15//2344 N/m/s
             * SUSPENSION::Rear3rdFastReboundSetting=15//2344 N/m/s
             */
            public final int getFastRebound()
            {
                return ( fastRebound );
            }
        }
        
        private final ThirdSpring font3dSpring = new ThirdSpring();
        private final ThirdSpring rear3dSpring = new ThirdSpring();
        
        public final ThirdSpring getFront3dSpring()
        {
            return ( font3dSpring );
        }
        
        public final ThirdSpring getRear3dSpring()
        {
            return ( rear3dSpring );
        }
    }
    
    //private final Suspension suspension = new Suspension();
    
    /*
     * 
     *
    public final Suspension getSuspension()
    {
        return ( suspension );
    }
    */
    
    public class Controls
    {
        //private float steeringLock; // CONTROLS::SteerLockSetting=23//17.5 deg
        private float rearBrakeBalance; // CONTROLS::RearBrakeSetting=85//55.0:45.0
        private float brakePressure; // CONTROLS::BrakePressureSetting=40//100%
        //private float handbrakePressure; // CONTROLS::HandbrakePressSetting=0//0%
        
        /*
         * CONTROLS::SteerLockSetting=23//17.5 deg
         *
        public final float getSteeringLock()
        {
            return ( steeringLock );
        }
        */
        
        /**
         * CONTROLS::RearBrakeSetting=85//55.0:45.0
         */
        public final float getRearBrakeBalance()
        {
            return ( rearBrakeBalance );
        }
        
        /**
         * CONTROLS::BrakePressureSetting=40//100%
         */
        public final float getBrakePressure()
        {
            return ( brakePressure );
        }
        
        /*
         * CONTROLS::HandbrakePressSetting=0//0%
         *
        public final float getHandbrakePressure()
        {
            return ( handbrakePressure );
        }
        */
    }
    
    private final Controls controls = new Controls();
    
    /**
     * 
     */
    public final Controls getControls()
    {
        return ( controls );
    }
    
    public class Engine
    {
        private int revLimitSetting; // ENGINE::RevLimitSetting=0//20,000
        private float revLimit; // ENGINE::RevLimitSetting=0//20,000
        private int boostMapping; // ENGINE::EngineBoostSetting=4//5
        //private int engineBrakeMap; // ENGINE::EngineBrakingMapSetting=4//4
        
        public final int getRevLimitSetting()
        {
            return ( revLimitSetting );
        }
        
        /**
         * ENGINE::RevLimitSetting=0//20,000
         */
        public final float getRevLimit()
        {
            return ( revLimit );
        }
        
        /**
         * ENGINE::EngineBoostSetting=4//5
         */
        public final int getBoostMapping()
        {
            return ( boostMapping );
        }
        
        /*
         * ENGINE::EngineBrakingMapSetting=4//4
         *
        public final int getEngineBrakeMap()
        {
            return ( engineBrakeMap );
        }
        */
    }
    
    private final Engine engine = new Engine();
    
    /**
     * 
     */
    public final Engine getEngine()
    {
        return ( engine );
    }
    
    private class GearBox
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
         */
        public final float getFinalDrive()
        {
            return ( finalDrive );
        }
        
        /**
         * DRIVELINE::ReverseSetting=2//11/38 (23.030)
         */
        public final float getReverseGear()
        {
            return ( reverseGear );
        }
        
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
         */
        public final float getGear( int gear )
        {
            return ( gears[gear - 1] );
        }
    }
    
    //private final GearBox gearBox = new GearBox();
    
    /*
     * 
     *
    public final GearBox getGearBox()
    {
        return ( gearBox );
    }
    */
    
    private class Differential
    {
        private float pump; // DRIVELINE::DiffPumpSetting=30//30%
        private float power; // DRIVELINE::DiffPowerSetting=15//15%
        private float coast; // DRIVELINE::DiffCoastSetting=20//20%
        private int preload; // DRIVELINE::DiffPreloadSetting=5//6
        private float frontSplit; // DRIVELINE::RearSplitSetting=0// 0.0:100.0
        
        /**
         * DRIVELINE::DiffPumpSetting=30//30%
         */
        public final float getPump()
        {
            return ( pump );
        }
        
        /**
         * DRIVELINE::DiffPowerSetting=15//15%
         */
        public final float getPower()
        {
            return ( power );
        }
        
        /**
         * DRIVELINE::DiffCoastSetting=20//20%
         */
        public final float getCoast()
        {
            return ( coast );
        }
        
        /**
         * DRIVELINE::DiffPreloadSetting=5//6
         */
        public final int getPreload()
        {
            return ( preload );
        }
        
        /**
         * DRIVELINE::RearSplitSetting=0// 0.0:100.0
         */
        public final float getFrontSplit()
        {
            return ( frontSplit );
        }
    }
    
    //private final Differential differential = new Differential();
    
    /*
     * 
     *
    public final Differential getDifferential()
    {
        return ( differential );
    }
    */
    
    public class WheelAndTire
    {
        //private float camber; // FRONTLEFT::CamberSetting=24//-3.3 deg
        private int tirePressure; // FRONTLEFT::PressureSetting=25//120 kPa
        /*
        private float packer; // FRONTLEFT::PackerSetting=21//2.5 cm
        private int springRate; // FRONTLEFT::SpringSetting=16//100 N/mm
        private int springRubber; // FRONTLEFT::SpringRubberSetting=0//Detached
        private float rideHeight; // FRONTLEFT::RideHeightSetting=11//3.0 cm
        
        private int slowBump; // FRONTLEFT::SlowBumpSetting=15//4590 N/m/s
        private int fastBump; // FRONTLEFT::FastBumpSetting=15//2295 N/m/s
        private int slowRebound; // FRONTLEFT::SlowReboundSetting=14//9840 N/m/s
        private int fastRebound; // FRONTLEFT::FastReboundSetting=15//5100 N/m/s
        */
        private float brakeDiscThickness; // FRONTLEFT::BrakeDiscSetting=5//2.8 cm
        /*
        private int brakePad; // FRONTLEFT::BrakePadSetting=2//3
        */
        
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
         */
        public final int getTirePressure()
        {
            // TODO: Provide in IMPERIAL units, too.
            return ( tirePressure );
        }
        
        /*
         * FRONTLEFT::PackerSetting=21//2.5 cm
         *
        public final float getPacker()
        {
            return ( packer );
        }
        
        /*
         * FRONTLEFT::SpringSetting=16//100 N/mm
         *
        public final int getSpringRate()
        {
            return ( springRate );
        }
        
        /*
         * FRONTLEFT::SpringRubberSetting=0//Detached
         *
        public final int getSpringRubber()
        {
            return ( springRubber );
        }
        
        /*
         * FRONTLEFT::RideHeightSetting=11//3.0 cm
         *
        public final float getRideHeight()
        {
            return ( rideHeight );
        }
        
        /*
         * FRONTLEFT::SlowBumpSetting=15//4590 N/m/s
         *
        public final int getSlowBump()
        {
            return ( slowBump );
        }
        
        /*
         * FRONTLEFT::FastBumpSetting=15//2295 N/m/s
         *
        public final int getFastBump()
        {
            return ( fastBump );
        }
        
        /*
         * FRONTLEFT::SlowReboundSetting=14//9840 N/m/s
         *
        public final int getSlowRebound()
        {
            return ( slowRebound );
        }
        
        /*
         * FRONTLEFT::FastReboundSetting=15//5100 N/m/s
         *
        public final int getFastRebound()
        {
            return ( fastRebound );
        }
        */
        
        /**
         * FRONTLEFT::BrakeDiscSetting=5//2.8 cm
         */
        public final float getBrakeDiscThickness()
        {
            return ( brakeDiscThickness );
        }
        
        /*
         * FRONTLEFT::BrakePadSetting=2//3
         *
        public final int getBrakePad()
        {
            return ( brakePad );
        }
        */
    }
    
    private final WheelAndTire flWheelAndTire = new WheelAndTire();
    private final WheelAndTire frWheelAndTire = new WheelAndTire();
    private final WheelAndTire rlWheelAndTire = new WheelAndTire();
    private final WheelAndTire rrWheelAndTire = new WheelAndTire();
    
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
    
    private VehicleSetup()
    {
    }
    
    private static final void parseSetup( final String filename, final Reader reader, final VehiclePhysics physics, final VehicleSetup setup ) throws IOException, ParsingException
    {
        new AbstractIniParser()
        {
            @Override
            protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
            {
                Logger.log( "WARNING: ParsingException in the current setup file!" );
                Logger.log( "Unparsable line in file \"" + filename + "\" is #" + lineNr + ": \"" + line + "\" (ommitting the quotes). Exception follows..." );
                
                Logger.log( t );
                
                return ( true );
            }
            
            @Override
            protected boolean onCommentParsed( int lineNr, String group, String comment )
            {
                try
                {
                    if ( group != null )
                        return ( parseLine( lineNr, group, comment ) );
                    
                    return ( true );
                }
                catch ( ParsingException e )
                {
                    return ( true );
                }
                catch ( IOException e )
                {
                    Logger.log( e );
                    
                    return ( false );
                }
            }
            
            private boolean numPitstopsFound = false;
            
            @Override
            protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
            {
                if ( group == null )
                {
                }
                else if ( group.equals( "GENERAL" ) )
                {
                    /*
                    if ( key.equals( "Symmetric" ) )
                    {
                        //Symmetric=1
                        setup.symmetric = Integer.parseInt( value ) == 1;
                    }
                    else if ( key.equals( "CGRightSetting" ) )
                    {
                        //CGRightSetting=8//50.0:50.0
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ':' ) ) ) / 100f;
                        setup.general.weightDistributionLeft = data;
                    }
                    else if ( key.equals( "CGRearSetting" ) )
                    {
                        //CGRearSetting=12//46.3:53.7
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ':' ) ) ) / 100f;
                        setup.general.weightDistributionFront = data;
                    }
                    else if ( key.equals( "WedgeSetting" ) )
                    {
                        //WedgeSetting=0//0.00 turns
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.general.wedge = data;
                    }
                    else */if ( key.equals( "FrontTireCompoundSetting" ) )
                    {
                        //FrontTireCompoundSetting=7//04-Hot
                        int setting = Integer.parseInt( value );
                        setup.general.frontTireCompound = physics.getTireCompound( setting );
                    }
                    else if ( key.equals( "RearTireCompoundSetting" ) )
                    {
                        //RearTireCompoundSetting=7//04-Hot
                        int setting = Integer.parseInt( value );
                        setup.general.rearTireCompound = physics.getTireCompound( setting );
                    }
                    else if ( key.equals( "FuelSetting" ) )
                    {
                        //FuelSetting=94//100L (13laps)
                        int setting = Integer.parseInt( value );
                        setup.general.applyFuel( physics.getFuelRange().getValueForSetting( setting ), 0 );
                    }
                    else if ( key.equals( "NumPitstopsSetting" ) )
                    {
                        //NumPitstopsSetting=2//2
                        int setting = Integer.parseInt( value );
                        //int data = Integer.parseInt( comment );
                        setup.general.numPitstops = setting;
                        numPitstopsFound = true;
                    }
                    else if ( key.startsWith( "Pitstop" ) )
                    {
                        //Pitstop1Setting=71//75L (10laps)
                        int pitstop = Integer.parseInt( key.substring( 7, 8 ) );
                        int setting = Integer.parseInt( value );
                        float fuel = physics.getFuelRange().getValueForSetting( setting );
                        if ( ( numPitstopsFound && pitstop > setup.general.numPitstops ) || ( comment.equals( "N/A" ) ) || ( comment.equals( "Nicht vorhanden" ) ) )
                            setup.general.applyFuel( 0f, pitstop );
                        else if ( comment.startsWith( "+" ) )
                            setup.general.applyFuel( -fuel, pitstop );
                        else
                            setup.general.applyFuel( fuel, pitstop );
                    }
                }
                else if ( group.equals( "LEFTFENDER" ) )
                {
                    /*
                    if ( key.equals( "FenderFlareSetting" ) )
                    {
                        //FenderFlareSetting=0//1
                        //int setting = Integer.parseInt( value );
                        int idx = comment.indexOf( ' ' );
                        if ( idx < 0 )
                            setup.general.leftFenderFlare = Float.parseFloat( comment );
                        else
                            setup.general.leftFenderFlare = Float.parseFloat( comment.substring( 0, idx ) );
                    }
                    */
                }
                else if ( group.equals( "RIGHTFENDER" ) )
                {
                    /*
                    if ( key.equals( "FenderFlareSetting" ) )
                    {
                        //FenderFlareSetting=0//1
                        //int setting = Integer.parseInt( value );
                        int idx = comment.indexOf( ' ' );
                        if ( idx < 0 )
                            setup.general.rightFenderFlare = Float.parseFloat( comment );
                        else
                            setup.general.rightFenderFlare = Float.parseFloat( comment.substring( 0, idx ) );
                    }
                    */
                }
                else if ( group.equals( "FRONTWING" ) )
                {
                    if ( key.equals( "FWSetting" ) )
                    {
                        //FWSetting=64//65
                        int setting = Integer.parseInt( value );
                        setup.general.frontWing = physics.getFrontWingRange().getValueForSetting( setting );
                    }
                }
                else if ( group.equals( "REARWING" ) )
                {
                    /*
                    if ( key.equals( "RWSetting" ) )
                    {
                        //RWSetting=62//63
                        //int setting = Integer.parseInt( value );
                        int idx = comment.indexOf( ' ' );
                        if ( idx < 0 )
                            setup.general.rearWing = Float.parseFloat( comment );
                        else
                            setup.general.rearWing = Float.parseFloat( comment.substring( 0, idx ) );
                    }
                    */
                }
                else if ( group.equals( "BODYAERO" ) )
                {
                    /*
                    if ( key.equals( "RadiatorSetting" ) )
                    {
                        //RadiatorSetting=3//4
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.general.radiatorSize = data;
                    }
                    else if ( key.equals( "BrakeDuctSetting" ) )
                    {
                        //BrakeDuctSetting=2//3
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.general.brakeDuctSize = data;
                    }
                    */
                }
                else if ( group.equals( "SUSPENSION" ) )
                {
                    /*
                    if ( key.equals( "FrontAntiSwaySetting" ) )
                    {
                        //FrontAntiSwaySetting=40//86 N/mm
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.suspension.frontAntiSwayBar = -1;
                        else
                            setup.suspension.frontAntiSwayBar = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "RearAntiSwaySetting" ) )
                    {
                        //RearAntiSwaySetting=9//20 N/mm
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.suspension.rearAntiSwayBar = -1;
                        else
                            setup.suspension.rearAntiSwayBar = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "FrontToeInSetting" ) )
                    {
                        //FrontToeInSetting=36//-0.10 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.frontToeIn = data;
                    }
                    else if ( key.equals( "RearToeInSetting" ) )
                    {
                        //RearToeInSetting=14//0.10 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rearToeIn = data;
                    }
                    else if ( key.equals( "LeftCasterSetting" ) )
                    {
                        //LeftCasterSetting=25//3.5 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.leftCaster = data;
                    }
                    else if ( key.equals( "RightCasterSetting" ) )
                    {
                        //RightCasterSetting=25//3.5 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rightCaster = data;
                    }
                    else if ( key.equals( "LeftTrackBarSetting" ) )
                    {
                        //LeftTrackBarSetting=0//0.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.leftTrackBar = data;
                    }
                    else if ( key.equals( "RightTrackBarSetting" ) )
                    {
                        //RightTrackBarSetting=0//0.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rightTrackBar = data;
                    }
                    else if ( key.equals( "Front3rdPackerSetting" ) )
                    {
                        //Front3rdPackerSetting=9//1.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.font3dSpring.packer = data;
                    }
                    else if ( key.equals( "Front3rdSpringSetting" ) )
                    {
                        //Front3rdSpringSetting=30//60 N/mm
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.suspension.font3dSpring.springRate = -1;
                        else
                            setup.suspension.font3dSpring.springRate = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "Front3rdSlowBumpSetting" ) )
                    {
                        //Front3rdSlowBumpSetting=15//2062 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.font3dSpring.slowBump = data;
                    }
                    else if ( key.equals( "Front3rdFastBumpSetting" ) )
                    {
                        //Front3rdFastBumpSetting=15//1031 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.font3dSpring.fastBump = data;
                    }
                    else if ( key.equals( "Front3rdSlowReboundSetting" ) )
                    {
                        //Front3rdSlowReboundSetting=13//4062 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.font3dSpring.slowRebound = data;
                    }
                    else if ( key.equals( "Front3rdFastReboundSetting" ) )
                    {
                        //Front3rdFastReboundSetting=15//2344 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.font3dSpring.fastRebound = data;
                    }
                    else if ( key.equals( "Rear3rdPackerSetting" ) )
                    {
                        //Rear3rdPackerSetting=19//2.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rear3dSpring.packer = data;
                    }
                    else if ( key.equals( "Rear3rdSpringSetting" ) )
                    {
                        //Rear3rdSpringSetting=25//50 N/mm
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.suspension.rear3dSpring.springRate = -1;
                        else
                            setup.suspension.rear3dSpring.springRate = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "Rear3rdSlowBumpSetting" ) )
                    {
                        //Rear3rdSlowBumpSetting=15//2062 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rear3dSpring.slowBump = data;
                    }
                    else if ( key.equals( "Rear3rdFastBumpSetting" ) )
                    {
                        //Rear3rdFastBumpSetting=15//1031 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rear3dSpring.fastBump = data;
                    }
                    else if ( key.equals( "Rear3rdSlowReboundSetting" ) )
                    {
                        //Rear3rdSlowReboundSetting=15//4688 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rear3dSpring.slowRebound = data;
                    }
                    else if ( key.equals( "Rear3rdFastReboundSetting" ) )
                    {
                        //Rear3rdFastReboundSetting=15//2344 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.suspension.rear3dSpring.fastRebound = data;
                    }
                    */
                }
                else if ( group.equals( "CONTROLS" ) )
                {
                    /*
                    if ( key.equals( "SteerLockSetting" ) )
                    {
                        //SteerLockSetting=23//17.5 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.controls.steeringLock = data;
                    }
                    else */if ( key.equals( "RearBrakeSetting" ) )
                    {
                        //RearBrakeSetting=85//55.0:45.0
                        int setting = Integer.parseInt( value );
                        setup.controls.rearBrakeBalance = physics.getBrakes().getRearDistributionRange().getValueForSetting( setting );
                    }
                    else if ( key.equals( "BrakePressureSetting" ) )
                    {
                        //BrakePressureSetting=40//100%
                        int setting = Integer.parseInt( value );
                        setup.controls.brakePressure = physics.getBrakes().getPressureRange().getValueForSetting( setting );
                    }
                    /*
                    else if ( key.equals( "HandbrakePressSetting" ) )
                    {
                        //HandbrakePressSetting=0//0%
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( '%' ) ) ) / 100f;
                        setup.controls.handbrakePressure = data;
                    }
                    */
                }
                else if ( group.equals( "ENGINE" ) )
                {
                    if ( key.equals( "RevLimitSetting" ) )
                    {
                        //RevLimitSetting=0//20,000
                        setup.engine.revLimitSetting = Integer.parseInt( value );
                        setup.engine.revLimit = physics.getEngine().getRevLimitRange().getValueForSetting( setup.engine.revLimitSetting );
                    }
                    else if ( key.equals( "EngineBoostSetting" ) )
                    {
                        //EngineBoostSetting=4//5
                        int setting = Integer.parseInt( value );
                        setup.engine.boostMapping = (int)physics.getEngine().getBoostRange().getValueForSetting( setting );
                    }
                    /*
                    else if ( key.equals( "EngineBrakingMapSetting" ) )
                    {
                        //EngineBrakingMapSetting=4//4
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.engine.engineBrakeMap = data;
                    }
                    */
                }
                else if ( group.equals( "DRIVELINE" ) )
                {
                    /*
                    if ( key.equals( "FinalDriveSetting" ) )
                    {
                        //FinalDriveSetting=5//10/60 (bevel 18/20)
                        int setting = Integer.parseInt( value );
                        // TODO: how to store that???
                        setup.gearBox.finalDrive = setting;
                    }
                    else if ( key.equals( "ReverseSetting" ) )
                    {
                        //ReverseSetting=2//11/38 (23.030)
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( comment.indexOf( '(' ) + 1, comment.indexOf( ')' ) ) );
                        setup.gearBox.reverseGear = data;
                    }
                    else if ( key.startsWith( "Gear" ) )
                    {
                        //Gear1Setting=21//11/29 (17.576)
                        //Gear2Setting=41//14/30 (14.286)
                        //Gear3Setting=52//15/27 (12.000)
                        //Gear4Setting=64//18/28 (10.370)
                        //Gear5Setting=72//18/25 (9.259)
                        //Gear6Setting=80//20/25 (8.333)
                        //Gear7Setting=90//21/24 (7.619)
                        
                        int gear = Integer.parseInt( key.substring( 4, 5 ) );
                        
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "N/A" ) )
                            setup.gearBox.applyGear( -1f, gear );
                        else
                            setup.gearBox.applyGear( Float.parseFloat( comment.substring( comment.indexOf( '(' ) + 1, comment.indexOf( ')' ) ) ), gear );
                    }
                    else if ( key.equals( "DiffPumpSetting" ) )
                    {
                        //DiffPumpSetting=30//30%
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( '%' ) ) ) / 100f;
                        setup.differential.pump = data;
                    }
                    else if ( key.equals( "DiffPowerSetting" ) )
                    {
                        //DiffPowerSetting=15//15%
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( '%' ) ) ) / 100f;
                        setup.differential.power = data;
                    }
                    else if ( key.equals( "DiffCoastSetting" ) )
                    {
                        //DiffCoastSetting=20//20%
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( '%' ) ) ) / 100f;
                        setup.differential.coast = data;
                    }
                    else if ( key.equals( "DiffPreloadSetting" ) )
                    {
                        //DiffPreloadSetting=5//6
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.differential.preload = data;
                    }
                    else if ( key.equals( "RearSplitSetting" ) )
                    {
                        //RearSplitSetting=0// 0.0:100.0
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ':' ) ) ) / 100f;
                        setup.differential.frontSplit = data;
                    }
                    */
                }
                else if ( group.equals( "FRONTLEFT" ) )
                {
                    /*
                    if ( key.equals( "CamberSetting" ) )
                    {
                        //CamberSetting=24//-3.3 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.camber = data;
                    }
                    else if ( key.equals( "PressureSetting" ) )
                    {
                        //PressureSetting=25//120 kPa
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.tirePressure = data;
                    }
                    else if ( key.equals( "PackerSetting" ) )
                    {
                        //PackerSetting=21//2.5 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.packer = data;
                    }
                    else if ( key.equals( "SpringSetting" ) )
                    {
                        //SpringSetting=16//100 N/mm
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.springRate = data;
                    }
                    else if ( key.equals( "SpringRubberSetting" ) )
                    {
                        //SpringRubberSetting=0//Detached
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.flWheelAndTire.springRubber = -1;
                        else
                            setup.flWheelAndTire.springRubber = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "RideHeightSetting" ) )
                    {
                        //RideHeightSetting=11//3.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.rideHeight = data;
                    }
                    else if ( key.equals( "SlowBumpSetting" ) )
                    {
                        //SlowBumpSetting=15//4590 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.slowBump = data;
                    }
                    else if ( key.equals( "FastBumpSetting" ) )
                    {
                        //FastBumpSetting=15//2295 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.fastBump = data;
                    }
                    else if ( key.equals( "SlowReboundSetting" ) )
                    {
                        //SlowReboundSetting=14//9840 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.slowRebound = data;
                    }
                    else if ( key.equals( "FastReboundSetting" ) )
                    {
                        //FastReboundSetting=15//5100 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.flWheelAndTire.fastRebound = data;
                    }
                    */
                    /*else */if ( key.equals( "BrakeDiscSetting" ) )
                    {
                        //BrakeDiscSetting=5//2.8 cm
                        int setting = Integer.parseInt( value );
                        setup.flWheelAndTire.brakeDiscThickness = physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).getDiscRange().getValueForSetting( setting );
                    }
                    /*
                    else if ( key.equals( "BrakePadSetting" ) )
                    {
                        //BrakePadSetting=2//3
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.flWheelAndTire.brakePad = data;
                    }
                    */
                }
                else if ( group.equals( "FRONTRIGHT" ) )
                {
                    /*
                    if ( key.equals( "CamberSetting" ) )
                    {
                        //CamberSetting=24//-3.3 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.camber = data;
                    }
                    else if ( key.equals( "PressureSetting" ) )
                    {
                        //PressureSetting=25//120 kPa
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.tirePressure = data;
                    }
                    else if ( key.equals( "PackerSetting" ) )
                    {
                        //PackerSetting=21//2.5 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.packer = data;
                    }
                    else if ( key.equals( "SpringSetting" ) )
                    {
                        //SpringSetting=16//100 N/mm
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.springRate = data;
                    }
                    else if ( key.equals( "SpringRubberSetting" ) )
                    {
                        //SpringRubberSetting=0//Detached
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.frWheelAndTire.springRubber = -1;
                        else
                            setup.frWheelAndTire.springRubber = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "RideHeightSetting" ) )
                    {
                        //RideHeightSetting=11//3.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.rideHeight = data;
                    }
                    else if ( key.equals( "SlowBumpSetting" ) )
                    {
                        //SlowBumpSetting=15//4590 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.slowBump = data;
                    }
                    else if ( key.equals( "FastBumpSetting" ) )
                    {
                        //FastBumpSetting=15//2295 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.fastBump = data;
                    }
                    else if ( key.equals( "SlowReboundSetting" ) )
                    {
                        //SlowReboundSetting=14//9840 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.slowRebound = data;
                    }
                    else if ( key.equals( "FastReboundSetting" ) )
                    {
                        //FastReboundSetting=15//5100 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.frWheelAndTire.fastRebound = data;
                    }
                    */
                    /*else */if ( key.equals( "BrakeDiscSetting" ) )
                    {
                        //BrakeDiscSetting=5//2.8 cm
                        int setting = Integer.parseInt( value );
                        setup.frWheelAndTire.brakeDiscThickness = physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).getDiscRange().getValueForSetting( setting );
                    }
                    /*
                    else if ( key.equals( "BrakePadSetting" ) )
                    {
                        //BrakePadSetting=2//3
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.frWheelAndTire.brakePad = data;
                    }
                    */
                }
                else if ( group.equals( "REARLEFT" ) )
                {
                    /*
                    if ( key.equals( "CamberSetting" ) )
                    {
                        //CamberSetting=21//-1.3 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.camber = data;
                    }
                    else if ( key.equals( "PressureSetting" ) )
                    {
                        //PressureSetting=17//112 kPa
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.tirePressure = data;
                    }
                    else if ( key.equals( "PackerSetting" ) )
                    {
                        //PackerSetting=25//3.5 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.packer = data;
                    }
                    else if ( key.equals( "SpringSetting" ) )
                    {
                        //SpringSetting=5//50 N/mm
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.springRate = data;
                    }
                    else if ( key.equals( "SpringRubberSetting" ) )
                    {
                        //SpringRubberSetting=0//Detached
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.rlWheelAndTire.springRubber = -1;
                        else
                            setup.rlWheelAndTire.springRubber = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "RideHeightSetting" ) )
                    {
                        //RideHeightSetting=39//7.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.rideHeight = data;
                    }
                    else if ( key.equals( "SlowBumpSetting" ) )
                    {
                        //SlowBumpSetting=8//2880 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.slowBump = data;
                    }
                    else if ( key.equals( "FastBumpSetting" ) )
                    {
                        //FastBumpSetting=8//1440 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.fastBump = data;
                    }
                    else if ( key.equals( "SlowReboundSetting" ) )
                    {
                        //SlowReboundSetting=9//6700 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.slowRebound = data;
                    }
                    else if ( key.equals( "FastReboundSetting" ) )
                    {
                        //FastReboundSetting=9//3350 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rlWheelAndTire.fastRebound = data;
                    }
                    */
                    /*else */if ( key.equals( "BrakeDiscSetting" ) )
                    {
                        //BrakeDiscSetting=5//2.8 cm
                        int setting = Integer.parseInt( value );
                        setup.rlWheelAndTire.brakeDiscThickness = physics.getBrakes().getBrake( Wheel.REAR_LEFT ).getDiscRange().getValueForSetting( setting );
                    }
                    /*
                    else if ( key.equals( "BrakePadSetting" ) )
                    {
                        //BrakePadSetting=2//3
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.rlWheelAndTire.brakePad = data;
                    }
                    */
                }
                else if ( group.equals( "REARRIGHT" ) )
                {
                    /*
                    if ( key.equals( "CamberSetting" ) )
                    {
                        //CamberSetting=21//-1.3 deg
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.camber = data;
                    }
                    else if ( key.equals( "PressureSetting" ) )
                    {
                        //PressureSetting=17//112 kPa
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.tirePressure = data;
                    }
                    else if ( key.equals( "PackerSetting" ) )
                    {
                        //PackerSetting=25//3.5 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.packer = data;
                    }
                    else if ( key.equals( "SpringSetting" ) )
                    {
                        //SpringSetting=5//50 N/mm
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.springRate = data;
                    }
                    else if ( key.equals( "SpringRubberSetting" ) )
                    {
                        //SpringRubberSetting=0//Detached
                        //int setting = Integer.parseInt( value );
                        if ( comment.equals( "Detached" ) )
                            setup.rrWheelAndTire.springRubber = -1;
                        else
                            setup.rrWheelAndTire.springRubber = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                    }
                    else if ( key.equals( "RideHeightSetting" ) )
                    {
                        //RideHeightSetting=39//7.0 cm
                        //int setting = Integer.parseInt( value );
                        float data = Float.parseFloat( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.rideHeight = data;
                    }
                    else if ( key.equals( "SlowBumpSetting" ) )
                    {
                        //SlowBumpSetting=8//2880 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.slowBump = data;
                    }
                    else if ( key.equals( "FastBumpSetting" ) )
                    {
                        //FastBumpSetting=8//1440 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.fastBump = data;
                    }
                    else if ( key.equals( "SlowReboundSetting" ) )
                    {
                        //SlowReboundSetting=9//6700 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.slowRebound = data;
                    }
                    else if ( key.equals( "FastReboundSetting" ) )
                    {
                        //FastReboundSetting=9//3350 N/m/s
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment.substring( 0, comment.indexOf( ' ' ) ) );
                        setup.rrWheelAndTire.fastRebound = data;
                    }
                    */
                    /*else */if ( key.equals( "BrakeDiscSetting" ) )
                    {
                        //BrakeDiscSetting=5//2.8 cm
                        int setting = Integer.parseInt( value );
                        setup.rrWheelAndTire.brakeDiscThickness = physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).getDiscRange().getValueForSetting( setting );
                    }
                    /*
                    else if ( key.equals( "BrakePadSetting" ) )
                    {
                        //BrakePadSetting=2//3
                        //int setting = Integer.parseInt( value );
                        int data = Integer.parseInt( comment );
                        setup.rrWheelAndTire.brakePad = data;
                    }
                    */
                }
                
                return ( true );
            }
        }.parse( reader );
    }
    
    public static final VehicleSetup loadSetup( File file, LiveGameData gameData )
    {
        VehicleSetup setup = new VehicleSetup();
        
        try
        {
            parseSetup( file.getAbsolutePath(), new BufferedReader( new FileReader( file ) ), gameData.getPhysics(), setup );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        
        gameData.setSetup( setup );
        
        return ( setup );
    }
    
    public static final VehicleSetup loadSetup( String filename, LiveGameData gameData  )
    {
        return ( loadSetup( new File( filename ), gameData ) );
    }
    
    public static final VehicleSetup loadSetup( LiveGameData gameData )
    {
        return ( loadSetup( new File( RFactorTools.getProfileFolder(), "tempGarage.svm" ), gameData ) );
    }
    
    public static final VehicleSetup loadEditorDefaults( LiveGameData gameData )
    {
        VehicleSetup setup = new VehicleSetup();
        
        try
        {
            parseSetup( "Resource: /data/tempGarage.svm", new InputStreamReader( VehicleSetup.class.getResourceAsStream( "/data/tempGarage.svm" ) ), gameData.getPhysics(), setup );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        
        gameData.setSetup( setup );
        
        return ( setup );
    }
}
