package net.ctdp.rfdynhud.gamedata;

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
        return ( new _rf1_ModInfo( gameData.getFileSystem(), gameData.getProfileInfo(), this ) );
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
    public VehicleInfoParser newVehicleInfoParser( String filename, VehicleInfo info )
    {
        return ( new _rf1_VehicleInfoParser( filename, info ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommentaryRequestInfoCapsule newCommentaryRequestInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_CommentaryRequestInfoCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GraphicsInfoCapsule newGraphicsInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_GraphicsInfoCapsule( gameData.getScoringInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TelemetryDataCapsule newTelemetryDataCapsule( LiveGameData gameData )
    {
        return ( new _rf1_TelemetryDataCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ScoringInfoCapsule newScoringInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_ScoringInfoCapsule() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleScoringInfoCapsule newVehicleScoringInfoCapsule( LiveGameData gameData )
    {
        return ( new _rf1_VehicleScoringInfoCapsule() );
    }
}
