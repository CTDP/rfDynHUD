package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.util.TextureManager;
import net.ctdp.rfdynhud.widgets.widget.Widget;

public class ImageProperty extends StringProperty
{
    private final boolean noImageAllowed;
    
    public final boolean getNoImageAllowed()
    {
        return ( noImageAllowed );
    }
    
    public final void setImageName( String imageName )
    {
        setStringValue( imageName );
    }
    
    public final String getImageName()
    {
        return ( getStringValue() );
    }
    
    public final boolean isNoImage()
    {
        return ( ( getStringValue() == null ) || ( getStringValue().equals( "" ) ) );
    }
    
    public final ImageTemplate getImage()
    {
        if ( isNoImage() )
            return ( null );
        
        return ( TextureManager.getImage( getImageName() ) );
    }
    
    public ImageProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly, boolean noImageAllowed )
    {
        super( widget, propertyName, nameForDisplay, defaultValue, true, readonly, PropertyEditorType.IMAGE );
        
        this.noImageAllowed = noImageAllowed;
    }
    
    public ImageProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false, false );
    }
    
    public ImageProperty( Widget widget, String propertyName, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly, false );
    }
    
    public ImageProperty( Widget widget, String propertyName, String defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
