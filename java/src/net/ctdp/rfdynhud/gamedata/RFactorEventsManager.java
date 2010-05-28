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
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationClearListener;

/**
 * The events manager receives events from rFactor and modifies state-flags appropriately.
 * 
 * @author Marvin Froehlich
 */
public class RFactorEventsManager implements ConfigurationClearListener
{
    private final RFDynHUD rfDynHUD;
    private final WidgetsDrawingManager widgetsManager;
    private LiveGameData gameData = null;
    
    private boolean running = false;
    
    private boolean sessionRunning = false;
    
    @SuppressWarnings( "unused" )
    private boolean isComingOutOfGarage = true;
    
    private boolean sessionJustStarted = false;
    private boolean waitingForData = false;
    private boolean waitingForTelemetry = false;
    private boolean waitingForScoring = false;
    private boolean waitingForSetup = false;
    private boolean waitingForGraphics = false;
    
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
                int gameResX = widgetsManager.getMainTexture().getWidth();
                int gameResY = widgetsManager.getMainTexture().getHeight();
                int viewportWidth = widgetsManager.getGameResolution().getResX();
                int viewportHeight = widgetsManager.getGameResolution().getResY();
                
                if ( ( viewportWidth == gameResX ) && ( viewportHeight == gameResY ) )
                    bigMonitor = true;
                else
                    smallMonitor = true;
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
            
            result = 0;
        }
        
        return ( result );
    }
    
    public byte onSessionStarted( EditorPresets editorPresets )
    {
        byte result = 0;
        
        this.sessionRunning = true;
        this.isComingOutOfGarage = true;
        this.isInGarage = true;
        this.waitingForTelemetry = ( editorPresets == null );
        this.waitingForScoring = ( editorPresets == null );
        this.waitingForSetup = false;
        this.waitingForGraphics = ( editorPresets == null );
        this.waitingForData = ( editorPresets == null );
        this.sessionJustStarted = true;
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
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
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
        this.waitingForGraphics = false;
        //this.sessionStartTime = -1f;
        this.sessionRunning = false;
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
        byte result = 0;
        
        this.isComingOutOfGarage = true;
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        this.waitingForTelemetry = false;//( editorPresets == null );
        this.waitingForScoring = false;//( editorPresets == null );
        this.waitingForSetup = false;//( editorPresets == null );
        this.waitingForGraphics = ( editorPresets == null );
        this.waitingForData = ( editorPresets == null );
        
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
                int gameResX = widgetsManager.getMainTexture().getWidth();
                int gameResY = widgetsManager.getMainTexture().getHeight();
                
                __WCPrivilegedAccess.setGameResolution( gameResX, gameResY, widgetsManager );
                
                if ( !gameData.getScoringInfo().getSessionType().isRace() )
                {
                    //result = reloadConfigAndSetupTexture( false );
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
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
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
        onRealtimeExited( null );
        
        //byte result = reloadConfigAndSetupTexture( false );
        __WCPrivilegedAccess.setValid( widgetsManager, false );
        waitingForData = true;
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( false );
        
        return ( 0 );
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
    
    private byte checkPosition( EditorPresets editorPresets )
    {
        if ( !isInGarage ) // For now we don't support reentering the garage with a special configuration, because of the inaccurate check.
            return ( 1 );
        
        byte result = 1;
        
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
    
    private byte checkWaitingData( boolean forceReload )
    {
        if ( !waitingForData  )
        {
            return ( widgetsManager.isValid() ? (byte)1 : (byte)0 );
        }
        
        if ( waitingForSetup )
        {
            if ( reloadSetup( false ) )
                waitingForSetup = false;
        }
        
        byte result = 0;
        
        if ( !waitingForTelemetry && !waitingForScoring && !waitingForSetup && !waitingForGraphics )
        {
            isInGarage = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
            
            if ( !gameData.isInRealtimeMode() && sessionJustStarted )
            {
                String modName = gameData.getModInfo().getName();
                String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
                SessionType sessionType = gameData.getScoringInfo().getSessionType();
                String trackName = gameData.getTrackInfo().getTrackName();
                Logger.log( "Session started. (Mod: \"" + modName + "\", Car: \"" + vehicleClass + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackName + "\")" );
                
                this.sessionJustStarted = false;
            }
            
            result = reloadConfigAndSetupTexture( forceReload );
            
            waitingForData = false;
        }
        else if ( gameData.isInRealtimeMode() )
        {
            result = widgetsManager.isValid() ? (byte)1 : (byte)0;
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
        waitingForTelemetry = false;
        
        byte result = checkWaitingData( false );
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
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
        
        waitingForScoring = false;
        
        this.lastSessionTime = gameData.getScoringInfo().getSessionTime();
        
        byte result = checkWaitingData( false );
        
        if ( !waitingForData && ( result != 0 ) )
        {
            byte result2 = checkPosition( null );
            
            if ( result2 != 1 )
            {
                result = result2;
            }
            
            VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
            
            if ( ( lastViewedVSIId == -1 ) || ( viewedVSI.getDriverId() != lastViewedVSIId ) || ( viewedVSI.getVehicleControl() != lastControl ) )
            {
                lastViewedVSIId = viewedVSI.getDriverId();
                lastControl = viewedVSI.getVehicleControl();
                widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, null );
                
                result2 = reloadConfigAndSetupTexture( false );
                
                if ( result2 == 2 )
                {
                    widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, null );
                }
                
                if ( result2 != 1 )
                {
                    result = result2;
                }
            }
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
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
        if ( !isSessionRunning() )
        {
            if ( rfDynHUD != null )
                rfDynHUD.setRenderMode( false );
            
            return ( 0 );
        }
        
        this.waitingForGraphics = false;
        
        byte result = 0;
        
        try
        {
            if ( __WCPrivilegedAccess.setGameResolution( viewportWidth, viewportHeight, widgetsManager ) )
            {
                //Logger.log( "Viewport changed: " + viewportWidth + "x" + viewportHeight );
                
                //result = reloadConfigAndSetupTexture( true );
                waitingForData = true;
                result = checkWaitingData( true );
            }
            else
            {
                result = checkWaitingData( false );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
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
     * @param rfDynHUD
     * @param widgetsManager
     */
    public RFactorEventsManager( RFDynHUD rfDynHUD, WidgetsDrawingManager widgetsManager )
    {
        this.rfDynHUD = rfDynHUD;
        this.widgetsManager = widgetsManager;
    }
}
