/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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

import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata._LiveGameDataObjectsFactory;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.input.InputDeviceManager;
import net.ctdp.rfdynhud.input.InputMappings;
import net.ctdp.rfdynhud.input.InputMappingsManager;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.__UtilHelper;
import net.ctdp.rfdynhud.util.__UtilPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.base.widget.WidgetFactory;

import org.jagatoo.util.versioning.Version;

/**
 * This is the entry point for the VM-invocation from rFactor.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class RFDynHUD
{
    static
    {
        ByteOrderInitializer.setByteOrder( 0, 1, 2, 3 );
        //ByteOrderInitializer.setByteOrder( 3, 2, 1, 0 );
        //ByteOrderInitializer.setByteOrder( 3, 0, 1, 2 );
        //ByteOrderInitializer.setByteOrder( 1, 2, 3, 0 );
    }
    
    public static final Version VERSION = new Version( 1, 4, 1, "Beta", 122 );
    
    private final String gameId;
    
    private final WidgetsDrawingManager drawingManager;
    private final LiveGameData gameData;
    private final GameEventsManager eventsManager;
    private final InputDeviceManager inputDeviceManager;
    private final InputMappingsManager inputMappingsManager;
    private InputMappings inputMappings = null;
    
    private boolean renderMode = false;
    
    public final String getGameId()
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
    
    public final InputMappings getInputMappings()
    {
        return ( inputMappings );
    }
    
    public void initInput( byte[] deviceData )
    {
        try
        {
            inputDeviceManager.decodeData( deviceData );
            
            inputMappings = inputMappingsManager.loadMappings( gameData.getFileSystem(), inputDeviceManager );
            
            __WCPrivilegedAccess.setInputMappings( drawingManager.getWidgetsConfiguration(), inputMappings );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
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
                RFDHLog.println( "Plugin disabled" );
                
                return ( 0 );
            }
            
            if ( pluginEnabled == +2 )
            {
                RFDHLog.println( "Plugin enabled" );
                
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
            RFDHLog.exception( t );
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
            RFDHLog.exception( t );
        }
        
        setRenderMode( result != 0 );
        
        return ( result );
    }
    
    private RFDynHUD( _LiveGameDataObjectsFactory gdFactory, int gameResX, int gameResY ) throws Throwable
    {
        //Logger.setStdStreams();
        
        try
        {
            gdFactory.init( false, __GDPrivilegedAccess.simulationMode );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        RFDHLog.println( "Creating RFDynHUD instance Version " + VERSION.toString() + "..." );
        
        RFDHLog.println( "    Detected game \"" + gdFactory.getGameId() + "\" (supported)." );
        
        this.gameId = gdFactory.getGameId();
        
        RFDHLog.print( "    Creating overlay texture interface for resolution " + gameResX + "x" + gameResY + "..." );
        
        this.drawingManager = new WidgetsDrawingManager( false, gameResX, gameResY );
        RFDHLog.println( " done." );
        
        this.eventsManager = gdFactory.newGameEventsManager( this, drawingManager );
        this.gameData = eventsManager.getGameData();
        
        __UtilHelper.configFolder = gameData.getFileSystem().getConfigFolder();
        __UtilHelper.bordersBolder = gameData.getFileSystem().getBordersFolder();
        __UtilHelper.imagesFolder = gameData.getFileSystem().getImagesFolder();
        __UtilHelper.editorPropertyDisplayNameGeneratorClass = gameData.getFileSystem().getPluginINI().getEditorPropertyDisplayNameGeneratorClass();
        __UtilPrivilegedAccess.updateLocalizationsManager( gameData.getFileSystem() );
        WidgetFactory.init( gameData.getFileSystem().getWidgetSetsFolder() );
        
        FontUtils.loadCustomFonts( gameData.getFileSystem() );
        
        this.inputDeviceManager = new InputDeviceManager();
        this.inputMappingsManager = new InputMappingsManager( this );
        
        RFDHLog.println( "Successfully created RFDynHUD instance." );
    }
    
    public static final RFDynHUD createInstance( _LiveGameDataObjectsFactory gdFactory, int gameResX, int gameResY )
    {
        try
        {
            return ( new RFDynHUD( gdFactory, gameResX, gameResY ) );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
            
            return ( null );
        }
    }
}
