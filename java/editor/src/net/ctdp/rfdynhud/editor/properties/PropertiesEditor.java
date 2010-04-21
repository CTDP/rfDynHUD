package net.ctdp.rfdynhud.editor.properties;

import java.util.ArrayList;

import net.ctdp.rfdynhud.editor.hiergrid.FlaggedList;
import net.ctdp.rfdynhud.properties.Property;

/**
 * 
 * @author Marvin Froehlich
 */
public class PropertiesEditor
{
    private static final long serialVersionUID = -1723298567515621091L;
    
    private FlaggedList properties;
    
    private final ArrayList<PropertyChangeListener> changeListeners = new ArrayList<PropertyChangeListener>();
    
    public void addChangeListener( PropertyChangeListener l )
    {
        changeListeners.add( l );
    }
    
    public void removeChangeListener( PropertyChangeListener l )
    {
        changeListeners.remove( l );
    }
    
    void invokeChangeListeners( Property property, Object oldValue, Object newValue, int row, int column )
    {
        for ( int i = 0; i < changeListeners.size(); i++ )
        {
            changeListeners.get( i ).onPropertyChanged( property, oldValue, newValue, row, column );
        }
    }
    
    public void clear()
    {
        properties.clear();
    }
    
    public final FlaggedList getPropertiesList()
    {
        return ( properties );
    }
    
    public void addProperty( Property p )
    {
        properties.add( p );
    }
    
    public void addProperties( FlaggedList props )
    {
        properties.add( props );
    }
    
    /*
    public Property getProperty( int row )
    {
        Object obj = properties.get( row );
        
        if ( obj instanceof Property )
            return ( (Property)obj );
        
        return ( null );
    }
    */
    
    public PropertiesEditor()
    {
        super();
        
        this.properties = new FlaggedList( "properties::" );
    }
}
