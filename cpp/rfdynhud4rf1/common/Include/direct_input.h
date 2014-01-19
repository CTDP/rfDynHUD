#ifndef _DIRECT_INPUT_H
#define _DIRECT_INPUT_H

#include <Windows.h>

const unsigned char MODIFIER_MASK_SHIFT = 1;
const unsigned char MODIFIER_MASK_CTRL  = 2;
const unsigned char MODIFIER_MASK_LALT  = 4;
const unsigned char MODIFIER_MASK_RALT  = 8;
const unsigned char MODIFIER_MASK_LMETA = 16;
const unsigned char MODIFIER_MASK_RMETA = 32;

bool initDirectInput( HWND hWnd );

unsigned short getNumKeys();
unsigned char getMaxKeyNameLength();
unsigned short getAllKeyNames( char* buffer );

void getKeyStates( const unsigned char numKeys, const unsigned short* keys, unsigned char* states, unsigned char* modifierMask );

const unsigned char MAX_JOYSTICK_NAME_LENGTH = 254;
const unsigned char MAX_JOYSTICK_BUTTON_NAME_LENGTH = 64;

unsigned char getNumJoysticks();
void getJoystickNames( char* names );
unsigned char getNumButtons( const unsigned char joystickIndex );
void getJoystickButtonNames( const unsigned char joystickIndex, char* names );
void getJoystickButtonStates( const unsigned char joystickIndex, const unsigned char numButtons, const unsigned short* buttons, unsigned char* states );

void disposeDirectInput();

#endif _DIRECT_INPUT_H
