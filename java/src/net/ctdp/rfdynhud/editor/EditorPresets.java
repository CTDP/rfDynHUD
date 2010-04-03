package net.ctdp.rfdynhud.editor;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.FloatProperty;
import net.ctdp.rfdynhud.editor.properties.IntegerProperty;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;

public class EditorPresets
{
    private final IntegerProperty engineRPM = new IntegerProperty( null, "engineRPM", 3750 );
    private final IntegerProperty engineLifetime = new IntegerProperty( null, "engineLifetime", 1000 );
    private final FloatProperty brakeDiscThicknessFL = new FloatProperty( null, "brakeDiscThicknessFL", 0.021f );
    private final FloatProperty brakeDiscThicknessFR = new FloatProperty( null, "brakeDiscThicknessFR", 0.0145f );
    private final FloatProperty brakeDiscThicknessRL = new FloatProperty( null, "brakeDiscThicknessRL", 0.018f );
    private final FloatProperty brakeDiscThicknessRR = new FloatProperty( null, "brakeDiscThicknessRR", 0.022f );
    
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
    
    void getProperties( FlaggedList props )
    {
        props.add( engineRPM );
        props.add( engineLifetime );
        props.add( brakeDiscThicknessFL );
        props.add( brakeDiscThicknessFR );
        props.add( brakeDiscThicknessRL );
        props.add( brakeDiscThicknessRR );
    }
    
    void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( engineRPM, null );
        writer.writeProperty( engineLifetime, null );
        writer.writeProperty( brakeDiscThicknessFL, null );
        writer.writeProperty( brakeDiscThicknessFR, null );
        writer.writeProperty( brakeDiscThicknessRL, null );
        writer.writeProperty( brakeDiscThicknessRR, null );
    }
    
    void loadProperty( String key, String value )
    {
        if ( engineRPM.loadProperty( key, value ) );
        else if ( engineLifetime.loadProperty( key, value ) );
        else if ( brakeDiscThicknessFL.loadProperty( key, value ) );
        else if ( brakeDiscThicknessFR.loadProperty( key, value ) );
        else if ( brakeDiscThicknessRL.loadProperty( key, value ) );
        else if ( brakeDiscThicknessRR.loadProperty( key, value ) );
    }
}
