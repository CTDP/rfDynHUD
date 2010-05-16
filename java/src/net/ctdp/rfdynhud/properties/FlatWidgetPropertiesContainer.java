package net.ctdp.rfdynhud.properties;

import java.util.ArrayList;
import java.util.List;


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
    protected void addGroupImpl( String groupName, boolean initiallyExpanded, boolean level2 )
    {
    }
    
    @Override
    protected void popGroupL2Impl()
    {
    }
    
    @Override
    protected void addPropertyImpl( Property property )
    {
        properties.add( property );
    }
}
