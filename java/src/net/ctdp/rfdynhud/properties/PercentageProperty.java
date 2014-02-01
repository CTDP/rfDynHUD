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


/**
 * The {@link PercentageProperty} serves for customizing a primitive float value between 0% and 100%.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class PercentageProperty extends FloatProperty
{
    public static float parseValue( String value )
    {
        if ( value.endsWith( "%" ) )
        {
            float f = Float.parseFloat( value.substring( 0, value.length() - 1 ) );
            
            return ( f / 100f );
        }
        
        float f = Float.parseFloat( value );
        
        return ( f / 100f );
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadValue( PropertyLoader loader, String value )
    {
        setValue( parseValue( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueForConfigurationFile()
    {
        return ( String.valueOf( getFloatValue() * 100f ) + "%" );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     * @param readonly read only property?
     */
    public PercentageProperty( String name, String nameForDisplay, float defaultValue, float minValue, float maxValue, boolean readonly )
    {
        super( name, nameForDisplay, defaultValue, minValue, maxValue, readonly, PropertyEditorType.PERCENTAGE, true );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public PercentageProperty( String name, String nameForDisplay, float defaultValue, boolean readonly )
    {
        this( name, nameForDisplay, defaultValue, 0.0f, 100.0f, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param defaultValue the default value
     */
    public PercentageProperty( String name, String nameForDisplay, float defaultValue )
    {
        this( name, nameForDisplay, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param readonly read only property?
     */
    public PercentageProperty( String name, float defaultValue, boolean readonly )
    {
        this( name, null, defaultValue, readonly );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     */
    public PercentageProperty( String name, float defaultValue )
    {
        this( name, defaultValue, false );
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}. 'nameForDisplay' is set to the same value.
     * @param defaultValue the default value
     * @param minValue the minimum value
     * @param maxValue the maximum value
     */
    public PercentageProperty( String name, float defaultValue, float minValue, float maxValue )
    {
        this( name, null, defaultValue, minValue, maxValue, false );
    }
}
