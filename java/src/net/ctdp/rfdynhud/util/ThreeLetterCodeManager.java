package net.ctdp.rfdynhud.util;

import java.io.File;
import java.util.HashMap;

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
    private static final String INI_FILENAME = "three_letter_codes.ini";
    
    private static HashMap<String, String> threeLetterCodes = null;
    private static HashMap<String, String> shortForms = null;
    private static long lastModified = -1L;
    
    private static String generateThreeLetterCode( String driverName )
    {
        if ( driverName.length() <= 3 )
        {
            String tlc = driverName.toUpperCase();
            threeLetterCodes.put( driverName, tlc );
            
            return ( tlc );
        }
        
        int sp = driverName.lastIndexOf( ' ' );
        if ( sp == -1 )
        {
            String tlc = driverName.substring( 0, 3 ).toUpperCase();
            threeLetterCodes.put( driverName, tlc );
            
            return ( tlc );
        }
        
        String tlc = driverName.charAt( 0 ) + driverName.substring( sp + 1, Math.min( sp + 3, driverName.length() ) ).toUpperCase();
        threeLetterCodes.put( driverName, tlc );
        
        return ( tlc );
    }
    
    private static String generateShortForm( String driverName )
    {
        int sp = driverName.lastIndexOf( ' ' );
        if ( sp == -1 )
        {
            shortForms.put( driverName, driverName );
            
            return ( driverName );
        }
        
        String sf = driverName.charAt( 0 ) + ". " + driverName.substring( sp + 1 );
        shortForms.put( driverName, sf );
        
        return ( sf );
    }
    
    public static void updateThreeLetterCodes()
    {
        File ini = new File( RFactorTools.CONFIG_PATH, INI_FILENAME );
        if ( !ini.exists() )
        {
            Logger.log( "WARNING: No " + INI_FILENAME + " found." );
            
            if ( threeLetterCodes == null )
                threeLetterCodes = new HashMap<String, String>();
            else
                threeLetterCodes.clear();
            
            if ( shortForms == null )
                shortForms = new HashMap<String, String>();
            else
                shortForms.clear();
            
            return;
        }
        
        if ( ini.lastModified() > lastModified )
        {
            lastModified = ini.lastModified();
            
            if ( threeLetterCodes == null )
                threeLetterCodes = new HashMap<String, String>();
            else
                threeLetterCodes.clear();
            
            if ( shortForms == null )
                shortForms = new HashMap<String, String>();
            else
                shortForms.clear();
            
            try
            {
                new AbstractIniParser()
                {
                    protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                    {
                        threeLetterCodes.put( key, value );
                        
                        shortForms.put( key, generateShortForm( key ) );
                        
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
    
    /**
     * Gets the three-letter-code assigned to the given driver-name.
     * If there is no entry in the three_letter_codes.ini, it wil be generated and a warning will be dumped to the log.
     * 
     * @param driverName
     * 
     * @return the three-letter-code.
     */
    public static String getThreeLetterCode( String driverName )
    {
        if ( threeLetterCodes == null )
            threeLetterCodes = new HashMap<String, String>();
        
        String tlc = threeLetterCodes.get( driverName );
        
        if ( tlc == null )
        {
            tlc = generateThreeLetterCode( driverName );
            
            Logger.log( "WARNING: No three letter code found for driver \"" + driverName + "\" in the " + INI_FILENAME + ". Generated \"" + tlc + "\"." );
            
            return ( tlc );
        }
        
        return ( tlc );
    }
    
    /**
     * Gets the short form assigned to the given driver-name.
     * If there is no entry in the three_letter_codes.ini, it wil be generated and a warning will be dumped to the log.
     * 
     * @param driverName
     * 
     * @return the short form.
     */
    public static String getShortForm( String driverName )
    {
        if ( shortForms == null )
            shortForms = new HashMap<String, String>();
        
        String sf = shortForms.get( driverName );
        
        if ( sf == null )
        {
            sf = generateShortForm( driverName );
            
            Logger.log( "WARNING: No entry found for driver \"" + driverName + "\" in the " + INI_FILENAME + ". Generated short form \"" + sf + "\"." );
            
            return ( sf );
        }
        
        return ( sf );
    }
}
