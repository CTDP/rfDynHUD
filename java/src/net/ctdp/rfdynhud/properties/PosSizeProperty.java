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


/**
 * The {@link PosSizeProperty} is utilized by the {@link Position} and {@link Size} classes to customize positional and size values.
 * This class is not meant to be used by a Widget programmer directly.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class PosSizeProperty extends Property
{
    private static final String FLIP_TEXT = "flip";
    private static final String FLIP_TOOLTIP = "Flips the Widget's width from absolute to screen-size-relative.";
    
    private static final String PERC_TEXT = "%";
    private static final String PERC_TOOLTIP = "Converts this property to percentages.";
    
    private static final String PX_TEXT = "px";
    private static final String PX_TOOLTIP = "Converts this property to pixels.";
    
    private final boolean isSizeProp;
    
    /**
     * Returns <code>true</code>, if this is a size property, <code>false</code>, if this is a position property.
     * 
     * @return <code>true</code>, if this is a size property, <code>false</code>, if this is a position property.
     */
    public final boolean isSizeProp()
    {
        return ( isSizeProp );
    }
    
    /**
     * Is percentage value?
     * 
     * @return is percentage value?
     */
    public abstract boolean isPercentage();
    
    public String getButton1Text( boolean isPerc )
    {
        if ( isSizeProp )
            return ( FLIP_TEXT );
        
        if ( isPerc )
            return ( PX_TEXT );
        
        return ( PERC_TEXT );
    }
    
    public String getButton1Tooltip( boolean isPerc )
    {
        if ( isSizeProp )
            return ( FLIP_TOOLTIP );
        
        if ( isPerc )
            return ( PX_TOOLTIP );
        
        return ( PERC_TOOLTIP );
    }
    
    public String getButton2Text( boolean isPerc )
    {
        if ( isPerc )
            return ( PX_TEXT );
        
        return ( PERC_TEXT );
    }
    
    public String getButton2Tooltip( boolean isPerc )
    {
        if ( isPerc )
            return ( PX_TOOLTIP );
        
        return ( PERC_TOOLTIP );
    }
    
    /**
     * 
     * @param button the clicked button
     */
    public void onButton2Clicked( Object button )
    {
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly read only property?
     * @param isSizeProp <code>true</code>, if this is a size property, <code>false</code>, if this is a position property
     */
    public PosSizeProperty( String name, String nameForDisplay, boolean readonly, boolean isSizeProp )
    {
        super( name, nameForDisplay, readonly, PropertyEditorType.POS_SIZE, null, null );
        
        this.isSizeProp = isSizeProp;
    }
    
    /**
     * 
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param isSizeProp <code>true</code>, if this is a size property, <code>false</code>, if this is a position property
     */
    public PosSizeProperty( String name, String nameForDisplay, boolean isSizeProp )
    {
        this( name, nameForDisplay, false, isSizeProp );
    }
}
