#include "direct_input.h"

#include "dll_handle.h"

#include <dinput.h>
#include <stdio.h>

#include "logging.h"

// include the DirectX Library files
#pragma comment (lib, "dinput8.lib")
#pragma comment (lib, "dxguid.lib")

LPDIRECTINPUT8 din;    // the pointer to our DirectInput interface

const unsigned short MAX_KEYS = 256;
char** keyNames = (char**)malloc( MAX_KEYS * sizeof( char* ) );
unsigned char maxKeyNameLength = 0;
unsigned short numKeys = 0;
LPDIRECTINPUTDEVICE8 dinKeyboard;    // the pointer to the keyboard device

const unsigned char MAX_JOYSTICKS = 32;
LPDIRECTINPUTDEVICE8* dinJoysticks = (LPDIRECTINPUTDEVICE8*)malloc( MAX_JOYSTICKS * sizeof( LPDIRECTINPUTDEVICE8 ) );
char** joystickNames = (char**)malloc( MAX_JOYSTICKS * sizeof( char* ) );
char*** buttonNames = (char***)malloc( MAX_JOYSTICKS * sizeof( char** ) );
unsigned char* numButtons = (unsigned char*)malloc( MAX_JOYSTICKS * sizeof( unsigned char ) );
unsigned char numJoysticks = 0;

BOOL CALLBACK EnumKeysCallback( const DIDEVICEOBJECTINSTANCE* pdidoi, VOID* pContext )
{
    if ( pdidoi->dwOfs < MAX_KEYS )
    {
        unsigned char len = (unsigned char)strlen( pdidoi->tszName );
        if ( len > maxKeyNameLength )
            maxKeyNameLength = len;
        char* name = (char*)malloc( len + 1 );
        memcpy( name, pdidoi->tszName, len + 1 );
        keyNames[pdidoi->dwOfs] = name;
        
        if ( pdidoi->dwOfs + 1 > numKeys )
            numKeys = (unsigned short)( pdidoi->dwOfs + 1 );
    }
    
    return ( TRUE );
}

bool initKeyboard( HWND hWnd )
{
    // create the keyboard device
    HRESULT result = din->CreateDevice( GUID_SysKeyboard,    // the default keyboard ID being used
                                        &dinKeyboard,    // the pointer to the device interface
                                        NULL    // COM stuff, so we'll set it to NULL
                                      );
    
    if ( FAILED( result ) )
    {
        logg( "    ERROR: Unable to initialize keyboard." );
        return ( false );
    }
    
    if ( FAILED( dinKeyboard->SetDataFormat( &c_dfDIKeyboard ) ) ) // set the data format to keyboard format
    {
        logg( "    ERROR: Unable to set keyboard data format." );
        return ( false );
    }
    
    // set the control you will have over the keyboard
    if ( FAILED( dinKeyboard->SetCooperativeLevel( hWnd, DISCL_NONEXCLUSIVE | DISCL_BACKGROUND ) ) )
    {
        logg( "    ERROR: Unable to set keyboard cooperative level." );
        return ( false );
    }
    
    char** kn = keyNames;
    for ( unsigned short i = 0; i < MAX_KEYS; i++ )
        *kn++ = "";
    numKeys = 0;
    if ( FAILED( dinKeyboard->EnumObjects( &EnumKeysCallback, NULL, DIDFT_ALL ) ) )
    {
        logg( "    ERROR: Unable to enumerate keyboard keys." );
        return ( false );
    }
    
    return ( true );
}

BOOL CALLBACK EnumButtonsCallback( const DIDEVICEOBJECTINSTANCE* pdidoi, VOID* pContext )
{
    const unsigned char i = *( (unsigned char*)pContext );
    
    unsigned int nameLength = strlen( pdidoi->tszName );
    char* name = (char*)malloc( nameLength + 1 );
    memcpy( name, pdidoi->tszName, nameLength + 1 );
    buttonNames[i][numButtons[i]++] = name;
    
    return ( TRUE );
}

BOOL CALLBACK EnumJoysticksCallback( const DIDEVICEINSTANCE* pdidInstance, VOID* pContext )
{
    LPDIRECTINPUTDEVICE8 dinJoystick;
    // Obtain an interface to the enumerated joystick.
    HRESULT hr = din->CreateDevice( pdidInstance->guidInstance, &dinJoystick, NULL );
    if ( FAILED( hr ) )
        return ( DIENUM_CONTINUE );
    
    dinJoystick->SetDataFormat( &c_dfDIJoystick2 );
    
    HWND hWnd = *( (HWND*)pContext );
    dinJoystick->SetCooperativeLevel( hWnd, DISCL_NONEXCLUSIVE | DISCL_BACKGROUND );
    
    unsigned char i = numJoysticks++;
    dinJoysticks[i] = dinJoystick;
    unsigned int nameLength = strlen( pdidInstance->tszInstanceName );
    char* name = (char*)malloc( nameLength + 1 );
    memcpy( name, pdidInstance->tszInstanceName, nameLength + 1 );
    joystickNames[i] = name;
    
    numButtons[i] = 0;
    buttonNames[i] = (char**)malloc( 256 * sizeof( char* ) );
    if ( FAILED( dinJoystick->EnumObjects( &EnumButtonsCallback, &i, DIDFT_BUTTON ) ) )
    {
        logg( "    WARNING: Unable to enumerate joystick buttons." );
    }
    
    //return ( DIENUM_STOP );
    return ( DIENUM_CONTINUE );
}

bool initJoysticks( HWND hWnd )
{
    if ( FAILED( din->EnumDevices( DI8DEVCLASS_GAMECTRL, EnumJoysticksCallback, &hWnd, DIEDFL_ATTACHEDONLY ) ) )
    {
        logg( "    ERROR: Unable to enumerate joysticks." );
        return ( false );
    }
    
    return ( true );
}

bool init( HINSTANCE hInstance, HWND hWnd )
{
    // create the DirectInput interface
    HRESULT result = DirectInput8Create( hInstance,    // the handle to the application
                                         DIRECTINPUT_VERSION,    // the compatible version
                                         IID_IDirectInput8,    // the DirectInput interface version
                                         (void**)&din,    // the pointer to the interface
                                         NULL    // COM stuff, so we'll set it to NULL
                                       );
    
    if ( FAILED( result ) )
    {
        logg( "    ERROR: Unable to init DirectInput" );
        return ( false );
    }
    
    if ( !initKeyboard( hWnd ) )
    {
        logg( "    ERROR: Keyboard not initialized." );
        return ( false );
    }
    
    if ( !initJoysticks( hWnd ) )
    {
        logg( "    ERROR: Joystick(s) not initialized." );
        return ( false );
    }
    
    return ( true );
}

bool initDirectInput( HWND hWnd )
{
    return ( init( getDLLHandle(), hWnd ) );
}

unsigned short getNumKeys()
{
    return ( numKeys );
}

unsigned char getMaxKeyNameLength()
{
    return ( maxKeyNameLength );
}

unsigned short getAllKeyNames( char* buffer )
{
    for ( int i = 0; i < numKeys; i++ )
    {
        if ( keyNames[i] == NULL )
            *buffer = '\0';
        else
            memcpy( buffer, keyNames[i], strlen( keyNames[i] ) + 1 );
        
        buffer += maxKeyNameLength + 1;
    }
    
    return ( numKeys );
}

void getKeyStates( const unsigned char numKeys, const unsigned short* keys, unsigned char* states, unsigned char* modifierMask )
{
    static BYTE keystate[MAX_KEYS];    // create a static storage for the key-states
    
    dinKeyboard->Acquire();    // get access if we don't have it already
    
    dinKeyboard->GetDeviceState( MAX_KEYS, (LPVOID)keystate );    // fill keystate with values
    
    *modifierMask = 0;
    if ( keystate[DIK_LSHIFT] & 0x80 )
        *modifierMask |= MODIFIER_MASK_SHIFT;
    if ( keystate[DIK_RSHIFT] & 0x80 )
        *modifierMask |= MODIFIER_MASK_SHIFT;
    if ( keystate[DIK_LCONTROL] & 0x80 )
        *modifierMask |= MODIFIER_MASK_CTRL;
    if ( keystate[DIK_RCONTROL] & 0x80 )
        *modifierMask |= MODIFIER_MASK_CTRL;
    if ( keystate[DIK_LMENU] & 0x80 )
        *modifierMask |= MODIFIER_MASK_LALT;
    if ( keystate[DIK_RMENU] & 0x80 )
        *modifierMask |= MODIFIER_MASK_RALT;
    if ( keystate[DIK_LMENU] & 0x80 )
        *modifierMask |= MODIFIER_MASK_LMETA;
    if ( keystate[DIK_RMENU] & 0x80 )
        *modifierMask |= MODIFIER_MASK_RMETA;
    
    for ( unsigned short i = 0; i < numKeys; i++ )
    {
        states[i] = ( keystate[keys[i]] & 0x80 ) ? 1 : 0;
    }
}

unsigned char getNumJoysticks()
{
    return ( numJoysticks );
}

void getJoystickNames( char* names )
{
    for ( unsigned char i = 0; i < numJoysticks; i++ )
    {
        memcpy( names + i * MAX_JOYSTICK_NAME_LENGTH, joystickNames[i], strlen( joystickNames[i] ) + 1 );
    }
}

unsigned char getNumButtons( const unsigned char joystickIndex )
{
    return ( numButtons[joystickIndex] );
}

void getJoystickButtonNames( const unsigned char joystickIndex, char* names )
{
    for ( unsigned char i = 0; i < numButtons[joystickIndex]; i++ )
    {
        memcpy( names + i * MAX_JOYSTICK_BUTTON_NAME_LENGTH, buttonNames[joystickIndex][i], strlen( buttonNames[joystickIndex][i] ) + 1 );
    }
}

void getJoystickButtonStates( const unsigned char joystickIndex, const unsigned char numButtons, const unsigned short* buttons, unsigned char* states )
{
    const unsigned char i = joystickIndex;
    
    dinJoysticks[i]->Acquire();
    
    DIJOYSTATE2 js;
    
    dinJoysticks[i]->GetDeviceState( sizeof( DIJOYSTATE2 ), &js );
    
    for ( unsigned char j = 0; j < numButtons; j++ )
    {
        states[j] = ( js.rgbButtons[buttons[j]] & 0x80 ) ? 1 : 0;
    }
}

void disposeDirectInput()
{
    logg( "    Disposing Keyboard...", false );
    dinKeyboard->Unacquire();    // make sure the keyboard is unacquired
    dinKeyboard = NULL;
    logg( " Successfully disposed Keyboard." );
    
    logg( "    Disposing Joysticks...", false );
    for ( int i = numJoysticks - 1; i >= 0; i-- )
    {
        try
        {
            dinJoysticks[i]->Unacquire();
            dinJoysticks[i]->Release();
        }
        catch ( ... )
        {
            loggui( " WARNING: Caught an exception while trying to dispose Joystick ", i );
        }
        
        dinJoysticks[i] = NULL;
    }
    logg( " Successfully disposed Joysticks." );
    
    logg( "    Disposing DirectInput...", false );
    din->Release();    // close DirectInput before exiting
    din = NULL;
    logg( " Successfully disposed DirectInput." );
}
