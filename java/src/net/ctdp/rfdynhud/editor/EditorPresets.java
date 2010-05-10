package net.ctdp.rfdynhud.editor;

import java.io.IOException;
import java.util.Random;

import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;

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
    
    private final StringProperty driverName = new StringProperty( null, "driverName", "Mike Self" );
    private final FloatProperty lastSector1Time = new FloatProperty( null, "lastSector1Time", 28.829182f, 0f, Float.MAX_VALUE );
    private final FloatProperty lastSector2Time = new FloatProperty( null, "lastSector2Time", 29.413128f, 0f, Float.MAX_VALUE );
    private final FloatProperty lastSector3Time = new FloatProperty( null, "lastSector3Time", 26.336235f, 0f, Float.MAX_VALUE );
    private final FloatProperty currentSector1Time = new FloatProperty( null, "currentSector1Time", 29.138f, 0f, Float.MAX_VALUE );
    private final FloatProperty currentSector2Time = new FloatProperty( null, "currentSector2Time", 27.988f, 0f, Float.MAX_VALUE );
    private final FloatProperty currentSector3Time = new FloatProperty( null, "currentSector3Time", 26.440f, 0f, Float.MAX_VALUE );
    private final EnumProperty<EngineBoostMapping> engineBoost = new EnumProperty<EngineBoostMapping>( null, "engineBoost", EngineBoostMapping.B5 );
    private final IntProperty engineRPM = new IntProperty( null, "engineRPM", 3750, 0, 22000 );
    private final IntProperty engineLifetime = new IntProperty( null, "engineLifetime", 1000, 0, Integer.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessFL = new FloatProperty( null, "brakeDiscThicknessFL", 0.021f, 0f, Float.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessFR = new FloatProperty( null, "brakeDiscThicknessFR", 0.0145f, 0f, Float.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessRL = new FloatProperty( null, "brakeDiscThicknessRL", 0.018f, 0f, Float.MAX_VALUE );
    private final FloatProperty brakeDiscThicknessRR = new FloatProperty( null, "brakeDiscThicknessRR", 0.022f, 0f, Float.MAX_VALUE );
    
    private final float[] topSpeeds = new float[ 22 ];
    
    public final String getDriverName()
    {
        return ( driverName.getStringValue() );
    }
    
    public final float getLastSector1Time()
    {
        return ( lastSector1Time.getFloatValue() );
    }
    
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
    
    public final float getLastSector3Time()
    {
        return ( lastSector3Time.getFloatValue() );
    }
    
    public final float getLastLapTime()
    {
        if ( ( lastSector1Time.getFloatValue() < 0f ) || ( lastSector2Time.getFloatValue() < 0f ) || ( lastSector3Time.getFloatValue() < 0f ) )
            return ( -1f );
        
        return ( lastSector1Time.getFloatValue() + lastSector2Time.getFloatValue() + lastSector3Time.getFloatValue() );
    }
    
    public final Laptime getLastLaptime()
    {
        return ( new Laptime( 23, lastSector1Time.getFloatValue(), lastSector2Time.getFloatValue(), lastSector3Time.getFloatValue(), false, false, true ) );
    }
    
    public final float getCurrentSector1Time()
    {
        return ( currentSector1Time.getFloatValue() );
    }
    
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
    
    public final float getCurrentSector3Time()
    {
        return ( currentSector3Time.getFloatValue() );
    }
    
    public final float getCurrentLapTime()
    {
        if ( ( currentSector1Time.getFloatValue() < 0f ) || ( currentSector2Time.getFloatValue() < 0f ) || ( currentSector3Time.getFloatValue() < 0f ) )
            return ( -1f );
        
        return ( currentSector1Time.getFloatValue() + currentSector2Time.getFloatValue() + currentSector3Time.getFloatValue() );
    }
    
    public final Laptime getCurrentLaptime()
    {
        return ( new Laptime( 24, currentSector1Time.getFloatValue(), currentSector2Time.getFloatValue(), currentSector3Time.getFloatValue(), false, false, true ) );
    }
    
    public final int getEngineBoost()
    {
        return ( engineBoost.getEnumValue().ordinal() + 1 );
    }
    
    public final int getEngineRPM()
    {
        return ( engineRPM.getIntValue() );
    }
    
    public final int getEngineLifetime()
    {
        return ( engineLifetime.getIntValue() );
    }
    
    public final float getBrakeDiscThicknessFL()
    {
        return ( brakeDiscThicknessFL.getFloatValue() );
    }
    
    public final float getBrakeDiscThicknessFR()
    {
        return ( brakeDiscThicknessFR.getFloatValue() );
    }
    
    public final float getBrakeDiscThicknessRL()
    {
        return ( brakeDiscThicknessRL.getFloatValue() );
    }
    
    public final float getBrakeDiscThicknessRR()
    {
        return ( brakeDiscThicknessRR.getFloatValue() );
    }
    
    public final float getTopSpeed( int index )
    {
        return ( topSpeeds[index] );
    }
    
    void getProperties( WidgetPropertiesContainer propsCont )
    {
        propsCont.addGroup( "Driver" );
        
        propsCont.addProperty( driverName );
        
        propsCont.addGroup( "Lap Times" );
        
        propsCont.addProperty( lastSector1Time );
        propsCont.addProperty( lastSector2Time );
        propsCont.addProperty( lastSector3Time );
        propsCont.addProperty( currentSector1Time );
        propsCont.addProperty( currentSector2Time );
        propsCont.addProperty( currentSector3Time );
        
        propsCont.addGroup( "Engine" );
        
        propsCont.addProperty( engineBoost );
        propsCont.addProperty( engineRPM );
        propsCont.addProperty( engineLifetime );
        
        propsCont.addGroup( "Brakes" );
        
        propsCont.addProperty( brakeDiscThicknessFL );
        propsCont.addProperty( brakeDiscThicknessFR );
        propsCont.addProperty( brakeDiscThicknessRL );
        propsCont.addProperty( brakeDiscThicknessRR );
    }
    
    void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( driverName, null );
        writer.writeProperty( lastSector1Time, null );
        writer.writeProperty( lastSector2Time, null );
        writer.writeProperty( lastSector3Time, null );
        writer.writeProperty( currentSector1Time, null );
        writer.writeProperty( currentSector2Time, null );
        writer.writeProperty( currentSector3Time, null );
        writer.writeProperty( engineBoost, null );
        writer.writeProperty( engineRPM, null );
        writer.writeProperty( engineLifetime, null );
        writer.writeProperty( brakeDiscThicknessFL, null );
        writer.writeProperty( brakeDiscThicknessFR, null );
        writer.writeProperty( brakeDiscThicknessRL, null );
        writer.writeProperty( brakeDiscThicknessRR, null );
    }
    
    void loadProperty( String key, String value )
    {
        if ( driverName.loadProperty( key, value ) );
        else if ( lastSector1Time.loadProperty( key, value ) );
        else if ( lastSector2Time.loadProperty( key, value ) );
        else if ( lastSector3Time.loadProperty( key, value ) );
        else if ( currentSector1Time.loadProperty( key, value ) );
        else if ( currentSector2Time.loadProperty( key, value ) );
        else if ( currentSector3Time.loadProperty( key, value ) );
        else if ( engineBoost.loadProperty( key, value ) );
        else if ( engineRPM.loadProperty( key, value ) );
        else if ( engineLifetime.loadProperty( key, value ) );
        else if ( brakeDiscThicknessFL.loadProperty( key, value ) );
        else if ( brakeDiscThicknessFR.loadProperty( key, value ) );
        else if ( brakeDiscThicknessRL.loadProperty( key, value ) );
        else if ( brakeDiscThicknessRR.loadProperty( key, value ) );
    }
    
    public EditorPresets()
    {
        Random rnd = new Random( System.nanoTime() );
        
        for ( int i = 0; i < topSpeeds.length; i++ )
        {
            topSpeeds[i] = 250f + rnd.nextFloat() * 55f;
        }
    }
}
