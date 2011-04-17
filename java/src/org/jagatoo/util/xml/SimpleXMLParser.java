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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jagatoo.util.io.UnicodeBOM;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>
 * This class invokes simple XML parsing.
 * </p>
 * 
 * <p>
 * This not only provides full XML path information at every state of the parsing process,
 * but also gives you the opportunity to fork the entity handling and delegate it to another handler.
 * The fork is automatically undone when the root of the fork is detected.
 * </p>
 * 
 * <p>
 * An example:<br />
 * Implement SimpleXMLHandlerFork and call the instance f.<br />
 * Implement SimpleXMLHandler and call the instance h.<br />
 * Invoke the parser and pass it h.<br />
 * In the h implementation you would then detect a certain element start and invoke the handler's fork() method and pass f.<br />
 * All the elements inside of this element are now handled by f which doesn't need to know anything about the parent elements
 * and even gets an XMLPath, that roots to the forking element.
 * </p>
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class SimpleXMLParser
{
    private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
    
    /**
     * Parses the given file.<br>
     * This method implements the actual parsing code.
     * 
     * @param in
     * @param codepage
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected static void parseXMLImpl( InputStream in, String codepage, Charset charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        if ( !in.markSupported() )
            in = new BufferedInputStream( in );
        
        UnicodeBOM bom = UnicodeBOM.skipBOM( in );
        
        if ( ( bom != null ) && ( bom.getCharset() != null ) )
            charset = bom.getCharset();
        else if ( codepage != null )
            charset = Charset.forName( codepage );
        
        BufferedReader reader = ( charset == null ) ? new BufferedReader( new InputStreamReader( in ) ) : new BufferedReader( new InputStreamReader( in, charset ) );
        
        XMLHandlerAdapter adapter = new XMLHandlerAdapter( handler );
        
        SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
        
        saxParser.parse( new InputSource( reader ), adapter );
        
        reader.close();
    }
    
    /**
     * Parses the given file.
     * 
     * @param in
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( InputStream in, String charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( in, charset, null, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param in
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( InputStream in, Charset charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( in, null, charset, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param in
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( InputStream in, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( in, null, null, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param url
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( URL url, String charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( url.openStream(), charset, null, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param url
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( URL url, Charset charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( url.openStream(), null, charset, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param url
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( URL url, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( url.openStream(), null, null, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( File file, String charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( new FileInputStream( file ), charset, null, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( File file, Charset charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( new FileInputStream( file ), null, charset, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param file
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( File file, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( new FileInputStream( file ), null, null, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param filename
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( String filename, String charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( new FileInputStream( filename ), charset, null, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param filename
     * @param charset
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( String filename, Charset charset, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( new FileInputStream( filename ), null, charset, handler );
    }
    
    /**
     * Parses the given file.
     * 
     * @param filename
     * @param handler
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static final void parseXML( String filename, SimpleXMLHandler handler ) throws IOException, ParserConfigurationException, SAXException
    {
        parseXMLImpl( new FileInputStream( filename ), null, null, handler );
    }
}
