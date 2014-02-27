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
package net.ctdp.rfdynhud.gamedata.rfactor1;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata._LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.PluginINI;
import net.ctdp.rfdynhud.util.RFDHLog;

public class _rf1_LiveGameDataObjectsFactory implements _LiveGameDataObjectsFactory
{
    public static final String GAME_ID = "rFactor1";
    
    private static final String NATIVE_LIB_NAME = "rfdynhud4rf1";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init( boolean isEditorMode, boolean isSimulationMode )
    {
        if ( !isEditorMode && !isSimulationMode )
        {
            try
            {
                System.loadLibrary( NATIVE_LIB_NAME );
            }
            catch ( UnsatisfiedLinkError e )
            {
                RFDHLog.error( "[ERROR] Couldn't find " + NATIVE_LIB_NAME + ".dll" );
            }
            catch ( Throwable t )
            {
                RFDHLog.exception( t );
            }
        }
    }
    
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
    public _rf1_GameEventsManager newGameEventsManager( RFDynHUD rfDynHUD, WidgetsDrawingManager drawingManager )
    {
        return ( new _rf1_GameEventsManager( rfDynHUD, drawingManager, this ) );
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
    public _rf1_GameFileSystem newGameFileSystem( PluginINI pluginIni )
    {
        return ( new _rf1_GameFileSystem( pluginIni ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_ProfileInfo newProfileInfo( LiveGameData gameData )
    {
        return ( new _rf1_ProfileInfo( (_rf1_GameFileSystem)gameData.getFileSystem() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_ModInfo newModInfo( LiveGameData gameData )
    {
        return ( new _rf1_ModInfo( gameData.getFileSystem(), gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_TrackInfo newTrackInfo( LiveGameData gameData )
    {
        return ( new _rf1_TrackInfo( (_rf1_ProfileInfo)gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_VehicleInfo newVehicleInfo( LiveGameData gameData )
    {
        return ( new _rf1_VehicleInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_VehiclePhysics newVehiclePhysics( LiveGameData gameData )
    {
        return ( new _rf1_VehiclePhysics() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_VehicleSetup newVehicleSetup( LiveGameData gameData )
    {
        return ( new _rf1_VehicleSetup() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_DrivingAids newDrivingAids( LiveGameData gameData )
    {
        return ( new _rf1_DrivingAids( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_CommentaryRequestInfo newCommentaryRequestInfo( LiveGameData gameData )
    {
        return ( new _rf1_CommentaryRequestInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_GraphicsInfo newGraphicsInfo( LiveGameData gameData )
    {
        return ( new _rf1_GraphicsInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_TelemetryData newTelemetryData( LiveGameData gameData )
    {
        return ( new _rf1_TelemetryData( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_ScoringInfo newScoringInfo( LiveGameData gameData )
    {
        return ( new _rf1_ScoringInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_WeatherInfo newWeatherInfo( LiveGameData gameData )
    {
        return ( new _rf1_WeatherInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf1_VehicleScoringInfo[] newVehicleScoringInfos( LiveGameData gameData, int count, VehicleScoringInfo[] toRecycle )
    {
        byte[] buffer = new byte[ _rf1_VehicleScoringInfo.BUFFER_SIZE * count ];
        
        _rf1_VehicleScoringInfo[] result = new _rf1_VehicleScoringInfo[ count ];
        
        for ( int i = 0; i < count; i++ )
            result[i] = new _rf1_VehicleScoringInfo( gameData.getScoringInfo(), gameData.getProfileInfo(), gameData, buffer, i * _rf1_VehicleScoringInfo.BUFFER_SIZE );
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadVehiclePhysicsDefaults( VehiclePhysics physics )
    {
        ( (_rf1_VehiclePhysics)physics ).loadDefaults();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadVehiclePhysics( LiveGameData gameData )
    {
        _rf1_VehiclePhysicsParser.loadFromPhysicsFiles( (_rf1_ProfileInfo)gameData.getProfileInfo(), (_rf1_TrackInfo)gameData.getTrackInfo(), (_rf1_VehiclePhysics)gameData.getPhysics() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadVehicleSetupDefaults( LiveGameData gameData )
    {
        _rf1_VehicleSetupParser.loadDefaultSetup( (_rf1_VehiclePhysics)gameData.getPhysics(), (_rf1_VehicleSetup)gameData.getSetup() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadVehicleSetupIfChanged( LiveGameData gameData )
    {
        return ( _rf1_VehicleSetupParser.loadSetupIfChanged( gameData ) );
    }
}
