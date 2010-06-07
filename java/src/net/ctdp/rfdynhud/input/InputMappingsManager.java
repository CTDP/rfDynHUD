package net.ctdp.rfdynhud.input;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.ctdp.rfdynhud.RFDynHUD;
import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.RFactorEventsManager;
import net.ctdp.rfdynhud.gamedata.RFactorFileSystem;
import net.ctdp.rfdynhud.gamedata.__GDPrivilegedAccess;
import net.ctdp.rfdynhud.render.WidgetsDrawingManager;
import net.ctdp.rfdynhud.util.Logger;

import org.jagatoo.util.errorhandling.ParsingException;
import org.jagatoo.util.ini.AbstractIniParser;


/**
 * This manager manages mappings of {@link InputAction}s to input device components (buttons and keys).
 * 
 * @author Marvin Froehlich
 */
public class InputMappingsManager
{
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
    
    public InputMappings loadMappings( final InputDeviceManager devManager )
    {
        try
        {
            File configFile = new File( RFactorFileSystem.CONFIG_FOLDER, "input_bindings.ini" );
            
            if ( !configFile.exists() )
            {
                Logger.log( "    No input_bindings.ini config file found in the config folder." );
                return ( null );
            }
            
            final ArrayList<String[]> rawBindings = new ArrayList<String[]>();
            
            new AbstractIniParser()
            {
                @Override
                protected boolean onSettingParsed( int lineNr, String group, String key, String value, String comment ) throws ParsingException
                {
                    String device = null;
                    String component = null;
                    String widgetName = null;
                    String actionName = null;
                    
                    String[] keyParts = key.split( "::", 2 );
                    if ( keyParts.length < 2 )
                    {
                        device = "Keyboard";
                        component = keyParts[0];
                    }
                    else
                    {
                        device = keyParts[0];
                        component = keyParts[1];
                    }
                    
                    if ( device.equalsIgnoreCase( "Keyboard" ) )
                    {
                        int ki = devManager.getKeyIndexByEnglishName( component );
                        if ( ki < 0 )
                            ki = devManager.getKeyIndex( component );
                        
                        if ( ki < 0 )
                        {
                            Logger.log( "    WARNING: The Keyboard doesn't have a key called \"" + component + "\". Skipping mapping in line #" + lineNr + "." );
                            return ( true );
                        }
                        
                        component = devManager.getKeyName( ki );
                    }
                    else if ( device.equalsIgnoreCase( "Mouse" ) )
                    {
                        // TODO
                    }
                    else // Joystick
                    {
                        int ji = devManager.getJoystickIndex( device );
                        
                        if ( ji < 0 )
                        {
                            Logger.log( "    WARNING: Joystick \"" + device + "\" not found. Skipping mapping in line #" + lineNr );
                            return ( true );
                        }
                        
                        device = devManager.getJoystickName( ji );
                        
                        int bi = devManager.getJoystickButtonIndex( ji, component );
                        
                        if ( bi < 0 )
                        {
                            Logger.log( "    WARNING: Joystick \"" + device + "\" doesn't have a button called \"" + component + "\". Skipping mapping in line #" + lineNr + "." );
                            return ( true );
                        }
                        
                        component = devManager.getJoystickButtonName( ji, bi );
                    }
                    
                    String[] valueParts = value.split( "::", 2 );
                    
                    if ( valueParts.length < 2 )
                    {
                        Logger.log( "    WARNING: Illegal mapping at line #" + lineNr + ". Skipping." );
                        return ( true );
                    }
                    
                    widgetName = valueParts[0];
                    actionName = valueParts[1];
                    
                    if ( KnownInputActions.get( actionName ) == null )
                    {
                        Logger.log( "    WARNING: No InputAction with the name \"" + actionName + "\" found. Skipping line #" + lineNr + "." );
                        return ( true );
                    }
                    
                    rawBindings.add( new String[] { device, component, widgetName, actionName } );
                    
                    return ( true );
                }
                
                @Override
                protected void onParsingFinished()
                {
                }
            }.parse( configFile );
            
            
            Collections.sort( rawBindings, new Comparator<String[]>()
            {
                public int compare( String[] o1, String[] o2 )
                {
                    int cmp = o1[0].compareTo( o2[0] );
                    if ( cmp != 0 )
                        return ( cmp );
                    
                    cmp = o1[1].compareTo( o2[1] );
                    if ( cmp != 0 )
                        return ( cmp );
                    
                    return ( 0 );
                }
            } );
            
            String lastDevice = null;
            numDevices = 0;
            numComponents = new int[ 256 ];
            int maxNumComponents = 0;
            int nc = 0;
            for ( int i = 0; i < rawBindings.size(); i++ )
            {
                String device = rawBindings.get( i )[0];
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
                String device = rawBindings.get( i )[0];
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
                String device = rawBindings.get( i )[0];
                String component = rawBindings.get( i )[1];
                String widgetName = rawBindings.get( i )[2];
                String actionName = rawBindings.get( i )[3];
                
                if ( !device.equals( lastDevice ) )
                {
                    deviceIndex++;
                    c = 0;
                    lastDevice = device;
                }
                
                short componentIndex = 0;
                
                if ( device.equalsIgnoreCase( "Keyboard" ) )
                {
                    componentIndex = (short)devManager.getKeyIndex( component );
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
                
                mappings[i] = new InputMapping( widgetName, KnownInputActions.get( actionName ), device + "::" + component );
                
                Logger.log( "    Bound \"" + mappings[i].getDeviceComponent() + "\" to \"" + widgetName + "::" + actionName + "\"" );
                
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
            Logger.log( t );
        }
        
        return ( new InputMappings( mappings ) );
    }
    
    private long firstToggleStrokeTime = -1L;
    private int toggleStrokes = 0;
    
    private void handleTogglePlugin( long when )
    {
        if ( when - firstToggleStrokeTime > 1000000000L )
        {
            toggleStrokes = 1;
            firstToggleStrokeTime = when;
        }
        else if ( ++toggleStrokes >= 3 )
        {
            isPluginEnabled = !isPluginEnabled;
            
            toggleStrokes = 0;
        }
    }
    
    /**
     * 
     * @param widgetsManager
     * @param gameData
     * @param editorPresets
     * @param eventsManager
     * @param modifierMask
     * 
     * @return -1 if plugin got disabled, 0 if plugin was and is disabled, 1 if plugin was and is enabled., 2 if plugin got enabled.
     */
    public int update( WidgetsDrawingManager widgetsManager, LiveGameData gameData, EditorPresets editorPresets, RFactorEventsManager eventsManager, int modifierMask )
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
            
            for ( int j = 0; j < states1[i].length; j++ )
            {
                boolean oldState = ( states0[i][j] != 0 );
                boolean state = ( states1[i][j] != 0 );
                if ( state != oldState )
                {
                    InputAction action = mappings[k].getAction();
                    
                    if ( ( action.getAcceptedState() != null ) && ( action.getAcceptedState().booleanValue() != state ) )
                        continue;
                    
                    if ( action == KnownInputActions.TogglePlugin )
                    {
                        handleTogglePlugin( when );
                    }
                    
                    if ( isPluginEnabled && rfDynHUD.isInRenderMode() && gameData.isInRealtimeMode() )
                    {
                        if ( action.getConsumer() != null )
                        {
                            action.getConsumer().onBoundInputStateChanged( action, state, modifierMask, when, gameData, editorPresets );
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
                            widgetsManager.fireOnInputStateChanged( mappings[k], state, modifierMask, when, gameData, editorPresets );
                        }
                    }
                }
                
                k++;
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
