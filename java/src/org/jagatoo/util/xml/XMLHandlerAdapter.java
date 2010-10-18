/**
 * Copyright (c) 2007-20010, JAGaToo Project Group all rights reserved.
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class XMLHandlerAdapter extends org.xml.sax.helpers.DefaultHandler
{
    private final XMLPath path = new XMLPath();
    
    private SimpleXMLHandler simpleHandler = null;
    private Object userObject = null;
    
    public void setSimpleHandler( SimpleXMLHandler simpleHandler )
    {
        if ( simpleHandler == null )
            throw new IllegalArgumentException( "simpleHandler must not be null." );
        
        if ( simpleHandler == this.simpleHandler )
            return;
        
        this.simpleHandler.setAdapter( null );
        this.simpleHandler = simpleHandler;
        this.simpleHandler.setAdapter( this );
    }
    
    public void setUserObject( Object userObject )
    {
        this.userObject = userObject;
    }
    
    public final Object getUserObject()
    {
        return ( userObject );
    }
    
    @Override
    public void startDocument() throws SAXException
    {
        path.reset();
        
        simpleHandler.onDocumentStarted();
    }
    
    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {
        SimpleXMLHandler handler = this.simpleHandler;
        
        Object element = handler.getPathObject( path.getLevel() + 1, qName );
        if ( element == null )
            throw new RuntimeException( "The getPathObject() method must never return null." );
        
        path.pushPath( qName, element );
        
        handler.onElementStarted( path, qName, attributes );
    }
    
    @Override
    public void characters( char[] data, int start, int length ) throws SAXException
    {
        simpleHandler.onElementData( path, data, start, length );
    }
    
    @Override
    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        simpleHandler.onElementEnded( path, qName );
        
        path.popPath();
    }
    
    @Override
    public void warning( SAXParseException ex ) throws SAXException
    {
        simpleHandler.onParsingException( path, SimpleXMLHandler.ExceptionSeverity.WARNING, ex );
    }
    
    @Override
    public void error( SAXParseException ex ) throws SAXException
    {
        simpleHandler.onParsingException( path, SimpleXMLHandler.ExceptionSeverity.ERROR, ex );
    }
    
    @Override
    public void fatalError( SAXParseException ex ) throws SAXException
    {
        simpleHandler.onParsingException( path, SimpleXMLHandler.ExceptionSeverity.FATAL_ERROR, ex );
    }
    
    @Override
    public void endDocument() throws SAXException
    {
        simpleHandler.onDocumentEnded();
    }
    
    public XMLHandlerAdapter( SimpleXMLHandler simpleHandler )
    {
        this.simpleHandler = simpleHandler;
        this.simpleHandler.setAdapter( this );
    }
}
