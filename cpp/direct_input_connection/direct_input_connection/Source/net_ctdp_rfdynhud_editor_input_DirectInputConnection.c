#include "net_ctdp_rfdynhud_editor_input_DirectInputConnection.h"

#include <jni.h>
#include "DirectInputConnection_cpp_adapter.h"

JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_editor_input_DirectInputConnection_initDirectInputAndStartPolling( JNIEnv* env, jobject directInputConnection, jbyteArray jBuffer, jint titleLength, jint bufferLength )
{
    cpp_initDirectInputAndStartPolling( env, directInputConnection, jBuffer, titleLength, bufferLength );
}

JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_editor_input_DirectInputConnection_nativeInterruptPolling( JNIEnv* env, jobject directInputConnection )
{
    cpp_interruptPolling();
}
