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

import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
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
    private static final boolean isEditorMode = __EDPrivilegedAccess.isEditorMode;
    
    private final Widget widget;
    private final BackgroundProperty property;
    
    private int changeCount = 0;
    
    private float backgroundScaleX = 1.0f;
    private float backgroundScaleY = 1.0f;
    
    private TextureImage2D backgroundTexture = null;
    private boolean bgTexDirty = true;
    private TextureImage2D mergedBackgroundTexture = null;
    private boolean mergedBgTexDirty = true;
    
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
     * The result is only valid, if the {@link BackgroundType} ({@link #getType()}) is {@link BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link WidgetBackground}.
     */
    public final Color getColor()
    {
        if ( !property.getBackgroundType().isColor() )
            return ( null );
        
        return ( property.getColorValue() );
    }
    
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
            boolean looksLikeEditorMode = ( changeCount > 1 );
            
            boolean reloadBackground = bgTexDirty;
            if ( !bgTexDirty && looksLikeEditorMode && ( ( backgroundTexture == null ) || ( backgroundTexture.getWidth() != width ) || ( backgroundTexture.getHeight() != height ) ) )
                reloadBackground = true;
            
            if ( reloadBackground )
            {
                try
                {
                    backgroundTexture = image.getScaledTextureImage( width, height, backgroundTexture, isEditorMode );
                    bgTexDirty = false;
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
                
                backgroundScaleX = (float)width / (float)image.getBaseWidth();
                backgroundScaleY = (float)height / (float)image.getBaseHeight();
            }
        }
    }
    
    public final int getWidth()
    {
        return ( widget.getEffectiveWidth() - widget.getBorder().getInnerLeftWidthWOPadding() - widget.getBorder().getInnerRightWidthWOPadding() );
    }
    
    public final int getHeight()
    {
        return ( widget.getEffectiveHeight() - widget.getBorder().getInnerTopHeightWOPadding() - widget.getBorder().getInnerBottomHeightWOPadding() );
    }
    
    void onPropertyValueChanged( Widget widget, BackgroundType oldBGType, BackgroundType newBGType, String oldValue, String newValue )
    {
        changeCount++;
        
        int width = getWidth();
        int height = getHeight();
        
        float deltaScaleX = -1.0f;
        float deltaScaleY = -1.0f;
        
        if ( newBGType.isColor() )
        {
            if ( oldBGType.isImage() )
            {
                ImageTemplate it = TextureManager.getImage( oldValue );
                
                deltaScaleX = (float)width / (float)it.getBaseWidth();
                deltaScaleY = (float)height / (float)it.getBaseHeight();
            }
            
            backgroundTexture = null;
            
            bgTexDirty = true;
            mergedBgTexDirty = true;
        }
        else if ( newBGType.isImage() )
        {
            if ( oldBGType.isColor() )
            {
                ImageTemplate it = TextureManager.getImage( newValue );
                
                deltaScaleX = (float)it.getBaseWidth() / (float)width;
                deltaScaleY = (float)it.getBaseHeight() / (float)height;
            }
            else if ( oldBGType.isImage() )
            {
                ImageTemplate it = TextureManager.getImage( oldValue );
                
                float oldBgScaleX = (float)width / (float)it.getBaseWidth();
                float oldBgScaleY = (float)height / (float)it.getBaseHeight();
                
                it = TextureManager.getImage( newValue );
                
                float newScaleX = (float)width / (float)it.getBaseWidth();
                float newScaleY = (float)height / (float)it.getBaseHeight();
                
                deltaScaleX = oldBgScaleX / newScaleX;
                deltaScaleY = oldBgScaleY / newScaleY;
            }
            
            backgroundTexture = null;
            
            bgTexDirty = true;
            mergedBgTexDirty = true;
        }
        
        widget.onBackgroundChanged( deltaScaleX, deltaScaleY );
        widget.forceAndSetDirty( true );
    }
    
    /**
     * 
     * @param widget
     */
    void onWidgetSizeChanged( Widget widget )
    {
        bgTexDirty = true;
        mergedBgTexDirty = true;
    }
    
    /**
     * Gets the {@link TextureImage2D} from this {@link BackgroundProperty}.
     * The result is only valid, if the {@link BackgroundType} ({@link #getType()}) is {@link BackgroundType#IMAGE}.
     * 
     * @return the {@link TextureImage2D} from this {@link BackgroundProperty}.
     */
    public final TextureImage2D getTexture()
    {
        if ( !property.getBackgroundType().isImage() )
            return ( null );
        
        if ( bgTexDirty )
        {
            loadBackgroundImage( property.getImageValue(), getWidth(), getHeight() );
        }
        
        return ( backgroundTexture );
    }
    
    void setMergedBGDirty()
    {
        this.mergedBgTexDirty = true;
    }
    
    private static boolean needsTexture( AssembledWidget widget, Color backgroundColor )
    {
        for ( int i = 0; i < widget.getNumParts(); i++ )
        {
            Widget part = widget.getPart( i );
            WidgetBackground bg = part.getBackground();
            
            if ( ( ( bg != null ) && bg.getType().isImage() ) || part.overridesDrawBackground )
                return ( true );
            
            if ( ( bg != null ) && bg.getType().isColor() && ( bg.getColor().getAlpha() > 0 ) && !bg.getColor().equals( backgroundColor ) )
                return ( true );
            
            if ( part instanceof AssembledWidget )
            {
                if ( needsTexture( (AssembledWidget)part, backgroundColor ) )
                    return ( true );
            }
        }
        
        return ( false );
    }
    
    private void createAndUpdateMergedBackgroundTexture( LiveGameData gameData, boolean isEditorMode )
    {
        int width = getWidth();
        int height = getHeight();
        
        mergedBackgroundTexture = TextureImage2D.getOrCreateDrawTexture( width, height, true, mergedBackgroundTexture, isEditorMode );
        
        widget.drawBackground_( gameData, isEditorMode, mergedBackgroundTexture, 0, 0, width, height, true );
        mergedBgTexDirty = false;
    }
    
    /**
     * 
     * @param gameData
     * @param isEditorMode
     */
    void updateMergedBackground( LiveGameData gameData, boolean isEditorMode )
    {
        if ( mergedBgTexDirty )
        {
            if ( property.getBackgroundType().isColor() )
            {
                if ( widget.overridesDrawBackground )
                {
                    createAndUpdateMergedBackgroundTexture( gameData, isEditorMode );
                }
                else if ( ( widget instanceof AssembledWidget ) && needsTexture( (AssembledWidget)widget, getColor() ) )
                {
                    createAndUpdateMergedBackgroundTexture( gameData, isEditorMode );
                }
                else
                {
                    mergedBackgroundTexture = null;
                    mergedBgTexDirty = false;
                }
            }
            else //if ( property.getBackgroundType().isImage() )
            {
                if ( widget.overridesDrawBackground )
                {
                    createAndUpdateMergedBackgroundTexture( gameData, isEditorMode );
                }
                else if ( ( widget instanceof AssembledWidget ) && needsTexture( (AssembledWidget)widget, null ) )
                {
                    createAndUpdateMergedBackgroundTexture( gameData, isEditorMode );
                }
                else
                {
                    mergedBackgroundTexture = null;
                    mergedBgTexDirty = false;
                }
            }
        }
    }
    
    /**
     * Gets the merged background, composed of the backgrounds of this (possibly assembled) {@link Widget} and the parts of this Widget.
     * If this {@link Widget} is not an {@link AssembledWidget} and has a background color (no image)
     * and doesn't override the {@link Widget#drawBackground(net.ctdp.rfdynhud.gamedata.LiveGameData, net.ctdp.rfdynhud.editor.EditorPresets, TextureImage2D, int, int, int, int, boolean)} method,
     * this method returns <code>null</code>.
     * 
     * @return the marged background texture or <code>null</code>.
     */
    public final TextureImage2D getMergedTexture()
    {
        return ( mergedBackgroundTexture );
    }
    
    /**
     * Gets the factor, by which the background image has been scaled to fit the area.
     * Returns 1.0, if a background color is used.
     * 
     * @return the factor, by which the background image has been scaled to fit the area.
     */
    public final float getScaleX()
    {
        if ( property.getBackgroundType().isColor() )
            return ( 1.0f );
        
        if ( bgTexDirty )
        {
            loadBackgroundImage( property.getImageValue(), getWidth(), getHeight() );
        }
        
        return ( backgroundScaleX );
    }
    
    /**
     * Gets the factor, by which the background image has been scaled to fit the area.
     * Returns 1.0, if a background color is used.
     * 
     * @return the factor, by which the background image has been scaled to fit the area.
     */
    public final float getScaleY()
    {
        if ( property.getBackgroundType().isColor() )
            return ( 1.0f );
        
        if ( bgTexDirty )
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
