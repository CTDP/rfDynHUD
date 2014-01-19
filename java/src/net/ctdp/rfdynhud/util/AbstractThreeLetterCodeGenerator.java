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
package net.ctdp.rfdynhud.util;


/**
 * Standard implementation of the {@link ThreeLetterCodeGenerator}.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class AbstractThreeLetterCodeGenerator implements ThreeLetterCodeGenerator
{
    /**
     * This method is called from {@link #generateThreeLetterCode(String)}, if and only if the driverName contains a space and one character before it and after it.
     * 
     * @param driverName the complete driver's name
     * @param lastSpacePos the last space position in the driverName
     * 
     * @return the generated short form
     */
    protected abstract String generateThreeLetterCodeFromForeAndLastName( String driverName, int lastSpacePos );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String generateThreeLetterCode( String driverName )
    {
        if ( driverName.length() <= 3 )
        {
            if ( ( driverName.length() == 3 ) && ( driverName.charAt( 1 ) == ' ' ) )
                return ( driverName.charAt( 0 ) + "" + driverName.charAt( 2 ) );
            
            return ( driverName );
        }
        
        int sp = driverName.lastIndexOf( ' ' );
        if ( sp == -1 )
        {
            return ( driverName.substring( 0, 3 ) );
        }
        
        return ( generateThreeLetterCodeFromForeAndLastName( driverName, sp ) );
    }
    
    /**
     * This method is called from {@link #generateShortForm(String)}, if and only if the driverName contains a space and one character before it and after it.
     * 
     * @param driverName the complete driver's name
     * @param lastSpacePos the last space position in the driverName
     * 
     * @return the generated short form
     */
    protected String generateShortFormFromForeAndLastName( String driverName, int lastSpacePos )
    {
        String sf = driverName.charAt( 0 ) + ". " + driverName.substring( lastSpacePos + 1 );
        
        return ( sf );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String generateShortForm( String driverName )
    {
        int sp = driverName.lastIndexOf( ' ' );
        if ( sp == -1 )
        {
            return ( driverName );
        }
        
        return ( generateShortFormFromForeAndLastName( driverName, sp ) );
    }
    
    private static final ThreeLetterCodeGenerator getDefaultTLC()
    {
        return ( new ThreeLetterCodeGeneratorForename1Lastname2() );
    }
    
    /**
     * Tries to instantiate the class given by name. If it exists, is accessible/instantiatable and implements {@link ThreeLetterCodeGenerator}, the instance is returned.
     * Otherwise the default implementation is returned.
     * 
     * @param className the class name for the {@link ThreeLetterCodeGenerator}
     * 
     * @return the initialized instance.
     */
    public static final ThreeLetterCodeGenerator initThreeLetterCodeGenerator( String className )
    {
        if ( className == null )
            return ( getDefaultTLC() );
        
        Class<?> clazz = null;
        
        try
        {
            clazz = Class.forName( className );
        }
        catch ( ClassNotFoundException e )
        {
            RFDHLog.exception( "WARNING: ", "Class ", className, " not found through standard ClassLoader. Using default ThreeLetterCodeGenerator." );
            
            return ( getDefaultTLC() );
        }
        
        if ( !ThreeLetterCodeGenerator.class.isAssignableFrom( clazz ) )
        {
            RFDHLog.exception( "WARNING: ", "Class ", className, " doesn't implement net.ctdp.rfdynhud.util.ThreeLetterCodeGenerator. Using default implementation." );
            
            return ( getDefaultTLC() );
        }
        
        try
        {
            return ( (ThreeLetterCodeGenerator)clazz.newInstance() );
        }
        catch ( InstantiationException e )
        {
            RFDHLog.exception( "WARNING: ", "Cannot instantiate class ", className, ". Using default ThreeLetterCodeGenerator." );
            
            return ( getDefaultTLC() );
        }
        catch ( IllegalAccessException e )
        {
            RFDHLog.exception( "WARNING: ", "Cannot instantiate class ", className, ". Using default ThreeLetterCodeGenerator." );
            
            return ( getDefaultTLC() );
        }
    }
}
