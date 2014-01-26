/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.gamedata.rfactor2;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;
import net.ctdp.rfdynhud.gamedata.CommentaryRequestInfo;
import net.ctdp.rfdynhud.gamedata.DrivingAids;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.GraphicsInfo;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ModInfo;
import net.ctdp.rfdynhud.gamedata.ProfileInfo;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.TrackInfo;
import net.ctdp.rfdynhud.gamedata.VehicleInfo;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata._LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.PluginINI;
import net.ctdp.rfdynhud.util.RFDHLog;

public class _rf2_LiveGameDataObjectsFactory implements _LiveGameDataObjectsFactory
{
    static
    {
        if ( __EDPrivilegedAccess.editorClassLoader == null )
        {
            try
            {
                System.loadLibrary( "rfdynhud4rf2" );
            }
            catch ( Throwable t )
            {
                RFDHLog.exception( t );
            }
        }
    }
    
    public static final String GAME_ID = "rFactor2";
    
    private final String dataPath;
    
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
    public _rf2_GameEventsManager newGameEventsManager( RFDynHUD rfDynHUD, WidgetsDrawingManager drawingManager )
    {
        return ( new _rf2_GameEventsManager( rfDynHUD, drawingManager, this ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public LiveGameData newLiveGameData( GameEventsManager eventsManager )
    {
        return ( new LiveGameData( eventsManager, this ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GameFileSystem newGameFileSystem( PluginINI pluginIni )
    {
        return ( new _rf2_GameFileSystem( dataPath, pluginIni ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileInfo newProfileInfo( LiveGameData gameData )
    {
        return ( new _rf2_ProfileInfo( (_rf2_GameFileSystem)gameData.getFileSystem() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ModInfo newModInfo( LiveGameData gameData )
    {
        return ( new _rf2_ModInfo( gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TrackInfo newTrackInfo( LiveGameData gameData )
    {
        return ( new _rf2_TrackInfo( (_rf2_ProfileInfo)gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleInfo newVehicleInfo( LiveGameData gameData )
    {
        return ( new _rf2_VehicleInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehiclePhysics newVehiclePhysics( LiveGameData gameData )
    {
        return ( new _rf2_VehiclePhysics() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleSetup newVehicleSetup( LiveGameData gameData )
    {
        return ( new _rf2_VehicleSetup() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DrivingAids newDrivingAids( LiveGameData gameData )
    {
        return ( new _rf2_DrivingAids( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CommentaryRequestInfo newCommentaryRequestInfo( LiveGameData gameData )
    {
        return ( new _rf2_CommentaryRequestInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GraphicsInfo newGraphicsInfo( LiveGameData gameData )
    {
        return ( new _rf2_GraphicsInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TelemetryData newTelemetryData( LiveGameData gameData )
    {
        return ( new _rf2_TelemetryData( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ScoringInfo newScoringInfo( LiveGameData gameData )
    {
        return ( new _rf2_ScoringInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleScoringInfo[] newVehicleScoringInfos( LiveGameData gameData, int count, VehicleScoringInfo[] toRecycle )
    {
        byte[] buffer = new byte[ _rf2_VehicleScoringInfo.BUFFER_SIZE * count ];
        
        _rf2_VehicleScoringInfo[] result = new _rf2_VehicleScoringInfo[ count ];
        
        for ( int i = 0; i < count; i++ )
            result[i] = new _rf2_VehicleScoringInfo( gameData.getScoringInfo(), gameData.getProfileInfo(), gameData, buffer, i * _rf2_VehicleScoringInfo.BUFFER_SIZE );
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadVehiclePhysicsDefaults( VehiclePhysics physics )
    {
        ( (_rf2_VehiclePhysics)physics ).loadDefaults();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadVehiclePhysics( LiveGameData gameData )
    {
        // TODO
        //_rf2_VehiclePhysicsParser.loadFromPhysicsFiles( (_rf2_ProfileInfo)gameData.getProfileInfo(), (_rf2_TrackInfo)gameData.getTrackInfo(), (_rf2_VehiclePhysics)gameData.getPhysics() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadVehicleSetupDefaults( LiveGameData gameData )
    {
        _rf2_VehicleSetupParser.loadDefaultSetup( (_rf2_VehiclePhysics)gameData.getPhysics(), (_rf2_VehicleSetup)gameData.getSetup() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadVehicleSetupIfChanged( LiveGameData gameData )
    {
        return ( _rf2_VehicleSetupParser.loadSetupIfChanged( gameData ) );
    }
    
    public _rf2_LiveGameDataObjectsFactory( byte[] dataPath, int dataPathLength )
    {
        this.dataPath = new String( dataPath, 0, dataPathLength );
    }
}
