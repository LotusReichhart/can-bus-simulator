#include <jni.h>

extern "C" JNIEXPORT jbyte JNICALL
Java_com_lotusreichhart_canbussimulator_data_jni_NativeCalculator_calculateChecksum(
        JNIEnv *env,
        jobject thiz,
        jbyteArray data) {
    if (data == nullptr) {
        return 0;
    }

    jsize len = env->GetArrayLength(data);
    jbyte *body = env->GetByteArrayElements(data, nullptr);

    unsigned char crc = 0x00;
    for (jsize i = 0; i < len; i++) {
        crc ^= static_cast<unsigned char>(body[i]);
        for (int j = 0; j < 8; j++) {
            if (crc & 0x80) {
                crc = (crc << 1) ^ 0x07;
            } else {
                crc <<= 1;
            }
        }
    }

    env->ReleaseByteArrayElements(data, body, JNI_ABORT);
    return static_cast<jbyte>(crc);
}
