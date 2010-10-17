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
package org.jagatoo.util.classes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The {@link ClassSearcher} provides utility methods to search classes.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class ClassSearcher
{
    /**
     * Reads all classnames in a given folder and its subfolders and
     * puts them into the List.
     * 
     * @param baseFolder the folder from which to read the classes
     * @param folder the folder from which to read the classes as a File object
     * @param classes the List to put the classname into
     */
    private static void findClassNamesFromFolder( String baseFolder, File folder, String[] packagePrefixes, ClassSearchCriterium crit, Set<Class<?>> classes )
    {
        for ( File file: folder.listFiles() )
        {
            if ( file.isDirectory() )
            {
                findClassNamesFromFolder( baseFolder, file, packagePrefixes, crit, classes );
            }
            else if ( file.getAbsolutePath().endsWith( ".class" ) )
            {
                final int lastIndex = Math.max( 0, file.getAbsolutePath().lastIndexOf( baseFolder ) ) + baseFolder.length();
                String className = file.getAbsolutePath().substring( lastIndex ).replace( '\\', '/' ).replace( '/', '.' );
                className = className.substring( 0, className.length() - 6 );
                
                for ( String pkgName: packagePrefixes )
                {
                    if ( className.startsWith( pkgName + "." ) )
                    {
                        try
                        {
                            Class<?> clazz = Class.forName( className, false, ClassSearcher.class.getClassLoader() );
                            if ( crit.check( clazz ) )
                                classes.add( clazz );
                        }
                        catch ( ClassNotFoundException e )
                        {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Reads all classnames in a given folder and its subfolders and
     * puts them into the List.
     * 
     * @param baseFolder the folder from which to read the classes
     * @param classes the List to put the classname into
     */
    private static void findClassNamesFromFolder( String folderName, String[] packagePrefixes, ClassSearchCriterium crit, Set<Class<?>> classes )
    {
        findClassNamesFromFolder( folderName + File.separator, new File( folderName ), packagePrefixes, crit, classes );
    }
    
    /**
     * Reads all classes in given packages from a JarFile and puts their names
     * into a List.
     * 
     * @param jarFilename the filename of the jar
     * @param classes the List to put the classnames into
     */
    private static void findClassNamesFromJar( String jarFilename, String[] packagePrefixes, ClassSearchCriterium crit, Set<Class<?>> classes )
    {
        if ( !( new File( jarFilename ).exists() ) )
        {
            System.err.println( "Couldn't find jar file " + jarFilename );
            return;
        }
        
        try
        {
            jarFilename = new File( jarFilename ).getCanonicalPath();
            JarFile jar = new JarFile( jarFilename );
            Enumeration<JarEntry> jarEntries = jar.entries();
            while ( jarEntries.hasMoreElements() )
            {
                JarEntry jarEntry = jarEntries.nextElement();
                for ( int i = 0; i < packagePrefixes.length; i++ )
                {
                    if ( ( jarEntry.getName().startsWith( packagePrefixes[ i ] + "/" ) ) && ( jarEntry.getName().endsWith( ".class" ) ) )
                    {
                        String className = jarEntry.getName().replace( '/', '.' );
                        className = className.substring( 0, className.length() - 6 );
                        try
                        {
                            Class<?> clazz = Class.forName( className, false, ClassSearcher.class.getClassLoader() );
                            if ( crit.check( clazz ) )
                                classes.add( clazz );
                        }
                        catch ( ClassNotFoundException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param crit the Criterium to check for each class
     * @param packagePrefixes dot separated package prefix names (like "org.xith3d.test")
     * 
     * @return the filled up List
     */
    public static List<Class<?>> findClasses( ClassSearchCriterium crit, String... packagePrefixes )
    {
        String[] packagePrifixes_slash = new String[ packagePrefixes.length ];
        for ( int i = 0; i < packagePrefixes.length; i++ )
        {
            packagePrifixes_slash[i] = packagePrefixes[i].replace( '.', '/' );
        }
        
        Set<Class<?>> tmp = new HashSet<Class<?>>();
        
        String[] classPath = System.getProperty( "java.class.path" ).split( System.getProperty( "path.separator" ) );
        for ( String cp: classPath )
        {
            if ( cp.endsWith( ".jar" ) )
            {
                findClassNamesFromJar( cp, packagePrifixes_slash, crit, tmp );
            }
            else
            {
                findClassNamesFromFolder( cp, packagePrefixes, crit, tmp );
            }
        }
        
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.addAll( tmp );
        Collections.sort( classes, new Comparator<Class<?>>()
        {
            @Override
            public int compare( Class<?> c1, Class< ? > c2 )
            {
                return ( c1.getName().compareTo( c2.getName() ) );
            }
        } );
        
        return ( classes );
    }
    
    private ClassSearcher()
    {
    }
}
