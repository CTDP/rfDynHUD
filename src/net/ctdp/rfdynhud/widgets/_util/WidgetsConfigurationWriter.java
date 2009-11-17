package net.ctdp.rfdynhud.widgets._util;

import java.io.IOException;

public interface WidgetsConfigurationWriter
{
    public void writeProperty( String key, Object value, Boolean quoteValue, String comment ) throws IOException;
    
    public void writeProperty( String key, Object value, String comment ) throws IOException;
}
