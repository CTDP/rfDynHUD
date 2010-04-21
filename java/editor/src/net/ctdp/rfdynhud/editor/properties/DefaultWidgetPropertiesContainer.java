package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;

public class DefaultWidgetPropertiesContainer extends WidgetPropertiesContainer
{
    private final FlaggedList root;
    private FlaggedList list;
    
    @Override
    protected void clearImpl()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void addGroupImpl( String groupName, boolean initiallyExpanded )
    {
        FlaggedList group = new FlaggedList( groupName, initiallyExpanded );
        root.add( group );
        list = group;
    }
    
    @Override
    protected void addPropertyImpl( Property property )
    {
        list.add( property );
    }
    
    public DefaultWidgetPropertiesContainer( FlaggedList root )
    {
        this.root = root;
        this.list = root;
    }
}
