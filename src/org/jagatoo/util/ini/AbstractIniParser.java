/**
 * Copyright (c) 2007-2009, JAGaToo Project Group all rights reserved.
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
package org.jagatoo.util.ini;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.jagatoo.util.errorhandling.ParsingException;

/**
 * The {@link AbstractIniParser} parses ini files ;).
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public abstract class AbstractIniParser
{
    /**
     * This method is invoked, when a standalone comment line has been found.
     * 
     * @param lineNr
     * @param group
     * @param comment
     * 
     * @return true, to indicate, that parsing should be proceeded, false to stop parsing.
     * 
     * @throws ParsingException
     */
    protected boolean onCommentParsed( int lineNr, String group, String comment ) throws ParsingException
    {
        return ( true );
    }
    
    /**
     * This method is invoked, when a new group has been found.
     * 
     * @param lineNr
     * @param group
     * 
     * @return true, to indicate, that parsing should be proceeded, false to stop parsing.
     * 
     * @throws ParsingException
     */
    protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
    {
        return ( true );
    }
    
    /**
     * This method is invoked, when a new setting has been found.
     * 
     * @param lineNr
     * @param group
     * @param key
     * @param value
     * @param comment the comment behind the value. (can be null)
     * 
     * @return true, to indicate, that parsing should be proceeded, false to stop parsing.
     * 
     * @throws ParsingException
     */
    protected abstract boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException;
    
    /**
     * This method is called when an illigal line was detected.
     * 
     * @param lineNr
     * @param group
     * @param line
     * 
     * @return true, if this line was handled, false otherwise
     * @throws ParsingException
     */
    protected boolean verifyIllegalLine( int lineNr, String group, String line ) throws ParsingException
    {
        return ( false );
    }
    
    /**
     * This method is called when an illigal line was detected.
     * 
     * @param lineNr
     * @param group
     * @param line
     * @return true, to indicate, that parsing should be proceeded, false to stop parsing.
     * @throws ParsingException
     */
    protected boolean handleParsingException( int lineNr, String group, String line, Throwable t ) throws ParsingException
    {
        if ( t instanceof ParsingException )
            throw (ParsingException)t;
        
        if ( t instanceof RuntimeException )
            throw (RuntimeException)t;
        
        if ( t instanceof Error )
            throw (Error)t;
        
        throw new ParsingException( t );
    }
    
    /**
     * This method is invoked when the parsing of the file as been finished.
     */
    protected void onParsingFinished()
    {
    }
    
    /**
     * Parses the given line.<br>
     * This method implements the actual parsing code for a single line.
     * 
     * @param lineNr
     * @param currentGroup
     * @param line
     * 
     * @throws IOException
     * @throws ParsingException
     */
    protected boolean parseLine( int lineNr, String currentGroup, String line ) throws IOException, ParsingException
    {
        if ( line.length() == 0 )
            return ( true );
        
        if ( line.startsWith( "#" ) )
        {
            try
            {
                return ( onCommentParsed( lineNr, currentGroup, line.substring( 1 ).trim() ) );
            }
            catch ( Throwable t )
            {
                return ( handleParsingException( lineNr, currentGroup, line, t ) );
            }
        }
        
        if ( line.startsWith( "/" ) )
        {
            int skipChars = 1;
            if ( line.startsWith( "//" ) )
                skipChars = 2;
            
            try
            {
                return ( onCommentParsed( lineNr, currentGroup, line.substring( skipChars ).trim() ) );
            }
            catch ( Throwable t )
            {
                handleParsingException( lineNr, currentGroup, line, t );
            }
        }
        
        if ( line.startsWith( "[" ) )
            return ( handleParsingException( lineNr, currentGroup, line, new ParsingException( "This method cannot parse groups." ) ) );
        
        int idx = line.indexOf( "=" );
        if ( idx < 0 )
        {
            boolean proceedParse = false;
            
            try
            {
                proceedParse = verifyIllegalLine( lineNr, currentGroup, line );
            }
            catch ( Throwable t )
            {
                return ( handleParsingException( lineNr, currentGroup, line, t ) );
            }
            
            if ( !proceedParse )
                return ( handleParsingException( lineNr, currentGroup, line, new ParsingException( "Illegal line #" + lineNr + ": " + line ) ) );
            
            return ( true );
        }
        
        String key = line.substring( 0, idx ).trim();
        String value = line.substring( idx + 1 ).trim();
        
        boolean proceedParse = true;
        
        if ( value.startsWith( "\"" ) )
        {
            char lastChar = '\0';
            for ( int i = 1; i < value.length(); i++ )
            {
                char ch = value.charAt( i );
                
                if ( ( ch == '"' ) && ( lastChar != '\\' ) )
                {
                    idx = value.indexOf( "//", i + 1 );
                    try
                    {
                        if ( idx >= 0 )
                            proceedParse = onSettingParsed( lineNr, currentGroup, key, value.substring( 1, i ), value.substring( idx + 2, value.length() ).trim() );
                        else
                            proceedParse = onSettingParsed( lineNr, currentGroup, key, value.substring( 1, i ), null );
                    }
                    catch ( Throwable t )
                    {
                        proceedParse = handleParsingException( lineNr, currentGroup, line, t );
                    }
                    
                    lastChar = ch;
                    break;
                }
                
                lastChar = ch;
            }
            
            if ( lastChar != '"' )
                proceedParse = handleParsingException( lineNr, currentGroup, line, new ParsingException( "Illegal line #" + lineNr + ": " + line ) );
        }
        else
        {
            idx = value.indexOf( "//" );
            try
            {
                if ( idx >= 0 )
                    proceedParse = onSettingParsed( lineNr, currentGroup, key, value.substring( 0, idx ).trim(), value.substring( idx + 2, value.length() ).trim() );
                else
                    proceedParse = onSettingParsed( lineNr, currentGroup, key, value, null );
            }
            catch ( Throwable t )
            {
                proceedParse = handleParsingException( lineNr, currentGroup, line, t );
            }
        }
        
        return ( proceedParse );
    }
    
    /**
     * Parses the given file.<br>
     * This method implements the actual parsing code.
     * 
     * @param reader
     * 
     * @throws IOException
     * @throws ParsingException
     */
    protected void parseImpl( BufferedReader reader ) throws IOException, ParsingException
    {
        String currentGroup = null;
        
        String line = null;
        int lineNr = 0;
        boolean proceedParse = true;
        while ( proceedParse && ( ( line = reader.readLine() ) != null ) )
        {
            line = line.trim();
            lineNr++;
            
            if ( line.length() == 0 )
                continue;
            
            if ( line.startsWith( "[" ) )
            {
                if ( line.endsWith( "]" ) )
                {
                    currentGroup = line.substring( 1, line.length() - 1 ).trim();
                    proceedParse = onGroupParsed( lineNr, currentGroup );
                    continue;
                }
                
                int idx = line.indexOf( "]" );
                if ( idx < 0 )
                    throw new ParsingException( "Illegal line #" + lineNr + ": " + line );
                
                currentGroup = line.substring( 1, idx ).trim();
                proceedParse = onGroupParsed( lineNr, currentGroup );
                continue;
            }
            
            proceedParse = parseLine( lineNr, currentGroup, line );
        }
        
        reader.close();
        
        onParsingFinished();
    }
    
    /**
     * Parses the given file.
     * 
     * @param reader
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public final void parse( Reader reader ) throws IOException, ParsingException
    {
        if ( reader instanceof BufferedReader )
            parseImpl( (BufferedReader)reader );
        else
            parseImpl( new BufferedReader ( reader ) );
    }
    
    /**
     * Parses the given file.
     * 
     * @param in
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public final void parse( InputStream in ) throws IOException, ParsingException
    {
        parseImpl( new BufferedReader( new InputStreamReader( in ) ) );
    }
    
    /**
     * Parses the given file.
     * 
     * @param url
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public final void parse( URL url ) throws IOException, ParsingException
    {
        parse( url.openStream() );
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public final void parse( File file ) throws IOException, ParsingException
    {
        parse( file.toURI().toURL() );
    }
    
    /**
     * Parses the given file.
     * 
     * @param filename
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public final void parse( String filename ) throws IOException, ParsingException
    {
        parse( new File( filename ) );
    }
    
    /**
     * Creates a new {@link AbstractIniParser}.
     */
    public AbstractIniParser()
    {
    }
}
