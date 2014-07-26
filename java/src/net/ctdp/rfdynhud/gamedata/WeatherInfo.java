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

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits.Convert;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.SpeedUnits;
import net.ctdp.rfdynhud.util.RFDHLog;

/**
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class WeatherInfo
{
    private final LiveGameData gameData;
    
    private boolean updatedInTimeScope = false;
    private long updateId = 0L;
    private long updateTimestamp = -1L;
    
    public static interface WeatherInfoUpdateListener extends LiveGameData.GameDataUpdateListener
    {
        public void onWeatherInfoUpdated( LiveGameData gameData, boolean isEditorMode );
    }
    
    private WeatherInfoUpdateListener[] updateListeners = null;
    
    public void registerListener( WeatherInfoUpdateListener l )
    {
        if ( updateListeners == null )
        {
            updateListeners = new WeatherInfoUpdateListener[] { l };
        }
        else
        {
            for ( int i = 0; i < updateListeners.length; i++ )
            {
                if ( updateListeners[i] == l )
                    return;
            }
            
            WeatherInfoUpdateListener[] tmp = new WeatherInfoUpdateListener[ updateListeners.length + 1 ];
            System.arraycopy( updateListeners, 0, tmp, 0, updateListeners.length );
            updateListeners = tmp;
            updateListeners[updateListeners.length - 1] = l;
        }
        
        gameData.registerDataUpdateListener( l );
    }
    
    public void unregisterListener( WeatherInfoUpdateListener l )
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
        
        WeatherInfoUpdateListener[] tmp = new WeatherInfoUpdateListener[ updateListeners.length - 1 ];
        if ( index > 0 )
            System.arraycopy( updateListeners, 0, tmp, 0, index );
        if ( index < updateListeners.length - 1 )
            System.arraycopy( updateListeners, index + 1, tmp, index, updateListeners.length - index - 1 );
        updateListeners = tmp;
        
        gameData.unregisterDataUpdateListener( l );
    }
    
    public abstract void readFromStream( InputStream in, EditorPresets editorPresets ) throws IOException;
    
    /**
     * Read default values. This is usually done in editor mode.
     * 
     * @param editorPresets <code>null</code> in non editor mode
     */
    public abstract void loadDefaultValues( EditorPresets editorPresets );
    
    public abstract void writeToStream( OutputStream out ) throws IOException;
    
    
    /**
     * 
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected void prepareDataUpdate( Object userObject, long timestamp )
    {
    }
    
    /**
     * 
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected abstract void updateDataImpl( Object userObject, long timestamp );
    
    protected void applyEditorPresets( EditorPresets editorPresets )
    {
        if ( editorPresets == null )
            return;
    }
    
    /**
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected void onDataUpdatedImpl( Object userObject, long timestamp )
    {
    }
    
    /**
     * @param userObject (could be an instance of {@link EditorPresets}), if in editor mode
     * @param timestamp
     */
    protected final void onDataUpdated( Object userObject, long timestamp )
    {
        try
        {
            this.updatedInTimeScope = true;
            this.updateId++;
            this.updateTimestamp = timestamp;
            
            if ( userObject instanceof EditorPresets )
                applyEditorPresets( (EditorPresets)userObject );
            
            if ( updateListeners != null )
            {
                for ( int i = 0; i < updateListeners.length; i++ )
                {
                    try
                    {
                        updateListeners[i].onWeatherInfoUpdated( gameData, userObject instanceof EditorPresets );
                    }
                    catch ( Throwable t )
                    {
                        RFDHLog.exception( t );
                    }
                }
            }
            
            onDataUpdatedImpl( userObject, timestamp );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    protected void updateData( Object userObject, long timestamp )
    {
        //if ( gameData.getProfileInfo().isValid() )
        {
            prepareDataUpdate( userObject, timestamp );
            
            updateDataImpl( userObject, timestamp );
            
            onDataUpdated( userObject, timestamp );
        }
    }
    
    /**
     * @param timestamp
     * @param isEditorMode
     */
    final void onSessionStarted( long timestamp, boolean isEditorMode )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * 
     * @param timestamp
     */
    final void onSessionEnded( long timestamp )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * 
     * @param timestamp
     */
    final void onCockpitEntered( long timestamp )
    {
        this.updatedInTimeScope = true;
    }
    
    /**
     * 
     * @param timestamp
     */
    final void onCockpitExited( long timestamp )
    {
        this.updatedInTimeScope = false;
    }
    
    /**
     * Gets, whether the last update of these data has been done while in running session resp. cockpit mode.
     * @return whether the last update of these data has been done while in running session resp. cockpit mode.
     */
    public final boolean isUpdatedInTimeScope()
    {
        return ( updatedInTimeScope );
    }
    
    /**
     * Gets whether this data has been updated in the current session.
     * 
     * @return whether this data has been updated in the current session.
     */
    public final boolean isValid()
    {
        return ( updateId > 0L );
    }
    
    /**
     * Gets an ID, that in incremented every time, this {@link WeatherInfo} object is filled with new data from the game.
     * 
     * @return an ID, that in incremented every time, this {@link WeatherInfo} object is filled with new data from the game.
     */
    public final long getUpdateId()
    {
        return ( updateId );
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
     * Gets cloud darkness? 0.0-1.0
     * 
     * @return cloud darkness? 0.0-1.0
     */
    public abstract float getCloudDarkness();
    
    /**
     * Gets raining severity 0.0-1.0
     * 
     * @return raining severity 0.0-1.0
     */
    public abstract float getRainingSeverity();
    
    /**
     * Gets ambient temperature (Kelvin)
     * 
     * @return ambient temperature (Kelvin)
     */
    public abstract float getAmbientTemperatureK();
//        return ( data.getAmbientTemperature() - Convert.ZERO_KELVIN );
    
    /**
     * Gets ambient temperature (Celsius)
     * 
     * @return ambient temperature (Celsius)
     */
    public final float getAmbientTemperatureC()
    {
        return ( getAmbientTemperatureK() + Convert.ZERO_KELVIN );
    }
    
    /**
     * Gets ambient temperature (Fahrenheit)
     * 
     * @return ambient temperature (Fahrenheit)
     */
    public final float getAmbientTemperatureF()
    {
        return ( Convert.celsius2Fahrehheit( getAmbientTemperatureC() ) );
    }
    
    /**
     * Gets ambient temperature (PLR selected units)
     * 
     * @return ambient temperature (PLR selected units)
     */
    public final float getAmbientTemperature()
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getAmbientTemperatureF() );
        
        return ( getAmbientTemperatureC() );
    }
    
    /**
     * Gets track temperature (Kelvin)
     * 
     * @return track temperature (Kelvin)
     */
    public abstract float getTrackTemperatureK();
//        return ( data.getTrackTemperature() - Convert.ZERO_KELVIN );
    
    /**
     * Gets track temperature (Celsius)
     * 
     * @return track temperature (Celsius)
     */
    public final float getTrackTemperatureC()
    {
        return ( getTrackTemperatureK() + Convert.ZERO_KELVIN );
    }
    
    /**
     * Gets track temperature (Fahrenheit)
     * 
     * @return track temperature (Fahrenheit)
     */
    public final float getTrackTemperatureF()
    {
        return ( Convert.celsius2Fahrehheit( getTrackTemperatureC() ) );
    }
    
    /**
     * Gets track temperature (PLR selected units)
     * 
     * @return track temperature (PLR selected units)
     */
    public final float getTrackTemperature()
    {
        if ( gameData.getProfileInfo().getMeasurementUnits() == MeasurementUnits.IMPERIAL )
            return ( getTrackTemperatureF() );
        
        return ( getTrackTemperatureC() );
    }
    
    /**
     * Gets wind speed in m/sec.
     * 
     * @param speed output buffer
     * 
     * @return the input buffer back again.
     */
    public abstract TelemVect3 getWindSpeedMS( TelemVect3 speed );
    
    /**
     * Gets wind speed in km/h.
     * 
     * @param speed output buffer
     * 
     * @return the input buffer back again.
     */
    public final TelemVect3 getWindSpeedKmh( TelemVect3 speed )
    {
        speed = getWindSpeedMS( speed );
        
        speed.x *= SpeedUnits.Convert.MS_TO_KMH;
        speed.y *= SpeedUnits.Convert.MS_TO_KMH;
        speed.z *= SpeedUnits.Convert.MS_TO_KMH;
        
        return ( speed );
    }
    
    /**
     * Gets wind speed in mi/h.
     * 
     * @param speed output buffer
     * 
     * @return the input buffer back again.
     */
    public final TelemVect3 getWindSpeedMih( TelemVect3 speed )
    {
        speed = getWindSpeedMS( speed );
        
        speed.x *= SpeedUnits.Convert.MS_TO_MIH;
        speed.y *= SpeedUnits.Convert.MS_TO_MIH;
        speed.z *= SpeedUnits.Convert.MS_TO_MIH;
        
        return ( speed );
    }
    
    /**
     * Gets wind speed in km/h or mi/h depending on PLR settings.
     * 
     * @param speed output buffer
     * 
     * @return the input buffer back again.
     */
    public final TelemVect3 getWindSpeed( TelemVect3 speed )
    {
        if ( gameData.getProfileInfo().getSpeedUnits() == SpeedUnits.MIH )
            return ( getWindSpeedMih( speed ) );
        
        return ( getWindSpeedKmh( speed ) );
    }
    
    /**
     * Gets wetness on main path 0.0-1.0
     * 
     * @return wetness on main path 0.0-1.0
     */
    public abstract float getOnPathWetness();
    
    /**
     * Gets wetness off main path 0.0-1.0
     * 
     * @return wetness off main path 0.0-1.0
     */
    public abstract float getOffPathWetness();
    
    protected WeatherInfo( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
