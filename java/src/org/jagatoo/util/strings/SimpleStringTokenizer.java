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
package org.jagatoo.util.strings;

/**
 * The {@link SimpleStringTokenizer} can be used, where speed matters.
 * It is reusable for new Strings and only searches for whitespaces.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class SimpleStringTokenizer
{
    private String string;
    
    private int pos = -1;
    
    private Boolean hasMore = null;
    
    private String lastToken = null;
    
    private boolean useQuotes = false;
    
    private final StringBuilder sb = new StringBuilder();
    
    public void useQuotes( boolean keep )
    {
        this.useQuotes = keep;
    }
    
    public final boolean useQuotes()
    {
        return ( useQuotes );
    }
    
    public void reset()
    {
        this.pos = -1;
        this.hasMore = null;
        this.lastToken = null;
    }
    
    public void setString( String string, int skipChars )
    {
        if ( string == null )
        {
            throw new IllegalArgumentException( "string must not be null" );
        }
        
        this.string = string;
        reset();
        this.pos = skipChars - 1;
    }
    
    public void setString( String string )
    {
        setString( string, 0 );
    }
    
    public static final boolean isWhitespace( char ch )
    {
        //return ( Character.isWhitespace( ch ) );
        return ( ( ch == ' ' ) || ( ch == '\n' ) || ( ch == (char)13 ) || ( ch == '\t' ) || ( ch == '\f' ) );
    }
    
    private final boolean searchForNextToken()
    {
        while ( ++pos < string.length() )
        {
            char ch = string.charAt( pos );
            
            if ( !isWhitespace( ch ) )
            {
                return ( true );
            }
        }
        
        return ( false );
    }
    
    public final boolean hasMoreTokens()
    {
        if ( pos >= string.length() )
            return ( false );
        
        if ( hasMore == null )
        {
            hasMore = Boolean.valueOf( searchForNextToken() );
        }
        
        return ( hasMore.booleanValue() );
    }
    
    public final String nextToken( boolean discard )
    {
        if ( !hasMoreTokens() )
        {
            return ( null );
        }
        
        boolean quoteBegan = false;
        
        if ( !discard )
        {
            sb.setLength( 0 );
            char ch = string.charAt( pos );
            if ( ( ch != '\"' ) || !useQuotes )
                sb.append( ch );
            else
                quoteBegan = true;
        }
        
        while ( ++pos < string.length() )
        {
            char ch = string.charAt( pos );
            
            if ( ( ch == '\"' ) && useQuotes )
            {
                quoteBegan = !quoteBegan;
                
                continue;
            }
            
            if ( isWhitespace( ch ) && !quoteBegan )
            {
                hasMore = null;
                
                if ( discard )
                    return ( null );
                
                return ( sb.toString() );
            }
            
            if ( !discard )
                sb.append( ch );
        }
        
        hasMore = null;
        
        if ( discard || ( sb.length() == 0 ) )
            return ( null );
        
        return ( sb.toString() );
    }
    
    public final String nextToken()
    {
        lastToken = nextToken( false );
        
        return ( lastToken );
    }
    
    public final SimpleStringTokenizer skipToken()
    {
        nextToken( true );
        
        return ( this );
    }
    
    public final String getLastToken()
    {
        return ( lastToken );
    }
    
    private final String getRest( boolean acceptQuotes )
    {
        sb.setLength( 0 );
        
        int numWhitespaces = 0;
        for ( int i = pos; i < string.length(); i++ )
        {
            char ch = string.charAt( i );
            boolean ws = isWhitespace( ch );
            
            if ( ws )
            {
                numWhitespaces++;
            }
            else
            {
                numWhitespaces = 0;
            }
            
            if ( acceptQuotes || ( ch != '\"' ) )
            {
                if ( !ws || ( sb.length() > 0 ) )
                {
                    sb.append( ch );
                }
            }
        }
        
        if ( numWhitespaces > 0 )
        {
            sb.delete( sb.length() - numWhitespaces, sb.length() - 1 );
        }
        
        return ( sb.toString() );
    }
    
    public final String getRest()
    {
        return ( getRest( true ) );
    }
    
    public final String getUnquotedRest()
    {
        return ( getRest( false ) );
    }
    
    public SimpleStringTokenizer( String string )
    {
        setString( string );
    }
}
