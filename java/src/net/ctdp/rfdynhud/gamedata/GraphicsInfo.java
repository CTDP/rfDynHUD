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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class GraphicsInfo
{
    final GraphicsInfoCapsule data = new GraphicsInfoCapsule();
    
    private final LiveGameData gameData;
    
    private boolean updatedInRealtimeMode = false;
    
    private long updateId = 0L;
    
    public static interface GraphicsInfoUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onViewportChanged( LiveGameData gameData, int viewportX, int viewportY, int viewportWidth, int viewportHeight );
        
        public void onGraphicsInfoUpdated( LiveGameData gameData, boolean isEditorMode );
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
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] == l )
                    return;
            }
            
            GraphicsInfoUpdateListener[] tmp = new GraphicsInfoUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
        
        gameData.registerListener( l );
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
        
        gameData.unregisterListener( l );
    }
    
    void prepareDataUpdate()
    {
    }
    
    void onViewportChanged( int viewportX, int viewportY, int viewportWidth, int viewportHeight )
    {
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onViewportChanged( gameData, viewportX, viewportY, viewportWidth, viewportHeight );
        }
    }
    
    void onDataUpdated( boolean isEditorMode )
    {
        this.updatedInRealtimeMode = gameData.isInRealtimeMode();
        this.updateId++;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onGraphicsInfoUpdated( gameData, isEditorMode );
        }
    }
    
    /**
     * Gets, whether the last update of these data has been done while in realtime mode.
     * @return whether the last update of these data has been done while in realtime mode.
     */
    public final boolean isUpdatedInRealtimeMode()
    {
        return ( updatedInRealtimeMode );
    }
    
    /**
     * This is incremented every time the info is updated.
     *  
     * @return the current update id.
     */
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    void loadFromStream( InputStream in, boolean isEditorMode ) throws IOException
    {
        prepareDataUpdate();
        
        data.loadFromStream( in );
        
        onDataUpdated( isEditorMode );
    }
    
    public void readFromStream( InputStream in ) throws IOException
    {
        loadFromStream( in, false );
    }
    
    public void writeToStream( OutputStream out ) throws IOException
    {
        data.writeToStream( out );
    }
    
    /**
     * camera position in meters
     * 
     * @param position output buffer
     */
    public final void getCameraPosition( TelemVect3 position )
    {
        data.getCameraPosition( position );
    }
    
    /**
     * Gets camera position in meters.
     * 
     * @return camera position in meters.
     */
    public final float getCameraPositionX()
    {
        return ( data.getCameraPositionX() );
    }
    
    /**
     * Gets camera position in meters.
     * 
     * @return camera position in meters.
     */
    public final float getCameraPositionY()
    {
        return ( data.getCameraPositionY() );
    }
    
    /**
     * Gets camera position in meters.
     * 
     * @return camera position in meters.
     */
    public final float getCameraPositionZ()
    {
        return ( data.getCameraPositionZ() );
    }
    
    /**
     * camera orientation
     * 
     * @param orientation output buffer
     */
    public final void getCameraOrientation( TelemVect3 orientation )
    {
        data.getCameraOrientation( orientation );
    }
    
    /**
     * Gets the current ambient color.
     * 
     * @return the current ambient color.
     */
    public final java.awt.Color getAmbientColor()
    {
        return ( data.getAmbientColor() );
    }
    
    GraphicsInfo( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
