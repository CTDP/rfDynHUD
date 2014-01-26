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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jagatoo.util.strings.SimpleStringTokenizer;

/**
 * Holds a distinct set of all known and valid arguments of a command line.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class ArgumentsRegistry
{
    private static final Comparator<Argument> ARGUMENT_COMPARATOR = new Comparator<Argument>()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public int compare( Argument a1, Argument a2 )
        {
            int result = 0;
            
            if ( a1.getLongName() == null )
            {
                if ( a2.getLongName() != null )
                    return ( -1 );
            }
            else if ( a2.getLongName() == null )
            {
                return ( +1 );
            }
            
            result = a1.getLongName().compareTo( a2.getLongName() );
            
            if ( result != 0 )
                return ( result );
            
            if ( a1.getShortName() < a2.getShortName() )
                return ( -1 );
            
            if ( a1.getShortName() > a2.getShortName() )
                return ( +1 );
            
            return ( 0 );
        }
    };
    
    private final String headLine;
    private final String name;
    
    private final Map<Character, Argument> arguments_by_short_name = new HashMap<Character, Argument>();
    private final Map<String, Argument> arguments_by_long_name = new HashMap<String, Argument>();
    private final List<Argument> arguments = new ArrayList<Argument>();
    
    /**
     * Gets this registry's name.
     * 
     * @return the name.
     */
    public final String getName()
    {
        return ( name );
    }
    
    /**
     * Adds a new argument.
     * 
     * @param arg
     */
    public void addArgument( Argument arg )
    {
        if ( arg.getShortName() != '\0' )
        {
            if ( arguments_by_short_name.containsKey( arg.getShortName() ) )
                throw new Error( "duplicate Argument " + arg );
        }
        
        if ( arg.getLongName() != null )
        {
            if ( arguments_by_long_name.containsKey( arg.getLongName() ) )
                throw new Error( "duplicate Argument " + arg );
        }
        
        if ( arg.getShortName() != '\0' )
            arguments_by_short_name.put( arg.getShortName(), arg );
        
        if ( arg.getLongName() != null )
            arguments_by_long_name.put( arg.getLongName(), arg );
        
        arguments.add( arg );
    }
    
    /**
     * Removes an argument.
     * 
     * @param arg
     */
    public void removeArgument( Argument arg )
    {
        if ( arg.getShortName() != '\0' )
            arguments_by_short_name.remove( arg.getShortName() );
        
        if ( arg.getLongName() != null )
            arguments_by_long_name.remove( arg.getLongName() );
        
        arguments.remove( arg );
    }
    
    /**
     * Checks whether an argument exists with the given short-name.
     * 
     * @param shortName
     * 
     * @return true, if it exists.
     */
    public final boolean contains( char shortName )
    {
        return ( arguments_by_short_name.containsKey( shortName ) );
    }
    
    /**
     * Checks whether an argument exists with the given long-name.
     * 
     * @param longName
     * 
     * @return true, if it exists.
     */
    public final boolean contains( String longName )
    {
        return ( arguments_by_long_name.containsKey( longName ) );
    }
    
    /**
     * Gets the argument corresponding to the given short-name.
     * 
     * @param shortName
     * 
     * @return the corresponding argument.
     */
    public final Argument getArgument( char shortName )
    {
        return ( arguments_by_short_name.get( shortName ) );
    }
    
    /**
     * Gets the argument corresponding to the given long-name.
     * 
     * @param longName
     * 
     * @return the corresponding argument.
     */
    public final Argument getArgument( String longName )
    {
        return ( arguments_by_long_name.get( longName ) );
    }
    
    /**
     * Dumps all registered arguments.
     * 
     * @param maxLineWidth
     * @param out
     */
    public void dump( int maxLineWidth, java.io.PrintStream out )
    {
        if ( headLine == null )
            out.println( "Argument list for " + getName() );
        else
            out.println( headLine );
        
        Collections.sort( arguments, ARGUMENT_COMPARATOR );
        
        int indent = 0;
        for ( Argument arg : arguments )
        {
            // -a, --an-argument
            int length = 0;
            if ( arg.getShortName() != '\0' )
            {
                length += 2;
                
                if ( arg.getLongName() != null )
                {
                    length += 4 + arg.getLongName().length();
                }
            }
            else if ( arg.getLongName() != null )
            {
                length += 2 + arg.getLongName().length();
            }
            
            length += 2;
            
            if ( length > indent )
                indent = length;
        }
        
        int descWidth = maxLineWidth - indent;
        
        for ( Argument arg : arguments )
        {
            out.println();
            
            int length = 0;
            if ( arg.getShortName() != '\0' )
            {
                length += 2;
                
                out.print( '-' );
                out.print( arg.getShortName() );
                
                if ( arg.getLongName() != null )
                {
                    length += 4 + arg.getLongName().length();
                    
                    out.print( ", --" );
                    out.print( arg.getLongName() );
                }
            }
            else if ( arg.getLongName() != null )
            {
                length += 2 + arg.getLongName().length();
                
                out.print( "--" );
                out.print( arg.getLongName() );
            }
            length += 2;
            
            for ( int i = indent - length + 1; i >= 0; i-- )
                out.print( ' ' );
            
            String desc = arg.getDecription();
            
            if ( desc != null )
            {
                String[] descParts = desc.split( "\n" );
                int j = 0;
                for ( String part : descParts )
                {
                    if ( j++ > 0 )
                    {
                        for ( int i = 0; i < indent; i++ )
                            out.print( ' ' );
                    }
                    
                    if ( part.length() > 0 )
                    {
                        int offset = 0;
                        int nextOffset = 0;
                        while ( offset < part.length() )
                        {
                            int end = Math.min( offset + descWidth, part.length() );
                            if ( end < part.length() )
                            {
                                if ( !SimpleStringTokenizer.isWhitespace( part.charAt( end ) ) )
                                {
                                    while ( !SimpleStringTokenizer.isWhitespace( part.charAt( end ) ) )
                                    {
                                        end--;
                                    }
                                }
                            }
                            nextOffset = end;
                            
                            if ( offset > 0 )
                            {
                                for ( int i = 0; i < indent; i++ )
                                    out.print( ' ' );
                            }
                            
                            while ( SimpleStringTokenizer.isWhitespace( part.charAt( offset ) ) )
                            {
                                offset++;
                            }
                            
                            while ( SimpleStringTokenizer.isWhitespace( part.charAt( end - 1 ) ) )
                            {
                                end--;
                            }
                            
                            out.println( part.substring( offset, end ) );
                            offset = nextOffset;
                        }
                    }
                    else
                    {
                        out.println();
                    }
                }
            }
            else
            {
                out.println( "(No description available)" );
            }
        }
    }
    
    /**
     * Dumps all registered arguments to stdout.
     * 
     * @param maxLineWidth
     */
    public final void dump( int maxLineWidth )
    {
        dump( maxLineWidth, System.out );
    }
    
    /**
     * Dumps all registered arguments with a maximum line width of 80.
     * 
     * @param out
     */
    public final void dump( java.io.PrintStream out )
    {
        dump( 80, out );
    }
    
    /**
     * Dumps all registered arguments to stdout with a maximum line width of 80.
     */
    public final void dump()
    {
        dump( 80, System.out );
    }
    
    /**
     * Creates a new ArgumentsRegistry.
     * 
     * @param name the name (used in the {@link #dump()} method.
     * @param headLine if not null, the name is ignored in the {@link #dump()} method and this String is dumped first.
     */
    public ArgumentsRegistry( String name, String headLine )
    {
        this.name = name;
        this.headLine = headLine;
    }
    
    /**
     * Creates a new ArgumentsRegistry.
     * 
     * @param name the name (used in the {@link #dump()} method.
     */
    public ArgumentsRegistry( String name )
    {
        this( name, null );
    }
}
