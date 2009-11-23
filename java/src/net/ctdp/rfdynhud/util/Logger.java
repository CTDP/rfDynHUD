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
    private static File LOG_FOLDER = new File( RFactorTools.extractRFactorPath() + File.separator + "Plugins" + File.separator + "rfDynHud" + File.separator + "log" );
    private static File LOG_FILE = new File( LOG_FOLDER, "rfdynhud.log" );
    
    static void setEditorMode()
    {
        LOG_FOLDER = new File( RFactorTools.extractRFactorPath() + File.separator + "Plugins" + File.separator + "rfDynHud" + File.separator + "editor" );
        LOG_FILE = new File( LOG_FOLDER, "rfdynhud_editor.log" );
    }
    
    private static void logException( Throwable t )
    {
        if ( !LOG_FOLDER.exists() )
        {
            t.printStackTrace();
            return;
        }
        
        PrintWriter pw = null;
        try
        {
            if ( ResourceManager.isJarMode() )
            {
                pw = new PrintWriter( new FileOutputStream( LOG_FILE, true ) );
                
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
        
        if ( !LOG_FOLDER.exists() )
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
                BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( LOG_FILE, true ) ) );
                
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
