/**
 * Copyright (c) 2007-2010, JAGaToo Project Group all rights reserved.
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
package org.jagatoo.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Utility methods for files.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class FileUtils
{
    // magic number for Windows, 64Mb - 32Kb)
    private static final long MAX_COPY_COUNT = ( 64L * 1024L * 1024L ) - ( 32L * 1024L );
    
    /**
     * Copies one file to another one.
     * 
     * @param source
     * @param dest
     * 
     * @throws IOException
     */
    public static void copyFile( File source, File dest ) throws IOException
    {
        if ( dest.exists() )
            throw new IOException( "Destination file already exists." );
        
        FileChannel in = null;
        FileChannel out = null;
        
        try
        {
            in = new FileInputStream( source ).getChannel();
            out = new FileOutputStream( dest ).getChannel();
            
            long size = in.size();
            long position = 0L;
            
            while ( position < size )
                position += in.transferTo( position, MAX_COPY_COUNT, out );
            
            dest.setLastModified( source.lastModified() );
        }
        finally
        {
            if ( in != null )
                in.close();
            
            if ( out != null )
                out.close();
        }
    }
    
    public static void deleteFolderRecursively( File folder )
    {
        for ( File f : folder.listFiles() )
        {
            if ( f.isFile() )
                f.delete();
            else
                deleteFolderRecursively( f );
        }
        
        folder.delete();
    }
    
    public static final File getCanonicalFile( File file )
    {
        if ( file == null )
            throw new IllegalArgumentException( "file must not be null." );
        
        try
        {
            return ( file.getCanonicalFile() );
        }
        catch ( IOException e )
        {
            return ( file.getAbsoluteFile() );
        }
    }
    
    private FileUtils()
    {
    }
}
