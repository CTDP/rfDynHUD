package net.ctdp.rfdynhud.gamedata.rfactor1;

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

public class _rf1_LiveGameDataObjectsFactory extends _LiveGameDataObjectsFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public GameFileSystem newGameFileSystem( PluginINI pluginIni )
    {
        return ( new _rf1_GameFileSystem( pluginIni ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileInfo newProfileInfo( LiveGameData gameData )
    {
        return ( new _rf1_ProfileInfo( gameData.getFileSystem() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ModInfo newModInfo( LiveGameData gameData )
    {
        return ( new _rf1_ModInfo( gameData.getFileSystem(), gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TrackInfo newTrackInfo( LiveGameData gameData )
    {
        return ( new _rf1_TrackInfo( gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleInfo newVehicleInfo()
    {
        return ( new _rf1_VehicleInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void parseVehicleInfo( File file, String filename, VehicleInfo info ) throws IOException
    {
        new _rf1_VehicleInfoParser( filename, (_rf1_VehicleInfo)info ).parse( file );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _CommentaryRequestInfoCapsule newCommentaryRequestInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_CommentaryRequestInfoCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _GraphicsInfoCapsule newGraphicsInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_GraphicsInfoCapsule( gameData.getScoringInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _TelemetryDataCapsule newTelemetryDataCapsule( LiveGameData gameData )
    {
        return ( new _rf1_TelemetryDataCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _ScoringInfoCapsule newScoringInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_ScoringInfoCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _VehicleScoringInfoCapsule newVehicleScoringInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_VehicleScoringInfoCapsule() );
    }
}
