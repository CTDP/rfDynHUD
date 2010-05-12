package net.ctdp.rfdynhud.widgets.fuel;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( FuelWidget.class, key ) );
    }
    
    public static final String fuel_units_IMPERIAL = l( "fuel_units.IMPERIAL" );
    public static final String fuel_units_METRIC = l( "fuel_units.METRIC" );
    public static final String fuelHeader_prefix = l( "fuelHeader.prefix" );
    public static final String fuelHeader_postfix_IMPERIAL = l( "fuelHeader.postfix.IMPERIAL" );
    public static final String fuelHeader_postfix_METRIC = l( "fuelHeader.postfix.METRIC" );
    public static final String fuelLoad2_postfix = l( "fuelLoad2.postfix" );
    public static final String fuelLoad3_na = l( "fuelLoad3.na" );
    public static final String fuelLoad3_postfix = l( "fuelLoad3.postfix" );
    public static final String fuelUsageHeader = l( "fuelUsageHeader" );
    public static final String fuelUsageLastLapHeader = l( "fuelUsageLastLapHeader" );
    public static final String fuelUsageAvgHeader = l( "fuelUsageAvgHeader" );
    public static final String fuelUsageLastLap_na = l( "fuelUsageLastLap.na" );
    public static final String fuelUsageAvg_na = l( "fuelUsageAvg.na" );
    public static final String nextPitstopHeader = l( "nextPitstopHeader" );
    public static final String nextPitstopLap_prefix = l( "nextPitstopLap.prefix" );
    public static final String nextPitstopLap_enough = l( "nextPitstopLap.enough" );
    public static final String nextPitstopLap_na = l( "nextPitstopLap.na" );
    public static final String nextPitstopFuel_prefix = l( "nextPitstopFuel.prefix" );
    public static final String nextPitstopFuel_enough = l( "nextPitstopFuel.enough" );
    public static final String nextPitstopFuel_na = l( "nextPitstopFuel.na" );
    public static final String nextPitstopFuel_laps = l( "nextPitstopFuel.laps" );
}
