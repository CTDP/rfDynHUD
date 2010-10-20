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

import java.util.ArrayList;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class XMLHandlerAdapter extends org.xml.sax.helpers.DefaultHandler
{
    private final ArrayList<XMLPath> pathStack = new ArrayList<XMLPath>();
    private XMLPath path = null;
    private final Stack<SimpleXMLHandlerDelegate> simpleHandlerStack = new Stack<SimpleXMLHandlerDelegate>();
    private SimpleXMLHandlerDelegate simpleHandler = null;
    private Object userObject = null;
    
    private boolean forkAllowed = false;
    private String currElement = null;
    private Object currObject = null;
    private Attributes currAttribs = null;
    private final AttributesImpl lastAttribs = new AttributesImpl();
    
    final void delegate( SimpleXMLHandlerDelegate delegate ) throws SAXException
    {
        if ( delegate == null )
            throw new IllegalArgumentException( "delegate must not be null." );
        
        if ( !forkAllowed )
            throw new IllegalStateException( "delegate() can only be called from the onElementStarted() event." );
        
        delegate.handleForkElement( currElement, currObject, currAttribs );
        
        simpleHandler.setAdapter( null );
        simpleHandlerStack.push( delegate );
        simpleHandler = delegate;
        simpleHandler.setAdapter( this );
        
        if ( pathStack.size() < simpleHandlerStack.size() )
        {
            path = new XMLPath();
            pathStack.add( path );
        }
        else
        {
            path = pathStack.get( simpleHandlerStack.size() - 1 );
            path.reset();
        }
    }
    
    private void undelegate()
    {
        simpleHandler.setAdapter( null );
        simpleHandlerStack.pop();
        simpleHandler = simpleHandlerStack.peek();
        simpleHandler.setAdapter( this );
        
        path = pathStack.get( simpleHandlerStack.size() - 1 );
    }
    
    public void setSimpleHandler( SimpleXMLHandlerDelegate simpleHandler )
    {
        if ( simpleHandler == null )
            throw new IllegalArgumentException( "simpleHandler must not be null." );
        
        if ( simpleHandler == this.simpleHandler )
            return;
        
        if ( this.simpleHandler != null )
            this.simpleHandler.setAdapter( null );
        this.simpleHandlerStack.set( simpleHandlerStack.size() - 1, simpleHandler );
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
        
        ( (SimpleXMLHandler)simpleHandler ).onDocumentStarted();
    }
    
    @Override
    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {
        lastAttribs.clear();
        for ( int i = 0; i < attributes.getLength(); i++ )
            lastAttribs.addAttribute( attributes.getURI( i ), attributes.getLocalName( i ), attributes.getQName( i ), attributes.getType( i ), attributes.getType( i ) );
        
        SimpleXMLHandlerDelegate handler = this.simpleHandler;
        XMLPath path = this.path;
        
        Object object = handler.getPathObject( path, qName );
        if ( object == null )
            throw new RuntimeException( "The getPathObject() method must never return null." );
        
        forkAllowed = true;
        currElement = qName;
        currObject = object;
        currAttribs = attributes;
        try
        {
            handler.onElementStarted( path, qName, object, attributes );
        }
        finally
        {
            currAttribs = null;
            currObject = object;
            currElement = qName;
            forkAllowed = false;
        }
        
        path.pushPath( qName, object );
    }
    
    @Override
    public void characters( char[] data, int start, int length ) throws SAXException
    {
        simpleHandler.onElementData( path, lastAttribs, data, start, length );
    }
    
    @Override
    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        if ( path.getLevel() == 0 )
            undelegate();
        
        Object object = path.getLastPathObject();
        
        path.popPath();
        
        simpleHandler.onElementEnded( path, qName, object );
    }
    
    @Override
    public void warning( SAXParseException ex ) throws SAXException
    {
        XMLPath fullPath = XMLPath.getFullPath( pathStack );
        
        for ( int i = 0; i < simpleHandlerStack.size(); i++ )
            simpleHandlerStack.get( i ).onParsingException( fullPath, SimpleXMLHandlerDelegate.ExceptionSeverity.WARNING, ex );
    }
    
    @Override
    public void error( SAXParseException ex ) throws SAXException
    {
        XMLPath fullPath = XMLPath.getFullPath( pathStack );
        
        for ( int i = 0; i < simpleHandlerStack.size(); i++ )
            simpleHandlerStack.get( i ).onParsingException( fullPath, SimpleXMLHandlerDelegate.ExceptionSeverity.ERROR, ex );
    }
    
    @Override
    public void fatalError( SAXParseException ex ) throws SAXException
    {
        XMLPath fullPath = XMLPath.getFullPath( pathStack );
        
        for ( int i = 0; i < simpleHandlerStack.size(); i++ )
            simpleHandlerStack.get( i ).onParsingException( fullPath, SimpleXMLHandlerDelegate.ExceptionSeverity.FATAL_ERROR, ex );
    }
    
    private void release()
    {
        if ( this.simpleHandler != null )
            this.simpleHandler.setAdapter( null );
        
        this.simpleHandler = null;
        this.simpleHandlerStack.clear();
    }
    
    @Override
    public void endDocument() throws SAXException
    {
        ( (SimpleXMLHandler)simpleHandler ).onDocumentEnded();
        
        release();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        
        release();
    }
    
    public XMLHandlerAdapter( SimpleXMLHandler simpleHandler )
    {
        if ( simpleHandler == null )
            throw new IllegalArgumentException( "simpleHandler must not be null" );
        
        this.simpleHandler = simpleHandler;
        this.simpleHandlerStack.push( this.simpleHandler );
        this.simpleHandler.setAdapter( this );
        
        this.path = new XMLPath();
        this.pathStack.add( path );
    }
}
