#ifndef _JVM_CONNECTION_H
#define _JVM_CONNECTION_H

#include <jni.h>
#include <Windows.h>

class JVMD3DUpdateFunctions
{
private:
    JNIEnv* env;
    jobject rfdynhudObject;
    
    jmethodID updateMethod;
    
    jobject textureInfoBufferObj;
	char* textureInfoBuffer;
    jmethodID getDirtyRectsBufferMethod;
	jobject* dirtyRectsBufferObj;
    jmethodID getPixelDataMethod;
    jbyteArray pixelData;
    
    unsigned char numTextures;
    
    static const unsigned int SOFT_MAX_NUM_WIDGETS = 48;
    static const unsigned int MAX_TOTAL_NUM_RECTANGLES = 255;
    static const unsigned int MAX_NUM_TEXTURES = MAX_TOTAL_NUM_RECTANGLES - SOFT_MAX_NUM_WIDGETS + 1;
    
    static const unsigned int OFFSET_VISIBLE = 1;
    static const unsigned int OFFSET_SIZE = OFFSET_VISIBLE + MAX_NUM_TEXTURES * 1;
    static const unsigned int OFFSET_TRANSFORMED = OFFSET_SIZE + MAX_NUM_TEXTURES * 4;
    static const unsigned int OFFSET_TRANSLATION = OFFSET_TRANSFORMED + MAX_NUM_TEXTURES * 1;
    static const unsigned int OFFSET_ROT_CENTER = OFFSET_TRANSLATION + MAX_NUM_TEXTURES * 8;
    static const unsigned int OFFSET_ROTATION = OFFSET_ROT_CENTER + MAX_NUM_TEXTURES * 4;
    static const unsigned int OFFSET_SCALE = OFFSET_ROTATION + MAX_NUM_TEXTURES * 4;
    static const unsigned int OFFSET_CLIP_RECT = OFFSET_SCALE + MAX_NUM_TEXTURES * 8;
    static const unsigned int OFFSET_NUM_RECTANLES = OFFSET_CLIP_RECT + MAX_NUM_TEXTURES * 8;
    static const unsigned int OFFSET_RECT_VISIBLE_FLAGS = OFFSET_NUM_RECTANLES + MAX_NUM_TEXTURES * 1;
    static const unsigned int OFFSET_RECTANLES = OFFSET_RECT_VISIBLE_FLAGS + MAX_NUM_TEXTURES * SOFT_MAX_NUM_WIDGETS * 1;

    void releaseDirtyRectsBufferObjects();
    
public:
    unsigned short** dirtyRectsBuffers;
    unsigned short* textureSizes;
    char* textureVisibleFlags;
    char* textureIsTransformedFlags;
    float* textureTranslations;
    unsigned short* textureRotationCenters;
    float* textureRotations;
    float* textureScales;
    unsigned short* textureClipRects;
    unsigned char* numUsedRectangles;
    char* rectangleVisibleFlags;
    unsigned short* usedRectangles;
    
    JVMD3DUpdateFunctions()
    {
        numTextures = 0;
        
        dirtyRectsBuffers = (unsigned short**)malloc( MAX_NUM_TEXTURES * sizeof( unsigned short* ) );
        /*
        textureSizes = (unsigned short*)malloc( MAX_NUM_TEXTURES * sizeof( unsigned short ) );
        textureVisibleFlags = (char*)malloc( MAX_NUM_TEXTURES * sizeof( char ) );
        textureIsTransformedFlags = (char*)malloc( MAX_NUM_TEXTURES * sizeof( char ) );
        textureTranslations = (float*)malloc( MAX_NUM_TEXTURES * 2 * sizeof( float ) );
        textureRotationCenters = (unsigned short*)malloc( MAX_NUM_TEXTURES * 2 * sizeof( unsigned short ) );
        textureRotations = (float*)malloc( MAX_NUM_TEXTURES * sizeof( float ) );
        textureScales = (float*)malloc( MAX_NUM_TEXTURES * 2 * sizeof( float ) );
        textureClipRects = (unsigned short*)malloc( MAX_NUM_TEXTURES * 4 * sizeof( unsigned short ) );
        numUsedRectangles = (unsigned char*)malloc( MAX_NUM_TEXTURES * sizeof( unsigned char ) );
        rectangleVisibleFlags = (char*)malloc( MAX_NUM_TEXTURES * sizeof( char ) );
        usedRectangles = (unsigned short*)malloc( MAX_NUM_TEXTURES * sizeof( unsigned short ) * 4 );
        */
        textureSizes = NULL;
        textureVisibleFlags = NULL;
        textureIsTransformedFlags = NULL;
        textureTranslations = NULL;
        textureRotationCenters = NULL;
        textureRotations = NULL;
        textureScales = NULL;
        textureClipRects = NULL;
        numUsedRectangles = NULL;
        rectangleVisibleFlags = NULL;
        usedRectangles = NULL;
    }
    
    ~JVMD3DUpdateFunctions()
    {
        free( dirtyRectsBuffers ); dirtyRectsBuffers = NULL;
        /*
        free( textureSizes ); textureSizes = NULL;
        free( textureVisibleFlags ); textureVisibleFlags = NULL;
        free( textureIsTransformedFlags ); textureIsTransformedFlags = NULL;
        free( textureTranslations ); textureTranslations = NULL;
        free( textureRotationCenters ); textureRotationCenters = NULL;
        free( textureRotations ); textureRotations = NULL;
        free( textureScales ); textureScales = NULL;
        free( textureClipRects ); textureClipRects = NULL;
        free( numUsedRectangles ); numUsedRectangles = NULL;
        free( rectangleVisibleFlags ); rectangleVisibleFlags = NULL;
        free( usedRectangles ); usedRectangles = NULL;
        */
        textureSizes = NULL;
        textureVisibleFlags = NULL;
        textureIsTransformedFlags = NULL;
        textureTranslations = NULL;
        textureRotationCenters = NULL;
        textureRotations = NULL;
        textureScales = NULL;
        textureClipRects = NULL;
        numUsedRectangles = NULL;
        rectangleVisibleFlags = NULL;
        usedRectangles = NULL;
    }
    
    char call_update()
    {
        return ( env->CallByteMethod( rfdynhudObject, updateMethod ) );
    }
    
    unsigned char getNumTextures()
    {
        return ( numTextures );
    }
    
    unsigned char updateAllTextureInfos();
    
    unsigned char updateDynamicTextureInfos();
    
    unsigned char* getPixelData( unsigned char textureIndex );
    
    void releasePixelData( unsigned char textureIndex, unsigned char* pointer );
    
    bool init( JNIEnv* env, jclass rfdynhudClass, jobject rfdynhudObject );
    void setEnv( JavaVM* jvm, JNIEnv* env );
    
    void destroy();
};

class JVMInputFunctions
{
private:
    JNIEnv* env;
    jobject rfdynhudObject;
    jobject inputBufferObj;
    char* inputBuffer;
    jmethodID updateInputMethod;
    
public:
    
    bool init( JNIEnv* env, jclass rfdynhudClass, jobject rfdynhudObject );
    void setEnv( JavaVM* jvm, JNIEnv* env );
    
    char updateInput( bool* isPluginEnabled );
    
    void destroy();
};

class JVMTelemtryUpdateFunctions
{
private:
    JNIEnv* env;
    
    jclass GameEventsManager;
    jobject gameEventsManager;
    
    jmethodID onStartup;
    jmethodID onShutdown;
    jmethodID onSessionStarted;
    jmethodID onSessionEnded;
    jmethodID onRealtimeEntered;
    jmethodID onRealtimeExited;
    jmethodID onTelemetryDataUpdated;
    jobject telemetryDataAddressKeeper;
    jobject scoringInfoAddressKeeper;
    jobject commentaryRequestInfoAddressKeeper;
    jobject graphicsInfoAddressKeeper;
    jmethodID setBufferInfo;
    jmethodID setBufferInfo1;
    jmethodID setBufferInfo2;
    jmethodID onScoringInfoUpdated;
    jmethodID onCommentaryRequestInfoUpdated;
    jmethodID onGraphicsInfoUpdated;
    jmethodID beforeRender;
    
public:
    void call_onStartup();
    
    void call_onShutdown();
    
    char call_onSessionStarted();
    
    void call_onSessionEnded();
    
    char call_onRealtimeEntered();
    
    char call_onRealtimeExited();
    
    char call_onTelemetryDataUpdated( void* buffer, const unsigned int size );
    
    char call_onScoringInfoUpdated( const long numVehicles, void* buffer, const unsigned int size, void* buffer2, const unsigned int size2 );
    
    char call_onCommentaryRequestInfoUpdated( void* buffer, const unsigned int size );

    char call_onGraphicsInfoUpdated( void* buffer, const unsigned int size );

    char call_beforeRender( const unsigned short viewportX, const unsigned short viewportY, const unsigned short viewportWidth, const unsigned short viewportHeight );
    
    
    jclass rfdynhudClass;
    jobject rfdynhudObject;
    bool init( JNIEnv* env, jclass rfdynhudClass, jobject rfdynhudObject );
    void setEnv( JavaVM* jvm, JNIEnv* env );
    
    void destroy();
};

class JVMConnection
{
private:
    JavaVM* jvm;
    JNIEnv* env;
    
    jclass RFDynHUD;
    jobject rfDynHUD;
    
public:
    JVMD3DUpdateFunctions d3dFuncs;
    JVMInputFunctions inputFuncs;
    JVMTelemtryUpdateFunctions telemFuncs;
    
    bool init( const char* PLUGIN_PATH, const unsigned int resX, const unsigned int resY );
    
    bool attachCurrentThread();
    void detachCurrentThread();
    
    void destroy();
};

class Global
{
public:
    bool anyCriticalErrors;
    bool isInRenderMode;
    bool isSessionRunning;
    bool isInRealtime;
    JVMConnection jvmConn;
    
    bool isPluginEnabled;
    
    Global()
    {
        anyCriticalErrors = false;
        isInRenderMode = false;
        isSessionRunning = false;
        isInRealtime = false;
    }
};

Global* getGlobal();

#endif // _JVM_CONNECTION_H
