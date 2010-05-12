package net.ctdp.rfdynhud.widgets.timecomp;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( TimeCompareWidget.class, key ) );
    }
    
    public static final String header_lap_number = l( "header.lap_number" );
    public static final String header_sector1 = l( "header.sector1" );
    public static final String header_sector1_short = l( "header.sector1.short" );
    public static final String header_sector2 = l( "header.sector2" );
    public static final String header_sector2_short = l( "header.sector2.short" );
    public static final String header_sector3 = l( "header.sector3" );
    public static final String header_sector3_short = l( "header.sector3.short" );
    public static final String header_lap = l( "header.lap" );
    public static final String header_lap_short = l( "header.lap.short" );
    public static final String footer_gap = l( "footer.gap" );
    public static final String footer_gap_short = l( "footer.gap.short" );
}
