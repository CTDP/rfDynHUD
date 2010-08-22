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
package net.ctdp.rfdynhud.input;

import java.net.URL;

import net.ctdp.rfdynhud.util.StringUtil;

/**
 * This is a simple abstraction of an input action.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class InputAction implements Comparable<InputAction>
{
    public static final int MODIFIER_MASK_SHIFT = 1;
    public static final int MODIFIER_MASK_CTRL  = 2;
    public static final int MODIFIER_MASK_LALT  = 4;
    public static final int MODIFIER_MASK_RALT  = 8;
    public static final int MODIFIER_MASK_LMETA = 16;
    public static final int MODIFIER_MASK_RMETA = 32;
    
    private static int nextId = 1;
    
    private final int id;
    private final String name;
    private final Boolean acceptedState;
    private final boolean isWidgetAction;
    private final InputActionConsumer consumer;
    private String doc = null;
    
    public final int getID()
    {
        return ( id );
    }
    
    public final String getName()
    {
        return ( name );
    }
    
    public final Boolean getAcceptedState()
    {
        return ( acceptedState );
    }
    
    public final boolean isWidgetAction()
    {
        return ( isWidgetAction );
    }
    
    public final InputActionConsumer getConsumer()
    {
        return ( consumer );
    }
    
    private static final String doc_header = StringUtil.loadString( InputAction.class.getClassLoader().getResource( "net/ctdp/rfdynhud/editor/properties/doc_header.html" ) );
    private static final String doc_footer = StringUtil.loadString( InputAction.class.getClassLoader().getResource( "net/ctdp/rfdynhud/editor/properties/doc_footer.html" ) );
    
    private static String getDoc( URL docURL )
    {
        if ( docURL == null )
            return ( "" );
        
        if ( doc_header.equals( "" ) )
            return ( null );
        
        return ( doc_header + StringUtil.loadString( docURL ) + doc_footer );
    }
    
    void setDoc( URL docURL )
    {
        this.doc = getDoc( docURL );
    }
    
    public final String getDoc()
    {
        return ( doc );
    }
    
    @Override
    public int compareTo( InputAction o )
    {
        if ( o == null )
            return ( +1 );
        
        if ( o.id == this.id )
            return ( 0 );
        
        return ( String.CASE_INSENSITIVE_ORDER.compare( this.name, o.name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( o == this )
            return ( true );
        
        if ( !( o instanceof InputAction ) )
            return ( false );
        
        return ( this.id == ( (InputAction)o ).getID() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return ( id );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return ( getName() );
    }
    
    public String toString2()
    {
        return ( getName() + " (ID=" + getID() + ")" );
    }
    
    InputAction( String name, Boolean acceptedState, boolean isWidgetAction, InputActionConsumer consumer, URL docURL )
    {
        this.id = nextId++;
        
        this.name = name;
        this.acceptedState = acceptedState;
        this.isWidgetAction = isWidgetAction;
        this.consumer = consumer;
        this.setDoc( docURL );
    }
    
    /**
     * Creates a new InputAction.
     * The acceptedState defines, whether an input state change event is fired only for 
     * key pressed, key released or both.
     * 
     * @param name the action's name as displayed in the editor
     * @param acceptedState null to accept any state, true or false to accept only this state
     */
    public InputAction( String name, Boolean acceptedState )
    {
        this( name, acceptedState, true, null, null );
    }
    
    /**
     * Creates a new InputAction.
     * It will only accept true state, so it reacts on key pressed.
     * 
     * @param name the action's name as displayed in the editor
     */
    public InputAction( String name )
    {
        this( name, true, true, null, null );
    }
}
