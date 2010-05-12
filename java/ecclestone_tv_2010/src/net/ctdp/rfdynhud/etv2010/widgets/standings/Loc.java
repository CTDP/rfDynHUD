package net.ctdp.rfdynhud.etv2010.widgets.standings;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( ETVStandingsWidget.class, key ) );
    }
    
    public static final String gap_lap = l( "gap.lap" );
    public static final String gap_laps = l( "gap.laps" );
}
