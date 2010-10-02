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
package net.ctdp.rfdynhud.test;

import java.io.File;

import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;

/**
 * Class I (Marcel) use for local testing.
 */
public class VehicleParserTester
{
    //private static final File RFACTOR_FOLDER = new File( "D:\\Racing\\rFactor-F1test" );
    private static final File RFACTOR_FOLDER = new File( "D:\\Spiele\\rFactor" );
    private static final String MOD_NAME = "F1CTDP06";
    
    private static final File PROFILE_FOLDER = new File( RFACTOR_FOLDER, "UserData\\Marvin Froehlich" );
    private static final File CCH_FILE = new File( PROFILE_FOLDER, MOD_NAME + ".cch" );
    
    /*
    private static void testGripLoss( VehiclePhysics physics )
    {
        Wheel hottestWheel = Wheel.FRONT_LEFT;
        float temp = 110;
        float press = 120;
        float load = 2000;
        TireCompound tc = physics.getTireCompound( 1 );
        TireCompound.CompoundWheel tcw = tc.getWheel( hottestWheel );
        float tcGrip = ( tcw.getDryLateralGrip() + tcw.getDryLateralGrip() ) / 2f;
        float gripFact = tcw.getGripFraction( 1.0f, temp, press, load );
        float grip = tcGrip * gripFact;
        System.out.println( tc.getName() + ", " + tcw.getOptimumTemperature() + ", " + tcw.getOptimumPressure( load ) + ", " + tcGrip + ", " + gripFact + ", " + grip );
    }
    */
    
	public static void main( String[] args ) throws Throwable
	{
	    VehiclePhysics physics = new VehiclePhysics();
	    
		//VehiclePhysicsParser.parsePhysicsFiles( CCH_FILE, new File( RFACTOR_FOLDER, "GameData\\Vehicles\\CTDP\\CTDP\\CTDPF1_2006\\McLaren\\KR06.veh" ), "Silverstone", physics );
		//VehiclePhysicsParser.parsePhysicsFiles( CCH_FILE, new File( RFACTOR_FOLDER, "GameData\\Vehicles\\CTDP\\CTDP\\CTDPF1_2006\\Ferrari\\MS06.veh" ), "Silverstone", physics );
        //VehiclePhysicsParser.parsePhysicsFiles( CCH_FILE, new File( RFACTOR_FOLDER, "GameData\\Vehicles\\4r2009\\2008\\Equipos\\SToroRosso\\STR_15.veh" ), "Silverstone", physics );
        __GDPrivilegedAccess.parsePhysicsFiles( CCH_FILE, new File( RFACTOR_FOLDER, "GameData\\Vehicles\\CTDP\\CTDP\\CTDPF1_2006\\Ferrari\\F248_MS05.veh" ), "Silverstone", physics );
        
        //testGripLoss( physics );
	}
}
