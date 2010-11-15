/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.util;

import java.io.File;
import java.io.FileNotFoundException;

import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;

import org.jagatoo.logging.ConsoleLog;
import org.jagatoo.logging.FileLog;
import org.jagatoo.logging.Log;
import org.jagatoo.logging.LogChannel;
import org.jagatoo.logging.LogHandler;
import org.jagatoo.logging.LogLevel;
import org.jagatoo.logging.LogManager;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * The RFDHLog is a simple shortcut implementation for the {@link Log} class.
 * Please only use this for rfDynHUD internal logging. For any other case
 * use the Log class itself or write a similar implementation like this.
 * 
 * @see Log
 * 
 * @author Marvin Froehlich (CTDP)
 */
public final class RFDHLog
{
    private static final LogManager logMgr = LogManager.getInstance();
    
    private static final LogHandler createLogHandler( File file )
    {
        String logLevel_ = AbstractIniParser.parseIniValue( new File( __UtilHelper.PLUGIN_FOLDER, PluginINI.FILENAME ), "GENERAL", "logLevel", "EXHAUSTIVE" );
        
        LogLevel logLevel = LogLevel.EXHAUSTIVE;
        
        try
        {
            logLevel = LogLevel.valueOf( logLevel_ );
        }
        catch ( Throwable t )
        {
        }
        
        if ( !ResourceManager.isJarMode() || !FOLDER.exists() )
            return ( new ConsoleLog( LogLevel.PROFILE ) );
        
        try
        {
            return ( new FileLog( LogChannel.MASK_ALL, logLevel, file, true, true, true ) );
        }
        catch ( FileNotFoundException e )
        {
            return ( null );
        }
    }
    
    private static final File FOLDER = __UtilHelper.LOG_FOLDER;
    private static final String CORE_BASE_NAME = "rfdynhud";
    private static final String EDITOR_BASE_NAME = "rfdynhud_editor";
    private static File FILE = new File( FOLDER, __EDPrivilegedAccess.isEditorMode ? EDITOR_BASE_NAME + ".log" : CORE_BASE_NAME + ".log" ).getAbsoluteFile();
    private static LogHandler LOG = createLogHandler( FILE );
    
    static
    {
        logMgr.registerLog( LOG );
    }
    
    public static final LogChannel LOG_CHANNEL = new LogChannel( "rfDynHUD" );
    
    /**
     * @return the LogManager to register/deregister {@link LogHandler}s.
     */
    public static final LogManager getLogManager()
    {
        return ( logMgr );
    }
    
    /**
     * Sets the String to be prefixed to the actualy logging output n times.
     * 
     * @param indentationString
     */
    public static final void setIndentationString( String indentationString )
    {
        logMgr.setIndentationString( indentationString );
    }
    
    /**
     * @return the String to be prefixed to the actualy logging output n times.
     */
    public static final String getIndentationString()
    {
        return ( logMgr.getIndentationString() );
    }
    
    /**
     * Sets the indentation level to use for the following log outputs.
     * 
     * @param indentation
     */
    public static final void setIndentation( int indentation )
    {
        logMgr.setIndentation( indentation );
    }
    
    /**
     * @return the indentation level to use for the following log outputs.
     */
    public static final int getIndentation()
    {
        return ( logMgr.getIndentation() );
    }
    
    /**
     * Increases the indentation level to use for the following log outputs by one.
     */
    public static final void increaseIndentation()
    {
        logMgr.setIndentation( logMgr.getIndentation() + 1 );
    }
    
    /**
     * Decreases the indentation level to use for the following log outputs by one.
     */
    public static final void decreaseIndentation()
    {
        logMgr.setIndentation( logMgr.getIndentation() - 1 );
    }
    
    /**
     * Prints out a log message without a newline.
     * 
     * @param logLevel
     * @param message
     */
    public static final void print( LogLevel logLevel, Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, logLevel.level ) )
            logMgr.print( LOG_CHANNEL, logLevel.level, message );
    }
    
    /**
     * Prints out a log message without a newline.
     * 
     * @param logLevel
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void printCS( LogLevel logLevel, Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, logLevel.level ) )
            logMgr.printCS( LOG_CHANNEL, logLevel.level, message );
    }
    
    /**
     * This is an alias for print( REGULAR, message ).
     * 
     * @param message
     */
    public static final void print( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.REGULAR.level ) )
            logMgr.print( LOG_CHANNEL, LogLevel.REGULAR.level, message );
    }
    
    /**
     * This is an alias for print( REGULAR, message ).
     * 
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void printCS( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.REGULAR.level ) )
            logMgr.printCS( LOG_CHANNEL, LogLevel.REGULAR.level, message );
    }
    
    /**
     * Prints out a log message with a newline.
     * 
     * @param logLevel
     * @param message
     */
    public static final void println( LogLevel logLevel, Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, logLevel.level ) )
            logMgr.println( LOG_CHANNEL, logLevel.level, message );
    }
    
    /**
     * Prints out a log message with a newline.
     * 
     * @param logLevel
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void printlnCS( LogLevel logLevel, Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, logLevel.level ) )
            logMgr.printlnCS( LOG_CHANNEL, logLevel.level, message );
    }
    
    /**
     * This is an alias for println( REGULAR, message ).
     * 
     * @param message
     */
    public static final void println( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.REGULAR.level ) )
            logMgr.println( LOG_CHANNEL, LogLevel.REGULAR.level, message );
    }
    
    /**
     * This is an alias for println( REGULAR, message ).
     * 
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void printlnCS( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.REGULAR.level ) )
            logMgr.printlnCS( LOG_CHANNEL, LogLevel.REGULAR.level, message );
    }
    
    /**
     * This is an alias for println( EXHAUSTIVE, message ).
     * 
     * @param message
     */
    public static final void printlnEx( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.EXHAUSTIVE.level ) )
            logMgr.println( LOG_CHANNEL, LogLevel.EXHAUSTIVE.level, message );
    }
    
    /**
     * This is an alias for println( EXHAUSTIVE, message ).
     * 
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void printlnExCS( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.EXHAUSTIVE.level ) )
            logMgr.printlnCS( LOG_CHANNEL, LogLevel.EXHAUSTIVE.level, message );
    }
    
    /**
     * This is an alias for println( ERROR, message ).
     * 
     * @param message
     */
    public static final void error( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.ERROR.level ) )
            logMgr.println( LOG_CHANNEL, LogLevel.ERROR.level, message );
    }
    
    /**
     * This is an alias for println( ERROR, message ).
     * 
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void errorCS( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.ERROR.level ) )
            logMgr.printlnCS( LOG_CHANNEL, LogLevel.ERROR.level, message );
    }
    
    /**
     * This is an alias for println( EXCEPTION, message ).
     * 
     * @param message
     */
    public static final void exception( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.EXCEPTION.level ) )
            logMgr.println( LOG_CHANNEL, LogLevel.EXCEPTION.level, message );
    }
    
    /**
     * This is an alias for println( EXCEPTION, message ).
     * 
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void exceptionCS( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.EXCEPTION.level ) )
            logMgr.printlnCS( LOG_CHANNEL, LogLevel.EXCEPTION.level, message );
    }
    
    /**
     * This is an alias for println( DEBUG, message ).
     * 
     * @param message
     */
    public static final void debug( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.DEBUG.level ) )
            logMgr.println( LOG_CHANNEL, LogLevel.DEBUG.level, message );
    }
    
    /**
     * This is an alias for println( DEBUG, message ).
     * 
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void debugCS( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.DEBUG.level ) )
            logMgr.printlnCS( LOG_CHANNEL, LogLevel.DEBUG.level, message );
    }
    
    /**
     * This is an alias for println( PROFILE, message ).
     * 
     * @param message
     */
    public static final void profile( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.PROFILE.level ) )
            logMgr.println( LOG_CHANNEL, LogLevel.PROFILE.level, message );
    }
    
    /**
     * This is an alias for println( PROFILE, message ).
     * 
     * @param message the string message to be printed to the log (comma separated)
     */
    public static final void profileCS( Object... message )
    {
        if ( logMgr.isAnyLogInterfaceRegistered( LOG_CHANNEL, LogLevel.PROFILE.level ) )
            logMgr.printlnCS( LOG_CHANNEL, LogLevel.PROFILE.level, message );
    }
    
    public static final void flush()
    {
        Log.flush();
    }
    
    public static final void close()
    {
        Log.close();
    }
    
    private RFDHLog()
    {
    }
}
