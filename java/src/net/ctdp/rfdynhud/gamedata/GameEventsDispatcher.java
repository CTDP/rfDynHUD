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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarFile;

import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputMapping;
import net.ctdp.rfdynhud.input.InputMappingsManager;
import net.ctdp.rfdynhud.input.KnownInputActions;
import net.ctdp.rfdynhud.render.WidgetsRenderListenersManager;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.__WPrivilegedAccess;

import org.jagatoo.util.classes.ClassSearcher;
import org.jagatoo.util.classes.SuperClassCriterium;

/**
 * Dispatches game and game data events.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class GameEventsDispatcher
{
    private WidgetsConfiguration widgetsConfig = null;
    
    private final GameEventsPlugin[] plugins;
    
    /**
     * 
     * @param widgetsConfig
     */
    public void setWidgetsConfiguration( WidgetsConfiguration widgetsConfig )
    {
        this.widgetsConfig = widgetsConfig;
    }
    
    /**
     * 
     * @param isEditorMode
     * @param gameData
     * @param renderListenerManager
     */
    public void fireOnStarted( LiveGameData gameData, boolean isEditorMode, WidgetsRenderListenersManager renderListenerManager )
    {
        if ( plugins != null )
        {
            for ( int i = 0; i < plugins.length; i++ )
                plugins[i].onPluginStarted( gameData, isEditorMode, renderListenerManager );
        }
    }
    
    /**
     * 
     * @param manager
     * @param gameData
     * @param widgetsConfig
     */
    public void fireBeforeWidgetsConfigurationCleared( WidgetsRenderListenersManager manager, LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
        __RenderPrivilegedAccess.fireBeforeWidgetsConfigurationCleared( manager, gameData, widgetsConfig );
    }
    
    /**
     * 
     * @param manager
     * @param gameData
     * @param widgetsConfig
     */
    public void fireAfterWidgetsConfigurationLoaded( WidgetsRenderListenersManager manager, LiveGameData gameData, WidgetsConfiguration widgetsConfig )
    {
        __RenderPrivilegedAccess.fireAfterWidgetsConfigurationLoaded( manager, gameData, widgetsConfig );
    }
    
    /**
     * This method is executed when a new track was loaded.<br>
     * <br>
     * Calls {@link Widget#onTrackChanged(String, LiveGameData, boolean)} on each Widget.
     * 
     * @param trackname the track's name
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnTrackChanged( String trackname, LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onTrackChanged( trackname, gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onTrackChanged( trackname, gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    private final ArrayList<Widget> waitingWidgets = new ArrayList<Widget>();
    
    public final boolean hasWaitingWidgets()
    {
        return ( !waitingWidgets.isEmpty() );
    }
    
    /**
     * This method is called when a new session was started.<br>
     * <br>
     * Calls {@link Widget#onSessionStarted(SessionType, LiveGameData, boolean)} on each Widget.
     * 
     * @param sessionType the current session type
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
    {
        waitingWidgets.clear();
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            try
            {
                widget.onSessionStarted( sessionType, gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
            
            waitingWidgets.add( widget );
        }
    }
    
    /**
     * This method is called when a the user entered realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeEntered(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        waitingWidgets.clear();
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            try
            {
                widget.onRealtimeEntered( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
            
            waitingWidgets.add( widget );
        }
    }
    
    /**
     * This method is called when a the user entered realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeEntered(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void checkAndFireOnNeededDataComplete( LiveGameData gameData, boolean isEditorMode )
    {
        if ( waitingWidgets.size() == 0 )
            return;
        
        for ( int i = waitingWidgets.size() - 1; i >= 0; i-- )
        {
            Widget widget = waitingWidgets.get( i );
            int neededData = ( widget.getNeededData() & Widget.NEEDED_DATA_ALL );
            
            if ( ( ( neededData & Widget.NEEDED_DATA_TELEMETRY ) != 0 ) && gameData.getTelemetryData().isUpdatedInTimeScope() )
                neededData &= ~Widget.NEEDED_DATA_TELEMETRY;
            
            if ( ( ( neededData & Widget.NEEDED_DATA_SCORING ) != 0 ) && !gameData.getScoringInfo().isUpdatedInTimeScope() )
                neededData &= ~Widget.NEEDED_DATA_SCORING;
            
            //if ( ( ( neededData & Widget.NEEDED_DATA_SETUP ) != 0 ) && !gameData.getSetup().isUpdatedInTimeScope() )
            //    neededData &= ~Widget.NEEDED_DATA_SETUP;
            
            if ( neededData == 0 )
            {
                try
                {
                    widget.onNeededDataComplete( gameData, isEditorMode );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
                
                waitingWidgets.remove( i );
                
                widget.forceReinitialization();
                widget.forceCompleteRedraw( true );
            }
        }
    }
    
    /**
     * This method is called when a the user exited the garage.<br>
     * <br>
     * Calls {@link Widget#onPitsEntered(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnPitsEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onPitsEntered( gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onPitsEntered( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user entered the garage.<br>
     * <br>
     * Calls {@link Widget#onGarageEntered(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnGarageEntered( LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onGarageEntered( gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onGarageEntered( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user exited the garage.<br>
     * <br>
     * Calls {@link Widget#onGarageExited(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnGarageExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onGarageExited( gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onGarageExited( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user exited the garage.<br>
     * <br>
     * Calls {@link Widget#onPitsExited(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onPitsExited( gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onPitsExited( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user exited realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeExited(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnRealtimeExited( LiveGameData gameData, boolean isEditorMode )
    {
        waitingWidgets.clear();
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onRealtimeExited( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when {@link ScoringInfo} have been updated (done at 2Hz).
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onScoringInfoUpdated( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when {@link VehiclePhysics} have been updated.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnVehiclePhysicsUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onVehiclePhysicsUpdated( gameData );
        }
    }
    
    /**
     * This method is called when {@link VehicleSetup} has been updated.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnVehicleSetupUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onVehicleSetupUpdated( gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            try
            {
                widget.forceAndSetDirty( true );
                widget.onVehicleSetupUpdated( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when either the player's vehicle control has changed or another vehicle is being viewed.
     * 
     * @param viewedVSI the viewed vehicle
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                __WPrivilegedAccess.onVehicleControlChanged( widgetsConfig.getWidget( i ), viewedVSI, gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a lap has been finished and a new one was started.
     * 
     * @param vsi the vehicle
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        if ( gameData.gameEventsListeners != null )
        {
            for ( int i = 0; i < gameData.gameEventsListeners.length; i++ )
                gameData.gameEventsListeners[i].onLapStarted( vsi, gameData, isEditorMode );
        }
        
        final int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                widgetsConfig.getWidget( i ).onLapStarted( vsi, gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is fired by the {@link InputMappingsManager},
     * if the state of a bound input component has changed.
     * 
     * @param mapping the input mapping
     * @param state the current state of the input device component
     * @param modifierMask the current key modifier mask
     * @param when the timestamp of the input action in nano seconds
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnInputStateChanged( InputMapping mapping, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
    {
        Widget widget = widgetsConfig.getWidget( mapping.getWidgetName() );
        
        if ( widget == null )
            return;
        
        InputAction action = mapping.getAction();
        
        try
        {
            if ( action == KnownInputActions.ToggleWidgetVisibility )
                __WPrivilegedAccess.toggleInputVisible( widget );
            else
                __WPrivilegedAccess.onBoundInputStateChanged( widget, action, state, modifierMask, when, gameData, isEditorMode );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    /**
     * 
     * @param isEditorMode
     * @param gameData
     * @param renderListenerManager
     */
    public void fireOnShutdown( LiveGameData gameData, boolean isEditorMode, WidgetsRenderListenersManager renderListenerManager )
    {
        if ( plugins != null )
        {
            for ( int i = 0; i < plugins.length; i++ )
                plugins[i].onPluginShutdown( gameData, isEditorMode, renderListenerManager );
        }
    }
    
    private static void findPluginJars( File folder, List<URL> jars )
    {
        for ( File f : folder.listFiles() )
        {
            if ( f.isDirectory() )
            {
                if ( !f.getName().equals( ".svn" ) )
                    findPluginJars( f, jars );
            }
            else if ( f.getName().toLowerCase().endsWith( ".jar" ) )
            {
                try
                {
                    jars.add( f.toURI().toURL() );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
    }
    
    private static URL[] findPluginJars( File pluginsFolder )
    {
        ArrayList<URL> jars = new ArrayList<URL>();
        
        for ( File f : pluginsFolder.listFiles() )
        {
            if ( f.isDirectory() && !f.getName().equals( ".svn" ) )
            {
                findPluginJars( f, jars );
            }
        }
        
        URL[] urls = new URL[ jars.size() ];
        
        return ( jars.toArray( urls ) );
    }
    
    private static final GameEventsPlugin[] findPlugins()
    {
        Logger.log( "Loading GameEventsPlugins..." );
        
        File pluginsFolder = GameFileSystem.INSTANCE.getSubPluginsFolder();
        
        HashMap<Class<?>, JarFile> jarMap = new HashMap<Class<?>, JarFile>();
        List<Class<?>> pluginClasses;
        if ( ( pluginsFolder != null ) && pluginsFolder.exists() )
        {
            URLClassLoader classLoader = new URLClassLoader( findPluginJars( pluginsFolder ), GameEventsPlugin.class.getClassLoader() );
            
            try
            {
                pluginClasses = ClassSearcher.findClasses( true, classLoader, jarMap, new SuperClassCriterium( GameEventsPlugin.class, false ) );
            }
            catch ( IOException e )
            {
                Logger.log( e );
                pluginClasses = null;
            }
        }
        else
        {
            pluginClasses = ClassSearcher.findClasses( jarMap, new SuperClassCriterium( GameEventsPlugin.class, false ) );
        }
        
        if ( ( pluginClasses == null ) || ( pluginClasses.size() == 0 ) )
        {
            Logger.log( "No plugin found." );
            
            return ( null );
        }
        
        GameEventsPlugin[] plugins = new GameEventsPlugin[ pluginClasses.size() ];
        
        for ( int i = 0; i < pluginClasses.size(); i++ )
        {
            Class<?> pluginClazz = pluginClasses.get( i );
            JarFile jar = jarMap.get( pluginClazz );
            File baseFolder = ( jar == null ) ? null : new File( jar.getName() ).getParentFile();
            
            try
            {
                plugins[i] = (GameEventsPlugin)pluginClazz.getConstructor( File.class ).newInstance( baseFolder );
                
                Logger.log( "    Found plugin \"" + pluginClazz.getName() + "\"." );
            }
            catch ( Throwable t )
            {
                Logger.log( "WARNING: Couldn't instantiate GameEventsPlugin \"" + pluginClazz.getName() + "\"." );
                Logger.log( t );
            }
        }
        
        Logger.log( "Found and initialized " + plugins.length + " plugin" + ( plugins.length == 1 ? "" : "s" ) + "." );
        
        return ( plugins );
    }
    
    public GameEventsDispatcher()
    {
        this.plugins = findPlugins();
    }
}
