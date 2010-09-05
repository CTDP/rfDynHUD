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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.help.HelpWindow;
import net.ctdp.rfdynhud.editor.hiergrid.GridItemsContainer;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.presets.EditorPresetsWindow;
import net.ctdp.rfdynhud.editor.presets.ScaleType;
import net.ctdp.rfdynhud.editor.properties.DefaultWidgetPropertiesContainer;
import net.ctdp.rfdynhud.editor.properties.EditorTable;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditor;
import net.ctdp.rfdynhud.editor.properties.PropertySelectionListener;
import net.ctdp.rfdynhud.editor.properties.WidgetPropertyChangeListener;
import net.ctdp.rfdynhud.editor.util.AvailableDisplayModes;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.editor.util.DefaultWidgetsConfigurationWriter;
import net.ctdp.rfdynhud.editor.util.EditorPropertyLoader;
import net.ctdp.rfdynhud.editor.util.SaveAsDialog;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.ListProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.util.TextureManager;
import net.ctdp.rfdynhud.util.Tools;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniWriter;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RFDynHUDEditor implements Documented, PropertySelectionListener
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
    
    private static final String DEFAULT_SCREENSHOT_SET = "CTDPF106_Fer_T-Cam";
    private String screenshotSet = DEFAULT_SCREENSHOT_SET;
    
    private boolean alwaysShowHelpOnStartup = true;
    
    private final JFrame window;
    private final EditorMenuBar menuBar;
    private final EditorPanel editorPanel;
    private final JScrollPane editorScrollPane;
    
    private final PropertiesEditor propsEditor;
    private final EditorTable editorTable;
    private final JEditorPane docPanel;
    private boolean isSomethingDoced = false;
    
    private final EditorStatusBar statusBar;
    
    private final EditorPresets presets = new EditorPresets();
    
    private final EditorRunningIndicator runningIndicator;
    
    private static final String doc_header = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_header.html" ) );
    private static final String doc_footer = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_footer.html" ) );
    
    private boolean presetsWindowVisible = false;
    private final EditorPresetsWindow presetsWindow;
    
    private boolean dirtyFlag = false;
    
    private File currentConfigFile = null;
    private File currentTemplateFile = null;
    
    private WidgetsConfiguration templateConfig = null;
    private long lastTemplateConfigModified = -1L;
    
    public final LiveGameData getGameData()
    {
        return ( gameData );
    }
    
    public final WidgetsConfiguration getWidgetsConfiguration()
    {
        return ( widgetsConfig );
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
    
    private File getScreenshotSetFolder()
    {
        File backgroundsFolder = new File( GameFileSystem.INSTANCE.getEditorFolder(), "backgrounds" );
        
        return ( new File( backgroundsFolder, screenshotSet ) );
    }
    
    private File getBackgroundImageFile( int width, int height )
    {
        return ( new File( getScreenshotSetFolder(), File.separator + "background_" + width + "x" + height + ".jpg" ) );
    }
    
    private void setScreenshotSet( String screenshotSet )
    {
        File backgroundsFolder = new File( GameFileSystem.INSTANCE.getEditorFolder(), "backgrounds" );
        
        if ( !backgroundsFolder.exists() )
        {
            this.screenshotSet = DEFAULT_SCREENSHOT_SET;
            return;
        }
        
        File folder = new File( backgroundsFolder, screenshotSet );
        if ( folder.exists() && folder.isDirectory() )
        {
            this.screenshotSet = screenshotSet;
        }
        else
        {
            if ( new File( backgroundsFolder, DEFAULT_SCREENSHOT_SET ).exists() )
            {
                this.screenshotSet = DEFAULT_SCREENSHOT_SET;
            }
            else
            {
                for ( File f : backgroundsFolder.listFiles() )
                {
                    if ( f.isDirectory() )
                    {
                        this.screenshotSet = f.getName();
                        return;
                    }
                }
            }
            
            this.screenshotSet = DEFAULT_SCREENSHOT_SET;
        }
    }
    
    public final String getScreenshotSet()
    {
        return ( screenshotSet );
    }
    
    public final JFrame getMainWindow()
    {
        return ( window );
    }
    
    public final EditorMenuBar getMenuBar()
    {
        return ( menuBar );
    }
    
    public final EditorPanel getEditorPanel()
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
        BufferedReader br = null;
        
        try
        {
            br = new BufferedReader( new InputStreamReader( new FileInputStream( f ) ) );
            if ( !"[Meta]".equals( br.readLine() ) )
                return ( false );
            
            String line = br.readLine();
            
            if ( line == null )
                return ( false );
            
            if ( !line.startsWith( "rfDynHUD_Version" ) )
                return ( false );
            
            return ( true );
        }
        catch ( IOException e )
        {
            return ( false );
        }
        finally
        {
            try
            {
                br.close();
            }
            catch ( IOException e )
            {
            }
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
    
    private ArrayList<String> getScreenshotSets()
    {
        ArrayList<String> list = new ArrayList<String>();
        
        File root = new File( GameFileSystem.INSTANCE.getEditorFolder(), "backgrounds" );
        for ( File f : root.listFiles() )
        {
            if ( f.isDirectory() && !f.getName().toLowerCase().equals( ".svn" ) )
            {
                list.add( f.getAbsolutePath().substring( root.getAbsolutePath().length() + 1 ) );
            }
        }
        
        return ( list );
    }
    
    private void getProperties( WidgetPropertiesContainer propsCont )
    {
        propsCont.addGroup( "Editor - General" );
        
        propsCont.addProperty( new ListProperty<String, ArrayList<String>>( (Widget)null, "screenshotSet", screenshotSet, getScreenshotSets() )
        {
            @Override
            public void setValue( Object value )
            {
                switchScreenshotSet( (String)value );
            }
            
            @Override
            public String getValue()
            {
                return ( screenshotSet );
            }
        } );
        
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
        
        getEditorPanel().getProperties( propsCont );
        
        propsCont.addProperty( new ListProperty<String, ArrayList<String>>( (Widget)null, "templateConfig", "templateConfig", getCurrentTemplateFileForProperty(), getConfigurationFiles(), false, "reload" )
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
    
    private static void readExpandFlags( GridItemsContainer list, String keyPrefix, HashMap<String, Boolean> map )
    {
        for ( int i = 0; i < list.size(); i++ )
        {
            if ( list.get( i ) instanceof GridItemsContainer )
            {
                GridItemsContainer gic = (GridItemsContainer)list.get( i );
                map.put( keyPrefix + gic.getName(), gic.getExpandFlag() );
                
                readExpandFlags( gic, keyPrefix, map );
            }
        }
    }
    
    private static void restoreExpandFlags( GridItemsContainer list, String keyPrefix, HashMap<String, Boolean> map )
    {
        for ( int i = 0; i < list.size(); i++ )
        {
            if ( list.get( i ) instanceof GridItemsContainer )
            {
                GridItemsContainer gic = (GridItemsContainer)list.get( i );
                Boolean b = map.get( keyPrefix + gic.getName() );
                if ( b != null )
                    gic.setExpandFlag( b.booleanValue() );
                
                restoreExpandFlags( gic, keyPrefix, map );
            }
        }
    }
    
    public void onWidgetSelected( Widget widget, boolean doubleClick )
    {
        editorPanel.setSelectedWidget( widget, doubleClick );
        
        GridItemsContainer propsList = propsEditor.getPropertiesList();
        
        HashMap<String, Boolean> expandedRows = new HashMap<String, Boolean>();
        readExpandFlags( propsList, "", expandedRows );
        
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
        restoreExpandFlags( propsList, "", expandedRows );
        
        editorTable.apply();
    }
    
    //private long nextRedrawTime = -1L;
    
    public static final String WIDGET_CHANGE_POS_SIZE = "POS_SIZE";
    
    /**
     * @param widget
     * @param propertyName
     */
    public void onWidgetChanged( Widget widget, String propertyName )
    {
        //if ( System.currentTimeMillis() >= nextRedrawTime )
        {
            getEditorPanel().repaint();
            //nextRedrawTime = System.currentTimeMillis() + 1000L;
        }
        
        setDirtyFlag();
        
        if ( ( widget != null ) && ( widget == getEditorPanel().getSelectedWidget() ) )
        {
            //editorTable.apply();
            //onWidgetSelected( widget, false );
            
            HierarchicalTableModel m = (HierarchicalTableModel)editorTable.getModel();
            int rc = m.getRowCount();
            for ( int i = 0; i < rc; i++ )
            {
                Object v = m.getValueAt( i, 1 );
                if ( v instanceof String )
                {
                    String pn = (String)v;
                    if ( pn.equals( "x" ) || pn.equals( "y" ) || pn.equals( "width" ) || pn.equals( "height" ) )
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
    
    private void saveUserSettings( int extendedState )
    {
        File userSettingsFile = getEditorSettingsFile();
        
        IniWriter writer = null;
        try
        {
            writer = new IniWriter( userSettingsFile );
            WidgetsConfigurationWriter confWriter = new DefaultWidgetsConfigurationWriter( writer );
            
            writer.writeGroup( "General" );
            writer.writeSetting( "screenshotSet", screenshotSet );
            writer.writeSetting( "resolution", gameResolution.getResolutionString() );
            getEditorPanel().saveProperties( confWriter );
            writer.writeSetting( "templatesConfig", getCurrentTemplateFileForProperty() );
            writer.writeSetting( "defaultScaleType", presetsWindow.getDefaultScaleType() );
            writeLastConfig( writer );
            writer.writeSetting( "alwaysShowHelpOnStartup", alwaysShowHelpOnStartup );
            writer.writeGroup( "MainWindow" );
            writer.writeSetting( "windowLocation", getMainWindow().getX() + "x" + getMainWindow().getY() );
            writer.writeSetting( "windowSize", getMainWindow().getWidth() + "x" + getMainWindow().getHeight() );
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
                                    if ( checkResolution( resX, resY ) )
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
            DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
            
            resolution[0] = dm.getWidth();
            resolution[1] = dm.getHeight();
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
                        getEditorPanel().loadProperty( loader );
                        
                        if ( key.equals( "screenshotSet" ) )
                        {
                            setScreenshotSet( value );
                        }
                        else if ( key.equals( "resolution" ) )
                        {
                            try
                            {
                                String[] ss = value.split( "x" );
                                int resX = Integer.parseInt( ss[0] );
                                int resY = Integer.parseInt( ss[1] );
                                if ( ( ( gameResolution.getViewportWidth() != resX ) || ( gameResolution.getViewportHeight() != resY ) ) && checkResolution( resX, resY ) )
                                    switchToGameResolution( resX, resY );
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
                        else if ( key.equals( "alwaysShowHelpOnStartup" ) )
                        {
                            alwaysShowHelpOnStartup = Boolean.parseBoolean( value );
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
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader(), templateConfigFile, templateConfig, gameData, presets, null );
            
            this.currentTemplateFile = templateConfigFile;
            this.lastTemplateConfigModified = templateConfigFile.lastModified();
        }
        
        return ( templateConfigFile );
    }
    
    public void openConfig( File configFile )
    {
        try
        {
            int n = widgetsConfig.getNumWidgets();
            for ( int i = 0; i < n; i++ )
            {
                widgetsConfig.getWidget( i ).clearRegion( true, getOverlayTexture() );
            }
            
            __UtilPrivilegedAccess.forceLoadConfiguration( new ConfigurationLoader(), configFile, widgetsConfig, gameData, presets, null );
            
            currentConfigFile = configFile;
            
            updateWindowTitle();
            
            getOverlayTexture().clear( true, null );
            onWidgetSelected( null, false );
            getEditorPanel().repaint();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            JOptionPane.showMessageDialog( window, ( t.getMessage() != null ) ? t.getMessage() : t.getClass().getSimpleName(), t.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE );
        }
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
        
        try
        {
            JFileChooser fc = new JFileChooser();
            if ( currentConfigFile != null )
            {
                fc.setCurrentDirectory( currentConfigFile.getParentFile() );
                fc.setSelectedFile( currentConfigFile );
            }
            else
            {
                fc.setCurrentDirectory( GameFileSystem.INSTANCE.getConfigFolder() );
                fc.setSelectedFile( new File( GameFileSystem.INSTANCE.getConfigFolder(), "overlay.ini" ) );
            }
            
            fc.setMultiSelectionEnabled( false );
            fc.setFileSelectionMode( JFileChooser.FILES_ONLY );
            fc.setFileFilter( new FileNameExtensionFilter( "ini files", "ini" ) );
            
            if ( fc.showOpenDialog( window ) != JFileChooser.APPROVE_OPTION )
                return;
            
            openConfig( fc.getSelectedFile() );
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
            EditorPanel ep = getEditorPanel();
            ConfigurationSaver.saveConfiguration( widgetsConfig, gameResolution.getResolutionString(), ep.getGridOffsetX(), ep.getGridOffsetY(), ep.getGridSizeX(), ep.getGridSizeY(), currentConfigFile );
            
            resetDirtyFlag();
            
            return ( true );
        }
        catch ( Throwable t )
        {
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
    
    public void snapSelectedWidgetToGrid()
    {
        Widget widget = getEditorPanel().getSelectedWidget();
        if ( widget != null )
        {
            getEditorPanel().clearWidgetRegion( widget );
            getEditorPanel().snapWidgetToGrid( widget );
            onWidgetSelected( widget, false );
            getEditorPanel().repaint();
            setDirtyFlag();
        }
    }
    
    public void snapAllWidgetsToGrid()
    {
        for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
            getEditorPanel().clearWidgetRegion( widgetsConfig.getWidget( i ) );
        getEditorPanel().snapAllWidgetsToGrid();
        onWidgetSelected( getEditorPanel().getSelectedWidget(), false );
        getEditorPanel().repaint();
        setDirtyFlag();
    }
    
    public void removeSelectedWidget()
    {
        getEditorPanel().removeSelectedWidget();
        onWidgetSelected( null, false );
    }
    
    public void makeAllWidgetsUsePixels()
    {
        int result = JOptionPane.showConfirmDialog( getMainWindow(), "Do you really want to convert all Widgets' positions and sizes to be absolute pixels?", "Convert all Widgets' coordinates", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
        if ( result == JOptionPane.YES_OPTION )
        {
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
                widgetsConfig.getWidget( i ).setAllPosAndSizeToPixels();
        }
    }
    
    public void makeAllWidgetsUsePercents()
    {
        int result = JOptionPane.showConfirmDialog( getMainWindow(), "Do you really want to convert all Widgets' positions and sizes to be percents?", "Convert all Widgets' coordinates", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
        if ( result == JOptionPane.YES_OPTION )
        {
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
                widgetsConfig.getWidget( i ).setAllPosAndSizeToPercents();
        }
    }
    
    private void copyPropertiesFromTemplate( List<Property> lstTemplate, List<Property> lstTarget )
    {
        // We assume, that the order will be the same in both lists.
        
        for ( int i = 0; i < lstTemplate.size(); i++ )
        {
            if ( lstTemplate.get( i ) instanceof Property )
            {
                Property p0 = (Property)lstTemplate.get( i );
                Property p1 = (Property)lstTarget.get( i );
                
                if ( !p0.getName().equals( "x" ) && !p0.getName().equals( "y" ) && !p0.getName().equals( "positioning" ) )
                    p1.setValue( p0.getValue() );
            }
        }
    }
    
    private void copyPropertiesFromTemplate( Widget widget )
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
        
        FlatWidgetPropertiesContainer pcTemplate = new FlatWidgetPropertiesContainer();
        FlatWidgetPropertiesContainer pcTarget = new FlatWidgetPropertiesContainer();
        
        template.getProperties( pcTemplate, true );
        widget.getProperties( pcTarget, true );
        
        copyPropertiesFromTemplate( pcTemplate.getList(), pcTarget.getList() );
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
                onWidgetSelected( widget, false );
                getEditorPanel().repaint();
                
                setDirtyFlag();
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        return ( widget );
    }
    
    private static BufferedImage createFallbackImage( int width, int height, String message )
    {
        BufferedImage bi = TextureManager.createMissingImage( width, height );
        
        Graphics2D g = bi.createGraphics();
        
        g.setColor( Color.RED );
        g.setFont( new java.awt.Font( "Verdana", java.awt.Font.BOLD, 14 ) );
        g.drawString( message, 50, 50 );
        
        return ( bi );
    }
    
    public BufferedImage loadBackgroundImage( int width, int height )
    {
        BufferedImage result = null;
        
        File file = getBackgroundImageFile( width, height );
        
        if ( file.exists() && file.isFile() )
        {
            try
            {
                result = ImageIO.read( file );
            }
            catch ( IOException e )
            {
                Logger.log( "Unable to read background image file \"" + file.getAbsolutePath() + "\"" );
                Logger.log( e );
                
                //result = null;
            }
        }
        
        if ( result == null )
        {
            String message = "Background image not found: \"" + file.getAbsolutePath() + "\"!";
            
            Logger.log( message );
            
            result = createFallbackImage( width, height, message );
        }
        
        return ( result );
    }
    
    public boolean checkResolution( int resX, int resY )
    {
        return ( getBackgroundImageFile( resX, resY ).exists() );
    }
    
    public void switchToGameResolution( int resX, int resY )
    {
        BufferedImage backgroundImage = loadBackgroundImage( resX, resY );
        __GDPrivilegedAccess.setGameResolution( resX, resY, widgetsConfig );
        __WCPrivilegedAccess.setViewport( 0, 0, resX, resY, widgetsConfig );
        TransformableTexture overlayTexture = __RenderPrivilegedAccess.createMainTexture( resX, resY );
        
        editorPanel.setBackgroundImage( backgroundImage );
        editorPanel.setOverlayTexture( overlayTexture.getTexture() );
        editorPanel.setPreferredSize( new Dimension( resX, resY ) );
        editorPanel.setMaximumSize( new Dimension( resX, resY ) );
        editorPanel.setMinimumSize( new Dimension( resX, resY ) );
        ( (JScrollPane)editorPanel.getParent().getParent() ).doLayout();
        
        onWidgetSelected( editorPanel.getSelectedWidget(), false );
        
        getMainWindow().validate();
        getEditorPanel().repaint();
    }
    
    public void switchScreenshotSet( String screenshotSet )
    {
        Logger.log( "Switching to Screenshot Set \"" + screenshotSet + "\"..." );
        
        setScreenshotSet( screenshotSet );
        switchToGameResolution( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() );
    }
    
    public void showFullscreenPreview()
    {
        Logger.log( "Showing fullscreen preview" );
        
        final GraphicsDevice graphDev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final DisplayMode desktopDM = graphDev.getDisplayMode();
        
        JPanel p = new JPanel()
        {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void paintComponent( Graphics g )
            {
                getEditorPanel().drawWidgets( (Graphics2D)g, true, false );
            }
        };
        p.setBackground( Color.BLACK );
        
        DisplayMode dm = AvailableDisplayModes.getDisplayMode( gameResolution.getResolutionString() );
        
        //boolean isSameMode = dm.equals( desktopDM );
        boolean isSameMode = ( ( dm.getWidth() == desktopDM.getWidth() ) && ( dm.getHeight() == desktopDM.getHeight() ) );
        java.awt.Window w;
        //if ( isSameMode )
        {
            javax.swing.JDialog d = new javax.swing.JDialog( getMainWindow(), isSameMode );
            
            d.setUndecorated( true );
            d.setSize( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() );
            d.setResizable( false );
            d.setContentPane( p );
            
            w = d;
        }
        /*
        else
        {
            java.awt.Dialog d = new java.awt.Dialog( getMainWindow() );
            //java.awt.Frame d = new java.awt.Frame();
            
            d.setLayout( new java.awt.GridLayout( 1, 1 ) );
            d.add( p );
            d.setUndecorated( true );
            d.setSize( gameResX, gameResY );
            d.setResizable( false );
            
            w = d;
        }
        */
        
        w.setName( "preview frame" );
        w.setBackground( Color.BLACK );
        w.setLocation( 0, 0 );
        
        Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
        {
            @Override
            public void eventDispatched( AWTEvent event )
            {
                KeyEvent kev = (KeyEvent)event;
                
                if ( kev.getID() == KeyEvent.KEY_PRESSED )
                {
                    if ( ( kev.getKeyCode() == KeyEvent.VK_ESCAPE ) || ( kev.getKeyCode() == KeyEvent.VK_F11 ) )
                    {
                        Toolkit.getDefaultToolkit().removeAWTEventListener( this );
                        java.awt.Window w = null;
                        if ( event.getSource() instanceof java.awt.Window )
                            w = (java.awt.Window)event.getSource();
                        else if ( "preview frame".equals( ( (JComponent)event.getSource() ).getRootPane().getParent().getName() ) )
                            w = (java.awt.Window)( (JComponent)event.getSource() ).getRootPane().getParent();
                        
                        //graphDev.setDisplayMode( desktopDM );
                        
                        if ( graphDev.getFullScreenWindow() == w )
                            graphDev.setFullScreenWindow( null );
                        
                        //w.setVisible( false );
                        w.dispose();
                    }
                    else if ( kev.getKeyCode() == KeyEvent.VK_F12 )
                    {
                        takeScreenshot();
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK );
        
        w.addWindowListener( new WindowAdapter()
        {
            private boolean gridSuppressed = false;
            
            @Override
            public void windowOpened( WindowEvent e )
            {
                EditorPanel editorPanel = getEditorPanel();
                
                if ( editorPanel.getDrawGrid() && ( editorPanel.getGridSizeX() > 1 ) && ( editorPanel.getGridSizeY() > 1 ) )
                {
                    editorPanel.setBGImageReloadSuppressed( true );
                    editorPanel.setDrawGrid( false );
                    editorPanel.setBGImageReloadSuppressed( false );
                    editorPanel.setBackgroundImage( loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
                    
                    gridSuppressed = true;
                }
                
                //graphDev.setDisplayMode( displayModes.get( gameResX + "x" + gameResY ) );
            }
            
            @Override
            public void windowClosed( WindowEvent e )
            {
                Logger.log( "Closing fullscreen preview" );
                
                EditorPanel editorPanel = getEditorPanel();
                
                if ( gridSuppressed )
                {
                    editorPanel.setBGImageReloadSuppressed( true );
                    editorPanel.setDrawGrid( true );
                    editorPanel.setBGImageReloadSuppressed( false );
                    editorPanel.setBackgroundImage( loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
                }
                
                editorPanel.repaint();
                onWidgetSelected( editorPanel.getSelectedWidget(), false );
            }
        } );
        
        if ( isSameMode )
        {
            w.setVisible( true );
        }
        else
        {
            graphDev.setFullScreenWindow( w );
            graphDev.setDisplayMode( dm );
        }
    }
    
    public void takeScreenshot()
    {
        EditorPanel editorPanel = getEditorPanel();
        
        BufferedImage img = new BufferedImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight(), BufferedImage.TYPE_3BYTE_BGR );
        
        boolean gridSuppressed = false;
        
        if ( editorPanel.getDrawGrid() && ( editorPanel.getGridSizeX() > 1 ) && ( editorPanel.getGridSizeY() > 1 ) )
        {
            editorPanel.setBGImageReloadSuppressed( true );
            editorPanel.setDrawGrid( false );
            editorPanel.setBGImageReloadSuppressed( false );
            editorPanel.setBackgroundImage( loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
            
            gridSuppressed = true;
        }
        
        editorPanel.drawWidgets( img.createGraphics(), true, false );
        
        if ( gridSuppressed )
        {
            editorPanel.setBGImageReloadSuppressed( true );
            editorPanel.setDrawGrid( true );
            editorPanel.setBGImageReloadSuppressed( false );
            editorPanel.setBackgroundImage( loadBackgroundImage( gameResolution.getViewportWidth(), gameResolution.getViewportHeight() ) );
        }
        
        editorPanel.repaint();
        onWidgetSelected( editorPanel.getSelectedWidget(), false );
        
        try
        {
            File folder = GameFileSystem.INSTANCE.getGameScreenshotsFolder();
            folder.mkdirs();
            
            String filenameBase = ( currentConfigFile == null ) ? "rfDynHUD_screenshot_" : "rfDynHUD_" + currentConfigFile.getName().replace( ".", "_" ) + "_";
            int i = 0;
            File f = new File( folder, filenameBase + Tools.padLeft( i, 3, "0" ) + ".png" );
            while ( f.exists() )
            {
                i++;
                f = new File( folder, filenameBase + Tools.padLeft( i, 3, "0" ) + ".png" );
            }
            
            Logger.log( "Saving screenshot to file " + f.getPath() );
            
            ImageIO.write( img, "PNG", f );
        }
        catch ( IOException e )
        {
            Logger.log( e );
        }
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
            __GDPrivilegedAccess.loadFromStream( in, gameData.getCommentaryRequestInfo(), editorPresets );
            in.close();
            
            //in = new FileInputStream( "data/game_data/graphics_info" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/graphics_info" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getGraphicsInfo(), editorPresets );
            in.close();
            
            //in = new FileInputStream( "data/game_data/scoring_info" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/scoring_info" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getScoringInfo(), editorPresets );
            in.close();
            
            //in = new FileInputStream( "data/game_data/telemetry_data" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/telemetry_data" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getTelemetryData(), editorPresets );
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
        
        WidgetsDrawingManager drawingManager = new WidgetsDrawingManager( resolution[0], resolution[1] );
        this.widgetsConfig = drawingManager;
        this.eventsManager = new GameEventsManager( null, drawingManager );
        this.gameData = new LiveGameData( drawingManager.getGameResolution(), eventsManager );
        eventsManager.setGameData( this.gameData );
        __GDPrivilegedAccess.updateProfileInfo( gameData.getProfileInfo() );
        eventsManager.setGameData( gameData );
        
        __GDPrivilegedAccess.setUpdatedInTimescope( gameData.getSetup() );
        __GDPrivilegedAccess.updateInfo( gameData );
        
        eventsManager.onSessionStarted( presets );
        eventsManager.onTelemetryDataUpdated( presets );
        eventsManager.onScoringInfoUpdated( presets );
        
        __GDPrivilegedAccess.setRealtimeMode( true, gameData, presets );
        
        loadEditorPresets();
        initTestGameData( gameData, presets );
    }
    
    private EditorPanel createEditorPanel()
    {
        WidgetsDrawingManager drawingManager = (WidgetsDrawingManager)widgetsConfig;
        
        EditorPanel editorPanel = new EditorPanel( this, gameData, drawingManager.getMainTexture(), drawingManager );
        editorPanel.setPreferredSize( new Dimension( drawingManager.getGameResolution().getViewportWidth(), drawingManager.getGameResolution().getViewportHeight() ) );
        
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
        split.setResizeWeight( 1 );
        split.add( editorScrollPane );
        
        this.propsEditor = new PropertiesEditor();
        this.propsEditor.addChangeListener( new WidgetPropertyChangeListener( this ) );
        
        JSplitPane split2 = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        split2.setResizeWeight( 0 );
        split2.setPreferredSize( new Dimension( 300, Integer.MAX_VALUE ) );
        split2.setMinimumSize( new Dimension( 300, 10 ) );
        
        editorTable = new EditorTable( this, propsEditor );
        
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
    }
    
    public static void main( String[] args )
    {
        try
        {
            //Logger.setStdStreams();
            
            final RFDynHUDEditor editor = new RFDynHUDEditor();
            
            GraphicsDevice graphDev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode dm = graphDev.getDisplayMode();
            
            editor.getMainWindow().setSize( dm.getWidth(), dm.getHeight() );
            editor.getMainWindow().setExtendedState( JFrame.MAXIMIZED_BOTH );
            
            Object[] result = editor.loadUserSettings();
            
            if ( !(Boolean)result[0] )
            {
                if ( editor.checkResolution( dm.getWidth(), dm.getHeight() ) )
                    editor.switchToGameResolution( dm.getWidth(), dm.getHeight() );
            }
            
            if ( editor.currentConfigFile == null )
            {
                File configFile = (File)result[1];
                if ( configFile == null )
                    configFile = new File( GameFileSystem.INSTANCE.getConfigFolder(), "overlay.ini" );
                if ( configFile.exists() )
                    editor.openConfig( configFile );
                else
                    __UtilPrivilegedAccess.loadFactoryDefaults( new ConfigurationLoader(), editor.widgetsConfig, editor.gameData, editor.presets, null );
            }
            
            editor.eventsManager.onRealtimeEntered( editor.presets );
            
            //editor.getEditorPanel().getWidgetsDrawingManager().collectTextures( true, editor.gameData );
            
            editor.getEditorPanel().setBackgroundImage( editor.loadBackgroundImage( editor.gameResolution.getViewportWidth(), editor.gameResolution.getViewportHeight() ) );
            
            editor.getMainWindow().addWindowListener( new WindowAdapter()
            {
                private boolean shot = false;
                
                @Override
                public void windowOpened( WindowEvent e )
                {
                    if ( shot )
                        return;
                    
                    if ( editor.alwaysShowHelpOnStartup )
                    {
                        editor.alwaysShowHelpOnStartup = HelpWindow.showHelpWindow( editor.window, true ).getAlwaysShowOnStartup();
                    }
                    
                    shot = true;
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
            t.printStackTrace();
            Logger.log( t );
            
            String message = t.getMessage();
            if ( ( message == null ) || ( message.length() == 0 ) )
                message = t.getClass().getSimpleName() + ", " + t.getStackTrace()[0] + "\n" + "Please see the editor log file for more info.";
            
            JOptionPane.showMessageDialog( null, message, "Error running the rfDynHUD Editor", JOptionPane.ERROR_MESSAGE );
        }
    }
}
