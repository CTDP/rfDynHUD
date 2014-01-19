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
package net.ctdp.rfdynhud.values;

/**
 * This class is a container for runtime values.
 * You can update the value every time, a Widget is redrawn
 * and compare it with the old (previous) value.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class StringValue
{
    public static final String DEFAULT_RESET_VALUE = null;
    public static final String N_A_VALUE = "N/A";
    
    private final String resetValue;
    private String oldValue;
    private String value;
    
    public final String getResetValue()
    {
        return ( resetValue );
    }
    
    public final String getOldValue()
    {
        return ( oldValue );
    }
    
    public final String getValue()
    {
        return ( value );
    }
    
    public final boolean hasChanged( boolean setUnchanged )
    {
        boolean result = ( value == null ) ? ( oldValue != null ) : !value.equals( oldValue );
        
        if ( result && setUnchanged )
            update( value, true );
        
        return ( result );
    }
    
    public final boolean hasChanged()
    {
        return ( hasChanged( true ) );
    }
    
    /*
    private final boolean update( String newValue, boolean setUnchanged )
    {
        if ( setUnchanged )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    */
    private final boolean update( String newValue, boolean forceSetUnchanged )
    {
        if ( forceSetUnchanged || !hasChanged( false ) )
            this.oldValue = value;
        this.value = newValue;
        
        return ( hasChanged( false ) );
    }
    
    public final boolean update( String newValue )
    {
        return ( update( newValue, false ) );
    }
    
    public final void setUnchanged()
    {
        update( value, true );
    }
    
    public final StringValue reset( boolean resetOldValue )
    {
        this.value = resetValue;
        
        if ( resetOldValue )
            oldValue = resetValue;
        
        return ( this );
    }
    
    public final StringValue reset()
    {
        return ( reset( false ) );
    }
    
    public final boolean isValid()
    {
        return ( ( value != null ) && !value.equals( resetValue ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getValue() );
    }
    
    public StringValue( String resetValue )
    {
        this.resetValue = resetValue;
        
        this.oldValue = resetValue;
        this.value = resetValue;
    }
    
    public StringValue()
    {
        this( DEFAULT_RESET_VALUE );
    }
}
