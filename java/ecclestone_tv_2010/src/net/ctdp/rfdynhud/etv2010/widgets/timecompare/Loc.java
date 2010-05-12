package net.ctdp.rfdynhud.etv2010.widgets.timecompare;

import net.ctdp.rfdynhud.util.LocalizationsManager;

public class Loc
{
    private static final String l( String key )
    {
        return ( LocalizationsManager.INSTANCE.getLocalization( ETVTimeCompareWidget.class, key ) );
    }
    
    public static final String caption_lap = l( "caption.lap" );
}
