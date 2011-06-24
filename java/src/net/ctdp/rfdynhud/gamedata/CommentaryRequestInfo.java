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
public class CommentaryRequestInfo
{
    final _CommentaryRequestInfoCapsule data;
    
    private final LiveGameData gameData;
    
    private long updateId = 0L;
    
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
    
    void prepareDataUpdate()
    {
    }
    
    void onDataUpdated( boolean isEditorMode )
    {
        this.updateId++;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onCommentaryInfoUpdated( gameData, isEditorMode );
        }
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
     * Gets one of the event names in the commentary INI file
     * 
     * @return one of the event names in the commentary INI file
     */
    public final String getName()
    {
        return ( data.getName() );
    }
    
    /**
     * Gets first value to pass in (if any)
     * 
     * @return first value to pass in (if any)
     */
    public final double getInput1()
    {
        return ( data.getInput1() );
    }
    
    /**
     * Gets second value to pass in (if any)
     * 
     * @return second value to pass in (if any)
     */
    public final double getInput2()
    {
        return ( data.getInput2() );
    }
    
    /**
     * Gets third value to pass in (if any)
     * 
     * @return third value to pass in (if any)
     */
    public final double getInput3()
    {
        return ( data.getInput3() );
    }
    
    /**
     * @return ignores commentary detail and random probability of event
     */
    public final boolean getSkipChecks()
    {
        return ( data.getSkipChecks() );
    }
    
    CommentaryRequestInfo( LiveGameData gameData, _LiveGameDataObjectsFactory gdFactory )
    {
        this.gameData = gameData;
        this.data = gdFactory.newCommentaryRequestInfoCapsule( gameData );
    }
}
