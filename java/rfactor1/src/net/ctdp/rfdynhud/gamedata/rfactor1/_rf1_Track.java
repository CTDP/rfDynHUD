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
package net.ctdp.rfdynhud.gamedata.rfactor1;

import net.ctdp.rfdynhud.gamedata.Track;

/**
 * Model of a track with waypoints and utility methods
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf1_Track extends Track
{
    protected static final class _rf1_Waypoint extends Waypoint
    {
        float posX;
        float posY;
        float posZ;
        
        float vecX;
        float vecY;
        float vecZ;
        
        float normX;
        float normY;
        float normZ;
        
        float roadLeft;
        float roadRight;
        float farLeft;
        float farRight;
        
        byte sector;
        float lapDistance;
        float normalizedLapDistance;
        
        @Override
        public float getPosX()
        {
            return ( posX );
        }
        
        @Override
        public float getPosY()
        {
            return ( posY );
        }
        
        @Override
        public float getPosZ()
        {
            return ( posZ );
        }
        
        @Override
        public float getVecX()
        {
            return ( vecX );
        }
        
        @Override
        public float getVecY()
        {
            return ( vecY );
        }
        
        @Override
        public float getVecZ()
        {
            return ( vecZ );
        }
        
        //@Override
        public float getNormX()
        {
            return ( normX );
        }
        
        //@Override
        public float getNormY()
        {
            return ( normY );
        }
        
        //@Override
        public float getNormZ()
        {
            return ( normZ );
        }
        
        @Override
        public float getRoadLeft()
        {
            return ( roadLeft );
        }
        
        @Override
        public float getRoadRight()
        {
            return ( roadRight );
        }
        
        //@Override
        public float getFarLeft()
        {
            return ( farLeft );
        }
        
        //@Override
        public float getFarRight()
        {
            return ( farRight );
        }
        
        @Override
        public byte getSector()
        {
            return ( sector );
        }
        
        @Override
        public float getLapDistance()
        {
            return ( lapDistance );
        }
        
        //@Override
        public float getNormalizedLapDistance()
        {
            return ( normalizedLapDistance );
        }
    }
    
    private final _rf1_Waypoint[] waypointsTrack;
    private final _rf1_Waypoint[] waypointsPitlane;
    private final float sector1Length, sector2Length, trackLength, pitlaneLength;
    private final float minXPos, maxXPos, minYPos, maxYPos, minZPos, maxZPos;
    private final float maxWidth;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getSector1Length()
    {
        return ( sector1Length );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getSector2Length()
    {
        return ( sector2Length - sector1Length );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getSector3Length()
    {
        return ( trackLength - sector2Length );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getTrackLength()
    {
        return ( trackLength );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getPitlaneLength()
    {
        return ( pitlaneLength );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getMinXPos()
    {
        return ( minXPos );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getMinYPos()
    {
        return ( minYPos );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getMinZPos()
    {
        return ( minZPos );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getMaxXPos()
    {
        return ( maxXPos );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getMaxYPos()
    {
        return ( maxYPos );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final float getMaxZPos()
    {
        return ( maxZPos );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getMaxTrackWidth( float scale )
    {
        return ( Math.round( maxWidth * scale ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int getNumWaypoints( boolean pitlane )
    {
        if ( pitlane )
            return ( waypointsPitlane.length );
        
        return ( waypointsTrack.length );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Waypoint getWaypoint( boolean pitlane, int waypointIndex )
    {
        return ( pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex] );
    }
    
    protected _rf1_Track( _rf1_Waypoint[] waypointsTrack, _rf1_Waypoint[] waypointsPitlane, float sector1Length, float sector2Length, float trackLength, float pitlaneLength, float minXPos, float maxXPos, float minYPos, float maxYPos, float minZPos, float maxZPos, float maxWidth )
    {
        this.waypointsTrack = waypointsTrack;
        this.waypointsPitlane = waypointsPitlane;
        this.sector1Length = sector1Length;
        this.sector2Length = sector2Length;
        this.trackLength = trackLength;
        this.pitlaneLength = pitlaneLength;
        this.minXPos = minXPos;
        this.maxXPos = maxXPos;
        this.minYPos = minYPos;
        this.maxYPos = maxYPos;
        this.minZPos = minZPos;
        this.maxZPos = maxZPos;
        this.maxWidth = maxWidth;
    }
}
