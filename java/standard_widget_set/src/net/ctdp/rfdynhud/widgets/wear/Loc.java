package net.ctdp.rfdynhud.widgets.wear;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( WearWidget.class, key ) );
    }
    
    public static final String engine_header_prefix = l( "engine.header.prefix" );
    public static final String tires_header_prefix = l( "tires.header.prefix" );
    public static final String brakes_header_prefix = l( "brakes.header.prefix" );
}
