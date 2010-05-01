package net.ctdp.rfdynhud.render;

import java.util.HashMap;

import net.ctdp.rfdynhud.render.DrawnString.Alignment;

public class DrawnStringFactory
{
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
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     */
    public final DrawnString newDrawnString( String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        DrawnString ds = new DrawnString( xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, prefix, postfix );
        
        if ( name != null )
            map.put( name, ds );
        
        return ( ds );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     */
    public final DrawnString newDrawnString( String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        return ( newDrawnString( name, xRelativeTo, yRelativeTo, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor, prefix, postfix ) );
    }
    
    /**
     * Creates a new {@link DrawnString}.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     */
    public final DrawnString newDrawnString( String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnString( name, xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, null, null ) );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
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
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
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
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
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
     * @param fontAntiAliased
     * @param fontColor the used font color
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
     * @param fontAntiAliased
     * @param fontColor the used font color
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
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, DrawnString xRelativeTo, DrawnString yRelativeTo, int x, int y, Alignment alignment, boolean y_at_baseline, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor, String prefix, String postfix )
    {
        if ( !condition )
        {
            if ( name != null )
                map.remove( name );
            
            return ( null );
        }
        
        DrawnString ds = new DrawnString( xRelativeTo, yRelativeTo, x, y, alignment, y_at_baseline, font, fontAntiAliased, fontColor, prefix, postfix );
        
        if ( name != null )
            map.put( name, ds );
        
        return ( ds );
    }
    
    /**
     * Creates a new {@link DrawnString} with {@link Alignment#LEFT} and y_at_baseline = true.
     * 
     * @param condition if this is <code>false</code>, null is returned.
     * @param name (widget-local) unique name used by this factory to overwrite in the cache (can be anything)
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
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
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param alignment the alignment
     * @param y_at_baseline if true, the String's baseline will be placed to the getAbsY() location. Otherwise the String's upper bound will be at that y-location.
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
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
     * @param xRelativeTo if this is non-null, the {@link #getAbsX()} is computed by ( xRelativeTo.getAbsX() + xRelativeTo.maxWidth + this.getX() ), otherwise getAbsX() returns the plain getX() value.
     * @param yRelativeTo if this is non-null, the {@link #getAbsY()} is computed by ( xRelativeTo.getAbsY() + xRelativeTo.maxHeight + this.getY() ), otherwise getAbsY() returns the plain getY() value.
     * @param x the x-location
     * @param y the y-location
     * @param font the used font
     * @param fontAntiAliased
     * @param fontColor the used font color
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
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
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
     * @param fontAntiAliased
     * @param fontColor the used font color
     * @param prefix a String, that is always drawn seamlessly to the left of the major string, that is passed to the draw() method (or null for no prefix).
     * @param postfix a String, that is always drawn seamlessly to the right of the major string, that is passed to the draw() method (or null for no postfix).
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
     * @param fontAntiAliased
     * @param fontColor the used font color
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
     * @param fontAntiAliased
     * @param fontColor the used font color
     */
    public final DrawnString newDrawnStringIf( boolean condition, String name, int x, int y, java.awt.Font font, boolean fontAntiAliased, java.awt.Color fontColor )
    {
        return ( newDrawnStringIf( condition, name, x, y, Alignment.LEFT, true, font, fontAntiAliased, fontColor ) );
    }
}
