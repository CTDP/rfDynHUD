package net.ctdp.rfdynhud.util;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.TelemVect3;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

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
    private final float sector1Length, sector2Length, trackLength;
    private final float minXPos, maxXPos, minYPos, maxYPos, minZPos, maxZPos;
    private final float maxWidth;
    
    public final float getSector1Length()
    {
        return ( sector1Length );
    }
    
    public final float getSector2Length()
    {
        return ( sector2Length );
    }
    
    public final float getSector3Length()
    {
        return ( trackLength - sector2Length );
    }
    
    public final float getTrackLength()
    {
        return ( trackLength );
    }
    
    public float getScale( int targetWidth, int targetHeight )
    {
        float scaleX = targetWidth / Math.abs( maxXPos - minXPos );
        float scaleZ = targetHeight / Math.abs( maxZPos - minZPos );
        
        return ( Math.min( scaleX, scaleZ ) );
    }
    
    public final int getXExtend( float scale )
    {
        return ( Math.round( Math.abs( maxXPos - minXPos ) * scale ) );
    }
    
    public final int getYExtend( float scale )
    {
        return ( Math.round( Math.abs( maxYPos - minYPos ) * scale ) );
    }
    
    public final int getZExtend( float scale )
    {
        return ( Math.round( Math.abs( maxZPos - minZPos ) * scale ) );
    }
    
    public final int getMaxTrackWidth( float scale )
    {
        return ( Math.round( maxWidth * scale ) );
    }
    
    /**
     * @param pitlane
     */
    public final int getNumWaypoints( boolean pitlane )
    {
        if ( pitlane )
            return ( waypointsPitlane.length );
        
        return ( waypointsTrack.length );
    }
    
    /**
     * @param pitlane
     * @param waypointIndex
     * @param position
     */
    public final byte getWaypointSector( boolean pitlane, int waypointIndex )
    {
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        return ( wp.sector );
    }
    
    /**
     * @param pitlane
     * @param waypointIndex
     * @param position
     */
    public final void getWaypointPosition( boolean pitlane, int waypointIndex, TelemVect3 position )
    {
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        __GDPrivilegedAccess.setTelemVect3( wp.posX, wp.posY, wp.posZ, position );
    }
    
    /**
     * @param pitlane
     * @param waypointIndex
     * @param scale
     * @param point
     */
    public final void getWaypointPosition( boolean pitlane, int waypointIndex, float scale, Point point )
    {
        Waypoint wp = pitlane ? waypointsPitlane[waypointIndex] : waypointsTrack[waypointIndex];
        
        point.setLocation( Math.round( ( -minXPos + wp.posX ) * scale ), Math.round( ( -minZPos + wp.posZ ) * scale ) );
    }
    
    /**
     * @param pitlane
     * @param trackDistance
     * @param scale
     * @param point
     * @return success?
     */
    public final boolean getInterpolatedPosition( boolean pitlane, float trackDistance, float scale, Point point )
    {
        if ( ( trackDistance < 0f ) || ( trackDistance > trackLength ) )
            return ( false );
        
        Waypoint[] waypoints = pitlane ? waypointsPitlane : waypointsTrack;
        
        int waypointIndex = (int)( ( waypoints.length - 1 ) * trackDistance / trackLength );
        
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
        
        float vecX = wp1.posX - wp0.posX;
        float vecZ = wp1.posZ - wp0.posZ;
        
        point.setLocation( Math.round( ( -minXPos + wp0.posX + ( vecX * alpha ) ) * scale ), Math.round( ( -minZPos + wp0.posZ + ( vecZ * alpha ) ) * scale ) );
        
        return ( true );
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
                    
                    if ( ( currentWP.sector == 1 ) && ( pc.firstOfFirstSector[branchID] == -1 ) )
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
    
    protected Track( Waypoint[] waypointsTrack, Waypoint[] waypointsPitlane, float sector1Length, float sector2Length, float trackLength, float minXPos, float maxXPos, float minYPos, float maxYPos, float minZPos, float maxZPos, float maxWidth )
    {
        this.waypointsTrack = waypointsTrack;
        this.waypointsPitlane = waypointsPitlane;
        this.sector1Length = sector1Length;
        this.sector2Length = sector2Length;
        this.trackLength = trackLength;
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
            
            // rotate clockwisely by 90°
            float tmp = wp.posX;
            wp.posX = wp.posZ;
            wp.posZ = -tmp;
            
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
    
    public static Track parseTrackFromAIW( File aiw )
    {
        try
        {
            ParseContainer pc = parseAIW( aiw );
            
            Waypoint[] waypointsTrack = new Waypoint[ pc.numWaypoints[0] ];
            System.arraycopy( pc.waypoints[0], pc.firstOfFirstSector[0], waypointsTrack, 0, waypointsTrack.length - pc.firstOfFirstSector[0] );
            System.arraycopy( pc.waypoints[0], 0, waypointsTrack, waypointsTrack.length - pc.firstOfFirstSector[0], pc.firstOfFirstSector[0] );
            
            Waypoint[] waypointsPitlane = new Waypoint[ pc.numWaypoints[1] ];
            System.arraycopy( pc.waypoints[1], pc.firstOfFirstSector[1], waypointsPitlane, 0, waypointsPitlane.length - pc.firstOfFirstSector[1] );
            System.arraycopy( pc.waypoints[1], 0, waypointsPitlane, waypointsPitlane.length - pc.firstOfFirstSector[1], pc.firstOfFirstSector[1] );
            
            pc.minXPos = Float.MAX_VALUE;
            pc.maxXPos = -Float.MAX_VALUE;
            pc.minZPos = Float.MAX_VALUE;
            pc.maxZPos = -Float.MAX_VALUE;
            
            fixWaypoints( waypointsTrack, pc );
            fixWaypoints( waypointsPitlane, pc );
            
            return ( new Track( waypointsTrack, waypointsPitlane, pc.sector1Length, pc.sector2Length, pc.trackLength, pc.minXPos, pc.maxXPos, pc.minYPos, pc.maxYPos, pc.minZPos, pc.maxZPos, pc.maxWidth ) );
        }
        catch ( IOException e )
        {
            Logger.log( e );
            return ( null );
        }
    }
}
