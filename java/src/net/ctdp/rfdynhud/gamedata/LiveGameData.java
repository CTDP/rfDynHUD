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
package net.ctdp.rfdynhud.gamedata;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.util.Logger;

/**
 * 
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class LiveGameData
{
    private final GameResolution gameResolution;
    
    private boolean gamePaused = false;
    private boolean realtimeMode = false;
    
    private final VehiclePhysics physics = new VehiclePhysics();
    private final VehicleSetup setup = new VehicleSetup();
    
    private final TelemetryData telemetryData;
    private final ScoringInfo scoringInfo;
    private final GraphicsInfo graphicsInfo;
    private final CommentaryRequestInfo commentaryInfo;
    
    private final ProfileInfo profileInfo;
    private final ModInfo modInfo;
    private final TrackInfo trackInfo;
    
    public static interface GameDataUpdateListener
    {
        public void onSessionStarted( LiveGameData gameData, EditorPresets editorPresets );
        public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets );
        public void onGamePauseStateChanged( LiveGameData gameData, EditorPresets editorPresets, boolean isPaused );
        public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets );
    }
    
    private GameDataUpdateListener[] updateListeners = null;
    
    public void registerListener( GameDataUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new GameDataUpdateListener[] { l };
        }
        else
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] == l )
                    return;
            }
            
            GameDataUpdateListener[] tmp = new GameDataUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
    }
    
    public void unregisterListener( GameDataUpdateListener l )
    {
        if ( updateListeners == null )
            return;
        
        int index = -1;
        for ( int i = 0; i < updateListeners.length; i++ )
        {
            if ( updateListeners[i] == l )
            {
                index = i;
                break;
            }
        }
        
        if ( index < 0 )
            return;
        
        if ( updateListeners.length == 1 )
        {
            updateListeners = null;
            return;
        }
        
        GameDataUpdateListener[] tmp = new GameDataUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
    }
    
    public final GameResolution getGameResolution()
    {
        return ( gameResolution );
    }
    
    void setGamePaused( boolean paused, EditorPresets editorPresets )
    {
        if ( paused == this.gamePaused )
            return;
        
        this.gamePaused = paused;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                try
                {
                    updateListeners[i].onGamePauseStateChanged( this, editorPresets, paused );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
    }
    
    /**
     * Gets whether the game is paused. Since rFactor1 doesn't tell its plugins about the paused state,
     * this can only be a guess based on the last TelemetryData update. So this info can be up to some splitss of a second late.
     * 
     * @return whether the game is paused.
     */
    public final boolean isGamePaused()
    {
        return ( gamePaused );
    }
    
    void onSessionStarted2( EditorPresets editorPresets )
    {
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                try
                {
                    updateListeners[i].onSessionStarted( this, editorPresets );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
    }
    
    void setRealtimeMode( boolean realtimeMode, EditorPresets editorPresets )
    {
        boolean was = this.realtimeMode;
        
        this.realtimeMode = realtimeMode;
        
        if ( !was && realtimeMode )
        {
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onRealtimeEntered( this, editorPresets );
                    }
                    catch ( Throwable t )
                    {
                        Logger.log( t );
                    }
                }
            }
            
            getTelemetryData().onRealtimeEntered();
            getScoringInfo().onRealtimeEntered();
            getSetup().onRealtimeEntered();
        }
        else if ( was && !realtimeMode )
        {
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onRealtimeExited( this, editorPresets );
                    }
                    catch ( Throwable t )
                    {
                        Logger.log( t );
                    }
                }
            }
            
            getTelemetryData().onRealtimeExited();
            getScoringInfo().onRealtimeExited();
            getSetup().onRealtimeExited();
        }
    }
    
    public final boolean isInRealtimeMode()
    {
        return ( realtimeMode );
    }
    
    public final VehiclePhysics getPhysics()
    {
        return ( physics );
    }
    
    public final VehicleSetup getSetup()
    {
        return ( setup );
    }
    
    public final TelemetryData getTelemetryData()
    {
        return ( telemetryData );
    }
    
    public final ScoringInfo getScoringInfo()
    {
        return ( scoringInfo );
    }
    
    public final GraphicsInfo getGraphicsInfo()
    {
        return ( graphicsInfo );
    }
    
    public final CommentaryRequestInfo getCommentaryRequestInfo()
    {
        return ( commentaryInfo );
    }
    
    public final ModInfo getModInfo()
    {
        return ( modInfo );
    }
    
    public final ProfileInfo getProfileInfo()
    {
        return ( profileInfo );
    }
    
    public final TrackInfo getTrackInfo()
    {
        return ( trackInfo );
    }
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        telemetryData.applyEditorPresets( editorPresets );
        scoringInfo.applyEditorPresets( editorPresets );
        setup.applyEditorPresets( editorPresets );
    }
    
    public LiveGameData( GameResolution gameResolution, GameEventsManager eventsManager )
    {
        registerListener( DataCache.INSTANCE );
        
        this.gameResolution = gameResolution;
        this.telemetryData = new TelemetryData( this, eventsManager );
        this.scoringInfo = new ScoringInfo( this, eventsManager );
        this.graphicsInfo = new GraphicsInfo( this );
        this.commentaryInfo = new CommentaryRequestInfo( this );
        
        this.profileInfo = new ProfileInfo();
        this.modInfo = new ModInfo( profileInfo );
        this.trackInfo = new TrackInfo( profileInfo );
        
        VehicleSetupParser.loadDefaultSetup( physics, setup );
        
        eventsManager.setGameData( this );
    }
}
