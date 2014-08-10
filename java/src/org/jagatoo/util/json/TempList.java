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

/**
 * This class simply encapsulates an array lie an ArryList, but provides access to the array.
 * 
 * @author Marvin Froehlich
 */
public class TempList
{
    private final JSONParser jsonParser;
    
    private Object[] array = null;
    private int size = 0;
    
    public final boolean hasArray()
    {
        return ( array != null );
    }
    
    public void add( Object o, ParseMeta meta, String arrayName )
    {
        if ( array != null )
        {
            if ( size == array.length )
            {
                Object[] tmp = jsonParser.newArray( meta, arrayName, (int)( size * 1.5 ) + 1 );
                System.arraycopy( array, 0, tmp, 0, array.length );
                this.array = tmp;
            }
            
            array[size++] = o;
        }
    }
    
    public final int size()
    {
        return ( size );
    }
    
    public final Object get( int index )
    {
        if ( ( index < 0 ) || ( index >= size ) )
            throw new ArrayIndexOutOfBoundsException( "index out of bounds [0, " + ( size - 1 ) + "]" );
                        
        if ( array == null )
            return ( null );
        
        return ( array[index] );
    }
    
    public Object[] getArray( ParseMeta meta, String arrayName )
    {
        if ( array == null )
            return ( null );
        
        if ( array.length == size )
            return ( array );
        
        Object[] result = jsonParser.newArray( meta, arrayName, size );
        System.arraycopy( array, 0, result, 0, size );
        
        return ( result );
    }
    
    TempList( JSONParser jsonParser, ParseMeta meta, String arrayName )
    {
        this.jsonParser = jsonParser;
        
        this.array = jsonParser.newArray( meta, arrayName, 16 );
    }
}
