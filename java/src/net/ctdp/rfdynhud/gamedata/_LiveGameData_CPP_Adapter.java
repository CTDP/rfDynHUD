package net.ctdp.rfdynhud.gamedata;


/**
 * @author Marvin Froehlich
 */
public class _LiveGameData_CPP_Adapter
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
        gameData.getTelemetryData().onDataUpdated( null );
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
        return ( gameData.getScoringInfo().vehicleScoringInfoCapsules[index].getBuffer() );
    }
    
    public void notifyScoringInfoUpdated()
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        scoringInfo.assignVSICapsules();
        
        int n = scoringInfo.getNumVehicles();
        for ( int i = 0; i < n; i++ )
        {
            scoringInfo.getVehicleScoringInfo( i ).onDataUpdated();
        }
        
        scoringInfo.onDataUpdated( null );
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
        gameData.getGraphicsInfo().onDataUpdated( null );
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
        gameData.getCommentaryRequestInfo().onDataUpdated( null );
    }
    
    public _LiveGameData_CPP_Adapter( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
