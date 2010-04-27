#include "jvm_connection.hpp"
#include <Windows.h>
#include <winreg.h>
#include <jni.h>
#include "direct_input.h"
#include "filesystem.h"
#include "logging.h"

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
    
    logg( "WARNING: Registry key for Java 6 Runtime Environment not found." );
    
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
        
        logg( "WARNING: Couldn't find Java 6 Runtime Environment in the default folder." );
        
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
    char* fileBuffer = (char*)malloc( 2048 );
    
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
    
    char* searchPath = cropBuffer2( appendPath2( "\\*.jar", setBuffer( PLUGIN_PATH, fileBuffer ), false ) );
    
    setBuffer( "-Djava.class.path=", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud.jar", fileBuffer );
    
    WIN32_FIND_DATA data;
    HANDLE hFile = FindFirstFile( searchPath, &data );
    if ( hFile != INVALID_HANDLE_VALUE )
    {
        do
        {
            if ( strcmp( data.cFileName, "rfdynhud.jar" ) != 0 )
            {
                addPostFix( ";", fileBuffer );
                addPostFix( PLUGIN_PATH, fileBuffer );
                addPostFix( "\\", fileBuffer );
                addPostFix( data.cFileName, fileBuffer );
            }
        }
        while ( FindNextFile( hFile, &data ) );
    }
    
    const unsigned int nOptions = 10;
    JavaVMOption options[nOptions];
    
    options[0].optionString = cropBuffer2( fileBuffer );
    options[1].optionString = cropBuffer2( addPreFix( "-Dworkdir=", setBuffer( PLUGIN_PATH, fileBuffer ) ) );
    options[2].optionString = "-Xms96m";
    options[3].optionString = "-Xmx96m";
    options[4].optionString = "-XX:MaxGCPauseMillis=5";
    options[5].optionString = "-XX:+UseAdaptiveSizePolicy";
    options[6].optionString = "-Xincgc";
    options[7].optionString = "-Dsun.java2d.opengl=true";
    options[8].optionString = "-Dsun.java2d.d3d=false";
    options[9].optionString = "-Dsun.java2d.noddraw=true";
    
    free( fileBuffer );
    
    logg( "JVM options:" );
    for ( unsigned int i = 0; i < nOptions; i++ )
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
    
    jobject bb = env->CallObjectMethod( rfdynhudObject, mid );
    
    if ( bb == NULL )
    {
        logg( "ERROR: Failed to get the buffer for texture info." );
        return ( false );
    }
    
    textureInfoBuffer = (char*)env->GetDirectBufferAddress( bb );
    
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
    
    updateMethod = env->GetMethodID( rfdynhudClass, "update", "()V" );
    
    if ( updateMethod == 0 )
    {
        logg( "ERROR: Failed to find the update() method." );
        return ( false );
    }
    
    return ( true );
}

unsigned char JVMD3DUpdateFunctions::updateAllTextureInfos()
{
    numTextures = *(unsigned char*)textureInfoBuffer;
    
    for ( unsigned int i = 0; i < (unsigned int)numTextures; i++ )
    {
        jobject bb = env->CallObjectMethod( rfdynhudObject, getDirtyRectsBufferMethod, (jint)i );
        
        if ( bb == NULL )
        {
            logg( "ERROR: Failed to get the buffer for dirty rects." );
            continue;
        }
        
        dirtyRectsBuffers[i] = (unsigned short*)env->GetDirectBufferAddress( bb );
        
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
    pixelData = (jarray)env->CallObjectMethod( rfdynhudObject, getPixelDataMethod, (jint)textureIndex );
    
    if ( pixelData == NULL )
    {
        logg( "ERROR: texture pixel data is null." );
        return ( false );
    }
    
    return ( (unsigned char*)env->GetPrimitiveArrayCritical( pixelData, &isCopy ) );
}

void JVMD3DUpdateFunctions::releasePixelData( unsigned char textureIndex, unsigned char* pointer )
{
    env->ReleasePrimitiveArrayCritical( pixelData, pointer, 0 );
    pixelData = NULL;
}

void JVMD3DUpdateFunctions::destroy()
{
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
    
    mid = env->GetMethodID( rfdynhudClass, "getInputBuffer", "()Ljava/nio/ByteBuffer;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getInputBuffer() method." );
        return ( false );
    }
    
    jobject bb = env->CallObjectMethod( _rfdynhudObject, mid );
    
    if ( bb == NULL )
    {
        logg( "ERROR: Failed to get the input buffer." );
        return ( false );
    }
    
    inputBuffer = (char*)env->GetDirectBufferAddress( bb );
    
    if ( inputBuffer == NULL )
    {
        logg( "ERROR: Input buffer is null." );
        return ( false );
    }
    
    updateInputMethod = env->GetMethodID( rfdynhudClass, "updateInput", "(I)Z" );
    
    if ( updateInputMethod == 0 )
    {
        logg( "ERROR: Failed to find the updateInput() method." );
        return ( false );
    }
    
    return ( true );
}

void JVMInputFunctions::updateInput( bool* isPluginEnabled )
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
    
    *isPluginEnabled = ( env->CallBooleanMethod( rfdynhudObject, updateInputMethod, (int)modifierMask ) == JNI_TRUE );
}

void JVMInputFunctions::destroy()
{
    updateInputMethod = 0;
    inputBuffer = NULL;
    rfdynhudObject = NULL;
    env = NULL;
}

void JVMTelemtryUpdateFunctions::call_onStartup()
{
    env->CallVoidMethod( eventsManager, onStartup );
}

void JVMTelemtryUpdateFunctions::call_onShutdown()
{
    env->CallVoidMethod( eventsManager, onShutdown );
}

void JVMTelemtryUpdateFunctions::call_onSessionStarted()
{
    env->CallVoidMethod( eventsManager, onSessionStarted );
}

void JVMTelemtryUpdateFunctions::call_onSessionEnded()
{
    env->CallVoidMethod( eventsManager, onSessionEnded );
}

void JVMTelemtryUpdateFunctions::call_onRealtimeEntered()
{
    env->CallVoidMethod( eventsManager, onRealtimeEntered );
}

void JVMTelemtryUpdateFunctions::call_onRealtimeExited()
{
    env->CallVoidMethod( eventsManager, onRealtimeExited );
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
    
    RFactorEventsManager = env->FindClass( "net/ctdp/rfdynhud/util/RFactorEventsManager" );
    
    if ( RFactorEventsManager == 0 )
    {
        logg( "ERROR: Failed to find the RFactorEventsManager class." );
        return ( false );
    }
    
    jmethodID mid = env->GetMethodID( rfdynhudClass, "getEventsManager", "()Lnet/ctdp/rfdynhud/util/RFactorEventsManager;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getEventsManager() method." );
        return ( false );
    }
    
    eventsManager = env->CallObjectMethod( rfdynhudObject, mid );
    
    if ( eventsManager == NULL )
    {
        logg( "ERROR: eventsManager is null." );
        return ( false );
    }
    
    onStartup = env->GetMethodID( RFactorEventsManager, "onStartup", "()V" );
    
    if ( onStartup == 0 )
    {
        logg( "ERROR: Failed to find the onStartup() method." );
        return ( false );
    }
    
    onShutdown = env->GetMethodID( RFactorEventsManager, "onShutdown", "()V" );
    
    if ( onShutdown == 0 )
    {
        logg( "ERROR: Failed to find the onShutdown() method." );
        return ( false );
    }
    
    onSessionStarted = env->GetMethodID( RFactorEventsManager, "onSessionStarted", "()V" );
    
    if ( onSessionStarted == 0 )
    {
        logg( "ERROR: Failed to find the onSessionStarted() method." );
        return ( false );
    }
    
    onSessionEnded = env->GetMethodID( RFactorEventsManager, "onSessionEnded", "()V" );
    
    if ( onSessionEnded == 0 )
    {
        logg( "ERROR: Failed to find the onSessionEnded() method." );
        return ( false );
    }
    
    onRealtimeEntered = env->GetMethodID( RFactorEventsManager, "onRealtimeEntered", "()V" );
    
    if ( onRealtimeEntered == 0 )
    {
        logg( "ERROR: Failed to find the onRealtimeEntered() method." );
        return ( false );
    }
    
    onRealtimeExited = env->GetMethodID( RFactorEventsManager, "onRealtimeExited", "()V" );
    
    if ( onRealtimeExited == 0 )
    {
        logg( "ERROR: Failed to find the onRealtimeExited() method." );
        return ( false );
    }
    
    LiveGameData_CPP_Adapter = env->FindClass( "net/ctdp/rfdynhud/gamedata/LiveGameData_CPP_Adapter" );
    
    if ( LiveGameData_CPP_Adapter == 0 )
    {
        logg( "ERROR: Failed to find the LiveGameData_CPP_Adapter class." );
        return ( false );
    }
    
    mid = env->GetMethodID( rfdynhudClass, "getGameData_CPP_Adapter", "()Lnet/ctdp/rfdynhud/gamedata/LiveGameData_CPP_Adapter;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getGameData_CPP_Adapter() method." );
        return ( false );
    }
    
    gameData_CPP_Adapter = env->CallObjectMethod( rfdynhudObject, mid );
    
    if ( gameData_CPP_Adapter == NULL )
    {
        logg( "ERROR: gameData_CPP_Adapter is null." );
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
    notifyCommentaryInfoUpdated = 0;
    commentaryInfoBuffer = NULL;
    notifyGraphicsInfoUpdated = 0;
    graphicsInfoBuffer = NULL;
    notifyScoringInfoUpdated = 0;
    getVehicleScoringInfoBuffer = 0;
    initVehicleScoringInfo = 0;
    scoringInfoBuffer = NULL;
    notifyTelemetryUpdated = 0;
    telemetryBuffer = NULL;
    
    gameData_CPP_Adapter = NULL;
    LiveGameData_CPP_Adapter = NULL;
    
    onRealtimeExited = 0;
    onRealtimeEntered = 0;
    onSessionEnded = 0;
    onSessionStarted = 0;
    onShutdown = 0;
    onStartup = 0;
    
    eventsManager = NULL;
    RFactorEventsManager = NULL;
    
    env = NULL;
}

bool JVMConnection::init( const char* PLUGIN_PATH, const unsigned int resX, const unsigned int resY )
{
    if ( !createNewJavaVM( PLUGIN_PATH, &jvm, &env ) )
        return ( false );
    
    logg( "Retrieving Java objects and methods..." );
    
    rfdynhudClass = env->FindClass( "net/ctdp/rfdynhud/RFDynHUD" );
    
    if ( rfdynhudClass == 0 )
    {
        logg( "ERROR: Failed to find the main class." );
        return ( false );
    }
    
    //jmethodID mid = env->GetMethodID( rfdynhudClass, "<init>", "(II)V" );
    jmethodID mid = env->GetStaticMethodID( rfdynhudClass, "createInstance", "(II)Lnet/ctdp/rfdynhud/RFDynHUD;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the constructor method." );
        return ( false );
    }
    
    //rfdynhudObject = env->NewObject( rfdynhudClass, mid, resX, resY );
    rfdynhudObject = env->CallStaticObjectMethod( rfdynhudClass, mid, resX, resY );
    
    if ( rfdynhudObject == NULL )
    {
        logg( "ERROR: Failed to create the object through the constructor." );
        return ( false );
    }
    
    mid = env->GetMethodID( rfdynhudClass, "getFlagsBuffer", "()Ljava/nio/ByteBuffer;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the getFlagsBuffer() method." );
        return ( false );
    }
    
    jobject bb = env->CallObjectMethod( rfdynhudObject, mid );
    
    if ( bb == NULL )
    {
        logg( "ERROR: Failed to get the flags buffer." );
        return ( false );
    }
    
    flagsBuffer = (char*)env->GetDirectBufferAddress( bb );
    
    if ( !d3dFuncs.init( env, rfdynhudClass, rfdynhudObject ) )
        return ( false );
    
    if ( !inputFuncs.init( env, rfdynhudClass, rfdynhudObject ) )
        return ( false );
    
    if ( !telemFuncs.init( env, rfdynhudClass, rfdynhudObject ) )
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
    
    rfdynhudObject = NULL;
    rfdynhudClass = NULL;
    
    jvm->DestroyJavaVM();
    
    env = NULL;
    jvm = NULL;
    
    logg( "Successfully destroyed Java VM." );
}
