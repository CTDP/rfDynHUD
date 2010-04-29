package net.ctdp.rfdynhud.widgets.widget;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TexturedBorder;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.Position;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

import org.openmali.types.twodee.Rect2i;

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
    
    private final StringProperty type = new StringProperty( this, "type", Widget.this.getClass().getSimpleName(), true );
    
    private final StringProperty name = new StringProperty( this, "name", "" );
    
    private final Position position;
    private final Size size;
    
    private final FontProperty font = new FontProperty( this, "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty backgroundColor = new ColorProperty( this, "backgroundColor", ColorProperty.STANDARD_BACKGROUND_COLOR_NAME );
    private final ColorProperty fontColor = new ColorProperty( this, "fontColor", ColorProperty.STANDARD_FONT_COLOR_NAME );
    
    private final BorderProperty border = new BorderProperty( this, "border", BorderProperty.DEFAULT_BORDER_NAME );
    
    private final BooleanProperty visible = new BooleanProperty( this, "initialVisibility", true );
    private boolean needsCompleteRedraw = true;
    private boolean needsCompleteClear = false;
    
    private boolean initialized = false;
    
    private TransformableTexture[] subTextures = null;
    
    /**
     * 
     * @param property
     * @param oldValue
     * @param newValue
     */
    protected void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        if ( property == visible )
        {
            if ( (Boolean)newValue )
                this.needsCompleteRedraw = true;
            else
                this.needsCompleteClear = true;
        }
    }
    
    /**
     * 
     * @param oldPositioning
     * @param oldX
     * @param oldY
     * @param newPositioning
     * @param newX
     * @param newY
     */
    protected void onPositionChanged( RelativePositioning oldPositioning, float oldX, float oldY, RelativePositioning newPositioning, float newX, float newY )
    {
        WidgetsConfiguration wc = getConfiguration();
        
        if ( wc != null )
        {
            __WCPrivilegedAccess.sortWidgets( wc );
        }
    }
    
    /**
     * 
     * @param oldWidth
     * @param oldHeight
     * @param newWidth
     * @param newHeight
     */
    protected void onSizeChanged( float oldWidth, float oldHeight, float newWidth, float newHeight )
    {
        WidgetsConfiguration wc = getConfiguration();
        
        if ( wc != null )
        {
            __WCPrivilegedAccess.sortWidgets( wc );
        }
    }
    
    /**
     * Gets the default value for the given named color.
     * 
     * @param name
     * 
     * @return the default value for the given named color.
     */
    public String getDefaultNamedColorValue( String name )
    {
        return ( ColorProperty.getDefaultNamedColorValue( name ) );
    }
    
    /**
     * Gets the default value for the given named font.
     * 
     * @param name
     * 
     * @return the default value for the given named font.
     */
    public String getDefaultNamedFontValue( String name )
    {
        return ( FontProperty.getDefaultNamedFontValue( name ) );
    }
    
    /**
     * Gets the package to group the Widget in the editor.
     * This can be an empty String to be displayed in the root or a slash separated path.
     * 
     * @return the package to group the Widget in the editor.
     */
    public abstract String getWidgetPackage();
    
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
     * @param gameData
     * @param editorPresets
     * @param widgetInnerWidth
     * @param widgetInnerHeight
     * 
     * @return the {@link TransformableTexture}s, that this {@link Widget} keeps or null for no textures.
     */
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        return ( null );
    }
    
    /**
     * Gets the {@link TransformableTexture}s, that this {@link Widget} keeps.
     * 
     * @param gameData
     * @param editorPresets
     * @param widgetInnerHeight
     * @param widgetInnerWidth
     * 
     * @return the {@link TransformableTexture}s, that this {@link Widget} keeps or null for no textures.
     */
    public final TransformableTexture[] getSubTextures( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( !initialized )
        {
            subTextures = getSubTexturesImpl( gameData, editorPresets, widgetInnerWidth, widgetInnerHeight );
        }
        
        return ( subTextures );
    }
    
    final boolean isInitialized()
    {
        return ( initialized );
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
        this.name.setStringValue( name );
        setDirtyFlag();
    }
    
    /**
     * Gets this {@link Widget}'s name.
     * 
     * @return this {@link Widget}'s name.
     */
    public final String getName()
    {
        return ( name.getStringValue() );
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
     * @param gameData
     * 
     * @return the minimum width for this {@link Widget} in pixels.
     */
    public int getMinWidth( LiveGameData gameData )
    {
        return ( 25 );
    }
    
    /**
     * Gets the minimum height for this {@link Widget} in pixels.
     * 
     * @param gameData
     * 
     * @return the minimum height for this {@link Widget} in pixels.
     */
    public int getMinHeight( LiveGameData gameData )
    {
        return ( 25 );
    }
    
    /**
     * Gets the maximum width covered by this {@link Widget}.
     * By default this method returns the result of getEffectiveWidth(gameResX).
     * Override this method, if it will change its size during game play.
     * 
     * @param gameData
     * @param texCanvas
     * 
     * @return the maximum width covered by this {@link Widget}.
     */
    public int getMaxWidth( LiveGameData gameData, Texture2DCanvas texCanvas )
    {
        return ( size.getEffectiveWidth() );
    }
    
    /**
     * Gets the maximum height covered by this {@link Widget}.
     * By default this method returns the result of getEffectiveHeight(gameResX).
     * Override this method, if it will change its size during game play.
     * 
     * @param gameData
     * @param texCanvas
     * 
     * @return the maximum height covered by this {@link Widget}.
     */
    public int getMaxHeight( LiveGameData gameData, Texture2DCanvas texCanvas )
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
    
    public void setAllPosAndSizeToPercents()
    {
        if ( !position.isXPercentageValue() )
            position.flipXPercentagePx();
        
        if ( !position.isYPercentageValue() )
            position.flipYPercentagePx();
        
        if ( !size.isWidthPercentageValue() )
            size.flipWidthPercentagePx();
        
        if ( !size.isHeightPercentageValue() )
            size.flipHeightPercentagePx();
    }
    
    public void setAllPosAndSizeToPixels()
    {
        if ( position.isXPercentageValue() )
            position.flipXPercentagePx();
        
        if ( position.isYPercentageValue() )
            position.flipYPercentagePx();
        
        if ( size.isWidthPercentageValue() )
            size.flipWidthPercentagePx();
        
        if ( size.isHeightPercentageValue() )
            size.flipHeightPercentagePx();
    }
    
    protected final ColorProperty getBackgroundColorProperty()
    {
        return ( backgroundColor );
    }
    
    /**
     * Gets the {@link Widget}'s background color.
     * 
     * @return the {@link Widget}'s background color.
     */
    protected final Color getBackgroundColor()
    {
        return ( backgroundColor.getColor() );
    }
    
    protected final boolean hasBackgroundColor()
    {
        return ( backgroundColor.getColorKey() != null );
    }
    
    protected final FontProperty getFontProperty()
    {
        return ( font );
    }
    
    protected final java.awt.Font getFont()
    {
        return ( font.getFont() );
    }
    
    protected final boolean isFontAntiAliased()
    {
        return ( font.isAntiAliased() );
    }
    
    protected final ColorProperty getFontColorProperty()
    {
        return ( fontColor );
    }
    
    /**
     * Gets the {@link Widget}'s font color.
     * 
     * @return the {@link Widget}'s font color.
     */
    protected final Color getFontColor()
    {
        return ( fontColor.getColor() );
    }
    
    /**
     * Returns a {@link BorderWrapper}, that encapsulates the actual used border with convenience wrappers for the size getters.
     * The {@link BorderWrapper} instance is never null while the border can be null.
     * 
     * @return a {@link BorderWrapper} for the used Border (never null).
     */
    public final BorderWrapper getBorder()
    {
        return ( border.getBorder() );
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
        this.visible.setBooleanValue( visible );
    }
    
    /**
     * Gets this Widget's visibility flag.
     * 
     * @return this Widget's visibility flag.
     */
    public final boolean isVisible()
    {
        return ( visible.getBooleanValue() );
    }
    
    /**
     * This method is called first by the rendering system each frame before {@link #isVisible()} is checked.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void updateVisibility( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets )
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
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This event is fired right before the {@link WidgetsConfiguration} is cleared.
     * 
     * @param widgetsConfig
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void beforeConfigurationCleared( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is executed when a new track was loaded.
     * 
     * @param trackname
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onTrackChanged( String trackname, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is executed when a new session was started.
     * 
     * @param sessionType
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the user entered realtime mode.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car entered the pits.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onPitsEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car entered the garage.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onGarageEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car exited the garage.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onGarageExited( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car exited the pits.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onPitsExited( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the user exited realtime mode.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a lap has been finished and new new one was started.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onLapStarted( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when the driver has finished a lap and started a new one.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onPlayerLapStarted( LiveGameData gameData, EditorPresets editorPresets )
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
     * @param action
     * @param state
     * @param modifierMask see {@link InputAction}
     * @param when
     * @param gameData
     * @param editorPresets
     */
    public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
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
     * Checks, if the Widget needs any changes before it is drawn. If true, {@link #drawBorder(boolean, TexturedBorder, TextureImage2D, int, int, int, int)}
     * and {@link #clearBackground(boolean, LiveGameData, TextureImage2D, int, int, int, int)} are (re-)invoked.<br />
     * The original method is just an empty stub returning false.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     * 
     * @return true, if size has changed.
     */
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * This method is called once to initialized {@link DrawnString}s used on this Widget.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     */
    protected abstract void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height );
    
    /**
     * This method must contain the actual drawing code for this Widget.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param needsCompleteRedraw whether this widget needs to be completely redrawn (true) or just the changed parts (false)
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     */
    protected abstract void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height );
    
    /**
     * 
     * @param isEditorMode
     * @param border
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    protected void drawBorder( boolean isEditorMode, TexturedBorder border, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( border != null )
        {
            Texture2DCanvas texCanvas = texture.getTextureCanvas();
            texCanvas.setClip( offsetX, offsetY, width, height );
            border.drawBorder( texCanvas, offsetX, offsetY, width, height );
        }
    }
    
    /**
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( hasBackgroundColor() )
            texture.clear( getBackgroundColor(), offsetX, offsetY, width, height, true, null );
    }
    
    /**
     * This method invokes the parts of the actual drawing code for this Widget.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param completeRedrawForced
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     */
    public final void drawWidget( boolean clock1, boolean clock2, boolean completeRedrawForced, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture )
    {
        boolean wasInitialized = initialized;
        
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
        
        final Texture2DCanvas texCanvas = texture.getTextureCanvas();
        
        if ( !initialized )
        {
            initialize( clock1, clock2, gameData, editorPresets, texture, offsetX2, offsetY2, width2, height2 );
            
            initialized = true;
        }
        
        if ( checkForChanges( clock1, clock2, gameData, editorPresets, texture, offsetX2, offsetY2, width2, height2 ) )
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
            drawBorder( ( editorPresets != null ), getBorder().getBorder(), texture, offsetX, offsetY, width, height );
        }
        
        if ( completeRedrawForced )
        {
            texture.markDirty( offsetX, offsetY, width, height );
            
            clearBackground( gameData, editorPresets, texture, offsetX2, offsetY2, width2, height2 );
        }
        
        texCanvas.setClip( offsetX + borderOLW, offsetY + borderOTH, width - borderOLW - borderORW, height - borderOTH - borderOBH );
        
        drawWidget( clock1, clock2, completeRedrawForced, gameData, editorPresets, texture, offsetX2, offsetY2, width2, height2 );
        
        if ( editorPresets != null )
        {
            initialized = wasInitialized;
            TransformableTexture[] subTextures = getSubTextures( gameData, editorPresets, width2, height2 );
            initialized = true;
            
            if ( subTextures != null )
            {
                texCanvas.setClip( (Rect2i)null );
                
                for ( int i = 0; i < subTextures.length; i++ )
                {
                    subTextures[i].drawInEditor( texCanvas, offsetX2, offsetY2 );
                }
            }
        }
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
        writer.writeProperty( border, "The widget's border." );
        writer.writeProperty( visible, "The initial visibility." );
        
        if ( hasBackgroundColor() )
        {
            writer.writeProperty( backgroundColor, "The Widget's background color in the format #RRGGBBAA (hex)." );
        }
        
        if ( hasText() )
        {
            writer.writeProperty( font, "The used font." );
            writer.writeProperty( fontColor, "The Widget's font color in the format #RRGGBB (hex)." );
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
        if ( name.loadProperty( key, value ) );
        else if ( position.loadProperty( key, value, "positioning", "x", "y" ) );
        else if ( size.loadProperty( key, value, "width", "height" ) );
        else if ( canHaveBorder() && border.loadProperty( key, value ) );
        else if ( visible.loadProperty( key, value ) );
        else if ( backgroundColor.loadProperty( key, value ) );
        else if ( font.loadProperty( key, value ) );
        else if ( fontColor.loadProperty( key, value ) );
    }
    
    protected void addBorderPropertyToContainer( BorderProperty property, WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( property );
    }
    
    protected void addBackgroundColorPropertyToContainer( ColorProperty property, WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( property );
    }
    
    protected void addFontPropertiesToContainer( FontProperty font, ColorProperty fontColor, WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( font );
        propsCont.addProperty( fontColor );
    }
    
    /**
     * Puts all editable properties to the editor.
     * 
     * @param propsCont
     * @param forceAll
     */
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "General" );
        
        propsCont.addProperty( type );
        
        propsCont.addProperty( name );
        propsCont.addProperty( position.createPositioningProperty( "positioning" ) );
        propsCont.addProperty( position.createXProperty( "x" ) );
        propsCont.addProperty( position.createYProperty( "y" ) );
        propsCont.addProperty( size.createWidthProperty( "width" ) );
        propsCont.addProperty( size.createHeightProperty( "height" ) );
        propsCont.addProperty( visible );
        
        if ( canHaveBorder() )
        {
            addBorderPropertyToContainer( border, propsCont, forceAll );
        }
        
        if ( hasBackgroundColor() )
        {
            addBackgroundColorPropertyToContainer( backgroundColor, propsCont, forceAll );
        }
        
        if ( hasText() )
        {
            addFontPropertiesToContainer( font, fontColor, propsCont, forceAll );
        }
    }
    
    private String getDocumentationSource( Class<?> clazz, Property property )
    {
        URL docURL = null;
        
        if ( property == null )
            docURL = this.getClass().getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/widget.html" );
        else
            docURL = this.getClass().getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/" + property.getPropertyName() + ".html" );
        
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
        this.name.setStringValue( name );
        this.size = new Size( width, height, this, true );
        this.position = new Position( positioning, x, y, size, this, true );
        
        if ( !canHaveBorder() )
            border.setBorder( null );
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
