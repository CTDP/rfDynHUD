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
package net.ctdp.rfdynhud.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.JOptionPane;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.AbstractPropertiesKeeper;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration.ConfigurationLoadListener;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.base.widget.AbstractAssembledWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetFactory;
import net.ctdp.rfdynhud.widgets.base.widget.__WPrivilegedAccess;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.versioning.Version;
import org.openmali.vecmath2.util.ColorUtils;

/**
 * This utility class serves to load HUD configuration files.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ConfigurationLoader implements PropertyLoader
{
    public static final Object DEFAULT_PLACEHOLDER = "~DEFAULT~";
    
    private final ConfigurationLoadListener loadListener;
    
    private String noConfigFoundMessage = "Couldn't find any overlay configuration file.";
    
    private String keyPrefix = null;
    
    private String currentKey = null;
    private String currentValue = null;
    
    private String effectiveKey = null;
    
    private Version sourceVersion = null;
    
    public void setNoConfigFoundMessage( String message )
    {
        this.noConfigFoundMessage = message;
    }
    
    public final String getNoConfigFoundMessage()
    {
        return ( noConfigFoundMessage );
    }
    
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
            if ( DEFAULT_PLACEHOLDER.equals( currentValue ) )
                property.setValue( property.getDefaultValue() );
            else
                property.loadValue( this, currentValue );
            
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
                            RFDHLog.exception( "WARNING: Found setting before the first (known) group started (line " + lineNr + ")." );
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
                                        RFDHLog.exception( e );
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
     * @param widgetsConfig
     * @param gameData
     * @param isEditorMode
     * 
     * @throws IOException if anything went wrong.
     */
    public void clearConfiguration( WidgetsConfiguration widgetsConfig, LiveGameData gameData, final boolean isEditorMode ) throws IOException
    {
        __WCPrivilegedAccess.clear( widgetsConfig, gameData, isEditorMode, loadListener );
    }
    
    /**
     * Loads fully configured {@link Widget}s to a {@link WidgetsConfiguration}.
     * 
     * @param in
     * @param name
     * @param widgetsConfig
     * @param gameData
     * @param isEditorMode
     * 
     * @throws IOException if anything went wrong.
     */
    public void loadConfiguration( InputStream in, String name, final WidgetsConfiguration widgetsConfig, final LiveGameData gameData, final boolean isEditorMode ) throws IOException
    {
        clearConfiguration( widgetsConfig, gameData, isEditorMode );
        
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
                    currentKey = "/";
                    currentValue = "propertiesLoadingFinished";
                    setKeyPrefix( keyPrefix );
                    
                    if ( currentPart == null )
                        currentWidget.loadProperty( ConfigurationLoader.this, gameData );
                    else
                        currentPart.loadProperty( ConfigurationLoader.this, gameData );
                    
                    //__WCPrivilegedAccess.addWidget( widgetsConfig, currentWidget, true );
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
                        RFDHLog.exception( "WARNING: Found setting before the first (known) group started (line " + lineNr + ")." );
                        
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
                                RFDHLog.exception( "WANRING: Unable to parse rfDynHUD Version." );
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
                            RFDHLog.exception( msg );
                            
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
                        if ( getSourceVersion().getBuild() < 92 )
                        {
                            value = value.replace( '-', FontUtils.SEPARATOR );
                        }
                        
                        java.awt.Font font = FontUtils.parseFont( value, widgetsConfig.getGameResolution().getViewportHeight(), false, false );
                        
                        if ( ( font == FontUtils.FALLBACK_FONT ) || ( font == FontUtils.FALLBACK_VIRTUAL_FONT ) )
                        {
                            String msg = "ERROR: Illegal font value on line #" + lineNr + ": " + value;
                            RFDHLog.exception( msg );
                            
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
                                RFDHLog.error( "WARNING: Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                            
                            badWidget = true;
                        }
                        else if ( key.equals( "class" ) )
                        {
                            if ( widgetName != null )
                            {
                                Class<Widget> clazz = WidgetFactory.getWidgetClass( value );
                                if ( clazz == null )
                                {
                                    RFDHLog.error( "Error: Unknown Widget class \"" + value + "\"." );
                                    badWidget = true;
                                }
                                else
                                {
                                    if ( AbstractAssembledWidget.class.isAssignableFrom( clazz ) )
                                        currentWidget = WidgetFactory.createAssembledWidget( value, widgetName );
                                    else
                                        currentWidget = WidgetFactory.createWidget( value, widgetName );
                                    if ( currentWidget == null )
                                    {
                                        RFDHLog.error( "Error creating Widget instance of class " + value + "." );
                                        badWidget = true;
                                    }
                                    else
                                    {
                                        currentKey = "name";
                                        currentValue = widgetName;
                                        setKeyPrefix( keyPrefix );
                                        widgetName = null;
                                        
                                        if ( currentPart == null )
                                            currentWidget.loadProperty( ConfigurationLoader.this, gameData );
                                        else
                                            currentPart.loadProperty( ConfigurationLoader.this, gameData );
                                        
                                        __WCPrivilegedAccess.addWidget( widgetsConfig, currentWidget, true, gameData );
                                    }
                                }
                            }
                            else if ( partName != null )
                            {
                                currentPart = WidgetFactory.createWidget( value, partName );
                                if ( currentPart == null )
                                {
                                    RFDHLog.error( "Error creating Widget part instance of class " + value + "." );
                                    badWidget = true;
                                }
                                else
                                {
                                    //currentPart = ( (AbstractAssembledWidget)currentWidget ).getPart( partIndexStack.get( partIndexStack.size() - 2 ) );
                                    __WPrivilegedAccess.addPart( currentPart, (AbstractAssembledWidget)currentWidget, gameData );
                                    partStack.set( partStack.size() - 1, currentPart );
                                    currentKey = "name";
                                    currentValue = partName;
                                    setKeyPrefix( keyPrefix );
                                    partName = null;
                                    
                                    if ( currentPart == null )
                                        currentWidget.loadProperty( ConfigurationLoader.this, gameData );
                                    else
                                        currentPart.loadProperty( ConfigurationLoader.this, gameData );
                                }
                            }
                            else
                            {
                                RFDHLog.exception( "WARNING: Cannot load the Widget configuration line " + lineNr + "." );
                            }
                        }
                        else if ( ( currentWidget == null ) || ( widgetName != null ) )
                        {
                            if ( !badWidget )
                            {
                                if ( currentWidget != null )
                                {
                                    //throw new ParsingException( "Cannot load the Widget \"" + currentWidget.getName() + "\" (line " + lineNr + ")." );
                                    RFDHLog.error( "WARNING: Cannot load the Widget \"" + currentWidget.getName() + "\" (line " + lineNr + ")." );
                                    badWidget = true;
                                }
                                else if ( widgetName != null )
                                {
                                    //throw new ParsingException( "Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                                    RFDHLog.error( "WARNING: Cannot load the Widget \"" + widgetName + "\" (line " + lineNr + ")." );
                                    badWidget = true;
                                }
                                else
                                {
                                    //throw new ParsingException( "Cannot load the a Widget (line " + lineNr + ")." );
                                    RFDHLog.error( "WARNING: Cannot load the a Widget (line " + lineNr + ")." );
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
                                RFDHLog.error( "WARNING: Found Widget part end at line " + lineNr + " with no part begun." );
                            }
                            else
                            {
                                currentKey = "/";
                                currentValue = "propertiesLoadingFinished";
                                setKeyPrefix( keyPrefix );
                                
                                if ( currentPart == null )
                                    currentWidget.loadProperty( ConfigurationLoader.this, gameData );
                                else
                                    currentPart.loadProperty( ConfigurationLoader.this, gameData );
                                
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
                                    currentWidget.loadProperty( ConfigurationLoader.this, gameData );
                                else
                                    currentPart.loadProperty( ConfigurationLoader.this, gameData );
                            }
                            catch ( Throwable t )
                            {
                                //throw new Error( t );
                                RFDHLog.error( t );
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
                    //__WCPrivilegedAccess.addWidget( widgetsConfig, currentWidget, true );
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
        
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            AbstractPropertiesKeeper.attachKeeper( widget, true );
            
            if ( widget instanceof AbstractAssembledWidget )
            {
                __WPrivilegedAccess.sortWidgetParts( (AbstractAssembledWidget)widget );
            }
        }
        
        __WCPrivilegedAccess.sortWidgets( widgetsConfig );
        __WCPrivilegedAccess.setJustLoaded( widgetsConfig, gameData, isEditorMode, name, loadListener );
    }
    
    private File currentlyLoadedConfigFile = null;
    private long lastModified = -1L;
    
    private File _loadConfiguration( File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode ) throws IOException
    {
        RFDHLog.println( "Loading configuration file from \"" + file.getAbsolutePath() + "\"..." );
        
        String name = null;
        if ( file.getName().startsWith( gameData.getFileSystem().getConfigPath() + File.pathSeparator ) )
            name = file.getName().substring( gameData.getFileSystem().getConfigPath().length() + 1 );
        
        loadConfiguration( new FileInputStream( file ), name, widgetsConfig, gameData, isEditorMode );
        
        __WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        currentlyLoadedConfigFile = file.getAbsoluteFile();
        lastModified = currentlyLoadedConfigFile.lastModified();
        
        return ( currentlyLoadedConfigFile );
    }
    
    private File load( File currentlyLoadedConfigFile, long lastModified, File configFile, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode ) throws IOException
    {
        if ( currentlyLoadedConfigFile == null )
            return ( _loadConfiguration( configFile, widgetsConfig, gameData, isEditorMode ) );
        
        if ( !configFile.equals( currentlyLoadedConfigFile ) || ( configFile.lastModified() > lastModified ) )
            return ( _loadConfiguration( configFile, widgetsConfig, gameData, isEditorMode ) );
        
        //__WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        return ( currentlyLoadedConfigFile );
    }
    
    private boolean load( File configFile, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode ) throws IOException
    {
        File old_currentlyLoadedConfigFile = currentlyLoadedConfigFile;
        long old_lastModified = lastModified;
        
        load( currentlyLoadedConfigFile, lastModified, configFile, widgetsConfig, gameData, isEditorMode );
        
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
    File forceLoadConfiguration( File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode ) throws IOException
    {
        currentlyLoadedConfigFile = null;
        lastModified = -1L;
        
        if ( load( file, widgetsConfig, gameData, isEditorMode ) )
            return ( currentlyLoadedConfigFile );
        
        return ( null );
    }
    
    /**
     * 
     * @param widgetsConfig
     * @param gameData
     * @param isEditorMode
     * @throws IOException
     */
    void loadFactoryDefaults( WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode ) throws IOException
    {
        RFDHLog.println( "Loading factory default configuration." );
        
        clearConfiguration( widgetsConfig, gameData, isEditorMode );
        
        //__loadConfiguration( new InputStreamReader( ConfigurationLoader.class.getResourceAsStream( "/data/config/overlay.ini" ) ), widgetsConfig, gameData, isEditorMode, loadListener );
        
        if ( ( getNoConfigFoundMessage() != null ) && ( getNoConfigFoundMessage().trim().length() > 0 ) )
        {
            net.ctdp.rfdynhud.widgets.internal.InternalWidget internalWidget = new net.ctdp.rfdynhud.widgets.internal.InternalWidget();
            internalWidget.setMessage( getNoConfigFoundMessage() );
            __WCPrivilegedAccess.addWidget( widgetsConfig, internalWidget, true, gameData );
            internalWidget.getPosition().setEffectivePosition( RelativePositioning.TOP_CENTER, ( widgetsConfig.getGameResolution().getViewportWidth() - internalWidget.getEffectiveWidth() ) / 2, 200 );
            
            AbstractPropertiesKeeper.attachKeeper( internalWidget, true );
            __WCPrivilegedAccess.sortWidgets( widgetsConfig );
        }
        
        __WCPrivilegedAccess.setJustLoaded( widgetsConfig, gameData, isEditorMode, null, loadListener );
        __WCPrivilegedAccess.setValid( widgetsConfig, true );
        
        currentlyLoadedConfigFile = null;
        lastModified = -1L;
    }
    
    /**
     * Loads a configuration and searches for the file in the order, defined by the iterator.
     * Then it checks, if that file is newer than the already loaded one.
     * 
     * @param candidatesIterator
     * @param widgetsConfig
     * @param gameData
     * @param isEditorMode
     * @param force
     * @param shortBreakInvalidCondition
     */
    public void reloadConfiguration( Iterator<File> candidatesIterator, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, boolean force, boolean shortBreakInvalidCondition )
    {
        if ( force || !widgetsConfig.isValid() )
        {
            currentlyLoadedConfigFile = null;
            lastModified = -1L;
        }
        
        File f = null;
        
        try
        {
            if ( candidatesIterator != null )
            {
                RFDHLog.debug( "DEBUG: (Re-)Loading overlay configuration..." );
                
                while ( candidatesIterator.hasNext() )
                {
                    f = candidatesIterator.next();
                    
                    RFDHLog.debug( "DEBUG: Trying overlay configuration file \"", f.getAbsolutePath(), "\"... ", ( f.exists() ? "found." : "not found." ) );
                    
                    if ( f.exists() )
                    {
                        load( f, widgetsConfig, gameData, isEditorMode );
                        return;
                    }
                }
            }
            
            if ( shortBreakInvalidCondition )
            {
                __WCPrivilegedAccess.setValid( widgetsConfig, false );
                
                return;
            }
            
            if ( candidatesIterator != null || !widgetsConfig.isValid() )
            {
                loadFactoryDefaults( widgetsConfig, gameData, isEditorMode );
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.error( "Error loading overlay config file " + ( f != null ? f.getAbsolutePath() : "" ) + "." );
            RFDHLog.error( t );
            
            __WCPrivilegedAccess.setValid( widgetsConfig, false );
            
            currentlyLoadedConfigFile = null;
            lastModified = -1L;
        }
    }
    
    public ConfigurationLoader( ConfigurationLoadListener loadListener )
    {
        this.loadListener = loadListener;
    }
}
