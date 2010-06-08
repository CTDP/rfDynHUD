package net.ctdp.rfdynhud.gamedata;

import java.util.ArrayList;

import net.ctdp.rfdynhud.editor.EditorPresets;

public class LaptimesRecorder implements ScoringInfo.ScoringInfoUpdateListener
{
    @Override
    public void onSessionStarted( LiveGameData gameData, EditorPresets editorPresets ) {}
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets ) {}
    
    private ArrayList<Laptime> addLaptime( VehicleScoringInfo vsi, int lapsCompleted, Laptime laptime )
    {
        ArrayList<Laptime> laps = vsi.laptimes;
        
        for ( int i = laps.size(); i < lapsCompleted; i++ )
            laps.add( null );
        
        laps.add( laptime );
        
        return ( laps );
    }
    
    private void calcAvgLaptime( VehicleScoringInfo vsi )
    {
        if ( vsi.fastestLaptime == null )
        {
            vsi.avgSector1 = -1f;
            vsi.avgSector2 = -1f;
            vsi.avgSector3 = -1f;
            vsi.avgLaptime = -1f;
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
            vsi.avgSector1 = -1f;
            vsi.avgSector2 = -1f;
            vsi.avgSector3 = -1f;
            vsi.avgLaptime = -1f;
        }
        else
        {
            vsi.avgSector1 = sumS1 / (float)count;
            vsi.avgSector2 = sumS2 / (float)count;
            vsi.avgSector3 = sumS3 / (float)count;
            vsi.avgLaptime = sumL / (float)count;
        }
    }
    
    @Override
    public void onScoringInfoUpdated( LiveGameData gameData, EditorPresets editorPresets )
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
                    
                    if ( !scoringInfo.getSessionType().isRace() && ( last.isInLap == Boolean.TRUE ) )
                        last.sector3 = -1f;
                    else
                        last.sector3 = vsi.getLastSector3();
                    
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
                    
                    last.finished = true;
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
                            lastLap.sector3 = -1f;
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
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets ) {}
}
