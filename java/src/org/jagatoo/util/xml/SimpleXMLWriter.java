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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Writes XML data.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class SimpleXMLWriter
{
    private final XMLPath path = new XMLPath();
    private boolean[] lastElemPushed = new boolean[ 16 ];
    
    private final Charset charset;
    private BufferedWriter bw;
    
    private boolean useTabs = false;
    private String indentString = "    ";
    private boolean addNewLines = true;
    
    private String closeElementName = null;
    @SuppressWarnings( "unused" )
    private boolean indentCloseElem = false;
    private int lastElementIndent = 0;
    private String lastElementName = null;
    private Object[] lastAttributes = null;
    
    private final AttributesImpl attributes = new AttributesImpl();
    private ContentHandler contentHandler = null;
    
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
    
    private void initSAX() throws IOException, SAXException
    {
        OutputFormat of = new OutputFormat( "XML", charset.name(), indentString.length() > 0 );
        of.setIndent( indentString.length() );
        of.setLineSeparator( getAddNewLines() ? System.getProperty( "line.separator" ) : "" );
        of.setLineWidth( 0 );
        //of.setDoctype( null, "blah.dtd" );
        XMLSerializer serializer = new XMLSerializer( bw, of );
        
        contentHandler = serializer.asContentHandler();
        contentHandler.startDocument();
    }
    
    private void encapsulateAttributes( Object[] attributes )
    {
        this.attributes.clear();
        
        if ( attributes != null )
        {
            for ( int i = 0; i < attributes.length; i += 2 )
            {
                this.attributes.addAttribute( "", "", String.valueOf( attributes[i + 0] ), "CDATA", String.valueOf( attributes[i + 1] ) );
            }
        }
    }
    
    /**
     * 
     * @param newLine
     * @param closeDirectly
     * @throws SAXException
     */
    private void applyLastElement( boolean newLine, boolean closeDirectly ) throws SAXException
    {
        if ( closeElementName != null )
        {
            contentHandler.endElement( "", "", closeElementName );
            
            closeElementName = null;
        }
        
        if ( lastElementName != null )
        {
            encapsulateAttributes( lastAttributes );
            
            contentHandler.startElement( "", "", lastElementName, attributes );
            
            if ( closeDirectly )
                contentHandler.endElement( "", "", lastElementName );
            
            lastElementName = null;
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
     * @throws SAXException
     */
    protected void writeElement( boolean push, String name, Object[] attributes ) throws IOException, SAXException
    {
        if ( ( attributes != null ) && ( ( attributes.length % 2 ) != 0 ) )
            throw new IllegalArgumentException( "attributes array not of even length" );
        
        if ( contentHandler == null )
            initSAX();
        
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
     * @throws SAXException
     */
    public final void writeElement( String name, Object... attributes ) throws IOException, SAXException
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
     * @throws SAXException
     */
    public final void writeElementAndPush( String name, Object... attributes ) throws IOException, SAXException
    {
        writeElement( true, name, attributes );
    }
    
    /**
     * Writes element data into the last started element.
     * 
     * @param data
     * 
     * @throws IOException
     * @throws SAXException
     */
    public void writeElementData( String data ) throws IOException, SAXException
    {
        String closeElementName = lastElementName;
        applyLastElement( false, false );
        if ( closeElementName != null )
        {
            this.closeElementName = closeElementName;
            indentCloseElem = false;
        }
        
        contentHandler.characters( data.toCharArray(), 0, data.length() );
    }
    
    /**
     * Pops the level hierarchy one level up.
     * 
     * @throws IOException
     * @throws SAXException
     */
    public void popElement() throws IOException, SAXException
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
    
    /**
     * 
     * @throws IOException
     * @throws SAXException
     */
    public void close() throws IOException, SAXException
    {
        if ( bw == null )
            return;
        
        try
        {
            while ( path.getLevel() > 1 )
                popElement();
            
            applyLastElement( true, true );
            
            contentHandler.endDocument();
        }
        finally
        {
            try
            {
                bw.close();
            }
            finally
            {
                bw = null;
            }
        }
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
        
        this.charset = charset;
        this.bw = new BufferedWriter( new OutputStreamWriter( out, charset ) );
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
