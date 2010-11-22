// rfdynhud_editor_launcher.cpp : Defines the entry point for the console application.

#include "common.h"
#include "filesystem.h"
#include "jvm_connection.hpp"

//int _tmain( int argc, _TCHAR* argv[] )
int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow )
{
    initPluginIniFilename();
    
    deleteLogFile( getPluginPath() );
    
    if ( !launchEditor( getPluginPath() ) )
        return ( 1 );
    
    destroyJVM();
    
	return ( 0 );
}
