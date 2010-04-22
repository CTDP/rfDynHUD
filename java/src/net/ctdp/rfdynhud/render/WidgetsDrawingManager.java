package net.ctdp.rfdynhud.render;

import java.nio.ByteBuffer;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.input.InputMapping;
import net.ctdp.rfdynhud.input.InputMappingsManager;
import net.ctdp.rfdynhud.input.KnownInputActions;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link WidgetsDrawingManager} handles the drawing of all visible widgets.
 * 
 * @author Marvin Froehlich
 */
public class WidgetsDrawingManager extends WidgetsConfiguration
{
    private TransformableTexture[] textures;
    private TransformableTexture[][] widgetTextures = null;
    private final ByteBuffer textureBuffer = TransformableTexture.createByteBuffer();
    
    private long frameCounter = 0;
    
    private long measureStart = -1L;
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
            getWidget( i ).onTrackChanged( trackname, gameData, editorPresets );
        }
    }
    
    /**
     * This method is called when a new session was started.<br>
     * <br>
     * Calls {@link Widget#onSessionStarted(boolean, SessionType, LiveGameData)} on each Widget.
     */
    public void fireOnSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        //nextClockTime1 = 0L;
        //nextClockTime2 = 0L;
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            getWidget( i ).onSessionStarted( sessionType, gameData, editorPresets );
        }
    }
    
    /**
     * This method is called when a the user entered realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeEntered(boolean, LiveGameData)} on each Widget.
     */
    public void fireOnRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        measureStart = -1L;
        frameCounter = 0;
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            getWidget( i ).onRealtimeEntered( gameData, editorPresets );
        }
    }
    
    public void clearCompleteTexture()
    {
        TextureImage2D texture = textures[0].getTexture();
        texture.getTextureCanvas().setClip( 0, 0, texture.getUsedWidth(), texture.getUsedHeight() );
        texture.clear( true, null );
    }
    
    public int collectTextures( LiveGameData gameData, EditorPresets editorPresets )
    {
        textures[0].generateSubRectangles( gameData, this );
        
        widgetTextures = new TransformableTexture[ getNumWidgets() ][];
        
        final int numWidgets = getNumWidgets();
        int numTextures = 1;
        for ( int i = 0; i < numWidgets; i++ )
        {
            Widget widget = getWidget( i );
            TransformableTexture[] subTextures = widget.getSubTextures( editorPresets != null, widget.getSize().getEffectiveWidth() - widget.getBorder().getInnerLeftWidth() - widget.getBorder().getInnerRightWidth(), widget.getSize().getEffectiveHeight() - widget.getBorder().getInnerTopHeight() - widget.getBorder().getInnerBottomHeight() );
            if ( ( subTextures != null ) && ( subTextures.length > 0 ) )
            {
                widgetTextures[i] = subTextures;
                
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
                widgetTextures[i] = null;
            }
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
    
    public void refreshSubTextureBuffer()
    {
        textureBuffer.position( 0 );
        textureBuffer.limit( textureBuffer.capacity() );
        
        textureBuffer.put( (byte)textures.length );
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
            textures[0].setRectangleVisible( i, getWidget( i ).isVisible() );
        
        int k = 0;
        
        int rectOffset = textures[0].fillBuffer( true, 0, 0, k++, 0, textureBuffer );
        
        for ( int i = 0; i < widgetTextures.length; i++ )
        {
            Widget widget = getWidget( i );
            TransformableTexture[] textures = widgetTextures[i];
            if ( textures != null )
            {
                for ( int j = 0; j < textures.length; j++ )
                {
                    rectOffset = textures[j].fillBuffer( widget.isVisible(), widget.getPosition().getEffectiveX() + widget.getBorder().getInnerLeftWidth(), widget.getPosition().getEffectiveY() + widget.getBorder().getInnerTopHeight(), k++, rectOffset, textureBuffer );
                }
            }
        }
        
        textureBuffer.position( textures.length * TransformableTexture.STRUCT_SIZE );
        textureBuffer.flip();
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
            getWidget( i ).onPitsEntered( gameData, editorPresets );
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
            getWidget( i ).onGarageEntered( gameData, editorPresets );
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
            getWidget( i ).onGarageExited( gameData, editorPresets );
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
            getWidget( i ).onPitsExited( gameData, editorPresets );
        }
    }
    
    /**
     * This method is called when a the user exited realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeExited(boolean, LiveGameData)} on each Widget.
     */
    public void fireOnRealtimeExited( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            getWidget( i ).onRealtimeExited( gameData, editorPresets );
        }
    }
    
    /**
     * This method is called when a lap has been finished and new new one was started.
     * 
     * @param gameData
     * @param editorPresets
     */
    public void fireOnLapStarted( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            getWidget( i ).onLapStarted( gameData, editorPresets );
        }
    }
    /**
     * This method is called when the driver has finished a lap and started a new one.
     * 
     * @param gameData
     * @param editorPresets
     */
    public void fireOnPlayerLapStarted( LiveGameData gameData, EditorPresets editorPresets )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            getWidget( i ).onPlayerLapStarted( gameData, editorPresets );
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
            getWidget( i ).onEngineBoostMappingChanged( oldValue, newValue );
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
            getWidget( i ).onTemporaryEngineBoostStateChanged( enabled );
        }
    }
    
    /**
     * This method is fired by the {@link InputMappingsManager},
     * if the state of a bound input component has changed.
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param mapping
     * @param state
     */
    public void fireOnInputStateChanged( boolean isEditorMode, InputMapping mapping, boolean state, int modifierMask )
    {
        Widget widget = getWidget( mapping.getWidgetName() );
        InputAction action = mapping.getAction();
        
        if ( widget == null )
            return;
        
        if ( action == KnownInputActions.ToggleWidgetVisibility )
            widget.setVisible( !widget.isVisible() );
        else
            widget.onBoundInputStateChanged( isEditorMode, mapping.getAction(), state, modifierMask );
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
        
        texture.getTextureCanvas().setClip( 0, 0, texture.getUsedWidth(), texture.getUsedHeight() );
        
        Widget widget;
        
        checkFixAndBakeConfiguration( isEditorMode );
        
        if ( !isEditorMode )
            __GDPrivilegedAccess.updateSessionTime( gameData.getScoringInfo(), System.nanoTime() );
        
        long sessionNanos = gameData.getScoringInfo().getSessionNanos();
        
        if ( measureStart == -1L )
        {
            measureStart = sessionNanos;
            measureFrameCounter = 0;
        }
        else if ( measureStart + 1000000000L < sessionNanos )
        {
            long dt = sessionNanos - measureStart;
            clock1Frames = Math.max( 10L, measureFrameCounter * CLOCK_DELAY1 / dt );
            clock2Frames = Math.max( 30L, measureFrameCounter * CLOCK_DELAY2 / dt );
            //Logger.log( "Clock1: " + clock1Frames + " ( " + measureFrameCounter + ", " + CLOCK_DELAY1 + ", " + dt + " )" );
            //Logger.log( "Clock2: " + clock2Frames + " ( " + measureFrameCounter + ", " + CLOCK_DELAY2 + ", " + dt + " )" );
            
            measureStart = sessionNanos;
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
            widget = getWidget( i );
            
            try
            {
                widget.updateVisibility( gameData, editorPresets );
                
                if ( !isEditorMode || widget.getDirtyFlag( true ) )
                {
                    if ( widget.isVisible() || isEditorMode )
                    {
                        widget.drawWidget( clock1, clock2, completeRedrawForced, gameData, editorPresets, texture );
                    }
                    else if ( widget.needsCompleteClear() )
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
    }
    
    /**
     * Creates a new {@link WidgetsDrawingManager}.
     * 
     * @param overlayTT
     */
    public WidgetsDrawingManager( TransformableTexture overlayTT )
    {
        this.textures = new TransformableTexture[] { overlayTT };
    }
}
