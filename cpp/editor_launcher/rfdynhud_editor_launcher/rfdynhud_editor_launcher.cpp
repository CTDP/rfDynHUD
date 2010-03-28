// rfdynhud_editor_launcher.cpp : Defines the entry point for the console application.
//

#include "filesystem.h"
#include "jvm_connection.hpp"
//#include "tchar.h"

static const char* RFACTOR_PATH = getRFactorPath();
static const char* PLUGIN_PATH = getPluginPath();

//int _tmain( int argc, _TCHAR* argv[] )
int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow )
{
    deleteLogFile();
    
    if ( !launchEditor( PLUGIN_PATH ) )
        return ( 1 );
    
    destroyJVM();
    
	return ( 0 );
}
