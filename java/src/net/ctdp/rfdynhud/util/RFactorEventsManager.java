package net.ctdp.rfdynhud.util;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationClearListener;

/**
 * The events manager receives events from rFactor and modifies state-flags appropriately.
 * 
 * @author Marvin Froehlich
 */
public class RFactorEventsManager implements ConfigurationClearListener
{
    private LiveGameData gameData = null;
    private final WidgetsDrawingManager widgetsManager;
    private final RFDynHUD rfDynHUD;
    
    private String modName = null;
    
    private boolean running = false;
    
    private boolean sessionRunning = false;
    
    private boolean realtimeMode = false;
    private boolean isComingOutOfGarage = true;
    
    private boolean isInGarage = true;
    private boolean isInPits = true;
    private final TelemVect3 garageStartLocation = new TelemVect3();
    
    private boolean physicsLoadedOnce = false;
    
    private int lastKnownLap = -1;
    private int lastKnownPlayerLap = -1;
    
    private String lastTrackname = null;
    
    public void setGameData( LiveGameData gameData )
    {
        this.gameData = gameData;
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
    
    public void onSessionStarted( boolean isEditorMode )
    {
        try
        {
            this.isComingOutOfGarage = true;
            this.sessionRunning = true;
            
            __GDPrivilegedAccess.onSessionStarted( gameData );
            
//Logger.log( gameData.getScoringInfo().getSessionType() );
            __GDPrivilegedAccess.resetStintLengths( gameData.getScoringInfo() );
            
            if ( !physicsLoadedOnce )
            {
                if ( isEditorMode )
                    __GDPrivilegedAccess.loadEditorDefaults( gameData.getPhysics() );
                else
                    __GDPrivilegedAccess.loadFromPhysicsFiles( gameData );
                
                physicsLoadedOnce = true;
            }
            
            String trackname = gameData.getScoringInfo().getTrackName();
            if ( !trackname.equals( lastTrackname ) )
            {
                widgetsManager.fireOnTrackChanged( trackname, gameData );
                lastTrackname = trackname;
            }
            
            widgetsManager.fireOnSessionStarted( isEditorMode, gameData.getScoringInfo().getSessionType(), gameData );
        }
        catch ( Throwable t )
        {
            
        }
    }
    
    /**
     * This method must be called when a session has been started.
     * Note: LiveGameData must have been updated before.
     */
    public void onSessionStarted()
    {
        onSessionStarted( false );
    }
    
    /**
     * 
     * @param isEditorMode
     */
    public void onSessionEnded( boolean isEditorMode )
    {
        //this.sessionStartTime = -1f;
        this.sessionRunning = false;
    }
    
    /**
     * This method must be called when a session has been ended.
     */
    public final void onSessionEnded()
    {
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
    
    public void beforeWidgetsConfigurationCleared( WidgetsConfiguration widgetsConfig )
    {
        int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
            widgetsConfig.getWidget( i ).clearRegion( false, widgetsManager.getMainTexture() );
    }
    
    public void onRealtimeEntered( boolean isEditorMode )
    {
        this.realtimeMode = true;
        this.isComingOutOfGarage = true;
        
        try
        {
            __GDPrivilegedAccess.onRealtimeEntered( gameData );
            
            gameData.getTelemetryData().getPosition( garageStartLocation );
            this.isInPits = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
            this.isInGarage = isInPits;
            
            if ( isEditorMode )
            {
                __GDPrivilegedAccess.loadEditorDefaults( gameData.getPhysics() );
                VehicleSetup.loadEditorDefaults( gameData );
            }
            else
            {
                __GDPrivilegedAccess.loadFromPhysicsFiles( gameData );
                VehicleSetup.loadSetup( gameData );
                __GDPrivilegedAccess.setEngineBoostMapping( gameData.getSetup().getEngine().getBoostMapping(), gameData.getTelemetryData() );
            }
            
            physicsLoadedOnce = true;
            
            if ( ResourceManager.isJarMode() && !isEditorMode )
            {
                modName = RFactorTools.getModName( null );
                String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
                SessionType sessionType = gameData.getScoringInfo().getSessionType();
                Logger.log( "Entered cockpit. (Mod: " + modName + ", Car: " + vehicleClass + ", Session: " + sessionType.name() + ", Track: " + gameData.getScoringInfo().getTrackName() + ")" );
                if ( ConfigurationLoader.reloadConfiguration( isInGarage, modName, vehicleClass, sessionType, widgetsManager, gameData, null ) )
                {
                    widgetsManager.clearCompleteTexture();
                    TextureDirtyRectsManager.forceCompleteRedraw();
                    widgetsManager.collectTextures( isEditorMode, widgetsManager );
                }
            }
            
            widgetsManager.fireOnRealtimeEntered( isEditorMode, gameData );
            
            TextureDirtyRectsManager.forceCompleteRedraw();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    /**
     * This method must be called when realtime mode has been entered (the user clicked on "Drive").
     * 
     * Note: LiveGameData must have been updated before.
     */
    public final void onRealtimeEntered()
    {
        onRealtimeEntered( false );
    }
    
    public boolean reloadConfiguration()
    {
        final boolean isEditorMode = false;
        
        try
        {
            String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
            SessionType sessionType = gameData.getScoringInfo().getSessionType();
            if ( ConfigurationLoader.reloadConfiguration( isInGarage, modName, vehicleClass, sessionType, widgetsManager, gameData, this ) )
            {
                //widgetsManager.clearCompleteTexture();
                //TextureDirtyRectsManager.forceCompleteRedraw();
                
                widgetsManager.collectTextures( isEditorMode, widgetsManager );
                
                if ( rfDynHUD != null )
                {
                    rfDynHUD.setFlag( RFDynHUD.FLAG_CONFIGURATION_RELOADED, true );
                }
                
                return ( true );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        return ( false );
    }
    
    private final TelemVect3 position = new TelemVect3();
    
    public void checkPosition()
    {
        if ( !isInGarage ) // For now we don't support reentering the garage with a special configuration, because of the inaccurate check.
            return;
        
        gameData.getTelemetryData().getPosition( position );
        
        boolean isInGarage = !isInRealtimeMode() || ( gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits() && ( garageStartLocation.getDistanceXZToSquared( position ) < 4f ) ); // distance < 2 m
        
        if ( this.isInGarage && !isInGarage )
        {
            this.isInGarage = false;
            widgetsManager.fireOnGarageExited( gameData );
            
            if ( ( rfDynHUD != null ) && realtimeMode && reloadConfiguration() )
                widgetsManager.fireOnGarageExited( gameData );
        }
        else if ( !this.isInGarage && isInGarage )
        {
            this.isInGarage = true;
            widgetsManager.fireOnGarageEntered( gameData );
            
            if ( ( rfDynHUD != null ) && realtimeMode && reloadConfiguration() )
                widgetsManager.fireOnGarageEntered( gameData );
        }
        
        final boolean isInPits = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
        
        if ( this.isInPits && !isInPits )
        {
            this.isInPits = isInPits;
            widgetsManager.fireOnPitsExited( gameData );
        }
        else if ( !this.isInPits && isInPits )
        {
            this.isInPits = isInPits;
            widgetsManager.fireOnPitsEntered( gameData );
        }
    }
    
    public void onRealtimeExited( boolean isEditorMode )
    {
        //realtimeStartTime = -1f;
        this.realtimeMode = false;
        this.isComingOutOfGarage = true;
        
        try
        {
            widgetsManager.fireOnRealtimeExited( isEditorMode, gameData );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    /**
     * This method must be called when the user exited realtime mode (pressed ESCAPE in the cockpit).
     */
    public final void onRealtimeExited()
    {
        onRealtimeExited( false );
    }
    
    /**
     * Returns whether the user is currently in realtime mode.
     * Note: {@link #getRealtimeStartTime()} may not be valid, if this method returns false.
     * 
     * @return whether the user is currently in realtime mode.
     */
    public final boolean isInRealtimeMode()
    {
        return ( realtimeMode );
    }
    
    public final void checkAndFireOnLapStarted( boolean isEditorMode )
    {
        if ( !gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits() )
            this.isComingOutOfGarage = false;
        
        if ( isComingOutOfGarage )
            return;
        
        if ( gameData.getScoringInfo().getSessionType().isRace() )
        {
            int lap = gameData.getScoringInfo().getVehicleScoringInfo( 0 ).getLapsCompleted() + 1;
            if ( lap != lastKnownLap )
            {
                lastKnownLap = lap;
                
                widgetsManager.fireOnLapStarted( isEditorMode, gameData );
            }
        }
        
        {
            int lap = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getLapsCompleted() + 1;
            if ( lap != lastKnownPlayerLap )
            {
                lastKnownPlayerLap = lap;
                
                widgetsManager.fireOnPlayerLapStarted( isEditorMode, gameData );
            }
        }
    }
    
    public void onEngineBoostMappingChanged( int oldValue, int newValue )
    {
        widgetsManager.fireOnEngineBoostMappingChanged( oldValue, newValue );
    }
    
    public void onTemporaryEngineBoostStateChanged( boolean enabled )
    {
        widgetsManager.fireOnTemporaryEngineBoostStateChanged( enabled );
    }
    
    /**
     * Creates a new {@link RFactorEventsManager}.
     * 
     * @param widgetsManager
     * @param rfDynHUD
     */
    public RFactorEventsManager( WidgetsDrawingManager widgetsManager, RFDynHUD rfDynHUD )
    {
        this.widgetsManager = widgetsManager;
        this.rfDynHUD = rfDynHUD;
    }
}
