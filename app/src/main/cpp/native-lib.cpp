#include <jni.h>
#include <string>

uint8_t counter = 0;

extern "C" JNIEXPORT jstring JNICALL



Java_com_example_boat_1meter_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    hello += std::to_string(counter);
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_boat_1meter_MainActivity_button_1handler(JNIEnv *env, jobject thiz) {
    // TODO: implement button_handler()
    counter++;
}