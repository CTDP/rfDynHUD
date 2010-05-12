package net.ctdp.rfdynhud.widgets.standings;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( StandingsWidget.class, key ) );
    }
    
    public static final String column_time_gap_laps_singular = l( "column.time.gap.laps.singular" );
    public static final String column_time_gap_laps_plural = l( "column.time.gap.laps.plural" );
    public static final String column_time_gap_laps_short = l( "column.time.gap.laps.short" );
    public static final String column_laps_singular = l( "column.laps.singular" );
    public static final String column_laps_plural = l( "column.laps.plural" );
    public static final String column_laps_short = l( "column.laps.short" );
    public static final String column_stops_singular = l( "column.stops.singular" );
    public static final String column_stops_plural = l( "column.stops.plural" );
    public static final String column_stops_short = l( "column.stops.short" );
    public static final String column_topspeed_units_IMPERIAL = l( "column.topspeed.units.IMPERIAL" );
    public static final String column_topspeed_units_METRIC = l( "column.topspeed.units.METRIC" );
    public static final String finishsstatus_FINISHED = l( "finishsstatus.FINISHED" );
    public static final String finishsstatus_DNF = l( "finishsstatus.DNF" );
    public static final String finishsstatus_DQ = l( "finishsstatus.DQ" );
    public static final String out = l( "out" );
}
