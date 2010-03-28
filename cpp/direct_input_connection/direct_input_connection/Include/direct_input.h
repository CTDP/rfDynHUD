#ifndef _DIRECT_INPUT_H
#define _DIRECT_INPUT_H

#include <Windows.h>

void initDirectInput( HWND hWnd );

unsigned short getNumKeys();
unsigned char getMaxKeyNameLength();
unsigned short getAllKeyNames( char* buffer );
unsigned short getKeyName( const unsigned short index, char* buffer );

short pollKeyStates();

const unsigned char MAX_JOYSTICK_NAME_LENGTH = 254;
const unsigned char MAX_JOYSTICK_BUTTON_NAME_LENGTH = 64;

unsigned char getNumJoysticks();
void getJoystickNames( char* names );
unsigned char getNumButtons( const unsigned char joystickIndex );
void getJoystickButtonNames( const unsigned char joystickIndex, char* names );
unsigned short getJoystickButtonName( const unsigned char joystickIndex, unsigned char buttonIndex, char* buffer );
short pollJoystickButtonStates();

void disposeDirectInput();

#endif _DIRECT_INPUT_H
