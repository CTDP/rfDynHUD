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
 * @author Marvin Froehlich
 */
public class BoolValue
{
    public static final Boolean DEFAULT_RESET_VALUE = null;
    public static final String N_A_VALUE = "N/A";
    
    private final Boolean resetValue;
    private boolean oldValidity = false;
    private Boolean oldValue;
    private Boolean value;
    
    public final Boolean getResetValue()
    {
        return ( resetValue );
    }
    
    public final Boolean getOldValue()
    {
        return ( oldValue );
    }
    
    public final Boolean getValue()
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
    
    private final boolean update( boolean newValue, boolean setUnchanged )
    {
        this.oldValidity = isValid();
        
        if ( setUnchanged )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( boolean newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final BoolValue reset( boolean resetOldValue )
    {
        this.value = resetValue;
        
        if ( resetOldValue )
            oldValue = resetValue;
        
        this.oldValidity = false;
        
        return ( this );
    }
    
    public final BoolValue reset()
    {
        return ( reset( false ) );
    }
    
    public final boolean isValid()
    {
        return ( value != null );
    }
    
    public final boolean hasValidityChanged()
    {
        return ( isValid() != oldValidity );
    }
    
    public final String getValueAsString()
    {
        if ( value == resetValue )
            return ( N_A_VALUE );
        
        return ( String.valueOf( value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getValueAsString() );
    }
    
    public BoolValue( Boolean resetValue )
    {
        this.resetValue = resetValue;
        
        this.oldValidity = false;
        
        this.oldValue = resetValue;
        this.value = resetValue;
    }
    
    public BoolValue()
    {
        this( DEFAULT_RESET_VALUE );
    }
}
