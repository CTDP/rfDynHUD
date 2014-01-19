/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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

import net.ctdp.rfdynhud.render.TextureImage2D;

/**
 * The {@link ImagePropertyWithTexture} serves for customizing an image
 * and provides a {@link TextureImage2D} instance with a given size.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ImagePropertyWithTexture extends ImageProperty
{
    private TextureImage2D texture = null;
    private boolean knownEditorMode = false;
    
    private boolean textureDirty = false;
    
    /**
     * Updates the stored {@link TextureImage2D} to the given size and returns the texture.
     * 
     * @param width the new width
     * @param height the new height
     * @param isEditorMode is rendering in editor?
     * 
     * @return the scaled texture.
     */
    public TextureImage2D updateSize( int width, int height, boolean isEditorMode )
    {
        if ( isNoImage() )
            texture = null;
        else
            texture = getImage().getScaledTextureImage( width, height, texture, isEditorMode );
        
        knownEditorMode = isEditorMode;
        textureDirty = false;
        
        return ( texture );
    }
    
    /**
     * Gets the scaled texture. Make sure to call {@link #updateSize(int, int, boolean)} before.
     * 
     * @return the scaled texture.
     */
    public final TextureImage2D getTexture()
    {
        if ( textureDirty && ( texture != null ) )
            updateSize( texture.getWidth(), texture.getHeight(), knownEditorMode );
        
        return ( texture );
    }
    
    /**
     * Gets whether this property keeps a scaled texture instance.
     * 
     * @return whether this property keeps a scaled texture instance.
     */
    public final boolean hasTexture()
    {
        return ( getTexture() != null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onValueChanged( String oldValue, String newValue )
    {
        super.onValueChanged( oldValue, newValue );
        
        if ( isNoImage() )
        {
            texture = null;
            textureDirty = false;
        }
        else
        {
            textureDirty = true;
        }
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     * @param noImageAllowed allow "no image" for this property?
     */
    public ImagePropertyWithTexture( String name, String nameForDisplay, String defaultValue, boolean readonly, boolean noImageAllowed )
    {
        super( name, nameForDisplay, defaultValue, readonly, noImageAllowed );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public ImagePropertyWithTexture( String name, String nameForDisplay, String defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false, DEFAULT_NO_IMAGE_ALOWED );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public ImagePropertyWithTexture( String name, String defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly, DEFAULT_NO_IMAGE_ALOWED );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public ImagePropertyWithTexture( String name, String defaultValue )
    {
        this( name, defaultValue, false );
    }
}
