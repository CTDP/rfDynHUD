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
package org.jagatoo.util.classes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static Class<?> checkAndAddClass( ClassLoader classLoader, String className, ClassSearchCriterium crit, Set<Class<?>> classes )
    {
        try
        {
            Class<?> clazz = Class.forName( className, false, classLoader );
            if ( crit.check( clazz ) )
            {
                classes.add( clazz );
                
                return ( clazz );
            }
        }
        catch ( ClassNotFoundException e )
        {
            e.printStackTrace();
        }
        
        return ( null );
    }
    
    /**
     * Reads all classnames in a given folder and its subfolders and
     * puts them into the List.
     * 
     * @param classLoader
     * @param baseFolder the folder from which to read the classes
     * @param folder the folder from which to read the classes as a File object
     * @param classes the List to put the classname into
     */
    private static void findClassNamesFromFolder( ClassLoader classLoader, String baseFolder, File folder, String[] packagePrefixes, ClassSearchCriterium crit, Set<Class<?>> classes )
    {
        for ( File file: folder.listFiles() )
        {
            if ( file.isDirectory() )
            {
                findClassNamesFromFolder( classLoader, baseFolder, file, packagePrefixes, crit, classes );
            }
            else if ( file.getAbsolutePath().endsWith( ".class" ) )
            {
                final int lastIndex = Math.max( 0, file.getAbsolutePath().lastIndexOf( baseFolder ) ) + baseFolder.length();
                String className = file.getAbsolutePath().substring( lastIndex ).replace( '\\', '/' ).replace( '/', '.' );
                className = className.substring( 0, className.length() - 6 );
                
                if ( ( packagePrefixes == null ) || ( packagePrefixes.length == 0 ) )
                {
                    checkAndAddClass( classLoader, className, crit, classes );
                }
                else
                {
                    for ( String pkgName: packagePrefixes )
                    {
                        if ( className.startsWith( pkgName + "." ) )
                        {
                            checkAndAddClass( classLoader, className, crit, classes );
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Reads all classnames in a given folder and its subfolders and
     * puts them into the List.
     * 
     * @param classLoader
     * @param baseFolder the folder from which to read the classes
     * @param classes the List to put the classname into
     */
    private static void findClassNamesFromFolder( ClassLoader classLoader, File folderName, String[] packagePrefixes, ClassSearchCriterium crit, Set<Class<?>> classes )
    {
        findClassNamesFromFolder( classLoader, folderName.getAbsolutePath() + File.separator, folderName, packagePrefixes, crit, classes );
    }
    
    /**
     * Reads all classes in given packages from a JarFile and puts their names
     * into a List.
     * 
     * @param classLoader
     * @param jarFile the filename of the jar
     * @param packagePrefixes
     * @param crit
     * @param classes the List to put the classnames into
     * @param jarMap
     */
    private static void findClassNamesFromJar( ClassLoader classLoader, File jarFile, String[] packagePrefixes, ClassSearchCriterium crit, Set<Class<?>> classes, Map<Class<?>, JarFile> jarMap )
    {
        if ( !jarFile.exists() )
        {
            System.err.println( "Couldn't find jar file " + jarFile.getAbsolutePath() );
            return;
        }
        
        try
        {
            jarFile = jarFile.getCanonicalFile();
            JarFile jar = new JarFile( jarFile );
            Enumeration<JarEntry> jarEntries = jar.entries();
            
            while ( jarEntries.hasMoreElements() )
            {
                JarEntry jarEntry = jarEntries.nextElement();
                
                if ( jarEntry.getName().endsWith( ".class" ) )
                {
                    if ( ( packagePrefixes == null ) || ( packagePrefixes.length == 0 ) )
                    {
                        String className = jarEntry.getName().replace( '/', '.' );
                        className = className.substring( 0, className.length() - 6 );
                        
                        Class<?> clazz = checkAndAddClass( classLoader, className, crit, classes );
                        if ( ( clazz != null ) && ( jarMap != null ) )
                            jarMap.put( clazz, jar );
                    }
                    else
                    {
                        for ( int i = 0; i < packagePrefixes.length; i++ )
                        {
                            if ( jarEntry.getName().startsWith( packagePrefixes[ i ] + "/" ) )
                            {
                                String className = jarEntry.getName().replace( '/', '.' );
                                className = className.substring( 0, className.length() - 6 );
                                
                                Class<?> clazz = checkAndAddClass( classLoader, className, crit, classes );
                                if ( ( clazz != null ) && ( jarMap != null ) )
                                    jarMap.put( clazz, jar );
                                break;
                            }
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
    
    private static String[] getPackagePrefixesSlash( String[] packagePrefixes )
    {
        if ( packagePrefixes == null )
            return ( null );
        
        String[] packagePrifixes_slash = new String[ packagePrefixes.length ];
        for ( int i = 0; i < packagePrefixes.length; i++ )
        {
            packagePrifixes_slash[i] = packagePrefixes[i].replace( '.', '/' );
        }
        
        return ( packagePrifixes_slash );
    }
    
    private static List<Class<?>> getSortedList( Collection<Class<?>> coll )
    {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.addAll( coll );
        Collections.sort( classes, new Comparator<Class<?>>()
        {
            @Override
            public int compare( Class<?> c1, Class< ? > c2 )
            {
                return ( c1.getName().compareToIgnoreCase( c2.getName() ) );
            }
        } );
        
        return ( classes );
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param packagePrefixesDot dot separated package prefix names (like "org.jagatoo.test")
     * @param packagePrefixesSlash slash separated package prefix names (like "org/jagatoo/test")
     * @param crit the Criterium to check for each class
     * @param classes
     * @param jarMap
     */
    private static void findClassesInClassPath( String[] packagePrefixesDot, String[] packagePrefixesSlash, ClassSearchCriterium crit, Set<Class<?>> classes, Map<Class<?>, JarFile> jarMap )
    {
        ClassLoader classLoader = ClassSearcher.class.getClassLoader();
        
        String[] classPath = System.getProperty( "java.class.path" ).split( System.getProperty( "path.separator" ) );
        for ( String cp: classPath )
        {
            if ( cp.toLowerCase().endsWith( ".jar" ) )
                findClassNamesFromJar( classLoader, new File( cp ), packagePrefixesSlash, crit, classes, jarMap );
            else
                findClassNamesFromFolder( classLoader, new File( cp ), packagePrefixesDot, crit, classes );
        }
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param jarMap a map to fill with class-to-jar mappings (or null to ignore)
     * @param crit the Criterium to check for each class
     * @param packagePrefixes dot separated package prefix names (like "org.jagatoo.test")
     * 
     * @return the filled up List
     */
    public static List<Class<?>> findClasses( Map<Class<?>, JarFile> jarMap, ClassSearchCriterium crit, String... packagePrefixes )
    {
        String[] packagePrifixes_slash = getPackagePrefixesSlash( packagePrefixes );
        
        Set<Class<?>> tmp = new HashSet<Class<?>>();
        
        findClassesInClassPath( packagePrefixes, packagePrifixes_slash, crit, tmp, jarMap );
        
        return ( getSortedList( tmp ) );
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param crit the Criterium to check for each class
     * @param packagePrefixes dot separated package prefix names (like "org.jagatoo.test")
     * 
     * @return the filled up List
     */
    public static List<Class<?>> findClasses( ClassSearchCriterium crit, String... packagePrefixes )
    {
        return ( findClasses( (Map<Class<?>, JarFile>)null, crit, packagePrefixes ) );
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param classLoader
     * @param packagePrefixesDot dot separated package prefix names (like "org.jagatoo.test")
     * @param packagePrefixesSlash slash separated package prefix names (like "org/jagatoo/test")
     * @param crit the Criterium to check for each class
     * @param classes
     * @param jarMap
     * 
     * @throws IOException
     */
    private static void findClassesFromURLClassLoader( URLClassLoader classLoader, String[] packagePrefixesDot, String[] packagePrefixesSlash, ClassSearchCriterium crit, Set<Class<?>> classes, Map<Class<?>, JarFile> jarMap ) throws IOException
    {
        for ( URL url : classLoader.getURLs() )
        {
            try
            {
                URI uri = url.toURI();
                
                if ( uri.toString().endsWith( "/" ) )
                    findClassNamesFromFolder( classLoader, new File( uri ), packagePrefixesDot, crit, classes );
                else
                    findClassNamesFromJar( classLoader, new File( uri ), packagePrefixesSlash, crit, classes, jarMap );
            }
            catch ( URISyntaxException e )
            {
                throw new MalformedURLException( e.getMessage() );
            }
        }
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param includeClassPath whether to search the class path, too
     * @param classLoader the class loader to search classes in
     * @param jarMap a map to fill with class-to-jar mappings (or null to ignore)
     * @param crit the Criterium to check for each class
     * @param packagePrefixes dot separated package prefix names (like "org.jagatoo.test")
     * 
     * @return the filled up List
     * 
     * @throws IOException
     */
    public static List<Class<?>> findClasses( boolean includeClassPath, URLClassLoader classLoader, Map<Class<?>, JarFile> jarMap, ClassSearchCriterium crit, String... packagePrefixes ) throws IOException
    {
        String[] packagePrifixes_slash = getPackagePrefixesSlash( packagePrefixes );
        
        Set<Class<?>> tmp = new HashSet<Class<?>>();
        
        if ( includeClassPath )
            findClassesInClassPath( packagePrefixes, packagePrifixes_slash, crit, tmp, jarMap );
        
        findClassesFromURLClassLoader( classLoader, packagePrefixes, packagePrifixes_slash, crit, tmp, jarMap );
        
        return ( getSortedList( tmp ) );
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param includeClassPath whether to search the class path, too
     * @param classLoader the class loader to search classes in
     * @param crit the Criterium to check for each class
     * @param packagePrefixes dot separated package prefix names (like "org.jagatoo.test")
     * 
     * @return the filled up List
     * 
     * @throws IOException
     */
    public static List<Class<?>> findClasses( boolean includeClassPath, URLClassLoader classLoader, ClassSearchCriterium crit, String... packagePrefixes ) throws IOException
    {
        return ( findClasses( includeClassPath, classLoader, null, crit, packagePrefixes ) );
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param classLoader the class loader to search classes in
     * @param jarMap a map to fill with class-to-jar mappings (or null to ignore)
     * @param crit the Criterium to check for each class
     * @param packagePrefixes dot separated package prefix names (like "org.jagatoo.test")
     * 
     * @return the filled up List
     * 
     * @throws IOException
     */
    public static List<Class<?>> findClasses( URLClassLoader classLoader, Map<Class<?>, JarFile> jarMap, ClassSearchCriterium crit, String... packagePrefixes ) throws IOException
    {
        return ( findClasses( false, classLoader, jarMap, crit, packagePrefixes ) );
    }
    
    /**
     * Reads all classnames from given packages into a List, that match certrain
     * criteria.
     * 
     * @param classLoader the class loader to search classes in
     * @param crit the Criterium to check for each class
     * @param packagePrefixes dot separated package prefix names (like "org.jagatoo.test")
     * 
     * @return the filled up List
     * 
     * @throws IOException
     */
    public static List<Class<?>> findClasses( URLClassLoader classLoader, ClassSearchCriterium crit, String... packagePrefixes ) throws IOException
    {
        return ( findClasses( false, classLoader, crit, packagePrefixes ) );
    }
    
    private ClassSearcher()
    {
    }
}
