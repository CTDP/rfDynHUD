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

/**
 * The {@link BooleanProperty} serves for customizing a primitive boolean value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BooleanProperty extends Property
{
    private boolean value;
    
    /**
     * Invoked when the value has changed.
     * 
     * @param newValue the new value
     */
    protected void onValueChanged( boolean newValue )
    {
    }
    
    /**
     * Sets the property's value.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public boolean setBooleanValue( boolean value )
    {
        if ( value == this.value )
            return ( false );
        
        this.value = value;
        
        triggerCommonOnValueChanged( !value, value );
        if ( getTriggerOnValueChangedBeforeAttachedToConfig() || ( ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) ) )
            onValueChanged( value );
        
        return ( true );
    }
    
    /**
     * Gets the property's current value.
     * 
     * @return the property's current value.
     */
    public final boolean getBooleanValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setBooleanValue( ( (Boolean)value ).booleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean getValue()
    {
        return ( getBooleanValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( Boolean.parseBoolean( value ) );
    }
    
    /**
     * 
     * @param widgetsConfig the owner widgets configuration
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly read only property?
     */
    BooleanProperty( WidgetsConfiguration widgetsConfig, String name, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( widgetsConfig, name, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BooleanProperty( Widget widget, String name, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public BooleanProperty( Widget widget, String name, String nameForDisplay, boolean defaultValue )
    {
        this( widget, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BooleanProperty( Widget widget, String name, boolean defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public BooleanProperty( Widget widget, String name, boolean defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( w2pf, name, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean defaultValue )
    {
        this( w2pf, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, boolean defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, boolean defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
}
