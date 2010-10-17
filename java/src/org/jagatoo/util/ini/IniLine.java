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
package org.jagatoo.util.ini;

/**
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class IniLine
{
    private int lineNr = -1;
    private String line = "";
    private String currentGroup = null;
    private String key = null;
    private String value = null;
    private String comment = null;
    private Boolean isValid = null;
    
    void reset()
    {
        lineNr = -1;
        line = "";
        currentGroup = null;
        key = null;
        value = null;
        comment = null;
        isValid = null;
    }
    
    void setLine( int lineNr, String currentGroup, String line )
    {
        this.lineNr = lineNr;
        this.currentGroup= currentGroup;
        this.line = ( line == null ) ? line : line.trim();
    }
    
    public final int getLineNr()
    {
        return ( lineNr );
    }
    
    public final String getLine()
    {
        return ( line );
    }
    
    public final boolean isEmpty()
    {
        return ( ( line == null ) || ( line.length() == 0 ) );
    }
    
    String setCurrentGroup( String group )
    {
        this.currentGroup = group;
        
        return ( this.currentGroup );
    }
    
    public final String getCurrentGroup()
    {
        return ( currentGroup );
    }
    
    String setKey( String key )
    {
        this.key = key;
        
        return ( this.key );
    }
    
    public final String getKey()
    {
        return ( key );
    }
    
    String setValue( String value )
    {
        this.value = value;
        
        return ( this.value );
    }
    
    public final String getValue()
    {
        return ( value );
    }
    
    String setComment( String comment )
    {
        this.comment = comment;
        
        return ( this.comment );
    }
    
    public final String getComment()
    {
        return ( comment );
    }
    
    Boolean setValid( Boolean valid )
    {
        this.isValid = valid;
        
        return ( this.isValid );
    }
    
    public final Boolean isValid()
    {
        return ( isValid );
    }
}
