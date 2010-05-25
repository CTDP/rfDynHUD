package net.ctdp.rfdynhud.gamedata;

import java.util.HashMap;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
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
    
    private boolean running = false;
    
    private boolean sessionRunning = false;
    
    @SuppressWarnings( "unused" )
    private boolean isComingOutOfGarage = true;
    
    private boolean waitingForData = false;
    private boolean waitingForTelemetry = false;
    private boolean waitingForScoring = false;
    private boolean waitingForSetup = false;
    
    private boolean nextReloadForced = false;
    
    private boolean isInGarage = true;
    private boolean isInPits = true;
    private final TelemVect3 garageStartLocation = new TelemVect3();
    private final TelemVect3 garageStartOrientationX = new TelemVect3();
    private final TelemVect3 garageStartOrientationY = new TelemVect3();
    private final TelemVect3 garageStartOrientationZ = new TelemVect3();
    
    private int lastViewedVSIId = -1;
    private VehicleControl lastControl = null;
    
    private boolean physicsLoadedOnce = false;
    
    private final HashMap<Integer, Integer> lastKnownLaps = new HashMap<Integer, Integer>();
    
    private String lastTrackname = null;
    
    private long lastSessionStartedTimestamp = -1L;
    private float lastSessionTime = 0f;
    
    public void setGameData( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
    
    public void beforeWidgetsConfigurationCleared( WidgetsConfiguration widgetsConfig )
    {
        int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
            widgetsConfig.getWidget( i ).clearRegion( false, ( (WidgetsDrawingManager)widgetsConfig ).getMainTexture() );
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
    
    private void reloadPhysics( boolean isEditorMode, boolean onlyOnce )
    {
        if ( !onlyOnce || !physicsLoadedOnce )
        {
            if ( isEditorMode )
                __GDPrivilegedAccess.loadEditorDefaults( gameData.getPhysics() );
            else
                __GDPrivilegedAccess.loadFromPhysicsFiles( gameData.getProfileInfo(), gameData.getTrackInfo(), gameData.getPhysics() );
            
            physicsLoadedOnce = true;
        }
    }
    
    private boolean reloadSetup( boolean isEditorMode )
    {
        if ( __GDPrivilegedAccess.loadSetup( isEditorMode, gameData ) )
        {
            __GDPrivilegedAccess.setEngineBoostMapping( gameData.getSetup().getEngine().getBoostMapping(), gameData.getTelemetryData() );
            
            return ( true );
        }
        
        return ( false );
    }
    
    private boolean reloadConfigAndSetupTexture()
    {
        boolean result = false;
        
        try
        {
            //if ( ResourceManager.isJarMode() )
            {
                EditorPresets editorPresets = null;
                
                String modName = gameData.getModInfo().getName();
                String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
                SessionType sessionType = gameData.getScoringInfo().getSessionType();
                String trackName = gameData.getTrackInfo().getTrackName();
                if ( gameData.isInRealtimeMode() )
                    Logger.log( "Entered cockpit. (Mod: \"" + modName + "\", Car: \"" + vehicleClass + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackName + "\")" );
                else
                    Logger.log( "Displaying session monitor. (Mod: \"" + modName + "\", Car: \"" + vehicleClass + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackName + "\")" );
                if ( ConfigurationLoader.reloadConfiguration( isInGarage, modName, vehicleClass, sessionType, widgetsManager, gameData, editorPresets, null, nextReloadForced ) )
                {
                    TextureDirtyRectsManager.forceCompleteRedraw();
                    widgetsManager.collectTextures( gameData, editorPresets );
                    widgetsManager.clearCompleteTexture();
                    nextReloadForced = false;
                    
                    result = true;
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        TextureDirtyRectsManager.forceCompleteRedraw();
        
        return ( result );
    }
    
    public byte onSessionStarted( EditorPresets editorPresets )
    {
        byte result = 0;
        
        this.sessionRunning = true;
        this.isComingOutOfGarage = true;
        this.isInGarage = true;
        this.waitingForData = ( editorPresets == null );
        this.waitingForTelemetry = ( editorPresets == null );
        this.waitingForScoring = ( editorPresets == null );
        this.waitingForSetup = false;
        this.lastSessionStartedTimestamp = System.nanoTime();
        this.lastSessionTime = gameData.getScoringInfo().getSessionTime();
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        this.lastKnownLaps.clear();
        
        try
        {
            ThreeLetterCodeManager.updateThreeLetterCodes();
            __GDPrivilegedAccess.updateInfo( gameData );
            
            reloadPhysics( editorPresets != null, true );
            reloadSetup( editorPresets != null );
            
            __GDPrivilegedAccess.onSessionStarted( gameData );
            
            // We cannot load the configuration here, because we don't know, which one to load (no scoring info).
            
            String trackname = gameData.getTrackInfo().getTrackName();
            if ( !trackname.equals( lastTrackname ) )
            {
                widgetsManager.fireOnTrackChanged( trackname, gameData, editorPresets );
                lastTrackname = trackname;
            }
            
            widgetsManager.fireOnSessionStarted( gameData.getScoringInfo().getSessionType(), gameData, editorPresets );
        }
        catch ( Throwable t )
        {
        }
        
        return ( result );
    }
    
    /**
     * This method must be called when a session has been started.
     * Note: LiveGameData must have been updated before.
     */
    public byte onSessionStarted()
    {
        return ( onSessionStarted( null ) );
    }
    
    /**
     * 
     * @param editorPresets
     */
    public void onSessionEnded( EditorPresets editorPresets )
    {
        this.waitingForData = false;
        this.waitingForTelemetry = false;
        this.waitingForScoring = false;
        this.waitingForSetup = false;
        //this.sessionStartTime = -1f;
        this.sessionRunning = false;
    }
    
    /**
     * This method must be called when a session has been ended.
     */
    public final void onSessionEnded()
    {
        onSessionEnded( null );
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
    
    public byte onRealtimeEntered( EditorPresets editorPresets )
    {
        byte result = 0;
        
        this.isComingOutOfGarage = true;
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        this.waitingForData = ( editorPresets == null );
        this.waitingForTelemetry = ( editorPresets == null );
        this.waitingForScoring = ( editorPresets == null );
        this.waitingForSetup = ( editorPresets == null );
        
        if ( editorPresets == null )
        {
            Logger.log( "Entered cockpit." );
        }
        
        try
        {
            ThreeLetterCodeManager.updateThreeLetterCodes();
            
            __GDPrivilegedAccess.updateInfo( gameData );
            __GDPrivilegedAccess.setRealtimeMode( true, gameData );
            
            reloadPhysics( editorPresets != null, false );
            
            if ( reloadSetup( editorPresets != null ) )
            {
                waitingForSetup = false;
            }
            
            if ( editorPresets == null )
            {
                if ( !gameData.getScoringInfo().getSessionType().isRace() )
                {
                    if ( reloadConfigAndSetupTexture() )
                        result = 2;
                    else
                        result = 1;
                }
                
                /*
                File lastConfigFile = ConfigurationLoader.getCurrentlyLoadedConfigFile();
                if ( ( lastConfigFile != null ) && lastConfigFile.getName().toLowerCase().contains( "_garage" ) )
                {
                    widgetsManager.clear( gameData, null, this );
                    TextureDirtyRectsManager.forceCompleteRedraw();
                    widgetsManager.collectTextures( gameData, null );
                    widgetsManager.clearCompleteTexture();
                    
                    nextReloadForced = true;
                    
                    result = 2;
                }
                */
            }
            
            widgetsManager.fireOnRealtimeEntered( gameData, editorPresets );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        return ( result );
    }
    
    /**
     * This method must be called when realtime mode has been entered (the user clicked on "Drive").
     * 
     * @return true, if textures need to be updated.
     */
    public final byte onRealtimeEntered()
    {
        return ( onRealtimeEntered( null ) );
    }
    
    public void onRealtimeExited( EditorPresets editorPresets )
    {
        Logger.log( "Exited cockpit." );
        
        //realtimeStartTime = -1f;
        this.isComingOutOfGarage = true;
        
        this.isInPits = true;
        this.isInGarage = true;
        
        try
        {
            __GDPrivilegedAccess.setRealtimeMode( false, gameData );
            
            widgetsManager.fireOnRealtimeExited( gameData, editorPresets );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    /**
     * This method must be called when the user exited realtime mode (pressed ESCAPE in the cockpit).
     */
    public final byte onRealtimeExited()
    {
        byte result = 1;
        
        onRealtimeExited( null );
        
        return ( result );
    }
    
    public boolean reloadConfiguration( boolean force )
    {
        try
        {
            VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
            String modName = gameData.getModInfo().getName();
            String vehicleClass = viewedVSI.getVehicleClass();
            SessionType sessionType = gameData.getScoringInfo().getSessionType();
            if ( ConfigurationLoader.reloadConfiguration( isInGarage && viewedVSI.isPlayer(), modName, vehicleClass, sessionType, widgetsManager, gameData, null, this, force ) )
            {
                //widgetsManager.clearCompleteTexture();
                //TextureDirtyRectsManager.forceCompleteRedraw();
                
                widgetsManager.collectTextures( gameData, null );
                
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
    
    /*
    private final boolean checkIsInGarage()
    {
        if ( !isInRealtimeMode() )
            return ( true );
        
        if ( !gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits() )
            return ( false );
        
        gameData.getTelemetryData().getPosition( position );
        
        if ( garageStartLocation.getDistanceXZToSquared( position ) < 4f ) // distance < 2 m
            return ( true );
        
        return ( false );
    }
    */
    
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
    
    private boolean checkPosition( EditorPresets editorPresets )
    {
        if ( !isInGarage ) // For now we don't support reentering the garage with a special configuration, because of the inaccurate check.
            return ( false );
        
        boolean result = false;
        
        boolean isInGarage = checkIsInGarage();
        
        if ( this.isInGarage && !isInGarage )
        {
            this.isInGarage = false;
            widgetsManager.fireOnGarageExited( gameData, editorPresets );
            
            if ( ( rfDynHUD != null ) && reloadConfiguration( false ) )
            {
                widgetsManager.fireOnGarageExited( gameData, editorPresets );
                
                result = true;
            }
        }
        else if ( !this.isInGarage && isInGarage )
        {
            this.isInGarage = true;
            widgetsManager.fireOnGarageEntered( gameData, editorPresets );
            
            if ( ( rfDynHUD != null ) && reloadConfiguration( false ) )
            {
                widgetsManager.fireOnGarageEntered( gameData, editorPresets );
                
                result = true;
            }
        }
        
        final boolean isInPits = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
        
        if ( this.isInPits && !isInPits )
        {
            this.isInPits = isInPits;
            widgetsManager.fireOnPitsExited( gameData, editorPresets );
        }
        else if ( !this.isInPits && isInPits )
        {
            this.isInPits = isInPits;
            widgetsManager.fireOnPitsEntered( gameData, editorPresets );
        }
        
        return ( result );
    }
    
    private boolean checkWaitingData()
    {
        if ( !waitingForData  )
        {
            return ( false );
        }
        
        if ( waitingForSetup )
        {
            if ( reloadSetup( false ) )
                waitingForSetup = false;
        }
        
        boolean result = false;
        
        if ( !waitingForTelemetry && !waitingForScoring && !waitingForSetup )
        {
            isInGarage = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
            
            if ( gameData.getScoringInfo().getSessionType().isRace() || !gameData.isInRealtimeMode() )
            {
                result = reloadConfigAndSetupTexture();
            }
            else
            {
                result = reloadConfiguration( false );
            }
            
            waitingForData = false;
        }
        
        return ( result );
    }
    
    /**
     * This method must be called when TelemetryData has been updated.
     * 
     * @return true, if textures need to be updated.
     */
    public final byte onTelemetryDataUpdated()
    {
        byte result = gameData.isInRealtimeMode() || !waitingForData ? (byte)1 : (byte)0;
        
        waitingForTelemetry = false;
        
        if ( checkWaitingData() )
        {
            result = 2;
        }
        
        return ( result );
    }
    
    /**
     * This method must be called when ScoringInfo has been updated.
     * 
     * @return true, if textures need to be updated.
     */
    public final byte onScoringInfoUpdated()
    {
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
        
        byte result = gameData.isInRealtimeMode() || !waitingForData ? (byte)1 : (byte)0;
        
        waitingForScoring = false;
        
        if ( checkWaitingData() )
        {
            result = 2;
        }
        
        if ( !waitingForData )
        {
            if ( checkPosition( null ) )
            {
                result = 2;
            }
            
            VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
            
            if ( ( lastViewedVSIId == -1 ) || ( viewedVSI.getDriverId() != lastViewedVSIId ) || ( viewedVSI.getVehicleControl() != lastControl ) )
            {
                lastViewedVSIId = viewedVSI.getDriverId();
                lastControl = viewedVSI.getVehicleControl();
                widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, null );
                
                if ( ( rfDynHUD != null ) && reloadConfiguration( false ) )
                {
                    widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, null );
                    
                    result = 2;
                }
            }
        }
        
        this.lastSessionTime = gameData.getScoringInfo().getSessionTime();
        
        return ( result );
    }
    
    public final void checkRaceRestart( long updateTimestamp )
    {
        if ( ( lastSessionStartedTimestamp != -1L ) && ( updateTimestamp - lastSessionStartedTimestamp > 3000000000L ) && ( gameData.getScoringInfo().getSessionTime() > 0f ) && ( lastSessionTime > gameData.getScoringInfo().getSessionTime() ) )
        {
            onSessionStarted( null );
        }
    }
    
    public final void checkAndFireOnLapStarted( EditorPresets editorPresets )
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
            int lap = vsi.getLapsCompleted() + 1;
            Integer lastKnownLap = lastKnownLaps.get( vsi.getDriverID() );
            if ( ( lastKnownLap == null ) || ( lap != lastKnownLap.intValue() ) )
            {
                lastKnownLaps.put( vsi.getDriverID(), lap );
                
                widgetsManager.fireOnLapStarted( vsi, gameData, editorPresets );
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
