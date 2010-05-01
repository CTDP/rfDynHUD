package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.render.BorderCache;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class BorderProperty extends Property
{
    public static final String DEFAULT_BORDER_NAME = "StandardBorder";
    
    private final WidgetsConfiguration widgetsConf;
    
    private String borderName;
    private BorderWrapper border = null;
    
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void refresh()
    {
        this.border = null;
    }
    
    public void setBorder( String borderName )
    {
        if ( ( ( borderName == null ) && ( this.borderName == null ) ) || ( ( borderName != null ) && borderName.equals( this.borderName ) ) )
            return;
        
        String oldValue = this.borderName;
        this.borderName = borderName;
        this.border = null;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged( oldValue, borderName );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, borderName, widget );
    }
    
    public final String getBorderName()
    {
        return ( borderName );
    }
    
    public final BorderWrapper getBorder()
    {
        if ( border == null )
        {
            if ( ( borderName == null ) || borderName.equals( "" ) )
            {
                border = new BorderWrapper( null, null );
            }
            else
            {
                final WidgetsConfiguration widgetsConf = ( widget != null ) ? widget.getConfiguration() : this.widgetsConf;
                
                String borderName_ = widgetsConf.getBorderName( borderName );
                
                if ( borderName_ == null )
                    border = BorderCache.getBorder( borderName );
                else
                    border = BorderCache.getBorder( borderName_ );
            }
        }
        
        return ( border );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setBorder( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( borderName );
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
    
    private BorderProperty( WidgetsConfiguration widgetsConf, Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.BORDER, null, null );
        
        this.widgetsConf = widgetsConf;
        this.borderName = defaultValue;
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widgetsConf, null, propertyName, nameForDisplay, defaultValue, readonly );
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widgetsConf, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue, boolean readonly )
    {
        this( widgetsConf, propertyName, propertyName, defaultValue, readonly );
    }
    
    public BorderProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue )
    {
        this( widgetsConf, propertyName, defaultValue, false );
    }
    
    public BorderProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( null, widget, propertyName, nameForDisplay, defaultValue, readonly );
    }
    
    public BorderProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public BorderProperty( Widget widget, String propertyName, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public BorderProperty( Widget widget, String propertyName, String defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
    
    public static final BorderWrapper getBorderFromBorderName( String borderName, BorderWrapper border, WidgetsConfiguration widgetsConfig )
    {
        if ( border == null )
        {
            if ( ( borderName == null ) || borderName.equals( "" ) )
            {
                border = new BorderWrapper( null, null );
            }
            else
            {
                String borderName_ = widgetsConfig.getBorderName( borderName );
                
                if ( borderName_ == null )
                    border = BorderCache.getBorder( borderName );
                else
                    border = BorderCache.getBorder( borderName_ );
            }
        }
        
        return ( border );
    }
}
