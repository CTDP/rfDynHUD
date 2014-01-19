#include <direct_input.h>

#include <logging.h>
#include <Windows.h>

bool initDirectInput( HWND hWnd )
{
    logg( "ERROR: The dummy function initDirectInput() should never be called!!!" );
    
    return ( false );
}

unsigned short getNumKeys()
{
    logg( "ERROR: The dummy function getNumKeys() should never be called!!!" );
    
    return ( 0 );
}

unsigned char getMaxKeyNameLength()
{
    logg( "ERROR: The dummy function getMaxKeyNameLength() should never be called!!!" );
    
    return ( 0 );
}

unsigned short getAllKeyNames( char* buffer )
{
    logg( "ERROR: The dummy function getAllKeyNames() should never be called!!!" );
    
    return ( 0 );
}

void getKeyStates( const unsigned char numKeys, const unsigned short* keys, unsigned char* states, unsigned char* modifierMask )
{
    logg( "ERROR: The dummy function getKeyStates() should never be called!!!" );
}

unsigned char getNumJoysticks()
{
    logg( "ERROR: The dummy function getNumJoysticks() should never be called!!!" );
    
    return ( 0 );
}

void getJoystickNames( char* names )
{
    logg( "ERROR: The dummy function getJoystickNames() should never be called!!!" );
}

unsigned char getNumButtons( const unsigned char joystickIndex )
{
    logg( "ERROR: The dummy function getNumButtons() should never be called!!!" );
    
    return ( 0 );
}

void getJoystickButtonNames( const unsigned char joystickIndex, char* names )
{
    logg( "ERROR: The dummy function getJoystickButtonNames() should never be called!!!" );
}

void getJoystickButtonStates( const unsigned char joystickIndex, const unsigned char numButtons, const unsigned short* buttons, unsigned char* states )
{
    logg( "ERROR: The dummy function getJoystickButtonStates() should never be called!!!" );
}

void disposeDirectInput()
{
    logg( "ERROR: The dummy function dispostDirectInput() should never be called!!!" );
}
