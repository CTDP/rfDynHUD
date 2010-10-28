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
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.properties.BackgroundProperty.BackgroundType;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.WidgetTools;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.AbstractAssembledWidget;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetFactory;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * 
 * @author Marvin Froehlich
 */
public class EditorMenuBar extends JMenuBar
{
    private static final long serialVersionUID = 1419330166285223452L;
    
    private final RFDynHUDEditor editor;
    
    private final HashMap<String, DM> menuItemDMMap = new HashMap<String, EditorMenuBar.DM>();
    
    private boolean needsDMCheck = true;
    
    public void setNeedsDMCheck()
    {
        this.needsDMCheck = true;
    }
    
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
        
        JMenuItem importWidget = new JMenuItem( "Import Widget...", 0 );
        importWidget.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.importWidget();
            }
        } );
        menu.add( importWidget );
        
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
    
    private static JMenuItem createFixWidthByBackgroundMenu( final RFDynHUDEditor editor )
    {
        JMenuItem menu = new JMenuItem( "Fix width by background aspect" );
        menu.setEnabled( editor.getEditorPanel().getSelectedWidget().getBackground().getType() == BackgroundType.IMAGE );
        menu.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                if ( editor.getEditorPanel().fixSelectedWidgetWidthByBackgroundAspect() )
                    editor.setDirtyFlag();
            }
        } );
        
        return ( menu );
    }
    
    private static JMenuItem createFixHeightByBackgroundMenu( final RFDynHUDEditor editor )
    {
        JMenuItem menu = new JMenuItem( "Fix height by background aspect" );
        menu.setEnabled( editor.getEditorPanel().getSelectedWidget().getBackground().getType() == BackgroundType.IMAGE );
        menu.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                if ( editor.getEditorPanel().fixSelectedWidgetHeightByBackgroundAspect() )
                    editor.setDirtyFlag();
            }
        } );
        
        return ( menu );
    }
    
    private static JMenuItem createSnapSelWidgetToGridMenu( final RFDynHUDEditor editor )
    {
        JMenuItem snapSelWidgetToGrid = new JMenuItem( "Snap selected Widget to grid" );
        snapSelWidgetToGrid.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                if ( editor.getEditorPanel().snapSelectedWidgetToGrid() )
                    editor.setDirtyFlag();
            }
        } );
        
        return ( snapSelWidgetToGrid );
    }
    
    private static JMenuItem createSnapAllWidgetsToGridMenu( final RFDynHUDEditor editor )
    {
        JMenuItem snapAllWidgetsToGrid = new JMenuItem( "Snap all Widgets to grid" );
        snapAllWidgetsToGrid.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                if ( editor.getEditorPanel().snapAllWidgetsToGrid() )
                    editor.setDirtyFlag();
            }
        } );
        
        return ( snapAllWidgetsToGrid );
    }
    
    private static JMenuItem createRemoveWidgetMenu( final RFDynHUDEditor editor )
    {
        JMenuItem removeItem = new JMenuItem( "Remove selected Widget (DEL)" );
        //removeItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ) );
        removeItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.getEditorPanel().removeSelectedWidget();
            }
        } );
        
        return ( removeItem );
    }
    
    private JMenu createEditMenu()
    {
        JMenu menu = new JMenu( "Edit" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        final JMenuItem snapSelWidgetToGrid = createSnapSelWidgetToGridMenu( editor );
        menu.add( snapSelWidgetToGrid );
        
        final JMenuItem snapAllWidgetsToGrid = createSnapAllWidgetsToGridMenu( editor );
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
        
        final JMenuItem removeItem = createRemoveWidgetMenu( editor );
        menu.add( removeItem );
        
        menu.addMenuListener( new MenuListener()
        {
            @Override
            public void menuSelected( MenuEvent e )
            {
                boolean hasSelected = ( editor.getEditorPanel().getSelectedWidget() != null );
                boolean hasWidgets = ( editor.getWidgetsConfiguration().getNumWidgets() > 0 );
                
                snapSelWidgetToGrid.setEnabled( hasSelected && editor.getEditorPanel().getSettings().isGridUsed() );
                snapAllWidgetsToGrid.setEnabled( hasWidgets && editor.getEditorPanel().getSettings().isGridUsed() );
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
    
    private static JMenuItem createGoIntoMenu( final RFDynHUDEditor editor, final AbstractAssembledWidget widget )
    {
        JMenuItem goIntoItem = new JMenuItem( "Go into " + widget.getName() );
        goIntoItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.getEditorPanel().goInto( widget );
            }
        } );
        
        return ( goIntoItem );
    }
    
    private static JMenuItem createGoOutOfMenu( final RFDynHUDEditor editor, final AbstractAssembledWidget widget )
    {
        JMenuItem goIntoItem = new JMenuItem( "Go out of " + widget.getName() );
        goIntoItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.getEditorPanel().goInto( widget.getMasterWidget() );
            }
        } );
        
        return ( goIntoItem );
    }
    
    private static JMenuItem createSelectWidgetMenu( final RFDynHUDEditor editor, final Widget widget, boolean bold )
    {
        JMenuItem selectWidgetItem = new JMenuItem( "Select " + widget.getName() );
        if ( bold )
            selectWidgetItem.setFont( selectWidgetItem.getFont().deriveFont( Font.BOLD ) );
        selectWidgetItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.getEditorPanel().setSelectedWidget( widget, false );
            }
        } );
        
        return ( selectWidgetItem );
    }
    
    private static JMenuItem createResetZoomMenu( final RFDynHUDEditor editor )
    {
        JMenuItem resetZoomItem = new JMenuItem( "Reset zoom level (CTRL + Wheel click)" );
        resetZoomItem.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                editor.getEditorPanel().setScaleFactor( 1.0f );
            }
        } );
        
        return ( resetZoomItem );
    }
    
    public static void initContextMenu( RFDynHUDEditor editor, Widget[] hoveredWidgets, AbstractAssembledWidget scopeWidget )
    {
        JPopupMenu menu = new JPopupMenu();
        
        if ( hoveredWidgets != null )
        {
            if ( hoveredWidgets.length > 1 )
            {
                for ( int i = 0; i < hoveredWidgets.length; i++ )
                {
                    JMenuItem selectWidgetItem = createSelectWidgetMenu( editor, hoveredWidgets[i], i == 0 );
                    menu.add( selectWidgetItem );
                }
                
                menu.addSeparator();
            }
            
            boolean scopeSep = false;
            
            if ( ( hoveredWidgets.length > 0 ) && ( hoveredWidgets[0] instanceof AbstractAssembledWidget ) )
            {
                JMenuItem goIntoItem = createGoIntoMenu( editor, (AbstractAssembledWidget)hoveredWidgets[0] );
                menu.add( goIntoItem );
                
                scopeSep = true;
            }
            
            if ( scopeWidget != null )
            {
                JMenuItem goOutOfItem = createGoOutOfMenu( editor, scopeWidget );
                menu.add( goOutOfItem );
                
                scopeSep = true;
            }
            
            if ( scopeSep )
                menu.addSeparator();
        }
        
        JMenuItem resetZoomItem = createResetZoomMenu( editor );
        menu.add( resetZoomItem );
        
        menu.addSeparator();
        
        JMenuItem fixWidthByBackground = createFixWidthByBackgroundMenu( editor );
        menu.add( fixWidthByBackground );
        
        JMenuItem fixHeightByBackground = createFixHeightByBackgroundMenu( editor );
        menu.add( fixHeightByBackground );
        
        JMenuItem snapSelWidgetToGrid = createSnapSelWidgetToGridMenu( editor );
        menu.add( snapSelWidgetToGrid );
        
        JMenuItem snapAllWidgetsToGrid = createSnapAllWidgetsToGridMenu( editor );
        menu.add( snapAllWidgetsToGrid );
        
        menu.addSeparator();
        
        JMenuItem removeItem = createRemoveWidgetMenu( editor );
        menu.add( removeItem );
        
        boolean hasSelected = ( editor.getEditorPanel().getSelectedWidget() != null );
        boolean hasWidgets = ( editor.getWidgetsConfiguration().getNumWidgets() > 0 );
        
        snapSelWidgetToGrid.setEnabled( hasSelected && editor.getEditorPanel().getSettings().isGridUsed() );
        snapAllWidgetsToGrid.setEnabled( hasWidgets && editor.getEditorPanel().getSettings().isGridUsed() );
        removeItem.setEnabled( hasSelected );
        
        editor.getEditorPanel().setComponentPopupMenu( menu );
    }
    
    private JMenuItem createWidgetMenuItem( final Class<Widget> clazz, WidgetsConfiguration widgetsConfig )
    {
        try
        {
            JMenuItem widgetMenuItem = new WidgetMenuItem( editor, clazz, widgetsConfig );
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
    
    private JMenu createWidgetsMenu( LiveGameData gameData )
    {
        JMenu menu = new JMenu( "Widgets" );
        menu.setDisplayedMnemonicIndex( 0 );
        
        List<Class<Widget>> classes = WidgetTools.findWidgetClasses();
        
        HashMap<Class<Widget>, Widget> instances = new HashMap<Class<Widget>, Widget>();
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
        
        WidgetPackage rootPackage = new WidgetPackage( "", 0 );
        
        ArrayList<WidgetPackage> widgetPackages = new ArrayList<WidgetPackage>();
        Iterator<Class<Widget>> it = classes.iterator();
        while ( it.hasNext() )
        {
            Class<Widget> clazz = it.next();
            
            try
            {
                Widget widget = WidgetFactory.createWidget( clazz, "dummy" );
                if ( widget != null )
                {
                    instances.put( clazz, widget );
                    
                    WidgetPackage wp = widget.getWidgetPackage();
                    if ( wp == null )
                        wp = rootPackage;
                    
                    widgetPackages.add( wp );
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
        
        WidgetsDrawingManager widgetsManager = new WidgetsDrawingManager( true, WidgetMenuItem.ICON_WIDTH, WidgetMenuItem.ICON_HEIGHT );
        
        it = classes.iterator();
        while ( it.hasNext() )
        {
            Class<Widget> clazz = it.next();
            
            try
            {
                Widget widget = instances.get( clazz );
                String pkgName = ( widget.getWidgetPackage() == null ) ? "" : widget.getWidgetPackage().getName();
                String[] path = pkgName.split( "/" );
                
                JMenuItem mi = createWidgetMenuItem( clazz, widgetsManager.getWidgetsConfiguration() );
                
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
        
        __WCPrivilegedAccess.setJustLoaded( widgetsManager.getWidgetsConfiguration(), gameData, true, null );
        
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
            
            sysA = Math.round( dm.getWidth() * 100f / dm.getHeight() ) / 100f;
        }
        
        public final int w;
        public final int h;
        public final float a;
        public final Dimension forResolution;
        
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
        
        public DM( int w, int h, Dimension forResolution )
        {
            this.w = w;
            this.h = h;
            this.a = Math.round( w * 100f / h ) / 100f;
            this.forResolution = forResolution;
        }
    }
    
    private Font miFont = null;
    private Font selMiFont = null;
    
    private void checkResolutions( JMenu menu )
    {
        String currResString = editor.getWidgetsConfiguration().getGameResolution().getResolutionString();
        
        for ( Component c : menu.getMenuComponents() )
        {
            if ( c instanceof JMenu )
            {
                checkResolutions( (JMenu)c );
            }
            else if ( c instanceof JMenuItem )
            {
                JMenuItem mi = (JMenuItem)c;
                
                DM dm =  menuItemDMMap.get( mi.getName() );
                
                if ( needsDMCheck )
                {
                    if ( editor.getEditorPanel().getSettings().checkResolution( dm.w, dm.h ) )
                    {
                        if ( dm.forResolution == null )
                            mi.setText( dm.w + "x" + dm.h + " [" + dm.a + "]" );
                        else
                            mi.setText( dm.w + "x" + dm.h + " [" + dm.a + "] (for " + dm.forResolution.width + "x" + dm.forResolution.height + ")" );
                        mi.setEnabled( true );
                    }
                    else
                    {
                        if ( dm.forResolution == null )
                            mi.setText( dm.w + "x" + dm.h + " [" + dm.a + "] (no screenshot available)" );
                        else
                            mi.setText( dm.w + "x" + dm.h + " [" + dm.a + "] (for " + dm.forResolution.width + "x" + dm.forResolution.height + ") (no screenshot available)" );
                        mi.setEnabled( false );
                    }
                }
                
                
                
                if ( dm.forResolution != null )
                {
                    if ( currResString.equals( dm.forResolution.width + "x" + dm.forResolution.height ) )
                        mi.setFont( selMiFont );
                    else
                        mi.setFont( miFont );
                }
                
                mi.setSelected( currResString.equals( mi.getName() ) );
            }
        }
    }
    
    private JMenu createResolutionsMenu()
    {
        JMenu resMenu = new JMenu( "Resolutions" );
        resMenu.setDisplayedMnemonicIndex( 0 );
        
        DM[] array = new DM[ AvailableDisplayModes.getNumberOf() ];
        int i = 0;
        for ( DisplayMode dm : AvailableDisplayModes.getAll() )
        {
            array[i++] = new DM( dm.getWidth(), dm.getHeight(), ( dm.getBitDepth() < 1000 ) ? null : new Dimension( dm.getBitDepth() / 100, dm.getRefreshRate() / 100 ) );
        }
        Arrays.sort( array );
        
        ActionListener itemActionListener = new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                DM dm = menuItemDMMap.get( ( (JMenuItem)e.getSource() ).getName() );
                
                Logger.log( "Switching to resolution " + dm.w + "x" + dm.h + "..." );
                
                editor.getEditorPanel().switchToGameResolution( dm.w, dm.h );
            }
        };
        
        float lastA = -1;
        
        JMenu moniMenu = new JMenu( "Session Monitor" );
        lastA = array[0].a;
        for ( DM dm : array )
        {
            if ( dm.forResolution != null )
            {
                if ( lastA == -1 )
                    lastA = dm.a;
                
                if ( dm.a != lastA )
                {
                    moniMenu.addSeparator();
                    lastA = dm.a;
                }
                
                JMenuItem item = new JCheckBoxMenuItem( dm.w + "x" + dm.h + " [" + dm.a + "]" );
                item.setName( dm.w + "x" + dm.h );
                
                item.addActionListener( itemActionListener );
                
                moniMenu.add( item );
                menuItemDMMap.put( item.getName(), dm );
            }
        }
        
        resMenu.add( moniMenu );
        
        resMenu.addSeparator();
        
        lastA = -1;
        for ( DM dm : array )
        {
            if ( dm.forResolution == null )
            {
                if ( lastA == -1 )
                    lastA = dm.a;
                
                if ( dm.a != lastA )
                {
                    resMenu.addSeparator();
                    lastA = dm.a;
                }
                
                JMenuItem item = new JCheckBoxMenuItem( dm.w + "x" + dm.h + " [" + dm.a + "]" );
                item.setName( dm.w + "x" + dm.h );
                
                if ( miFont == null )
                {
                    miFont = item.getFont();
                    selMiFont = miFont.deriveFont( Font.BOLD );
                }
                
                item.addActionListener( itemActionListener );
                
                resMenu.add( item );
                menuItemDMMap.put( item.getName(), dm );
            }
        }
        
        resMenu.addMenuListener( new MenuListener()
        {
            @Override
            public void menuSelected( MenuEvent e )
            {
                checkResolutions( (JMenu)e.getSource() );
                
                needsDMCheck = false;
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
                
                PreviewAndScreenshotManager.showFullscreenPreview( editor.getMainWindow(), editor.getEditorPanel(), editor.getGameResolution(), editor.getCurrentConfigFile() );
                
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
                PreviewAndScreenshotManager.takeScreenshot( editor.getEditorPanel(), editor.getGameResolution(), editor.getCurrentConfigFile() );
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
        this.add( createWidgetsMenu( editor.getGameData() ) );
        this.add( createResolutionsMenu() );
        this.add( createToolsMenu() );
        this.add( createHelpMenu() );
    }
}
