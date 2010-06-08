package net.ctdp.rfdynhud.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputMapping;
import net.ctdp.rfdynhud.input.InputMappingsManager;
import net.ctdp.rfdynhud.input.KnownInputActions;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link WidgetsDrawingManager} handles the drawing of all visible widgets.
 * 
 * @author Marvin Froehlich
 */
public class WidgetsDrawingManager extends WidgetsConfiguration
{
    private TransformableTexture[] textures;
    private TransformableTexture[][] widgetSubTextures = null;
    private final ByteBuffer textureBuffer = TransformableTexture.createByteBuffer();
    
    private long frameCounter = 0;
    
    private long measureStart = -1L;
    private long measureEnd = -1L;
    private long measureFrameCounter = 0;
    
    //private long nextClockTime1 = -1L;
    //private long nextClockTime2 = -1L;
    
    private final long CLOCK_DELAY1 = 50000000L; // 50 ms
    private final long CLOCK_DELAY2 = 150000000L; // 100 ms
    
    private long clock1Frames = 10;
    private long clock2Frames = 30;
    
    public final TextureImage2D getMainTexture()
    {
        return ( textures[0].getTexture() );
    }
    
    /**
     * This method is executed when a new track was loaded.<br>
     * <br>
     * Calls {@link Widget#onTrackChanged(String, LiveGameData)} on each Widget.
     */
    public void fireOnTrackChanged( String trackname, LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onTrackChanged( trackname, gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    private final ArrayList<Widget> waitingWidgets = new ArrayList<Widget>();
    
    /**
     * This method is called when a new session was started.<br>
     * <br>
     * Calls {@link Widget#onSessionStarted(boolean, SessionType, LiveGameData)} on each Widget.
     */
    public void fireOnSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        //nextClockTime1 = 0L;
        //nextClockTime2 = 0L;
        
        waitingWidgets.clear();
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = getWidget( i );
            
            try
            {
                widget.onSessionStarted( sessionType, gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
            
            waitingWidgets.add( widget );
        }
    }
    
    /**
     * This method is called when a the user entered realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeEntered(boolean, LiveGameData)} on each Widget.
     */
    public void fireOnRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        measureEnd = -1L;
        frameCounter = 0;
        
        waitingWidgets.clear();
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = getWidget( i );
            
            try
            {
                widget.onRealtimeEntered( gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
            
            waitingWidgets.add( widget );
        }
    }
    
    /**
     * This method is called when a the user entered realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeEntered(boolean, LiveGameData)} on each Widget.
     */
    public void checkAndFireOnNeededDataComplete( LiveGameData gameData, EditorPresets editorPresets )
    {
        if ( waitingWidgets.size() == 0 )
            return;
        
        for ( int i = waitingWidgets.size() - 1; i >= 0; i-- )
        {
            Widget widget = waitingWidgets.get( i );
            int neededData = ( widget.getNeededData() & Widget.NEEDED_DATA_ALL );
            
            if ( ( ( neededData & Widget.NEEDED_DATA_TELEMETRY ) != 0 ) && gameData.getTelemetryData().isUpdatedInRealtimeMode() )
                neededData &= ~Widget.NEEDED_DATA_TELEMETRY;
            
            if ( ( ( neededData & Widget.NEEDED_DATA_SCORING ) != 0 ) && !gameData.getScoringInfo().isUpdatedInRealtimeMode() )
                neededData &= ~Widget.NEEDED_DATA_SCORING;
            
            if ( ( ( neededData & Widget.NEEDED_DATA_SETUP ) != 0 ) && !gameData.getSetup().isUpdatedInRealtimeMode() )
                neededData &= ~Widget.NEEDED_DATA_SETUP;
            
            if ( neededData == 0 )
            {
                widget.onNeededDataComplete( gameData, editorPresets );
                waitingWidgets.remove( i );
                
                widget.forceReinitialization();
                widget.forceCompleteRedraw();
            }
        }
    }
    
    public int collectTextures( LiveGameData gameData, EditorPresets editorPresets )
    {
        textures[0].generateSubRectangles( gameData, editorPresets, this );
        
        widgetSubTextures = new TransformableTexture[ getNumWidgets() ][];
        
        final int numWidgets = getNumWidgets();
        int numTextures = 1;
        for ( int i = 0; i < numWidgets; i++ )
        {
            Widget widget = getWidget( i );
            TransformableTexture[] subTextures = widget.getSubTextures( gameData, editorPresets, widget.getSize().getEffectiveWidth() - widget.getBorder().getInnerLeftWidth() - widget.getBorder().getInnerRightWidth(), widget.getSize().getEffectiveHeight() - widget.getBorder().getInnerTopHeight() - widget.getBorder().getInnerBottomHeight() );
            if ( ( subTextures != null ) && ( subTextures.length > 0 ) )
            {
                widgetSubTextures[i] = subTextures;
                
                if ( textures.length < numTextures + subTextures.length )
                {
                    TransformableTexture[] tmp = new TransformableTexture[ numTextures + subTextures.length ];
                    System.arraycopy( textures, 0, tmp, 0, numTextures );
                    textures = tmp;
                }
                
                System.arraycopy( subTextures, 0, textures, numTextures, subTextures.length );
                numTextures += subTextures.length;
            }
            else
            {
                widgetSubTextures[i] = null;
            }
        }
        
        if ( numTextures > TransformableTexture.MAX_NUM_TEXTURES )
        {
            Logger.log( "WARNING: Number of displayed textures truncated. Possible reason: maxOpponents = " + gameData.getModInfo().getMaxOpponents() );
            
            numTextures = TransformableTexture.MAX_NUM_TEXTURES;
        }
        
        if ( numTextures < textures.length )
        {
            TransformableTexture[] tmp = new TransformableTexture[ numTextures ];
            System.arraycopy( textures, 0, tmp, 0, numTextures );
            textures = tmp;
        }
        
        textureBuffer.position( 0 );
        textureBuffer.limit( textureBuffer.capacity() );
        
        textureBuffer.put( (byte)textures.length );
        
        int rectOffset = 0;
        for ( int i = 0; i < textures.length; i++ )
        {
            rectOffset = textures[i].fillBuffer( true, 0, 0, i, rectOffset, textureBuffer );
        }
        
        textureBuffer.position( textures.length * TransformableTexture.STRUCT_SIZE );
        textureBuffer.flip();
        
        return ( textures.length );
    }
    
    public void clearCompleteTexture()
    {
        for ( int i = 0; i < textures.length; i++ )
        {
            TextureImage2D texture = textures[i].getTexture();
            if ( textures[i].isDynamic() )
            {
                texture.getTextureCanvas().setClip( 0, 0, texture.getUsedWidth(), texture.getUsedHeight() );
                texture.clear( true, null );
            }
        }
        
        for ( int i = 0; i < getNumWidgets(); i++ )
        {
            getWidget( i ).forceCompleteRedraw();
            getWidget( i ).forceReinitialization();
        }
    }
    
    public void refreshSubTextureBuffer( boolean isEditorMode, LiveGameData gameData, boolean newConfig )
    {
        textureBuffer.position( 0 );
        textureBuffer.limit( textureBuffer.capacity() );
        
        textureBuffer.put( (byte)textures.length );
        
        final int n = getNumWidgets();
        int j = 0;
        for ( int i = 0; i < n; i++ )
        {
            if ( getWidget( i ).hasMasterCanvas( isEditorMode ) )
                textures[0].setRectangleVisible( j++, getWidget( i ).isVisible() );
        }
        
        int k = 0;
        
        int rectOffset = textures[0].fillBuffer( true, 0, 0, k++, 0, textureBuffer );
        int testRectOffset = textures[0].getNumUsedRectangles();
        
        for ( int i = 0; i < widgetSubTextures.length; i++ )
        {
            Widget widget = getWidget( i );
            TransformableTexture[] textures = widgetSubTextures[i];
            if ( textures != null )
            {
                for ( j = 0; j < textures.length; j++ )
                {
                    rectOffset = textures[j].fillBuffer( widget.isVisible(), widget.getPosition().getEffectiveX() + widget.getBorder().getInnerLeftWidth(), widget.getPosition().getEffectiveY() + widget.getBorder().getInnerTopHeight(), k++, rectOffset, textureBuffer );
                    testRectOffset += textures[j].getNumUsedRectangles();
                }
            }
        }
        
        textureBuffer.position( textures.length * TransformableTexture.STRUCT_SIZE );
        textureBuffer.flip();
        
        if ( newConfig && ( rectOffset < testRectOffset ) )
        {
            Logger.log( "WARNING: Number of displayed textures truncated. Possible reason: maxOpponents = " + gameData.getModInfo().getMaxOpponents() );
        }
    }
    
    public final int getNumTextures()
    {
        return ( textures.length );
    }
    
    public final ByteBuffer getSubTextureBuffer()
    {
        return ( textureBuffer );
    }
    
    public final TransformableTexture getTexture( int textureIndex )
    {
        return ( textures[textureIndex] );
    }
    
    /**
     * This method is called when a the user exited the garage.<br>
     * <br>
     * Calls {@link Widget#onPitsEntered(LiveGameData)} on each Widget.
     */
    public void fireOnPitsEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onPitsEntered( gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user entered the garage.<br>
     * <br>
     * Calls {@link Widget#onGarageEntered(LiveGameData)} on each Widget.
     */
    public void fireOnGarageEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onGarageEntered( gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user exited the garage.<br>
     * <br>
     * Calls {@link Widget#onGarageExited(LiveGameData)} on each Widget.
     */
    public void fireOnGarageExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onGarageExited( gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user exited the garage.<br>
     * <br>
     * Calls {@link Widget#onPitsExited(LiveGameData)} on each Widget.
     */
    public void fireOnPitsExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onPitsExited( gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a the user exited realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeExited(boolean, LiveGameData)} on each Widget.
     */
    public void fireOnRealtimeExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        waitingWidgets.clear();
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onRealtimeExited( gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when {@link ScoringInfo} have been updated (done at 2Hz).
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnScoringInfoUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onScoringInfoUpdated( gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when either the player's vehicle control has changed or another vehicle is being viewed.
     * 
     * @param viewedVSI
     * @param gameData
     * @param editorPresets
     */
    public void fireOnVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onVehicleControlChanged( viewedVSI, gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when a lap has been finished and a new one was started.
     * 
     * @param vsi
     * @param gameData
     * @param editorPresets
     */
    public void fireOnLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onLapStarted( vsi, gameData, editorPresets );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This event method is invoked when the engine boost mapping has changed.
     * 
     * @param oldValue
     * @param newValue
     */
    public void fireOnEngineBoostMappingChanged( int oldValue, int newValue )
    {
        int numWidgets = getNumWidgets();
        for ( int i = 0; i < numWidgets; i++ )
        {
            try
            {
                getWidget( i ).onEngineBoostMappingChanged( oldValue, newValue );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This event method is invoked when the temporary engine boost key or button state has changed.
     * 
     * @param enabled
     */
    public void fireOnTemporaryEngineBoostStateChanged( boolean enabled )
    {
        int numWidgets = getNumWidgets();
        for ( int i = 0; i < numWidgets; i++ )
        {
            try
            {
                getWidget( i ).onTemporaryEngineBoostStateChanged( enabled );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is fired by the {@link InputMappingsManager},
     * if the state of a bound input component has changed.
     * 
     * @param mapping
     * @param state
     * @param modifierMask
     * @param when
     * @param gameData
     * @param editorPresets
     */
    public void fireOnInputStateChanged( InputMapping mapping, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
    {
        Widget widget = getWidget( mapping.getWidgetName() );
        InputAction action = mapping.getAction();
        
        if ( widget == null )
            return;
        
        try
        {
            if ( action == KnownInputActions.ToggleWidgetVisibility )
                widget.setInputVisible( !widget.isInputVisible() );
            else
                widget.onBoundInputStateChanged( mapping.getAction(), state, modifierMask, when, gameData, editorPresets );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    private final boolean isWidgetReady( Widget widget, LiveGameData gameData )
    {
        boolean ready = true;
        
        if ( !waitingWidgets.isEmpty() )
        {
            int neededData = widget.getNeededData();
            
            if ( ( ( neededData & Widget.NEEDED_DATA_TELEMETRY ) != 0 ) && !gameData.getTelemetryData().isUpdatedInRealtimeMode() )
                ready = false;
            else if ( ( ( neededData & Widget.NEEDED_DATA_SCORING ) != 0 ) && !gameData.getScoringInfo().isUpdatedInRealtimeMode() )
                ready = false;
            else if ( ( ( neededData & Widget.NEEDED_DATA_SETUP ) != 0 ) && !gameData.getSetup().isUpdatedInRealtimeMode() )
                ready = false;
        }
        
        return ( ready );
    }
    
    /**
     * Draws all visible {@link Widget}s in the list.
     * 
     * @param gameData
     * @param editorPresets
     * @param completeRedrawForced
     * @param texture
     */
    public void drawWidgets( LiveGameData gameData, EditorPresets editorPresets, boolean completeRedrawForced, TextureImage2D texture )
    {
        final boolean isEditorMode = ( editorPresets != null );
        
        if ( !isValid() )
            return;
        
        texture.getTextureCanvas().setClip( 0, 0, texture.getUsedWidth(), texture.getUsedHeight() );
        
        checkFixAndBakeConfiguration( isEditorMode );
        
        long sessionNanos = gameData.getScoringInfo().getSessionNanos();
        
        if ( measureEnd == -1L )
        {
            measureStart = sessionNanos;
            measureEnd = sessionNanos + 1000000000L;
            measureFrameCounter = 0;
        }
        else if ( sessionNanos >= measureEnd )
        {
            long dt = sessionNanos - measureStart;
            clock1Frames = Math.max( 10L, measureFrameCounter * CLOCK_DELAY1 / dt );
            clock2Frames = Math.max( 30L, measureFrameCounter * CLOCK_DELAY2 / dt );
            //Logger.log( "Clock1: " + clock1Frames + " ( " + measureFrameCounter + ", " + CLOCK_DELAY1 + ", " + dt + " )" );
            //Logger.log( "Clock2: " + clock2Frames + " ( " + measureFrameCounter + ", " + CLOCK_DELAY2 + ", " + dt + " )" );
            
            measureStart = sessionNanos;
            measureEnd = sessionNanos + 1000000000L;
            measureFrameCounter = 0;
        }
        
        measureFrameCounter++;
        
        /*
        boolean clock1 = false;
        if ( sessionNanos >= nextClockTime1 )
        {
            //nextClockTime1 = Math.max( nextClockTime1 + CLOCK_DELAY1, sessionTime );
            nextClockTime1 = sessionNanos + CLOCK_DELAY1;
            clock1 = true;
        }
        
        boolean clock2 = false;
        if ( sessionNanos >= nextClockTime2 )
        {
            //nextClockTime2 = Math.max( nextClockTime2 + CLOCK_DELAY2, sessionTime );
            nextClockTime2 = sessionNanos + CLOCK_DELAY2;
            clock2 = true;
        }
        */
        
        boolean clock1 = ( frameCounter % clock1Frames ) == 0L;
        boolean clock2 = ( frameCounter % clock2Frames ) == 0L;
        //boolean clock1 = true;
        //boolean clock2 = true;
        //boolean clock1 = ( frameCounter % 10 ) == 0L;
        //boolean clock2 = ( frameCounter % 30 ) == 0L;
        
        if ( completeRedrawForced )
        {
            clock1 = true;
            clock2 = true;
        }
        
        frameCounter++;
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = getWidget( i );
            
            try
            {
                if ( isWidgetReady( widget, gameData ) )
                {
                    widget.updateVisibility( clock1, clock2, gameData, editorPresets );
                    
                    if ( isEditorMode )
                    {
                        if ( widget.getDirtyFlag( true ) )
                        {
                            widget.drawWidget( clock1, clock2, completeRedrawForced, gameData, editorPresets, texture );
                        }
                    }
                    else if ( !widget.isVisible() && widget.visibilityChangedSinceLastDraw() )
                    {
                        widget.clearRegion( isEditorMode, texture );
                    }
                }
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
        
        if ( !isEditorMode )
        {
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = getWidget( i );
                
                try
                {
                    if ( isWidgetReady( widget, gameData ) )
                    {
                        if ( widget.isVisible() )
                        {
                            widget.drawWidget( clock1, clock2, completeRedrawForced, gameData, editorPresets, texture );
                        }
                    }
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
    }
    
    /**
     * Creates a new {@link WidgetsDrawingManager}.
     * 
     * @param gameResX
     * @param gameResY
     */
    public WidgetsDrawingManager( int gameResX, int gameResY )
    {
        this.textures = new TransformableTexture[] { __RenderPrivilegedAccess.createMainTexture( gameResX, gameResY ) };
        
        __WCPrivilegedAccess.setGameResolution( gameResX, gameResY, this );
        __WCPrivilegedAccess.setViewport( 0, 0, gameResX, gameResY, this );
    }
}
