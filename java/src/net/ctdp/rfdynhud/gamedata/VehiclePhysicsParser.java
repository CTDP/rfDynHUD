package net.ctdp.rfdynhud.gamedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import net.ctdp.rfdynhud.util.Logger;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniLine;

public class VehiclePhysicsParser
{
    private static File locateFile( File dir, String name )
    {
        while ( dir != null )
        {
            File f = new File( dir, name );
            if ( f.exists() && f.isFile() )
                return ( f.getAbsoluteFile() );
            
            dir = dir.getParentFile();
        }
        
        return ( null );
    }
    
    private static final float parseFloat( float f0, float f1, String op )
    {
        if ( op == null )
            return ( f1 );
        
        if ( op.equals( "+=" ) )
            return ( f0 + f1 );
        
        if ( op.equals( "-=" ) )
            return ( f0 - f1 );
        
        if ( op.equals( "*=" ) )
            return ( f0 * f1 );
        
        if ( op.equals( "/=" ) )
            return ( f0 / f1 );
        
        //if ( op.equals( "=" ) )
            return ( f1 );
    }
    
    private static final int parseInt( int f0, int f1, String op )
    {
        if ( op == null )
            return ( f1 );
        
        if ( op.equals( "+=" ) )
            return ( f0 + f1 );
        
        if ( op.equals( "-=" ) )
            return ( f0 - f1 );
        
        if ( op.equals( "*=" ) )
            return ( f0 * f1 );
        
        if ( op.equals( "/=" ) )
            return ( f0 / f1 );
        
        //if ( op.equals( "=" ) )
            return ( f1 );
    }
    
    private static final String[] parseTuple( String value )
    {
        if ( value.startsWith( "(" ) && value.endsWith( ")" ) )
            value = value.substring( 1, value.length() - 1 );
        
        String[] values = value.split( "," );
        
        for ( int i = 0; i < values.length; i++ )
            values[i] = values[i].trim();
        
        return ( values );
    }
    
    private static final void parsePhysicsSetting( String value, VehiclePhysics.PhysicsSetting setting )
    {
        int bPos1 = value.indexOf( '(' );
        int start = ( bPos1 >= 0 ) ? bPos1 + 1 : 0;
        int bPos2 = value.indexOf( ')', start );
        int end = ( bPos2 >= 0 ) ? bPos2 - 1 : value.length() - 1;
        
        if ( ( start > 0 ) || ( end < value.length() - 1 ) )
            value = value.substring( start, end + 1 );
        
        StringTokenizer st = new StringTokenizer( value, "," );
        String bv = st.nextToken().trim();
        String ss = st.nextToken().trim();
        String ns = st.nextToken().trim();
        
        setting.set( Float.parseFloat( bv ), Float.parseFloat( ss ), Integer.parseInt( ns ) );
    }
    
    private static final void parsePhysicsSetting( String value, VehiclePhysics.PhysicsSetting setting, String op )
    {
        if ( ( op == null ) || op.equals( "=" ) )
        {
            parsePhysicsSetting( value, setting );
            return;
        }
        
        if ( value.startsWith( "(" ) && value.endsWith( ")" ) )
            value = value.substring( 1, value.length() - 1 );
        
        StringTokenizer st = new StringTokenizer( value, "," );
        String bv = st.nextToken().trim();
        String ss = st.nextToken().trim();
        String ns = st.nextToken().trim();
        
        if ( op.equals( "+=" ) )
            setting.set( setting.getBaseValue() + Float.parseFloat( bv ), setting.getStepSize() + Float.parseFloat( ss ), setting.getNumSteps() + Integer.parseInt( ns ) );
        else if ( op.equals( "-=" ) )
            setting.set( setting.getBaseValue() - Float.parseFloat( bv ), setting.getStepSize() - Float.parseFloat( ss ), setting.getNumSteps() - Integer.parseInt( ns ) );
        else if ( op.equals( "*=" ) )
            setting.set( setting.getBaseValue() * Float.parseFloat( bv ), setting.getStepSize() * Float.parseFloat( ss ), setting.getNumSteps() * Integer.parseInt( ns ) );
        else if ( op.equals( "/=" ) )
            setting.set( setting.getBaseValue() / Float.parseFloat( bv ), setting.getStepSize() / Float.parseFloat( ss ), setting.getNumSteps() / Integer.parseInt( ns ) );
    }
    
    private static class TBCParser extends AbstractIniParser
    {
        @SuppressWarnings( "unused" )
        private final File path;
        private final VehiclePhysics physics;
        private String currentLabel = null;
        private final HashMap<String, VehiclePhysics.SlipCurve> slipCurves = new HashMap<String, VehiclePhysics.SlipCurve>();
        private VehiclePhysics.SlipCurve currentSlipCurve = null;
        private VehiclePhysics.TireCompound[] tireCompounds = new VehiclePhysics.TireCompound[ 128 ];
        private int numCompounds = 0;
        private VehiclePhysics.TireCompound currentCompound = null;
        private String frontTempAndPressGrip = null;
        private String frontGripTempPress = null;
        private String rearTempAndPressGrip = null;
        private String rearGripTempPress = null;
        private String wearGrip1 = null;
        
        private void storeSlipCurve()
        {
            slipCurves.put( currentSlipCurve.name, currentSlipCurve );
            
            currentSlipCurve = null;
        }
        
        private void storeOffTempsAndPressure()
        {
            if ( frontGripTempPress != null )
            {
                // new way
                
                String[] values = frontGripTempPress.substring( 1, frontGripTempPress.length() - 1 ).split( "," );
                float belowTemp = Float.parseFloat( values[0].trim() );
                float aboveTemp = belowTemp;
                float offPress = Float.parseFloat( values[1].trim() );
                
                currentCompound.getWheel( Wheel.FRONT_LEFT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
                currentCompound.getWheel( Wheel.FRONT_RIGHT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
            }
            else if ( frontTempAndPressGrip != null )
            {
                // old way
                
                String[] values = frontTempAndPressGrip.substring( 1, frontGripTempPress.length() - 1 ).split( "," );
                float belowTemp = Float.parseFloat( values[0].trim() );
                float aboveTemp = Float.parseFloat( values[1].trim() );
                float offPress = Float.parseFloat( values[2].trim() );
                
                currentCompound.getWheel( Wheel.FRONT_LEFT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
                currentCompound.getWheel( Wheel.FRONT_RIGHT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
            }
            
            frontTempAndPressGrip = null;
            frontGripTempPress = null;
            
            if ( rearGripTempPress != null )
            {
                // new way
                
                String[] values = rearGripTempPress.substring( 1, rearGripTempPress.length() - 1 ).split( "," );
                float belowTemp = Float.parseFloat( values[0].trim() );
                float aboveTemp = belowTemp;
                float offPress = Float.parseFloat( values[1].trim() );
                
                currentCompound.getWheel( Wheel.REAR_LEFT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
                currentCompound.getWheel( Wheel.REAR_RIGHT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
            }
            else if ( rearTempAndPressGrip != null )
            {
                // old way
                
                String[] values = rearTempAndPressGrip.substring( 1, rearGripTempPress.length() - 1 ).split( "," );
                float belowTemp = Float.parseFloat( values[0].trim() );
                float aboveTemp = Float.parseFloat( values[1].trim() );
                float offPress = Float.parseFloat( values[2].trim() );
                
                currentCompound.getWheel( Wheel.REAR_LEFT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
                currentCompound.getWheel( Wheel.REAR_RIGHT ).setAboveAndBelowTempsAndPressures( belowTemp, aboveTemp, offPress );
            }
            
            rearTempAndPressGrip = null;
            rearGripTempPress = null;
        }
        
        private void storeCompound()
        {
            storeOffTempsAndPressure();
            
            if ( tireCompounds.length <= numCompounds )
            {
                VehiclePhysics.TireCompound[] tmp = new VehiclePhysics.TireCompound[ tireCompounds.length * 3 / 2 + 1 ];
                System.arraycopy( tireCompounds, 0, tmp, 0, numCompounds );
                tireCompounds = tmp;
            }
            
            currentCompound.index = numCompounds;
            tireCompounds[numCompounds++] = currentCompound;
            
            currentCompound = null;
        }
        
        @Override
        protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
        {
            if ( currentSlipCurve != null )
            {
                storeSlipCurve();
            }
            
            if ( currentCompound != null )
            {
                storeCompound();
            }
            
            currentLabel = null;
            wearGrip1 = null;
            
            if ( group.equals( "SLIPCURVE" ) )
            {
                currentSlipCurve = new VehiclePhysics.SlipCurve();
            }
            else if ( group.equals( "COMPOUND" ) )
            {
                currentCompound = new VehiclePhysics.TireCompound();
            }
            
            return ( super.onGroupParsed( lineNr, group ) );
        }
        
        private static final boolean isFrontLabel( String label )
        {
            if ( label.equalsIgnoreCase( "FRONT" ) )
                return ( true );
            
            if ( label.equalsIgnoreCase( "FRONTLEFT" ) )
                return ( true );
            
            if ( label.equalsIgnoreCase( "FRONTRIGHT" ) )
                return ( true );
            
            if ( label.equalsIgnoreCase( "ALL" ) )
                return ( true );
            
            return ( false );
        }
        
        private static final boolean isRearLabel( String label )
        {
            if ( label.equalsIgnoreCase( "REAR" ) )
                return ( true );
            
            if ( label.equalsIgnoreCase( "REARLEFT" ) )
                return ( true );
            
            if ( label.equalsIgnoreCase( "REARRIGHT" ) )
                return ( true );
            
            if ( label.equalsIgnoreCase( "ALL" ) )
                return ( true );
            
            return ( false );
        }
        
        @Override
        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
        {
            if ( group == null )
            {
            }
            else if ( group.equals( "SLIPCURVE" ) )
            {
                if ( key.equals( "Name" ) )
                {
                    currentSlipCurve.name = value;
                }
                else if ( key.equals( "Step" ) )
                {
                    currentSlipCurve.step = Float.parseFloat( value );
                }
                else if ( key.equals( "DropoffFunction" ) )
                {
                    currentSlipCurve.dropoffFunction = Float.parseFloat( value );
                }
            }
            else if ( group.equals( "COMPOUND" ) )
            {
                if ( key.equals( "Name" ) )
                {
                    currentCompound.name = value;
                }
                else if ( key.equals( "DryLatLong" ) )
                {
                    if ( currentLabel != null )
                    {
                        String[] values = parseTuple( value );
                        float laterial = Float.parseFloat( values[0] );
                        float longitudinal = Float.parseFloat( values[1] );
                        
                        if ( isFrontLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.FRONT_LEFT ).setDryGrip( laterial, longitudinal );
                            currentCompound.getWheel( Wheel.FRONT_RIGHT ).setDryGrip( laterial, longitudinal );
                        }
                        if ( isRearLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.REAR_LEFT ).setDryGrip( laterial, longitudinal );
                            currentCompound.getWheel( Wheel.REAR_RIGHT ).setDryGrip( laterial, longitudinal );
                        }
                    }
                }
                else if ( key.equals( "Temperatures" ) )
                {
                    if ( currentLabel != null )
                    {
                        if ( isFrontLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.FRONT_LEFT ).setOptimumTemperature( Float.parseFloat( value.substring( value.indexOf( "(" ) + 1, value.indexOf( "," ) ) ) );
                            currentCompound.getWheel( Wheel.FRONT_RIGHT ).setOptimumTemperature( currentCompound.getWheel( Wheel.FRONT_LEFT ).getOptimumTemperature() );
                        }
                        if ( isRearLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.REAR_LEFT ).setOptimumTemperature( Float.parseFloat( value.substring( value.indexOf( "(" ) + 1, value.indexOf( "," ) ) ) );
                            currentCompound.getWheel( Wheel.REAR_RIGHT ).setOptimumTemperature( currentCompound.getWheel( Wheel.REAR_LEFT ).getOptimumTemperature() );
                        }
                    }
                }
                else if ( key.equals( "OptimumPressure" ) )
                {
                    if ( currentLabel != null )
                    {
                        String[] values = parseTuple( value );
                        float optPress = Float.parseFloat( values[0] );
                        float optPressMult = Float.parseFloat( values[1] );
                        
                        if ( isFrontLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.FRONT_LEFT ).setOptimumPressure( optPress, optPressMult );
                            currentCompound.getWheel( Wheel.FRONT_RIGHT ).setOptimumPressure( optPress, optPressMult );
                        }
                        if ( isRearLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.REAR_LEFT ).setOptimumPressure( optPress, optPressMult );
                            currentCompound.getWheel( Wheel.REAR_RIGHT ).setOptimumPressure( optPress, optPressMult );
                        }
                    }
                }
                else if ( key.equals( "TempAndPressGrip" ) ) // old style
                {
                    if ( currentLabel != null )
                    {
                        if ( isFrontLabel( currentLabel ) )
                            frontTempAndPressGrip = value;
                        if ( isRearLabel( currentLabel ) )
                            rearTempAndPressGrip = value;
                    }
                }
                else if ( key.equals( "GripTempPress" ) ) // new style
                {
                    if ( currentLabel != null )
                    {
                        if ( isFrontLabel( currentLabel ) )
                            frontGripTempPress = value;
                        if ( isRearLabel( currentLabel ) )
                            rearGripTempPress = value;
                    }
                }
                else if ( key.equals( "WearGrip1" ) )
                {
                    wearGrip1 = value;
                }
                else if ( key.equals( "WearGrip2" ) )
                {
                    StringTokenizer st = new StringTokenizer(
                            wearGrip1.substring( wearGrip1.indexOf( "(" ) + 1, wearGrip1.indexOf( ")" ) ) + "," +
                            value.substring( value.indexOf( "(" ) + 1, value.indexOf( ")" ) ), ","
                            );
                    float[] wg = new float[ 17 ];
                    wg[0] = 1.0f;
                    for ( int i = 1; i < wg.length; i++ )
                    {
                        wg[i] = Float.parseFloat( st.nextToken() );
                    }
                    currentCompound.getWheel( Wheel.FRONT_LEFT ).gripFactorPerWear = wg;
                    currentCompound.getWheel( Wheel.FRONT_RIGHT).gripFactorPerWear = wg;
                    currentCompound.getWheel( Wheel.REAR_LEFT ).gripFactorPerWear = wg;
                    currentCompound.getWheel( Wheel.REAR_RIGHT ).gripFactorPerWear = wg;
                    
                    wearGrip1 = null;
                }
                else if ( key.equals( "LatCurve" ) )
                {
                    if ( ( currentLabel == null ) || isFrontLabel( currentLabel ) )
                        currentCompound.frontLatitudeSlipCurve = slipCurves.get( value );
                    if ( ( currentLabel == null ) || isRearLabel( currentLabel ) )
                        currentCompound.rearLatitudeSlipCurve = slipCurves.get( value );
                }
                else if ( key.equals( "BrakingCurve" ) )
                {
                    if ( ( currentLabel == null ) || isFrontLabel( currentLabel ) )
                        currentCompound.frontBrakingSlipCurve = slipCurves.get( value );
                    if ( ( currentLabel == null ) || isRearLabel( currentLabel ) )
                        currentCompound.rearBrakingSlipCurve = slipCurves.get( value );
                }
                else if ( key.equals( "TractiveCurve" ) )
                {
                    if ( ( currentLabel == null ) || isFrontLabel( currentLabel ) )
                        currentCompound.frontTractiveSlipCurve = slipCurves.get( value );
                    if ( ( currentLabel == null ) || isRearLabel( currentLabel ) )
                        currentCompound.rearTractiveSlipCurve = slipCurves.get( value );
                }
            }
            
            return ( true );
        }
        
        public static final String parseLabel( String line )
        {
            int slashPos = line.indexOf( "//" );
            int colonPos = line.indexOf( ":" );
            if ( ( colonPos != -1 ) && ( ( slashPos == -1 ) || ( colonPos < slashPos ) ) )
                return ( line.substring( 0, colonPos ).trim() );
            
            return ( null );
        }
        
        @Override
        protected boolean verifyIllegalLine( int lineNr, String group, String line ) throws ParsingException
        {
            String label = parseLabel( line );
            if ( label != null )
            {
                currentLabel = label;
                
                return ( true );
            }
            
            if ( !"Data".equals( currentLabel ) || ( currentSlipCurve == null ) )
                return ( false );
            
            String[] values = line.split( " " );
            if ( currentSlipCurve.data.length - currentSlipCurve.dataLength < values.length )
            {
                float[] tmp = new float[ currentSlipCurve.data.length * 3 / 2 + 1 ];
                System.arraycopy( currentSlipCurve.data, 0, tmp, 0, currentSlipCurve.dataLength );
                currentSlipCurve.data = tmp;
            }
            for ( int i = 0; i < values.length; i++ )
                currentSlipCurve.data[currentSlipCurve.dataLength++] = Float.parseFloat( values[i] );
            
            return ( true );
        }
        
        @Override
        protected void onParsingFinished() throws ParsingException
        {
            if ( currentSlipCurve != null )
            {
                storeSlipCurve();
            }
            
            if ( currentCompound != null )
            {
                storeCompound();
            }
            
            if ( numCompounds > 0 )
            {
                VehiclePhysics.TireCompound[] tireCompounds = new VehiclePhysics.TireCompound[ numCompounds ];
                System.arraycopy( this.tireCompounds, 0, tireCompounds, 0, numCompounds );
                physics.setTireCompounds( tireCompounds );
            }
        }
        
        public TBCParser( File path, VehiclePhysics physics )
        {
            this.path = path;
            this.physics = physics;
        }
        
        public static void parseTBCFile( File path, String filename, VehiclePhysics physics )
        {
            if ( !filename.toLowerCase().endsWith( ".tbc" ) )
                filename = filename + ".tbc";
            
            File tireFile = locateFile( path, filename );
            if ( tireFile == null )
            {
                // TODO: Remove this CTDP special!
                tireFile = new File( path.getParentFile().getParentFile(), filename );
                if ( !tireFile.exists() )
                    tireFile = new File( new File( path.getParentFile().getParentFile(), "Tyres" ), filename );
                
                if ( !tireFile.exists() )
                    tireFile = null;
            }
            
            
            if ( tireFile != null )
            {
                try
                {
                    new TBCParser( tireFile.getParentFile(), physics ).parse( tireFile );
                    physics.usedTBCFile = tireFile;
                }
                catch ( Throwable t )
                {
                    throw new ParsingException( t );
                }
            }
            else
            {
                Logger.log( "Warning: Unable to find tire file \"" + filename + "\"." );
            }
        }
    }
    
    private static class EnginePhysicsParser extends AbstractIniParser
    {
        @SuppressWarnings( "unused" )
        private final File path;
        private final VehiclePhysics.Engine engine;
        
        public static void parseEngineSetting( String key, String value, String op, VehiclePhysics.Engine engine )
        {
            if ( key.equals( "OptimumOilTemp" ) )
            {
                engine.optimumOilTemperature = parseFloat( engine.optimumOilTemperature, Float.parseFloat( value ), op );
            }
            else if ( key.equals( "LifetimeOilTemp" ) )
            {
                String[] values = value.substring( 1, value.length() - 1 ).split( "," );
                float v1 = Float.parseFloat( values[0] );
                float v2 = Float.parseFloat( values[1] );
                engine.baseLifetimeOilTemperature = parseFloat( engine.baseLifetimeOilTemperature, v1, op );
                engine.halfLifetimeOilTempOffset = parseFloat( engine.halfLifetimeOilTempOffset, v2, op );
            }
            else if ( key.equals( "LifetimeEngineRPM" ) )
            {
                String[] values = value.substring( 1, value.length() - 1 ).split( "," );
                float v1 = Float.parseFloat( values[0] );
                float v2 = Float.parseFloat( values[1] );
                engine.baseLifetimeRPM = parseFloat( engine.baseLifetimeRPM, v1, op );
                engine.halfLifetimeRPMOffset = parseFloat( engine.halfLifetimeRPMOffset, v2, op );
            }
            else if ( key.equals( "LifetimeAvg" ) )
            {
                engine.lifetimeAverage = parseInt( engine.lifetimeAverage, Integer.parseInt( value ), op );
            }
            else if ( key.equals( "LifetimeVar" ) )
            {
                engine.lifetimeVariance = parseInt( engine.lifetimeVariance, Integer.parseInt( value ), op );
            }
            else if ( key.equals( "EngineBoostRange" ) )
            {
                parsePhysicsSetting( value, engine.getBoostRange(), op );
            }
            else if ( key.equals( "BoostEffects" ) )
            {
                String[] values = value.substring( 1, value.length() - 1 ).split( "," );
                engine.rpmIncreasePerBoostSetting = parseFloat( engine.rpmIncreasePerBoostSetting, Float.parseFloat( values[0] ), op );
                engine.fuelUsageIncreasePerBoostSetting = parseFloat( engine.fuelUsageIncreasePerBoostSetting, Float.parseFloat( values[1] ), op );
                engine.wearIncreasePerBoostSetting = parseFloat( engine.wearIncreasePerBoostSetting, Float.parseFloat( values[2] ), op );
            }
            else if ( key.equals( "RamEffects" ) )
            {
                String[] values = value.substring( 1, value.length() - 1 ).split( "," );
                engine.wearIncreasePerVelocity = parseFloat( engine.wearIncreasePerVelocity, Float.parseFloat( values[3] ), op );
            }
        }
        
        @Override
        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
        {
            if ( group == null )
            {
                parseEngineSetting( key, value, null, engine );
            }
            
            return ( true );
        }
        
        public EnginePhysicsParser( File path, VehiclePhysics.Engine engine )
        {
            this.path = path;
            this.engine = engine;
        }
        
        public static void parseEngineFile( File path, String filename, VehiclePhysics.Engine engine )
        {
            if ( !filename.toLowerCase().endsWith( ".ini" ) )
                filename = filename + ".ini";
            
            File engineFile = locateFile( path, filename );
            if ( engineFile != null )
            {
                try
                {
                    new EnginePhysicsParser( engineFile.getParentFile(), engine ).parse( engineFile );
                }
                catch ( Throwable t )
                {
                    throw new ParsingException( t );
                }
            }
            else
            {
                Logger.log( "Warning: Unable to find engine file \"" + filename + "\"." );
            }
        }
    }
    
    private static final void parseResponseCurve( String range, VehiclePhysics.Brakes.WheelBrake brake )
    {
        if ( range.startsWith( "(" ) && range.endsWith( ")" ) )
            range = range.substring( 1, range.length() - 1 );
        
        StringTokenizer st = new StringTokenizer( range, "," );
        String ct = st.nextToken().trim();
        String min = st.nextToken().trim();
        String max = st.nextToken().trim();
        String ht = st.nextToken().trim();
        
        brake.setTemperatures( Float.parseFloat( ct ), Float.parseFloat( min ), Float.parseFloat( max ), Float.parseFloat( ht ) );
    }
    
    private static class HDVParser extends AbstractIniParser
    {
        private final File path;
        private final VehiclePhysics physics;
        
        public static void parseHDVLine( File path, String group, String key, String op, String value, VehiclePhysics physics )
        {
            if ( group.equals( "GENERAL" ) )
            {
                if ( key.equals( "TireBrand" ) )
                {
                    TBCParser.parseTBCFile( path, value, physics );
                }
                else if ( key.equals( "FuelRange" ) )
                    parsePhysicsSetting( value, physics.getFuelRange(), op );
            }
            else if ( group.equals( "FRONTWING" ) )
            {
                if ( key.equals( "FWRange" ) )
                    parsePhysicsSetting( value, physics.getFrontWingRange(), op );
            }
            else if ( group.equals( "ENGINE" ) )
            {
                if ( key.equals( "Normal" ) )
                {
                    EnginePhysicsParser.parseEngineFile( path, value, physics.getEngine() );
                }
            }
            else if ( group.equals( "FRONTLEFT" ) )
            {
                if ( key.equals( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).getDiscRange(), op );
                else if ( key.equals( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.FRONT_LEFT ) );
                else if ( key.equals( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equals( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equals( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).torque, Float.parseFloat( value ), op );
                else if ( key.equals( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.FRONT_LEFT ), op );
                else if ( key.equals( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "FRONTRIGHT" ) )
            {
                if ( key.equals( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).getDiscRange(), op );
                else if ( key.equals( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ) );
                else if ( key.equals( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equals( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equals( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).torque, Float.parseFloat( value ), op );
                else if ( key.equals( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.FRONT_RIGHT ), op );
                else if ( key.equals( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "REARLEFT" ) )
            {
                if ( key.equals( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.REAR_LEFT ).getDiscRange(), op );
                else if ( key.equals( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.REAR_LEFT ) );
                else if ( key.equals( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equals( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equals( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).torque, Float.parseFloat( value ), op );
                else if ( key.equals( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.REAR_LEFT ), op );
                else if ( key.equals( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "REARRIGHT" ) )
            {
                if ( key.equals( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).getDiscRange(), op );
                else if ( key.equals( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.REAR_RIGHT ) );
                else if ( key.equals( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equals( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equals( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).torque, Float.parseFloat( value ), op );
                else if ( key.equals( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.REAR_RIGHT ), op );
                else if ( key.equals( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "CONTROLS" ) )
            {
                if ( key.equals( "RearBrakeRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getRearDistributionRange(), op );
                else if ( key.equals( "BrakePressureRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getPressureRange(), op );
            }
            else if ( group.equals( "DRIVELINE" ) )
            {
                if ( key.equals( "WheelDrive" ) )
                    physics.wheelDrive = VehiclePhysics.WheelDrive.valueOf( value.toUpperCase() );
            }
        }
        
        @Override
        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
        {
            if ( group != null )
            {
                parseHDVLine( path, group, key, null, value, physics );
            }
            
            return ( true );
        }
        
        public HDVParser( File path, VehiclePhysics physics )
        {
            this.path = path;
            this.physics = physics;
        }
    }
    
    private static class CCHParser extends AbstractIniParser
    {
        private final String vehicleFile;
        
        private boolean groupFound = false;
        private boolean upgradeListStarted = false;
        
        private final ArrayList<Object[]> upgradesList = new ArrayList<Object[]>();
        
        public final Object[][] getUpgradesList()
        {
            if ( upgradesList.size() == 0 )
                return ( null );
            
            Object[][] ul = new Object[ upgradesList.size() ][];
            for ( int i = 0; i < upgradesList.size(); i++ )
            {
                ul[i] = upgradesList.get( i );
            }
            
            return ( ul );
        }
        
        @Override
        protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
        {
            if ( groupFound || upgradeListStarted )
                return ( false );
            
            return ( true );
        }
        
        @Override
        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
        {
            if ( upgradeListStarted )
            {
                if ( !key.equalsIgnoreCase( "Track Configuration" ) )
                {
                    String[] params = value.split( "," );
                    
                    Object[] upgrade = new Object[ 1 + params.length ];
                    
                    upgrade[0] = key;
                    
                    for ( int i = 0; i < params.length; i++ )
                        upgrade[1 + i] = Integer.valueOf( params[i] );
                    
                    upgradesList.add( upgrade );
                }
            }
            else if ( group == null )
            {
            }
            else if ( group.equals( "VEHICLE" ) )
            {
                if ( key.equals( "File" ) && value.equalsIgnoreCase( vehicleFile ) )
                {
                    groupFound = true;
                }
            }
            
            return ( true );
        }
        
        @Override
        protected boolean verifyIllegalLine( int lineNr, String group, String line ) throws ParsingException
        {
            String label = TBCParser.parseLabel( line );
            if ( label != null )
            {
                if ( label.equals( "UpgradeList" ) )
                {
                    upgradeListStarted = groupFound;
                    
                    return ( true );
                }
            }
            
            return ( false );
        }
        
        public CCHParser( String vehicleFile )
        {
            this.vehicleFile = vehicleFile;
        }
    }
    
    private static class UpgradesParser
    {
        private final File path;
        private final String searchedUpgradeType;
        private final int searchedUpgradeLevel;
        private final VehiclePhysics physics;
        
        private int hierarchy = 0;
        
        private String currentType = null;
        private String currentLevel = null;
        
        private boolean currentTypeUsed = false;
        private int usedUpgradeLevel = -1;
        private int currentLevelIndex = -1;
        
        private VehiclePhysics.UpgradeIdentifier identifier = null;
        
        private int parseTypes( String line )
        {
            if ( line.startsWith( "UpgradeType=\"" ) )
            {
                int dqPos = line.indexOf( '"', 13 );
                if ( dqPos < 0 )
                    return ( +1 );
                
                currentType = line.substring( 13, dqPos );
                currentLevel = null;
                
                currentTypeUsed = false;
                if ( currentType.equalsIgnoreCase( searchedUpgradeType ) )
                {
                    currentTypeUsed = true;
                    usedUpgradeLevel = searchedUpgradeLevel;
                }
                
                currentLevelIndex = -1;
                
                return ( +1 );
            }
            
            return ( 0 );
        }
        
        private int parseLevels( int lnr, String line )
        {
            if ( line.startsWith( "}" ) )
            {
                if ( currentType != null )
                {
                    currentType = null;
                    
                    return ( -1 );
                }
                
                return ( 0 );
            }
            else if ( line.startsWith( "UpgradeLevel=\"" ) )
            {
                int dqPos = line.indexOf( '"', 14 );
                if ( dqPos < 0 )
                    return ( +1 );
                
                currentLevel = line.substring( 14, dqPos );
                currentLevelIndex++;
                
                if ( currentTypeUsed && ( currentLevelIndex == usedUpgradeLevel ) )
                    identifier = new VehiclePhysics.UpgradeIdentifier( currentType, currentLevel, null );
                
                return ( +1 );
            }
            
            return ( 0 );
        }
        
        String currentHDVGroup = null;
        
        private void parseHDVValue( String group, String key, String op, String value )
        {
            if ( group.equals( "GENERAL" ) )
            {
                if ( key.equals( "TireBrand" ) && op.equals( "=" ) )
                {
                    TBCParser.parseTBCFile( path, value, physics );
                }
                else if ( key.equals( "FuelRange" ) )
                {
                    parsePhysicsSetting( value, physics.getFuelRange(), op );
                }
            }
            else if ( group.equals( "FRONTWING" ) )
            {
                if ( key.equals( "FWRange" ) )
                {
                    parsePhysicsSetting( value, physics.getFrontWingRange(), op );
                }
            }
            else if ( group.equals( "ENGINE" ) )
            {
                EnginePhysicsParser.parseEngineSetting( key, value, op, physics.getEngine() );
            }
            else
            {
                HDVParser.parseHDVLine( path, group, key, op, value, physics );
            }
        }
        
        private void parseHDVLine( String line )
        {
            if ( line.equals( "" ) )
                return;
            
            if ( line.startsWith( "[" ) && line.endsWith( "]" ) )
            {
                currentHDVGroup = line.substring( 1, line.length() - 1 );
            }
            else if ( currentHDVGroup != null )
            {
                @SuppressWarnings( "unused" )
                int opPos = -1;
                String op = null;
                if ( ( opPos = line.indexOf( "+=" ) ) >= 0 )
                    op = "+=";
                else if ( ( opPos = line.indexOf( "-=" ) ) >= 0 )
                    op = "-=";
                else if ( ( opPos = line.indexOf( "*=" ) ) >= 0 )
                    op = "*=";
                else if ( ( opPos = line.indexOf( "/=" ) ) >= 0 )
                    op = "/=";
                else if ( ( opPos = line.indexOf( "=" ) ) >= 0 )
                    op = "=";
                
                if ( op != null )
                {
                    //String key = line.substring( 0, opPos ).trim();
                    //String value = line.substring( opPos + op.length() ).trim();
                    
                    IniLine iniLine = new IniLine();
                    try
                    {
                        AbstractIniParser.parseLine( -1, currentHDVGroup, line, op, iniLine, null );
                    }
                    catch ( ParsingException e )
                    {
                        Logger.log( e );
                    }
                    catch ( IOException e )
                    {
                        Logger.log( e );
                    }
                    
                    parseHDVValue( currentHDVGroup, iniLine.getKey(), op, iniLine.getValue() );
                }
            }
        }
        
        private int parseEffectiveLine( String line )
        {
            if ( line.startsWith( "}" ) )
            {
                currentLevel = null;
                
                return ( -1 );
            }
            
            if ( currentLevelIndex != usedUpgradeLevel )
                return ( 0 );
            
            if ( line.startsWith( "Description=" ) )
            {
                int dqPos1 = line.indexOf( '"', 12 );
                if ( dqPos1 >= 0 )
                {
                    int dqPos2 = line.indexOf( '"', dqPos1 + 1 );
                    if ( dqPos2 - dqPos1 > 1 )
                    {
                        String desc = line.substring( dqPos1 + 1, dqPos2 );
                        if ( identifier.getDescription() == null )
                            identifier = new VehiclePhysics.UpgradeIdentifier( identifier.getUpgradeType(), identifier.getUpgradeLevel(), desc );
                        else
                            identifier = new VehiclePhysics.UpgradeIdentifier( identifier.getUpgradeType(), identifier.getUpgradeLevel(), identifier.getDescription() + "\n" + desc );
                    }
                }
            }
            else if ( line.startsWith( "HDV=" ) )
            {
                String line2 = line.substring( 4 ).trim();
                if ( !line2.startsWith( "//" ) )
                    parseHDVLine( line2 );
            }
            
            return ( 0 );
        }
        
        public void parse( File file ) throws IOException
        {
            BufferedReader br = null;
            try
            {
                br = new BufferedReader( new FileReader( file ) );
                String line = null;
                int lnr = 0;
                while ( ( line = br.readLine() ) != null )
                {
                    line = line.trim();
                    lnr++;
                    
                    if ( line.equals( "" ) )
                        continue;
                    
                    switch ( hierarchy )
                    {
                        case 0:
                            if ( identifier != null )
                                break;
                            
                            hierarchy += parseTypes( line );
                            break;
                        case 1:
                            hierarchy += parseLevels( lnr, line );
                            break;
                        case 2:
                            hierarchy += parseEffectiveLine( line );
                            break;
                    }
                }
            }
            finally
            {
                if ( br != null )
                    try { br.close(); } catch ( Throwable t ) {}
            }
        }
        
        public UpgradesParser( File path, String searchedUpgradeType, int searchedUpgradeLevel, VehiclePhysics physics )
        {
            this.path = path;
            this.searchedUpgradeType = searchedUpgradeType;
            this.searchedUpgradeLevel = searchedUpgradeLevel;
            this.physics = physics;
        }
    }
    
    private static class VEHParser extends AbstractIniParser
    {
        private final File path;
        private final Object[][] upgradesList;
        private final VehiclePhysics physics;
        
        @Override
        protected boolean acceptMissingTrailingQuote()
        {
            return ( true );
        }
        
        @Override
        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
        {
            if ( group == null )
            {
                if ( key.equals( "HDVehicle" ) )
                {
                    if ( !value.toLowerCase().endsWith( ".hdv" ) )
                        value = value + ".hdv";
                    
                    File hdvFile = locateFile( path, value );
                    if ( hdvFile != null )
                    {
                        try
                        {
                            new HDVParser( hdvFile.getParentFile(), physics ).parse( hdvFile );
                        }
                        catch ( Throwable t )
                        {
                            Logger.log( t );
                            throw new ParsingException( t );
                        }
                    }
                    else
                    {
                        Logger.log( "Warning: Unable to find HDV file \"" + value + "\"." );
                    }
                }
                else if ( key.equals( "Upgrades" ) )
                {
                    if ( upgradesList != null )
                    {
                        if ( !value.toLowerCase().endsWith( ".ini" ) )
                            value = value + ".ini";
                        
                        File upgradesFile = locateFile( path, value );
                        if ( upgradesFile != null )
                        {
                            try
                            {
                                VehiclePhysics.UpgradeIdentifier[] uis = new VehiclePhysics.UpgradeIdentifier[ upgradesList.length ];
                                for ( int i = 0; i < upgradesList.length; i++ )
                                {
                                    Object[] upgrade = upgradesList[i];
                                    UpgradesParser up = new UpgradesParser( upgradesFile.getParentFile(), (String)upgrade[0], (Integer)upgrade[1], physics );
                                    up.parse( upgradesFile );
                                    
                                    if ( up.identifier == null )
                                        uis[i] = new VehiclePhysics.UpgradeIdentifier( (String)upgrade[0], "Upgrade Level not found: " + upgrade[1], "UNKNOWN" );
                                    else
                                        uis[i] = up.identifier;
                                }
                                
                                physics.installedUpgrades = uis;
                            }
                            catch ( Throwable t )
                            {
                                throw new ParsingException( t );
                            }
                        }
                        else
                        {
                            Logger.log( "Warning: Unable to find Upgrades file \"" + value + "\"." );
                        }
                    }
                }
            }
            
            return ( true );
        }
        
        public VEHParser( File path, Object[][] upgradesList, VehiclePhysics physics )
        {
            this.path = path;
            this.upgradesList = upgradesList;
            this.physics = physics;
        }
    }
    
    private static int getTrackConfiguration( File trackConfigBaseFile, String trackName )
    {
        trackName = trackName.toLowerCase();
        
        BufferedReader br = null;
        try
        {
            br = new BufferedReader( new FileReader( trackConfigBaseFile ) );
            
            boolean trackSectionFound = false;
            
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                
                if ( trackSectionFound )
                {
                    if ( line.toLowerCase().startsWith( "track configuration" ) )
                    {
                        int idx = line.indexOf( '=', 19 );
                        if ( idx >= 0 )
                        {
                            return ( Integer.parseInt( line.substring( idx + 1 ).trim() ) );
                        }
                    }
                }
                
                trackSectionFound = ( line.toLowerCase().startsWith( "\"" + trackName + "\":" ) );
            }
            
            return ( -1 );
        }
        catch ( IOException e )
        {
            return ( -1 );
        }
        finally
        {
            if ( br != null )
            {
                try { br.close(); } catch ( IOException e ) {}
            }
        }
    }
    
    private static Object[][] ensureTrackConfiguration( Object[][] upgradesList, File vehicleFolder, String trackName )
    {
        if ( upgradesList != null )
        {
            for ( Object[] oo : upgradesList )
            {
                if ( "TRACK CONFIGURATION".equals( oo[0] ) )
                {
                    return ( upgradesList );
                }
            }
        }
        
        int trackConfig = -1;
        for ( File f : vehicleFolder.listFiles() )
        {
            if ( f.getName().toLowerCase().startsWith( "trackconfigs" ) && f.getName().toLowerCase().endsWith( ".ini" ) )
            {
                trackConfig = getTrackConfiguration( f, trackName );
                
                if ( trackConfig != -1 )
                    break;
            }
        }
        
        if ( trackConfig == -1 )
            return ( upgradesList );
        
        Object[][] upgradesList2 = null;
        if ( upgradesList == null )
        {
            upgradesList2 = new Object[ 1 ][];
        }
        else
        {
            upgradesList2 = new Object[ upgradesList.length + 1 ][];
            System.arraycopy( upgradesList, 0, upgradesList2, 0, upgradesList.length );
        }
        
        upgradesList2[upgradesList2.length - 1] = new Object[] { "TRACK CONFIGURATION", trackConfig };
        
        return ( upgradesList2 );
    }
    
    public static void parsePhysicsFiles( File cchFile, File rFactorFolder, String vehicleFilename, String trackName, VehiclePhysics physics ) throws Throwable
    {
        File vehicleFile = new File( rFactorFolder, vehicleFilename );
        
        CCHParser cchParser = new CCHParser( vehicleFilename );
        cchParser.parse( cchFile );
        
        Object[][] upgradesList = cchParser.getUpgradesList();
        upgradesList = ensureTrackConfiguration( upgradesList, vehicleFile.getParentFile(), trackName );
        
        new VEHParser( vehicleFile.getParentFile(), upgradesList, physics ).parse( vehicleFile );
    }
}
