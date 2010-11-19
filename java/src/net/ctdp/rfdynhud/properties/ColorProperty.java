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

import java.awt.Color;

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.openmali.vecmath2.util.ColorUtils;

/**
 * The {@link ColorProperty} serves for customizing a color value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ColorProperty extends Property
{
    public static final String STANDARD_BACKGROUND_COLOR_NAME = "StandardBackground";
    public static final String STANDARD_FONT_COLOR_NAME = "StandardFontColor";
    
    public static final Color FALLBACK_COLOR = Color.MAGENTA;
    
    private String colorKey;
    private Color color = null;
    
    public static String getDefaultNamedColorValue( String name )
    {
        if ( name.equals( STANDARD_BACKGROUND_COLOR_NAME ) )
            return ( "#00000096" );
        
        if ( name.equals( STANDARD_FONT_COLOR_NAME ) )
            return ( "#C0BC3D" );
        
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
     * Must be called after color names have changed in the editor.
     */
    public void refresh()
    {
        this.color = null;
    }
    
    /**
     * Sets the new property's value.
     * 
     * @param colorKey the new value
     * @param firstTime
     * 
     * @return changed?
     */
    protected final boolean setColor( String colorKey, boolean firstTime )
    {
        if ( ( ( colorKey == null ) && ( this.colorKey == null ) ) || ( ( colorKey != null ) && colorKey.equals( this.colorKey ) ) )
            return ( false );
        
        String oldValue = this.colorKey;
        this.colorKey = colorKey;
        this.color = null;
        
        onValueSet( this.colorKey );
        
        if ( !firstTime )
        {
            triggerKeepersOnPropertyChanged( oldValue, colorKey );
            onValueChanged( oldValue, colorKey );
        }
        
        return ( true );
    }
    
    /**
     * Sets the new property's value.
     * 
     * @param colorKey the new value
     * 
     * @return changed?
     */
    public final boolean setColor( String colorKey )
    {
        return ( setColor( colorKey, false ) );
    }
    
    /**
     * Sets the new property's value.
     * 
     * @param color the new value
     * 
     * @return changed?
     */
    public final boolean setColor( Color color )
    {
        return ( setColor( ColorUtils.colorToHex( color ) ) );
    }
    
    /**
     * Sets the new property's value.
     * 
     * @param red
     * @param green
     * @param blue
     * 
     * @return changed?
     */
    public final boolean setColor( int red, int green, int blue )
    {
        return ( setColor( ColorUtils.colorToHex( red, green, blue ) ) );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final String getColorKey()
    {
        return ( colorKey );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final Color getColor()
    {
        if ( colorKey == null )
            return ( null );
        
        if ( color == null )
        {
            final Widget widget = (Widget)getKeeper();
            final WidgetsConfiguration widgetsConf = widget.getConfiguration();
            
            color = widgetsConf.getNamedColor( colorKey );
            
            if ( color == null )
            {
                String colorStr = widget.getDefaultNamedColorValue( colorKey );
                if ( colorStr != null )
                {
                    Color color2 = ColorUtils.hexToColor( colorStr, false );
                    if ( color2 != null )
                    {
                        widgetsConf.addNamedColor( colorKey, color2 );
                        color = color2;
                    }
                }
            }
            
            if ( ( color == null ) && ( ( color = ColorUtils.hexToColor( colorKey, false ) ) == null ) )
                color = FALLBACK_COLOR;
        }
        
        return ( color );
    }
    
    /**
     * Gets, whether this {@link ColorProperty} hosts a visible color value.
     * 
     * @return <code>true</code>, if and only if {@link #getColor()} returns a non <code>null</code> value with an alpha channel > 0.
     */
    public final boolean hasVisibleColor()
    {
        Color color = getColor();
        
        return ( ( color != null ) && ( color.getAlpha() > 0 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setColor( ( value == null ) ? null : String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue()
    {
        return ( colorKey );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( value );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public ColorProperty( String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.COLOR, null, null );
        
        setColor( defaultValue, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public ColorProperty( String name, String nameForDisplay, String defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public ColorProperty( String name, String defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public ColorProperty( String name, String defaultValue )
    {
        this( name, defaultValue, false );
    }
}
