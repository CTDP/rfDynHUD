package net.ctdp.rfdynhud.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JOptionPane;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationClearListener;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.openmali.vecmath2.util.ColorUtils;

/**
 * This utility class servs to load HUD configuration files.
 * 
 * @author Marvin Froehlich
 */
public class ConfigurationLoader
{
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param reader
     * @param widgetsConfig
     * 
     * @throws IOException
     */
    private static void __loadConfiguration( Reader reader, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, final EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        widgetsConfig.clear( gameData, editorPresets, clearListener );
        
        new AbstractIniParser()
        {
            private Widget currentWidget = null;
            private String widgetName = null;
            private boolean badWidget = false;
            
            private String errorMessages = null;
            
            @Override
            protected boolean onGroupParsed( int lineNr, String group )
            {
                if ( currentWidget != null )
                {
                    widgetsConfig.addWidget( currentWidget );
                    currentWidget = null;
                    widgetName = null;
                    badWidget = false;
                }
                
                if ( ( group != null ) && group.startsWith( "Widget::" ) )
                {
                    widgetName = group.substring( 8 );
                    badWidget = false;
                }
                
                return ( true );
            }
            
            @Override
            protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
            {
                if ( group == null )
                    //throw new ParsingException( "Found setting before the first group started (line " + lineNr + ")." );
                    Logger.log( "WARNING: Found setting before the first group started (line " + lineNr + ")." );
                
                if ( group.equals( "Global" ) )
                {
                    widgetsConfig.loadProperty( key, value );
                }
                else if ( group.equals( "NamedColors" ) )
                {
                    java.awt.Color color = ColorUtils.hexToColor( value, false );
                    
                    if ( color == null )
                    {
                        String msg = "ERROR: Illegal color value on line #" + lineNr + ": " + value;
                        Logger.log( msg );
                        
                        if ( errorMessages == null )
                            errorMessages = msg;
                        else
                            errorMessages += "\n" + msg;
                    }
                    else
                    {
                        widgetsConfig.addNamedColor( key, color );
                    }
                }
                else if ( group.equals( "NamedFonts" ) )
                {
                    java.awt.Font font = FontUtils.parseFont( value, widgetsConfig.getGameResolution().getResY(), false, false );
                    
                    if ( ( font == FontUtils.FALLBACK_FONT ) || ( font == FontUtils.FALLBACK_VIRTUAL_FONT ) )
                    {
                        String msg = "ERROR: Illegal font value on line #" + lineNr + ": " + value;
                        Logger.log( msg );
                        
                        if ( errorMessages == null )
                            errorMessages = msg;
                        else
                            errorMessages += "\n" + msg;
                    }
                    else
                    {
                        widgetsConfig.addNamedFont( key, value );
                    }
                }
                else if ( group.equals( "BorderAliases" ) )
                {
                    widgetsConfig.addBorderAlias( key, value );
                }
                else if ( group.startsWith( "Widget::" ) )
                {
                    if ( ( widgetName != null ) && ( currentWidget == null ) && !key.equals( "class" ) )
                    {
                        if ( !badWidget )
                            //throw new ParsingException( "Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                            Logger.log( "WARNING: Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                        
                        badWidget = true;
                    }
                    else if ( key.equals( "class" ) )
                    {
                        try
                        {
                            Class<?> clazz = Class.forName( value, false, getClass().getClassLoader() );
                            
                            //currentWidget = (Widget)clazz.getConstructor( RelativePositioning.class, int.class, int.class, int.class, int.class ).newInstance( RelativePositioning.TOP_LEFT, 0, 0, 100, 100 );
                            currentWidget = (Widget)clazz.getConstructor( String.class ).newInstance( widgetName );
                            widgetName = null;
                        }
                        catch ( ClassNotFoundException e )
                        {
                            Logger.log( "WARNING: Widget class not found (" + value + ")" );
                            badWidget = true;
                        }
                        catch ( Throwable t )
                        {
                            Logger.log( "Error creating Widget instance of class " + value + ":" );
                            Logger.log( t );
                            badWidget = true;
                        }
                    }
                    else if ( ( currentWidget == null ) || ( widgetName != null ) )
                    {
                        if ( !badWidget )
                        {
                            if ( currentWidget != null )
                            {
                                //throw new ParsingException( "Cannot load the Widget \"" + currentWidget.getName() + "\" (line " + lineNr + ")." );
                                Logger.log( "WARNING: Cannot load the Widget \"" + currentWidget.getName() + "\" (line " + lineNr + ")." );
                                badWidget = true;
                            }
                            else if ( widgetName != null )
                            {
                                //throw new ParsingException( "Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                                Logger.log( "WARNING: Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                                badWidget = true;
                            }
                            else
                            {
                                //throw new ParsingException( "Cannot load the a Widget (line " + lineNr + ")." );
                                Logger.log( "WARNING: Cannot load the a Widget (line " + lineNr + ")." );
                                badWidget = true;
                            }
                        }
                    }
                    else
                    {
                        try
                        {
                            currentWidget.loadProperty( key, value );
                        }
                        catch ( Throwable t )
                        {
                            //throw new Error( t );
                            Logger.log( t );
                        }
                    }
                }
                
                return ( true );
            }
            
            @Override
            protected void onParsingFinished()
            {
                if ( currentWidget != null )
                {
                    widgetsConfig.addWidget( currentWidget );
                    currentWidget = null;
                }
                
                if ( ( errorMessages != null ) && ( editorPresets != null ) )
                {
                    JOptionPane.showMessageDialog( null, errorMessages, "Error loading config ini", JOptionPane.ERROR_MESSAGE );
                }
            }
        }.parse( reader );
        
        __WCPrivilegedAccess.sortWidgets( widgetsConfig );
        __WCPrivilegedAccess.setJustLoaded( widgetsConfig, gameData, editorPresets );
    }
    
    private static File currentlyLoadedConfigFile = null;
    private static long lastModified = -1L;
    private static boolean isFirstLoadAttempt = true;
    
    public static final File getCurrentlyLoadedConfigFile()
    {
        return ( currentlyLoadedConfigFile );
    }
    
    private static File _loadConfiguration( File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        Logger.log( "Loading configuration file from \"" + file.getAbsolutePath() + "\"" );
        
        __loadConfiguration( new FileReader( file ), widgetsConfig, gameData, editorPresets, clearListener );
        
        __WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        currentlyLoadedConfigFile = file.getAbsoluteFile();
        lastModified = currentlyLoadedConfigFile.lastModified();
        
        return ( currentlyLoadedConfigFile );
    }
    
    private static File load( File currentlyLoadedConfigFile, long lastModified, File configFile, WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        if ( currentlyLoadedConfigFile == null )
            return ( _loadConfiguration( configFile, widgetsConfig, gameData, editorPresets, clearListener ) );
        
        if ( !configFile.equals( currentlyLoadedConfigFile ) || ( configFile.lastModified() > lastModified ) )
            return ( _loadConfiguration( configFile, widgetsConfig, gameData, editorPresets, clearListener ) );
        
        //__WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        return ( currentlyLoadedConfigFile );
    }
    
    private static boolean load( File configFile, WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        File old_currentlyLoadedConfigFile = currentlyLoadedConfigFile;
        long old_lastModified = lastModified;
        
        load( currentlyLoadedConfigFile, lastModified, configFile, widgetsConfig, gameData, editorPresets, clearListener );
        
        if ( !currentlyLoadedConfigFile.equals( old_currentlyLoadedConfigFile ) || ( lastModified > old_lastModified ) )
        {
            //__WCPrivilegedAccess.setJustLoaded( widgetsConfig, gameData );
            
            return ( true );
        }
        
        return ( false );
    }
    
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param file
     * @param widgetsConfig
     * 
     * @return the file, from which the configuration has been loaded.
     * 
     * @throws IOException
     */
    public static File loadConfiguration( File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        if ( load( file, widgetsConfig, gameData, editorPresets, clearListener ) )
            return ( currentlyLoadedConfigFile );
        
        return ( null );
    }
    
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param filename
     * @param widgetsConfig
     * 
     * @return the file, from which the configuration has been loaded.
     * 
     * @throws IOException
     */
    public static File loadConfiguration( String filename, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        return ( loadConfiguration( new File( filename ), widgetsConfig, gameData, editorPresets, clearListener ) );
    }
    
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param file
     * @param widgetsConfig
     * 
     * @return the file, from which the configuration has been loaded.
     * 
     * @throws IOException
     */
    public static File forceLoadConfiguration( File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        currentlyLoadedConfigFile = null;
        lastModified = -1L;
        
        if ( load( file, widgetsConfig, gameData, editorPresets, clearListener ) )
            return ( currentlyLoadedConfigFile );
        
        return ( null );
    }
    
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param filename
     * @param widgetsConfig
     * 
     * @return the file, from which the configuration has been loaded.
     * 
     * @throws IOException
     */
    public static File forceLoadConfiguration( String filename, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        return ( forceLoadConfiguration( new File( filename ), widgetsConfig, gameData, editorPresets, clearListener ) );
    }
    
    public static void loadFactoryDefaults( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener ) throws IOException
    {
        Logger.log( "Loading factory default configuration." );
        
        __loadConfiguration( new InputStreamReader( ConfigurationLoader.class.getResourceAsStream( "/data/config/overlay.ini" ) ), widgetsConfig, gameData, editorPresets, clearListener );
        
        __WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        currentlyLoadedConfigFile = null;
        lastModified = -1L;
    }
    
    /**
     * Loads a configuration and searches for the file in the following order:
     * <ul>
     *   <li>CONFIGURATION_FOLDER/MOD_FOLDER/overlay_VEHICLE_CLASS.ini</li>
     *   <li>CONFIGURATION_FOLDER/MOD_FOLDER/overlay.ini</li>
     *   <li>CONFIGURATION_FOLDER/config.ini</li>
     * </ul>
     * Then it checks, if that file is newer than the already loaded one.
     * 
     * @param smallMonitor
     * @param bigMonitor
     * @param isInGarage
     * @param currentlyLoadedConfigFile
     * @param modName
     * @param vehicleClass
     * @param sessionType
     * @param widgetsConfig
     * @param gameData
     * @param editorPresets
     * @param clearListener
     * @param force
     * 
     * @return the file, from which the configuration has been loaded.
     */
    public static Boolean reloadConfiguration( boolean smallMonitor, boolean bigMonitor, boolean isInGarage, String modName, String vehicleClass, SessionType sessionType, WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets, ConfigurationClearListener clearListener, boolean force )
    {
        if ( force )
        {
            currentlyLoadedConfigFile = null;
            lastModified = -1L;
        }
        
        File old_currentlyLoadedConfigFile = currentlyLoadedConfigFile;
        
        File f = null;
        
        try
        {
            ConfigurationCandidatesIterator it = new ConfigurationCandidatesIterator( smallMonitor, bigMonitor, isInGarage, modName, vehicleClass, sessionType );
            
            while ( it.hasNext() )
            {
                f = it.next();
                
                if ( f.exists() )
                {
                    return ( load( f, widgetsConfig, gameData, editorPresets, clearListener ) );
                }
            }
            
            if ( smallMonitor || bigMonitor )
            {
                __WCPrivilegedAccess.setValid( widgetsConfig, false );
                
                return ( null );
            }
            
            if ( ( currentlyLoadedConfigFile != null ) || isFirstLoadAttempt )
            {
                loadFactoryDefaults( widgetsConfig, gameData, editorPresets, clearListener );
                TextureDirtyRectsManager.forceCompleteRedraw();
            }
            
            isFirstLoadAttempt = false;
            
            return ( old_currentlyLoadedConfigFile != null );
        }
        catch ( Throwable t )
        {
            Logger.log( "Error loading overlay config file " + ( f != null ? f.getAbsolutePath() : "" ) + "." );
            Logger.log( t );
            
            __WCPrivilegedAccess.setValid( widgetsConfig, false );
            
            currentlyLoadedConfigFile = null;
            lastModified = -1L;
            
            return ( old_currentlyLoadedConfigFile != null );
        }
    }
}
