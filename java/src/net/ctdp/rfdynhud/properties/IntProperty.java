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
 * The {@link IntProperty} serves for customizing a primitive int value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class IntProperty extends Property
{
    private int value;
    
    private final int minValue;
    private final int maxValue;
    
    public final int getMinValue()
    {
        return ( minValue );
    }
    
    public final int getMaxValue()
    {
        return ( maxValue );
    }
    
    protected int fixValue( int value )
    {
        return ( Math.max( minValue, Math.min( value, maxValue ) ) );
    }
    
    /**
     * 
     * @param oldValue
     * @param newValue
     */
    protected void onValueChanged( int oldValue, int newValue )
    {
    }
    
    public void setIntValue( int value )
    {
        value = fixValue( value );
        
        if ( value == this.value )
            return;
        
        int oldValue = this.value;
        this.value = value;
        
        if ( widget != null )
            widget.forceAndSetDirty();
        
        onValueChanged();
        onValueChanged( oldValue, value );
        
        if ( widget != null )
            __WPrivilegedAccess.onPropertyChanged( this, oldValue, value, widget );
    }
    
    public final int getIntValue()
    {
        return ( value );
    }
    
    public final float getFloatValue()
    {
        return ( value );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue( Object value )
    {
        setIntValue( ( (Number)value ).intValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getValue()
    {
        return ( getIntValue() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( String value )
    {
        setValue( Integer.parseInt( value ) );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public IntProperty( Widget widget, String name, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        super( widget, name, nameForDisplay, readonly, PropertyEditorType.INTEGER, null, null );
        
        this.value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    public IntProperty( Widget widget, String name, String nameForDisplay, int defaultValue, boolean readonly )
    {
        this( widget, name, nameForDisplay, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public IntProperty( Widget widget, String name, String nameForDisplay, int defaultValue )
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
    public IntProperty( Widget widget, String name, int defaultValue, boolean readonly )
    {
        this( widget, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public IntProperty( Widget widget, String name, int defaultValue )
    {
        this( widget, name, defaultValue, false );
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public IntProperty( Widget widget, String name, int defaultValue, int minValue, int maxValue )
    {
        this( widget, name, null, defaultValue, minValue, maxValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param minValue
     * @param maxValue
     * @param readonly
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int defaultValue, int minValue, int maxValue, boolean readonly )
    {
        this( (Widget)null, name, nameForDisplay, defaultValue, minValue, maxValue, readonly );
        
        w2pf.addProperty( this );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     * @param readonly
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int defaultValue, boolean readonly )
    {
        this( w2pf, name, nameForDisplay, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, String nameForDisplay, int defaultValue )
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
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, int defaultValue, boolean readonly )
    {
        this( w2pf, name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, int defaultValue )
    {
        this( w2pf, name, defaultValue, false );
    }
    
    /**
     * 
     * @param w2pf
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue
     * @param minValue
     * @param maxValue
     */
    public IntProperty( WidgetToPropertyForwarder w2pf, String name, int defaultValue, int minValue, int maxValue )
    {
        this( w2pf, name, null, defaultValue, minValue, maxValue, false );
    }
}
