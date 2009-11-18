package net.ctdp.rfdynhud.gamedata;


/**
 * @author Marvin Froehlich
 */
public class LiveGameData_CPP_Adapter
{
    private final LiveGameData gameData;
    
    public final void prepareTelemetryDataUpdate()
    {
        gameData.getTelemetryData().prepareDataUpdate();
    }
    
    public final byte[] getTelemetryBuffer()
    {
        return ( gameData.getTelemetryData().buffer );
    }
    
    public void notifyTelemetryUpdated()
    {
        gameData.getTelemetryData().onDataUpdated();
    }
    
    public final void prepareScoringInfoDataUpdate()
    {
        gameData.getScoringInfo().prepareDataUpdate();
    }
    
    public final byte[] getScoringInfoBuffer()
    {
        return ( gameData.getScoringInfo().buffer );
    }
    
    public void initVehicleScoringInfo()
    {
        gameData.getScoringInfo().initVehicleScoringInfo();
    }
    
    public final byte[] getVehicleScoringInfoBuffer( int index )
    {
        return ( gameData.getScoringInfo().getVehicleScoringInfo( index ).buffer );
    }
    
    public void notifyScoringInfoUpdated()
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        int n = scoringInfo.getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            scoringInfo.getVehicleScoringInfo( i ).onDataUpdated();
        }
        
        scoringInfo.onDataUpdated();
    }
    
    public final void prepareGraphicsInfoDataUpdate()
    {
        gameData.getGraphicsInfo().prepareDataUpdate();
    }
    
    public final byte[] getGraphicsInfoBuffer()
    {
        return ( gameData.getGraphicsInfo().buffer );
    }
    
    public void notifyGraphicsInfoUpdated()
    {
        gameData.getGraphicsInfo().onDataUpdated();
    }
    
    public final void prepareCommentaryInfoDataUpdate()
    {
        gameData.getCommentaryRequestInfo().prepareDataUpdate();
    }
    
    public final byte[] getCommentaryInfoBuffer()
    {
        return ( gameData.getCommentaryRequestInfo().buffer );
    }
    
    public void notifyCommentaryInfoUpdated()
    {
        gameData.getCommentaryRequestInfo().onDataUpdated();
    }
    
    public LiveGameData_CPP_Adapter( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
