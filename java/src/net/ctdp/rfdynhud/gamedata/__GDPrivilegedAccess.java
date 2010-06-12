package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Engine;
import net.ctdp.rfdynhud.input.InputAction;

public class __GDPrivilegedAccess
{
    public static final InputAction INPUT_ACTION_RESET_FUEL_CONSUMPTION = FuelUsageRecorder.INPUT_ACTION_RESET_FUEL_CONSUMPTION;
    public static final InputAction INPUT_ACTION_RESET_TOPSPEEDS = TopspeedRecorder.INPUT_ACTION_RESET_TOPSPEEDS;
    
    public static final boolean updateProfileInfo( ProfileInfo profileInfo )
    {
        return ( profileInfo.update() );
    }
    
    public static final void updateTrackInfo( TrackInfo trackInfo )
    {
        trackInfo.update();
    }
    
    public static final void updateInfo( LiveGameData gameData )
    {
        if ( gameData.getProfileInfo().update() )
        {
            gameData.getModInfo().update();
            gameData.getTrackInfo().update();
            
            gameData.getPhysics().applyMeasurementUnits( gameData.getProfileInfo().getMeasurementUnits() );
        }
    }
    
    public static final void loadEditorDefaults( VehiclePhysics physics )
    {
        physics.loadEditorDefaults();
    }
    
    public static final void loadFromPhysicsFiles( ProfileInfo profileInfo, TrackInfo trackInfo, VehiclePhysics physics )
    {
        physics.loadFromPhysicsFiles( profileInfo, trackInfo );
    }
    
    public static void parsePhysicsFiles( File cchFile, File rFactorFolder, String vehicleFilename, String trackName, VehiclePhysics physics ) throws Throwable
    {
        VehiclePhysicsParser.parsePhysicsFiles( cchFile, rFactorFolder, vehicleFilename, trackName, physics );
    }
    
    public static final boolean loadSetup( boolean isEditorMode, LiveGameData gameData )
    {
        return ( VehicleSetupParser.loadSetup( isEditorMode, gameData ) );
    }
    
    public static final void applyEditorPresets( EditorPresets editorPresets, LiveGameData gameData )
    {
        gameData.applyEditorPresets( editorPresets );
    }
    
    public static final LaptimesRecorder getLaptimesRecorder( ScoringInfo scoringInfo )
    {
        return ( scoringInfo.getLaptimesRecorder() );
    }
    
    public static final void loadFromStream( InputStream in, TelemetryData telemetryData, EditorPresets editorPresets ) throws IOException
    {
        telemetryData.loadFromStream( in, editorPresets );
    }
    
    public static final void loadFromStream( InputStream in, ScoringInfo scoringInfo, EditorPresets editorPresets ) throws IOException
    {
        scoringInfo.loadFromStream( in, editorPresets );
    }
    
    public static final void loadFromStream( InputStream in, CommentaryRequestInfo commentaryInfo, EditorPresets editorPresets ) throws IOException
    {
        commentaryInfo.loadFromStream( in, editorPresets );
    }
    
    public static final void loadFromStream( InputStream in, GraphicsInfo graphicsInfo, EditorPresets editorPresets ) throws IOException
    {
        graphicsInfo.loadFromStream( in, editorPresets );
    }
    
    public static final void onSessionStarted( LiveGameData gameData, EditorPresets editorPresets )
    {
        gameData.getTelemetryData().onSessionStarted( editorPresets );
        gameData.getScoringInfo().onSessionStarted( editorPresets );
        gameData.getSetup().onSessionStarted();
    }
    
    public static final void onSessionEnded( LiveGameData gameData )
    {
        gameData.getTelemetryData().onSessionEnded();
        gameData.getScoringInfo().onSessionEnded();
        gameData.getSetup().onSessionEnded();
    }
    
    public static final void setRealtimeMode( boolean realtimeMode, LiveGameData gameData, EditorPresets editorPresets )
    {
        gameData.setRealtimeMode( realtimeMode, editorPresets );
    }
    
    public static final void updateSessionTime( LiveGameData gameData, long timestamp )
    {
        gameData.gamePaused = gameData.getTelemetryData().checkGamePaused( timestamp );
        gameData.getScoringInfo().updateSessionTime( timestamp );
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
    
    public static final void setTempBoostFlag( TelemetryData telemData, boolean tempBoostFlag )
    {
        telemData.setTempBoostFlag( tempBoostFlag );
    }
    
    public static final void setTelemVect3( float x, float y, float z, TelemVect3 vect )
    {
        vect.x = x;
        vect.y = y;
        vect.z = z;
    }
}
