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
import java.util.Iterator;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;
import net.ctdp.rfdynhud.input.InputMapping;
import net.ctdp.rfdynhud.properties.AbstractPropertiesKeeper;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.render.WidgetsManager;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.ThreeLetterCodeManager;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationLoadListener;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

import org.jagatoo.util.Tools;

/**
 * The events manager receives events from rFactor and modifies state-flags appropriately.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class GameEventsManager implements ConfigurationLoadListener
{
    protected final RFDynHUD rfDynHUD;
    protected final WidgetsDrawingManager widgetsManager;
    protected final LiveGameData gameData;
    
    protected boolean running = false;
    
    protected boolean sessionRunning = false;
    
    protected boolean isComingOutOfGarage = true;
    
    protected boolean sessionJustStarted = false;
    protected Boolean currentSessionIsRace = null;
    
    protected boolean waitingForRender = false;
    protected boolean waitingForGraphics = false;
    protected boolean waitingForTelemetry = false;
    protected boolean waitingForScoring = false;
    protected boolean waitingForWeather = false;
    protected boolean waitingForGarageStartLocation = false;
    protected boolean waitingForSetup = false;
    protected long setupReloadTryTime = -1L;
    protected boolean waitingForData = false;
    protected Boolean cockpitLeftInRaceSession = null;
    
    protected boolean needsOnVehicleControlChangedEvent = false;
    
    protected boolean isInGarage = true;
    protected boolean isInPits = true;
    
    private int lastViewedVSIId = -1;
    private VehicleControl lastControl = null;
    
    protected boolean physicsLoadedOnce = false;
    
    private String lastTrackname = null;
    
    protected final GameEventsDispatcher eventsDispatcher;
    private final WidgetsManager renderListenersManager;
    
    private Iterator<File> configurationCandidatesIterator = null;
    protected final ConfigurationLoader loader = new ConfigurationLoader( this );
    private boolean texturesRequested = false;
    
    public final LiveGameData getGameData()
    {
        return ( gameData );
    }
    
    protected final WidgetsDrawingManager getWidgetsManager()
    {
        return ( widgetsManager );
    }
    
    protected final GameEventsDispatcher getEventsDispatcher()
    {
        return ( eventsDispatcher );
    }
    
    public final boolean hasWaitingWidgets()
    {
        return ( eventsDispatcher.hasWaitingWidgets() );
    }
    
    public void setConfigurationCandidatesIterator( Iterator<File> configurationCandidatesIterator )
    {
        this.configurationCandidatesIterator = configurationCandidatesIterator;
    }
    
    public final Iterator<File> getConfigurationCandidatesIterator()
    {
        return ( configurationCandidatesIterator );
    }
    
    public final ConfigurationLoader getConfigurationLoader()
    {
        return ( loader );
    }
    
    private void validateInputBindings()
    {
        if ( ( rfDynHUD == null ) || ( rfDynHUD.getInputMappings() == null ) )
            return;
        
        if ( !gameData.isInCockpit() )
            return;
        
        if ( !widgetsManager.getWidgetsConfiguration().isValid() )
            return;
        
        if ( !gameData.getProfileInfo().isValid() )
            return;
        
        String[] warning = gameData.getProfileInfo().validateInputBindings( rfDynHUD.getInputMappings() );
        
        if ( warning != null )
        {
            net.ctdp.rfdynhud.widgets.internal.InternalWidget internalWidget = new net.ctdp.rfdynhud.widgets.internal.InternalWidget();
            internalWidget.setMessage( warning );
            __WCPrivilegedAccess.addWidget( widgetsManager.getWidgetsConfiguration(), internalWidget, true, gameData );
            internalWidget.getSize().setEffectiveSize( 600, 200 );
            internalWidget.getPosition().setEffectivePosition( RelativePositioning.TOP_CENTER, ( widgetsManager.getWidgetsConfiguration().getGameResolution().getViewportWidth() - internalWidget.getEffectiveWidth() ) / 2, ( widgetsManager.getWidgetsConfiguration().getGameResolution().getViewportHeight() - internalWidget.getEffectiveHeight() ) / 2 );
            
            AbstractPropertiesKeeper.attachKeeper( internalWidget, true );
            __WCPrivilegedAccess.sortWidgets( widgetsManager.getWidgetsConfiguration() );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeWidgetsConfigurationCleared( WidgetsConfiguration widgetsConfig, LiveGameData GameData, boolean isEditorMode )
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
    public void afterWidgetsConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData GameData, boolean isEditorMode )
    {
        needsOnVehicleControlChangedEvent = true;
        texturesRequested = true;
        validateInputBindings();
        
        eventsDispatcher.fireAfterWidgetsConfigurationLoaded( renderListenersManager, gameData, widgetsConfig );
        
        widgetsManager.collectTextures( gameData, isEditorMode );
        //if ( usePlayer )
            widgetsManager.clearCompleteTexture();
        
        //System.gc();
        Runtime runtime = Runtime.getRuntime();
        RFDHLog.debug( "INFO: Free heap space memory: " + Tools.formatBytes( runtime.freeMemory() ) + " / " + Tools.formatBytes( runtime.totalMemory() ) + " / " + Tools.formatBytes( runtime.maxMemory() ) );
    }
    
    /**
     * This method must be called when the game started up.
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     */
    public void onStartup( Object userObject )
    {
        this.running = true;
        
        eventsDispatcher.fireOnStarted( this, gameData, userObject instanceof EditorPresets, renderListenersManager );
    }
    
    /**
     * This method must be called when the game shuts down.
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     */
    public void onShutdown( Object userObject)
    {
        this.running = false;
        
        eventsDispatcher.fireOnShutdown( this, gameData, userObject instanceof EditorPresets, renderListenersManager );
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
        try
        {
            gameData.getVehicleInfo().reload( gameData );
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
            __GDPrivilegedAccess.loadVehiclePhysics( gameData );
            
            physicsLoadedOnce = true;
            
            eventsDispatcher.fireOnVehiclePhysicsUpdated( gameData, isEditorMode );
        }
    }
    
    private boolean reloadSetup()
    {
        boolean result = false;
        
        if ( gameData.getGameDataObjectsFactory().loadVehicleSetupIfChanged( gameData ) )
        {
            result = true;
        }
        
        __GDPrivilegedAccess.setEngineBoostMapping( gameData.getSetup().getEngine().getBoostMapping(), gameData.getTelemetryData() );
        
        return ( result );
    }
    
    /**
     * 
     * @param force force reload ignoring, whether it is already in action?
     */
    protected abstract void reloadConfigImpl( boolean force );
    
    /**
     * 
     * @param force force reload ignoring, whether it is already in action?
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte reloadConfigAndSetupTexture( boolean force )
    {
        byte result = 1;
        
        try
        {
            if ( !__GDPrivilegedAccess.simulationMode && ( __EDPrivilegedAccess.editorClassLoader == null ) )
                reloadConfigImpl( force );
            
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
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     */
    protected void onSessionStartedImpl( Object userObject, long timestamp )
    {
        ThreeLetterCodeManager.updateThreeLetterCodes( gameData.getFileSystem().getConfigFolder(), gameData.getScoringInfo().getThreeLetterCodeGenerator() );
        __GDPrivilegedAccess.updateInfo( gameData );
        
        if ( gameData.getProfileInfo().isValid() )
        {
            boolean isEditorMode = ( userObject instanceof EditorPresets );
            
            boolean loadPhysicsAndSetup = gameData.getSetup().checkLoadPhysicsAndSetupOnSessionStarted( gameData, isEditorMode );
            
            reloadVehicleInfo();
            
            if ( loadPhysicsAndSetup )
            {
                reloadPhysics( true, isEditorMode );
                reloadSetup();
            }
            
            __GDPrivilegedAccess.onSessionStarted( gameData, timestamp, isEditorMode );
            
            // We cannot load the configuration here, because we don't know, which one to load (no scoring info).
        }
    }
    
    /**
     * This method must be called when a session has been started.
     * Note: LiveGameData must have been updated before.
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onSessionStarted( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onSessionStarted()" );
        
        long now = System.nanoTime();
        
        boolean isEditorMode = ( userObject instanceof EditorPresets );
        
        //if ( currentSessionIsRace == Boolean.TRUE )
        if ( sessionRunning && !isEditorMode && !__GDPrivilegedAccess.simulationMode )
        {
            RFDHLog.debug( "INFO: Got a call to StartSession() in already started session. Looks like an rFactor bug. Ignoring this call." );
            return ( ( rfDynHUD == null ) ? (byte)2 : ( rfDynHUD.isInRenderMode() ? (byte)2 : (byte)0 ) );
        }
        
        this.sessionRunning = true;
        this.isComingOutOfGarage = true;
        this.isInGarage = true;
        
        this.waitingForGraphics = !isEditorMode;
        this.waitingForTelemetry = !isEditorMode;
        this.waitingForScoring = !isEditorMode;
        this.waitingForWeather = !isEditorMode;
        this.waitingForGarageStartLocation = !isEditorMode;
        this.waitingForSetup = false;
        this.setupReloadTryTime = -1L;
        this.waitingForData = !isEditorMode;
        
        this.sessionJustStarted = true;
        this.currentSessionIsRace = null;
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        if ( gameData.isInCockpit() )
            this.cockpitLeftInRaceSession = false;
        
        byte result = 0;
        
        if ( texturesRequested )
        {
            result = 2;
            
            texturesRequested = false;
        }
        
        try
        {
            onSessionStartedImpl( userObject, now );
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
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     */
    protected void onSessionEndedImpl( Object userObject, long timestamp )
    {
        if ( gameData.getProfileInfo().isValid() )
        {
            __GDPrivilegedAccess.onSessionEnded( gameData, timestamp );
        }
    }
    
    /**
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     */
    public void onSessionEnded( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onSessionEnded()" );
        
        long now = System.nanoTime();
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( false );
        
        this.waitingForRender = false;
        this.waitingForGraphics = false;
        this.waitingForTelemetry = false;
        this.waitingForScoring = false;
        this.waitingForWeather = false;
        this.waitingForGarageStartLocation = false;
        this.waitingForSetup = false;
        this.setupReloadTryTime = -1L;
        this.waitingForData = false;
        
        //this.sessionStartTime = -1f;
        this.sessionRunning = false;
        this.currentSessionIsRace = null;
        
        try
        {
            onSessionEndedImpl( userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        if ( !( userObject instanceof EditorPresets ) && !__GDPrivilegedAccess.simulationMode )
            __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
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
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     */
    protected void onCockpitEnteredImpl( Object userObject, long timestamp )
    {
        ThreeLetterCodeManager.updateThreeLetterCodes( gameData.getFileSystem().getConfigFolder(), gameData.getScoringInfo().getThreeLetterCodeGenerator() );
        
        __GDPrivilegedAccess.updateInfo( gameData );
        
        if ( gameData.getProfileInfo().isValid() )
        {
            boolean isEditorMode = ( userObject instanceof EditorPresets );
            
            __GDPrivilegedAccess.setInCockpit( true, gameData, timestamp, isEditorMode );
            
            if ( !isEditorMode )
            {
                reloadPhysics( false, isEditorMode );
                
                if ( reloadSetup() )
                {
                    waitingForSetup = false;
                    
                    eventsDispatcher.fireOnVehicleSetupUpdated( gameData, isEditorMode );
                }
            }
            
            widgetsManager.onCockpitEntered( gameData );
            eventsDispatcher.fireOnCockpitEntered( gameData, isEditorMode );
        }
    }
    
    /**
     * This method must be called when the cockpit has been entered (the user clicked on "Drive").
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onCockpitEntered( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onCockpitEntered()" );
        byte result = 0;
        long now = System.nanoTime();
        
        boolean isEditorMode = ( userObject instanceof EditorPresets );
        
        if ( texturesRequested )
        {
            result = 2;
            
            texturesRequested = false;
        }
        
        this.isComingOutOfGarage = true;
        this.lastViewedVSIId = -1;
        this.lastControl = null;
        
        this.waitingForRender = waitingForRender || !isEditorMode;
        this.waitingForGraphics = waitingForGraphics || !isEditorMode;
        this.waitingForTelemetry = waitingForTelemetry || ( !isEditorMode && ( currentSessionIsRace != Boolean.FALSE ) );
        this.waitingForScoring = waitingForScoring || ( !isEditorMode && ( currentSessionIsRace != Boolean.FALSE ) );
        this.waitingForWeather = waitingForWeather || ( !isEditorMode && ( currentSessionIsRace != Boolean.FALSE ) );
        this.waitingForGarageStartLocation = true;
        this.waitingForSetup = false; //waitingForSetup || ( currentSessionIsRace != Boolean.FALSE ); //!isEditorMode;
        this.setupReloadTryTime = now + 5000000000L;
        this.waitingForData = !isEditorMode;
        this.needsOnVehicleControlChangedEvent = true;
        
        if ( !isEditorMode )
        {
            RFDHLog.printlnEx( "Entered cockpit." );
        }
        
        try
        {
            onCockpitEnteredImpl( userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //Logger.log( ">>> /onCockpitEntered(), result: " + result );
        return ( result );
    }
    
    /**
     * This method must be called when the user exited realtime mode (pressed ESCAPE in the cockpit).
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     */
    protected void onCockpitExitedImpl( Object userObject, long timestamp )
    {
        if ( gameData.getProfileInfo().isValid() )
        {
            __GDPrivilegedAccess.setInCockpit( false, gameData, timestamp, userObject instanceof EditorPresets );
            
            eventsDispatcher.fireOnCockpitExited( gameData, userObject instanceof EditorPresets );
        }
    }
    
    /**
     * This method must be called when the user exited the cockpit (pressed ESCAPE in the cockpit).
     * 
     * @param userObject custom user object from native side (could be an instance of {@link EditorPresets})
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onCockpitExited( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onCockpitExited()" );
        RFDHLog.printlnEx( "Exited cockpit." );
        
        long now = System.nanoTime();
        
        //realtimeStartTime = -1f;
        this.isComingOutOfGarage = true;
        
        this.isInPits = true;
        this.isInGarage = true;
        
        this.waitingForRender = true;
        //this.waitingForGraphics = true;
        this.waitingForData = true;
        
        this.needsOnVehicleControlChangedEvent = true;
        
        if ( gameData.getScoringInfo().getSessionType().isRace() )
            cockpitLeftInRaceSession = true;
        
        try
        {
            onCockpitExitedImpl( userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        //byte result = reloadConfigAndSetupTexture( false );
        __WCPrivilegedAccess.setValid( widgetsManager.getWidgetsConfiguration(), false );
        waitingForData = true;
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( false );
        
        texturesRequested = false;
        
        return ( 0 );
    }
    
    /**
     * Checks, whether re-entering the garage and showing the garage WidgetConfiguration is supported.
     * 
     * @return whether re-entering the garage and showing the garage WidgetConfiguration is supported.
     */
    protected boolean isReenteringGarageSupported()
    {
        return ( true );
    }
    
    /**
     * Checks, whether the player's vehicle is in the garage.
     * 
     * @return whether the player's vehicle is in the garage.
     */
    protected abstract boolean checkIsInGarage();
    
    /**
     * 
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param isEditorMode
     * 
     * @return the result back again.
     */
    protected byte onGarageExited( byte result, boolean isEditorMode )
    {
        eventsDispatcher.fireOnGarageExited( gameData, isEditorMode );
        
        if ( !isEditorMode )
        {
            result = reloadConfigAndSetupTexture( false );
            
            if ( result != 0 )
                eventsDispatcher.fireOnGarageExited( gameData, isEditorMode );
        }
        
        return ( result );
    }
    
    /**
     * 
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param isEditorMode
     * 
     * @return the result back again.
     */
    protected byte onGarageEntered( byte result, boolean isEditorMode )
    {
        eventsDispatcher.fireOnGarageEntered( gameData, isEditorMode );
        
        if ( !isEditorMode )
        {
            result = reloadConfigAndSetupTexture( false );
            
            if ( result != 0 )
                eventsDispatcher.fireOnGarageEntered( gameData, isEditorMode );
        }
        
        return ( result );
    }
    
    protected boolean checkIsInPits()
    {
        return ( gameData.getScoringInfo().getPlayersVehicleScoringInfo().isInPits() );
    }
    
    /**
     * 
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param isEditorMode
     * 
     * @return the result back again.
     */
    protected byte onPitsExited( byte result, boolean isEditorMode )
    {
        eventsDispatcher.fireOnPitsExited( gameData, isEditorMode );
        
        return ( result );
    }
    
    /**
     * 
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param isEditorMode
     * 
     * @return the result back again.
     */
    protected byte onPitsEntered( byte result, boolean isEditorMode )
    {
        eventsDispatcher.fireOnPitsEntered( gameData, isEditorMode );
        
        return ( result );
    }
    
    protected byte checkPosition( boolean isEditorMode )
    {
        byte result = 1;
        
        if ( isInGarage || isReenteringGarageSupported() )
        {
            boolean isInGarage = checkIsInGarage();
            
            if ( this.isInGarage && !isInGarage )
            {
                this.isInGarage = false;
                
                result = onGarageExited( result, isEditorMode );
            }
            else if ( !this.isInGarage && isInGarage )
            {
                this.isInGarage = true;
                
                onGarageEntered( result, isEditorMode );
            }
        }
        
        final boolean isInPits = checkIsInPits();
        
        if ( this.isInPits && !isInPits )
        {
            this.isInPits = isInPits;
            
            result = onPitsExited( result, isEditorMode );
        }
        else if ( !this.isInPits && isInPits )
        {
            this.isInPits = isInPits;
            
            result = onPitsEntered( result, isEditorMode );
        }
        
        return ( result );
    }
    
    protected void onVehicleSetupUpdated( boolean isEditorMode )
    {
        eventsDispatcher.fireOnVehicleSetupUpdated( gameData, isEditorMode );
    }
    
    /**
     * 
     * @param timestamp event timestamp in nano seconds
     * @param isEditorMode
     * @return <code>true</code>, if track has changed, <code>false</code> otherwise.
     */
    protected boolean checkTrackChanged( long timestamp, boolean isEditorMode )
    {
        String trackname = gameData.getTrackInfo().getTrackName();
        
        boolean result = !Tools.objectsEqual( trackname, lastTrackname );
        
        lastTrackname = trackname;
        
        return ( result );
    }
    
    /**
     * 
     * @param timestamp event timestamp in nano seconds
     * @param isEditorMode
     */
    protected void onTrackChanged( long timestamp, boolean isEditorMode )
    {
        eventsDispatcher.fireOnTrackChanged( gameData.getTrackInfo().getTrackName(), gameData, isEditorMode );
    }
    
    protected byte mergeResults( byte result1, byte result2 )
    {
        if ( ( result1 == 0 ) || ( result2 == 0 ) )
        {
            if ( ( result1 == 2 ) || ( result2 == 2 ) )
                texturesRequested = true;
            
            return ( 0 );
        }
        
        if ( ( result1 == 2 ) || ( result2 == 2 ) )
            return ( 2 );
        
        return ( 1 );
    }
    
    protected void onWaitingForDataCompleted( long timestamp, boolean isEditorMode )
    {
        if ( sessionJustStarted )
        {
            __GDPrivilegedAccess.onSessionStarted2( gameData, timestamp, isEditorMode );
            
            if ( !gameData.isInCockpit() || gameData.getScoringInfo().getSessionType().isRace() )
            {
                String modName = gameData.getModInfo().getName();
                String vehicleClass = gameData.getScoringInfo().getPlayersVehicleScoringInfo().getVehicleClass();
                String vehicleName = gameData.getVehicleInfo().getTeamNameCleaned();
                if ( ( vehicleName != null ) && ( vehicleName.trim().length() == 0 ) )
                    vehicleName = null;
                SessionType sessionType = gameData.getScoringInfo().getSessionType();
                String trackname = gameData.getTrackInfo().getTrackName();
                RFDHLog.printlnEx( "Session started. (Mod: \"" + modName + "\", Vehicle-Class: \"" + vehicleClass + "\", Car: \"" + ( vehicleName == null ? "N/A" : vehicleName ) + "\", Session: \"" + sessionType.name() + "\", Track: \"" + trackname + "\")" );
                
                if ( checkTrackChanged( timestamp, isEditorMode ) )
                {
                    onTrackChanged( timestamp, isEditorMode );
                }
                
                eventsDispatcher.fireOnSessionStarted( gameData.getScoringInfo().getSessionType(), gameData, isEditorMode );
                needsOnVehicleControlChangedEvent = true;
            }
            
            this.sessionJustStarted = false;
        }
    }
    
    public final boolean getWaitingForData( boolean isEditorMode )
    {
        if ( isEditorMode )
            return ( waitingForTelemetry || waitingForScoring || waitingForWeather );
        
        return ( waitingForData );
    }
    
    protected byte checkWaitingData( boolean isEditorMode, boolean forceReload )
    {
        long now = System.nanoTime();
        
        eventsDispatcher.checkAndFireOnNeededDataComplete( gameData, isEditorMode );
        
        boolean waitingForSetup2 = ( now <= setupReloadTryTime );
        if ( __GDPrivilegedAccess.simulationMode )
            waitingForSetup2 = false;
        
        if ( !waitingForScoring && gameData.getScoringInfo().getSessionType().isRace() && ( cockpitLeftInRaceSession == Boolean.FALSE ) )
        {
            waitingForSetup = false;
            waitingForSetup2 = false;
            gameData.getSetup().updatedInTimeScope = true;
        }
        
        if ( !waitingForScoring )
            cockpitLeftInRaceSession = null;
        
        if ( waitingForSetup || waitingForSetup2 )
        {
            if ( reloadSetup() )
            {
                waitingForSetup = false;
                waitingForSetup2 = false;
                setupReloadTryTime = -1L;
                
                onVehicleSetupUpdated( isEditorMode );
            }
            else if ( waitingForSetup && !waitingForSetup2 )
            {
                waitingForSetup = false;
                gameData.getSetup().updatedInTimeScope = true;
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
        
        if ( !waitingForRender && !waitingForGraphics && !waitingForTelemetry && !waitingForScoring && !waitingForWeather/* && !waitingForSetup && !waitingForSetup2*/ )
        {
            onWaitingForDataCompleted( now, isEditorMode );
            
            waitingForData = false;
            
            result = reloadConfigAndSetupTexture( forceReload );
        }
        else if ( gameData.isInCockpit() )
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
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte onDrivingAidsUpdatedImpl( byte result, Object userObject, long timestamp )
    {
        gameData.getDrivingAids().updateData( userObject, System.nanoTime() );
        
        return ( checkWaitingData( userObject instanceof EditorPresets, false ) );
    }
    
    /**
     * @param userObject a custom user object passed through to the sim specific implementation 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onDrivingAidsUpdated( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onDrivingAidsUpdated()" );
        
        byte result = 0;
        
        long now = System.nanoTime();
        
        try
        {
            result = onDrivingAidsUpdatedImpl( result, userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        //RFDHLog.println( ">>> /onDrivingAidsUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte onTelemetryDataUpdatedImpl( byte result, Object userObject, long timestamp )
    {
        gameData.getTelemetryData().updateData( userObject, System.nanoTime() );
        
        this.waitingForTelemetry = false;
        
        if ( gameData.getProfileInfo().isValid() )
        {
            result = checkWaitingData( userObject instanceof EditorPresets, false );
        }
        
        return ( result );
    }
    
    /**
     * This method must be called when TelemetryData has been updated.
     * 
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public byte onTelemetryDataUpdated( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onTelemetryDataUpdated()" );
        
        byte result = 0;
        
        long now = System.nanoTime();
        
        try
        {
            result = onTelemetryDataUpdatedImpl( result, userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //RFDHLog.println( ">>> /onTelemetryDataUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * 
     * @param updateTimestamp the timestamp at the update
     * 
     * @return <code>true</code>, if the race has been restarted, <code>false</code> otherwise.
     */
    protected abstract boolean checkRaceRestartImpl( long updateTimestamp );
    
    /**
     * 
     * @param updateTimestamp the timestamp at the update
     */
    public void checkRaceRestart( long updateTimestamp )
    {
        if ( checkRaceRestartImpl( updateTimestamp ) )
        {
            //RFDHLog.debug( "RACE RESTART" );
            onSessionStarted( false );
        }
    }
    
    /**
     * 
     * @param isEditorMode editor mode?
     */
    protected void checkAndFireOnLapStarted( boolean isEditorMode )
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
     * 
     * @param viewedVSI
     * @param timestamp event timestamp in nano seconds
     * @param isEditorMode
     */
    protected void onVehicleControlChanged( VehicleScoringInfo viewedVSI, long timestamp, boolean isEditorMode )
    {
        eventsDispatcher.fireOnVehicleControlChanged( viewedVSI, gameData, isEditorMode );
    }
    
    /**
     * 
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param numVehicles
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte onScoringInfoUpdatedImpl( byte result, int numVehicles, Object userObject, long timestamp )
    {
        gameData.getScoringInfo().updateData( numVehicles, userObject, timestamp );
        
        this.waitingForScoring = false;
        
        boolean isEditorMode = ( userObject instanceof EditorPresets );
        
        checkRaceRestart( timestamp );
        checkAndFireOnLapStarted( isEditorMode );
        
        this.currentSessionIsRace = gameData.getScoringInfo().getSessionType().isRace();
        
        if ( waitingForGarageStartLocation )
        {
            this.isInPits = checkIsInPits();
            this.isInGarage = isInPits;
            
            this.waitingForGarageStartLocation = false;
        }
        
        if ( gameData.getProfileInfo().isValid() )
        {
            result = checkWaitingData( isEditorMode, false );
            
            eventsDispatcher.fireOnScoringInfoUpdated( gameData, isEditorMode );
            
            if ( !waitingForData && ( result != 0 ) )
            {
                byte result2 = checkPosition( isEditorMode );
                
                result = mergeResults( result, result2 );
                
                VehicleScoringInfo viewedVSI = gameData.getScoringInfo().getViewedVehicleScoringInfo();
                
                if ( ( lastViewedVSIId == -1 ) || ( viewedVSI.getDriverId() != lastViewedVSIId ) || ( viewedVSI.getVehicleControl() != lastControl ) )
                {
                    lastViewedVSIId = viewedVSI.getDriverId();
                    lastControl = viewedVSI.getVehicleControl();
                    onVehicleControlChanged( viewedVSI, timestamp, isEditorMode );
                    
                    result2 = reloadConfigAndSetupTexture( false );
                    
                    if ( result2 == 2 )
                    {
                        onVehicleControlChanged( viewedVSI, timestamp, isEditorMode );
                        needsOnVehicleControlChangedEvent = false;
                    }
                    
                    result = mergeResults( result, result2 );
                }
                else if ( needsOnVehicleControlChangedEvent )
                {
                    onVehicleControlChanged( viewedVSI, timestamp, isEditorMode );
                    needsOnVehicleControlChangedEvent = false;
                }
            }
        }
        
        return ( result );
    }
    
    /**
     * This method must be called when ScoringInfo has been updated.
     * 
     * @param numVehicles
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public byte onScoringInfoUpdated( int numVehicles, Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onScoringInfoUpdated()" );
        
        final long now = System.nanoTime();
        
        byte result = 0;
        
        try
        {
            result = onScoringInfoUpdatedImpl( result, numVehicles, userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        //RFDHLog.println( ">>> /onScoringInfoUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte onWeatherInfoUpdatedImpl( byte result, Object userObject, long timestamp )
    {
        gameData.getWeatherInfo().updateData( userObject, System.nanoTime() );
        
        this.waitingForWeather = false;
        
        return ( checkWaitingData( userObject instanceof EditorPresets, false ) );
    }
    
    /**
     * @param userObject a custom user object passed through to the sim specific implementation 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onWeatherInfoUpdated( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onWeatherInfoUpdated()" );
        
        byte result = 0;
        
        long now = System.nanoTime();
        
        try
        {
            result = onWeatherInfoUpdatedImpl( result, userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        //RFDHLog.println( ">>> /onWeatherInfoUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte onCommentaryRequestInfoUpdatedImpl( byte result, Object userObject, long timestamp )
    {
        //if ( !isEditorMode )
            gameData.getCommentaryRequestInfo().updateData( userObject, System.nanoTime() );
        
        return ( checkWaitingData( userObject instanceof EditorPresets, false ) );
    }
    
    /**
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onCommentaryRequestInfoUpdated( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onCommentaryRequestInfoUpdated()" );
        
        byte result = 0;
        
        long now = System.nanoTime();
        
        try
        {
            result = onCommentaryRequestInfoUpdatedImpl( result, userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        //RFDHLog.println( ">>> /onCommentaryRequestInfoUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     * @param timestamp event timestamp in nano seconds
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte onGraphicsInfoUpdatedImpl( byte result, Object userObject, long timestamp )
    {
        gameData.getGraphicsInfo().updateData( userObject, timestamp );
        
        return ( result );
    }
    
    /**
     * @param userObject a custom user object passed through to the sim specific implementation (could be an instance of {@link EditorPresets})
     *  
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    public final byte onGraphicsInfoUpdated( Object userObject )
    {
        RFDHLog.profile( "[PROFILE]: onGraphicsInfoUpdated()" );
        this.waitingForGraphics = false;
        
        byte result = 1;
        
        long now = System.nanoTime();
        
        try
        {
            result = onGraphicsInfoUpdatedImpl( result, userObject, now );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        result = mergeResults( result, checkWaitingData( userObject instanceof EditorPresets, false ) );
        
        //RFDHLog.println( ">>> /onGraphicsInfoUpdated(), result: " + result );
        return ( result );
    }
    
    /**
     * 
     * @param viewportX the left coordinate of the viewport
     * @param viewportY the top coordinate of the viewport
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * 
     * @return <code>true</code>, if the viewport has changed, <code>false</code> otherwise.
     */
    protected boolean checkAndApplyChangedViewport( short viewportX, short viewportY, short viewportWidth, short viewportHeight )
    {
        boolean vpChanged = __WCPrivilegedAccess.setViewport( viewportX, viewportY, viewportWidth, viewportHeight, widgetsManager.getWidgetsConfiguration() );
        
        if ( ( viewportWidth > gameData.getGameResolution().getResX() ) || ( viewportHeight > gameData.getGameResolution().getResY() ) )
        {
            widgetsManager.resizeMainTexture( viewportWidth, viewportHeight );
        }
        
        return ( vpChanged );
    }
    
    protected void onViewportChanged( short viewportX, short viewportY, short viewportWidth, short viewportHeight )
    {
        gameData.getGraphicsInfo().onViewportChanged( viewportX, viewportY, viewportWidth, viewportHeight );
    }
    
    /**
     * 
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param viewportX the left coordinate of the viewport
     * @param viewportY the top coordinate of the viewport
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * @param viewportChanged
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte handleViewport( byte result, short viewportX, short viewportY, short viewportWidth, short viewportHeight, boolean viewportChanged )
    {
        if ( viewportChanged )
        {
            RFDHLog.debug( "[DEBUG]: (Viewport changed): ", viewportX, ", ", viewportY, "; ", viewportWidth, "x", viewportHeight );
            
            if ( gameData.getProfileInfo().isValid() )
            {
                //result = reloadConfigAndSetupTexture( true );
                waitingForData = true;
                result = checkWaitingData( false, true );
            }
            
            onViewportChanged( viewportX, viewportY, viewportWidth, viewportHeight );
        }
        else
        {
            result = checkWaitingData( false, false );
        }
        
        return ( result );
    }
    
    /**
     * Will and must be called any time, the game is redendered (called from the C++-Plugin).
     * 
     * @param result 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested
     * @param viewportX the left coordinate of the viewport
     * @param viewportY the top coordinate of the viewport
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * 
     * @return 0 for no HUD to be drawn, 1 for HUD drawn, 2 for HUD drawn and texture re-requested.
     */
    protected byte beforeRenderImpl( byte result, short viewportX, short viewportY, short viewportWidth, short viewportHeight )
    {
        if ( !__GDPrivilegedAccess.simulationMode )
        {
            boolean vpChanged = checkAndApplyChangedViewport( viewportX, viewportY, viewportWidth, viewportHeight );
            
            if ( isSessionRunning() && gameData.getProfileInfo().isValid() )
            {
                handleViewport( result, viewportX, viewportY, viewportWidth, viewportHeight, vpChanged );
            }
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
    public final byte beforeRender( short viewportX, short viewportY, short viewportWidth, short viewportHeight )
    {
        RFDHLog.profile( "[PROFILE]: beforeRender()" );
        this.waitingForRender = false;
        
        byte result = 1;
        
        try
        {
            result = beforeRenderImpl( result, viewportX, viewportY, viewportWidth, viewportHeight );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        if ( rfDynHUD != null )
            rfDynHUD.setRenderMode( result != 0 );
        
        result = mergeResults( result, checkWaitingData( false, false ) );
        
        //RFDHLog.println( ">>> /beforeRender(), result: " + result );
        return ( result );
    }
    
    /**
     * Creates a new {@link GameEventsManager}.
     * 
     * @param gameId a String, identifying the used simulation
     * @param rfDynHUD the main {@link RFDynHUD} instance
     * @param drawingManager the widgets drawing manager
     * @param gdFactory
     * @param configurationCandidatesIterator
     */
    public GameEventsManager( String gameId, RFDynHUD rfDynHUD, WidgetsDrawingManager drawingManager, _LiveGameDataObjectsFactory gdFactory, Iterator<File> configurationCandidatesIterator )
    {
        this.rfDynHUD = rfDynHUD;
        this.widgetsManager = drawingManager;
        
        this.gameData = gdFactory.newLiveGameData( this );
        
        this.eventsDispatcher = GameEventsDispatcher.createGameEventsDispatcher( gameData.getFileSystem() );
        eventsDispatcher.setWidgetsConfiguration( widgetsManager.getWidgetsConfiguration() );
        this.renderListenersManager = widgetsManager.getRenderListenersManager();
        __RenderPrivilegedAccess.setConfigurationAndLoader( widgetsManager.getWidgetsConfiguration(), loader, renderListenersManager );
    }
}
