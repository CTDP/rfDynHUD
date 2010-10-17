/**
 * Copyright (c) 2007-2010, JAGaToo Project Group all rights reserved.
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
package org.jagatoo.util.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Writes XML data.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class SimpleXMLWriter
{
    private static final String XML_HEADER1 = "<?xml version=\"1.0\" encoding=\"";
    private static final String XML_HEADER2 = "\"?>";
    
    private final XMLPath path = new XMLPath();
    private boolean[] lastElemPushed = new boolean[ 16 ];
    
    private BufferedWriter bw;
    
    private boolean useTabs = false;
    private String indentString = "    ";
    private boolean addNewLines = true;
    
    private String closeElementName = null;
    private boolean indentCloseElem = false;
    private int lastElementIndent = 0;
    private String lastElementName = null;
    private Object[] lastAttributes = null;
    
    public void setIndentation( int indent, boolean useTabs )
    {
        char[] chars = new char[ indent ];
        for ( int i = 0; i < indent; i++ )
            chars[i] = useTabs ? '\t' : ' ';
        
        this.indentString = new String( chars );
        this.useTabs = useTabs;
    }
    
    public final void setIndentation( int indent )
    {
        setIndentation( indent, false );
    }
    
    public final boolean getUseTabsForIndentation()
    {
        return ( useTabs );
    }
    
    public void setAddNewLines( boolean addNewLines )
    {
        this.addNewLines = addNewLines;
    }
    
    public final boolean getAddNewLines()
    {
        return ( addNewLines );
    }
    
    public final XMLPath getPath()
    {
        return ( path );
    }
    
    public final int getLevel()
    {
        return ( path.getLevel() );
    }
    
    protected void newLine() throws IOException
    {
        bw.newLine();
    }
    
    /**
     * 
     * @param name
     * 
     * @return the name.
     */
    protected String validateElementName( String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "element name must not be null" );
        
        name = name.trim();
        
        if ( name.length() == 0 )
            throw new IllegalArgumentException( "element name must not be empty" );
        
        for ( int i = 0; i < name.length(); i++ )
        {
            if ( Character.isWhitespace( name.charAt( i ) ) )
                throw new IllegalArgumentException( "The element name must not contain whitespaces." );
        }
        
        return ( name );
    }
    
    /**
     * 
     * @param name
     * 
     * @return the name.
     */
    protected String validateAttributeName( String name )
    {
        if ( name == null )
            throw new IllegalArgumentException( "attribute names must not be null" );
        
        name = name.trim();
        
        if ( name.length() == 0 )
            throw new IllegalArgumentException( "attribute names must not be empty" );
        
        for ( int i = 0; i < name.length(); i++ )
        {
            if ( Character.isWhitespace( name.charAt( i ) ) )
                throw new IllegalArgumentException( "An attribute name must not contain whitespaces." );
        }
        
        return ( name );
    }
    
    /**
     * Encodes the attribute value to valid XML data.
     * 
     * @param value
     * 
     * @return the encoded attribute value.
     */
    protected String encodeAttributeValue( Object value )
    {
        // TODO
        return ( String.valueOf( value ) );
    }
    
    /**
     * Encodes the element data to valid XML data.
     * 
     * @param data
     * 
     * @return the encoded XML data.
     */
    protected String encodeElementData( Object data )
    {
        // TODO
        return ( String.valueOf( data ) );
    }
    
    private void applyLastElement( boolean newLine, boolean closeDirectly ) throws IOException
    {
        if ( closeElementName != null )
        {
            if ( indentCloseElem )
            {
                for ( int i = 0; i < lastElementIndent; i++ )
                    bw.write( indentString );
            }
            
            bw.write( "</" );
            bw.write( closeElementName );
            bw.write( '>' );
            
            if ( getAddNewLines() )
                newLine();
            
            closeElementName = null;
        }
        
        if ( lastElementName != null )
        {
            for ( int i = 0; i < lastElementIndent; i++ )
                bw.write( indentString );
            
            bw.write( '<' );
            bw.write( lastElementName );
            
            if ( lastAttributes != null )
            {
                for ( int i = 0; i < lastAttributes.length; i += 2 )
                {
                    bw.write( ' ' );
                    bw.write( String.valueOf( lastAttributes[i + 0] ).trim() );
                    bw.write( "=\"" );
                    bw.write( encodeAttributeValue( lastAttributes[i + 1] ) );
                    bw.write( '\"' );
                }
            }
            
            if ( closeDirectly )
                bw.write( " />" );
            else
                bw.write( '>' );
            
            if ( newLine && getAddNewLines() )
                newLine();
            
            lastElementName = null;
            lastAttributes = null;
        }
    }
    
    /**
     * Converts the passed element to a path element. By default the passed element is returned.
     * 
     * @param level
     * @param element
     * 
     * @return the converted path element.
     */
    protected Object getPathObject( int level, String element )
    {
        return ( element );
    }
    
    /**
     * Writes an XML element to the file.
     * 
     * @param push push element hierarchy one level down?
     * @param name the element's name
     * @param attributes the attributes (attribName1, attribValue1, attribName2, attribValue2, ...)
     * 
     * @throws IOException
     */
    protected void writeElement( boolean push, String name, Object[] attributes ) throws IOException
    {
        if ( ( attributes != null ) && ( ( attributes.length % 2 ) != 0 ) )
            throw new IllegalArgumentException( "attributes array not of even length" );
        
        name = validateElementName( name );
        
        if ( attributes != null )
        {
            for ( int i = 0; i < attributes.length; i += 2 )
            {
                validateAttributeName( String.valueOf( attributes[i + 0] ) );
            }
        }
        
        if ( lastElemPushed.length <= path.getLevel() + 1 )
        {
            boolean[] tmp = new boolean[ lastElemPushed.length * 150 / 100 + 1 ];
            System.arraycopy( lastElemPushed, 0, tmp, 0, lastElemPushed.length );
            lastElemPushed = tmp;
        }
        
        if ( ( path.getLevel() > 0 ) && !lastElemPushed[path.getLevel() - 1] )
            path.popPath();
        
        applyLastElement( true, !lastElemPushed[Math.max( 0, lastElementIndent )] );
        
        path.pushPath( name, getPathObject( path.getLevel(), name ) );
        
        lastElemPushed[path.getLevel() - 1] = push;
        
        closeElementName = null;
        lastElementIndent = path.getLevel() - 1;
        lastElementName = name;
        lastAttributes = attributes;
    }
    
    /**
     * Writes an XML element to the file.
     * 
     * @param name the element's name
     * @param attributes the attributes (attribName1, attribValue1, attribName2, attribValue2, ...)
     * 
     * @throws IOException
     */
    public final void writeElement( String name, Object... attributes ) throws IOException
    {
        writeElement( false, name, attributes );
    }
    
    /**
     * Writes an XML element to the file and pushes one level down, so that succeeding elements become children of this.
     * 
     * @param name the element's name
     * @param attributes the attributes (attribName1, attribValue1, attribName2, attribValue2, ...)
     * 
     * @throws IOException
     */
    public final void writeElementAndPush( String name, Object... attributes ) throws IOException
    {
        writeElement( true, name, attributes );
    }
    
    public void writeElementData( String data ) throws IOException
    {
        String closeElementName = lastElementName;
        applyLastElement( false, false );
        if ( closeElementName != null )
        {
            this.closeElementName = closeElementName;
            indentCloseElem = false;
        }
        bw.write( encodeElementData( data ) );
    }
    
    /**
     * Pops the level hierarchy one level up.
     * 
     * @throws IOException
     */
    public void popElement() throws IOException
    {
        if ( path.getLevel() < 1 )
            throw new IllegalStateException( "Cannot pop hierarchy" );
        
        applyLastElement( true, true );
        
        path.popPath();
        
        closeElementName = path.getLastPathElement();
        indentCloseElem = true;
        lastElementIndent = path.getLevel() - 1;
        
        applyLastElement( true, true );
        
        if ( getLevel() > 0 )
            lastElemPushed[getLevel() - 1] = false;
    }
    
    public void close()
    {
        if ( bw == null )
            return;
        
        try
        {
            while ( path.getLevel() > 1 )
                popElement();
            
            applyLastElement( true, true );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
        
        try
        {
            bw.close();
        }
        catch ( IOException e )
        {
        }
        
        bw = null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable
    {
        Throwable tt = null;
        
        try
        {
            super.finalize();
        }
        catch ( Throwable t )
        {
            tt = t;
        }
        
        close();
        
        if ( tt != null )
            throw tt;
    }
    
    /**
     * 
     * @param out
     * @param codepage
     * @param charset
     * 
     * @throws IOException
     */
    protected SimpleXMLWriter( OutputStream out, String codepage, Charset charset ) throws IOException
    {
        if ( codepage != null )
            charset = Charset.forName( codepage );
        
        if ( charset == null )
            charset = Charset.forName( "UTF-8" );
        
        this.bw = new BufferedWriter( new OutputStreamWriter( out, charset ) );
        
        bw.write( XML_HEADER1 );
        bw.write( charset.name() );
        bw.write( XML_HEADER2 );
        if ( getAddNewLines() )
            newLine();
        if ( getAddNewLines() )
            newLine();
    }
    
    /**
     * Parses the given file.
     * 
     * @param out
     * @param charset
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( OutputStream out, String charset ) throws IOException
    {
        this( out, charset, null );
    }
    
    /**
     * Parses the given file.
     * 
     * @param out
     * @param charset
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( OutputStream out, Charset charset ) throws IOException
    {
        this( out, null, charset );
    }
    
    /**
     * Parses the given file.
     * 
     * @param out
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( OutputStream out ) throws IOException
    {
        this( out, null, null );
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * @param charset
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( File file, String charset ) throws IOException
    {
        this( new FileOutputStream( file ), charset, null );
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * @param charset
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( File file, Charset charset ) throws IOException
    {
        this( new FileOutputStream( file ), null, charset );
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( File file ) throws IOException
    {
        this( new FileOutputStream( file ), null, null );
    }
    
    /**
     * Parses the given file.
     * 
     * @param filename
     * @param charset
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( String filename, String charset ) throws IOException
    {
        this( new FileOutputStream( filename ), charset, null );
    }
    
    /**
     * Parses the given file.
     * 
     * @param filename
     * @param charset
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( String filename, Charset charset ) throws IOException
    {
        this( new FileOutputStream( filename ), null, charset );
    }
    
    /**
     * Parses the given file.
     * 
     * @param filename
     * 
     * @throws IOException
     */
    public SimpleXMLWriter( String filename ) throws IOException
    {
        this( new FileOutputStream( filename ), null, null );
    }
}
