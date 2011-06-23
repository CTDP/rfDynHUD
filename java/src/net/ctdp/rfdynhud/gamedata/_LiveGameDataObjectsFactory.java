package net.ctdp.rfdynhud.gamedata;

import net.ctdp.rfdynhud.util.PluginINI;

public abstract class _LiveGameDataObjectsFactory
{
    public abstract GameFileSystem newGameFileSystem( PluginINI pluginIni );
    
    public abstract ProfileInfo newProfileInfo( LiveGameData gameData );
    
    public abstract ModInfo newModInfo( LiveGameData gameData );
    
    public abstract TrackInfo newTrackInfo( LiveGameData gameData );
    
    public abstract VehicleInfoParser newVehicleInfoParser( String filename, VehicleInfo info );
    
    public abstract CommentaryRequestInfoCapsule newCommentaryRequestInfoCapsule( LiveGameData gameData );
    
    public abstract GraphicsInfoCapsule newGraphicsInfoCapsule( LiveGameData gameData );
    
    public abstract TelemetryDataCapsule newTelemetryDataCapsule( LiveGameData gameData );
    
    public abstract ScoringInfoCapsule newScoringInfoCapsule( LiveGameData gameData );
    
    public abstract VehicleScoringInfoCapsule newVehicleScoringInfoCapsule( LiveGameData gameData );
    
    public static _LiveGameDataObjectsFactory get( SupportedGames gameId )
    {
        switch ( gameId )
        {
            case rFactor: //rFactor1_v1_255
                return ( new _rf1_LiveGameDataObjectsFactory() );
        }
        
        throw new Error( "Unsupported game: " + gameId );
    }
}
