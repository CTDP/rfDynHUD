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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author Marvin Froehlich (CTDP)
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
        }
        finally
        {
            if ( in != null )
                try { in.close(); } catch ( IOException e2 ) {}
        }
        
        return ( sb.toString() );
    }
}
