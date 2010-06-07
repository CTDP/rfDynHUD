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
                float trackPos = ( vsi.getLapDistance() / scoringInfo.getTrackLength() );
                
                Laptime laptime = vsi.getLaptime( lapsCompleted + 1 );
                
                if ( laptime == null )
                {
                    laptime = new Laptime( lapsCompleted + 1 );
                    addLaptime( vsi, lapsCompleted, laptime );
                }
                
                if ( trackPos > 0.5f )
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
        }
    }
    
    @Override
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets ) {}
}
