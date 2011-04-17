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
package org.jagatoo.util.ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.io.FileUtils;

/**
 * Read-only interface to an ini file.
 * 
 * @author Marvin Froehlich (aka Qudus)
 */
public class IniFile
{
    private final HashMap<String, HashMap<String, String>> settings = new HashMap<String, HashMap<String, String>>();
    private final ArrayList<String> groupsOrder = new ArrayList<String>();
    private final HashMap<String, ArrayList<String>> settingsOrder = new HashMap<String, ArrayList<String>>();
    
    public static final boolean DEFAULT_CASE_SENSITIVITY = true;
    
    private final File file;
    private final boolean caseSensitivity;
    
    private long lastModified = -1L;
    
    private void addGroup( String group )
    {
        if ( caseSensitivity )
        {
            settings.put( group, new HashMap<String, String>() );
            groupsOrder.add( group );
            settingsOrder.put( group, new ArrayList<String>() );
        }
        else
        {
            String groupLC = ( group == null ) ? null : group.toLowerCase();
            
            settings.put( groupLC, new HashMap<String, String>() );
            groupsOrder.add( group );
            settingsOrder.put( groupLC, new ArrayList<String>() );
        }
    }
    
    private void parse() throws IOException, ParsingException
    {
        new AbstractIniParser()
        {
            @Override
            protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
            {
                addGroup( group );
                
                return ( true );
            }
            
            @Override
            protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
            {
                if ( ( group == null ) && !settingsOrder.containsKey( null ) )
                {
                    addGroup( group );
                }
                
                if ( caseSensitivity )
                {
                    settings.get( group ).put( key, value );
                    settingsOrder.get( group ).add( key );
                }
                else
                {
                    String groupLC = ( group == null ) ? null : group.toLowerCase();
                    
                    settings.get( groupLC ).put( key.toLowerCase(), value );
                    settingsOrder.get( groupLC ).add( key );
                }
                
                return ( true );
            }
        }.parse( file );
    }
    
    /**
     * Checks, if the ini file has bee modified since the last refresh and updates in case.
     * 
     * @return whether a refresh as necessary.
     * 
     * @throws IOException
     * @throws ParsingException
     */
    public boolean refresh() throws IOException, ParsingException
    {
        if ( !file.exists() )
            throw new FileNotFoundException( file.getAbsolutePath() );
        
        if ( lastModified != file.lastModified() )
        {
            lastModified = file.lastModified();
            
            settings.clear();
            groupsOrder.clear();
            settingsOrder.clear();
            
            parse();
            
            return ( true );
        }
        
        return ( false );
    }
    
    /**
     * Gets the settings's value for the given group and key.
     * 
     * @param group
     * @param key
     * 
     * @return the settings's value for the given group and key or <code>null</code>, if not found.
     */
    public final boolean settingExists( String group, String key )
    {
        String group_ = ( group == null ) ? null : ( caseSensitivity ? group : group.toLowerCase() );
        HashMap<String, String> map = settings.get( group_ );
        
        if ( map == null )
            return ( false );
        
        return ( map.containsKey( caseSensitivity ? key : key.toLowerCase() ) );
    }
    
    /**
     * Gets the settings's value for the given group and key.
     * 
     * @param group
     * @param key
     * @param defaultValue the value to be returned, if the settings does not exist
     * 
     * @return the settings's value for the given group and key or 'defaultValue', if not found.
     */
    public final String getSetting( String group, String key, String defaultValue )
    {
        String group_ = ( group == null ) ? null : ( caseSensitivity ? group : group.toLowerCase() );
        HashMap<String, String> map = settings.get( group_ );
        
        if ( map == null )
            return ( defaultValue );
        
        String value = map.get( caseSensitivity ? key : key.toLowerCase() );
        
        if ( value == null )
            return ( defaultValue );
        
        return ( value );
    }
    
    /**
     * Gets the settings's value for the given group and key.
     * 
     * @param group
     * @param key
     * 
     * @return the settings's value for the given group and key or <code>null</code>, if not found.
     */
    public final String getSetting( String group, String key )
    {
        String group_ = ( group == null ) ? null : ( caseSensitivity ? group : group.toLowerCase() );
        HashMap<String, String> map = settings.get( group_ );
        
        if ( map == null )
            return ( null );
        
        return ( map.get( caseSensitivity ? key : key.toLowerCase() ) );
    }
    
    /**
     * Gets the number of groups.
     * 
     * @return the number of groups.
     */
    public final int getNumGroups()
    {
        return ( groupsOrder.size() );
    }
    
    /**
     * Gets the index'th group name.
     * 
     * @param index
     * 
     * @return the index'th group name.
     */
    public final String getGroup( int index )
    {
        return ( groupsOrder.get( index ) );
    }
    
    /**
     * Gets an unmodifiable list of the group names.
     * 
     * @return an unmodifiable list of the group names.
     */
    public final List<String> getGroups()
    {
        return ( Collections.unmodifiableList( groupsOrder ) );
    }
    
    /**
     * Gets the number of settings in the given group.
     * If the given group does not exist, -1 is returned.
     * 
     * @param group
     * 
     * @return the number of settings in the given group or -1.
     */
    public final int getNumSettings( String group )
    {
        String group_ = ( group == null ) ? null : ( caseSensitivity ? group : group.toLowerCase() );
        
        ArrayList<String> list = settingsOrder.get( group_ );
        
        if ( list == null )
            return ( -1 );
        
        return ( list.size() );
    }
    
    /**
     * Gets the index'th setting key in the passed group.
     * 
     * @param group
     * @param index
     * 
     * @return the index'th setting key in the passed group.
     */
    public final String getKey( String group, int index )
    {
        String group_ = ( group == null ) ? null : ( caseSensitivity ? group : group.toLowerCase() );
        
        ArrayList<String> list = settingsOrder.get( group_ );
        
        if ( list == null )
            return ( null );
        
        return ( list.get( index ) );
    }
    
    /**
     * Gets an unmodifiable list of the key names.
     * 
     * @param group
     * 
     * @return an unmodifiable list of the key names.
     */
    public final List<String> getKeys( String group )
    {
        String group_ = ( group == null ) ? null : ( caseSensitivity ? group : group.toLowerCase() );
        
        ArrayList<String> list = settingsOrder.get( group_ );
        
        if ( list == null )
            return ( null );
        
        return ( Collections.unmodifiableList( list ) );
    }
    
    /**
     * Creates a new ini file interface. The constructor doesn't call the {@link #refresh()} method.
     * 
     * @param file
     * @param caseSensitivity
     */
    public IniFile( File file, boolean caseSensitivity )
    {
        if ( file == null )
            throw new IllegalArgumentException( "file must not be null." );
        
        this.file = file;
        this.caseSensitivity = caseSensitivity;
    }
    
    /**
     * Creates a new ini file interface. The constructor doesn't call the {@link #refresh()} method.
     * 
     * @param filename
     * @param caseSensitivity
     */
    public IniFile( String filename, boolean caseSensitivity )
    {
        this( FileUtils.getCanonicalFile( new File( filename ) ), caseSensitivity );
    }
    
    /**
     * Creates a new ini file interface. The constructor doesn't call the {@link #refresh()} method.
     * 
     * @param file
     */
    public IniFile( File file )
    {
        this( file, DEFAULT_CASE_SENSITIVITY );
    }
    
    /**
     * Creates a new ini file interface. The constructor doesn't call the {@link #refresh()} method.
     * 
     * @param filename
     */
    public IniFile( String filename )
    {
        this( filename, DEFAULT_CASE_SENSITIVITY );
    }
}
