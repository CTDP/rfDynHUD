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
package net.ctdp.rfdynhud.editor.commandline;

import net.ctdp.rfdynhud.editor.commandline.arguments.ExcludeJarArgument;

import org.jagatoo.commandline.Argument;
import org.jagatoo.commandline.ArgumentsHandler;
import org.jagatoo.commandline.CommandlineParsingException;
import org.jagatoo.commandline.arguments.HelpArgument;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorArgumentsHandler extends ArgumentsHandler
{
    private final EditorArguments arguments = new EditorArguments();
    
    private boolean helpRequested = false;
    
    /**
     * Gets the arguments capsule.
     * 
     * @return the arguments capsule.
     */
    public final EditorArguments getArguments()
    {
        return ( arguments );
    }
    
    public final boolean helpRequested()
    {
        return ( helpRequested );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleArgument( Argument arg, Object value )
    {
        if ( arg == ExcludeJarArgument.INSTANCE )
            arguments.addExcludedJar( (String)value );
        else if ( arg == HelpArgument.INSTANCE )
            this.helpRequested = true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validate( int numArguments ) throws CommandlineParsingException
    {
    }
}
