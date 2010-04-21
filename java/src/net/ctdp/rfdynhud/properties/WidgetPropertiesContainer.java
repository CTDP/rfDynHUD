package net.ctdp.rfdynhud.properties;


public abstract class WidgetPropertiesContainer
{
    private int numGroups = 0;
    private int numProperties = 0;
    
    protected abstract void clearImpl();
    
    public final void clear()
    {
        clearImpl();
        
        numGroups = 0;
        numProperties = 0;
    }
    
    protected abstract void addGroupImpl( String groupName, boolean initiallyExpanded );
    
    /**
     * Creates a new property group.
     * 
     * @param groupName
     * @param initiallyExpanded
     */
    public final void addGroup( String groupName, boolean initiallyExpanded )
    {
        addGroupImpl( groupName, initiallyExpanded );
        
        numGroups++;
    }
    
    /**
     * Creates a new property group (initially expanded).
     * 
     * @param groupName
     */
    public final void addGroup( String groupName )
    {
        addGroup( groupName, true );
    }
    
    protected abstract void addPropertyImpl( Property property );
    
    /**
     * Adds the property to the container under the last created group.
     * 
     * @param property
     */
    public final void addProperty( Property property )
    {
        addPropertyImpl( property );
        
        numProperties++;
    }
}
