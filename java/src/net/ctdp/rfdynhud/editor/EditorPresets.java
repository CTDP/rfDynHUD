package net.ctdp.rfdynhud.editor;

import java.io.IOException;

import net.ctdp.rfdynhud.properties.FloatProperty;
import net.ctdp.rfdynhud.properties.IntegerProperty;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;

public class EditorPresets
{
    private final StringProperty driverName = new StringProperty( null, "driverName", "Mike Self" );
    private final FloatProperty lastSector1Time = new FloatProperty( null, "lastSector1Time", 28.829182f );
    private final FloatProperty lastSector2Time = new FloatProperty( null, "lastSector2Time", 29.413128f );
    private final FloatProperty lastSector3Time = new FloatProperty( null, "lastSector3Time", 26.336235f );
    private final FloatProperty currentSector1Time = new FloatProperty( null, "currentSector1Time", 29.138f );
    private final FloatProperty currentSector2Time = new FloatProperty( null, "currentSector2Time", 27.988f );
    private final FloatProperty currentSector3Time = new FloatProperty( null, "currentSector3Time", 26.440f );
    private final IntegerProperty engineRPM = new IntegerProperty( null, "engineRPM", 3750 );
    private final IntegerProperty engineLifetime = new IntegerProperty( null, "engineLifetime", 1000 );
    private final FloatProperty brakeDiscThicknessFL = new FloatProperty( null, "brakeDiscThicknessFL", 0.021f );
    private final FloatProperty brakeDiscThicknessFR = new FloatProperty( null, "brakeDiscThicknessFR", 0.0145f );
    private final FloatProperty brakeDiscThicknessRL = new FloatProperty( null, "brakeDiscThicknessRL", 0.018f );
    private final FloatProperty brakeDiscThicknessRR = new FloatProperty( null, "brakeDiscThicknessRR", 0.022f );
    
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
    
    public final float getLastLaptime()
    {
        if ( ( lastSector1Time.getFloatValue() < 0f ) || ( lastSector2Time.getFloatValue() < 0f ) || ( lastSector3Time.getFloatValue() < 0f ) )
            return ( -1f );
        
        return ( lastSector1Time.getFloatValue() + lastSector2Time.getFloatValue() + lastSector3Time.getFloatValue() );
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
    
    public final float getCurrentLaptime()
    {
        if ( ( currentSector1Time.getFloatValue() < 0f ) || ( currentSector2Time.getFloatValue() < 0f ) || ( currentSector3Time.getFloatValue() < 0f ) )
            return ( -1f );
        
        return ( currentSector1Time.getFloatValue() + currentSector2Time.getFloatValue() + currentSector3Time.getFloatValue() );
    }
    
    public int getEngineRPM()
    {
        return ( engineRPM.getIntegerValue() );
    }
    
    public final int getEngineLifetime()
    {
        return ( engineLifetime.getIntegerValue() );
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
        else if ( engineRPM.loadProperty( key, value ) );
        else if ( engineLifetime.loadProperty( key, value ) );
        else if ( brakeDiscThicknessFL.loadProperty( key, value ) );
        else if ( brakeDiscThicknessFR.loadProperty( key, value ) );
        else if ( brakeDiscThicknessRL.loadProperty( key, value ) );
        else if ( brakeDiscThicknessRR.loadProperty( key, value ) );
    }
}
