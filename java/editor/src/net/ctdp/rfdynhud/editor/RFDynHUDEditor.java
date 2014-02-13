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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.WidgetImportManager.ImportDecision;
import net.ctdp.rfdynhud.editor.commandline.EditorArguments;
import net.ctdp.rfdynhud.editor.commandline.EditorArgumentsHandler;
import net.ctdp.rfdynhud.editor.commandline.EditorArgumentsRegistry;
import net.ctdp.rfdynhud.editor.director.DirectorManager;
import net.ctdp.rfdynhud.editor.help.HelpWindow;
import net.ctdp.rfdynhud.editor.hiergrid.GridItemsContainer;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTable;
import net.ctdp.rfdynhud.editor.hiergrid.PropertySelectionListener;
import net.ctdp.rfdynhud.editor.input.InputBindingsGUI;
import net.ctdp.rfdynhud.editor.live.LiveCommunicator;
import net.ctdp.rfdynhud.editor.live.SimulationControls;
import net.ctdp.rfdynhud.editor.live.SimulationPlaybackControl;
import net.ctdp.rfdynhud.editor.presets.EditorPresetsWindow;
import net.ctdp.rfdynhud.editor.presets.ScaleType;
import net.ctdp.rfdynhud.editor.properties.DefaultPropertiesContainer;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditor;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditorTableModel;
import net.ctdp.rfdynhud.editor.properties.WidgetPropertyChangeListener;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.editor.util.DefaultPropertyWriter;
import net.ctdp.rfdynhud.editor.util.EditorPropertyLoader;
import net.ctdp.rfdynhud.editor.util.OverlayFileFilter;
import net.ctdp.rfdynhud.editor.util.SaveAsDialog;
import net.ctdp.rfdynhud.editor.util.StrategyTool;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata._LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.plugins.simulation.SimulationPlayer;
import net.ctdp.rfdynhud.properties.AbstractPropertiesKeeper;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.PropertiesContainer;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.util.PropertyWriter;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.__UtilHelper;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.base.widget.AbstractAssembledWidget;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetFactory;
import net.ctdp.rfdynhud.widgets.base.widget.__WPrivilegedAccess;

import org.jagatoo.commandline.ArgumentsRegistry;
import org.jagatoo.commandline.CommandlineParser;
import org.jagatoo.commandline.CommandlineParsingException;
import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.gui.awt_swing.GUITools;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniWriter;
import org.jagatoo.util.strings.StringUtils;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RFDynHUDEditor implements WidgetsEditorPanelListener, PropertySelectionListener<Property>
{
    static
    {
        __EDPrivilegedAccess.editorClassLoader = RFDynHUDEditor.class.getClassLoader();
        
        //ByteOrderInitializer.setByteOrder( 3, 2, 1, 0 );
        ByteOrderInitializer.setByteOrder( 0, 1, 2, 3 );
    }
    
    private static final String BASE_WINDOW_TITLE = "rFactor dynamic HUD Editor v" + RFDynHUD.VERSION.toString();
    
    public static String GAME_ID = null;
    public static GameFileSystem FILESYSTEM = null;
    
    private LiveGameData gameData;
    private WidgetsDrawingManager drawingManager;
    private WidgetsConfiguration widgetsConfig;
    private GameEventsManager eventsManager;
    private GameResolution gameResolution;
    private Property gameResProp = new Property( "resolution", true, PropertyEditorType.STRING )
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
        public Object getDefaultValue()
        {
            return ( null );
        }
        
        @Override
        public boolean hasDefaultValue()
        {
            return ( false );
        }
        
        @Override
        public void loadValue( PropertyLoader loader, String value )
        {
        }
    };
    
    private boolean alwaysShowHelpOnStartup = true;
    private boolean watchedWaitFor60Seconds = false;
    
    private JFrame window;
    private int windowLeft, windowTop;
    private int windowWidth, windowHeight;
    private EditorMenuBar menuBar;
    private WidgetsEditorPanel editorPanel;
    private JScrollPane editorScrollPane;
    
    JPanel fullscreenPanel = null;
    
    private JSplitPane mainSplit;
    private JSplitPane propsSplit;
    
    private JTabbedPane tabDirector;
    private DirectorManager directorMgr = null;
    private File currentDirectorStatesSetsFile = null;
    private String directorConnectionStrings = null;
    
    private LiveCommunicator liveMgr = null;
    
    private String liveConnectionStrings = null;
    private File currentSimulationFile = null;
    
    private PropertiesEditor propsEditor;
    private HierarchicalTable<Property> editorTable;
    private JEditorPane docPanel;
    private boolean isSomethingDoced = false;
    
    private EditorStatusBar statusBar;
    
    private EditorPresets presets = new EditorPresets();
    
    private EditorRunningIndicator runningIndicator;
    
    public static final String doc_header = StringUtils.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_header.html" ) );
    public static final String doc_footer = StringUtils.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_footer.html" ) );
    
    private boolean presetsWindowVisible = false;
    EditorPresetsWindow presetsWindow;
    
    private boolean dirtyFlag = false;
    
    private File currentConfigFile = null;
    private File lastImportFile = null;
    private File currentTemplateFile = null;
    
    private WidgetsConfiguration templateConfig = null;
    private long lastTemplateConfigModified = -1L;
    
    private Property templateConfigProp;
    
    ImportDecision lastImportDecision = ImportDecision.USE_DESTINATION_ALIASES;
    
    public final LiveGameData getGameData()
    {
        return ( gameData );
    }
    
    public final GameEventsManager getGameEventsManager()
    {
        return ( eventsManager );
    }
    
    public final GameResolution getGameResolution()
    {
        return ( gameResolution );
    }
    
    public final WidgetsConfiguration getWidgetsConfiguration()
    {
        return ( widgetsConfig );
    }
    
    public final DirectorManager getDirectorManager()
    {
        return ( directorMgr );
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
    
    public void repaintEditorPanel()
    {
        if ( fullscreenPanel == null )
            editorPanel.repaint();
        else
            fullscreenPanel.repaint();
    }
    
    public final TextureImage2D getOverlayTexture()
    {
        return ( getEditorPanel().getOverlayTexture() );
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
                    String helpText = editorPanel.getSelectedWidget().getDocumentationSource();
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
                docPanel.setText( doc_header + property.getDocumentationSource() + doc_footer );
            }
            else
            {
                docPanel.setText( doc_header + property.getDocumentationSource() + doc_footer );
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
    
    private void fillConfigurationFiles( File folder, List<String> list )
    {
        for ( File f : folder.listFiles() )
        {
            if ( f.isFile() && f.getName().toLowerCase().endsWith( ".ini" ) )
            {
                if ( checkConfigFile( f ) )
                    list.add( f.getAbsolutePath().substring( gameData.getFileSystem().getConfigPath().length() + 1 ) );
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
    
    private List<String> getConfigurationFiles()
    {
        List<String> list = new ArrayList<String>();
        
        list.add( "<none>" );
        
        fillConfigurationFiles( gameData.getFileSystem().getConfigFolder(), list );
        
        return ( list );
    }
    
    private String getCurrentTemplateFileForProperty()
    {
        if ( currentTemplateFile == null )
            return ( "<none>" );
        
        return ( currentTemplateFile.getAbsolutePath().substring( gameData.getFileSystem().getConfigPath().length() + 1 ) );
    }
    
    private void getProperties( PropertiesContainer propsCont )
    {
        propsCont.addGroup( "Editor - General" );
        
        propsCont.addProperty( gameResProp );
        
        getEditorPanel().getSettings().getProperties( propsCont, true );
        
        propsCont.addProperty( templateConfigProp );
        
        propsCont.addGroup( "Configuration - Global" );
        
        widgetsConfig.getProperties( propsCont, false );
    }
    
    private final Map<String, Boolean> expandedRows = new HashMap<String, Boolean>();
    
    @Override
    public void onWidgetSelected( Widget widget, boolean selectionChanged, boolean doubleClick )
    {
        GridItemsContainer<Property> propsList = propsEditor.getPropertiesList();
        
        expandedRows.clear();
        PropertiesEditorTableModel.readExpandFlags( PropertiesEditorTableModel.ITEMS_HANDLER, propsList, expandedRows );
        
        propsEditor.clear();
        
        if ( widget == null )
        {
            this.getProperties( new DefaultPropertiesContainer( propsList ) );
        }
        else
        {
            widget.getProperties( new DefaultPropertiesContainer( propsList ), false );
        }
        
        onPropertySelected( null, -1 );
        PropertiesEditorTableModel.restoreExpandFlags( PropertiesEditorTableModel.ITEMS_HANDLER, propsList, expandedRows );
        
        editorTable.applyToModel();
    }
    
    @Override
    public void onScopeWidgetChanged( AbstractAssembledWidget scopeWidget )
    {
    }
    
    /**
     * @param widget the changed widget
     * @param propertyName the name of the changed property
     * @param isPosSize is position or size?
     */
    public void onWidgetChanged( Widget widget, String propertyName, boolean isPosSize )
    {
        repaintEditorPanel();
        
        setDirtyFlag();
        
        if ( ( widget != null ) && ( widget == getEditorPanel().getSelectedWidget() ) )
        {
            //editorTable.apply();
            //onWidgetSelected( widget, false );
            
            PropertiesEditorTableModel m = (PropertiesEditorTableModel)editorTable.getModel();
            int keyCol = m.getFirstNonExpanderColumn() + 0;
            int valCol = m.getFirstNonExpanderColumn() + 1;
            int rc = m.getRowCount();
            for ( int i = 0; i < rc; i++ )
            {
                if ( m.isDataRow( i ) )
                {
                    String pn = (String)m.getValueAt( i, keyCol );
                    if ( pn.equals( "positioning" ) || pn.equals( "x" ) || pn.equals( "y" ) || pn.equals( "width" ) || pn.equals( "height" ) )
                        m.fireTableCellUpdated( i, valCol );
                }
                else
                {
                    // For some reason group headers need to be fully updated.
                    m.fireTableRowsUpdated( i, i );
                }
            }
        }
    }
    
    private File getEditorSettingsFile( GameFileSystem fileSystem )
    {
        /*
        File dir = new File( System.getProperty( "user.home" ) );
        
        dir = new File( dir, "CTDP/.rfdynhud" );
        if ( !dir.exists() )
            dir.mkdirs();
        */
        //File dir = RFactorTools.EDITOR_FOLDER;
        File dir = fileSystem.getConfigFolder();
        
        return ( new File( dir, "editor_settings.ini" ) );
    }
    
    private File getEditorSettingsFile()
    {
        return ( getEditorSettingsFile( gameData.getFileSystem() ) );
    }
    
    private void writeLastConfig( IniWriter writer ) throws Throwable
    {
        if ( ( currentConfigFile != null ) && currentConfigFile.exists() )
        {
            String currentConfigFilename = currentConfigFile.getAbsolutePath();
            if ( currentConfigFilename.startsWith( gameData.getFileSystem().getConfigPath() ) )
            {
                if ( currentConfigFilename.charAt( gameData.getFileSystem().getConfigPath().length() ) == File.separatorChar )
                    currentConfigFilename = currentConfigFilename.substring( gameData.getFileSystem().getConfigPath().length() + 1 );
                else
                    currentConfigFilename = currentConfigFilename.substring( gameData.getFileSystem().getConfigPath().length() );
            }
            
            writer.writeSetting( "lastConfig", currentConfigFilename );
        }
    }
    
    private void writeLastDirectorStatesFile( IniWriter writer ) throws Throwable
    {
        File file = currentDirectorStatesSetsFile;
        if ( ( directorMgr != null ) && ( directorMgr.getCurrentFile() != null ) && directorMgr.getCurrentFile().exists() )
            file = directorMgr.getCurrentFile();
        
        if ( ( file != null ) && file.exists() )
        {
            String filename = file.getAbsolutePath();
            if ( filename.startsWith( gameData.getFileSystem().getConfigPath() ) )
            {
                if ( filename.charAt( gameData.getFileSystem().getConfigPath().length() ) == File.separatorChar )
                    filename = filename.substring( gameData.getFileSystem().getConfigPath().length() + 1 );
                else
                    filename = filename.substring( gameData.getFileSystem().getConfigPath().length() );
            }
            
            writer.writeSetting( "lastStatesSetsFile", filename );
        }
    }
    
    private void writeLastSimulationFile( IniWriter writer ) throws Throwable
    {
        File file = currentSimulationFile;
        
        if ( ( file != null ) && file.exists() )
        {
            String filename = file.getAbsolutePath();
            if ( filename.startsWith( gameData.getFileSystem().getGamePath() ) )
            {
                if ( filename.charAt( gameData.getFileSystem().getGamePath().length() ) == File.separatorChar )
                    filename = filename.substring( gameData.getFileSystem().getGamePath().length() + 1 );
                else
                    filename = filename.substring( gameData.getFileSystem().getGamePath().length() );
            }
            
            writer.writeSetting( "lastFile", filename );
        }
    }
    
    private void writeLastImportFile( IniWriter writer ) throws Throwable
    {
        if ( lastImportFile != null )
        {
            String filename = lastImportFile.getAbsolutePath();
            if ( filename.startsWith( gameData.getFileSystem().getConfigPath() ) )
            {
                if ( filename.charAt( gameData.getFileSystem().getConfigPath().length() ) == File.separatorChar )
                    filename = filename.substring( gameData.getFileSystem().getConfigPath().length() + 1 );
                else
                    filename = filename.substring( gameData.getFileSystem().getConfigPath().length() );
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
            PropertyWriter confWriter = new DefaultPropertyWriter( writer, false );
            
            writer.writeGroup( "General" );
            writer.writeSetting( "resolution", gameResolution.getResolutionString() );
            getEditorPanel().getSettings().saveProperties( confWriter );
            writer.writeSetting( "templatesConfig", getCurrentTemplateFileForProperty() );
            writer.writeSetting( "defaultScaleType", presetsWindow.getDefaultScaleType() );
            writeLastConfig( writer );
            writeLastImportFile( writer );
            writer.writeSetting( "alwaysShowHelpOnStartup", alwaysShowHelpOnStartup );
            writer.writeSetting( "waitedWithHelp", watchedWaitFor60Seconds ? "yes_we_can!" : false );
            writer.writeSetting( "lastImportDecision", lastImportDecision );
            
            writer.writeGroup( "MainWindow" );
            writer.writeSetting( "windowLocation", windowLeft + "x" + windowTop );
            writer.writeSetting( "windowSize", windowWidth + "x" + windowHeight );
            writer.writeSetting( "windowState", extendedState );
            writer.writeSetting( "mainSplit", mainSplit.getDividerLocation() );
            writer.writeSetting( "propsSplit", propsSplit.getDividerLocation() );
            
            writer.writeGroup( "PresetsWindow" );
            writer.writeSetting( "windowLocation", presetsWindow.getX() + "x" + presetsWindow.getY() );
            writer.writeSetting( "windowSize", presetsWindow.getWidth() + "x" + presetsWindow.getHeight() );
            writer.writeSetting( "windowVisible", presetsWindowVisible );
            writer.writeSetting( "autoApply", presetsWindow.getAutoApply() );
            
            writer.writeGroup( "EditorPresets" );
            __EDPrivilegedAccess.saveProperties( presets, confWriter );
            
            writer.writeGroup( "Director" );
            
            writeLastDirectorStatesFile( writer );
            
            if ( directorConnectionStrings != null )
                writer.writeSetting( "connectionStrings", directorConnectionStrings );
            
            writer.writeGroup( "Live" );
            
            if ( liveConnectionStrings != null )
                writer.writeSetting( "connectionStrings", liveConnectionStrings );
            
            writer.writeGroup( "Simulation" );
            
            writeLastSimulationFile( writer );
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
    
    private int[] loadResolutionFromUserSettings( GameFileSystem fileSystem )
    {
        File userSettingsFile = getEditorSettingsFile( fileSystem );
        
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
                RFDHLog.exception( t );
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
            RFDHLog.exception( t );
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
                                loadTemplateConfig( new File( gameData.getFileSystem().getConfigFolder(), value ) );
                            }
                            catch ( IOException e )
                            {
                                RFDHLog.exception( e );
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
                                configFile = new File( gameData.getFileSystem().getConfigFolder(), value );
                            if ( configFile.exists() )
                                //openConfig( configFile );
                                result[1] = configFile;
                        }
                        else if ( key.equals( "lastImportFile" ) )
                        {
                            File configFile = new File( value );
                            if ( !configFile.isAbsolute() )
                                configFile = new File( gameData.getFileSystem().getConfigFolder(), value );
                            
                            lastImportFile = configFile;
                        }
                        else if ( key.equals( "alwaysShowHelpOnStartup" ) )
                        {
                            alwaysShowHelpOnStartup = Boolean.parseBoolean( value );
                        }
                        else if ( key.equals( "waitedWithHelp" ) )
                        {
                            watchedWaitFor60Seconds = value.equals( "yes_we_can!" );
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
                        else if ( key.equals( "mainSplit" ) )
                        {
                            try
                            {
                                mainSplit.setDividerLocation( Integer.parseInt( value ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "propsSplit" ) )
                        {
                            try
                            {
                                propsSplit.setDividerLocation( Integer.parseInt( value ) );
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
                    else if ( group.equals( "Director" ) )
                    {
                        if ( key.equals( "lastStatesSetsFile" ) )
                        {
                            File file = new File( value );
                            if ( !file.isAbsolute() )
                                currentDirectorStatesSetsFile = new File( gameData.getFileSystem().getConfigFolder(), value );
                            if ( file.exists() )
                                currentDirectorStatesSetsFile = file;
                        }
                        else if ( key.equals( "connectionStrings" ) )
                        {
                            directorConnectionStrings = value;
                        }
                    }
                    else if ( group.equals( "Live" ) )
                    {
                        if ( key.equals( "connectionStrings" ) )
                        {
                            liveConnectionStrings = value;
                        }
                    }
                    else if ( group.equals( "Simulation" ) )
                    {
                        if ( key.equals( "lastFile" ) )
                        {
                            currentSimulationFile = new File( value );
                        }
                    }
                    
                    return ( true );
                }
            }.parse( userSettingsFile );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
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
            this.templateConfig = new WidgetsConfiguration( gameData.getGameResolution().getViewportWidth(), gameData.getGameResolution().getViewportHeight() );
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader( null ), templateConfigFile, templateConfig, gameData, true );
            
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
        __UtilPrivilegedAccess.loadFactoryDefaults( new ConfigurationLoader( null ), widgetsConfig, gameData, true );
        
        getOverlayTexture().clear( true, null );
        editorPanel.setSelectedWidget( null, false );
        repaintEditorPanel();
    }
    
    public void openConfig( File configFile )
    {
        try
        {
            editorPanel.goInto( null );
            
            clearWidetRegions();
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader( null ), configFile, widgetsConfig, gameData, true );
            
            currentConfigFile = configFile;
            
            updateWindowTitle();
            
            getOverlayTexture().clear( true, null );
            editorPanel.setSelectedWidget( null, false );
            repaintEditorPanel();
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            JOptionPane.showMessageDialog( window, ( t.getMessage() != null ) ? t.getMessage() : t.getClass().getSimpleName(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE );
        }
    }
    
    private File getOpenConfigFile( File initialFile )
    {
        JFileChooser fc = new JFileChooser();
        if ( initialFile == null )
        {
            fc.setCurrentDirectory( gameData.getFileSystem().getConfigFolder() );
            fc.setSelectedFile( new File( gameData.getFileSystem().getConfigFolder(), "overlay.ini" ) );
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
        fc.setFileFilter( new OverlayFileFilter() );
        
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
            RFDHLog.exception( t );
            
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
            
            WidgetsDrawingManager wdm = new WidgetsDrawingManager( true, designResolution[0], designResolution[1] );
            WidgetsEditorPanel ep = new WidgetsEditorPanel( null, this, gameData, wdm );
            ImportWidgetsEditorPanelInputHandler inputHandler = new ImportWidgetsEditorPanelInputHandler( this, ep, wdm.getWidgetsConfiguration() );
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
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader( null ), file, wdm.getWidgetsConfiguration(), gameData, true );
            
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
            repaintEditorPanel();
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            
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
            RFDHLog.exception( t );
            
            JOptionPane.showMessageDialog( window, ( t.getMessage() != null ) ? t.getMessage() : t.getClass().getSimpleName(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE );
            
            return ( false );
        }
    }
    
    public File saveConfigAs()
    {
        SaveAsDialog sad = new SaveAsDialog( this, gameData.getModInfo() );
        sad.setSelectedFile( gameData.getFileSystem(), currentConfigFile );
        
        sad.setVisible( true );
        
        if ( sad.getSelectedFile( gameData.getFileSystem() ) == null )
            return ( null );
        
        currentConfigFile = sad.getSelectedFile( gameData.getFileSystem() );
        
        try
        {
            currentConfigFile.getParentFile().mkdirs();
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
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
        
        if ( ( directorMgr != null ) && directorMgr.isConnected() )
        {
            if ( !directorMgr.checkUnsavedChanges() )
                return;
            
            directorMgr.close();
            
            try
            {
                Thread.sleep( 200L );
            }
            catch ( Throwable t )
            {
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
        if ( HelpWindow.instance != null )
            HelpWindow.instance.dispose();
        System.exit( 0 );
    }
    
    @Override
    public void onWidgetPositionSizeChanged( Widget widget )
    {
        onWidgetChanged( widget, null, true );
    }
    
    @Override
    public Widget onWidgetCopyRequested( Widget widget )
    {
        String widgetType = widget.getClass().getSimpleName();
        
        RFDHLog.println( "Copying Widget of type \"" + widgetType + "\"..." );
        
        String name = widgetsConfig.findFreeName( widgetType );
        
        Widget newWidget = widget.clone();
        
        if ( newWidget == null )
            return ( null );
        
        newWidget.setName( name );
        
        __WCPrivilegedAccess.addWidget( widgetsConfig, newWidget, false, gameData );
        
        return ( newWidget );
    }
    
    @Override
    public boolean onWidgetRemoved( Widget widget )
    {
        if ( widget == null )
            return ( false );
        
        RFDHLog.println( "Removing Widget of type \"" + widget.getClass().getName() + "\" and name \"" + widget.getName() + "\"..." );
        
        if ( widget.getMasterWidget() == null )
            __WCPrivilegedAccess.removeWidget( widgetsConfig, widget, gameData );
        else
            __WPrivilegedAccess.removePart( widget, widget.getMasterWidget(), gameData );
        
        setDirtyFlag();
        
        return ( true );
    }
    
    @Override
    public void onContextMenuRequested( Widget[] hoveredWidgets, AbstractAssembledWidget scopeWidget )
    {
        EditorMenuBar.initContextMenu( this, hoveredWidgets, scopeWidget );
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
    
    public static Widget createWidgetInstance( Class<Widget> widgetClass, String name, boolean showMessage )
    {
        Widget widget = WidgetFactory.createWidget( widgetClass, name );
        
        if ( widget == null )
        {
            if ( showMessage )
                JOptionPane.showMessageDialog( null, "Cannot create Widget instance of type " + widgetClass.getName() + ". See log for more info." );
            else
                RFDHLog.error( "Cannot create Widget instance of type " + widgetClass.getName() );
        }
        
        return ( widget );
    }
    
    private Widget getNewWidgetInstance( Class<Widget> widgetClazz )
    {
        Widget template = null;
        
        if ( templateConfig != null )
        {
            for ( int i = 0; i < templateConfig.getNumWidgets(); i++ )
            {
                if ( templateConfig.getWidget( i ).getClass() == widgetClazz )
                {
                    template = templateConfig.getWidget( i );
                    break;
                }
            }
        }
        
        String name = widgetsConfig.findFreeName( widgetClazz.getSimpleName() );
        
        if ( template == null )
            return ( createWidgetInstance( widgetClazz, name, false ) );
        
        Widget clone = template.clone();
        clone.setName( name );
        
        return ( clone );
    }
    
    public Widget addNewWidget( Class<Widget> widgetClazz )
    {
        if ( AbstractAssembledWidget.class.isAssignableFrom( widgetClazz ) && ( editorPanel.getScopeWidget() != null ) )
        {
            JOptionPane.showMessageDialog( getMainWindow(), "You cannot add an AssembledWidget to an AssembledWidget", "Error adding new Widget", JOptionPane.ERROR_MESSAGE );
            
            return ( null );
        }
        
        Widget widget = null;
        
        try
        {
            RFDHLog.println( "Creating and adding new Widget of type \"" + widgetClazz.getSimpleName() + "\"..." );
            
            widget = getNewWidgetInstance( widgetClazz );
            if ( widget != null )
            {
                int w = 0;
                int h = 0;
                
                if ( editorPanel.getScopeWidget() != null )
                {
                    String name = editorPanel.getScopeWidget().findFreePartName( widget.getClass().getSimpleName() );
                    widget.setName( "dummy" + System.currentTimeMillis() );
                    __WCPrivilegedAccess.addWidget( widgetsConfig, widget, false, gameData );
                    w = widget.getSize().getEffectiveWidth() - widget.getBorder().getInnerLeftWidth() - widget.getBorder().getInnerRightWidth();
                    h = widget.getSize().getEffectiveHeight() - widget.getBorder().getInnerTopHeight() - widget.getBorder().getInnerBottomHeight();
                    __WCPrivilegedAccess.removeWidget( widgetsConfig, widget, gameData );
                    widget.setName( name );
                }
                
                if ( editorPanel.getScopeWidget() == null )
                    __WCPrivilegedAccess.addWidget( widgetsConfig, widget, false, gameData );
                else
                    __WPrivilegedAccess.addPart( widget, editorPanel.getScopeWidget(), gameData );
                if ( presetsWindow.getDefaultScaleType() == ScaleType.PERCENTS )
                    widget.setAllPosAndSizeToPercents();
                else if ( presetsWindow.getDefaultScaleType() == ScaleType.ABSOLUTE_PIXELS )
                    widget.setAllPosAndSizeToPixels();
                
                if ( editorPanel.getScopeWidget() == null )
                {
                    int vpw = Math.min( editorScrollPane.getViewport().getExtentSize().width, gameResolution.getViewportWidth() );
                    int vph = Math.min( editorScrollPane.getViewport().getExtentSize().height, gameResolution.getViewportHeight() );
                    int x = editorScrollPane.getHorizontalScrollBar().getValue() + ( vpw - widget.getSize().getEffectiveWidth() ) / 2;
                    int y = editorScrollPane.getVerticalScrollBar().getValue() + ( vph - widget.getSize().getEffectiveHeight() ) / 2;
                    
                    widget.getPosition().setEffectivePosition( x, y );
                }
                else
                {
                    int sw = editorPanel.getScopeWidget().getInnerSize().getEffectiveWidth();
                    int sh = editorPanel.getScopeWidget().getInnerSize().getEffectiveHeight();
                    
                    widget.getSize().setEffectiveSize( w, h );
                    
                    sw = editorPanel.getScopeWidget().getInnerSize().getEffectiveWidth();
                    sh = editorPanel.getScopeWidget().getInnerSize().getEffectiveHeight();
                    int x = ( sw - widget.getSize().getEffectiveWidth() ) / 2;
                    int y = ( sh - widget.getSize().getEffectiveHeight() ) / 2;
                    widget.getPosition().setEffectivePosition( x, y );
                }
                
                editorPanel.setSelectedWidget( widget, false );
                
                setDirtyFlag();
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        return ( widget );
    }
    
    public void showFullscreenPreview()
    {
        PreviewAndScreenshotManager.showFullscreenPreview( getMainWindow(), getEditorPanel(), getGameResolution(), getCurrentConfigFile() );
    }
    
    public void takeScreenshot()
    {
        PreviewAndScreenshotManager.takeScreenshot( getEditorPanel(), getGameResolution(), getCurrentConfigFile() );
    }
    
    public void showStrategyTool()
    {
        StrategyTool.showStrategyTool( getMainWindow(), gameData );
    }
    
    public void mergeDirectorConnectionString( String connectionString )
    {
        if ( this.directorConnectionStrings == null )
        {
            this.directorConnectionStrings = connectionString;
        }
        else
        {
            String[] connStrings = this.directorConnectionStrings.split( ";" );
            
            String newConnStrings = "";
            for ( int i = 0; i < connStrings.length; i++ )
            {
                if ( !connStrings[i].equalsIgnoreCase( connectionString ) )
                    newConnStrings += ";" + connStrings[i];
            }
            this.directorConnectionStrings = connectionString + newConnStrings;
        }
    }
    
    public boolean switchToDirectorMode()
    {
        int divLoc = mainSplit.getDividerLocation();
        
        if ( directorMgr == null )
            directorMgr = new DirectorManager( this );
        
        if ( ( currentDirectorStatesSetsFile != null ) && currentDirectorStatesSetsFile.exists() )
        {
            directorMgr.loadStatesSets( currentDirectorStatesSetsFile );
        }
        
        tabDirector.removeAll();
        tabDirector.addTab( "Director", directorMgr.getComponent() );
        tabDirector.addTab( "Main", propsSplit );
        tabDirector.setSelectedIndex( 0 );
        
        mainSplit.setRightComponent( tabDirector );
        
        mainSplit.setDividerLocation( divLoc );
        ( (JSplitPane)directorMgr.getComponent() ).setDividerLocation( propsSplit.getDividerLocation() );
        
        if ( !directorMgr.startDirecting( directorConnectionStrings ) )
        {
            switchToEditorMode();
            
            return ( false );
        }
        
        return ( true );
    }
    
    public void mergeLiveConnectionString( String connectionString )
    {
        if ( this.liveConnectionStrings == null )
        {
            this.liveConnectionStrings = connectionString;
        }
        else
        {
            String[] connStrings = this.liveConnectionStrings.split( ";" );
            
            String newConnStrings = "";
            for ( int i = 0; i < connStrings.length; i++ )
            {
                if ( !connStrings[i].equalsIgnoreCase( connectionString ) )
                    newConnStrings += ";" + connStrings[i];
            }
            this.liveConnectionStrings = connectionString + newConnStrings;
        }
    }
    
    public boolean switchToLiveMode()
    {
        if ( liveMgr == null )
            liveMgr = new LiveCommunicator( this, eventsManager );
        
        if ( !liveMgr.startLiveMode( liveConnectionStrings ) )
        {
            switchToEditorMode();
            
            return ( false );
        }
        
        return ( true );
    }
    
    private volatile boolean simulating = false;
    private volatile boolean simulating2 = false;
    
    private SimulationControls simControls = null;
    
    public void switchToSimulationMode()
    {
        SimulationPlaybackControl control = new SimulationPlaybackControl()
        {
            @Override
            public boolean isCancelled()
            {
                return ( cancelled || !simulating );
            }
        };
        
        int divLoc = mainSplit.getDividerLocation();
        
        if ( simControls == null )
            simControls = new SimulationControls( this, control );
        
        simControls.setFile( currentSimulationFile );
        
        mainSplit.setRightComponent( simControls );
        
        mainSplit.setDividerLocation( divLoc );
    }
    
    public boolean startSimulation( final File file, final SimulationPlaybackControl control )
    {
        if ( file == null )
        {
            JOptionPane.showMessageDialog( getMainWindow(), "No file given", "Error", JOptionPane.ERROR_MESSAGE );
            return ( false );
        }
        
        if ( !file.exists() )
        {
            JOptionPane.showMessageDialog( getMainWindow(), "File does not exist.", "Error", JOptionPane.ERROR_MESSAGE );
            return ( false );
        }
        
        currentSimulationFile = file;
        
        getEditorPanel().liveMode = true;
        
        eventsManager.onSessionEnded( presets );
        eventsManager.onSessionStarted( null );
        
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    simulating = true;
                    simulating2 = true;
                    
                    //Object synMonitor = getEditorPanel().getDrawSyncMonitor();
                    
                    while ( simulating )
                    {
                        if ( !eventsManager.getWaitingForData( true ) )
                        {
                            //synchronized ( syncMonitor )
                            {
                                repaintEditorPanel();
                            }
                        }
                        
                        try
                        {
                            Thread.sleep( 100L );
                        }
                        catch ( InterruptedException e )
                        {
                        }
                    }
                    
                    simulating2 = false;
                }
                finally
                {
                    getEditorPanel().liveMode = false;
                }
            }
        }.start();
        
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    SimulationPlayer.playback( eventsManager, getEditorPanel().getDrawSyncMonitor(), file, control );
                }
                catch ( Throwable t )
                {
                    RFDHLog.exception( t );
                }
                finally
                {
                    if ( simControls != null )
                        simControls.btnPlay.setEnabled( true );
                    
                    eventsManager.onSessionEnded( presets );
                    eventsManager.onSessionStarted( presets );
                    
                    initTestGameData();
                }
            }
        }.start();
        
        return ( true );
    }
    
    public void stopSimulation( boolean complete )
    {
        if ( simulating )
        {
            simulating = false;
            
            if ( complete )
            {
                while ( simulating2 )
                {
                    try
                    {
                        Thread.sleep( 100L );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
                
                eventsManager.onSessionEnded( presets );
                eventsManager.onSessionStarted( presets );
                
                initTestGameData();
            }
        }
    }
    
    public void switchToEditorMode()
    {
        if ( mainSplit.getRightComponent() != propsSplit )
        {
            int divLoc = mainSplit.getDividerLocation();
            mainSplit.setRightComponent( propsSplit );
            mainSplit.setDividerLocation( divLoc );
        }
        
        if ( directorMgr != null )
        {
            if ( !directorMgr.checkUnsavedChanges() )
                return;
            
            directorMgr.close();
            //directorMgr = null;
        }
        
        if ( liveMgr != null )
        {
            if ( liveMgr.isConnected() )
                liveMgr.stop();
        }
        
        simControls = null;
        
        stopSimulation( true );
        
        repaintEditorPanel();
        
        menuBar.afterSwitchedToEditorMode();
    }
    
    public void showInputBindingsGUI()
    {
        InputBindingsGUI.showInputBindingsGUI( this );
    }
    
    public void showPresetsWindow()
    {
        presetsWindow.setVisible( true );
    }
    
    public void showHelpWindow()
    {
        alwaysShowHelpOnStartup = HelpWindow.showHelpWindow( window, alwaysShowHelpOnStartup, false ).getAlwaysShowOnStartup();
    }
    
    private void initTestGameData()
    {
        eventsManager.onDrivingAidsUpdated( presets );
        eventsManager.onScoringInfoUpdated( -1, presets );
        eventsManager.onWeatherInfoUpdated( presets );
        eventsManager.onTelemetryDataUpdated( presets );
        eventsManager.onCommentaryRequestInfoUpdated( presets );
        eventsManager.onGraphicsInfoUpdated( presets );
    }
    
    private void initGameDataObjects( _LiveGameDataObjectsFactory gdFactory )
    {
        int[] resolution = loadResolutionFromUserSettings( gdFactory.newGameFileSystem( __UtilHelper.PLUGIN_INI ) );
        
        this.drawingManager = new WidgetsDrawingManager( true, resolution[0], resolution[1] );
        this.widgetsConfig = drawingManager.getWidgetsConfiguration();
        this.eventsManager = gdFactory.newGameEventsManager( null, drawingManager );
        this.gameData = eventsManager.getGameData();
        
        RFDynHUDEditor.GAME_ID = this.gameData.getGameID();
        RFDynHUDEditor.FILESYSTEM = this.gameData.getFileSystem();
        
        __UtilHelper.configFolder = gameData.getFileSystem().getConfigFolder();
        __UtilHelper.bordersBolder = gameData.getFileSystem().getBordersFolder();
        __UtilHelper.imagesFolder = gameData.getFileSystem().getImagesFolder();
        __UtilHelper.editorPropertyDisplayNameGeneratorClass = gameData.getFileSystem().getPluginINI().getEditorPropertyDisplayNameGeneratorClass();
        __UtilPrivilegedAccess.updateLocalizationsManager( gameData.getFileSystem() );
        WidgetFactory.init( gameData.getFileSystem().getWidgetSetsFolder() );
        
        __GDPrivilegedAccess.updateProfileInfo( gameData.getProfileInfo() );
        
        __GDPrivilegedAccess.setUpdatedInTimescope( gameData.getSetup() );
        __GDPrivilegedAccess.updateInfo( gameData );
        
        eventsManager.onStartup( presets );
        eventsManager.onSessionStarted( presets );
        
        loadEditorPresets();
        initTestGameData();
        
        __GDPrivilegedAccess.setInCockpit( true, gameData, System.nanoTime(), true );
    }
    
    @SuppressWarnings( "unchecked" )
    private _LiveGameDataObjectsFactory initGameDataObjectsFactory( ClassLoader classLoader, String objectFactory )
    {
        _LiveGameDataObjectsFactory gdFactory = null;
        
        if ( objectFactory != null )
        {
            try
            {
                Class<_LiveGameDataObjectsFactory> clazz = null;
                
                if ( classLoader == null )
                    clazz = (Class<_LiveGameDataObjectsFactory>)Class.forName( objectFactory );
                else
                    clazz = (Class<_LiveGameDataObjectsFactory>)Class.forName( objectFactory, true, classLoader );
                
                gdFactory = clazz.newInstance();
            }
            catch ( Throwable t )
            {
                throw new Error( t );
            }
        }
        
        return ( gdFactory );
    }
    
    private WidgetsEditorPanel createEditorPanel()
    {
        WidgetsEditorPanel editorPanel = new WidgetsEditorPanel( this, gameData, drawingManager );
        editorPanel.setPreferredSize( new Dimension( widgetsConfig.getGameResolution().getViewportWidth(), widgetsConfig.getGameResolution().getViewportHeight() ) );
        WidgetsEditorPanelInputHandler inputHandler = new WidgetsEditorPanelInputHandler( editorPanel, widgetsConfig );
        editorPanel.addMouseListener( inputHandler );
        editorPanel.addMouseMotionListener( inputHandler );
        editorPanel.addKeyListener( inputHandler );
        
        editorPanel.addWidgetsEditorPanelListener( this );
        
        return ( editorPanel );
    }
    
    private Property createTemplateConfigProperty( final GameFileSystem fileSystem )
    {
        Property templateConfigProp = new ListProperty<String, List<String>>( "templateConfig", "templateConfig", getCurrentTemplateFileForProperty(), getConfigurationFiles(), false, "reload" )
        {
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
                        loadTemplateConfig( new File( fileSystem.getConfigFolder(), (String)value ) );
                    }
                    catch ( IOException e )
                    {
                        RFDHLog.exception( e );
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
                        RFDHLog.exception( e );
                    }
                }
            }
        };
        
        return ( templateConfigProp );
    }
    
    public void start( _LiveGameDataObjectsFactory gdFactory )
    {
        try
        {
            initGameDataObjects( gdFactory );
            
            FontUtils.loadCustomFonts( gameData.getFileSystem() );
            
            this.window = new JFrame( BASE_WINDOW_TITLE );
            
            window.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
            
            this.menuBar = new EditorMenuBar( this );
            window.setJMenuBar( menuBar );
            
            Container contentPane = window.getContentPane();
            contentPane.setLayout( new BorderLayout() );
            
            this.editorPanel = createEditorPanel();
            this.gameResolution = widgetsConfig.getGameResolution();
            
            templateConfigProp = createTemplateConfigProperty( gameData.getFileSystem()  );
            
            AbstractPropertiesKeeper.setKeeper( gameResProp, null );
            AbstractPropertiesKeeper.setKeeper( templateConfigProp, null );
            
            editorScrollPane = new JScrollPane( editorPanel );
            editorScrollPane.getHorizontalScrollBar().setUnitIncrement( 20 );
            editorScrollPane.getVerticalScrollBar().setUnitIncrement( 20 );
            editorScrollPane.getViewport().setScrollMode( JViewport.SIMPLE_SCROLL_MODE );
            editorScrollPane.setPreferredSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
            
            mainSplit = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
            mainSplit.setOneTouchExpandable( true );
            mainSplit.setContinuousLayout( true );
            mainSplit.setResizeWeight( 1 );
            mainSplit.add( editorScrollPane );
            
            this.propsEditor = new PropertiesEditor();
            this.propsEditor.addChangeListener( new WidgetPropertyChangeListener( this ) );
            
            propsSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
            propsSplit.setContinuousLayout( true );
            propsSplit.setResizeWeight( 0 );
            propsSplit.setPreferredSize( new Dimension( 300, Integer.MAX_VALUE ) );
            propsSplit.setMinimumSize( new Dimension( 300, 10 ) );
            
            editorTable = PropertiesEditorTableModel.newTable( this, propsEditor );
            
            propsSplit.add( editorTable.createScrollPane() );
            
            editorTable.addPropertySelectionListener( this );
            
            docPanel = new JEditorPane( "text/html", "" );
            ( (HTMLDocument)docPanel.getDocument() ).getStyleSheet().importStyleSheet( RFDynHUDEditor.class.getClassLoader().getResource( "net/ctdp/rfdynhud/editor/properties/doc.css" ) );
            docPanel.setEditable( false );
            docPanel.setAutoscrolls( false );
            JScrollPane sp = new JScrollPane( docPanel );
            sp.setMinimumSize( new Dimension( 300, 10 ) );
            propsSplit.add( sp );
            propsSplit.setDividerLocation( 450 );
            
            propsSplit.resetToPreferredSizes();
            
            mainSplit.add( propsSplit );
            contentPane.add( mainSplit, BorderLayout.CENTER );
            
            mainSplit.resetToPreferredSizes();
            
            this.tabDirector = new JTabbedPane();
            
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
            
            AbstractPropertiesKeeper.attachKeeper( editorPanel.getSettings() );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            
            System.exit( 1 );
        }
        
        final RFDynHUDEditor editor = this;
        
        try
        {
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
                    configFile = new File( editor.getGameData().getFileSystem().getConfigFolder(), "overlay.ini" );
                if ( configFile.exists() )
                    editor.openConfig( configFile );
                else
                    editor.loadFallbackConfig();
            }
            
            editor.eventsManager.onCockpitEntered( true );
            
            __GDPrivilegedAccess.applyEditorPresets( presets, gameData );
            
            //editor.getEditorPanel().getWidgetsDrawingManager().collectTextures( true, editor.gameData );
            
            if ( (Boolean)result[0] )
                editor.getEditorPanel().initBackgroundImage();
            
            editor.getMainWindow().addWindowListener( new WindowAdapter()
            {
                @Override
                public void windowOpened( WindowEvent e )
                {
                    ( (JFrame)e.getSource() ).removeWindowListener( this );
                    
                    if ( editor.alwaysShowHelpOnStartup || !editor.watchedWaitFor60Seconds )
                    {
                        editor.alwaysShowHelpOnStartup = HelpWindow.showHelpWindow( editor.window, editor.alwaysShowHelpOnStartup, !editor.watchedWaitFor60Seconds ).getAlwaysShowOnStartup();
                        editor.watchedWaitFor60Seconds = ( HelpWindow.instance.waitEndTime != null ) && ( HelpWindow.instance.waitEndTime < System.nanoTime() );
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
            
            editor.eventsManager.onShutdown( true );
            
            //System.exit( 0 );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            
            String message = t.getMessage();
            if ( ( message == null ) || ( message.length() == 0 ) )
                message = t.getClass().getSimpleName() + ", " + t.getStackTrace()[0] + "\n" + "Please see the editor log file for more info.";
            
            JOptionPane.showMessageDialog( null, message, "Error running the rfDynHUD Editor", JOptionPane.ERROR_MESSAGE );
            
            System.exit( 1 );
        }
    }
    
    public void start( ClassLoader classLoader, String objectFactory )
    {
        _LiveGameDataObjectsFactory gdFactory = initGameDataObjectsFactory( classLoader, objectFactory );
        
        start( gdFactory );
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
    }
    
    private static final EditorArguments parseCommandLine( String[] args ) throws CommandlineParsingException
    {
        EditorArgumentsHandler handler = new EditorArgumentsHandler();
        
        ArgumentsRegistry argReg = EditorArgumentsRegistry.createStandardArgumentsRegistry();
        CommandlineParser parser = new CommandlineParser( argReg, handler );
        parser.parseCommandline( args );
        
        if ( handler.helpRequested() )
        {
            argReg.dump();
            System.exit( 0 );
        }
        
        return ( handler.getArguments() );
    }
    
    private static ClassLoader loadAdditionalJars( String[] jars ) throws Throwable
    {
        if ( jars == null )
            return ( null );
        
        java.net.URL[] urls = new java.net.URL[ jars.length ];
        
        for ( int i = 0; i < jars.length; i++ )
        {
            urls[i] = new java.io.File( jars[i] ).toURI().toURL();
        }
        
        return ( new URLClassLoader( urls ) );
    }
    
    public static void main( String[] args )
    {
        EditorArguments arguments = null;
        
        try
        {
            arguments = parseCommandLine( args );
        }
        catch ( CommandlineParsingException e )
        {
            e.printStackTrace();
            return;
        }
        
        ClassLoader classLoader = null;
        
        try
        {
            classLoader = loadAdditionalJars( arguments.getAdditionalJars() );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            return;
        }
        
        WidgetFactory.setExcludedJars( arguments.getExcludedJars() );
        
        //Logger.setStdStreams();
        
        final RFDynHUDEditor editor = new RFDynHUDEditor();
        
        try
        {
            editor.start( classLoader, arguments.getObjectFactory() );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
    }
}
