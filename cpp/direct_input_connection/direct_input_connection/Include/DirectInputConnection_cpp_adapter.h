#ifndef DIRECT_INPUT_CONNECTION_CPP_ADAPTER_H
#define DIRECT_INPUT_CONNECTION_CPP_ADAPTER_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

int cpp_initInputDeviceManager( JNIEnv* env, jobject directInputConnection, jbyteArray jBuffer, jint titleLength, jint bufferLength );
int cpp_initDirectInputAndStartPolling( JNIEnv* env, jobject directInputConnection, jbyteArray jBuffer, jint titleLength, jint bufferLength );
void cpp_interruptPolling();

#ifdef __cplusplus
}
#endif

#endif // DIRECT_INPUT_CONNECTION_CPP_ADAPTER_H
