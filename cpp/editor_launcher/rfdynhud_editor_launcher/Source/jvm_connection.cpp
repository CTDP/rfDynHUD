#include "jvm_connection.hpp"
#include <Windows.h>
#include <winreg.h>
#include <jni.h>
#include "filesystem.h"

static const char* LOG_FOLDER = getLogFolder();
static const char* LOG_FILENAME = getLogFilename();

void deleteLogFile()
{
    CreateDirectoryA( LOG_FOLDER, NULL );
    
    _unlink( LOG_FILENAME );
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
    
    getFullPath( JAVA_HOME, "bin\\msvcr71.dll", fileBuffer );
    logg( "    Loading msvcr71.dll..." );
    HMODULE msvcdll = LoadLibrary( fileBuffer );
    
    if ( msvcdll == NULL )
    {
        logg( "ERROR: Failed to load msvcr71.dll." );
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
    
    addPostFix( ";", fileBuffer );
    addPostFix( PLUGIN_PATH, fileBuffer );
    addPostFix( "\\editor\\rfdynhud_editor.jar", fileBuffer );
    
    options[0].optionString = cropBuffer2( fileBuffer );
    options[1].optionString = cropBuffer2( addPreFix( "-Dworkdir=", setBuffer( PLUGIN_PATH, fileBuffer ) ) );
    options[2].optionString = "-Xms256m";
    //options[3].optionString = "-Xmx512m";
    options[3].optionString = "-Xmx256m";
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
