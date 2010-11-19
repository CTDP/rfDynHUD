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
package net.ctdp.rfdynhud.editor;

import java.io.IOException;
import java.util.Random;

import net.ctdp.rfdynhud.properties.AbstractPropertiesKeeper;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FlatPropertiesContainer;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.util.PropertyWriter;

/**
 * Presets for telemetry and scoring info in the editor.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorPresets
{
    private static enum EngineBoostMapping
    {
        B1,
        B2,
        B3,
        B4,
        B5,
        B6,
        B7,
        B8,
        B9,
        ;
    }
    
    private final StringProperty driverName = new StringProperty( "driverName", "Mike Self" );
    private final FloatProperty lastSector1Time = new FloatProperty( "lastSector1Time", 27.729182f, 0f, Float.MAX_VALUE );
    private final FloatProperty lastSector2Time = new FloatProperty( "lastSector2Time", 29.413128f, 0f, Float.MAX_VALUE );
    private final FloatProperty lastSector3Time = new FloatProperty( "lastSector3Time", 26.336235f, 0f, Float.MAX_VALUE );
    private final FloatProperty currentSector1Time = new FloatProperty( "currentSector1Time", 29.138f, 0f, Float.MAX_VALUE );
    private final FloatProperty currentSector2Time = new FloatProperty( "currentSector2Time", 27.988f, 0f, Float.MAX_VALUE );
    private final EnumProperty<EngineBoostMapping> engineBoost = new EnumProperty<EngineBoostMapping>( "engineBoost", EngineBoostMapping.B5 );
    private final IntProperty engineRPM = new IntProperty( "engineRPM", 3750, 0, 22000 );
    private final IntProperty engineLifetime = new IntProperty( "engineLifetime", 1000, 0, Integer.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessFL = new FloatProperty( "brakeDiscThicknessFL", 0.021f, 0f, Float.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessFR = new FloatProperty( "brakeDiscThicknessFR", 0.0145f, 0f, Float.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessRL = new FloatProperty( "brakeDiscThicknessRL", 0.018f, 0f, Float.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessRR = new FloatProperty( "brakeDiscThicknessRR", 0.022f, 0f, Float.MAX_VALUE );
    private final FloatProperty fuelLoad = new FloatProperty( "fuelLoad", 90.0f, 0f, 300f );
    
    private final float[] topSpeeds = new float[ 22 ];
    
    /**
     * Gets the driver's name.
     * 
     * @return the driver's name.
     */
    public final String getDriverName()
    {
        return ( driverName.getStringValue() );
    }
    
    /**
     * Gets the last sector 1 time.
     * 
     * @return the last sector 1 time.
     */
    public final float getLastSector1Time()
    {
        return ( lastSector1Time.getFloatValue() );
    }
    
    /**
     * Gets the last sector 2 time.
     * 
     * @param includeSector1 sum up sector 1 and 2?
     * 
     * @return the last sector 2 time.
     */
    public final float getLastSector2Time( boolean includeSector1 )
    {
        if ( includeSector1 )
        {
            if ( ( lastSector1Time.getFloatValue() < 0f ) || ( lastSector2Time.getFloatValue() < 0f ) )
                return ( -1f );
            
            return ( lastSector1Time.getFloatValue() + lastSector2Time.getFloatValue() );
        }
        
        return ( lastSector2Time.getFloatValue() );
    }
    
    /**
     * Gets the last sector 3 time.
     * 
     * @return the last sector 3 time.
     */
    public final float getLastSector3Time()
    {
        return ( lastSector3Time.getFloatValue() );
    }
    
    /**
     * Gets the last lap time.
     * 
     * @return the last lap time.
     */
    public final float getLastLapTime()
    {
        if ( ( lastSector1Time.getFloatValue() < 0f ) || ( lastSector2Time.getFloatValue() < 0f ) || ( lastSector3Time.getFloatValue() < 0f ) )
            return ( -1f );
        
        return ( lastSector1Time.getFloatValue() + lastSector2Time.getFloatValue() + lastSector3Time.getFloatValue() );
    }
    
    /**
     * Gets the current sector 1 time.
     * 
     * @return the current sector 1 time.
     */
    public final float getCurrentSector1Time()
    {
        return ( currentSector1Time.getFloatValue() );
    }
    
    /**
     * Gets the current sector 2 time.
     * 
     * @param includeSector1 sum up sector 1 and 2?
     * 
     * @return the current sector 2 time.
     */
    public final float getCurrentSector2Time( boolean includeSector1 )
    {
        if ( includeSector1 )
        {
            if ( ( currentSector1Time.getFloatValue() < 0f ) || ( currentSector2Time.getFloatValue() < 0f ) )
                return ( -1f );
            
            return ( currentSector1Time.getFloatValue() + currentSector2Time.getFloatValue() );
        }
        
        return ( currentSector2Time.getFloatValue() );
    }
    
    /**
     * Gets the engine boost.
     * 
     * @return the engine boost.
     */
    public final int getEngineBoost()
    {
        return ( engineBoost.getEnumValue().ordinal() + 1 );
    }
    
    /**
     * Gets the engine RPM.
     * 
     * @return the engine RPM.
     */
    public final int getEngineRPM()
    {
        return ( engineRPM.getIntValue() );
    }
    
    /**
     * Gets the engine lifetime in seconds.
     * 
     * @return the engine lifetime in seconds.
     */
    public final int getEngineLifetime()
    {
        return ( engineLifetime.getIntValue() );
    }
    
    /**
     * Gets the front left brake disc thickness in meters.
     * 
     * @return the front left brake disc thickness in meters.
     */
    public final float getBrakeDiscThicknessFL()
    {
        return ( brakeDiscThicknessFL.getFloatValue() );
    }
    
    /**
     * Gets the front right brake disc thickness in meters.
     * 
     * @return the front right brake disc thickness in meters.
     */
    public final float getBrakeDiscThicknessFR()
    {
        return ( brakeDiscThicknessFR.getFloatValue() );
    }
    
    /**
     * Gets the rear left brake disc thickness in meters.
     * 
     * @return the rear left brake disc thickness in meters.
     */
    public final float getBrakeDiscThicknessRL()
    {
        return ( brakeDiscThicknessRL.getFloatValue() );
    }
    
    /**
     * Gets the rear right brake disc thickness in meters.
     * 
     * @return the rear right brake disc thickness in meters.
     */
    public final float getBrakeDiscThicknessRR()
    {
        return ( brakeDiscThicknessRR.getFloatValue() );
    }
    
    /**
     * Gets the fuel load.
     * 
     * @return the fuel load.
     */
    public final float getFuelLoad()
    {
        return ( fuelLoad.getFloatValue() );
    }
    
    /**
     * Gets the top speed in km/h.
     * 
     * @param index vehicle index
     * 
     * @return the top speed in km/h.
     */
    public final float getTopSpeed( int index )
    {
        return ( topSpeeds[index] );
    }
    
    void saveProperties( PropertyWriter writer ) throws IOException
    {
        writer.writeProperty( driverName, null );
        writer.writeProperty( lastSector1Time, null );
        writer.writeProperty( lastSector2Time, null );
        writer.writeProperty( lastSector3Time, null );
        writer.writeProperty( currentSector1Time, null );
        writer.writeProperty( currentSector2Time, null );
        writer.writeProperty( engineBoost, null );
        writer.writeProperty( engineRPM, null );
        writer.writeProperty( engineLifetime, null );
        writer.writeProperty( brakeDiscThicknessFL, null );
        writer.writeProperty( brakeDiscThicknessFR, null );
        writer.writeProperty( brakeDiscThicknessRL, null );
        writer.writeProperty( brakeDiscThicknessRR, null );
        writer.writeProperty( fuelLoad, null );
    }
    
    void loadProperty( PropertyLoader loader )
    {
        if ( loader.loadProperty( driverName ) );
        else if ( loader.loadProperty( lastSector1Time ) );
        else if ( loader.loadProperty( lastSector2Time ) );
        else if ( loader.loadProperty( lastSector3Time ) );
        else if ( loader.loadProperty( currentSector1Time ) );
        else if ( loader.loadProperty( currentSector2Time ) );
        else if ( loader.loadProperty( engineBoost ) );
        else if ( loader.loadProperty( engineRPM ) );
        else if ( loader.loadProperty( engineLifetime ) );
        else if ( loader.loadProperty( brakeDiscThicknessFL ) );
        else if ( loader.loadProperty( brakeDiscThicknessFR ) );
        else if ( loader.loadProperty( brakeDiscThicknessRL ) );
        else if ( loader.loadProperty( brakeDiscThicknessRR ) );
        else if ( loader.loadProperty( fuelLoad ) );
    }
    
    void getProperties( PropertiesContainer propsCont )
    {
        propsCont.addGroup( "Driver" );
        
        propsCont.addProperty( driverName );
        
        propsCont.addGroup( "Lap Times" );
        
        propsCont.addProperty( lastSector1Time );
        propsCont.addProperty( lastSector2Time );
        propsCont.addProperty( lastSector3Time );
        propsCont.addProperty( currentSector1Time );
        propsCont.addProperty( currentSector2Time );
        
        propsCont.addGroup( "Engine" );
        
        propsCont.addProperty( engineBoost );
        propsCont.addProperty( engineRPM );
        propsCont.addProperty( engineLifetime );
        
        propsCont.addGroup( "Brakes" );
        
        propsCont.addProperty( brakeDiscThicknessFL );
        propsCont.addProperty( brakeDiscThicknessFR );
        propsCont.addProperty( brakeDiscThicknessRL );
        propsCont.addProperty( brakeDiscThicknessRR );
        
        propsCont.addGroup( "Fuel" );
        
        propsCont.addProperty( fuelLoad );
    }
    
    /**
     * Constructs {@link EditorPresets}.
     */
    public EditorPresets()
    {
        Random rnd = new Random( System.nanoTime() );
        
        for ( int i = 0; i < topSpeeds.length; i++ )
        {
            topSpeeds[i] = 250f + rnd.nextFloat() * 55f;
        }
        
        FlatPropertiesContainer pc = new FlatPropertiesContainer();
        
        getProperties( pc );
        
        for ( int i = 0; i < pc.getList().size(); i++ )
        {
            AbstractPropertiesKeeper.setKeeper( pc.getList().get( i ), null );
        }
   }
}
