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
package net.ctdp.rfdynhud.editor;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import net.ctdp.rfdynhud.util.ResourceManager;

/**
 * This class simply launches the rfDynHUD editor from an IDE.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class TestRFDynHUDEditorLauncher
{
    private static void launch( ClassLoader classLoader, String[] args )
    {
        try
        {
            Class<?> clazz = Class.forName( "net.ctdp.rfdynhud.editor.RFDynHUDEditor", true, classLoader );
            
            Method mm = clazz.getMethod( "main", String[].class );
            
            mm.invoke( null, (Object)args );
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
    }
    
    public static void main( String[] args )
    {
        if ( !ResourceManager.isIDEMode() )
        {
            System.err.println( "You're running in jar mode." );
            System.exit( 1 );
            return;
        }
        
        if ( ResourceManager.getRFDynHUD_Jar() == null )
        {
            System.err.println( "rfdynhud.jar not found in classpath." );
            System.exit( 1 );
            return;
        }
        
        if ( ResourceManager.getRFDynHUDEditor_Jar() != null )
        {
            launch( TestRFDynHUDEditorLauncher.class.getClassLoader(), args );
        }
        else
        {
            File editor_jar = new File( new File( ResourceManager.getRFDynHUD_Jar().getParentFile(), "editor" ), "rfdynhud_editor.jar" );
            
            try
            {
                URLClassLoader classLoader = new URLClassLoader( new URL[] { editor_jar.toURI().toURL() } );
                
                launch( classLoader, args );
            }
            catch ( MalformedURLException e )
            {
                e.printStackTrace();
            }
        }
    }
}
