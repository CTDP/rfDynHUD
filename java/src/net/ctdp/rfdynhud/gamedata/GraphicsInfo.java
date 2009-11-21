package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Marvin Froehlich
 */
public class GraphicsInfo
{
    private static final int OFFSET_CAM_POS = 0;
    private static final int OFFSET_CAM_ORI = OFFSET_CAM_POS + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_HWND = OFFSET_CAM_ORI + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_AMBIENT_RED = OFFSET_HWND + ByteUtil.SIZE_LONG;
    private static final int OFFSET_AMBIENT_GREEN = OFFSET_AMBIENT_RED + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_AMBIENT_BLUE = OFFSET_AMBIENT_GREEN + ByteUtil.SIZE_FLOAT;
    
    private static final int BUFFER_SIZE = OFFSET_AMBIENT_BLUE + ByteUtil.SIZE_FLOAT;
    
    final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private final LiveGameData gameData;
    
    private long updateID = 0L;
    
    public static interface GraphicsInfoUpdateListener
    {
        public void onGraphicsInfoUpdated( LiveGameData gameData );
    }
    
    private GraphicsInfoUpdateListener[] updateListeners = null;
    
    public void registerListener( GraphicsInfoUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new GraphicsInfoUpdateListener[] { l };
        }
        else
        {
            GraphicsInfoUpdateListener[] tmp = new GraphicsInfoUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
    }
    
    public void unregisterListener( GraphicsInfoUpdateListener l )
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
        
        GraphicsInfoUpdateListener[] tmp = new GraphicsInfoUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
    }
    
    void prepareDataUpdate()
    {
    }
    
    void onDataUpdated()
    {
        this.updateID++;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onGraphicsInfoUpdated( gameData );
        }
    }
    
    public final long getUpdateID()
    {
        return ( updateID );
    }
    
    void loadFromStream( InputStream in ) throws IOException
    {
        prepareDataUpdate();
        
        int offset = 0;
        int bytesToRead = BUFFER_SIZE;
        
        while ( bytesToRead > 0 )
        {
            int n = in.read( buffer, offset, bytesToRead );
            
            if ( n < 0 )
                throw new IOException();
            
            offset += n;
            bytesToRead -= n;
        }
        
        onDataUpdated();
    }
    
    // GraphicsInfo
    
    /**
     * camera position
     * 
     * @param position
     */
    public final void getCameraPosition( TelemVect3 position )
    {
        // TelemVect3 mCamPos
        
        ByteUtil.readVector( buffer, OFFSET_CAM_POS, position );
    }
    
    /**
     * camera orientation
     * 
     * @param orientation
     */
    public final void getCameraOrientation( TelemVect3 orientation )
    {
        // TelemVect3 mCamOri
        
        ByteUtil.readVector( buffer, OFFSET_CAM_ORI, orientation );
    }
    
    // HWND mHWND;                    // app handle
    
    // GraphicsInfoV2
    
    /**
     */
    public final java.awt.Color getAmbientColor()
    {
        // float mAmbientRed
        // float mAmbientGreen
        // float mAmbientBlue
        
        float red = ByteUtil.readFloat( buffer, OFFSET_AMBIENT_RED );
        float green = ByteUtil.readFloat( buffer, OFFSET_AMBIENT_GREEN );
        float blue = ByteUtil.readFloat( buffer, OFFSET_AMBIENT_BLUE );
        
        return ( new java.awt.Color( (int)( red * 255f ), (int)( green * 255f ), (int)( blue * 255f ) ) );
    }
    
    GraphicsInfo( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
