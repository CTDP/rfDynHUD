package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Engine;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

public class __GDPrivilegedAccess
{
    public static final InputAction INPUT_ACTION_RESET_FUEL_CONSUMPTION = FuelUsageRecorder.INPUT_ACTION_RESET_FUEL_CONSUMPTION;
    public static final InputAction INPUT_ACTION_RESET_TOPSPEEDS = TopspeedRecorder.INPUT_ACTION_RESET_TOPSPEEDS;
    
    public static final boolean updateProfileInfo( ProfileInfo profileInfo )
    {
        return ( profileInfo.update() );
    }
    
    public static final void updateModInfo( ModInfo modInfo )
    {
        modInfo.update();
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
    
    public static final void onSessionStarted2( LiveGameData gameData, EditorPresets editorPresets )
    {
        gameData.onSessionStarted2( editorPresets );
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
    
    public static final void updateSessionTime( LiveGameData gameData, EditorPresets editorPresets, long timestamp )
    {
        gameData.setGamePaused( gameData.getTelemetryData().checkGamePaused( timestamp ), editorPresets );
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
    
    public static Float loadFuelUsageFromCache( final String modName, final String trackName, final String teamName )
    {
        return ( DataCache.loadFuelUsageFromCache( modName, trackName, teamName ) );
    }
    
    public static final boolean setGameResolution( int gameResX, int gameResY, WidgetsConfiguration widgetsConfig )
    {
        return ( widgetsConfig.getGameResolution().setResolution( gameResX, gameResY ) );
    }
    
    public static final boolean setViewport( int x, int y, int w, int h, GameResolution gameRes )
    {
        return ( gameRes.setViewport( x, y, w, h ) );
    }
}
