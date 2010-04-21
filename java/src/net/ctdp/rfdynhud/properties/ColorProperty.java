package net.ctdp.rfdynhud.properties;

import java.awt.Color;

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

import org.openmali.vecmath2.util.ColorUtils;

public class ColorProperty extends Property
{
    public static final String STANDARD_BACKGROUND_COLOR_NAME = "StandardBackground";
    public static final String STANDARD_FONT_COLOR_NAME = "StandardFontColor";
    
    public static final Color FALLBACK_COLOR = Color.MAGENTA;
    
    private final WidgetsConfiguration widgetsConf;
    private final Widget widget;
    
    private String colorKey;
    private Color color = null;
    
    public static String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( STANDARD_BACKGROUND_COLOR_NAME ) )
            return ( "#00000096" );
        
        if ( name.equals( STANDARD_FONT_COLOR_NAME ) )
            return ( "#C0BC3D" );
        
        return ( null );
    }
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void refresh()
    {
        this.color = null;
    }
    
    public void setColor( String colorKey )
    {
        if ( ( ( colorKey == null ) && ( this.colorKey == null ) ) || ( ( colorKey != null ) && colorKey.equals( this.colorKey ) ) )
            return;
        
        String oldValue = this.colorKey;
        this.colorKey = colorKey;
        this.color = null;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, colorKey );
        
        if ( widget != null )
            __WPrivilegedAccess.onColorChanged( this, oldValue, colorKey, widget );
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
        if ( colorKey == null )
            return ( null );
        
        if ( color == null )
        {
            final WidgetsConfiguration widgetsConf = ( widget != null ) ? widget.getConfiguration() : this.widgetsConf;
            
            color = widgetsConf.getNamedColor( colorKey );
            
            if ( ( color == null ) && ( widget != null ) )
            {
                String colorStr = widget.getDefaultNamedColorValue( colorKey );
                if ( colorStr != null )
                {
                    Color color2 = ColorUtils.hexToColor( colorStr, false );
                    if ( color2 != null )
                    {
                        widgetsConf.addNamedColor( colorKey, color2 );
                        color = color2;
                    }
                }
            }
            
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
        setColor( ( value == null ) ? null : String.valueOf( value ) );
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
    
    private ColorProperty( WidgetsConfiguration widgetsConf, Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( propertyName, nameForDisplay, readonly, PropertyEditorType.COLOR, null, null );
        
        this.widgetsConf = widgetsConf;
        this.widget = widget;
        this.colorKey = defaultValue;
    }
    
    public ColorProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widgetsConf, null, propertyName, nameForDisplay, defaultValue, readonly );
    }
    
    public ColorProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widgetsConf, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public ColorProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue, boolean readonly )
    {
        this( widgetsConf, propertyName, propertyName, defaultValue, readonly );
    }
    
    public ColorProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue )
    {
        this( widgetsConf, propertyName, defaultValue, false );
    }
    
    public ColorProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( null, widget, propertyName, nameForDisplay, defaultValue, readonly );
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
