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
public abstract class _ScoringInfoCapsule
{
    public abstract byte[] getBuffer();
    
    public abstract void loadFromStream( InputStream in ) throws IOException;
    
    public abstract void writeToStream( OutputStream out ) throws IOException;
    
    /**
     * @return current track name
     */
    public abstract String getTrackName();
    
    /**
     * @return current session
     */
    public abstract SessionType getSessionType();
    
    /**
     * @return current session time
     */
    public abstract float getSessionTime();
    
    /**
     * @return session ending time
     */
    public abstract float getEndTime();
    
    /**
     * @return maximum laps
     */
    public abstract int getMaxLaps();
    
    /**
     * @return distance around track
     */
    public abstract float getTrackLength();
    
    /**
     * @return current number of vehicles
     */
    public abstract int getNumVehicles();
    
    /**
     * @return Game phases
     */
    public abstract GamePhase getGamePhase();
    
    /**
     * @return Yellow flag states (applies to full-course only)
     */
    public abstract YellowFlagState getYellowFlagState();
    
    /**
     * @return whether there are any local yellows at the moment in each sector
     * 
     * @param sector the sector in question
     */
    public abstract boolean getSectorYellowFlag( int sector );
    
    /**
     * @return start light frame (number depends on track)
     */
    public abstract int getStartLightFrame();
    
    /**
     * @return number of red lights in start sequence
     */
    public abstract int getNumRedLights();
    
    /**
     * @return in realtime as opposed to at the monitor
     */
    public abstract boolean isInRealtimeMode();
    
    /**
     * @return player name (including possible multiplayer override)
     */
    public abstract String getPlayerName();
    
    /**
     * @return may be encoded to be a legal filename
     */
    public abstract String getPlayerFilename();
    
    /**
     * @return cloud darkness? 0.0-1.0
     */
    public abstract float getCloudDarkness();
    
    /**
     * @return raining severity 0.0-1.0
     */
    public abstract float getRainingSeverity();
    
    /**
     * @return temperature (Celsius)
     */
    public abstract float getAmbientTemperature();
    
    /**
     * @return temperature (Celsius)
     */
    public abstract float getTrackTemperature();
    
    /**
     * wind speed
     * 
     * @param speed output buffer
     */
    public abstract void getWindSpeed( TelemVect3 speed );
    
    /**
     * @return on main path 0.0-1.0
     */
    public abstract float getOnPathWetness();
    
    /**
     * @return off main path 0.0-1.0
     */
    public abstract float getOffPathWetness();
    
    protected _ScoringInfoCapsule()
    {
    }
}
