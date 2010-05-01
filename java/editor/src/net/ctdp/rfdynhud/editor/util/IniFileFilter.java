package net.ctdp.rfdynhud.editor.util;

import java.io.File;
import java.io.FileFilter;

public class IniFileFilter implements FileFilter
{
    public boolean accept( File file )
    {
        String name = file.getName();
        
        if ( file.isDirectory() )
            return ( !name.equalsIgnoreCase( ".svn" ) );
        
        int pos = name.lastIndexOf( '.' );
        if ( pos < 0 )
            return ( false );
        String ext = name.substring( pos ).toLowerCase();
        
        if ( ext.endsWith( ".ini" ) )
            return ( true );
        
        return ( false );
    }
    
    protected IniFileFilter()
    {
    }
    
    public static final IniFileFilter INSTANCE = new IniFileFilter(); 
}
