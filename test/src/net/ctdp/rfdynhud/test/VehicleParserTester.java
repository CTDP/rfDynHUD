package net.ctdp.rfdynhud.test;
import java.io.File;

import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehiclePhysicsParser;

/** Class I (Marcel) use for local testing. */
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
	    
		//VehiclePhysicsParser.parsePhysicsFiles( CCH_FILE, RFACTOR_FOLDER, "GameData\\Vehicles\\CTDP\\CTDP\\CTDPF1_2006\\McLaren\\KR06.veh", physics );
		//VehiclePhysicsParser.parsePhysicsFiles( CCH_FILE, RFACTOR_FOLDER, "GameData\\Vehicles\\CTDP\\CTDP\\CTDPF1_2006\\Ferrari\\MS06.veh", physics );
        //VehiclePhysicsParser.parsePhysicsFiles( CCH_FILE, RFACTOR_FOLDER, "GameData\\Vehicles\\4r2009\\2008\\Equipos\\SToroRosso\\STR_15.veh", physics );
        VehiclePhysicsParser.parsePhysicsFiles( CCH_FILE, RFACTOR_FOLDER, "GameData\\Vehicles\\CTDP\\CTDP\\CTDPF1_2006\\Ferrari\\F248_MS05.veh", physics );
        
        //testGripLoss( physics );
	}
}
