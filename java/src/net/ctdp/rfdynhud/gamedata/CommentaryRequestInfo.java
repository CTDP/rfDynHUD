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

import net.ctdp.rfdynhud.editor.EditorPresets;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class CommentaryRequestInfo
{
    final CommentaryRequestInfoCapsule data = new CommentaryRequestInfoCapsule();
    
    private final LiveGameData gameData;
    
    private long updateId = 0L;
    
    public static interface CommentaryInfoUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onCommentaryInfoUpdated( LiveGameData gameData, EditorPresets editorPresets );
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
        
        gameData.registerListener( l );
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
        
        gameData.unregisterListener( l );
    }
    
    void prepareDataUpdate()
    {
    }
    
    void onDataUpdated( EditorPresets editorPresets )
    {
        this.updateId++;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
                updateListeners[i].onCommentaryInfoUpdated( gameData, editorPresets );
        }
    }
    
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    void loadFromStream( InputStream in, EditorPresets editorPresets ) throws IOException
    {
        prepareDataUpdate();
        
        data.loadFromStream( in );
        
        onDataUpdated( editorPresets );
    }
    
    /**
     * one of the event names in the commentary INI file
     */
    public final String getName()
    {
        return ( data.getName() );
    }
    
    /**
     * first value to pass in (if any)
     */
    public final double getInput1()
    {
        return ( data.getInput1() );
    }
    
    /**
     * first value to pass in (if any)
     */
    public final double getInput2()
    {
        return ( data.getInput2() );
    }
    
    /**
     * first value to pass in (if any)
     */
    public final double getInput3()
    {
        return ( data.getInput3() );
    }
    
    /**
     * ignores commentary detail and random probability of event
     */
    public final boolean getSkipChecks()
    {
        return ( data.getSkipChecks() );
    }
    
    CommentaryRequestInfo( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
