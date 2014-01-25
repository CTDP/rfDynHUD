#include "jvm_connection.hpp"
#include <Windows.h>
#include <winreg.h>
#include <jni.h>
#include "direct_input.h"
#include "filesystem.h"
#include "common.h"
#include "util.h"
#include "logging.h"
#include "java_native_methods.h"

char* readJavaHomeFromRegistry()
{
    char* buffer0 = (char*)malloc( sizeof( int ) + MAX_PATH );
    char* buffer = buffer0 + sizeof( int );
    HKEY keyHandle;
    DWORD size1;
    DWORD Type;
    
    if ( RegOpenKeyEx( HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment\\1.7", 0, KEY_QUERY_VALUE, &keyHandle ) == ERROR_SUCCESS )
    {
        size1 = MAX_PATH - 1;
        RegQueryValueEx( keyHandle, "JavaHome", NULL, &Type, (LPBYTE)buffer, &size1 );
        RegCloseKey( keyHandle );
        
        *((int*)buffer0) = 17;
        
        return ( buffer0 );
    }
    
    if ( RegOpenKeyEx( HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment\\1.6", 0, KEY_QUERY_VALUE, &keyHandle ) == ERROR_SUCCESS )
    {
        size1 = MAX_PATH - 1;
        RegQueryValueEx( keyHandle, "JavaHome", NULL, &Type, (LPBYTE)buffer, &size1 );
        RegCloseKey( keyHandle );
        
        *((int*)buffer0) = 16;
        
        return ( buffer0 );
    }
    
    free( buffer0 );
    
    logg( "WARNING: Registry key for 32 bit Java 7 or 6 Runtime Environment not found." );
    
    return ( NULL );
}

char* guessJavaHome()
{
    char* buffer0 = (char*)malloc( sizeof( int ) + MAX_PATH );
    char* buffer = buffer0 + sizeof( int );
    
    DWORD len = GetEnvironmentVariable( "ProgramFiles", buffer, MAX_PATH );
    DWORD lenPF = len;
    
    char* buff = buffer + lenPF;
    memcpy( buff, "\\Java\\jre7", 11 );
    len += 11;
    
	if ( checkDirectoryExists( buffer, false ) == 1 )
    {
        *((int*)buffer0) = 17;
        
        return ( buffer0 );
    }
    
    buff = buffer + lenPF;
    memcpy( buff, "\\Java\\jre6", 11 );
    len += 11;
    
	if ( checkDirectoryExists( buffer, false ) == 1 )
    {
        *((int*)buffer0) = 16;
        
        return ( buffer0 );
    }
    
    free( buffer0 );
    
    logg( "WARNING: Couldn't find 32 bit Java 7 or 6 Runtime Environment in the default folder." );
    
    return ( NULL );
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
    logg( "        ", false );
    logg( prop, false );
    logg( " = \"", false );
    logg( cstr, false );
    logg( "\"", true );
    env->ReleaseStringUTFChars( jstr, cstr );
}

void appendHeapSizeArguments( JavaVMOption* options, unsigned int* i, char* fileBuffer, const unsigned int FILEBUFFER_LENGTH )
{
    setBuffer( "-Xms", fileBuffer );
    getPluginIniSetting( "GENERAL", "pluginMemory", "96m", fileBuffer + 4, FILEBUFFER_LENGTH - 4 );
    options[(*i)++].optionString = cropBuffer2( fileBuffer );
    fileBuffer[3] = 'x'; // convert to "-Xmx"
    options[(*i)++].optionString = cropBuffer2( fileBuffer );
}

bool createNewJavaVM( const char* PLUGIN_PATH, JavaVM** jvm, JNIEnv** env )
{
    const unsigned int FILEBUFFER_LENGTH = 16384;
    char* fileBuffer = (char*)malloc( FILEBUFFER_LENGTH );
    
    if ( JAVA_HOME == NULL )
    {
        logg( "ERROR: Could not locate JAVA_HOME." );
        
        return ( false );
    }
    
    const char* jh = JAVA_HOME + sizeof( int );
    
    memcpy( fileBuffer, "Using Java from folder \"", 24 );
    memcpy( fileBuffer + 24, jh, strlen( jh ) );
    memcpy( fileBuffer + 24 + strlen( jh ), "\".", 3 );

    logg( fileBuffer );
    
    const int jreVersion = *((int*) JAVA_HOME );
    
    if ( jreVersion == 16 )
    {
        getFullPath( jh, "bin\\msvcr71.dll", fileBuffer );
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
    }
    else if ( jreVersion == 17 )
    {
        getFullPath( jh, "bin\\msvcr100.dll", fileBuffer );
        logg( "    Loading msvcr100.dll...", false );
        HMODULE msvcdll = LoadLibrary( fileBuffer );
        
        if ( msvcdll == NULL )
        {
            logg( " ERROR: Failed to load msvcr100.dll." );
            return ( false );
        }
        else
        {
            logg( " done." );
        }
    }
    else
    {
        loggi( " ERROR: Unsupported JRE version ", jreVersion );
        return ( false );
    }
    
    getFullPath( jh, "bin\\client\\jvm.dll", fileBuffer );
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
    
    logg( "Successfully loaded Java DLLs." );
    
    logg( "Invoking Java VM..." );
    
    JavaVMOption* options = new JavaVMOption[32];
    unsigned int i = 0;
    
    // Prepend rFactor's Plugins folder to the java.library.path.
    setBuffer( "-Djava.library.path=", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    removeLastPathComponent( fileBuffer, -1 );
    addPostFix( ";${system_property:java.library.path}", fileBuffer );
    options[i++].optionString = cropBuffer2( fileBuffer );
    
    // Add rfdynhud.jar and rFactor2-specific gamedata jar to the classpath.
    setBuffer( "-Djava.class.path=", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud.jar", fileBuffer );
    addPostFix( ";", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud_gamedata_rfactor2.jar", fileBuffer );
	options[i++].optionString = cropBuffer2( fileBuffer );
    
    options[i++].optionString = cropBuffer2( addPreFix( "-Dworkdir=", setBuffer( PLUGIN_PATH, fileBuffer ) ) );
    appendHeapSizeArguments( options, &i, fileBuffer, FILEBUFFER_LENGTH );
    options[i++].optionString = "-XX:MaxGCPauseMillis=5";
    options[i++].optionString = "-XX:+UseAdaptiveSizePolicy";
    options[i++].optionString = "-Xincgc";
    options[i++].optionString = "-Dsun.java2d.opengl=true";
    options[i++].optionString = "-Dsun.java2d.d3d=false";
    options[i++].optionString = "-Dsun.java2d.noddraw=true";
//options[i++].optionString = "-Xcheck:jni";
    
    DWORD len = GetEnvironmentVariable( "YourKitAgentDLL", fileBuffer + 11, FILEBUFFER_LENGTH - 11 );
    if ( len > 0 )
    {
		memcpy( fileBuffer, "-agentpath:", 11 );
        options[i++].optionString = cropBuffer( fileBuffer, 11 + len + 1 );
    }
    
    free( fileBuffer );
    
    const unsigned int nOptions = i;
    
    logg( "    JVM options:" );
    for ( i = 0; i < nOptions; i++ )
        logg2( "        ", options[i].optionString, true );
    
    JavaVMInitArgs vm_args;
    
    vm_args.version = JNI_VERSION_1_6;
    vm_args.options = options;
    vm_args.nOptions = nOptions;
    vm_args.ignoreUnrecognized = FALSE;
    
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
    
    logg( "    JVM properties:" );
    
    jclass System = (*env)->FindClass( "java/lang/System" );
    jmethodID getProperty = (*env)->GetStaticMethodID( System, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;" );
    loggSystemProperty( *env, System, getProperty, "java.vm.vendor" );
    loggSystemProperty( *env, System, getProperty, "java.vm.name" );
    loggSystemProperty( *env, System, getProperty, "java.vm.version" );
    loggSystemProperty( *env, System, getProperty, "java.runtime.version" );
    loggSystemProperty( *env, System, getProperty, "java.awt.graphicsenv" );
    (*env)->DeleteLocalRef( System );
    
    logg( "Successfully invoked Java VM." );
    
    delete options;
    
    return ( true );
}

jclass globalizeClass( JNIEnv* env, jclass local )
{
    if ( local == NULL )
        return ( NULL );
    
    jclass global = (jclass)env->NewGlobalRef( local );
    
    env->DeleteLocalRef( local );
    
    return ( global );
}

jobject globalizeObject( JNIEnv* env, jobject local )
{
    if ( local == NULL )
        return ( NULL );
    
    jobject global = env->NewGlobalRef( local );
    
    env->DeleteLocalRef( local );
    
    return ( global );
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
    
    textureInfoBufferObj = globalizeObject( env, env->CallObjectMethod( rfdynhudObject, mid ) );
    
    if ( textureInfoBufferObj == NULL )
    {
        logg( "ERROR: Failed to get the buffer for texture info." );
        return ( false );
    }
    
    textureInfoBuffer = (char*)env->GetDirectBufferAddress( textureInfoBufferObj );
    
    if ( textureInfoBuffer == NULL )
    {
        logg( "ERROR: Texture info buffer is null." );
        return ( false );
    }
    
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

void JVMD3DUpdateFunctions::setEnv( JavaVM* jvm, JNIEnv* _env )
{
    env = _env;
}

void JVMD3DUpdateFunctions::releaseDirtyRectsBufferObjects()
{
    if ( numTextures <= 0 )
        return;
    
    for ( unsigned int i = 0; i < (unsigned int)numTextures; i++ )
    {
        if ( dirtyRectsBufferObj[i] != NULL )
            env->DeleteGlobalRef( dirtyRectsBufferObj[i] );
        dirtyRectsBufferObj[i] = NULL;
    }
    
    free( dirtyRectsBufferObj );
    dirtyRectsBufferObj = NULL;
}

unsigned char JVMD3DUpdateFunctions::updateAllTextureInfos()
{
    releaseDirtyRectsBufferObjects();
    
    numTextures = *(unsigned char*)textureInfoBuffer;
    
    if ( numTextures == 0 )
        return ( 0 );
    
    dirtyRectsBufferObj = (jobject*)malloc( numTextures * sizeof( jobject ) );
    
    for ( unsigned int i = 0; i < (unsigned int)numTextures; i++ )
    {
        dirtyRectsBufferObj[i] = globalizeObject( env, env->CallObjectMethod( rfdynhudObject, getDirtyRectsBufferMethod, (jint)i ) );
        
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
        env->DeleteGlobalRef( textureInfoBufferObj );
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
    
    inputBufferObj = globalizeObject( env, env->CallObjectMethod( _rfdynhudObject, mid ) );
    
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

void JVMInputFunctions::setEnv( JavaVM* jvm, JNIEnv* _env )
{
    env = _env;
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
        env->DeleteGlobalRef( inputBufferObj );
    inputBufferObj = NULL;
    inputBuffer = NULL;
    rfdynhudObject = NULL;
    env = NULL;
}

void JVMTelemtryUpdateFunctions::call_onStartup()
{
    env->CallVoidMethod( gameEventsManager, onStartup, NULL );
}

void JVMTelemtryUpdateFunctions::call_onShutdown()
{
    env->CallVoidMethod( gameEventsManager, onShutdown, NULL );
}

char JVMTelemtryUpdateFunctions::call_onSessionStarted()
{
    return ( env->CallByteMethod( gameEventsManager, onSessionStarted, NULL ) );
}

void JVMTelemtryUpdateFunctions::call_onSessionEnded()
{
    env->CallVoidMethod( gameEventsManager, onSessionEnded, NULL );
}

char JVMTelemtryUpdateFunctions::call_onRealtimeEntered()
{
    return ( env->CallByteMethod( gameEventsManager, onRealtimeEntered, NULL ) );
}

char JVMTelemtryUpdateFunctions::call_onRealtimeExited()
{
    return ( env->CallByteMethod( gameEventsManager, onRealtimeExited, NULL ) );
}

char JVMTelemtryUpdateFunctions::call_onTelemetryDataUpdated( void* buffer, const unsigned int size )
{
    env->CallVoidMethod( telemetryDataAddressKeeper, setBufferInfo, (jlong)(long)buffer, (jint)size );
    //return ( env->CallBooleanMethod( gameEventsManager, onTelemetryDataUpdated, telemetryDataAddressKeeper ) == JNI_TRUE );
    return ( env->CallByteMethod( gameEventsManager, onTelemetryDataUpdated, telemetryDataAddressKeeper ) );
}

JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1TelemetryData_fetchData( JNIEnv* env, jobject telemetryData, jlong sourceBufferAddress, jint sourceBufferSize, jbyteArray targetBuffer )
{
    void* buffer = env->GetPrimitiveArrayCritical( targetBuffer, &isCopy );
    memcpy( buffer, (void*)(long)sourceBufferAddress, (unsigned int)sourceBufferSize );
    env->ReleasePrimitiveArrayCritical( targetBuffer, buffer, 0 );
}

char JVMTelemtryUpdateFunctions::call_onScoringInfoUpdated( const long numVehicles, void* buffer, const unsigned int size, void* buffer2, const unsigned int size2 )
{
    env->CallVoidMethod( scoringInfoAddressKeeper, setBufferInfo1, (jlong)(long)buffer, (jint)size );
    env->CallVoidMethod( scoringInfoAddressKeeper, setBufferInfo2, (jlong)(long)buffer2, (jint)size2 );
    //return ( env->CallBooleanMethod( gameEventsManager, onScoringInfoUpdated, (jint)numVehicles, scoringInfoAddressKeeper ) == JNI_TRUE );
    return ( env->CallByteMethod( gameEventsManager, onScoringInfoUpdated, (jint)numVehicles, scoringInfoAddressKeeper ) );
}

JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1ScoringInfo_fetchData( JNIEnv* env, jobject scoringInfo, jint numVehicles, jlong sourceBufferAddress, jint sourceBufferSize, jbyteArray targetBuffer, jlong sourceBufferAddress2, jint sourceBufferSize2, jbyteArray targetBuffer2 )
{
    void* buffer = env->GetPrimitiveArrayCritical( targetBuffer, &isCopy );
    memcpy( buffer, (void*)(long)sourceBufferAddress, (unsigned int)sourceBufferSize );
    env->ReleasePrimitiveArrayCritical( targetBuffer, buffer, 0 );
    
    if ( (int)sourceBufferSize2 > 0 )
    {
        void* buffer2 = env->GetPrimitiveArrayCritical( targetBuffer2, &isCopy );
        
        unsigned int numVehicles_ = (unsigned int)numVehicles;
        memcpy( buffer2, (void*)(long)sourceBufferAddress2, (unsigned int)sourceBufferSize2 * numVehicles_ );
        env->ReleasePrimitiveArrayCritical( targetBuffer2, buffer2, 0 );
    }
}

char JVMTelemtryUpdateFunctions::call_onCommentaryRequestInfoUpdated( void* buffer, const unsigned int size )
{
    env->CallVoidMethod( commentaryRequestInfoAddressKeeper, setBufferInfo, (jlong)(long)buffer, (jint)size );
    return ( env->CallByteMethod( gameEventsManager, onCommentaryRequestInfoUpdated, commentaryRequestInfoAddressKeeper ) );
}

JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1CommentaryRequestInfo_fetchData( JNIEnv* env, jobject commentaryRequestInfo, jlong sourceBufferAddress, jint sourceBufferSize, jbyteArray targetBuffer )
{
    void* buffer = env->GetPrimitiveArrayCritical( targetBuffer, &isCopy );
    memcpy( buffer, (void*)(long)sourceBufferAddress, (unsigned int)sourceBufferSize );
    env->ReleasePrimitiveArrayCritical( targetBuffer, buffer, 0 );
}

char JVMTelemtryUpdateFunctions::call_onGraphicsInfoUpdated( void* buffer, const unsigned int size )
{
    env->CallVoidMethod( graphicsInfoAddressKeeper, setBufferInfo, (jlong)(long)buffer, (jint)size );
    return ( env->CallByteMethod( gameEventsManager, onGraphicsInfoUpdated, graphicsInfoAddressKeeper ) );
}

JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1GraphicsInfo_fetchData( JNIEnv* env, jobject graphicsInfo, jlong sourceBufferAddress, jint sourceBufferSize, jbyteArray targetBuffer )
{
    void* buffer = env->GetPrimitiveArrayCritical( targetBuffer, &isCopy );
    memcpy( buffer, (void*)(long)sourceBufferAddress, (unsigned int)sourceBufferSize );
    env->ReleasePrimitiveArrayCritical( targetBuffer, buffer, 0 );
}

char JVMTelemtryUpdateFunctions::call_beforeRender( const unsigned short viewportX, const unsigned short viewportY, const unsigned short viewportWidth, const unsigned short viewportHeight )
{
    return ( env->CallByteMethod( gameEventsManager, beforeRender, (jshort)viewportX, (jshort)viewportY, (jshort)viewportWidth, (jshort)viewportHeight ) );
}


bool JVMTelemtryUpdateFunctions::init( JNIEnv* _env, jclass _rfdynhudClass, jobject _rfdynhudObject )
{
    env = _env;
    rfdynhudClass = _rfdynhudClass;
    rfdynhudObject = _rfdynhudObject;
    
    GameEventsManager = globalizeClass( env, env->FindClass( "net/ctdp/rfdynhud/gamedata/GameEventsManager" ) );
    
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
    
    gameEventsManager = globalizeObject( env, env->CallObjectMethod( rfdynhudObject, mid ) );
    
    if ( gameEventsManager == NULL )
    {
        logg( "ERROR: gameEventsManager is null." );
        return ( false );
    }
    
    onStartup = env->GetMethodID( GameEventsManager, "onStartup", "(Ljava/lang/Object;)V" );
    
    if ( onStartup == 0 )
    {
        logg( "ERROR: Failed to find the onStartup() method." );
        return ( false );
    }
    
    onShutdown = env->GetMethodID( GameEventsManager, "onShutdown", "(Ljava/lang/Object;)V" );
    
    if ( onShutdown == 0 )
    {
        logg( "ERROR: Failed to find the onShutdown() method." );
        return ( false );
    }
    
    onSessionStarted = env->GetMethodID( GameEventsManager, "onSessionStarted", "(Ljava/lang/Object;)B" );
    
    if ( onSessionStarted == 0 )
    {
        logg( "ERROR: Failed to find the onSessionStarted() method." );
        return ( false );
    }
    
    onSessionEnded = env->GetMethodID( GameEventsManager, "onSessionEnded", "(Ljava/lang/Object;)V" );
    
    if ( onSessionEnded == 0 )
    {
        logg( "ERROR: Failed to find the onSessionEnded() method." );
        return ( false );
    }
    
    onRealtimeEntered = env->GetMethodID( GameEventsManager, "onRealtimeEntered", "(Ljava/lang/Object;)B" );
    
    if ( onRealtimeEntered == 0 )
    {
        logg( "ERROR: Failed to find the onRealtimeEntered() method." );
        return ( false );
    }

    onRealtimeExited = env->GetMethodID( GameEventsManager, "onRealtimeExited", "(Ljava/lang/Object;)B" );
    
    if ( onRealtimeExited == 0 )
    {
        logg( "ERROR: Failed to find the onRealtimeExited() method." );
        return ( false );
    }
    
    onTelemetryDataUpdated = env->GetMethodID( GameEventsManager, "onTelemetryDataUpdated", "(Ljava/lang/Object;)B" );
    
    if ( onTelemetryDataUpdated == 0 )
    {
        logg( "ERROR: Failed to find the onTelemetryDataUpdated() method on GameEventsManager." );
        return ( false );
    }
    
    onScoringInfoUpdated = env->GetMethodID( GameEventsManager, "onScoringInfoUpdated", "(ILjava/lang/Object;)B" );
    
    if ( onScoringInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the onScoringInfoUpdated() method on GameEventsManager." );
        return ( false );
    }
    
    onCommentaryRequestInfoUpdated = env->GetMethodID( GameEventsManager, "onCommentaryRequestInfoUpdated", "(Ljava/lang/Object;)B" );
    
    if ( onCommentaryRequestInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the onCommentaryRequestInfoUpdated() method on GameEventsManager." );
        return ( false );
    }
    
    onGraphicsInfoUpdated = env->GetMethodID( GameEventsManager, "onGraphicsInfoUpdated", "(Ljava/lang/Object;)B" );
    
    if ( onGraphicsInfoUpdated == 0 )
    {
        logg( "ERROR: Failed to find the onGraphicsInfoUpdated() method on GameEventsManager." );
        return ( false );
    }
    
    jclass DataAddressKeeper = env->FindClass( "net/ctdp/rfdynhud/gamedata/rfactor2/_rf2_DataAddressKeeper" );
    
    if ( DataAddressKeeper == 0 )
    {
        logg( "ERROR: Failed to find the DataAddressKeeper class." );
        return ( false );
    }
    
    setBufferInfo = env->GetMethodID( DataAddressKeeper, "setBufferInfo", "(JI)V" );
    
    if ( setBufferInfo == 0 )
    {
        logg( "ERROR: Failed to find the setBufferInfo() method on DataAddressKeeper." );
        return ( false );
    }
    
    jclass DataAddressKeeper2 = env->FindClass( "net/ctdp/rfdynhud/gamedata/rfactor2/_rf2_DataAddressKeeper2" );
    
    if ( DataAddressKeeper2 == 0 )
    {
        logg( "ERROR: Failed to find the DataAddressKeeper2 class." );
        return ( false );
    }
    
    setBufferInfo1 = env->GetMethodID( DataAddressKeeper2, "setBufferInfo", "(JI)V" );
    
    if ( setBufferInfo1 == 0 )
    {
        logg( "ERROR: Failed to find the setBufferInfo() method on DataAddressKeeper2." );
        return ( false );
    }
    
    setBufferInfo2 = env->GetMethodID( DataAddressKeeper2, "setBufferInfo2", "(JI)V" );
    
    if ( setBufferInfo2 == 0 )
    {
        logg( "ERROR: Failed to find the setBufferInfo2() method on DataAddressKeeper2." );
        return ( false );
    }
    
    jmethodID DataAddressKeeper_constructor = env->GetMethodID( DataAddressKeeper, "<init>", "()V" );
    
    if ( DataAddressKeeper_constructor == 0 )
    {
        logg( "ERROR: Failed to find the DataAddressKeeper empty constructor." );
        return ( false );
    }
    
    jmethodID DataAddressKeeper2_constructor = env->GetMethodID( DataAddressKeeper2, "<init>", "()V" );
    
    if ( DataAddressKeeper2_constructor == 0 )
    {
        logg( "ERROR: Failed to find the DataAddressKeeper2 empty constructor." );
        return ( false );
    }
    
    telemetryDataAddressKeeper = globalizeObject( env, env->NewObject( DataAddressKeeper, DataAddressKeeper_constructor ) );
    scoringInfoAddressKeeper = globalizeObject( env, env->NewObject( DataAddressKeeper2, DataAddressKeeper2_constructor ) );
    commentaryRequestInfoAddressKeeper = globalizeObject( env, env->NewObject( DataAddressKeeper, DataAddressKeeper_constructor ) );
    graphicsInfoAddressKeeper = globalizeObject( env, env->NewObject( DataAddressKeeper, DataAddressKeeper_constructor ) );
    
    beforeRender = env->GetMethodID( GameEventsManager, "beforeRender", "(SSSS)B" );
    
    if ( beforeRender == 0 )
    {
        logg( "ERROR: Failed to find the beforeRender() method on GameEventsManager." );
        return ( false );
    }
    
    return ( true );
}

void JVMTelemtryUpdateFunctions::setEnv( JavaVM* jvm, JNIEnv* _env )
{
    env = _env;
}

void JVMTelemtryUpdateFunctions::destroy()
{
    gameEventsManager = NULL;
    if ( gameEventsManager != NULL )
        env->DeleteGlobalRef( gameEventsManager );
    GameEventsManager = NULL;
    
    if ( telemetryDataAddressKeeper != NULL )
        env->DeleteGlobalRef( telemetryDataAddressKeeper );
    if ( scoringInfoAddressKeeper != NULL )
        env->DeleteGlobalRef( scoringInfoAddressKeeper );
    if ( graphicsInfoAddressKeeper != NULL )
        env->DeleteGlobalRef( graphicsInfoAddressKeeper );
    if ( commentaryRequestInfoAddressKeeper != NULL )
        env->DeleteGlobalRef( commentaryRequestInfoAddressKeeper );
    
    onRealtimeExited = 0;
    onRealtimeEntered = 0;
    onSessionEnded = 0;
    onSessionStarted = 0;
    onShutdown = 0;
    onStartup = 0;
    
    env = NULL;
}

int readDataPath( JNIEnv* env, jbyteArray buffer )
{
    HKEY keyHandle;
    DWORD size;
    DWORD Type;
    
    if ( RegOpenKeyEx( HKEY_CURRENT_USER, "Software\\Image Space Incorporated\\rFactor2 Mod Manager\\Packages Dir", 0, KEY_QUERY_VALUE, &keyHandle ) == ERROR_SUCCESS )
    {
        size = MAX_PATH;
        jboolean isCopy;
        char* buffer2 = (char*)env->GetPrimitiveArrayCritical( buffer, &isCopy );
        if ( RegQueryValueEx( keyHandle, "File1", NULL, &Type, (LPBYTE)buffer2, &size ) == ERROR_SUCCESS )
        {
            size = removeLastPathComponent( buffer2, size - 2 );
        }
        else
        {
            size = -1;
        }
        env->ReleasePrimitiveArrayCritical( buffer, buffer2, 0 );
        RegCloseKey( keyHandle );
        
        return ( (int)size );
    }
    
    return ( -1 );
}

int setDefaultDataPath( const char* GAME_PATH, JNIEnv* env, jbyteArray buffer )
{
    char* buffer2 = (char*)env->GetPrimitiveArrayCritical( buffer, &isCopy );
    int length = strlen( GAME_PATH );
    memcpy( buffer2, GAME_PATH, length );
    memcpy( buffer2 + length, "\\UserData\0", 10 );
    env->ReleasePrimitiveArrayCritical( buffer, buffer2, 0 );
    
    return ( length + 9 );
}

bool JVMConnection::init( const char* GAME_PATH, const char* PLUGIN_PATH, const unsigned int resX, const unsigned int resY )
{
    if ( !createNewJavaVM( PLUGIN_PATH, &jvm, &env ) )
        return ( false );
    
    logg( "Retrieving Java objects and methods..." );
    
    jclass LiveGameDataObjectsFactory = env->FindClass( "net/ctdp/rfdynhud/gamedata/rfactor2/_rf2_LiveGameDataObjectsFactory" );
    
    if ( LiveGameDataObjectsFactory == 0 )
    {
        logg( "ERROR: Failed to find the LiveGameDataObjectsFactory class." );
        return ( false );
    }
    
    jmethodID LiveGameDataObjectsFactory_constructor = env->GetMethodID( LiveGameDataObjectsFactory, "<init>", "([BI)V" );
    
    if ( LiveGameDataObjectsFactory_constructor == 0 )
    {
        logg( "ERROR: Failed to find the LiveGameDataObjectsFactory empty constructor." );
        return ( false );
    }
    
    jbyteArray dataPathBuffer = env->NewByteArray( MAX_PATH );
    int dataPathLength = readDataPath( env, dataPathBuffer );
    if ( dataPathLength < 0 )
    {
        dataPathLength = setDefaultDataPath( GAME_PATH, env, dataPathBuffer );
    }

    jobject liveGameDataObjectsFactory = env->NewObject( LiveGameDataObjectsFactory, LiveGameDataObjectsFactory_constructor, dataPathBuffer, (jint)dataPathLength );
    
    env->DeleteLocalRef( dataPathBuffer );
    
    RFDynHUD = globalizeClass( env, env->FindClass( "net/ctdp/rfdynhud/RFDynHUD" ) );
    
    if ( RFDynHUD == 0 )
    {
        logg( "ERROR: Failed to find the main class." );
        return ( false );
    }
    
    jmethodID mid = env->GetStaticMethodID( RFDynHUD, "createInstance", "(Lnet/ctdp/rfdynhud/gamedata/_LiveGameDataObjectsFactory;II)Lnet/ctdp/rfdynhud/RFDynHUD;" );
    
    if ( mid == 0 )
    {
        logg( "ERROR: Failed to find the constructor method." );
        return ( false );
    }
    
    rfDynHUD = globalizeObject( env, env->CallStaticObjectMethod( RFDynHUD, mid, liveGameDataObjectsFactory, resX, resY ) );
    
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

bool JVMConnection::attachCurrentThread()
{
    JNIEnv* tmp_env = NULL;
    int envState = jvm->GetEnv( (void**)&tmp_env, JNI_VERSION_1_6 );
    
    if ( envState == JNI_EVERSION )
    {
        logg( "ERROR: Unsupported JVM version." );
        
        return ( false );
    }
    
    if ( envState == JNI_EDETACHED )
    {
        /*
        JavaVMAttachArgs attArgs;
        attArgs.version = JNI_VERSION_1_6;
        attArgs.name = NULL;
        attArgs.group = NULL;

        jvm->AttachCurrentThread( (void**)&env, (void*)&attArgs );
        */
        jvm->AttachCurrentThread( (void**)&env, NULL );
        
        d3dFuncs.setEnv( jvm, env );
        inputFuncs.setEnv( jvm, env );
        telemFuncs.setEnv( jvm, env );
        
        return ( true );
    }
    
    if ( envState != JNI_OK )
        loggi( "ERROR: Unexpected JNI attached state: ", envState );
    
    env = tmp_env;
    d3dFuncs.setEnv( jvm, env );
    inputFuncs.setEnv( jvm, env );
    telemFuncs.setEnv( jvm, env );
    
    return ( false );
}

void JVMConnection::detachCurrentThread()
{
    jvm->DetachCurrentThread();
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
        env->DeleteGlobalRef( rfDynHUD );
    rfDynHUD = NULL;
    RFDynHUD = NULL;
    
    jvm->DestroyJavaVM();
    
    env = NULL;
    jvm = NULL;
    
    logg( "Successfully destroyed Java VM." );
}
