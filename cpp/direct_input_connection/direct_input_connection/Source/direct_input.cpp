#include "direct_input.h"

#include <Windows.h>
#include <dinput.h>

#include <stdio.h>
#include <string>
#include <iostream>

// include the DirectX Library files
#pragma comment (lib, "dinput8.lib")
#pragma comment (lib, "dxguid.lib")

const unsigned short MAX_KEYS = 256;
char** keyNames = (char**)malloc( MAX_KEYS * sizeof( char** ) );
unsigned char maxKeyNameLength = 0;
unsigned short numKeys = 0;

const unsigned char MAX_JOYSTICKS = 32;
char** joystickNames = (char**)malloc( MAX_JOYSTICKS * sizeof( char** ) );
char*** buttonNames = (char***)malloc( MAX_JOYSTICKS * sizeof( char*** ) );
unsigned char* numButtons = (unsigned char*)malloc( MAX_JOYSTICKS * sizeof( unsigned char* ) );
unsigned char numJoysticks = 0;

LPDIRECTINPUT8 din;    // the pointer to our DirectInput interface

LPDIRECTINPUTDEVICE8 dinKeyboard;    // the pointer to the keyboard device
LPDIRECTINPUTDEVICE8* dinJoysticks = (LPDIRECTINPUTDEVICE8*)malloc( MAX_JOYSTICKS * sizeof( LPDIRECTINPUTDEVICE8* ) );

unsigned int strlenW( const wchar_t* s )
{
    for ( unsigned int l = 0; l < 1024; l++ )
    {
        if ( *s++ == '\0' )
            return ( l );
    }
    
    return ( 0 );
}

unsigned int copyWideStringToString( const wchar_t* in, char* out )
{
    wchar_t c;
    const wchar_t zero = L'\0';
    unsigned int len = 0;
    while ( ( c = *in++ ) != zero )
    {
        *out++ = (char)c;
        len++;
    }
    *out = '\0';
    
    return ( len );
}

BOOL CALLBACK EnumKeysCallback( const DIDEVICEOBJECTINSTANCE* pdidoi, VOID* pContext )
{
    if ( pdidoi->dwOfs < MAX_KEYS )
    {
        unsigned char len = (unsigned char)strlenW( pdidoi->tszName );
        if ( len > maxKeyNameLength )
            maxKeyNameLength = len;
        char* name = (char*)malloc( len + 1 );
        //memcpy( name, pdidoi->tszName, len + 1 );
        copyWideStringToString( pdidoi->tszName, name );
        keyNames[pdidoi->dwOfs] = name;
        
        if ( pdidoi->dwOfs + 1 > numKeys )
            numKeys = (unsigned short)( pdidoi->dwOfs + 1 );
    }
    
    return ( TRUE );
}

void initKeyboard( HWND hWnd )
{
    // create the keyboard device
    din->CreateDevice( GUID_SysKeyboard,    // the default keyboard ID being used
                       &dinKeyboard,    // the pointer to the device interface
                       NULL    // COM stuff, so we'll set it to NULL
                     );
    
    dinKeyboard->SetDataFormat( &c_dfDIKeyboard ); // set the data format to keyboard format
    
    // set the control you will have over the keyboard
    dinKeyboard->SetCooperativeLevel( hWnd, DISCL_NONEXCLUSIVE | DISCL_BACKGROUND );
    
    char** kn = keyNames;
    for ( unsigned short i = 0; i < MAX_KEYS; i++ )
        *kn++ = "";
    numKeys = 0;
    dinKeyboard->EnumObjects( &EnumKeysCallback, NULL, DIDFT_ALL );
}

BOOL CALLBACK EnumButtonsCallback( const DIDEVICEOBJECTINSTANCE* pdidoi, VOID* pContext )
{
    const unsigned char i = *( (unsigned char*)pContext );
    
    unsigned int nameLength = strlenW( pdidoi->tszName );
    char* name = (char*)malloc( nameLength + 1 );
    //memcpy( name, pdidoi->tszName, nameLength + 1 );
    copyWideStringToString( pdidoi->tszName, name );
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
    unsigned int nameLength = strlenW( pdidInstance->tszInstanceName );
    char* name = (char*)malloc( nameLength + 1 );
    //memcpy( name, pdidInstance->tszInstanceName, nameLength + 1 );
    copyWideStringToString( pdidInstance->tszInstanceName, name );
    joystickNames[i] = name;
    
    numButtons[i] = 0;
    buttonNames[i] = (char**)malloc( 256 );
    dinJoystick->EnumObjects( &EnumButtonsCallback, &i, DIDFT_BUTTON );
    
    //return ( DIENUM_STOP );
    return ( DIENUM_CONTINUE );
}

void initJoysticks( HWND hWnd )
{
    numJoysticks = 0;
    for ( unsigned char i = 0; i < MAX_JOYSTICKS; i++ )
        numButtons[i] = 0;
    
    din->EnumDevices( DI8DEVCLASS_GAMECTRL, EnumJoysticksCallback, &hWnd, DIEDFL_ATTACHEDONLY );
}

void init( HINSTANCE hInstance, HWND hWnd )
{
    // create the DirectInput interface
    DirectInput8Create( hInstance,    // the handle to the application
                        DIRECTINPUT_VERSION,    // the compatible version
                        IID_IDirectInput8,    // the DirectInput interface version
                        (void**)&din,    // the pointer to the interface
                        NULL    // COM stuff, so we'll set it to NULL
                      );
    
    initKeyboard( hWnd );
    initJoysticks( hWnd );
}

void initDirectInput( HWND hWnd )
{
    init( GetModuleHandle( NULL ), hWnd );
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

unsigned short getKeyName( const unsigned short index, char* buffer )
{
    memcpy( buffer, "Keyboard::", 10 );
    unsigned short l = strlen( keyNames[index] );
    memcpy( buffer + 10, keyNames[index], l + 1 );
    
    return ( 10 + l );
}

short pollKeyStates()
{
    static BYTE keystate[MAX_KEYS];    // create a static storage for the key-states
    
    dinKeyboard->Acquire();    // get access if we don't have it already
    
    HRESULT hr = dinKeyboard->GetDeviceState( MAX_KEYS, (LPVOID)keystate );    // fill keystate with values
    
    BYTE* ss = keystate;
    for ( unsigned short i = 0; i < numKeys; i++ )
    {
        if ( *ss++ & 0x80 )
        {
            //std::cout << keyNames[i] << "\n"; std::cout.flush();
            return ( i );
        }
    }
    
    return ( -1 );
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

unsigned short getJoystickButtonName( const unsigned char joystickIndex, unsigned char buttonIndex, char* buffer )
{
    unsigned short lj = strlen( joystickNames[joystickIndex] );
    memcpy( buffer, joystickNames[joystickIndex], lj );
    buffer += lj;
    memcpy( buffer, "::", 2 );
    buffer += 2;
    unsigned short lb = strlen( buttonNames[joystickIndex][buttonIndex] );
    memcpy( buffer, buttonNames[joystickIndex][buttonIndex], lb + 1 );
    
    return ( lj + 2 + lb );
}

short pollJoystickButtonStates()
{
    DIJOYSTATE2 js;
    
    for ( unsigned char i = 0; i < numJoysticks; i++ )
    {
        dinJoysticks[i]->Acquire();
        
        dinJoysticks[i]->GetDeviceState( sizeof( DIJOYSTATE2 ), &js );
        
        BYTE* ss = js.rgbButtons;
        unsigned char n = numButtons[i];
        for ( unsigned char j = 0; j < n; j++ )
        {
            if ( *ss++ & 0x80 )
            {
                //std::cout << i << ", " << j << ", " << joystickNames[i] << ", " << buttonNames[i][j] << "\n"; std::cout.flush();
                return ( ( (short)i << 8 ) | (short)j );
            }
        }
    }
    
    return ( -1 );
}

void disposeDirectInput()
{
    dinKeyboard->Unacquire();    // make sure the keyboard is unacquired
    dinKeyboard = NULL;
    
    for ( int i = numJoysticks - 1; i >= 0; i-- )
    {
        dinJoysticks[i]->Unacquire();
        dinJoysticks[i]->Release();
        
        dinJoysticks[i] = NULL;
    }
    
    din->Release();    // close DirectInput before exiting
    din = NULL;
}
