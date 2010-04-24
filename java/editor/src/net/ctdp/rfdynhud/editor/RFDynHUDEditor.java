package net.ctdp.rfdynhud.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.help.AboutPage;
import net.ctdp.rfdynhud.editor.help.HelpWindow;
import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.input.InputBindingsGUI;
import net.ctdp.rfdynhud.editor.options.OptionsWindow;
import net.ctdp.rfdynhud.editor.options.ScaleType;
import net.ctdp.rfdynhud.editor.properties.DefaultWidgetPropertiesContainer;
import net.ctdp.rfdynhud.editor.properties.EditorTable;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditor;
import net.ctdp.rfdynhud.editor.properties.PropertySelectionListener;
import net.ctdp.rfdynhud.editor.properties.WidgetPropertyChangeListener;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.editor.util.DefaultWidgetsConfigurationWriter;
import net.ctdp.rfdynhud.editor.util.StrategyTool;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.RFactorEventsManager;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.util.Tools;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
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
    
    private int gameResX;
    private int gameResY;
    
    private String screenshotSet = "default";
    
    private boolean alwaysShowHelpOnStartup = true;
    
    private final JFrame window;
    private final EditorPanel editorPanel;
    
    private final PropertiesEditor propsEditor;
    private final EditorTable editorTable;
    private final JEditorPane docPanel;
    
    private final EditorPresets presets = new EditorPresets();
    
    private static final String doc_header = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_header.html" ) );
    private static final String doc_footer = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_footer.html" ) );
    
    private boolean optionsWindowVisible = false;
    private final OptionsWindow optionsWindow;
    
    private boolean dirtyFlag = false;
    
    private File currentConfigFile = null;
    private File currentTemplateFile = null;
    
    private WidgetsConfiguration templateConfig = null;
    private long lastTemplateConfigModified = -1L;
    
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
    
    public final int getGameResX()
    {
        return ( gameResX );
    }
    
    public final int getGameResY()
    {
        return ( gameResY );
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
            if ( currentDocedWidget != editorPanel.getSelectedWidget() )
            {
                if ( editorPanel.getSelectedWidget() == null )
                {
                    docPanel.setText( doc_header + "" + doc_footer );
                }
                else
                {
                    String helpText = editorPanel.getSelectedWidget().getDocumentationSource( null );
                    if ( helpText == null )
                        docPanel.setText( doc_header + "" + doc_footer );
                    else
                        docPanel.setText( doc_header + helpText + doc_footer );
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
    
    private void getProperties( WidgetPropertiesContainer propsCont )
    {
        propsCont.addGroup( "General" );
        
        propsCont.addProperty( new Property( null, "resolution", true, PropertyEditorType.STRING )
        {
            @Override
            public void setValue( Object value )
            {
            }
            
            @Override
            public Object getValue()
            {
                return ( gameResX + "x" + gameResY );
            }
        } );
        
        getEditorPanel().getProperties( propsCont );
        
        propsCont.addProperty( new Property( null, "screenshotSet", true, PropertyEditorType.STRING )
        {
            @Override
            public void setValue( Object value )
            {
            }
            
            @Override
            public Object getValue()
            {
                return ( screenshotSet );
            }
        } );
        
        propsCont.addProperty( new Property( null, "templateConfig", true, PropertyEditorType.STRING )
        {
            @Override
            public void setValue( Object value )
            {
            }
            
            @Override
            public Object getValue()
            {
                if ( currentTemplateFile == null )
                    return ( "none" );
                
                return ( currentTemplateFile.getAbsolutePath().substring( RFactorTools.CONFIG_PATH.length() + 1 ) );
            }
        } );
    }
    
    private static void readExpandFlags( FlaggedList list, String keyPrefix, HashMap<String, Boolean> map )
    {
        for ( int i = 0; i < list.size(); i++ )
        {
            if ( list.get( i ) instanceof FlaggedList )
            {
                FlaggedList fl = (FlaggedList)list.get( i );
                map.put( keyPrefix + fl.getName(), fl.getExpandFlag() );
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
            }
        }
    }
    
    public void onWidgetSelected( Widget widget )
    {
        editorPanel.setSelectedWidget( widget );
        
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
            widget.getProperties( new DefaultWidgetPropertiesContainer( propsList ) );
        }
        
        onPropertySelected( null, -1 );
        restoreExpandFlags( propsList, "", expandedRows );
        
        editorTable.apply();
    }
    
    //private long nextRedrawTime = -1L;
    
    public void onWidgetChanged( Widget widget, String propertyName, boolean isSelected )
    {
        //if ( System.currentTimeMillis() >= nextRedrawTime )
        {
            getEditorPanel().repaint();
            //nextRedrawTime = System.currentTimeMillis() + 1000L;
        }
        
        setDirtyFlag();
        
        if ( isSelected )
            editorTable.apply();
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
        return ( new File( RFactorTools.EDITOR_PATH ) );
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
            if ( currentConfigFilename.startsWith( RFactorTools.CONFIG_PATH ) )
            {
                if ( currentConfigFilename.charAt( RFactorTools.CONFIG_PATH.length() ) == File.separatorChar )
                    currentConfigFilename = currentConfigFilename.substring( RFactorTools.CONFIG_PATH.length() + 1 );
                else
                    currentConfigFilename = currentConfigFilename.substring( RFactorTools.CONFIG_PATH.length() );
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
            writer.writeSetting( "resolution", gameResX + "x" + gameResY );
            getEditorPanel().saveProperties( confWriter );
            writer.writeSetting( "defaultScaleType", optionsWindow.getDefaultScaleType() );
            writer.writeSetting( "screenshotSet", screenshotSet );
            writeLastConfig( writer );
            writer.writeSetting( "alwaysShowHelpOnStartup", alwaysShowHelpOnStartup );
            writer.writeGroup( "MainWindow" );
            writer.writeSetting( "windowLocation", getMainWindow().getX() + "x" + getMainWindow().getY() );
            writer.writeSetting( "windowSize", getMainWindow().getWidth() + "x" + getMainWindow().getHeight() );
            writer.writeSetting( "windowState", extendedState );
            writer.writeGroup( "OptionsWindow" );
            writer.writeSetting( "windowLocation", optionsWindow.getX() + "x" + optionsWindow.getY() );
            writer.writeSetting( "windowSize", optionsWindow.getWidth() + "x" + optionsWindow.getHeight() );
            writer.writeSetting( "windowVisible", optionsWindowVisible );
            writer.writeSetting( "autoApply", optionsWindow.getAutoApply() );
            writer.writeGroup( "EditorPresets" );
            
            EDPrivilegedAccess.saveProperties( presets, confWriter );
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
            GraphicsDevice graphDev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode dm = graphDev.getDisplayMode();
            
            resolution[0] = dm.getWidth();
            resolution[1] = dm.getHeight();
        }
        
        return ( resolution );
    }
    
    private boolean loadUserSettings()
    {
        File userSettingsFile = getEditorSettingsFile();
        
        if ( !userSettingsFile.exists() )
            return ( false );
        
        try
        {
            new AbstractIniParser()
            {
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( group == null )
                    {
                        
                    }
                    else if ( group.equals( "General" ) )
                    {
                        getEditorPanel().loadProperty( key, value );
                        
                        if ( key.equals( "resolution" ) )
                        {
                            try
                            {
                                String[] ss = value.split( "x" );
                                int resX = Integer.parseInt( ss[0] );
                                int resY = Integer.parseInt( ss[1] );
                                if ( ( ( gameResX != resX ) || ( gameResY != resY ) ) && checkResolution( resX, resY ) )
                                    switchToGameResolution( resX, resY );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "defaultScaleType" ) )
                        {
                            try
                            {
                                optionsWindow.setDefaultScaleType( ScaleType.valueOf( value ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                        else if ( key.equals( "screenshotSet" ) )
                        {
                            screenshotSet = value;
                        }
                        else if ( key.equals( "lastConfig" ) )
                        {
                            File configFile = new File( value );
                            if ( !configFile.isAbsolute() )
                                configFile = new File( RFactorTools.CONFIG_PATH, value );
                            if ( configFile.exists() )
                                openConfig( configFile );
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
                    else if ( group.equals( "OptionsWindow" ) )
                    {
                        if ( key.equals( "windowLocation" ) )
                        {
                            try
                            {
                                //optionsWindow.setLocationRelativeTo( null );
                                String[] ss = value.split( "x" );
                                optionsWindow.setLocation( Integer.parseInt( ss[0] ), Integer.parseInt( ss[1] ) );
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
                                optionsWindow.setSize( Integer.parseInt( ss[0] ), Integer.parseInt( ss[1] ) );
                                optionsWindow.setDontSetWindowSize();
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
                                optionsWindowVisible = Boolean.parseBoolean( value );
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
                                optionsWindow.setAutoApply( Boolean.parseBoolean( value ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                    }
                    else if ( group.equals( "EditorPresets" ) )
                    {
                        EDPrivilegedAccess.loadProperty( presets, key, value );
                    }
                    
                    return ( true );
                }
            }.parse( userSettingsFile );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        return ( true );
    }
    
    private File loadTemplateConfig( File configFile ) throws IOException
    {
        File path = configFile.getParentFile();
        File templateConfigFile = new File( path, "templates.ini" );
        while ( !templateConfigFile.exists() && !path.equals( RFactorTools.CONFIG_FOLDER ) )
        {
            path = path.getParentFile();
            templateConfigFile = new File( path, "templates.ini" );
        }
        
        if ( !templateConfigFile.exists() )
            return ( null );
        
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
            currentTemplateFile = loadTemplateConfig( configFile );
            
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
            onWidgetSelected( null );
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
                fc.setCurrentDirectory( new File( RFactorTools.CONFIG_PATH ) );
                fc.setSelectedFile( new File( RFactorTools.CONFIG_PATH, "overlay.ini" ) );
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
            ConfigurationSaver.saveConfiguration( getEditorPanel().getWidgetsDrawingManager(), gameResX + "x" + gameResY, currentConfigFile );
            
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
            fc.setCurrentDirectory( new File( RFactorTools.CONFIG_PATH ) );
            fc.setSelectedFile( new File( RFactorTools.CONFIG_PATH, "overlay.ini" ) );
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
        
        optionsWindowVisible = optionsWindow.isVisible();
        optionsWindow.setVisible( false );
        
        getMainWindow().setVisible( false );
        int extendedState = getMainWindow().getExtendedState();
        getMainWindow().setExtendedState( JFrame.NORMAL );
        saveUserSettings( extendedState );
        
        optionsWindow.dispose();
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
        
        template.getProperties( pcTemplate );
        widget.getProperties( pcTarget );
        
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
            if ( optionsWindow.getDefaultScaleType() == ScaleType.PERCENTS )
                widget.setAllPosAndSizeToPercents();
            else if ( optionsWindow.getDefaultScaleType() == ScaleType.ABSOLUTE_PIXELS )
                widget.setAllPosAndSizeToPixels();
            onWidgetSelected( widget );
            getEditorPanel().repaint();
            
            setDirtyFlag();
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        return ( widget );
    }
    
    private File getBackgroundImageFile( int width, int height )
    {
        return ( new File( RFactorTools.EDITOR_PATH + File.separator + "backgrounds" + File.separator + screenshotSet + File.separator + "background_" + width + "x" + height + ".jpg" ) );
    }
    
    public BufferedImage loadBackgroundImage( int width, int height )
    {
        try
        {
            return ( ImageIO.read( getBackgroundImageFile( width, height ) ) );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            
            return ( null );
        }
    }
    
    public void switchToGameResolution( int resX, int resY )
    {
        BufferedImage backgroundImage = loadBackgroundImage( resX, resY );
        this.gameResX = backgroundImage.getWidth();
        this.gameResY = backgroundImage.getHeight();
        TransformableTexture overlayTexture = __RenderPrivilegedAccess.createMainTexture( gameResX, gameResY );
        
        editorPanel.setBackgroundImage( backgroundImage );
        editorPanel.setOverlayTexture( overlayTexture.getTexture() );
        editorPanel.setPreferredSize( new Dimension( gameResX, gameResY ) );
        editorPanel.setMaximumSize( new Dimension( gameResX, gameResY ) );
        editorPanel.setMinimumSize( new Dimension( gameResX, gameResY ) );
        __WCPrivilegedAccess.setGameResolution( gameResX, gameResY, editorPanel.getWidgetsDrawingManager() );
        ( (JScrollPane)editorPanel.getParent().getParent() ).doLayout();
        
        onWidgetSelected( editorPanel.getSelectedWidget() );
        
        int numWidgets = editorPanel.getWidgetsDrawingManager().getNumWidgets();
        for ( int i = 0; i < numWidgets; i++ )
        {
            editorPanel.getWidgetsDrawingManager().getWidget( i ).forceReinitialization();
        }
        
        getMainWindow().validate();
        getEditorPanel().repaint();
    }
    
    public void switchScreenshotSet( String screenshotSet )
    {
        Logger.log( "Switching to Screenshot Set \"" + screenshotSet + "\"..." );
        
        this.screenshotSet = screenshotSet;
        switchToGameResolution( gameResX, gameResY );
    }
    
    private void takeScreenshot()
    {
        BufferedImage img = new BufferedImage( gameResX, gameResY, BufferedImage.TYPE_3BYTE_BGR );
        
        getEditorPanel().drawWidgets( img.createGraphics(), true );
        
        try
        {
            File folder = RFactorTools.getUserScreenShotsFolder();
            folder.mkdirs();
            
            String filenameBase = ( currentConfigFile == null ) ? "rfDynHUD_screenshot_" : "rfDynHUD_" + currentConfigFile.getName().replace( ".", "_" ) + "_";
            int i = 0;
            File f = new File( folder, filenameBase + Tools.padLeft( i, 3, "0" ) + ".png" );
            while ( f.exists() )
            {
                i++;
                f = new File( folder, filenameBase + Tools.padLeft( i, 3, "0" ) + ".png" );
            }
            
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
    
    private JMenu createEditMenu()
    {
        JMenu menu = new JMenu( "Edit" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem makeAllPixels = new JMenuItem( "Make all Widgets use Pixels" );
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
        
        JMenuItem makeAllPercents = new JMenuItem( "Make all Widgets use Percents" );
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
        
        JMenuItem removeItem = new JMenuItem( "Remove selected Widget (DEL)" );
        //removeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ) );
        removeItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                getEditorPanel().removeSelectedWidget();
                onWidgetSelected( null );
            }
        } );
        
        menu.add( removeItem );
        
        return ( menu );
    }
    
    private JMenuItem createWidgetMenuItem( final Class<?> clazz )
    {
        //JMenuItem widgetMenuItem = new JMenuItem( clazz.getName() );
        JMenuItem widgetMenuItem = new JMenuItem( clazz.getSimpleName() );
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
    
    private JMenu createScreenshotSetsMenu()
    {
        JMenu menu = new JMenu( "Screenshot Set" );
        
        File root = new File( RFactorTools.EDITOR_PATH + File.separator + "backgrounds" );
        for ( File f : root.listFiles() )
        {
            if ( f.isDirectory() && !f.getName().toLowerCase().equals( ".svn" ) )
            {
                final JMenuItem mi = new JMenuItem( f.getName() );
                mi.addActionListener( new ActionListener()
                {
                    public void actionPerformed( ActionEvent e )
                    {
                        switchScreenshotSet( mi.getText() );
                    }
                } );
                
                menu.add( mi );
            }
        }
        
        return ( menu );
    }
    
    private JMenu createResolutionsMenu()
    {
        JMenu resMenu = new JMenu( "Resolutions" );
        resMenu.setDisplayedMnemonicIndex( 0 );
        
        JMenu sssMenu = createScreenshotSetsMenu();
        resMenu.setDisplayedMnemonicIndex( 0 );
        resMenu.add( sssMenu );
        resMenu.add( new JSeparator() );
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        HashSet<DM> set = new HashSet<DM>();
        for ( DisplayMode dm : ge.getDefaultScreenDevice().getDisplayModes() )
        {
            if ( ( dm.getWidth() >= 800 ) && ( dm.getWidth() >= 600 ) )
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
                item.setEnabled( false );
            }
            else
            {
                item = new JMenuItem( resString + " [" + dm.a + "]" );
                
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
        
        return ( resMenu );
    }
    
    private JMenu createInputMenu()
    {
        JMenu menu = new JMenu( "Input" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem manangerItem = new JMenuItem( "Open InputBindingsManager..." );
        manangerItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                InputBindingsGUI.showInputBindingsGUI( RFDynHUDEditor.this );
            }
        } );
        
        menu.add( manangerItem );
        
        return ( menu );
    }
    
    private JMenu createToolsMenu()
    {
        JMenu menu = new JMenu( "Tools" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem screenshotItem = new JMenuItem( "Take Screenshot" );
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
        
        JMenuItem optionsItem = new JMenuItem( "Options..." );
        optionsItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                optionsWindow.setVisible( true );
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
        menuBar.add( createInputMenu() );
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
            __GDPrivilegedAccess.loadFromStream( in, gameData.getCommentaryRequestInfo() );
            in.close();
            
            //in = new FileInputStream( "data/game_data/graphics_info" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/graphics_info" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getGraphicsInfo() );
            in.close();
            
            //in = new FileInputStream( "data/game_data/scoring_info" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/scoring_info" );
            __GDPrivilegedAccess.loadFromStream( in, editorPresets, gameData.getScoringInfo() );
            in.close();
            
            //in = new FileInputStream( "data/game_data/telemetry_data" );
            in = LiveGameData.class.getResourceAsStream( "/data/game_data/telemetry_data" );
            __GDPrivilegedAccess.loadFromStream( in, gameData.getTelemetryData() );
            in.close();
        }
        catch ( IOException e )
        {
            throw new Error( e );
        }
    }
    
    private RFactorEventsManager eventsManager;
    
    private EditorPanel createEditorPanel()
    {
        int[] resolution = loadResolutionFromUserSettings();
        
        BufferedImage backgroundImage = loadBackgroundImage( resolution[0], resolution[1] );
        this.gameResX = backgroundImage.getWidth();
        this.gameResY = backgroundImage.getHeight();
        TransformableTexture overlayTexture = __RenderPrivilegedAccess.createMainTexture( gameResX, gameResY );
        WidgetsDrawingManager drawingManager = new WidgetsDrawingManager( overlayTexture );
        
        eventsManager = new RFactorEventsManager( drawingManager, null );
        this.gameData = new LiveGameData( eventsManager );
        eventsManager.setGameData( gameData );
        
        EditorPanel editorPanel = new EditorPanel( this, backgroundImage, gameData, overlayTexture.getTexture(), drawingManager );
        editorPanel.setPreferredSize( new Dimension( gameResX, gameResY ) );
        
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
        
        __UtilPrivilegedAccess.setLoggerEditorMode();
        
        //ByteOrderInitializer.setByteOrder( 3, 2, 1, 0 );
        ByteOrderInitializer.setByteOrder( 0, 1, 2, 3 );
        
        this.window = new EditorWindow( BASE_WINDOW_TITLE );
        
        window.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        
        createMenu();
        
        Container contentPane = window.getContentPane();
        contentPane.setLayout( new BorderLayout() );
        
        this.editorPanel = createEditorPanel();
        
        JScrollPane scrollPane = new JScrollPane( editorPanel );
        scrollPane.getHorizontalScrollBar().setUnitIncrement( 20 );
        scrollPane.getVerticalScrollBar().setUnitIncrement( 20 );
        scrollPane.getViewport().setScrollMode( JViewport.SIMPLE_SCROLL_MODE );
        scrollPane.setPreferredSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
        
        JSplitPane split = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        split.setResizeWeight( 1 );
        split.add( scrollPane );
        
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
        
        this.optionsWindow = new OptionsWindow( this );
        
        window.addWindowListener( new WindowAdapter()
        {
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
            
            if ( !editor.loadUserSettings() )
            {
                if ( editor.checkResolution( dm.getWidth(), dm.getHeight() ) )
                    editor.switchToGameResolution( dm.getWidth(), dm.getHeight() );
            }
            
            if ( editor.currentConfigFile == null )
            {
                File configFile = new File( RFactorTools.CONFIG_PATH, "overlay.ini" );
                if ( configFile.exists() )
                    editor.openConfig( configFile );
                else
                    ConfigurationLoader.loadFactoryDefaults( editor.getEditorPanel().getWidgetsDrawingManager(), editor.gameData, editor.presets, null );
            }
            
            __GDPrivilegedAccess.loadEditorDefaults( editor.gameData.getPhysics() );
            VehicleSetup.loadEditorDefaults( editor.gameData );
            
            initTestGameData( editor.gameData, editor.presets );
            
            editor.eventsManager.onSessionStarted( editor.presets );
            editor.eventsManager.onRealtimeEntered( editor.presets );
            
            //editor.getEditorPanel().getWidgetsDrawingManager().collectTextures( true, editor.gameData );
            
            editor.getMainWindow().addWindowListener( new WindowAdapter()
            {
                private boolean shot = false;
                
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
            
            if ( editor.optionsWindowVisible )
                editor.optionsWindow.setVisible( true );
            
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
