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

import net.ctdp.rfdynhud.render.ImageTemplate;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link BackgroundProperty} unites {@link ColorProperty} and {@link ImageProperty}
 * to select something for a Widget's background.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BackgroundProperty extends Property
{
    public static enum BackgroundType
    {
        COLOR,
        IMAGE,
        ;
        
        /**
         * Whether this type is {@link #COLOR}.
         * 
         * @return this type is {@link #COLOR}.
         */
        public final boolean isColor()
        {
            return ( this == COLOR );
        }
        
        /**
         * Whether this type is {@link #IMAGE}.
         * 
         * @return this type is {@link #IMAGE}.
         */
        public final boolean isImage()
        {
            return ( this == IMAGE );
        }
    }
    
    private ColorProperty color = null;
    private ImageProperty image = null;
    
    private BackgroundType backgroundType;
    
    /**
     * Gets the current type of this background.
     * 
     * @return the current type of this background.
     */
    public final BackgroundType getBackgroundType()
    {
        return ( backgroundType );
    }
    
    private void setPropertyFromValue( String value )
    {
        if ( value == null )
            throw new IllegalArgumentException( "value must not be null." );
        
        value = value.trim();
        
        if ( value.startsWith( "color:" ) )
        {
            value = value.substring( 6 ).trim();
            
            if ( color == null )
                this.color = new ColorProperty( widget, getName(), getNameForDisplay(), value, false );
            else
                this.color.setColor( value );
            
            if ( image == null )
                this.image = new ImageProperty( widget, getName(), getNameForDisplay(), "default_rev_meter_bg.png", false, false );
            
            this.backgroundType = BackgroundType.COLOR;
        }
        else if ( value.startsWith( "image:" ) )
        {
            value = value.substring( 6 ).trim();
            
            if ( color == null )
                this.color = new ColorProperty( widget, getName(), getNameForDisplay(), "StandardBackground", false );
            
            if ( image == null )
                this.image = new ImageProperty( widget, getName(), getNameForDisplay(), value, false, false );
            else
                this.image.setImageName( value );
            
            this.backgroundType = BackgroundType.IMAGE;
        }
        else
        {
            throw new IllegalArgumentException( "The value must either start with \"color:\" or \"image:\"." );
        }
    }
    
    /**
     * Gets the encapsulated {@link ColorProperty}.
     * 
     * @return the encapsulated {@link ColorProperty}.
     */
    public final ColorProperty getColorProperty()
    {
        return ( color );
    }
    
    /**
     * Gets the encapsulated {@link ImageProperty}.
     * 
     * @return the encapsulated {@link ImageProperty}.
     */
    public final ImageProperty getImageProperty()
    {
        return ( image );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setPropertyFromValue( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue()
    {
        if ( getBackgroundType().isColor() )
            return ( "color:" + getColorProperty().getValue() );
        
        if ( getBackgroundType().isImage() )
            return ( "image:" + getImageProperty().getValue() );
        
        throw new Error( "Unreachable code" );
    }
    
    /**
     * Gets the {@link Color} from this {@link BackgroundProperty}.
     * The result is only valid, if the {@link BackgroundType} ({@link #getBackgroundType()}) is {@value BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link Background}.
     */
    public final Color getColorValue()
    {
        if ( getColorProperty() == null )
            return ( null );
        
        return ( getColorProperty().getColor() );
    }
    
    /**
     * Gets the {@link Color} from this {@link BackgroundProperty}.
     * The result is only valid, if the {@link BackgroundType} ({@link #getBackgroundType()}) is {@value BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link Background}.
     */
    public final ImageTemplate getImageValue()
    {
        if ( getImageProperty() == null )
            return ( null );
        
        return ( getImageProperty().getImage() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( String value )
    {
        setPropertyFromValue( value );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public BackgroundProperty( Widget widget, String name, String nameForDisplay, String defaultValue )
    {
        super( widget, name, nameForDisplay, false, PropertyEditorType.BACKGROUND );
        
        setPropertyFromValue( defaultValue );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public BackgroundProperty( Widget widget, String name, String defaultValue )
    {
        this( widget, name, null, defaultValue );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public BackgroundProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public BackgroundProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue )
    {
        this( w2pf, name, null, defaultValue );
    }
}
