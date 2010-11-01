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
package net.ctdp.rfdynhud;

import java.nio.ByteBuffer;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.SupportedGames;
import net.ctdp.rfdynhud.gamedata._LiveGameData_CPP_Adapter;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.gamedata.__GameIDHelper;
import net.ctdp.rfdynhud.input.InputDeviceManager;
import net.ctdp.rfdynhud.input.InputMappings;
import net.ctdp.rfdynhud.input.InputMappingsManager;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

import org.jagatoo.util.versioning.Version;

/**
 * This is the entry point for the VM-invocation from rFactor.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RFDynHUD
{
    public static final Version VERSION = new Version( 1, 2, 0, "Beta", 91 );
    
    private final SupportedGames gameId;
    
    private final WidgetsDrawingManager drawingManager;
    private final LiveGameData gameData;
    private final _LiveGameData_CPP_Adapter gameData_CPP_Adapter;
    private final GameEventsManager eventsManager;
    private final InputDeviceManager inputDeviceManager;
    private final InputMappingsManager inputMappingsManager;
    
    private boolean renderMode = false;
    
    public final SupportedGames getGameId()
    {
        return ( gameId );
    }
    
    public void setRenderMode( boolean renderMode )
    {
        this.renderMode = renderMode;
    }
    
    public final boolean isInRenderMode()
    {
        return ( renderMode );
    }
    
    public final ByteBuffer getTextureInfoBuffer()
    {
        return ( drawingManager.getTextureInfoBuffer() );
    }
    
    public final ByteBuffer getDirtyRectsBuffer( int textureIndex )
    {
        return ( drawingManager.getTexture( textureIndex ).getDirtyRectsBuffer() );
    }
    
    public final byte[] getTextureData( int textureIndex )
    {
        return ( drawingManager.getTexture( textureIndex ).getTextureData() );
    }
    
    public final LiveGameData getGameData()
    {
        return ( gameData );
    }
    
    public final _LiveGameData_CPP_Adapter getGameData_CPP_Adapter()
    {
        return ( gameData_CPP_Adapter );
    }
    
    public final GameEventsManager getEventsManager()
    {
        return ( eventsManager );
    }
    
    public final InputDeviceManager getInputDeviceManager()
    {
        return ( inputDeviceManager );
    }
    
    public final InputMappingsManager getInputMappingsManager()
    {
        return ( inputMappingsManager );
    }
    
    public void initInput( byte[] deviceData )
    {
        try
        {
            inputDeviceManager.decodeData( deviceData );
            
            InputMappings mappings = inputMappingsManager.loadMappings( inputDeviceManager );
            
            __WCPrivilegedAccess.setInputMappings( drawingManager.getWidgetsConfiguration(), mappings );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    public final ByteBuffer getInputBuffer()
    {
        return ( inputMappingsManager.getBuffer() );
    }
    
    public byte updateInput( int modifierMask )
    {
        try
        {
            int pluginEnabled = inputMappingsManager.update( eventsManager, drawingManager, gameData, false, modifierMask );
            
            if ( pluginEnabled == -1 )
            {
                Logger.log( "Plugin disabled" );
                
                return ( 0 );
            }
            
            if ( pluginEnabled == +2 )
            {
                Logger.log( "Plugin enabled" );
                
                byte result = eventsManager.reloadConfigAndSetupTexture( false );
                if ( result != 0 )
                {
                    int numWidgets = drawingManager.getWidgetsConfiguration().getNumWidgets();
                    for ( int i = 0; i < numWidgets; i++ )
                    {
                        drawingManager.getWidgetsConfiguration().getWidget( i ).forceCompleteRedraw( true );
                    }
                }
                
                return ( result );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( inputMappingsManager.isPluginEnabled() && drawingManager.getWidgetsConfiguration().isValid() )
            return ( 1 );
        
        return ( 0 );
    }
    
    private int lastConfigId = -1;
    
    /**
     * Will and must be called any time, the game is redendered (called from the C++-Plugin).
     * 
     * @return 0, if nothing should be rendered anymore, 1 to render something, 2 to render and update texture info.
     */
    public final byte update()
    {
        byte result = 1;
        
        try
        {
            boolean newConfig = ( drawingManager.getWidgetsConfiguration().getId() != lastConfigId );
            lastConfigId = drawingManager.getWidgetsConfiguration().getId();
            
            __GDPrivilegedAccess.updateSessionTime( gameData, false, System.nanoTime() );
            
            drawingManager.refreshTextureInfoBuffer( false, gameData, newConfig );
            
            drawingManager.drawWidgets( gameData, false, eventsManager.hasWaitingWidgets(), newConfig );
            //TextureDirtyRectsManager.drawDirtyRects( overlay );
            
            int n = drawingManager.getNumTextures();
            for ( int i = 0; i < n; i++ )
            {
                TextureDirtyRectsManager.getDirtyRects( drawingManager.getTexture( i ).getTexture(), drawingManager.getTexture( i ).getDirtyRectsBuffer(), true );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        setRenderMode( result != 0 );
        
        return ( result );
    }
    
    private static String extractGameName( byte[] buffer )
    {
        return ( new String( buffer ) );
    }
    
    public RFDynHUD( String gameName, int gameResX, int gameResY ) throws Throwable
    {
        //Logger.setStdStreams();
        
        Logger.log( "Creating RFDynHUD instance Version " + VERSION.toString() + "..." );
        
        SupportedGames gameId = null;
        try
        {
            gameId = SupportedGames.valueOf( gameName );
        }
        catch ( Throwable t )
        {
        }
        
        Logger.log( "    Detected game \"" + gameName + "\" (" + ( gameId == null ? "unsupported" : "supported" ) + ")." );
        
        if ( gameId == null )
            throw new Error( "Unsupported game" );
        
        this.gameId = gameId;
        __GameIDHelper.gameId = gameId;
        
        ByteOrderInitializer.setByteOrder( 0, 1, 2, 3 );
        //ByteOrderInitializer.setByteOrder( 3, 2, 1, 0 );
        //ByteOrderInitializer.setByteOrder( 3, 0, 1, 2 );
        //ByteOrderInitializer.setByteOrder( 1, 2, 3, 0 );
        
        Logger.log( "    Creating overlay texture interface for resolution " + gameResX + "x" + gameResY + "...", false );
        
        this.drawingManager = new WidgetsDrawingManager( false, gameResX, gameResY );
        Logger.log( " done." );
        
        this.eventsManager = new GameEventsManager( this, drawingManager );
        
        this.gameData = new LiveGameData( gameId, drawingManager.getWidgetsConfiguration().getGameResolution(), eventsManager );
        eventsManager.setGameData( this.gameData, drawingManager.getRenderListenersManager() );
        this.gameData_CPP_Adapter = new _LiveGameData_CPP_Adapter( gameData );
        
        this.inputDeviceManager = new InputDeviceManager();
        this.inputMappingsManager = new InputMappingsManager( this );
        
        Logger.log( "Successfully created RFDynHUD instance." );
    }
    
    private RFDynHUD( byte[] gameNameBuffer, int gameResX, int gameResY ) throws Throwable
    {
        this( extractGameName( gameNameBuffer ), gameResX, gameResY );
    }
    
    public static final RFDynHUD createInstance( byte[] gameNameBuffer, int gameResX, int gameResY )
    {
        try
        {
            return ( new RFDynHUD( gameNameBuffer, gameResX, gameResY ) );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( null );
        }
    }
}
