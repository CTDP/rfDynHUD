package net.ctdp.rfdynhud.gamedata;

import java.io.File;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.util.RFactorEventsManager;
import net.ctdp.rfdynhud.util.RFactorTools;
import net.ctdp.rfdynhud.util.Track;

/**
 * 
 * 
 * @author Marvin Froehlich
 */
public class LiveGameData
{
    private boolean realtimeMode = false;
    
    private final VehiclePhysics physics = new VehiclePhysics();
    private VehicleSetup setup = null;
    
    private final TelemetryData telemetryData;
    private final ScoringInfo scoringInfo;
    private final GraphicsInfo graphicsInfo;
    private final CommentaryRequestInfo commentaryInfo;
    
    void setRealtimeMode( boolean realtimeMode )
    {
        this.realtimeMode = realtimeMode;
    }
    
    public final boolean isInRealtimeMode()
    {
        return ( realtimeMode );
    }
    
    public final VehiclePhysics getPhysics()
    {
        return ( physics );
    }
    
    void setSetup( VehicleSetup setup )
    {
        this.setup = setup;
    }
    
    public final VehicleSetup getSetup()
    {
        return ( setup );
    }
    
    public final TelemetryData getTelemetryData()
    {
        return ( telemetryData );
    }
    
    public final ScoringInfo getScoringInfo()
    {
        return ( scoringInfo );
    }
    
    public final GraphicsInfo getGraphicsInfo()
    {
        return ( graphicsInfo );
    }
    
    public final CommentaryRequestInfo getCommentaryRequestInfo()
    {
        return ( commentaryInfo );
    }
    
    private String lastTrackName = null;
    private File lastTrackFolder = null;
    private int lastTrackRaceLaps = -1;
    private File lastAIWFile = null;
    private Track lastTrack = null;
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        telemetryData.applyEditorPresets( editorPresets );
        scoringInfo.applyEditorPresets( editorPresets );
        setup.applyEditorPresets( editorPresets );
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
    public File getTrackFolder( String trackname )
    {
        if ( trackname.equals( lastTrackName ) && ( lastTrackFolder != null ) )
            return ( lastTrackFolder );
        
        lastTrackFolder = null;
        
        lastTrackFolder = RFactorTools.findTrackFolder( trackname );
        
        if ( lastTrackFolder != null )
            lastTrackName = trackname;
        
        return ( lastTrackFolder );
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
     * @return the first matching folder (or null, if not found, but shouldn't happen).
     */
    public File getTrackFolder()
    {
        String trackname = getScoringInfo().getTrackName();
        
        return ( getTrackFolder( trackname ) );
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
    public int getTrackRaceLaps( String trackname )
    {
        getTrackFolder( trackname );
        
        lastTrackRaceLaps = RFactorTools.getTrackRaceLaps();
        
        return ( lastTrackRaceLaps );
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
     * @return the first matching folder (or null, if not found, but shouldn't happen).
     */
    public int getTrackRaceLaps()
    {
        String trackname = getScoringInfo().getTrackName();
        
        return ( getTrackRaceLaps( trackname ) );
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
    public Track getTrack( File trackFolder )
    {
        File aiw = RFactorTools.findAIWFile( trackFolder );
        
        if ( ( aiw == null ) || !aiw.exists() )
            return ( null );
        
        if ( aiw.equals( lastAIWFile ) && ( lastTrack != null ) )
            return ( lastTrack );
        
        lastAIWFile = aiw;
        lastTrack = Track.parseTrackFromAIW( aiw );
        
        return ( lastTrack );
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
    public Track getTrack()
    {
        File trackFolder = getTrackFolder();
        
        return ( getTrack( trackFolder ) );
    }
    
    public LiveGameData( RFactorEventsManager eventsManager )
    {
        this.telemetryData = new TelemetryData( this, eventsManager );
        this.scoringInfo = new ScoringInfo( this, eventsManager );
        this.graphicsInfo = new GraphicsInfo( this );
        this.commentaryInfo = new CommentaryRequestInfo( this );
        
        eventsManager.setGameData( this );
    }
}
