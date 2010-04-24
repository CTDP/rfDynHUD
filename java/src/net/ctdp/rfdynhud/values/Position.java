package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class Position
{
    public static final float PERCENT_OFFSET = Size.PERCENT_OFFSET;
    public static final float PERCENT_OFFSET_CHECK_POSITIVE = Size.PERCENT_OFFSET_CHECK_POSITIVE;
    public static final float PERCENT_OFFSET_CHECK_NEGATIVE = Size.PERCENT_OFFSET_CHECK_NEGATIVE;
    
    private RelativePositioning positioning;
    private float x;
    private float y;
    
    private int bakedX = -1;
    private int bakedY = -1;
    
    private final Size size;
    private final Widget widget;
    private final boolean isWidgetPosition;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    public final boolean isWidgetPosition()
    {
        return ( isWidgetPosition );
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
    public final float getX()
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
    public final float getY()
    {
        return ( y );
    }
    
    private final float getScaleWidth()
    {
        if ( isWidgetPosition )
            return ( widget.getConfiguration().getGameResX() );
        
        return ( widget.getSize().getEffectiveWidth() );
    }
    
    private final float getScaleHeight()
    {
        if ( isWidgetPosition )
            return ( widget.getConfiguration().getGameResY() );
        
        return ( widget.getSize().getEffectiveHeight() );
    }
    
    private final float getHundretPercentWidth()
    {
        if ( isWidgetPosition )
            return ( widget.getConfiguration().getGameResY() * 4 / 3 );
        
        return ( widget.getSize().getEffectiveWidth() );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param positioning
     * @param x
     * @param y
     */
    public Position set( RelativePositioning positioning, float x, float y )
    {
        if ( widget.getConfiguration() != null )
        {
            if ( positioning.isHCenter() )
            {
                if ( x < PERCENT_OFFSET_CHECK_NEGATIVE )
                    x = Math.max( -PERCENT_OFFSET - 0.5f + ( isWidgetPosition ? size.getEffectiveWidth() / 2f / getHundretPercentWidth() : 0f ), x );
                else if ( x > PERCENT_OFFSET_CHECK_POSITIVE )
                    x = Math.min( PERCENT_OFFSET + 0.5f - ( isWidgetPosition ? size.getEffectiveWidth() / 2f / getHundretPercentWidth() : 0f ), x );
                else if ( x < 0f )
                    x = Math.max( -getScaleWidth() / 2f + ( isWidgetPosition ? size.getEffectiveWidth() / 2f : 0f ), x );
                else if ( x > 0f )
                    x = Math.min( getScaleWidth() / 2f - ( isWidgetPosition ? size.getEffectiveWidth() / 2f : 0f ), x );
            }
            else
            {
                if ( Math.abs( x ) > PERCENT_OFFSET_CHECK_POSITIVE )
                    x = Math.max( PERCENT_OFFSET, x );
                else
                    x = Math.max( 0f, x );
            }
            
            if ( positioning.isVCenter() )
            {
                if ( y < PERCENT_OFFSET_CHECK_NEGATIVE )
                    y = Math.max( -PERCENT_OFFSET - 0.5f + ( isWidgetPosition ? size.getEffectiveHeight() / 2f / getScaleHeight() : 0f ), x );
                else if ( y > PERCENT_OFFSET_CHECK_POSITIVE )
                    y = Math.min( PERCENT_OFFSET + 0.5f - ( isWidgetPosition ? size.getEffectiveHeight() / 2f / getScaleHeight() : 0f ), x );
                else if ( y < 0f )
                    y = Math.max( -getScaleWidth() / 2f + ( isWidgetPosition ? size.getEffectiveHeight() / 2f : 0f ), x );
                else if ( y > 0f )
                    y = Math.min( getScaleWidth() / 2f - ( isWidgetPosition ? size.getEffectiveHeight() / 2f : 0f ), x );
            }
            else
            {
                if ( Math.abs( y ) > PERCENT_OFFSET_CHECK_POSITIVE )
                    y = Math.max( PERCENT_OFFSET, y );
                else
                    y = Math.max( 0f, y );
            }
        }
        
        unbake();
        
        if ( ( positioning != this.positioning ) || ( x != this.x ) || ( y != this.y ) )
        {
            RelativePositioning oldPositioning = this.positioning;
            float oldX = this.x;
            float oldY = this.y;
            
            this.positioning = positioning;
            
            this.x = x;
            this.y = y;
            
            //widget.forceAndSetDirty();
            widget.forceCompleteRedraw();
            widget.setDirtyFlag();
            
            __WPrivilegedAccess.onPositionChanged( oldPositioning, oldX, oldY, positioning, x, y, widget );
        }
        widget.setDirtyFlag();
        
        return ( this );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param x
     * @param y
     */
    public final Position set( float x, float y )
    {
        return ( set( getPositioning(), x, y ) );
    }
    
    /**
     * Sets this Widget's x-position.
     * 
     * @param x
     */
    public final Position setX( float x )
    {
        return ( set( getPositioning(), x, getY() ) );
    }
    
    /**
     * Sets this Widget's y-position.
     * 
     * @param y
     */
    public final Position setY( float y )
    {
        return ( set( getPositioning(), getX(), y ) );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param positioning
     * @param x
     * @param y
     * @param gameResX
     * @param gameResY
     */
    public final Position setEffectivePosition( RelativePositioning positioning, int x, int y )
    {
        float scaleW = getScaleWidth();
        float scaleH = getScaleHeight();
        
        if ( isWidgetPosition && ( Math.abs( this.x ) > PERCENT_OFFSET_CHECK_POSITIVE ) )
        {
            if ( positioning.isRight() )
                x = (int)Math.max( scaleW - getHundretPercentWidth() - widget.getSize().getEffectiveWidth(), x );
            else
                x = (int)Math.min( x, getHundretPercentWidth() );
        }
        
        if ( positioning.isVCenter() )
            y = y + (int)( size.getEffectiveHeight() - scaleH ) / 2;
        else if ( positioning.isBottom() )
            y = (int)scaleH - y - (int)size.getEffectiveHeight();
        
        if ( positioning.isHCenter() )
            x = x + ( size.getEffectiveWidth() - (int)scaleW ) / 2;
        else if ( positioning.isRight() )
            x = (int)scaleW - x - size.getEffectiveWidth();
        
        if ( Math.abs( this.x ) > PERCENT_OFFSET_CHECK_POSITIVE )
        {
            float hundretPercentW = getHundretPercentWidth();
            
            if ( Math.abs( this.y ) > PERCENT_OFFSET_CHECK_POSITIVE )
                set( positioning, ( x < 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)x / hundretPercentW, ( y < 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)y / scaleH );
            else
                set( positioning, ( x < 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)x / hundretPercentW, y );
        }
        else if ( Math.abs( this.y ) > PERCENT_OFFSET_CHECK_POSITIVE )
        {
            set( positioning, x, ( y < 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)y / scaleH );
        }
        else
        {
            set( positioning, x, y );
        }
        
        return ( this );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param x
     * @param y
     * @param gameResX
     * @param gameResY
     */
    public final Position setEffectivePosition( int x, int y )
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
                if ( x > PERCENT_OFFSET_CHECK_POSITIVE )
                    return ( (int)( ( x - PERCENT_OFFSET ) * getHundretPercentWidth() ) );
                
                if ( x < PERCENT_OFFSET_CHECK_NEGATIVE )
                    return ( (int)( ( x + PERCENT_OFFSET ) * getHundretPercentWidth() ) );
                
                return ( (int)x );
            case TOP_CENTER:
            case CENTER_CENTER:
            case BOTTOM_CENTER:
                if ( x > PERCENT_OFFSET_CHECK_POSITIVE )
                    return ( ( (int)scaleW - size.getEffectiveWidth() ) / 2 + (int)( ( x - PERCENT_OFFSET ) * getHundretPercentWidth() ) );
                
                if ( x < PERCENT_OFFSET_CHECK_NEGATIVE )
                    return ( ( (int)scaleW - size.getEffectiveWidth() ) / 2 + (int)( ( x + PERCENT_OFFSET ) * getHundretPercentWidth() ) );
                
                return ( ( (int)scaleW - size.getEffectiveWidth() ) / 2 + (int)x );
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                if ( x > PERCENT_OFFSET_CHECK_POSITIVE )
                    return ( (int)scaleW - (int)( ( x - PERCENT_OFFSET ) * getHundretPercentWidth() ) - size.getEffectiveWidth() );
                
                if ( x < PERCENT_OFFSET_CHECK_NEGATIVE )
                    return ( (int)scaleW - (int)( ( x + PERCENT_OFFSET ) * getHundretPercentWidth() ) - size.getEffectiveWidth() );
                
                return ( (int)scaleW - (int)x - size.getEffectiveWidth() );
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
                if ( y > PERCENT_OFFSET_CHECK_POSITIVE )
                    return ( (int)( ( y - PERCENT_OFFSET ) * scaleH ) );
                
                if ( y < PERCENT_OFFSET_CHECK_NEGATIVE )
                    return ( (int)( ( y + PERCENT_OFFSET ) * scaleH ) );
                
                return ( (int)y );
            case CENTER_LEFT:
            case CENTER_CENTER:
            case CENTER_RIGHT:
                if ( y > PERCENT_OFFSET_CHECK_POSITIVE )
                    return ( ( (int)scaleH - size.getEffectiveHeight() ) / 2 + (int)( y * scaleH ) );
                
                if ( y < PERCENT_OFFSET_CHECK_NEGATIVE )
                    return ( ( (int)scaleH - size.getEffectiveHeight() ) / 2 + (int)( ( y + PERCENT_OFFSET ) * scaleH ) );
                
                return ( ( (int)scaleH - size.getEffectiveHeight() ) / 2 + (int)y );
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                if ( y > PERCENT_OFFSET_CHECK_POSITIVE )
                    return ( (int)scaleH - (int)( ( y - PERCENT_OFFSET ) * (int)scaleH ) - size.getEffectiveHeight() );
                
                if ( y < PERCENT_OFFSET_CHECK_NEGATIVE )
                    return ( (int)scaleH - (int)( ( y + PERCENT_OFFSET ) * (int)scaleH ) - size.getEffectiveHeight() );
                
                return ( (int)scaleH - (int)y - size.getEffectiveHeight() );
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
        unbake();
        
        int tmpX = getEffectiveX();
        int tmpY = getEffectiveY();
        
        x = 1;
        y = 1;
        setEffectivePosition( RelativePositioning.TOP_LEFT, tmpX, tmpY );
        
        bakedX = tmpX;
        bakedY = tmpY;
    }
    
    public final boolean isXPercentageValue()
    {
        return ( ( x < PERCENT_OFFSET_CHECK_NEGATIVE ) || ( x > PERCENT_OFFSET_CHECK_POSITIVE ) );
    }
    
    public final boolean isYPercentageValue()
    {
        return ( ( y < PERCENT_OFFSET_CHECK_NEGATIVE ) || ( y > PERCENT_OFFSET_CHECK_POSITIVE ) );
    }
    
    public Position flipXPercentagePx()
    {
        if ( Math.abs( x ) < PERCENT_OFFSET_CHECK_POSITIVE )
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( x > 0f )
                this.x = +PERCENT_OFFSET + 0.5f;
            else
                this.x = -PERCENT_OFFSET - 0.5f;
            
            setEffectivePosition( effX, effY );
        }
        else
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( x > 0f )
                this.x = +10f;
            else
                this.x = -10f;
            
            setEffectivePosition( effX, effY );
        }
        
        return ( this );
    }
    
    public Position flipYPercentagePx()
    {
        if ( Math.abs( y ) < PERCENT_OFFSET_CHECK_POSITIVE )
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( y > 0f )
                this.y = +PERCENT_OFFSET + 0.5f;
            else
                this.y = -PERCENT_OFFSET - 0.5f;
            
            setEffectivePosition( effX, effY );
        }
        else
        {
            int effX = getEffectiveX();
            int effY = getEffectiveY();
            
            if ( y > 0f )
                this.y = +10f;
            else
                this.y = -10f;
            
            setEffectivePosition( effX, effY );
        }
        
        return ( this );
    }
    
    public static float parseValue( String value )
    {
        if ( value.endsWith( "%" ) )
        {
            float f = Float.parseFloat( value.substring( 0, value.length() - 1 ) );
            if ( f < 0f )
                return ( -PERCENT_OFFSET + ( f / 100f ) );
            
            return ( +PERCENT_OFFSET + ( f / 100f ) );
        }
        
        return ( Float.parseFloat( value ) );
    }
    
    public float parseX( String value )
    {
        setX( parseValue( value ) );
        
        return ( getX() );
    }
    
    public float parseY( String value )
    {
        setY( parseValue( value ) );
        
        return ( getY() );
    }
    
    public static String unparseValue( float value )
    {
        if ( value > PERCENT_OFFSET_CHECK_POSITIVE )
            return ( String.valueOf( ( value - PERCENT_OFFSET ) * 100f ) + "%" );
        
        if ( value < PERCENT_OFFSET_CHECK_NEGATIVE )
            return ( String.valueOf( ( value + PERCENT_OFFSET ) * 100f ) + "%" );
        
        return ( String.valueOf( (int)value ) );
    }
    
    public String unparseX()
    {
        return ( unparseValue( getX() ) );
    }
    
    public String unparseY()
    {
        return ( unparseValue( getY() ) );
    }
    
    public boolean loadProperty( String key, String value, String positioningKey, String xKey, String yKey )
    {
        if ( key.equals( positioningKey ) )
        {
            set( RelativePositioning.valueOf( value ), getX(), getY() );
            
            return ( true );
        }
        
        if ( key.equals( xKey ) )
        {
            setX( Position.parseValue( value ) );
            
            return ( true );
        }
        
        if ( key.equals( yKey ) )
        {
            setY( Position.parseValue( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    protected void onPositioningPropertySet( RelativePositioning positioning )
    {
    }
    
    public Property createPositioningProperty( String name )
    {
        Property prop = new Property( widget, name, PropertyEditorType.ENUM )
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
        };
        
        return ( prop );
    }
    
    protected void onXPropertySet( float x )
    {
    }
    
    public PosSizeProperty createXProperty( String name )
    {
        PosSizeProperty prop = new PosSizeProperty( widget, name, false, false )
        {
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
        };
        
        return ( prop );
    }
    
    protected void onYPropertySet( float y )
    {
    }
    
    public PosSizeProperty createYProperty( String name )
    {
        PosSizeProperty prop = new PosSizeProperty( widget, name, false, false )
        {
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
        };
        
        return ( prop );
    }
    
    public Position( RelativePositioning positioning, float x, float y, Size size, Widget widget, boolean isWidgetPosition )
    {
        this.positioning = positioning;
        this.x = x;
        this.y = y;
        
        this.size = size;
        
        this.widget = widget;
        
        this.isWidgetPosition = isWidgetPosition;
    }
    
    public Position( RelativePositioning positioning, float x, float y, Size size, Widget widget )
    {
        this( positioning, x, y, size, widget, false );
    }
}
