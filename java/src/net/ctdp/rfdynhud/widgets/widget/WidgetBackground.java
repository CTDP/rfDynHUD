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
package net.ctdp.rfdynhud.widgets.widget;

import java.awt.Color;

import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BackgroundProperty.BackgroundType;
import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.TextureManager;

/**
 * This class encapsulates a {@link Widget}'s effective background.
 * This can be a simple color or a scaled image.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class WidgetBackground
{
    private final Widget widget;
    private final BackgroundProperty property;
    
    private int changeCount = 0;
    
    private float backgroundScaleX = 1.0f;
    private float backgroundScaleY = 1.0f;
    
    /**
     * Gets the current type of this background.
     * 
     * @return the current type of this background.
     */
    public final BackgroundType getType()
    {
        return ( property.getBackgroundType() );
    }
    
    /**
     * Gets the {@link Color} from this {@link WidgetBackground}.
     * The result is only valid, if the {@link BackgroundType} ({@link #getType()}) is {@value BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link WidgetBackground}.
     */
    public final Color getColor()
    {
        if ( !property.getBackgroundType().isColor() )
            return ( null );
        
        return ( property.getColorValue() );
    }
    
    private TextureImage2D backgroundTexture = null;
    
    private void loadBackgroundImage( ImageTemplate image, int width, int height )
    {
        /*
        if ( backgroundImageName.isNoImage() )
        {
            backgroundTexture = null;
            backgroundScaleX = 1.0f;
            backgroundScaleY = 1.0f;
        }
        else
        */
        {
            boolean reloadBackground = ( backgroundTexture == null );
            boolean looksLikeEditorMode = ( changeCount > 1 );
            
            if ( !reloadBackground && looksLikeEditorMode && ( ( backgroundTexture.getWidth() != width ) || ( backgroundTexture.getHeight() != height ) ) )
                reloadBackground = true;
            
            if ( reloadBackground )
            {
                try
                {
                    backgroundTexture = image.getScaledTextureImage( width, height );
                    backgroundScaleX = (float)width / (float)image.getBaseWidth();
                    backgroundScaleY = (float)height / (float)image.getBaseHeight();
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
    }
    
    public final int getWidth()
    {
        int width = widget.getEffectiveWidth();
        
        int borderLW = widget.getBorder().getInnerLeftWidth();
        int borderRW = widget.getBorder().getInnerRightWidth();
        
        int paddingL = widget.getBorder().getPaddingLeft();
        int paddingR = widget.getBorder().getPaddingRight();
        
        width = width - ( borderLW + borderRW ) + ( paddingL + paddingR );
        
        return ( width );
    }
    
    public final int getHeight()
    {
        int height = widget.getEffectiveHeight();
        
        int borderTH = widget.getBorder().getInnerTopHeight();
        int borderBH = widget.getBorder().getInnerBottomHeight();
        
        int paddingT = widget.getBorder().getPaddingTop();
        int paddingB = widget.getBorder().getPaddingBottom();
        
        height = height - ( borderTH + borderBH ) + ( paddingT + paddingB );
        
        return ( height );
    }
    
    void onPropertyValueChanged( Widget widget, BackgroundType newBGType, String oldValue, String newValue )
    {
        changeCount++;
        
        int width = getWidth();
        int height = getHeight();
        
        float deltaScaleX = 1.0f;
        float deltaScaleY = 1.0f;
        
        if ( newBGType.isColor() )
        {
            if ( backgroundTexture != null )
            {
                ImageTemplate it = TextureManager.getImage( oldValue );
                
                deltaScaleX = (float)width / (float)it.getBaseWidth();
                deltaScaleY = (float)height / (float)it.getBaseHeight();
            }
            
            backgroundTexture = null;
        }
        else
        {
            float oldBgScaleX = 1.0f;
            float oldBgScaleY = 1.0f;
            
            boolean needsScaleChange = false;
            if ( backgroundTexture != null )
            {
                needsScaleChange = true;
                
                ImageTemplate it = TextureManager.getImage( oldValue );
                
                oldBgScaleX = (float)width / (float)it.getBaseWidth();
                oldBgScaleY = (float)height / (float)it.getBaseHeight();
            }
            
            backgroundTexture = null;
            
            if ( needsScaleChange )
            {
                ImageTemplate it = TextureManager.getImage( newValue );
                
                float scaleX = (float)width / (float)it.getBaseWidth();
                float scaleY = (float)height / (float)it.getBaseHeight();
                
                deltaScaleX = oldBgScaleX / scaleX;
                deltaScaleY = oldBgScaleY / scaleY;
            }
            else
            {
                deltaScaleX = -1f;
                deltaScaleY = -1f;
            }
        }
        
        widget.onBackgroundChanged( deltaScaleX, deltaScaleY );
        widget.forceAndSetDirty();
    }
    
    /**
     * 
     * @param widget
     */
    void onWidgetSizeChanged( Widget widget )
    {
        backgroundTexture = null;
    }
    
    /**
     * Gets the {@link Color} from this {@link BackgroundProperty}.
     * The result is only valid, if the {@link BackgroundType} ({@link #getBackgroundType()}) is {@value BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link BackgroundProperty}.
     */
    public final TextureImage2D getTexture()
    {
        if ( !property.getBackgroundType().isImage() )
            return ( null );
        
        if ( backgroundTexture == null )
        {
            loadBackgroundImage( property.getImageValue(), getWidth(), getHeight() );
        }
        
        return ( backgroundTexture );
    }
    
    /**
     * Gets the factor, by which the background image has been scaled to fit the area.
     * 
     * @return the factor, by which the background image has been scaled to fit the area.
     */
    public final float getBackgroundScaleX()
    {
        if ( property.getBackgroundType().isColor() )
            return ( 1.0f );
        
        if ( backgroundTexture == null )
        {
            loadBackgroundImage( property.getImageValue(), getWidth(), getHeight() );
        }
        
        return ( backgroundScaleX );
    }
    
    /**
     * Gets the factor, by which the background image has been scaled to fit the area.
     * 
     * @return the factor, by which the background image has been scaled to fit the area.
     */
    public final float getBackgroundScaleY()
    {
        if ( property.getBackgroundType().isColor() )
            return ( 1.0f );
        
        if ( backgroundTexture == null )
        {
            loadBackgroundImage( property.getImageValue(), getWidth(), getHeight() );
        }
        
        return ( backgroundScaleY );
    }
    
    public final boolean valueEquals( Color color )
    {
        if ( !property.getBackgroundType().isColor() )
            return ( false );
        
        return ( getColor().equals( color ) );
    }
    
    public WidgetBackground( final Widget widget, BackgroundProperty property )
    {
        this.widget = widget;
        this.property = property;
    }
}
