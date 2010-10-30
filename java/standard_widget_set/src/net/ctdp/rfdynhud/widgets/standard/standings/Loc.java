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
package net.ctdp.rfdynhud.widgets.standard.standings;

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
