/**
 * Copyright (c) 2007-2011, JAGaToo Project Group all rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of the 'Xith3D Project Group' nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
 * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE
 */
package org.jagatoo.commandline;

/**
 * A single argument of a command line.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class Argument
{
    private final char shortName;
    private final String longName;
    
    private final String description;
    
    private final boolean needsValue;
    
    /**
     * Gets the short name (one char) of this argument.
     * 
     * @return the short name.
     */
    public final char getShortName()
    {
        return ( shortName );
    }
    
    /**
     * Gets the long name of this argument.
     * 
     * @return the long name.
     */
    public final String getLongName()
    {
        return ( longName );
    }
    
    /**
     * Gets this argument's description.
     * 
     * @return the description.
     */
    public final String getDecription()
    {
        return ( description );
    }
    
    /**
     * Returns whether this argument needs a value or if it is a switch.
     * 
     * @return true, if the argument needs a value, false otherwise.
     */
    public final boolean needsValue()
    {
        return ( needsValue );
    }
    
    /**
     * This method must be overridden for arguments, that take non-String values.
     * 
     * @param rawValue the raw String value
     * 
     * @return the parsed value (by default the input String is passed back).
     * 
     * @throws CommandlineParsingException
     */
    protected Object parseValueImpl( String rawValue ) throws CommandlineParsingException
    {
        return ( rawValue );
    }
    
    /**
     * Parses the concrete value from the input String.
     * 
     * @param rawValue the raw String value
     * 
     * @return the parsed value.
     * 
     * @throws CommandlineParsingException
     */
    public final Object parseValue( String rawValue ) throws CommandlineParsingException
    {
        try
        {
            return ( parseValueImpl( rawValue ) );
        }
        catch ( CommandlineParsingException e )
        {
            throw e;
        }
        catch ( Throwable t )
        {
            throw new CommandlineParsingException( "Invalid value for " + this + ": " + rawValue, t );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !( o instanceof Argument ) )
            return ( false );
        
        Argument a = (Argument)o;
        
        if ( a.shortName == '\0' )
        {
            if ( this.shortName != '\0' )
                return ( false );
            
            return ( a.longName.equals( this.longName ) );
        }
        
        if ( this.shortName == '\0' )
            return ( false );
        
        return ( a.shortName == this.shortName );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if ( shortName != '\0' )
            return ( shortName );
        
        return ( longName.hashCode() );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        if ( shortName == '\0' )
            return ( longName );
        
        if ( longName == null )
            return ( String.valueOf( shortName ) );
        
        return ( longName + " (" + shortName + ")" );
    }
    
    /**
     * Creates a new argument.
     * 
     * @param shortName the short name. Use '\0' for no short name.
     * @param longName the long name. Use null for no long name.
     * @param description the description (can be null).
     * @param needsValue
     */
    public Argument( char shortName, String longName, String description, boolean needsValue )
    {
        if ( "".equals( longName ) )
            longName = null;
        
        if ( "".equals( description ) )
            description = null;
        
        if ( ( shortName == '\0' ) && ( longName == null ) )
            throw new IllegalArgumentException( "shortName and longName cannot be both null." );
        
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
        this.needsValue = needsValue;
    }
}
