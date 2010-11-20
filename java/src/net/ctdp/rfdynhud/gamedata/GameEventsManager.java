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
import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;
import net.ctdp.rfdynhud.input.InputMapping;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.render.WidgetsManager;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationLoadListener;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

import org.jagatoo.util.Tools;

/**
 * The events manager receives events from rFactor and modifies state-flags appropriately.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class GameEventsManager implements ConfigurationLoadListener
{
    public static boolean simulationMode = false;
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
    private boolean waitingForGarageStartLocation = false;
    private boolean waitingForSetup = false;
    private long setupReloadTryTime = -1L;
    private boolean waitingForData = false;
    
    private boolean needsOnVehicleControlChangedEvent = false;
    
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
    
    private GameEventsDispatcher eventsDispatcher;
    private WidgetsManager renderListenersManager;
    
    private final ConfigurationLoader loader = new ConfigurationLoader( this );
    private boolean texturesRequested = false;
    
    public final boolean hasWaitingWidgets()
    {
        return ( eventsDispatcher.hasWaitingWidgets() );
    }
    
    public final ConfigurationLoader getConfigurationLoader()
    {
        return ( loader );
    }
    
    /**
     * Sets live game data instance.
     * 
     * @param gameData
     * @param renderListenersManager
     */
    public void setGameData( LiveGameData gameData, WidgetsManager renderListenersManager )
    {
        this.gameData = gameData;
        this.eventsDispatcher = new GameEventsDispatcher();
        eventsDispatcher.setWidgetsConfiguration( widgetsManager.getWidgetsConfiguration() );
        this.renderListenersManager = renderListenersManager;
        __RenderPrivilegedAccess.setConfigurationAndLoader( widgetsManager.getWidgetsConfiguration(), loader, renderListenersManager );
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
        
        eventsDispatcher.fireBeforeWidgetsConfigurationCleared( renderListenersManager, gameData, widgetsConfig );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterWidgetsConfigurationLoaded( WidgetsConfiguration widgetsConfig )
    {
        needsOnVehicleControlChangedEvent = true;
        texturesRequested = true;
        
        eventsDispatcher.fireAfterWidgetsConfigurationLoaded( renderListenersManager, gameData, widgetsConfig );
        
        widgetsManager.collectTextures( gameData, __EDPrivilegedAccess.isEditorMode );
        //if ( usePlayer )
            widgetsManager.clearCompleteTexture();
        
        //System.gc();
        Runtime runtime = Runtime.getRuntime();
        RFDHLog.debug( "INFO: Free heap space memory: " + Tools.formatBytes( runtime.freeMemory() ) + " / " + Tools.formatBytes( runtime.totalMemory() ) + " / " + Tools.formatBytes( runtime.maxMemory() ) );
    }
    
    /**
     * This method must be called when the game started up.
     * 
     * @param isEditorMode
     */
    public void onStartup( boolean isEditorMode )
    {
        this.running = true;
        
        eventsDispatcher.fireOnStarted( gameData, isEditorMode, renderListenersManager );
    }
    
    /**
     * This method must be called when the game started up.
     */
    public final void onStartup()
    {
        onStartup( false );
    }
    
    /**
     * This method must be called when the game shut down.
     * 
     * @param isEditorMode
     */
    public void onShutdown( boolean isEditorMode )
    {
        this.running = false;
        
        eventsDispatcher.fireOnShutdown( gameData, isEditorMode, renderListenersManager );
    }
    
    /**
     * This method must be called when the game shut down.
     */
    public final void onShutdown()
    {
        onShutdown( false );
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
    
    /**
     * 
     * @param mapping
     * @param state
     * @param modifierMask
     * @param when
     * @param isEditorMode
     */
    public void fireOnInputStateChanged( InputMapping mapping, boolean state, int modifierMask, long when, boolean isEditorMode )
    {
        eventsDispatcher.fireOnInputStateChanged( mapping, state, modifierMask, when, gameData, isEditorMode );
    }
    
    private void reloadVehicleInfo()
    {
        gameData.getVehicleInfo().reset();
        
        try
        {
            File playerVEHFile = gameData.getProfileInfo().getVehicleFile();
            
            if ( ( playerVEHFile != null ) && playerVEHFile.exists() )
            {
                new VehicleInfoParser( playerVEHFile.getAbsolutePath(), gameData.getVehicleInfo() ).parse( playerVEHFile );
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    private void reloadPhysics( boolean onlyOnce, boolean isEditorMode )
    {
        if ( !onlyOnce || !physicsLoadedOnce )
        {
            __GDPrivilegedAccess.loadFromPhysicsFiles( gameData.getProfileInfo(), gameData.getTrackInfo(), gameData.getPhysics() );
            
            physicsLoadedOnce = true;
            
            eventsDispatcher.fireOnVehiclePhysicsUpdated( gameData, isEditorMode );
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
            String vehicleName = gameData.getVehicleInfo().getTeamName();
            if ( ( vehicleName == null ) || ( vehicleName.length() == 0 ) )
                vehicleName = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
            __UtilPrivilegedAccess.reloadConfiguration( loader, smallMonitor, bigMonitor, isInGarage && vsi.isPlayer(), modName, vehicleName, sessionType, widgetsManager.getWidgetsConfiguration(), gameData, isEditorMode, force );
            
            if ( texturesRequested )
            {
                result = 2;
                
                texturesRequested = false;
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            
            __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
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
            RFDHLog.debug( "INFO: Got a call to StartSession() in already started session. Looks like an rFactor bug. Ignoring this call." );
            return ( rfDynHUD.isInRenderMode() ? (byte)2 : (byte)0 );
        }
        
        this.sessionRunning = true;
        this.isComingOutOfGarage = true;
        this.isInGarage = true;
        
        this.waitingForGraphics = !isEditorMode;
        this.waitingForTelemetry = !isEditorMode;
        this.waitingForScoring = !isEditorMode;
        this.waitingForGarageStartLocation = !isEditorMode;
        this.waitingForSetup = false;
        this.setupReloadTryTime = -1L;
        this.waitingForData = !isEditorMode;
        
        this.sessionJustStarted = true;
        this.currentSessionIsRace = null;
        this.lastSessionStartedTimestamp = System.nanoTime();
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        byte result = 0;
        
        if ( texturesRequested )
        {
            result = 2;
            
            texturesRequested = false;
        }
        
        try
        {
            ThreeLetterCodeManager.updateThreeLetterCodes( gameData.getScoringInfo().getThreeLetterCodeGenerator() );
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
                
                reloadVehicleInfo();
                
                if ( loadPhysicsAndSetup )
                {
                    reloadPhysics( true, isEditorMode );
                    reloadSetup();
                }
                
                __GDPrivilegedAccess.onSessionStarted( gameData, isEditorMode );
                
                // We cannot load the configuration here, because we don't know, which one to load (no scoring info).
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
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
        this.waitingForGarageStartLocation = false;
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
        
        __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
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
        
        if ( texturesRequested )
        {
            result = 2;
            
            texturesRequested = false;
        }
        
        this.isComingOutOfGarage = true;
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        this.waitingForGraphics = waitingForGraphics || !isEditorMode;
        this.waitingForTelemetry = waitingForTelemetry || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.waitingForScoring = waitingForScoring || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.waitingForGarageStartLocation = true;
        this.waitingForSetup = false; //waitingForSetup || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.setupReloadTryTime = System.nanoTime() + 5000000000L;
        this.waitingForData = !isEditorMode;
        this.needsOnVehicleControlChangedEvent = true;
        
        if ( !isEditorMode )
        {
            RFDHLog.printlnEx( "Entered cockpit." );
        }
        
        try
        {
            ThreeLetterCodeManager.updateThreeLetterCodes( gameData.getScoringInfo().getThreeLetterCodeGenerator() );
            
            __GDPrivilegedAccess.updateInfo( gameData );
            
            if ( gameData.getProfileInfo().isValid() )
            {
                __GDPrivilegedAccess.setRealtimeMode( true, gameData, isEditorMode );
                
                if ( !isEditorMode )
                {
                    reloadPhysics( false, isEditorMode );
                    
                    if ( reloadSetup() )
                    {
                        waitingForSetup = false;
                        
                        eventsDispatcher.fireOnVehicleSetupUpdated( gameData, isEditorMode );
                    }
                }
                
                widgetsManager.onRealtimeEntered( gameData );
                eventsDispatcher.fireOnRealtimeEntered( gameData, isEditorMode );
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
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
        RFDHLog.printlnEx( "Exited cockpit." );
        
        //realtimeStartTime = -1f;
        this.isComingOutOfGarage = true;
        
        this.isInPits = true;
        this.isInGarage = true;
        
        this.waitingForGraphics = true;
        this.waitingForData = true;
        
        this.needsOnVehicleControlChangedEvent = true;
        
        try
        {
            if ( gameData.getProfileInfo().isValid() )
            {
                __GDPrivilegedAccess.setRealtimeMode( false, gameData, isEditorMode );
                
                eventsDispatcher.fireOnRealtimeExited( gameData, isEditorMode );
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
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
        __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
        waitingForData = true;
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( false );
        
        texturesRequested = false;
        
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
            eventsDispatcher.fireOnGarageExited( gameData, isEditorMode );
            
            if ( !isEditorMode )
            {
                result = reloadConfigAndSetupTexture( false );
                
                if ( result != 0 )
                    eventsDispatcher.fireOnGarageExited( gameData, isEditorMode );
            }
        }
        else if ( !this.isInGarage && isInGarage )
        {
            this.isInGarage = true;
            eventsDispatcher.fireOnGarageEntered( gameData, isEditorMode );
            
            if ( !isEditorMode )
            {
                result = reloadConfigAndSetupTexture( false );
                
                if ( result != 0 )
                    eventsDispatcher.fireOnGarageEntered( gameData, isEditorMode );
            }
        }
        
        final boolean isInPits = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
        
        if ( this.isInPits && !isInPits )
        {
            this.isInPits = isInPits;
            eventsDispatcher.fireOnPitsExited( gameData, isEditorMode );
        }
        else if ( !this.isInPits && isInPits )
        {
            this.isInPits = isInPits;
            eventsDispatcher.fireOnPitsEntered( gameData, isEditorMode );
        }
        
        return ( result );
    }
    
    private byte checkWaitingData( boolean isEditorMode, boolean forceReload )
    {
        eventsDispatcher.checkAndFireOnNeededDataComplete( gameData, isEditorMode );
        
        boolean waitingForSetup2 = ( System.nanoTime() <= setupReloadTryTime );
        if ( simulationMode )
            waitingForSetup2 = false;
        
        if ( waitingForSetup || waitingForSetup2 )
        {
            if ( reloadSetup() )
            {
                waitingForSetup = false;
                waitingForSetup2 = false;
                setupReloadTryTime = -1L;
                
                eventsDispatcher.fireOnVehicleSetupUpdated( gameData, isEditorMode );
            }
        }
        
        if ( !waitingForData  )
        {
            byte result = 1;
            
            if ( texturesRequested )
            {
                result = 2;
                
                texturesRequested = false;
            }
            
            return ( widgetsManager.getWidgetsConfiguration().isValid() ? result : (byte)0 );
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
                    String vehicleName = gameData.getVehicleInfo().getTeamName();
                    if ( ( vehicleName == null ) || ( vehicleName.length() == 0 ) )
                        vehicleName = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
                    SessionType sessionType = gameData.getScoringInfo().getSessionType();
                    String trackName = gameData.getTrackInfo().getTrackName();
                    RFDHLog.printlnEx( "Session started. (Mod: \"" + modName + "\", Car: \"" + vehicleName + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackName + "\")" );
                    
                    String trackname = gameData.getTrackInfo().getTrackName();
                    if ( !trackname.equals( lastTrackname ) )
                    {
                        eventsDispatcher.fireOnTrackChanged( trackname, gameData, isEditorMode );
                        lastTrackname = trackname;
                    }
                    
                    eventsDispatcher.fireOnSessionStarted( gameData.getScoringInfo().getSessionType(), gameData, isEditorMode );
                    needsOnVehicleControlChangedEvent = true;
                }
                
                this.sessionJustStarted = false;
            }
            
            waitingForData = false;
            
            result = reloadConfigAndSetupTexture( forceReload );
        }
        else if ( gameData.isInRealtimeMode() )
        {
            result = 1;
            
            if ( texturesRequested )
            {
                result = 2;
                
                texturesRequested = false;
            }
            
            result = widgetsManager.getWidgetsConfiguration().isValid() ? result : (byte)0;
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
            boolean vpChanged = __WCPrivilegedAccess.setViewport( viewportX, viewportY, viewportWidth, viewportHeight, widgetsManager.getWidgetsConfiguration() );
            
            if ( ( viewportWidth > gameData.getGameResolution().getResX() ) || ( viewportHeight > gameData.getGameResolution().getResY() ) )
            {
                widgetsManager.resizeMainTexture( viewportWidth, viewportHeight );
            }
            
            if ( isSessionRunning() && gameData.getProfileInfo().isValid() )
            {
                if ( vpChanged )
                {
                    //Logger.log( "INFO: (Viewport changed) " + viewportX + ", " + viewportY + ", " + viewportWidth + "x" + viewportHeight );
                    
                    if ( gameData.getProfileInfo().isValid() )
                    {
                        if ( !gameData.isInRealtimeMode() && ( viewportY == 0 ) )
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
                    
                    gameData.getGraphicsInfo().onViewportChanged( viewportX, viewportY, viewportWidth, viewportHeight );
                }
                else if ( !gameData.isInRealtimeMode() && ( viewportY == 0 ) )
                {
                    __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
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
            RFDHLog.exception( t );
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
            RFDHLog.exception( t );
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
            
            if ( waitingForGarageStartLocation )
            {
                final VehicleScoringInfo vsi = gameData.getScoringInfo().getPlayersVehicleScoringInfo();
                vsi.getWorldPosition( garageStartLocation );
                vsi.getOrientationX( garageStartOrientationX );
                vsi.getOrientationY( garageStartOrientationY );
                vsi.getOrientationZ( garageStartOrientationZ );
                
                this.isInPits = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
                this.isInGarage = isInPits;
                
                this.waitingForGarageStartLocation = false;
            }
            
            this.waitingForScoring = false;
            
            this.lastSessionTime = gameData.getScoringInfo().getSessionTime();
            
            if ( gameData.getProfileInfo().isValid() )
            {
                result = checkWaitingData( isEditorMode, false );
                
                eventsDispatcher.fireOnScoringInfoUpdated( gameData, isEditorMode );
                
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
                        eventsDispatcher.fireOnVehicleControlChanged( viewedVSI, gameData, isEditorMode );
                        
                        result2 = reloadConfigAndSetupTexture( false );
                        
                        if ( result2 == 2 )
                        {
                            eventsDispatcher.fireOnVehicleControlChanged( viewedVSI, gameData, isEditorMode );
                            needsOnVehicleControlChangedEvent = false;
                        }
                        
                        if ( result2 != 1 )
                        {
                            result = result2;
                        }
                    }
                    else if ( needsOnVehicleControlChangedEvent )
                    {
                        eventsDispatcher.fireOnVehicleControlChanged( viewedVSI, gameData, isEditorMode );
                        needsOnVehicleControlChangedEvent = false;
                    }
                }
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
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
                eventsDispatcher.fireOnLapStarted( vsi, gameData, isEditorMode );
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
