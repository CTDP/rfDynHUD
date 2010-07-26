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
        return ( gameData.getTelemetryData().data.getBuffer() );
    }
    
    public void notifyTelemetryUpdated()
    {
        if ( !gameData.getProfileInfo().isValid() )
            return;
        
        gameData.getTelemetryData().onDataUpdated( null );
    }
    
    public final void prepareScoringInfoDataUpdate()
    {
        gameData.getScoringInfo().prepareDataUpdate();
    }
    
    public final byte[] getScoringInfoBuffer()
    {
        return ( gameData.getScoringInfo().data.getBuffer() );
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
        if ( !gameData.getProfileInfo().isValid() )
            return;
        
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
        return ( gameData.getGraphicsInfo().data.getBuffer() );
    }
    
    public void notifyGraphicsInfoUpdated()
    {
        if ( !gameData.getProfileInfo().isValid() )
            return;
        
        gameData.getGraphicsInfo().onDataUpdated( null );
    }
    
    public final void prepareCommentaryInfoDataUpdate()
    {
        gameData.getCommentaryRequestInfo().prepareDataUpdate();
    }
    
    public final byte[] getCommentaryInfoBuffer()
    {
        return ( gameData.getCommentaryRequestInfo().data.getBuffer() );
    }
    
    public void notifyCommentaryInfoUpdated()
    {
        if ( !gameData.getProfileInfo().isValid() )
            return;
        
        gameData.getCommentaryRequestInfo().onDataUpdated( null );
    }
    
    public _LiveGameData_CPP_Adapter( LiveGameData gameData )
    {
        this.gameData = gameData;
    }
}
