#include "jvm_connection.hpp"
#include <Windows.h>
#include <winreg.h>
#include <jni.h>
#include "filesystem.h"
#include "timing.h"
#include "util.h"

//#define RFACTOR1
#define RFACTOR2

static const char* LOG_FOLDER = getLogFolder();
static const char* LOG_FILENAME = getLogFilename();

int findLastSeparator( const char* filename )
{
    for ( int i = strlen( filename ) - 1; i >= 0; i-- )
    {
        if ( filename[i] == '\\' )
            return ( i );
    }
    
    return ( -1 );
}

void renameOldLogFile()
{
    //_unlink( LOG_FILENAME );
    
    char* buffer = (char*)malloc( MAX_PATH );
    int folderLength = findLastSeparator( LOG_FILENAME );
    memcpy( buffer, LOG_FILENAME, folderLength + 1 );
    memcpy( buffer + folderLength + 1, "rfdynhud_editor-", 16 );
    unsigned int timeLength = getFileTimeString( LOG_FILENAME, buffer + folderLength + 1 + 16 );
    memcpy( buffer + folderLength + 1 + 16 + timeLength, ".log", 4 + 1 );
    
    MoveFile( LOG_FILENAME, buffer );
    
    free( buffer );
}

void handleArchivedLogFiles( const char* PLUGIN_PATH )
{
    char* filename = (char*)malloc( MAX_PATH );
    const unsigned int pluginPathLength = strlen( PLUGIN_PATH );
    memcpy( filename, PLUGIN_PATH, pluginPathLength );
    memcpy( filename + pluginPathLength, "\\rfdynhud.ini", 14 );
    
    char* buffer = (char*)malloc( 16 );
    readIniString( filename, "GENERAL", "numArchivedLogFiles", "5", buffer, 16 );
    unsigned int numArchivedLogFiles = (unsigned int)max( 0, atoi( buffer ) );
    free( buffer );
    buffer = NULL;
    
    const unsigned int folderLength = findLastSeparator( LOG_FILENAME );
    memcpy( filename, LOG_FILENAME, folderLength + 1 );
    memcpy( filename + folderLength + 1, "rfdynhud_editor-*.log", 22 );
    
    char** files = (char**)malloc( 64 * sizeof( char* ) );
    unsigned int numFiles = 0;
    WIN32_FIND_DATA data;
    HANDLE hFile = FindFirstFile( filename, &data );
    if ( hFile != INVALID_HANDLE_VALUE )
    {
        do
        {
            if ( ( ( data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY ) == 0 ) && ( data.cFileName[0] != '.' ) )
            {
                unsigned int len = strlen( data.cFileName );
                char* filename2 = (char*)malloc( len + 1 );
                memcpy( filename2, data.cFileName, len + 1 );
                files[numFiles++] = filename2;
            }
        }
        while ( FindNextFile( hFile, &data ) && ( numFiles < 64 ) );
    }
    
    unsigned int numFiles2 = numFiles;
    while ( numFiles2 > numArchivedLogFiles )
    {
        unsigned int smallestIndex = -1;
        for ( unsigned int i = 0; i < numFiles; i++ )
        {
            if ( files[i] != NULL )
            {
                smallestIndex = i;
                break;
            }
        }
        
        for ( unsigned int i = smallestIndex + 1; i < numFiles; i++ )
        {
            if ( ( files[i] != NULL ) && ( strcmp( files[i], files[smallestIndex] ) < 0 ) )
                smallestIndex = i;
        }
        
        memcpy( filename + folderLength + 1, files[smallestIndex], strlen( files[smallestIndex] ) + 1 );
        _unlink( filename );
        free( files[smallestIndex] );
        files[smallestIndex] = NULL;
        numFiles2--;
    }
    
    for ( unsigned int i = 0; i < numFiles; i++ )
    {
        if ( files[i] != NULL )
        {
            free( files[i] );
            files[i] = NULL;
        }
    }
    
    free( files );
    free( filename );
}

void deleteLogFile( const char* PLUGIN_FOLDER )
{
    CreateDirectoryA( LOG_FOLDER, NULL );
    
    WIN32_FIND_DATA data;
    HANDLE hFile = FindFirstFile( LOG_FILENAME, &data );
    CloseHandle( hFile );
    if ( hFile != INVALID_HANDLE_VALUE )
    {
        renameOldLogFile();
        handleArchivedLogFiles( PLUGIN_FOLDER );
    }
}

void logg( const char* message, const bool newLine )
{
    FILE* f;
    fopen_s( &f, LOG_FILENAME, "a" );
    if ( f )
    {
        if ( newLine )
            fprintf( f, "%s\n", message );
        else
            fprintf( f, "%s", message );
        fclose( f );
    }
}

void logg( const char* message )
{
    logg( message, true );
}

void logg2( const char* message1, const char* message2, const bool newLine )
{
    FILE* f;
    fopen_s( &f, LOG_FILENAME, "a" );
    if ( f )
    {
        if ( newLine )
            fprintf( f, "%s%s\n", message1, message2 );
        else
            fprintf( f, "%s%s", message1, message2 );
        fclose( f );
    }
}

void logg2( const char* message1, const char* message2 )
{
    logg2( message1, message2, true );
}

void loggi( const char* message, const int value, const bool newLine )
{
    FILE* f;
    fopen_s( &f, LOG_FILENAME, "a" );
    if ( f )
    {
        if ( newLine )
            fprintf( f, "%s%d\n", message, value );
        else
            fprintf( f, "%s%d", message, value );
        fclose( f );
    }
}

void loggi( const char* message, const int value )
{
    loggi( message, value, true );
}

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

typedef jint ( APIENTRY * CreateJavaVMPROC ) ( JavaVM** pvm, void** penv, void* args );

bool createNewJavaVM( const char* PLUGIN_PATH, JavaVM** jvm, JNIEnv** env )
{
    const unsigned int FILEBUFFER_LENGTH = 16384;
    char* fileBuffer = (char*)malloc( FILEBUFFER_LENGTH );
    
    if ( JAVA_HOME == NULL )
    {
        logg( "ERROR: Could not locate JAVA_HOME." );
        
        return ( false );
    }
    
    const char* jh = JAVA_HOME +  + sizeof( int );
    
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
    
    logg( "Successfully loaded Java dlls." );
    
    logg( "Invoking Java VM..." );
    
    JavaVMOption* options = new JavaVMOption[32];
    unsigned int i = 0;
    
    setBuffer( "-Djava.class.path=", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud.jar", fileBuffer );
    
    #ifdef RFACTOR1
    addPostFix( ";", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud_gamedata_rfactor1.jar", fileBuffer );
    #endif
    
    #ifdef RFACTOR2
    addPostFix( ";", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud_gamedata_rfactor2.jar", fileBuffer );
    #endif
    
    addPostFix( ";", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\editor\\rfdynhud_editor.jar", fileBuffer );
	options[i++].optionString = cropBuffer2( fileBuffer );
    
    options[i++].optionString = cropBuffer2( addPreFix( "-Dworkdir=", setBuffer( PLUGIN_PATH, fileBuffer ) ) );
    options[i++].optionString = "-Xms512m";
    options[i++].optionString = "-Xmx512m";
    options[i++].optionString = "-XX:MaxGCPauseMillis=5";
    options[i++].optionString = "-XX:+UseAdaptiveSizePolicy";
    options[i++].optionString = "-Xincgc";
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
    
    logg( "Successfully invoked Java VM." );
    
    return ( true );
}

JavaVM* jvm = NULL;
JNIEnv* env = NULL;

bool launchEditor( const char* PLUGIN_PATH )
{
    createNewJavaVM( PLUGIN_PATH, &jvm, &env );
    
    jobject liveGameDataObjectsFactory = NULL;
    
    #ifdef RFACTOR1
    jclass LiveGameDataObjectsFactory = env->FindClass( "net/ctdp/rfdynhud/gamedata/rfactor1/_rf1_LiveGameDataObjectsFactory" );
    
    if ( LiveGameDataObjectsFactory == 0 )
    {
        logg( "ERROR: Failed to find the LiveGameDataObjectsFactory class." );
        return ( false );
    }
    
    jmethodID LiveGameDataObjectsFactory_constructor = env->GetMethodID( LiveGameDataObjectsFactory, "<init>", "()V" );
    
    if ( LiveGameDataObjectsFactory_constructor == 0 )
    {
        logg( "ERROR: Failed to find the LiveGameDataObjectsFactory empty constructor." );
        return ( false );
    }
    
    liveGameDataObjectsFactory = env->NewObject( LiveGameDataObjectsFactory, LiveGameDataObjectsFactory_constructor );
    #endif
    
    #ifdef RFACTOR2
    jclass LiveGameDataObjectsFactory = env->FindClass( "net/ctdp/rfdynhud/gamedata/rfactor2/_rf2_LiveGameDataObjectsFactory" );
    
    if ( LiveGameDataObjectsFactory == 0 )
    {
        logg( "ERROR: Failed to find the LiveGameDataObjectsFactory class." );
        return ( false );
    }
    
    jmethodID LiveGameDataObjectsFactory_constructor = env->GetMethodID( LiveGameDataObjectsFactory, "<init>", "()V" );
    
    if ( LiveGameDataObjectsFactory_constructor == 0 )
    {
        logg( "ERROR: Failed to find the LiveGameDataObjectsFactory empty constructor." );
        return ( false );
    }
    
    liveGameDataObjectsFactory = env->NewObject( LiveGameDataObjectsFactory, LiveGameDataObjectsFactory_constructor );
    #endif
    
    jclass RFDynHUDEditor = env->FindClass( "net/ctdp/rfdynhud/editor/RFDynHUDEditor" );
    
    if ( RFDynHUDEditor == NULL )
    {
        logg( "ERROR: Failed to find the RFDynHUDEditor class." );
        return ( false );
    }
    
    jmethodID RFDynHUDEditor_constructor = env->GetMethodID( RFDynHUDEditor, "<init>", "()V" );
    
    if ( RFDynHUDEditor_constructor == 0 )
    {
        logg( "ERROR: Failed to find the RFDynHUDEditor empty constructor." );
        return ( false );
    }
    
    jmethodID startMethod = env->GetMethodID( RFDynHUDEditor, "start", "(Lnet/ctdp/rfdynhud/gamedata/_LiveGameDataObjectsFactory;)V" );
    
    if ( startMethod == 0 )
    {
        logg( "ERROR: Failed to find the start() method." );
        return ( false );
    }
    
    logg( "Launching RFDynHUD Editor..." );
    jobject rfDynHUDEditor = env->NewObject( RFDynHUDEditor, RFDynHUDEditor_constructor );
    env->CallVoidMethod( rfDynHUDEditor, startMethod, liveGameDataObjectsFactory );
    logg( "RFDynHUD Editor quit." );
    
    return ( true );
}

void destroyJVM()
{
    logg( "Destroying Java VM..." );
    
    if ( jvm == NULL )
    {
        logg( "Nothing to destroy." );
        return;
    }
    
    jvm->DestroyJavaVM();
    
    env = NULL;
    jvm = NULL;
    
    logg( "Successfully destroyed Java VM." );
}
