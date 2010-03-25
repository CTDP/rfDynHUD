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
    public static final String DEFAULT_OPERATOR = "=";
    
    /**
     * This method is invoked, when an empty line has been found.
     * 
     * @param lineNr
     * @param group
     * 
     * @return true, to indicate, that parsing should be proceeded, false to stop parsing.
     * 
     * @throws ParsingException
     */
    protected boolean onEmptyLineParsed( int lineNr, String group ) throws ParsingException
    {
        return ( true );
    }
    
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
     * Override this method and return true to accept a missing trailing (double-)quote as a correct line.
     * 
     * @return true to accept lines like that.
     */
    protected boolean acceptMissingTrailingQuote()
    {
        return ( false );
    }
    
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
     * Parses the given line (must be trimmed). If it is a group header, the group name is returned, null otherwise.
     * 
     * @param lineNr
     * @param line
     * 
     * @return the group name, if the given line is a group name, null otherwise.
     * 
     * @throws ParsingException
     */
    public static String tryToParseGroup( int lineNr, String line ) throws ParsingException
    {
        if ( line.startsWith( "[" ) )
        {
            int idx = -1;
            if ( line.endsWith( "]" ) )
            {
                return ( line.substring( 1, line.length() - 1 ).trim() );
            }
            else if ( ( idx = line.indexOf( ']', 1 ) ) >= 0 )
            {
                return ( line.substring( 1, idx ).trim() );
            }
            
            if ( lineNr <= 0 )
                throw new ParsingException( "Illegal line: " + line );
            
            throw new ParsingException( "Illegal line #" + lineNr + ": " + line );
        }
        
        return ( null );
    }
    
    /**
     * Parses the given line (must be trimmed). If it is a group header, the group name is returned, null otherwise.
     * 
     * @return the group name, if the given line is a group name, null otherwise.
     * 
     * @throws ParsingException
     */
    public static String tryToParseGroup( String line ) throws ParsingException
    {
        return ( tryToParseGroup( -1, line ) );
    }
    
    /**
     * Parses the given line.<br>
     * This method implements the actual parsing code for a single line.
     * 
     * @param lineNr
     * @param currentGroup
     * @param line
     * @param operator
     * @param iniLine
     * @param handler
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public static boolean parseLine( int lineNr, String currentGroup, String line, String operator, IniLine iniLine, AbstractIniParser handler ) throws IOException, ParsingException
    {
        if ( iniLine != null )
        {
            iniLine.reset();
            iniLine.setLine( lineNr, currentGroup, line );
            line = iniLine.getLine();
        }
        else
        {
            line = line.trim();
        }
        
        if ( ( ( iniLine != null ) && ( iniLine.isEmpty() ) ) || ( line.length() == 0 ) )
        {
            if ( handler == null )
                return ( true );
            
            return ( handler.onEmptyLineParsed( iniLine.getLineNr(), iniLine.getCurrentGroup() ) );
        }
        
        if ( line.startsWith( "#" ) )
        {
            try
            {
                String comment = line.substring( 1 ).trim();
                if ( iniLine != null )
                    iniLine.setComment( comment );
                
                if ( handler == null )
                    return ( true );
                
                return ( handler.onCommentParsed( lineNr, currentGroup, comment ) );
            }
            catch ( Throwable t )
            {
                if ( iniLine != null )
                    iniLine.setComment( null );
                
                if ( handler == null )
                    return ( false );
                
                return ( handler.handleParsingException( lineNr, currentGroup, line, t ) );
            }
        }
        
        if ( line.startsWith( "/" ) )
        {
            int skipChars = 1;
            if ( line.startsWith( "//" ) )
                skipChars = 2;
            
            try
            {
                String comment = line.substring( skipChars ).trim();
                if ( iniLine != null )
                    iniLine.setComment( comment );
                
                if ( handler == null )
                    return ( true );
                
                return ( handler.onCommentParsed( lineNr, currentGroup, comment ) );
            }
            catch ( Throwable t )
            {
                if ( iniLine != null )
                    iniLine.setComment( null );
                
                if ( handler != null )
                    handler.handleParsingException( lineNr, currentGroup, line, t );
                else if ( t instanceof ParsingException )
                    throw (ParsingException)t;
                else if ( t instanceof RuntimeException )
                    throw (RuntimeException)t;
                else if ( t instanceof Error )
                    throw (Error)t;
                else
                    throw new ParsingException( t );
            }
        }
        
        if ( line.startsWith( "[" ) )
        {
            if ( handler == null )
                throw new ParsingException( "This method cannot parse groups." );
            
            return ( handler.handleParsingException( lineNr, currentGroup, line, new ParsingException( "This method cannot parse groups." ) ) );
        }
        
        int idx = line.indexOf( operator );
        if ( idx < 0 )
        {
            boolean proceedParse = false;
            
            try
            {
                if ( iniLine != null )
                    iniLine.setValid( false );
                
                if ( handler == null )
                    proceedParse = false;
                else
                    proceedParse = handler.verifyIllegalLine( lineNr, currentGroup, line );
            }
            catch ( Throwable t )
            {
                boolean result;
                if ( handler == null )
                    result = false;
                else
                    result = handler.handleParsingException( lineNr, currentGroup, line, t );
                
                if ( iniLine != null )
                    iniLine.setValid( result );
                
                return ( result );
            }
            
            if ( !proceedParse )
            {
                if ( handler == null )
                    return ( false );
                
                return ( handler.handleParsingException( lineNr, currentGroup, line, new ParsingException( "Illegal line #" + lineNr + ": " + line ) ) );
            }
            
            return ( true );
        }
        
        String key = line.substring( 0, idx ).trim();
        if ( iniLine != null )
            iniLine.setKey( key );
        String value = line.substring( idx + operator.length() ).trim();
        
        boolean proceedParse = true;
        
        if ( ( value.length() > 0 ) && ( value.charAt( 0 ) == '"' ) )
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
                        String value2 = value.substring( 1, i );
                        if ( iniLine != null )
                            iniLine.setValue( value2 );
                        
                        if ( idx >= 0 )
                        {
                            String comment = value.substring( idx + 2, value.length() ).trim();
                            if ( iniLine != null )
                                iniLine.setComment( comment );
                            
                            if ( handler == null )
                                proceedParse = true;
                            else
                                proceedParse = handler.onSettingParsed( lineNr, currentGroup, key, value2, comment );
                        }
                        else
                        {
                            if ( handler == null )
                                proceedParse = true;
                            else
                                proceedParse = handler.onSettingParsed( lineNr, currentGroup, key, value2, null );
                        }
                        
                        if ( iniLine != null )
                            iniLine.setValid( true );
                    }
                    catch ( Throwable t )
                    {
                        if ( iniLine != null )
                            iniLine.setValid( false );
                        
                        if ( handler == null )
                            proceedParse = false;
                        else
                            proceedParse = handler.handleParsingException( lineNr, currentGroup, line, t );
                    }
                    
                    lastChar = ch;
                    break;
                }
                
                lastChar = ch;
            }
            
            if ( lastChar != '"' )
            {
                if ( ( handler != null ) && handler.acceptMissingTrailingQuote() )
                {
                    try
                    {
                        String value2 = value.substring( 1 );
                        
                        if ( iniLine != null )
                        {
                            iniLine.setValid( true );
                            iniLine.setValue( value2 );
                        }
                        
                        proceedParse = handler.onSettingParsed( lineNr, currentGroup, key, value2, null );
                    }
                    catch ( Throwable t )
                    {
                        if ( iniLine != null )
                            iniLine.setValid( false );
                        proceedParse = handler.handleParsingException( lineNr, currentGroup, line, t );
                    }
                }
                else
                {
                    Boolean b = null;
                    try
                    {
                        if ( iniLine != null )
                            iniLine.setValid( false );
                        if ( handler == null )
                            b = false;
                        else
                            b = handler.verifyIllegalLine( lineNr, currentGroup, line );
                    }
                    catch ( Throwable t )
                    {
                        if ( iniLine != null )
                            iniLine.setValid( false );
                        if ( handler == null )
                            proceedParse = false;
                        else
                            proceedParse = handler.handleParsingException( lineNr, currentGroup, line, t );
                    }
                    
                    if ( b != null )
                    {
                        if ( iniLine != null )
                            iniLine.setValid( b );
                        
                        proceedParse = b.booleanValue();
                    }
                    else
                    {
                        if ( handler == null )
                            proceedParse = false;
                        else
                            proceedParse = handler.handleParsingException( lineNr, currentGroup, line, new ParsingException( "Illegal line #" + lineNr + ": " + line ) );
                    }
                }
            }
        }
        else
        {
            idx = value.indexOf( "//" );
            try
            {
                if ( iniLine != null )
                    iniLine.setValid( true );
                
                if ( idx >= 0 )
                {
                    String value2 = value.substring( 0, idx ).trim();
                    String comment = value.substring( idx + 2, value.length() ).trim();
                    if ( iniLine != null )
                    {
                        iniLine.setValue( value2 );
                        iniLine.setComment( comment );
                    }
                    
                    if ( handler == null )
                        proceedParse = true;
                    else
                        proceedParse = handler.onSettingParsed( lineNr, currentGroup, key, value2, comment );
                }
                else
                {
                    if ( iniLine != null )
                        iniLine.setValue( value );
                    
                    if ( handler == null )
                        proceedParse = true;
                    else
                        proceedParse = handler.onSettingParsed( lineNr, currentGroup, key, value, null );
                }
            }
            catch ( Throwable t )
            {
                if ( iniLine != null )
                    iniLine.setValid( false );
                if ( handler == null )
                    proceedParse = false;
                else
                    proceedParse = handler.handleParsingException( lineNr, currentGroup, line, t );
            }
        }
        
        return ( proceedParse );
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
        return ( parseLine( lineNr, currentGroup, line, DEFAULT_OPERATOR, null, this ) );
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
        IniLine iniLine = new IniLine();
        
        String line2 = null;
        int lineNr = 0;
        boolean proceedParse = true;
        while ( proceedParse && ( ( line2 = reader.readLine() ) != null ) )
        {
            iniLine.setLine( ++lineNr, iniLine.getCurrentGroup(), line2 );
            
            if ( iniLine.isEmpty() )
            {
                proceedParse = onEmptyLineParsed( lineNr, iniLine.getCurrentGroup() );
                continue;
            }
            
            String groupName = tryToParseGroup( lineNr, iniLine.getLine() );
            if ( groupName == null )
            {
                proceedParse = parseLine( lineNr, iniLine.getCurrentGroup(), iniLine.getLine(), DEFAULT_OPERATOR, iniLine, this );
            }
            else
            {
                iniLine.setCurrentGroup( groupName );
                proceedParse = onGroupParsed( iniLine.getLineNr(), iniLine.getCurrentGroup() );
            }
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
