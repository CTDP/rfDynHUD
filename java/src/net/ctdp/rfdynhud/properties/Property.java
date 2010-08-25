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
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

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
    private final boolean readonly;
    private final PropertyEditorType editorType;
    private final String buttonText;
    private final String buttonTooltip;
    
    Object cellRenderer = null;
    Object cellEditor = null;
    
    public final Widget getWidget()
    {
        return ( widget );
    }
    
    public final String getName()
    {
        return ( name );
    }
    
    public String getNameForDisplay()
    {
        return ( nameForDisplay );
    }
    
    public final boolean isReadOnly()
    {
        return ( readonly );
    }
    
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
    
    public abstract void setValue( Object value );
    
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
     * @param button
     */
    public void onButtonClicked( Object button )
    {
    }
    
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
     * @param key
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
     * @param value
     */
    public abstract void loadValue( String value );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( this.getClass().getSimpleName() + "( \"" + getName() + "\" = \"" + String.valueOf( getValue() ) + "\" )" );
    }
    
    /**
     * 
     * @param widgetsConfig
     * @param name
     * @param nameForDisplay
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    protected Property( WidgetsConfiguration widgetsConfig, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = widgetsConfig;
        this.widget = null;
        this.name = name;
        this.nameForDisplay = nameForDisplay;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = null;
        this.widget = widget;
        this.name = name;
        this.nameForDisplay = ( nameForDisplay == null ) ? name : nameForDisplay;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly
     * @param editorType
     */
    public Property( Widget widget, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, readonly, editorType, null, null );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param editorType
     */
    public Property( Widget widget, String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( widget, name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * 
     * @param name
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( widget, name, null, readonly, editorType, buttonText, buttonTooltip );
    }
    
    public Property( Widget widget, String name, boolean readonly, PropertyEditorType editorType )
    {
        this( widget, name, readonly, editorType, null, null );
    }
    
    public Property( Widget widget, String name, PropertyEditorType editorType )
    {
        this( widget, name, false, editorType, null, null );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this.widgetsConfig = null;
        this.widget = null;
        this.name = name;
        this.nameForDisplay = ( nameForDisplay == null ) ? name : nameForDisplay;
        this.readonly = readonly;
        this.editorType = editorType;
        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly
     * @param editorType
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean readonly, PropertyEditorType editorType )
    {
        this( w2pf, name, nameForDisplay, readonly, editorType, null, null );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param editorType
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, PropertyEditorType editorType )
    {
        this( w2pf, name, nameForDisplay, false, editorType, null, null );
    }
    
    /**
     * @param w2pf
     * @param name
     * @param readonly
     * @param editorType
     * @param buttonText
     * @param buttonTooltip
     */
    public Property( WidgetToPropertyForwarder w2pf, String name, boolean readonly, PropertyEditorType editorType, String buttonText, String buttonTooltip )
    {
        this( w2pf, name, null, readonly, editorType, buttonText, buttonTooltip );
    }
    
    public Property( WidgetToPropertyForwarder w2pf, String name, boolean readonly, PropertyEditorType editorType )
    {
        this( w2pf, name, readonly, editorType, null, null );
    }
    
    public Property( WidgetToPropertyForwarder w2pf, String name, PropertyEditorType editorType )
    {
        this( w2pf, name, false, editorType, null, null );
    }
}
