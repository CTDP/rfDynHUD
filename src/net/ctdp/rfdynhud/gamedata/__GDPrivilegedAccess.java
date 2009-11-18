package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;

import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Engine;

public class __GDPrivilegedAccess
{
    public static final void loadEditorDefaults( VehiclePhysics physics )
    {
        physics.loadEditorDefaults();
    }
    
    public static final void loadFromPhysicsFiles( VehiclePhysics physics )
    {
        physics.loadFromPhysicsFiles();
    }
    
    public static final LaptimesRecorder getLaptimesRecorder( ScoringInfo scoringInfo )
    {
        return ( scoringInfo.getLaptimesRecorder() );
    }
    
    public static final void loadFromStream( InputStream in, TelemetryData telemetryData ) throws IOException
    {
        telemetryData.loadFromStream( in );
    }
    
    public static final void loadFromStream( InputStream in, ScoringInfo scoringInfo ) throws IOException
    {
        scoringInfo.loadFromStream( in );
    }
    
    public static final void loadFromStream( InputStream in, CommentaryRequestInfo commentaryInfo ) throws IOException
    {
        commentaryInfo.loadFromStream( in );
    }
    
    public static final void loadFromStream( InputStream in, GraphicsInfo graphicsInfo ) throws IOException
    {
        graphicsInfo.loadFromStream( in );
    }
    
    public static final void resetStintLengths( ScoringInfo scoringInfo )
    {
        scoringInfo.resetStintLengths();
    }
    
    public static final void onSessionStarted( LiveGameData gameData )
    {
        gameData.getScoringInfo().onSessionStarted();
    }
    
    public static final void onRealtimeEntered( LiveGameData gameData )
    {
        gameData.getScoringInfo().onRealtimeEntered();
    }
    
    public static final void updateSessionTime( ScoringInfo scoringInfo )
    {
        scoringInfo.updateSessionTime();
    }
    
    public static final void setEngineBoostMapping( int boost, TelemetryData telemData )
    {
        telemData.setEngineBoostMapping( boost );
    }
    
    public static final void incEngineBoostMapping( TelemetryData telemData, Engine engine )
    {
        telemData.incEngineBoostMapping( engine );
    }
    
    public static final void decEngineBoostMapping( TelemetryData telemData, Engine engine )
    {
        telemData.decEngineBoostMapping( engine );
    }
    
    public static final void setTelemVect3( float x, float y, float z, TelemVect3 vect )
    {
        vect.x = x;
        vect.y = y;
        vect.z = z;
    }
}
