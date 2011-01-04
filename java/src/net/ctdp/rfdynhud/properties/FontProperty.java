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
package net.ctdp.rfdynhud.properties;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import net.ctdp.rfdynhud.util.FontUtils;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

/**
 * The {@link FontProperty} serves for customizing a font value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
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
    
    private final String defaultValue;
    
    private String fontKey;
    private Font font = null;
    private Boolean antiAliased = null;
    
    private FontMetrics metrics = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultValue()
    {
        return ( defaultValue );
    }
    
    public static String getDefaultNamedFontValue( String name )
    {
        if ( name.equals( STANDARD_FONT_NAME ) )
            return ( FontUtils.getFontString( "Dialog", Font.BOLD, 13, true, true ) );
        
        if ( name.equals( STANDARD_FONT2_NAME ) )
            return ( FontUtils.getFontString( "Dialog", Font.BOLD, 12, true, true ) );
        
        if ( name.equals( STANDARD_FONT3_NAME ) )
            return ( FontUtils.getFontString( "Dialog", Font.BOLD, 11, true, true ) );
        
        if ( name.equals( SMALLER_FONT_NAME ) )
            return ( FontUtils.getFontString( "Dialog", Font.BOLD, 13, true, true ) );
        
        if ( name.equals( SMALLER_FONT3_NAME ) )
            return ( FontUtils.getFontString( "Dialog", Font.BOLD, 9, true, true ) );
        
        if ( name.equals( BIGGER_FONT_NAME ) )
            return ( FontUtils.getFontString( "Dialog", Font.PLAIN, 14, true, true ) );
        
        return ( null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onKeeperSet()
    {
        super.onKeeperSet();
        
        onValueChanged( null, getValue() );
    }
    
    /**
     * Invoked when the value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    /**
     * Invoked when the value has been set.
     * 
     * @param value the new value
     */
    void onValueSet( String value )
    {
    }
    
    /**
     * Must be called after font names have changed in the editor.
     */
    public void refresh()
    {
        this.font = null;
    }
    
    /**
     * Sets the property's current value.
     * 
     * @param fontKey the new value
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setFont( String fontKey, boolean firstTime )
    {
        if ( ( fontKey == null ) && ( this.fontKey == null ) )
            return ( false );
        
        final WidgetsConfiguration widgetsConf = ( getKeeper() == null ) ? null : ( (Widget)getKeeper() ).getConfiguration();
        
        if ( widgetsConf == null )
        {
            this.fontKey = fontKey;
            this.font = null;
            this.antiAliased = null;
            this.metrics = null;
            
            onValueSet( this.fontKey );
            
            if ( !firstTime )
            {
                triggerKeepersOnPropertyChanged( null, fontKey );
                onValueChanged( null, fontKey );
            }
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
                onValueSet( this.fontKey );
                
                if ( !firstTime )
                {
                    triggerKeepersOnPropertyChanged( firstTime ? null : oldValue, fontKey );
                    onValueChanged( firstTime ? null : oldValue, fontKey );
                }
            }
        }
        
        return ( true );
    }
    
    /**
     * Sets the property's current value.
     * 
     * @param fontKey the new value
     * 
     * @return changed?
     */
    public final boolean setFont( String fontKey )
    {
        return ( setFont( fontKey, false ) );
    }
    
    /**
     * Sets the property's current value.
     * 
     * @param font
     * @param virtual
     * @param antiAliased
     * 
     * @return changed?
     */
    public final boolean setFont( Font font, boolean virtual, boolean antiAliased )
    {
        return ( setFont( FontUtils.getFontString( font, virtual, antiAliased ) ) );
    }
    
    /**
     * Sets the property's current value.
     * 
     * @param fontName
     * @param style
     * @param size
     * @param virtual
     * @param antiAliased
     * 
     * @return changed?
     */
    public final boolean setFont( String fontName, int style, int size, boolean virtual, boolean antiAliased )
    {
        return ( setFont( FontUtils.getFontString( fontName, style, size, virtual, antiAliased ) ) );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final String getFontKey()
    {
        return ( fontKey );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final Font getFont()
    {
        if ( fontKey == null )
            return ( null );
        
        if ( font == null )
        {
            final Widget widget = (Widget)getKeeper();
            final WidgetsConfiguration widgetsConfig = widget.getConfiguration();
            
            font = widgetsConfig.getNamedFont( fontKey );
            if ( font == null )
            {
                String fontStr = widget.getDefaultNamedFontValue( fontKey );
                if ( fontStr != null )
                {
                    widgetsConfig.addNamedFont( fontKey, fontStr );
                    font = widgetsConfig.getNamedFont( fontKey );
                }
                else
                {
                    font = FontUtils.parseFont( fontKey, widgetsConfig.getGameResolution().getViewportHeight(), false, true );
                }
            }
        }
        
        return ( font );
    }
    
    /**
     * Is the currently selected font anti aliased?
     * 
     * @return whether the currently selected font anti aliased?
     */
    public final boolean isAntiAliased()
    {
        if ( antiAliased == null )
        {
            final Widget widget = (Widget)getKeeper();
            final WidgetsConfiguration widgetsConfig = widget.getConfiguration();
            
            String fontStr = widget.getConfiguration().getNamedFontString( fontKey );
            if ( fontStr == null )
            {
                fontStr = widget.getDefaultNamedFontValue( fontKey );
                if ( fontStr != null )
                    widgetsConfig.addNamedFont( fontKey, fontStr );
                else
                    fontStr = fontKey;
            }
            
            antiAliased = FontUtils.parseAntiAliasFlag( fontStr, false, true );
        }
        
        return ( antiAliased );
    }
    
    /**
     * Gets font metrics for the selected font.
     * 
     * @return font metrics for the selected font.
     */
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
    public String getValue()
    {
        return ( fontKey );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        if ( loader.getSourceVersion().getBuild() < 92 )
        {
            value = value.replace( '-', FontUtils.SEPARATOR );
        }
        
        setValue( value );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    public FontProperty( String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.FONT, null, null );
        
        this.defaultValue = defaultValue;
        
        setFont( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public FontProperty( String name, String nameForDisplay, String defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public FontProperty( String name, String defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public FontProperty( String name, String defaultValue )
    {
        this( name, defaultValue, false );
    }
}
