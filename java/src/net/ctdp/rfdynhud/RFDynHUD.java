package net.ctdp.rfdynhud;

import java.nio.ByteBuffer;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.LiveGameData_CPP_Adapter;
import net.ctdp.rfdynhud.input.InputDeviceManager;
import net.ctdp.rfdynhud.input.InputMappings;
import net.ctdp.rfdynhud.input.InputMappingsManager;
import net.ctdp.rfdynhud.render.ByteOrderInitializer;
import net.ctdp.rfdynhud.render.TextureDirtyRectsManager;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.RFactorEventsManager;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

import org.jagatoo.util.versioning.Version;

/**
 * This is the entry point for the VM-invocation from rFactor.
 * 
 * @author Marvin Froehlich
 */
public class RFDynHUD
{
    public static final Version VERSION = new Version( 1, 0, 1, null, 69 );
    
    public static final int FLAG_CONFIGURATION_RELOADED = 0;
    
    private final WidgetsDrawingManager drawingManager;
    private final LiveGameData gameData;
    private final LiveGameData_CPP_Adapter gameData_CPP_Adapter;
    private final RFactorEventsManager eventsManager;
    private final InputDeviceManager inputDeviceManager;
    private final InputMappingsManager inputMappingsManager;
    
    private final ByteBuffer flagsBuffer = ByteBuffer.allocateDirect( 16 );
    
    public final ByteBuffer getFlagsBuffer()
    {
        return ( flagsBuffer );
    }
    
    public void setFlag( int flag, boolean value )
    {
        flagsBuffer.position( flag );
        flagsBuffer.put( value ? (byte)1 : (byte)0 );
        flagsBuffer.position( 0 );
    }
    
    public final ByteBuffer getTextureInfoBuffer()
    {
        return ( drawingManager.getSubTextureBuffer() );
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
    
    public final LiveGameData_CPP_Adapter getGameData_CPP_Adapter()
    {
        return ( gameData_CPP_Adapter );
    }
    
    public final RFactorEventsManager getEventsManager()
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
            
            __WCPrivilegedAccess.setInputMappings( drawingManager, mappings );
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
    
    public boolean updateInput( int modifierMask )
    {
        try
        {
            int pluginEnabled = inputMappingsManager.update( drawingManager, gameData, null, eventsManager, modifierMask );
            
            if ( pluginEnabled == -1 )
            {
                Logger.log( "Plugin disabled" );
            }
            else if ( pluginEnabled == 2 )
            {
                Logger.log( "Plugin enabled" );
                
                if ( !eventsManager.reloadConfiguration() )
                {
                    int numWidgets = drawingManager.getNumWidgets();
                    for ( int i = 0; i < numWidgets; i++ )
                    {
                        drawingManager.getWidget( i ).forceCompleteRedraw();
                    }
                }
            }
            
            return ( pluginEnabled > 0 );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( inputMappingsManager.isPluginEnabled() );
        }
    }
    
    private long frameIndex = 0;
    
    public final void update()
    {
        try
        {
            frameIndex++;
            
            drawingManager.refreshSubTextureBuffer();
            
            drawingManager.drawWidgets( gameData, null, TextureDirtyRectsManager.isCompleteRedrawForced(), drawingManager.getTexture( 0 ).getTexture() );
            //TextureDirtyRectsManager.drawDirtyRects( overlay );
            
            int n = drawingManager.getNumTextures();
            for ( int i = 0; i < n; i++ )
            {
                TextureDirtyRectsManager.getDirtyRects( frameIndex, drawingManager.getTexture( i ).getTexture(), drawingManager.getTexture( i ).getDirtyRectsBuffer(), true );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    private RFDynHUD( int gameResX, int gameResY ) throws Throwable
    {
        //Logger.setStdStreams();
        
        Logger.log( "Creating RFDynHUD instance Version " + VERSION.toString() + "..." );
        
        ByteOrderInitializer.setByteOrder( 0, 1, 2, 3 );
        //ByteOrderInitializer.setByteOrder( 3, 2, 1, 0 );
        //ByteOrderInitializer.setByteOrder( 3, 0, 1, 2 );
        //ByteOrderInitializer.setByteOrder( 1, 2, 3, 0 );
        
        Logger.log( "    Creating overlay texture interface for resolution " + gameResX + "x" + gameResY + "...", false );
        
        this.drawingManager = new WidgetsDrawingManager( __RenderPrivilegedAccess.createMainTexture( gameResX, gameResY ) );
        Logger.log( " done." );
        
        __WCPrivilegedAccess.setGameResolution( gameResX, gameResY, drawingManager );
        
        this.eventsManager = new RFactorEventsManager( drawingManager, this );
        
        this.gameData = new LiveGameData( eventsManager );
        this.gameData_CPP_Adapter = new LiveGameData_CPP_Adapter( gameData );
        
        eventsManager.setGameData( gameData );
        
        this.inputDeviceManager = new InputDeviceManager();
        this.inputMappingsManager = new InputMappingsManager();
        
        Logger.log( "Successfully created RFDynHUD instance." );
    }
    
    public static final RFDynHUD createInstance( int gameResX, int gameResY )
    {
        try
        {
            return ( new RFDynHUD( gameResX, gameResY ) );
        }
        catch ( Throwable t )
        {
            Logger.log( t );
            
            return ( null );
        }
    }
}
