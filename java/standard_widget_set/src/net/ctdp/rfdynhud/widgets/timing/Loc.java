package net.ctdp.rfdynhud.widgets.timing;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( TimingWidget.class, key ) );
    }
    
    public static final String abs_fastest_header_prefix = l( "abs_fastest.header.prefix" );
    public static final String abs_fastest_prefix = l( "abs_fastest.prefix" );
    public static final String abs_second_fastest_prefix = l( "abs_second_fastest.prefix" );
    public static final String timing_sector1_prefix = l( "timing.sector1.prefix" );
    public static final String timing_sector2_prefix = l( "timing.sector2.prefix" );
    public static final String timing_sector3_prefix = l( "timing.sector3.prefix" );
    public static final String timing_lap_prefix = l( "timing.lap.prefix" );
}
