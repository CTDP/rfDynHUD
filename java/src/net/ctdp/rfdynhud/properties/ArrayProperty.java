package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.util.Tools;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class ArrayProperty<E extends Object> extends Property
{
    private E[] array;
    private E value;
    
    public final E[] getArray()
    {
        return ( array );
    }
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( E oldValue, E newValue )
    {
    }
    
    public void setSelectedValue( E value )
    {
        if ( Tools.objectsEqual( value, this.value ) )
            return;
        
        E oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final E getSelectedValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public void setValue( Object value )
    {
        setSelectedValue( (E)value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( getSelectedValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            for ( E e : this.array )
            {
                if ( Tools.objectsEqual( value, e ) )
                {
                    setValue( e );
                    
                    return ( true );
                }
            }
            
            return ( true );
        }
        
        return ( false );
    }
    
    public ArrayProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue, E[] array, boolean readonly, String buttonText )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.ARRAY, buttonText, null );
        
        this.value = defaultValue;
        this.array = array;
    }
    
    public ArrayProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue, E[] array, boolean readonly )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, array, readonly, null );
    }
    
    public ArrayProperty( Widget widget, String propertyName, String nameForDisplay, E defaultValue, E[] array )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, array, false );
    }
    
    public ArrayProperty( Widget widget, String propertyName, E defaultValue, E[] array, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, array, readonly );
    }
    
    public ArrayProperty( Widget widget, String propertyName, E defaultValue, E[] array )
    {
        this( widget, propertyName, defaultValue, array, false );
    }
}
