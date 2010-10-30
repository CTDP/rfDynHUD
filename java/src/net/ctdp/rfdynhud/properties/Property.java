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

import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.__WPrivilegedAccess;

/**
 * The {@link Property} serves as a general data container and adapter.
 * You can use it to put data into a GUI component and live update it.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class Property
{
    protected final WidgetsConfiguration widgetsConfig;
    protected /*final*/ Widget widget;
    
    private final String name;
    private final String nameForDisplay;
    private final String nameForDisplay2;
    private final boolean readonly;
    private final PropertyEditorType editorType;
    private final String buttonText;
    private final String buttonTooltip;
    
    Object cellRenderer = null;
    Object cellEditor = null;
    
    /**
     * Gets the owner {@link Widget}.
     * 
     * @return the owner {@link Widget}.
     */
    public final Widget getWidget()
    {
        return ( widget );
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
    
    protected void triggerCommonOnValueChanged( Object oldValue, Object newValue )
    {
        if ( widgetsConfig != null )
        {
            for ( int i = 0; i < widgetsConfig.getNumWidgets(); i++ )
                widgetsConfig.getWidget( i ).forceAndSetDirty( true );
        }
        
        if ( widget != null )
        {
            if ( widget.getConfiguration() != null )
                __WPrivilegedAccess.onPropertyChanged( this, oldValue, newValue, widget );
            
            widget.forceAndSetDirty( true );
        }
    }
    
    protected boolean getTriggerOnValueChangedBeforeAttachedToConfig()
    {
        return ( false );
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
     * @param value the value to load
     */
    public abstract void loadValue( String value );
    
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
    
    private static String generateNameForDisplay( String nameForDisplay )
    {
        StringBuilder sb = new StringBuilder( String.valueOf( Character.toLowerCase( nameForDisplay.charAt( 0 ) ) ) );
        
        boolean lastUpper = Character.isUpperCase( nameForDisplay.charAt( 0 ) );
        
        for ( int i = 1; i < nameForDisplay.length(); i++ )
        {
            char ch = nameForDisplay.charAt( i );
            
            if ( Character.isUpperCase( ch ) )
            {
                if ( lastUpper )
                {
                    sb.setCharAt( sb.length() - 1, Character.toUpperCase( sb.charAt( sb.length() - 1 ) ) );
                    sb.append( ch );
                }
                else
                {
                    sb.append( ' ' );
                    sb.append( Character.toLowerCase( ch ) );
                }
                
                lastUpper = true;
            }
            else
            {
                sb.append( ch );
                
                lastUpper = false;
            }
        }
        
        return ( sb.toString() );
    }
    
    /**
     * 
     * @param widgetsConfig the ownding {@link WidgetsConfiguration}
     * @param name the property name
     * @param nameForDisplay the name for editor display (<code>null</code> to use name)
     * @param readonly read only property?
     * @param editorType the property editor type
     * @param buttonText the text for the button (may be <code>null</code>)
     * @param buttonTooltip the tooltip for the button (ignored when button text is <code>null</code>)
     */
    protected Property( WidgetsConfiguration widgetsConfig, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = widgetsConfig;
        this.widget = null;
        this.name = name;
        this.nameForDisplay = nameForDisplay;
        this.nameForDisplay2 = generateNameForDisplay( this.nameForDisplay );
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly read only property?
     * @param editorType the property editor type
     * @param buttonText the text for the button (may be <code>null</code>)
     * @param buttonTooltip the tooltip for the button (ignored when button text is <code>null</code>)
     */
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = null;
        this.widget = widget;
        this.name = name;
        this.nameForDisplay = ( nameForDisplay == null ) ? name : nameForDisplay;
        this.nameForDisplay2 = generateNameForDisplay( this.nameForDisplay );
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly read only property?
     * @param editorType the property editor type
     */
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, readonly, editorType, null, null );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param editorType the property editor type
     */
    public Property( Widget widget, String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param readonly read only property?
     * @param editorType the property editor type
     * @param buttonText the text for the button (may be <code>null</code>)
     * @param buttonTooltip the tooltip for the button (ignored when button text is <code>null</code>)
     */
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( widget, name, null, readonly, editorType, buttonText, buttonTooltip );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param readonly read only property?
     * @param editorType the property editor type
     */
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, readonly, editorType, null, null );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param editorType the property editor type
     */
    public Property( Widget widget, String name, PropertyEditorType editorType )
    {
        this( widget, name, false, editorType, null, null );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly read only property?
     * @param editorType the property editor type
     * @param buttonText the text for the button (may be <code>null</code>)
     * @param buttonTooltip the tooltip for the button (ignored when button text is <code>null</code>)
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = null;
        this.widget = null;
        this.name = name;
        this.nameForDisplay = ( nameForDisplay == null ) ? name : nameForDisplay;
        this.nameForDisplay2 = generateNameForDisplay( this.nameForDisplay );
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly read only property?
     * @param editorType the property editor type
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( w2pf, name, nameForDisplay, readonly, editorType, null, null );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param editorType the property editor type
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( w2pf, name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param readonly read only property?
     * @param editorType the property editor type
     * @param buttonText the text for the button (may be <code>null</code>)
     * @param buttonTooltip the tooltip for the button (ignored when button text is <code>null</code>)
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( w2pf, name, null, readonly, editorType, buttonText, buttonTooltip );
    }
    
    /**
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param readonly read only property?
     * @param editorType the property editor type
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, boolean readonly, PropertyEditorType editorType )
    {
        this( w2pf, name, readonly, editorType, null, null );
    }
    
    /**
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param editorType the property editor type
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, PropertyEditorType editorType )
    {
        this( w2pf, name, false, editorType, null, null );
    }
}
