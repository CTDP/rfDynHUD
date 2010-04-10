package net.ctdp.rfdynhud.widgets._util;

import java.util.ArrayList;
import java.util.List;

import net.ctdp.rfdynhud.editor.properties.Property;

public class FlatWidgetPropertiesContainer extends WidgetPropertiesContainer
{
    private final ArrayList<Property> properties = new ArrayList<Property>();
    
    public final List<Property> getList()
    {
        return ( properties );
    }
    
    @Override
    protected void clearImpl()
    {
        properties.clear();
    }
    
    @Override
    protected void addGroupImpl( String groupName, boolean initiallyExpanded )
    {
    }
    
    @Override
    protected void addPropertyImpl( Property property )
    {
        properties.add( property );
    }
}
