package net.ctdp.rfdynhud;

import java.nio.ByteBuffer;

import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.RFactorEventsManager;
import net.ctdp.rfdynhud.gamedata._LiveGameData_CPP_Adapter;
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
 * @author Marvin Froehlich
 */
public class RFDynHUD
{
    public static final Version VERSION = new Version( 1, 1, 0, "Alpha", 72 );
    
    private final WidgetsDrawingManager drawingManager;
    private final LiveGameData gameData;
    private final _LiveGameData_CPP_Adapter gameData_CPP_Adapter;
    private final RFactorEventsManager eventsManager;
    private final InputDeviceManager inputDeviceManager;
    private final InputMappingsManager inputMappingsManager;
    
    private boolean renderMode = false;
    
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
    
    public final _LiveGameData_CPP_Adapter getGameData_CPP_Adapter()
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
    
    public byte updateInput( int modifierMask )
    {
        try
        {
            int pluginEnabled = inputMappingsManager.update( drawingManager, gameData, null, eventsManager, modifierMask );
            
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
                    int numWidgets = drawingManager.getNumWidgets();
                    for ( int i = 0; i < numWidgets; i++ )
                    {
                        drawingManager.getWidget( i ).forceCompleteRedraw();
                    }
                }
                
                return ( result );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        
        if ( inputMappingsManager.isPluginEnabled() && drawingManager.isValid() )
            return ( 1 );
        
        return ( 0 );
    }
    
    private long frameIndex = 0;
    
    /**
     * Will and must be called any time, the game is redendered (called from the C++-Plugin).
     * 
     * @return 0, if nothing shouldbe rendered anymore, 1 to render something, 2 to render and update texture info.
     */
    public final byte update()
    {
        byte result = 1;
        
        try
        {
            frameIndex++;
            
            drawingManager.refreshSubTextureBuffer( false );
            
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
        
        setRenderMode( result != 0 );
        
        return ( result );
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
        
        this.drawingManager = new WidgetsDrawingManager( gameResX, gameResY );
        Logger.log( " done." );
        
        this.eventsManager = new RFactorEventsManager( this, drawingManager );
        
        this.gameData = new LiveGameData( eventsManager );
        this.gameData_CPP_Adapter = new _LiveGameData_CPP_Adapter( gameData );
        
        eventsManager.setGameData( gameData );
        
        this.inputDeviceManager = new InputDeviceManager();
        this.inputMappingsManager = new InputMappingsManager( this );
        
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
