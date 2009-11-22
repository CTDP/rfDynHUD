package net.ctdp.rfdynhud.editor.properties;

import java.awt.Color;

import net.ctdp.rfdynhud.widgets.widget.Widget;

import org.openmali.vecmath2.util.ColorUtils;

public class ColorProperty extends Property
{
    public static final Color FALLBACK_COLOR = Color.MAGENTA;
    
    private final Widget widget;
    
    private String colorKey;
    private Color color = null;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void setColor( String colorKey )
    {
        if ( ( ( colorKey == null ) && ( this.colorKey == null ) ) || ( ( colorKey != null ) && colorKey.equals( this.colorKey ) ) )
            return;
        
        String oldValue = this.colorKey;
        this.colorKey = colorKey;
        this.color = null;
        
        widget.forceAndSetDirty();
        
        onValueChanged( oldValue, colorKey );
    }
    
    public final void setColor( Color color )
    {
        setColor( ColorUtils.colorToHex( color ) );
    }
    
    public final void setColor( int red, int green, int blue )
    {
        setColor( ColorUtils.colorToHex( red, green, blue ) );
    }
    
    public final String getColorKey()
    {
        return ( colorKey );
    }
    
    public final Color getColor()
    {
        if ( color == null )
        {
            color = widget.getConfiguration().getNamedColor( colorKey );
            if ( ( color == null ) && ( ( color = ColorUtils.hexToColor( colorKey, false ) ) == null ) )
                color = FALLBACK_COLOR;
        }
        
        return ( color );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setColor( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( colorKey );
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
    
    public ColorProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( propertyName, nameForDisplay, readonly, PropertyEditorType.COLOR, null, null );
        
        this.widget = widget;
        this.colorKey = defaultValue;
    }
    
    public ColorProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public ColorProperty( Widget widget, String propertyName, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public ColorProperty( Widget widget, String propertyName, String defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
