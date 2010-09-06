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
package net.ctdp.rfdynhud.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
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
 * @author Marvin Froehlich (CTDP)
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
    
    public void resizeMainTexture( int gameResX, int gameResY )
    {
        this.textures[0] = __RenderPrivilegedAccess.createMainTexture( gameResX, gameResY );
        
        __GDPrivilegedAccess.setGameResolution( gameResX, gameResY, this );
        __WCPrivilegedAccess.setViewport( 0, 0, gameResX, gameResY, this );
    }
    
    public final TextureImage2D getMainTexture()
    {
        return ( textures[0].getTexture() );
    }
    
    /**
     * This method is executed when a new track was loaded.<br>
     * <br>
     * Calls {@link Widget#onTrackChanged(String, LiveGameData, boolean)} on each Widget.
     */
    public void fireOnTrackChanged( String trackname, LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onTrackChanged( trackname, gameData, isEditorMode );
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
     * Calls {@link Widget#onSessionStarted(SessionType, LiveGameData, boolean)} on each Widget.
     */
    public void fireOnSessionStarted( SessionType sessionType, LiveGameData gameData, boolean isEditorMode )
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
                widget.onSessionStarted( sessionType, gameData, isEditorMode );
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
     * Calls {@link Widget#onRealtimeEntered(LiveGameData, boolean)} on each Widget.
     */
    public void fireOnRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
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
                widget.onRealtimeEntered( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
            
            waitingWidgets.add( widget );
        }
    }
    
    public void setAllWidgetsDirty()
    {
        int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            getWidget( i ).forceAndSetDirty( true );
        }
    }
    
    /**
     * This method is called when a the user entered realtime mode.<br>
     * <br>
     * Calls {@link Widget#onRealtimeEntered(LiveGameData, boolean)} on each Widget.
     */
    public void checkAndFireOnNeededDataComplete( LiveGameData gameData, boolean isEditorMode )
    {
        if ( waitingWidgets.size() == 0 )
            return;
        
        for ( int i = waitingWidgets.size() - 1; i >= 0; i-- )
        {
            Widget widget = waitingWidgets.get( i );
            int neededData = ( widget.getNeededData() & Widget.NEEDED_DATA_ALL );
            
            if ( ( ( neededData & Widget.NEEDED_DATA_TELEMETRY ) != 0 ) && gameData.getTelemetryData().isUpdatedInTimeScope() )
                neededData &= ~Widget.NEEDED_DATA_TELEMETRY;
            
            if ( ( ( neededData & Widget.NEEDED_DATA_SCORING ) != 0 ) && !gameData.getScoringInfo().isUpdatedInTimeScope() )
                neededData &= ~Widget.NEEDED_DATA_SCORING;
            
            //if ( ( ( neededData & Widget.NEEDED_DATA_SETUP ) != 0 ) && !gameData.getSetup().isUpdatedInTimeScope() )
            //    neededData &= ~Widget.NEEDED_DATA_SETUP;
            
            if ( neededData == 0 )
            {
                try
                {
                    widget.onNeededDataComplete( gameData, isEditorMode );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
                
                waitingWidgets.remove( i );
                
                widget.forceReinitialization();
                widget.forceCompleteRedraw( true );
            }
        }
    }
    
    public int collectTextures( LiveGameData gameData, boolean isEditorMode )
    {
        textures[0].generateSubRectangles( gameData, isEditorMode, this );
        
        widgetSubTextures = new TransformableTexture[ getNumWidgets() ][];
        
        final int numWidgets = getNumWidgets();
        int numTextures = 1;
        for ( int i = 0; i < numWidgets; i++ )
        {
            Widget widget = getWidget( i );
            TransformableTexture[] subTextures = widget.getSubTextures( gameData, isEditorMode, widget.getSize().getEffectiveWidth() - widget.getBorder().getInnerLeftWidth() - widget.getBorder().getInnerRightWidth(), widget.getSize().getEffectiveHeight() - widget.getBorder().getInnerTopHeight() - widget.getBorder().getInnerBottomHeight() );
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
            rectOffset = textures[i].fillBuffer( true, 0 + textures[i].getOffsetXToMasterWidget(), 0 + textures[i].getOffsetYToMasterWidget(), i, rectOffset, textureBuffer );
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
                texture.getTextureCanvas().setClip( 0, 0, texture.getWidth(), texture.getHeight() );
                texture.clear( true, null );
            }
        }
        
        for ( int i = 0; i < getNumWidgets(); i++ )
        {
            getWidget( i ).forceCompleteRedraw( true );
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
                    rectOffset = textures[j].fillBuffer( widget.isVisible(), widget.getPosition().getEffectiveX() + widget.getBorder().getInnerLeftWidth() + textures[j].getOffsetXToMasterWidget(), widget.getPosition().getEffectiveY() + widget.getBorder().getInnerTopHeight() + textures[j].getOffsetYToMasterWidget(), k++, rectOffset, textureBuffer );
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
     * Calls {@link Widget#onPitsEntered(LiveGameData, boolean)} on each Widget.
     */
    public void fireOnPitsEntered( LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onPitsEntered( gameData, isEditorMode );
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
     * Calls {@link Widget#onGarageEntered(LiveGameData, boolean)} on each Widget.
     */
    public void fireOnGarageEntered( LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onGarageEntered( gameData, isEditorMode );
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
     * Calls {@link Widget#onGarageExited(LiveGameData, boolean)} on each Widget.
     */
    public void fireOnGarageExited( LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onGarageExited( gameData, isEditorMode );
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
     * Calls {@link Widget#onPitsExited(LiveGameData, boolean)} on each Widget.
     */
    public void fireOnPitsExited( LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onPitsExited( gameData, isEditorMode );
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
     * Calls {@link Widget#onRealtimeExited(LiveGameData, boolean)} on each Widget.
     */
    public void fireOnRealtimeExited( LiveGameData gameData, boolean isEditorMode )
    {
        waitingWidgets.clear();
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onRealtimeExited( gameData, isEditorMode );
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
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onScoringInfoUpdated( gameData, isEditorMode );
            }
            catch ( Throwable t )
            {
                Logger.log( t );
            }
        }
    }
    
    /**
     * This method is called when {@link VehicleSetup} has been updated.
     * 
     * @param gameData
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnVehicleSetupUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = getWidget( i );
            
            try
            {
                widget.forceAndSetDirty( true );
                widget.onVehicleSetupUpdated( gameData, isEditorMode );
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
     * @param isEditorMode
     */
    public void fireOnVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onVehicleControlChanged( viewedVSI, gameData, isEditorMode );
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
     * @param isEditorMode
     */
    public void fireOnLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                getWidget( i ).onLapStarted( vsi, gameData, isEditorMode );
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
     * @param isEditorMode
     */
    public void fireOnInputStateChanged( InputMapping mapping, boolean state, int modifierMask, long when, LiveGameData gameData, boolean isEditorMode )
    {
        Widget widget = getWidget( mapping.getWidgetName() );
        
        if ( widget == null )
            return;
        
        InputAction action = mapping.getAction();
        
        try
        {
            if ( action == KnownInputActions.ToggleWidgetVisibility )
                widget.setInputVisible( !widget.isInputVisible() );
            else
                widget.onBoundInputStateChanged( mapping.getAction(), state, modifierMask, when, gameData, isEditorMode );
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
            
            if ( ( ( neededData & Widget.NEEDED_DATA_TELEMETRY ) != 0 ) && !gameData.getTelemetryData().isUpdatedInTimeScope() )
                ready = false;
            else if ( ( ( neededData & Widget.NEEDED_DATA_SCORING ) != 0 ) && !gameData.getScoringInfo().isUpdatedInTimeScope() )
                ready = false;
            //else if ( ( ( neededData & Widget.NEEDED_DATA_SETUP ) != 0 ) && !gameData.getSetup().isUpdatedInTimeScope() )
            //    ready = false;
        }
        
        return ( ready );
    }
    
    /**
     * Draws all visible {@link Widget}s in the list.
     * 
     * @param gameData
     * @param isEditorMode
     * @param completeRedrawForced
     * @param texture
     */
    public void drawWidgets( LiveGameData gameData, boolean isEditorMode, boolean completeRedrawForced, TextureImage2D texture )
    {
        if ( !isValid() )
            return;
        
        texture.getTextureCanvas().setClip( 0, 0, texture.getWidth(), texture.getHeight() );
        
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
                    widget.updateVisibility( clock1, clock2, gameData, isEditorMode );
                    
                    if ( isEditorMode )
                    {
                        if ( widget.getDirtyFlag( true ) )
                        {
                            widget.drawWidget( clock1, clock2, completeRedrawForced, gameData, isEditorMode, texture );
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
                            widget.drawWidget( clock1, clock2, completeRedrawForced, gameData, isEditorMode, texture );
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
        
        __GDPrivilegedAccess.setGameResolution( gameResX, gameResY, this );
        __WCPrivilegedAccess.setViewport( 0, 0, gameResX, gameResY, this );
    }
}
