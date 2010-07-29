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
package net.ctdp.rfdynhud.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import net.ctdp.rfdynhud.gamedata.ByteUtil;
import net.ctdp.rfdynhud.util.Logger;

/**
 * This manager keeps information about plugged in Joysticks (Wheels) and their buttons.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class InputDeviceManager
{
    private static final int MAX_JOYSTICK_NAME_LENGTH = 254;
    private static final int MAX_JOYSTICK_BUTTON_NAME_LENGTH = 64;
    
    private int numKeys = 0;
    private String[] keyNames = null;
    
    private int numJoysticks = 0;
    private String[] joystickNames = null;
    private int[] numButtons = null;
    private String[][] buttonNames = null;
    
    private int numComponents = 0;
    private int[] indexOffsets = null;
    
    public final int getNumKeys()
    {
        return ( numKeys );
    }
    
    public final String getKeyName( int i )
    {
        if ( keyNames == null )
            return ( null );
        
        if ( ( i < 0 ) || ( i >= keyNames.length ) )
            return ( null );
        
        return ( keyNames[i] );
    }
    
    public final int getKeyIndex( String name )
    {
        if ( keyNames == null )
            return ( -1 );
        
        for ( int i = 0; i < numKeys; i++ )
        {
            if ( keyNames[i].equalsIgnoreCase( name ) )
                return ( i );
        }
        
        return ( -1 );
    }
    
    private static HashMap<String, Integer> englishToIndex = new HashMap<String, Integer>();
    private static HashMap<Integer, String> indexToEnglish = new HashMap<Integer, String>();
    static
    {
        try
        {
            InputStream is = InputDeviceManager.class.getClassLoader().getResourceAsStream( InputDeviceManager.class.getPackage().getName().replace( '.', '/' ) + "/dinput.h" );
            BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                line = line.trim();
                
                if ( line.length() == 0 )
                    continue;
                
                if ( line.startsWith( "#define" ) )
                {
                    line = line.substring( 7 ).trim();
                    
                    int p1 = line.indexOf( ' ' );
                    String name = line.substring( 0, p1 );
                    if ( name.startsWith( "DIK_" ) )
                        name = name.substring( 4 );
                    line = line.substring( p1 ).trim();
                    p1 = line.indexOf( ' ' );
                    String indexStr = ( p1 < 0 ) ? line : line.substring( 0, p1 );
                    int index = indexStr.startsWith( "0x" ) ? Integer.parseInt( indexStr.substring( 2 ), 16 ) : Integer.parseInt( indexStr, 16 );
                    
                    if ( index >= 0 )
                    {
                        englishToIndex.put( name, index );
                        indexToEnglish.put( index, name );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
    
    public final String getEnglishKeyName( int i )
    {
        return ( indexToEnglish.get( i ) );
    }
    
    public final int getKeyIndexByEnglishName( String name )
    {
        Integer index = englishToIndex.get( name );
        
        if ( index == null )
            return ( -1 );
        
        return ( index.intValue() );
    }
    
    public final int getNumJoysticks()
    {
        return ( numJoysticks );
    }
    
    public final String getJoystickNameForIni( int i )
    {
        return ( "Joystick" + ( i + 1 ) );
    }
    
    public final String getJoystickButtonNameForIni( int i )
    {
        return ( "Button" + ( i + 1 ) );
    }
    
    public final String getJoystickName( int i )
    {
        if ( joystickNames == null )
            return ( null );
        
        if ( ( i < 0 ) || ( i >= joystickNames.length ) )
            return ( null );
        
        return ( joystickNames[i] );
    }
    
    public final int getJoystickIndex( String name )
    {
        for ( int i = 1; i <= numJoysticks; i++ )
        {
            if ( name.equals( "Joystick" + i ) )
                return ( i - 1 );
        }
        
        if ( joystickNames == null )
            return ( -1 );
        
        for ( int i = 0; i < numJoysticks; i++ )
        {
            if ( joystickNames[i].equalsIgnoreCase( name ) )
                return ( i );
        }
        
        return ( -1 );
    }
    
    public final int getNumJoystickButtons( int joystickIndex )
    {
        return ( numButtons[joystickIndex] );
    }
    
    public final String getJoystickButtonName( int joystickIndex, int i )
    {
        return ( buttonNames[joystickIndex][i] );
    }
    
    public final int getJoystickButtonIndex( int joystickIndex, String name )
    {
        for ( int i = 1; i <= numButtons[joystickIndex]; i++ )
        {
            if ( name.equalsIgnoreCase( "Button" + i ) )
                return ( i - 1 );
        }
        
        for ( int i = 0; i < numButtons[joystickIndex]; i++ )
        {
            if ( buttonNames[joystickIndex][i].equalsIgnoreCase( name ) )
                return ( i );
        }
        
        return ( -1 );
    }
    
    public final int getNumComponents()
    {
        return ( numComponents );
    }
    
    public final int getJoystickButtonIndexOffset( int joystickIndex )
    {
        return ( indexOffsets[joystickIndex] );
    }
    
    public void decodeData( byte[] buffer )
    {
        try
        {
            numComponents = 0;
            
            int bufferOffset = 0;
            
            numKeys = ByteUtil.readUnsignedShort( buffer, bufferOffset );
            bufferOffset += ByteUtil.SIZE_SHORT;
            int maxKeyNameLength = ByteUtil.readUnsignedByte( buffer, bufferOffset );
            bufferOffset += ByteUtil.SIZE_CHAR;
            
            keyNames = new String[ numKeys ];
            
            for ( int i = 0; i < numKeys; i++ )
            {
                keyNames[i] = ByteUtil.readString( buffer, bufferOffset, maxKeyNameLength ).trim();
                bufferOffset += maxKeyNameLength + 1;
            }
            
            numComponents += numKeys;
            
            numComponents += 20; // reserved for Mouse buttons
            
            numJoysticks = ByteUtil.readUnsignedByte( buffer, bufferOffset );
            bufferOffset += ByteUtil.SIZE_CHAR;
            joystickNames = new String[ numJoysticks ];
            numButtons = new int[ numJoysticks ];
            buttonNames = new String[ numJoysticks ][];
            indexOffsets = new int[ numJoysticks ];
            
            int buttonOffset = bufferOffset + numJoysticks * MAX_JOYSTICK_NAME_LENGTH;
            for ( int i = 0; i < numJoysticks; i++ )
            {
                joystickNames[i] = ByteUtil.readString( buffer, bufferOffset + i * MAX_JOYSTICK_NAME_LENGTH, MAX_JOYSTICK_NAME_LENGTH ).trim();
                
                numButtons[i] = ByteUtil.readUnsignedByte( buffer, buttonOffset );
                buttonOffset++;
                
                indexOffsets[i] = numComponents;
                numComponents += numButtons[i];
                
                buttonNames[i] = new String[ numButtons[i] ];
                
                for ( int j = 0; j < numButtons[i]; j++ )
                {
                    buttonNames[i][j] = ByteUtil.readString( buffer, buttonOffset, MAX_JOYSTICK_BUTTON_NAME_LENGTH ).trim();
                    buttonOffset += MAX_JOYSTICK_BUTTON_NAME_LENGTH;
                }
            }
            
            /*
            Logger.log( "Num Keys: " + numKeys );
            Logger.log( "Max Keyname length: " + maxKeyNameLength );
            
            for ( int i = 0; i < numKeys; i++ )
            {
                Logger.log( "Key Name[" + i + "]: " + keyNames[i] );
            }
            
            Logger.log( "Num Joysticks: " + numJoysticks );
            
            for ( int i = 0; i < numJoysticks; i++ )
            {
                Logger.log( "Joystick-Name[" + i + "]: " + joystickNames[i] );
                Logger.log( "Num Buttons[" + i + "]: " + numButtons[i] );
                
                for ( int j = 0; j < numButtons[i]; j++ )
                {
                    Logger.log( "Button-Name[" + i + "][" + j + "]: " + buttonNames[i][j] );
                }
            }
            */
        }
        catch ( Throwable t )
        {
            Logger.log( t );
        }
    }
    
    public InputDeviceManager()
    {
    }
}
