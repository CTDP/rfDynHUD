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
