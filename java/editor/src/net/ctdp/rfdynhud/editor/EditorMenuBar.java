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

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.ctdp.rfdynhud.editor.help.AboutPage;
import net.ctdp.rfdynhud.editor.input.InputBindingsGUI;
import net.ctdp.rfdynhud.editor.util.AvailableDisplayModes;
import net.ctdp.rfdynhud.editor.util.StrategyTool;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

import org.jagatoo.util.classes.ClassSearcher;
import org.jagatoo.util.classes.PackageSearcher;
import org.jagatoo.util.classes.SuperClassCriterium;

/**
 * 
 * @author Marvin Froehlich
 */
public class EditorMenuBar extends JMenuBar
{
    private static final long serialVersionUID = 1419330166285223452L;
    
    private final RFDynHUDEditor editor;
    
    private JMenu createFileMenu()
    {
        JMenu menu = new JMenu( "File" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        JMenuItem open = new JMenuItem( "Open...", 0 );
        open.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.openConfig();
            }
        } );
        menu.add( open );
        
        menu.add( new JSeparator() );
        
        JMenuItem save = new JMenuItem( "Save", 0 );
        save.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, InputEvent.CTRL_MASK ) );
        save.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.saveConfig();
            }
        } );
        menu.add( save );
        
        JMenuItem saveAs = new JMenuItem( "Save As...", 5 );
        saveAs.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.saveConfigAs();
            }
        } );
        menu.add( saveAs );
        
        menu.add( new JSeparator() );
        
        JMenuItem close = new JMenuItem( "Close", 0 );
        close.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.onCloseRequested();
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
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.snapSelectedWidgetToGrid();
            }
        } );
        
        return ( snapSelWidgetToGrid );
    }
    
    private JMenuItem createSnapAllWidgetsToGridMenu()
    {
        JMenuItem snapAllWidgetsToGrid = new JMenuItem( "Snap all Widgets to grid" );
        snapAllWidgetsToGrid.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.snapAllWidgetsToGrid();
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
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.removeSelectedWidget();
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
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.makeAllWidgetsUsePixels();
            }
        } );
        menu.add( makeAllPixels );
        
        final JMenuItem makeAllPercents = new JMenuItem( "Make all Widgets use Percents" );
        makeAllPercents.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.makeAllWidgetsUsePercents();
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
                boolean hasSelected = ( editor.getEditorPanel().getSelectedWidget() != null );
                boolean hasWidgets = ( editor.getWidgetsConfiguration().getNumWidgets() > 0 );
                
                snapSelWidgetToGrid.setEnabled( hasSelected && editor.getEditorPanel().isGridUsed() );
                snapAllWidgetsToGrid.setEnabled( hasWidgets && editor.getEditorPanel().isGridUsed() );
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
        
        boolean hasSelected = ( editor.getEditorPanel().getSelectedWidget() != null );
        boolean hasWidgets = ( editor.getWidgetsConfiguration().getNumWidgets() > 0 );
        
        snapSelWidgetToGrid.setEnabled( hasSelected && editor.getEditorPanel().isGridUsed() );
        snapAllWidgetsToGrid.setEnabled( hasWidgets && editor.getEditorPanel().isGridUsed() );
        removeItem.setEnabled( hasSelected );
        
        editor.getEditorPanel().setComponentPopupMenu( menu );
    }
    
    private JMenuItem createWidgetMenuItem( final Class<Widget> clazz )
    {
        try
        {
            JMenuItem widgetMenuItem = new WidgetMenuItem( editor, clazz );
            widgetMenuItem.setName( clazz.getName() );
            widgetMenuItem.addActionListener( new ActionListener()
            {
                private final Class<Widget> widgetClazz = clazz;
                
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    editor.addNewWidget( widgetClazz );
                }
            } );
            
            return ( widgetMenuItem );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( null );
        }
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
    
    private JMenu getMenu( JMenu parent, String[] path, int i, WidgetPackage pkg )
    {
        for ( Component c : parent.getMenuComponents() )
        {
            if ( c instanceof JMenu )
            {
                JMenu m = (JMenu)c;
                
                if ( m.getText().equals( path[i] ) )
                {
                    if ( i < path.length - 1 )
                        return ( getMenu( m, path, i + 1, pkg ) );
                    
                    return ( m );
                }
            }
        }
        
        JMenu m = new WidgetMenu( path[i], pkg, i );
        parent.add( m );
        
        if ( i < path.length - 1 )
            return ( getMenu( m, path, i + 1, pkg ) );
        
        return ( m );
    }
    
    @SuppressWarnings( "unchecked" )
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
        
        ArrayList<WidgetPackage> widgetPackages = new ArrayList<WidgetPackage>();
        Iterator<Class<?>> it = classes.iterator();
        while ( it.hasNext() )
        {
            Class<Widget> clazz = (Class<Widget>)it.next();
            
            try
            {
                Widget widget = RFDynHUDEditor.createWidgetInstance( clazz, null, false );
                if ( widget != null )
                {
                    instances.put( clazz, widget );
                    widgetPackages.add( widget.getWidgetPackage() );
                }
            }
            catch ( Throwable t )
            {
                it.remove();
                Logger.log( "Error handling Widget class " + clazz.getName() + ":" );
                Logger.log( t );
            }
        }
        
        Collections.sort( widgetPackages );
        
        for ( WidgetPackage widgetPackage : widgetPackages )
        {
            String pkgName = ( widgetPackage == null ) ? "" : widgetPackage.getName();
            String[] path = pkgName.split( "/" );
            
            if ( ( path.length > 1 ) || !path[0].equals( "" ) )
                getMenu( menu, path, 0, widgetPackage );
        }
        
        it = classes.iterator();
        while ( it.hasNext() )
        {
            Class<Widget> clazz = (Class<Widget>)it.next();
            
            try
            {
                Widget widget = instances.get( clazz );
                String pkgName = ( widget.getWidgetPackage() == null ) ? "" : widget.getWidgetPackage().getName();
                String[] path = pkgName.split( "/" );
                
                JMenuItem mi = createWidgetMenuItem( clazz );
                
                if ( mi != null )
                {
                    if ( ( path.length == 1 ) && path[0].equals( "" ) )
                        menu.add( mi );
                    else
                        getMenu( menu, path, 0, widget.getWidgetPackage() ).add( mi );
                }
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
                
                int n = editor.getWidgetsConfiguration().getNumWidgets();
                for ( int i = 0; i < n; i++ )
                {
                    if ( editor.getWidgetsConfiguration().getWidget( i ).getClass().getName().equals( item.getName() ) )
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
        
        @Override
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
    
    private JMenu createResolutionsMenu()
    {
        JMenu resMenu = new JMenu( "Resolutions" );
        resMenu.setDisplayedMnemonicIndex( 0 );
        
        HashSet<DM> set = new HashSet<DM>();
        for ( DisplayMode dm : AvailableDisplayModes.getAll() )
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
            
            if ( !editor.checkResolution( dm.w, dm.h ) )
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
                    @Override
                    public void actionPerformed( ActionEvent e )
                    {
                        Logger.log( "Switching to resolution " + dm2.w + "x" + dm2.h + "..." );
                        
                        editor.switchToGameResolution( dm2.w, dm2.h );
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
                String resString = editor.getWidgetsConfiguration().getGameResolution().getResolutionString();
                
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
            
            @Override
            public void actionPerformed( ActionEvent e )
            {
                if ( e.getWhen() < lastWhen + 1500L )
                    return;
                
                editor.showFullscreenPreview();
                
                lastWhen = e.getWhen();
            }
        } );
        menu.add( previewItem );
        
        JMenuItem screenshotItem = new JMenuItem( "Take Screenshot" );
        screenshotItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F12, 0 ) );
        screenshotItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.takeScreenshot();
            }
        } );
        menu.add( screenshotItem );
        
        menu.addSeparator();
        
        JMenuItem manangerItem = new JMenuItem( "Open Strategy Calculator..." );
        manangerItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                StrategyTool.showStrategyTool( editor.getMainWindow() );
            }
        } );
        menu.add( manangerItem );
        
        menu.addSeparator();
        
        ImageIcon icon_inputMgrItem = null;
        try
        {
            icon_inputMgrItem = new ImageIcon( ImageIO.read( this.getClass().getClassLoader().getResource( this.getClass().getPackage().getName().replace( '.', '/' ) + "/input/khotkeys.png" ) ) );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        JMenuItem inputMgrItem = new JMenuItem( "Open InputBindingsManager...", icon_inputMgrItem );
        inputMgrItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                InputBindingsGUI.showInputBindingsGUI( editor );
            }
        } );
        
        menu.add( inputMgrItem );
        
        menu.addSeparator();
        
        JMenuItem optionsItem = new JMenuItem( "Editor Presets..." );
        optionsItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.showPresetsWindow();
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
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.showHelpWindow();
            }
        } );
        
        menu.add( help );
        
        menu.addSeparator();
        
        JMenuItem about = new JMenuItem( "About" );
        about.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                AboutPage.showAboutPage( editor.getMainWindow() );
            }
        } );
        
        menu.add( about );
        
        return ( menu );
    }
    
    public EditorMenuBar( RFDynHUDEditor editor )
    {
        super();
        
        this.editor = editor;
        
        this.add( createFileMenu() );
        this.add( createEditMenu() );
        this.add( createWidgetsMenu() );
        this.add( createResolutionsMenu() );
        this.add( createToolsMenu() );
        this.add( createHelpMenu() );
    }
}
