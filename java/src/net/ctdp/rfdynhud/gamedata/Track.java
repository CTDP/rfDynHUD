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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Model of a track with waypoints and utility methods
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Track
{
    @SuppressWarnings( "unused" )
    protected static final class Waypoint
    {
        private float posX;
        private float posY;
        private float posZ;
        
        private float vecX;
        private float vecY;
        private float vecZ;
        
        private float normX;
        private float normY;
        private float normZ;
        
        private float roadLeft;
        private float roadRight;
        private float farLeft;
        private float farRight;
        
        private byte sector;
        private float lapDistance;
        private float normalizedLapDistance;
        
        public final float getRoadWidth()
        {
            return ( Math.abs( roadLeft - roadRight ) );
        }
    }
    
    private final Waypoint[] waypointsTrack;
    private final Waypoint[] waypointsPitlane;
    private final float sector1Length, sector2Length, trackLength, pitlaneLength;
    private final float minXPos, maxXPos, minYPos, maxYPos, minZPos, maxZPos;
    private final float maxWidth;
    
    /**
     * Gets the length of sector 1 in meters.
     * 
     * @return the length of sector 1 in meters.
     */
    public final float getSector1Length()
    {
        return ( sector1Length );
    }
    
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
            return ( sector2Length );
        
        return ( sector2Length - sector1Length );
    }
    
    /**
     * Gets the length of sector 3 in meters.
     * 
     * @return the length of sector 3 in meters.
     */
    public final float getSector3Length()
    {
        return ( trackLength - sector2Length );
    }
    
    /**
     * Gets the track length in meters.
     * 
     * @return the track length in meters.
     */
    public final float getTrackLength()
    {
        return ( trackLength );
    }
    
    public final float getPitlaneLength()
    {
        return ( pitlaneLength );
    }
    
    public final float getMinXPos()
    {
        return ( minXPos );
    }
    
    public final float getMinYPos()
    {
        return ( minYPos );
    }
    
    public final float getMinZPos()
    {
        return ( minZPos );
    }
    
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
        float scaleX = targetWidth / Math.abs( maxXPos - minXPos );
        float scaleZ = targetHeight / Math.abs( maxZPos - minZPos );
        
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
        return ( Math.round( Math.abs( maxXPos - minXPos ) * scale ) );
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
        return ( Math.round( Math.abs( maxYPos - minYPos ) * scale ) );
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
        return ( Math.round( Math.abs( maxZPos - minZPos ) * scale ) );
    }
    
    /**
     * Gets the maximum track width.
     * 
     * @param scale the scale. See {@link #getScale(int, int)}
     * 
     * @return the maximum track width.
     */
    public final int getMaxTrackWidth( float scale )
    {
        return ( Math.round( maxWidth * scale ) );
    }
    
    /**
     * Gets the number of waypoints of the main track or the pitlane.
     * 
     * @param pitlane waypoints of main track or pitlane?
     * 
     * @return the number of waypoints of the main track or the pitlane.
     */
    public final int getNumWaypoints( boolean pitlane )
    {
        if ( pitlane )
            return ( waypointsPitlane.length );
        
        return ( waypointsTrack.length );
    }
    
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
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        return ( wp.sector );
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
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        __GDPrivilegedAccess.setTelemVect3( wp.posX, wp.posY, wp.posZ, position );
    }
    
    /**
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * @param scale the scale. See {@link #getScale(int, int)}
     * @param point output buffer
     */
    public final void getWaypointPosition( boolean pitlane, int waypointIndex, float scale, Point point )
    {
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        point.setLocation( Math.round( ( -minXPos + wp.posX ) * scale ), Math.round( ( -minZPos + wp.posZ ) * scale ) );
    }
    
    /**
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * @param scale the scale. See {@link #getScale(int, int)}
     * @param point output buffer
     */
    public final void getWaypointPosition( boolean pitlane, int waypointIndex, float scale, Point2D.Float point )
    {
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        point.setLocation( ( -minXPos + wp.posX ) * scale, ( -minZPos + wp.posZ ) * scale );
    }
    
    /**
     * @param pitlane waypoint of main track or pitlane?
     * @param waypointIndex the index of the waypoint
     * @param vector output buffer
     */
    public final void getWaypointVector( boolean pitlane, int waypointIndex, Point2D.Float vector )
    {
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        vector.setLocation( wp.vecX, wp.vecZ );
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
            
            if ( trackDistance > pitlaneLength )
                trackDistance = pitlaneLength;
        }
        else
        {
            while ( trackDistance < 0f )
                trackDistance += trackLength;
            
            trackDistance = trackDistance % trackLength;
        }
        
        Waypoint[] waypoints = pitlane ? waypointsPitlane : waypointsTrack;
        float tl = pitlane ? pitlaneLength : trackLength;
        int waypointIndex = (int)( ( waypoints.length - 1 ) * trackDistance / tl );
        
        Waypoint wp0 = waypoints[waypointIndex];
        while ( ( waypointIndex > 0 ) && ( wp0.lapDistance > trackDistance ) )
        {
            wp0 = waypoints[--waypointIndex];
        }
        while ( ( waypointIndex < waypoints.length - 1 ) && ( waypoints[waypointIndex + 1].lapDistance < trackDistance ) )
        {
            wp0 = waypoints[++waypointIndex];
        }
        
        Waypoint wp1;
        float alpha;
        if ( wp0.lapDistance > trackDistance )
        {
            wp1 = waypoints[waypointIndex];
            wp0 = waypoints[waypoints.length - 1];
            float delta = wp0.lapDistance + ( tl - wp1.lapDistance );
            alpha = ( ( tl - wp1.lapDistance ) + trackDistance ) / delta;
        }
        else if ( waypointIndex == waypoints.length - 1 )
        {
            wp1 = waypoints[0];
            float delta = ( tl - wp0.lapDistance ) + wp1.lapDistance;
            alpha = ( trackDistance - wp0.lapDistance ) / delta;
        }
        else
        {
            wp1 = waypoints[waypointIndex + 1];
            float delta = wp1.lapDistance - wp0.lapDistance;
            alpha = ( trackDistance - wp0.lapDistance ) / delta;
        }
        
        float vecX = wp1.posX - wp0.posX;
        float vecZ = wp1.posZ - wp0.posZ;
        
        point.setLocation( ( -minXPos + wp0.posX + ( vecX * alpha ) ) * scale, ( -minZPos + wp0.posZ + ( vecZ * alpha ) ) * scale );
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
            
            if ( trackDistance > pitlaneLength )
                trackDistance = pitlaneLength;
        }
        else
        {
            while ( trackDistance < 0f )
                trackDistance += trackLength;
            
            trackDistance = trackDistance % trackLength;
        }
        
        Waypoint[] waypoints = pitlane ? waypointsPitlane : waypointsTrack;
        float tl = pitlane ? pitlaneLength : trackLength;
        int waypointIndex = (int)( ( waypoints.length - 1 ) * trackDistance / tl );
        
        Waypoint wp0 = waypoints[waypointIndex];
        while ( ( waypointIndex > 0 ) && ( wp0.lapDistance > trackDistance ) )
        {
            wp0 = waypoints[--waypointIndex];
        }
        while ( ( waypointIndex < waypoints.length - 1 ) && ( waypoints[waypointIndex + 1].lapDistance < trackDistance ) )
        {
            wp0 = waypoints[++waypointIndex];
        }
        
        Waypoint wp1;
        float alpha;
        if ( wp0.lapDistance > trackDistance )
        {
            wp1 = waypoints[waypointIndex];
            wp0 = waypoints[waypoints.length - 1];
            float delta = wp0.lapDistance + ( trackLength - wp1.lapDistance );
            alpha = ( ( trackLength - wp1.lapDistance ) + trackDistance ) / delta;
        }
        else if ( waypointIndex == waypoints.length - 1 )
        {
            wp1 = waypoints[0];
            float delta = ( trackLength - wp0.lapDistance ) + wp1.lapDistance;
            alpha = ( trackDistance - wp0.lapDistance ) / delta;
        }
        else
        {
            wp1 = waypoints[waypointIndex + 1];
            float delta = wp1.lapDistance - wp0.lapDistance;
            alpha = ( trackDistance - wp0.lapDistance ) / delta;
        }
        
        float beta = 1f - alpha;
        
        float vecX = ( wp0.vecX * beta ) + ( wp1.vecX * alpha );
        float vecY = ( wp0.vecY * beta ) + ( wp1.vecY * alpha );
        float vecZ = ( wp0.vecZ * beta ) + ( wp1.vecZ * alpha );
        
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
    
    private static final class ParseContainer
    {
        private Waypoint[][] waypoints = null;
        private int[] numWaypoints = null;
        private float trackLength = 1f;
        private float sector1Length = 1f;
        private float sector2Length = 1f;
        private float minXPos = Float.MAX_VALUE, maxXPos = -Float.MAX_VALUE, minYPos = Float.MAX_VALUE, maxYPos = -Float.MAX_VALUE, minZPos = Float.MAX_VALUE, maxZPos = -Float.MAX_VALUE;
        private float maxWidth = -Float.MAX_VALUE;
        private int[] firstOfFirstSector = null;
    }
    
    protected static ParseContainer parseAIW( File aiw ) throws IOException
    {
        final ParseContainer pc = new ParseContainer();
        
        new AbstractIniParser()
        {
            private boolean inWaypointsGroup = false;
            
            private Waypoint currentWP = null;
            private int branchID = -1;
            
            private void storeWaypoint()
            {
                if ( currentWP != null )
                {
                    if ( pc.numWaypoints.length <= branchID )
                    {
                        int[] tmp = new int[ branchID + 1 ];
                        System.arraycopy( pc.numWaypoints, 0, tmp, 0, pc.numWaypoints.length );
                        
                        for ( int i = pc.numWaypoints.length; i <= branchID; i++ )
                        {
                            tmp[i] = 0;
                        }
                        
                        pc.numWaypoints = tmp;
                    }
                    
                    if ( pc.waypoints.length <= branchID )
                    {
                        Waypoint[][] tmp = new Waypoint[ branchID + 1 ][];
                        System.arraycopy( pc.waypoints, 0, tmp, 0, pc.waypoints.length );
                        
                        for ( int i = pc.waypoints.length; i <= branchID; i++ )
                        {
                            tmp[i] = new Waypoint[ pc.waypoints[0].length ];
                        }
                        
                        pc.waypoints = tmp;
                    }
                    
                    if ( pc.firstOfFirstSector.length <= branchID )
                    {
                        int[] tmp = new int[ branchID + 1 ];
                        System.arraycopy( pc.firstOfFirstSector, 0, tmp, 0, pc.firstOfFirstSector.length );
                        
                        for ( int i = pc.firstOfFirstSector.length; i <= branchID; i++ )
                        {
                            tmp[i] = -1;
                        }
                        
                        pc.firstOfFirstSector = tmp;
                    }
                    
                    if ( ( currentWP.sector == 1 ) && ( ( pc.firstOfFirstSector[branchID] == -1 ) || ( currentWP.lapDistance < pc.waypoints[branchID][pc.firstOfFirstSector[branchID]].lapDistance ) ) )
                        pc.firstOfFirstSector[branchID] = pc.numWaypoints[branchID];
                    
                    pc.waypoints[branchID][pc.numWaypoints[branchID]++] = currentWP;
                    
                    currentWP = null;
                }
                
                branchID = -1;
            }
            
            @Override
            protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
            {
                inWaypointsGroup = group.equalsIgnoreCase( "Waypoint" );
                
                return ( true );
            }
            
            private void parsePosition( String value, Waypoint wp )
            {
                int p0 = 1;
                int p1 = value.indexOf( ',', p0 );
                wp.posX = Float.parseFloat( value.substring( p0, p1 ) );
                p0 = p1 + 1;
                p1 = value.indexOf( ',', p0 );
                wp.posY = Float.parseFloat( value.substring( p0, p1 ) );
                wp.posZ = Float.parseFloat( value.substring( p1 + 1, value.length() - 1 ) );
                
                if ( wp.posX < pc.minXPos )
                    pc.minXPos = wp.posX;
                if ( wp.posX > pc.maxXPos )
                    pc.maxXPos = wp.posX;
                if ( wp.posY < pc.minYPos )
                    pc.minYPos = wp.posY;
                if ( wp.posY > pc.maxYPos )
                    pc.maxYPos = wp.posY;
                if ( wp.posZ < pc.minZPos )
                    pc.minZPos = wp.posZ;
                if ( wp.posZ > pc.maxZPos )
                    pc.maxZPos = wp.posZ;
            }
            
            private void parseNormal( String value, Waypoint wp )
            {
                int p0 = 1;
                int p1 = value.indexOf( ',', p0 );
                wp.normX = Float.parseFloat( value.substring( p0, p1 ) );
                p0 = p1 + 1;
                p1 = value.indexOf( ',', p0 );
                wp.normY = Float.parseFloat( value.substring( p0, p1 ) );
                wp.normZ = Float.parseFloat( value.substring( p1 + 1, value.length() - 1 ) );
            }
            
            private void parseVector( String value, Waypoint wp )
            {
                int p0 = 1;
                int p1 = value.indexOf( ',', p0 );
                wp.vecX = Float.parseFloat( value.substring( p0, p1 ) );
                p0 = p1 + 1;
                p1 = value.indexOf( ',', p0 );
                wp.vecY = Float.parseFloat( value.substring( p0, p1 ) );
                wp.vecZ = Float.parseFloat( value.substring( p1 + 1, value.length() - 1 ) );
            }
            
            private void parseWidth( String value, Waypoint wp )
            {
                int p0 = 1;
                int p1 = value.indexOf( ',', p0 );
                wp.roadLeft = Float.parseFloat( value.substring( p0, p1 ) );
                p0 = p1 + 1;
                p1 = value.indexOf( ',', p0 );
                wp.roadRight = Float.parseFloat( value.substring( p0, p1 ) );
                p0 = p1 + 1;
                p1 = value.indexOf( ',', p0 );
                wp.farLeft = Float.parseFloat( value.substring( p0, p1 ) );
                wp.farRight = Float.parseFloat( value.substring( p1 + 1, value.length() - 1 ) );
                
                float width = wp.getRoadWidth();
                if ( width > pc.maxWidth )
                    pc.maxWidth = width;
            }
            
            private void parseScore( String value, Waypoint wp )
            {
                int p0 = 1;
                int p1 = value.indexOf( ',', p0 );
                wp.sector = (byte)( Byte.parseByte( value.substring( p0, p1 ) ) + 1 );
                wp.lapDistance = Float.parseFloat( value.substring( p1 + 1, value.length() - 1 ) );
                wp.normalizedLapDistance = wp.lapDistance / pc.trackLength;
            }
            
            @Override
            protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
            {
                if ( !inWaypointsGroup )
                    return ( true );
                
                if ( key.equals( "wp_pos" ) )
                {
                    storeWaypoint();
                    
                    currentWP = new Waypoint();
                    parsePosition( value, currentWP );
                }
                else if ( key.equals( "wp_normal" ) )
                {
                    parseNormal( value, currentWP );
                }
                else if ( key.equals( "wp_vect" ) )
                {
                    parseVector( value, currentWP );
                }
                else if ( key.equals( "wp_width" ) )
                {
                    parseWidth( value, currentWP );
                }
                else if ( key.equals( "wp_score" ) )
                {
                    parseScore( value, currentWP );
                }
                else if ( key.equals( "wp_branchID" ) )
                {
                    branchID = Integer.parseInt( value.substring( 1, value.length() - 1 ) );
                }
                else if ( ( pc.numWaypoints == null ) || ( pc.numWaypoints[0] == 0 ) )
                {
                    if ( key.equals( "number_waypoints" ) )
                    {
                        pc.waypoints = new Waypoint[ 1 ][ Integer.parseInt( value ) ];
                        pc.numWaypoints = new int[ 1 ];
                        pc.numWaypoints[0] = 0;
                        pc.firstOfFirstSector = new int[ 1 ];
                        pc.firstOfFirstSector[0] = -1;
                    }
                    else if ( key.equals( "lap_length" ) )
                        pc.trackLength = Float.parseFloat( value );
                    else if ( key.equals( "sector_1_length" ) )
                        pc.sector1Length = Float.parseFloat( value );
                    else if ( key.equals( "sector_2_length" ) )
                        pc.sector2Length = Float.parseFloat( value );
                }
                
                return ( true );
            }
            
            @Override
            protected void onParsingFinished() throws ParsingException
            {
                if ( pc.waypoints == null )
                    return;
                
                storeWaypoint();
                
                for ( int i = 0; i < pc.firstOfFirstSector.length; i++ )
                {
                    if ( pc.firstOfFirstSector[i] == -1 )
                        pc.firstOfFirstSector[i] = 0;
                }
            }
        }.parse( aiw );
        
        return ( pc );
    }
    
    protected Track( Waypoint[] waypointsTrack, Waypoint[] waypointsPitlane, float sector1Length, float sector2Length, float trackLength, float pitlaneLength, float minXPos, float maxXPos, float minYPos, float maxYPos, float minZPos, float maxZPos, float maxWidth )
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
    
    private static void fixWaypoints( Waypoint[] waypoints, ParseContainer pc )
    {
        for ( int i = 0; i < waypoints.length; i++ )
        {
            Waypoint wp = waypoints[i];
            
            // mirror the x-coordinates
            wp.posX *= -1f;
            wp.vecX *= -1f;
            wp.normX *= -1f;
            //wp.posZ *= -1f;
            
            /*
            //wp.normX *= -1f;
            
            // rotate clockwisely by 90°
            float tmp = wp.posX;
            wp.posX = wp.posZ;
            wp.posZ = -tmp;
            tmp = wp.vecX;
            wp.vecX = wp.vecZ;
            wp.vecZ = -tmp;
            tmp = wp.normX;
            wp.normX = wp.normZ;
            wp.normZ = -tmp;
            
            // Fix vector to face into the direction of the road (but not backwards)
            wp.vecX *= -1f;
            wp.vecZ *= -1f;
            */
            
            if ( wp.posX < pc.minXPos )
                pc.minXPos = wp.posX;
            if ( wp.posX > pc.maxXPos )
                pc.maxXPos = wp.posX;
            if ( wp.posZ < pc.minZPos )
                pc.minZPos = wp.posZ;
            if ( wp.posZ > pc.maxZPos )
                pc.maxZPos = wp.posZ;
        }
    }
    
    private static Waypoint[] fixOrder( Waypoint[] waypoints, int numWaypoints, int firstOfFirstSector )
    {
        Waypoint[] result = new Waypoint[ numWaypoints ];
        
        System.arraycopy( waypoints, firstOfFirstSector, result, 0, numWaypoints - firstOfFirstSector );
        System.arraycopy( waypoints, 0, result, numWaypoints - firstOfFirstSector, firstOfFirstSector );
        
        return ( result );
    }
    
    /**
     * Parses an AIW file and returns a {@link Track} instance.
     * 
     * @param aiw the AIW file to parse
     * 
     * @return a {@link Track} instance for the parsed AIW file.
     * 
     * @throws IOException if there's something wrong with the file (missing, not readable, etc.).
     */
    public static Track parseTrackFromAIW( File aiw ) throws IOException
    {
        ParseContainer pc = parseAIW( aiw );
        
        Waypoint[] waypointsTrack = fixOrder( pc.waypoints[0], pc.numWaypoints[0], pc.firstOfFirstSector[0] );
        Waypoint[] waypointsPitlane = fixOrder( pc.waypoints[1], pc.numWaypoints[1], pc.firstOfFirstSector[1] );
        
        pc.minXPos = Float.MAX_VALUE;
        pc.maxXPos = -Float.MAX_VALUE;
        pc.minZPos = Float.MAX_VALUE;
        pc.maxZPos = -Float.MAX_VALUE;
        
        fixWaypoints( waypointsTrack, pc );
        fixWaypoints( waypointsPitlane, pc );
        
        float maxWPLDPit = -Float.MIN_VALUE;
        for ( int i = 0; i < waypointsPitlane.length; i++ )
        {
            if ( waypointsPitlane[i].lapDistance > maxWPLDPit )
            {
                maxWPLDPit = waypointsPitlane[i].lapDistance;
            }
        }
        
        return ( new Track( waypointsTrack, waypointsPitlane, pc.sector1Length, pc.sector2Length, pc.trackLength, maxWPLDPit, pc.minXPos, pc.maxXPos, pc.minYPos, pc.maxYPos, pc.minZPos, pc.maxZPos, pc.maxWidth ) );
    }
}
