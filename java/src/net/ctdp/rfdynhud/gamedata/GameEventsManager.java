/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
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

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.util.Tools;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationLoadListener;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

/**
 * The events manager receives events from rFactor and modifies state-flags appropriately.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class GameEventsManager implements ConfigurationLoadListener
{
    private final RFDynHUD rfDynHUD;
    private final WidgetsDrawingManager widgetsManager;
    private LiveGameData gameData = null;
    
    private boolean running = false;
    
    private boolean sessionRunning = false;
    
    @SuppressWarnings( "unused" )
    private boolean isComingOutOfGarage = true;
    
    private boolean sessionJustStarted = false;
    private Boolean currentSessionIsRace = null;
    
    private boolean waitingForGraphics = false;
    private boolean waitingForTelemetry = false;
    private boolean waitingForScoring = false;
    private boolean waitingForSetup = false;
    private long setupReloadTryTime = -1L;
    private boolean waitingForData = false;
    
    private boolean isInGarage = true;
    private boolean isInPits = true;
    private final TelemVect3 garageStartLocation = new TelemVect3();
    private final TelemVect3 garageStartOrientationX = new TelemVect3();
    private final TelemVect3 garageStartOrientationY = new TelemVect3();
    private final TelemVect3 garageStartOrientationZ = new TelemVect3();
    
    private int lastViewedVSIId = -1;
    private VehicleControl lastControl = null;
    
    private boolean physicsLoadedOnce = false;
    
    private String lastTrackname = null;
    
    private long lastSessionStartedTimestamp = -1L;
    private float lastSessionTime = 0f;
    
    /**
     * Sets live game data instance.
     * 
     * @param gameData
     */
    public void setGameData( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeWidgetsConfigurationCleared( WidgetsConfiguration widgetsConfig )
    {
        /*
        int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
            widgetsConfig.getWidget( i ).clearRegion( false, ( (WidgetsDrawingManager)widgetsConfig ).getMainTexture( i ) );
        */
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterWidgetsConfigurationLoaded( WidgetsConfiguration widgetsConfig )
    {
    }
    
    /**
     * This method must be called when the game started up.
     */
    public void onStartup()
    {
        this.running = true;
    }
    
    /**
     * This method must be called when the game shut down.
     */
    public void onShutdown()
    {
        this.running = false;
    }
    
    /**
     * Returns whether the game has been started up and not yet been shut down.
     * 
     * @return whether the game has been started up and not yet been shut down.
     */
    public final boolean isRunning()
    {
        return ( running );
    }
    
    private void reloadPhysics( boolean onlyOnce )
    {
        if ( !onlyOnce || !physicsLoadedOnce )
        {
            __GDPrivilegedAccess.loadFromPhysicsFiles( gameData.getProfileInfo(), gameData.getTrackInfo(), gameData.getPhysics() );
            
            physicsLoadedOnce = true;
        }
    }
    
    private boolean reloadSetup()
    {
        boolean result = false;
        
        if ( VehicleSetupParser.loadSetup( gameData ) )
        {
            result = true;
        }
        
        __GDPrivilegedAccess.setEngineBoostMapping( gameData.getSetup().getEngine().getBoostMapping(), gameData.getTelemetryData() );
        
        return ( result );
    }
    
    /**
     * 
     * @param force force reload ignoring, whether it is already in action?
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public byte reloadConfigAndSetupTexture( boolean force )
    {
        byte result = 1;
        
        try
        {
            boolean isEditorMode = false;
            
            boolean smallMonitor = false;
            boolean bigMonitor = false;
            
            if ( !gameData.isInRealtimeMode() )
            {
                int gameResX = widgetsManager.getGameResolution().getResX();
                //int gameResY = widgetsManager.getGameResolution().getResY();
                int viewportWidth = widgetsManager.getGameResolution().getViewportWidth();
                //int viewportHeight = widgetsManager.getGameResolution().getViewportHeight();
                
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
            String vehicleClass = vsi.getVehicleClass();
            Boolean result2 = __UtilPrivilegedAccess.reloadConfiguration( new ConfigurationLoader(), smallMonitor, bigMonitor, isInGarage && vsi.isPlayer(), modName, vehicleClass, sessionType, widgetsManager, gameData, isEditorMode, this, force );
            
            if ( result2 == null )
            {
                result = 0;
            }
            else if ( result2.booleanValue() )
            {
                widgetsManager.collectTextures( gameData, isEditorMode );
                //if ( usePlayer )
                    widgetsManager.clearCompleteTexture();
                
                //System.gc();
                Runtime runtime = Runtime.getRuntime();
                Logger.log( "INFO: Free heap space memory: " + Tools.formatBytes( runtime.freeMemory() ) + " / " + Tools.formatBytes( runtime.totalMemory() ) + " / " + Tools.formatBytes( runtime.maxMemory() ) );
                
                result = 2;
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            __WCPrivilegedAccess.setValid( widgetsManager, false );
            result = 0;
        }
        
        return ( result );
    }
    
    /**
     * 
     * @param isEditorMode editor mode?
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public byte onSessionStarted( boolean isEditorMode )
    {
        //Logger.log( ">>> onSessionStarted()" );
        //if ( currentSessionIsRace == Boolean.TRUE )
        if ( sessionRunning )
        {
            //Logger.log( "INFO: Got a call to StartSession() in already started RACE session. Looks like an rFactor bug. Ignoring this call." );
            Logger.log( "INFO: Got a call to StartSession() in already started session. Looks like an rFactor bug. Ignoring this call." );
            return ( rfDynHUD.isInRenderMode() ? (byte)2 : (byte)0 );
        }
        
        this.sessionRunning = true;
        this.isComingOutOfGarage = true;
        this.isInGarage = true;
        
        this.waitingForGraphics = !isEditorMode;
        this.waitingForTelemetry = !isEditorMode;
        this.waitingForScoring = !isEditorMode;
        this.waitingForSetup = false;
        this.setupReloadTryTime = -1L;
        this.waitingForData = !isEditorMode;
        
        this.sessionJustStarted = true;
        this.currentSessionIsRace = null;
        this.lastSessionStartedTimestamp = System.nanoTime();
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        byte result = 0;
        
        try
        {
            ThreeLetterCodeManager.updateThreeLetterCodes();
            __GDPrivilegedAccess.updateInfo( gameData );
            
            if ( gameData.getProfileInfo().isValid() )
            {
                boolean loadPhysicsAndSetup = true;
                
                if ( isEditorMode )
                {
                    File cchFile = gameData.getProfileInfo().getCCHFile();
                    File playerVEHFile = gameData.getProfileInfo().getVehicleFile();
                    String trackName = gameData.getTrackInfo().getTrackName();
                    File setupFile = GameFileSystem.INSTANCE.locateSetupFile( gameData );
                    
                    if ( ( cchFile == null ) || !cchFile.exists() )
                        loadPhysicsAndSetup = false;
                    
                    if ( ( playerVEHFile == null ) || !playerVEHFile.exists() )
                        loadPhysicsAndSetup = false;
                    
                    if ( ( trackName == null ) || trackName.equals( "" ) )
                        loadPhysicsAndSetup = false;
                    
                    if ( ( setupFile == null ) || !setupFile.exists() )
                        loadPhysicsAndSetup = false;
                }
                
                if ( loadPhysicsAndSetup )
                {
                    reloadPhysics( true );
                    reloadSetup();
                }
                
                __GDPrivilegedAccess.onSessionStarted( gameData, isEditorMode );
                
                // We cannot load the configuration here, because we don't know, which one to load (no scoring info).
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> /onSessionStarted(), result: " + result );
        return ( result );
    }
    
    /**
     * This method must be called when a session has been started.
     * Note: LiveGameData must have been updated before.
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public byte onSessionStarted()
    {
        return ( onSessionStarted( false ) );
    }
    
    /**
     * 
     * @param isEditorMode editor mode?
     */
    public void onSessionEnded( boolean isEditorMode )
    {
        //Logger.log( ">>> onSessionEnded()" );
        this.waitingForGraphics = false;
        this.waitingForTelemetry = false;
        this.waitingForScoring = false;
        this.waitingForSetup = false;
        this.setupReloadTryTime = -1L;
        this.waitingForData = false;
        
        //this.sessionStartTime = -1f;
        this.sessionRunning = false;
        this.currentSessionIsRace = null;
        
        if ( gameData.getProfileInfo().isValid() )
        {
            __GDPrivilegedAccess.onSessionEnded( gameData );
        }
        
        __WCPrivilegedAccess.setValid( widgetsManager, false );
    }
    
    /**
     * This method must be called when a session has been ended.
     */
    public final void onSessionEnded()
    {
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( false );
        
        onSessionEnded( false );
    }
    
    /**
     * Returns whether the current session is running.
     * 
     * @return whether the current session is running.
     */
    public final boolean isSessionRunning()
    {
        return ( sessionRunning );
    }
    
    /**
     * 
     * @param isEditorMode editor mode?
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public byte onRealtimeEntered( boolean isEditorMode )
    {
        //Logger.log( ">>> onRealtimeEntered()" );
        byte result = 0;
        
        this.isComingOutOfGarage = true;
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        this.waitingForGraphics = waitingForGraphics || !isEditorMode;
        this.waitingForTelemetry = waitingForTelemetry || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.waitingForScoring = waitingForScoring || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.waitingForSetup = false; //waitingForSetup || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.setupReloadTryTime = System.nanoTime() + 5000000000L;
        this.waitingForData = !isEditorMode;
        
        if ( !isEditorMode )
        {
            Logger.log( "Entered cockpit." );
        }
        
        try
        {
            ThreeLetterCodeManager.updateThreeLetterCodes();
            
            __GDPrivilegedAccess.updateInfo( gameData );
            
            if ( gameData.getProfileInfo().isValid() )
            {
                __GDPrivilegedAccess.setRealtimeMode( true, gameData, isEditorMode );
                
                if ( !isEditorMode )
                {
                    reloadPhysics( false );
                    
                    if ( reloadSetup() )
                    {
                        waitingForSetup = false;
                        
                        widgetsManager.fireOnVehicleSetupUpdated( gameData, isEditorMode );
                    }
                }
                
                widgetsManager.fireOnRealtimeEntered( gameData, isEditorMode );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> /onRealtimeEntered(), result: " + result );
        return ( result );
    }
    
    /**
     * This method must be called when realtime mode has been entered (the user clicked on "Drive").
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onRealtimeEntered()
    {
        return ( onRealtimeEntered( false ) );
    }
    
    /**
     * This method must be called when the user exited realtime mode (pressed ESCAPE in the cockpit).
     * 
     * @param isEditorMode
     */
    public void onRealtimeExited( boolean isEditorMode )
    {
        //Logger.log( ">>> onRealtimeExited()" );
        Logger.log( "Exited cockpit." );
        
        //realtimeStartTime = -1f;
        this.isComingOutOfGarage = true;
        
        this.isInPits = true;
        this.isInGarage = true;
        
        this.waitingForGraphics = true;
        this.waitingForData = true;
        
        try
        {
            if ( gameData.getProfileInfo().isValid() )
            {
                __GDPrivilegedAccess.setRealtimeMode( false, gameData, isEditorMode );
                
                widgetsManager.fireOnRealtimeExited( gameData, isEditorMode );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    /**
     * This method must be called when the user exited realtime mode (pressed ESCAPE in the cockpit).
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onRealtimeExited()
    {
        onRealtimeExited( false );
        
        //byte result = reloadConfigAndSetupTexture( false );
        __WCPrivilegedAccess.setValid( widgetsManager, false );
        waitingForData = true;
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( false );
        
        return ( 0 );
    }
    
    private final TelemVect3 position = new TelemVect3();
    
    private final boolean checkIsInGarage()
    {
        if ( !gameData.isInRealtimeMode() )
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
    
    private byte checkPosition( boolean isEditorMode )
    {
        byte result = 1;
        
        if ( !isInGarage ) // For now we don't support reentering the garage with a special configuration, because of the inaccurate check.
            return ( result );
        
        boolean isInGarage = checkIsInGarage();
        
        if ( this.isInGarage && !isInGarage )
        {
            this.isInGarage = false;
            widgetsManager.fireOnGarageExited( gameData, isEditorMode );
            
            if ( !isEditorMode )
            {
                result = reloadConfigAndSetupTexture( false );
                
                if ( result != 0 )
                    widgetsManager.fireOnGarageExited( gameData, isEditorMode );
            }
        }
        else if ( !this.isInGarage && isInGarage )
        {
            this.isInGarage = true;
            widgetsManager.fireOnGarageEntered( gameData, isEditorMode );
            
            if ( !isEditorMode )
            {
                result = reloadConfigAndSetupTexture( false );
                
                if ( result != 0 )
                    widgetsManager.fireOnGarageEntered( gameData, isEditorMode );
            }
        }
        
        final boolean isInPits = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
        
        if ( this.isInPits && !isInPits )
        {
            this.isInPits = isInPits;
            widgetsManager.fireOnPitsExited( gameData, isEditorMode );
        }
        else if ( !this.isInPits && isInPits )
        {
            this.isInPits = isInPits;
            widgetsManager.fireOnPitsEntered( gameData, isEditorMode );
        }
        
        return ( result );
    }
    
    private byte checkWaitingData( boolean isEditorMode, boolean forceReload )
    {
        widgetsManager.checkAndFireOnNeededDataComplete( gameData, isEditorMode );
        
        boolean waitingForSetup2 = ( System.nanoTime() <= setupReloadTryTime );
        
        if ( waitingForSetup || waitingForSetup2 )
        {
            if ( reloadSetup() )
            {
                waitingForSetup = false;
                waitingForSetup2 = false;
                setupReloadTryTime = -1L;
                
                widgetsManager.fireOnVehicleSetupUpdated( gameData, isEditorMode );
            }
        }
        
        if ( !waitingForData  )
        {
            return ( widgetsManager.isValid() ? (byte)1 : (byte)0 );
        }
        
        byte result = 0;
        
        if ( !waitingForGraphics && !waitingForTelemetry && !waitingForScoring/* && !waitingForSetup && !waitingForSetup2*/ )
        {
            isInGarage = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
            
            if ( sessionJustStarted )
            {
                __GDPrivilegedAccess.onSessionStarted2( gameData, isEditorMode );
                
                if ( !gameData.isInRealtimeMode() || gameData.getScoringInfo().getSessionType().isRace() )
                {
                    String modName = gameData.getModInfo().getName();
                    String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
                    SessionType sessionType = gameData.getScoringInfo().getSessionType();
                    String trackName = gameData.getTrackInfo().getTrackName();
                    Logger.log( "Session started. (Mod: \"" + modName + "\", Car: \"" + vehicleClass + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackName + "\")" );
                    
                    String trackname = gameData.getTrackInfo().getTrackName();
                    if ( !trackname.equals( lastTrackname ) )
                    {
                        widgetsManager.fireOnTrackChanged( trackname, gameData, isEditorMode );
                        lastTrackname = trackname;
                    }
                    
                    widgetsManager.fireOnSessionStarted( gameData.getScoringInfo().getSessionType(), gameData, isEditorMode );
                }
                
                this.sessionJustStarted = false;
            }
            
            waitingForData = false;
            
            result = reloadConfigAndSetupTexture( forceReload );
        }
        else if ( gameData.isInRealtimeMode() )
        {
            result = widgetsManager.isValid() ? (byte)1 : (byte)0;
        }
        
        return ( result );
    }
    
    /**
     * Will and must be called any time, the game is redendered (called from the C++-Plugin).
     * 
     * @param viewportX the left coordinate of the viewport
     * @param viewportY the top coordinate of the viewport
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onGraphicsInfoUpdated( short viewportX, short viewportY, short viewportWidth, short viewportHeight )
    {
        //Logger.log( ">>> onGraphicsInfoUpdated()" );
        this.waitingForGraphics = false;
        
        byte result = 0;
        
        try
        {
            boolean vpChanged = __WCPrivilegedAccess.setViewport( viewportX, viewportY, viewportWidth, viewportHeight, widgetsManager );
            
            if ( ( viewportWidth > gameData.getGameResolution().getResX() ) || ( viewportHeight > gameData.getGameResolution().getResY() ) )
            {
                widgetsManager.resizeMainTexture( viewportWidth, viewportHeight );
            }
            
            if ( isSessionRunning() && gameData.getProfileInfo().isValid() )
            {
                if ( vpChanged )
                {
                    //Logger.log( "Viewport changed: " + viewportX + ", " + viewportY + ", " + viewportWidth + "x" + viewportHeight );
                    
                    if ( gameData.getProfileInfo().isValid() )
                    {
                        if ( !gameData.isInRealtimeMode() && ( viewportY == 0 ) )
                        {
                            __WCPrivilegedAccess.setValid( widgetsManager, false );
                            result = 0;
                        }
                        else
                        {
                            //result = reloadConfigAndSetupTexture( true );
                            waitingForData = true;
                            result = checkWaitingData( false, true );
                        }
                    }
                }
                else if ( !gameData.isInRealtimeMode() && ( viewportY == 0 ) )
                {
                    __WCPrivilegedAccess.setValid( widgetsManager, false );
                    result = 0;
                }
                else
                {
                    result = checkWaitingData( false, false );
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> /onGraphicsInfoUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * 
     * @param isEditorMode editor mode?
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onTelemetryDataUpdated( boolean isEditorMode )
    {
        //Logger.log( ">>> onTelemetryDataUpdated()" );
        byte result = 0;
        
        try
        {
            this.waitingForTelemetry = false;
            
            if ( gameData.getProfileInfo().isValid() )
            {
                result = checkWaitingData( isEditorMode, false );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> /onTelemetryDataUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * This method must be called when TelemetryData has been updated.
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onTelemetryDataUpdated()
    {
        return ( onTelemetryDataUpdated( false ) );
    }
    
    /**
     * 
     * @param isEditorMode editor mode?
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onScoringInfoUpdated( boolean isEditorMode )
    {
        //Logger.log( ">>> onScoringInfoUpdated() (" + gameData.getScoringInfo().getNumVehicles() + ")" );
        if ( gameData.getScoringInfo().getNumVehicles() == 0 ) // What the hell is this again???
        {
            if ( rfDynHUD != null )
                rfDynHUD.setRenderMode( false );
            
            return ( 0 );
        }
        
        byte result = 0;
        
        try
        {
            this.currentSessionIsRace = gameData.getScoringInfo().getSessionType().isRace();
            
            if ( waitingForScoring )
            {
                final VehicleScoringInfo vsi = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
                vsi.getWorldPosition( garageStartLocation );
                vsi.getOrientationX( garageStartOrientationX );
                vsi.getOrientationY( garageStartOrientationY );
                vsi.getOrientationZ( garageStartOrientationZ );
                
                this.isInPits = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
                this.isInGarage = isInPits;
            }
            
            this.waitingForScoring = false;
            
            this.lastSessionTime = gameData.getScoringInfo().getSessionTime();
            
            if ( gameData.getProfileInfo().isValid() )
            {
                result = checkWaitingData( isEditorMode, false );
                
                widgetsManager.fireOnScoringInfoUpdated( gameData, isEditorMode );
                
                if ( !waitingForData && ( result != 0 ) )
                {
                    byte result2 = checkPosition( isEditorMode );
                    
                    if ( result2 != 1 )
                    {
                        result = result2;
                    }
                    
                    VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
                    
                    if ( ( lastViewedVSIId == -1 ) || ( viewedVSI.getDriverId() != lastViewedVSIId ) || ( viewedVSI.getVehicleControl() != lastControl ) )
                    {
                        lastViewedVSIId = viewedVSI.getDriverId();
                        lastControl = viewedVSI.getVehicleControl();
                        widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, isEditorMode );
                        
                        result2 = reloadConfigAndSetupTexture( false );
                        
                        if ( result2 == 2 )
                        {
                            widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, isEditorMode );
                        }
                        
                        if ( result2 != 1 )
                        {
                            result = result2;
                        }
                    }
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> /onScoringInfoUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * This method must be called when ScoringInfo has been updated.
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onScoringInfoUpdated()
    {
        return ( onScoringInfoUpdated( false ) );
    }
    
    /**
     * 
     * @param updateTimestamp the timestamp at the update
     */
    public final void checkRaceRestart( long updateTimestamp )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( !waitingForScoring && scoringInfo.getSessionType().isRace() && ( lastSessionStartedTimestamp != -1L ) && ( updateTimestamp - lastSessionStartedTimestamp > 3000000000L ) && ( scoringInfo.getSessionTime() > 0f ) && ( lastSessionTime > scoringInfo.getSessionTime() ) )
        {
            onSessionStarted( false );
        }
    }
    
    /**
     * 
     * @param isEditorMode editor mode?
     */
    public final void checkAndFireOnLapStarted( boolean isEditorMode )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( !scoringInfo.getPlayersVehicleScoringInfo().isInPits() )
            this.isComingOutOfGarage = false;
        
        //if ( isComingOutOfGarage )
        //    return;
        
        final int n = scoringInfo.getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            if ( vsi.isLapJustStarted() )
                widgetsManager.fireOnLapStarted( vsi, gameData, isEditorMode );
        }
    }
    
    /**
     * Creates a new {@link GameEventsManager}.
     * 
     * @param rfDynHUD the main {@link RFDynHUD} instance
     * @param widgetsManager the widgets manager
     */
    public GameEventsManager( RFDynHUD rfDynHUD, WidgetsDrawingManager widgetsManager )
    {
        this.rfDynHUD = rfDynHUD;
        this.widgetsManager = widgetsManager;
    }
}
