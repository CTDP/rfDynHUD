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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Unicode BOM model.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public enum UnicodeBOM
{
    UTF_8( 0xEFBBBF, 3, "UTF-8" ),
    UTF_16_BE( 0xFEFF, 2, "UTF-16BE" ), // X-UTF-32BE-BOM
    UTF_16_LE( 0xFFFE, 2, "UTF-16LE" ), // x-UTF-16LE-BOM
    UTF_32_BE( 0x0000FEFF, 4, "UTF-32BE" ),
    UTF_32_LE( 0xFEFF0000, 4, "UTF-32LE" ), // X-UTF-32LE-BOM
    UTF_7a( 0x2B2F7638, 4, "UTF-7" ), // seems to be unavailable
    UTF_7b( 0x2B2F7639, 4, "UTF-7" ), // seems to be unavailable
    UTF_7c( 0x2B2F762B, 4, "UTF-7" ), // seems to be unavailable
    UTF_7d( 0x2B2F762F, 4, "UTF-7" ), // seems to be unavailable
    UTF_1( 0xF7644C, 3, "UTF-1" ), // seems to be unavailable
    UTF_EBCDIC( 0xDD736673, 4, "UTF-EBCDIC" ), // seems to be unavailable
    SUSU( 0x0EFEFF, 3, "SUSU" ), // seems to be unavailable
    BOCU_1( 0xFBEE28, 3, "BOCU-1" ), // seems to be unavailable // optional trailing FF and length 4
    GB_18030( 0x84319533, 4, "GB18030" ),
    ;
    
    private final int bom;
    private final int length;
    private final Charset charset;
    
    /**
     * Gets the BOM code.
     * 
     * @return the BOM code.
     */
    public final int getBOM()
    {
        return ( bom );
    }
    
    /**
     * The theoretical byte length. Could be more for {@value #BOCU_1}.
     * 
     * @return theoretical length. Could be more for {@value #BOCU_1}.
     */
    public final int getLength()
    {
        return ( length );
    }
    
    /**
     * The actual byte length of the given BOM.
     * 
     * @param bom the 4 byte bom (first 4 bytes of the file)
     * 
     * @return the actual byte length of the given BOM.
     */
    public final int getLength( int bom )
    {
        if ( this == BOCU_1 )
        {
            if ( ( bom & 0xFF ) == 0xFF )
                return ( 4 );
            
            return ( 3 );
        }
        
        return ( getLength() );
    }
    
    /**
     * Gets the corresponding {@link Charset} or <code>null</code>, if not available.
     * 
     * @return the corresponding {@link Charset} or <code>null</code>, if not available.
     */
    public final Charset getCharset()
    {
        return ( charset );
    }
    
    private UnicodeBOM( int bom, int length, String charset )
    {
        this.bom = bom;
        this.length = length;
        
        Charset cs = null;
        
        try
        {
            cs = Charset.forName( charset );
        }
        catch ( IllegalCharsetNameException e )
        {
        }
        catch ( UnsupportedCharsetException e )
        {
        }
        
        this.charset = cs;
    }
    
    private static UnicodeBOM recognize4( int bom )
    {
        if ( bom == UTF_32_BE.getBOM() )
            return ( UTF_32_BE );
        
        if ( bom == UTF_32_BE.getBOM() )
            return ( UTF_32_BE );
        
        if ( bom == UTF_32_LE.getBOM() )
            return ( UTF_32_LE );
        
        if ( bom == UTF_7a.getBOM() )
            return ( UTF_7a );
        
        if ( bom == UTF_7b.getBOM() )
            return ( UTF_7b );
        
        if ( bom == UTF_7c.getBOM() )
            return ( UTF_7c );
        
        if ( bom == UTF_7d.getBOM() )
            return ( UTF_7d );
        
        if ( bom == UTF_EBCDIC.getBOM() )
            return ( UTF_EBCDIC );
        
        if ( bom == GB_18030.getBOM() )
            return ( GB_18030 );
        
        return ( null );
    }
    
    private static UnicodeBOM recognize3( int bom )
    {
        if ( bom == BOCU_1.getBOM() )
        {
            //if ( ( bom & 0xFF ) == 0xFF )
            
            return ( BOCU_1 );
        }
        
        if ( bom == SUSU.getBOM() )
            return ( SUSU );
        
        if ( bom == UTF_1.getBOM() )
            return ( UTF_1 );
        
        if ( bom == UTF_8.getBOM() )
            return ( UTF_8 );
        
        return ( null );
    }
    
    private static UnicodeBOM recognize2( int bom )
    {
        if ( bom == UTF_16_BE.getBOM() )
            return ( UTF_16_BE );
        
        if ( bom == UTF_16_LE.getBOM() )
            return ( UTF_16_LE );
        
        return ( null );
    }
    
    /**
     * <p>
     * Attempts to recognize the passed unicode BOM. If it can't be recognized, <code>null</code> is returned.
     * </p>
     * 
     * <p>
     * The difference to the {@link #valueOf(int)} method is, that {@link #recognize(int)} expects
     * the first four bytes of the file, where a BOM of length 3 appears in the first three byte
     * and hence the value is shifted to the left by one byte
     * while {@link #valueOf(int)} takes the concrete BOM code.
     * </p>
     * 
     * @param bom the 4 byte bom (first 4 bytes of the file)
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     * 
     * @see #valueOf(int)
     */
    public static UnicodeBOM recognize( int bom )
    {
        UnicodeBOM result = recognize4( bom );
        
        if ( result != null )
            return ( result );
        
        int bom3 = ( bom & 0xFFFFFF00 ) >>> 8;
        
        result = recognize3( bom3 );
        
        if ( result != null )
            return ( result );
        
        int bom2 = ( bom & 0xFFFF00 ) >>> 16;
        
        result = recognize2( bom2 );
        
        //if ( result != null )
        //    return ( result );
        
        return ( null );
    }
    
    /**
     * <p>
     * Attempts to recognize the passed unicode BOM. If it can't be recognized, <code>null</code> is returned.
     * </p>
     * 
     * <p>
     * The difference to the {@link #recognize(int)} method is, that {@link #valueOf(int)} takes the concrete BOM code
     * while {@link #recognize(int)} expects the first four bytes of the file, where a BOM of length 3 appears in the first three byte
     * and hence the value is shifted to the left by one byte.
     * </p>
     * 
     * @param bom the BOM code
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     * 
     * @see #recognize(int)
     */
    public static UnicodeBOM valueOf( int bom )
    {
        UnicodeBOM result = recognize4( bom );
        
        if ( result != null )
            return ( result );
        
        result = recognize3( bom );
        
        if ( result != null )
            return ( result );
        
        result = recognize2( bom );
        
        //if ( result != null )
        //    return ( result );
        
        return ( null );
    }
    
    private static UnicodeBOM skipBOM( InputStream in, int[] info, boolean reset ) throws IOException
    {
        if ( reset && !in.markSupported() )
            throw new IllegalArgumentException( "The passed InputStream doesn't support mark/reset." );
        
        if ( reset )
            in.mark( 16 );
        
        try
        {
            // probe length 2
            
            int off = 0;
            int n = 2;
            byte[] buffer = new byte[ 4 ];
            while ( n > 0 )
            {
                n = in.read( buffer, off, n );
                
                if ( n > 0 )
                {
                    off += n;
                    n = 2 - off;
                }
            }
            
            n = off;
            
            int bom = 0;
            for ( int i = 0; i < n; i++ )
            {
                bom = ( ( ( bom << 8 ) & 0xFFFFFF00 ) | ( buffer[i] & 0xFF ) );
            }
            
            UnicodeBOM uniBOM = UnicodeBOM.recognize2( bom );
            
            if ( uniBOM != null )
            {
                if ( info != null )
                {
                    info[0] = uniBOM.getLength( bom );
                    info[1] = bom;
                }
                
                reset = false;
                
                return ( uniBOM );
            }
            
            // probe length 3
            
            n = 1;
            while ( n > 0 )
            {
                n = in.read( buffer, off, n );
                
                if ( n > 0 )
                {
                    off += n;
                    n = 2 - off;
                }
            }
            
            n = off;
            
            for ( int i = 2; i < n; i++ )
            {
                bom = ( ( ( bom << 8 ) & 0xFFFFFF00 ) | ( buffer[i] & 0xFF ) );
            }
            
            uniBOM = UnicodeBOM.recognize3( bom );
            
            if ( uniBOM != null )
            {
                if ( info != null )
                {
                    info[0] = uniBOM.getLength( bom );
                    info[1] = bom;
                }
                
                reset = false;
                
                return ( uniBOM );
            }
            
            // probe length 4
            
            n = 1;
            while ( n > 0 )
            {
                n = in.read( buffer, off, n );
                
                if ( n > 0 )
                {
                    off += n;
                    n = 2 - off;
                }
            }
            
            n = off;
            
            for ( int i = 3; i < n; i++ )
            {
                bom = ( ( ( bom << 8 ) & 0xFFFFFF00 ) | ( buffer[i] & 0xFF ) );
            }
            
            uniBOM = UnicodeBOM.recognize4( bom );
            
            if ( uniBOM != null )
            {
                if ( info != null )
                {
                    info[0] = uniBOM.getLength( bom );
                    info[1] = bom;
                }
                
                reset = false;
                
                return ( uniBOM );
            }
            
            return ( null );
        }
        finally
        {
            if ( reset )
                in.reset();
        }
    }
    
    /**
     * Skips the bytes, covered by a possible unicode BOM, or, if not recognized, does nothing.
     * The passed {@link InputStream} needs to support mark/reset.
     * 
     * @param in
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     * 
     * @throws IOException if anything went wrong.
     */
    public static UnicodeBOM skipBOM( InputStream in ) throws IOException
    {
        return ( skipBOM( in, null, true ) );
    }
    
    /**
     * Only ready up to the first four bytes of the file and tries to recognize the unicode BOM from these data.
     * 
     * @param file the file to probe
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     * 
     * @throws IOException if something went wrong
     */
    public static UnicodeBOM readBOM( File file ) throws IOException
    {
        FileInputStream in = null;
        
        try
        {
            in = new FileInputStream( file );
            
            return ( skipBOM( in, null, false ) );
        }
        finally
        {
            if ( in != null )
                try { in.close(); } catch ( IOException e ) {}
        }
    }
    
    /**
     * Removes the BOM bytes from the input file, if and only if one was found.
     * The method uses a temp file to opcy the contents, which replaces the input file afterwards.
     * 
     * @param file the input file
     * @param temp the temp file name
     * @param targetCharset the charset to use for the new file or <code>null</code> for default
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     * 
     * @throws IOException if something went wrong
     */
    public static UnicodeBOM removeBOM( File file, File temp, Charset targetCharset ) throws IOException
    {
        InputStream in = null;
        Reader in2 = null;
        Writer out = null;
        
        UnicodeBOM bom = null;
        
        boolean success = false;
        
        try
        {
            in = new FileInputStream( file );
            
            int[] info = { -1, 0 };
            bom = skipBOM( in, info, false );
            
            if ( bom == null )
                return ( null );
            
            if ( bom.getCharset() == null )
                in2 = new InputStreamReader( in );
            else
                in2 = new InputStreamReader( in, bom.getCharset() );
            
            if ( targetCharset == null )
                targetCharset = bom.getCharset();
            
            if ( targetCharset == null )
                out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( temp ) ) );
            else
                out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( temp ), targetCharset ) );
            
            char[] buffer = new char[ 1024 ];
            int n = 0;
            while ( ( n = in2.read( buffer, 0, Math.min( buffer.length, in.available() + 1 ) ) ) >= 0 )
            {
                if ( n > 0 )
                    out.write( buffer, 0, n );
            }
            
            success = true;
        }
        finally
        {
            if ( in2 != null )
                try { in2.close(); in = null; } catch ( IOException e ) {}
            
            if ( in != null )
                try { in.close(); } catch ( IOException e ) {}
            
            if ( out != null )
                try { out.close(); } catch ( IOException e ) {}
        }
        
        if ( success )
        {
            if ( !file.delete() )
                throw new IOException( "Could't delete the file \"" + file.getAbsolutePath() + "\"." );
            
            if ( !temp.renameTo( file ) )
                throw new IOException( "Could't rename the file \"" + temp.getAbsolutePath() + "\" to \"" + file.getAbsolutePath() + "\"." );
        }
        
        return ( bom );
    }
    
    /**
     * Removes the BOM bytes from the input file, if and only if one was found.
     * The method uses a temp file to opcy the contents, which replaces the input file afterwards.
     * 
     * @param file the input file
     * @param temp the temp file name
     * 
     * @return the recognized {@link UnicodeBOM} or <code>null</code>.
     * 
     * @throws IOException if something went wrong
     */
    public static UnicodeBOM removeBOM( File file, File temp ) throws IOException
    {
        return ( removeBOM( file, temp, null ) );
    }
}
