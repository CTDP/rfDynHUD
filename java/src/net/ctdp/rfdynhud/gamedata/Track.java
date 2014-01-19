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

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Model of a track with waypoints and utility methods
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class Track
{
    protected static abstract class Waypoint
    {
        public abstract float getPosX();
        public abstract float getPosY();
        public abstract float getPosZ();
        
        public abstract float getVecX();
        public abstract float getVecY();
        public abstract float getVecZ();
        
        public abstract float getRoadLeft();
        public abstract float getRoadRight();
        
        public abstract byte getSector();
        public abstract float getLapDistance();
        
        public final float getRoadWidth()
        {
            return ( Math.abs( getRoadLeft() - getRoadRight() ) );
        }
    }
    
    /**
     * Gets the length of sector 1 in meters.
     * 
     * @return the length of sector 1 in meters.
     */
    public abstract float getSector1Length();
    
    /**
     * Gets the length of sector 2 (not including sector 1) in meters.
     * 
     * @return the length of sector 2 in meters.
     */
    public abstract float getSector2Length();
    
    /**
     * Gets the length of sector 2 in meters.
     * 
     * @param includingSector1
     * 
     * @return the length of sector 2 in meters.
     */
    public final float getSector2Length( boolean includingSector1 )
    {
        if ( includingSector1 )
            return ( getSector2Length() + getSector1Length() );
        
        return ( getSector2Length() );
    }
    
    /**
     * Gets the length of sector 3 in meters.
     * 
     * @return the length of sector 3 in meters.
     */
    public abstract float getSector3Length();
    
    /**
     * Gets the track length in meters.
     * 
     * @return the track length in meters.
     */
    public abstract float getTrackLength();
    
    public abstract float getPitlaneLength();
    
    public abstract float getMinXPos();
    
    public abstract float getMinYPos();
    
    public abstract float getMinZPos();
    
    public abstract float getMaxXPos();
    
    public abstract float getMaxYPos();
    
    public abstract float getMaxZPos();
    
    /**
     * Calculates a scale factor for all values, if the track should be drawn on the given size.
     * 
     * @param targetWidth the target width to draw on
     * @param targetHeight the target height to draw on
     * 
     * @return a scale factor for all values.
     */
    public float getScale( int targetWidth, int targetHeight )
    {
        float scaleX = targetWidth / Math.abs( getMaxXPos() - getMinXPos() );
        float scaleZ = targetHeight / Math.abs( getMaxZPos() - getMinZPos() );
        
        return ( Math.min( scaleX, scaleZ ) );
    }
    
    /**
     * Gets the extend along the x axis.
     * 
     * @param scale the scale. See {@link #getScale(int, int)}
     * 
     * @return the extend along the x axis.
     */
    public final int getXExtend( float scale )
    {
        return ( Math.round( Math.abs( getMaxXPos() - getMinXPos()  ) * scale ) );
    }
    
    /**
     * Gets the extend along the y axis.
     * 
     * @param scale the scale. See {@link #getScale(int, int)}
     * 
     * @return the extend along the y axis.
     */
    public final int getYExtend( float scale )
    {
        return ( Math.round( Math.abs( getMaxYPos() - getMinYPos()  ) * scale ) );
    }
    
    /**
     * Gets the extend along the z axis.
     * 
     * @param scale the scale. See {@link #getScale(int, int)}
     * 
     * @return the extend along the z axis.
     */
    public final int getZExtend( float scale )
    {
        return ( Math.round( Math.abs( getMaxZPos() - getMinZPos()  ) * scale ) );
    }
    
    /**
     * Gets the maximum track width.
     * 
     * @param scale the scale. See {@link #getScale(int, int)}
     * 
     * @return the maximum track width.
     */
    public abstract int getMaxTrackWidth( float scale );
    
    /**
     * Gets the number of waypoints of the main track or the pitlane.
     * 
     * @param pitlane waypoints of main track or pitlane?
     * 
     * @return the number of waypoints of the main track or the pitlane.
     */
    public abstract int getNumWaypoints( boolean pitlane );
    
    /**
     * Gets the requested waypoint.
     * 
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * 
     * @return the requested waypoint.
     */
    protected abstract Waypoint getWaypoint( boolean pitlane, int waypointIndex );
    
    /**
     * Gets the sector, the requested waypoint is in.
     * 
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * 
     * @return the sector, the requested waypoint is in.
     */
    public final byte getWaypointSector( boolean pitlane, int waypointIndex )
    {
        Waypoint wp = getWaypoint( pitlane, waypointIndex );
        
        return ( wp.getSector() );
    }
    
    /**
     * Gets the waypoint's position.
     * 
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * @param position output buffer
     */
    public final void getWaypointPosition( boolean pitlane, int waypointIndex, TelemVect3 position )
    {
        Waypoint wp = getWaypoint( pitlane, waypointIndex );
        
        __GDPrivilegedAccess.setTelemVect3( wp.getPosX(), wp.getPosY(), wp.getPosZ(), position );
    }
    
    /**
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * @param scale the scale. See {@link #getScale(int, int)}
     * @param point output buffer
     */
    public final void getWaypointPosition( boolean pitlane, int waypointIndex, float scale, Point point )
    {
        Waypoint wp = getWaypoint( pitlane, waypointIndex );
        
        point.setLocation( Math.round( ( -getMinXPos() + wp.getPosX() ) * scale ), Math.round( ( -getMinZPos() + wp.getPosZ() ) * scale ) );
    }
    
    /**
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * @param scale the scale. See {@link #getScale(int, int)}
     * @param point output buffer
     */
    public final void getWaypointPosition( boolean pitlane, int waypointIndex, float scale, Point2D.Float point )
    {
        Waypoint wp = getWaypoint( pitlane, waypointIndex );
        
        point.setLocation( ( -getMinXPos() + wp.getPosX() ) * scale, ( -getMinZPos() + wp.getPosZ() ) * scale );
    }
    
    /**
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * @param vector output buffer
     */
    public final void getWaypointVector( boolean pitlane, int waypointIndex, Point2D.Float vector )
    {
        Waypoint wp = getWaypoint( pitlane, waypointIndex );
        
        vector.setLocation( wp.getVecX(), wp.getVecZ() );
    }
    
    /**
     * @param pitlane waypoint of main track or pitlane?
     * @param trackDistance the distance along the track in meters
     * @param scale the scale. See {@link #getScale(int, int)}
     * @param point output buffer
     */
    public final void getInterpolatedPosition( boolean pitlane, float trackDistance, float scale, Point2D.Float point )
    {
        if ( pitlane )
        {
            if ( trackDistance < 0 )
                trackDistance = 0;
            
            if ( trackDistance > getPitlaneLength() )
                trackDistance = getPitlaneLength();
        }
        else
        {
            while ( trackDistance < 0f )
                trackDistance += getTrackLength();
            
            trackDistance = trackDistance % getTrackLength();
        }
        
        int numWaypoints = getNumWaypoints( pitlane );
        float tl = pitlane ? getPitlaneLength() : getTrackLength();
        int waypointIndex = (int)( ( numWaypoints - 1 ) * trackDistance / tl );
        
        Waypoint wp0 = getWaypoint( pitlane, waypointIndex );
        while ( ( waypointIndex > 0 ) && ( wp0.getLapDistance() > trackDistance ) )
        {
            wp0 = getWaypoint( pitlane, --waypointIndex );
        }
        while ( ( waypointIndex < numWaypoints - 1 ) && ( getWaypoint( pitlane, waypointIndex + 1 ).getLapDistance() < trackDistance ) )
        {
            wp0 = getWaypoint( pitlane, ++waypointIndex );
        }
        
        Waypoint wp1;
        float alpha;
        if ( wp0.getLapDistance() > trackDistance )
        {
            wp1 = getWaypoint( pitlane, waypointIndex );
            wp0 = getWaypoint( pitlane, numWaypoints - 1 );
            float delta = wp0.getLapDistance() + ( tl - wp1.getLapDistance() );
            alpha = ( ( tl - wp1.getLapDistance() ) + trackDistance ) / delta;
        }
        else if ( waypointIndex == numWaypoints - 1 )
        {
            wp1 = getWaypoint( pitlane, 0 );
            float delta = ( tl - wp0.getLapDistance() ) + wp1.getLapDistance();
            alpha = ( trackDistance - wp0.getLapDistance() ) / delta;
        }
        else
        {
            wp1 = getWaypoint( pitlane, waypointIndex + 1 );
            float delta = wp1.getLapDistance() - wp0.getLapDistance();
            alpha = ( trackDistance - wp0.getLapDistance() ) / delta;
        }
        
        float vecX = wp1.getPosX() - wp0.getPosX();
        float vecZ = wp1.getPosZ() - wp0.getPosZ();
        
        point.setLocation( ( -getMinXPos() + wp0.getPosX() + ( vecX * alpha ) ) * scale, ( -getMinZPos() + wp0.getPosZ() + ( vecZ * alpha ) ) * scale );
    }
    
    /**
     * Gets an interpolated vector along the track at the given waypoint.
     * 
     * @param pitlane waypoint of main track or pitlane?
     * @param trackDistance the distance along the track in meters
     * @param vector output buffer
     * 
     * @return success?
     */
    public final boolean getInterpolatedVector( boolean pitlane, float trackDistance, TelemVect3 vector )
    {
        //if ( ( trackDistance < 0f ) || ( trackDistance > trackLength ) )
        //    return ( false );
        if ( pitlane )
        {
            if ( trackDistance < 0 )
                trackDistance = 0;
            
            if ( trackDistance > getPitlaneLength() )
                trackDistance = getPitlaneLength();
        }
        else
        {
            while ( trackDistance < 0f )
                trackDistance += getTrackLength();
            
            trackDistance = trackDistance % getTrackLength();
        }
        
        int numWaypoints = getNumWaypoints( pitlane );
        float tl = pitlane ? getPitlaneLength() : getTrackLength();
        int waypointIndex = (int)( ( numWaypoints - 1 ) * trackDistance / tl );
        
        Waypoint wp0 = getWaypoint( pitlane, waypointIndex );
        while ( ( waypointIndex > 0 ) && ( wp0.getLapDistance() > trackDistance ) )
        {
            wp0 = getWaypoint( pitlane, --waypointIndex );
        }
        while ( ( waypointIndex < numWaypoints - 1 ) && ( getWaypoint( pitlane, waypointIndex + 1 ).getLapDistance() < trackDistance ) )
        {
            wp0 = getWaypoint( pitlane, ++waypointIndex );
        }
        
        Waypoint wp1;
        float alpha;
        if ( wp0.getLapDistance() > trackDistance )
        {
            wp1 = getWaypoint( pitlane, waypointIndex );
            wp0 = getWaypoint( pitlane, numWaypoints - 1 );
            float delta = wp0.getLapDistance() + ( getTrackLength() - wp1.getLapDistance() );
            alpha = ( ( getTrackLength() - wp1.getLapDistance() ) + trackDistance ) / delta;
        }
        else if ( waypointIndex == numWaypoints - 1 )
        {
            wp1 = getWaypoint( pitlane, 0 );
            float delta = ( getTrackLength() - wp0.getLapDistance() ) + wp1.getLapDistance();
            alpha = ( trackDistance - wp0.getLapDistance() ) / delta;
        }
        else
        {
            wp1 = getWaypoint( pitlane, waypointIndex + 1 );
            float delta = wp1.getLapDistance() - wp0.getLapDistance();
            alpha = ( trackDistance - wp0.getLapDistance() ) / delta;
        }
        
        float beta = 1f - alpha;
        
        float vecX = ( wp0.getVecX() * beta ) + ( wp1.getVecX() * alpha );
        float vecY = ( wp0.getVecY() * beta ) + ( wp1.getVecY() * alpha );
        float vecZ = ( wp0.getVecZ() * beta ) + ( wp1.getVecZ() * alpha );
        
        __GDPrivilegedAccess.setTelemVect3( vecX, vecY, vecZ, vector );
        
        return ( true );
    }
    
    private final TelemVect3 vec1 = new TelemVect3();
    
    /**
     * Gets the interpolated angle of the viewed vehicle to the road.
     * 
     * @param scoringInfo
     * 
     * @return the interpolated angle of the viewed vehicle to the road.
     */
    public final float getInterpolatedAngleToRoad( ScoringInfo scoringInfo )
    {
        VehicleScoringInfo vsi = scoringInfo.getViewedVehicleScoringInfo();
        float lapDistance = vsi.getLapDistance();
        
        getInterpolatedVector( vsi.isInPits(), lapDistance, vec1 );
        
        if ( vec1.getX() == 0f )
        {
            if ( vec1.getZ() < 0f )
                return ( 0f );
            
            return ( (float)Math.PI );
        }
        
        float dot = vec1.getX() * 0f + vec1.getZ() * -1f;
        float angle = (float)Math.abs( Math.atan2( vec1.getX() * -1f - vec1.getZ() * 0f, dot ) );
        
        if ( vec1.getX() < 0f )
            return ( angle );
        
        return ( -angle );
    }
}
