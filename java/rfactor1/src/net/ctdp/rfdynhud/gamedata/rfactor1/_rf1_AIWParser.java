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

import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.rfactor1._rf1_Track._rf1_Waypoint;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
class _rf1_AIWParser
{
    private static final class ParseContainer
    {
        private _rf1_Waypoint[][] waypoints = null;
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
            
            private _rf1_Waypoint currentWP = null;
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
                        _rf1_Waypoint[][] tmp = new _rf1_Waypoint[ branchID + 1 ][];
                        System.arraycopy( pc.waypoints, 0, tmp, 0, pc.waypoints.length );
                        
                        for ( int i = pc.waypoints.length; i <= branchID; i++ )
                        {
                            tmp[i] = new _rf1_Waypoint[ pc.waypoints[0].length ];
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
            
            private void parsePosition( String value, _rf1_Waypoint wp )
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
            
            private void parseNormal( String value, _rf1_Waypoint wp )
            {
                int p0 = 1;
                int p1 = value.indexOf( ',', p0 );
                wp.normX = Float.parseFloat( value.substring( p0, p1 ) );
                p0 = p1 + 1;
                p1 = value.indexOf( ',', p0 );
                wp.normY = Float.parseFloat( value.substring( p0, p1 ) );
                wp.normZ = Float.parseFloat( value.substring( p1 + 1, value.length() - 1 ) );
            }
            
            private void parseVector( String value, _rf1_Waypoint wp )
            {
                int p0 = 1;
                int p1 = value.indexOf( ',', p0 );
                wp.vecX = Float.parseFloat( value.substring( p0, p1 ) );
                p0 = p1 + 1;
                p1 = value.indexOf( ',', p0 );
                wp.vecY = Float.parseFloat( value.substring( p0, p1 ) );
                wp.vecZ = Float.parseFloat( value.substring( p1 + 1, value.length() - 1 ) );
            }
            
            private void parseWidth( String value, _rf1_Waypoint wp )
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
            
            private void parseScore( String value, _rf1_Waypoint wp )
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
                    
                    currentWP = new _rf1_Waypoint();
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
                        pc.waypoints = new _rf1_Waypoint[ 1 ][ Integer.parseInt( value ) ];
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
    
    private static void fixWaypoints( _rf1_Waypoint[] waypoints, ParseContainer pc )
    {
        for ( int i = 0; i < waypoints.length; i++ )
        {
            _rf1_Waypoint wp = waypoints[i];
            
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
    
    private static _rf1_Waypoint[] fixOrder( _rf1_Waypoint[] waypoints, int numWaypoints, int firstOfFirstSector )
    {
        _rf1_Waypoint[] result = new _rf1_Waypoint[ numWaypoints ];
        
        System.arraycopy( waypoints, firstOfFirstSector, result, 0, numWaypoints - firstOfFirstSector );
        System.arraycopy( waypoints, 0, result, numWaypoints - firstOfFirstSector, firstOfFirstSector );
        
        return ( result );
    }
    
    /**
     * Parses an AIW file and returns a {@link _rf1_Track} instance.
     * 
     * @param aiw the AIW file to parse
     * 
     * @return a {@link _rf1_Track} instance for the parsed AIW file.
     * 
     * @throws IOException if there's something wrong with the file (missing, not readable, etc.).
     */
    public static _rf1_Track parseTrackFromAIW( File aiw ) throws IOException
    {
        ParseContainer pc = parseAIW( aiw );
        
        _rf1_Waypoint[] waypointsTrack = fixOrder( pc.waypoints[0], pc.numWaypoints[0], pc.firstOfFirstSector[0] );
        _rf1_Waypoint[] waypointsPitlane = fixOrder( pc.waypoints[1], pc.numWaypoints[1], pc.firstOfFirstSector[1] );
        
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
        
        return ( new _rf1_Track( waypointsTrack, waypointsPitlane, pc.sector1Length, pc.sector2Length, pc.trackLength, maxWPLDPit, pc.minXPos, pc.maxXPos, pc.minYPos, pc.maxYPos, pc.minZPos, pc.maxZPos, pc.maxWidth ) );
    }
    
    public static void main( String[] args ) throws Throwable
    {
        parseTrackFromAIW( new File( "d:/R03_MoSport.AIW" ) );
    }
}
