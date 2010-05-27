package net.ctdp.rfdynhud.values;

import java.io.IOException;

import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;
import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyEditorType;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

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
        if ( isWidgetPosition )
            return ( widget.getConfiguration().getGameResolution().getResX() );
        
        return ( widget.getEffectiveInnerWidth() );
    }
    
    private final float getScaleHeight()
    {
        if ( isWidgetPosition )
            return ( widget.getConfiguration().getGameResolution().getResY() );
        
        return ( widget.getEffectiveInnerHeight() );
    }
    
    private final float getHundretPercentWidth()
    {
        if ( isWidgetPosition )
            return ( widget.getConfiguration().getGameResolution().getResY() * 4 / 3 );
        
        return ( widget.getEffectiveInnerWidth() );
    }
    
    /**
     * Sets this Widget's position.
     * 
     * @param positioning
     * @param x
     * @param y
     */
    private boolean set( RelativePositioning positioning, float x, float y )
    {
        if ( widget.getConfiguration() != null )
        {
            if ( positioning.isHCenter() )
            {
                if ( isNegPixelValue( x ) )
                    x = Math.max( -PIXEL_OFFSET - getScaleWidth() / 2f + ( isWidgetPosition ? size.getEffectiveWidth() / 2f : 0f ), x );
                else if ( isPosPixelValue( x ) )
                    x = Math.min( +PIXEL_OFFSET + getScaleWidth() / 2f - ( isWidgetPosition ? size.getEffectiveWidth() / 2f : 0f ), x );
                else if ( x < 0f )
                    x = Math.max( -0.5f + ( isWidgetPosition ? size.getEffectiveWidth() / 2f / getHundretPercentWidth() : 0f ), x );
                else if ( x > 0f )
                    x = Math.min( +0.5f - ( isWidgetPosition ? size.getEffectiveWidth() / 2f / getHundretPercentWidth() : 0f ), x );
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
                    y = Math.max( -PIXEL_OFFSET - getScaleHeight() / 2f + ( isWidgetPosition ? size.getEffectiveHeight() / 2f : 0f ), y );
                else if ( isPosPixelValue( y ) )
                    y = Math.min( +PIXEL_OFFSET + getScaleHeight() / 2f - ( isWidgetPosition ? size.getEffectiveHeight() / 2f : 0f ), y );
                else if ( y < 0f )
                    y = Math.max( -0.5f + ( isWidgetPosition ? size.getEffectiveHeight() / 2f / getScaleHeight() : 0f ), y );
                else if ( y > 0f )
                    y = Math.min( +0.5f - ( isWidgetPosition ? size.getEffectiveHeight() / 2f / getScaleHeight() : 0f ), y );
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
                widget.forceCompleteRedraw();
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
     * @param positioning
     * @param x
     * @param y
     * @param gameResX
     * @param gameResY
     */
    public final boolean setEffectivePosition( RelativePositioning positioning, int x, int y )
    {
        float scaleW = getScaleWidth();
        float scaleH = getScaleHeight();
        
        if ( isWidgetPosition && !isPixelValue( this.x ) )
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
     * @param x
     * @param y
     * @param gameResX
     * @param gameResY
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
    
    public void savePositioningProperty( String key, String comment, WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( key, getPositioning(), comment );
    }
    
    public void saveXProperty( String key, String comment, WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( key, unparseValue( getX() ), false, comment );
    }
    
    public void saveYProperty( String key, String comment, WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( key, unparseValue( getY() ), false, comment );
    }
    
    public void saveProperty( String positioningKey, String positioningComment, String xKey, String xComment, String yKey, String yComment, WidgetsConfigurationWriter writer ) throws IOException
    {
        if ( positioningKey != null )
            savePositioningProperty( positioningKey, positioningComment, writer );
        
        if ( xKey != null )
            saveXProperty( xKey, xComment, writer );
        
        if ( yKey != null )
            saveYProperty( yKey, yComment, writer );
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
            if ( !value.endsWith( "%" ) && !value.endsWith( "px" ) )
                value += "px";
            
            setX( parseValue( value, !isPixelValue( x ) ) );
            
            return ( true );
        }
        
        if ( key.equals( yKey ) )
        {
            if ( !value.endsWith( "%" ) && !value.endsWith( "px" ) )
                value += "px";
            
            setY( parseValue( value, !isPixelValue( y ) ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    /**
     * 
     * @param positioning
     */
    protected void onPositioningPropertySet( RelativePositioning positioning )
    {
    }
    
    public Property createPositioningProperty( String name, String nameForDisplay )
    {
        Property prop = new Property( widget, name, nameForDisplay, PropertyEditorType.ENUM )
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
    
    public Property createPositioningProperty( String name )
    {
        return ( createPositioningProperty( name, name ) );
    }
    
    /**
     * 
     * @param x
     */
    protected void onXPropertySet( float x )
    {
    }
    
    public PosSizeProperty createXProperty( String name, String nameForDisplay )
    {
        PosSizeProperty prop = new PosSizeProperty( widget, name, nameForDisplay, false, false )
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
        };
        
        return ( prop );
    }
    
    public PosSizeProperty createXProperty( String name )
    {
        return ( createXProperty( name, name ) );
    }
    
    /**
     * 
     * @param y
     */
    protected void onYPropertySet( float y )
    {
    }
    
    public PosSizeProperty createYProperty( String name, String nameFordisplay )
    {
        PosSizeProperty prop = new PosSizeProperty( widget, name, nameFordisplay, false, false )
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
        };
        
        return ( prop );
    }
    
    public PosSizeProperty createYProperty( String name )
    {
        return ( createYProperty( name, name ) );
    }
    
    Position( RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size, Widget widget, boolean isWidgetPosition )
    {
        this.positioning = positioning;
        this.x = xPercent ? x * 0.01f : PIXEL_OFFSET + x;
        this.y = yPercent ? y * 0.01f : PIXEL_OFFSET + y;
        
        this.size = size;
        
        this.widget = widget;
        
        this.isWidgetPosition = isWidgetPosition;
    }
    
    public Position( RelativePositioning positioning, float x, boolean xPercent, float y, boolean yPercent, AbstractSize size, Widget widget )
    {
        this( positioning, x, xPercent, y, yPercent, size, widget, false );
    }
}
