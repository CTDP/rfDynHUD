#include <jni.h>

#ifndef _Included_java_native_methods
#define _Included_java_native_methods
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor2__rf2_TelemetryData
 * Method:    fetchData
 * Signature: (JI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1TelemetryData_fetchData
  (JNIEnv *, jobject, jlong, jint, jbyteArray);

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor2__rf2_ScoringInfo
 * Method:    fetchData
 * Signature: (IJI[BJI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1ScoringInfo_fetchData
  (JNIEnv *, jobject, jint, jlong, jint, jbyteArray, jlong, jint, jbyteArray);

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor2__rf2_CommentaryRequestInfo
 * Method:    fetchData
 * Signature: (JI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1CommentaryRequestInfo_fetchData
  (JNIEnv *, jobject, jlong, jint, jbyteArray);

/*
 * Class:     net_ctdp_rfdynhud_gamedata_rfactor2__rf2_GraphicsInfo
 * Method:    fetchData
 * Signature: (JI[B)V
 */
JNIEXPORT void JNICALL Java_net_ctdp_rfdynhud_gamedata_rfactor2__1rf2_1GraphicsInfo_fetchData
  (JNIEnv *, jobject, jlong, jint, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
