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
 * Keeps meta data during parsing.
 * 
 * @author Marvin Froehlich
 */
public class ParseMeta
{
    private boolean isRoot = true;
    private long lineNo = 1;
    private char lastChar = 0;
    private boolean lastCharConsumed = false;
    private final String[] nameStack;
    private final Object[] stack;
    private int stackSize = 0;
    
    void notRoot()
    {
        this.isRoot = false;
    }
    
    public final boolean isRoot()
    {
        return ( isRoot );
    }
    
    public void incLineNo()
    {
        this.lineNo++;
    }
    
    public final long getLineNo()
    {
        return ( lineNo );
    }
    
    public void setLastChar( char ch )
    {
        this.lastChar = ch;
        this.lastCharConsumed = false;
    }
    
    public final char getLastChar()
    {
        return ( lastChar );
    }
    
    public final char consumeLastChar()
    {
        if ( lastCharConsumed )
            return ( 0 );
        
        lastCharConsumed = true;
        
        return ( lastChar );
    }
    
    public final Object[] getStack()
    {
        return ( stack );
    }
    
    public final int getStackSize()
    {
        return ( stackSize );
    }
    
    public final String getNameStackObject( int index )
    {
        if ( ( index < 0 ) || ( index >= stackSize ) )
            throw new ArrayIndexOutOfBoundsException( index + " out of bounds [0, " + ( stackSize - 1 ) + "]" );
        
        return ( nameStack[index] );
    }
    
    public final Object getStackObject( int index )
    {
        if ( ( index < 0 ) || ( index >= stackSize ) )
            throw new ArrayIndexOutOfBoundsException( index + " out of bounds [0, " + ( stackSize - 1 ) + "]" );
        
        return ( stack[index] );
    }
    
    public final String getLastNameStackObject()
    {
        if ( stackSize <= 0 )
            throw new IllegalStateException( "There is not currently an object on the stack." );
        
        return ( nameStack[stackSize - 1] );
    }
    
    public final Object getLastStackObject()
    {
        if ( stackSize <= 0 )
            throw new IllegalStateException( "There is not currently an object on the stack." );
        
        return ( stack[stackSize - 1] );
    }
    
    public final int pushToStack( String name, Object object )
    {
        nameStack[stackSize] = name;
        stack[stackSize] = object;
        
        stackSize++;
        
        return ( stackSize - 1 );
    }
    
    public final Object popFromStack()
    {
        if ( stackSize <= 0 )
            throw new IllegalStateException( "There is not currently an object on the stack." );
        
        stackSize--;
        Object result = stack[stackSize];
        nameStack[stackSize] = null;
        stack[stackSize] = null;
        
        return ( result );
    }
    
    public ParseMeta( int stackSize )
    {
        this.nameStack = new String[ stackSize ];
        this.stack = new Object[ stackSize ];
    }
    
    public ParseMeta()
    {
        this( 64 );
    }
}
