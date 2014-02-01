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

import java.net.URL;

import net.ctdp.rfdynhud.util.RFDHLog;
import net.ctdp.rfdynhud.util.__UtilHelper;

import org.jagatoo.util.strings.StringUtils;


/**
 * The {@link Property} serves as a general data container and adapter.
 * You can use it to put data into a GUI component and live update it.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class Property
{
    private static DisplayNameGenerator displayNameGenerator = null;
    
    private PropertiesKeeper keeper = null;
    
    private final String name;
    private final String nameForDisplay;
    private final String nameForDisplay2;
    private final boolean readonly;
    private final PropertyEditorType editorType;
    private final String buttonText;
    private final String buttonTooltip;
    
    Object cellRenderer = null;
    Object cellEditor = null;
    
    private static DisplayNameGenerator newDefaultDisplayNameGenerator()
    {
        return ( new SpacedCamelCaseDisplayNameGenerator() );
    }
    
    private static final DisplayNameGenerator getDisplayNameGenerator()
    {
        if ( displayNameGenerator == null )
        {
            String className = __UtilHelper.editorPropertyDisplayNameGeneratorClass;
            if ( className == null )
            {
                displayNameGenerator = newDefaultDisplayNameGenerator();
                return ( displayNameGenerator );
            }
            
            Class<?> clazz = null;
            
            try
            {
                clazz = Class.forName( className );
            }
            catch ( ClassNotFoundException e )
            {
                displayNameGenerator = newDefaultDisplayNameGenerator();
                
                RFDHLog.exception( "WARNING: Could not find DisplayNameGenerator Class " + className + ". Using default " + displayNameGenerator.getClass().getName() + "." );
                
                return ( displayNameGenerator );
            }
            
            if ( !DisplayNameGenerator.class.isAssignableFrom( clazz ) )
            {
                displayNameGenerator = newDefaultDisplayNameGenerator();
                
                RFDHLog.exception( "WARNING: Class defined for propertyDisplayNameGenrator in rfdynhud.ini does not implement net.ctdp.rfdynhud.properties.DisplayNameGenerator. Using default " + displayNameGenerator.getClass().getName() + "." );
                
                return ( displayNameGenerator );
            }
            
            try
            {
                displayNameGenerator = (DisplayNameGenerator)clazz.newInstance();
            }
            catch ( InstantiationException e )
            {
                displayNameGenerator = newDefaultDisplayNameGenerator();
                
                RFDHLog.exception( "WARNING: Couldn't instantiate class " + className + " through the empty constructor. Falling back to default " + displayNameGenerator.getClass().getName() + "." );
            }
            catch ( IllegalAccessException e )
            {
                displayNameGenerator = newDefaultDisplayNameGenerator();
                
                RFDHLog.exception( "WARNING: Empty constructor is not accessible for class " + className + ". Falling back to default " + displayNameGenerator.getClass().getName() + "." );
            }
        }
        
        return ( displayNameGenerator );
    }
    
    protected void onKeeperSet()
    {
        triggerKeepersOnPropertyChanged( null, getValue() );
    }
    
    boolean setKeeper( PropertiesKeeper keeper, boolean force )
    {
        if ( !force && ( keeper == this.keeper ) )
            return ( false );
        
        this.keeper = keeper;
        
        onKeeperSet();
        
        return ( true );
    }
    
    /**
     * Gets the owner {@link PropertiesKeeper}.
     * 
     * @return the owner {@link PropertiesKeeper}.
     */
    public final PropertiesKeeper getKeeper()
    {
        return ( keeper );
    }
    
    /**
     * Gets the property's technical name.
     * 
     * @return the property's technical name.
     */
    public final String getName()
    {
        return ( name );
    }
    
    /**
     * Gets the property's name for editor display.
     * 
     * @return the property's name for editor display.
     */
    public String getNameForDisplay()
    {
        //return ( nameForDisplay );
        return ( nameForDisplay2 );
    }
    
    /**
     * Is read only property?
     * 
     * @return whether this property is read only.
     */
    public final boolean isReadOnly()
    {
        return ( readonly );
    }
    
    /**
     * Gets the proeprty editor type.
     * 
     * @return the proeprty editor type.
     */
    public final PropertyEditorType getEditorType()
    {
        return ( editorType );
    }
    
    public String getButtonText()
    {
        return ( buttonText );
    }
    
    public String getButtonTooltip()
    {
        return ( buttonTooltip );
    }
    
    /**
     * Sets the new value for this property.
     * 
     * @param value the new value
     */
    public abstract void setValue( Object value );
    
    /**
     * Gets the current value fo this property.
     * 
     * @return the current value fo this property.
     */
    public abstract Object getValue();
    
    /**
     * Gets the value to feed into the editor.
     * 
     * @return the value to feed into the editor.
     */
    public Object getValueForEditor()
    {
        return ( getValue() );
    }
    
    /**
     * Gets the default (initial) value fo this property.
     * 
     * @return the default value fo this property.
     */
    public abstract Object getDefaultValue();
    
    /**
     * Gets, whether this property currently has its default (initial) value.
     * 
     * @return whether this property currently has its default (initial) value.
     */
    public boolean hasDefaultValue()
    {
        Object value = getValue();
        Object defaultValue = getDefaultValue();
        
        if ( value == null )
            return ( defaultValue == null );
        
        if ( defaultValue == null )
            return ( false );
        
        return ( value.equals( defaultValue ) );
    }
    
    void triggerKeepersOnPropertyChanged( Object oldValue, Object newValue )
    {
        if ( keeper != null )
            keeper.onPropertyChanged( this, oldValue, newValue );
    }
    
    /**
     * 
     * @param button the clicked button
     */
    public void onButtonClicked( Object button )
    {
    }
    
    /**
     * Gets whether to quote this property's value in the config file (default is null, type dependent, numbers won't, others will).
     * 
     * @return whether to quote this property's value in the config file.
     */
    public Boolean quoteValueInConfigurationFile()
    {
        return ( null );
    }
    
    /**
     * Gets the value prepared for the configuration file.
     * This can be a String or some other primitive value.
     * 
     * @return the value prepared for the configuration file.
     */
    public Object getValueForConfigurationFile()
    {
        return ( getValue() );
    }
    
    /**
     * Checks whether the given key (from the configuration file) belongs to this {@link Property}.
     * 
     * @param key the probed property key
     * 
     * @return whether the given key (from the configuration file) belongs to this {@link Property}.
     */
    public boolean isMatchingKey( String key )
    {
        return ( key.equals( name ) );
    }
    
    /**
     * Loads the value from the configuration file.
     * 
     * @param loader the loader
     * @param value the value to load
     */
    public abstract void loadValue( PropertyLoader loader, String value );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        Class<?> clazz = this.getClass();
        //while ( clazz.getName().lastIndexOf( '$' ) >= 0 )
        while ( clazz.getSimpleName().equals( "" ) )
            clazz = clazz.getSuperclass();
        
        return ( clazz.getSimpleName() + "( \"" + getName() + "\" = \"" + String.valueOf( getValue() ) + "\" )" );
    }
    
    private String getDocumentationSource( Class<?> clazz )
    {
        URL docURL = clazz.getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/" + this.getName() + ".html" );
        
        if ( docURL == null )
        {
            if ( ( clazz.getSuperclass() != null ) && ( clazz.getSuperclass() != Object.class ) )
                return ( getDocumentationSource( clazz.getSuperclass() ) );
            
            return ( "" );
        }
        
        return ( StringUtils.loadString( docURL ) );
    }
    
    /**
     * Loads documentation for this {@link Property} from a &quote;doc&quote; folder under the keeper's package.
     * 
     * @return the documentation.
     */
    public String getDocumentationSource()
    {
        if ( keeper == null )
            return ( "" );
        
        return ( getDocumentationSource( keeper.getClass() ) );
    }
    
    /**
     * 
     * @param name the property name
     * @param nameForDisplay the name for editor display (<code>null</code> to use name)
     * @param readonly read only property?
     * @param editorType the property editor type
     * @param buttonText the text for the button (may be <code>null</code>)
     * @param buttonTooltip the tooltip for the button (ignored when button text is <code>null</code>)
     */
    protected Property( String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.name = name;
        this.nameForDisplay = ( nameForDisplay == null ) ? name : nameForDisplay;
        this.nameForDisplay2 = getDisplayNameGenerator().generateNameForDisplay( this.nameForDisplay );
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly read only property?
     * @param editorType the property editor type
     */
    public Property( String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( name, nameForDisplay, readonly, editorType, null, null );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param editorType the property editor type
     */
    public Property( String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param readonly read only property?
     * @param editorType the property editor type
     * @param buttonText the text for the button (may be <code>null</code>)
     * @param buttonTooltip the tooltip for the button (ignored when button text is <code>null</code>)
     */
    public Property( String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( name, null, readonly, editorType, buttonText, buttonTooltip );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param readonly read only property?
     * @param editorType the property editor type
     */
    public Property( String name, boolean readonly, PropertyEditorType editorType )
    {
        this( name, readonly, editorType, null, null );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param editorType the property editor type
     */
    public Property( String name, PropertyEditorType editorType )
    {
        this( name, false, editorType, null, null );
    }
}
