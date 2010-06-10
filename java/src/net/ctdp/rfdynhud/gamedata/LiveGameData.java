package net.ctdp.rfdynhud.gamedata;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.widgets.GameResolution;

/**
 * 
 * 
 * @author Marvin Froehlich
 */
public class LiveGameData
{
    private final GameResolution gameResolution;
    
    private boolean realtimeMode = false;
    
    private final VehiclePhysics physics = new VehiclePhysics();
    private VehicleSetup setup = null;
    
    private final TelemetryData telemetryData;
    private final ScoringInfo scoringInfo;
    private final GraphicsInfo graphicsInfo;
    private final CommentaryRequestInfo commentaryInfo;
    
    private final ProfileInfo profileInfo;
    private final ModInfo modInfo;
    private final TrackInfo trackInfo;
    
    public final GameResolution getGameResolution()
    {
        return ( gameResolution );
    }
    
    void setRealtimeMode( boolean realtimeMode, EditorPresets editorPresets )
    {
        boolean was = this.realtimeMode;
        
        this.realtimeMode = realtimeMode;
        
        if ( !was && realtimeMode )
        {
            getTelemetryData().onRealtimeEntered( editorPresets );
            getScoringInfo().onRealtimeEntered( editorPresets );
            getSetup().onRealtimeEntered();
        }
        else if ( was && !realtimeMode )
        {
            getTelemetryData().onRealtimeExited( editorPresets );
            getScoringInfo().onRealtimeExited( editorPresets );
            getSetup().onRealtimeExited();
        }
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
    
    public final ModInfo getModInfo()
    {
        return ( modInfo );
    }
    
    public final ProfileInfo getProfileInfo()
    {
        return ( profileInfo );
    }
    
    public final TrackInfo getTrackInfo()
    {
        return ( trackInfo );
    }
    
    void applyEditorPresets( EditorPresets editorPresets )
    {
        telemetryData.applyEditorPresets( editorPresets );
        scoringInfo.applyEditorPresets( editorPresets );
        setup.applyEditorPresets( editorPresets );
    }
    
    public LiveGameData( GameResolution gameResolution, GameEventsManager eventsManager )
    {
        this.gameResolution = gameResolution;
        this.telemetryData = new TelemetryData( this, eventsManager );
        this.scoringInfo = new ScoringInfo( this, eventsManager );
        this.graphicsInfo = new GraphicsInfo( this );
        this.commentaryInfo = new CommentaryRequestInfo( this );
        
        this.profileInfo = new ProfileInfo();
        this.modInfo = new ModInfo( profileInfo );
        this.trackInfo = new TrackInfo( profileInfo );
        
        eventsManager.setGameData( this );
    }
}
