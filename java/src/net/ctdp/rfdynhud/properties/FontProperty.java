package net.ctdp.rfdynhud.properties;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

public class FontProperty extends Property
{
    public static final String STANDARD_FONT_NAME = "StandardFont";
    public static final String STANDARD_FONT2_NAME = "StandardFont2";
    public static final String STANDARD_FONT3_NAME = "StandardFont3";
    public static final String SMALLER_FONT_NAME = "SmallerFont";
    public static final String SMALLER_FONT3_NAME = "SmallerFont3";
    public static final String BIGGER_FONT_NAME = "BiggerFont";
    
    private static final BufferedImage METRICS_PROVIDER_IMAGE = new BufferedImage( 16, 16, BufferedImage.TYPE_INT_BGR );
    private static final Graphics2D METRICS_PROVIDER = METRICS_PROVIDER_IMAGE.createGraphics();
    
    private final WidgetsConfiguration widgetsConf;
    
    private String fontKey;
    private Font font = null;
    private Boolean antiAliased = null;
    
    private FontMetrics metrics = null;
    
    public static String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( STANDARD_FONT_NAME ) )
            return ( "Monospaced-BOLD-13va" );
        
        if ( name.equals( STANDARD_FONT2_NAME ) )
            return ( "Monospaced-BOLD-12va" );
        
        if ( name.equals( STANDARD_FONT3_NAME ) )
            return ( "Monospaced-BOLD-11va" );
        
        if ( name.equals( SMALLER_FONT_NAME ) )
            return ( "Monospaced-BOLD-13va" );
        
        if ( name.equals( SMALLER_FONT3_NAME ) )
            return ( "Monospaced-BOLD-9va" );
        
        if ( name.equals( BIGGER_FONT_NAME ) )
            return ( "Monospaced-BOLD-14va" );
        
        return ( null );
    }
    
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void refresh()
    {
        this.font = null;
    }
    
    public void setFont( String fontKey )
    {
        if ( ( fontKey == null ) && ( this.fontKey == null ) )
            return;
        
        final WidgetsConfiguration widgetsConf = ( widget != null ) ? widget.getConfiguration() : this.widgetsConf;
        
        if ( widgetsConf == null )
        {
            this.fontKey = fontKey;
            this.font = null;
            this.antiAliased = null;
            this.metrics = null;
        }
        else
        {
            String oldValue = widgetsConf.getNamedFontString( this.fontKey );
            if ( oldValue == null )
                oldValue = this.fontKey;
            
            this.fontKey = fontKey;
            this.font = null;
            this.antiAliased = null;
            this.metrics = null;
            
            String newValue = widgetsConf.getNamedFontString( this.fontKey );
            if ( newValue == null )
                newValue = this.fontKey;
            
            if ( ( newValue == null ) || !newValue.equals( oldValue ) )
            {
                if ( widget != null )
                    widget.forceAndSetDirty();
                
                onValueChanged( oldValue, fontKey );
                
                if ( widget != null )
                    __WPrivilegedAccess.onPropertyChanged( this, oldValue, fontKey, widget );
            }
        }
    }
    
    public final void setFont( Font font, boolean virtual, boolean antiAliased )
    {
        setFont( FontUtils.getFontString( font, virtual, antiAliased ) );
    }
    
    public final String getFontKey()
    {
        return ( fontKey );
    }
    
    public final Font getFont()
    {
        if ( fontKey == null )
            return ( null );
        
        if ( font == null )
        {
            font = widget.getConfiguration().getNamedFont( fontKey );
            if ( font == null )
            {
                String fontStr = widget.getDefaultNamedFontValue( fontKey );
                if ( fontStr != null )
                {
                    widget.getConfiguration().addNamedFont( fontKey, fontStr );
                    font = widget.getConfiguration().getNamedFont( fontKey );
                }
                else
                {
                    font = FontUtils.parseFont( fontKey, widget.getConfiguration().getGameResolution().getResY(), false, true );
                }
            }
        }
        
        return ( font );
    }
    
    public final boolean isAntiAliased()
    {
        if ( antiAliased == null )
        {
            String fontStr = widget.getConfiguration().getNamedFontString( fontKey );
            if ( fontStr == null )
            {
                fontStr = widget.getDefaultNamedFontValue( fontKey );
                if ( fontStr != null )
                    widget.getConfiguration().addNamedFont( fontKey, fontStr );
                else
                    fontStr = fontKey;
            }
            
            antiAliased = FontUtils.parseAntiAliasFlag( fontStr, false, true );
        }
        
        return ( antiAliased );
    }
    
    public final FontMetrics getMetrics()
    {
        if ( metrics == null )
        {
            metrics = METRICS_PROVIDER.getFontMetrics( getFont() );
        }
        
        return ( metrics );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setFont( ( value == null ) ? null : String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue()
    {
        return ( fontKey );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( value );
            
            return ( true );
        }
        
        return ( false );
    }
    
    private FontProperty( WidgetsConfiguration widgetsConf, Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.FONT, null, null );
        
        this.widgetsConf = widgetsConf;
        this.fontKey = defaultValue;
    }
    
    public FontProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widgetsConf, null, propertyName, nameForDisplay, defaultValue, readonly );
    }
    
    public FontProperty( WidgetsConfiguration widgetsConf, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widgetsConf, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public FontProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue, boolean readonly )
    {
        this( widgetsConf, propertyName, propertyName, defaultValue, readonly );
    }
    
    public FontProperty( WidgetsConfiguration widgetsConf, String propertyName, String defaultValue )
    {
        this( widgetsConf, propertyName, defaultValue, false );
    }
    
    public FontProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( null, widget, propertyName, nameForDisplay, defaultValue, readonly );
    }
    
    public FontProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public FontProperty( Widget widget, String propertyName, String defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public FontProperty( Widget widget, String propertyName, String defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
