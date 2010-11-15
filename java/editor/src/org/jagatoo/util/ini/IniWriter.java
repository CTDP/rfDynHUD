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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * The {@link IniWriter} writes ini files ;).
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class IniWriter
{
    private final BufferedWriter writer;
    
    private boolean spaceGroup = false;
    private String indentString = null;
    private boolean putSpacesAroundEqualSign = true;
    private int minEqualSignPosition = -1;
    private int minValuePosition = -1;
    private int minCommentPosition = -1;
    private String nullValue = "N/A";
    
    private boolean isFirstLine = true;
    private boolean isInGroup = false;
    
    /**
     * Sets the number of spaces to indent settings by.
     * 
     * @param numSpaces
     */
    public void setSettingIndentSpaces( int numSpaces )
    {
        if ( numSpaces <= 0)
        {
            indentString = null;
        }
        else
        {
            char[] spaces = new char[ numSpaces ];
            for ( int i = 0; i < numSpaces; i++ )
            {
                spaces[i] = ' ';
            }
            this.indentString = new String( spaces );
        }
    }
    
    /**
     * Gets the number of spaces to indent settings by.
     * 
     * @return the number of spaces to indent settings by.
     */
    public final int getSettingIndentSpaces()
    {
        if ( indentString == null )
            return ( 0 );
        
        return ( indentString.length() );
    }
    
    /**
     * Configures the writer to put a spaces before and after the group name (between the brackets).
     * 
     * @param b
     */
    public void setSpaceGroup( boolean b )
    {
        this.spaceGroup = b;
    }
    
    /**
     * Gets setting "spaceGroup".
     * 
     * @return setting "spaceGroup".
     */
    public final boolean getSpaceGroup()
    {
        return ( spaceGroup );
    }
    
    /**
     * Configures the writer to put a spaces before and after the equal sign of a setting.
     * 
     * @param b
     */
    public void setPutSpacesAroundEqualSign( boolean b )
    {
        this.putSpacesAroundEqualSign = b;
    }
    
    /**
     * Gets setting "putSpacesAroundEqualSign".
     * 
     * @return setting "putSpacesAroundEqualSign".
     */
    public final boolean getPutSpacesAroundEqualSign()
    {
        return ( putSpacesAroundEqualSign );
    }
    
    /**
     * Configures the writer to put as many spaces before the equals sign, so that it is placed at least at the given column.
     * 
     * @param minPos (Use negative values for no specific rule.)
     */
    public void setMinEqualSignPosition( int minPos )
    {
        this.minEqualSignPosition = minPos;
    }
    
    /**
     * Gets setting "minEqualSignPosition".
     * 
     * @return setting "minEqualSignPosition".
     */
    public final int getMinEqualSignPosition()
    {
        return ( minEqualSignPosition );
    }
    
    /**
     * Configures the writer to put as many spaces before the value, so that it is placed at least at the given column.
     * 
     * @param minPos (Use negative values for no specific rule.)
     */
    public void setMinValuePosition( int minPos )
    {
        this.minValuePosition = minPos;
    }
    
    /**
     * Gets setting "minValuePosition".
     * 
     * @return setting "minValuePosition".
     */
    public final int getMinValuePosition()
    {
        return ( minValuePosition );
    }
    
    /**
     * Configures the writer to put as many spaces before the comment, so that it is placed at least at the given column.
     * 
     * @param minPos (Use negative values for no specific rule.)
     */
    public void setMinCommentPosition( int minPos )
    {
        this.minCommentPosition = minPos;
    }
    
    /**
     * Gets setting "minCommentPosition".
     * 
     * @return setting "minCommentPosition".
     */
    public final int getMinCommentPosition()
    {
        return ( minCommentPosition );
    }
    
    /**
     * Sets the String to be written for a null value.
     * 
     * @param nullValue
     */
    public void setNullValue( String nullValue )
    {
        if ( nullValue == null )
            throw new IllegalArgumentException( "nullValue must not be null." );
        
        this.nullValue = nullValue;
    }
    
    /**
     * Gets the String to be written for a null value.
     * 
     * @return the String to be written for a null value.
     */
    public final String getNullValue()
    {
        return ( nullValue );
    }
    
    /**
     * Gets the String to write to the ini file from the passed value.
     * 
     * @param value the value to write to the ini file
     * @param quoteValue if <code>true</code>, the value will be enclosed by double quotes, if <code>false</code>, no quotes will be used, if <code>null</code> some default behavior will be used.
     * 
     * @return the String to write to the ini file from the passed value.
     */
    protected String getObjectValue( Object value, Boolean quoteValue )
    {
        if ( quoteValue == Boolean.TRUE )
            return ( "\"" + String.valueOf( value ) + "\"" );
        
        if ( quoteValue == Boolean.FALSE )
            return ( String.valueOf( value ) );
        
        return ( "\"" + String.valueOf( value ) + "\"" );
    }
    
    /**
     * Writes a new Group to the file.
     * 
     * @param group
     * 
     * @throws IOException
     */
    public void writeGroup( String group ) throws IOException
    {
        if ( !isFirstLine )
            writer.newLine();
        
        writer.write( "[" );
        if ( getSpaceGroup() )
            writer.write( " " );
        writer.write( group );
        if ( getSpaceGroup() )
            writer.write( " " );
        writer.write( "]" );
        writer.newLine();
        
        isFirstLine = false;
        isInGroup = true;
    }
    
    /**
     * Writes a new setting to the next line.
     * 
     * @param key
     * @param value if this is not a number, it is quoted in double-quotes and then written using the toString() method.
     * @param quoteValue
     * @param comment null for no comment
     * 
     * @throws IOException
     */
    public void writeSetting( String key, Object value, Boolean quoteValue, String comment ) throws IOException
    {
        int pos = 0;
        
        if ( indentString != null )
        {
            writer.write( indentString );
            pos += indentString.length();
        }
        
        writer.write( key );
        pos += key.length();
        if ( getMinEqualSignPosition() > pos )
        {
            for ( int i = pos; i < getMinEqualSignPosition() - 1; i++ )
            {
                writer.write( ' ' );
                pos++;
            }
            
            if ( getPutSpacesAroundEqualSign() )
            {
                writer.write( "= " );
                pos += 2;
            }
            else
            {
                writer.write( "=" );
                pos += 1;
            }
        }
        else if ( getPutSpacesAroundEqualSign() )
        {
            writer.write( " = " );
            pos += 3;
        }
        else
        {
            writer.write( "=" );
            pos += 1;
        }
        
        if ( value == null )
        {
            value = getNullValue();
        }
        else if ( ( value instanceof Number ) || ( value instanceof Boolean ) )
        {
            value = String.valueOf( value );
        }
        else if ( value instanceof Enum<?> )
        {
            value = ( (Enum<?>)value ).name();
        }
        else
        {
            value = getObjectValue( value, quoteValue );
        }
        
        if ( getMinValuePosition() > pos )
        {
            for ( int i = pos; i < getMinValuePosition() - 1; i++ )
            {
                writer.write( ' ' );
                pos++;
            }
        }
        
        writer.write( (String)value );
        pos += ( (String)value ).length();
        
        if ( comment != null )
        {
            writer.write( ' ' );
            
            if ( getMinCommentPosition() > pos )
            {
                for ( int i = pos; i < getMinCommentPosition() - 1; i++ )
                {
                    writer.write( ' ' );
                    pos++;
                }
            }
            
            writer.write( "// " );
            pos += 3;
            writer.write( comment );
            pos += comment.length();
        }
        
        writer.newLine();
        
        isFirstLine = false;
    }
    
    /**
     * Writes a new setting to the next line.
     * 
     * @param key
     * @param value if this is not a number, it is quoted in double-quotes and then written using the toString() method.
     * @param comment null for no comment
     * 
     * @throws IOException
     */
    public void writeSetting( String key, Object value, String comment ) throws IOException
    {
        writeSetting( key, value, null, comment );
    }
    
    /**
     * Writes a new setting to the next line.
     * 
     * @param key
     * @param value if this is not a number, it is quoted in double-quotes and then written using the toString() method.
     * 
     * @throws IOException
     */
    public final void writeSetting( String key, Object value ) throws IOException
    {
        writeSetting( key, value, null );
    }
    
    /**
     * Writes a new (standalone) comment to the file.
     * 
     * @param comment
     * 
     * @throws IOException
     */
    public void writeComment( String comment ) throws IOException
    {
        if ( isInGroup && ( indentString != null ) )
        {
            writer.write( indentString );
        }
        
        writer.write( "# " );
        writer.write( comment );
        
        writer.newLine();
        
        isFirstLine = false;
    }
    
    /**
     * Writes an empty line to the file.
     * 
     * @throws IOException
     */
    public void writeEmptyLine() throws IOException
    {
        if ( isInGroup && ( indentString != null ) )
        {
            writer.write( indentString );
        }
        
        writer.newLine();
        
        isFirstLine = false;
    }
    
    /**
     * Flushes the file.
     * 
     * @throws IOException
     */
    public void flush() throws IOException
    {
        writer.flush();
    }
    
    /**
     * Flushes and closes the file.
     * 
     * @throws IOException
     */
    public void close() throws IOException
    {
        writer.close();
    }
    
    public IniWriter( Writer writer )
    {
        if ( writer instanceof BufferedWriter )
            this.writer = (BufferedWriter)writer;
        else
            this.writer = new BufferedWriter( writer );
    }
    
    public IniWriter( OutputStream out )
    {
        this( new OutputStreamWriter( out ) );
    }
    
    public IniWriter( File file ) throws IOException
    {
        this( new FileWriter( file ) );
    }
    
    public IniWriter( String filename ) throws IOException
    {
        this( new FileWriter( filename ) );
    }
}
