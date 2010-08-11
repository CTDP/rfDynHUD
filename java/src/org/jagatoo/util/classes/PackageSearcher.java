/**
 * Copyright (c) 2007-2009, JAGaToo Project Group all rights reserved.
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * The {@link PackageSearcher} provides utility methods to search classes.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class PackageSearcher
{
    /**
     * Reads all package names in a given folder and its subfolders and
     * puts them into the List.
     * 
     * @param baseFolder the folder from which to read the classes
     * @param folder the folder from which to read the classes as a File object
     * @param packages the List to put the packages into
     */
    private static void findPackageNamesFromFolder( String baseFolder, File folder, int levelToParent, String[] packagePrefixes, boolean includeParents, boolean includeSubpackages, List<String> packages )
    {
        for ( File file: folder.listFiles() )
        {
            if ( file.isDirectory() )
            {
                final String packageName = file.getAbsolutePath();
                
                for ( int i = 0; i < packagePrefixes.length; i++ )
                {
                    boolean isSubPackage = false;
                    
                    if ( packageName.startsWith( packagePrefixes[i], baseFolder.length() ) )
                    {
                        if ( packageName.length() == baseFolder.length() + packagePrefixes[i].length() )
                        {
                            if ( includeParents )
                                packages.add( packageName.substring( baseFolder.length() ).replace( File.separatorChar, '.' ) );
                            
                            levelToParent = 0;
                        }
                        else
                        {
                            if ( includeSubpackages || ( levelToParent <= 1 ) )
                                packages.add( packageName.substring( baseFolder.length() ).replace( File.separatorChar, '.' ) );
                            
                            isSubPackage = true;
                        }
                    }
                    
                    if ( levelToParent == 0 )
                        includeParents = false;
                    
                    if ( ( levelToParent >= 0 ) && isSubPackage )
                        levelToParent++;
                    
                    if ( ( levelToParent > 1 ) && !includeSubpackages )
                        findPackageNamesFromFolder( baseFolder, file, levelToParent, packagePrefixes, includeParents, false, packages );
                    else
                        findPackageNamesFromFolder( baseFolder, file, levelToParent, packagePrefixes, includeParents, includeSubpackages, packages );
                }
            }
        }
    }
    
    /**
     * Reads all package names in a given folder and its subfolders and
     * puts them into the List.
     * 
     * @param baseFolder the folder from which to read the classes
     * @param packages the List to put the packages into
     */
    private static void findPackageNamesFromFolder( String folderName, String[] packagePrefixes, boolean includeParents, boolean includeSubpackages, List<String> packages )
    {
        findPackageNamesFromFolder( new File( folderName ).getAbsolutePath() + File.separator, new File( folderName ), -1, packagePrefixes, includeParents, includeSubpackages, packages );
    }
    
    /**
     * Reads all package names in a given folder and its subfolders and
     * puts them into the List.
     * 
     * @param baseFolder the folder from which to read the classes
     * @param folder the folder from which to read the classes as a File object
     * @param packages the List to put the packages into
     */
    private static void findPackageNamesFromFolder( String baseFolder, File folder, String[] packageContains, List<String> packages )
    {
        for ( File file: folder.listFiles() )
        {
            if ( file.isDirectory() )
            {
                final String packageName = file.getAbsolutePath();
                
                for ( int i = 0; i < packageContains.length; i++ )
                {
                    if ( packageName.contains( packageContains[i] ) )
                    {
                        packages.add( packageName.substring( baseFolder.length() ).replace( File.separatorChar, '.' ) );
                    }
                }
                
                findPackageNamesFromFolder( baseFolder, file, packageContains, packages );
            }
        }
    }
    
    private static int getDepth( String packageName, char separator )
    {
        if ( ( packageName.length() == 1 ) && ( packageName.charAt( 0 ) == separator ) )
            return ( 0 );
        
        int depth = 1;
        for ( int i = 0; i < packageName.length(); i++ )
        {
            if ( packageName.charAt( i ) == separator )
                depth++;
        }
        
        if ( packageName.charAt( packageName.length() - 1 ) == separator )
            return ( depth - 1 );
        
        return ( depth );
    }
    
    /**
     * Reads all package names in given packages from a JarFile and puts their names
     * into a List.
     * 
     * @param jarFilename the filename of the jar
     * @param packages the List to put the packages into
     */
    private static void findPackageNamesFromJar( String jarFilename, String[] packagePrefixes, boolean includeParents, boolean includeSubpackages, List<String> packages )
    {
        if ( !( new File( jarFilename ).exists() ) )
        {
            System.err.println( "Couldn't find jar file " + jarFilename );
            return;
        }
        
        int[] depths = new int[ packagePrefixes.length ];
        for ( int i = 0; i < packagePrefixes.length; i++ )
        {
            depths[i] = getDepth( packagePrefixes[i], '/' );
        }
        
        try
        {
            HashSet<String> set = new HashSet<String>();
            
            for ( int i = 0; i < packagePrefixes.length; i++ )
            {
                set.add( packagePrefixes[i].replace( '/', '.' ) );
            }
            
            jarFilename = new File( jarFilename ).getCanonicalPath();
            JarFile jar = new JarFile( jarFilename );
            Enumeration<JarEntry> jarEntries = jar.entries();
            while ( jarEntries.hasMoreElements() )
            {
                JarEntry jarEntry = jarEntries.nextElement();
                for ( int i = 0; i < packagePrefixes.length; i++ )
                {
                    if ( jarEntry.getName().startsWith( packagePrefixes[i] ) )
                    {
                        if ( jarEntry.isDirectory() )
                        {
                            set.add( jarEntry.getName().replace( '/', '.' ) );
                        }
                        else
                        {
                            String entryName = jarEntry.getName();
                            int idx = entryName.lastIndexOf( '/' );
                            if ( idx >= 0 )
                            {
                                entryName = entryName.substring( 0, idx );
                                set.add( entryName.replace( '/', '.' ) );
                            }
                        }
                    }
                }
            }
            
            for ( String packageName : set )
            {
                for ( int i = 0; i < packagePrefixes.length; i++ )
                {
                    if ( packageName.length() == packagePrefixes[ i ].length() )
                    {
                        if ( includeParents )
                            packages.add( packageName );
                    }
                    else
                    {
                        if ( includeSubpackages )
                            packages.add( packageName );
                        else if ( getDepth( packageName, '.' ) == depths[i] + 1 )
                            packages.add( packageName );
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
     * Reads all package names in given packages from a JarFile and puts their names
     * into a List.
     * 
     * @param jarFilename the filename of the jar
     * @param packages the List to put the packages into
     */
    private static void findPackageNamesFromJar( String jarFilename, String[] packageContains, List<String> packages )
    {
        if ( !( new File( jarFilename ).exists() ) )
        {
            System.err.println( "Couldn't find jar file " + jarFilename );
            return;
        }
        
        try
        {
            HashSet<String> set = new HashSet<String>();
            
            for ( int i = 0; i < packageContains.length; i++ )
            {
                set.add( packageContains[i].replace( '/', '.' ) );
            }
            
            jarFilename = new File( jarFilename ).getCanonicalPath();
            JarFile jar = new JarFile( jarFilename );
            Enumeration<JarEntry> jarEntries = jar.entries();
            while ( jarEntries.hasMoreElements() )
            {
                JarEntry jarEntry = jarEntries.nextElement();
                for ( int i = 0; i < packageContains.length; i++ )
                {
                    if ( jarEntry.getName().contains( packageContains[i] ) )
                    {
                        if ( jarEntry.isDirectory() )
                        {
                            set.add( jarEntry.getName().replace( '/', '.' ) );
                        }
                        else
                        {
                            String entryName = jarEntry.getName();
                            int idx = entryName.lastIndexOf( '/' );
                            if ( idx >= 0 )
                            {
                                entryName = entryName.substring( 0, idx );
                                set.add( entryName.replace( '/', '.' ) );
                            }
                        }
                    }
                }
            }
            
            for ( String packageName : set )
            {
                packages.add( packageName );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Reads all sub-packages from given packages into a List.
     * 
     * @param includeParents include the packages from the passed-in list themselfs into the result list
     * @param includeSubpackages include the packages not directly in the searched packages into the result list
     * @param packagePrefixes dot separated package prefix names (like "org.xith3d.test")
     * 
     * @return the filled up List
     */
    public static List<String> findPackages( boolean includeParents, boolean includeSubpackages, String... packagePrefixes )
    {
        String[] packagePrifixes_slash = new String[ packagePrefixes.length ];
        String[] packagePrifixes_separator = new String[ packagePrefixes.length ];
        String[] packageContains_slash = new String[ packagePrefixes.length ];
        String[] packageContains_separator = new String[ packagePrefixes.length ];
        int numPrefixes = 0;
        int numContains = 0;
        for ( int i = 0; i < packagePrefixes.length; i++ )
        {
            String prefixSlash = packagePrefixes[i].replace( '.', '/' );
            String prefixSeparator = packagePrefixes[i].replace( '.', File.separatorChar );
            
            if ( prefixSlash.startsWith( "*" ) )
            {
                if ( prefixSlash.endsWith( "*" ) )
                {
                    packageContains_slash[numContains] = prefixSlash.substring( 1, prefixSlash.length() - 1 );
                    packageContains_separator[numContains] = prefixSeparator.substring( 1, prefixSeparator.length() - 1 );
                }
                else
                {
                    packageContains_slash[numContains] = prefixSlash.substring( 1 );
                    packageContains_separator[numContains] = prefixSeparator.substring( 1 );
                }
                numContains++;
            }
            else if ( prefixSlash.endsWith( "*" ) )
            {
                packageContains_slash[numContains] = prefixSlash.substring( 0, prefixSlash.length() - 1 );
                packageContains_separator[numContains] = prefixSeparator.substring( 0, prefixSeparator.length() - 1 );
                numContains++;
            }
            else
            {
                packagePrifixes_slash[numPrefixes] = prefixSlash;
                packagePrifixes_separator[numPrefixes] = prefixSeparator;
                
                numPrefixes++;
            }
        }
        
        if ( numPrefixes < packagePrefixes.length )
        {
            String[] tmp = new String[ numPrefixes ];
            System.arraycopy( packagePrifixes_slash, 0, tmp, 0, numPrefixes );
            packagePrifixes_slash = tmp;
            tmp = new String[ numPrefixes ];
            System.arraycopy( packagePrifixes_separator, 0, tmp, 0, numPrefixes );
            packagePrifixes_separator = tmp;
        }
        
        if ( numContains < packagePrefixes.length )
        {
            String[] tmp = new String[ numContains ];
            System.arraycopy( packageContains_slash, 0, tmp, 0, numContains );
            packageContains_slash = tmp;
            tmp = new String[ numContains ];
            System.arraycopy( packageContains_separator, 0, tmp, 0, numContains );
            packageContains_separator = tmp;
        }
        
        List<String> packages = new ArrayList<String>();
        
        String[] classPath = System.getProperty( "java.class.path" ).split( System.getProperty( "path.separator" ) );
        for ( String cp: classPath )
        {
            if ( cp.endsWith( ".jar" ) )
            {
                if ( numPrefixes > 0 )
                    findPackageNamesFromJar( cp, packagePrifixes_slash, includeParents, includeSubpackages, packages );
                
                if ( numContains > 0 )
                    findPackageNamesFromJar( cp, packageContains_slash, packages );
            }
            else
            {
                if ( numPrefixes > 0 )
                    findPackageNamesFromFolder( cp, packagePrifixes_separator, includeParents, includeSubpackages, packages );
                
                if ( numContains > 0 )
                    findPackageNamesFromFolder( new File( cp ).getAbsolutePath() + File.separator, new File( cp ), packageContains_separator, packages );
            }
        }
        
        Collections.sort( packages, new Comparator<String>()
        {
            @Override
            public int compare( String p1, String p2 )
            {
                return ( p1.compareTo( p2 ) );
            }
        } );
        
        return ( packages );
    }
    
    /**
     * Reads all sub-packages from given packages into a List.
     * 
     * @param packagePrefixes dot separated package prefix names (like "org.xith3d.test")
     * 
     * @return the filled up List
     */
    public static List<String> findPackages( String... packagePrefixes )
    {
        return ( findPackages( true, true, packagePrefixes ) );
    }
    
    private PackageSearcher()
    {
    }
}
