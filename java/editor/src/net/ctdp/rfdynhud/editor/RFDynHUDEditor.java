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
package net.ctdp.rfdynhud.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.WidgetImportManager.ImportDecision;
import net.ctdp.rfdynhud.editor.help.HelpWindow;
import net.ctdp.rfdynhud.editor.hiergrid.GridItemsContainer;
import net.ctdp.rfdynhud.editor.hiergrid.PropertySelectionListener;
import net.ctdp.rfdynhud.editor.presets.EditorPresetsWindow;
import net.ctdp.rfdynhud.editor.presets.ScaleType;
import net.ctdp.rfdynhud.editor.properties.DefaultWidgetPropertiesContainer;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditor;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditorTable;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditorTableModel;
import net.ctdp.rfdynhud.editor.properties.WidgetPropertyChangeListener;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.editor.util.DefaultWidgetsConfigurationWriter;
import net.ctdp.rfdynhud.editor.util.EditorPropertyLoader;
import net.ctdp.rfdynhud.editor.util.SaveAsDialog;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SupportedGames;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.gamedata.__GameIDHelper;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.gui.awt_swing.GUITools;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniWriter;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RFDynHUDEditor implements WidgetsEditorPanelListener, Documented, PropertySelectionListener<Property>
{
    static
    {
        __EDPrivilegedAccess.isEditorMode = true;
    }
    
    private static final String BASE_WINDOW_TITLE = "rFactor dynamic HUD Editor v" + RFDynHUD.VERSION.toString();
    
    private LiveGameData gameData;
    private WidgetsConfiguration widgetsConfig;
    private GameEventsManager eventsManager;
    private final GameResolution gameResolution;
    
    private boolean alwaysShowHelpOnStartup = true;
    
    private final JFrame window;
    private int windowLeft, windowTop;
    private int windowWidth, windowHeight;
    private final EditorMenuBar menuBar;
    private final WidgetsEditorPanel editorPanel;
    private final JScrollPane editorScrollPane;
    
    private final PropertiesEditor propsEditor;
    private final PropertiesEditorTable editorTable;
    private final JEditorPane docPanel;
    private boolean isSomethingDoced = false;
    
    private final EditorStatusBar statusBar;
    
    private final EditorPresets presets = new EditorPresets();
    
    private final EditorRunningIndicator runningIndicator;
    
    private static final String doc_header = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_header.html" ) );
    private static final String doc_footer = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_footer.html" ) );
    
    private boolean presetsWindowVisible = false;
    final EditorPresetsWindow presetsWindow;
    
    private boolean dirtyFlag = false;
    
    private File currentConfigFile = null;
    private File lastImportFile = null;
    private File currentTemplateFile = null;
    
    private WidgetsConfiguration templateConfig = null;
    private long lastTemplateConfigModified = -1L;
    
    ImportDecision lastImportDecision = ImportDecision.USE_DESTINATION_ALIASES;
    
    public final LiveGameData getGameData()
    {
        return ( gameData );
    }
    
    public final GameResolution getGameResolution()
    {
        return ( gameResolution );
    }
    
    public final WidgetsConfiguration getWidgetsConfiguration()
    {
        return ( widgetsConfig );
    }
    
    public final File getCurrentConfigFile()
    {
        return ( currentConfigFile );
    }
    
    public final EditorPresets getEditorPresets()
    {
        return ( presets );
    }
    
    private void updateWindowTitle()
    {
        if ( currentConfigFile == null )
            window.setTitle( BASE_WINDOW_TITLE + ( dirtyFlag ? " (*)" : "" ) );
        else
            window.setTitle( BASE_WINDOW_TITLE + " - " + currentConfigFile.getAbsolutePath() + ( dirtyFlag ? "*" : "" ) );
    }
    
    public final EditorRunningIndicator getRunningIndicator()
    {
        return ( runningIndicator );
    }
    
    public void setDirtyFlag()
    {
        this.dirtyFlag = true;
        
        updateWindowTitle();
    }
    
    private void resetDirtyFlag()
    {
        this.dirtyFlag = false;
        
        updateWindowTitle();
    }
    
    public final boolean getDirtyFlag()
    {
        return ( dirtyFlag );
    }
    
    public final JFrame getMainWindow()
    {
        return ( window );
    }
    
    public final EditorMenuBar getMenuBar()
    {
        return ( menuBar );
    }
    
    public final WidgetsEditorPanel getEditorPanel()
    {
        return ( editorPanel );
    }
    
    public final TextureImage2D getOverlayTexture()
    {
        return ( getEditorPanel().getOverlayTexture() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDocumentationSource( Property property )
    {
        if ( property == null )
            return ( "" );
        
        if ( __PropsPrivilegedAccess.isWidgetsConfigProperty( property ) )
            return ( widgetsConfig.getDocumentationSource( property ) );
        
        URL docURL = this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/doc/" + property.getName() + ".html" );
        
        if ( docURL == null )
            return ( "" );
        
        return ( StringUtil.loadString( docURL ) );
    }
    
    private Property currentDocedProperty = null;
    private Widget currentDocedWidget = null;
    
    @Override
    public void onPropertySelected( Property property, int row )
    {
        if ( property == null )
        {
            if ( ( currentDocedWidget != editorPanel.getSelectedWidget() ) || ( ( currentDocedWidget == null ) && isSomethingDoced ) )
            {
                if ( editorPanel.getSelectedWidget() == null )
                {
                    docPanel.setText( doc_header + "" + doc_footer );
                    isSomethingDoced = false;
                }
                else
                {
                    String helpText = editorPanel.getSelectedWidget().getDocumentationSource( null );
                    if ( helpText == null )
                    {
                        docPanel.setText( doc_header + "" + doc_footer );
                        isSomethingDoced = false;
                    }
                    else
                    {
                        docPanel.setText( doc_header + helpText + doc_footer );
                        isSomethingDoced = true;
                    }
                }
                
                docPanel.setCaretPosition( 0 );
            }
            
            currentDocedProperty = null;
            currentDocedWidget = editorPanel.getSelectedWidget();
        }
        else if ( property != currentDocedProperty )
        {
            if ( editorPanel.getSelectedWidget() == null )
            {
                docPanel.setText( doc_header + getDocumentationSource( property ) + doc_footer );
            }
            else
            {
                //docPanel.setText( doc_header + editorPanel.getSelectedWidget().getDocumentationSource( property ) + doc_footer );
                docPanel.setText( doc_header + property.getWidget().getDocumentationSource( property ) + doc_footer );
            }
            
            docPanel.setCaretPosition( 0 );
            
            currentDocedProperty = property;
            currentDocedWidget = null;
        }
    }
    
    private static boolean checkConfigFile( File f )
    {
        try
        {
            // Check for header existence.
            return ( ConfigurationLoader.readDesignResolutionFromConfiguration( f, true ) != null );
        }
        catch ( Throwable e )
        {
            return ( false );
        }
    }
    
    private void fillConfigurationFiles( File folder, ArrayList<String> list )
    {
        for ( File f : folder.listFiles() )
        {
            if ( f.isFile() && f.getName().toLowerCase().endsWith( ".ini" ) )
            {
                if ( checkConfigFile( f ) )
                    list.add( f.getAbsolutePath().substring( GameFileSystem.INSTANCE.getConfigPath().length() + 1 ) );
            }
        }
        
        for ( File f : folder.listFiles() )
        {
            if ( f.isDirectory() )
            {
                fillConfigurationFiles( f, list );
            }
        }
    }
    
    private ArrayList<String> getConfigurationFiles()
    {
        ArrayList<String> list = new ArrayList<String>();
        
        list.add( "<none>" );
        
        fillConfigurationFiles( GameFileSystem.INSTANCE.getConfigFolder(), list );
        
        return ( list );
    }
    
    private String getCurrentTemplateFileForProperty()
    {
        if ( currentTemplateFile == null )
            return ( "<none>" );
        
        return ( currentTemplateFile.getAbsolutePath().substring( GameFileSystem.INSTANCE.getConfigPath().length() + 1 ) );
    }
    
    private void getProperties( WidgetPropertiesContainer propsCont )
    {
        propsCont.addGroup( "Editor - General" );
        
        propsCont.addProperty( new Property( (Widget)null, "resolution", true, PropertyEditorType.STRING )
        {
            @Override
            public void setValue( Object value )
            {
            }
            
            @Override
            public Object getValue()
            {
                return ( gameResolution.getResolutionString() );
            }
            
            @Override
            public void loadValue( String value )
            {
            }
        } );
        
        getEditorPanel().getSettings().getProperties( propsCont );
        
        propsCont.addProperty( new ListProperty<String, ArrayList<String>>( (Widget)null, "templateConfig", "templateConfig", getCurrentTemplateFileForProperty(), getConfigurationFiles(), false, "reload" )
        {
            @Override
            protected boolean getTriggerOnValueChangedBeforeAttachedToConfig()
            {
                return ( true );
            }
            
            @Override
            public void setValue( Object value )
            {
                if ( value.equals( "none" ) )
                {
                    currentTemplateFile = null;
                    lastTemplateConfigModified = -1L;
                    templateConfig = null;
                }
                else
                {
                    try
                    {
                        loadTemplateConfig( new File( GameFileSystem.INSTANCE.getConfigFolder(), (String)value ) );
                    }
                    catch ( IOException e )
                    {
                        Logger.log( e );
                    }
                }
            }
            
            @Override
            public String getValue()
            {
                return ( getCurrentTemplateFileForProperty() );
            }
            
            @Override
            public void onButtonClicked( Object button )
            {
                if ( currentTemplateFile != null )
                {
                    try
                    {
                        loadTemplateConfig( currentTemplateFile );
                    }
                    catch ( IOException e )
                    {
                        Logger.log( e );
                    }
                }
            }
        } );
        
        propsCont.addGroup( "Configuration - Global" );
        
        widgetsConfig.getProperties( propsCont, false );
    }
    
    private final HashMap<String, Boolean> expandedRows = new HashMap<String, Boolean>();
    
    @Override
    public void onWidgetSelected( Widget widget, boolean selectionChanged, boolean doubleClick )
    {
        GridItemsContainer<Property> propsList = propsEditor.getPropertiesList();
        
        expandedRows.clear();
        PropertiesEditorTableModel.readExpandFlags( PropertiesEditorTableModel.ITEMS_HANDLER, propsList, expandedRows );
        
        propsEditor.clear();
        
        if ( widget == null )
        {
            this.getProperties( new DefaultWidgetPropertiesContainer( propsList ) );
        }
        else
        {
            widget.getProperties( new DefaultWidgetPropertiesContainer( propsList ), false );
        }
        
        onPropertySelected( null, -1 );
        PropertiesEditorTableModel.restoreExpandFlags( PropertiesEditorTableModel.ITEMS_HANDLER, propsList, expandedRows );
        
        editorTable.applyToModel();
    }
    
    //private long nextRedrawTime = -1L;
    
    /**
     * @param widget the changed widget
     * @param propertyName the name of the changed property
     * @param isPosSize is position or size?
     */
    public void onWidgetChanged( Widget widget, String propertyName, boolean isPosSize )
    {
        editorPanel.repaint();
        
        setDirtyFlag();
        
        if ( ( widget != null ) && ( widget == getEditorPanel().getSelectedWidget() ) )
        {
            //editorTable.apply();
            //onWidgetSelected( widget, false );
            
            PropertiesEditorTableModel m = (PropertiesEditorTableModel)editorTable.getModel();
            int rc = m.getRowCount();
            for ( int i = 0; i < rc; i++ )
            {
                if ( m.isDataRow( i ) )
                {
                    String pn = (String)m.getValueAt( i, 1 );
                    if ( pn.equals( "positioning" ) || pn.equals( "x" ) || pn.equals( "y" ) || pn.equals( "width" ) || pn.equals( "height" ) )
                        m.fireTableCellUpdated( i, 2 );
                }
                else
                {
                    // For some reason group headers need to be fully updated.
                    m.fireTableRowsUpdated( i, i );
                }
            }
        }
    }
    
    private static File getSettingsDir()
    {
        /*
        File f = new File( System.getProperty( "user.home" ) );
        
        f = new File( f, "CTDP/.rfdynhud" );
        if ( !f.exists() )
            f.mkdirs();
        
        return ( f );
        */
        //return ( RFactorTools.EDITOR_FOLDER );
        return ( GameFileSystem.INSTANCE.getConfigFolder() );
    }
    
    private static File getEditorSettingsFile()
    {
        return ( new File( getSettingsDir(), "editor_settings.ini" ) );
    }
    
    private void writeLastConfig( IniWriter writer ) throws Throwable
    {
        if ( ( currentConfigFile != null ) && currentConfigFile.exists() )
        {
            String currentConfigFilename = currentConfigFile.getAbsolutePath();
            if ( currentConfigFilename.startsWith( GameFileSystem.INSTANCE.getConfigPath() ) )
            {
                if ( currentConfigFilename.charAt( GameFileSystem.INSTANCE.getConfigPath().length() ) == File.separatorChar )
                    currentConfigFilename = currentConfigFilename.substring( GameFileSystem.INSTANCE.getConfigPath().length() + 1 );
                else
                    currentConfigFilename = currentConfigFilename.substring( GameFileSystem.INSTANCE.getConfigPath().length() );
            }
            
            writer.writeSetting( "lastConfig", currentConfigFilename );
        }
    }
    
    private void writeLastImportFile( IniWriter writer ) throws Throwable
    {
        if ( lastImportFile != null )
        {
            String filename = lastImportFile.getAbsolutePath();
            if ( filename.startsWith( GameFileSystem.INSTANCE.getConfigPath() ) )
            {
                if ( filename.charAt( GameFileSystem.INSTANCE.getConfigPath().length() ) == File.separatorChar )
                    filename = filename.substring( GameFileSystem.INSTANCE.getConfigPath().length() + 1 );
                else
                    filename = filename.substring( GameFileSystem.INSTANCE.getConfigPath().length() );
            }
            
            writer.writeSetting( "lastImportFile", filename );
        }
    }
    
    private void saveUserSettings( int extendedState )
    {
        File userSettingsFile = getEditorSettingsFile();
        
        IniWriter writer = null;
        try
        {
            writer = new IniWriter( userSettingsFile );
            WidgetsConfigurationWriter confWriter = new DefaultWidgetsConfigurationWriter( writer );
            
            writer.writeGroup( "General" );
            writer.writeSetting( "resolution", gameResolution.getResolutionString() );
            getEditorPanel().getSettings().saveProperties( confWriter, writer );
            writer.writeSetting( "templatesConfig", getCurrentTemplateFileForProperty() );
            writer.writeSetting( "defaultScaleType", presetsWindow.getDefaultScaleType() );
            writeLastConfig( writer );
            writeLastImportFile( writer );
            writer.writeSetting( "alwaysShowHelpOnStartup", alwaysShowHelpOnStartup );
            writer.writeSetting( "lastImportDecision", lastImportDecision );
            
            writer.writeGroup( "MainWindow" );
            writer.writeSetting( "windowLocation", windowLeft + "x" + windowTop );
            writer.writeSetting( "windowSize", windowWidth + "x" + windowHeight );
            writer.writeSetting( "windowState", extendedState );
            
            writer.writeGroup( "PresetsWindow" );
            writer.writeSetting( "windowLocation", presetsWindow.getX() + "x" + presetsWindow.getY() );
            writer.writeSetting( "windowSize", presetsWindow.getWidth() + "x" + presetsWindow.getHeight() );
            writer.writeSetting( "windowVisible", presetsWindowVisible );
            writer.writeSetting( "autoApply", presetsWindow.getAutoApply() );
            
            writer.writeGroup( "EditorPresets" );
            __EDPrivilegedAccess.saveProperties( presets, confWriter );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        finally
        {
            if ( writer != null )
            {
                try { writer.close(); } catch ( Throwable t ) {}
            }
        }
    }
    
    private int[] loadResolutionFromUserSettings()
    {
        File userSettingsFile = getEditorSettingsFile();
        
        final boolean[] resFound = { false };
        final int[] resolution = new int[ 2 ];
        
        if ( userSettingsFile.exists() )
        {
            try
            {
                new AbstractIniParser()
                {
                    @Override
                    protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                    {
                        if ( group == null )
                        {
                        }
                        else if ( group.equals( "General" ) )
                        {
                            if ( key.equals( "resolution" ) )
                            {
                                try
                                {
                                    String[] ss = value.split( "x" );
                                    int resX = Integer.parseInt( ss[0] );
                                    int resY = Integer.parseInt( ss[1] );
                                    //if ( getEditorPanel().getSettings().checkResolution( resX, resY ) )
                                    {
                                        resolution[0] = resX;
                                        resolution[1] = resY;
                                        resFound[0] = true;
                                        
                                        return ( false );
                                    }
                                }
                                catch ( Throwable t )
                                {
                                    t.printStackTrace();
                                }
                            }
                        }
                        
                        return ( true );
                    }
                }.parse( userSettingsFile );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
        
        if ( !resFound[0] )
        {
            Rectangle screenBounds = GUITools.getCurrentScreenBounds();
            
            resolution[0] = screenBounds.width;
            resolution[1] = screenBounds.height;
        }
        
        return ( resolution );
    }
    
    private void loadEditorPresets()
    {
        File userSettingsFile = getEditorSettingsFile();
        
        if ( !userSettingsFile.exists() )
            return;
        
        final EditorPropertyLoader loader = new EditorPropertyLoader();
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    loader.setCurrentSetting( key, value );
                    
                    if ( group == null )
                    {
                    }
                    else if ( group.equals( "EditorPresets" ) )
                    {
                        __EDPrivilegedAccess.loadProperty( presets, loader );
                    }
                    
                    return ( true );
                }
            }.parse( userSettingsFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    private Object[] loadUserSettings()
    {
        final Object[] result = new Object[] { false, null };
        
        File userSettingsFile = getEditorSettingsFile();
        
        if ( !userSettingsFile.exists() )
            return ( result );
        
        final EditorPropertyLoader loader = new EditorPropertyLoader();
        
        try
        {
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    loader.setCurrentSetting( key, value );
                    
                    if ( group == null )
                    {
                    }
                    else if ( group.equals( "General" ) )
                    {
                        getEditorPanel().getSettings().loadProperty( loader );
                        
                        if ( key.equals( "screenshotSet" ) )
                        {
                            editorPanel.getSettings().setScreenshotSet( value );
                        }
                        else if ( key.equals( "resolution" ) )
                        {
                            try
                            {
                                String[] ss = value.split( "x" );
                                int resX = Integer.parseInt( ss[0] );
                                int resY = Integer.parseInt( ss[1] );
                                if ( ( ( gameResolution.getViewportWidth() != resX ) || ( gameResolution.getViewportHeight() != resY ) ) && getEditorPanel().getSettings().checkResolution( resX, resY ) )
                                    getEditorPanel().switchToGameResolution( resX, resY );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "templatesConfig" ) )
                        {
                            try
                            {
                                loadTemplateConfig( new File( GameFileSystem.INSTANCE.getConfigFolder(), value ) );
                            }
                            catch ( IOException e )
                            {
                                Logger.log( e );
                            }
                        }
                        else if ( key.equals( "defaultScaleType" ) )
                        {
                            try
                            {
                                presetsWindow.setDefaultScaleType( ScaleType.valueOf( value ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "lastConfig" ) )
                        {
                            File configFile = new File( value );
                            if ( !configFile.isAbsolute() )
                                configFile = new File( GameFileSystem.INSTANCE.getConfigFolder(), value );
                            if ( configFile.exists() )
                                //openConfig( configFile );
                                result[1] = configFile;
                        }
                        else if ( key.equals( "lastImportFile" ) )
                        {
                            File configFile = new File( value );
                            if ( !configFile.isAbsolute() )
                                configFile = new File( GameFileSystem.INSTANCE.getConfigFolder(), value );
                            
                            lastImportFile = configFile;
                        }
                        else if ( key.equals( "alwaysShowHelpOnStartup" ) )
                        {
                            alwaysShowHelpOnStartup = Boolean.parseBoolean( value );
                        }
                        else if ( key.equals( "lastImportDecision" ) )
                        {
                            try
                            {
                                lastImportDecision = ImportDecision.valueOf( value );
                            }
                            catch ( Throwable t )
                            {
                            }
                        }
                    }
                    else if ( group.equals( "MainWindow" ) )
                    {
                        if ( key.equals( "windowLocation" ) )
                        {
                            try
                            {
                                String[] ss = value.split( "x" );
                                getMainWindow().setLocation( Integer.parseInt( ss[0] ), Integer.parseInt( ss[1] ) );
                                windowLeft = getMainWindow().getX();
                                windowTop = getMainWindow().getY();
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "windowSize" ) )
                        {
                            try
                            {
                                String[] ss = value.split( "x" );
                                getMainWindow().setSize( Integer.parseInt( ss[0] ), Integer.parseInt( ss[1] ) );
                                windowWidth = getMainWindow().getWidth();
                                windowHeight = getMainWindow().getHeight();
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "windowState" ) )
                        {
                            try
                            {
                                getMainWindow().setExtendedState( Integer.parseInt( value ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                    }
                    else if ( group.equals( "PresetsWindow" ) )
                    {
                        if ( key.equals( "windowLocation" ) )
                        {
                            try
                            {
                                //optionsWindow.setLocationRelativeTo( null );
                                String[] ss = value.split( "x" );
                                presetsWindow.setLocation( Integer.parseInt( ss[0] ), Integer.parseInt( ss[1] ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "windowSize" ) )
                        {
                            try
                            {
                                String[] ss = value.split( "x" );
                                presetsWindow.setSize( Integer.parseInt( ss[0] ), Integer.parseInt( ss[1] ) );
                                presetsWindow.setDontSetWindowSize();
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "windowVisible" ) )
                        {
                            try
                            {
                                presetsWindowVisible = Boolean.parseBoolean( value );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "autoApply" ) )
                        {
                            try
                            {
                                presetsWindow.setAutoApply( Boolean.parseBoolean( value ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                    }
                    else if ( group.equals( "EditorPresets" ) )
                    {
                        __EDPrivilegedAccess.loadProperty( presets, loader );
                    }
                    
                    return ( true );
                }
            }.parse( userSettingsFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        result[0] = true;
        
        return ( result );
    }
    
    private File loadTemplateConfig( File templateConfigFile ) throws IOException
    {
        if ( !templateConfigFile.exists() )
        {
            this.currentTemplateFile = null;
            this.lastTemplateConfigModified = -1L;
            this.templateConfig = null;
            
            return ( null );
        }
        
        if ( !templateConfigFile.equals( this.currentTemplateFile ) || ( templateConfigFile.lastModified() > lastTemplateConfigModified ) )
        {
            this.templateConfig = new WidgetsConfiguration();
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader(), templateConfigFile, templateConfig, gameData, true, null );
            
            this.currentTemplateFile = templateConfigFile;
            this.lastTemplateConfigModified = templateConfigFile.lastModified();
        }
        
        return ( templateConfigFile );
    }
    
    private void clearWidetRegions()
    {
        int n = widgetsConfig.getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = widgetsConfig.getWidget( i );
            
            widget.clearRegion( getOverlayTexture(), widget.getPosition().getEffectiveX(), widget.getPosition().getEffectiveY() );
        }
    }
    
    private void loadFallbackConfig() throws IOException
    {
        clearWidetRegions();
        
        __WCPrivilegedAccess.clear( widgetsConfig, gameData, true, null );
        __UtilPrivilegedAccess.loadFactoryDefaults( new ConfigurationLoader(), widgetsConfig, gameData, true, null );
        
        getOverlayTexture().clear( true, null );
        editorPanel.setSelectedWidget( null, false );
        getEditorPanel().repaint();
    }
    
    public void openConfig( File configFile )
    {
        try
        {
            clearWidetRegions();
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader(), configFile, widgetsConfig, gameData, true, null );
            
            currentConfigFile = configFile;
            
            updateWindowTitle();
            
            getOverlayTexture().clear( true, null );
            editorPanel.setSelectedWidget( null, false );
            getEditorPanel().repaint();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            JOptionPane.showMessageDialog( window, ( t.getMessage() != null ) ? t.getMessage() : t.getClass().getSimpleName(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE );
        }
    }
    
    private File getOpenConfigFile( File initialFile )
    {
        JFileChooser fc = new JFileChooser();
        if ( initialFile == null )
        {
            fc.setCurrentDirectory( GameFileSystem.INSTANCE.getConfigFolder() );
            fc.setSelectedFile( new File( GameFileSystem.INSTANCE.getConfigFolder(), "overlay.ini" ) );
        }
        else if ( initialFile.isFile() )
        {
            fc.setCurrentDirectory( initialFile.getParentFile() );
            fc.setSelectedFile( initialFile );
        }
        else
        {
            fc.setCurrentDirectory( initialFile );
        }
        
        fc.setMultiSelectionEnabled( false );
        fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
        fc.setFileFilter( new FileNameExtensionFilter( "ini files", "ini" ) );
        
        if ( fc.showOpenDialog( window ) != JFileChooser.APPROVE_OPTION )
            return ( null );
        
        return ( fc.getSelectedFile() );
    }
    
    public void openConfig()
    {
        if ( getDirtyFlag() )
        {
            int result = JOptionPane.showConfirmDialog( window, "Do you want to save the changes before opening a new config?", window.getTitle(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
            
            if ( result == JOptionPane.CANCEL_OPTION )
                return;
            
            if ( result == JOptionPane.YES_OPTION )
            {
                if ( !saveConfig() )
                    return;
            }
        }
        
        File file = getOpenConfigFile( currentConfigFile );
        
        if ( file == null )
            return;
        
        try
        {
            openConfig( file );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            JOptionPane.showMessageDialog( window, ( t.getMessage() != null ) ? t.getMessage() : t.getClass().getSimpleName(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE );
        }
    }
    
    public void importWidget()
    {
        File file = lastImportFile;
        while ( ( file != null ) && !file.exists() )
        {
            file = file.getParentFile();
        }
        
        file = getOpenConfigFile( file );
        
        if ( file == null )
            return;
        
        lastImportFile = file;
        
        try
        {
            int[] designResolution = ConfigurationLoader.readDesignResolutionFromConfiguration( file, false );
            
            if ( designResolution == null )
            {
                designResolution = new int[] { gameResolution.getViewportWidth(), gameResolution.getViewportHeight() };
            }
            
            JDialog f = new JDialog( window, "Import Widget", true );
            f.setResizable( false );
            f.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
            
            WidgetsDrawingManager wdm = new WidgetsDrawingManager( true, designResolution[0], designResolution[1], true );
            WidgetsEditorPanel ep = new WidgetsEditorPanel( null, this, gameData, wdm );
            ImportWidgetsEditorPanelInputHandler inputHandler = new ImportWidgetsEditorPanelInputHandler( this, ep, wdm );
            ep.addMouseListener( inputHandler );
            ep.addKeyListener( inputHandler );
            
            Rectangle screenBounds = GUITools.getCurrentScreenBounds();
            
            int maxWidth = (int)( screenBounds.width * 0.9f );
            int maxHeight = (int)( screenBounds.height * 0.9f );
            
            int width = Math.min( designResolution[0], maxWidth );
            int height = Math.min( designResolution[1], maxHeight );
            
            float scaleX = (float)width / (float)designResolution[0];
            float scaleY = (float)height / (float)designResolution[1];
            float scale = Math.min( Math.min( scaleX, scaleY ), 1.0f );
            
            ep.setScaleFactor( scale );
            ep.initBackgroundImage();
            
            f.setContentPane( ep );
            f.pack();
            
            f.setLocationRelativeTo( window );
            f.setLocation( f.getX(), Math.max( 0, f.getY() ) );
            
            //System.out.println( f.getWidth() + ", " + f.getHeight() );
            
            //clearWidetRegions();
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader(), file, wdm, gameData, true, null );
            
            //updateWindowTitle();
            
            //wdm.getMainTexture( 0 ).clear( true, null );
            //onWidgetSelected( null, false );
            //ep.repaint();
            
            f.setVisible( true );
            
            widgetsConfig.setAllDirtyFlags();
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
            {
                widgetsConfig.getWidget( i ).forceReinitialization();
            }
            editorPanel.repaint();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            JOptionPane.showMessageDialog( window, ( t.getMessage() != null ) ? t.getMessage() : t.getClass().getSimpleName(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE );
        }
    }
    
    public boolean saveConfig()
    {
        if ( currentConfigFile == null )
        {
            if ( saveConfigAs() != null )
                return ( true );
            
            return ( false );
        }
        
        try
        {
            WidgetsEditorPanel ep = getEditorPanel();
            ConfigurationSaver.saveConfiguration( widgetsConfig, gameResolution.getResolutionString(), ep.getSettings().getGridOffsetX(), ep.getSettings().getGridOffsetY(), ep.getSettings().getGridSizeX(), ep.getSettings().getGridSizeY(), currentConfigFile );
            
            resetDirtyFlag();
            
            return ( true );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            JOptionPane.showMessageDialog( window, ( t.getMessage() != null ) ? t.getMessage() : t.getClass().getSimpleName(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE );
            
            return ( false );
        }
    }
    
    public File saveConfigAs()
    {
        SaveAsDialog sad = new SaveAsDialog( this );
        sad.setSelectedFile( currentConfigFile );
        
        sad.setVisible( true );
        
        if ( sad.getSelectedFile() == null )
            return ( null );
        
        currentConfigFile = sad.getSelectedFile();
        
        try
        {
            currentConfigFile.getParentFile().mkdirs();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        saveConfig();
        
        return ( currentConfigFile );
    }
    
    public void onCloseRequested()
    {
        if ( getDirtyFlag() )
        {
            int result = JOptionPane.showConfirmDialog( window, "Do you want to save the changes before exit?", window.getTitle(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
            
            if ( result == JOptionPane.CANCEL_OPTION )
                return;
            
            if ( result == JOptionPane.YES_OPTION )
            {
                saveConfig();
            }
        }
        
        presetsWindowVisible = presetsWindow.isVisible();
        presetsWindow.setVisible( false );
        
        getMainWindow().setVisible( false );
        int extendedState = getMainWindow().getExtendedState();
        getMainWindow().setExtendedState( JFrame.NORMAL );
        saveUserSettings( extendedState );
        
        presetsWindow.dispose();
        getMainWindow().dispose();
        System.exit( 0 );
    }
    
    @Override
    public void onWidgetPositionSizeChanged( Widget widget )
    {
        onWidgetChanged( widget, null, true );
    }
    
    @Override
    public boolean onWidgetRemoved( Widget widget )
    {
        if ( widget == null )
            return ( false );
        
        Logger.log( "Removing Widget of type \"" + widget.getClass().getName() + "\" and name \"" + widget.getName() + "\"..." );
        
        __WCPrivilegedAccess.removeWidget( widgetsConfig, widget );
        
        setDirtyFlag();
        
        return ( true );
    }
    
    @Override
    public void onContextMenuRequested( Widget[] hoveredWidgets )
    {
        EditorMenuBar.initContextMenu( this, hoveredWidgets );
    }
    
    @Override
    public void onZoomLevelChanged( float oldZoomLevel, float newZoomLevel )
    {
        statusBar.setZoomLevel( newZoomLevel );
    }
    
    public void makeAllWidgetsUsePixels()
    {
        int result = JOptionPane.showConfirmDialog( getMainWindow(), "Do you really want to convert all Widgets' positions and sizes to be absolute pixels?", "Convert all Widgets' coordinates", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
        if ( result == JOptionPane.YES_OPTION )
        {
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
                widgetsConfig.getWidget( i ).setAllPosAndSizeToPixels();
            
            setDirtyFlag();
        }
    }
    
    public void makeAllWidgetsUsePercents()
    {
        int result = JOptionPane.showConfirmDialog( getMainWindow(), "Do you really want to convert all Widgets' positions and sizes to be percents?", "Convert all Widgets' coordinates", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
        if ( result == JOptionPane.YES_OPTION )
        {
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
                widgetsConfig.getWidget( i ).setAllPosAndSizeToPercents();
            
            setDirtyFlag();
        }
    }
    
    public void copyPropertiesFromTemplate( Widget template, Widget target, boolean includeName, boolean includePosition )
    {
        FlatWidgetPropertiesContainer pcTemplate = new FlatWidgetPropertiesContainer();
        FlatWidgetPropertiesContainer pcTarget = new FlatWidgetPropertiesContainer();
        
        template.getProperties( pcTemplate, true );
        target.getProperties( pcTarget, true );
        
        List<Property> lstTemplate = pcTemplate.getList();
        List<Property> lstTarget = pcTarget.getList();
        
        // We assume, that the order will be the same in both lists.
        
        for ( int i = 0; i < lstTemplate.size(); i++ )
        {
            if ( lstTemplate.get( i ) instanceof Property )
            {
                Property p0 = lstTemplate.get( i );
                Property p1 = lstTarget.get( i );
                
                if ( ( includeName || !p0.getName().equals( "name" ) ) && ( includePosition || ( !p0.getName().equals( "x" ) && !p0.getName().equals( "y" ) && !p0.getName().equals( "positioning" ) ) ) )
                    p1.setValue( p0.getValue() );
            }
        }
    }
    
    public void copyPropertiesFromTemplate( Widget widget )
    {
        if ( templateConfig == null )
            return;
        
        Widget template = null;
        for ( int i = 0; i < templateConfig.getNumWidgets(); i++ )
        {
            if ( templateConfig.getWidget( i ).getClass() == widget.getClass() )
            {
                template = templateConfig.getWidget( i );
                break;
            }
        }
        
        if ( template == null )
            return;
        
        copyPropertiesFromTemplate( template, widget, false, false );
    }
    
    private static final Throwable getRootCause( Throwable t )
    {
        if ( t.getCause() == null )
            return ( t );
        
        return ( getRootCause( t.getCause() ) );
    }
    
    public static Widget createWidgetInstance( Class<Widget> widgetClass, WidgetsConfiguration widgetsConfig, boolean showMessage )// throws IllegalArgumentException, SecurityException
    {
        String name = ( widgetsConfig != null ) ? widgetsConfig.findFreeName( widgetClass.getSimpleName() ) : "";
        
        try
        {
            return ( (Widget)widgetClass.getConstructor( String.class ).newInstance( name ) );
        }
        catch ( Throwable t )
        {
            if ( showMessage )
                JOptionPane.showMessageDialog( null, "Cannot create Widget instance of type " + widgetClass.getName() + ". See log for more info." );
            else
                Logger.log( "Cannot create Widget instance of type " + widgetClass.getName() );
            
            Logger.log( getRootCause( t ) );
            
            return ( null );
        }
    }
    
    public Widget addNewWidget( Class<Widget> widgetClazz )
    {
        Widget widget = null;
        
        try
        {
            Logger.log( "Creating and adding new Widget of type \"" + widgetClazz.getSimpleName() + "\"..." );
            
            //widget = (Widget)widgetClazz.getConstructor( RelativePositioning.class, int.class, int.class, int.class, int.class ).newInstance( RelativePositioning.TOP_LEFT, 0, 0, 100, 100 );
            widget = createWidgetInstance( widgetClazz, widgetsConfig, false );
            if ( widget != null )
            {
                copyPropertiesFromTemplate( widget );
                __WCPrivilegedAccess.addWidget( widgetsConfig, widget, false );
                if ( presetsWindow.getDefaultScaleType() == ScaleType.PERCENTS )
                    widget.setAllPosAndSizeToPercents();
                else if ( presetsWindow.getDefaultScaleType() == ScaleType.ABSOLUTE_PIXELS )
                    widget.setAllPosAndSizeToPixels();
                
                int vpw = Math.min( editorScrollPane.getViewport().getExtentSize().width, gameResolution.getViewportWidth() );
                int vph = Math.min( editorScrollPane.getViewport().getExtentSize().height, gameResolution.getViewportHeight() );
                int x = editorScrollPane.getHorizontalScrollBar().getValue() + ( vpw - widget.getSize().getEffectiveWidth() ) / 2;
                int y = editorScrollPane.getVerticalScrollBar().getValue() + ( vph - widget.getSize().getEffectiveHeight() ) / 2;
                
                widget.getPosition().setEffectivePosition( x, y );
                editorPanel.setSelectedWidget( widget, false );
                
                setDirtyFlag();
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        return ( widget );
    }
    
    public void showPresetsWindow()
    {
        presetsWindow.setVisible( true );
    }
    
    public void showHelpWindow()
    {
        alwaysShowHelpOnStartup = HelpWindow.showHelpWindow( window, alwaysShowHelpOnStartup ).getAlwaysShowOnStartup();
    }
    
    private static void initTestGameData( LiveGameData gameData, EditorPresets editorPresets )
    {
        try
        {
            //InputStream in = new FileInputStream( "data/game_data/commentary_info" );
            InputStream in = LiveGameData.class.getResourceAsStream( "/data/game_data/commentary_info" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getCommentaryRequestInfo(), true );
            in.close();
            
            //in = new FileInputStream( "data/game_data/graphics_info" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/graphics_info" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getGraphicsInfo(), true );
            in.close();
            
            //in = new FileInputStream( "data/game_data/scoring_info" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/scoring_info" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getScoringInfo(), editorPresets );
            in.close();
            
            //in = new FileInputStream( "data/game_data/telemetry_data" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/telemetry_data" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getTelemetryData(), true );
            in.close();
            
            __GDPrivilegedAccess.applyEditorPresets( editorPresets, gameData );
        }
        catch ( IOException e )
        {
            throw new Error( e );
        }
    }
    
    private void initGameDataObjects()
    {
        int[] resolution = loadResolutionFromUserSettings();
        
        WidgetsDrawingManager drawingManager = new WidgetsDrawingManager( true, resolution[0], resolution[1], true );
        this.widgetsConfig = drawingManager;
        this.eventsManager = new GameEventsManager( null, drawingManager );
        this.gameData = new LiveGameData( drawingManager.getGameResolution(), eventsManager );
        eventsManager.setGameData( this.gameData );
        __GDPrivilegedAccess.updateProfileInfo( gameData.getProfileInfo() );
        eventsManager.setGameData( gameData );
        
        __GDPrivilegedAccess.setUpdatedInTimescope( gameData.getSetup() );
        __GDPrivilegedAccess.updateInfo( gameData );
        
        eventsManager.onSessionStarted( true );
        eventsManager.onTelemetryDataUpdated( true );
        eventsManager.onScoringInfoUpdated( true );
        
        __GDPrivilegedAccess.setRealtimeMode( true, gameData, true );
        
        loadEditorPresets();
        initTestGameData( gameData, presets );
    }
    
    private WidgetsEditorPanel createEditorPanel()
    {
        WidgetsDrawingManager drawingManager = (WidgetsDrawingManager)widgetsConfig;
        
        WidgetsEditorPanel editorPanel = new WidgetsEditorPanel( this, gameData, drawingManager );
        editorPanel.setPreferredSize( new Dimension( drawingManager.getGameResolution().getViewportWidth(), drawingManager.getGameResolution().getViewportHeight() ) );
        WidgetsEditorPanelInputHandler inputHandler = new WidgetsEditorPanelInputHandler( editorPanel, drawingManager );
        editorPanel.addMouseListener( inputHandler );
        editorPanel.addMouseMotionListener( inputHandler );
        editorPanel.addKeyListener( inputHandler );
        
        editorPanel.addWidgetsEditorPanelListener( this );
        
        return ( editorPanel );
    }
    
    public RFDynHUDEditor()
    {
        super();
        
        try
        {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch ( Throwable t )
        {
        }
        
        //ByteOrderInitializer.setByteOrder( 3, 2, 1, 0 );
        ByteOrderInitializer.setByteOrder( 0, 1, 2, 3 );
        
        __GameIDHelper.gameId = SupportedGames.rFactor; // make this dynamic somehow!
        
        initGameDataObjects();
        
        this.window = new JFrame( BASE_WINDOW_TITLE );
        
        window.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        
        this.menuBar = new EditorMenuBar( this );
        window.setJMenuBar( menuBar );
        
        Container contentPane = window.getContentPane();
        contentPane.setLayout( new BorderLayout() );
        
        this.editorPanel = createEditorPanel();
        this.gameResolution = widgetsConfig.getGameResolution();
        
        editorScrollPane = new JScrollPane( editorPanel );
        editorScrollPane.getHorizontalScrollBar().setUnitIncrement( 20 );
        editorScrollPane.getVerticalScrollBar().setUnitIncrement( 20 );
        editorScrollPane.getViewport().setScrollMode( JViewport.SIMPLE_SCROLL_MODE );
        editorScrollPane.setPreferredSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
        
        JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        split.setOneTouchExpandable( true );
        split.setResizeWeight( 1 );
        split.add( editorScrollPane );
        
        this.propsEditor = new PropertiesEditor();
        this.propsEditor.addChangeListener( new WidgetPropertyChangeListener( this ) );
        
        JSplitPane split2 = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        split2.setResizeWeight( 0 );
        split2.setPreferredSize( new Dimension( 300, Integer.MAX_VALUE ) );
        split2.setMinimumSize( new Dimension( 300, 10 ) );
        
        editorTable = new PropertiesEditorTable( this, propsEditor );
        
        split2.add( editorTable.createScrollPane() );
        
        editorTable.addPropertySelectionListener( this );
        
        split.resetToPreferredSizes();
        
        docPanel = new JEditorPane( "text/html", "" );
        ( (HTMLDocument)docPanel.getDocument() ).getStyleSheet().importStyleSheet( RFDynHUDEditor.class.getClassLoader().getResource( "net/ctdp/rfdynhud/editor/properties/doc.css" ) );
        docPanel.setEditable( false );
        docPanel.setAutoscrolls( false );
        JScrollPane sp = new JScrollPane( docPanel );
        sp.setMinimumSize( new Dimension( 300, 10 ) );
        split2.add( sp );
        split2.setDividerLocation( 450 );
        
        split2.resetToPreferredSizes();
        split2.setContinuousLayout( true );
        split.setContinuousLayout( true );
        
        split.add( split2 );
        contentPane.add( split, BorderLayout.CENTER );
        
        this.runningIndicator = new EditorRunningIndicator( this );
        
        this.statusBar = new EditorStatusBar( runningIndicator );
        
        contentPane.add( statusBar, BorderLayout.SOUTH );
        
        this.presetsWindow = new EditorPresetsWindow( this );
        
        window.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                onCloseRequested();
            }
        } );
        
        window.addComponentListener( new ComponentAdapter()
        {
            @Override    
            public void componentMoved( ComponentEvent e )
            {
                if ( getMainWindow().getExtendedState() == JFrame.NORMAL )
                {
                    windowLeft = getMainWindow().getX();
                    windowTop = getMainWindow().getY();
                }
            }
            
            @Override
            public void componentResized( ComponentEvent e )
            {
                if ( getMainWindow().getExtendedState() == JFrame.NORMAL )
                {
                    windowWidth = getMainWindow().getWidth();
                    windowHeight = getMainWindow().getHeight();
                }
            }
        } );
    }
    
    public static void main( String[] args )
    {
        try
        {
            //Logger.setStdStreams();
            
            final RFDynHUDEditor editor = new RFDynHUDEditor();
            
            Rectangle screenBounds = GUITools.getCurrentScreenBounds();
            
            editor.getMainWindow().setBounds( screenBounds );
            editor.windowLeft = editor.getMainWindow().getX();
            editor.windowTop = editor.getMainWindow().getY();
            editor.windowWidth = editor.getMainWindow().getWidth();
            editor.windowHeight = editor.getMainWindow().getHeight();
            editor.getMainWindow().setExtendedState( JFrame.MAXIMIZED_BOTH );
            
            Object[] result = editor.loadUserSettings();
            
            if ( !(Boolean)result[0] )
            {
                if ( editor.getEditorPanel().getSettings().checkResolution( screenBounds.width, screenBounds.height ) )
                    editor.getEditorPanel().switchToGameResolution( screenBounds.width, screenBounds.height );
            }
            
            if ( editor.currentConfigFile == null )
            {
                File configFile = (File)result[1];
                if ( configFile == null )
                    configFile = new File( GameFileSystem.INSTANCE.getConfigFolder(), "overlay.ini" );
                if ( configFile.exists() )
                    editor.openConfig( configFile );
                else
                    editor.loadFallbackConfig();
            }
            
            editor.eventsManager.onRealtimeEntered( true );
            
            //editor.getEditorPanel().getWidgetsDrawingManager().collectTextures( true, editor.gameData );
            
            if ( (Boolean)result[0] )
                editor.getEditorPanel().initBackgroundImage();
            
            editor.getMainWindow().addWindowListener( new WindowAdapter()
            {
                @Override
                public void windowOpened( WindowEvent e )
                {
                    ( (JFrame)e.getSource() ).removeWindowListener( this );
                    
                    if ( editor.alwaysShowHelpOnStartup )
                    {
                        editor.alwaysShowHelpOnStartup = HelpWindow.showHelpWindow( editor.window, true ).getAlwaysShowOnStartup();
                    }
                }
            } );
            
            editor.getMainWindow().setVisible( true );
            
            if ( editor.presetsWindowVisible )
                editor.presetsWindow.setVisible( true );
            
            while ( editor.getMainWindow().isVisible() )
            {
                try { Thread.sleep( 50L ); } catch ( InterruptedException e ) { e.printStackTrace(); }
            }
            
            //System.exit( 0 );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            String message = t.getMessage();
            if ( ( message == null ) || ( message.length() == 0 ) )
                message = t.getClass().getSimpleName() + ", " + t.getStackTrace()[0] + "\n" + "Please see the editor log file for more info.";
            
            JOptionPane.showMessageDialog( null, message, "Error running the rfDynHUD Editor", JOptionPane.ERROR_MESSAGE );
        }
    }
}
