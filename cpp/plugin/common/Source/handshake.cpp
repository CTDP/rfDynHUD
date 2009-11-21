#include "handshake.hpp"

#include <Windows.h>

#include "filesystem.h"
#include "logging.h"

const char* MEM_KEY = "rfDynHUD";

bool checkMemKey( const char* mem )
{
    for ( unsigned int i = 0; i < 8; i++ )
    {
        if ( mem[i] != MEM_KEY[i] )
            return ( false );
    }
    
    return ( true );
}

char doHandshake( bool isD3DCalling, Handshake** handshake )
{
    const unsigned char partState = isD3DCalling ? 1 : 2;
    if ( isD3DCalling )
        logg( "Doing Handshake from D3D side..." );
    else
        logg( "Doing Handshake from Plugin side..." );
    
    HANDLE hmmf = CreateFileMapping( (HANDLE)0xFFFFFFFF, NULL, PAGE_READWRITE, 0, 1024, MEM_KEY );
    
    if ( hmmf == NULL )
    {
        logg( "ERROR: Handshake failed. Unable to create PAGEFILE Memory Mapped File." );
        return ( -1 );
    }
    
    unsigned char* mem0 = (unsigned char*)MapViewOfFile( hmmf, FILE_MAP_WRITE, 0, 0, 0 );
    unsigned char* mem = mem0;
    
    if ( mem == NULL )
    {
        logg( "ERROR: Handshake failed. Unable to get a pointer to memory of PAGEFILE Memory Mapped File." );
        return ( -1 );
    }
    
    logg( "    Successfully created a pointer to a PAGEFILE Memory Mapped File." );
    
    DWORD procId = GetCurrentProcessId();
    
    unsigned char initializeMem = 0;
    if ( !checkMemKey( (char*)mem ) )
        initializeMem = 1;
    else if ( *(DWORD*)( mem + 8 ) != procId )
        initializeMem = 2;
    
    if ( initializeMem != 0 )
    {
        if ( initializeMem == 1 )
            logg( "    Found virgin memory." );
        else if ( initializeMem == 2 )
            logg( "    Found outdated memory." );
        
        memcpy( mem, MEM_KEY, 8 );
        mem += 8;
        *(DWORD*)mem = procId;
        mem += sizeof( DWORD );
        *mem = partState;
        mem += 1;
        
        *handshake = new Handshake();
        (*handshake)->isPluginEnabled = true;
        (*handshake)->state = HANDSHAKE_STATE_WAITING_FOR_OTHER_SIDE;
        *(unsigned long long*)mem = (unsigned long long)*handshake;
        
        UnmapViewOfFile( mem0 );
        
        logg( "First part of Handshake successful." );
        
        return ( 0 ); // Handshake incomplete
    }
    
    mem += 8 + sizeof( DWORD );
    
    unsigned char state = *mem;
    
    if ( ( state & partState ) != 0 )
    {
        UnmapViewOfFile( mem0 );
        
        logg( "Handshake repeat detected for unknown reason. This tends to happen in windowed mode, where two D3D contexts seem to be created. Skipping!" );
        
        return ( 2 );
    }
    
    *mem = state | partState;
    
    mem += 1;
    
    logg( "    Found first part of handshake. Doing second part..." );
    
    *handshake = (Handshake*)(*(unsigned long long*)mem);
    (*handshake)->state = HANDSHAKE_STATE_COMPLETE;
    
    UnmapViewOfFile( mem0 );
    
    logg( "Second part of Handshake successful. Handshake complete." );
    
    return ( 1 );
}

bool Handshake::doSanityCheck( const char* RFACTOR_PATH, const char* PLUGIN_PATH, char* fileBuffer )
{
    logg( "Doing a sanity check..." );
    
    bool warningsDetected = false;
    
    if ( checkDirectoryExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\log", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: log directory not found. Using rFactor root folder." );
        warningsDetected = true;
        //return ( false );
    }
    
    if ( checkDirectoryExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD", fileBuffer ), false ) == 0 )
    {
        logg( "    ERROR: rfDynHud directory not found in the rFactor Plugins folder. The plugin won't work." );
        return ( false );
    }
    
    if ( checkFileExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\rfdynhud.jar", fileBuffer ), false ) == 0 )
    {
        logg( "    ERROR: rfdynhud.jar not found in the rFactor Plugins\\rfDynHUD folder. The plugin won't work." );
        return ( false );
    }
    
    if ( checkDirectoryExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\config", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: config directory not found in the rFactor Plugins\\rfDynHUD folder. Fallback config will be used.\r\nUse the editor to create a valid config and store it as overlay.ini in the config folder. Please see the readme.txt in the config folder for more info." );
        warningsDetected = true;
        //return ( false );
    }
    
    if ( checkFileExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\config\\overlay.ini", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: overlay.ini configuration file not found in the rFactor Plugins\\rfDynHUD\\config folder. If no mod specific configurations are stored (which is not checked here), fallback config will be used.\r\nUse the editor to create a valid config and store it as overlay.ini in the config folder. Please see the readme.txt in the config folder for more info." );
        warningsDetected = true;
        //return ( false );
    }
    
    if ( checkFileExists( getFullPath2( RFACTOR_PATH, "Plugins\\rfDynHUD\\config\\three_letter_codes.ini", fileBuffer ), false ) == 0 )
    {
        logg( "    WARNING: three_letter_codes.ini file not found in the rFactor Plugins\\rfDynHUD\\config folder. Driver names will be displayed in full length regardless of the configuration.\r\nPlease see the readme.txt in the config folder for more info." );
        warningsDetected = true;
        //return ( false );
    }
    
    if ( warningsDetected )
        logg( "Sanity check passed, but warnings detected. The plugin may not work as expected." );
    else
        logg( "Sanity check passed." );
    
    return ( true );
}
