#include "jvm_connection.hpp"
#include <Windows.h>
#include <winreg.h>
#include <jni.h>
#include "direct_input.h"
#include "filesystem.h"
#include "logging.h"

const char* GAME_NAME = "rFactor";

char* readJavaHomeFromRegistry()
{
    char* buffer = (char*)malloc( MAX_PATH );
    HKEY keyHandle;
    DWORD size1;
    DWORD Type;
    
    if ( RegOpenKeyEx( HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment\\1.6", 0, KEY_QUERY_VALUE, &keyHandle ) == ERROR_SUCCESS )
    {
        size1 = MAX_PATH - 1;
        RegQueryValueEx( keyHandle, "JavaHome", NULL, &Type, (LPBYTE)buffer, &size1);
        RegCloseKey( keyHandle );
        
        return ( buffer );
    }
    
    free( buffer );
    
    logg( "WARNING: Registry key for 32 bit Java 6 Runtime Environment not found." );
    
    return ( NULL );
}

char* guessJavaHome()
{
    char* buffer = (char*)malloc( MAX_PATH );
    
    DWORD len = GetEnvironmentVariable( "ProgramFiles", buffer, MAX_PATH );
    
    char* buff = buffer + len;
    memcpy( buff, "\\Java\\jre6", 11 );
    len += 11;
    
    char* result = (char*)malloc( len );
    memcpy( result, buffer, len );
    free( buffer );
    
	if ( checkDirectoryExists( result, false ) != 1 )
    {
        free( result );
        
        logg( "WARNING: Couldn't find 32 bit Java 6 Runtime Environment in the default folder." );
        
        return ( NULL );
    }
    
    return ( result );
}

char* getJavaHome()
{
    char* result;
    
    result = readJavaHomeFromRegistry();
    if ( result != NULL )
        return ( result );
    
    result = guessJavaHome();
    if ( result != NULL )
        return ( result );
    
    // TODO: Read the path from a user defined file.
    
    return ( NULL );
}

static const char* JAVA_HOME = getJavaHome();

jboolean isCopy = false;

typedef jint ( APIENTRY * CreateJavaVMPROC ) ( JavaVM** pvm, void** penv, void* args );

void loggSystemProperty( JNIEnv* env, jclass System, jmethodID getProperty, const char* prop )
{
    jstring jProp = env->NewStringUTF( prop );
    jstring jstr = (jstring)env->CallStaticObjectMethod( System, getProperty, jProp );
    jboolean blnIsCopy;
    const char* cstr = env->GetStringUTFChars( jstr, &blnIsCopy );
    logg( "    ", false );
    logg( prop, false );
    logg( " = \"", false );
    logg( cstr, false );
    logg( "\"", true );
    env->ReleaseStringUTFChars( jstr, cstr );
}

bool createNewJavaVM( const char* PLUGIN_PATH, JavaVM** jvm, JNIEnv** env )
{
    char* fileBuffer = (char*)malloc( 16384 );
    
    if ( JAVA_HOME == NULL )
    {
        logg( "ERROR: Could not locate JAVA_HOME." );
        
        return ( false );
    }
    
    memcpy( fileBuffer, "Using Java from folder \"", 24 );
    memcpy( fileBuffer + 24, JAVA_HOME, strlen( JAVA_HOME ) );
    memcpy( fileBuffer + 24 + strlen( JAVA_HOME ), "\".", 3 );

    logg( fileBuffer );
    
    getFullPath( JAVA_HOME, "bin\\msvcr71.dll", fileBuffer );
    logg( "    Loading msvcr71.dll...", false );
    HMODULE msvcdll = LoadLibrary( fileBuffer );
    
    if ( msvcdll == NULL )
    {
        logg( " ERROR: Failed to load msvcr71.dll." );
        return ( false );
    }
    else
    {
        logg( " done." );
    }
    
    getFullPath( JAVA_HOME, "bin\\client\\jvm.dll", fileBuffer );
    logg( "    Loading jvm.dll...", false );
    HMODULE jvmdll = LoadLibrary( fileBuffer );
    
    if ( jvmdll == NULL )
    {
        logg( " ERROR: Failed to load jvm.dll." );
        return ( false );
    }
    else
    {
        logg( " done." );
    }
    
    logg( "Successfully loaded Java dlls." );
    
    logg( "Invoking Java VM..." );
    
    char* searchPath = cropBuffer2( appendPath2( "\\widget_sets\\*", setBuffer( PLUGIN_PATH, fileBuffer ), false ) );
    
    setBuffer( "-Djava.class.path=", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud.jar", fileBuffer );
    
    char* searchPath2 = (char*)malloc( MAX_PATH );
    
    WIN32_FIND_DATA data;
    WIN32_FIND_DATA data2;
    HANDLE hFile = FindFirstFile( searchPath, &data );
    if ( hFile != INVALID_HANDLE_VALUE )
    {
        do
        {
            if ( ( ( data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY ) != 0 ) && ( data.cFileName[0] != '.' ) && ( strcmp( data.cFileName, ".svn" ) != 0 ) )
            {
                searchPath2 = appendPath2( "\\*.jar", appendPath2( data.cFileName, appendPath2( "widget_sets", setBuffer( PLUGIN_PATH, searchPath2 ), true ), true ), false );
                HANDLE hFile2 = FindFirstFile( searchPath2, &data2 );
                if ( hFile2 != INVALID_HANDLE_VALUE )
                {
                    do
                    {
                        addPostFix( ";", fileBuffer );
                        addPostFix( PLUGIN_PATH, fileBuffer );
                        addPostFix( "\\", fileBuffer );
                        addPostFix( "widget_sets\\", fileBuffer );
                        addPostFix( data.cFileName, fileBuffer );
                        addPostFix( "\\", fileBuffer );
                        addPostFix( data2.cFileName, fileBuffer );
                    }
                    while ( FindNextFile( hFile2, &data2 ) );
                }
            }
        }
        while ( FindNextFile( hFile, &data ) );
    }
    
    free( searchPath2 );
    
	const bool WITH_PROFILER = false;
	const unsigned int nOptions = WITH_PROFILER ? 11 : 10;
    JavaVMOption options[nOptions];
    
    unsigned int i = 0;
	options[i++].optionString = cropBuffer2( fileBuffer );
    options[i++].optionString = cropBuffer2( addPreFix( "-Dworkdir=", setBuffer( PLUGIN_PATH, fileBuffer ) ) );
    options[i++].optionString = "-Xms96m";
    options[i++].optionString = "-Xmx96m";
    options[i++].optionString = "-XX:MaxGCPauseMillis=5";
    options[i++].optionString = "-XX:+UseAdaptiveSizePolicy";
    options[i++].optionString = "-Xincgc";
    options[i++].optionString = "-Dsun.java2d.opengl=true";
    options[i++].optionString = "-Dsun.java2d.d3d=false";
    options[i++].optionString = "-Dsun.java2d.noddraw=true";
	if ( WITH_PROFILER )
		options[i++].optionString = "-agentpath:c:\\Program Files (x86)\\YourKit Java Profiler 9.0.8\\bin\\win32\\yjpagent.dll";
    
    free( fileBuffer );
    
    logg( "JVM options:" );
    for ( i = 0; i < nOptions; i++ )
        logg2( "    ", options[i].optionString, true );
    
    JavaVMInitArgs vm_args;
    
    vm_args.version = JNI_VERSION_1_6;
    vm_args.options = options;
    vm_args.nOptions = nOptions;
    vm_args.ignoreUnrecognized = TRUE;
    
    CreateJavaVMPROC CreateJavaVM = (CreateJavaVMPROC)GetProcAddress( jvmdll, "JNI_CreateJavaVM" );
    
    if ( CreateJavaVM == NULL )
    {
        logg( "ERROR: Failed to get proc address of JNI_CreateJavaVM." );
        return ( false );
    }
    
    jint res = CreateJavaVM( jvm, (void **)env, &vm_args );
    
    if ( res < 0 )
    {
        logg( "ERROR: Failed to create Java virtual machine." );
        return ( false );
    }
    
    jclass System = (*env)->FindClass( "java/lang/System" );
    jmethodID getProperty = (*env)->GetStaticMethodID( System, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;" );
    loggSystemProperty( *env, System, getProperty, "java.vm.vendor" );
    loggSystemProperty( *env, System, getProperty, "java.vm.name" );
    loggSystemProperty( *env, System, getProperty, "java.vm.version" );
    loggSystemProperty( *env, System, getProperty, "java.runtime.version" );
    loggSystemProperty( *env, System, getProperty, "java.awt.graphicsenv" );
    
    logg( "Successfully invoked Java VM." );
    
    return ( true );
}

bool JVMD3DUpdateFunctions::init( JNIEnv* _env, jclass rfdynhudClass, jobject _rfdynhudObject )
{
    env = _env;
    rfdynhudObject = _rfdynhudObject;
    
    getPixelDataMethod = env->GetMethodID( rfdynhudClass, "getTextureData", "(I)[B" );
    
    if ( getPixelDataMethod == 0 )
    {
        logg( "ERROR: Failed to find the getTextureData() method." );
        return ( false );
    }
    
    jmethodID mid = env->GetMethodID( rfdynhudClass, "getTextureInfoBuffer", "()Ljava/nio/ByteBuffer;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getTextureInfoBuffer() method." );
        return ( false );
    }
    
    textureInfoBufferObj = env->CallObjectMethod( rfdynhudObject, mid );
    
    if ( textureInfoBufferObj == NULL )
    {
        logg( "ERROR: Failed to get the buffer for texture info." );
        return ( false );
    }
    
    textureInfoBuffer = (char*)env->GetDirectBufferAddress( textureInfoBufferObj );
    
    textureSizes = (unsigned short*)( textureInfoBuffer + OFFSET_SIZE );
    textureVisibleFlags = (char*)( textureInfoBuffer + OFFSET_VISIBLE );
    textureIsTransformedFlags = (char*)( textureInfoBuffer + OFFSET_TRANSFORMED );
    textureTranslations = (float*)( textureInfoBuffer + OFFSET_TRANSLATION );
    textureRotationCenters = (unsigned short*)( textureInfoBuffer + OFFSET_ROT_CENTER );
    textureRotations = (float*)( textureInfoBuffer + OFFSET_ROTATION );
    textureScales = (float*)( textureInfoBuffer + OFFSET_SCALE );
    textureClipRects = (unsigned short*)( textureInfoBuffer + OFFSET_CLIP_RECT );
    numUsedRectangles = (unsigned char*)( textureInfoBuffer + OFFSET_NUM_RECTANLES );
    rectangleVisibleFlags = (char*)( textureInfoBuffer + OFFSET_RECT_VISIBLE_FLAGS );
    usedRectangles = (unsigned short*)( textureInfoBuffer + OFFSET_RECTANLES );
    
    if ( textureInfoBuffer == NULL )
    {
        logg( "ERROR: Texture info buffer is null." );
        return ( false );
    }
    
    getDirtyRectsBufferMethod = env->GetMethodID( rfdynhudClass, "getDirtyRectsBuffer", "(I)Ljava/nio/ByteBuffer;" );
    
    if ( getDirtyRectsBufferMethod == 0 )
    {
        logg( "ERROR: Failed to find the getDirtyRectsBuffer() method." );
        return ( false );
    }
    
    updateMethod = env->GetMethodID( rfdynhudClass, "update", "()B" );
    
    if ( updateMethod == 0 )
    {
        logg( "ERROR: Failed to find the update() method." );
        return ( false );
    }
    
    return ( true );
}

void JVMD3DUpdateFunctions::releaseDirtyRectsBufferObjects()
{
    if ( numTextures <= 0 )
        return;
    
    for ( unsigned int i = 0; i < (unsigned int)numTextures; i++ )
    {
        if ( dirtyRectsBufferObj[i] != NULL )
            env->DeleteLocalRef( dirtyRectsBufferObj[i] );
        dirtyRectsBufferObj[i] = NULL;
    }
    
    free( dirtyRectsBufferObj );
    dirtyRectsBufferObj = NULL;
}

unsigned char JVMD3DUpdateFunctions::updateAllTextureInfos()
{
    releaseDirtyRectsBufferObjects();
    
    numTextures = *(unsigned char*)textureInfoBuffer;
    
    dirtyRectsBufferObj = (jobject*)malloc( numTextures * sizeof( jobject ) );
    
    for ( unsigned int i = 0; i < (unsigned int)numTextures; i++ )
    {
        dirtyRectsBufferObj[i] = env->CallObjectMethod( rfdynhudObject, getDirtyRectsBufferMethod, (jint)i );
        
        if ( dirtyRectsBufferObj[i] == NULL )
        {
            logg( "ERROR: Failed to get the buffer for dirty rects." );
            continue;
        }
        
        dirtyRectsBuffers[i] = (unsigned short*)env->GetDirectBufferAddress( dirtyRectsBufferObj[i] );
        
        if ( dirtyRectsBuffers[i] == NULL )
        {
            logg( "ERROR: Dirty rects buffer is null." );
            continue;
        }
    }
    
    return ( numTextures );
}

unsigned char* JVMD3DUpdateFunctions::getPixelData( unsigned char textureIndex )
{
    pixelData = (jbyteArray)env->CallObjectMethod( rfdynhudObject, getPixelDataMethod, (jint)textureIndex );
    
    if ( pixelData == NULL )
    {
        logg( "ERROR: texture pixel data is null." );
        return ( false );
    }
    
	return ( (unsigned char*)env->GetPrimitiveArrayCritical( pixelData, &isCopy ) );
}

void JVMD3DUpdateFunctions::releasePixelData( unsigned char textureIndex, unsigned char* pointer )
{
    env->ReleasePrimitiveArrayCritical( pixelData, (void*)pointer, 0 );
	env->DeleteLocalRef( pixelData );
    pixelData = NULL;
}

void JVMD3DUpdateFunctions::destroy()
{
    releaseDirtyRectsBufferObjects();
    
    if ( textureInfoBufferObj != NULL )
        env->DeleteLocalRef( textureInfoBufferObj );
    textureInfoBufferObj = NULL;
    
    updateMethod = NULL;
    
    rfdynhudObject = NULL;
    env = NULL;
}

bool JVMInputFunctions::init( JNIEnv* _env, jclass rfdynhudClass, jobject _rfdynhudObject )
{
    env = _env;
    rfdynhudObject = _rfdynhudObject;
    
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
    
    jmethodID mid = env->GetMethodID( rfdynhudClass, "initInput", "([B)V" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the initInput() method." );
        return ( false );
    }
    
    logg( "Initializing input bindings..." );
    env->CallVoidMethod( _rfdynhudObject, mid, arr );
    logg( "Finished initialization of input bindings." );
    
    env->DeleteLocalRef( arr );
    
    mid = env->GetMethodID( rfdynhudClass, "getInputBuffer", "()Ljava/nio/ByteBuffer;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getInputBuffer() method." );
        return ( false );
    }
    
    inputBufferObj = env->CallObjectMethod( _rfdynhudObject, mid );
    
    if ( inputBufferObj == NULL )
    {
        logg( "ERROR: Failed to get the input buffer." );
        return ( false );
    }
    
    inputBuffer = (char*)env->GetDirectBufferAddress( inputBufferObj );
    
    if ( inputBuffer == NULL )
    {
        logg( "ERROR: Input buffer is null." );
        return ( false );
    }
    
    updateInputMethod = env->GetMethodID( rfdynhudClass, "updateInput", "(I)B" );
    
    if ( updateInputMethod == 0 )
    {
        logg( "ERROR: Failed to find the updateInput() method." );
        return ( false );
    }
    
    return ( true );
}

char JVMInputFunctions::updateInput( bool* isPluginEnabled )
{
    unsigned char modifierMask = 0;
    
    unsigned char numDevices = *inputBuffer;
    for ( unsigned char i = 0; i < numDevices; i++ )
    {
        unsigned char  deviceType    = *( inputBuffer + 1 + i * 5 + 0 );
        unsigned char  deviceIndex   = *( inputBuffer + 1 + i * 5 + 1 );
        unsigned short arrayOffset   = *(unsigned short*)( inputBuffer + 1 + i * 5 + 2 );
        unsigned char  numComponents = *( inputBuffer + 1 + i * 5 + 4 );
        
        switch ( deviceType )
        {
            case 1: // Keyboard
                getKeyStates( numComponents, (unsigned short*)( inputBuffer + arrayOffset ), (unsigned char*)( inputBuffer + arrayOffset + numComponents * 2 ), &modifierMask );
                break;
            case 2: // Mouse
                break;
            case 3: // Joystick
                getJoystickButtonStates( deviceIndex, numComponents, (unsigned short*)( inputBuffer + arrayOffset ), (unsigned char*)( inputBuffer + arrayOffset + numComponents * 2 ) );
                break;
        }
    }
    
    char result = env->CallByteMethod( rfdynhudObject, updateInputMethod, (int)modifierMask );
    
    *isPluginEnabled = ( result != 0 );
    
    return ( result );
}

void JVMInputFunctions::destroy()
{
    updateInputMethod = 0;
    if ( inputBufferObj != NULL )
        env->DeleteLocalRef( inputBufferObj );
    inputBufferObj = NULL;
    inputBuffer = NULL;
    rfdynhudObject = NULL;
    env = NULL;
}

void JVMTelemtryUpdateFunctions::call_onStartup()
{
    env->CallVoidMethod( gameEventsManager, onStartup );
}

void JVMTelemtryUpdateFunctions::call_onShutdown()
{
    env->CallVoidMethod( gameEventsManager, onShutdown );
}

char JVMTelemtryUpdateFunctions::call_onSessionStarted()
{
    return ( env->CallByteMethod( gameEventsManager, onSessionStarted ) );
}

void JVMTelemtryUpdateFunctions::call_onSessionEnded()
{
    env->CallVoidMethod( gameEventsManager, onSessionEnded );
}

char JVMTelemtryUpdateFunctions::call_onRealtimeEntered()
{
    return ( env->CallByteMethod( gameEventsManager, onRealtimeEntered ) );
}

char JVMTelemtryUpdateFunctions::call_onRealtimeExited()
{
    return ( env->CallByteMethod( gameEventsManager, onRealtimeExited ) );
}

char JVMTelemtryUpdateFunctions::call_onTelemetryDataUpdated()
{
    //return ( env->CallBooleanMethod( gameEventsManager, onTelemetryDataUpdated ) == JNI_TRUE );
    return ( env->CallByteMethod( gameEventsManager, onTelemetryDataUpdated ) );
}

char JVMTelemtryUpdateFunctions::call_onScoringInfoUpdated()
{
    //return ( env->CallBooleanMethod( gameEventsManager, onScoringInfoUpdated ) == JNI_TRUE );
    return ( env->CallByteMethod( gameEventsManager, onScoringInfoUpdated ) );
}

char JVMTelemtryUpdateFunctions::call_onGraphicsInfoUpdated( const unsigned short viewportX, const unsigned short viewportY, const unsigned short viewportWidth, const unsigned short viewportHeight )
{
    return ( env->CallByteMethod( gameEventsManager, onGraphicsInfoUpdated, (jshort)viewportX, (jshort)viewportY, (jshort)viewportWidth, (jshort)viewportHeight ) );
}

void JVMTelemtryUpdateFunctions::copyTelemetryBuffer( void* info, unsigned int size )
{
    env->CallVoidMethod( gameData_CPP_Adapter, prepareTelemetryDataUpdate );
    
    void* buffer = env->GetPrimitiveArrayCritical( telemetryBuffer, &isCopy );
    memcpy( buffer, info, size );
    env->ReleasePrimitiveArrayCritical( telemetryBuffer, buffer, 0 );
    
    env->CallVoidMethod( gameData_CPP_Adapter, notifyTelemetryUpdated );
}

void JVMTelemtryUpdateFunctions::copyScoringInfoBuffer( void* info, unsigned int size )
{
    env->CallVoidMethod( gameData_CPP_Adapter, prepareScoringInfoDataUpdate );
    
    void* buffer = env->GetPrimitiveArrayCritical( scoringInfoBuffer, &isCopy );
    memcpy( buffer, info, size );
    env->ReleasePrimitiveArrayCritical( scoringInfoBuffer, buffer, 0 );
    
    env->CallVoidMethod( gameData_CPP_Adapter, initVehicleScoringInfo );
}

void JVMTelemtryUpdateFunctions::copyVehicleScoringInfoBuffer( long index, void* info, unsigned int size, bool isLast )
{
    jarray vsiBuffer = (jarray)env->CallObjectMethod( gameData_CPP_Adapter, getVehicleScoringInfoBuffer, (jint)index );
    unsigned char* buffer = (unsigned char*)env->GetPrimitiveArrayCritical( vsiBuffer, &isCopy );
    memcpy( buffer, info, size );
    env->ReleasePrimitiveArrayCritical( vsiBuffer, buffer, 0 );
	env->DeleteLocalRef( vsiBuffer );
    
    if ( isLast )
        env->CallVoidMethod( gameData_CPP_Adapter, notifyScoringInfoUpdated );
}

void JVMTelemtryUpdateFunctions::copyGraphicsInfoBuffer( void* info, unsigned int size )
{
    env->CallVoidMethod( gameData_CPP_Adapter, prepareGraphicsInfoDataUpdate );
    
    void* buffer = env->GetPrimitiveArrayCritical( graphicsInfoBuffer, &isCopy );
    memcpy( buffer, info, size );
    env->ReleasePrimitiveArrayCritical( graphicsInfoBuffer, buffer, 0 );
    
    env->CallVoidMethod( gameData_CPP_Adapter, notifyGraphicsInfoUpdated );
}

void JVMTelemtryUpdateFunctions::copyCommentaryInfoBuffer( void* info, unsigned int size )
{
    env->CallVoidMethod( gameData_CPP_Adapter, prepareCommentaryInfoDataUpdate );
    
    void* buffer = env->GetPrimitiveArrayCritical( commentaryInfoBuffer, &isCopy );
    memcpy( buffer, info, size );
    env->ReleasePrimitiveArrayCritical( commentaryInfoBuffer, buffer, 0 );
    
    env->CallVoidMethod( gameData_CPP_Adapter, notifyCommentaryInfoUpdated );
}


bool JVMTelemtryUpdateFunctions::init( JNIEnv* _env, jclass rfdynhudClass, jobject _rfdynhudObject )
{
    env = _env;
    rfdynhudObject = _rfdynhudObject;
    
    GameEventsManager = env->FindClass( "net/ctdp/rfdynhud/gamedata/GameEventsManager" );
    
    if ( GameEventsManager == 0 )
    {
        logg( "ERROR: Failed to find the GameEventsManager class." );
        return ( false );
    }
    
    jmethodID mid = env->GetMethodID( rfdynhudClass, "getEventsManager", "()Lnet/ctdp/rfdynhud/gamedata/GameEventsManager;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getEventsManager() method." );
        return ( false );
    }
    
    gameEventsManager = env->CallObjectMethod( rfdynhudObject, mid );
    
    if ( gameEventsManager == NULL )
    {
        logg( "ERROR: gameEventsManager is null." );
        return ( false );
    }
    
    onStartup = env->GetMethodID( GameEventsManager, "onStartup", "()V" );
    
    if ( onStartup == 0 )
    {
        logg( "ERROR: Failed to find the onStartup() method." );
        return ( false );
    }
    
    onShutdown = env->GetMethodID( GameEventsManager, "onShutdown", "()V" );
    
    if ( onShutdown == 0 )
    {
        logg( "ERROR: Failed to find the onShutdown() method." );
        return ( false );
    }
    
    onSessionStarted = env->GetMethodID( GameEventsManager, "onSessionStarted", "()B" );
    
    if ( onSessionStarted == 0 )
    {
        logg( "ERROR: Failed to find the onSessionStarted() method." );
        return ( false );
    }
    
    onSessionEnded = env->GetMethodID( GameEventsManager, "onSessionEnded", "()V" );
    
    if ( onSessionEnded == 0 )
    {
        logg( "ERROR: Failed to find the onSessionEnded() method." );
        return ( false );
    }
    
    onRealtimeEntered = env->GetMethodID( GameEventsManager, "onRealtimeEntered", "()B" );
    
    if ( onRealtimeEntered == 0 )
    {
        logg( "ERROR: Failed to find the onRealtimeEntered() method." );
        return ( false );
    }
    
    onRealtimeExited = env->GetMethodID( GameEventsManager, "onRealtimeExited", "()B" );
    
    if ( onRealtimeExited == 0 )
    {
        logg( "ERROR: Failed to find the onRealtimeExited() method." );
        return ( false );
    }
    
    onTelemetryDataUpdated = env->GetMethodID( GameEventsManager, "onTelemetryDataUpdated", "()B" );
    
    if ( onTelemetryDataUpdated == 0 )
    {
        logg( "ERROR: Failed to find the onTelemetryDataUpdated() method on RFactorEventsManager." );
        return ( false );
    }
    
    onScoringInfoUpdated = env->GetMethodID( GameEventsManager, "onScoringInfoUpdated", "()B" );
    
    if ( onScoringInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the onScoringInfoUpdated() method on RFactorEventsManager." );
        return ( false );
    }
    
    onGraphicsInfoUpdated = env->GetMethodID( GameEventsManager, "onGraphicsInfoUpdated", "(SSSS)B" );
    
    if ( onGraphicsInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the onGraphicsInfoUpdated() method on RFactorEventsManager." );
        return ( false );
    }
    
    LiveGameData_CPP_Adapter = env->FindClass( "net/ctdp/rfdynhud/gamedata/_LiveGameData_CPP_Adapter" );
    
    if ( LiveGameData_CPP_Adapter == 0 )
    {
        logg( "ERROR: Failed to find the _LiveGameData_CPP_Adapter class." );
        return ( false );
    }
    
    mid = env->GetMethodID( rfdynhudClass, "getGameData_CPP_Adapter", "()Lnet/ctdp/rfdynhud/gamedata/_LiveGameData_CPP_Adapter;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getGameData_CPP_Adapter() method." );
        return ( false );
    }
    
    gameData_CPP_Adapter = env->CallObjectMethod( rfdynhudObject, mid );
    
    if ( gameData_CPP_Adapter == NULL )
    {
        logg( "ERROR: getGameData_CPP_Adapter returns null." );
        return ( false );
    }
    
    prepareTelemetryDataUpdate = env->GetMethodID( LiveGameData_CPP_Adapter, "prepareTelemetryDataUpdate", "()V" );
    
    if ( prepareTelemetryDataUpdate == 0 )
    {
        logg( "ERROR: Failed to find the prepareTelemetryDataUpdate() method." );
        return ( false );
    }
    
    mid = env->GetMethodID( LiveGameData_CPP_Adapter, "getTelemetryBuffer", "()[B" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getTelemetryBuffer() method." );
        return ( false );
    }
    
    telemetryBuffer = (jarray)env->CallObjectMethod( gameData_CPP_Adapter, mid );
    
    if ( telemetryBuffer == NULL )
    {
        logg( "ERROR: telemetryBuffer is null." );
        return ( false );
    }
    
    notifyTelemetryUpdated = env->GetMethodID( LiveGameData_CPP_Adapter, "notifyTelemetryUpdated", "()V" );
    
    if ( notifyTelemetryUpdated == 0 )
    {
        logg( "ERROR: Failed to find the notifyTelemetryUpdated method." );
        return ( false );
    }
    
    prepareScoringInfoDataUpdate = env->GetMethodID( LiveGameData_CPP_Adapter, "prepareScoringInfoDataUpdate", "()V" );
    
    if ( prepareScoringInfoDataUpdate == 0 )
    {
        logg( "ERROR: Failed to find the prepareScoringInfoDataUpdate() method." );
        return ( false );
    }
    
    mid = env->GetMethodID( LiveGameData_CPP_Adapter, "getScoringInfoBuffer", "()[B" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getScoringInfoBuffer() method." );
        return ( false );
    }
    
    scoringInfoBuffer = (jarray)env->CallObjectMethod( gameData_CPP_Adapter, mid );
    
    if ( scoringInfoBuffer == NULL )
    {
        logg( "ERROR: scoringInfoBuffer is null." );
        return ( false );
    }
    
    initVehicleScoringInfo = env->GetMethodID( LiveGameData_CPP_Adapter, "initVehicleScoringInfo", "()V" );
    
    if ( initVehicleScoringInfo == 0 )
    {
        logg( "ERROR: Failed to find the initVehicleScoringInfo() method." );
        return ( false );
    }
    
    getVehicleScoringInfoBuffer = env->GetMethodID( LiveGameData_CPP_Adapter, "getVehicleScoringInfoBuffer", "(I)[B" );
    
    if ( getVehicleScoringInfoBuffer == 0 )
    {
        logg( "ERROR: Failed to find the getVehicleScoringInfoBuffer() method." );
        return ( false );
    }
    
    notifyScoringInfoUpdated = env->GetMethodID( LiveGameData_CPP_Adapter, "notifyScoringInfoUpdated", "()V" );
    
    if ( notifyScoringInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the notifyScoringInfoUpdated() method." );
        return ( false );
    }
    
    prepareGraphicsInfoDataUpdate = env->GetMethodID( LiveGameData_CPP_Adapter, "prepareGraphicsInfoDataUpdate", "()V" );
    
    if ( prepareGraphicsInfoDataUpdate == 0 )
    {
        logg( "ERROR: Failed to find the prepareGraphicsInfoDataUpdate() method." );
        return ( false );
    }
    
    mid = env->GetMethodID( LiveGameData_CPP_Adapter, "getGraphicsInfoBuffer", "()[B" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getGraphicsInfoBuffer() method." );
        return ( false );
    }
    
    graphicsInfoBuffer = (jarray)env->CallObjectMethod( gameData_CPP_Adapter, mid );
    
    if ( graphicsInfoBuffer == NULL )
    {
        logg( "ERROR: graphicsInfoBuffer is null." );
        return ( false );
    }
    
    notifyGraphicsInfoUpdated = env->GetMethodID( LiveGameData_CPP_Adapter, "notifyGraphicsInfoUpdated", "()V" );
    
    if ( notifyGraphicsInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the notifyGraphicsInfoUpdated() method." );
        return ( false );
    }
    
    prepareCommentaryInfoDataUpdate = env->GetMethodID( LiveGameData_CPP_Adapter, "prepareCommentaryInfoDataUpdate", "()V" );
    
    if ( prepareCommentaryInfoDataUpdate == 0 )
    {
        logg( "ERROR: Failed to find the prepareCommentaryInfoDataUpdate() method." );
        return ( false );
    }
    
    mid = env->GetMethodID( LiveGameData_CPP_Adapter, "getCommentaryInfoBuffer", "()[B" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getCommentaryInfoBuffer() method." );
        return ( false );
    }
    
    commentaryInfoBuffer = (jarray)env->CallObjectMethod( gameData_CPP_Adapter, mid );
    
    if ( commentaryInfoBuffer == NULL )
    {
        logg( "ERROR: commentaryInfoBuffer is null." );
        return ( false );
    }
    
    notifyCommentaryInfoUpdated = env->GetMethodID( LiveGameData_CPP_Adapter, "notifyCommentaryInfoUpdated", "()V" );
    
    if ( notifyCommentaryInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the notifyCommentaryInfoUpdated() method." );
        return ( false );
    }
    
    return ( true );
}

void JVMTelemtryUpdateFunctions::destroy()
{
    notifyTelemetryUpdated = 0;
    if ( telemetryBuffer != NULL )
        env->DeleteLocalRef( telemetryBuffer );
    telemetryBuffer = NULL;
    
    notifyScoringInfoUpdated = 0;
    getVehicleScoringInfoBuffer = 0;
    initVehicleScoringInfo = 0;
    if ( scoringInfoBuffer != NULL )
        env->DeleteLocalRef( scoringInfoBuffer );
    scoringInfoBuffer = NULL;
    
    notifyGraphicsInfoUpdated = 0;
    if ( graphicsInfoBuffer != NULL )
        env->DeleteLocalRef( graphicsInfoBuffer );
    graphicsInfoBuffer = NULL;
    
    notifyCommentaryInfoUpdated = 0;
    if ( commentaryInfoBuffer != NULL )
        env->DeleteLocalRef( commentaryInfoBuffer );
    commentaryInfoBuffer = NULL;
    
    gameEventsManager = NULL;
    if ( gameEventsManager != NULL )
        env->DeleteLocalRef( gameEventsManager );
    GameEventsManager = NULL;
    
    if ( gameData_CPP_Adapter != NULL )
        env->DeleteLocalRef( gameData_CPP_Adapter );
    gameData_CPP_Adapter = NULL;
    LiveGameData_CPP_Adapter = NULL;
    
    onRealtimeExited = 0;
    onRealtimeEntered = 0;
    onSessionEnded = 0;
    onSessionStarted = 0;
    onShutdown = 0;
    onStartup = 0;
    
    env = NULL;
}

bool JVMConnection::init( const char* PLUGIN_PATH, const unsigned int resX, const unsigned int resY )
{
    if ( !createNewJavaVM( PLUGIN_PATH, &jvm, &env ) )
        return ( false );
    
    logg( "Retrieving Java objects and methods..." );
    
    RFDynHUD = env->FindClass( "net/ctdp/rfdynhud/RFDynHUD" );
    
    if ( RFDynHUD == 0 )
    {
        logg( "ERROR: Failed to find the main class." );
        return ( false );
    }
    
    //jmethodID mid = env->GetMethodID( RFDynHUD, "<init>", "(II)V" );
    jmethodID mid = env->GetStaticMethodID( RFDynHUD, "createInstance", "([BII)Lnet/ctdp/rfdynhud/RFDynHUD;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the constructor method." );
        return ( false );
    }
    
    jbyteArray gameNameBuffer = env->NewByteArray( strlen( GAME_NAME ) );
    void* gameNameBufferPtr = env->GetPrimitiveArrayCritical( gameNameBuffer, NULL );
    memcpy( gameNameBufferPtr, GAME_NAME, strlen( GAME_NAME ) );
    env->ReleasePrimitiveArrayCritical( gameNameBuffer, gameNameBufferPtr, 0 );
    
    //rfDynHUD = env->NewObject( RFDynHUD, mid, resX, resY );
    rfDynHUD = env->CallStaticObjectMethod( RFDynHUD, mid, gameNameBuffer, resX, resY );
    
    env->DeleteLocalRef( gameNameBuffer );
    
    if ( rfDynHUD == NULL )
    {
        logg( "ERROR: Failed to create the RFDynHUD object through the constructor." );
        return ( false );
    }
    
    if ( !d3dFuncs.init( env, RFDynHUD, rfDynHUD ) )
        return ( false );
    
    if ( !inputFuncs.init( env, RFDynHUD, rfDynHUD ) )
        return ( false );
    
    if ( !telemFuncs.init( env, RFDynHUD, rfDynHUD ) )
        return ( false );
    
    logg( "Successfully retrieved Java objects and methods." );
    
    return ( true );
}

void JVMConnection::destroy()
{
    if ( jvm == NULL )
        return;
    
    logg( "Destroying Java VM..." );
    
    telemFuncs.destroy();
    inputFuncs.destroy();
    d3dFuncs.destroy();
    
    if ( rfDynHUD != NULL )
        env->DeleteLocalRef( rfDynHUD );
    rfDynHUD = NULL;
    RFDynHUD = NULL;
    
    jvm->DestroyJavaVM();
    
    env = NULL;
    jvm = NULL;
    
    logg( "Successfully destroyed Java VM." );
}
