/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.properties;

import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.util.TextureManager;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ImageProperty} serves for customizing an image.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImageProperty extends StringProperty
{
    public static final boolean DEFAULT_NO_IMAGE_ALOWED = false;
    
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
    
    /**
     * Gets the {@link ImageTemplate} defined by this {@link ImageProperty}.
     * 
     * @return the {@link ImageTemplate} defined by this {@link ImageProperty} or <code>null</code>, if set to no image.
     */
    public final ImageTemplate getImage()
    {
        if ( isNoImage() )
            return ( null );
        
        return ( TextureManager.getImage( getImageName() ) );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     * @param noImageAllowed
     */
    public ImageProperty( Widget widget, String name, String nameForDisplay, String defaultValue, boolean readonly, boolean noImageAllowed )
    {
        super( widget, name, nameForDisplay, defaultValue, true, readonly, PropertyEditorType.IMAGE );
        
        this.noImageAllowed = noImageAllowed;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public ImageProperty( Widget widget, String name, String nameForDisplay, String defaultValue )
    {
        this( widget, name, nameForDisplay, defaultValue, false, DEFAULT_NO_IMAGE_ALOWED );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param readonly
     */
    public ImageProperty( Widget widget, String name, String defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly, DEFAULT_NO_IMAGE_ALOWED );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public ImageProperty( Widget widget, String name, String defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     * @param noImageAllowed
     */
    public ImageProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue, boolean readonly, boolean noImageAllowed )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue, readonly, noImageAllowed );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public ImageProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue )
    {
        this( w2pf, name, nameForDisplay, defaultValue, false, DEFAULT_NO_IMAGE_ALOWED );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param readonly
     */
    public ImageProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly, DEFAULT_NO_IMAGE_ALOWED );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public ImageProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
}
