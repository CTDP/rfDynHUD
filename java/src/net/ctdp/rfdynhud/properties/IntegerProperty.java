package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class IntegerProperty extends Property
{
    private final Widget widget;
    
    private int value;
    
    private final int minValue;
    private final int maxValue;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    protected void onValueChanged( int oldValue, int newValue )
    {
    }
    
    public void setIntegerValue( int value )
    {
        value = Math.max( minValue, Math.min( value, maxValue ) );
        
        if ( value == this.value )
            return;
        
        int oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final int getIntegerValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setIntegerValue( ( (Number)value ).intValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getIntegerValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( Integer.parseInt( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public IntegerProperty( Widget widget, String propertyName, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        super( propertyName, nameForDisplay, readonly, PropertyEditorType.INTEGER, null, null );
        
        this.widget = widget;
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public IntegerProperty( Widget widget, String propertyName, String nameForDisplay, int defaultValue, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    public IntegerProperty( Widget widget, String propertyName, String nameForDisplay, int defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public IntegerProperty( Widget widget, String propertyName, int defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public IntegerProperty( Widget widget, String propertyName, int defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
