package net.ctdp.rfdynhud.editor.properties;

import net.ctdp.rfdynhud.render.BorderCache;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;

public class BorderProperty extends Property
{
    private final Widget widget;
    
    private String borderName;
    private BorderWrapper border = null;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void setBorder( String borderName )
    {
        if ( ( ( borderName == null ) && ( this.borderName == null ) ) || ( ( borderName != null ) && borderName.equals( this.borderName ) ) )
            return;
        
        String oldValue = this.borderName;
        this.borderName = borderName;
        this.border = null;
        
        widget.forceAndSetDirty();
        
        onValueChanged( oldValue, borderName );
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
                border = new BorderWrapper( null );
            }
            else
            {
                String borderName_ = widget.getConfiguration().getBorderName( borderName );
                
                if ( borderName_ == null )
                    border = new BorderWrapper( BorderCache.getTexturedBorder( borderName ) );
                else
                    border = new BorderWrapper( BorderCache.getTexturedBorder( borderName_ ) );
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
    
    public BorderProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( propertyName, nameForDisplay, readonly, PropertyEditorType.BORDER, null, null );
        
        this.widget = widget;
        this.borderName = defaultValue;
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
                border = new BorderWrapper( null );
            }
            else
            {
                String borderName_ = widgetsConfig.getBorderName( borderName );
                
                if ( borderName_ == null )
                    border = new BorderWrapper( BorderCache.getTexturedBorder( borderName ) );
                else
                    border = new BorderWrapper( BorderCache.getTexturedBorder( borderName_ ) );
            }
        }
        
        return ( border );
    }
}
