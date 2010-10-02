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
package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.WidgetToPropertyForwarder;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class Size implements AbstractSize
{
    private static final float PIXEL_OFFSET = 10f;
    private static final float PIXEL_OFFSET_CHECK_POSITIVE = +PIXEL_OFFSET - 0.001f;
    private static final float PIXEL_OFFSET_CHECK_NEGATIVE = -PIXEL_OFFSET + 0.001f;
    
    private float width;
    private float height;
    
    private int bakedWidth = -1;
    private int bakedHeight = -1;
    
    /*final*/ Widget widget;
    private final boolean isGlobalSize;
    
    /*
    public final Widget getWidget()
    {
        return ( widget );
    }
    */
    
    public final boolean isGlobalSize()
    {
        return ( isGlobalSize );
    }
    
    /**
     * Gets this Widget's width. If it is a negative number, the actual width is (screen_width - width).
     * 
     * @return this Widget's width.
     */
    private final float getWidth()
    {
        return ( width );
    }
    
    /**
     * Gets this Widget's height. If it is a negative number, the actual height is (screen_height - height).
     * 
     * @return this Widget's height.
     */
    private final float getHeight()
    {
        return ( height );
    }
    
    public final boolean isNegativeWidth()
    {
        return ( width <= 0f );
    }
    
    public final boolean isNegativeHeight()
    {
        return ( height <= 0f );
    }
    
    private static final boolean isNegPixelValue( float v )
    {
        return ( v < PIXEL_OFFSET_CHECK_NEGATIVE );
    }
    
    private static final boolean isPosPixelValue( float v )
    {
        return ( v > PIXEL_OFFSET_CHECK_POSITIVE );
    }
    
    private static final boolean isPixelValue( float v )
    {
        return ( ( v < PIXEL_OFFSET_CHECK_NEGATIVE ) || ( v > PIXEL_OFFSET_CHECK_POSITIVE ) );
    }
    
    private final float getMinWidth()
    {
        if ( isGlobalSize )
            return ( widget.getMinWidth( null, false ) );
        
        return ( 10f );
    }
    
    private final float getMinHeight()
    {
        if ( isGlobalSize )
            return ( widget.getMinHeight( null, false ) );
        
        return ( 10f );
    }
    
    private final float getScaleWidth()
    {
        if ( isGlobalSize )
            return ( widget.getConfiguration().getGameResolution().getViewportWidth() );
        
        return ( widget.getInnerSize().getEffectiveWidth() );
    }
    
    private final float getScaleHeight()
    {
        if ( isGlobalSize )
            return ( widget.getConfiguration().getGameResolution().getViewportHeight() );
        
        return ( widget.getInnerSize().getEffectiveHeight() );
    }
    
    private final float getHundretPercentWidth()
    {
        if ( isGlobalSize )
            return ( widget.getConfiguration().getGameResolution().getViewportHeight() * 4 / 3 );
        
        return ( widget.getInnerSize().getEffectiveWidth() );
    }
    
    private void applyLimits()
    {
        unbake();
        
        if ( widget.getConfiguration() == null )
            return;
        
        if ( isPosPixelValue( width ) )
            width = +PIXEL_OFFSET + Math.min( width - PIXEL_OFFSET, getScaleWidth() );
        else if ( isNegPixelValue( width ) )
            width = -PIXEL_OFFSET + Math.max( width + PIXEL_OFFSET, -getScaleWidth() );
        else if ( width > 0f )
            width = Math.max( 0f, Math.min( width, +1.0f ) );
        else if ( width <= 0f )
            width = Math.min( 0f, Math.max( width, -1.0f ) );
        
        if ( isPosPixelValue( height ) )
            height = +PIXEL_OFFSET + Math.min( height - PIXEL_OFFSET, getScaleHeight() );
        else if ( isNegPixelValue( height ) )
            height = -PIXEL_OFFSET + Math.max( height + PIXEL_OFFSET, -getScaleHeight() );
        else if ( height > 0f )
            height = Math.max( 0f, Math.min( height, +1.0f ) );
        else if ( height <= 0f )
            height = Math.min( 0f, Math.max( height, -1.0f ) );
    }
    
    /**
     * Sets this {@link Widget}'s size. (only works for non-fixed-sized {@link Widget}s)
     * 
     * @param width
     * @param height
     */
    private boolean set( float width, float height )
    {
        /*
        if ( widget.getConfiguration() != null )
        {
            if ( isPosPixelValue( width ) )
                width = Math.max( width, getMinWidth() );
            else if ( isNegPixelValue( width ) )
                width = -Math.max( -width, -getMinWidth() );
            else if ( width > 0f )
                width = +PERCENT_OFFSET + Math.max( width - PERCENT_OFFSET, getMinWidth() / getHundretPercentWidth() );
            else if ( width <= 0f )
                width = -PERCENT_OFFSET + Math.max( width + PERCENT_OFFSET, ( getMinWidth() / getScaleWidth() ) - 1.0f );
            
            if ( isPosPixelValue( height ) )
                height = Math.max( height, getMinHeight() );
            else if ( isNegPixelValue( height ) )
                height = -Math.max( -height, -getMinHeight() );
            else if ( height > 0f )
                height = +PERCENT_OFFSET + Math.max( height - PERCENT_OFFSET, getMinHeight() / getScaleHeight() );
            else if ( height <= 0f )
                height = -PERCENT_OFFSET + Math.min( height + PERCENT_OFFSET, -( getMinHeight() / getScaleHeight() ) );
        }
        */
        
        unbake();
        
        boolean changed = false;
        
        if ( ( width != this.width ) || ( height != this.height ) )
        {
            boolean b = ( widget.getConfiguration() != null );
            int oldW = b ? getEffectiveWidth() : 0;
            int oldH = b ? getEffectiveHeight() : 0;
            
            this.width = width;
            this.height = height;
            
            applyLimits();
            
            widget.forceAndSetDirty( true );
            
            if ( b )
            {
                int newW = getEffectiveWidth();
                int newH = getEffectiveHeight();
                
                if ( oldW != newW || oldH != newH )
                    __WPrivilegedAccess.onSizeChanged( oldW, oldH, newW, newH, widget );
            }
            
            changed = true;
        }
        //widget.setDirtyFlag();
        
        return ( changed );
    }
    
    /**
     * Sets this {@link Widget}'s width. (only works for non-fixed-sized {@link Widget}s)
     * 
     * @param width
     */
    private boolean setWidth( float width )
    {
        return ( set( width, getHeight() ) );
    }
    
    /**
     * Sets this {@link Widget}'s height. (only works for non-fixed-sized {@link Widget}s)
     * 
     * @param height
     */
    private boolean setHeight( float height )
    {
        return ( set( getWidth(), height ) );
    }
    
    /**
     * Sets this {@link Widget}'s size in absolute pixel coordinates. (only works for non-fixed-sized {@link Widget}s)
     * 
     * @param width the new absolute pixel width
     * @param height the new absolute pixel height
     * 
     * @return changed?
     */
    public final boolean setEffectiveSize( int width, int height )
    {
        float scaleW = getScaleWidth();
        float scaleH = getScaleHeight();
        
        width = Math.max( width, (int)getMinWidth() );
        height = Math.max( height, (int)getMinHeight() );
        
        if ( !isPixelValue( this.width ) )
        {
            width = Math.min( width, (int)getHundretPercentWidth() );
        }
        
        if ( this.width <= 0f )
            width -= (int)scaleW;
        
        if ( this.height <= 0f )
            height -= (int)scaleH;
        
        float newW, newH;
        
        if ( isPixelValue( this.width ) )
        {
            if ( isPixelValue( this.height ) )
            {
                newW = ( width <= 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + width;
                newH = ( height <= 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + height;
            }
            else
            {
                newW = ( width <= 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + width;
                newH = (float)height / scaleH;
            }
        }
        else
        {
            float hundretPercentW = ( this.width <= 0f ) ? scaleW : getHundretPercentWidth();
            
            if ( isPixelValue( this.height ) )
            {
                newW = (float)width / hundretPercentW;
                newH = ( height <= 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + height;
            }
            else
            {
                newW = (float)width / hundretPercentW;
                newH = (float)height / scaleH;
            }
        }
        
        boolean changed = set( newW, newH );
        
        applyLimits();
        
        return ( changed );
    }
    
    /**
     * Gets the effective Widget's width. If {@link #getWidth()} returns a
     * negative number, the effective width is (screen_width - width).
     * 
     * @return the effective Widget's width.
     */
    @Override
    public final int getEffectiveWidth()
    {
        if ( bakedWidth >= 0 )
            return ( bakedWidth );
        
        float scaleW = getScaleWidth();
        
        if ( isPosPixelValue( width ) )
            return ( (int)Math.max( getMinWidth(), width - PIXEL_OFFSET ) );
        
        if ( isNegPixelValue( width ) )
            return ( (int)Math.max( getMinWidth(), scaleW + width + PIXEL_OFFSET ) );
        
        if ( width > 0f )
            return ( Math.round( Math.max( getMinWidth(), width * getHundretPercentWidth() ) ) );
        
        return ( Math.round( Math.max( getMinWidth(), scaleW + ( width * scaleW ) ) ) );
    }
    
    /**
     * Gets the effective Widget's height. If {@link #getHeight()} returns a
     * negative number, the effective height is (screen_height - height).
     * 
     * @return the effective Widget's height.
     */
    @Override
    public final int getEffectiveHeight()
    {
        if ( bakedHeight >= 0 )
            return ( bakedHeight );
        
        float scaleH = getScaleHeight();
        
        if ( isPosPixelValue( height ) )
            return ( (int)Math.max( getMinHeight(), height - PIXEL_OFFSET ) );
        
        if ( isNegPixelValue( height ) )
            return ( (int)Math.max( getMinHeight(), scaleH + height + PIXEL_OFFSET ) );
        
        if ( height > 0f )
            return ( Math.round( Math.max( getMinHeight(), height * scaleH ) ) );
        
        return ( Math.round( Math.max( getMinHeight(), scaleH + ( height * scaleH ) ) ) );
    }
    
    public void unbake()
    {
        bakedWidth = -1;
        bakedHeight = -1;
    }
    
    public void bake()
    {
        unbake();
        
        bakedWidth = getEffectiveWidth();
        bakedHeight = getEffectiveHeight();
    }
    
    public boolean isBaked()
    {
        return ( bakedWidth >= 0 );
    }
    
    public Size setWidthToPercents()
    {
        if ( isPixelValue( width ) )
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( width > 0f )
                this.width = +PIXEL_OFFSET * 0.9f;
            else
                this.width = -PIXEL_OFFSET * 0.9f;
            
            setEffectiveSize( effW, effH );
        }
        
        return ( this );
    }
    
    public Size setWidthToPixels()
    {
        if ( !isPixelValue( height ) )
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( width > 0f )
                this.width = +PIXEL_OFFSET + 10000f;
            else
                this.width = -PIXEL_OFFSET - 10000f;
            
            setEffectiveSize( effW, effH );
        }
        
        return ( this );
    }
    
    public Size flipWidthPercentagePx()
    {
        if ( isPixelValue( width ) )
            setWidthToPercents();
        else
            setWidthToPixels();
        
        return ( this );
    }
    
    public Size setHeightToPercents()
    {
        if ( isPixelValue( height ) )
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( height > 0f )
                this.height = +PIXEL_OFFSET * 0.9f;
            else
                this.height = -PIXEL_OFFSET * 0.9f;
            
            setEffectiveSize( effW, effH );
        }
        
        return ( this );
    }
    
    public Size setHeightToPixels()
    {
        if ( !isPixelValue( height ) )
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( height > 0f )
                this.height = +PIXEL_OFFSET + 10000f;
            else
                this.height = -PIXEL_OFFSET - 10000f;
            
            setEffectiveSize( effW, effH );
        }
        
        return ( this );
    }
    
    public Size flipHeightPercentagePx()
    {
        if ( isPixelValue( height ) )
            setHeightToPercents();
        else
            setHeightToPixels();
        
        return ( this );
    }
    
    public Size flipWidthSign()
    {
        int gameResX = widget.getConfiguration().getGameResolution().getViewportWidth();
        
        if ( isNegPixelValue( width ) )
            width = +PIXEL_OFFSET + gameResX + ( width + PIXEL_OFFSET );
        else if ( isPosPixelValue( width ) )
            width = -PIXEL_OFFSET + ( width - PIXEL_OFFSET ) - gameResX;
        else if ( width < 0f )
            width = + ( 1.0f + width ) * ( getScaleWidth() / getHundretPercentWidth() );
        else if ( width > 0f )
            width = - 1.0f + ( width / ( getScaleWidth() / getHundretPercentWidth() ) );
        
        applyLimits();
        
        widget.forceAndSetDirty( true );
        
        return ( this );
    }
    
    public Size flipHeightSign()
    {
        int gameResY = widget.getConfiguration().getGameResolution().getViewportHeight();
        
        if ( isNegPixelValue( height ) )
            height = +PIXEL_OFFSET + gameResY + ( height + PIXEL_OFFSET );
        else if ( isPosPixelValue( height ) )
            height = -PIXEL_OFFSET + ( height - PIXEL_OFFSET ) - gameResY;
        else if ( height < 0f )
            height = 1.0f + height;
        else if ( height > 0f )
            height = height - 1.0f;
        
        applyLimits();
        
        widget.forceAndSetDirty( true );
        
        return ( this );
    }
    
    public static float parseValue( String value, boolean defaultPerc )
    {
        boolean isPerc = value.endsWith( "%" );
        boolean isPx = value.endsWith( "px" );
        
        if ( !isPerc && !isPx )
        {
            if ( defaultPerc )
            {
                value += "%";
                isPerc = true;
            }
            else
            {
                value += "px";
                isPx = true;
            }
        }
        
        if ( isPerc )
        {
            float f = Float.parseFloat( value.substring( 0, value.length() - 1 ) );
            
            return ( f / 100f );
        }
        
        if ( isPx )
        {
            float f = Float.parseFloat( value.substring( 0, value.length() - 2 ) );
            
            if ( f < 0f )
                return ( -PIXEL_OFFSET + f );
            
            return ( +PIXEL_OFFSET + f );
        }
        
        // Unreachable!
        return ( Float.parseFloat( value ) );
    }
    
    /*
    private float parseWidth( String value )
    {
        setWidth( parseValue( value ) );
        
        return ( getWidth() );
    }
    
    private float parseHeight( String value )
    {
        setHeight( parseValue( value ) );
        
        return ( getHeight() );
    }
    */
    
    public static String unparseValue( float value )
    {
        if ( isPosPixelValue( value ) )
            return ( String.valueOf( (int)( value - PIXEL_OFFSET ) ) + "px" );
        
        if ( isNegPixelValue( value ) )
            return ( String.valueOf( (int)( value + PIXEL_OFFSET ) ) + "px" );
        
        return ( String.valueOf( value * 100f ) + "%" );
    }
    
    /*
    private String unparseWidth()
    {
        return ( unparseValue( getWidth() ) );
    }
    
    private String unparseHeight()
    {
        return ( unparseValue( getHeight() ) );
    }
    */
    
    private static final boolean propExistsWithName( Property prop, String name, String nameForDisplay )
    {
        if ( prop == null )
            return ( false );
        
        if ( !prop.getName().equals( name ) )
            return ( false );
        
        if ( ( nameForDisplay == null ) && !prop.getName().equals( prop.getNameForDisplay() ) )
            return ( false );
        
        return ( true );
    }
    
    /**
     * 
     * @param width the new width
     */
    protected void onWidthPropertySet( float width )
    {
    }
    
    private PosSizeProperty widthProp = null;
    
    public PosSizeProperty getWidthProperty( String name, String nameForDisplay )
    {
        if ( !propExistsWithName( widthProp, name, nameForDisplay ) )
        {
            boolean ro = isGlobalSize ? widget.hasFixedSize() : false;
            
            widthProp = new PosSizeProperty( widget, name, nameForDisplay, ro, true )
            {
                @Override
                public boolean isPercentage()
                {
                    return ( !isPixelValue( width ) );
                }
                
                @Override
                public void setValue( Object value )
                {
                    float width = ( (Number)value ).floatValue();
                    
                    set( width, getHeight() );
                    
                    onWidthPropertySet( width );
                }
                
                @Override
                public Object getValue()
                {
                    return ( getWidth() );
                }
                
                @Override
                public void onButtonClicked( Object button )
                {
                    flipWidthSign();
                }
                
                @Override
                public void onButton2Clicked( Object button )
                {
                    flipWidthPercentagePx();
                }
                
                @Override
                public Boolean quoteValueInConfigurationFile()
                {
                    return ( false );
                }
                
                @Override
                public Object getValueForConfigurationFile()
                {
                    return ( unparseValue( getWidth() ) );
                }
                
                @Override
                public void loadValue( String value )
                {
                    if ( !value.endsWith( "%" ) && !value.endsWith( "px" ) )
                        value += "px";
                    
                    setWidth( parseValue( value, !isPixelValue( width ) ) );
                }
            };
        }
        
        return ( widthProp );
    }
    
    public PosSizeProperty getWidthProperty( String name )
    {
        return ( getWidthProperty( name, name ) );
    }
    
    /**
     * 
     * @param height the new height
     */
    protected void onHeightPropertySet( float height )
    {
    }
    
    private PosSizeProperty heightProp = null;
    
    public PosSizeProperty getHeightProperty( String name, String nameForDisplay )
    {
        if ( !propExistsWithName( heightProp, name, nameForDisplay ) )
        {
            boolean ro = isGlobalSize ? widget.hasFixedSize() : false;
            
            heightProp = new PosSizeProperty( widget, name, nameForDisplay, ro, true )
            {
                @Override
                public boolean isPercentage()
                {
                    return ( !isPixelValue( height ) );
                }
                
                @Override
                public void setValue( Object value )
                {
                    float height = ( (Number)value ).floatValue();
                    
                    set( getWidth(), height );
                    
                    onHeightPropertySet( height );
                }
                
                @Override
                public Object getValue()
                {
                    return ( getHeight() );
                }
                
                @Override
                public void onButtonClicked( Object button )
                {
                    flipHeightSign();
                }
                
                @Override
                public void onButton2Clicked( Object button )
                {
                    flipHeightPercentagePx();
                }
                
                @Override
                public Boolean quoteValueInConfigurationFile()
                {
                    return ( false );
                }
                
                @Override
                public Object getValueForConfigurationFile()
                {
                    return ( unparseValue( getHeight() ) );
                }
                
                @Override
                public void loadValue( String value )
                {
                    if ( !value.endsWith( "%" ) && !value.endsWith( "px" ) )
                        value += "px";
                    
                    setHeight( parseValue( value, !isPixelValue( height ) ) );
                }
            };
        }
        
        return ( heightProp );
    }
    
    public PosSizeProperty getHeightProperty( String name )
    {
        return ( getHeightProperty( name, name ) );
    }
    
    protected Size( Widget widget, boolean isGlobalSize, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        this.widget = widget;
        this.isGlobalSize = isGlobalSize;
        
        this.width = widthPercent ? width * 0.01f : PIXEL_OFFSET + width;
        this.height = heightPercent ? height * 0.01f : PIXEL_OFFSET + height;
    }
    
    /**
     * Create a new size property for sizes local to a Widget's area.
     * 
     * @param widget the owning {@link Widget}.
     * @param width the new width value
     * @param widthPercent interpret 'width' as percents?
     * @param height the new height value
     * @param heightPercent interpret 'height' as percents?
     * 
     * @return the new Size.
     */
    public static final Size newLocalSize( Widget widget, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        return ( new Size( widget, false, width, widthPercent, height, heightPercent ) );
    }
    
    /**
     * Create a new size property for global positions on the whole screen area.
     * 
     * @param widget the owning {@link Widget}.
     * @param width the new width value
     * @param widthPercent interpret 'width' as percents?
     * @param height the new height value
     * @param heightPercent interpret 'height' as percents?
     * 
     * @return the new Size.
     */
    public static final Size newGlobalSize( Widget widget, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        return ( new Size( widget, true, width, widthPercent, height, heightPercent ) );
    }
    
    protected Size( WidgetToPropertyForwarder w2pf, boolean isGlobalSize, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        this.widget = null;
        this.isGlobalSize = isGlobalSize;
        
        this.width = widthPercent ? width * 0.01f : PIXEL_OFFSET + width;
        this.height = heightPercent ? height * 0.01f : PIXEL_OFFSET + height;
        
        __PropsPrivilegedAccess.addSize( w2pf, this );
    }
    
    /**
     * Create a new size property for sizes local to a Widget's area.
     * 
     * @param w2pf the object to use as a placeholder for the Widget to come
     * @param width the new width value
     * @param widthPercent interpret 'width' as percents?
     * @param height the new height value
     * @param heightPercent interpret 'height' as percents?
     * 
     * @return the new Size.
     */
    public static final Size newLocalSize( WidgetToPropertyForwarder w2pf, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        return ( new Size( w2pf, false, width, widthPercent, height, heightPercent ) );
    }
    
    /**
     * Create a new size property for global positions on the whole screen area.
     * 
     * @param w2pf the object to use as a placeholder for the Widget to come
     * @param width the new width value
     * @param widthPercent interpret 'width' as percents?
     * @param height the new height value
     * @param heightPercent interpret 'height' as percents?
     * 
     * @return the new Size.
     */
    public static final Size newGlobalSize( WidgetToPropertyForwarder w2pf, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        return ( new Size( w2pf, true, width, widthPercent, height, heightPercent ) );
    }
}
