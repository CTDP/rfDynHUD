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

import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.__WPrivilegedAccess;

/**
 * The {@link StringProperty} serves for customizing a simple String value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StringProperty extends Property
{
    private final boolean forceTrimOnSet;
    private String value;
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( String oldValue, String newValue )
    {
    }
    
    public void setStringValue( String value )
    {
        if ( forceTrimOnSet && ( value != null ) )
            value = value.trim();
        
        if ( ( ( value == null ) && ( this.value == null ) ) || ( ( value != null ) && value.equals( this.value ) ) )
            return;
        
        String oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged();
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final String getStringValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setStringValue( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue()
    {
        return ( getStringValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( String value )
    {
        setValue( value );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param forceTrimOnSet
     * @param readonly
     * @param propEdType
     */
    protected StringProperty( Widget widget, String name, String nameForDisplay, String defaultValue, boolean forceTrimOnSet, boolean readonly, PropertyEditorType propEdType )
    {
        super( widget, name, nameForDisplay, readonly, propEdType, null, null );
        
        this.forceTrimOnSet = forceTrimOnSet;
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
    public StringProperty( Widget widget, String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( widget, name, nameForDisplay, defaultValue, false, readonly, PropertyEditorType.STRING );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public StringProperty( Widget widget, String name, String nameForDisplay, String defaultValue )
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
    public StringProperty( Widget widget, String name, String defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public StringProperty( Widget widget, String name, String defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param forceTrimOnSet
     * @param readonly
     * @param propEdType
     */
    protected StringProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue, boolean forceTrimOnSet, boolean readonly, PropertyEditorType propEdType )
    {
        super( w2pf, name, nameForDisplay, readonly, propEdType, null, null );
        
        this.forceTrimOnSet = forceTrimOnSet;
        this.value = defaultValue;
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    public StringProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, defaultValue, false, readonly, PropertyEditorType.STRING );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public StringProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, String defaultValue )
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
    public StringProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public StringProperty( WidgetToPropertyForwarder w2pf, String name, String defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
}
