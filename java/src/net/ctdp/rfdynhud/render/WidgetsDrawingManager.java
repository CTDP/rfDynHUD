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
import net.ctdp.rfdynhud.valuemanagers.Clock;
import net.ctdp.rfdynhud.valuemanagers.TimeBasedClock;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

/**
 * The {@link WidgetsDrawingManager} handles the drawing of all visible widgets.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetsDrawingManager extends WidgetsConfiguration
{
    private final boolean oneTextureForAllWidgets;
    
    private TransformableTexture[] textures;
    private TransformableTexture[] widgetTextures;
    private TransformableTexture[][] widgetSubTextures = null;
    private final ByteBuffer textureInfoBuffer = TransformableTexture.createByteBuffer();
    
    private long frameCounter = 0L;
    
    private final Clock clock = new TimeBasedClock( 50000000L ); // 50 ms
    
    public void resizeMainTexture( int gameResX, int gameResY )
    {
        if ( oneTextureForAllWidgets )
        {
            if ( ( textures[0].getWidth() != gameResX ) || ( textures[0].getHeight() != gameResY ) )
                textures[0] = TransformableTexture.createMainTexture( gameResX, gameResY, false );
        }
        
        __GDPrivilegedAccess.setGameResolution( gameResX, gameResY, this );
        __WCPrivilegedAccess.setViewport( 0, 0, gameResX, gameResY, this );
    }
    
    public final TextureImage2D getMainTexture( int widgetIndex )
    {
        if ( oneTextureForAllWidgets )
            return ( textures[0].getTexture() );
        
        TransformableTexture tt = widgetTextures[widgetIndex];
        
        if ( tt == null )
            return ( null );
        
        return ( tt.getTexture() );
    }
    
    /**
     * This method is executed when a new track was loaded.<br>
     * <br>
     * Calls {@link Widget#onTrackChanged(String, LiveGameData, boolean)} on each Widget.
     * 
     * @param trackname the track's name
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * 
     * @param sessionType the current session type
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnRealtimeEntered( LiveGameData gameData, boolean isEditorMode )
    {
        clock.init( gameData.getScoringInfo().getSessionNanos() );
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
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
        int numTextures = 0;
        
        final int numWidgets = getNumWidgets();
        
        if ( oneTextureForAllWidgets )
        {
            numTextures = 1;
            textures[0].generateRectanglesForOneBigTexture( gameData, isEditorMode, this );
        }
        else
        {
            numTextures = 0;
            textures = new TransformableTexture[ numWidgets ];
            widgetTextures = new TransformableTexture[ numWidgets ];
            
            for ( int i = 0; i < numWidgets; i++ )
            {
                Widget widget = getWidget( i );
                
                if ( widget.hasMasterCanvas( isEditorMode ) )
                {
                    textures[numTextures] = TransformableTexture.createMainTexture( widget.getMaxWidth( gameData, isEditorMode ), widget.getMaxHeight( gameData, isEditorMode ), true );
                    textures[numTextures].setTranslation( widget.getPosition().getEffectiveX(), widget.getPosition().getEffectiveY() );
                    
                    widgetTextures[i] = textures[numTextures];
                    numTextures++;
                }
                else
                {
                    widgetTextures[i] = null;
                }
            }
        }
        
        widgetSubTextures = new TransformableTexture[ numWidgets ][];
        
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
        
        textureInfoBuffer.position( 0 );
        textureInfoBuffer.limit( textureInfoBuffer.capacity() );
        
        textureInfoBuffer.put( (byte)textures.length );
        
        int rectOffset = 0;
        for ( int i = 0; i < textures.length; i++ )
        {
            rectOffset = textures[i].fillBuffer( true, 0, 0, i, rectOffset, textureInfoBuffer ); // offsets are irrelevant here
            textures[i].setDirty();
        }
        
        textureInfoBuffer.position( textures.length * TransformableTexture.STRUCT_SIZE );
        textureInfoBuffer.flip();
        
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
            // TODO: We possibly need to set this texture dirty!
        }
    }
    
    public void refreshTextureInfoBuffer( boolean isEditorMode, LiveGameData gameData, boolean newConfig )
    {
        textureInfoBuffer.position( 0 );
        textureInfoBuffer.limit( textureInfoBuffer.capacity() );
        
        textureInfoBuffer.put( (byte)textures.length );
        
        final int n = getNumWidgets();
        if ( oneTextureForAllWidgets )
        {
            int j = 0;
            for ( int i = 0; i < n; i++ )
            {
                Widget widget = getWidget( i );
                
                if ( widget.hasMasterCanvas( isEditorMode ) )
                    textures[0].setRectangleVisible( j++, widget.isVisible() );
            }
        }
        
        int k = 0;
        int rectOffset = 0;
        int testRectOffset = 0;
        
        if ( oneTextureForAllWidgets )
        {
            rectOffset = textures[0].fillBuffer( true, 0, 0, k++, rectOffset, textureInfoBuffer );
            testRectOffset += textures[0].getNumUsedRectangles();
        }
        else
        {
            for ( int i = 0; i < n; i++ )
            {
                if ( widgetTextures[i] != null )
                {
                    Widget widget = getWidget( i );
                    
                    rectOffset = widgetTextures[i].fillBuffer( widget.isVisible(), 0, 0, k++, rectOffset, textureInfoBuffer );
                    testRectOffset += widgetTextures[i].getNumUsedRectangles();
                }
            }
        }
        
        for ( int i = 0; i < widgetSubTextures.length; i++ )
        {
            Widget widget = getWidget( i );
            TransformableTexture[] subTextures = widgetSubTextures[i];
            if ( subTextures != null )
            {
                int offX = widget.getPosition().getEffectiveX() + widget.getBorder().getInnerLeftWidth();
                int offY = widget.getPosition().getEffectiveY() + widget.getBorder().getInnerTopHeight();
                
                for ( int j = 0; j < subTextures.length; j++ )
                {
                    rectOffset = subTextures[j].fillBuffer( widget.isVisible(), offX + subTextures[j].getOffsetXToRootMasterWidget(), offY + subTextures[j].getOffsetYToRootMasterWidget(), k++, rectOffset, textureInfoBuffer );
                    testRectOffset += subTextures[j].getNumUsedRectangles();
                }
            }
        }
        
        textureInfoBuffer.position( textures.length * TransformableTexture.STRUCT_SIZE );
        textureInfoBuffer.flip();
        
        if ( newConfig && ( rectOffset < testRectOffset ) )
        {
            Logger.log( "WARNING: Number of displayed textures truncated. Possible reason: maxOpponents = " + gameData.getModInfo().getMaxOpponents() );
        }
    }
    
    public final int getNumTextures()
    {
        return ( textures.length );
    }
    
    public final ByteBuffer getTextureInfoBuffer()
    {
        return ( textureInfoBuffer );
    }
    
    public final TransformableTexture getTexture( int textureIndex )
    {
        return ( textures[textureIndex] );
    }
    
    /**
     * This method is called when a the user exited the garage.<br>
     * <br>
     * Calls {@link Widget#onPitsEntered(LiveGameData, boolean)} on each Widget.
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * 
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * @param gameData the live game data
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
     * @param gameData the live game data
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
     * @param viewedVSI the viewed vehicle
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     */
    public void fireOnVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, boolean isEditorMode )
    {
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            try
            {
                __WPrivilegedAccess.onVehicleControlChanged( getWidget( i ), viewedVSI, gameData, isEditorMode );
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
     * @param vsi the vehicle
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
     * @param mapping the input mapping
     * @param state the current state of the input device component
     * @param modifierMask the current key modifier mask
     * @param when the timestamp of the input action in nano seconds
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
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
                __WPrivilegedAccess.toggleInputVisible( widget );
            else
                __WPrivilegedAccess.onBoundInputStateChanged( widget, action, state, modifierMask, when, gameData, isEditorMode );
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
     * @param gameData the live game data
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     * @param completeRedrawForced complete redraw forced?
     */
    public void drawWidgets( LiveGameData gameData, boolean isEditorMode, boolean completeRedrawForced )
    {
        if ( !isValid() )
            return;
        
        checkFixAndBakeConfiguration( isEditorMode );
        
        long sessionNanos = gameData.getScoringInfo().getSessionNanos();
        
        clock.update( sessionNanos, frameCounter, completeRedrawForced );
        
        frameCounter++;
        
        final int n = getNumWidgets();
        for ( int i = 0; i < n; i++ )
        {
            Widget widget = getWidget( i );
            TextureImage2D texture = getMainTexture( i );
            
            if ( texture != null )
                texture.getTextureCanvas().setClip( 0, 0, texture.getWidth(), texture.getHeight() );
            
            try
            {
                if ( isWidgetReady( widget, gameData ) )
                {
                    __WPrivilegedAccess.updateVisibility( widget, gameData, isEditorMode );
                    
                    if ( isEditorMode )
                    {
                        if ( widget.getDirtyFlag( true ) )
                        {
                            widget.drawWidget( clock, completeRedrawForced, gameData, isEditorMode, texture, !oneTextureForAllWidgets );
                        }
                    }
                    else if ( !widget.isVisible() && widget.visibilityChangedSinceLastDraw() )
                    {
                        int offsetX = oneTextureForAllWidgets ? widget.getPosition().getEffectiveX() : 0;
                        int offsetY = oneTextureForAllWidgets ? widget.getPosition().getEffectiveY() : 0;
                        
                        widget.clearRegion( texture, offsetX, offsetY );
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
                TextureImage2D texture = getMainTexture( i );
                
                if ( texture != null )
                    texture.getTextureCanvas().setClip( 0, 0, texture.getWidth(), texture.getHeight() );
                
                try
                {
                    if ( isWidgetReady( widget, gameData ) )
                    {
                        if ( widget.isVisible() )
                        {
                            widget.drawWidget( clock, completeRedrawForced, gameData, isEditorMode, texture, !oneTextureForAllWidgets );
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
     * @param isEditorMode <code>true</code>, if the Editor is used for rendering instead of rFactor
     * @param gameResX the game x-resolution (current viewport)
     * @param gameResY the game y-resolution (current viewport)
     * @param createTexture create a texture?
     */
    public WidgetsDrawingManager( boolean isEditorMode, int gameResX, int gameResY, boolean createTexture )
    {
        this.oneTextureForAllWidgets = isEditorMode;
        
        if ( createTexture )
        {
            if ( oneTextureForAllWidgets )
                this.textures = new TransformableTexture[] { TransformableTexture.createMainTexture( gameResX, gameResY, false ) };
            else
                this.textures = new TransformableTexture[] {};
        }
        else
        {
            this.textures = null;
        }
        
        __GDPrivilegedAccess.setGameResolution( gameResX, gameResY, this );
        __WCPrivilegedAccess.setViewport( 0, 0, gameResX, gameResY, this );
    }
}
