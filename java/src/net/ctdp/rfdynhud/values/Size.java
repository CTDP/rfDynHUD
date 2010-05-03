package net.ctdp.rfdynhud.values;

import net.ctdp.rfdynhud.properties.PosSizeProperty;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class Size
{
    private static final float PERCENT_OFFSET = 10000f;
    private static final float PERCENT_OFFSET_CHECK_POSITIVE = +PERCENT_OFFSET - 0.001f;
    private static final float PERCENT_OFFSET_CHECK_NEGATIVE = -PERCENT_OFFSET + 0.001f;
    
    private float width;
    private float height;
    
    private int bakedWidth = -1;
    private int bakedHeight = -1;
    
    private final Widget widget;
    private final boolean isWidgetSize;
    
    public static final float getPercent( float percent )
    {
        return ( PERCENT_OFFSET + percent * 0.01f );
    }
    
    public static final boolean isPercent( float value )
    {
        return ( Math.abs( value ) > PERCENT_OFFSET_CHECK_POSITIVE );
    }
    
    public static final float getAbsolute( float value )
    {
        return ( value );
    }
    
    /*
    public final Widget getWidget()
    {
        return ( widget );
    }
    */
    
    public final boolean isWidgetSize()
    {
        return ( isWidgetSize );
    }
    
    /**
     * Gets this Widget's width. If it is a negative number, the actual width is (screen_width - width).
     * 
     * @return this Widget's width.
     */
    public final float getWidth()
    {
        return ( width );
    }
    
    /**
     * Gets this Widget's height. If it is a negative number, the actual height is (screen_height - height).
     * 
     * @return this Widget's height.
     */
    public final float getHeight()
    {
        return ( height );
    }
    
    public final boolean isPercentageWidth()
    {
        if ( width > PERCENT_OFFSET_CHECK_POSITIVE )
            return ( true );
        
        if ( width < PERCENT_OFFSET_CHECK_NEGATIVE )
            return ( true );
        
        return ( false );
    }
    
    public final boolean isPercentageHeight()
    {
        if ( height > PERCENT_OFFSET_CHECK_POSITIVE )
            return ( true );
        
        if ( height < PERCENT_OFFSET_CHECK_NEGATIVE )
            return ( true );
        
        return ( false );
    }
    
    private final float getMinWidth()
    {
        if ( isWidgetSize )
            return ( widget.getMinWidth( null ) );
        
        return ( 10f );
    }
    
    private final float getMinHeight()
    {
        if ( isWidgetSize )
            return ( widget.getMinHeight( null ) );
        
        return ( 10f );
    }
    
    private final float getScaleWidth()
    {
        if ( isWidgetSize )
            return ( widget.getConfiguration().getGameResX() );
        
        return ( widget.getSize().getEffectiveWidth() - widget.getBorder().getOpaqueLeftWidth() - widget.getBorder().getOpaqueRightWidth() );
    }
    
    private final float getScaleHeight()
    {
        if ( isWidgetSize )
            return ( widget.getConfiguration().getGameResY() );
        
        return ( widget.getSize().getEffectiveHeight() - widget.getBorder().getOpaqueTopHeight() - widget.getBorder().getOpaqueBottomHeight() );
    }
    
    private final float getHundretPercentWidth()
    {
        if ( isWidgetSize )
            return ( widget.getConfiguration().getGameResY() * 4 / 3 );
        
        return ( widget.getSize().getEffectiveWidth() );
    }
    
    private void applyLimits()
    {
        unbake();
        
        if ( widget.getConfiguration() == null )
            return;
        
        if ( width > PERCENT_OFFSET_CHECK_POSITIVE )
            width = Math.max( +PERCENT_OFFSET, Math.min( width, +PERCENT_OFFSET + 1.00f ) );
        else if ( width < PERCENT_OFFSET_CHECK_NEGATIVE )
            width = Math.min( -PERCENT_OFFSET, Math.max( width, -PERCENT_OFFSET - 1.00f ) );
        else if ( width > 0f )
            width = Math.min( width, getScaleWidth() );
        else if ( width <= 0f )
            width = Math.max( width, -getScaleWidth() );
        
        if ( height > PERCENT_OFFSET_CHECK_POSITIVE )
            height = Math.max( +PERCENT_OFFSET, Math.min( height, +PERCENT_OFFSET + 1.00f ) );
        else if ( height < PERCENT_OFFSET_CHECK_NEGATIVE )
            height = Math.min( -PERCENT_OFFSET, Math.max( height, -PERCENT_OFFSET - 1.00f ) );
        else if ( height > 0f )
            height = Math.min( height, getScaleHeight() );
        else if ( height <= 0f )
            height = Math.max( height, -getScaleHeight() );
    }
    
    /**
     * Sets this {@link Widget}'s size. (only works for non-fixed-sized {@link Widget}s)
     * 
     * @param width
     * @param height
     */
    public boolean set( float width, float height )
    {
        if ( widget.getConfiguration() != null )
        {
            if ( width > PERCENT_OFFSET_CHECK_POSITIVE )
                width = +PERCENT_OFFSET + Math.max( width - PERCENT_OFFSET, getMinWidth() / getHundretPercentWidth() );
            else if ( width < PERCENT_OFFSET_CHECK_NEGATIVE )
                width = -PERCENT_OFFSET + Math.max( width + PERCENT_OFFSET, ( getMinWidth() / getScaleWidth() ) - 1.0f );
            else if ( width > 0f )
                width = Math.max( width, getMinWidth() );
            else if ( width <= 0f )
                width = -Math.max( -width, -getMinWidth() );
            
            if ( height > PERCENT_OFFSET_CHECK_POSITIVE )
                height = +PERCENT_OFFSET + Math.max( height - PERCENT_OFFSET, getMinHeight() / getScaleHeight() );
            else if ( height < PERCENT_OFFSET_CHECK_NEGATIVE )
                height = -PERCENT_OFFSET - Math.min( height + PERCENT_OFFSET, -( getMinHeight() / getScaleHeight() ) );
            else if ( height > 0f )
                height = Math.max( height, getMinHeight() );
            else if ( height <= 0f )
                height = -Math.max( -height, -getMinHeight() );
        }
        
        unbake();
        
        boolean changed = false;
        
        if ( ( width != this.width ) || ( height != this.height ) )
        {
            float oldWidth = this.width;
            float oldHeight = this.height;
            
            this.width = width;
            this.height = height;
            
            applyLimits();
            
            widget.forceAndSetDirty();
            
            __WPrivilegedAccess.onSizeChanged( oldWidth, oldHeight, width, height, widget );
            
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
    public boolean setWidth( float width )
    {
        return ( set( width, getHeight() ) );
    }
    
    /**
     * Sets this {@link Widget}'s height. (only works for non-fixed-sized {@link Widget}s)
     * 
     * @param height
     */
    public boolean setHeight( float height )
    {
        return ( set( getWidth(), height ) );
    }
    
    /**
     * Sets this {@link Widget}'s size in absolute pixel coordinates. (only works for non-fixed-sized {@link Widget}s)
     * 
     * @param width
     * @param height
     * @param gameResX
     * @param gameResY
     */
    public final boolean setEffectiveSize( int width, int height )
    {
        float scaleW = getScaleWidth();
        float scaleH = getScaleHeight();
        
        width = Math.max( width, (int)getMinWidth() );
        height = Math.max( height, (int)getMinHeight() );
        
        if ( this.width > PERCENT_OFFSET_CHECK_POSITIVE )
        {
            width = Math.min( width, (int)getHundretPercentWidth() );
        }
        
        if ( this.width <= 0f )
            width -= (int)scaleW;
        
        if ( this.height <= 0f )
            height -= (int)scaleH;
        
        boolean changed = false;
        
        if ( Math.abs( this.width ) > PERCENT_OFFSET_CHECK_POSITIVE )
        {
            float hundretPercentW = ( this.width <= 0f ) ? scaleW : getHundretPercentWidth();
            
            if ( Math.abs( this.height ) > PERCENT_OFFSET_CHECK_POSITIVE )
                changed = set( ( width <= 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)width / hundretPercentW, ( height <= 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)height / scaleH );
            else
                changed = set( ( width <= 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)width / hundretPercentW, height );
        }
        else if ( Math.abs( this.height ) > PERCENT_OFFSET_CHECK_POSITIVE )
        {
            changed = set( width, ( height <= 0 ? -PERCENT_OFFSET : +PERCENT_OFFSET ) + (float)height / scaleH );
        }
        else
        {
            changed = set( width, height );
        }
        
        applyLimits();
        
        return ( changed );
    }
    
    /**
     * Gets the effective Widget's width. If {@link #getWidth()} returns a
     * negative number, the effective width is (screen_width - width).
     * 
     * @return the effective Widget's width.
     */
    public final int getEffectiveWidth()
    {
        if ( bakedWidth >= 0 )
            return ( bakedWidth );
        
        float scaleW = getScaleWidth();
        
        if ( width > PERCENT_OFFSET_CHECK_POSITIVE )
            return ( (int)Math.max( getMinWidth(), ( width - PERCENT_OFFSET ) * getHundretPercentWidth() ) );
        
        if ( width < PERCENT_OFFSET_CHECK_NEGATIVE )
            return ( (int)Math.max( getMinWidth(), scaleW + ( ( width + PERCENT_OFFSET ) * scaleW ) ) );
        
        if ( width <= 0f )
            return ( (int)Math.max( getMinWidth(), scaleW + width ) );
        
        return ( (int)Math.max( getMinWidth(), width ) );
    }
    
    /**
     * Gets the effective Widget's height. If {@link #getHeight()} returns a
     * negative number, the effective height is (screen_height - height).
     * 
     * @return the effective Widget's height.
     */
    public final int getEffectiveHeight()
    {
        if ( bakedHeight >= 0 )
            return ( bakedHeight );
        
        float scaleH = getScaleHeight();
        
        if ( height > PERCENT_OFFSET_CHECK_POSITIVE )
            return ( (int)Math.max( getMinHeight(), ( height - PERCENT_OFFSET ) * scaleH ) );
        
        if ( height < PERCENT_OFFSET_CHECK_NEGATIVE )
            return ( (int)Math.max( getMinHeight(), scaleH + ( ( height + PERCENT_OFFSET ) * scaleH ) ) );
        
        if ( height <= 0f )
            return ( (int)Math.max( getMinHeight(), scaleH + height ) );
        
        return ( (int)Math.max( getMinHeight(), height ) );
    }
    
    public void unbake()
    {
        bakedWidth = -1;
        bakedHeight = -1;
    }
    
    public void bake()
    {
        unbake();
        
        int tmpWidth = getEffectiveWidth();
        int tmpHeight = getEffectiveHeight();
        
        width = 1;
        height = 1;
        setEffectiveSize( tmpWidth, tmpHeight );
        
        bakedWidth = tmpWidth;
        bakedHeight = tmpHeight;
    }
    
    public Size flipWidthPercentagePx()
    {
        if ( Math.abs( width ) < PERCENT_OFFSET_CHECK_POSITIVE )
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( width > 0f )
                this.width = +PERCENT_OFFSET + 0.5f;
            else
                this.width = -PERCENT_OFFSET - 0.5f;
            
            setEffectiveSize( effW, effH );
        }
        else
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( width > 0f )
                this.width = +10f;
            else
                this.width = -10f;
            
            setEffectiveSize( effW, effH );
        }
        
        return ( this );
    }
    
    public Size flipHeightPercentagePx()
    {
        if ( Math.abs( height ) < PERCENT_OFFSET_CHECK_POSITIVE )
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( height > 0f )
                this.height = +PERCENT_OFFSET + 0.5f;
            else
                this.height = -PERCENT_OFFSET - 0.5f;
            
            setEffectiveSize( effW, effH );
        }
        else
        {
            int effW = getEffectiveWidth();
            int effH = getEffectiveHeight();
            
            if ( height > 0f )
                this.height = +10f;
            else
                this.height = -10f;
            
            setEffectiveSize( effW, effH );
        }
        
        return ( this );
    }
    
    public final boolean isWidthPercentageValue()
    {
        return ( ( width < PERCENT_OFFSET_CHECK_NEGATIVE ) || ( width > PERCENT_OFFSET_CHECK_POSITIVE ) );
    }
    
    public final boolean isHeightPercentageValue()
    {
        return ( ( height < PERCENT_OFFSET_CHECK_NEGATIVE ) || ( height > PERCENT_OFFSET_CHECK_POSITIVE ) );
    }
    
    public Size flipWidthSign()
    {
        int gameResX = widget.getConfiguration().getGameResX();
        
        if ( width < PERCENT_OFFSET_CHECK_NEGATIVE )
            width = PERCENT_OFFSET + ( 1.0f + ( width + PERCENT_OFFSET ) ) * ( getScaleWidth() / getHundretPercentWidth() );
        else if ( width > PERCENT_OFFSET_CHECK_POSITIVE )
            width = -PERCENT_OFFSET - 1.0f + ( ( width - PERCENT_OFFSET ) / ( getScaleWidth() / getHundretPercentWidth() ) );
        else if ( width < 0f )
            width = gameResX + width;
        else if ( width > 0f )
            width = width - gameResX;
        
        applyLimits();
        
        widget.forceAndSetDirty();
        
        return ( this );
    }
    
    public Size flipHeightSign()
    {
        int gameResY = widget.getConfiguration().getGameResY();
        
        if ( height < PERCENT_OFFSET_CHECK_NEGATIVE )
            height = PERCENT_OFFSET + 1.0f + ( height + PERCENT_OFFSET );
        else if ( height > PERCENT_OFFSET_CHECK_POSITIVE )
            height = -PERCENT_OFFSET + ( height - PERCENT_OFFSET ) - 1.0f;
        else if ( height < 0f )
            height = gameResY + height;
        else if ( height > 0f )
            height = height - gameResY;
        
        applyLimits();
        
        widget.forceAndSetDirty();
        
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
    
    public float parseWidth( String value )
    {
        setWidth( parseValue( value ) );
        
        return ( getWidth() );
    }
    
    public float parseHeight( String value )
    {
        setHeight( parseValue( value ) );
        
        return ( getHeight() );
    }
    
    public static String unparseValue( float value )
    {
        if ( value > PERCENT_OFFSET_CHECK_POSITIVE )
            return ( String.valueOf( ( value - PERCENT_OFFSET ) * 100f ) + "%" );
        
        if ( value < PERCENT_OFFSET_CHECK_NEGATIVE )
            return ( String.valueOf( ( value + PERCENT_OFFSET ) * 100f ) + "%" );
        
        return ( String.valueOf( (int)value ) );
    }
    
    public String unparseWidth()
    {
        return ( unparseValue( getWidth() ) );
    }
    
    public String unparseHeight()
    {
        return ( unparseValue( getHeight() ) );
    }
    
    public boolean loadProperty( String key, String value, String widthKey, String heightKey )
    {
        if ( key.equals( widthKey ) )
        {
            setWidth( Size.parseValue( value ) );
            
            return ( true );
        }
        
        if ( key.equals( heightKey ) )
        {
            setHeight( Size.parseValue( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    protected void onWidthPropertySet( float width )
    {
    }
    
    public PosSizeProperty createWidthProperty( String name )
    {
        boolean ro = isWidgetSize ? widget.hasFixedSize() : false;
        
        PosSizeProperty prop = new PosSizeProperty( widget, name, ro, true )
        {
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
        };
        
        return ( prop );
    }
    
    protected void onHeightPropertySet( float height )
    {
    }
    
    public PosSizeProperty createHeightProperty( String name )
    {
        boolean ro = isWidgetSize ? widget.hasFixedSize() : false;
        
        PosSizeProperty prop = new PosSizeProperty( widget, name, ro, true )
        {
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
        };
        
        return ( prop );
    }
    
    public Size( float width, float height, Widget widget, boolean isWidgetSize )
    {
        this.width = width;
        this.height = height;
        
        this.widget = widget;
        
        this.isWidgetSize = isWidgetSize;
    }
    
    public Size( float width, float height, Widget widget )
    {
        this( width, height, widget, false );
    }
}
