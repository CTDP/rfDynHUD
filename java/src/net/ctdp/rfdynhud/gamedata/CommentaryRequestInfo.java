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
package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class CommentaryRequestInfo
{
    private long updateTimestamp = -1L;
    private long updateId = 0L;
    
    protected final LiveGameData gameData;
    
    public static interface CommentaryInfoUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onCommentaryInfoUpdated( LiveGameData gameData, boolean isEditorMode );
    }
    
    private CommentaryInfoUpdateListener[] updateListeners = null;
    
    public void registerListener( CommentaryInfoUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new CommentaryInfoUpdateListener[] { l };
        }
        else
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] == l )
                    return;
            }
            
            CommentaryInfoUpdateListener[] tmp = new CommentaryInfoUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
        
        gameData.registerDataUpdateListener( l );
    }
    
    public void unregisterListener( CommentaryInfoUpdateListener l )
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
        
        CommentaryInfoUpdateListener[] tmp = new CommentaryInfoUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
        
        gameData.unregisterDataUpdateListener( l );
    }
    
    public abstract void readFromStream( InputStream in, boolean isEditorMode ) throws IOException;
    
    /**
     * Read default values. This is usually done in editor mode.
     * 
     * @param isEditorMode
     * 
     * @throws IOException
     */
    public abstract void readDefaultValues( boolean isEditorMode ) throws IOException;
    
    public abstract void writeToStream( OutputStream out ) throws IOException;
    
    /**
     * Gets the system nano time for the last data update.
     * 
     * @return the system nano time for the last data update.
     */
    public final long getUpdateTimestamp()
    {
        return ( updateTimestamp );
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
    
    /**
     * 
     * @param userObject
     * @param timestamp
     */
    protected void prepareDataUpdate( Object userObject, long timestamp )
    {
    }
    
    /**
     * @param userObject
     * @param timestamp
     * @param isEditorMode
     */
    protected void onDataUpdatedImpl( Object userObject, long timestamp, boolean isEditorMode )
    {
    }
    
    /**
     * @param userObject
     * @param timestamp
     * @param isEditorMode
     */
    protected final void onDataUpdated( Object userObject, long timestamp, boolean isEditorMode )
    {
        this.updateTimestamp = timestamp;
        this.updateId++;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                try
                {
                    updateListeners[i].onCommentaryInfoUpdated( gameData, isEditorMode );
                }
                catch ( Throwable t )
                {
                    RFDHLog.exception( t );
                }
            }
        }
        
        try
        {
            onDataUpdatedImpl( userObject, timestamp, isEditorMode );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    protected abstract void updateDataImpl( Object userObject, long timestamp );
    
    protected void updateData( Object userObject, long timestamp )
    {
        prepareDataUpdate( userObject, timestamp );
        
        updateDataImpl( userObject, timestamp );
        
        onDataUpdated( userObject, timestamp, false );
    }
    
    /**
     * Gets one of the event names in the commentary INI file
     * 
     * @return one of the event names in the commentary INI file
     */
    public abstract String getName();
    
    /**
     * Gets first value to pass in (if any)
     * 
     * @return first value to pass in (if any)
     */
    public abstract double getInput1();
    
    /**
     * Gets second value to pass in (if any)
     * 
     * @return second value to pass in (if any)
     */
    public abstract double getInput2();
    
    /**
     * Gets third value to pass in (if any)
     * 
     * @return third value to pass in (if any)
     */
    public abstract double getInput3();
    
    /**
     * @return ignores commentary detail and random probability of event
     */
    public abstract boolean getSkipChecks();
    
    protected CommentaryRequestInfo( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
