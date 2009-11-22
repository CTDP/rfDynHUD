package net.ctdp.rfdynhud.editor.properties;

import java.awt.image.BufferedImage;

import net.ctdp.rfdynhud.util.TextureLoader;
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
    
    public final BufferedImage getBufferedImage()
    {
        return ( TextureLoader.getImage( getImageName() ) );
    }
    
    public ImageProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly, boolean noImageAllowed )
    {
        super( widget, propertyName, nameForDisplay, defaultValue, readonly, PropertyEditorType.IMAGE );
        
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
