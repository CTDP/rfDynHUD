/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
    
    private final String defaultValue;
    
    private ColorProperty color = null;
    private ImageProperty image = null;
    
    private BackgroundType backgroundType = null;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultValue()
    {
        return ( defaultValue );
    }
    
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
     * {@inheritDoc}
     */
    @Override
    protected void onKeeperSet()
    {
        super.onKeeperSet();
        
        onValueChanged( null, getBackgroundType(), null, getValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean setKeeper( PropertiesKeeper keeper, boolean force )
    {
        boolean result = super.setKeeper( keeper, force );
        
        if ( result )
        {
            if ( color != null )
                color.setKeeper( keeper, force );
            if ( image != null )
                image.setKeeper( keeper, force );
        }
        
        return ( result );
    }
    
    /**
     * Invoked when the value has changed.
     * 
     * @param oldBGType the old background type
     * @param newBGType thew new background type
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( BackgroundType oldBGType, BackgroundType newBGType, String oldValue, String newValue )
    {
    }
    
    /**
     * Invoked when the value has been set.
     * 
     * @param bgType thew new background type
     * @param value the new value
     */
    void onValueSet( BackgroundType bgType, String value )
    {
    }
    
    private boolean setPropertyFromValue( String value, boolean firstTime, boolean suppressEvent )
    {
        if ( value == null )
            throw new IllegalArgumentException( "value must not be null." );
        
        value = value.trim();
        
        if ( ( getBackgroundType() != null ) && value.equals( getValue() ) )
            return ( false );
        
        BackgroundType oldBGType = firstTime ? null : this.backgroundType;
        String oldValue = firstTime ? null : ( ( oldBGType == null ) ? null : ( oldBGType.isColor() ? this.getColorProperty().getColorKey() : this.getImageProperty().getImageName() ) );
        
        if ( value.startsWith( BackgroundProperty.COLOR_INDICATOR ) )
        {
            value = value.substring( BackgroundProperty.COLOR_INDICATOR.length() ).trim();
            
            if ( color == null )
            {
                this.color = new ColorProperty( getName(), getNameForDisplay(), value, false );
                if ( getKeeper() != null )
                    this.color.setKeeper( getKeeper(), false );
            }
            else
            {
                this.color.setColor( value );
            }
            
            if ( image == null )
            {
                this.image = new ImageProperty( getName(), getNameForDisplay(), "default_rev_meter_bg.png", false, false );
                if ( getKeeper() != null )
                    this.image.setKeeper( getKeeper(), false );
            }
            
            this.backgroundType = BackgroundType.COLOR;
            
            onValueSet( this.backgroundType, value );
            
            if ( !suppressEvent )
            {
                triggerKeepersOnPropertyChanged( oldValue, value );
                onValueChanged( oldBGType, backgroundType, oldValue, value );
            }
        }
        else if ( value.startsWith( BackgroundProperty.IMAGE_INDICATOR ) )
        {
            value = value.substring( BackgroundProperty.IMAGE_INDICATOR.length() ).trim();
            
            if ( color == null )
            {
                this.color = new ColorProperty( getName(), getNameForDisplay(), "StandardBackground", false );
                if ( getKeeper() != null )
                    this.color.setKeeper( getKeeper(), false );
            }
            
            if ( image == null )
            {
                this.image = new ImageProperty( getName(), getNameForDisplay(), value, false, false );
                if ( getKeeper() != null )
                    this.image.setKeeper( getKeeper(), false );
            }
            else
            {
                this.image.setImageName( value );
            }
            
            this.backgroundType = BackgroundType.IMAGE;
            
            onValueSet( this.backgroundType, value );
            
            if ( !suppressEvent )
            {
                triggerKeepersOnPropertyChanged( oldValue, value );
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
        setPropertyFromValue( String.valueOf( value ), false, false );
    }
    
    /**
     * Sets this property to a color of the given value.
     * 
     * @param value the new color value/name
     * 
     * @return changed?
     */
    public boolean setColorValue( String value )
    {
        return ( setPropertyFromValue( COLOR_INDICATOR + value, false, false ) );
    }
    
    /**
     * Sets this property to an image of the given value.
     * 
     * @param value the new image name
     * 
     * @return changed?
     */
    public boolean setImageValue( String value )
    {
        return ( setPropertyFromValue( IMAGE_INDICATOR + value, false, false ) );
    }
    
    /**
     * Sets the current value.
     * 
     * @param type
     * @param colorValue
     * @param imageValue
     */
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
            
            setPropertyFromValue( IMAGE_INDICATOR + imageValue, false, true );
            setPropertyFromValue( COLOR_INDICATOR + colorValue, false, true );
            
            triggerKeepersOnPropertyChanged( oldValue, colorValue );
            onValueChanged( oldBGType, backgroundType, oldValue, colorValue );
        }
        else if ( type.isImage() )
        {
            if ( imageValue.equals( oldValue ) )
                return;
            
            setPropertyFromValue( COLOR_INDICATOR + colorValue, false, true );
            setPropertyFromValue( IMAGE_INDICATOR + imageValue, false, true );
            
            triggerKeepersOnPropertyChanged( oldValue, imageValue );
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
     * The result is only valid, if the {@link BackgroundType} ({@link #getBackgroundType()}) is {@link BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link BackgroundProperty}.
     */
    public final Color getColorValue()
    {
        if ( ( getColorProperty() == null ) || !getBackgroundType().isColor() )
            return ( null );
        
        return ( getColorProperty().getColor() );
    }
    
    /**
     * Gets the {@link Color} from this {@link BackgroundProperty}.
     * The result is only valid, if the {@link BackgroundType} ({@link #getBackgroundType()}) is {@link BackgroundType#COLOR}.
     * 
     * @return the {@link Color} from this {@link BackgroundProperty}.
     */
    public final ImageTemplate getImageValue()
    {
        if ( ( getImageProperty() == null ) || !getBackgroundType().isImage() )
            return ( null );
        
        return ( getImageProperty().getImage() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        // backwards compatiblity!
        
        if ( loader.getSourceVersion().getBuild() < 91 )
        {
            if ( value.startsWith( IMAGE_INDICATOR ) && ( getKeeper() != null ) )
            {
                String value2 = value;
                
                if ( getKeeper().getClass().getSimpleName().startsWith( "ETV" ) )
                {
                    if ( value.startsWith( "etv2010/", IMAGE_INDICATOR.length() ) )
                        value2 = IMAGE_INDICATOR + "etv2010/telemetry/" + value.substring( IMAGE_INDICATOR.length() + 8 );
                }
                else
                {
                    if ( value.startsWith( "default_", IMAGE_INDICATOR.length() ) )
                        value2 = IMAGE_INDICATOR + "standard/" + value.substring( IMAGE_INDICATOR.length() + 8 );
                    else
                        value2 = IMAGE_INDICATOR + "standard/" + value.substring( IMAGE_INDICATOR.length() );
                }
                
                if ( value2.equals( defaultValue ) )
                    value = value2;
            }
        }
        
        setPropertyFromValue( value, false, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public BackgroundProperty( String name, String nameForDisplay, String defaultValue )
    {
        super( name, nameForDisplay, false, PropertyEditorType.BACKGROUND );
        
        this.defaultValue = defaultValue;
        
        setPropertyFromValue( defaultValue, true, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public BackgroundProperty( String name, String defaultValue )
    {
        this( name, null, defaultValue );
    }
}
