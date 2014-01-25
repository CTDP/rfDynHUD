package net.ctdp.rfdynhud.gamedata;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.PluginINI;

public interface _LiveGameDataObjectsFactory
{
    /**
     * @return a String, identifying the used simulation.
     */
    public abstract String getGameId();
    
    /**
     * @param rfDynHUD the main {@link RFDynHUD} instance (could be null when in editor mode)
     * @param drawingManager the widgets drawing manager
     * 
     * @return the {@link GameEventsManager}.
     */
    public abstract GameEventsManager newGameEventsManager( RFDynHUD rfDynHUD, WidgetsDrawingManager drawingManager );
    
    public abstract LiveGameData newLiveGameData( GameEventsManager eventsManager );
    
    public abstract GameFileSystem newGameFileSystem( PluginINI pluginIni );
    
    public abstract ProfileInfo newProfileInfo( LiveGameData gameData );
    
    public abstract ModInfo newModInfo( LiveGameData gameData );
    
    public abstract TrackInfo newTrackInfo( LiveGameData gameData );
    
    public abstract VehicleInfo newVehicleInfo( LiveGameData gameData );
    
    public abstract VehiclePhysics newVehiclePhysics( LiveGameData gameData );
    
    public abstract VehicleSetup newVehicleSetup( LiveGameData gameData );
    
    public abstract CommentaryRequestInfo newCommentaryRequestInfo( LiveGameData gameData );
    
    public abstract GraphicsInfo newGraphicsInfo( LiveGameData gameData );
    
    public abstract TelemetryData newTelemetryData( LiveGameData gameData );
    
    public abstract ScoringInfo newScoringInfo( LiveGameData gameData );
    
    public abstract VehicleScoringInfo[] newVehicleScoringInfos( LiveGameData gameData, int count, VehicleScoringInfo[] toRecycle );
    
    public abstract void loadVehiclePhysicsDefaults( VehiclePhysics physics );
    
    public abstract void loadVehiclePhysics( LiveGameData gameData );
    
    public abstract void loadVehicleSetupDefaults( LiveGameData gameData );
    
    public abstract boolean loadVehicleSetupIfChanged( LiveGameData gameData );
}
