#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_novaos_mobile_MainActivity_nativeSummary(JNIEnv* env, jobject) {
    const std::string value = "NOVA native layer: C++17 / JNI loaded";
    return env->NewStringUTF(value.c_str());
}
