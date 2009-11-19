package net.ctdp.rfdynhud.editor.properties;

import java.awt.Font;

import net.ctdp.rfdynhud.widgets._util.FontUtils;
import net.ctdp.rfdynhud.widgets.widget.Widget;

public class FontProperty extends Property
{
    private final Widget widget;
    
    private String fontKey;
    private Font font = null;
    private Boolean antiAliased = null;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    public void setFont( String fontKey )
    {
        this.fontKey = fontKey;
        this.font = null;
        this.antiAliased = null;
        
        widget.forceAndSetDirty();
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
        if ( font == null )
        {
            font = widget.getConfiguration().getNamedFont( fontKey );
            if ( font == null )
                font = FontUtils.parseFont( fontKey, widget.getConfiguration().getGameResY() );
        }
        
        return ( font );
    }
    
    public final boolean isAntiAliased()
    {
        if ( antiAliased == null )
        {
            String fk = widget.getConfiguration().getNamedFontString( fontKey );
            if ( fk == null )
                fk = fontKey;
            
            antiAliased = FontUtils.parseAntiAliasFlag( fk );
        }
        
        return ( antiAliased );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setFont( String.valueOf( value ) );
        
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
            setFont( value );
            
            return ( true );
        }
        
        return ( false );
    }
    
    public FontProperty( Widget widget, String propertyName, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( propertyName, nameForDisplay, readonly, PropertyEditorType.FONT, null, null );
        
        this.widget = widget;
        this.fontKey = defaultValue;
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
