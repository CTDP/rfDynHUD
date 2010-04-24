package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class BooleanProperty extends Property
{
    private boolean value;
    
    protected void onValueChanged( boolean newValue )
    {
    }
    
    public void setBooleanValue( boolean value )
    {
        if ( value == this.value )
            return;
        
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, !value, value, widget );
    }
    
    public final boolean getBooleanValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setBooleanValue( ( (Boolean)value ).booleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getBooleanValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( Boolean.parseBoolean( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public BooleanProperty( Widget widget, String propertyName, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
    }
    
    public BooleanProperty( Widget widget, String propertyName, String nameForDisplay, boolean defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public BooleanProperty( Widget widget, String propertyName, boolean defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public BooleanProperty( Widget widget, String propertyName, boolean defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}