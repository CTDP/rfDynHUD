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
package net.ctdp.rfdynhud.etv2010.widgets.sessionstate;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( ETVSessionStateWidget.class, key ) );
    }
    
    public static final String caption_TEST_DAY_laps = l( "caption.TEST_DAY.laps" );
    public static final String caption_TEST_DAY_time = l( "caption.TEST_DAY.time" );
    public static final String caption_PRACTICE1_laps = l( "caption.PRACTICE1.laps" );
    public static final String caption_PRACTICE1_time = l( "caption.PRACTICE1.time" );
    public static final String caption_PRACTICE2_laps = l( "caption.PRACTICE2.laps" );
    public static final String caption_PRACTICE2_time = l( "caption.PRACTICE2.time" );
    public static final String caption_PRACTICE3_laps = l( "caption.PRACTICE3.laps" );
    public static final String caption_PRACTICE3_time = l( "caption.PRACTICE3.time" );
    public static final String caption_PRACTICE4_laps = l( "caption.PRACTICE4.laps" );
    public static final String caption_PRACTICE4_time = l( "caption.PRACTICE4.time" );
    public static final String caption_QUALIFYING_laps = l( "caption.QUALIFYING.laps" );
    public static final String caption_QUALIFYING_time = l( "caption.QUALIFYING.time" );
    public static final String caption_WARMUP_laps = l( "caption.WARMUP.laps" );
    public static final String caption_WARMUP_time = l( "caption.WARMUP.time" );
    public static final String caption_RACE_laps = l( "caption.RACE.laps" );
    public static final String caption_RACE_time = l( "caption.RACE.time" );
}
