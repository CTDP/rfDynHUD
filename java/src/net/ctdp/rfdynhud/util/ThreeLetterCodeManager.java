package net.ctdp.rfdynhud.util;

import java.io.File;
import java.util.HashMap;

import net.ctdp.rfdynhud.gamedata.RFactorFileSystem;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;

/**
 * The {@link ThreeLetterCodeManager} loads name-to-code mappings from an
 * ini file and provides the information to the user.
 * 
 * @author Marvin Froehlich
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
    
    private static void resetMaps()
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
    
    private static String generateThreeLetterCode( String driverName, Integer driverID )
    {
        if ( driverName.length() <= 3 )
        {
            return ( addTLC( driverName, driverID, driverName.toUpperCase() ) );
        }
        
        int sp = driverName.lastIndexOf( ' ' );
        if ( sp == -1 )
        {
            return ( addTLC( driverName, driverID, driverName.substring( 0, 3 ).toUpperCase() ) );
        }
        
        String tlc = driverName.charAt( 0 ) + driverName.substring( sp + 1, Math.min( sp + 3, driverName.length() ) ).toUpperCase();
        
        return ( addTLC( driverName, driverID, tlc ) );
    }
    
    private static String generateShortForm( String driverName, Integer driverID )
    {
        int sp = driverName.lastIndexOf( ' ' );
        if ( sp == -1 )
        {
            return ( addShortForm( driverName, driverID, driverName ) );
        }
        
        String sf = driverName.charAt( 0 ) + ". " + driverName.substring( sp + 1 );
        
        return ( addShortForm( driverName, driverID, sf ) );
    }
    
    public static void updateThreeLetterCodes()
    {
        try
        {
            File ini = new File( RFactorFileSystem.CONFIG_FOLDER, INI_FILENAME );
            if ( !ini.exists() )
            {
                Logger.log( "WARNING: No " + INI_FILENAME + " found." );
                
                resetMaps();
                
                return;
            }
            
            if ( ini.lastModified() > lastModified )
            {
                lastModified = ini.lastModified();
                
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
                                    generateShortForm( key, null );
                                }
                            }
                            else
                            {
                                addTLC( key, null, value );
                                
                                generateShortForm( key, null );
                            }
                            
                            return ( true );
                        }
                    }.parse( ini );
                }
                catch ( Throwable t )
                {
                    Logger.log( t );
                }
            }
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    /**
     * Gets the three-letter-code assigned to the given driver-name.
     * If there is no entry in the three_letter_codes.ini, it wil be generated and a warning will be dumped to the log.
     * 
     * @param driverName
     * @param driverID
     * 
     * @return the three-letter-code.
     */
    public static String getThreeLetterCode( String driverName, Integer driverID )
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
        
        String tlc = generateThreeLetterCode( driverName, driverID );
        
        Logger.log( "WARNING: No three letter code found for driver \"" + driverName + "\" in the " + INI_FILENAME + ". Generated \"" + tlc + "\"." );
        
        return ( tlc );
    }
    
    /**
     * Gets the short form assigned to the given driver-name.
     * If there is no entry in the three_letter_codes.ini, it wil be generated and a warning will be dumped to the log.
     * 
     * @param driverName
     * @param driverID
     * 
     * @return the short form.
     */
    public static String getShortForm( String driverName, Integer driverID )
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
        
        String sf = generateShortForm( driverName, driverID );
        
        //Logger.log( "WARNING: No entry found for driver \"" + driverName + "\" in the " + INI_FILENAME + ". Generated short form \"" + sf + "\"." );
        
        return ( sf );
    }
}
