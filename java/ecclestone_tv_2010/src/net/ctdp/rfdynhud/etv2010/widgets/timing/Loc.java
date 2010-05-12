package net.ctdp.rfdynhud.etv2010.widgets.timing;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( ETVTimingWidget.class, key ) );
    }
    
    public static final String blah = l( "blah" );
}
