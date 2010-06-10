package net.ctdp.rfdynhud.editor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.help.AboutPage;
import net.ctdp.rfdynhud.editor.help.HelpWindow;
import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.input.InputBindingsGUI;
import net.ctdp.rfdynhud.editor.presets.EditorPresetsWindow;
import net.ctdp.rfdynhud.editor.presets.ScaleType;
import net.ctdp.rfdynhud.editor.properties.DefaultWidgetPropertiesContainer;
import net.ctdp.rfdynhud.editor.properties.EditorTable;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditor;
import net.ctdp.rfdynhud.editor.properties.PropertySelectionListener;
import net.ctdp.rfdynhud.editor.properties.WidgetPropertyChangeListener;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.editor.util.DefaultWidgetsConfigurationWriter;
import net.ctdp.rfdynhud.editor.util.StrategyTool;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
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
import net.ctdp.rfdynhud.widgets.GameResolution;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.jagatoo.util.classes.ClassSearcher;
import org.jagatoo.util.classes.PackageSearcher;
import org.jagatoo.util.classes.SuperClassCriterium;
import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;
import org.jagatoo.util.ini.IniWriter;

/**
 * 
 * @author Marvin Froehlich
 */
public class RFDynHUDEditor implements Documented, PropertySelectionListener
{
    static
    {
        __EDPrivilegedAccess.isEditorMode = true;
    }
    
    public class EditorWindow extends JFrame
    {
        private static final long serialVersionUID = 1944993989958953367L;
        
        public RFDynHUDEditor getEditor()
        {
            return ( RFDynHUDEditor.this );
        }
        
        public EditorWindow( String title )
        {
            super( title );
        }
    }
    
    private static final String BASE_WINDOW_TITLE = "rFactor dynamic HUD Editor v" + RFDynHUD.VERSION.toString();
    
    private LiveGameData gameData;
    
    private final GameResolution gameResolution;
    
    private static final String DEFAULT_SCREENSHOT_SET = "CTDPF106_Fer_T-Cam";
    private String screenshotSet = DEFAULT_SCREENSHOT_SET;
    
    private boolean alwaysShowHelpOnStartup = true;
    
    private final JFrame window;
    private final EditorPanel editorPanel;
    private final JScrollPane editorScrollPane;
    
    private final PropertiesEditor propsEditor;
    private final EditorTable editorTable;
    private final JEditorPane docPanel;
    private boolean isSomethingDoced = false;
    
    private final EditorPresets presets = new EditorPresets();
    
    private static final String doc_header = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_header.html" ) );
    private static final String doc_footer = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_footer.html" ) );
    
    private boolean presetsWindowVisible = false;
    private final EditorPresetsWindow presetsWindow;
    
    private boolean dirtyFlag = false;
    
    private File currentConfigFile = null;
    private File currentTemplateFile = null;
    
    private WidgetsConfiguration templateConfig = null;
    private long lastTemplateConfigModified = -1L;
    
    private static final HashMap<String, java.awt.DisplayMode> displayModes = new HashMap<String, java.awt.DisplayMode>();
    static
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for ( DisplayMode dm : ge.getDefaultScreenDevice().getDisplayModes() )
        {
            if ( ( dm.getWidth() >= 800 ) && ( dm.getWidth() >= 600 ) && ( dm.getBitDepth() == 32 ) )
            {
                String key = dm.getWidth() + "x" + dm.getHeight();
                
                DisplayMode dm2 = displayModes.get( key );
                if ( dm2 == null )
                {
                    displayModes.put( key, dm );
                }
                else
                {
                    if ( dm.getRefreshRate() > dm2.getRefreshRate() )
                    {
                        displayModes.remove( key );
                        displayModes.put( key, dm );
                    }
                }
            }
        }
    }
    
    public final LiveGameData getGameData()
    {
        return ( gameData );
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
    public String getDocumentationSource( Property property )
    {
        if ( property == null )
            return ( "" );
        
        if ( __PropsPrivilegedAccess.isWidgetsConfigProperty( property ) )
            return ( getEditorPanel().getWidgetsDrawingManager().getDocumentationSource( property ) );
        
        URL docURL = this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/doc/" + property.getPropertyName() + ".html" );
        
        if ( docURL == null )
            return ( "" );
        
        return ( StringUtil.loadString( docURL ) );
    }
    
    private Property currentDocedProperty = null;
    private Widget currentDocedWidget = null;
    
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
                docPanel.setText( doc_header + editorPanel.getSelectedWidget().getDocumentationSource( property ) + doc_footer );
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
        
        propsCont.addProperty( new ListProperty<String, ArrayList<String>>( null, "screenshotSet", screenshotSet, getScreenshotSets() )
        {
            @Override
            public void setValue( Object value )
            {
                switchScreenshotSet( (String)value );
            }
            
            @Override
            public Object getValue()
            {
                return ( screenshotSet );
            }
        } );
        
        propsCont.addProperty( new Property( null, "resolution", true, PropertyEditorType.STRING )
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
        } );
        
        getEditorPanel().getProperties( propsCont );
        
        propsCont.addProperty( new ListProperty<String, ArrayList<String>>( null, "templateConfig", "templateConfig", getCurrentTemplateFileForProperty(), getConfigurationFiles(), false, "reload" )
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
            public Object getValue()
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
        
        getEditorPanel().getWidgetsDrawingManager().getProperties( propsCont, false );
    }
    
    private static void readExpandFlags( FlaggedList list, String keyPrefix, HashMap<String, Boolean> map )
    {
        for ( int i = 0; i < list.size(); i++ )
        {
            if ( list.get( i ) instanceof FlaggedList )
            {
                FlaggedList fl = (FlaggedList)list.get( i );
                map.put( keyPrefix + fl.getName(), fl.getExpandFlag() );
                
                readExpandFlags( fl, keyPrefix, map );
            }
        }
    }
    
    private static void restoreExpandFlags( FlaggedList list, String keyPrefix, HashMap<String, Boolean> map )
    {
        for ( int i = 0; i < list.size(); i++ )
        {
            if ( list.get( i ) instanceof FlaggedList )
            {
                FlaggedList fl = (FlaggedList)list.get( i );
                Boolean b = map.get( keyPrefix + fl.getName() );
                if ( b != null )
                    fl.setExpandFlag( b.booleanValue() );
                
                restoreExpandFlags( fl, keyPrefix, map );
            }
        }
    }
    
    public void onWidgetSelected( Widget widget, boolean doubleClick )
    {
        editorPanel.setSelectedWidget( widget, doubleClick );
        
        FlaggedList propsList = propsEditor.getPropertiesList();
        
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
            editorTable.apply();
            onWidgetSelected( widget, false );
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
    
    private Object[] loadUserSettings()
    {
        final Object[] result = new Object[] { false, null };
        
        File userSettingsFile = getEditorSettingsFile();
        
        if ( !userSettingsFile.exists() )
            return ( result );
        
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
                        getEditorPanel().loadProperty( key, value );
                        
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
                        __EDPrivilegedAccess.loadProperty( presets, key, value );
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
            
            ConfigurationLoader.forceLoadConfiguration( templateConfigFile, templateConfig, gameData, presets, null );
            
            this.currentTemplateFile = templateConfigFile;
            this.lastTemplateConfigModified = templateConfigFile.lastModified();
        }
        
        return ( templateConfigFile );
    }
    
    public void openConfig( File configFile )
    {
        try
        {
            WidgetsDrawingManager widgetsManager = getEditorPanel().getWidgetsDrawingManager();
            int n = widgetsManager.getNumWidgets();
            for ( int i = 0; i < n; i++ )
            {
                widgetsManager.getWidget( i ).clearRegion( true, getOverlayTexture() );
            }
            
            ConfigurationLoader.forceLoadConfiguration( configFile, widgetsManager, gameData, presets, null );
            
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
            ConfigurationSaver.saveConfiguration( getEditorPanel().getWidgetsDrawingManager(), gameResolution.getResolutionString(), ep.getGridOffsetX(), ep.getGridOffsetY(), ep.getGridSizeX(), ep.getGridSizeY(), currentConfigFile );
            
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
        
        do
        {
            if ( fc.showSaveDialog( window ) != JFileChooser.APPROVE_OPTION )
                return ( null );
            
            if ( !fc.getSelectedFile().getName().endsWith( ".ini" ) )
                fc.setSelectedFile( new File( fc.getSelectedFile().getAbsolutePath() + ".ini" ) );
            
            if ( fc.getSelectedFile().exists() )
            {
                int result = JOptionPane.showConfirmDialog( window, "Do you want to overwrite the existing file \"" + fc.getSelectedFile().getAbsolutePath() + "\"?", window.getTitle(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
                
                if ( result == JOptionPane.CANCEL_OPTION )
                    return ( null );
                
                if ( result == JOptionPane.YES_OPTION )
                {
                    break;
                }
            }
        }
        while ( fc.getSelectedFile().exists() );
        
        currentConfigFile = fc.getSelectedFile();
        
        saveConfig();
        
        return ( currentConfigFile );
    }
    
    private void onCloseRequested()
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
    
    private void copyPropertiesFromTemplate( List<Property> lstTemplate, List<Property> lstTarget )
    {
        // We assume, that the order will be the same in both lists.
        
        for ( int i = 0; i < lstTemplate.size(); i++ )
        {
            if ( lstTemplate.get( i ) instanceof Property )
            {
                Property p0 = (Property)lstTemplate.get( i );
                Property p1 = (Property)lstTarget.get( i );
                
                if ( !p0.getPropertyName().equals( "x" ) && !p0.getPropertyName().equals( "y" ) && !p0.getPropertyName().equals( "positioning" ) )
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
    
    private static Widget createWidgetInstance( Class<?> widgetClass, WidgetsConfiguration widgetsConfig ) throws InvocationTargetException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, NoSuchMethodException
    {
        String name = ( widgetsConfig != null ) ? widgetsConfig.findFreeName( widgetClass.getSimpleName() ) : "";
        
        return ( (Widget)widgetClass.getConstructor( String.class ).newInstance( name ) );
    }
    
    private Widget addNewWidget( Class<?> widgetClazz )
    {
        Widget widget = null;
        
        try
        {
            Logger.log( "Creating and adding new Widget of type \"" + widgetClazz.getSimpleName() + "\"..." );
            
            //widget = (Widget)widgetClazz.getConstructor( RelativePositioning.class, int.class, int.class, int.class, int.class ).newInstance( RelativePositioning.TOP_LEFT, 0, 0, 100, 100 );
            widget = createWidgetInstance( widgetClazz, getEditorPanel().getWidgetsDrawingManager() );
            copyPropertiesFromTemplate( widget );
            getEditorPanel().getWidgetsDrawingManager().addWidget( widget );
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
    
    public void switchToGameResolution( int resX, int resY )
    {
        BufferedImage backgroundImage = loadBackgroundImage( resX, resY );
        __WCPrivilegedAccess.setGameResolution( resX, resY, editorPanel.getWidgetsDrawingManager() );
        __WCPrivilegedAccess.setViewport( 0, 0, resX, resY, editorPanel.getWidgetsDrawingManager() );
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
    
    private void showFullscreenPreview()
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
        
        DisplayMode dm = displayModes.get( gameResolution.getResolutionString() );
        
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
    
    private void takeScreenshot()
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
    
    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu( "File" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem open = new JMenuItem( "Open...", 0 );
        open.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                openConfig();
            }
        } );
        menu.add( open );
        
        menu.add( new JSeparator() );
        
        JMenuItem save = new JMenuItem( "Save", 0 );
        save.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_MASK ) );
        save.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                saveConfig();
            }
        } );
        menu.add( save );
        
        JMenuItem saveAs = new JMenuItem( "Save As...", 5 );
        saveAs.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                saveConfigAs();
            }
        } );
        menu.add( saveAs );
        
        menu.add( new JSeparator() );
        
        JMenuItem close = new JMenuItem( "Close", 0 );
        close.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                onCloseRequested();
            }
        } );
        menu.add( close );
        
        return ( menu );
    }
    
    private JMenuItem createSnapSelWidgetToGridMenu()
    {
        JMenuItem snapSelWidgetToGrid = new JMenuItem( "Snap selected Widget to grid" );
        snapSelWidgetToGrid.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
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
        } );
        
        return ( snapSelWidgetToGrid );
    }
    
    private JMenuItem createSnapAllWidgetsToGridMenu()
    {
        JMenuItem snapAllWidgetsToGrid = new JMenuItem( "Snap all Widgets to grid" );
        snapAllWidgetsToGrid.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                for ( int i = 0; i < getEditorPanel().getWidgetsDrawingManager().getNumWidgets(); i++ )
                    getEditorPanel().clearWidgetRegion( getEditorPanel().getWidgetsDrawingManager().getWidget( i ) );
                getEditorPanel().snapAllWidgetsToGrid();
                onWidgetSelected( getEditorPanel().getSelectedWidget(), false );
                getEditorPanel().repaint();
                setDirtyFlag();
            }
        } );
        
        return ( snapAllWidgetsToGrid );
    }
    
    private JMenuItem createRemoveWidgetMenu()
    {
        JMenuItem removeItem = new JMenuItem( "Remove selected Widget (DEL)" );
        //removeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ) );
        removeItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                getEditorPanel().removeSelectedWidget();
                onWidgetSelected( null, false );
            }
        } );
        
        return ( removeItem );
    }
    
    private JMenu createEditMenu()
    {
        JMenu menu = new JMenu( "Edit" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        final JMenuItem snapSelWidgetToGrid = createSnapSelWidgetToGridMenu();
        menu.add( snapSelWidgetToGrid );
        
        final JMenuItem snapAllWidgetsToGrid = createSnapAllWidgetsToGridMenu();
        menu.add( snapAllWidgetsToGrid );
        
        menu.addSeparator();
        
        final JMenuItem makeAllPixels = new JMenuItem( "Make all Widgets use Pixels" );
        makeAllPixels.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                int result = JOptionPane.showConfirmDialog( getMainWindow(), "Do you really want to convert all Widgets' positions and sizes to be absolute pixels?", "Convert all Widgets' coordinates", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( result == JOptionPane.YES_OPTION )
                {
                    WidgetsConfiguration wc = getEditorPanel().getWidgetsDrawingManager();
                    for ( int i = 0; i < wc.getNumWidgets(); i++ )
                        wc.getWidget( i ).setAllPosAndSizeToPixels();
                }
            }
        } );
        menu.add( makeAllPixels );
        
        final JMenuItem makeAllPercents = new JMenuItem( "Make all Widgets use Percents" );
        makeAllPercents.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                int result = JOptionPane.showConfirmDialog( getMainWindow(), "Do you really want to convert all Widgets' positions and sizes to be percents?", "Convert all Widgets' coordinates", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( result == JOptionPane.YES_OPTION )
                {
                    WidgetsConfiguration wc = getEditorPanel().getWidgetsDrawingManager();
                    for ( int i = 0; i < wc.getNumWidgets(); i++ )
                        wc.getWidget( i ).setAllPosAndSizeToPercents();
                }
            }
        } );
        menu.add( makeAllPercents );
        
        menu.addSeparator();
        
        final JMenuItem removeItem = createRemoveWidgetMenu();
        menu.add( removeItem );
        
        menu.addMenuListener( new MenuListener()
        {
            @Override
            public void menuSelected( MenuEvent e )
            {
                boolean hasSelected = ( getEditorPanel().getSelectedWidget() != null );
                boolean hasWidgets = ( getEditorPanel().getWidgetsDrawingManager().getNumWidgets() > 0 );
                
                snapSelWidgetToGrid.setEnabled( hasSelected && getEditorPanel().isGridUsed() );
                snapAllWidgetsToGrid.setEnabled( hasWidgets && getEditorPanel().isGridUsed() );
                removeItem.setEnabled( hasSelected );
                makeAllPixels.setEnabled( hasWidgets );
                makeAllPercents.setEnabled( hasWidgets );
            }
            
            @Override
            public void menuDeselected( MenuEvent e )
            {
            }
            
            @Override
            public void menuCanceled( MenuEvent e )
            {
            }
        } );
        
        return ( menu );
    }
    
    public void initContextMenu()
    {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem snapSelWidgetToGrid = createSnapSelWidgetToGridMenu();
        menu.add( snapSelWidgetToGrid );
        
        JMenuItem snapAllWidgetsToGrid = createSnapAllWidgetsToGridMenu();
        menu.add( snapAllWidgetsToGrid );
        
        JMenuItem removeItem = createRemoveWidgetMenu();
        menu.add( removeItem );
        
        boolean hasSelected = ( getEditorPanel().getSelectedWidget() != null );
        boolean hasWidgets = ( getEditorPanel().getWidgetsDrawingManager().getNumWidgets() > 0 );
        
        snapSelWidgetToGrid.setEnabled( hasSelected && getEditorPanel().isGridUsed() );
        snapAllWidgetsToGrid.setEnabled( hasWidgets && getEditorPanel().isGridUsed() );
        removeItem.setEnabled( hasSelected );
        
        getEditorPanel().setComponentPopupMenu( menu );
    }
    
    private JMenuItem createWidgetMenuItem( final Class<?> clazz )
    {
        //JMenuItem widgetMenuItem = new JMenuItem( clazz.getName() );
        JMenuItem widgetMenuItem = new JCheckBoxMenuItem( clazz.getSimpleName() );
        widgetMenuItem.setName( clazz.getName() );
        widgetMenuItem.addActionListener( new ActionListener()
        {
            private final Class<?> widgetClazz = clazz;
            
            public void actionPerformed( ActionEvent e )
            {
                addNewWidget( widgetClazz );
            }
        } );
        
        return ( widgetMenuItem );
    }
    
    /*
    @SuppressWarnings( "unchecked" )
    private void applyHierarchy( String[] path, int offset, HashMap<String, Object> map )
    {
        HashMap<String, Object> map2 = (HashMap<String, Object>)map.get( path[offset] );
        if ( map2 == null )
        {
            map2 = new HashMap<String, Object>();
            map.put( path[offset], map2 );
        }
        
        if ( offset < path.length - 1 )
        {
            applyHierarchy( path, offset + 1, map2 );
        }
    }
    
    @SuppressWarnings( "unchecked" )
    private void collapseSingleEntries( HashMap<String, Object> map )
    {
        ArrayList<String> keys = new ArrayList<String>( map.keySet() );
        
        for ( String key : keys )
        {
            HashMap<String, Object> map2 = (HashMap<String, Object>)map.get( key );
            
            if ( map2 != null )
            {
                if ( map2.size() == 1 )
                {
                    String key2 = map2.keySet().iterator().next();
                    String newKey = key + "/" + key2;
                }
                else if ( map2.size() > 1 )
                {
                    collapseSingleEntries( map2 );
                }
            }
        }
    }
    
    private HashMap<String, Object> buildHierarchy( List<Class<?>> classes, HashMap<Class<?>, Widget> instances )
    {
        HashMap<String, Object> hierarchy = new HashMap<String, Object>();
        
        for ( Class<?> clazz : classes )
        {
            try
            {
                Widget widget = createWidgetInstance( clazz, null );
                instances.put( clazz, widget );
                String pkg = widget.getWidgetPackage();
                
                String[] path = pkg.split( "/" );
                
                applyHierarchy( path, 0, hierarchy );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
        
        return ( hierarchy );
    }
    */
    
    private JMenu getMenu( JMenu parent, String[] pkg, int i )
    {
        for ( Component c : parent.getMenuComponents() )
        {
            if ( c instanceof JMenu )
            {
                JMenu m = (JMenu)c;
                
                if ( m.getText().equals( pkg[i] ) )
                {
                    if ( i < pkg.length - 1 )
                        return ( getMenu( m, pkg, i + 1 ) );
                    
                    return ( m );
                }
            }
        }
        
        JMenu m = new JMenu( pkg[i] );
        parent.add( m );
        
        if ( i < pkg.length - 1 )
            return ( getMenu( m, pkg, i + 1 ) );
        
        return ( m );
    }
    
    private JMenu createWidgetsMenu()
    {
        JMenu menu = new JMenu( "Widgets" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        List<String> packages = PackageSearcher.findPackages( "*widgets*" );
        List<Class<?>> classes = ClassSearcher.findClasses( new SuperClassCriterium( Widget.class, false ), packages.toArray( new String[ packages.size() ] ) );
        
        Collections.sort( classes, new Comparator< Class<?> >()
        {
            @Override
            public int compare( Class<?> o1, Class<?> o2 )
            {
                return ( String.CASE_INSENSITIVE_ORDER.compare( o1.getSimpleName(), o2.getSimpleName() ) );
            }
        } );
        
        HashMap<Class<?>, Widget> instances = new HashMap<Class<?>, Widget>();
        /*
        HashMap<String, Object> hierarchy = buildHierarchy( classes, instances );
        System.out.println( hierarchy );
        for ( Class<?> clazz : classes )
        {
            Widget widget = instances.get( clazz );
            String pkg = widget.getWidgetPackage();
            
            String[] path = pkg.split( "/" );
            
            applyHierarchy( path, 0, hierarchy );
        }
        */
        
        ArrayList<String> widgetPackages = new ArrayList<String>();
        Iterator<Class<?>> it = classes.iterator();
        while ( it.hasNext() )
        {
            Class<?> clazz = it.next();
            
            try
            {
                Widget widget = createWidgetInstance( clazz, null );
                instances.put( clazz, widget );
                widgetPackages.add( widget.getWidgetPackage() );
            }
            catch ( Throwable t )
            {
                it.remove();
                Logger.log( "Error handling Widget class " + clazz.getName() + ":" );
                Logger.log( t );
            }
        }
        
        Collections.sort( widgetPackages, String.CASE_INSENSITIVE_ORDER );
        
        for ( String widgetPackage : widgetPackages )
        {
            String[] pkg = widgetPackage.split( "/" );
            
            if ( ( pkg.length > 1 ) || !pkg[0].equals( "" ) )
                getMenu( menu, pkg, 0 );
        }
        
        it = classes.iterator();
        while ( it.hasNext() )
        {
            Class<?> clazz = it.next();
            
            try
            {
                Widget widget = instances.get( clazz );
                String[] pkg = widget.getWidgetPackage().split( "/" );
                
                if ( ( pkg.length == 1 ) && pkg[0].equals( "" ) )
                    menu.add( createWidgetMenuItem( clazz ) );
                else
                    getMenu( menu, pkg, 0 ).add( createWidgetMenuItem( clazz ) );
            }
            catch ( Throwable t )
            {
                it.remove();
                Logger.log( "Error handling Widget class " + clazz.getName() + ":" );
                Logger.log( t );
            }
        }
        
        menu.addMenuListener( new MenuListener()
        {
            private void checkWidgetUsed( JMenuItem item )
            {
                if ( item instanceof JMenu )
                {
                    for ( Component mi : ( (JMenu)item ).getMenuComponents() )
                    {
                        if ( mi instanceof JMenuItem )
                            checkWidgetUsed( (JMenuItem)mi );
                    }
                }
                
                item.setSelected( false );
                
                WidgetsConfiguration widgetsConfig = getEditorPanel().getWidgetsDrawingManager();
                int n = widgetsConfig.getNumWidgets();
                for ( int i = 0; i < n; i++ )
                {
                    if ( widgetsConfig.getWidget( i ).getClass().getName().equals( item.getName() ) )
                    {
                        item.setSelected( true );
                        break;
                    }
                }
            }
            
            @Override
            public void menuSelected( MenuEvent e )
            {
                JMenu menu = (JMenu)e.getSource();
                
                for ( Component mi : menu.getMenuComponents() )
                {
                    if ( mi instanceof JMenuItem )
                        checkWidgetUsed( (JMenuItem)mi );
                }
            }
            
            @Override
            public void menuDeselected( MenuEvent e )
            {
            }
            
            @Override
            public void menuCanceled( MenuEvent e )
            {
            }
        } );
        
        return ( menu );
    }
    
    private static class DM implements Comparable<DM>
    {
        private static final float sysA;
        static
        {
            DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
            
            sysA = Math.round( (float)dm.getWidth() * 100f / (float)dm.getHeight() ) / 100f;
        }
        
        public final int w;
        public final int h;
        public final float a;
        
        public int compareTo( DM o )
        {
            if ( this.a != o.a )
            {
                return ( Float.compare( Math.abs( this.a - sysA ), Math.abs( o.a - sysA ) ) );
            }
            
            if ( this.w < o.w )
                return ( -1 );
            
            if ( this.w > o.w )
                return ( +1 );
            
            if ( this.h < o.h )
                return ( -1 );
            
            if ( this.h > o.h )
                return ( +1 );
            
            return ( 0 );
        }
        
        @Override
        public boolean equals( Object o )
        {
            if ( o == this )
                return ( true );
            
            if ( !( o instanceof DM ) )
                return ( false );
            
            return ( ( this.w == ( (DM)o ).w ) && ( this.h == ( (DM)o ).h ) );
        }
        
        @Override
        public int hashCode()
        {
            return ( ( ( w & 0xFFFF ) << 16 ) | ( ( h & 0xFFFF ) << 0 ) );
        }
        
        public DM( int w, int h )
        {
            this.w = w;
            this.h = h;
            this.a = Math.round( (float)w * 100f / (float)h ) / 100f;
        }
    }
    
    private boolean checkResolution( int resX, int resY )
    {
        return ( getBackgroundImageFile( resX, resY ).exists() );
    }
    
    private JMenu createResolutionsMenu()
    {
        JMenu resMenu = new JMenu( "Resolutions" );
        resMenu.setDisplayedMnemonicIndex( 0 );
        
        HashSet<DM> set = new HashSet<DM>();
        for ( DisplayMode dm : displayModes.values() )
        {
            set.add( new DM( dm.getWidth(), dm.getHeight() ) );
        }
        
        DM[] array = new DM[ set.size() ];
        int i = 0;
        for ( DM dm : set )
        {
            array[i++] = dm;
        }
        Arrays.sort( array );
        
        float lastA = array[0].a;
        JMenuItem item;
        for ( DM dm : array )
        {
            if ( dm.a != lastA )
            {
                resMenu.add( new JSeparator() );
                lastA = dm.a;
            }
            
            final String resString = dm.w + "x" + dm.h;
            
            if ( !checkResolution( dm.w, dm.h ) )
            {
                item = new JMenuItem( resString + " [" + dm.a + "] (no screenshot available)" );
                item.setName( dm.w + "x" + dm.h );
                item.setEnabled( false );
            }
            else
            {
                item = new JCheckBoxMenuItem( resString + " [" + dm.a + "]" );
                item.setName( dm.w + "x" + dm.h );
                
                item.setActionCommand( resString );
                final DM dm2 = dm;
                item.addActionListener( new ActionListener()
                {
                    public void actionPerformed( ActionEvent e )
                    {
                        Logger.log( "Switching to resolution " + dm2.w + "x" + dm2.h + "..." );
                        
                        switchToGameResolution( dm2.w, dm2.h );
                    }
                } );
            }
            
            resMenu.add( item );
        }
        
        resMenu.addMenuListener( new MenuListener()
        {
            @Override
            public void menuSelected( MenuEvent e )
            {
                String resString = gameResolution.getResolutionString();
                
                JMenu menu = (JMenu)e.getSource();
                
                for ( Component mi : menu.getMenuComponents() )
                {
                    if ( mi instanceof JMenuItem )
                        ( (JMenuItem)mi ).setSelected( resString.equals( mi.getName() ) );
                }
            }
            
            @Override
            public void menuDeselected( MenuEvent e )
            {
            }
            
            @Override
            public void menuCanceled( MenuEvent e )
            {
            }
        } );
        
        return ( resMenu );
    }
    
    private JMenu createToolsMenu()
    {
        JMenu menu = new JMenu( "Tools" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem previewItem = new JMenuItem( "Show fullscreen preview..." );
        previewItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F11, 0 ) );
        previewItem.addActionListener( new ActionListener()
        {
            private long lastWhen = -1L;
            
            public void actionPerformed( ActionEvent e )
            {
                if ( e.getWhen() < lastWhen + 1500L )
                    return;
                
                showFullscreenPreview();
                
                lastWhen = e.getWhen();
            }
        } );
        menu.add( previewItem );
        
        JMenuItem screenshotItem = new JMenuItem( "Take Screenshot" );
        screenshotItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F12, 0 ) );
        screenshotItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                takeScreenshot();
            }
        } );
        menu.add( screenshotItem );
        
        menu.addSeparator();
        
        JMenuItem manangerItem = new JMenuItem( "Open Strategy Calculator..." );
        manangerItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                StrategyTool.showStrategyTool( RFDynHUDEditor.this.window );
            }
        } );
        menu.add( manangerItem );
        
        menu.addSeparator();
        
        JMenuItem inputMgrItem = new JMenuItem( "Open InputBindingsManager..." );
        inputMgrItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                InputBindingsGUI.showInputBindingsGUI( RFDynHUDEditor.this );
            }
        } );
        
        menu.add( inputMgrItem );
        
        menu.addSeparator();
        
        JMenuItem optionsItem = new JMenuItem( "Editor Presets..." );
        optionsItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                presetsWindow.setVisible( true );
            }
        } );
        menu.add( optionsItem );
        
        return ( menu );
    }
    
    private JMenu createHelpMenu()
    {
        JMenu menu = new JMenu( "Help" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem help = new JMenuItem( "Help" );
        help.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                alwaysShowHelpOnStartup = HelpWindow.showHelpWindow( RFDynHUDEditor.this.window, alwaysShowHelpOnStartup ).getAlwaysShowOnStartup();
            }
        } );
        
        menu.add( help );
        
        menu.addSeparator();
        
        JMenuItem about = new JMenuItem( "About" );
        about.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                AboutPage.showAboutPage( RFDynHUDEditor.this.window );
            }
        } );
        
        menu.add( about );
        
        return ( menu );
    }
    
    private void createMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        
        menuBar.add( createFileMenu() );
        menuBar.add( createEditMenu() );
        menuBar.add( createWidgetsMenu() );
        menuBar.add( createResolutionsMenu() );
        menuBar.add( createToolsMenu() );
        menuBar.add( createHelpMenu() );
        
        window.setJMenuBar( menuBar );
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
    
    private GameEventsManager eventsManager;
    
    private EditorPanel createEditorPanel()
    {
        int[] resolution = loadResolutionFromUserSettings();
        
        WidgetsDrawingManager drawingManager = new WidgetsDrawingManager( resolution[0], resolution[1] );
        
        eventsManager = new GameEventsManager( null, drawingManager );
        this.gameData = new LiveGameData( drawingManager.getGameResolution(), eventsManager );
        __GDPrivilegedAccess.updateProfileInfo( gameData.getProfileInfo() );
        eventsManager.setGameData( gameData );
        
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
        
        this.window = new EditorWindow( BASE_WINDOW_TITLE );
        
        window.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        
        createMenu();
        
        Container contentPane = window.getContentPane();
        contentPane.setLayout( new BorderLayout() );
        
        this.editorPanel = createEditorPanel();
        this.gameResolution = editorPanel.getWidgetsDrawingManager().getGameResolution();
        
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
        contentPane.add( split );
        
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
            
            __GDPrivilegedAccess.loadEditorDefaults( editor.gameData.getPhysics() );
            __GDPrivilegedAccess.loadSetup( true, editor.gameData );
            __GDPrivilegedAccess.updateInfo( editor.gameData );
            
            editor.eventsManager.onSessionStarted( editor.presets );
            editor.eventsManager.onTelemetryDataUpdated( editor.presets );
            editor.eventsManager.onScoringInfoUpdated( editor.presets );
            
            __GDPrivilegedAccess.setRealtimeMode( true, editor.gameData, editor.presets );
            initTestGameData( editor.gameData, editor.presets );
            
            if ( editor.currentConfigFile == null )
            {
                File configFile = (File)result[1];
                if ( configFile == null )
                    configFile = new File( GameFileSystem.INSTANCE.getConfigFolder(), "overlay.ini" );
                if ( configFile.exists() )
                    editor.openConfig( configFile );
                else
                    ConfigurationLoader.loadFactoryDefaults( editor.getEditorPanel().getWidgetsDrawingManager(), editor.gameData, editor.presets, null );
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
