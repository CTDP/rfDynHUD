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

public abstract class PosSizeProperty extends Property
{
    private static final String FLIP_TEXT = "flip";
    private static final String FLIP_TOOLTIP = "Flips the Widget's width from absolute to screen-size-relative.";
    
    private static final String PERC_TEXT = "%";
    private static final String PERC_TOOLTIP = "Converts this property to percentages.";
    
    private static final String PX_TEXT = "px";
    private static final String PX_TOOLTIP = "Converts this property to pixels.";
    
    private final boolean isSizeProp;
    
    public final boolean isSizeProp()
    {
        return ( isSizeProp );
    }
    
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
     * @param button
     */
    public void onButton2Clicked( Object button )
    {
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param readonly
     * @param isSizeProp
     */
    public PosSizeProperty( Widget widget, String name, String nameFordisplay, boolean readonly, boolean isSizeProp )
    {
        super( widget, name, nameFordisplay, readonly, PropertyEditorType.POS_SIZE, null, null );
        
        this.isSizeProp = isSizeProp;
    }
    
    /**
     * 
     * @param widget
     * @param name the technical name used internally. See {@link #getName()}.
     * @param nameForDisplay the name displayed in the editor. See {@link #getNameForDisplay()}. If <code>null</code> is passed, the value of the name parameter is used.
     * @param isSizeProp
     */
    public PosSizeProperty( Widget widget, String name, String nameFordisplay, boolean isSizeProp )
    {
        this( widget, name, nameFordisplay, false, isSizeProp );
    }
}
