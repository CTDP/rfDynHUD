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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import net.ctdp.rfdynhud.util.Logger;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniLine;

class VehiclePhysicsParser
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
        
        setting.set( Float.parseFloat( bv ), Float.parseFloat( ss ), (int)Float.parseFloat( ns ) ); // ILMS has wrong physics and has fractions for the third value. We're simply truncating them.
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
        private final String filename;
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
            
            group = group.toUpperCase();
            
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
            label = label.toUpperCase();
            
            if ( label.equals( "FRONT" ) )
                return ( true );
            
            if ( label.equals( "FRONTLEFT" ) )
                return ( true );
            
            if ( label.equals( "FRONTRIGHT" ) )
                return ( true );
            
            if ( label.equals( "ALL" ) )
                return ( true );
            
            return ( false );
        }
        
        private static final boolean isRearLabel( String label )
        {
            label = label.toUpperCase();
            
            if ( label.equals( "REAR" ) )
                return ( true );
            
            if ( label.equals( "REARLEFT" ) )
                return ( true );
            
            if ( label.equals( "REARRIGHT" ) )
                return ( true );
            
            if ( label.equals( "ALL" ) )
                return ( true );
            
            return ( false );
        }
        
        @Override
        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
        {
            if ( group == null )
                return ( true );
            
            group = group.toUpperCase();
            
            if ( group.equals( "SLIPCURVE" ) )
            {
                if ( key.equalsIgnoreCase( "Name" ) )
                {
                    currentSlipCurve.name = value;
                }
                else if ( key.equalsIgnoreCase( "Step" ) )
                {
                    currentSlipCurve.step = Float.parseFloat( value );
                }
                else if ( key.equalsIgnoreCase( "DropoffFunction" ) )
                {
                    currentSlipCurve.dropoffFunction = Float.parseFloat( value );
                }
            }
            else if ( group.equals( "COMPOUND" ) )
            {
                if ( key.equalsIgnoreCase( "Name" ) )
                {
                    currentCompound.name = value;
                }
                else if ( key.equalsIgnoreCase( "DryLatLong" ) )
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
                else if ( key.equalsIgnoreCase( "Temperatures" ) )
                {
                    if ( currentLabel != null )
                    {
                        if ( isFrontLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.FRONT_LEFT ).setOptimumTemperatureC( Float.parseFloat( value.substring( value.indexOf( "(" ) + 1, value.indexOf( "," ) ) ) );
                            currentCompound.getWheel( Wheel.FRONT_RIGHT ).setOptimumTemperatureC( currentCompound.getWheel( Wheel.FRONT_LEFT ).getOptimumTemperatureC() );
                        }
                        if ( isRearLabel( currentLabel ) )
                        {
                            currentCompound.getWheel( Wheel.REAR_LEFT ).setOptimumTemperatureC( Float.parseFloat( value.substring( value.indexOf( "(" ) + 1, value.indexOf( "," ) ) ) );
                            currentCompound.getWheel( Wheel.REAR_RIGHT ).setOptimumTemperatureC( currentCompound.getWheel( Wheel.REAR_LEFT ).getOptimumTemperatureC() );
                        }
                    }
                }
                else if ( key.equalsIgnoreCase( "OptimumPressure" ) )
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
                else if ( key.equalsIgnoreCase( "TempAndPressGrip" ) ) // old style
                {
                    if ( currentLabel != null )
                    {
                        if ( isFrontLabel( currentLabel ) )
                            frontTempAndPressGrip = value;
                        if ( isRearLabel( currentLabel ) )
                            rearTempAndPressGrip = value;
                    }
                }
                else if ( key.equalsIgnoreCase( "GripTempPress" ) ) // new style
                {
                    if ( currentLabel != null )
                    {
                        if ( isFrontLabel( currentLabel ) )
                            frontGripTempPress = value;
                        if ( isRearLabel( currentLabel ) )
                            rearGripTempPress = value;
                    }
                }
                else if ( key.equalsIgnoreCase( "WearGrip1" ) )
                {
                    wearGrip1 = value;
                }
                else if ( key.equalsIgnoreCase( "WearGrip2" ) )
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
                else if ( key.equalsIgnoreCase( "LatCurve" ) )
                {
                    if ( ( currentLabel == null ) || isFrontLabel( currentLabel ) )
                        currentCompound.frontLatitudeSlipCurve = slipCurves.get( value );
                    if ( ( currentLabel == null ) || isRearLabel( currentLabel ) )
                        currentCompound.rearLatitudeSlipCurve = slipCurves.get( value );
                }
                else if ( key.equalsIgnoreCase( "BrakingCurve" ) )
                {
                    if ( ( currentLabel == null ) || isFrontLabel( currentLabel ) )
                        currentCompound.frontBrakingSlipCurve = slipCurves.get( value );
                    if ( ( currentLabel == null ) || isRearLabel( currentLabel ) )
                        currentCompound.rearBrakingSlipCurve = slipCurves.get( value );
                }
                else if ( key.equalsIgnoreCase( "TractiveCurve" ) )
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
            
            if ( !"Data".equalsIgnoreCase( currentLabel ) || ( currentSlipCurve == null ) )
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
        protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
        {
            Logger.log( "Warning: Unable to parse the line #" + lineNr + " from TBC physics \"" + filename + "\"." );
            Logger.log( "Line was \"" + line + "\". Exception follows." );
            Logger.log( t );
            
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
        
        public TBCParser( String filename, VehiclePhysics physics )
        {
            this.filename = filename;
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
                    new TBCParser( tireFile.getAbsolutePath(), physics ).parse( tireFile );
                    physics.usedTBCFile = tireFile;
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                    //throw new ParsingException( t );
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
        private final String filename;
        private final VehiclePhysics.Engine engine;
        
        public static void parseEngineSetting( String key, String value, String op, VehiclePhysics.Engine engine )
        {
            if ( key.equalsIgnoreCase( "OptimumOilTemp" ) )
            {
                engine.optimumOilTemperature = parseFloat( engine.optimumOilTemperature, Float.parseFloat( value ), op );
            }
            else if ( key.equalsIgnoreCase( "LifetimeOilTemp" ) )
            {
                String[] values = value.substring( 1, value.length() - 1 ).split( "," );
                float v1 = Float.parseFloat( values[0] );
                float v2 = Float.parseFloat( values[1] );
                engine.baseLifetimeOilTemperature = parseFloat( engine.baseLifetimeOilTemperature, v1, op );
                engine.halfLifetimeOilTempOffset = parseFloat( engine.halfLifetimeOilTempOffset, v2, op );
            }
            else if ( key.equalsIgnoreCase( "LifetimeEngineRPM" ) )
            {
                String[] values = value.substring( 1, value.length() - 1 ).split( "," );
                float v1 = Float.parseFloat( values[0] );
                float v2 = Float.parseFloat( values[1] );
                engine.baseLifetimeRPM = parseFloat( engine.baseLifetimeRPM, v1, op );
                engine.halfLifetimeRPMOffset = parseFloat( engine.halfLifetimeRPMOffset, v2, op );
            }
            else if ( key.equalsIgnoreCase( "LifetimeAvg" ) )
            {
                engine.lifetimeAverage = Math.round( parseFloat( engine.lifetimeAverage, Float.parseFloat( value ), op ) );
            }
            else if ( key.equalsIgnoreCase( "LifetimeVar" ) )
            {
                engine.lifetimeVariance = Math.round( parseFloat( engine.lifetimeVariance, Float.parseFloat( value ), op ) );
            }
            else if ( key.equalsIgnoreCase( "RevLimitRange" ) )
            {
                parsePhysicsSetting( value, engine.getRevLimitRange(), op );
            }
            else if ( key.equalsIgnoreCase( "EngineBoostRange" ) )
            {
                parsePhysicsSetting( value, engine.getBoostRange(), op );
            }
            else if ( key.equalsIgnoreCase( "BoostEffects" ) )
            {
                String[] values = value.substring( 1, value.length() - 1 ).split( "," );
                engine.rpmIncreasePerBoostSetting = parseFloat( engine.rpmIncreasePerBoostSetting, Float.parseFloat( values[0] ), op );
                engine.fuelUsageIncreasePerBoostSetting = parseFloat( engine.fuelUsageIncreasePerBoostSetting, Float.parseFloat( values[1] ), op );
                engine.wearIncreasePerBoostSetting = parseFloat( engine.wearIncreasePerBoostSetting, Float.parseFloat( values[2] ), op );
            }
            else if ( key.equalsIgnoreCase( "RamEffects" ) )
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
        
        @Override
        protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
        {
            Logger.log( "Warning: Unable to parse the line #" + lineNr + " from engine physics \"" + filename + "\"." );
            Logger.log( "Line was \"" + line + "\". Exception follows." );
            Logger.log( t );
            
            return ( true );
        }
        
        public EnginePhysicsParser( String filename, VehiclePhysics.Engine engine )
        {
            this.filename = filename;
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
                    new EnginePhysicsParser( engineFile.getAbsolutePath(), engine ).parse( engineFile );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                    //throw new ParsingException( t );
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
        private final String filename;
        private final VehiclePhysics physics;
        
        public static void parseHDVLine( File path, String group, String key, String op, String value, VehiclePhysics physics )
        {
            group = group.toUpperCase();
            
            if ( group.equalsIgnoreCase( "GENERAL" ) )
            {
                if ( key.equalsIgnoreCase( "TireBrand" ) )
                {
                    TBCParser.parseTBCFile( path, value, physics );
                }
                else if ( key.equalsIgnoreCase( "FuelRange" ) )
                {
                    parsePhysicsSetting( value, physics.getFuelRangeL(), op );
                    parsePhysicsSetting( value, physics.getFuelRange(), op );
                }
            }
            else if ( group.equals( "FRONTWING" ) )
            {
                if ( key.equalsIgnoreCase( "FWRange" ) )
                    parsePhysicsSetting( value, physics.getFrontWingRange(), op );
            }
            else if ( group.equals( "ENGINE" ) )
            {
                if ( key.equalsIgnoreCase( "Normal" ) )
                {
                    EnginePhysicsParser.parseEngineFile( path, value, physics.getEngine() );
                }
            }
            else if ( group.equals( "FRONTLEFT" ) )
            {
                if ( key.equalsIgnoreCase( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).getDiscRange(), op );
                else if ( key.equalsIgnoreCase( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.FRONT_LEFT ) );
                else if ( key.equalsIgnoreCase( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equalsIgnoreCase( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).torque, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.FRONT_LEFT ), op );
                else if ( key.equalsIgnoreCase( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_LEFT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "FRONTRIGHT" ) )
            {
                if ( key.equalsIgnoreCase( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).getDiscRange(), op );
                else if ( key.equalsIgnoreCase( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ) );
                else if ( key.equalsIgnoreCase( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equalsIgnoreCase( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).torque, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.FRONT_RIGHT ), op );
                else if ( key.equalsIgnoreCase( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.FRONT_RIGHT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "REARLEFT" ) )
            {
                if ( key.equalsIgnoreCase( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.REAR_LEFT ).getDiscRange(), op );
                else if ( key.equalsIgnoreCase( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.REAR_LEFT ) );
                else if ( key.equalsIgnoreCase( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equalsIgnoreCase( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).torque, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.REAR_LEFT ), op );
                else if ( key.equalsIgnoreCase( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_LEFT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_LEFT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "REARRIGHT" ) )
            {
                if ( key.equalsIgnoreCase( "BrakeDiscRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).getDiscRange(), op );
                else if ( key.equalsIgnoreCase( "BrakeResponseCurve" ) )
                    parseResponseCurve( value, physics.getBrakes().getBrake( Wheel.REAR_RIGHT ) );
                else if ( key.equalsIgnoreCase( "BrakeWearRate" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).wearRate = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).wearRate, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "BrakeFailure" ) )
                {
                    String[] values = parseTuple( value );
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureAverage = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureAverage, Float.parseFloat( values[0] ), op );
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureVariance = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).discFailureVariance, Float.parseFloat( values[1] ), op );
                }
                else if ( key.equalsIgnoreCase( "BrakeTorque" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).torque = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).torque, Float.parseFloat( value ), op );
                else if ( key.equalsIgnoreCase( "PressureRange" ) )
                    parsePhysicsSetting( value, physics.getTirePressureRange( Wheel.REAR_RIGHT ), op );
                else if ( key.equalsIgnoreCase( "BrakeFadeRange" ) )
                    physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).brakeFadeRange = parseFloat( physics.getBrakes().getBrake( Wheel.REAR_RIGHT ).brakeFadeRange, Float.parseFloat( value ), op );
            }
            else if ( group.equals( "CONTROLS" ) )
            {
                if ( key.equalsIgnoreCase( "RearBrakeRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getRearDistributionRange(), op );
                else if ( key.equalsIgnoreCase( "BrakePressureRange" ) )
                    parsePhysicsSetting( value, physics.getBrakes().getPressureRange(), op );
            }
            else if ( group.equals( "DRIVELINE" ) )
            {
                if ( key.equalsIgnoreCase( "WheelDrive" ) )
                    physics.wheelDrive = VehiclePhysics.WheelDrive.valueOf( value.toUpperCase() );
                else if ( key.equalsIgnoreCase( "ForwardGears" ) )
                    physics.numForwardGears = (short)parseInt( physics.numForwardGears, Integer.parseInt( value ), op );
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
        
        @Override
        protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
        {
            Logger.log( "Warning: Unable to parse the line #" + lineNr + " from HDV file \"" + filename + "\"." );
            Logger.log( "Line was \"" + line + "\". Exception follows." );
            Logger.log( t );
            
            return ( true );
        }
        
        public HDVParser( File file, VehiclePhysics physics )
        {
            this.path = file.getParentFile();
            this.filename = file.getAbsolutePath();
            this.physics = physics;
        }
    }
    
    private static class CCHParser extends AbstractIniParser
    {
        private final String filename;
        private final String vehicleFile;
        
        private boolean groupFound = false;
        private boolean upgradeListStarted = false;
        
        private final ArrayList<Object[]> upgradesList = new ArrayList<Object[]>();
        
        public final ArrayList<Object[]> getUpgradesList()
        {
            return ( upgradesList );
        }
        
        @Override
        protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
        {
            if ( groupFound || upgradeListStarted )
                return ( false );
            
            return ( true );
        }
        
        public static final Object[] parseUpgradeSelection( String key, String value )
        {
            try
            {
                String[] params = value.split( "," );
                
                Object[] upgrade = new Object[ 1 + params.length ];
                
                upgrade[0] = key;
                
                for ( int i = 0; i < params.length; i++ )
                    upgrade[1 + i] = Integer.valueOf( params[i] );
                
                return ( upgrade );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
                
                return ( null );
            }
        }
        
        @Override
        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
        {
            if ( upgradeListStarted )
            {
                //if ( !key.equalsIgnoreCase( "Track Configuration" ) )
                {
                    Object[] upgrade = parseUpgradeSelection( key, value );
                    
                    if ( upgrade != null )
                        upgradesList.add( upgrade );
                }
            }
            else if ( group == null )
            {
            }
            else if ( group.equals( "VEHICLE" ) )
            {
                if ( key.equalsIgnoreCase( "File" ) && value.equalsIgnoreCase( vehicleFile ) )
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
                if ( label.equalsIgnoreCase( "UpgradeList" ) )
                {
                    upgradeListStarted = groupFound;
                    
                    return ( true );
                }
            }
            
            return ( false );
        }
        
        @Override
        protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
        {
            Logger.log( "Warning: Unable to parse the line #" + lineNr + " from CCH file \"" + filename + "\"." );
            Logger.log( "Line was \"" + line + "\". Exception follows." );
            Logger.log( t );
            
            return ( true );
        }
        
        public CCHParser( String filename, String vehicleFile )
        {
            this.filename = filename;
            this.vehicleFile = vehicleFile;
        }
    }
    
    private static class UpgradesParser
    {
        private final String filename;
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
        
        /**
         * 
         * @param lnr
         * @param line
         * @return
         */
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
            group = group.toUpperCase();
            
            if ( group.equals( "GENERAL" ) )
            {
                if ( key.equalsIgnoreCase( "TireBrand" ) && op.equals( "=" ) )
                {
                    TBCParser.parseTBCFile( path, value, physics );
                }
                else if ( key.equalsIgnoreCase( "FuelRange" ) )
                {
                    parsePhysicsSetting( value, physics.getFuelRangeL(), op );
                    parsePhysicsSetting( value, physics.getFuelRange(), op );
                }
            }
            else if ( group.equals( "FRONTWING" ) )
            {
                if ( key.equalsIgnoreCase( "FWRange" ) )
                {
                    parsePhysicsSetting( value, physics.getFrontWingRange(), op );
                }
            }
            else if ( group.equalsIgnoreCase( "ENGINE" ) )
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
                        parseHDVValue( currentHDVGroup, iniLine.getKey(), op, iniLine.getValue() );
                    }
                    catch ( Throwable t )
                    {
                        Logger.log( "Warning: Unable to parse upgrade HDV line from upgrades file \"" + filename + "\"." );
                        Logger.log( "Line was \"" + line + "\". Exception follows." );
                        Logger.log( t );
                    }
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
        
        public UpgradesParser( File file, String searchedUpgradeType, int searchedUpgradeLevel, VehiclePhysics physics )
        {
            this.path = file.getParentFile();
            this.filename = file.getAbsolutePath();
            this.searchedUpgradeType = searchedUpgradeType;
            this.searchedUpgradeLevel = searchedUpgradeLevel;
            this.physics = physics;
        }
    }
    
    private static class VEHParser extends AbstractIniParser
    {
        private final File path;
        private final String filename;
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
                if ( key.equalsIgnoreCase( "HDVehicle" ) )
                {
                    if ( !value.toLowerCase().endsWith( ".hdv" ) )
                        value = value + ".hdv";
                    
                    File hdvFile = locateFile( path, value );
                    if ( hdvFile != null )
                    {
                        try
                        {
                            new HDVParser( hdvFile, physics ).parse( hdvFile );
                        }
                        catch ( Throwable t )
                        {
                            Logger.log( t );
                            //throw new ParsingException( t );
                        }
                    }
                    else
                    {
                        Logger.log( "Warning: Unable to find HDV file \"" + value + "\"." );
                    }
                }
                else if ( key.equalsIgnoreCase( "Upgrades" ) )
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
                                    UpgradesParser up = new UpgradesParser( upgradesFile, (String)upgrade[0], (Integer)upgrade[1], physics );
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
                                Logger.log( t );
                                //throw new ParsingException( t );
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
        
        @Override
        protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
        {
            Logger.log( "Warning: Unable to parse the line #" + lineNr + " from engine physics \"" + filename + "\"." );
            Logger.log( "Line was \"" + line + "\". Exception follows." );
            Logger.log( t );
            
            return ( true );
        }
        
        public VEHParser( File file, Object[][] upgradesList, VehiclePhysics physics )
        {
            this.path = file.getParentFile();
            this.filename = file.getAbsolutePath();
            this.upgradesList = upgradesList;
            this.physics = physics;
        }
    }
    
    private static void mergeForcedUpgrade( Object[] upgrade, ArrayList<Object[]> upgradesList )
    {
        String upgradeName = ( (String)upgrade[0] ).toLowerCase();
        
        for ( int i = 0; i < upgradesList.size(); i++ )
        {
            Object[] upgrade2 = upgradesList.get( i );
            String upgradeName2 = ( (String)upgrade2[0] ).toLowerCase();
            
            if ( upgradeName2.equalsIgnoreCase( upgradeName ) )
            {
                upgradesList.set( i, upgrade );
                return;
            }
        }
        
        upgradesList.add( upgrade );
    }
    
    private static boolean loadForcedUpgrades( ArrayList<Object[]> upgradesList, File trackConfigBaseFile, String trackName )
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
                
                if ( ( line.length() == 0 ) || line.startsWith( "//" ) || line.startsWith( "#" ) )
                    continue;
                
                if ( trackSectionFound )
                {
                    int ep = line.indexOf( '=' );
                    
                    if ( ep > 0 )
                    {
                        String key = line.substring( 0, ep ).trim();
                        String value = line.substring( ep + 1 ).trim();
                        
                        Object[] upgrade = CCHParser.parseUpgradeSelection( key, value );
                        
                        if ( upgrade != null )
                            mergeForcedUpgrade( upgrade, upgradesList );
                    }
                }
                
                String lline = line.toLowerCase();
                if ( lline.startsWith( "\"" ) && ( lline.indexOf( "\":" ) > 0 ) )
                {
                    // is track section
                    
                    if ( trackSectionFound )
                    {
                        // another track section has been started. We can quit parsing.
                        
                        return ( true );
                    }
                    
                    trackSectionFound = ( lline.startsWith( "\"" + trackName + "\":" ) );
                }
            }
            
            return ( trackSectionFound );
        }
        catch ( IOException e )
        {
            return ( false );
        }
        finally
        {
            if ( br != null )
            {
                try { br.close(); } catch ( IOException e ) {}
            }
        }
    }
    
    private static final boolean isTrackConfigsIni( File f )
    {
        String lower = f.getName().toLowerCase();
        
        return ( lower.startsWith( "trackconfigs" ) && lower.endsWith( ".ini" ) );
    }
    
    private static void findAndLoadForcedUpgrades( ArrayList<Object[]> upgradesList, File vehicleFolder, String trackName )
    {
        boolean foundTrackConfigsIni = false;
        boolean foundForTrack = false;
        
        for ( File f : vehicleFolder.listFiles() )
        {
            if ( isTrackConfigsIni( f ) )
            {
                foundTrackConfigsIni = true;
                
                foundForTrack = loadForcedUpgrades( upgradesList, f, trackName ) || foundForTrack;
                
                //if ( foundForTrack )
                //    break;
            }
        }
        
        if ( foundTrackConfigsIni && !foundForTrack )
        {
            // If we didn't find forced upgrades for the track name, we try to find the joker...
            
            for ( File f : vehicleFolder.listFiles() )
            {
                if ( isTrackConfigsIni( f ) )
                {
                    foundForTrack = loadForcedUpgrades( upgradesList, f, "*" ) || foundForTrack;
                    
                    //if ( foundForTrack )
                    //    break;
                }
            }
        }
    }
    
    private static final Object[][] simplifyUpgradesList( ArrayList<Object[]> upgradesList )
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
    
    public static void parsePhysicsFiles( File cchFile, File vehicleFile, String trackName, VehiclePhysics physics ) throws Throwable
    {
        CCHParser cchParser = new CCHParser( cchFile.getAbsolutePath(), vehicleFile.getName() );
        cchParser.parse( cchFile );
        
        ArrayList<Object[]> upgradesList = cchParser.getUpgradesList();
        findAndLoadForcedUpgrades( upgradesList, vehicleFile.getParentFile(), trackName );
        
        new VEHParser( vehicleFile, simplifyUpgradesList( upgradesList ), physics ).parse( vehicleFile );
    }
}
