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
package net.ctdp.rfdynhud.widgets.lessons.lesson6;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String t( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( Lesson6Widget_Localizations.class, key ) );
    }
    
    public static final String temperature_units_METRIC = t( "temperature.units.METRIC" );
    public static final String temperature_units_IMPERIAL = t( "temperature.units.IMPERIAL" );
    public static final String mytext_caption = t( "mytext.caption" );
}
