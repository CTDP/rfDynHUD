package net.ctdp.rfdynhud.widgets.misc;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( MiscWidget.class, key ) );
    }
    
    public static final String scoring_leader_prefix = l( "scoring.leader.prefix" );
    public static final String scoring_leader_na = l( "scoring.leader.na" );
    public static final String scoring_place_prefix = l( "scoring.place.prefix" );
    public static final String scoring_place_na = l( "scoring.place.na" );
    public static final String scoring_fastest_lap_prefix = l( "scoring.fastest_lap.prefix" );
    public static final String scoring_fastest_lap_na = l( "scoring.fastest_lap.na" );
    public static final String timing_current_lap_prefix = l( "timing.current_lap.prefix" );
    public static final String timing_laps_done_prefix = l( "timing.laps_done.prefix" );
    public static final String timing_stintlength_prefix = l( "timing.stintlength.prefix" );
    public static final String timing_stintlength_na = l( "timing.stintlength.na" );
    public static final String timing_sessiontime_prefix = l( "timing.sessiontime.prefix" );
    public static final String velocity_topspeed1_prefix = l( "velocity.topspeed1.prefix" );
    public static final String velocity_topspeed2_prefix = l( "velocity.topspeed2.prefix" );
    public static final String velocity_velocity_prefix = l( "velocity.velocity.prefix" );
}
