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
package org.jagatoo.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class easily handles XML parsing.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public abstract class SimpleXMLHandlerDelegate
{
    public static enum ExceptionSeverity
    {
        WARNING,
        ERROR,
        FATAL_ERROR,
        ;
    }
    
    private XMLHandlerAdapter adapter = null;
    
    void setAdapter( XMLHandlerAdapter adapter )
    {
        this.adapter = adapter;
    }
    
    public final XMLHandlerAdapter getAdapter()
    {
        return ( adapter );
    }
    
    /**
     * Start a delegate handler.
     * 
     * @param delegate
     * 
     * @throws SAXException
     */
    protected final void delegate( SimpleXMLHandlerDelegate delegate ) throws SAXException
    {
        adapter.delegate( delegate );
    }
    
    /**
     * Invoked first when this object is used to start a fork.
     * 
     * @param name
     * @param object
     * @param attributes
     * 
     * @throws SAXException
     */
    public abstract void handleForkElement( String name, Object object, Attributes attributes ) throws SAXException;
    
    /**
     * Converts the passed element to a path element. By default the passed element is returned.
     * 
     * @param path then current XML element path
     * @param element the element's name
     * 
     * @return the converted path element.
     */
    protected Object getPathObject( XMLPath path, String element )
    {
        return ( element );
    }
    
    /**
     * Invoked when an XML element has been detected.
     * 
     * @param path then current XML element path
     * @param name the element's name
     * @param object the path object
     * @param attributes the attributes
     * 
     * @throws SAXException
     */
    protected abstract void onElementStarted( XMLPath path, String name, Object object, Attributes attributes ) throws SAXException;
    
    /**
     * Invoked when an XML element's character data is available.
     * 
     * @param path then current XML element path
     * @param attributes the attributes
     * @param data the characters
     * @param start the start position in the character array
     * @param length the number of characters to use from the character array
     * 
     * @throws SAXException
     */
    protected abstract void onElementData( XMLPath path, Attributes attributes, char[] data, int start, int length ) throws SAXException;
    
    /**
     * Invoked when an XML element end has been detected.
     * 
     * @param path then current XML element path
     * @param name the element's name
     * @param object the path object
     * 
     * @throws SAXException
     */
    protected abstract void onElementEnded( XMLPath path, String name, Object object ) throws SAXException;
    
    /**
     * Invoked when a parsing exception occurred.
     * 
     * @param path then current XML element path
     * @param severity the exception severity
     * @param ex the exception
     * 
     * @throws SAXException
     */
    protected void onParsingException( XMLPath path, ExceptionSeverity severity, SAXParseException ex ) throws SAXException
    {
    }
}
