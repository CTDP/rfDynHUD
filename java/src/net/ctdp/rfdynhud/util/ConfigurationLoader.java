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
package net.ctdp.rfdynhud.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.swing.JOptionPane;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationLoadListener;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.AssembledWidget;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.versioning.Version;
import org.openmali.vecmath2.util.ColorUtils;

/**
 * This utility class servs to load HUD configuration files.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ConfigurationLoader implements PropertyLoader
{
    private String keyPrefix = null;
    
    private String currentKey = null;
    private String currentValue = null;
    
    private String effectiveKey = null;
    
    private Version sourceVersion = null;
    
    public void setKeyPrefix( String prefix )
    {
        this.keyPrefix = prefix;
        
        if ( keyPrefix == null )
            effectiveKey = currentKey;
        else
            effectiveKey = currentKey.substring( keyPrefix.length() );
    }
    
    public final String getKeyPrefix()
    {
        return ( keyPrefix );
    }
    
    @Override
    public final String getCurrentKey()
    {
        return ( effectiveKey );
    }
    
    @Override
    public final String getCurrentValue()
    {
        return ( currentValue );
    }
    
    @Override
    public final Version getSourceVersion()
    {
        return ( sourceVersion );
    }
    
    @Override
    public boolean loadProperty( Property property )
    {
        if ( property.isMatchingKey( effectiveKey ) )
        {
            property.loadValue( currentValue );
            
            return ( true );
        }
        
        return ( false );
    }
    
    private static enum GroupType
    {
        Meta,
        Global,
        NamedColors,
        NamedFonts,
        BorderAliases,
        Widget,
        ;
        
        public static GroupType parseGroupType( String groupName )
        {
            GroupType groupType = null;
            
            if ( groupName.equals( "Meta" ) )
                groupType = GroupType.Meta;
            else if ( groupName.equals( "Global" ) )
                groupType = GroupType.Global;
            else if ( groupName.equals( "NamedColors" ) )
                groupType = GroupType.NamedColors;
            else if ( groupName.equals( "NamedFonts" ) )
                groupType = GroupType.NamedFonts;
            else if ( groupName.equals( "BorderAliases" ) )
                groupType = GroupType.BorderAliases;
            else if ( groupName.startsWith( "Widget::" ) )
                groupType = GroupType.Widget;
            
            return ( groupType );
        }
    }
    
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param file the file to parse
     * @param quietMode suppress warnings to the log?
     * 
     * @return the design resolution as an int array or <code>null</code>.
     * 
     * @throws IOException if anything went wrong.
     */
    public static int[] readDesignResolutionFromConfiguration( File file, final boolean quietMode ) throws IOException
    {
        final int[] result = { -1, -1 };
        
        new AbstractIniParser()
        {
            private GroupType currentGroupType = null;
            
            private boolean headerFound = false;
            
            private boolean settingBeforeGroupWarningThrown = false;
            
            @Override
            protected boolean onGroupParsed( int lineNr, String group )
            {
                currentGroupType = GroupType.parseGroupType( group );
                
                if ( currentGroupType == GroupType.Meta )
                {
                    headerFound = true;
                }
                else// if ( currentGroupType != null )
                {
                    if ( headerFound )
                        return ( false );
                }
                
                return ( true );
            }
            
            @Override
            protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
            {
                if ( currentGroupType == null )
                {
                    if ( !settingBeforeGroupWarningThrown )
                    {
                        if ( !quietMode )
                        {
                            //throw new ParsingException( "Found setting before the first (known) group started (line " + lineNr + ")." );
                            Logger.log( "WARNING: Found setting before the first (known) group started (line " + lineNr + ")." );
                        }
                        
                        settingBeforeGroupWarningThrown = true;
                    }
                    
                    return ( true );
                }
                
                switch ( currentGroupType )
                {
                    case Meta:
                        /*
                        if ( key.equals( "rfDynHUD_Version" ) )
                        {
                            try
                            {
                                sourceVersion = Version.parseVersion( value );
                            }
                            catch ( Throwable t )
                            {
                                Logger.log( "WANRING: Unable to parse rfDynHUDVersion." );
                                sourceVersion = new Version( 0, 0, 0, null, 0 );
                            }
                        }
                        */
                        if ( key.equals( "Design_Resolution" ) )
                        {
                            String[] parts = value.split( "x" );
                            
                            if ( parts.length == 2 )
                            {
                                try
                                {
                                    result[0] = Integer.parseInt( parts[0] );
                                    result[1] = Integer.parseInt( parts[1] );
                                }
                                catch ( NumberFormatException e )
                                {
                                    if ( !quietMode )
                                        Logger.log( e );
                                }
                            }
                        }
                        
                        break;
                }
                
                return ( true );
            }
        }.parse( file );
        
        if ( ( result[0] < 0 ) || ( result[1] < 0 ) )
            return ( null );
        
        return ( result );
    }
    
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param in
     * @param widgetsConfig
     * 
     * @throws IOException if anything went wrong.
     */
    private void __loadConfiguration( InputStream in, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, final boolean isEditorMode, ConfigurationLoadListener loadListener ) throws IOException
    {
        __WCPrivilegedAccess.clear( widgetsConfig, gameData, isEditorMode, loadListener );
        
        currentKey = null;
        currentValue = null;
        keyPrefix = null;
        effectiveKey = null;
        sourceVersion = null;
        
        new AbstractIniParser()
        {
            private GroupType currentGroupType = null;
            private Widget currentWidget = null;
            private String widgetName = null;
            private boolean badWidget = false;
            
            private final Stack<Widget> partStack = new Stack<Widget>();
            private final Stack<Integer> partIndexStack = new Stack<Integer>();
            private Widget currentPart = null;
            private String partName = null;
            
            private boolean settingBeforeGroupWarningThrown = false;
            private String errorMessages = null;
            
            @Override
            protected boolean onGroupParsed( int lineNr, String group )
            {
                if ( currentWidget != null )
                {
                    __WCPrivilegedAccess.addWidget( widgetsConfig, currentWidget, true );
                    currentWidget = null;
                    widgetName = null;
                    badWidget = false;
                    partStack.clear();
                    partIndexStack.clear();
                    currentPart = null;
                    partName = null;
                }
                
                currentGroupType = GroupType.parseGroupType( group );
                
                if ( currentGroupType == GroupType.Widget )
                {
                    widgetName = group.substring( 8 );
                    badWidget = false;
                    partStack.clear();
                    //partStack.push( null );
                    partIndexStack.clear();
                    partIndexStack.push( -1 );
                }
                
                return ( true );
            }
            
            @Override
            protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
            {
                if ( currentGroupType == null )
                {
                    if ( !settingBeforeGroupWarningThrown )
                    {
                        //throw new ParsingException( "Found setting before the first (known) group started (line " + lineNr + ")." );
                        Logger.log( "WARNING: Found setting before the first (known) group started (line " + lineNr + ")." );
                        
                        settingBeforeGroupWarningThrown = true;
                    }
                    
                    return ( true );
                }
                
                currentKey = key;
                currentValue = value;
                setKeyPrefix( keyPrefix );
                
                switch ( currentGroupType )
                {
                    case Meta:
                        if ( key.equals( "rfDynHUD_Version" ) )
                        {
                            try
                            {
                                sourceVersion = Version.parseVersion( value );
                            }
                            catch ( Throwable t )
                            {
                                Logger.log( "WANRING: Unable to parse rfDynHUDVersion." );
                                sourceVersion = new Version( 0, 0, 0, null, 0 );
                            }
                        }
                        
                        break;
                    case Global:
                        widgetsConfig.loadProperty( ConfigurationLoader.this );
                        break;
                    case NamedColors:
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
                        
                        break;
                    case NamedFonts:
                        java.awt.Font font = FontUtils.parseFont( value, widgetsConfig.getGameResolution().getViewportHeight(), false, false );
                        
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
                        
                        break;
                    case BorderAliases:
                        widgetsConfig.addBorderAlias( key, value );
                        
                        break;
                    case Widget:
                        if ( ( widgetName != null ) && ( currentWidget == null ) && !key.equals( "class" ) )
                        {
                            if ( !badWidget )
                                //throw new ParsingException( "Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                                Logger.log( "WARNING: Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                            
                            badWidget = true;
                        }
                        else if ( key.equals( "class" ) )
                        {
                            if ( widgetName != null )
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
                            else if ( partName != null )
                            {
                                // TODO: Actually create the part object!
                                
                                currentPart = ( (AssembledWidget)currentWidget ).getPart( partIndexStack.get( partIndexStack.size() - 2 ) );
                                partStack.set( partStack.size() - 1, currentPart );
                                partName = null;
                            }
                            else
                            {
                                Logger.log( "WARNING: Cannot load the Widget configuration line " + lineNr + "." );
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
                        else if ( key.equals( "<WidgetPart>" ) )
                        {
                            partName = value;
                            
                            partStack.push( null );
                            
                            partIndexStack.set( partIndexStack.size() - 1, partIndexStack.peek() + 1 );
                            partIndexStack.push( -1 );
                        }
                        else if ( key.equals( "</WidgetPart>" ) )
                        {
                            if ( partStack.isEmpty() )
                            {
                                Logger.log( "WARNING: Found Widget part end at line " + lineNr + " with no part begun." );
                            }
                            else
                            {
                                partStack.pop();
                                partIndexStack.pop();
                                
                                if ( !partStack.isEmpty() )
                                    currentPart = partStack.peek();
                                else
                                    currentPart = null;
                            }
                        }
                        else
                        {
                            try
                            {
                                if ( currentPart == null )
                                    currentWidget.loadProperty( ConfigurationLoader.this );
                                else
                                    currentPart.loadProperty( ConfigurationLoader.this );
                            }
                            catch ( Throwable t )
                            {
                                //throw new Error( t );
                                Logger.log( t );
                            }
                        }
                        
                        break;
                }
                
                return ( true );
            }
            
            @Override
            protected void onParsingFinished()
            {
                if ( currentWidget != null )
                {
                    __WCPrivilegedAccess.addWidget( widgetsConfig, currentWidget, true );
                    currentWidget = null;
                }
                
                if ( ( errorMessages != null ) && isEditorMode )
                {
                    JOptionPane.showMessageDialog( null, errorMessages, "Error loading config ini", JOptionPane.ERROR_MESSAGE );
                }
            }
        }.parse( in );
        
        currentKey = null;
        currentValue = null;
        keyPrefix = null;
        effectiveKey = null;
        
        __WCPrivilegedAccess.sortWidgets( widgetsConfig );
        __WCPrivilegedAccess.setJustLoaded( widgetsConfig, gameData, isEditorMode, loadListener );
    }
    
    private static File currentlyLoadedConfigFile = null;
    private static long lastModified = -1L;
    private static boolean isFirstLoadAttempt = true;
    
    private File _loadConfiguration( File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener loadListener ) throws IOException
    {
        Logger.log( "Loading configuration file from \"" + file.getAbsolutePath() + "\"" );
        
        __loadConfiguration( new FileInputStream( file ), widgetsConfig, gameData, isEditorMode, loadListener );
        
        __WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        currentlyLoadedConfigFile = file.getAbsoluteFile();
        lastModified = currentlyLoadedConfigFile.lastModified();
        
        return ( currentlyLoadedConfigFile );
    }
    
    private File load( File currentlyLoadedConfigFile, long lastModified, File configFile, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener loadListener ) throws IOException
    {
        if ( currentlyLoadedConfigFile == null )
            return ( _loadConfiguration( configFile, widgetsConfig, gameData, isEditorMode, loadListener ) );
        
        if ( !configFile.equals( currentlyLoadedConfigFile ) || ( configFile.lastModified() > lastModified ) )
            return ( _loadConfiguration( configFile, widgetsConfig, gameData, isEditorMode, loadListener ) );
        
        //__WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        return ( currentlyLoadedConfigFile );
    }
    
    private boolean load( File configFile, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener loadListener ) throws IOException
    {
        File old_currentlyLoadedConfigFile = currentlyLoadedConfigFile;
        long old_lastModified = lastModified;
        
        load( currentlyLoadedConfigFile, lastModified, configFile, widgetsConfig, gameData, isEditorMode, loadListener );
        
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
    File forceLoadConfiguration( File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener loadListener ) throws IOException
    {
        currentlyLoadedConfigFile = null;
        lastModified = -1L;
        
        if ( load( file, widgetsConfig, gameData, isEditorMode, loadListener ) )
            return ( currentlyLoadedConfigFile );
        
        return ( null );
    }
    
    /**
     * 
     * @param widgetsConfig
     * @param gameData
     * @param isEditorMode
     * @param loadListener
     * @throws IOException
     */
    void loadFactoryDefaults( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener loadListener ) throws IOException
    {
        Logger.log( "Loading factory default configuration." );
        
        //__loadConfiguration( new InputStreamReader( ConfigurationLoader.class.getResourceAsStream( "/data/config/overlay.ini" ) ), widgetsConfig, gameData, isEditorMode, loadListener );
        
        net.ctdp.rfdynhud.widgets.hidden.internal.InternalWidget internalWidget = new net.ctdp.rfdynhud.widgets.hidden.internal.InternalWidget();
        internalWidget.setMessage( "Couldn't find any overlay file." );
        __WCPrivilegedAccess.addWidget( widgetsConfig, internalWidget, true );
        internalWidget.getPosition().setEffectivePosition( RelativePositioning.TOP_CENTER, ( widgetsConfig.getGameResolution().getViewportWidth() - internalWidget.getEffectiveWidth() ) / 2, 200 );
        
        __WCPrivilegedAccess.sortWidgets( widgetsConfig );
        __WCPrivilegedAccess.setJustLoaded( widgetsConfig, gameData, isEditorMode, loadListener );
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
     * @param modName
     * @param vehicleClass
     * @param sessionType
     * @param widgetsConfig
     * @param gameData
     * @param isEditorMode
     * @param loadListener
     * @param force
     * 
     * @return the file, from which the configuration has been loaded.
     */
    Boolean reloadConfiguration( boolean smallMonitor, boolean bigMonitor, boolean isInGarage, String modName, String vehicleClass, SessionType sessionType, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener loadListener, boolean force )
    {
        if ( force || !widgetsConfig.isValid() )
        {
            currentlyLoadedConfigFile = null;
            lastModified = -1L;
        }
        
        File f = null;
        
        try
        {
            ConfigurationCandidatesIterator it = new ConfigurationCandidatesIterator( smallMonitor, bigMonitor, isInGarage, modName, vehicleClass, sessionType );
            
            while ( it.hasNext() )
            {
                f = it.next();
                
                if ( f.exists() )
                {
                    return ( load( f, widgetsConfig, gameData, isEditorMode, loadListener ) );
                }
            }
            
            if ( smallMonitor || bigMonitor )
            {
                __WCPrivilegedAccess.setValid( widgetsConfig, false );
                
                return ( null );
            }
            
            boolean result = ( ( currentlyLoadedConfigFile != null ) || isFirstLoadAttempt );
            
            if ( result )
            {
                loadFactoryDefaults( widgetsConfig, gameData, isEditorMode, loadListener );
            }
            
            isFirstLoadAttempt = false;
            
            return ( result );
        }
        catch ( Throwable t )
        {
            Logger.log( "Error loading overlay config file " + ( f != null ? f.getAbsolutePath() : "" ) + "." );
            Logger.log( t );
            
            __WCPrivilegedAccess.setValid( widgetsConfig, false );
            
            boolean result = ( currentlyLoadedConfigFile != null );
            
            currentlyLoadedConfigFile = null;
            lastModified = -1L;
            
            return ( result );
        }
    }
}
