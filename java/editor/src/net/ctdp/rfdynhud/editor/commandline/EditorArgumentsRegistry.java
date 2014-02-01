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

import net.ctdp.rfdynhud.editor.commandline.arguments.AdditionalClassPathArgument;
import net.ctdp.rfdynhud.editor.commandline.arguments.ExcludeJarArgument;
import net.ctdp.rfdynhud.editor.commandline.arguments.ObjectFactoryArgument;

import org.jagatoo.commandline.ArgumentsRegistry;
import org.jagatoo.commandline.arguments.HelpArgument;

/**
 * Insert class comment here.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class EditorArgumentsRegistry
{
    /**
     * Fills all the standard arguments into an {@link ArgumentsRegistry}.
     * 
     * @param argReg
     */
    public static void addStandardArguments( ArgumentsRegistry argReg )
    {
        argReg.addArgument( ExcludeJarArgument.INSTANCE );
        argReg.addArgument( AdditionalClassPathArgument.INSTANCE );
        argReg.addArgument( ObjectFactoryArgument.INSTANCE );
        
        argReg.addArgument( HelpArgument.INSTANCE );
    }
    
    /**
     * Creates a new {@link ArgumentsRegistry} and fills all the standard arguments into it.
     * 
     * @return the filled {@link ArgumentsRegistry}.
     */
    public static ArgumentsRegistry createStandardArgumentsRegistry()
    {
        ArgumentsRegistry argReg = new ArgumentsRegistry( "rfDynHUD Editor Application" );
        
        addStandardArguments( argReg );
        
        return ( argReg );
    }
    
    /**
     * Dumps the valid arguments with descriptions.
     * 
     * @see ArgumentsRegistry#dump()
     */
    public static void dumpHelpForStandardArguments()
    {
        createStandardArgumentsRegistry().dump();
    }
    
    private EditorArgumentsRegistry()
    {
    }
}
