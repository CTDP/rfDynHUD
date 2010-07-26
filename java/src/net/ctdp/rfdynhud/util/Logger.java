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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import net.ctdp.rfdynhud.editor.__EDPrivilegedAccess;

public class Logger
{
    public static final File FOLDER = __UtilHelper.LOG_FOLDER;
    private static File FILE = new File( FOLDER, __EDPrivilegedAccess.isEditorMode ? "rfdynhud_editor.log" : "rfdynhud.log" ).getAbsoluteFile();
    
    private static void logException( Throwable t )
    {
        if ( !FOLDER.exists() )
        {
            t.printStackTrace();
            return;
        }
        
        PrintWriter pw = null;
        try
        {
            if ( ResourceManager.isJarMode() )
            {
                pw = new PrintWriter( new FileOutputStream( FILE, true ) );
                
                t.printStackTrace( pw );
            }
            else
            {
                t.printStackTrace();
            }
        }
        catch ( IOException e )
        {
        }
        finally
        {
            if ( pw != null )
            {
                try
                {
                    pw.close();
                }
                catch ( Throwable t2 )
                {
                }
            }
        }
    }
    
    public static void log( Object message, boolean newLine )
    {
        if ( message instanceof Throwable )
        {
            logException( (Throwable)message );
            return;
        }
        
        if ( !FOLDER.exists() )
        {
            if ( newLine )
                System.out.println( message );
            else
                System.out.print( message );
            return;
        }
        
        try
        {
            if ( ResourceManager.isJarMode() )
            {
                BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( FILE, true ) ) );
                
                bw.write( String.valueOf( message ) );
                if ( newLine )
                    bw.newLine();
                
                bw.close();
            }
            else
            {
                if ( newLine )
                    System.out.println( message );
                else
                    System.out.print( message );
            }
        }
        catch ( IOException e )
        {
        }
    }
    
    public static void log( Object message )
    {
        log( message, true );
    }
    
    public static void setStdStreams()
    {
        try
        {
            System.setOut( new PrintStream( new File( "C:\\rfdynhud.stdout" ) ) );
            System.setErr( new PrintStream( new File( "C:\\rfdynhud.stderr" ) ) );
        }
        catch ( Throwable t )
        {
            log( t );
        }
    }
}
