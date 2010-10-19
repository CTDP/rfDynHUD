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

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class XMLHandlerAdapter extends org.xml.sax.helpers.DefaultHandler
{
    private final ArrayList<XMLPath> pathStack = new ArrayList<XMLPath>();
    private XMLPath path = null;
    private final Stack<SimpleXMLHandlerFork> simpleHandlerStack = new Stack<SimpleXMLHandlerFork>();
    private SimpleXMLHandlerFork simpleHandler = null;
    private Object userObject = null;
    
    private boolean forkAllowed = false;
    private Attributes currAttribs = null;
    
    final void fork( SimpleXMLHandlerFork fork ) throws SAXException
    {
        if ( fork == null )
            throw new IllegalArgumentException( "fork must not be null." );
        
        if ( !forkAllowed )
            throw new IllegalStateException( "fork() can only be called from the onElementStarted() event." );
        
        fork.handleForkElement( path.getLastPathElement(), path.getLastPathObject(), currAttribs );
        
        simpleHandler.setAdapter( null );
        simpleHandlerStack.push( fork );
        simpleHandler = fork;
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
    
    private void unfork()
    {
        simpleHandler.setAdapter( null );
        simpleHandlerStack.pop();
        simpleHandler = simpleHandlerStack.peek();
        simpleHandler.setAdapter( this );
        
        path = pathStack.get( simpleHandlerStack.size() - 1 );
    }
    
    public void setSimpleHandler( SimpleXMLHandlerFork simpleHandler )
    {
        if ( simpleHandler == null )
            throw new IllegalArgumentException( "simpleHandler must not be null." );
        
        if ( simpleHandler == this.simpleHandler )
            return;
        
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
        SimpleXMLHandlerFork handler = this.simpleHandler;
        XMLPath path = this.path;
        forkAllowed = true;
        
        Object element = handler.getPathObject( path.getLevel(), qName );
        if ( element == null )
            throw new RuntimeException( "The getPathObject() method must never return null." );
        
        currAttribs = attributes;
        handler.onElementStarted( path, qName, attributes );
        currAttribs = null;
        
        path.pushPath( qName, element );
        
        forkAllowed = false;
    }
    
    @Override
    public void characters( char[] data, int start, int length ) throws SAXException
    {
        simpleHandler.onElementData( path, data, start, length );
    }
    
    @Override
    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        if ( path.getLevel() == 0 )
            unfork();
        
        path.popPath();
        
        simpleHandler.onElementEnded( path, qName );
    }
    
    @Override
    public void warning( SAXParseException ex ) throws SAXException
    {
        simpleHandler.onParsingException( path, SimpleXMLHandlerFork.ExceptionSeverity.WARNING, ex );
    }
    
    @Override
    public void error( SAXParseException ex ) throws SAXException
    {
        simpleHandler.onParsingException( path, SimpleXMLHandlerFork.ExceptionSeverity.ERROR, ex );
    }
    
    @Override
    public void fatalError( SAXParseException ex ) throws SAXException
    {
        simpleHandler.onParsingException( path, SimpleXMLHandlerFork.ExceptionSeverity.FATAL_ERROR, ex );
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
