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
/**
 * :Id: LogType.java,v 1.6 2003/02/24 00:13:53 wurp Exp $
 * 
 * :Log: LogType.java,v $
 * Revision 1.6  2003/02/24 00:13:53  wurp
 * Formatted all java code for cvs (strictSunConvention.xml)
 * 
 * Revision 1.5  2002/09/23 00:48:39  dilvish
 * New profiling, spatial trees
 * 
 * Revision 1.4  2001/06/20 04:05:42  wurp
 * added log4j.
 * 
 * Revision 1.3  2001/01/28 07:52:20  wurp
 * Removed <dollar> from Id and Log in log comments.
 * Added several new commands to AdminApp
 * Unfortunately, several other changes that I have lost track of.  Try diffing this
 * version with the previous one.
 * 
 * Revision 1.2  2000/12/16 22:07:33  wurp
 * Added Id and Log to almost all of the files that didn't have it.  It's
 * possible that the script screwed something up.  I did a commit and an update
 * right before I ran the script, so if a file is screwed up you should be able
 * to fix it by just going to the version before this one.
 */
package org.jagatoo.logging;

/**
 * Use these constants as parameter for the Log methods.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public enum LogLevel
{
    /**
     * a serious error has been detected
     */
    ERROR( -2 ),
    
    /**
     * an unexcepted exception occured
     */
    EXCEPTION( -1 ),
    
    /**
     * regular logging output
     */
    REGULAR( 0 ),
    
    /**
     * everything goes here, this is expensive
     */
    EXHAUSTIVE( 1 ),
    
    /**
     * debugging information, usually turned off
     */
    DEBUG( 2 ),
    
    /**
     * debugging information, usually turned off
     */
    PROFILE( 3 ),
    ;
    
    public final byte level;
    
    public static final LogLevel MINIMUM = ERROR;
    
    public final boolean isError()
    {
        return ( ( this == ERROR ) || ( this == EXCEPTION ) );
    }
    
    public static final boolean isError( int logLevel )
    {
        return ( ( logLevel == ERROR.level ) || ( logLevel == EXCEPTION.level ) );
    }
    
    public final int compareLevel( LogLevel logLevel )
    {
        return ( this.level - logLevel.level );
    }
    
    public final boolean isSmallerOrEqual( LogLevel logLevel )
    {
        return ( this.level <= logLevel.level );
    }
    
    private LogLevel( int level )
    {
        this.level = (byte)level;
    }
}
