package net.ctdp.rfdynhud.gamedata;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationClearListener;

/**
 * The events manager receives events from rFactor and modifies state-flags appropriately.
 * 
 * @author Marvin Froehlich
 */
public class GameEventsManager implements ConfigurationClearListener
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
        boolean result = false;
        
        if ( __GDPrivilegedAccess.loadSetup( isEditorMode, gameData ) )
        {
            result = true;
        }
        
        __GDPrivilegedAccess.setEngineBoostMapping( gameData.getSetup().getEngine().getBoostMapping(), gameData.getTelemetryData() );
        
        return ( result );
    }
    
    public byte reloadConfigAndSetupTexture( boolean force )
    {
        byte result = 1;
        
        try
        {
            EditorPresets editorPresets = null;
            
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
            Boolean result2 = ConfigurationLoader.reloadConfiguration( smallMonitor, bigMonitor, isInGarage && vsi.isPlayer(), modName, vehicleClass, sessionType, widgetsManager, gameData, editorPresets, this, force );
            
            if ( result2 == null )
            {
                result = 0;
            }
            else if ( result2.booleanValue() )
            {
                //if ( usePlayer )
                    TextureDirtyRectsManager.forceCompleteRedraw();
                widgetsManager.collectTextures( gameData, editorPresets );
                //if ( usePlayer )
                    widgetsManager.clearCompleteTexture();
                
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
    
    public byte onSessionStarted( EditorPresets editorPresets )
    {
        //Logger.log( ">>> onSessionStarted" );
        //if ( currentSessionIsRace == Boolean.TRUE )
        if ( sessionRunning )
        {
            //Logger.log( "INFO: Got a call to StartSession() in already started RACE session. Looks like an rFactor bug. Ignoring this call." );
            Logger.log( "INFO: Got a call to StartSession() in already started session. Looks like an rFactor bug. Ignoring this call." );
            return ( rfDynHUD.isInRenderMode() ? (byte)1 : (byte)0 );
        }
        
        this.sessionRunning = true;
        this.isComingOutOfGarage = true;
        this.isInGarage = true;
        
        this.waitingForGraphics = ( editorPresets == null );
        this.waitingForTelemetry = ( editorPresets == null );
        this.waitingForScoring = ( editorPresets == null );
        this.waitingForSetup = false;
        this.setupReloadTryTime = -1L;
        this.waitingForData = ( editorPresets == null );
        
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
                reloadPhysics( editorPresets != null, true );
                reloadSetup( editorPresets != null );
                
                __GDPrivilegedAccess.onSessionStarted( gameData, editorPresets );
                
                // We cannot load the configuration here, because we don't know, which one to load (no scoring info).
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> result: " + result );
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
        //Logger.log( ">>> onSessionEnded" );
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
        //Logger.log( ">>> onRealtimeEntered" );
        byte result = 0;
        
        this.isComingOutOfGarage = true;
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        this.waitingForGraphics = waitingForGraphics || ( editorPresets == null );
        this.waitingForTelemetry = waitingForTelemetry || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.waitingForScoring = waitingForScoring || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.waitingForSetup = false; //waitingForSetup || ( currentSessionIsRace != Boolean.FALSE ); //( editorPresets == null );
        this.setupReloadTryTime = System.nanoTime() + 2000000000L;
        this.waitingForData = ( editorPresets == null );
        
        if ( editorPresets == null )
        {
            Logger.log( "Entered cockpit." );
        }
        
        try
        {
            ThreeLetterCodeManager.updateThreeLetterCodes();
            
            __GDPrivilegedAccess.updateInfo( gameData );
            
            if ( gameData.getProfileInfo().isValid() )
            {
                __GDPrivilegedAccess.setRealtimeMode( true, gameData, editorPresets );
                
                reloadPhysics( editorPresets != null, false );
                
                if ( reloadSetup( editorPresets != null ) )
                {
                    waitingForSetup = false;
                    
                    widgetsManager.fireOnVehicleSetupUpdated( gameData, editorPresets );
                }
                
                widgetsManager.fireOnRealtimeEntered( gameData, editorPresets );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> result: " + result );
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
        //Logger.log( ">>> onRealtimeExited" );
        Logger.log( "Exited cockpit." );
        
        //realtimeStartTime = -1f;
        this.isComingOutOfGarage = true;
        
        this.isInPits = true;
        this.isInGarage = true;
        
        try
        {
            if ( gameData.getProfileInfo().isValid() )
            {
                __GDPrivilegedAccess.setRealtimeMode( false, gameData, editorPresets );
                
                widgetsManager.fireOnRealtimeExited( gameData, editorPresets );
            }
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
        onRealtimeExited( null );
        
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
    
    private byte checkPosition( EditorPresets editorPresets )
    {
        byte result = 1;
        
        if ( !isInGarage ) // For now we don't support reentering the garage with a special configuration, because of the inaccurate check.
            return ( result );
        
        boolean isInGarage = checkIsInGarage();
        
        if ( this.isInGarage && !isInGarage )
        {
            this.isInGarage = false;
            widgetsManager.fireOnGarageExited( gameData, editorPresets );
            
            if ( editorPresets == null )
            {
                result = reloadConfigAndSetupTexture( false );
                
                if ( result != 0 )
                    widgetsManager.fireOnGarageExited( gameData, editorPresets );
            }
        }
        else if ( !this.isInGarage && isInGarage )
        {
            this.isInGarage = true;
            widgetsManager.fireOnGarageEntered( gameData, editorPresets );
            
            if ( editorPresets == null )
            {
                result = reloadConfigAndSetupTexture( false );
                
                if ( result != 0 )
                    widgetsManager.fireOnGarageEntered( gameData, editorPresets );
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
    
    private byte checkWaitingData( EditorPresets editorPresets, boolean forceReload )
    {
        widgetsManager.checkAndFireOnNeededDataComplete( gameData, editorPresets );
        
        boolean waitingForSetup2 = ( System.nanoTime() <= setupReloadTryTime );
        
        if ( waitingForSetup || waitingForSetup2 )
        {
            if ( reloadSetup( false ) )
            {
                waitingForSetup = false;
                waitingForSetup2 = false;
                setupReloadTryTime = -1L;
                
                widgetsManager.fireOnVehicleSetupUpdated( gameData, editorPresets );
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
            
            __GDPrivilegedAccess.onSessionStarted2( gameData, editorPresets );
            
            if ( !gameData.isInRealtimeMode() && sessionJustStarted )
            {
                String modName = gameData.getModInfo().getName();
                String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
                SessionType sessionType = gameData.getScoringInfo().getSessionType();
                String trackName = gameData.getTrackInfo().getTrackName();
                Logger.log( "Session started. (Mod: \"" + modName + "\", Car: \"" + vehicleClass + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackName + "\")" );
                
                String trackname = gameData.getTrackInfo().getTrackName();
                if ( !trackname.equals( lastTrackname ) )
                {
                    widgetsManager.fireOnTrackChanged( trackname, gameData, editorPresets );
                    lastTrackname = trackname;
                }
                
                widgetsManager.fireOnSessionStarted( gameData.getScoringInfo().getSessionType(), gameData, editorPresets );
                
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
     * @param viewportX
     * @param viewportY
     * @param viewportWidth
     * @param viewportHeight
     * 
     * @return 0, if nothing shouldbe rendered anymore, 1 to render something, 2 to render and update texture info.
     */
    public final byte onGraphicsInfoUpdated( short viewportX, short viewportY, short viewportWidth, short viewportHeight )
    {
        //Logger.log( ">>> onGraphicsInfoUpdated" );
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
                            result = checkWaitingData( null, true );
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
                    result = checkWaitingData( null, false );
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> result: " + result );
        return ( result );
    }
    
    /**
     * 
     * @param editorPresets
     * @return
     */
    public final byte onTelemetryDataUpdated( EditorPresets editorPresets )
    {
        //Logger.log( ">>> onTelemetryDataUpdated" );
        byte result = 0;
        
        try
        {
            this.waitingForTelemetry = false;
            
            if ( gameData.getProfileInfo().isValid() )
            {
                result = checkWaitingData( editorPresets, false );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> result: " + result );
        return ( result );
    }
    
    /**
     * This method must be called when TelemetryData has been updated.
     * 
     * @return true, if textures need to be updated.
     */
    public final byte onTelemetryDataUpdated()
    {
        return ( onTelemetryDataUpdated( null ) );
    }
    
    public final byte onScoringInfoUpdated( EditorPresets editorPresets )
    {
        //Logger.log( ">>> onScoringInfoUpdated" );
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
                result = checkWaitingData( editorPresets, false );
                
                widgetsManager.fireOnScoringInfoUpdated( gameData, editorPresets );
                
                if ( !waitingForData && ( result != 0 ) )
                {
                    byte result2 = checkPosition( editorPresets );
                    
                    if ( result2 != 1 )
                    {
                        result = result2;
                    }
                    
                    VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
                    
                    if ( ( lastViewedVSIId == -1 ) || ( viewedVSI.getDriverId() != lastViewedVSIId ) || ( viewedVSI.getVehicleControl() != lastControl ) )
                    {
                        lastViewedVSIId = viewedVSI.getDriverId();
                        lastControl = viewedVSI.getVehicleControl();
                        widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, editorPresets );
                        
                        result2 = reloadConfigAndSetupTexture( false );
                        
                        if ( result2 == 2 )
                        {
                            widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, editorPresets );
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
        
        //Logger.log( ">>> result: " + result );
        return ( result );
    }
    
    /**
     * This method must be called when ScoringInfo has been updated.
     * 
     * @return true, if textures need to be updated.
     */
    public final byte onScoringInfoUpdated()
    {
        return ( onScoringInfoUpdated( null ) );
    }
    
    public final void checkRaceRestart( long updateTimestamp )
    {
        ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        if ( !waitingForScoring && scoringInfo.getSessionType().isRace() && ( lastSessionStartedTimestamp != -1L ) && ( updateTimestamp - lastSessionStartedTimestamp > 3000000000L ) && ( scoringInfo.getSessionTime() > 0f ) && ( lastSessionTime > scoringInfo.getSessionTime() ) )
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
            if ( vsi.isLapJustStarted() )
                widgetsManager.fireOnLapStarted( vsi, gameData, editorPresets );
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
     * Creates a new {@link GameEventsManager}.
     * 
     * @param rfDynHUD
     * @param widgetsManager
     */
    public GameEventsManager( RFDynHUD rfDynHUD, WidgetsDrawingManager widgetsManager )
    {
        this.rfDynHUD = rfDynHUD;
        this.widgetsManager = widgetsManager;
    }
}
