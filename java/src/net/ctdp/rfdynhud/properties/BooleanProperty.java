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

/**
 * The {@link BooleanProperty} serves for customizing a primitive boolean value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class BooleanProperty extends Property
{
    private boolean value;
    
    /**
     * 
     * @param newValue
     */
    protected void onValueChanged( boolean newValue )
    {
    }
    
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
    public void loadValue( String value )
    {
        setValue( Boolean.parseBoolean( value ) );
    }
    
    /**
     * 
     * @param widgetsConfig
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    BooleanProperty( WidgetsConfiguration widgetsConfig, String name, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( widgetsConfig, name, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    public BooleanProperty( Widget widget, String name, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public BooleanProperty( Widget widget, String name, String nameForDisplay, boolean defaultValue )
    {
        this( widget, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param readonly
     */
    public BooleanProperty( Widget widget, String name, boolean defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public BooleanProperty( Widget widget, String name, boolean defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( w2pf, name, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, boolean defaultValue )
    {
        this( w2pf, name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param readonly
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, boolean defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public BooleanProperty( WidgetToPropertyForwarder w2pf, String name, boolean defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
}
