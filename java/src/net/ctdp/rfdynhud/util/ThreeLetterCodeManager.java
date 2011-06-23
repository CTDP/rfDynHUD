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
import java.util.HashMap;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * The {@link ThreeLetterCodeManager} loads name-to-code mappings from an
 * ini file and provides the information to the user.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class ThreeLetterCodeManager
{
    // TODO: Preserve generated when updating!
    
    private static final String INI_FILENAME = "three_letter_codes.ini";
    
    private static HashMap<String, String> name2TLCMap = null;
    private static HashMap<Integer, String> id2TLCMap = null;
    private static HashMap<String, String> name2ShortFormMap = null;
    private static HashMap<Integer, String> id2ShortFormMap = null;
    
    private static long lastModified = -1L;
    
    private static int updateId = 0;
    
    public static final int getUpdateId()
    {
        return ( updateId );
    }
    
    public static void resetMaps()
    {
        if ( name2TLCMap == null )
            name2TLCMap = new HashMap<String, String>();
        else
            name2TLCMap.clear();
        
        if ( id2TLCMap == null )
            id2TLCMap = new HashMap<Integer, String>();
        else
            id2TLCMap.clear();
        
        if ( name2ShortFormMap == null )
            name2ShortFormMap = new HashMap<String, String>();
        else
            name2ShortFormMap.clear();
        
        if ( id2ShortFormMap == null )
            id2ShortFormMap = new HashMap<Integer, String>();
        else
            id2ShortFormMap.clear();
    }
    
    private static String addTLC( String driverName, Integer driverID, String tlc )
    {
        if ( driverID != null )
        {
            if ( id2TLCMap == null )
                id2TLCMap = new HashMap<Integer, String>();
            
            id2TLCMap.put( driverID, tlc );
        }
        else
        {
            if ( name2TLCMap == null )
                name2TLCMap = new HashMap<String, String>();
            
            name2TLCMap.put( driverName, tlc );
        }
        
        return ( tlc );
    }
    
    private static String addShortForm( String driverName, Integer driverID, String sf )
    {
        if ( driverID != null )
        {
            if ( id2ShortFormMap == null )
                id2ShortFormMap = new HashMap<Integer, String>();
            
            id2ShortFormMap.put( driverID, sf );
        }
        else
        {
            if ( name2ShortFormMap == null )
                name2ShortFormMap = new HashMap<String, String>();
            
            name2ShortFormMap.put( driverName, sf );
        }
        
        return ( sf );
    }
    
    public static void updateThreeLetterCodes( File configFolder, final ThreeLetterCodeGenerator tlcGenerator )
    {
        try
        {
            File ini = new File( configFolder, INI_FILENAME );
            if ( !ini.exists() )
            {
                RFDHLog.exception( "WARNING: No " + INI_FILENAME + " found." );
                
                resetMaps();
                
                return;
            }
            
            if ( ini.lastModified() > lastModified )
            {
                lastModified = ini.lastModified();
                updateId++;
                
                resetMaps();
                
                try
                {
                    new AbstractIniParser()
                    {
                        @Override
                        protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                        {
                            int idx = value.indexOf( ';' );
                            if ( idx >= 0 )
                            {
                                addTLC( key, null, value.substring( 0, idx ) );
                                
                                if ( idx < value.length() - 1 )
                                {
                                    addShortForm( key, null, value.substring( idx + 1 ) );
                                }
                                else
                                {
                                    addShortForm( key, null, tlcGenerator.generateShortForm( key ) );
                                }
                            }
                            else
                            {
                                addTLC( key, null, value );
                                
                                addShortForm( key, null, tlcGenerator.generateShortForm( key ) );
                            }
                            
                            return ( true );
                        }
                    }.parse( ini );
                }
                catch ( Throwable t )
                {
                    RFDHLog.exception( t );
                }
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
    }
    
    /**
     * Gets the three-letter-code assigned to the given driver-name.
     * If there is no entry in the three_letter_codes.ini, it wil be generated and a warning will be dumped to the log.
     * 
     * @param driverName the driver's full name
     * @param driverID the driver's id
     * @param tlcGenerator
     * 
     * @return the three-letter-code.
     */
    public static String getThreeLetterCode( String driverName, Integer driverID, ThreeLetterCodeGenerator tlcGenerator )
    {
        if ( ( driverID != null ) && ( id2TLCMap != null ) )
        {
            String tlc = id2TLCMap.get( driverID );
            
            if ( tlc != null )
                return ( tlc );
        }
        
        if ( name2TLCMap != null )
        {
            String tlc = name2TLCMap.get( driverName );
            
            if ( tlc != null )
            {
                if ( driverID != null )
                {
                    addTLC( driverName, driverID, tlc );
                }
                
                return ( tlc );
            }
        }
        
        String tlc = addTLC( driverName, driverID, tlcGenerator.generateThreeLetterCode( driverName ) );
        
        RFDHLog.printlnEx( "No three letter code found for driver \"" + driverName + "\" in the " + INI_FILENAME + ". Generated \"" + tlc.toUpperCase() + "\"." );
        
        return ( tlc );
    }
    
    /**
     * Gets the short form assigned to the given driver-name.
     * If there is no entry in the three_letter_codes.ini, it wil be generated and a warning will be dumped to the log.
     * 
     * @param driverName the driver's full name
     * @param driverID the driver's id
     * @param tlcGenerator
     * 
     * @return the short form.
     */
    public static String getShortForm( String driverName, Integer driverID, ThreeLetterCodeGenerator tlcGenerator )
    {
        if ( ( driverID != null ) && ( id2ShortFormMap != null ) )
        {
            String sf = id2ShortFormMap.get( driverID );
            
            if ( sf != null )
                return ( sf );
        }
        
        if ( name2ShortFormMap != null )
        {
            String sf = name2ShortFormMap.get( driverName );
            
            if ( sf != null )
            {
                if ( driverID != null )
                {
                    addShortForm( driverName, driverID, sf );
                }
                
                return ( sf );
            }
        }
        
        String sf = addShortForm( driverName, driverID, tlcGenerator.generateShortForm( driverName ) );
        
        //Logger.log( "WARNING: No entry found for driver \"" + driverName + "\" in the " + INI_FILENAME + ". Generated short form \"" + sf + "\"." );
        
        return ( sf );
    }
}
