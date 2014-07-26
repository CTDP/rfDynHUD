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
package org.jagatoo.util.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.jagatoo.util.streams.StreamUtils;

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
    
    /**
     * Deletes directory at path recursively.
     * 
     * @param path
     * @param includeSelf also delete the passed directory itself?
     * 
     * @throws IOException 
     */
    public static void deleteFolderRecursively( File path, boolean includeSelf ) throws IOException
    {
        if ( !path.isDirectory() )
            throw new IllegalArgumentException( "The passed file is not a directory." );
        
        File[] files = path.listFiles();
        
        if ( files == null )
            return;
        
        for ( int i = 0; i < files.length; i++ )
        {
            File f = files[i];
            
            if ( f.isFile() )
            {
                if ( !f.delete() )
                {
                    throw new IOException( "Couldn't delete file: " + f.getAbsolutePath() );
                }
            }
            else
            {
                deleteFolderRecursively( f, true );
            }
        }
        
        if ( includeSelf && !path.delete() )
        {
            throw new IOException( "Couldn't delete directory: " + path.getAbsolutePath() );
        }
    }
    
    /**
     * Deletes directory at path recursively.
     * 
     * @param path
     * @param includeSelf also delete the passed directory itself?
     * 
     * @throws IOException 
     */
    public static void deleteFolderRecursively( String path, boolean includeSelf ) throws IOException
    {
        deleteFolderRecursively( new File( path ), includeSelf );
    }
    
    /**
     * Deletes directory at path recursively.
     * 
     * @param path
     * 
     * @throws IOException 
     */
    public static void deleteFolderRecursively( File path ) throws IOException
    {
        deleteFolderRecursively( path, true );
    }
    
    /**
     * Deletes directory at path recursively.
     * 
     * @param path
     * 
     * @throws IOException 
     */
    public static void deleteFolderRecursively( String path ) throws IOException
    {
        deleteFolderRecursively( new File( path ), true );
    }
    
    /**
     * writes string contents to file file
     * 
     * @param contents
     * @param file
     * 
     * @throws IOException
     */
    public static void writeStringToFile( CharSequence contents, File file ) throws IOException
    {
        PrintStream ps = null;
        
        try
        {
            ps = new PrintStream( file );
            
            ps.print( contents );
            
            ps.close();
        }
        finally
        {
            if ( ps != null )
                //try { ps.close(); } catch ( IOException e ) {}
                ps.close();
        }
    }
    
    /**
     * writes string contents to file file
     * 
     * @param contents
     * @param filename
     * 
     * @throws IOException
     */
    public static void writeStringToFile( CharSequence contents, String filename ) throws IOException
    {
        writeStringToFile( contents, new File( filename ) );
    }
    
    /**
     * Reads text-file file and returns contents as one big byte-array.
     * 
     * @param file
     * @return a byte-array
     * 
     * @throws IOException
     */
    public static byte[] getFileAsByteArray( File file ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream( (int)file.length() );
        
        StreamUtils.transferBytes( new FileInputStream( file ), baos, true, true );
        
        return ( baos.toByteArray() );
    }
    
    /**
     * Reads text-file file and returns contents as one big string.
     * 
     * @param file
     * @return string
     * 
     * @throws IOException
     */
    public static String getFileAsString( File file ) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream( (int)file.length() );
        
        StreamUtils.transferBytes( new FileInputStream( file ), baos, true, true );
        
        return ( baos.toString() );
    }
    
    /**
     * 
     * @param file
     * @param charset_name
     * 
     * @return the file contets as string.
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException 
     */
    public static String getFileAsString( File file, String charset_name ) throws IOException, UnsupportedEncodingException
    {
        return ( new String( getFileAsByteArray( file ), charset_name ) );
    }
    
    /**
     * 
     * @param file
     * @param charset
     * 
     * @return the file contets as string.
     * 
     * @throws IOException
     */
    public static String getFileAsString( File file, Charset charset ) throws IOException
    {
        return( new String( getFileAsByteArray( file ), charset ) );
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
    
    /**
     * Returns the canonical representation of the given file using {@link File#getCanonicalFile()}.
     * If this fails, the result of {@link File#getAbsoluteFile()} is returned.
     * 
     * @param filename
     * 
     * @return the canonical representation of the given file.
     */
    public static final File getCanonicalFile( String filename )
    {
        return ( getCanonicalFile( new File( filename ) ) );
    }
    
    private FileUtils()
    {
    }
}
