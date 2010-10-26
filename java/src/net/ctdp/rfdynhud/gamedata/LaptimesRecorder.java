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

import java.util.ArrayList;

class LaptimesRecorder implements ScoringInfo.ScoringInfoUpdateListener
{
    @Override
    public void onSessionStarted( LiveGameData gameData, boolean isEditorMode ) {}
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, boolean isEditorMode ) {}
    
    private static ArrayList<Laptime> addLaptime( VehicleScoringInfo vsi, int lapsCompleted, Laptime laptime )
    {
        ArrayList<Laptime> laps = vsi.laptimes;
        
        for ( int i = laps.size(); i < lapsCompleted; i++ )
            laps.add( null );
        
        laps.add( laptime );
        
        return ( laps );
    }
    
    static void calcAvgLaptime( VehicleScoringInfo vsi )
    {
        if ( vsi.fastestLaptime == null )
        {
            vsi.oldAverageLaptime = null;
            vsi.averageLaptime = null;
            return;
        }
        
        float fastest = vsi.fastestLaptime.getLapTime();
        float accepted = fastest * 1.06f;
        
        float sumS1 = 0f;
        float sumS2 = 0f;
        float sumS3 = 0f;
        float sumL = 0f;
        int count = 0;
        
        for ( int i = 0; i < vsi.laptimes.size(); i++ )
        {
            Laptime lt = vsi.laptimes.get( i );
            if ( ( lt != null ) && lt.finished && !lt.isOutLap && ( lt.isInLap == Boolean.FALSE ) )
            {
                float ltt = lt.getLapTime();
                
                if ( ltt <= accepted )
                {
                    sumS1 += lt.getSector1();
                    sumS2 += lt.getSector2();
                    sumS3 += lt.getSector3();
                    sumL += ltt;
                    count++;
                }
            }
        }
        
        if ( count == 0 )
        {
            vsi.oldAverageLaptime = null;
            vsi.averageLaptime = null;
        }
        else
        {
            if ( vsi.averageLaptime == null )
            {
                vsi.oldAverageLaptime = null;
                
                vsi.averageLaptime = new Laptime( 0 );
                vsi.averageLaptime.isInLap = false;
                vsi.averageLaptime.isOutLap = false;
                vsi.averageLaptime.finished = true;
            }
            else
            {
                if ( vsi.oldAverageLaptime == null )
                {
                    vsi.oldAverageLaptime = new Laptime( vsi.averageLaptime.lap );
                    vsi.oldAverageLaptime.isInLap = false;
                    vsi.oldAverageLaptime.isOutLap = false;
                    vsi.oldAverageLaptime.finished = true;
                    vsi.oldAverageLaptime.sector1 = vsi.averageLaptime.sector1;
                    vsi.oldAverageLaptime.sector2 = vsi.averageLaptime.sector2;
                    vsi.oldAverageLaptime.sector3 = vsi.averageLaptime.sector3;
                    vsi.oldAverageLaptime.laptime = vsi.averageLaptime.laptime;
                }
                else if ( count != vsi.oldAverageLaptime.lap )
                {
                    vsi.oldAverageLaptime.lap = vsi.averageLaptime.lap;
                    vsi.oldAverageLaptime.sector1 = vsi.averageLaptime.sector1;
                    vsi.oldAverageLaptime.sector2 = vsi.averageLaptime.sector2;
                    vsi.oldAverageLaptime.sector3 = vsi.averageLaptime.sector3;
                    vsi.oldAverageLaptime.laptime = vsi.averageLaptime.laptime;
                }
            }
            
            vsi.averageLaptime.lap = count;
            vsi.averageLaptime.sector1 = sumS1 / count;
            vsi.averageLaptime.sector2 = sumS2 / count;
            vsi.averageLaptime.sector3 = sumS3 / count;
            vsi.averageLaptime.laptime = sumL / count;
        }
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, boolean isEditorMode )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        for ( int i = 0; i < scoringInfo.getNumVehicles(); i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            int lapsCompleted = vsi.getLapsCompleted();
            
            if ( vsi.isLapJustStarted() )
            {
                Laptime laptime = new Laptime( lapsCompleted + 1 );
                ArrayList<Laptime> laps = addLaptime( vsi, lapsCompleted, laptime );
                
                Laptime last = ( lapsCompleted == 0 ) ? null : laps.get( lapsCompleted - 1 );
                
                if ( last != null )
                {
                    last.sector1 = vsi.getLastSector1();
                    last.sector2 = vsi.getLastSector2( false );
                    
                    SessionType sessionType = scoringInfo.getSessionType();
                    
                    if ( !sessionType.isRace() && ( last.isInLap == Boolean.TRUE ) )
                        last.sector3 = -1f;
                    else
                        last.sector3 = vsi.getLastSector3();
                    
                    last.updateLaptimeFromSectors();
                    
                    if ( last.getLapTime() < 0f )
                    {
                        laps.set( lapsCompleted - 1, null );
                    }
                    else
                    {
                        Laptime fastestLaptime = vsi.fastestLaptime;
                        if ( ( fastestLaptime == null ) || ( fastestLaptime.getLapTime() < 0f ) || ( last.getLapTime() < fastestLaptime.getLapTime() ) )
                        {
                            vsi.fastestLaptime = last;
                        }
                    }
                    
                    if ( sessionType == SessionType.RACE )
                        last.setType( Laptime.LapType.RACE );
                    else if ( sessionType == SessionType.QUALIFYING )
                        last.setType( Laptime.LapType.QUALIFY );
                    else if ( Laptime.isHotlap( gameData ) )
                        last.setType( Laptime.LapType.HOTLAP );
                    else
                        last.setType( Laptime.LapType.NORMAL );
                    
                    last.finished = true;
                    
                    if ( ( last.isInLap != Boolean.TRUE ) && !last.isOutLap && ( last.getLapTime() > 0f ) )
                    {
                        DataCache.INSTANCE.addLaptime( scoringInfo, gameData.getProfileInfo().getTeamName(), last );
                    }
                }
            }
            else if ( vsi.getFinishStatus() == FinishStatus.NONE )
            {
                Laptime laptime = vsi.laptimes.get( lapsCompleted );
                
                if ( laptime == null )
                {
                    laptime = new Laptime( lapsCompleted + 1 );
                    addLaptime( vsi, lapsCompleted, laptime );
                }
                
                switch ( vsi.getSector() )
                {
                    case 1:
                        laptime.sector1 = vsi.getCurrentSector1();
                        laptime.sector2 = -1f;
                        laptime.sector3 = -1f;
                        break;
                    case 2:
                        laptime.sector1 = vsi.getLastSector1();
                        laptime.sector2 = vsi.getCurrentSector2( false );
                        laptime.sector3 = -1f;
                        break;
                    case 3:
                        laptime.sector1 = vsi.getLastSector1();
                        laptime.sector2 = vsi.getLastSector2( false );
                        if ( !scoringInfo.getSessionType().isRace() && ( laptime.isInLap == Boolean.TRUE ) )
                            laptime.sector3 = -1f;
                        else
                            laptime.sector3 = scoringInfo.getSessionTime() - vsi.getLapStartTime() - laptime.sector1 - laptime.sector2;
                        break;
                }
                
                laptime.updateLaptimeFromSectors();
            }
            
            if ( vsi.isInPits() )
            {
                Laptime laptime = vsi.getLaptime( lapsCompleted + 1 );
                
                if ( laptime == null )
                {
                    laptime = new Laptime( lapsCompleted + 1 );
                    addLaptime( vsi, lapsCompleted, laptime );
                }
                
                if ( vsi.getNormalizedLapDistance() > 0.5f )
                {
                    if ( vsi.getStintStartLap() != lapsCompleted + 1 )
                        laptime.isInLap = true;
                }
                else
                {
                    laptime.isOutLap = true;
                    
                    Laptime lastLap = vsi.getLaptime( lapsCompleted );
                    if ( lastLap != null )
                    {
                        lastLap.isInLap = true;
                        if ( !scoringInfo.getSessionType().isRace() )
                        {
                            lastLap.sector3 = -1f;
                            lastLap.updateLaptimeFromSectors();
                        }
                    }
                }
            }
            else if ( vsi.getStintLength() > 2.0f )
            {
                Laptime lastLap = vsi.getLaptime( lapsCompleted );
                if ( ( lastLap != null ) && ( lastLap.isInLap == null ) )
                {
                    lastLap.isInLap = false;
                }
            }
            
            if ( vsi.isLapJustStarted() )
            {
                calcAvgLaptime( vsi );
            }
        }
    }
    
    @Override
    public void onGamePauseStateChanged( LiveGameData gameData, boolean isEditorMode, boolean isPaused ) {}
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, boolean isEditorMode ) {}
    
    private LaptimesRecorder()
    {
    }
    
    static final LaptimesRecorder INSTANCE = new LaptimesRecorder();
}
