/**
 * Copyright (c) 2007-2014, JAGaToo Project Group all rights reserved.
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
package org.jagatoo.util.json;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.io.UnicodeBOM;

/**
 * Parses general JSON files.
 * 
 * @author Marvin Froehlich
 */
public class JSONParser
{
    /**
     * State transitions:
     * 
     * Initial: SEARCHING_VALUE
     * 
     * SEARCHING_VALUE -> [ READING_VALUE, SEARCHING_NAME, SEARCHING_ARRAY_ELEMENT ]  // regular value found, OBJECT_BEGIN found, ARRAY_BEGIN found
     * READING_NAME -> [ SEARCHING_DELIMITER, SEARCHING_VALUE ]  // name completed, NAME_VALUE_DELIMITER found without white space
     * SEARCHING_DELIMITER -> [ SEARCHING_VALUE ]
     * SEARCHING_VALUE -> [ READING_VALUE, SEARCHING_NAME, SEARCHING_ARRAY_ELEMENT ]  // regular value found, OBJECT_BEGIN found, ARRAY_BEGIN found
     * SEARCHING_ARRAY_ELEMENT -> [ READING_VALUE, SEARCHING_NAME, SEARCHING_ARRAY_ELEMENT ]  // regular entry found, OBJECT_BEGIN found, ARRAY_BEGIN found
     * READING_VALUE -> [ SEARCHING_TERMINATOR, SEARCHING_NAME, SEARCHING_ARRAY_ELEMENT ]  // regular entry found, OBJECT_BEGIN found, terminator found
     * SEARCHING_TERMINATOR
     * 
     * @author Marvin Froehlich
     */
    
    public static final char OBJECT_BEGIN = '{';
    public static final char OBJECT_END = '}';
    public static final char ARRAY_BEGIN = '[';
    public static final char ARRAY_END = ']';
    public static final char ESCAPE_CHAR = '\\';
    public static final char QUOTE = '\"';
    public static final char NEW_LINE = '\n';
    public static final char NAME_VALUE_DELIMITER = ':';
    public static final char ELEMENT_DELIMITER = ',';
    
    /**
     * Creates a new regular named object.
     * 
     * @param meta the parse meta data
     * @param name the object's name
     * 
     * @return the new object.
     */
    protected Object newObject( ParseMeta meta, String name )
    {
        return ( new java.util.HashMap<String, Object>() );
        //return ( new SimpleMap() );
    }
    
    /**
     * Creates a new regular named object.
     * 
     * @param meta the parse meta data (stack includes the current object)
     * @param objectName the current object's name
     * @param object the current object
     * @param childName the child object's name
     * @param childValue the child object to add to object
     */
    protected void addToObject( ParseMeta meta, String objectName, Object object, String childName, Object childValue )
    {
        @SuppressWarnings( "unchecked" )
        java.util.Map<String, Object> map = (java.util.Map<String, Object>)object;
        //SimpleMap map = (SimpleMap)object;
        map.put( childName, childValue );
    }
    
    /**
     * Creates a new array.
     * 
     * @param meta the parse meta data
     * @param name the array's name
     * @param size the array's size
     * 
     * @return the new object.
     */
    protected Object[] newArray( ParseMeta meta, String name, int size )
    {
        return ( new Object[ size ] );
    }
    
    /**
     * 
     * @param meta
     * @param list
     * @param arrayName
     * @param value
     * 
     * @return the index.
     */
    protected int addToArray( ParseMeta meta, TempList list, String arrayName, Object value )
    {
        list.add( value, meta, arrayName );
        
        return ( list.size() - 1 );
    }
    
    /**
     * Creates a custom object from the raw value. The default implementation returns a Long, if couldBeNumeric is true and the value is parseable to a Long, or a Double otherwise
     * and returns the raw value as a String, if couldBeNumeric is false.
     * 
     * @param meta
     * @param name
     * @param rawValue
     * @param couldBeNumeric
     * @param couldBeBoolean
     * 
     * @return the value object.
     */
    protected Object newValue( ParseMeta meta, String name, String rawValue, boolean couldBeNumeric, boolean couldBeBoolean )
    {
        if ( couldBeNumeric )
        {
            try
            {
                long value = Long.parseLong( rawValue, 10 );
                
                if ( ( value >= Integer.MIN_VALUE ) && ( value <= Integer.MAX_VALUE ) )
                    return ( (int)value );
                
                return ( value );
            }
            catch ( NumberFormatException e )
            {
                return ( Double.valueOf( rawValue ) );
            }
        }
        
        if ( couldBeBoolean )
            return ( Boolean.parseBoolean( rawValue ) );
        
        return ( rawValue );
    }
    
    /**
     * Searches the begin of a name.
     * 
     * @param br
     * @param meta
     * 
     * @return <code>true</code>, if a name was found, <code>false</code> otherwise (delimiter hit).
     * 
     * @throws IOException
     * @throws ParsingException
     */
    protected boolean searchName( BufferedReader br, ParseMeta meta ) throws IOException, ParsingException
    {
        int ch_ = -1;
        
        while ( ( ch_ = br.read() ) != -1 )
        {
            final char ch = (char)ch_;
            
            if ( Character.isWhitespace( ch ) )
            {
                if ( ch == NEW_LINE )
                    meta.incLineNo();
            }
            else if ( ch == QUOTE )
            {
                meta.setLastChar( ch );
                
                return ( true );
            }
            else if ( ( ( ch >= 'a' ) && ( ch <= 'z' ) ) || ( ( ch >= 'A' ) && ( ch <= 'Z' ) ) || ( ch == '_' ) )
            {
                meta.setLastChar( ch );
                
                return ( true );
            }
            else if ( ch == ELEMENT_DELIMITER )
            {
                // skip
            }
            else if ( ch == OBJECT_END )
            {
                meta.setLastChar( ch );
                
                return ( false );
            }
            else
            {
                throw new ParsingException( meta.getLineNo(), "Unexpected Character '" + ch + "' in line #" + meta.getLineNo() + "." );
            }
        }
        
        throw new ParsingException( meta.getLineNo(), "Unexpected EOF at line #" + meta.getLineNo() + "." );
    }
    
    private static void rTrim( StringBuilder sb )
    {
        while ( ( sb.length() > 0 ) && Character.isWhitespace( sb.charAt( sb.length() - 1 ) ) )
            sb.setLength( sb.length() - 1 );
    }
    
    /**
     * Reads a name.
     * 
     * @param br
     * @param meta
     * 
     * @return the name.
     * 
     * @throws IOException
     * @throws ParsingException
     */
    protected String readName( BufferedReader br, ParseMeta meta ) throws IOException, ParsingException
    {
        StringBuilder sb = new StringBuilder();
        
        if ( !searchName( br, meta ) )
            return ( null );
        
        boolean quoted = ( meta.getLastChar() == QUOTE );
        if ( !quoted )
            sb.append( meta.getLastChar() );
        
        char prevChar = meta.getLastChar();
        int ch_ = -1;
        
        while ( ( ch_ = br.read() ) != -1 )
        {
            final char ch = (char)ch_;
            
            if ( quoted && ( ch == QUOTE ) && ( prevChar != ESCAPE_CHAR ) )
            {
                meta.setLastChar( ch );
                
                return ( sb.toString() );
            }
            else if ( !quoted && ( ch == NAME_VALUE_DELIMITER ) )
            {
                meta.setLastChar( ch );
                
                rTrim( sb );
                
                return ( sb.toString() );
            }
            else
            {
                if ( ch == NEW_LINE )
                    meta.incLineNo();
                
                if ( ( ch == QUOTE ) && ( prevChar == ESCAPE_CHAR ) )
                    sb.setLength( sb.length() - 1 );
                
                sb.append( ch );
            }
            
            prevChar = ch;
        }
        
        rTrim( sb );
        
        return ( sb.toString() );
    }
    
    /**
     * Searching the begin of a value.
     * 
     * Transitions: SEARCHING_VALUE -> [ READING_VALUE, SEARCHING_NAME, SEARCHING_ARRAY_ELEMENT ]  // regular value found, OBJECT_BEGIN found, ARRAY_BEGIN found
     * 
     * @param br
     * @param meta the parse meta data
     * 
     * @return -1 for no value, 1 for quoted string value, 2 for true, 3 for false, 4 for numeric value, 5 for object, 6 for array
     * 
     * @throws IOException
     * @throws ParsingException
     */
    protected int searchValue( BufferedReader br, ParseMeta meta ) throws IOException, ParsingException
    {
        int ch_ = -1;
        
        while ( ( ch_ = br.read() ) != -1 )
        {
            final char ch = (char)ch_;
            
            if ( Character.isWhitespace( ch ) )
            {
                if ( ch == NEW_LINE )
                    meta.incLineNo();
            }
            else if ( ch == OBJECT_BEGIN )
            {
                meta.setLastChar( ch );
                
                return ( 5 );
            }
            else if ( !meta.isRoot() && ( ch == ELEMENT_DELIMITER ) )
            {
                // skip
            }
            else if ( !meta.isRoot() && ( ch == OBJECT_END ) )
            {
                return ( -1 );
            }
            else if ( !meta.isRoot() && ( ch == ARRAY_BEGIN ) )
            {
                meta.setLastChar( ch );
                
                return ( 6 );
            }
            else if ( !meta.isRoot() && ( ch == ARRAY_END ) )
            {
                return ( -1 );
            }
            else if ( !meta.isRoot() && ( ch == QUOTE ) )
            {
                meta.setLastChar( ch );
                
                return ( 1 );
            }
            else if ( !meta.isRoot() && ( ( ch == 't' ) || ( ch == 'T' ) ) )
            {
                meta.setLastChar( ch );
                
                return ( 2 );
            }
            else if ( !meta.isRoot() && ( ( ch == 'f' ) || ( ch == 'F' ) ) )
            {
                meta.setLastChar( ch );
                
                return ( 3 );
            }
            else if ( !meta.isRoot() && ( Character.isDigit( ch ) || ( ch == '.' ) || ( ch == '-' ) || ( ch == '+' ) ) )
            {
                meta.setLastChar( ch );
                
                return ( 4 );
            }
            else
            {
                throw new ParsingException( meta.getLineNo(), "Unexpected Character '" + ch + "' in line #" + meta.getLineNo() + "." );
            }
        }
        
        return ( -1 );
    }
    
    /**
     * Searching the begin of a value.
     * 
     * Transitions: SEARCHING_VALUE -> [ READING_VALUE, SEARCHING_NAME, SEARCHING_ARRAY_ELEMENT ]  // regular value found, OBJECT_BEGIN found, ARRAY_BEGIN found
     * 
     * @param br
     * @param meta the parse meta data
     * 
     * @throws IOException
     * @throws ParsingException
     */
    protected void searchNameValueDelimiter( BufferedReader br, ParseMeta meta ) throws IOException, ParsingException
    {
        int ch_ = -1;
        
        while ( ( ch_ = br.read() ) != -1 )
        {
            final char ch = (char)ch_;
            
            if ( Character.isWhitespace( ch ) )
            {
                if ( ch == NEW_LINE )
                    meta.incLineNo();
            }
            else if ( ch == NAME_VALUE_DELIMITER )
            {
                meta.setLastChar( ch );
                
                return;
            }
            else
            {
                throw new ParsingException( meta.getLineNo(), "Unexpected Character '" + ch + "' in line #" + meta.getLineNo() + "." );
            }
        }
    }
    
    /**
     * Reads a value.
     * 
     * @param br
     * @param meta
     * @param parentObject
     * @param name
     * 
     * @return the value.
     * 
     * @throws IOException
     * @throws ParsingException
     */
    protected Object readValue( BufferedReader br, ParseMeta meta, Object parentObject, String name ) throws IOException, ParsingException
    {
        StringBuilder sb = new StringBuilder();
        
        int valueType = searchValue( br, meta );
        
        if ( valueType < 0 )
            return ( null );
        
        long lineNo = meta.getLineNo();
        meta.notRoot();
        
        switch ( valueType )
        {
            case 1: // quoted string
            case 2: // true
            case 3: // false
            case 4: // numeric value
                boolean quoted = ( valueType == 1 );
                if ( !quoted )
                    sb.append( meta.getLastChar() );
                
                char prevChar = meta.getLastChar();
                int ch_ = -1;
                
                while ( ( ch_ = br.read() ) != -1 )
                {
                    final char ch = (char)ch_;
                    
                    if ( quoted && ( ch == QUOTE ) && ( prevChar != ESCAPE_CHAR ) )
                    {
                        meta.setLastChar( ch );
                        
                        return ( newValue( meta, name, sb.toString(), false, false ) );
                    }
                    else if ( !quoted && ( ( ch == ELEMENT_DELIMITER ) || ( ch == OBJECT_END ) || ( ch == ARRAY_END ) ) )
                    {
                        meta.setLastChar( ch );
                        
                        rTrim( sb );
                        
                        if ( valueType == 2 )
                        {
                            String value = sb.toString();
                            
                            if ( !value.equalsIgnoreCase( "true" ) )
                                throw new ParsingException( lineNo, "Unexpected value \"" + value + "\" in line #" + lineNo + "." );
                            
                            return ( newValue( meta, name, sb.toString(), false, true ) );
                        }
                        
                        if ( valueType == 3 )
                        {
                            String value = sb.toString();
                            
                            if ( !value.equalsIgnoreCase( "false" ) )
                                throw new ParsingException( lineNo, "Unexpected value \"" + value + "\" in line #" + lineNo + "." );
                            
                            return ( newValue( meta, name, sb.toString(), false, true ) );
                        }
                        
                        return ( newValue( meta, name, sb.toString(), true, false ) );
                    }
                    else
                    {
                        if ( ch == NEW_LINE )
                            meta.incLineNo();
                        
                        if ( ( ch == QUOTE ) && ( prevChar == ESCAPE_CHAR ) )
                            sb.setLength( sb.length() - 1 );
                        else if ( ( ch == '/' ) && ( prevChar == ESCAPE_CHAR ) )
                            sb.setLength( sb.length() - 1 );
                        else if ( ( ch == '\\' ) && ( prevChar == ESCAPE_CHAR ) )
                            sb.setLength( sb.length() - 1 );
                        
                        sb.append( ch );
                    }
                    
                    prevChar = ch;
                }
                
                rTrim( sb );
                
                return ( sb.toString() );
                
            case 5: // object
                Object object = newObject( meta, name );
                meta.pushToStack( name, object );
                
                try
                {
                    String childName = null;
                    
                    while ( ( childName = readName( br, meta ) ) != null )
                    {
                        if ( meta.getLastChar() != NAME_VALUE_DELIMITER )
                            searchNameValueDelimiter( br, meta );
                        
                        Object value = readValue( br, meta, object, childName );
                        
                        addToObject( meta, name, object, childName, value );
                        
                        if ( meta.consumeLastChar() == OBJECT_END )
                            break;
                    }
                }
                finally
                {
                    meta.consumeLastChar();
                    meta.popFromStack();
                }
                
                return ( object );
                
            case 6: // array
                TempList list = new TempList( this, meta, name );
                meta.pushToStack( name, list );
                
                try
                {
                    Object value = null;
                    
                    int index = 0;
                    
                    while ( ( value = readValue( br, meta, list, String.valueOf( index ) ) ) != null )
                    {
                        addToArray( meta, list, name, value );
                        
                        index++;
                        
                        char lc = meta.consumeLastChar();
                        
                        if ( lc == OBJECT_END )
                            throw new ParsingException( meta.getLineNo(), "Unexpected Character '" + meta.getLastChar() + "' in line #" + meta.getLineNo() + "." );
                        
                        if ( lc == ARRAY_END )
                            break;
                    }
                }
                finally
                {
                    meta.consumeLastChar();
                    meta.popFromStack();
                }
                
                return ( list.getArray( meta, name ) );
        }
        
        throw new Error( "Unsupported value type: " + valueType );
    }
    
    protected void checkRemainingFileContents( BufferedReader br, ParseMeta meta ) throws IOException, ParsingException
    {
        int ch_ = -1;
        
        while ( ( ch_ = br.read() ) != -1 )
        {
            final char ch = (char)ch_;
            
            if ( Character.isWhitespace( ch ) )
            {
                if ( ch == NEW_LINE )
                    meta.incLineNo();
            }
            else
            {
                throw new ParsingException( meta.getLineNo(), "Unexpected Character '" + ch + "' in line #" + meta.getLineNo() + "." );
            }
        }
    }
    
    protected ParseMeta newParseMeta()
    {
        return ( new ParseMeta() );
    }
    
    protected Object parse( BufferedReader br ) throws IOException, ParsingException
    {
        try
        {
            ParseMeta meta = newParseMeta();
            Object result = readValue( br, meta, null, null );
            
            checkRemainingFileContents( br, meta );
            
            return ( result );
        }
        finally
        {
            try
            {
                br.close();
            }
            catch ( IOException e )
            {
            }
        }
    }
    
    public final Object parse( Reader r ) throws IOException, ParsingException
    {
        return ( parse( ( r instanceof BufferedReader ) ? (BufferedReader)r : new BufferedReader( r ) ) );
    }
    
    public final Object parse( InputStream in, Charset charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( in, charset ) ) );
    }
    
    public final Object parse( InputStream in, String charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( in, Charset.forName( charset ) ) ) );
    }
    
    public final Object parse( InputStream in ) throws IOException, ParsingException
    {
        BufferedInputStream bin = ( in instanceof BufferedInputStream ) ? (BufferedInputStream)in : new BufferedInputStream( in );
        
        UnicodeBOM bom = UnicodeBOM.skipBOM( bin );
        
        Charset charset = ( bom == null ) ? null : bom.getCharset();
        
        if ( charset == null )
            return ( parse( new InputStreamReader( in ) ) );
        
        return ( parse( new InputStreamReader( in, charset ) ) );
    }
    
    public final Object parse( URL url, Charset charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( url.openStream(), charset ) ) );
    }
    
    public final Object parse( URL url, String charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( url.openStream(), Charset.forName( charset ) ) ) );
    }
    
    public final Object parse( URL url ) throws IOException, ParsingException
    {
        return ( parse( url.openStream() ) );
    }
    
    public final Object parse( File file, Charset charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( new FileInputStream( file ), charset ) ) );
    }
    
    public final Object parse( File file, String charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( new FileInputStream( file ), Charset.forName( charset ) ) ) );
    }
    
    public final Object parse( File file ) throws IOException, ParsingException
    {
        return ( parse( new FileInputStream( file ) ) );
    }
    
    public final Object parse( String filename, Charset charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( new FileInputStream( filename ), charset ) ) );
    }
    
    public final Object parse( String filename, String charset ) throws IOException, ParsingException
    {
        return ( parse( new InputStreamReader( new FileInputStream( filename ), Charset.forName( charset ) ) ) );
    }
    
    public final Object parse( String filename ) throws IOException, ParsingException
    {
        return ( parse(new FileInputStream( filename ) ) );
    }
    
    public JSONParser()
    {
    }
    
    /*
    public static void main( String[] args ) throws Throwable
    {
        Object object = new JSONParser().parse( "c:\\Spiele\\rFactor2\\UserData\\player\\Controller.JSON" );
        
        System.out.println( object );
    }
    */
}
