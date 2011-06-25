package net.ctdp.rfdynhud.gamedata.rfactor2;

import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata._CommentaryRequestInfoCapsule;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata._GraphicsInfoCapsule;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.gamedata._ScoringInfoCapsule;
import net.ctdp.rfdynhud.gamedata._TelemetryDataCapsule;
import net.ctdp.rfdynhud.gamedata.TrackInfo;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.gamedata._VehicleScoringInfoCapsule;
import net.ctdp.rfdynhud.gamedata._LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.util.PluginINI;

public class _rf2_LiveGameDataObjectsFactory extends _LiveGameDataObjectsFactory
{
    public static final String GAME_ID = "rFactor2";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getGameId()
    {
        return ( GAME_ID );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GameFileSystem newGameFileSystem( PluginINI pluginIni )
    {
        return ( new _rf2_GameFileSystem( pluginIni ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileInfo newProfileInfo( LiveGameData gameData )
    {
        return ( new _rf2_ProfileInfo( gameData.getFileSystem() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ModInfo newModInfo( LiveGameData gameData )
    {
        return ( new _rf2_ModInfo( gameData.getFileSystem(), gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TrackInfo newTrackInfo( LiveGameData gameData )
    {
        return ( new _rf2_TrackInfo( gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleInfo newVehicleInfo()
    {
        return ( new _rf2_VehicleInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void parseVehicleInfo( File file, String filename, VehicleInfo info ) throws IOException
    {
        new _rf2_VehicleInfoParser( filename, (_rf2_VehicleInfo)info ).parse( file );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _CommentaryRequestInfoCapsule newCommentaryRequestInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf2_CommentaryRequestInfoCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _GraphicsInfoCapsule newGraphicsInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf2_GraphicsInfoCapsule( gameData.getScoringInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _TelemetryDataCapsule newTelemetryDataCapsule( LiveGameData gameData )
    {
        return ( new _rf2_TelemetryDataCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _ScoringInfoCapsule newScoringInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf2_ScoringInfoCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _VehicleScoringInfoCapsule newVehicleScoringInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf2_VehicleScoringInfoCapsule() );
    }
}
