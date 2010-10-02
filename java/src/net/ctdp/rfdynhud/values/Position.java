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

import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;
import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.properties.WidgetToPropertyForwarder;
import net.ctdp.rfdynhud.properties.__PropsPrivilegedAccess;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

/**
 * The {@link Position} class is an abstraction of a positional value tuple.
 * It can be used with percentual values or absolute pixels and can be global or Widget local.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Position
{
    private static final float PIXEL_OFFSET = 10f;
    private static final float PIXEL_OFFSET_CHECK_POSITIVE = +PIXEL_OFFSET - 0.001f;
    private static final float PIXEL_OFFSET_CHECK_NEGATIVE = -PIXEL_OFFSET + 0.001f;
    
    private RelativePositioning positioning;
    private float x;
    private float y;
    
    private int bakedX = -1;
    private int bakedY = -1;
    
    private final AbstractSize size;
    /*final*/ Widget widget;
    private final boolean isGlobalPosition;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    public final boolean isGlobalPosition()
    {
        return ( isGlobalPosition );
    }
    
    public final RelativePositioning getPositioning()
    {
        return ( positioning );
    }
    
    /**
     * Gets the current x-location of this Widget.
     * 
     * @see #getPositioning()
     * 
     * @return the current x-location of this Widget.
     */
    private final float getX()
    {
        return ( x );
    }
    
    /**
     * Gets the current y-location of this Widget.
     * 
     * @see #getPositioning()
     * 
     * @return the current y-location of this Widget.
     */
    private final float getY()
    {
        return ( y );
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
    
    private final float getScaleWidth()
    {
        if ( isGlobalPosition )
            return ( widget.getConfiguration().getGameResolution().getViewportWidth() );
        
        return ( widget.getInnerSize().getEffectiveWidth() );
    }
    
    private final float getScaleHeight()
    {
        if ( isGlobalPosition )
            return ( widget.getConfiguration().getGameResolution().getViewportHeight() );
        
        return ( widget.getInnerSize().getEffectiveHeight() );
    }
    
    private final float getHundretPercentWidth()
    {
        if ( isGlobalPosition )
            return ( widget.getConfiguration().getGameResolution().getViewportHeight() * 4 / 3 );
        
        return ( widget.getInnerSize().getEffectiveWidth() );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param positioning
     * @param x
     * @param y
     * 
     * @return changed?
     */
    private boolean set( RelativePositioning positioning, float x, float y )
    {
        if ( widget.getConfiguration() != null )
        {
            if ( positioning.isHCenter() )
            {
                if ( isNegPixelValue( x ) )
                    x = Math.max( -PIXEL_OFFSET - getScaleWidth() / 2f + ( isGlobalPosition ? size.getEffectiveWidth() / 2f : 0f ), x );
                else if ( isPosPixelValue( x ) )
                    x = Math.min( +PIXEL_OFFSET + getScaleWidth() / 2f - ( isGlobalPosition ? size.getEffectiveWidth() / 2f : 0f ), x );
                else if ( x < 0f )
                    x = Math.max( -0.5f + ( isGlobalPosition ? size.getEffectiveWidth() / 2f / getHundretPercentWidth() : 0f ), x );
                else if ( x > 0f )
                    x = Math.min( +0.5f - ( isGlobalPosition ? size.getEffectiveWidth() / 2f / getHundretPercentWidth() : 0f ), x );
            }
            else if ( isPixelValue( x ) )
            {
                x = Math.max( PIXEL_OFFSET, x );
            }
            else
            {
                x = Math.max( 0f, x );
            }
            
            if ( positioning.isVCenter() )
            {
                if ( isNegPixelValue( y ) )
                    y = Math.max( -PIXEL_OFFSET - getScaleHeight() / 2f + ( isGlobalPosition ? size.getEffectiveHeight() / 2f : 0f ), y );
                else if ( isPosPixelValue( y ) )
                    y = Math.min( +PIXEL_OFFSET + getScaleHeight() / 2f - ( isGlobalPosition ? size.getEffectiveHeight() / 2f : 0f ), y );
                else if ( y < 0f )
                    y = Math.max( -0.5f + ( isGlobalPosition ? size.getEffectiveHeight() / 2f / getScaleHeight() : 0f ), y );
                else if ( y > 0f )
                    y = Math.min( +0.5f - ( isGlobalPosition ? size.getEffectiveHeight() / 2f / getScaleHeight() : 0f ), y );
            }
            else if ( isPixelValue( y ) )
            {
                y = Math.max( PIXEL_OFFSET, y );
            }
            else
            {
                y = Math.max( 0f, y );
            }
        }
        
        unbake();
        
        boolean changed = false;
        
        if ( ( positioning != this.positioning ) || ( x != this.x ) || ( y != this.y ) )
        {
            RelativePositioning oldPositioning = this.positioning;
            boolean b = ( widget.getConfiguration() != null );
            int oldX = b ? getEffectiveX() : 0;
            int oldY = b ? getEffectiveY() : 0;
            
            this.positioning = positioning;
            
            this.x = x;
            this.y = y;
            
            if ( !__EDPrivilegedAccess.isEditorMode )
                widget.forceCompleteRedraw( true );
            widget.setDirtyFlag();
            
            if ( b )
            {
                int newX = getEffectiveX();
                int newY = getEffectiveY();
                
                if ( oldX != newX || oldY != newY )
                    __WPrivilegedAccess.onPositionChanged( oldPositioning, oldX, oldY, positioning, newX, newY, widget );
            }
            
            changed = true;
        }
        
        return ( changed );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param x
     * @param y
     */
    private final boolean set( float x, float y )
    {
        return ( set( getPositioning(), x, y ) );
    }
    
    /**
     * Sets this Widget's x-position.
     * 
     * @param x
     */
    private final boolean setX( float x )
    {
        return ( set( getPositioning(), x, getY() ) );
    }
    
    /**
     * Sets this Widget's y-position.
     * 
     * @param y
     */
    private final boolean setY( float y )
    {
        return ( set( getPositioning(), getX(), y ) );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param positioning the used {@link RelativePositioning}
     * @param x the absolute pixel x
     * @param y the absolute pixel y
     * 
     * @return changed?
     */
    public final boolean setEffectivePosition( RelativePositioning positioning, int x, int y )
    {
        float scaleW = getScaleWidth();
        float scaleH = getScaleHeight();
        
        if ( isGlobalPosition && !isPixelValue( this.x ) )
        {
            if ( positioning.isRight() )
                x = (int)Math.max( scaleW - getHundretPercentWidth() - widget.getSize().getEffectiveWidth(), x );
            else
                x = (int)Math.min( x, getHundretPercentWidth() );
        }
        
        if ( positioning.isVCenter() )
            y = y + ( size.getEffectiveHeight() - (int)scaleH ) / 2;
        else if ( positioning.isBottom() )
            y = (int)scaleH - y - (int)size.getEffectiveHeight();
        
        if ( positioning.isHCenter() )
            x = x + ( size.getEffectiveWidth() - (int)scaleW ) / 2;
        else if ( positioning.isRight() )
            x = (int)scaleW - x - size.getEffectiveWidth();
        
        float newX, newY;
        
        if ( isPixelValue( this.x ) )
        {
            if ( isPixelValue( this.y ) )
            {
                newX = ( x < 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + x;
                newY = ( y < 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + y;
            }
            else
            {
                newX = ( x < 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + x;
                newY = (float)y / scaleH;
            }
        }
        else if ( isPixelValue( this.y ) )
        {
            newX = (float)x / getHundretPercentWidth();
            newY = ( y < 0 ? -PIXEL_OFFSET : +PIXEL_OFFSET ) + y;
        }
        else
        {
            newX = (float)x / getHundretPercentWidth();
            newY = (float)y / scaleH;
        }
        
        return ( set( positioning, newX, newY ) );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param x the absolute pixel x
     * @param y the absolute pixel y
     * 
     * @return changed?
     */
    public final boolean setEffectivePosition( int x, int y )
    {
        return ( setEffectivePosition( getPositioning(), x, y ) );
    }
    
    /**
     * Gets the effective Widget's x-location using {@link #getPositioning()}.
     * 
     * @return the effective Widget's x-location.
     */
    public final int getEffectiveX()
    {
        if ( bakedX >= 0 )
            return ( bakedX );
        
        float scaleW = getScaleWidth();
        
        switch ( getPositioning() )
        {
            case TOP_LEFT:
            case CENTER_LEFT:
            case BOTTOM_LEFT:
                if ( isPosPixelValue( x ) )
                    return ( (int)( x - PIXEL_OFFSET ) );
                
                if ( isNegPixelValue( x ) )
                    return ( (int)( x + PIXEL_OFFSET ) );
                
                return ( Math.round( x * getHundretPercentWidth() ) );
            case TOP_CENTER:
            case CENTER_CENTER:
            case BOTTOM_CENTER:
                if ( isPosPixelValue( x ) )
                    return ( ( (int)scaleW - size.getEffectiveWidth() ) / 2 + (int)( x - PIXEL_OFFSET ) );
                
                if ( isNegPixelValue( x ) )
                    return ( ( (int)scaleW - size.getEffectiveWidth() ) / 2 + (int)( x + PIXEL_OFFSET ) );
                
                return ( ( (int)scaleW - size.getEffectiveWidth() ) / 2 + Math.round( x * getHundretPercentWidth() ) );
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                if ( isPosPixelValue( x ) )
                    return ( (int)scaleW - (int)( x - PIXEL_OFFSET ) - size.getEffectiveWidth() );
                
                if ( isNegPixelValue( x ) )
                    return ( (int)scaleW - (int)( x + PIXEL_OFFSET ) - size.getEffectiveWidth() );
                
                return ( (int)scaleW - Math.round( x * getHundretPercentWidth() ) - size.getEffectiveWidth() );
        }
        
        // Unreachable code!
        return ( -1 );
    }
    
    /**
     * Gets the effective Widget's y-location using {@link #getPositioning()}.
     * 
     * @return the effective Widget's y-location.
     */
    public final int getEffectiveY()
    {
        if ( bakedY >= 0 )
            return ( bakedY );
        
        float scaleH = getScaleHeight();
        
        switch ( getPositioning() )
        {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                if ( isPosPixelValue( y ) )
                    return ( (int)( y - PIXEL_OFFSET ) );
                
                if ( isNegPixelValue( y ) )
                    return ( (int)( y + PIXEL_OFFSET ) );
                
                return ( Math.round( y * scaleH ) );
            case CENTER_LEFT:
            case CENTER_CENTER:
            case CENTER_RIGHT:
                if ( isPosPixelValue( y ) )
                    return ( ( (int)scaleH - size.getEffectiveHeight() ) / 2 + (int)( y - PIXEL_OFFSET ) );
                
                if ( isNegPixelValue( y ) )
                    return ( ( (int)scaleH - size.getEffectiveHeight() ) / 2 + (int)( y + PIXEL_OFFSET ) );
                
                return ( ( (int)scaleH - size.getEffectiveHeight() ) / 2 + Math.round( y * scaleH ) );
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                if ( isPosPixelValue( y ) )
                    return ( (int)scaleH - (int)( y - PIXEL_OFFSET ) - size.getEffectiveHeight() );
                
                if ( isNegPixelValue( y ) )
                    return ( (int)scaleH - (int)( y + PIXEL_OFFSET ) - size.getEffectiveHeight() );
                
                return ( Math.round( scaleH - ( y * scaleH ) - size.getEffectiveHeight() ) );
        }
        
        // Unreachable code!
        return ( -1 );
    }
    
    public void unbake()
    {
        bakedX = -1;
        bakedY = -1;
    }
    
    public void bake()
    {
        boolean isSizeBasked = false;
        if ( size instanceof Size )
        {
            isSizeBasked = ( (Size)size ).isBaked();
            ( (Size)size ).unbake();
        }
        unbake();
        
        bakedX = getEffectiveX();
        bakedY = getEffectiveY();
        
        if ( isSizeBasked )
        {
            ( (Size)size ).bake();
        }
    }
    
    public final boolean isBaked()
    {
        return ( bakedX >= 0 );
    }
    
    public Position setXToPercents()
    {
        if ( isPixelValue( x ) )
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( x > 0f )
                this.x = +PIXEL_OFFSET * 0.9f;
            else
                this.x = -PIXEL_OFFSET * 0.9f;
            
            setEffectivePosition( getPositioning(), effX, effY );
        }
        
        return ( this );
    }
    
    public Position setXToPixels()
    {
        if ( !isPixelValue( x ) )
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( x > 0f )
                this.x = +PIXEL_OFFSET + 10000f;
            else
                this.x = -PIXEL_OFFSET - 10000f;
            
            setEffectivePosition( getPositioning(), effX, effY );
        }
        
        return ( this );
    }
    
    public Position flipXPercentagePx()
    {
        if ( isPixelValue( x ) )
            setXToPercents();
        else
            setXToPixels();
        
        return ( this );
    }
    
    public Position setYToPercents()
    {
        if ( isPixelValue( y ) )
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( y > 0f )
                this.y = +PIXEL_OFFSET * 0.9f;
            else
                this.y = -PIXEL_OFFSET * 0.9f;
            
            setEffectivePosition( getPositioning(), effX, effY );
        }
        
        return ( this );
    }
    
    public Position setYToPixels()
    {
        if ( !isPixelValue( y ) )
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( y > 0f )
                this.y = +PIXEL_OFFSET + 10000f;
            else
                this.y = -PIXEL_OFFSET - 10000f;
            
            setEffectivePosition( getPositioning(), effX, effY );
        }
        
        return ( this );
    }
    
    public Position flipYPercentagePx()
    {
        if ( isPixelValue( y ) )
            setYToPercents();
        else
            setYToPixels();
        
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
    private float parseX( String value )
    {
        setX( parseValue( value ) );
        
        return ( getX() );
    }
    
    private float parseY( String value )
    {
        setY( parseValue( value ) );
        
        return ( getY() );
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
    private String unparseX()
    {
        return ( unparseValue( getX() ) );
    }
    
    private String unparseY()
    {
        return ( unparseValue( getY() ) );
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
     * @param positioning the new positioning
     */
    protected void onPositioningPropertySet( RelativePositioning positioning )
    {
    }
    
    private Property posProp = null;
    
    public Property getPositioningProperty( String name, String nameForDisplay )
    {
        if ( !propExistsWithName( posProp, name, nameForDisplay ) )
        {
            posProp = new Property( widget, name, nameForDisplay, PropertyEditorType.ENUM )
            {
                @Override
                public void setValue( Object value )
                {
                    if ( positioning == value )
                        return;
                    
                    int currX = getEffectiveX();
                    int currY = getEffectiveY();
                    
                    setEffectivePosition( (RelativePositioning)value, currX, currY );
                    
                    onPositioningPropertySet( (RelativePositioning)value );
                }
                
                @Override
                public Object getValue()
                {
                    return ( getPositioning() );
                }
                
                @Override
                public void loadValue( String value )
                {
                    Position.this.set( RelativePositioning.valueOf( value ), getX(), getY() );
                }
            };
        }
        
        return ( posProp );
    }
    
    public final Property getPositioningProperty( String name )
    {
        return ( getPositioningProperty( name, null ) );
    }
    
    /**
     * 
     * @param x the new x
     */
    protected void onXPropertySet( float x )
    {
    }
    
    private PosSizeProperty xProp = null;
    
    public PosSizeProperty getXProperty( String name, String nameForDisplay )
    {
        if ( !propExistsWithName( xProp, name, nameForDisplay ) )
        {
            xProp = new PosSizeProperty( widget, name, nameForDisplay, false, false )
            {
                @Override
                public boolean isPercentage()
                {
                    return ( !isPixelValue( x ) );
                }
                
                @Override
                public void setValue( Object value )
                {
                    float x = ( (Number)value ).floatValue();
                    
                    set( x, getY() );
                    
                    onXPropertySet( x );
                }
                
                @Override
                public Object getValue()
                {
                    return ( getX() );
                }
                
                @Override
                public void onButtonClicked( Object button )
                {
                    flipXPercentagePx();
                }
                
                @Override
                public Boolean quoteValueInConfigurationFile()
                {
                    return ( false );
                }
                
                @Override
                public Object getValueForConfigurationFile()
                {
                    return ( unparseValue( getX() ) );
                }
                
                @Override
                public void loadValue( String value )
                {
                    if ( !value.endsWith( "%" ) && !value.endsWith( "px" ) )
                        value += "px";
                    
                    setX( parseValue( value, !isPixelValue( x ) ) );
                }
            };
        }
        
        return ( xProp );
    }
    
    public final PosSizeProperty getXProperty( String name )
    {
        return ( getXProperty( name, null ) );
    }
    
    /**
     * 
     * @param y the new y
     */
    protected void onYPropertySet( float y )
    {
    }
    
    private PosSizeProperty yProp = null;
    
    public PosSizeProperty getYProperty( String name, String nameForDisplay )
    {
        if ( !propExistsWithName( yProp, name, nameForDisplay ) )
        {
            yProp = new PosSizeProperty( widget, name, nameForDisplay, false, false )
            {
                @Override
                public boolean isPercentage()
                {
                    return ( !isPixelValue( y ) );
                }
                
                @Override
                public void setValue( Object value )
                {
                    float y = ( (Number)value ).floatValue();
                    
                    set( getX(), y );
                    
                    onYPropertySet( y );
                }
                
                @Override
                public Object getValue()
                {
                    return ( getY() );
                }
                
                @Override
                public void onButtonClicked( Object button )
                {
                    flipYPercentagePx();
                }
                
                @Override
                public Boolean quoteValueInConfigurationFile()
                {
                    return ( false );
                }
                
                @Override
                public Object getValueForConfigurationFile()
                {
                    return ( unparseValue( getY() ) );
                }
                
                @Override
                public void loadValue( String value )
                {
                    if ( !value.endsWith( "%" ) && !value.endsWith( "px" ) )
                        value += "px";
                    
                    setY( parseValue( value, !isPixelValue( y ) ) );
                }
            };
        }
        
        return ( yProp );
    }
    
    public final PosSizeProperty getYProperty( String name )
    {
        return ( getYProperty( name, null ) );
    }
    
    protected Position( Widget widget, boolean isGlobalPosition, RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size )
    {
        this.widget = widget;
        this.isGlobalPosition = isGlobalPosition;
        
        this.positioning = positioning;
        this.x = xPercent ? x * 0.01f : PIXEL_OFFSET + x;
        this.y = yPercent ? y * 0.01f : PIXEL_OFFSET + y;
        
        this.size = size;
    }
    
    /**
     * Create a new positional property for positions local to a Widget's area.
     * 
     * @param widget the owning {@link Widget}.
     * @param positioning the used {@link RelativePositioning}
     * @param x the x position
     * @param xPercent interpret 'x' as percents?
     * @param y the y position
     * @param yPercent interpret 'y' as percents?
     * @param size the size for the area
     * 
     * @return the new Position.
     */
    public static final Position newLocalPosition( Widget widget, RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size )
    {
        return ( new Position( widget, false, positioning, x, xPercent, y, yPercent, size ) );
    }
    
    /**
     * Create a new positional property for global positions on the whole screen area.
     * 
     * @param widget the owning {@link Widget}.
     * @param positioning the used {@link RelativePositioning}
     * @param x the x position
     * @param xPercent interpret 'x' as percents?
     * @param y the y position
     * @param yPercent interpret 'y' as percents?
     * @param size the size for the area
     * 
     * @return the new Position.
     */
    public static final Position newGlobalPosition( Widget widget, RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size )
    {
        return ( new Position( widget, true, positioning, x, xPercent, y, yPercent, size ) );
    }
    
    protected Position( WidgetToPropertyForwarder w2pf, boolean isGlobalPosition, RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size )
    {
        this.widget = null;
        this.isGlobalPosition = isGlobalPosition;
        
        this.positioning = positioning;
        this.x = xPercent ? x * 0.01f : PIXEL_OFFSET + x;
        this.y = yPercent ? y * 0.01f : PIXEL_OFFSET + y;
        
        this.size = size;
        
        __PropsPrivilegedAccess.addPosition( w2pf, this );
    }
    
    /**
     * Create a new positional property for positions local to a Widget's area.
     * 
     * @param w2pf the object to use as a placeholder for the Widget to come
     * @param positioning the used {@link RelativePositioning}
     * @param x the x position
     * @param xPercent interpret 'x' as percents?
     * @param y the y position
     * @param yPercent interpret 'y' as percents?
     * @param size the size for the area
     * 
     * @return the new Position.
     */
    public static final Position newLocalPosition( WidgetToPropertyForwarder w2pf, RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size )
    {
        return ( new Position( w2pf, false, positioning, x, xPercent, y, yPercent, size ) );
    }
    
    /**
     * Create a new positional property for global positions on the whole screen area.
     * 
     * @param w2pf the object to use as a placeholder for the Widget to come
     * @param positioning the used {@link RelativePositioning}
     * @param x the x position
     * @param xPercent interpret 'x' as percents?
     * @param y the y position
     * @param yPercent interpret 'y' as percents?
     * @param size the size for the area
     * 
     * @return the new Position.
     */
    public static final Position newGlobalPosition( WidgetToPropertyForwarder w2pf, RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size )
    {
        return ( new Position( w2pf, true, positioning, x, xPercent, y, yPercent, size ) );
    }
}
