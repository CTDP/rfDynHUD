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
package net.ctdp.rfdynhud.properties;

/**
 * This {@link DisplayNameGenerator} separates the camel cased words and makes them all lower case.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class SpacedAllLowerCaseDisplayNameGenerator implements DisplayNameGenerator
{
    /**
     * {@inheritDoc}
     */
    @Override
    public String generateNameForDisplay( String rawNameForDisplay )
    {
        if ( ( rawNameForDisplay == null ) || ( rawNameForDisplay.length() == 0 ) )
            return ( "" );
        
        StringBuilder sb = new StringBuilder( rawNameForDisplay.charAt( 0 ) * 120 / 100 );
        
        char ch = rawNameForDisplay.charAt( 0 );
        sb.append( Character.toLowerCase( ch ) );
        
        boolean lastUpper = Character.isUpperCase( ch );
        int wordLength = 1;
        
        for ( int i = 1; i < rawNameForDisplay.length(); i++ )
        {
            ch = rawNameForDisplay.charAt( i );
            
            if ( Character.isUpperCase( ch ) )
            {
                if ( lastUpper )
                {
                    sb.setCharAt( sb.length() - 1, Character.toUpperCase( sb.charAt( sb.length() - 1 ) ) );
                    sb.append( ch );
                    wordLength++;
                }
                else
                {
                    if ( wordLength == 1 )
                        sb.setCharAt( sb.length() - 1, Character.toLowerCase( sb.charAt( sb.length() - 1 ) ) );
                    
                    sb.append( ' ' );
                    sb.append( Character.toLowerCase( ch ) );
                    wordLength = 1;
                }
                
                lastUpper = true;
            }
            else
            {
                if ( lastUpper )
                {
                    if ( ( i > 1 ) && Character.isUpperCase( rawNameForDisplay.charAt( i - 2 ) ) )
                    {
                        sb.insert( sb.length() - 1, ' ' );
                    }
                }
                
                sb.append( ch );
                
                lastUpper = false;
                wordLength++;
            }
        }
        
        if ( wordLength == 1 )
        {
            sb.setCharAt( sb.length() - 1, Character.toLowerCase( sb.charAt( sb.length() - 1 ) ) );
        }
        
        return ( sb.toString() );
    }
}
