package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;

public class DefaultWidgetPropertiesContainer extends WidgetPropertiesContainer
{
    private final FlaggedList root;
    private FlaggedList list;
    private FlaggedList listL2 = null;
    
    @Override
    protected void clearImpl()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void addGroupImpl( String groupName, boolean initiallyExpanded, boolean level2 )
    {
        if ( level2 )
        {
            if ( list == root )
                throw new IllegalStateException( "No group for level 1 defined." );
            
            FlaggedList group = new FlaggedList( groupName, initiallyExpanded );
            list.add( group );
            listL2 = group;
        }
        else
        {
            FlaggedList group = new FlaggedList( groupName, initiallyExpanded );
            root.add( group );
            list = group;
        }
    }
    
    @Override
    protected void popGroupL2Impl()
    {
        if ( listL2 == null )
            throw new IllegalStateException( "No group for level 2 defined/active." );
        
        listL2 = null;
    }
    
    @Override
    protected void addPropertyImpl( Property property )
    {
        if ( listL2 != null )
            listL2.add( property );
        else
            list.add( property );
    }
    
    public DefaultWidgetPropertiesContainer( FlaggedList root )
    {
        this.root = root;
        this.list = root;
    }
}
