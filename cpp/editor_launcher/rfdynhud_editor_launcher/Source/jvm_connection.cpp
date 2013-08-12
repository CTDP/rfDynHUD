#include "jvm_connection.hpp"
#include <Windows.h>
#include <winreg.h>
#include <jni.h>
#include "filesystem.h"
#include "timing.h"
#include "util.h"

static const char* LOG_FOLDER = getLogFolder();
static const char* LOG_FILENAME = getLogFilename();
int JAVA_VERSION = 0;

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

void logg( const char* message )
{
    FILE* f;
    fopen_s( &f, LOG_FILENAME, "a" );
    fprintf( f, "%s\n", message );
    fclose( f );
}

void logg2( const char* message )
{
    FILE* f;
    fopen_s( &f, LOG_FILENAME, "a" );
    fprintf( f, "%s", message );
    fclose( f );
}

char* readJavaHomeFromRegistry()
{//int& version
    char* buffer = (char*)malloc( MAX_PATH );
    HKEY keyHandle;
    DWORD size1;
    DWORD Type;
    
    if ( RegOpenKeyEx( HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment\\1.7", 0, KEY_QUERY_VALUE, &keyHandle ) == ERROR_SUCCESS )
    {
        size1 = MAX_PATH - 1;
        RegQueryValueEx( keyHandle, "JavaHome", NULL, &Type, (LPBYTE)buffer, &size1);
        RegCloseKey( keyHandle );
        JAVA_VERSION = 7;
        return ( buffer );
    }
    if ( RegOpenKeyEx( HKEY_LOCAL_MACHINE, "SOFTWARE\\JavaSoft\\Java Runtime Environment\\1.6", 0, KEY_QUERY_VALUE, &keyHandle ) == ERROR_SUCCESS )
    {
        size1 = MAX_PATH - 1;
        RegQueryValueEx( keyHandle, "JavaHome", NULL, &Type, (LPBYTE)buffer, &size1);
        RegCloseKey( keyHandle );
        JAVA_VERSION = 6;
        return ( buffer );
    }
    free( buffer );
    
    logg( "WARNING: Registry key for 32 bit Java 6 Runtime Environment not found." );
    
    return ( NULL );
}

char* guessJavaHome()
{
    char* buffer = (char*)malloc( MAX_PATH );
    char* buffer7 = (char*)malloc( MAX_PATH );
    
    DWORD len = GetEnvironmentVariable( "ProgramFiles", buffer, MAX_PATH );
    DWORD len7 = GetEnvironmentVariable( "ProgramFiles", buffer, MAX_PATH );
    
    char* buff = buffer + len;
    memcpy( buff, "\\Java\\jre6", 11 );
    len += 11;
    
    char* result = (char*)malloc( len );
    memcpy( result, buffer, len );
    free( buffer );

	char* buff7 = buffer7 + len7;
    memcpy( buff7, "\\Java\\jre7", 11 );
    len7 += 11;
    
    char* result7 = (char*)malloc( len7 );
    memcpy( result7, buffer7, len7 );
    free( buffer7 );
    
	if ( checkDirectoryExists( result, false ) != 1 && checkDirectoryExists( result7, false ) != 1)
    {
        free( result );
        free( result7 );
        
        logg( "WARNING: Couldn't find 32 bit Java 6 or Java 7 Runtime Environment in the default folder." );
        
        return ( NULL );
    }
    if ( checkDirectoryExists( result7, false ) != 1)
	{
		JAVA_VERSION = 7;
		return ( result7 );
	}
	JAVA_VERSION = 6;
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

typedef jint ( APIENTRY * CreateJavaVMPROC ) ( JavaVM** pvm, void** penv, void* args );

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
    
	if(JAVA_VERSION == 7)
	{
		getFullPath( JAVA_HOME, "bin\\msvcr100.dll", fileBuffer );
		logg( "    Loading msvcr100.dll...", false );
	}
	else
	{
		getFullPath( JAVA_HOME, "bin\\msvcr71.dll", fileBuffer );
		logg( "    Loading msvcr71.dll...", false );
	}

    HMODULE msvcdll = LoadLibrary( fileBuffer );
    
    if ( msvcdll == NULL )
    {
        if(JAVA_VERSION == 7)
			logg( " ERROR: Failed to load msvcr100.dll." );
		else
			logg( " ERROR: Failed to load msvcr71.dll." );
        return ( false );
    }
    
    getFullPath( JAVA_HOME, "bin\\client\\jvm.dll", fileBuffer );
    logg( "    Loading jvm.dll..." );
    HMODULE jvmdll = LoadLibrary( fileBuffer );
    
    if ( jvmdll == NULL )
    {
        logg( "ERROR: Failed to load jvm.dll." );
        return ( false );
    }
    
    logg( "Successfully loaded Java dlls." );
    
    logg( "Invoking Java VM..." );
    
    const unsigned int NUM_OPTIONS = 7;
    JavaVMOption options[NUM_OPTIONS];
    
    setBuffer( "-Djava.class.path=", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud.jar", fileBuffer );
    
    // TODO: Detect rFactor version somehow!
    addPostFix( ";", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\rfdynhud_gamedata_rfactor1.jar", fileBuffer );
    
    addPostFix( ";", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\editor\\rfdynhud_editor.jar", fileBuffer );
    
    options[0].optionString = cropBuffer2( fileBuffer );
    options[1].optionString = cropBuffer2( addPreFix( "-Dworkdir=", setBuffer( PLUGIN_PATH, fileBuffer ) ) );
    options[2].optionString = "-Xms512m";
    options[3].optionString = "-Xmx512m";
    options[4].optionString = "-XX:MaxGCPauseMillis=5";
    options[5].optionString = "-XX:+UseAdaptiveSizePolicy";
    options[6].optionString = "-Xincgc";
    
    free( fileBuffer );
    
    logg( "JVM options:" );
    for ( unsigned int i = 0; i < NUM_OPTIONS; i++ )
    {
        logg2( "    " );
        logg( options[i].optionString );
    }
    
    JavaVMInitArgs vm_args;
    
    vm_args.version = JNI_VERSION_1_6;
    vm_args.options = options;
    vm_args.nOptions = 7;
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
    
    logg( "Successfully invoked Java VM." );
    
    return ( true );
}

JavaVM* jvm = NULL;
JNIEnv* env = NULL;

bool launchEditor( const char* PLUGIN_PATH )
{
    createNewJavaVM( PLUGIN_PATH, &jvm, &env );
    
    jclass RFDynHUDEditor = env->FindClass( "net/ctdp/rfdynhud/editor/RFDynHUDEditor" );
    
    if ( RFDynHUDEditor == NULL )
    {
        logg( "ERROR: Failed to find the RFDynHUDEditor class." );
        return ( false );
    }
    
    jmethodID mainMethod = env->GetStaticMethodID( RFDynHUDEditor, "main", "([Ljava/lang/String;)V" );
    
    if ( mainMethod == 0 )
    {
        logg( "ERROR: Failed to find the main() method." );
        return ( false );
    }
    
    jobjectArray jargs = env->NewObjectArray( 0, env->FindClass( "java/lang/String" ), NULL );
    
    logg( "Launching RFDynHUD Editor..." );
    env->CallStaticVoidMethod( RFDynHUDEditor, mainMethod, jargs );
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
