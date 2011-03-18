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
package net.ctdp.rfdynhud.widgets.standard.timing;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( TimingWidget.class, key ) );
    }
    
    public static final String abs_fastest_prefix = l( "abs_fastest.prefix" );
    public static final String abs_second_fastest_prefix = l( "abs_second_fastest.prefix" );
    public static final String abs_fastest_header_prefix = l( "abs_fastest.header.prefix" );
    public static final String own_fastest_prefix = l( "own_fastest.prefix" );
    public static final String current_prefix = l( "current.prefix" );
    public static final String timing_sector1_prefix = l( "timing.sector1.prefix" );
    public static final String timing_sector2_prefix = l( "timing.sector2.prefix" );
    public static final String timing_sector3_prefix = l( "timing.sector3.prefix" );
    public static final String timing_lap_prefix = l( "timing.lap.prefix" );
}
