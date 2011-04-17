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
 * :Id: FileLog.java,v 1.5 2003/02/24 00:13:53 wurp Exp $
 * 
 * :Log: FileLog.java,v $
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * This class implements the LogInterface and adds support for
 * writing logs to files. The filename is specified in the constructor.
 * 
 * @author David Yazel
 * @author Marvin Froehlich (aka Qudus)
 */
public class FileLog extends LogHandler
{
    private final File file;
    private PrintStream ps;
    private final boolean autoFlush;
    
    private void createPrintStream()
    {
        try
        {
            ps = new PrintStream( new FileOutputStream( file, true ), false );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsChannelAndLevel( LogChannel channel, int logLevel )
    {
        boolean accepts = super.acceptsChannelAndLevel( channel, logLevel );
        
        if ( accepts && ( file != null ) && ( ps == null ) )
        {
            createPrintStream();
        }
        
        return ( accepts );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void print( LogChannel channel, int logLevel, String message )
    {
        if ( ps == null )
            createPrintStream();
        
        ps.print( message );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void println( LogChannel channel, int logLevel, String message )
    {
        if ( ps == null )
            createPrintStream();
        
        ps.println( message );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void print( LogChannel channel, int logLevel, Throwable t )
    {
        if ( ps == null )
            createPrintStream();
        
        t.printStackTrace( ps );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endMessage()
    {
        if ( ps != null )
        {
            if ( file != null )
            {
                ps.close();
                ps = null;
            }
            else if ( autoFlush )
            {
                ps.flush();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void flush()
    {
        ps.flush();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
    {
        try
        {
            ps.close();
        }
        catch ( Throwable t )
        {
        }
    }
    
    public FileLog( int channelFilter, LogLevel logLevel, File file, boolean append, boolean autoFlush, boolean instantClose ) throws FileNotFoundException
    {
        super( channelFilter, logLevel );
        
        if ( instantClose )
        {
            this.file = file;
            this.ps = null;
        }
        else
        {
            this.file = null;
            this.ps = new PrintStream( new FileOutputStream( file, append ), false );
        }
        
        this.autoFlush = autoFlush;
    }
    
    public FileLog( LogLevel logLevel, File file, boolean append, boolean autoFlush, boolean instantClose ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, logLevel, file, append, autoFlush, instantClose );
    }
    
    public FileLog( File file, boolean append, boolean autoFlush, boolean instantClose ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, LogLevel.REGULAR, file, append, autoFlush, instantClose );
    }
    
    public FileLog( int channelFilter, LogLevel logLevel, File file ) throws FileNotFoundException
    {
        this( channelFilter, logLevel, file, false, true, false );
    }
    
    public FileLog( LogLevel logLevel, File file ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, logLevel, file, false, true, false );
    }
    
    public FileLog( File file ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, LogLevel.REGULAR, file, false, true, false );
    }
    
    public FileLog( int channelFilter, LogLevel logLevel, String filename, boolean append, boolean autoFlush, boolean instantClose ) throws FileNotFoundException
    {
        this( channelFilter, logLevel, new File( filename ), append, autoFlush, instantClose );
    }
    
    public FileLog( LogLevel logLevel, String filename, boolean append, boolean autoFlush, boolean instantClose ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, logLevel, new File( filename ), append, autoFlush, instantClose );
    }
    
    public FileLog( String filename, boolean append, boolean autoFlush, boolean instantClose ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, LogLevel.REGULAR, new File( filename ), append, autoFlush, instantClose );
    }
    
    public FileLog( int channelFilter, LogLevel logLevel, String filename ) throws FileNotFoundException
    {
        this( channelFilter, logLevel, new File( filename ), false, true, false );
    }
    
    public FileLog( LogLevel logLevel, String filename ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, logLevel, new File( filename ), false, true, false );
    }
    
    public FileLog( String filename ) throws FileNotFoundException
    {
        this( LogChannel.MASK_ALL, LogLevel.REGULAR, new File( filename ), false, true, false );
    }
}
