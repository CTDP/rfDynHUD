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
package net.ctdp.rfdynhud.values;

/**
 * This class is a container for runtime values.
 * You can update the value every time, a Widget is redrawn
 * and compare it with the old (previous) value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EnumValue<E extends Enum<E>>
{
    public static final String N_A_VALUE = "N/A";
    
    private final E resetValue;
    private E oldValue;
    private E value;
    
    public final E getResetValue()
    {
        return ( resetValue );
    }
    
    public final E getOldValue()
    {
        return ( oldValue );
    }
    
    public final E getValue()
    {
        return ( value );
    }
    
    public final boolean hasChanged( boolean setUnchanged )
    {
        boolean result = ( value != oldValue );
        
        if ( result && setUnchanged )
            update( value, true );
        
        return ( result );
    }
    
    public final boolean hasChanged()
    {
        return ( hasChanged( true ) );
    }
    
    private final boolean update( E newValue, boolean setUnchanged )
    {
        if ( setUnchanged )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( E newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final EnumValue<E> reset( boolean resetOldValue )
    {
        this.value = resetValue;
        
        if ( resetOldValue )
            oldValue = resetValue;
        
        return ( this );
    }
    
    public final EnumValue<E> reset()
    {
        return ( reset( false ) );
    }
    
    public final boolean isValid()
    {
        return ( value != null );
    }
    
    public final String getValueAsString()
    {
        return ( value.name() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( value.toString() );
    }
    
    public EnumValue( E resetValue )
    {
        this.resetValue = resetValue;
        
        this.oldValue = resetValue;
        this.value = resetValue;
    }
    
    public EnumValue()
    {
        this( null );
    }
}
