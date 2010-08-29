#include "DirectInputConnection_cpp_adapter.h"

#include <jni.h>

#include <Windows.h>
#include <stdio.h>
#include <string>
#include <iostream>
#include <dinput.h>
#include "window_handle.h"
#include "direct_input.h"

bool pollingInterrupted = false;

void initInputDeviceManager( JNIEnv* env, jobject directInputConnection )
{
    unsigned short numKeys = getNumKeys();
    unsigned char maxKeyNameLength = getMaxKeyNameLength();
    
    unsigned int bufferSize = 2 + 1 + numKeys * ( maxKeyNameLength + 1 );
    
    unsigned char numJoysticks = getNumJoysticks();
    unsigned char* numButtons = (unsigned char*)malloc( numJoysticks );
    bufferSize += 1 + numJoysticks * MAX_JOYSTICK_NAME_LENGTH;
    for ( unsigned char i = 0; i < numJoysticks; i++ )
    {
        numButtons[i] = getNumButtons( i );
        bufferSize += 1 + numButtons[i] * MAX_JOYSTICK_BUTTON_NAME_LENGTH;
    }
    
    jbyteArray arr = env->NewByteArray( bufferSize );
    jboolean isCopy;
    char* buffer = (char*)env->GetPrimitiveArrayCritical( arr, &isCopy );
    
    unsigned int bufferOffset = 0;
    *( (unsigned short*)( buffer + bufferOffset ) ) = numKeys;
    bufferOffset += 2;
    *( (unsigned char*)( buffer + bufferOffset ) ) = maxKeyNameLength;
    bufferOffset += 1;
    getAllKeyNames( buffer + bufferOffset );
    bufferOffset += numKeys * ( maxKeyNameLength + 1 );
    
    *( (unsigned char*)( buffer + bufferOffset ) ) = numJoysticks;
    bufferOffset += 1;
    
    getJoystickNames( (char*)( buffer + bufferOffset ) );
    bufferOffset += numJoysticks * MAX_JOYSTICK_NAME_LENGTH;
    
    for ( unsigned char i = 0; i < numJoysticks; i++ )
    {
        *( (unsigned char*)( buffer + bufferOffset ) ) = numButtons[i];
        bufferOffset += 1;
        
        getJoystickButtonNames( i, (char*)( buffer + bufferOffset ) );
        bufferOffset += numButtons[i] * MAX_JOYSTICK_BUTTON_NAME_LENGTH;
    }
    
    env->ReleasePrimitiveArrayCritical( arr, buffer, 0 );
    
    
    jclass DirectInputConnection = env->FindClass( "net/ctdp/rfdynhud/editor/input/DirectInputConnection" );
    
    jmethodID mid = env->GetMethodID( DirectInputConnection, "initInput", "([B)V" );
    
    env->CallVoidMethod( directInputConnection, mid, arr );
}

int cpp_initInputDeviceManager( JNIEnv* env, jobject directInputConnection, jbyteArray jBuffer, jint titleLength, jint bufferLength )
{
    char* windowTitle = (char*)malloc( 1024 );
    jboolean isCopy;
    char* buffer = (char*)env->GetPrimitiveArrayCritical( jBuffer, &isCopy );
    memcpy( windowTitle, buffer, (unsigned int)titleLength );
    env->ReleasePrimitiveArrayCritical( jBuffer, buffer, 0 );
    
    HWND windowHandle = getWindowHandle( windowTitle );
    if ( windowHandle == 0 )
        return ( 0 );
    
    initDirectInput( windowHandle );
    
    initInputDeviceManager( env, directInputConnection );
    
    disposeDirectInput();
    
    free( windowTitle );
    
    return ( 1 );
}

int initDirectInputAndStartPolling( int* keyCode, char* buffer )
{
    HWND windowHandle = getWindowHandle( buffer );
    if ( windowHandle == 0 )
        return ( -2 );
    
    initDirectInput( windowHandle );
    
    int result = -1;
	(*keyCode) = -1;
	unsigned short modifierMask = 0;
	unsigned short modifierMask2 = 0;
    pollingInterrupted = false;
    while ( !pollingInterrupted )
    {
        short s = pollKeyStates( &modifierMask, &modifierMask2 );
        if ( s >= 0 )
        {
			(*keyCode) = s;
            result = getKeyName( (unsigned short)s, buffer );
			result |= ( ( modifierMask << 16 ) & 0xFFFF0000 );
            break;
        }
        
        s = pollJoystickButtonStates();
        if ( s >= 0 )
        {
            unsigned char joystickIndex = (unsigned char)( ( s & 0xFF00 ) >> 8 );
            unsigned char buttonIndex   = (unsigned char)( s & 0xFF );
            result = getJoystickButtonName( joystickIndex, buttonIndex, buffer );
            break;
        }
        
        Sleep( 10 );
    }
    
    disposeDirectInput();
    
    return ( result );
}

void notifyOnInputEventReceived( JNIEnv* env, jobject directInputConnection, int keyCode, jbyteArray jBuffer, int resultLength, int modifierMask )
{
    jclass DirectInputConnection = env->FindClass( "net/ctdp/rfdynhud/editor/input/DirectInputConnection" );
    jmethodID mid = env->GetMethodID( DirectInputConnection, "onInputEventReceived", "(I[BII)V" );
    env->CallVoidMethod( directInputConnection, mid, keyCode, jBuffer, (jint)resultLength, modifierMask );
}

int cpp_initDirectInputAndStartPolling( JNIEnv* env, jobject directInputConnection, jbyteArray jBuffer, jint titleLength, jint bufferLength )
{
    char* windowTitle = (char*)malloc( 1024 );
    jboolean isCopy;
    char* buffer = (char*)env->GetPrimitiveArrayCritical( jBuffer, &isCopy );
    memcpy( windowTitle, buffer, (unsigned int)titleLength + 1 );
    env->ReleasePrimitiveArrayCritical( jBuffer, buffer, 0 );
    
    //std::cout << windowTitle << "\n"; std::cout.flush();
    
    int keyCode = -1;
	int result = initDirectInputAndStartPolling( &keyCode, windowTitle );
    
    //std::cout << result << "\n"; std::cout.flush();
    
    if ( result > 0 )
    {
		int modifierMask = ( ( result & 0x00FF0000 ) >> 16 ) & 0xFF;
		int length = result & 0xFFFF;
		result = length;
        
		buffer = (char*)env->GetPrimitiveArrayCritical( jBuffer, &isCopy );
        memcpy( buffer, windowTitle, length + 1 );
        env->ReleasePrimitiveArrayCritical( jBuffer, buffer, 0 );
        
        notifyOnInputEventReceived( env, directInputConnection, keyCode, jBuffer, result, modifierMask );
    }
    
    free( windowTitle );
    
    return ( result );
}

void cpp_interruptPolling()
{
    pollingInterrupted = true;
}
