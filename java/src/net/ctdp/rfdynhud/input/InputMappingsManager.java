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

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.gamedata.GameEventsManager;
import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.RFDHLog;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;


/**
 * This manager manages mappings of {@link InputAction}s to input device components (buttons and keys).
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class InputMappingsManager
{
    public static final String CONFIG_FILE_NAME = "input_bindings.ini";
    
    private final RFDynHUD rfDynHUD;
    
    private InputMapping[] mappings = null;
    
    private ByteBuffer buffer = null;
    private int numDevices = 0;
    private short[] bufferOffsets = null;
    private int[] numComponents = null;
    
    private byte[][] states0 = null;
    private byte[][] states1 = null;
    
    private boolean isPluginEnabled = true;
    
    public final ByteBuffer getBuffer()
    {
        return ( buffer );
    }
    
    public final boolean isPluginEnabled()
    {
        return ( isPluginEnabled );
    }
    
    /*
    public void clearKnownActions()
    {
        knownActions.clear();
    }
    
    public void addKnownActions( InputAction[] actions )
    {
        for ( int i = 0; i < actions.length; i++ )
        {
            knownActions.put( actions[i].getName(), actions[i] );
        }
    }
    */
    
    private static final int parseModifierMask( String[] parts )
    {
        int modifierMask = 0;
        
        for ( int i = 0; i < parts.length - 1; i++ )
        {
            if ( parts[i].equals( "SHIFT" ) )
                modifierMask |= InputMapping.MODIFIER_MASK_SHIFT;
            else if ( parts[i].equals( "CTRL" ) )
                modifierMask |= InputMapping.MODIFIER_MASK_CTRL;
            else if ( parts[i].equals( "LALT" ) )
                modifierMask |= InputMapping.MODIFIER_MASK_LALT;
            else if ( parts[i].equals( "RALT" ) )
                modifierMask |= InputMapping.MODIFIER_MASK_RALT;
            else if ( parts[i].equals( "LMETA" ) )
                modifierMask |= InputMapping.MODIFIER_MASK_LMETA;
            else if ( parts[i].equals( "RMETA" ) )
                modifierMask |= InputMapping.MODIFIER_MASK_RMETA;
        }
        
        return ( modifierMask );
    }
    
    public static String unparseModifierMask( int modifierMask )
    {
        String stringValue = "";
        
        if ( ( modifierMask & InputMapping.MODIFIER_MASK_SHIFT ) != 0 )
            stringValue += "SHIFT+";
        
        if ( ( modifierMask & InputMapping.MODIFIER_MASK_CTRL ) != 0 )
            stringValue += "CTRL+";
        
        if ( ( modifierMask & InputMapping.MODIFIER_MASK_LALT ) != 0 )
            stringValue += "LALT+";
        
        if ( ( modifierMask & InputMapping.MODIFIER_MASK_RALT ) != 0 )
            stringValue += "RALT+";
        
        if ( ( modifierMask & InputMapping.MODIFIER_MASK_LMETA ) != 0 )
            stringValue += "LMETA+";
        
        if ( ( modifierMask & InputMapping.MODIFIER_MASK_RMETA ) != 0 )
            stringValue += "RMETA+";
        
        return ( stringValue );
    }
    
    public static String getComponentNameForTable( InputMapping mapping )
    {
        String stringValue = "";
        
        String[] tmp = mapping.getDeviceComponent().split( "::" );
        
        int v0 = 0;
        if ( tmp.length > 1 )
        {
            stringValue += tmp[0] + "::";
            v0 = 1;
        }
        
        stringValue += unparseModifierMask( mapping.getModifierMask() );
        
        for ( int i = v0; i < tmp.length; i++ )
            stringValue += tmp[i];
        
        return ( stringValue );
    }
    
    public static Object[] parseMapping( int lineNr, String key, String value, InputDeviceManager devManager )
    {
        key = key.trim();
        value = value.trim();
        int keyCode = -1;
        int modifierMask = 0;
        int hitTimes = 1;
        
        String[] keyParts = key.split( "::" );
        String device = keyParts[0];
        String component = keyParts[1];
        
        if ( device.equals( "Keyboard" ) )
        {
            String[] parts2 = component.split( "\\+" );
            
            if ( parts2.length > 1 )
            {
                modifierMask = parseModifierMask( parts2 );
                component = parts2[parts2.length - 1];
            }
            
            String[] parts3 = component.split( "," );
            
            if ( parts3.length > 1 )
            {
                component = parts3[0];
                hitTimes = Integer.parseInt( parts3[1] );
            }
            
            keyCode = devManager.getKeyIndexByEnglishName( parts3[0] );
            if ( keyCode < 0 )
                keyCode = devManager.getKeyIndex( parts3[0] );
            
            if ( keyCode < 0 )
            {
                RFDHLog.exception( "    WARNING: The Keyboard doesn't have a key called \"" + parts3[0] + "\". Skipping mapping in line #" + lineNr + "." );
                return ( null );
            }
            
            component = devManager.getKeyName( keyCode );
        }
        else if ( device.equals( "Mouse" ) )
        {
        }
        else // Joystick
        {
            int jindex = devManager.getJoystickIndex( device );
            
            if ( jindex < 0 )
            {
                RFDHLog.exception( "    WARNING: Joystick \"" + device + "\" not found. Skipping mapping in line #" + lineNr );
                return ( null );
            }
            
            device = devManager.getJoystickName( jindex );
            int bindex;
            
            String[] parts3 = component.split( "," );
            if ( parts3.length > 1 )
            {
                bindex = devManager.getJoystickButtonIndex( jindex, parts3[0] );
                
                if ( bindex < 0 )
                {
                    RFDHLog.exception( "    WARNING: Joystick \"" + device + "\" doesn't have a button called \"" + parts3[0] + "\". Skipping mapping in line #" + lineNr + "." );
                    return ( null );
                }
                
                hitTimes = Integer.parseInt( parts3[1] );
            }
            else
            {
                bindex = devManager.getJoystickButtonIndex( jindex, component );
                
                if ( bindex < 0 )
                {
                    RFDHLog.exception( "    WARNING: Joystick \"" + device + "\" doesn't have a button called \"" + component + "\". Skipping mapping in line #" + lineNr + "." );
                    return ( null );
                }
            }
            
            component = devManager.getJoystickButtonName( jindex, bindex );
        }
        
        
        String[] valueParts = value.split( "::", 2 );
        
        if ( valueParts.length < 2 )
        {
            RFDHLog.exception( "    WARNING: Illegal mapping at line #" + lineNr + ". Skipping." );
            return ( null );
        }
        
        String widgetName = valueParts[0];
        String actionName;
        if ( valueParts.length == 1 )
            actionName = null;
        else
            actionName = valueParts[1];
        
        if ( ( actionName != null ) && ( KnownInputActions.get( actionName ) == null ) )
        {
            RFDHLog.exception( "    WARNING: No InputAction with the name \"" + actionName + "\" found. Skipping line #" + lineNr + "." );
            return ( null );
        }
        
        InputMapping mapping = new InputMapping( widgetName, KnownInputActions.get( actionName ), device + "::" + component, keyCode, modifierMask, hitTimes );
        
        return ( new Object[] { mapping, getComponentNameForTable( mapping ) } );
    }
    
    public InputMappings loadMappings( final GameFileSystem fileSystem, final InputDeviceManager devManager )
    {
        try
        {
            File configFile = new File( fileSystem.getConfigFolder(), CONFIG_FILE_NAME );
            
            final ArrayList<Object[]> rawBindings = new ArrayList<Object[]>();
            String lastDevice = null;
            
            if ( !configFile.exists() || !configFile.canRead() )
            {
                RFDHLog.exception( "    WARNING: No readable " + CONFIG_FILE_NAME + " config file found in the config folder." );
                
                numDevices = 0;
                numComponents = null;
            }
            else
            {
                new AbstractIniParser()
                {
                    @Override
                    protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                    {
                        Object[] tmp = parseMapping( lineNr, key, value, devManager );
                        
                        if ( tmp == null )
                            return ( true );
                        
                        InputMapping mapping = (InputMapping)tmp[0];
                        
                        String device = mapping.getDeviceComponent().split( "::" )[0];
                        String component = mapping.getDeviceComponent().split( "::" )[1];
                        String widgetName = mapping.getWidgetName();
                        String actionName = ( mapping.getAction() == null ) ? null : mapping.getAction().getName();
                        int keyCode = mapping.getKeyCode();
                        int modifierMask = mapping.getModifierMask();
                        int hitTimes = mapping.getHitTimes();
                        
                        rawBindings.add( new Object[] { device, component, widgetName, actionName, keyCode, modifierMask, hitTimes } );
                        
                        return ( true );
                    }
                    
                    @Override
                    protected void onParsingFinished()
                    {
                    }
                }.parse( configFile );
                
                
                Collections.sort( rawBindings, new Comparator<Object[]>()
                {
                    @Override
                    public int compare( Object[] o1, Object[] o2 )
                    {
                        int cmp = ( (String)o1[0] ).compareTo( ( (String)o2[0] ) );
                        if ( cmp != 0 )
                            return ( cmp );
                        
                        cmp = ( (String)o1[1] ).compareTo( (String)o2[1] );
                        if ( cmp != 0 )
                            return ( cmp );
                        
                        return ( 0 );
                    }
                } );
                
                numDevices = 0;
                numComponents = new int[ 256 ];
                int maxNumComponents = 0;
                int nc = 0;
                for ( int i = 0; i < rawBindings.size(); i++ )
                {
                    String device = (String)rawBindings.get( i )[0];
                    if ( !device.equals( lastDevice ) )
                    {
                        if ( numDevices == 1 )
                        {
                            numComponents[0] = nc;
                            if ( numComponents[0] > maxNumComponents )
                                maxNumComponents = numComponents[0];
                        }
                        else if ( numDevices > 1 )
                        {
                            numComponents[numDevices - 1] = nc - numComponents[numDevices - 2];
                            if ( numComponents[numDevices - 1] > maxNumComponents )
                                maxNumComponents = numComponents[numDevices - 1];
                        }
                        
                        numDevices++;
                        lastDevice = device;
                    }
                    
                    nc++;
                }
                
                if ( numDevices == 1 )
                {
                    numComponents[0] = nc;
                    if ( numComponents[0] > maxNumComponents )
                        maxNumComponents = numComponents[0];
                }
                else if ( numDevices > 1 )
                {
                    numComponents[numDevices - 1] = nc - numComponents[numDevices - 2];
                    if ( numComponents[numDevices - 1] > maxNumComponents )
                        maxNumComponents = numComponents[numDevices - 1];
                }
                
                int[] tmp = new int[ numDevices ];
                System.arraycopy( numComponents, 0, tmp, 0, numDevices );
                numComponents = tmp;
            }
            // { numDevices[1], deviceType_0[1], deviceIndex_0[1], arrayOffset_0[2], numComponents_0[1], ... , deviceType_n[1], deviceIndex_n[1], arrayOffset_n[2], numComponents_n[1], componentIndex_0_0[2], ..., componentIndex_0_n[2], state_0_0[1], ..., state_0_n[1], ...... , componentIndex_n_0[2], ..., componentIndex_n_n[2], state_n_0[1], ..., state_n_n[1]
            buffer = ByteBuffer.allocateDirect( 1 + ( 1 + 1 + 2 + 1 ) * numDevices + 3 * rawBindings.size() ).order( ByteOrder.nativeOrder() );
            
            buffer.position( 0 );
            buffer.put( (byte)numDevices );
            
            bufferOffsets = new short[ numDevices ];
            
            lastDevice = null;
            int deviceIndex = 0;
            short arrayOffset = (short)( 1 + ( 1 + 1 + 2 + 1 ) * numDevices );
            for ( int i = 0; i < rawBindings.size(); i++ )
            {
                String device = (String)rawBindings.get( i )[0];
                if ( !device.equals( lastDevice ) )
                {
                    if ( device.equalsIgnoreCase( "Keyboard" ) )
                    {
                        buffer.put( (byte)1 );
                        buffer.put( (byte)0 );
                    }
                    else if ( device.equalsIgnoreCase( "Mouse" ) )
                    {
                        buffer.put( (byte)2 );
                        buffer.put( (byte)0 );
                    }
                    else // Joystick
                    {
                        buffer.put( (byte)3 );
                        buffer.put( (byte)devManager.getJoystickIndex( device ) );
                    }
                    
                    bufferOffsets[deviceIndex] = arrayOffset;
                    buffer.putShort( arrayOffset );
                    buffer.put( (byte)numComponents[deviceIndex] );
                    
                    deviceIndex++;
                    lastDevice = device;
                }
                
                arrayOffset += 3;
            }
            
            
            mappings = new InputMapping[ rawBindings.size() ];
            //pluginEnabledBufferOffset = -1;
            
            lastDevice = null;
            deviceIndex = -1;
            int c = 0;
            for ( int i = 0; i < rawBindings.size(); i++ )
            {
                String device = (String)rawBindings.get( i )[0];
                String component = (String)rawBindings.get( i )[1];
                String widgetName = (String)rawBindings.get( i )[2];
                String actionName = (String)rawBindings.get( i )[3];
                Integer keyCode = (Integer)rawBindings.get( i )[4];
                Integer modifierMask = (Integer)rawBindings.get( i )[5];
                Integer hitTimes = (Integer)rawBindings.get( i )[6];
                
                if ( !device.equals( lastDevice ) )
                {
                    deviceIndex++;
                    c = 0;
                    lastDevice = device;
                }
                
                short componentIndex = 0;
                
                if ( device.equalsIgnoreCase( "Keyboard" ) )
                {
                    //componentIndex = (short)devManager.getKeyIndex( component );
                    componentIndex = (short)keyCode.intValue();
                }
                else if ( device.equalsIgnoreCase( "Mouse" ) )
                {
                    // TODO
                }
                else // Joystick
                {
                    int ji = devManager.getJoystickIndex( device );
                    componentIndex = (short)devManager.getJoystickButtonIndex( ji, component );
                }
                
                //Logger.log( bufferOffsets[deviceIndex] + ", " + c + ", " + buffer.capacity() + ", " + buffer.limit() + ", " + ( bufferOffsets[deviceIndex] + ( c * 2 ) ) );
                buffer.putShort( bufferOffsets[deviceIndex] + ( c * 2 ), componentIndex );
                
                /*
                if ( actionName.equalsIgnoreCase( KnownInputActions.TogglePlugin.getName() ) )
                {
                    pluginEnabledBufferOffset = (short)( bufferOffsets[deviceIndex] + ( numComponents[deviceIndex] & 0xFF ) * 2 + c );
                }
                */
                
                mappings[i] = new InputMapping( widgetName, KnownInputActions.get( actionName ), device + "::" + component, keyCode, modifierMask, hitTimes );
                
                RFDHLog.println( "    Bound \"" + mappings[i].getDeviceComponent() + "\" to \"" + widgetName + "::" + actionName + "\"" );
                
                c++;
            }
            
            states0 = new byte[ numDevices ][];
            states1 = new byte[ numDevices ][];
            for ( int i = 0; i < numDevices; i++ )
            {
                states0[i] = new byte[ numComponents[i] ];
                states1[i] = new byte[ numComponents[i] ];
            }
        }
        catch ( Throwable t )
        {
            RFDHLog.exception( t );
        }
        
        return ( new InputMappings( mappings ) );
    }
    
    private InputAction lastHitAction = null;
    private long firstActionHitTime = -1L;
    private int hitCount = 0;
    
    /**
     * @param eventsManager
     * @param widgetsManager the manager to fire widget events on
     * @param gameData the live game data
     * @param isEditorMode editor mode? (certainly false)
     * @param modifierMask the key modifier mask
     * 
     * @return -1 if plugin got disabled, 0 if plugin was and is disabled, 1 if plugin was and is enabled., 2 if plugin got enabled.
     */
    public int update( GameEventsManager eventsManager, WidgetsDrawingManager widgetsManager, LiveGameData gameData, boolean isEditorMode, int modifierMask )
    {
        if ( !gameData.getProfileInfo().isValid() )
        {
            return ( 0 );
        }
        
        boolean wasPluginEnabled = isPluginEnabled;
        
        long when = gameData.getScoringInfo().getSessionNanos();
        
        int k = 0;
        for ( int i = 0; i < numDevices; i++ )
        {
            System.arraycopy( states1[i], 0, states0[i], 0, states1[i].length );
            buffer.position( bufferOffsets[i] + 2 * numComponents[i] );
            buffer.get( states1[i] );
            buffer.position( 0 );
            
            for ( int j = 0; j < states1[i].length; j++, k++ )
            {
                boolean oldState = ( states0[i][j] != 0 );
                boolean state = ( states1[i][j] != 0 );
                if ( state != oldState )
                {
                    InputMapping mapping = mappings[k];
                    InputAction action = mapping.getAction();
                    
                    if ( action.acceptsState( state ) && ( ( modifierMask & mapping.getModifierMask() ) == mapping.getModifierMask() ) )
                    {
                        if ( action != lastHitAction )
                        {
                            lastHitAction = action;
                            firstActionHitTime = when;
                            hitCount = 1;
                        }
                        else if ( when - firstActionHitTime < 1000000000L )
                        {
                            hitCount++;
                        }
                        else
                        {
                            firstActionHitTime = when;
                            hitCount = 1;
                        }
                        
                        if ( hitCount >= mapping.getHitTimes() )
                        {
                            hitCount = 0;
                            lastHitAction = null;
                            
                            if ( action == KnownInputActions.TogglePlugin )
                            {
                                isPluginEnabled = !isPluginEnabled;
                            }
                            else if ( isPluginEnabled && rfDynHUD.isInRenderMode() && gameData.isInRealtimeMode() )
                            {
                                if ( action.getConsumer() != null )
                                {
                                    action.getConsumer().onBoundInputStateChanged( action, state, modifierMask, when, gameData, isEditorMode );
                                }
                                else if ( action == KnownInputActions.ToggleFixedViewedVehicle )
                                {
                                    if ( gameData.getScoringInfo().isInRealtimeMode() )
                                        __GDPrivilegedAccess.toggleFixedViewedVSI( gameData.getScoringInfo() );
                                }
                                else if ( action == KnownInputActions.IncBoost )
                                {
                                    if ( gameData.getScoringInfo().isInRealtimeMode() )
                                        __GDPrivilegedAccess.incEngineBoostMapping( gameData.getTelemetryData(), gameData.getPhysics().getEngine() );
                                }
                                else if ( action == KnownInputActions.DecBoost )
                                {
                                    if ( gameData.getScoringInfo().isInRealtimeMode() )
                                        __GDPrivilegedAccess.decEngineBoostMapping( gameData.getTelemetryData(), gameData.getPhysics().getEngine() );
                                }
                                else if ( action == KnownInputActions.TempBoost )
                                {
                                    if ( gameData.getScoringInfo().isInRealtimeMode() )
                                        __GDPrivilegedAccess.setTempBoostFlag( gameData.getTelemetryData(), state );
                                }
                                else if ( isPluginEnabled && action.isWidgetAction() )
                                {
                                    eventsManager.fireOnInputStateChanged( mapping, state, modifierMask, when, isEditorMode );
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if ( isPluginEnabled )
        {
            if ( wasPluginEnabled )
                return ( 1 );
            
            return ( 2 );
        }
        
        if ( wasPluginEnabled )
            return ( -1 );
        
        return ( 0 );
    }
    
    public InputMappingsManager( RFDynHUD rfDynHUD )
    {
        this.rfDynHUD = rfDynHUD;
    }
}
