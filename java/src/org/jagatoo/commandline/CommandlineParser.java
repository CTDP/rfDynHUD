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

import org.jagatoo.util.strings.SimpleStringTokenizer;

/**
 * <p>
 * Parses standard command lines.
 * </p>
 * <p>
 * These commandlines ca have the form:
 * -a -xf 0 --long-option "option value" -long-option2 value2
 * </p>
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class CommandlineParser
{
    private final ArgumentsRegistry argReg;
    private final ArgumentsHandler handler;
    
    private void onError( int chunk, String message ) throws CommandlineParsingException
    {
        handler.onError( chunk, message );
    }
    
    private Argument onArgument( int chunk, String argName ) throws CommandlineParsingException
    {
        Argument arg = argReg.getArgument( argName );
        
        if ( arg == null )
            throw new CommandlineParsingException( chunk, "There is no argument \"" + argName + "\"." );
        
        if ( arg.needsValue() )
        {
            return ( arg );
        }
        
        onArgumentComplete( arg, null );
        
        return ( null );
    }
    
    private void onArgumentComplete( Argument arg, String rawValue ) throws CommandlineParsingException
    {
        if ( rawValue == null )
            handler._handleArgument( arg, null );
        else if ( arg == null )
            handler._handleArgument( null, rawValue );
        else
            handler._handleArgument( arg, arg.parseValue( rawValue ) );
    }
    
    private Argument parseSingleCharArguments( int chunkNum, String chunk ) throws CommandlineParsingException
    {
        Argument lastArg = null;
        
        for ( int i = 1; i < chunk.length(); i++ )
        {
            char ch = chunk.charAt( i );
            
            lastArg = argReg.getArgument( ch );
            
            if ( lastArg == null )
                throw new CommandlineParsingException( chunkNum, "There is no argument '" + ch + "'." );
            
            if ( !lastArg.needsValue() )
            {
                onArgumentComplete( lastArg, null );
                lastArg = null;
            }
            else if ( i < chunk.length() - 1 )
            {
                onError( chunkNum, "No value provided for argument " + lastArg );
            }
        }
        
        return ( lastArg );
    }
    
    /**
     * Parses a command line from an array of chunks.
     * 
     * @param chunks the chunks as passed to a main() method (a String split by white spaces, quoted Strings kept together omitting quotes)
     * 
     * @throws CommandlineParsingException
     */
    public void parseCommandline( String[] chunks ) throws CommandlineParsingException
    {
        int minusCount = 0;
        char lastChar = '\0';
        int firstNameChar = 0;
        Argument lastValueArg = null;
        
        for ( int j = 0; j < chunks.length; j++ )
        {
            String chunk = chunks[j];
            
            if ( lastValueArg != null )
            {
                onArgumentComplete( lastValueArg, chunk );
                lastValueArg = null;
                
                continue;
            }
            
            minusCount = 0;
            lastChar = '\0';
            firstNameChar = 0;
            
            for ( int i = 0; i < chunk.length(); i++ )
            {
                char ch = chunk.charAt( i );
                
                if ( ch == '-' )
                {
                    if ( ( lastChar == '\0' ) || ( lastChar == '-' ) )
                    {
                        minusCount++;
                        firstNameChar++;
                    }
                }
                else if ( minusCount == 0 )
                {
                    // loose value detected
                    
                    onArgumentComplete( null, chunk );
                    break;
                }
                else if ( minusCount == 1 )
                {
                    lastValueArg = parseSingleCharArguments( j, chunk );
                    break;
                }
                else if ( minusCount == 2 )
                {
                    lastValueArg = onArgument( j, chunk.substring( 2 ) );
                    break;
                }
                else
                {
                    onError( j, "invalid chunk " + j );
                }
                
                lastChar = ch;
            }
            
            if ( firstNameChar == chunk.length() )
            {
                onError( j, "invalid chunk " + j );
            }
        }
        
        if ( lastValueArg != null )
        {
            onError( chunks.length, "No value provided for argument " + lastValueArg );
        }
        
        handler._validate();
    }
    
    /**
     * Parses a command line from a single String.
     * 
     * @param commandline the complete commandline as one String
     * 
     * @throws CommandlineParsingException
     */
    public void parseCommandline( String commandline ) throws CommandlineParsingException
    {
        ArrayList<String> argsList = new ArrayList<String>();
        
        SimpleStringTokenizer tokenizer = new SimpleStringTokenizer( commandline );
        tokenizer.useQuotes( true );
        
        while ( tokenizer.hasMoreTokens() )
        {
            argsList.add( tokenizer.nextToken() );
        }
        
        parseCommandline( argsList.toArray( new String[ argsList.size() ] ) );
    }
    
    /**
     * Creates a new {@link CommandlineParser}.
     * 
     * @param argReg
     * @param handler
     */
    public CommandlineParser( ArgumentsRegistry argReg, ArgumentsHandler handler )
    {
        if ( argReg == null )
            throw new IllegalArgumentException( "argReg must not be null." );
        
        if ( handler == null )
            throw new IllegalArgumentException( "handler must not be null." );
        
        this.argReg = argReg;
        this.handler = handler;
    }
}
