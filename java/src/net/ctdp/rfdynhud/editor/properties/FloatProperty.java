package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public class FloatProperty extends Property
{
    private final Widget widget;
    
    private float value;
    
    private final float minValue;
    private final float maxValue;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    protected void onValueChanged( float oldValue, float newValue )
    {
    }
    
    public void setFloatValue( float value )
    {
        value = Math.max( minValue, Math.min( value, maxValue ) );
        
        if ( value == this.value )
            return;
        
        float oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
    }
    
    public final float getFloatValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setFloatValue( ( (Number)value ).floatValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getFloatValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( Float.parseFloat( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public FloatProperty( Widget widget, String propertyName, String nameForDisplay, float defaultValue, float minValue, float maxValue, boolean readonly )
    {
        super( propertyName, nameForDisplay, readonly, PropertyEditorType.FLOAT, null, null );
        
        this.widget = widget;
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public FloatProperty( Widget widget, String propertyName, String nameForDisplay, float defaultValue, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, -Float.MAX_VALUE, +Float.MAX_VALUE, readonly );
    }
    
    public FloatProperty( Widget widget, String propertyName, String nameForDisplay, float defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public FloatProperty( Widget widget, String propertyName, float defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public FloatProperty( Widget widget, String propertyName, float defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
