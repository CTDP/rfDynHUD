package net.ctdp.rfdynhud.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

public class Logger
{
    public static final File FOLDER = RFactorTools.LOG_FOLDER;
    private static File FILE = new File( FOLDER, "rfdynhud.log" ).getAbsoluteFile();
    
    static void setEditorMode()
    {
        FILE = new File( FOLDER, "rfdynhud_editor.log" ).getAbsoluteFile();
    }
    
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
