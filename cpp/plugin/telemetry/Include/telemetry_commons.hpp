#ifndef _TELEMETRY_COMMONS_H
#define _TELEMETRY_COMMONS_H

#include "handshake.hpp"

void _checkRenderModeResult( const char* source, const char result );

void onGameStartup( const long version );

void onGameShutdown();

Handshake* getHandshakeIfComplete();

void onSessionStarted();

void onSessionEnded();

void onRealtimeEntered();

void onRealtimeExited();

#endif _TELEMETRY_COMMONS_H
