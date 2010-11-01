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

import java.util.Collection;

import net.ctdp.rfdynhud.widgets.base.widget.Widget;
import net.ctdp.rfdynhud.widgets.base.widget.__WPrivilegedAccess;

import org.jagatoo.util.Tools;

/**
 * The {@link ListProperty} serves for customizing a value from a list.
 * 
 * @author Marvin Froehlich (CTDP)
 * 
 * @param <E> the list element type
 * @param <L> the list type
 */
public class ListProperty<E extends Object, L extends Collection<E>> extends Property
{
    private L list;
    private E value;
    
    /**
     * Gets the data list.
     * 
     * @return the data list.
     */
    public final L getList()
    {
        return ( list );
    }
    
    /**
     * Invoked when the value has changed.
     * 
     * @param oldValue the old value
     * @param newValue the new value
     */
    protected void onValueChanged( E oldValue, E newValue )
    {
    }
    
    /**
     * Sets the new selected value for this property.
     * 
     * @param value the new value
     * 
     * @return changed?
     */
    public boolean setSelectedValue( E value )
    {
        if ( Tools.objectsEqual( value, this.value ) )
            return ( false );
        
        E oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty( true );
        
        triggerCommonOnValueChanged( oldValue, value );
        if ( getTriggerOnValueChangedBeforeAttachedToConfig() || ( ( getWidget() != null ) && ( getWidget().getConfiguration() != null ) ) )
            onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
        
        return ( false );
    }
    
    /**
     * Gets the currently selected element.
     * 
     * @return the currently selected element.
     */
    public final E getSelectedValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public void setValue( Object value )
    {
        setSelectedValue( (E)value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public E getValue()
    {
        return ( getSelectedValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        for ( E e : this.list )
        {
            if ( Tools.objectsEqual( value, e ) )
            {
                setValue( e );
                
                return;
            }
        }
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     * @param buttonText the text for the displayed button in the editor
     */
    public ListProperty( Widget widget, String name, String nameForDisplay, E defaultValue, L list, boolean readonly, String buttonText )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.LIST, buttonText, null );
        
        this.value = defaultValue;
        this.list = list;
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     */
    public ListProperty( Widget widget, String name, String nameForDisplay, E defaultValue, L list, boolean readonly )
    {
        this( widget, name, nameForDisplay, defaultValue, list, readonly, null );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     */
    public ListProperty( Widget widget, String name, String nameForDisplay, E defaultValue, L list )
    {
        this( widget, name, nameForDisplay, defaultValue, list, false );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     */
    public ListProperty( Widget widget, String name, E defaultValue, L list, boolean readonly )
    {
        this( widget, name, null, defaultValue, list, readonly );
    }
    
    /**
     * 
     * @param widget the owner widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param list the data list
     */
    public ListProperty( Widget widget, String name, E defaultValue, L list )
    {
        this( widget, name, defaultValue, list, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     * @param buttonText the text for the displayed button in the editor
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, L list, boolean readonly, String buttonText )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue, list, readonly, buttonText );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, L list, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, defaultValue, list, readonly, null );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param list the data list
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, E defaultValue, L list )
    {
        this( w2pf, name, nameForDisplay, defaultValue, list, false );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param list the data list
     * @param readonly read only property?
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue, L list, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, list, readonly );
    }
    
    /**
     * 
     * @param w2pf call {@link WidgetToPropertyForwarder#finish(Widget)} after all
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param list the data list
     */
    public ListProperty( WidgetToPropertyForwarder w2pf, String name, E defaultValue, L list )
    {
        this( w2pf, name, defaultValue, list, false );
    }
}
