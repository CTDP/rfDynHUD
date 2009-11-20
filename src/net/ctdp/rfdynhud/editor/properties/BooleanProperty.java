package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public class BooleanProperty extends Property
{
    private final Widget widget;
    
    private boolean value;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    protected void onValueChanged( boolean newValue )
    {
    }
    
    public void setBooleanValue( boolean value )
    {
        if ( value == this.value )
            return;
        
        this.value = value;
        
        widget.forceAndSetDirty();
        
        onValueChanged( value );
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
            setBooleanValue( Boolean.parseBoolean( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public BooleanProperty( Widget widget, String propertyName, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( propertyName, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.widget = widget;
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
