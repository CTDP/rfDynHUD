package net.ctdp.rfdynhud.util;

import java.io.File;
import java.util.HashMap;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.TelemetryData;
import net.ctdp.rfdynhud.gamedata.VehicleControl;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
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
    private int enterRealtimePending = 0;
    private byte telemDataCurrentForSession = 0;
    private byte scoringInfoCurrentForSession = 0;
    
    private boolean realtimeMode2 = false;
    @SuppressWarnings( "unused" )
    private boolean isComingOutOfGarage = true;
    
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
    
    public void onSessionStarted( EditorPresets editorPresets )
    {
        try
        {
            this.isComingOutOfGarage = true;
            this.sessionRunning = true;
            this.telemDataCurrentForSession = 0;
            this.scoringInfoCurrentForSession = 0;
            this.lastSessionStartedTimestamp = System.nanoTime();
            this.lastSessionTime = gameData.getScoringInfo().getSessionTime();
            
            lastKnownLaps.clear();
            
            __GDPrivilegedAccess.onSessionStarted( gameData );
            
//Logger.log( gameData.getScoringInfo().getSessionType() );
            __GDPrivilegedAccess.resetStintLengths( gameData.getScoringInfo() );
            
            if ( !physicsLoadedOnce )
            {
                if ( editorPresets == null )
                    __GDPrivilegedAccess.loadFromPhysicsFiles( gameData.getPhysics(), gameData.getScoringInfo().getTrackName() );
                else
                    __GDPrivilegedAccess.loadEditorDefaults( gameData.getPhysics() );
                
                physicsLoadedOnce = true;
            }
            
            String trackname = gameData.getScoringInfo().getTrackName();
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
    }
    
    /**
     * This method must be called when a session has been started.
     * Note: LiveGameData must have been updated before.
     */
    public void onSessionStarted()
    {
        onSessionStarted( null );
    }
    
    /**
     * 
     * @param editorPresets
     */
    public void onSessionEnded( EditorPresets editorPresets )
    {
        this.telemDataCurrentForSession = -1;
        this.scoringInfoCurrentForSession = -1;
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
    
    public void beforeWidgetsConfigurationCleared( WidgetsConfiguration widgetsConfig )
    {
        int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
            widgetsConfig.getWidget( i ).clearRegion( false, widgetsManager.getMainTexture() );
    }
    
    private void onRealtimeEntered1( EditorPresets editorPresets )
    {
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        this.isComingOutOfGarage = true;
        
        ThreeLetterCodeManager.updateThreeLetterCodes();
        
        String trackName = gameData.getScoringInfo().getTrackName();
        
        try
        {
            if ( editorPresets == null )
            {
                __GDPrivilegedAccess.loadFromPhysicsFiles( gameData.getPhysics(), trackName );
                VehicleSetup.loadSetup( gameData );
                __GDPrivilegedAccess.setEngineBoostMapping( gameData.getSetup().getEngine().getBoostMapping(), gameData.getTelemetryData() );
            }
            else
            {
                __GDPrivilegedAccess.loadEditorDefaults( gameData.getPhysics() );
                VehicleSetup.loadEditorDefaults( gameData );
            }
            
            physicsLoadedOnce = true;
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        try
        {
            modName = RFactorTools.getModName( null );
            
            if ( ResourceManager.isJarMode() && ( editorPresets == null ) )
            {
                final ScoringInfo scoringInfo = gameData.getScoringInfo();
                
                String vehicleClass = scoringInfo.getPlayersVehicleScoringInfo().getVehicleClass();
                SessionType sessionType = scoringInfo.getSessionType();
                Logger.log( "Entered cockpit. (Mod: \"" + modName + "\", Car: \"" + vehicleClass + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackName + "\")" );
                if ( ConfigurationLoader.reloadConfiguration( isInGarage, modName, vehicleClass, sessionType, widgetsManager, gameData, editorPresets, null, nextReloadForced ) )
                {
                    TextureDirtyRectsManager.forceCompleteRedraw();
                    widgetsManager.collectTextures( gameData, editorPresets );
                    widgetsManager.clearCompleteTexture();
                    nextReloadForced = false;
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        try
        {
            widgetsManager.fireOnRealtimeEntered( 1, gameData, editorPresets );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        TextureDirtyRectsManager.forceCompleteRedraw();
    }
    
    /**
     * TelemetryData and ScoringInfo are valid here.
     * 
     * @param editorPresets
     */
    private void onRealtimeEntered2( EditorPresets editorPresets )
    {
        this.realtimeMode2 = true;
        
        try
        {
            __GDPrivilegedAccess.setEngineBoostMapping( gameData.getSetup().getEngine().getBoostMapping(), gameData.getTelemetryData() );
            widgetsManager.clearCompleteTexture();
            
            final ScoringInfo scoringInfo = gameData.getScoringInfo();
            final TelemetryData telemData = gameData.getTelemetryData();
            
            telemData.getPosition( garageStartLocation );
            telemData.getOrientationX( garageStartOrientationX );
            telemData.getOrientationY( garageStartOrientationY );
            telemData.getOrientationZ( garageStartOrientationZ );
            this.isInPits = scoringInfo.getPlayersVehicleScoringInfo().isInPits();
            this.isInGarage = isInPits;
            
            __GDPrivilegedAccess.onRealtimeEntered( gameData );
            widgetsManager.fireOnRealtimeEntered( 2, gameData, editorPresets );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    public void onRealtimeEntered( EditorPresets editorPresets )
    {
        // this method is only called from the editor.
        
        onRealtimeEntered1( editorPresets );
        onRealtimeEntered2( editorPresets );
    }
    
    /**
     * This method must be called when realtime mode has been entered (the user clicked on "Drive").
     * 
     * @return true, if textures need to be updated.
     */
    public final byte onRealtimeEntered()
    {
        __GDPrivilegedAccess.setRealtimeMode( true, gameData );
        
        enterRealtimePending = 1;
        
        if ( scoringInfoCurrentForSession == 1 )
        {
            isInGarage = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
            
            onRealtimeEntered1( null );
            
            return ( 1 );
        }
        
        File lastConfigFile = ConfigurationLoader.getCurrentlyLoadedConfigFile();
        if ( ( lastConfigFile != null ) && lastConfigFile.getName().toLowerCase().contains( "_garage" ) )
        {
            widgetsManager.clear( gameData, null, this );
            TextureDirtyRectsManager.forceCompleteRedraw();
            widgetsManager.collectTextures( gameData, null );
            widgetsManager.clearCompleteTexture();
            
            nextReloadForced = true;
            
            return ( -1 );
        }
        
        return ( 0 );
    }
    
    public void onRealtimeExited( EditorPresets editorPresets )
    {
        //realtimeStartTime = -1f;
        this.realtimeMode2 = false;
        this.isComingOutOfGarage = true;
        
        this.isInPits = true;
        this.isInGarage = true;
        
        try
        {
            __GDPrivilegedAccess.onRealtimeExited( gameData );
            
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
    public final void onRealtimeExited()
    {
        __GDPrivilegedAccess.setRealtimeMode( false, gameData );
        
        //if ( enterRealtimePending == 0 )
        {
            onRealtimeExited( null );
        }
        
        enterRealtimePending = 0;
    }
    
    public boolean reloadConfiguration()
    {
        try
        {
            VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
            String vehicleClass = viewedVSI.getVehicleClass();
            SessionType sessionType = gameData.getScoringInfo().getSessionType();
            if ( ConfigurationLoader.reloadConfiguration( isInGarage && viewedVSI.isPlayer(), modName, vehicleClass, sessionType, widgetsManager, gameData, null, this, false ) )
            {
                //widgetsManager.clearCompleteTexture();
                //TextureDirtyRectsManager.forceCompleteRedraw();
                
                widgetsManager.collectTextures( gameData, null );
                
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
        if ( !realtimeMode2 )
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
    
    private void checkPosition( EditorPresets editorPresets )
    {
        if ( !isInGarage ) // For now we don't support reentering the garage with a special configuration, because of the inaccurate check.
            return;
        
        boolean isInGarage = checkIsInGarage();
        
        if ( this.isInGarage && !isInGarage )
        {
            this.isInGarage = false;
            widgetsManager.fireOnGarageExited( gameData, editorPresets );
            
            if ( ( rfDynHUD != null ) && realtimeMode2 && reloadConfiguration() )
                widgetsManager.fireOnGarageExited( gameData, editorPresets );
        }
        else if ( !this.isInGarage && isInGarage )
        {
            this.isInGarage = true;
            widgetsManager.fireOnGarageEntered( gameData, editorPresets );
            
            if ( ( rfDynHUD != null ) && realtimeMode2 && reloadConfiguration() )
                widgetsManager.fireOnGarageEntered( gameData, editorPresets );
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
    }
    
    /**
     * This method must be called when TelemetryData has been updated.
     * 
     * @return true, if textures need to be updated.
     */
    public final boolean onTelemetryDataUpdated()
    {
        boolean result = false;
        
        if ( scoringInfoCurrentForSession == 1 )
            checkPosition( null );
        
        if ( ( enterRealtimePending & 1 ) != 0 )
        {
            enterRealtimePending |= 2;
            if ( enterRealtimePending == 7 )
            {
                if ( ( telemDataCurrentForSession == 0 ) && ( scoringInfoCurrentForSession == 1 ) )
                {
                    onRealtimeEntered1( null );
                    result = true;
                }
                onRealtimeEntered2( null );
                enterRealtimePending = 0;
            }
        }
        
        if ( sessionRunning )
            this.telemDataCurrentForSession = 1;
        
        return ( result );
    }
    
    /**
     * This method must be called when ScoringInfo has been updated.
     * 
     * @return true, if textures need to be updated.
     */
    public final boolean onScoringInfoUpdated()
    {
        boolean result = false;
        
        if ( ( enterRealtimePending & 1 ) != 0 )
        {
            if ( ( enterRealtimePending & 4 ) == 0 )
            {
                isInGarage = gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits();
            }
            
            enterRealtimePending |= 4;
            if ( enterRealtimePending == 7 )
            {
                if ( ( telemDataCurrentForSession == 1 ) && ( scoringInfoCurrentForSession == 0 ) )
                {
                    onRealtimeEntered1( null );
                    result = true;
                }
                onRealtimeEntered2( null );
                enterRealtimePending = 0;
            }
        }
        
        if ( sessionRunning )
            this.scoringInfoCurrentForSession = 1;
        
        if ( realtimeMode2 )
        {
            VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
            
            if ( ( lastViewedVSIId == -1 ) || ( viewedVSI.getDriverId() != lastViewedVSIId ) || ( viewedVSI.getVehicleControl() != lastControl ) )
            {
                lastViewedVSIId = viewedVSI.getDriverId();
                lastControl = viewedVSI.getVehicleControl();
                widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, null );
                
                if ( ( rfDynHUD != null ) && reloadConfiguration() )
                    widgetsManager.fireOnVehicleControlChanged( viewedVSI, gameData, null );
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
