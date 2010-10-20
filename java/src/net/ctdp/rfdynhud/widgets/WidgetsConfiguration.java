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
package net.ctdp.rfdynhud.widgets;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.ctdp.rfdynhud.gamedata.GameResolution;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.input.InputMappings;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FlatWidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.util.ConfigurationLoader;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.util.WidgetTools;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.StatefulWidget;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * The {@link WidgetsConfiguration} handles the drawing of all visible widgets.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetsConfiguration implements Documented
{
    public static interface ConfigurationLoadListener
    {
        public void beforeWidgetsConfigurationCleared( WidgetsConfiguration widgetsConfig );
        public void afterWidgetsConfigurationLoaded( WidgetsConfiguration widgetsConfig );
    }
    
    private int id = 0;
    
    private final ArrayList<Widget> widgets = new ArrayList<Widget>();
    private final HashMap<String, Widget> widgetsMap = new HashMap<String, Widget>();
    
    private final HashMap<String, Color> colorMap = new HashMap<String, Color>();
    private final HashMap<String, Font> fontMap = new HashMap<String, Font>();
    private final HashMap<String, String> fontStringMap = new HashMap<String, String>();
    private final HashMap<String, Boolean> fontVirtualMap = new HashMap<String, Boolean>();
    private final HashMap<String, String> borderMap = new HashMap<String, String>();
    
    //@SuppressWarnings( "unchecked" )
    //private final HashMap<Class<? extends StatefulWidget>, Object> generalStores = new HashMap<Class<? extends StatefulWidget>, Object>();
    private final HashMap<String, Object> localStores = new HashMap<String, Object>();
    private final HashMap<String, Boolean> visibilities = new HashMap<String, Boolean>();
    
    private final BooleanProperty useClassScoring = __PropsPrivilegedAccess.newBooleanProperty( this, "useClassScoring", "useClassScoring", false, false );
    
    private boolean needsCheckFixAndBake = true;
    
    private final GameResolution gameResolution = new GameResolution();
    
    private InputMappings inputMappings = null;
    
    private boolean isValid = false;
    
    public final int getId()
    {
        return ( id );
    }
    
    void setValid( boolean valid )
    {
        this.isValid = valid;
    }
    
    public final boolean isValid()
    {
        return ( isValid );
    }
    
    private static String getLocalStoreKey( Widget widget )
    {
        return ( widget.getClass().getName() + "::" + widget.getName() );
    }
    
    /**
     * Finds a free name starting with 'baseName'.
     * 
     * @param baseName the name prefix
     * 
     * @return the found free name.
     */
    public String findFreeName( String baseName )
    {
        for ( int i = 1; i < Integer.MAX_VALUE; i++ )
        {
            String name = baseName + i;
            boolean isFree = true;
            for ( int j = 0; j < widgets.size(); j++ )
            {
                if ( name.equals( widgets.get( j ).getName() ) )
                {
                    isFree = false;
                    break;
                }
            }
            
            if ( isFree )
                return ( name );
        }
        
        // Theoretically unreachable code!
        return ( null );
    }
    
    void sortWidgets()
    {
        Collections.sort( widgets, WidgetTools.WIDGET_Z_Y_X_COMPARATOR );
    }
    
    /**
     * Adds a new {@link Widget} to be drawn by this manager.
     * 
     * @param widget
     * @param isLoading
     */
    void addWidget( Widget widget, boolean isLoading )
    {
        widgets.add( widget );
        widgetsMap.put( widget.getName(), widget );
        __WPrivilegedAccess.setConfiguration( this, widget, isLoading );
        
        sortWidgets();
    }
    
    /**
     * Removes a {@link Widget} from the drawing process.
     * 
     * @param widget
     */
    @SuppressWarnings( "rawtypes" )
    void removeWidget( Widget widget )
    {
        widgets.remove( widget );
        widgetsMap.remove( widget.getName() );
        
        if ( ( widget instanceof StatefulWidget ) && __WPrivilegedAccess.hasLocalStore( (StatefulWidget)widget ) )
        {
            localStores.put( getLocalStoreKey( widget ), ( (StatefulWidget)widget ).getLocalStore() );
            //if ( !isEditorMode )
            //    visibilities.put( getLocalStoreKey( widget ), widget.isInputVisible() );
        }
        
        __WPrivilegedAccess.setConfiguration( null, widget, false );
    }
    
    /**
     * Removes all {@link Widget}s and clears all name- and alias maps.
     */
    @SuppressWarnings( "rawtypes" )
    void clear( LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener clearListener )
    {
        if ( clearListener != null )
            clearListener.beforeWidgetsConfigurationCleared( this );
        
        //localStores.clear();
        
        for ( int i = 0; i < widgets.size(); i++ )
        {
            Widget widget = widgets.get( i );
            
            widget.beforeConfigurationCleared( this, gameData, isEditorMode );
        }
        
        if ( !isEditorMode )
        {
            for ( int i = 0; i < widgets.size(); i++ )
            {
                Widget widget = widgets.get( i );
                
                if ( ( widget instanceof StatefulWidget ) && __WPrivilegedAccess.hasLocalStore( (StatefulWidget)widget ) )
                    localStores.put( getLocalStoreKey( widget ), ( (StatefulWidget)widget ).getLocalStore() );
                visibilities.put( getLocalStoreKey( widget ), widget.getInputVisibility() );
                
                __WPrivilegedAccess.setConfiguration( null, widget, false );
            }
        }
        
        widgets.clear();
        widgetsMap.clear();
        
        colorMap.clear();
        fontMap.clear();
        fontStringMap.clear();
        fontVirtualMap.clear();
        borderMap.clear();
    }
    
    boolean updateNameMapping( Widget widget, String oldName )
    {
        if ( widgetsMap.get( oldName ) == widget )
        {
            widgetsMap.remove( oldName );
            widgetsMap.put( widget.getName(), widget );
            
            return ( true );
        }
        
        return ( false );
    }
    
    /**
     * Gets the number of {@link Widget}s in this manager.
     * 
     * @return the number of {@link Widget}s in this manager.
     */
    public final int getNumWidgets()
    {
        return ( widgets.size() );
    }
    
    /**
     * Gets the index-th {@link Widget} from this manager.
     * 
     * @param index the Widget's index
     * 
     * @return the index-th {@link Widget} from this manager.
     */
    public final Widget getWidget( int index )
    {
        return ( widgets.get( index ) );
    }
    
    /**
     * Gets the {@link Widget} with the specified name from this manager.
     * 
     * @param name the Widget's name
     * 
     * @return the {@link Widget} with the specified name from this manager.
     */
    public final Widget getWidget( String name )
    {
        return ( widgetsMap.get( name ) );
    }
    
    /**
     * Sets the dirty flags on all {@link Widget}s.
     */
    public void setAllDirtyFlags()
    {
        for ( int i = 0; i < getNumWidgets(); i++ )
        {
            getWidget( i ).setDirtyFlag();
        }
    }
    
    void setInputMappings( InputMappings inputMappings )
    {
        this.inputMappings = inputMappings;
    }
    
    public final InputMappings getInputMappings()
    {
        return ( inputMappings );
    }
    
    @SuppressWarnings( "rawtypes" )
    void setJustLoaded( LiveGameData gameData, boolean isEditorMode, ConfigurationLoadListener loadListener )
    {
        this.id++;
        this.needsCheckFixAndBake = true;
        
        for ( int i = 0; i < widgets.size(); i++ )
        {
            Widget widget = widgets.get( i );
            
            if ( widget instanceof StatefulWidget )
            {
                Object localStore = localStores.get( getLocalStoreKey( widget ) );
                if ( localStore != null )
                {
                    __WPrivilegedAccess.setLocalStore( localStore, (StatefulWidget)widget );
                }
            }
            
            if ( !isEditorMode )
            {
                Boolean visibility = visibilities.get( getLocalStoreKey( widget ) );
                if ( visibility != null )
                {
                    __WPrivilegedAccess.setInputVisible( widget, visibility );
                }
            }
        }
        
        for ( int i = 0; i < widgets.size(); i++ )
        {
            Widget widget = widgets.get( i );
            
            widget.afterConfigurationLoaded( this, gameData, isEditorMode );
        }
        
        if ( loadListener != null )
            loadListener.afterWidgetsConfigurationLoaded( this );
    }
    
    private void fixVirtualNamedFonts()
    {
        for ( String name : fontMap.keySet() )
        {
            Boolean virtual = fontVirtualMap.get( name );
            
            if ( virtual == Boolean.TRUE )
            {
                fontMap.put( name, FontUtils.parseFont( fontStringMap.get( name ), gameResolution.getViewportHeight(), false, true ) );
            }
        }
        
        resetAllFontProperties();
    }
    
    boolean setViewport( int x, int y, int w, int h )
    {
        if ( __GDPrivilegedAccess.setViewport( x, y, w, h, gameResolution ) )
        {
            int n = getNumWidgets();
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = getWidget( i );
                
                widget.forceReinitialization();
                widget.forceCompleteRedraw( true );
                if ( widget.getPosition().isBaked() )
                    widget.bake();
            }
            
            fixVirtualNamedFonts();
            
            return ( true );
        }
        
        return ( false );
    }
    
    public final GameResolution getGameResolution()
    {
        return ( gameResolution );
    }
    
    /**
     * Checks, if all Widgets are within the game's bounds.
     * If not, they are moved and possibly resized to be in bounds.
     * 
     * @param isEditorMode editor mode?
     */
    protected void checkFixAndBakeConfiguration( boolean isEditorMode )
    {
        if ( !needsCheckFixAndBake )
            return;
        
        final int gameResX = gameResolution.getViewportWidth();
        final int gameResY = gameResolution.getViewportHeight();
        
        int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget w = getWidget( i );
            
            if ( w.getPosition().getEffectiveX() < 0 )
            {
                if ( w.getPosition().getEffectiveY() < 0 )
                    w.getPosition().setEffectivePosition( 0, 0 );
                else
                    w.getPosition().setEffectivePosition( 0, w.getPosition().getEffectiveY() );
            }
            else if ( w.getPosition().getEffectiveY() < 0 )
            {
                w.getPosition().setEffectivePosition( w.getPosition().getEffectiveX(), 0 );
            }
            
            if ( w.getPosition().getEffectiveX() + w.getSize().getEffectiveWidth() >= gameResX )
            {
                int newX = gameResX - w.getSize().getEffectiveWidth();
                
                if ( newX < 0 )
                {
                    w.getPosition().setEffectivePosition( 0, w.getPosition().getEffectiveY() );
                    w.getSize().setEffectiveSize( gameResX, w.getSize().getEffectiveHeight() );
                }
                else
                {
                    w.getPosition().setEffectivePosition( newX, w.getPosition().getEffectiveY() );
                }
            }
            
            if ( w.getPosition().getEffectiveY() + w.getSize().getEffectiveHeight() >= gameResY )
            {
                int newY = gameResY - w.getSize().getEffectiveHeight();
                
                if ( newY < 0 )
                {
                    w.getPosition().setEffectivePosition( w.getPosition().getEffectiveX(), 0 );
                    w.getSize().setEffectiveSize( w.getSize().getEffectiveWidth(), gameResY );
                }
                else
                {
                    w.getPosition().setEffectivePosition( w.getPosition().getEffectiveX(), newY );
                }
            }
            
            //w.getPosition().set( w.getPosition().getPositioning(), w.getPosition().getX(), w.getPosition().getY() );
            //w.getSize().set( w.getSize().getWidth(), w.getSize().getHeight() );
            
            if ( !isEditorMode )
            {
                w.bake();
            }
        }
        
        needsCheckFixAndBake = false;
    }
    
    /*
     * Gets the general store object for the given Widget class.
     * 
     * @param widgetClass
     * 
     * @return the general store object for the given Widget class.
     */
    /*
    final Object getGeneralStore( Class<? extends StatefulWidget> widgetClass )
    {
        Object generalStore = generalStores.get( widgetClass );
        
        if ( generalStore == null )
        {
            try
            {
                StatefulWidget widget = (StatefulWidget)WidgetFactory.createWidget( widgetClass, "dummy" );
                
                generalStore = widget.getGeneralStore();
                generalStores.put( widgetClass, generalStore );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
        
        return ( generalStore );
    }
    */
    
    /**
     * Maps a new named color.
     * 
     * @param name the name
     * @param color the color
     * 
     * @return changed?
     */
    public boolean addNamedColor( String name, Color color )
    {
        Color old = this.colorMap.put( name, color );
        
        return ( !color.equals( old ) );
    }
    
    /**
     * Gets a named color from the map or <code>null</code>, if not found.
     * 
     * @param name the name
     * 
     * @return a named color from the map or <code>null</code>, if not found.
     */
    public final Color getNamedColor( String name )
    {
        return ( colorMap.get( name ) );
    }
    
    /**
     * Gets all currently mapped color names.
     * 
     * @return all currently mapped color names.
     */
    public final Set<String> getColorNames()
    {
        return ( colorMap.keySet() );
    }
    
    /**
     * Removes a mapped named color.
     * 
     * @param name the name
     * 
     * @return the previously mapped color.
     */
    public Color removeNamedColor( String name )
    {
        Color color = colorMap.remove( name );
        
        if ( color != null )
        {
            String colorString = ColorUtils.colorToHex( color );
            
            resetColors( name, colorString );
        }
        
        return ( color );
    }
    
    private static void renameColorPropertyValues( List<Property> list, String oldName, String newName )
    {
        for ( Property prop : list )
        {
            if ( prop instanceof ColorProperty )
            {
                ColorProperty colorProp = (ColorProperty)prop;
                if ( ( colorProp.getValue() != null ) && colorProp.getValue().equals( oldName ) )
                    colorProp.setValue( newName );
            }
            else if ( ( prop instanceof BackgroundProperty ) && ( (BackgroundProperty)prop ).getBackgroundType().isColor() )
            {
                ColorProperty colorProp = ( (BackgroundProperty)prop ).getColorProperty();
                if ( ( colorProp.getValue() != null ) && colorProp.getValue().equals( oldName ) )
                    colorProp.setValue( newName );
            }
        }
    }
    
    /**
     * Renames the color.
     * 
     * @param oldName the old name
     * @param newName the new name
     */
    public void renameColor( String oldName, String newName )
    {
        Color color = colorMap.get( oldName );
        if ( color == null )
            return;
        
        colorMap.remove( oldName );
        colorMap.put( newName, color );
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( Widget widget : widgets )
        {
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            renameColorPropertyValues( propsCont.getList(), oldName, newName );
        }
    }
    
    /**
     * Reset colors to defaults.
     * 
     * @param oldName the old name
     * @param newValue the new name
     */
    public void resetColors( String oldName, String newValue )
    {
        Color color = colorMap.get( oldName );
        if ( color == null )
            return;
        
        colorMap.remove( oldName );
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( Widget widget : widgets )
        {
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            renameColorPropertyValues( propsCont.getList(), oldName, newValue );
        }
    }
    
    /**
     * Maps a new named font.
     * 
     * @param name the name
     * @param fontStr tje font definition
     * 
     * @return changed?
     */
    public boolean addNamedFont( String name, String fontStr )
    {
        Font font = FontUtils.parseFont( fontStr, gameResolution.getViewportHeight(), false, true );
        boolean virtual = FontUtils.parseVirtualFlag( fontStr, false, true );
        
        Font oldFont = this.fontMap.put( name, font );
        this.fontStringMap.put( name, fontStr );
        Boolean oldVirt = this.fontVirtualMap.put( name, virtual );
        
        return ( !font.equals( oldFont ) || !oldVirt.equals( virtual ) );
    }
    
    /**
     * Gets a named font from the map or <code>null</code>, if not found.
     * 
     * @param name the name
     * 
     * @return a named font from the map or <code>null</code>, if not found.
     */
    public final Font getNamedFont( String name )
    {
        return ( fontMap.get( name ) );
    }
    
    /**
     * Gets a named font from the map or <code>null</code>, if not found.
     * 
     * @param name the name
     * 
     * @return a named font from the map or <code>null</code>, if not found.
     */
    public final String getNamedFontString( String name )
    {
        return ( fontStringMap.get( name ) );
    }
    
    /**
     * Gets a named font's virtual flag from the map or <code>null</code>, if not found.
     * 
     * @param name the name
     * 
     * @return a named font's virtual flag from the map or <code>null</code>, if not found.
     */
    public final Boolean getNamedFontVirtual( String name )
    {
        return ( fontVirtualMap.get( name ) );
    }
    
    /**
     * Gets all currently mapped font names.
     * 
     * @return all currently mapped font names.
     */
    public final Set<String> getFontNames()
    {
        return ( fontMap.keySet() );
    }
    
    /**
     * Removes a mapped named font.
     * 
     * @param name the name
     * 
     * @return the previously mapped font.
     */
    public Font removeNamedFont( String name )
    {
        Font font = fontMap.remove( name );
        String fontString = fontStringMap.remove( name );
        Boolean virtual = fontVirtualMap.remove( name );
        
        if ( font != null )
        {
            boolean antiAliased = FontUtils.parseAntiAliasFlag( fontString, false, true );
            fontString = FontUtils.getFontString( font, virtual, antiAliased );
            
            resetFonts( name, fontString );
        }
        
        return ( font );
    }
    
    private static void renameFontPropertyValues( List<Property> list, String oldName, String newName )
    {
        for ( Property prop : list )
        {
            if ( prop instanceof FontProperty )
            {
                FontProperty fontProp = (FontProperty)prop;
                if ( ( fontProp.getValue() != null ) && fontProp.getValue().equals( oldName ) )
                    fontProp.setValue( newName );
            }
        }
    }
    
    private static void resetAllFontProperties( List<Property> list )
    {
        for ( Property prop : list )
        {
            if ( prop instanceof FontProperty )
            {
                FontProperty fontProp = (FontProperty)prop;
                fontProp.setValue( fontProp.getValue() );
            }
        }
    }
    
    private void resetAllFontProperties()
    {
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( Widget widget : widgets )
        {
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            resetAllFontProperties( propsCont.getList() );
        }
    }
    
    public void renameFont( String oldName, String newName )
    {
        Font font = fontMap.get( oldName );
        if ( font == null )
            return;
        
        fontMap.remove( oldName );
        fontMap.put( newName, font );
        fontStringMap.put( newName, fontStringMap.remove( oldName ) );
        fontVirtualMap.put( newName, fontVirtualMap.remove( oldName ) );
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( Widget widget : widgets )
        {
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            renameFontPropertyValues( propsCont.getList(), oldName, newName );
            //break;
        }
    }
    
    public void resetFonts( String oldName, String newValue )
    {
        Font font = fontMap.get( oldName );
        if ( font == null )
            return;
        
        fontMap.remove( oldName );
        fontStringMap.remove( oldName );
        fontVirtualMap.remove( oldName );
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( Widget widget : widgets )
        {
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            renameFontPropertyValues( propsCont.getList(), oldName, newValue );
            //break;
        }
    }
    
    /**
     * Maps a new border alias to its filename.
     * 
     * @param alias the alias name
     * @param border the border
     * 
     * @return changed?
     */
    public boolean addBorderAlias( String alias, String border )
    {
        String old = borderMap.put( alias, border );
        
        return ( !border.equals( old ) );
    }
    
    /**
     * Gets a border filename from the map or <code>null</code>, if not found.
     * 
     * @param alias the alias name
     * 
     * @return a border filename from the map or <code>null</code>, if not found.
     */
    public final String getBorderName( String alias )
    {
        return ( borderMap.get( alias ) );
    }
    
    /**
     * Gets all currently mapped font names.
     * 
     * @return all currently mapped font names.
     */
    public final Set<String> getBorderAliases()
    {
        return ( borderMap.keySet() );
    }
    
    /**
     * Removes a mapped border alias.
     * 
     * @param alias the alias name
     * 
     * @return the previously mapped border alias.
     */
    public String removeBorderAlias( String alias )
    {
        String border = borderMap.remove( alias );
        
        if ( border != null )
        {
            resetFonts( alias, border );
        }
        
        return ( border );
    }
    
    private static void renameBorderPropertyValues( List<Property> list, String oldName, String newName )
    {
        for ( Property prop : list )
        {
            if ( prop instanceof BorderProperty )
            {
                BorderProperty borderProp = (BorderProperty)prop;
                if ( ( borderProp.getValue() != null ) && borderProp.getValue().equals( oldName ) )
                    borderProp.setValue( newName );
            }
        }
    }
    
    public void renameBorder( String oldName, String newName )
    {
        String border = borderMap.get( oldName );
        if ( border == null )
            return;
        
        borderMap.remove( oldName );
        borderMap.put( newName, border );
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( Widget widget : widgets )
        {
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            renameBorderPropertyValues( propsCont.getList(), oldName, newName );
            //break;
        }
    }
    
    public void resetBorders( String oldName, String newValue )
    {
        String border = borderMap.get( oldName );
        if ( border == null )
            return;
        
        borderMap.remove( oldName );
        
        FlatWidgetPropertiesContainer propsCont = new FlatWidgetPropertiesContainer();
        for ( Widget widget : widgets )
        {
            propsCont.clear();
            widget.getProperties( propsCont, true );
            
            renameBorderPropertyValues( propsCont.getList(), oldName, newValue );
            //break;
        }
    }
    
    /**
     * Gets whether class relative scoring is enabled or not.
     * 
     * @return whether class relative scoring is enabled or not.
     */
    public final boolean getUseClassScoring()
    {
        return ( useClassScoring.getBooleanValue() );
    }
    
    /**
     * Saves all settings to the config file.
     * 
     * @param writer the writer to write to
     * 
     * @throws IOException if something went wrong
     */
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( useClassScoring, "Ignore vehicles from other classes than the viewed one for scoring?" );
    }
    
    /**
     * Loads (and parses) a certain property from a config file.
     * 
     * @param loader the loader to load from
     */
    public void loadProperty( ConfigurationLoader loader )
    {
        if ( loader.loadProperty( useClassScoring ) );
    }
    
    /**
     * Puts all editable properties to the editor.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        //propsCont.addGroup( "General" );
        
        propsCont.addProperty( useClassScoring );
    }
    
    private String getDocumentationSource( Class<?> clazz, Property property )
    {
        URL docURL = this.getClass().getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/" + property.getName() + ".html" );
        
        if ( docURL == null )
        {
            if ( ( clazz.getSuperclass() != null ) && ( clazz.getSuperclass() != Object.class ) )
                return ( getDocumentationSource( clazz.getSuperclass(), property ) );
            
            return ( "" );
        }
        
        return ( StringUtil.loadString( docURL ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getDocumentationSource( Property property )
    {
        if ( property == null )
            return ( "" );
        
        return ( getDocumentationSource( this.getClass(), property ) );
    }
    
    /**
     * Creates a new {@link WidgetsConfiguration}.
     */
    public WidgetsConfiguration()
    {
    }
}
