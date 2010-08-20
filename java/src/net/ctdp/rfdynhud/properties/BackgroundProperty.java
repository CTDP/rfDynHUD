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
    public static final String COLOR_INDICATOR = "color:";
    public static final String IMAGE_INDICATOR = "image:";
    
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
    
    private BackgroundType backgroundType = null;
    
    /**
     * Gets the current type of this background.
     * 
     * @return the current type of this background.
     */
    public final BackgroundType getBackgroundType()
    {
        return ( backgroundType );
    }
    
    /**
     * 
     * @param oldBGType
     * @param newBGType
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( BackgroundType oldBGType, BackgroundType newBGType, String oldValue, String newValue )
    {
    }
    
    private boolean setPropertyFromValue( String value, boolean suppressEvent )
    {
        if ( value == null )
            throw new IllegalArgumentException( "value must not be null." );
        
        value = value.trim();
        
        if ( ( getBackgroundType() != null ) && value.equals( getValue() ) )
            return ( false );
        
        BackgroundType oldBGType = this.backgroundType;
        String oldValue = ( oldBGType == null ) ? null : ( oldBGType.isColor() ? this.getColorProperty().getColorKey() : this.getImageProperty().getImageName() );
        
        if ( value.startsWith( BackgroundProperty.COLOR_INDICATOR ) )
        {
            value = value.substring( BackgroundProperty.COLOR_INDICATOR.length() ).trim();
            
            if ( color == null )
                this.color = new ColorProperty( widget, getName(), getNameForDisplay(), value, false );
            else
                this.color.setColor( value );
            
            if ( image == null )
                this.image = new ImageProperty( widget, getName(), getNameForDisplay(), "default_rev_meter_bg.png", false, false );
            
            this.backgroundType = BackgroundType.COLOR;
            
            if ( !suppressEvent && ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) )
            {
                onValueChanged();
                onValueChanged( oldBGType, backgroundType, oldValue, value );
            }
        }
        else if ( value.startsWith( BackgroundProperty.IMAGE_INDICATOR ) )
        {
            value = value.substring( BackgroundProperty.IMAGE_INDICATOR.length() ).trim();
            
            if ( color == null )
                this.color = new ColorProperty( widget, getName(), getNameForDisplay(), "StandardBackground", false );
            
            if ( image == null )
                this.image = new ImageProperty( widget, getName(), getNameForDisplay(), value, false, false );
            else
                this.image.setImageName( value );
            
            this.backgroundType = BackgroundType.IMAGE;
            
            if ( !suppressEvent && ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) )
            {
                onValueChanged();
                onValueChanged( oldBGType, backgroundType, oldValue, value );
            }
        }
        else
        {
            throw new IllegalArgumentException( "The value must either start with \"" + BackgroundProperty.COLOR_INDICATOR + "\" or \"" + BackgroundProperty.IMAGE_INDICATOR + ":\"." );
        }
        
        return ( true );
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
        setPropertyFromValue( String.valueOf( value ), false );
    }
    
    /**
     * Sets this property to a color of the given value.
     * 
     * @param value
     */
    public void setColorValue( String value )
    {
        setPropertyFromValue( COLOR_INDICATOR + value, false );
    }
    
    /**
     * Sets this property to an image of the given value.
     * 
     * @param value
     */
    public void setImageValue( String value )
    {
        setPropertyFromValue( IMAGE_INDICATOR + value, false );
    }
    
    public void setValues( BackgroundType type, String colorValue, String imageValue )
    {
        BackgroundType oldBGType = getBackgroundType();
        String oldValue = null;
        if ( oldBGType.isColor() )
            oldValue = getColorProperty().getValue();
        else if ( oldBGType.isImage() )
            oldValue = getImageProperty().getValue();
        
        if ( type.isColor() )
        {
            if ( colorValue.equals( oldValue ) )
                return;
            
            setPropertyFromValue( IMAGE_INDICATOR + imageValue, true );
            setPropertyFromValue( COLOR_INDICATOR + colorValue, true );
            
            onValueChanged();
            onValueChanged( oldBGType, backgroundType, oldValue, colorValue );
        }
        else if ( type.isImage() )
        {
            if ( imageValue.equals( oldValue ) )
                return;
            
            setPropertyFromValue( COLOR_INDICATOR + colorValue, true );
            setPropertyFromValue( IMAGE_INDICATOR + imageValue, true );
            
            onValueChanged();
            onValueChanged( oldBGType, backgroundType, oldValue, imageValue );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue()
    {
        if ( getBackgroundType().isColor() )
            return ( BackgroundProperty.COLOR_INDICATOR + getColorProperty().getValue() );
        
        if ( getBackgroundType().isImage() )
            return ( BackgroundProperty.IMAGE_INDICATOR + getImageProperty().getValue() );
        
        throw new Error( "Unreachable code" );
    }
    
    /**
     * Gets the {@link Color} from this {@link BackgroundProperty}.
     * The result is only valid, if the {@link BackgroundType} ({@link #getBackgroundType()}) is {@value BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link BackgroundProperty}.
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
     * @return the {@link Color} from this {@link BackgroundProperty}.
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
        setPropertyFromValue( value, false );
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
        
        setPropertyFromValue( defaultValue, true );
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
