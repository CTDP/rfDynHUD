package net.ctdp.rfdynhud.gamedata;

import java.util.ArrayList;
import java.util.HashMap;

public class LaptimesRecorder implements ScoringInfo.ScoringInfoUpdateListener
{
    private final HashMap<String, Integer> lapsCompletedMap = new HashMap<String, Integer>();
    private final HashMap<String, ArrayList<Laptime>> laptimesMap = new HashMap<String, ArrayList<Laptime>>();
    private final HashMap<String, Laptime> fastestLaptimesMap = new HashMap<String, Laptime>();
    
    //private Laptime absFastestLaptime = null;
    
    public void reset()
    {
        lapsCompletedMap.clear();
        laptimesMap.clear();
        fastestLaptimesMap.clear();
        //absFastestLaptime = null;
    }
    
    public void onSessionStarted( LiveGameData gameData )
    {
        reset();
    }
    
    public void onRealtimeEntered( LiveGameData gameData ) {}
    
    private ArrayList<Laptime> addLaptime( String driverName, int lapsCompleted, Laptime laptime )
    {
        ArrayList<Laptime> laps = laptimesMap.get( driverName );
        if ( laps == null )
        {
            laps = new ArrayList<Laptime>();
            laptimesMap.put( driverName, laps );
        }
        
        for ( int i = laps.size(); i < lapsCompleted; i++ )
            laps.add( null );
        
        laps.add( laptime );
        
        return ( laps );
    }
    
    public void onScoringInfoUpdated( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        for ( int i = 0; i < scoringInfo.getNumVehicles(); i++ )
        {
            VehicleScoringInfo vsi = scoringInfo.getVehicleScoringInfo( i );
            
            if ( vsi.getFinishStatus() != FinishStatus.NONE )
                continue;
            
            String driverName = vsi.getDriverName();
            int lapsCompleted = vsi.getLapsCompleted();
            
            ArrayList<Laptime> laps;
            Integer lastLapsCompleted = lapsCompletedMap.get( driverName );
            if ( lastLapsCompleted == null )
            {
                lapsCompletedMap.put( driverName, lapsCompleted );
                Laptime laptime = new Laptime( lapsCompleted + 1 );
                laps = addLaptime( driverName, lapsCompleted, laptime );
            }
            else if ( lastLapsCompleted.intValue() < lapsCompleted )
            {
                lapsCompletedMap.put( driverName, lapsCompleted );
                Laptime laptime = new Laptime( lapsCompleted + 1 );
                laps = addLaptime( driverName, lapsCompleted, laptime );
                
                Laptime last = laptimesMap.get( driverName ).get( lapsCompleted - 1 );
                
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
                        Laptime fastestLaptime = fastestLaptimesMap.get( driverName );
                        if ( ( fastestLaptime == null ) || ( fastestLaptime.getLapTime() < 0f ) || ( last.getLapTime() < fastestLaptime.getLapTime() ) )
                        {
                            fastestLaptimesMap.put( driverName, last );
                        }
                        
                        /*
                        if ( ( absFastestLaptime == null ) || ( absFastestLaptime.getLapTime() < 0f ) || ( last.getLapTime() < absFastestLaptime.getLapTime() ) )
                        {
                            absFastestLaptime = last;
                        }
                        */
                    }
                }
            }
            else
            {
                laps = laptimesMap.get( driverName );
                Laptime laptime = laps.get( lapsCompleted );
                
                if ( laptime == null )
                {
                    laptime = new Laptime( lapsCompleted + 1 );
                    addLaptime( driverName, lapsCompleted, laptime );
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
            
            vsi.laptimes = laps;
            vsi.fastestLaptime = fastestLaptimesMap.get( driverName );
            
            if ( vsi.isInPits() )
            {
                float trackPos = ( vsi.getLapDistance() / scoringInfo.getTrackLength() );
                
                Laptime laptime = vsi.getLaptime( lapsCompleted + 1 );
                
                if ( laptime == null )
                {
                    laptime = new Laptime( lapsCompleted + 1 );
                    addLaptime( driverName, lapsCompleted, laptime );
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
        
        //scoringInfo.absFastestLaptime = this.absFastestLaptime;
    }
    
    public void onRealtimeExited( LiveGameData gameData ) {}
}
