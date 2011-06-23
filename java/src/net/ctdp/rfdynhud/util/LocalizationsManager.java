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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.widgets.base.widget.Widget;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

public class LocalizationsManager
{
    private final HashMap<String, String> map = new HashMap<String, String>();
    private final HashMap<String, Boolean> missingLoggedMap = new HashMap<String, Boolean>();
    
    private String[] readCodepageAndLanguage( final File f )
    {
        final String[] result = { null, null };
        
        try
        {
            new AbstractIniParser()
            {
                private boolean generalGroupFound = false;
                
                @Override
                protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
                {
                    if ( group.equals( "GENERAL" ) )
                    {
                        generalGroupFound = true;
                    }
                    else if ( generalGroupFound )
                    {
                        //Logger.log( "Invalid localizations file \"" + f.getAbsolutePath() + "\"." );
                        
                        return ( false );
                    }
                    
                    return ( true );
                }
                
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    if ( "GENERAL".equals( group ) )
                    {
                        if ( key.equals( "codepage" ) )
                            result[0] = value;
                        else if ( key.equals( "language" ) )
                            result[1] = value;
                    }
                    
                    return ( ( result[0] == null ) || ( result[1] == null ) );
                }
            }.parse( f );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        return ( result );
    }
    
    private void readFile( File f, String usedLanguage )
    {
        String[] result = readCodepageAndLanguage( f );
        String codepage = result[0];
        String language = result[1];
        
        if ( language == null )
        {
            RFDHLog.exception( "Invalid localizations file \"" + f.getAbsolutePath() + "\"." );
            return;
        }
        
        if ( !language.equals( usedLanguage ) )
        {
            return;
        }
        
        AbstractIniParser iniParser = new AbstractIniParser()
        {
            private boolean isLocalizationsGroup = false;
            
            @Override
            protected boolean onGroupParsed( int lineNr, String group ) throws ParsingException
            {
                isLocalizationsGroup = !group.equals( "GENERAL" );
                
                return ( true );
            }
            
            @Override
            protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
            {
                if ( isLocalizationsGroup )
                {
                    map.put( group + "." + key, value );
                }
                
                return ( true );
            }
        };
        
        try
        {
            iniParser.parse( f, codepage );
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    private void update( PluginINI pluginINI, File baseFolder )
    {
        String usedLanguage = pluginINI.getGeneralLanguage();
        ArrayList<File> files = new ArrayList<File>();
        
        for ( File f : baseFolder.listFiles() )
        {
            if ( f.isDirectory() )
            {
                files.clear();
                
                for ( File f2 : f.listFiles() )
                {
                    if ( f2.getName().startsWith( "localizations_" ) && f2.getName().endsWith( ".ini" ) )
                    {
                        files.add( f2 );
                    }
                }
                
                Collections.sort( files );
                
                for ( File f2 : files )
                {
                    readFile( f2, usedLanguage );
                }
            }
        }
    }
    
    void update( GameFileSystem fileSystem )
    {
        if ( map.size() > 0 )
            return;
        
        if ( ResourceManager.isCompleteIDEMode() )
            update( fileSystem.getPluginINI(), fileSystem.getPluginFolder() );
        else
            update( fileSystem.getPluginINI(), new File( fileSystem.getPluginFolder(), "widget_sets" ) );
    }
    
    public final String getLocalization( Class<? extends Widget> widgetClass, String key )
    {
        //update();
        
        String globalKey = widgetClass.getName() + "." + key;
        String value = map.get( globalKey );
        
        if ( value == null )
        {
            if ( missingLoggedMap.get( globalKey ) != Boolean.TRUE )
            {
                RFDHLog.exception( "WARNING: Localization key \"" + key + "\" missing for " + widgetClass.getName() + "." );
                
                missingLoggedMap.put( globalKey, Boolean.TRUE );
            }
            
            return ( key );
        }
        
        return ( value );
    }
    
    private LocalizationsManager()
    {
        //update( GameFileSystem.INSTANCE );
    }
    
    public static final LocalizationsManager INSTANCE = new LocalizationsManager();
}
