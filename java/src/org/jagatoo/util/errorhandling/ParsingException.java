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
package org.jagatoo.util.errorhandling;

/**
 * Exception used to indicate that the loader encountered
 * a problem parsing the specified file.
 * 
 * @author Amos Wenger (aka BlueSky)
 */
public class ParsingException extends RuntimeException
{
    private static final long serialVersionUID = 8739835886126935739L;
    
    private final long lineNo;
    
    public final long getLineNo()
    {
        return ( lineNo );
    }
    
    public ParsingException( long lineNo )
    {
        super();
        
        this.lineNo = lineNo;
    }
    
    public ParsingException( long lineNo, String s )
    {
        super( s );
        
        this.lineNo = lineNo;
    }
    
    public ParsingException( long lineNo, Throwable cause )
    {
        super( cause );
        
        this.lineNo = lineNo;
    }
    
    public ParsingException()
    {
        this( -1L );
    }
    
    public ParsingException( String s )
    {
        this( -1L, s );
    }
    
    public ParsingException( Throwable cause )
    {
        this( -1L, cause );
    }
}
