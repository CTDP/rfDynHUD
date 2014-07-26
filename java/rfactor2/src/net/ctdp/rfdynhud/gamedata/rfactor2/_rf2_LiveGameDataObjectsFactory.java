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
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata._LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.PluginINI;
import net.ctdp.rfdynhud.util.RFDHLog;

public class _rf2_LiveGameDataObjectsFactory implements _LiveGameDataObjectsFactory
{
    public static final String GAME_ID = "rFactor2";
    
    private static final String NATIVE_LIB_NAME_32 = "rfdynhud4rf2_32bit";
    private static final String NATIVE_LIB_NAME_64 = "rfdynhud4rf2_64bit";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void init( boolean isEditorMode, boolean isSimulationMode )
    {
        if ( !isEditorMode && !isSimulationMode )
        {
            String native_lib_name = null;
            
            try
            {
                String bitness_ = System.getProperty( "sun.arch.data.model" );
                int bitness = -1;
                if ( bitness_ != null )
                    bitness = Integer.parseInt( bitness_ );
                RFDHLog.printlnEx( "System bitness: " + ( bitness < 0 ? "N/A" : bitness + " bit" ) );
                if ( bitness == 32 )
                    native_lib_name = NATIVE_LIB_NAME_32;
                else if ( bitness == 64 )
                    native_lib_name = NATIVE_LIB_NAME_64;
                
                System.loadLibrary( native_lib_name );
            }
            catch ( UnsatisfiedLinkError e )
            {
                RFDHLog.error( "[ERROR] Couldn't " + native_lib_name + ".dll" );
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
    public _rf2_GameFileSystem newGameFileSystem( PluginINI pluginIni )
    {
        return ( new _rf2_GameFileSystem( pluginIni ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_ProfileInfo newProfileInfo( LiveGameData gameData )
    {
        return ( new _rf2_ProfileInfo( (_rf2_GameFileSystem)gameData.getFileSystem() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_ModInfo newModInfo( LiveGameData gameData )
    {
        return ( new _rf2_ModInfo( gameData.getProfileInfo() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_TrackInfo newTrackInfo( LiveGameData gameData )
    {
        return ( new _rf2_TrackInfo( (_rf2_ProfileInfo)gameData.getProfileInfo(), (_rf2_TelemetryData)gameData.getTelemetryData() ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_VehicleInfo newVehicleInfo( LiveGameData gameData )
    {
        return ( new _rf2_VehicleInfo() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_VehiclePhysics newVehiclePhysics( LiveGameData gameData )
    {
        return ( new _rf2_VehiclePhysics() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_VehicleSetup newVehicleSetup( LiveGameData gameData )
    {
        return ( new _rf2_VehicleSetup() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_DrivingAids newDrivingAids( LiveGameData gameData )
    {
        return ( new _rf2_DrivingAids( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_CommentaryRequestInfo newCommentaryRequestInfo( LiveGameData gameData )
    {
        return ( new _rf2_CommentaryRequestInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_GraphicsInfo newGraphicsInfo( LiveGameData gameData )
    {
        return ( new _rf2_GraphicsInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_TelemetryData newTelemetryData( LiveGameData gameData )
    {
        return ( new _rf2_TelemetryData( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_ScoringInfo newScoringInfo( LiveGameData gameData )
    {
        return ( new _rf2_ScoringInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_WeatherInfo newWeatherInfo( LiveGameData gameData )
    {
        return ( new _rf2_WeatherInfo( gameData ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public _rf2_VehicleScoringInfo[] newVehicleScoringInfos( LiveGameData gameData, int count, VehicleScoringInfo[] toRecycle )
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
    
    public _rf2_LiveGameDataObjectsFactory()
    {
    }
}
