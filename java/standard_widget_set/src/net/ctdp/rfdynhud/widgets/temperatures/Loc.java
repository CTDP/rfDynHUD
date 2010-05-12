package net.ctdp.rfdynhud.widgets.temperatures;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( TemperaturesWidget.class, key ) );
    }
    
    public static final String temperature_units_METRIC = l( "temperature.units.METRIC" );
    public static final String engine_header_prefix = l( "engine.header.prefix" );
    public static final String engine_watertemp_prefix = l( "engine.watertemp.prefix" );
    public static final String brakes_header_prefix = l( "brakes.header.prefix" );
}
