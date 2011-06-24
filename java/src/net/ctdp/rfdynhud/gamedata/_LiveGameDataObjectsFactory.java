package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.rfactor1._rf1_LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.util.PluginINI;

public abstract class _LiveGameDataObjectsFactory
{
    public abstract GameFileSystem newGameFileSystem( PluginINI pluginIni );
    
    public abstract ProfileInfo newProfileInfo( LiveGameData gameData );
    
    public abstract ModInfo newModInfo( LiveGameData gameData );
    
    public abstract TrackInfo newTrackInfo( LiveGameData gameData );
    
    public abstract VehicleInfo newVehicleInfo();
    
    public abstract void parseVehicleInfo( File file, String filename, VehicleInfo info ) throws IOException;
    
    public abstract _CommentaryRequestInfoCapsule newCommentaryRequestInfoCapsule( LiveGameData gameData );
    
    public abstract _GraphicsInfoCapsule newGraphicsInfoCapsule( LiveGameData gameData );
    
    public abstract _TelemetryDataCapsule newTelemetryDataCapsule( LiveGameData gameData );
    
    public abstract _ScoringInfoCapsule newScoringInfoCapsule( LiveGameData gameData );
    
    public abstract _VehicleScoringInfoCapsule newVehicleScoringInfoCapsule( LiveGameData gameData );
    
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
