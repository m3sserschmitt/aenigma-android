#include <jni.h>
#include <map>

#include "./libcryptography7/include/cryptography/Libcryptography.hh"

using namespace std;

static map<int64_t, ICryptoContext *> handles;

static long long getHandle(ICryptoContext *ctx)
{
    static int64_t i = 0;

    if(!ctx)
    {
        return -1;
    }

    i = i < 0 ? 0 : i;
    handles[i] = ctx;
    i++;

    return i - 1;
}

static void freeHandle(int64_t i)
{
    delete handles[i];
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createEncryptionContext(JNIEnv *env,
                                                                       jobject thiz,
                                                                       jstring key) {
    const char *publicKey = env->GetStringUTFChars(key, nullptr);

    ICryptoContext *ctx = CreateAsymmetricEncryptionContext(publicKey);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createDecryptionContext(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring key,
                                                                                     jstring passphrase) {
    const char *privateKey = env->GetStringUTFChars(key, nullptr);
    const char *protectionPassphrase = env->GetStringUTFChars(passphrase, nullptr);

    unsigned int passLength = strlen(protectionPassphrase) + 1;
    char *_protectionPassphrase = new char[passLength];
    memset(_protectionPassphrase, 0, passLength);
    strcpy(_protectionPassphrase, protectionPassphrase);

    ICryptoContext *ctx = CreateAsymmetricDecryptionContext(privateKey, _protectionPassphrase);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createSignatureContext(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jstring key,
                                                                                    jstring passphrase) {
    const char *privateKey = env->GetStringUTFChars(key, nullptr);
    const char *protectionPassphrase = env->GetStringUTFChars(passphrase, nullptr);

    unsigned int passLength = strlen(protectionPassphrase) + 1;
    char *_protectionPassphrase = new char[passLength];
    memset(_protectionPassphrase, 0, passLength);
    strcpy(_protectionPassphrase, protectionPassphrase);

    ICryptoContext *ctx = CreateSignatureContext(privateKey, _protectionPassphrase);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createSignatureVerificationContext(
        JNIEnv *env, jobject thiz, jstring key) {

    const char *publicKey = env->GetStringUTFChars(key, nullptr);

    ICryptoContext *ctx = CreateVerificationContext(publicKey);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_freeHandle(JNIEnv *env,
                                                                        jobject thiz,
                                                                        jlong i) {
    freeHandle(i);
}
