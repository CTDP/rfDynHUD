/**
 * Copyright (C) 2009-2014 Cars and Tracks Development Project (CTDP).
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
package net.ctdp.rfdynhud.editor.commandline.arguments;

import org.jagatoo.commandline.Argument;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ExcludeJarArgument extends Argument
{
    public static final ExcludeJarArgument INSTANCE = new ExcludeJarArgument();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String parseValueImpl( String rawValue )
    {
        return ( rawValue.replace( '/', '\\' ).toLowerCase() );
    }
    
    private ExcludeJarArgument()
    {
        super( 'x', "exclude-jar", "Excludes the given jar file from being used.", true );
    }
}
