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
    private static HashMap<String, String> threeLetterCodes = null;
    
    /**
     * Gets the three-letter-code assigned to the given driver-name.
     * 
     * @param driverName
     * 
     * @return the three-letter-code or null, if the driver-name was not found in the map.
     */
    public static String getThreeLetterCode( String driverName )
    {
        if ( threeLetterCodes == null )
        {
            threeLetterCodes = new HashMap<String, String>();
            
            String ini = RFactorTools.CONFIG_PATH + File.separator + "three_letter_codes.ini";
            
            try
            {
                new AbstractIniParser()
                {
                    protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                    {
                        threeLetterCodes.put( key, value );
                        
                        return ( true );
                    }
                }.parse( ini );
            }
            catch ( Throwable t )
            {
                t.printStackTrace();
            }
        }
        
        String tlc = threeLetterCodes.get( driverName );
        
        if ( tlc == null )
            return ( driverName );
        
        return ( tlc );
    }
}
