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
    
    public void setBooleanValue( boolean value )
    {
        if ( value == this.value )
            return;
        
        this.value = value;
        
        onValueChanged();
        onValueChanged( value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, !value, value, widget );
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
    public Object getValue()
    {
        return ( getBooleanValue() );
    }
    
    public final boolean loadProperty( String key, String value )
    {
        if ( key.equals( getPropertyName() ) )
        {
            setValue( Boolean.parseBoolean( value ) );
            
            return ( true );
        }
        
        return ( false );
    }
    
    BooleanProperty( WidgetsConfiguration widgetsConfig, String propertyName, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( widgetsConfig, propertyName, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
    }
    
    public BooleanProperty( Widget widget, String propertyName, String nameForDisplay, boolean defaultValue, boolean readonly )
    {
        super( widget, propertyName, nameForDisplay, readonly, PropertyEditorType.BOOLEAN, null, null );
        
        this.value = defaultValue;
    }
    
    public BooleanProperty( Widget widget, String propertyName, String nameForDisplay, boolean defaultValue )
    {
        this( widget, propertyName, nameForDisplay, defaultValue, false );
    }
    
    public BooleanProperty( Widget widget, String propertyName, boolean defaultValue, boolean readonly )
    {
        this( widget, propertyName, propertyName, defaultValue, readonly );
    }
    
    public BooleanProperty( Widget widget, String propertyName, boolean defaultValue )
    {
        this( widget, propertyName, defaultValue, false );
    }
}
