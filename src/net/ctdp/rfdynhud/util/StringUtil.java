package net.ctdp.rfdynhud.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author Marvin Froehlich
 */
public class StringUtil
{
    public static final String loadString( URL url )
    {
        if ( url == null )
            return ( "" );
        
        StringBuilder sb = new StringBuilder();
        
        BufferedInputStream in = null;
        try
        {
            int c;
            in = new BufferedInputStream( url.openStream() );
            while ( ( c = in.read() ) >= 0 )
            {
                sb.append( (char)c );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            if ( in != null )
            {
                try
                {
                    in.close();
                }
                catch ( IOException e2 )
                {
                    e2.printStackTrace();
                }
            }
        }
        
        return ( sb.toString() );
    }
}
