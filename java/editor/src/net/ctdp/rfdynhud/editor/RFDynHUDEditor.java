package net.ctdp.rfdynhud.editor;

import java.awt.BorderLayout;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLDocument;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.hiergrid.HierarchicalTableModel;
import net.ctdp.rfdynhud.editor.properties.PropertiesEditor;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.editor.util.ConfigurationSaver;
import net.ctdp.rfdynhud.editor.util.StrategyTool;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.RFactorEventsManager;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
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
public class RFDynHUDEditor implements Documented
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
    
    private static final String BASE_WINDOW_TITLE = "RFactor dynamic HUD Editor";
    
    private LiveGameData gameData;
    
    private int gameResX;
    private int gameResY;
    
    private String screenshotSet = "default";
    
    private final JFrame window;
    private final EditorPanel editorPanel;
    
    private final PropertiesEditor propsEditor;
    private final JEditorPane docPanel;
    
    private static final String doc_header = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_header.html" ) );
    private static final String doc_footer = StringUtil.loadString( RFDynHUDEditor.class.getResource( "net/ctdp/rfdynhud/editor/properties/doc_footer.html" ) );
    
    private boolean dirtyFlag = false;
    
    private File currentConfigFile = null;
    
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
        /*
        URL docURL = null;
        
        if ( property == null )
            docURL = this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/doc/widget.html" );
        else
            docURL = this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/doc/" + property.getKey() + ".html" );
        
        if ( docURL == null )
            return ( "" );
        
        return ( StringUtil.loadString( docURL ) );
        */
        return ( "" );
    }
    
    public void onPropertySelected( Property property )
    {
        if ( property == null )
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
                
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Thread.sleep( 200L );
                        }
                        catch ( Throwable t )
                        {
                        }
                        
                        JScrollPane sp = (JScrollPane)docPanel.getParent().getParent();
                        sp.getHorizontalScrollBar().setValue( 0 );
                        sp.getVerticalScrollBar().setValue( 0 );
                    }
                }.start();
            }
        }
        else if ( editorPanel.getSelectedWidget() == null )
        {
            docPanel.setText( doc_header + getDocumentationSource( property ) + doc_footer );
        }
        else
        {
            docPanel.setText( doc_header + editorPanel.getSelectedWidget().getDocumentationSource( property ) + doc_footer );
        }
    }
    
    private void getProperties( FlaggedList propsList )
    {
        FlaggedList props = new FlaggedList( "General", true );
        
        props.add( new Property( "resolution", true, PropertyEditorType.STRING )
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
        
        props.add( new Property( "screenshotSet", true, PropertyEditorType.STRING )
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
        
        propsList.add( props );
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
            getProperties( propsList );
        }
        else
        {
            widget.getProperties( propsList );
        }
        
        onPropertySelected( null );
        restoreExpandFlags( propsList, "", expandedRows );
        
        propsEditor.apply();
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
            propsEditor.apply();
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
    
    private void saveUserSettings( int extendedState )
    {
        File userSettingsFile = new File( getSettingsDir(), "editor_settings.ini" );
        
        IniWriter writer = null;
        try
        {
            writer = new IniWriter( userSettingsFile );
            
            writer.writeSetting( "resolution", gameResX + "x" + gameResY );
            writer.writeSetting( "windowLocation", getMainWindow().getX() + "x" + getMainWindow().getY() );
            writer.writeSetting( "windowSize", getMainWindow().getWidth() + "x" + getMainWindow().getHeight() );
            writer.writeSetting( "windowState", extendedState );
            writer.writeSetting( "screenshotSet", screenshotSet );
            
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
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close();
                }
                catch ( Throwable t )
                {
                }
            }
        }
    }
    
    private boolean loadUserSettings()
    {
        File userSettingsFile = new File( getSettingsDir(), "editor_settings.ini" );
        
        if ( !userSettingsFile.exists() )
            return ( false );
        
        try
        {
            new AbstractIniParser()
            {
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( key.equals( "resolution" ) )
                    {
                        if ( checkResolution( value ) )
                        {
                            try
                            {
                                String[] ss = value.split( "x" );
                                switchToGameResolution( Integer.parseInt( ss[0] ), Integer.parseInt( ss[1] ) );
                            }
                            catch ( Throwable t )
                            {
                                t.printStackTrace();
                            }
                        }
                    }
                    else if ( key.equals( "windowLocation" ) )
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
                    else if ( key.equals( "lastConfig" ) )
                    {
                        File configFile = new File( value );
                        if ( !configFile.isAbsolute() )
                            configFile = new File( RFactorTools.CONFIG_PATH, value );
                        if ( configFile.exists() )
                            openConfig( configFile );
                    }
                    else if ( key.equals( "screenshotSet" ) )
                    {
                        screenshotSet = value;
                    }
                    
                    return ( true );
                }
            }.parse( userSettingsFile );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        
        return ( true );
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
            
            ConfigurationLoader.loadConfiguration( configFile, widgetsManager, gameData, null );
            
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
            ConfigurationSaver.saveConfiguration( getEditorPanel().getWidgetsDrawingManager(), currentConfigFile );
            
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
        if ( !getDirtyFlag() )
        {
            getMainWindow().setVisible( false );
            int extendedState = getMainWindow().getExtendedState();
            getMainWindow().setExtendedState( JFrame.NORMAL );
            saveUserSettings( extendedState );
            
            //System.exit( 0 );
            getMainWindow().dispose();
            return;
        }
        
        int result = JOptionPane.showConfirmDialog( window, "Do you want to save the changes before exit?", window.getTitle(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
        
        if ( result == JOptionPane.CANCEL_OPTION )
            return;
        
        if ( result == JOptionPane.YES_OPTION )
        {
            saveConfig();
        }
        
        getMainWindow().setVisible( false );
        int extendedState = getMainWindow().getExtendedState();
        getMainWindow().setExtendedState( JFrame.NORMAL );
        saveUserSettings( extendedState );
        
        System.exit( 0 );
    }
    
    private BufferedImage loadBackgroundImage( int width, int height )
    {
        try
        {
            //return ( ImageIO.read( RFDynHUDEditor.class.getClassLoader().getResource( "data/background_" + width + "x" + height + ".jpg" ) ) );
            return ( ImageIO.read( new File( RFactorTools.EDITOR_PATH + File.separator + "backgrounds" + File.separator + screenshotSet + File.separator + "background_" + width + "x" + height + ".jpg" ) ) );
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
        TransformableTexture overlayTexture = TransformableTexture.createMainTexture( gameResX, gameResY );
        
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
        this.screenshotSet = screenshotSet;
        switchToGameResolution( gameResX, gameResY );
    }
    
    private JMenu createFileMenu()
    {
        JMenu file = new JMenu( "File" );
        file.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem open = new JMenuItem( "Open...", 0 );
        open.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                openConfig();
            }
        } );
        file.add( open );
        
        file.add( new JSeparator() );
        
        JMenuItem save = new JMenuItem( "Save", 0 );
        save.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_MASK ) );
        save.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                saveConfig();
            }
        } );
        file.add( save );
        
        JMenuItem saveAs = new JMenuItem( "Save As...", 5 );
        saveAs.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                saveConfigAs();
            }
        } );
        file.add( saveAs );
        
        file.add( new JSeparator() );
        
        JMenuItem close = new JMenuItem( "Close", 0 );
        close.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                onCloseRequested();
            }
        } );
        file.add( close );
        
        return ( file );
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
                try
                {
                    //Widget widget = (Widget)widgetClazz.getConstructor( RelativePositioning.class, int.class, int.class, int.class, int.class ).newInstance( RelativePositioning.TOP_LEFT, 0, 0, 100, 100 );
                    Widget widget = (Widget)widgetClazz.getConstructor( String.class ).newInstance( getEditorPanel().getWidgetsDrawingManager().findFreeName( widgetClazz.getSimpleName() ) );
                    getEditorPanel().getWidgetsDrawingManager().addWidget( widget );
                    onWidgetSelected( widget );
                    getEditorPanel().repaint();
                }
                catch ( Throwable t )
                {
                    t.printStackTrace();
                }
            }
        } );
        
        return ( widgetMenuItem );
    }
    
    private JMenu createWidgetsMenu()
    {
        JMenu widgetsMenu = new JMenu( "Widgets" );
        widgetsMenu.setDisplayedMnemonicIndex( 0 );
        
        List<String> packages = PackageSearcher.findPackages( "*widgets*" );
        List<Class<?>> classes = ClassSearcher.findClasses( new SuperClassCriterium( Widget.class, false ), packages.toArray( new String[ packages.size() ] ) );
        
        for ( Class<?> clazz : classes )
        {
            widgetsMenu.add( createWidgetMenuItem( clazz ) );
        }
        
        widgetsMenu.add( new JSeparator() );
        
        JMenuItem removeItem = new JMenuItem( "Remove selected Widget" );
        removeItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                getEditorPanel().removeSelectedWidget();
                onWidgetSelected( null );
            }
        } );
        
        widgetsMenu.add( removeItem );
        
        return ( widgetsMenu );
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
    
    private boolean checkResolution( String resString )
    {
        //URL url = RFDynHUDEditor.class.getClassLoader().getResource( "data/background_" + resString + ".jpg" );
        File file = new File( RFactorTools.EDITOR_PATH + File.separator + "backgrounds" + File.separator + screenshotSet + File.separator + "background_" + resString + ".jpg" );
        return ( file.exists() );
    }
    
    private boolean checkResolution( int resX, int resY )
    {
        return ( checkResolution( resX + "x" + resY ) );
    }
    
    private JMenu createScreenshotSetsMenu()
    {
        JMenu sssMenu = new JMenu( "Screenshot Set" );
        
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
                
                sssMenu.add( mi );
            }
        }
        
        return ( sssMenu );
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
            
            if ( !checkResolution( resString ) )
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
        JMenu widgetsMenu = new JMenu( "Input" );
        widgetsMenu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem manangerItem = new JMenuItem( "Open InputBindingsManager..." );
        manangerItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                InputBindingsGUI.showInputBindingsGUI( RFDynHUDEditor.this );
            }
        } );
        
        widgetsMenu.add( manangerItem );
        
        return ( widgetsMenu );
    }
    
    private JMenu createToolsMenu()
    {
        JMenu widgetsMenu = new JMenu( "Tools" );
        widgetsMenu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem manangerItem = new JMenuItem( "Open Strategy Calculator..." );
        manangerItem.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                StrategyTool.showStrategyTool( RFDynHUDEditor.this.window );
            }
        } );
        
        widgetsMenu.add( manangerItem );
        
        return ( widgetsMenu );
    }
    
    private void createMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        
        menuBar.add( createFileMenu() );
        menuBar.add( createWidgetsMenu() );
        menuBar.add( createResolutionsMenu() );
        menuBar.add( createInputMenu() );
        menuBar.add( createToolsMenu() );
        
        window.setJMenuBar( menuBar );
    }
    
    private static void initTestGameData( LiveGameData gameData )
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
            __GDPrivilegedAccess.loadFromStream( in, gameData.getScoringInfo() );
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
        BufferedImage backgroundImage = loadBackgroundImage( 1920, 1200 );
        this.gameResX = backgroundImage.getWidth();
        this.gameResY = backgroundImage.getHeight();
        TransformableTexture overlayTexture = TransformableTexture.createMainTexture( gameResX, gameResY );
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
        
        this.propsEditor = new PropertiesEditor( this );
        
        JSplitPane split2 = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        split2.setResizeWeight( 0 );
        split2.setPreferredSize( new Dimension( 300, Integer.MAX_VALUE ) );
        split2.setMinimumSize( new Dimension( 300, 10 ) );
        
        split2.add( propsEditor.getGUI() );
        
        propsEditor.getTable().getSelectionModel().addListSelectionListener( new ListSelectionListener()
        {
            public void valueChanged( ListSelectionEvent e )
            {
                if ( !e.getValueIsAdjusting() )
                {
                    HierarchicalTableModel m = (HierarchicalTableModel)propsEditor.getTable().getModel();
                    
                    if ( m.isDataRow( propsEditor.getTable().getSelectedRow() ) )
                        onPropertySelected( (Property)m.getRowAt( propsEditor.getTable().getSelectedRow() ) );
                    else
                        onPropertySelected( null );
                }
            }
        } );
        
        split.resetToPreferredSizes();
        
        docPanel = new JEditorPane( "text/html", "" );
        ( (HTMLDocument)docPanel.getDocument() ).getStyleSheet().importStyleSheet( RFDynHUDEditor.class.getClassLoader().getResource( "net/ctdp/rfdynhud/editor/properties/doc.css" ) );
        docPanel.setEditable( false );
        JScrollPane sp = new JScrollPane( docPanel );
        sp.setMinimumSize( new Dimension( 300, 10 ) );
        split2.add( sp );
        split2.setDividerLocation( 450 );
        
        split2.resetToPreferredSizes();
        split2.setContinuousLayout( true );
        split.setContinuousLayout( true );
        
        split.add( split2 );
        contentPane.add( split );
        
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
            
            RFDynHUDEditor editor = new RFDynHUDEditor();
            
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
                    ConfigurationLoader.loadFactoryDefaults( editor.getEditorPanel().getWidgetsDrawingManager(), editor.gameData, null );
            }
            
            __GDPrivilegedAccess.loadEditorDefaults( editor.gameData.getPhysics() );
            VehicleSetup.loadEditorDefaults( editor.gameData );
            
            initTestGameData( editor.gameData );
            
            editor.eventsManager.onSessionStarted( true );
            editor.eventsManager.onRealtimeEntered( true );
            
            editor.getMainWindow().setVisible( true );
            
            while ( editor.getMainWindow().isVisible() )
            {
                try { Thread.sleep( 50L ); } catch ( InterruptedException e ) { e.printStackTrace(); }
            }
            
            //System.exit( 0 );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
            
            String message = t.getMessage();
            if ( ( message == null ) || ( message.length() == 0 ) )
                message = t.getClass().getSimpleName() + ", " + t.getStackTrace()[0];
            
            JOptionPane.showMessageDialog( null, message, "Error starting the rfDynHUD Editor", JOptionPane.ERROR_MESSAGE );
        }
    }
}
