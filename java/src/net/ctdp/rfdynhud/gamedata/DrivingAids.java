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

import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * Represents vehicle driving aids.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class DrivingAids
{
    private long updateTimestamp = -1L;
    private long updateId = 0L;
    private boolean updatedInTimeScope = false;
    
    private final LiveGameData gameData;
    
    public static interface DrivingAidsUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onDrivingAidsUpdated( LiveGameData gameData, boolean isEditorMode );
    }
    
    public static interface DrivingAidStateChangeListener extends DrivingAidsUpdateListener
    {
        public void onDrivingAidStateChanged( LiveGameData gameData, int aidIndex, int oldState, int newState );
    }
    
    private DrivingAidsUpdateListener[] updateListeners = null;
    private boolean hasStateChangeListener = false;
    private int[] oldStates = null;
    
    private void updateHasStateChangeListener()
    {
        hasStateChangeListener = false;
        
        if ( updateListeners != null )
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] instanceof DrivingAidStateChangeListener )
                {
                    hasStateChangeListener = true;
                    break;
                }
            }
        }
    }
    
    public void registerListener( DrivingAidsUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new DrivingAidsUpdateListener[] { l };
        }
        else
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] == l )
                    return;
            }
            
            DrivingAidsUpdateListener[] tmp = new DrivingAidsUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
        
        updateHasStateChangeListener();
        
        gameData.registerDataUpdateListener( l );
    }
    
    public void unregisterListener( DrivingAidsUpdateListener l )
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
            updateHasStateChangeListener();
            return;
        }
        
        DrivingAidsUpdateListener[] tmp = new DrivingAidsUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
        
        updateHasStateChangeListener();
        
        gameData.unregisterDataUpdateListener( l );
    }
    
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
     * This is incremented every time the data is updated.
     *  
     * @return the current update id.
     */
    public final long getUpdateId()
    {
        return ( updateId );
    }
    
    /**
     * Gets, whether these DrivingAids have at least been updated once.
     * 
     * @return whether these DrivingAids have at least been updated once.
     */
    public final boolean isValid()
    {
        return ( updateId > 0L );
    }
    
    /**
     * Gets, whether the last update of these data has been done while in the cockpit.
     * @return whether the last update of these data has been done while in the cockpit.
     */
    public final boolean isUpdatedInTimeScope()
    {
        return ( updatedInTimeScope );
    }
    
    /**
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
        try
        {
            this.updatedInTimeScope = gameData.isInCockpit();
            this.updateTimestamp = timestamp;
            this.updateId++;
            
            final int numAids = getNumAids();
            
            if ( ( numAids > 0 ) && ( updateListeners != null ) )
            {
                //boolean stateUpdates = gameData.isInCockpit() && hasStateChangeListener;
                boolean stateUpdates = hasStateChangeListener;
                
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onDrivingAidsUpdated( gameData, isEditorMode );
                        
                        if ( ( oldStates != null ) && ( updateListeners[i] instanceof DrivingAidStateChangeListener ) )
                        {
                            for ( int j = 0; j < numAids; j++ )
                            {
                                int state = getAidState( j );
                                if ( state != oldStates[j] )
                                    ( (DrivingAidStateChangeListener)updateListeners[i] ).onDrivingAidStateChanged( gameData, j, oldStates[j], state );
                            }
                        }
                    }
                    catch ( Throwable t )
                    {
                        RFDHLog.exception( t );
                    }
                }
                
                if ( stateUpdates )
                {
                    if ( ( oldStates == null ) || ( oldStates.length != numAids ) )
                        oldStates = new int[ numAids ];
                    
                    for ( int i = 0; i < numAids; i++ )
                    {
                        oldStates[i] = getAidState( i );
                    }
                }
                
                onDataUpdatedImpl( userObject, timestamp, isEditorMode );
            }
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
     * Gets the number of driving aids available in the current sim.
     * 
     * @return the number of driving aids available in the current sim.
     */
    public abstract int getNumAids();
    
    /**
     * Gets the index into the array of aids for the traction control aid.
     * 
     * @return the index into the array of aids for the traction control aid.
     */
    public abstract int getAidIndexTractionControl();
    
    /**
     * Gets the index into the array of aids for the anti lock brakes aid.
     * 
     * @return the index into the array of aids for the anti lock brakes aid.
     */
    public abstract int getAidIndexTractionAntiLockBrakes();
    
    /**
     * Gets the index into the array of aids for the auto shift aid.
     * 
     * @return the index into the array of aids for the auto shift aid.
     */
    public abstract int getAidIndexAutoShift();
    
    /**
     * Gets the index into the array of aids for the invulnerability mode.
     * 
     * @return the index into the array of aids for the invulnerability mode.
     */
    public abstract int getAidIndexInvulnerability();
    
    /**
     * Gets the driving aid's name at the given index.
     * 
     * @param index the index into the aids array
     * 
     * @return the driving aid's name at the given index.
     * 
     * @see #getNumAids()
     */
    public abstract String getAidName( int index );
    
    /**
     * Gets the requested driving aid's current state. 0 is usually disabled.
     * 
     * @param index the index into the aids array
     * 
     * @return the requested driving aid's current state.
     * 
     * @see #getNumAids()
     * @see #isAidEnabled(int)
     */
    public abstract int getAidState( int index );
    
    /**
     * Checks, whether the requested driving aid is currently enabled.
     * 
     * @param index the index into the aids array
     * 
     * @return whether the requested driving aid is currently enabled.
     * 
     * @see #getNumAids()
     * @see #getAidState(int)
     */
    public boolean isAidEnabled( int index )
    {
        return ( getAidState( index ) != 0 );
    }
    
    /**
     * Gets a display name for the requested state.
     * 
     * @param index the index into the aids array
     * @param state the requested state
     * 
     * @return a display name for the requested state.
     * 
     * @see #getNumAids()
     */
    public abstract String getAidStateName( int index, int state );
    
    /**
     * Gets a display name for the current state.
     * 
     * @param index the index into the aids array
     * 
     * @return a display name for the requested state.
     * 
     * @see #getNumAids()
     */
    public String getAidStateName( int index )
    {
        return ( getAidStateName( index, getAidState( index ) ) );
    }
    
    /**
     * Gets the minimum state for the requested driving aid (usually 0).
     * 
     * @param index the index into the aids array
     * 
     * @return the minimum state for the requested driving aid.
     * 
     * @see #getNumAids()
     */
    public abstract int getMinState( int index );
    
    /**
     * Gets the maximum state for the requested driving aid (usually 0).
     * 
     * @param index the index into the aids array
     * 
     * @return the maximum state for the requested driving aid.
     * 
     * @see #getNumAids()
     */
    public abstract int getMaxState( int index );
    
    /**
     * Gets the number of states for the requested driving aid (usually 0).
     * 
     * @param index the index into the aids array
     * 
     * @return the number of states for the requested driving aid.
     * 
     * @see #getNumAids()
     */
    public abstract int getNumStates( int index );
    
    /**
     * Gets an {@link ImageTemplate} for the requested aid and state.
     * 
     * @param index
     * @param state
     * 
     * @return an {@link ImageTemplate} for the requested aid and state.
     * 
     * @see #getNumAids()
     */
    public abstract ImageTemplate getAidIcon( int index, int state );
    
    /**
     * Gets an {@link ImageTemplate} for the requested aid and the current state.
     * 
     * @param index
     * 
     * @return an {@link ImageTemplate} for the requested aid and the current state.
     * 
     * @see #getNumAids()
     */
    public final ImageTemplate getAidIcon( int index )
    {
        return ( getAidIcon( index, getAidState( index ) ) );
    }
    
    public DrivingAids( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
