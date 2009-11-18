package net.ctdp.rfdynhud.widgets.widget;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.editor.properties.BorderProperty;
import net.ctdp.rfdynhud.editor.properties.ColorProperty;
import net.ctdp.rfdynhud.editor.properties.FontProperty;
import net.ctdp.rfdynhud.editor.properties.Property;
import net.ctdp.rfdynhud.editor.properties.PropertyEditorType;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TexturedBorder;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets._util.DrawnString;
import net.ctdp.rfdynhud.widgets._util.FontUtils;
import net.ctdp.rfdynhud.widgets._util.Position;
import net.ctdp.rfdynhud.widgets._util.RelativePositioning;
import net.ctdp.rfdynhud.widgets._util.Size;
import net.ctdp.rfdynhud.widgets._util.WidgetsConfigurationWriter;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * This is the base for all Widgets to be drawn on the HUD.<br>
 * Any concrete extension must have a parameterless constructor.
 * 
 * @author Marvin Froehlich
 */
public abstract class Widget implements Documented
{
    private WidgetsConfiguration config = null;
    
    private static final HashMap<Class<? extends Widget>, Object> generalStores = new HashMap<Class<? extends Widget>, Object>();
    private Object generalStore = null;
    private Object localStore = null;
    
    private boolean dirtyFlag = true;
    
    private String name = "";
    
    private final Position position;
    private final Size size;
    
    private String backgroundColorKey = "StandardBackground";
    private Color backgroundColor = null;
    private String fontKey = "StandardFont";
    private Font font = null;
    private String fontColorKey = "StandardFontColor";
    private Color fontColor = null;
    
    //private String borderName = "yellow_border.png";
    private String borderName = "StandardBorder";
    private BorderWrapper border = null;
    
    private boolean visible = true;
    private boolean needsCompleteRedraw = true;
    private boolean needsCompleteClear = false;
    
    private boolean initialized = false;
    
    final void setConfiguration( WidgetsConfiguration config )
    {
        this.config = config;
    }
    
    /**
     * Gets the {@link WidgetsConfiguration}, this {@link Widget} is a member of.
     * 
     * @return the {@link WidgetsConfiguration}, this {@link Widget} is a member of.
     */
    public final WidgetsConfiguration getConfiguration()
    {
        return ( config );
    }
    
    /**
     * Creates a store object for all widgets of this type.
     * 
     * @return the general store object. <code>null</code> is explicitly permitted and default implementation simply returns <code>null</code>.
     */
    protected Object createGeneralStore()
    {
        return ( null );
    }
    
    protected Class<? extends Widget> getGeneralStoreKey()
    {
        return ( this.getClass() );
    }
    
    /**
     * Gets a value store object for all {@link Widget}s of this class.
     * 
     * @return a value store object for all {@link Widget}s of this class.
     */
    public final Object getGeneralStore()
    {
        if ( generalStore == null )
        {
            final Class<? extends Widget> key = getGeneralStoreKey();
            if ( !generalStores.containsKey( key ) )
            {
                generalStore = createGeneralStore();
                generalStores.put( key, generalStore );
            }
            
            generalStore = generalStores.get( key );
        }
        
        return ( generalStore );
    }
    
    /**
     * Creates a store object for this Widget only.
     * 
     * @return the local store object. <code>null</code> is explicitly permitted and default implementation simply returns <code>null</code>.
     */
    protected Object createLocalStore()
    {
        return ( null );
    }
    
    final void setLocalStore( Object localStore )
    {
        this.localStore = localStore;
    }
    
    /**
     * Gets a value store object for this {@link Widget}.
     * The store is restored when the widget configuration is reloaded.
     * The object is stored by the {@link Widget}'s class and name.
     * 
     * @return a value store object for this {@link Widget}.
     */
    public Object getLocalStore()
    {
        if ( localStore == null )
        {
            localStore = createLocalStore();
        }
        
        return ( localStore );
    }
    
    /**
     * Gets the InputActions, that can be bound with a Widget of this type.
     * "Override" this method to return your own custom actions.
     * 
     * @return the InputActions, that can be bound with a Widget of this type.
     */
    public InputAction[] getInputActions()
    {
        return ( null );
    }
    
    /**
     * Gets the {@link TransformableTexture}s, that this {@link Widget} keeps.
     * 
     * @param isEditorMode
     * @param widgetWidth
     * @param widgetHeight
     * 
     * @return the {@link TransformableTexture}s, that this {@link Widget} keeps or null for no textures.
     */
    public TransformableTexture[] getSubTextures( boolean isEditorMode, int widgetInnerWidth, int widgetInnerHeight )
    {
        return ( null );
    }
    
    public void setDirtyFlag()
    {
        this.dirtyFlag = true;
    }
    
    public final boolean getDirtyFlag( boolean reset )
    {
        boolean result = dirtyFlag;
        
        if ( reset )
            this.dirtyFlag = false;
        
        return ( result );
    }
    
    public void forceReinitialization()
    {
        this.initialized = false;
        setDirtyFlag();
    }
    
    /**
     * Sets this {@link Widget}'s name.
     * 
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
        setDirtyFlag();
    }
    
    /**
     * Gets this {@link Widget}'s name.
     * 
     * @return this {@link Widget}'s name.
     */
    public final String getName()
    {
        return ( name );
    }
    
    /**
     * Gets the {@link Widget}'s position.
     * 
     * @return the {@link Widget}'s position.
     */
    public final Position getPosition()
    {
        return ( position );
    }
    
    /**
     * Gets, whether this Widget has a fixed (unmodifiable) size.
     * 
     * @return whether this Widget has a fixed (unmodifiable) size.
     */
    public boolean hasFixedSize()
    {
        return ( false );
    }
    
    /**
     * Gets this Widget's size.
     * 
     * @return this Widget's width.
     */
    public final Size getSize()
    {
        return ( size );
    }
    
    /**
     * Gets the minimum width for this {@link Widget} in pixels.
     * 
     * @return the minimum width for this {@link Widget} in pixels.
     */
    public int getMinWidth()
    {
        return ( 25 );
    }
    
    /**
     * Gets the minimum height for this {@link Widget} in pixels.
     * 
     * @return the minimum height for this {@link Widget} in pixels.
     */
    public int getMinHeight()
    {
        return ( 25 );
    }
    
    /**
     * Gets the maximum width covered by this {@link Widget}.
     * By default this method returns the result of getEffectiveWidth(gameResX).
     * Override this method, if it will change its size during game play.
     * 
     * @param texCanvas
     * 
     * @return the maximum width covered by this {@link Widget}.
     */
    public int getMaxWidth( Texture2DCanvas texCanvas )
    {
        return ( size.getEffectiveWidth() );
    }
    
    /**
     * Gets the maximum height covered by this {@link Widget}.
     * By default this method returns the result of getEffectiveHeight(gameResX).
     * Override this method, if it will change its size during game play.
     * 
     * @param texCanvas
     * 
     * @return the maximum height covered by this {@link Widget}.
     */
    public int getMaxHeight( Texture2DCanvas texCanvas )
    {
        return ( size.getEffectiveHeight() );
    }
    
    /**
     * Bakes effective position and size to variables, so that they don't need to be recalculated
     * during runtime on each access.
     */
    public void bake()
    {
        position.bake();
        size.bake();
    }
    
    /**
     * Sets the {@link Widget}'s background color.
     * 
     * @param color as hex string
     */
    public void setBackgroundColor( String color )
    {
        this.backgroundColorKey = color;
        this.backgroundColor = null;
        
        forceAndSetDirty();
    }
    
    /**
     * Sets the {@link Widget}'s background color.
     * 
     * @param color
     */
    public final void setBackgroundColor( Color color )
    {
        setBackgroundColor( ColorUtils.colorToHex( color ) );
    }
    
    /**
     * Sets the {@link Widget}'s background color.
     * 
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    public final void setBackgroundColor( int red, int green, int blue, int alpha )
    {
        setBackgroundColor( ColorUtils.colorToHex( red, green, blue, alpha ) );
    }
    
    /**
     * Gets the {@link Widget}'s background color.
     * 
     * @return the {@link Widget}'s background color.
     */
    public final Color getBackgroundColor()
    {
        backgroundColor = ColorProperty.getColorFromColorKey( backgroundColorKey, backgroundColor, getConfiguration() );
        
        return ( backgroundColor );
    }
    
    public final boolean hasBackgroundColor()
    {
        return ( backgroundColorKey != null );
    }
    
    public void setFont( String font )
    {
        this.fontKey = font;
        this.font = null;
        
        forceAndSetDirty();
    }
    
    public final void setFont( Font font, boolean virtual )
    {
        setFont( FontUtils.getFontString( font, virtual ) );
    }
    
    public final Font getFont()
    {
        font = FontProperty.getFontFromFontKey( fontKey, font, getConfiguration() );
        
        return ( font );
    }
    
    /**
     * Sets the {@link Widget}'s font color.
     * 
     * @param color the color as hex string
     */
    public void setFontColor( String color )
    {
        this.fontColorKey = color;
        this.fontColor = null;
        
        forceAndSetDirty();
    }
    
    /**
     * Sets the {@link Widget}'s font color.
     * 
     * @param color
     */
    public final void setFontColor( Color color )
    {
        setFontColor( ColorUtils.colorToHex( color ) );
    }
    
    /**
     * Sets the {@link Widget}'s font color.
     * 
     * @param red
     * @param green
     * @param blue
     */
    public final void setFontColor( int red, int green, int blue )
    {
        setFontColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    /**
     * Gets the {@link Widget}'s font color.
     * 
     * @return the {@link Widget}'s font color.
     */
    public final Color getFontColor()
    {
        fontColor = ColorProperty.getColorFromColorKey( fontColorKey, fontColor, getConfiguration() );
        
        return ( fontColor );
    }
    
    /**
     * Sets the {@link Widget}'s border.
     * 
     * @param border the border filename or border alias
     */
    public void setBorder( String border )
    {
        this.borderName = border;
        this.border = null;
        
        forceAndSetDirty();
    }
    
    /**
     * Returns a {@link BorderWrapper}, that encapsulates the actual used border with convenience wrappers for the size getters.
     * The {@link BorderWrapper} instance is never null while the border can be null.
     * 
     * @return a {@link BorderWrapper} for the used Border (never null).
     */
    public final BorderWrapper getBorder()
    {
        border = BorderProperty.getBorderFromBorderName( borderName, border, getConfiguration() );
        
        return ( border );
    }
    
    /**
     * Gets whether this {@link Widget} has a border or not.
     * 
     * @return whether this {@link Widget} has a border or not.
     */
    public final boolean hasBorder()
    {
        return ( getBorder().getBorder() != null );
    }
    
    /**
     * Forces a complete redraw on the next render.
     */
    public void forceCompleteRedraw()
    {
        this.needsCompleteRedraw = true;
    }
    
    /**
     * This simply calls {@link #forceCompleteRedraw()}, {@link #forceReinitialization()} and {@link #setDirtyFlag()}.
     * This method must be called after a value has been changed, that requires a reinitialization of all positioned strings, etc.
     */
    public final void forceAndSetDirty()
    {
        forceCompleteRedraw();
        forceReinitialization();
        setDirtyFlag();
    }
    
    /**
     * Sets this Widget's visibility flag.
     * 
     * @param visible
     */
    public void setVisible( boolean visible )
    {
        if ( visible == this.visible )
            return;
        
        this.visible = visible;
        
        if ( visible )
        {
            this.needsCompleteRedraw = true;
        }
        else
        {
            this.needsCompleteClear = true;
        }
        
        setDirtyFlag();
    }
    
    /**
     * Gets this Widget's visibility flag.
     * 
     * @return this Widget's visibility flag.
     */
    public final boolean isVisible()
    {
        return ( visible );
    }
    
    /**
     * This method is called first by the rendering system each frame before {@link #isVisible()} is checked.
     * 
     * @param gameData
     */
    public void updateVisibility( LiveGameData gameData )
    {
    }
    
    private final boolean needsCompleteRedraw()
    {
        boolean result = needsCompleteRedraw;
        
        needsCompleteRedraw = false;
        
        return ( result );
    }
    
    /**
     * Gets, whether this Widget has just been set invisible and its area hence needs to be cleared.
     * The flag is forced to false after this method has been called.
     * 
     * @return whether this Widget has just been set invisible and its area hence needs to be cleared.
     */
    public final boolean needsCompleteClear()
    {
        boolean result = needsCompleteClear;
        
        needsCompleteClear = false;
        
        return ( result );
    }
    
    /**
     * This event is fired right after the {@link WidgetsConfiguration} has been (re-)loaded.
     * 
     * @param widgetsConfig
     * @param gameData
     */
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData )
    {
    }
    
    /**
     * This event is fired right before the {@link WidgetsConfiguration} is cleared.
     * 
     * @param widgetsConfig
     * @param gameData
     */
    public void beforeConfigurationCleared( WidgetsConfiguration widgetsConfig, LiveGameData gameData )
    {
    }
    
    /**
     * This method is executed when a new track was loaded.
     * 
     * @param trackname
     * @param gameData
     */
    public void onTrackChanged( String trackname, LiveGameData gameData )
    {
    }
    
    /**
     * This method is executed when a new session was started.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param sessionType
     * @param gameData
     */
    public void onSessionStarted( boolean isEditorMode, SessionType sessionType, LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when a the user entered realtime mode.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param gameData
     */
    public void onRealtimeEntered( boolean isEditorMode, LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when a the car entered the pits.
     * 
     * @param gameData
     */
    public void onPitsEntered( LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when a the car entered the garage.
     * 
     * @param gameData
     */
    public void onGarageEntered( LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when a the car exited the garage.
     * 
     * @param gameData
     */
    public void onGarageExited( LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when a the car exited the pits.
     * 
     * @param gameData
     */
    public void onPitsExited( LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when a the user exited realtime mode.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param gameData
     */
    public void onRealtimeExited( boolean isEditorMode, LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when a lap has been finished and new new one was started.
     * 
     * @param isEditorMode
     * @param gameData
     */
    public void onLapStarted( boolean isEditorMode, LiveGameData gameData )
    {
    }
    
    /**
     * This method is called when the driver has finished a lap and started a new one.
     * 
     * @param isEditorMode
     * @param gameData
     */
    public void onPlayerLapStarted( boolean isEditorMode, LiveGameData gameData )
    {
    }
    
    /**
     * This event method is invoked when the engine boost mapping has changed.
     * 
     * @param oldValue
     * @param newValue
     */
    public void onEngineBoostMappingChanged( int oldValue, int newValue )
    {
    }
    
    /**
     * This event method is invoked when the temporary engine boost key or button state has changed.
     * 
     * @param enabled
     */
    public void onTemporaryEngineBoostStateChanged( boolean enabled )
    {
    }
    
    /**
     * This event is fired, when a bound input component has changed its state.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param action
     * @param state
     * @param modifierMask see {@link InputAction}
     */
    public abstract void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask );
    
    /**
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param texture
     */
    public void clearRegion( boolean isEditorMode, TextureImage2D texture )
    {
        int offsetX = position.getEffectiveX();
        int offsetY = position.getEffectiveY();
        int width = size.getEffectiveWidth();
        int height = size.getEffectiveHeight();
        
        texture.getTextureCanvas().pushClip( offsetX, offsetY, width, height );
        
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        texture.getTextureCanvas().popClip();
    }
    
    /**
     * Checks, if the Widget needs any changes fore it is drawn.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display
     * @param gameData the live game data
     * @param texCanvas the texture canvas to draw on. Use {@link Texture2DCanvas#getImage()} to retrieve the {@link TextureImage2D} for fast drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     * 
     * @return true, if size has changed.
     */
    protected abstract boolean checkForChanges( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height );
    
    /**
     * This method is called once to initialized {@link DrawnString}s used on this Widget.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display
     * @param gameData the live game data
     * @param texCanvas the texture canvas to draw on. Use {@link Texture2DCanvas#getImage()} to retrieve the {@link TextureImage2D} for fast drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     */
    protected abstract void initialize( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height );
    
    /**
     * This method must contain the actual drawing code for this Widget.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display
     * @param gameData the live game data
     * @param texCanvas the texture canvas to draw on. Use {@link Texture2DCanvas#getImage()} to retrieve the {@link TextureImage2D} for fast drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     * @param needsCompleteRedraw whether this widget needs to be completely redrawn (true) or just the changed parts (false)
     */
    protected abstract void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw );
    
    /**
     * 
     * @param isEditorMode
     * @param border
     * @param texCanvas
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     * @param needsCompleteRedraw
     */
    protected void drawBorder( boolean isEditorMode, TexturedBorder border, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height, boolean needsCompleteRedraw )
    {
        if ( border != null )
        {
            texCanvas.setClip( offsetX, offsetY, width, height );
            border.drawBorder( texCanvas, offsetX, offsetY, width, height );
        }
    }
    
    /**
     * 
     * @param isEditorMode
     * @param gameData
     * @param texCanvas
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    protected void clearBackground( boolean isEditorMode, LiveGameData gameData, Texture2DCanvas texCanvas, int offsetX, int offsetY, int width, int height )
    {
        if ( hasBackgroundColor() )
            texCanvas.getImage().clear( getBackgroundColor(), offsetX, offsetY, width, height, true, null );
    }
    
    /**
     * This method must contain the actual drawing code for this Widget.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display
     * @param gameData the live game data
     * @param texCanvas the texture canvas to draw on. Use {@link Texture2DCanvas#getImage()} to retrieve the {@link TextureImage2D} for fast drawing.
     * @param gameResX
     * @param gameResY
     * @param width the width on the texture
     * @param height the height on the texture
     * @param completeRedrawForced
     */
    public final void drawWidget( boolean isEditorMode, boolean clock1, boolean clock2, LiveGameData gameData, Texture2DCanvas texCanvas, boolean completeRedrawForced )
    {
        int offsetX = position.getEffectiveX();
        int offsetY = position.getEffectiveY();
        int width = size.getEffectiveWidth();
        int height = size.getEffectiveHeight();
        
        int borderOLW = getBorder().getOpaqueLeftWidth();
        int borderOTH = getBorder().getOpaqueTopHeight();
        int borderORW = getBorder().getOpaqueRightWidth();
        int borderOBH = getBorder().getOpaqueBottomHeight();
        
        int borderLW = getBorder().getInnerLeftWidth();
        int borderTH = getBorder().getInnerTopHeight();
        int borderRW = getBorder().getInnerRightWidth();
        int borderBH = getBorder().getInnerBottomHeight();
        
        int offsetX2 = offsetX + borderLW;
        int offsetY2 = offsetY + borderTH;
        int width2 = width - borderLW - borderRW;
        int height2 = height - borderTH - borderBH;
        
        if ( !initialized )
        {
            initialize( isEditorMode, clock1, clock2, gameData, texCanvas, offsetX2, offsetY2, width2, height2 );
            
            initialized = true;
        }
        
        if ( checkForChanges( isEditorMode, clock1, clock2, gameData, texCanvas, offsetX2, offsetY2, width2, height2 ) )
        {
            completeRedrawForced = true;
            
            offsetX = position.getEffectiveX();
            offsetY = position.getEffectiveY();
            width = size.getEffectiveWidth();
            height = size.getEffectiveHeight();
            
            offsetX2 = offsetX + borderLW;
            offsetY2 = offsetY + borderTH;
            width2 = width - borderLW - borderRW;
            height2 = height - borderTH - borderBH;
        }
        
        texCanvas.setClip( offsetX, offsetY, width, height );
        
        completeRedrawForced = needsCompleteRedraw() || completeRedrawForced;
        
        if ( completeRedrawForced )
        {
            drawBorder( isEditorMode, getBorder().getBorder(), texCanvas, offsetX, offsetY, width, height, completeRedrawForced );
        }
        
        if ( completeRedrawForced )
        {
            texCanvas.getImage().markDirty( offsetX, offsetY, width, height );
            
            clearBackground( isEditorMode, gameData, texCanvas, offsetX2, offsetY2, width2, height2 );
        }
        
        texCanvas.setClip( offsetX + borderOLW, offsetY + borderOTH, width - borderOLW - borderORW, height - borderOTH - borderOBH );
        
        drawWidget( isEditorMode, clock1, clock2, gameData, texCanvas, offsetX2, offsetY2, width2, height2, completeRedrawForced );
    }
    
    
    /**
     * Saves all settings to the config file.
     * 
     * @param writer
     */
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( "positioning", position.getPositioning(), "The way, position coordinates are interpreted (relative to). Valid values: TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER_CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT." );
        writer.writeProperty( "x", Position.unparseValue( position.getX() ), false, "The x-coordinate for the position." );
        writer.writeProperty( "y", Position.unparseValue( position.getY() ), false, "The y-coordinate for the position." );
        writer.writeProperty( "width", Size.unparseValue( size.getWidth() ), false, "The width. Use negative values to make the Widget be sized relative to screen size." );
        writer.writeProperty( "height", Size.unparseValue( size.getHeight() ), false, "The height. Use negative values to make the Widget be sized relative to screen size." );
        writer.writeProperty( "initiallyVisibile", isVisible(), "The initial visibility." );
        if ( hasBackgroundColor() )
            writer.writeProperty( "backgroundColor", backgroundColorKey, "The Widget's background color in the format #RRGGBBAA (hex)." );
        if ( hasText() )
        {
            writer.writeProperty( "font", fontKey, "The used font." );
            writer.writeProperty( "fontColor", fontColorKey, "The Widget's font color in the format #RRGGBB (hex)." );
        }
    }
    
    /**
     * Loads (and parses) a certain property from a config file.
     * 
     * @param key
     * @param value
     */
    public void loadProperty( String key, String value )
    {
        if ( key.equals( "name" ) )
            this.name = value;
        
        else if ( key.equals( "positioning" ) )
            this.position.set( RelativePositioning.valueOf( value ), position.getX(), position.getY() );
        
        else if ( key.equals( "x" ) )
            this.position.setX( Position.parseValue( value ) );
        
        else if ( key.equals( "y" ) )
            this.position.setY( Position.parseValue( value ) );
        
        else if ( key.equals( "width" ) )
            this.size.setWidth( Size.parseValue( value ) );
        
        else if ( key.equals( "height" ) )
            this.size.setHeight( Size.parseValue( value ) );
        
        else if ( key.equals( "initiallyVisibile" ) )
            this.visible = Boolean.parseBoolean( value );
        
        else if ( key.equals( "backgroundColor" ) )
            this.backgroundColorKey = value;
        
        else if ( key.equals( "font" ) )
            this.fontKey = value;
        
        else if ( key.equals( "fontColor" ) )
            this.fontColorKey = value;
    }
    
    /**
     * Puts all editable properties to the editor.
     * 
     * @param propsList
     */
    public void getProperties( FlaggedList propsList )
    {
        FlaggedList props = new FlaggedList( "General", true );
        
        props.add( new Property( "type", true, PropertyEditorType.STRING )
        {
            @Override
            public void setValue( Object value )
            {
            }
            
            @Override
            public Object getValue()
            {
                return ( Widget.this.getClass().getSimpleName() );
            }
        } );
        
        props.add( new Property( "name", PropertyEditorType.STRING )
        {
            @Override
            public void setValue( Object value )
            {
                setName( String.valueOf( value ) );
            }
            
            @Override
            public Object getValue()
            {
                return ( getName() );
            }
        } );
        
        props.add( position.createPositioningProperty( "positioning" ) );
        
        props.add( position.createXProperty( "x" ) );
        
        props.add( position.createYProperty( "y" ) );
        
        props.add( size.createWidthProperty( "width" ) );
        
        props.add( size.createHeightProperty( "height" ) );
        
        props.add( new Property( "initialVisibility", PropertyEditorType.BOOLEAN )
        {
            @Override
            public void setValue( Object value )
            {
                setVisible( ( (Boolean)value ).booleanValue() );
            }
            
            @Override
            public Object getValue()
            {
                return ( isVisible() );
            }
        } );
        
        if ( canHaveBorder() )
        {
            props.add( new BorderProperty( "border", getConfiguration() )
            {
                @Override
                public void setValue( Object value )
                {
                    setBorder( String.valueOf( value ) );
                }
                
                @Override
                public Object getValue()
                {
                    return ( borderName );
                }
            } );
        }
        
        if ( hasBackgroundColor() )
        {
            props.add( new ColorProperty( "backgroundColor", getConfiguration() )
            {
                @Override
                public void setValue( Object value )
                {
                    setBackgroundColor( String.valueOf( value ) );
                }
                
                @Override
                public Object getValue()
                {
                    return ( backgroundColorKey );
                }
            } );
        }
        
        if ( hasText() )
        {
            props.add( new FontProperty( "font", getConfiguration() )
            {
                @Override
                public void setValue( Object value )
                {
                    setFont( String.valueOf( value ) );
                }
                
                @Override
                public Object getValue()
                {
                    return ( fontKey );
                }
            } );
            
            props.add( new ColorProperty( "fontColor", getConfiguration() )
            {
                @Override
                public void setValue( Object value )
                {
                    setFontColor( String.valueOf( value ) );
                }
                
                @Override
                public Object getValue()
                {
                    return ( fontColorKey );
                }
            } );
        }
        
        propsList.add( props );
    }
    
    private String getDocumentationSource( Class<?> clazz, Property property )
    {
        URL docURL = null;
        
        if ( property == null )
            docURL = this.getClass().getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/widget.html" );
        else
            docURL = this.getClass().getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/" + property.getKey() + ".html" );
        
        if ( docURL == null )
        {
            if ( clazz != Widget.class )
                return ( getDocumentationSource( clazz.getSuperclass(), property ) );
            
            return ( "" );
        }
        
        return ( StringUtil.loadString( docURL ) );
    }
    
    /**
     * {@inheritDoc}
     */
    public final String getDocumentationSource( Property property )
    {
        return ( getDocumentationSource( this.getClass(), property ) );
    }
    
    
    
    /**
     * Defines, if a Widget type (potentially) contains any text.
     * If <code>false</code>, the editor won't provide font or font-color selection.
     * Should return a contant value.
     * 
     * @return if this Widget can contain any text.
     */
    protected boolean hasText()
    {
        return ( true );
    }
    
    /**
     * Defines, if this Widget type can have a border.
     * 
     * @return if this Widget type can have a border.
     */
    protected boolean canHaveBorder()
    {
        return ( true );
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param name
     * @param positioning
     * @param x
     * @param y
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    protected Widget( String name, RelativePositioning positioning, float x, float y, float width, float height )
    {
        this.name = name;
        this.size = new Size( width, height, this, true );
        this.position = new Position( positioning, x, y, size, this, true );
        
        if ( !canHaveBorder() )
            setBorder( null );
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param name
     * @param x
     * @param y
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    protected Widget( String name, float x, float y, float width, float height )
    {
        this( name, RelativePositioning.TOP_LEFT, x, y, width, height );
    }
    
    /**
     * Creates a new Widget.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    protected Widget( String name, float width, float height )
    {
        this( name, Position.PERCENT_OFFSET + 0f, Position.PERCENT_OFFSET + 0f, width, height );
    }
}
