package net.ctdp.rfdynhud.editor.presets;

import net.ctdp.rfdynhud.properties.Property;

public class ChangedProperty implements Comparable<ChangedProperty>
{
    private final Property property;
    private final Object oldValue;
    private int changeId;
    
    public final Property getProperty()
    {
        return ( property );
    }
    
    public final Object getOldValue()
    {
        return ( oldValue );
    }
    
    public void resetValue()
    {
        property.setValue( oldValue );
    }
    
    public void setChangeId( int changeId )
    {
        this.changeId = changeId;
    }
    
    public final int getChangeId()
    {
        return ( changeId );
    }
    
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof ChangedProperty ) )
            return ( false );
        
        return ( property.getPropertyName().equals( ( (ChangedProperty)o ).property.getPropertyName() ) );
    }
    
    @Override
    public int hashCode()
    {
        return ( property.getPropertyName().hashCode() );
    }
    
    @Override
    public int compareTo( ChangedProperty o )
    {
        if ( this.changeId < o.changeId )
            return ( -1 );
        
        if ( this.changeId > o.changeId )
            return ( +1 );
        
        return ( 0 );
    }
    
    public ChangedProperty( Property property, Object oldValue, int changeId )
    {
        this.property = property;
        this.oldValue = oldValue;
        this.changeId = changeId;
    }
}
