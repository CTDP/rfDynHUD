package net.ctdp.rfdynhud.gamedata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.util.PluginINI;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.classes.ClassSearcher;
import org.jagatoo.util.classes.SuperClassCriterium;

public abstract class _LiveGameDataObjectsFactory
{
    public abstract String getGameId();
    
    public abstract GameFileSystem newGameFileSystem( PluginINI pluginIni );
    
    public abstract ProfileInfo newProfileInfo( LiveGameData gameData );
    
    public abstract ModInfo newModInfo( LiveGameData gameData );
    
    public abstract TrackInfo newTrackInfo( LiveGameData gameData );
    
    public abstract VehicleInfo newVehicleInfo();
    
    public abstract void parseVehicleInfo( File file, String filename, VehicleInfo info ) throws IOException;
    
    public abstract _CommentaryRequestInfoCapsule newCommentaryRequestInfoCapsule( LiveGameData gameData );
    
    public abstract _GraphicsInfoCapsule newGraphicsInfoCapsule( LiveGameData gameData );
    
    public abstract _TelemetryDataCapsule newTelemetryDataCapsule( LiveGameData gameData );
    
    public abstract _ScoringInfoCapsule newScoringInfoCapsule( LiveGameData gameData );
    
    public abstract _VehicleScoringInfoCapsule newVehicleScoringInfoCapsule( LiveGameData gameData );
    
    private static final HashMap<String, _LiveGameDataObjectsFactory> gdFactoriesMap = new HashMap<String, _LiveGameDataObjectsFactory>();
    
    public static synchronized _LiveGameDataObjectsFactory get( String gameId )
    {
        if ( gdFactoriesMap.containsKey( gameId ) )
            return ( gdFactoriesMap.get( gameId ) );
        
        List<Class<?>> classes = ClassSearcher.findClasses( RFDynHUD.class.getClassLoader(), new SuperClassCriterium( _LiveGameDataObjectsFactory.class, false ), (String[])null );
        
        for ( Class<?> clazz : classes )
        {
            _LiveGameDataObjectsFactory f = null;
            try
            {
                f = (_LiveGameDataObjectsFactory)clazz.newInstance();
            }
            catch ( InstantiationException e )
            {
                RFDHLog.println( e );
            }
            catch ( IllegalAccessException e )
            {
                RFDHLog.println( e );
            }
            
            if ( f != null )
            {
                gdFactoriesMap.put( f.getGameId(), f );
            }
        }
        
        return ( gdFactoriesMap.get( gameId ) );
    }
}
