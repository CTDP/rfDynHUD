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
package net.ctdp.rfdynhud.gamedata;

import java.io.File;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Engine;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class __GDPrivilegedAccess
{
    public static final InputAction INPUT_ACTION_RESET_FUEL_CONSUMPTION = FuelUsageRecorder.INPUT_ACTION_RESET_FUEL_CONSUMPTION;
    public static final InputAction INPUT_ACTION_RESET_TOPSPEEDS = TopspeedRecorder.INPUT_ACTION_RESET_TOPSPEEDS;
    public static final InputAction INPUT_ACTION_RESET_LAPTIMES_CACHE = DataCache.INPUT_ACTION_RESET_LAPTIMES_CACHE;
    
    public static File readDevGameFolder( String game )
    {
        try
        {
            String result = AbstractIniParser.parseIniValue( "game_folders.ini", game, "game", null );
            
            if ( result == null )
                return ( null );
            
            return ( new File( result ).getAbsoluteFile() );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            
            return ( null );
        }
    }
    
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
    
    public static final void setUpdatedInTimescope( VehicleSetup setup )
    {
        setup.updatedInTimeScope = true;
    }
    
    public static final void set( float baseValue, float stepSize, int numSteps, VehiclePhysics.PhysicsSetting setting )
    {
        setting.set( baseValue, stepSize, numSteps );
    }
    
    public static final _LiveGameDataObjectsFactory getGameDataObjectsFactory( LiveGameData gameData )
    {
        return ( gameData.getGameDataObjectsFactory() );
    }
    
    public static final void loadVehiclePhysics( LiveGameData gameData )
    {
        gameData.getGameDataObjectsFactory().loadVehiclePhysics( gameData );
    }
    
    public static final boolean loadSetup( LiveGameData gameData )
    {
        return ( gameData.getGameDataObjectsFactory().loadVehicleSetupIfChanged( gameData ) );
    }
    
    public static final void applyEditorPresets( EditorPresets editorPresets, LiveGameData gameData )
    {
        gameData.applyEditorPresets( editorPresets );
    }
    
    public static final void onSessionStarted( LiveGameData gameData, long timestamp, boolean isEditorMode )
    {
        gameData.getTelemetryData().onSessionStarted( isEditorMode );
        gameData.getScoringInfo().onSessionStarted( timestamp, isEditorMode );
        gameData.getSetup().onSessionStarted( timestamp );
    }
    
    public static final void onSessionStarted2( LiveGameData gameData, long timestamp, boolean isEditorMode )
    {
        gameData.onSessionStarted2( timestamp, isEditorMode );
    }
    
    public static final void onSessionEnded( LiveGameData gameData, long timestamp )
    {
        gameData.getTelemetryData().onSessionEnded( timestamp );
        gameData.getScoringInfo().onSessionEnded( timestamp );
        gameData.getSetup().onSessionEnded( timestamp );
    }
    
    public static final void setInCockpit( boolean isInCockpit, LiveGameData gameData, long timestamp, boolean isEditorMode )
    {
        gameData.setInCockpit( isInCockpit, timestamp, isEditorMode );
    }
    
    public static final void updateSessionTime( LiveGameData gameData, boolean isEditorMode, long timestamp )
    {
        gameData.setGamePaused( gameData.getTelemetryData().checkGamePaused( timestamp ), isEditorMode );
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
    
    public static Float loadFuelUsageFromCache( GameFileSystem fileSystem, final String modName, final String trackName, final String teamName )
    {
        return ( DataCache.loadFuelUsageFromCache( fileSystem, modName, trackName, teamName ) );
    }
    
    public static final boolean setGameResolution( int gameResX, int gameResY, WidgetsConfiguration widgetsConfig )
    {
        return ( widgetsConfig.getGameResolution().setResolution( gameResX, gameResY ) );
    }
    
    public static final boolean setViewport( int x, int y, int w, int h, GameResolution gameRes )
    {
        return ( gameRes.setViewport( x, y, w, h ) );
    }
    
    public static final void toggleFixedViewedVSI( ScoringInfo scoringInfo )
    {
        scoringInfo.toggleFixedViewedVSI();
    }
    
    public static final void setControlledVSIs( ScoringInfo scoringInfo, VehicleScoringInfo controlledViewedVSI, VehicleScoringInfo controlledCompareVSI )
    {
        scoringInfo.setControlledCompareVSI( controlledCompareVSI );
        scoringInfo.setControlledViewedVSI( controlledViewedVSI );
    }
    
    public static final void setVehicleClass( ScoringInfo scoringInfo, int index, String vehClass )
    {
        scoringInfo.getVehicleScoringInfo( index ).setVehicleClass( vehClass );
    }
    
    public static final void setAllWidgetsDirty( WidgetsConfiguration widgetsConfig )
    {
        widgetsConfig.setAllDirtyFlags();
    }
}
