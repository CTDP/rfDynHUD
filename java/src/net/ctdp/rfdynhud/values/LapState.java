package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;

public enum LapState
{
    OUTLAP( "OL" ),
    SOMEWHERE( "SW" ),
    AFTER_SECTOR1_START( "AS1S" ),
    BEFORE_SECTOR1_END( "BS1E" ),
    AFTER_SECTOR2_START( "AS2S" ),
    BEFORE_SECTOR2_END( "BS2E" ),
    AFTER_SECTOR3_START( "AS3S" ),
    BEFORE_SECTOR3_END( "BS3E" ),
    ;
    
    public final String SHORT;
    
    public final boolean isAfterSectorStart()
    {
        return ( ( this == AFTER_SECTOR1_START ) || ( this == AFTER_SECTOR2_START ) || ( this == AFTER_SECTOR3_START ) );
    }
    
    public final boolean isBeforeSectorEnd()
    {
        return ( ( this == BEFORE_SECTOR1_END ) || ( this == BEFORE_SECTOR2_END ) || ( this == BEFORE_SECTOR3_END ) );
    }
    
    private LapState( String SHORT )
    {
        this.SHORT = SHORT;
    }
    
    public static LapState getLapState( ScoringInfo scoringInfo, VehicleScoringInfo viewedVSI, Laptime refTime, float beforeSectorTime, float afterSectorTime, boolean firstSec1StartIsSomewhere )
    {
        if ( viewedVSI.getStintLength() < 1.0f )
            return ( OUTLAP );
        
        float laptime = viewedVSI.getCurrentLaptime();
        
        if ( ( refTime == null ) || !refTime.isFinished() )
        {
            if ( laptime < afterSectorTime )
            {
                if ( firstSec1StartIsSomewhere && ( viewedVSI.getStintLength() < 2.0f ) )
                    return ( SOMEWHERE );
                
                return ( AFTER_SECTOR1_START );
            }
            
            if ( ( viewedVSI.getStintLength() % 1.0f ) > 0.9f )
                return ( BEFORE_SECTOR3_END );
            
            return ( SOMEWHERE );
        }
        
        switch ( viewedVSI.getSector() )
        {
            case 1:
                if ( laptime < afterSectorTime )
                {
                    if ( firstSec1StartIsSomewhere && ( viewedVSI.getStintLength() < 2.0f ) )
                        return ( SOMEWHERE );
                    
                    return ( AFTER_SECTOR1_START );
                }
                
                if ( laptime < refTime.getSector1() - beforeSectorTime )
                    return ( SOMEWHERE );
                
                return ( BEFORE_SECTOR1_END );
                
            case 2:
                float sec1 = viewedVSI.getCurrentSector1();
                if ( laptime < sec1 + afterSectorTime )
                    return ( AFTER_SECTOR2_START );
                
                float gap1 = viewedVSI.getCurrentSector1() - refTime.getSector1();
                if ( laptime < refTime.getSector2( true ) + gap1 - beforeSectorTime )
                    return ( SOMEWHERE );
                
                return ( BEFORE_SECTOR2_END );
                
            case 3:
                float sec2 = viewedVSI.getCurrentSector2( true );
                if ( laptime < sec2 + afterSectorTime )
                    return ( AFTER_SECTOR3_START );
                
                float gap2 = viewedVSI.getCurrentSector2( true ) - refTime.getSector2( true );
                if ( laptime < refTime.getLapTime() + gap2 - beforeSectorTime )
                    return ( SOMEWHERE );
                
                return ( BEFORE_SECTOR3_END );
        }
        
        // Should be unreachable!
        return ( SOMEWHERE );
    }
}
