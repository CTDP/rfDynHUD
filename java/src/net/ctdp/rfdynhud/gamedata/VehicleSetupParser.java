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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.ctdp.rfdynhud.util.Logger;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * This class loads setup data from an SVM file into a {@link VehicleSetup} instance.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class VehicleSetupParser
{
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
    
    private static long lastProfileUpdateId = -1L;
    private static File file = null;
    private static long lastLastModified = -1L;
    
    private static final void loadSetup( String filename, Reader reader, VehiclePhysics physics, VehicleSetup setup )
    {
        try
        {
            parseSetup( filename, reader, physics, setup );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
        
        setup.updatedInTimeScope = true;
    }
    
    static final void loadDefaultSetup( VehiclePhysics physics, VehicleSetup setup )
    {
        lastProfileUpdateId = -1L;
        file = null;
        lastLastModified = -1L;
        
        InputStream in = VehicleSetupParser.class.getClassLoader().getResourceAsStream( VehicleSetup.class.getPackage().getName().replace( '.', '/' ) + "/tempGarage.svm" );
        
        loadSetup( "Default setup", new InputStreamReader( in ), physics, setup );
    }
    
    static final boolean loadSetup( LiveGameData gameData )
    {
        if ( lastProfileUpdateId < gameData.getProfileInfo().getUpdateId() )
        {
            file = new File( gameData.getProfileInfo().getProfileFolder(), "tempGarage.svm" );
            lastLastModified = -1L;
            lastProfileUpdateId = gameData.getProfileInfo().getUpdateId();
        }
        
        if ( !file.exists() )
        {
            lastLastModified = -1L;
            return ( false );
        }
        
        if ( lastLastModified == file.lastModified() )
        {
            return ( false );
        }
        
        try
        {
            loadSetup( file.getAbsolutePath(), new BufferedReader( new FileReader( file ) ), gameData.getPhysics(), gameData.getSetup() );
            
            lastLastModified = file.lastModified();
            
            return ( true );
        }
        catch ( FileNotFoundException e )
        {
        }
        
        return ( true );
    }
}
