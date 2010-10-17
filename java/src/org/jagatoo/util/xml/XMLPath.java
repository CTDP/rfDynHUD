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

/**
 * Keeps the current XML parsing path.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class XMLPath
{
    public static final int DEFAULT_INITIAL_SIZE = 16;
    
    private String[] path;
    private Object[] path2;
    private int level = 0;
    
    void pushPath( String element, Object object )
    {
        if ( path.length <= level )
        {
            String[] tmp = new String[ path.length * 150 / 100 + 1 ];
            System.arraycopy( path, 0, tmp, 0, path.length );
            path = tmp;
        }
        
        if ( path2.length <= level )
        {
            Object[] tmp = new Object[ path2.length * 150 / 100 + 1 ];
            System.arraycopy( path2, 0, tmp, 0, path2.length );
            path2 = tmp;
        }
        
        path[level] = element;
        path2[level] = object;
        level++;
    }
    
    void popPath()
    {
        level--;
        path[level] = null;
        path2[level] = null;
    }
    
    void reset()
    {
        for ( int i = 0; i < level; i++ )
        {
            path[i] = null;
            path2[i] = null;
        }
        
        level = 0;
    }
    
    /**
     * Gets the path element by level.
     * 
     * @param level
     * 
     * @return the path element by level.
     */
    public final Object getElement( int level )
    {
        return ( path[level] );
    }
    
    /**
     * Gets the path element by level.
     * 
     * @param level
     * 
     * @return the path element by level.
     */
    public final Object getObject( int level )
    {
        return ( path2[level] );
    }
    
    /**
     * Gets the path element last in the current order.
     * 
     * @return the path element last in the current order.
     */
    public final String getLastPathElement()
    {
        if ( level == 0 )
            return ( null );
        
        return ( path[level - 1] );
    }
    
    /**
     * Gets the path element last in the current order.
     * 
     * @return the path element last in the current order.
     */
    public final Object getLastPathObject()
    {
        if ( level == 0 )
            return ( null );
        
        return ( path2[level - 1] );
    }
    
    /**
     * Gets the current hierarchy level. 0 is document root, 1 is inside a root element.
     * 
     * @return the current hierarchy level.
     */
    public final int getLevel()
    {
        return ( level );
    }
    
    XMLPath( int initialSize )
    {
        this.path = new String[ initialSize ];
        this.path2 = new Object[ initialSize ];
    }
    
    XMLPath()
    {
        this( DEFAULT_INITIAL_SIZE );
    }
}
