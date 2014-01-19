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
package net.ctdp.rfdynhud.render;

import java.util.HashMap;

import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

public class DrawnStringFactory
{
    private final Widget widget;
    
    private final HashMap<String, DrawnString> map = new HashMap<String, DrawnString>();
    
    void onWidgetCleared()
    {
        for ( DrawnString ds : map.values() )
        {
            ds.resetClearRect();
        }
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        DrawnString ds = new DrawnString( widget, name, xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, prefix, postfix );
        
        if ( name != null )
            map.put( name, ds );
        
        return ( ds );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        return ( newDrawnString( name, xRelativeTo, yRelativeTo, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor, prefix, postfix ) );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnString( name, xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, null, null ) );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnString( name, xRelativeTo, yRelativeTo, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor ) );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        return ( newDrawnString( name, null, null, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, prefix, postfix ) );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        return ( newDrawnString( name, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor, prefix, postfix ) );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnString( name, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, null, null ) );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString}.
     */
    public final DrawnString newDrawnString( String name, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnString( name, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor ) );
    }
    
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        if ( !condition )
        {
            if ( name != null )
                map.remove( name );
            
            return ( null );
        }
        
        DrawnString ds = new DrawnString( widget, name, xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, prefix, postfix );
        
        if ( name != null )
            map.put( name, ds );
        
        return ( ds );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        return ( newDrawnStringIf( condition, name, xRelativeTo, yRelativeTo, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor, prefix, postfix ) );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnStringIf( condition, name, xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, null, null ) );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link DrawnString#getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link DrawnString#getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnStringIf( condition, name, xRelativeTo, yRelativeTo, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor ) );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        return ( newDrawnStringIf( condition, name, null, null, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, prefix, postfix ) );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        return ( newDrawnStringIf( condition, name, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor, prefix, postfix ) );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnStringIf( condition, name, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, null, null ) );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased anti aliased font?
     * @param fontColor the used font color
     * 
     * @return the newly created {@link DrawnString} or <code>null</code>.
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnStringIf( condition, name, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor ) );
    }
    
    public DrawnStringFactory( Widget widget )
    {
        this.widget = widget;
    }
}
