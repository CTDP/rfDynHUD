package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class StringProperty extends Property
{
    private final boolean forceTrimOnSet;
    private String value;
    
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void setStringValue( String value )
    {
        if ( forceTrimOnSet && ( value != null ) )
            value = value.trim();
        
        if ( ( ( value == null ) && ( this.value == null ) ) || ( ( value != null ) && value.equals( this.value ) ) )
            return;
        
        String oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final String getStringValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setStringValue( (String)value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getStringValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( value );
            
            return ( true );
        }
        
        return ( false );
    }
    
    protected StringProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean forceTrimOnSet, boolean readonly, PropertyEditorType propEdType )
    {
        super( widget, propertyName, nameForDisplay, readonly, propEdType, null, null );
        
        this.forceTrimOnSet = forceTrimOnSet;
        this.value = defaultValue;
    }
    
    public StringProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false, readonly, PropertyEditorType.STRING );
    }
    
    public StringProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public StringProperty( Widget widget, String propertyName, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public StringProperty( Widget widget, String propertyName, String defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
