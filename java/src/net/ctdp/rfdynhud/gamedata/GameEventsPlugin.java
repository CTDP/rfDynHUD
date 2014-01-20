/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.gamedata;

import java.io.File;

import net.ctdp.rfdynhud.render.WidgetsManager;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.jagatoo.logging.Log;
import org.jagatoo.logging.LogChannel;
import org.jagatoo.logging.LogLevel;

/**
 * <p>
 * This interface defines the entry point for custom event listeners without having to
 * create an add a whole {@link Widget}.
 * </p>
 * 
 * <p>
 * Implementing classes must define a constructor taking a java.io.File, which is being passed the GameEventsPlugin's 'baseFolder'.
 * </p>
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class GameEventsPlugin
{
    public static final LogChannel LOG_CHANNEL = new LogChannel( "rfDynHUD-GameEventsPlugin" );
    
    private final String name;
    private final String logPrefix;
    private final File baseFolder;
    
    public final String getName()
    {
        return ( name );
    }
    
    public final File getBaseFolder()
    {
        return ( baseFolder );
    }
    
    public void log( LogLevel logLevel, Object... message )
    {
        if ( ( message != null ) && ( message.length > 0 ) )
        {
            if ( ( message.length == 1 ) && ( message[0] instanceof Throwable ) )
            {
                Log.println( LOG_CHANNEL, logLevel, message );
            }
            else
            {
                Object[] message2 = new Object[ message.length + 1 ];
                message2[0] = logPrefix;
                System.arraycopy( message, 0, message2, 1, message.length );
                Log.println( LOG_CHANNEL, logLevel, message2 );
            }
        }
    }
    
    public void logCS( LogLevel logLevel, Object... message )
    {
        if ( ( message != null ) && ( message.length > 0 ) )
        {
            if ( ( message.length == 1 ) && ( message[0] instanceof Throwable ) )
            {
                Log.printlnCS( LOG_CHANNEL, logLevel, message );
            }
            else
            {
                Object[] message2 = new Object[ message.length + 1 ];
                message2[0] = logPrefix;
                System.arraycopy( message, 0, message2, 1, message.length );
                Log.printlnCS( LOG_CHANNEL, logLevel, message2 );
            }
        }
    }
    
    public void log( Object... message )
    {
        if ( ( message != null ) && ( message.length > 0 ) )
        {
            if ( ( message.length == 1 ) && ( message[0] instanceof Throwable ) )
            {
                Log.println( LOG_CHANNEL, message );
            }
            else
            {
                Object[] message2 = new Object[ message.length + 1 ];
                message2[0] = logPrefix;
                System.arraycopy( message, 0, message2, 1, message.length );
                Log.println( LOG_CHANNEL, message2 );
            }
        }
    }
    
    public void logCS( Object... message )
    {
        if ( ( message != null ) && ( message.length > 0 ) )
        {
            if ( ( message.length == 1 ) && ( message[0] instanceof Throwable ) )
            {
                Log.printlnCS( LOG_CHANNEL, message );
            }
            else
            {
                Object[] message2 = new Object[ message.length + 1 ];
                message2[0] = logPrefix;
                System.arraycopy( message, 0, message2, 1, message.length );
                Log.printlnCS( LOG_CHANNEL, message2 );
            }
        }
    }
    
    /**
     * Logs data to the plugin's log file.
     * 
     * @param message the data to log
     */
    public final void debug( Object... message )
    {
        if ( ( message != null ) && ( message.length > 0 ) )
        {
            if ( ( message.length == 1 ) && ( message[0] instanceof Throwable ) )
            {
                Log.debug( LOG_CHANNEL, message );
            }
            else
            {
                Object[] message2 = new Object[ message.length + 1 ];
                message2[0] = logPrefix;
                System.arraycopy( message, 0, message2, 1, message.length );
                Log.debug( LOG_CHANNEL, message2 );
            }
        }
    }
    
    /**
     * Logs data to the plugin's log file.
     * 
     * @param message the data to log
     */
    public final void debugCS( Object... message )
    {
        if ( ( message != null ) && ( message.length > 0 ) )
        {
            if ( ( message.length == 1 ) && ( message[0] instanceof Throwable ) )
            {
                Log.debugCS( LOG_CHANNEL, message );
            }
            else
            {
                Object[] message2 = new Object[ message.length + 1 ];
                message2[0] = logPrefix;
                System.arraycopy( message, 0, message2, 1, message.length );
                Log.debugCS( LOG_CHANNEL, message2 );
            }
        }
    }
    
    public abstract void onPluginStarted( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager );
    
    public abstract void onPluginShutdown( GameEventsManager eventsManager, LiveGameData gameData, boolean isEditorMode, WidgetsManager widgetsManager );
    
    protected GameEventsPlugin( String name, File baseFolder )
    {
        this.name = name;
        this.logPrefix = "[" + name + "] ";
        this.baseFolder = baseFolder;
    }
}
