package net.ctdp.rfdynhud.editor.properties;

import java.awt.image.BufferedImage;

import net.ctdp.rfdynhud.util.TextureLoader;
import net.ctdp.rfdynhud.widgets.widget.Widget;

public class ImageProperty extends StringProperty
{
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
    
    public ImageProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, defaultValue, readonly, PropertyEditorType.IMAGE );
    }
    
    public ImageProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public ImageProperty( Widget widget, String propertyName, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public ImageProperty( Widget widget, String propertyName, String defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
