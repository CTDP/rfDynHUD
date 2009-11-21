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
    
    public static void log( Object message )
    {
        if ( message instanceof Throwable )
        {
            logException( (Throwable)message );
            return;
        }
        
        if ( !LOG_FOLDER.exists() )
        {
            System.out.println( message );
            return;
        }
        
        try
        {
            if ( ResourceManager.isJarMode() )
            {
                BufferedWriter bw = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( LOG_FILE, true ) ) );
                
                bw.write( String.valueOf( message ) );
                bw.newLine();
                
                bw.close();
            }
            else
            {
                System.out.println( message );
            }
        }
        catch ( IOException e )
        {
            
        }
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
