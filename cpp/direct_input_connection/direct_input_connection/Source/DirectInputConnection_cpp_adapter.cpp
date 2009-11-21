#include "DirectInputConnection_cpp_adapter.h"

#include <jni.h>

#include <Windows.h>
#include <stdio.h>
#include <string>
#include <iostream>
#include "window_handle.h"
#include "direct_input.h"

bool pollingInterrupted = false;

/*
short getFirstTrueState( bool* states, unsigned short n )
{
    for ( unsigned short i = 0; i < n; i++ )
    {
        if ( *states++ )
            return ( (short)i );
    }
    
    return ( -1 );
}
*/

int initDirectInputAndStartPolling( char* buffer )
{
    HWND windowHandle = getWindowHandle( buffer );
    if ( windowHandle == 0 )
        return ( -2 );
    
    initDirectInput( windowHandle );
    
    int result = -1;
    pollingInterrupted = false;
    while ( !pollingInterrupted )
    {
        short s = pollKeyStates();
        if ( s >= 0 )
        {
            result = getKeyName( (unsigned short)s, buffer );
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

void notifyOnInputEventReceived( JNIEnv* env, jobject directInputConnection, jbyteArray jBuffer, int resultLength )
{
    jclass DirectInputConnection = env->FindClass( "net/ctdp/rfdynhud/editor/input/DirectInputConnection" );
    jmethodID mid = env->GetMethodID( DirectInputConnection, "onInputEventReceived", "([BI)V" );
    env->CallVoidMethod( directInputConnection, mid, jBuffer, (jint)resultLength );
}

int cpp_initDirectInputAndStartPolling( JNIEnv* env, jobject directInputConnection, jbyteArray jBuffer, jint titleLength, jint bufferLength )
{
    char* windowTitle = (char*)malloc( 1024 );
    jboolean isCopy;
    char* buffer = (char*)env->GetPrimitiveArrayCritical( jBuffer, &isCopy );
    memcpy( windowTitle, buffer, (unsigned int)titleLength );
    env->ReleasePrimitiveArrayCritical( jBuffer, buffer, 0 );
    
    //std::cout << windowTitle << "\n"; std::cout.flush();
    
    int result = initDirectInputAndStartPolling( windowTitle );
    
    //std::cout << result << "\n"; std::cout.flush();
    
    if ( result > 0 )
    {
        buffer = (char*)env->GetPrimitiveArrayCritical( jBuffer, &isCopy );
        memcpy( buffer, windowTitle, result );
        env->ReleasePrimitiveArrayCritical( jBuffer, buffer, 0 );
        
        notifyOnInputEventReceived( env, directInputConnection, jBuffer, result );
    }
    
    return ( result );
}

void cpp_interruptPolling()
{
    pollingInterrupted = true;
}
