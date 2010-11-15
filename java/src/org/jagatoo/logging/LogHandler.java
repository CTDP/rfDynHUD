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
/**
 * :Id: LogInterface.java,v 1.5 2003/02/24 00:13:53 wurp Exp $
 * 
 * :Log: LogInterface.java,v $
 * Revision 1.5  2003/02/24 00:13:53  wurp
 * Formatted all java code for cvs (strictSunConvention.xml)
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
 * @author David Yazel
 * @author Marvin Froehlich (aka Qudus)
 */
public abstract class LogHandler
{
    private LogLevel logLevel;
    private int logLevelLevel;
    private int channelFilter;
    
    public void setLogLevel( int logLevel )
    {
        this.logLevel = null;
        
        for ( LogLevel ll : LogLevel.values() )
        {
            if ( ll.level == logLevel )
            {
                this.logLevel = ll;
                break;
            }
        }
        
        this.logLevelLevel = logLevel;
    }
    
    public final void setLogLevel( LogLevel logLevel )
    {
        setLogLevel( logLevel.level );
    }
    
    public final LogLevel getLogLevel()
    {
        return ( logLevel );
    }
    
    public final int getLogLevelLevel()
    {
        return ( logLevelLevel );
    }
    
    public void setChannelFilter( int filter )
    {
        this.channelFilter = filter;
    }
    
    public final int getChannelFilter()
    {
        return ( channelFilter );
    }
    
    public final boolean acceptsChannel( LogChannel channel )
    {
        return ( channel.isInFilter( channelFilter ) );
    }
    
    public boolean acceptsChannelAndLevel( LogChannel channel, int logLevel )
    {
        return ( channel.isInFilter( channelFilter ) && ( logLevel <= this.logLevelLevel ) );
    }
    
    public abstract void print( LogChannel channel, int logLevel, String message );
    
    public abstract void println( LogChannel channel, int logLevel, String message );
    
    public abstract void print( LogChannel channel, int logLevel, Throwable t );
    
    public abstract void endMessage();
    
    public abstract void flush();
    
    public abstract void close();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize()
    {
        close();
    }
    
    public LogHandler( int channelFilter, LogLevel logLevel )
    {
        this.channelFilter = channelFilter;
        
        setLogLevel( logLevel );
    }
    
    public LogHandler( int channelFilter, int logLevel )
    {
        this.channelFilter = channelFilter;
        
        setLogLevel( logLevel );
    }
}
