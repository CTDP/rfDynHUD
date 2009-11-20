package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public class StringProperty extends Property
{
    private final Widget widget;
    
    private String value;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void setStringValue( String value )
    {
        if ( ( ( value == null ) && ( this.value == null ) ) || ( ( value != null ) && value.equals( this.value ) ) )
            return;
        
        String oldValue = this.value;
        this.value = value;
        
        widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
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
    
    protected StringProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly, PropertyEditorType propEdType )
    {
        super( propertyName, nameForDisplay, readonly, propEdType, null, null );
        
        this.widget = widget;
        this.value = defaultValue;
    }
    
    public StringProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, readonly, PropertyEditorType.STRING );
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
