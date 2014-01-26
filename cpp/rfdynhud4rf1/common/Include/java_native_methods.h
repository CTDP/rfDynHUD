#include <jni.h>

#ifndef _Included_java_native_methods
#define _Included_java_native_methods
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor1__rf1_TelemetryData
 * Method:    fetchData
 * Signature: (JI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor1__1rf1_1TelemetryData_fetchData
  (JNIEnv *, jclass, jlong, jint, jbyteArray);

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor1__rf1_ScoringInfo
 * Method:    fetchData
 * Signature: (IJI[BJI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor1__1rf1_1ScoringInfo_fetchData
  (JNIEnv *, jclass, jint, jlong, jint, jbyteArray, jlong, jint, jbyteArray);

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor1__rf1_CommentaryRequestInfo
 * Method:    fetchData
 * Signature: (JI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor1__1rf1_1CommentaryRequestInfo_fetchData
  (JNIEnv *, jclass, jlong, jint, jbyteArray);

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor1__rf1_GraphicsInfo
 * Method:    fetchData
 * Signature: (JI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor1__1rf1_1GraphicsInfo_fetchData
  (JNIEnv *, jclass, jlong, jint, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
