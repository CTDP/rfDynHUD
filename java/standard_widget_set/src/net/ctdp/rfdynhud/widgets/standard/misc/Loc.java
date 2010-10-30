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
package net.ctdp.rfdynhud.widgets.standard.misc;

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
    public static final String velocity_units_IMPERIAL = l( "velocity.units.IMPERIAL" );
    public static final String velocity_units_METRIC = l( "velocity.units.METRIC" );
}
