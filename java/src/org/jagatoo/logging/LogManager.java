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
 * :Id: LogManager.java,v 1.9 2003/02/24 00:13:53 wurp Exp $
 * 
 * :Log: LogManager.java,v $
 * Revision 1.9  2003/02/24 00:13:53  wurp
 * Formatted all java code for cvs (strictSunConvention.xml)
 * 
 * Revision 1.8  2002/02/12 02:22:27  dilvish
 * Bunch of bug fixes
 * 
 * Revision 1.7  2002/01/15 03:39:00  dilvish
 * Added the ability to dump exception stacks to log
 * 
 * Revision 1.6  2001/06/20 04:05:42  wurp
 * added log4j.
 * 
 * Revision 1.5  2001/04/04 01:06:29  wizofid
 * New framerate window, new animation system
 * 
 * Revision 1.4  2001/03/13 01:43:34  wizofid
 * Added the portal
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

import java.util.ArrayList;
import java.util.HashSet;

/**
 * This object manages multiple logs.  This provides a single point
 * at which the application can write to logs, but allows log control
 * to be handled centrally.  Multiple LogInterface objects are registered
 * with the logger.  The LogManager will step through them when a logging
 * message comes in and give each LogInterface an opportunity to consume
 * the log message.
 * 
 * @author David Yazel
 * @author Marvin Froehlich (aka Qudus)
 */
public class LogManager
{
    private final ArrayList<LogHandler> logs;
    private int maxRegisteredLogLevel = -Integer.MAX_VALUE;
    private int registeredChannels = 0;
    private long startTime;
    private boolean timestampsEnabled = false;
    private boolean channelsVisible = false;
    private boolean lastNewLine = true;
    private final HashSet<String> debugPackageFilter = new HashSet<String>();
    
    private String indentationString = "    ";
    private int indentation = 0;
    
    private static LogManager instance = null;
    
    /**
     * Sets the String to be prefixed to the actualy logging output n times.
     * 
     * @param indentationString
     */
    public final void setIndentationString( String indentationString )
    {
        this.indentationString = indentationString;
    }
    
    /**
     * @return the String to be prefixed to the actualy logging output n times.
     */
    public final String getIndentationString()
    {
        return ( indentationString );
    }
    
    /**
     * Sets the indentation level to use for the following log outputs.
     * 
     * @param indentation
     */
    public final void setIndentation( int indentation )
    {
        this.indentation = Math.max( 0, indentation );
    }
    
    /**
     * @return the indentation level to use for the following log outputs.
     */
    public final int getIndentation()
    {
        return ( indentation );
    }
    
    public final void setTimestampingEnabled( boolean enabled )
    {
        this.timestampsEnabled = enabled;
    }
    
    public final boolean isTimestampingEnabled()
    {
        return ( timestampsEnabled );
    }
    
    public final void setChannelsVisible( boolean visible )
    {
        this.channelsVisible = visible;
    }
    
    public final boolean areChannelsVisible()
    {
        return ( channelsVisible );
    }
    
    public final void addDebuggingPackage( String pkg )
    {
        debugPackageFilter.add( pkg );
    }
    
    public final void removeDebuggingPackage( String pkg )
    {
        debugPackageFilter.remove( pkg );
    }
    
    public final HashSet<String> getDebuggingPackageFiler()
    {
        return ( debugPackageFilter );
    }
    
    private String getTimeString()
    {
        final long delta = System.currentTimeMillis() - startTime;
        
        return ( LogFormatter.formatTime( delta ) );
    }
    
    private static String getMemory()
    {
        Runtime runtime = Runtime.getRuntime();
        final long mem = runtime.totalMemory();
        final long free = runtime.freeMemory();
        
        return ( LogFormatter.formatMemory( mem - free ) + "/" + LogFormatter.formatMemory( mem ) );
    }
    
    /**
     * Must be called, if the logLevel of a registeredLogInterface has been changed.
     */
    public final void refreshLogInterfaces()
    {
        maxRegisteredLogLevel = -Integer.MAX_VALUE;
        registeredChannels = 0;
        
        for ( int i = 0; i < logs.size(); i++ )
        {
            final LogHandler log = logs.get( i );
            
            //if ( log.getLogLevel().compareLevel( maxRegisteredLogLevel ) > 0 )
            if ( log.getLogLevel().level > maxRegisteredLogLevel )
                maxRegisteredLogLevel = log.getLogLevel().level;
            registeredChannels |= log.getChannelFilter();
        }
    }
    
    /**
     * This method allows you to register a class that implements the
     * LogInterface. Every log so registered will get a copy of every
     * log message, along with its mask.
     * 
     * @param log
     */
    public final void registerLog( LogHandler log )
    {
        logs.add( log );
        
        refreshLogInterfaces();
    }
    
    /**
     * This method allows you to deregister a class that implements the
     * LogInterface. Every log so deregistered won't get a copy of every
     * log message anymore.
     * 
     * @param log
     */
    public final void deregisterLog( LogHandler log )
    {
        logs.remove( log );
        
        refreshLogInterfaces();
    }
    
    public final boolean isAnyLogInterfaceRegistered( LogChannel channel, int logLevel )
    {
        if ( logs.size() == 0 )
            return ( false );
        
        if ( logLevel > maxRegisteredLogLevel )
            return ( false );
        
        if ( !channel.isInFilter( registeredChannels ) )
            return ( false );
        
        return ( true );
    }
    
    private static final String getCallerPackage()
    {
        final StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        
        String callerClass = null;
        for ( int i = 3; i < stes.length; i++ )
        {
            //if ( !stes[ i ].getClassName().startsWith( LogManager.class.getPackage().getName() ) )
            if ( !stes[ i ].getClassName().toLowerCase().contains( "log" ) )
            {
                callerClass = stes[ i ].getClassName();
                break;
            }
        }
        
        if ( ( callerClass == null ) || ( callerClass.length() == 0 ) )
            return ( "" );
        
        final int lastDot = callerClass.lastIndexOf( '.' );
        final String callerPackage;
        if ( lastDot >= 0 )
            callerPackage = callerClass.substring( 0, lastDot );
        else
            callerPackage = "";
        
        return ( callerPackage );
    }
    
    private final void printThrowable( LogChannel channel, int level, Throwable t, LogHandler log )
    {
        log.println( channel, level, t.getClass().getName() + ": " + t.getMessage() );
        
        StackTraceElement[] st = t.getStackTrace();
        
        for ( StackTraceElement ste : st )
        {
            log.println( channel, level, "  at " + ste.toString() );
        }
    }
    
    private synchronized final void internalPrint( LogChannel channel, int logLevel, Object[] message, boolean commaSeparated, boolean appendNL )
    {
        if ( ( message == null ) || ( message.length == 0 ) )
            return;
        
        // redundant!
        //if ( !isAnyLogInterfaceRegistered( channel, logLevel ) )
        //    return;
        
        String callerPackage = null;
        
        if ( !debugPackageFilter.isEmpty() && LogLevel.isError( logLevel ) )
        {
            callerPackage = getCallerPackage();
            if ( !debugPackageFilter.contains( callerPackage  ) )
                return;
        }
        
        String is = getIndentationString();
        
        for ( int i = 0; i < logs.size(); i++ )
        {
            final LogHandler log = logs.get( i );
            
            if ( log.acceptsChannelAndLevel( channel, logLevel ) )
            {
                try
                {
                    if ( !( message[0] instanceof Throwable ) )
                    {
                        for ( int j = 0; j < indentation; j++ )
                        {
                            log.print( channel, logLevel, is );
                        }
                        
                        if ( lastNewLine && areChannelsVisible() )
                        {
                            log.print( channel, logLevel, channel.getLogString() );
                            log.print( channel, logLevel, " " );
                        }
                        
                        if ( lastNewLine && isTimestampingEnabled() )
                        {
                            log.print( channel, logLevel, "[" );
                            log.print( channel, logLevel, getTimeString() );
                            log.print( channel, logLevel, ", " );
                            log.print( channel, logLevel, getMemory() );
                            log.print( channel, logLevel, "] " );
                        }
                    }
                    
                    for ( int j = 0; j < message.length; j++ )
                    {
                        if ( message[j] instanceof Throwable )
                        {
                            if ( !debugPackageFilter.isEmpty() )
                            {
                                if ( callerPackage == null )
                                    callerPackage = getCallerPackage();
                                
                                if ( debugPackageFilter.contains( callerPackage ) )
                                {
                                    int logLevel2 = ( message[j] instanceof Error ) ? LogLevel.ERROR.level : LogLevel.EXCEPTION.level;
                                    if ( logLevel2 <= log.getLogLevelLevel() )
                                        printThrowable( channel, logLevel2, (Throwable)message[j], log );
                                    
                                    lastNewLine = true;
                                }
                            }
                            else
                            {
                                int logLevel2 = ( message[j] instanceof Error ) ? LogLevel.ERROR.level : LogLevel.EXCEPTION.level;
                                if ( logLevel2 <= log.getLogLevelLevel() )
                                    printThrowable( channel, logLevel2, (Throwable)message[j], log );
                                
                                lastNewLine = true;
                            }
                        }
                        else if ( appendNL && ( j == message.length - 1 ) )
                        {
                            log.println( channel, logLevel, String.valueOf( message[ j ] ) );
                        }
                        else
                        {
                            log.print( channel, logLevel, String.valueOf( message[ j ] ) );
                        }
                        
                        if ( commaSeparated && ( j < message.length - 1 ) )
                        {
                            log.print( channel, logLevel, ", " );
                        }
                    }
                }
                finally
                {
                    log.endMessage();
                }
            }
        }
        
        this.lastNewLine = appendNL;
    }
    
    /**
     * This method will call all the log objects to store the message,
     * if they want to.
     * 
     * @param channel
     * @param logLevel the logLevel of this message
     * @param message the string message to be printed to the log
     */
    public final void print( LogChannel channel, int logLevel, Object[] message )
    {
        internalPrint( channel, logLevel, message, false, false );
    }
    
    /**
     * This method will call all the log objects to store the message,
     * if they want to.
     * 
     * @param channel
     * @param logLevel the logLevel of this message
     * @param message the string message to be printed to the log (comma separated)
     */
    public final void printCS( LogChannel channel, int logLevel, Object[] message )
    {
        internalPrint( channel, logLevel, message, true, false );
    }
    
    /**
     * This method will call all the log objects to store the message,
     * if they want to.
     * 
     * @param channel
     * @param logLevel the logLevel of this message
     * @param message the string message to be printed to the log
     */
    public final void println( LogChannel channel, int logLevel, Object[] message )
    {
        internalPrint( channel, logLevel, message, false, true );
    }
    
    /**
     * This method will call all the log objects to store the message,
     * if they want to.
     * 
     * @param channel
     * @param logLevel the logLevel of this message
     * @param message the string message to be printed to the log (comma separated)
     */
    public final void printlnCS( LogChannel channel, int logLevel, Object[] message )
    {
        internalPrint( channel, logLevel, message, true, true );
    }
    
    /**
     * Steps through the logs and flushes all of them. Necessary since they
     * could be implemented using files with buffers.
     */
    final void flush()
    {
        for ( int i = 0; i < logs.size(); i++ )
        {
            logs.get( i ).flush();
        }
    }
    
    /**
     * Steps through the logs and closes all of them. Necessary since they
     * could be implemented using files with buffers.
     */
    final void close()
    {
        for ( int i = 0; i < logs.size(); i++ )
        {
            logs.get( i ).close();
        }
    }
    
    private LogManager()
    {
        this.logs = new ArrayList<LogHandler>( 2 );
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * @return the LogManager's singleton instance.
     */
    public static final LogManager getInstance()
    {
        if ( instance == null )
            instance = new LogManager();
        
        return ( instance );
    }
}
