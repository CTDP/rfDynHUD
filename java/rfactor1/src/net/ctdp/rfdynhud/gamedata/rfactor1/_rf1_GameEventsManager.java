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
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata._LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

/**
 * @author Marvin Froehlich (CTDP)
 */
class _rf1_GameEventsManager extends GameEventsManager
{
    private final TelemVect3 position = new TelemVect3();
    private final TelemVect3 garageStartLocation = new TelemVect3();
    private final TelemVect3 garageStartOrientationX = new TelemVect3();
    private final TelemVect3 garageStartOrientationY = new TelemVect3();
    private final TelemVect3 garageStartOrientationZ = new TelemVect3();
    
    private long lastSessionStartedTimestamp = -1L;
    private float lastSessionTime = 0f;
    
    @Override
    protected void reloadConfigImpl( boolean force )
    {
        boolean isEditorMode = false;
        
        boolean smallMonitor = false;
        boolean bigMonitor = false;
        
        if ( !gameData.isInCockpit() )
        {
            int gameResX = widgetsManager.getWidgetsConfiguration().getGameResolution().getResX();
            //int gameResY = widgetsManager.getWidgetsConfiguration().getGameResolution().getResY();
            int viewportWidth = widgetsManager.getWidgetsConfiguration().getGameResolution().getViewportWidth();
            //int viewportHeight = widgetsManager.getWidgetsConfiguration().getGameResolution().getViewportHeight();
            
            if ( (float)viewportWidth / (float)gameResX > 0.8f )
                bigMonitor = true;
            else
                smallMonitor = true;
            /*
            if ( ( viewportWidth == gameResX ) && ( viewportHeight == gameResY ) )
                bigMonitor = true;
            else
                smallMonitor = true;
            */
        }
        
        String modName = gameData.getModInfo().getName();
        SessionType sessionType = gameData.getScoringInfo().getSessionType();
        VehicleScoringInfo vsi = gameData.getScoringInfo().getViewedVehicleScoringInfo();
        String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
        String vehicleName = gameData.getVehicleInfo().getTeamNameCleaned();
        if ( ( vehicleName != null ) && ( vehicleName.trim().length() == 0 ) )
            vehicleName = null;
        
        _rf1_ConfigurationCandidatesIterator candidatesIterator = (_rf1_ConfigurationCandidatesIterator)getConfigurationCandidatesIterator();
        
        if ( candidatesIterator != null )
        {
            candidatesIterator.reset();
            candidatesIterator.collectCandidates( smallMonitor, bigMonitor, isInGarage && vsi.isPlayer(), modName, vehicleClass, vehicleName, sessionType );
        }
        
        loader.reloadConfiguration( candidatesIterator, widgetsManager.getWidgetsConfiguration(), gameData, isEditorMode, force, smallMonitor || bigMonitor );
    }
    
    @Override
    protected void onSessionStartedImpl( Object userObject, long timestamp )
    {
        this.lastSessionStartedTimestamp = timestamp;
        
        super.onSessionStartedImpl( userObject, timestamp );
    }
    
    @Override
    protected boolean isReenteringGarageSupported()
    {
        // For now we don't support reentering the garage with a special configuration, because of the inaccurate check.
        return ( false );
    }
    
    @Override
    protected boolean checkIsInGarage()
    {
        if ( !gameData.isInCockpit() )
            return ( true );
        
        if ( !gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits() )
            return ( false );
        
        gameData.getTelemetryData().getPosition( position );
        
        float relWorldX = position.getX() - garageStartLocation.getX();
        float relWorldY = position.getY() - garageStartLocation.getY();
        float relWorldZ = position.getZ() - garageStartLocation.getZ();
        float currLocalX = ( garageStartOrientationX.getX() * relWorldX ) + ( garageStartOrientationY.getX() * relWorldY ) + ( garageStartOrientationZ.getX() * relWorldZ );
        //float currLocalY = ( garageStartOrientationX.getY() * relWorldX ) + ( garageStartOrientationY.getY() * relWorldY ) + ( garageStartOrientationZ.getY() * relWorldZ );
        float currLocalZ = ( garageStartOrientationX.getZ() * relWorldX ) + ( garageStartOrientationY.getZ() * relWorldY ) + ( garageStartOrientationZ.getZ() * relWorldZ );
        
        if ( ( currLocalX < -5f ) || ( currLocalX > +5f ) )
            return ( false );
        
        if ( ( currLocalZ < -1.75f ) || ( currLocalZ > +10f ) )
            return ( false );
        
        return ( true );
    }
    
    @Override
    protected void onWaitingForDataCompleted( long timestamp, boolean isEditorMode )
    {
        isInGarage = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
        
        super.onWaitingForDataCompleted( timestamp, isEditorMode );
    }
    
    @Override
    protected byte handleViewport( byte result, short viewportX, short viewportY, short viewportWidth, short viewportHeight, boolean viewportChanged )
    {
        if ( viewportChanged )
        {
            RFDHLog.debug( "[DEBUG]: (Viewport changed): ", viewportX, ", ", viewportY, "; ", viewportWidth, "x", viewportHeight );
            
            if ( gameData.getProfileInfo().isValid() )
            {
                if ( !gameData.isInCockpit() && ( viewportY == 0 ) )
                {
                    __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
                    result = 0;
                }
                else
                {
                    //result = reloadConfigAndSetupTexture( true );
                    waitingForData = true;
                    result = checkWaitingData( false, true );
                }
            }
            
            onViewportChanged( viewportX, viewportY, viewportWidth, viewportHeight );
        }
        else if ( !gameData.isInCockpit() && ( viewportY == 0 ) )
        {
            __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
            result = 0;
        }
        else
        {
            result = checkWaitingData( false, false );
        }
        
        return ( result );
    }
    
    @Override
    protected byte onScoringInfoUpdatedImpl( byte result, int numVehicles, Object userObject, long timestamp )
    {
        boolean wfgsl = waitingForGarageStartLocation;
        
        this.lastSessionTime = gameData.getScoringInfo().getSessionTime();
        
        result = super.onScoringInfoUpdatedImpl( result, numVehicles, userObject, timestamp );
        
        if ( wfgsl )
        {
            final VehicleScoringInfo vsi = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
            vsi.getWorldPosition( garageStartLocation );
            vsi.getOrientationX( garageStartOrientationX );
            vsi.getOrientationY( garageStartOrientationY );
            vsi.getOrientationZ( garageStartOrientationZ );
        }
        
        return ( result );
    }
    
    @Override
    public byte onScoringInfoUpdated( int numVehicles, Object userObject )
    {
        /*
        //RFDHLog.profile( "[PROFILE]: onScoringInfoUpdated(", gameData.getScoringInfo().getNumVehicles(), ")" );
        if ( gameData.getScoringInfo().getNumVehicles() == 0 ) // What the hell is this again???
        {
            if ( rfDynHUD != null )
                rfDynHUD.setRenderMode( false );
            
            return ( 0 );
        }
        */
        
        return ( super.onScoringInfoUpdated( numVehicles, userObject ) );
    }
    
    @Override
    protected boolean checkRaceRestartImpl( long updateTimestamp )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        //RFDHLog.debugCS( waitingForScoring, lastSessionStartedTimestamp, updateTimestamp, updateTimestamp - lastSessionStartedTimestamp > 3000000000L, scoringInfo.getSessionTime(), lastSessionTime, lastSessionTime > scoringInfo.getSessionTime() );
        if ( !waitingForScoring && scoringInfo.getSessionType().isRace() && ( lastSessionStartedTimestamp != -1L ) && ( updateTimestamp - lastSessionStartedTimestamp > 3000000000L ) && ( scoringInfo.getSessionTime() > 0f ) && ( lastSessionTime > scoringInfo.getSessionTime() ) )
        {
            return ( true );
        }
        
        return ( false );
    }
    
    _rf1_GameEventsManager( RFDynHUD rfDynHUD, WidgetsDrawingManager drawingManager, _LiveGameDataObjectsFactory gdFactory )
    {
        super( _rf1_LiveGameDataObjectsFactory.GAME_ID, rfDynHUD, drawingManager, gdFactory, null );
        
        setConfigurationCandidatesIterator( new _rf1_ConfigurationCandidatesIterator( gameData.getFileSystem().getConfigFolder() ) );
    }
}
