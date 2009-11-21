package net.ctdp.rfdynhud.editor.util;

import java.io.File;
import java.io.FileFilter;

public class ImageFileFilter implements FileFilter
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
        
        if ( ext.endsWith( ".png" ) )
            return ( true );
        
        if ( ext.endsWith( ".jpg" ) )
            return ( true );
        
        if ( ext.endsWith( ".gif" ) )
            return ( true );
        
        return ( false );
    }
    
    protected ImageFileFilter()
    {
    }
    
    public static final ImageFileFilter INSTANCE = new ImageFileFilter(); 
}
