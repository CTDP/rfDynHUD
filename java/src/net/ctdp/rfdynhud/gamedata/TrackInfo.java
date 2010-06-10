package net.ctdp.rfdynhud.gamedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import net.ctdp.rfdynhud.util.Logger;

public class TrackInfo
{
    public static final File LOCATIONS_FOLDER = GameFileSystem.getPathFromGameConfigINI( GameFileSystem.GAME_FOLDER, "TracksDir", "GameData\\Locations\\" );
    
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
     * 
     * @param trackname
     * 
     * @return the first matching folder (or null, if not found, but shouldn't happen).
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
    
    public final File getTrackFolder()
    {
        return ( trackFolder );
    }
    
    public final File getSceneFile()
    {
        return ( profileInfo.getLastUsedSceneFile() );
    }
    
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
                this.lastTrack = Track.parseTrackFromAIW( aiw );
            }
        }
        
        return ( lastTrack );
    }
    
    public TrackInfo( ProfileInfo profileInfo )
    {
        this.profileInfo = profileInfo;
    }
}
