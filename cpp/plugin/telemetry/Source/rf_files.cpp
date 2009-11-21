#include "filesystem.h"
#include "util.h"
#include <Windows.h>

bool checkProfileFolder( const char* plr, WIN32_FIND_DATA* findFileData, DWORD minAgeHigh, DWORD minAgeLow )
{
    HANDLE hFind = FindFirstFile( plr, findFileData );
    
    if ( hFind == INVALID_HANDLE_VALUE )
        return ( false );
    
    if ( findFileData->ftLastWriteTime.dwHighDateTime < minAgeHigh )
        return ( false );
    
    if ( findFileData->ftLastWriteTime.dwLowDateTime < minAgeLow )
        return ( false );
    
    return ( true );
}

char* getActivePlayerProfileFolder( const char* rfPath )
{
    char* buffer = (char*)malloc( MAX_PATH );
    char* buffer0 = (char*)malloc( MAX_PATH );
    getFullPath( rfPath, "UserData\\*", buffer0 );
    
    char* buffer2 = (char*)malloc( MAX_PATH );
    char* buffer3 = (char*)malloc( MAX_PATH );
    
    getFullPath( rfPath, "UserData", buffer2 );
    
    WIN32_FIND_DATA findFileData;
    WIN32_FIND_DATA findFileData2;
    //HANDLE hFind = FindFirstFileEx( buffer0, FindExInfoStandard, &findFileData, FindExSearchLimitToDirectories, NULL, 0 );
    HANDLE hFind = FindFirstFileEx( buffer0, FindExInfoStandard, &findFileData, FindExSearchNameMatch, NULL, 0 );
    DWORD minAgeHigh = 0;
    DWORD minAgeLow = 0;
    
    bool found = false;
    while ( hFind != INVALID_HANDLE_VALUE )
    {
        if ( ( findFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY ) && !stringsEqual( findFileData.cFileName, "." ) && !stringsEqual( findFileData.cFileName, ".." ) )
        {
            unsigned int l = getFullPath( buffer2, findFileData.cFileName, buffer3 );
            appendPath( findFileData.cFileName, buffer3, true );
            appendPath( ".PLR", buffer3, false );
            
            if ( checkProfileFolder( buffer3, &findFileData2, minAgeHigh, minAgeLow ) )
            {
                memcpy( buffer, buffer3, l );
                buffer[l] = '\0';
                minAgeHigh = findFileData2.ftLastWriteTime.dwHighDateTime;
                minAgeLow = findFileData2.ftLastWriteTime.dwLowDateTime;
                found = true;
            }
        }
        
        if ( !FindNextFile( hFind, &findFileData ) )
        {
            hFind = INVALID_HANDLE_VALUE;
        }
    }
    
    char* result = NULL;
    if ( found )
        result = cropBuffer( buffer, strlen( buffer ) + 1 );
    
    free( buffer3 );
    free( buffer2 );
    free( buffer0 );
    free( buffer );
    
    return ( result );
}

char* getActivePlayerProfileName( const char* rfPath )
{
    char* buffer = (char*)malloc( MAX_PATH );
    char* buffer0 = (char*)malloc( MAX_PATH );
    getFullPath( rfPath, "UserData\\*", buffer0 );
    
    char* buffer2 = (char*)malloc( MAX_PATH );
    char* buffer3 = (char*)malloc( MAX_PATH );
    
    getFullPath( rfPath, "UserData", buffer2 );
    
    WIN32_FIND_DATA findFileData;
    WIN32_FIND_DATA findFileData2;
    //HANDLE hFind = FindFirstFileEx( buffer0, FindExInfoStandard, &findFileData, FindExSearchLimitToDirectories, NULL, 0 );
    HANDLE hFind = FindFirstFileEx( buffer0, FindExInfoStandard, &findFileData, FindExSearchNameMatch, NULL, 0 );
    DWORD minAgeHigh = 0;
    DWORD minAgeLow = 0;
    
    bool found = false;
    while ( hFind != INVALID_HANDLE_VALUE )
    {
        if ( ( findFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY ) && !stringsEqual( findFileData.cFileName, "." ) && !stringsEqual( findFileData.cFileName, ".." ) )
        {
            unsigned int l = getFullPath( buffer2, findFileData.cFileName, buffer3 );
            appendPath( findFileData.cFileName, buffer3, true );
            appendPath( ".PLR", buffer3, false );
            
            if ( checkProfileFolder( buffer3, &findFileData2, minAgeHigh, minAgeLow ) )
            {
                memcpy( buffer, findFileData.cFileName, strlen( findFileData.cFileName ) + 1 );
                minAgeHigh = findFileData2.ftLastWriteTime.dwHighDateTime;
                minAgeLow = findFileData2.ftLastWriteTime.dwLowDateTime;
                found = true;
            }
        }
        
        if ( !FindNextFile( hFind, &findFileData ) )
        {
            hFind = INVALID_HANDLE_VALUE;
        }
    }
    
    char* result = NULL;
    if ( found )
        result = cropBuffer( buffer, strlen( buffer ) + 1 );
    
    free( buffer3 );
    free( buffer2 );
    free( buffer0 );
    free( buffer );
    
    return ( result );
}

bool checkMod( const char* mod )
{
    // We support any my by the CTDP (Cars and Tracks Development Project) :).
    
    if ( stringsEqual( mod, "F1CTDP06" ) )
        return ( true );
    
    if ( stringsEqual( mod, "F1CTDP05" ) )
        return ( true );
    
    if ( stringsEqual( mod, "F1CTDP05SE" ) )
        return ( true );
    
    // The commercial mod by CTDP.
    if ( stringsEqual( mod, "SuperLeagueFormula" ) )
        return ( true );
    
    // A wonderful little F1 91 mod, that we want to support.
    if ( stringsEqual( mod, "F1_1991_LE" ) )
        return ( true );
    
	// The Endurance Racing mod is a mod that can only be obtained through
    // http://www.endurance-racing.org/ when that site goes live.
    // For now it is only available for beta testers.
	if ( stringsEqual( mod, "EnduranceRacing" ) )
        return ( true );
    
    return ( false );
}

bool isModSupported( const char* profilePath, const char* profileFilename )
{
    return ( true );
    /*
    char* plr = (char*)malloc( MAX_PATH );
    getFullPath( profilePath, profileFilename, plr );
    appendPath( ".PLR", plr, false );
    
    char* buffer = (char*)malloc( 128 );
    
    GetPrivateProfileString( "DRIVER", "Game Description", "NOT_FOUND", buffer, 128, plr );
    
    free( plr );
    
    if ( stringsEqual( buffer, "NOT_FOUND" ) )
    {
        free( buffer );
        return ( false );
    }
    
    char* modName = buffer + 1;
    unsigned int l = 0;
    while ( *modName++ != '"' )
        l++;
    l -= 4; // truncate ".rfm"
    modName = buffer + 1;
    modName[l] = '\0';
    
    bool result = checkMod( modName );
    
    free( buffer );
    
    return ( result );
    */
}
