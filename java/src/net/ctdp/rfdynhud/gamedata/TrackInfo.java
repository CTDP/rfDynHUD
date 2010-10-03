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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.ctdp.rfdynhud.util.Logger;

/**
 * Model of the currently used track
 * 
 * @author Marvin Froehlich
 */
public class TrackInfo
{
    private final ProfileInfo profileInfo;
    
    private File trackFolder = null;
    private String trackName = null;
    private int raceLaps = -1;
    private File aiwFile = null;
    private Track lastTrack = null;
    
    private static Object[] checkGDB( File gdb, String trackname )
    {
        BufferedReader br = null;
        
        try
        {
            boolean trackFound = false;
            boolean raceLapsFound = false;
            
            String trackname2 = null;
            int trackRaceLaps = -1;
            
            br = new BufferedReader( new FileReader( gdb ) );
            
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                if ( line.startsWith( "TrackName" ) )
                {
                    int idx = line.indexOf( '=', 9 );
                    if ( idx >= 0 )
                    {
                        String tn = line.substring( idx + 1 ).trim();
                        
                        if ( ( trackname == null ) || tn.equals( trackname ) )
                        {
                            trackname2 = tn;
                            trackFound = true;
                        }
                    }
                }
                else if ( line.startsWith( "RaceLaps" ) )
                {
                    int idx = line.indexOf( '=', 8 );
                    if ( idx >= 0 )
                    {
                        try
                        {
                            trackRaceLaps = Integer.parseInt( line.substring( idx + 1 ).trim() );
                            raceLapsFound = true;
                        }
                        catch ( Throwable t )
                        {
                        }
                    }
                }
                
                if ( trackFound && raceLapsFound )
                    return ( new Object[] { trackname2, trackRaceLaps } );
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
        finally
        {
            if ( br != null )
                try { br.close(); } catch ( Throwable t ) { Logger.log( t ); }
        }
        
        return ( null );
    }
    
    /*
    private static Object[] searchTrackFolder( File parentDir, String trackname )
    {
        for ( File f : parentDir.listFiles() )
        {
            if ( f.isDirectory() )
            {
                Object[] result = searchTrackFolder( f, trackname );
                if ( result != null )
                    return ( result );
            }
            else if ( f.getName().toLowerCase().endsWith( ".gdb" ) )
            {
                Object[] result = checkGDB( f, trackname );
                if ( result != null )
                    return ( new Object[] { f.getParentFile(), (Integer)result[1] } );
            }
        }
        
        return ( null );
    }
    */
    
    private void readTrackHeader( File trackFolder, String trackname )
    {
        if ( ( trackFolder == null ) || !trackFolder.exists() )
        {
            Logger.log( "WARNING: Track folder not found: " + trackFolder );
            
            this.trackName = "N/A";
            this.raceLaps = 60;
            
            return;
        }
        
        for ( File f : trackFolder.listFiles() )
        {
            if ( f.isFile() && f.getName().toLowerCase().endsWith( ".gdb" ) )
            {
                Object[] result = checkGDB( f, trackname );
                if ( result != null )
                {
                    this.trackName = (String)result[0];
                    this.raceLaps = (Integer)result[1];
                    return;
                }
            }
        }
        
        return;
    }
    
    private void reset()
    {
        this.trackFolder = null;
        this.trackName = null;
        this.raceLaps = -1;
        this.aiwFile = null;
        this.lastTrack = null;
    }
    
    /**
     * <p>
     * Finds the folder from the GameData\Locations folder, in which a .gdb file
     * exists, that contains a line<br>
     *   TrackName = trackname
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     */
    void update()
    {
        File sceneFile = profileInfo.getLastUsedSceneFile();
        
        if ( sceneFile == null )
        {
            reset();
            return;
        }
        
        File trackFolder = sceneFile.getParentFile();
        
        if ( trackFolder.equals( this.trackFolder ) )
            return;
        
        this.trackFolder = trackFolder;
        
        this.trackName = null;
        this.raceLaps = -1;
        
        this.aiwFile = null;
        this.lastTrack = null;
    }
    
    /**
     * Gets the track's folder.
     * 
     * @return the track's folder.
     */
    public final File getTrackFolder()
    {
        return ( trackFolder );
    }
    
    /**
     * Gets the track's scene file.
     * 
     * @return the track's scene file.
     */
    public final File getSceneFile()
    {
        return ( profileInfo.getLastUsedSceneFile() );
    }
    
    /**
     * Gets the track's name.
     * 
     * @return the track's name.
     */
    public final String getTrackName()
    {
        if ( trackName == null )
        {
            readTrackHeader( trackFolder, null );
        }
        
        return ( trackName );
    }
    
    /**
     * Gets last read track race laps.
     * 
     * @return last read track race laps.
     */
    public final int getRaceLaps()
    {
        if ( raceLaps < 0 )
        {
            readTrackHeader( trackFolder, null );
        }
        
        return ( raceLaps );
    }
    
    /**
     * <p>
     * Finds the AIW file for the given track.
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     * 
     * @param trackFolder
     * 
     * @return the AIW file for the given track.
     */
    private static File findAIWFile( File trackFolder )
    {
        if ( trackFolder == null )
            return ( null );
        
        File aiw = null;
        for ( File f : trackFolder.listFiles() )
        {
            if ( !f.isDirectory() && f.getName().toUpperCase().endsWith( ".AIW" ) )
            {
                aiw = f;
                break;
            }
        }
        
        return ( aiw );
    }
    
    /**
     * <p>
     * Finds the AIW file for the given track.
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     * 
     * @return the AIW file for the given track.
     */
    public File getAIWFile()
    {
        if ( aiwFile == null )
        {
            aiwFile = findAIWFile( trackFolder );
        }
        
        return ( aiwFile );
    }
    
    /**
     * <p>
     * Gets the track abstraction (waypoints) of the current track.
     * </p>
     * WARNING:<br>
     * This operation may take a long time.
     * 
     * @return the track abstraction (waypoints) of the current track.
     */
    public final Track getTrack()
    {
        if ( lastTrack == null )
        {
            File aiw = getAIWFile();
            
            if ( aiw != null )
            {
                this.aiwFile = aiw;
                try
                {
                    this.lastTrack = Track.parseTrackFromAIW( aiw );
                }
                catch ( IOException e )
                {
                    Logger.log( e );
                    this.lastTrack = null;
                }
            }
        }
        
        return ( lastTrack );
    }
    
    /**
     * Create a new instance.
     * 
     * @param profileInfo
     */
    public TrackInfo( ProfileInfo profileInfo )
    {
        this.profileInfo = profileInfo;
    }
}
