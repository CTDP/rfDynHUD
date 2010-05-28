package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

public class __PropsPrivilegedAccess
{
    public static final BooleanProperty newBooleanProperty( WidgetsConfiguration widgetsConfig, String propertyName, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        return ( new BooleanProperty( widgetsConfig, propertyName, nameForDisplay, defaultValue, readonly ) );
    }
    
    public static final boolean isWidgetsConfigProperty( Property property )
    {
        return ( property.widgetsConfig != null );
    }
}
