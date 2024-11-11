#include <jni.h>
#include <map>

#include "cryptography/Aenigma.hh"

using namespace std;

static map<int64_t, CryptoContext *> handles;

static long long getHandle(CryptoContext *ctx)
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

static bool freeHandle(int64_t handle)
{
    auto it = handles.find(handle);

    if(it == handles.end())
    {
        return false;
    }

    delete it->second;

    return true;
}

static CryptoContext *getCryptoContext(int64_t handle)
{
    auto it = handles.find(handle);

    if(it == handles.end())
    {
        return nullptr;
    }

    return it->second;
}

static unsigned char* asUnsignedCharArray(JNIEnv *env, jbyteArray array, int &len) {
    len = env->GetArrayLength (array);

    auto *buf = new unsigned char[len];
    env->GetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));

    return buf;
}

static jbyteArray toJByteArray(JNIEnv *env, const unsigned char *data, unsigned int size)
{
    jbyteArray ret = env->NewByteArray((int)size);
    env->SetByteArrayRegion (ret, 0, (int)size, (jbyte *)data);

    return ret;
}

static const char **toArrayOfStrings(JNIEnv *env, jobjectArray array, int &size)
{
    size = env->GetArrayLength(array);
    const char **strings = new const char*[size];

    for (int t = 0; t < size; t ++) {

        auto javaString = (jstring)env->GetObjectArrayElement(array, t);
        const char *cString = env->GetStringUTFChars(javaString, nullptr);

        int stringLen = env->GetStringUTFLength(javaString);
        char *cStringCopy = new char[stringLen + 1];

        memcpy(cStringCopy, cString, stringLen);
        cStringCopy[stringLen] = 0;

        strings[t] = cStringCopy;

        env->ReleaseStringUTFChars(javaString, cString);
        env->DeleteLocalRef(javaString);
    }

    return strings;
}

static void releaseStrings(const char **strings, int count)
{
    for(int k = 0; k < count; k ++)
    {
        delete[] strings[k];
    }

    delete[] strings;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createEncryptionContext(
        JNIEnv *env,
        jobject thiz,
        jstring key) {
    const char *publicKey = env->GetStringUTFChars(key, nullptr);

    CryptoContext *ctx = CreateAsymmetricEncryptionContext(publicKey);
    env->ReleaseStringUTFChars(key, publicKey);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createDecryptionContext(
        JNIEnv *env,
        jobject thiz,
        jstring key,
        jstring passphrase) {

    const char *privateKey = env->GetStringUTFChars(key, nullptr);
    const char *protectionPassphrase = env->GetStringUTFChars(passphrase, nullptr);

    unsigned int passLength = strlen(protectionPassphrase) + 1;
    char *_protectionPassphrase = new char[passLength];
    memset(_protectionPassphrase, 0, passLength);
    strcpy(_protectionPassphrase, protectionPassphrase);

    CryptoContext *ctx = CreateAsymmetricDecryptionContext(privateKey, _protectionPassphrase);

    delete[] _protectionPassphrase;
    env->ReleaseStringUTFChars(key, privateKey);
    env->ReleaseStringUTFChars(passphrase, protectionPassphrase);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createSignatureContext(
        JNIEnv *env,
        jobject thiz,
        jstring key,
        jstring passphrase) {

    const char *privateKey = env->GetStringUTFChars(key, nullptr);
    const char *protectionPassphrase = env->GetStringUTFChars(passphrase, nullptr);

    unsigned int passLength = strlen(protectionPassphrase) + 1;
    char *_protectionPassphrase = new char[passLength];
    memset(_protectionPassphrase, 0, passLength);
    strcpy(_protectionPassphrase, protectionPassphrase);

    CryptoContext *ctx = CreateSignatureContext(privateKey, _protectionPassphrase);

    delete[] _protectionPassphrase;
    env->ReleaseStringUTFChars(key, privateKey);
    env->ReleaseStringUTFChars(passphrase, protectionPassphrase);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_createSignatureVerificationContext(
        JNIEnv *env,
        jobject thiz,
        jstring key) {

    const char *publicKey = env->GetStringUTFChars(key, nullptr);

    CryptoContext *ctx = CreateVerificationContext(publicKey);

    env->ReleaseStringUTFChars(key, publicKey);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_enigma_crypto_CryptoContextHandle_00024Companion_freeContext(
        JNIEnv *env,
        jobject thiz,
        jlong handle) {

    return freeHandle(handle);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_encrypt
(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jbyteArray plaintext) {

    CryptoContext *ctx = getCryptoContext(handle);

    if(not ctx)
    {
        return nullptr;
    }

    int len;
    const unsigned char *data = asUnsignedCharArray(env, plaintext, len);

    const EncrypterData *ciphertext = EncryptDataEx(ctx, data, len);

    delete[] data;

    if(not ciphertext or ciphertext->isError())
    {
        return nullptr;
    }

    return toJByteArray(env, ciphertext->getData(), ciphertext->getDataSize());
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_decrypt(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jbyteArray ciphertext) {

    CryptoContext *ctx = getCryptoContext(handle);

    if(not ctx)
    {
        return nullptr;
    }

    int len;
    const unsigned char *data = asUnsignedCharArray(env, ciphertext, len);

    const EncrypterData *plaintext = DecryptDataEx(ctx, data, len);

    delete[] data;

    if(not plaintext or plaintext->isError())
    {
        return nullptr;
    }

    return toJByteArray(env, plaintext->getData(), plaintext->getDataSize());
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_sign(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jbyteArray data) {

    CryptoContext *ctx = getCryptoContext(handle);

    if(not ctx)
    {
        return nullptr;
    }

    int len;
    const unsigned char *_data = asUnsignedCharArray(env, data, len);

    const EncrypterData *signature = SignDataEx(ctx, _data, len);

    delete[] _data;

    if(not signature or signature->isError())
    {
        return nullptr;
    }

    return toJByteArray(env, signature->getData(), signature->getDataSize());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_verify(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jbyteArray signature) {

    CryptoContext *ctx = getCryptoContext(handle);

    if(not ctx)
    {
        return false;
    }

    int len;
    const unsigned char *_data = asUnsignedCharArray(env, signature, len);

    bool result = VerifySignature(ctx, _data, len);

    delete[] _data;

    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_unsealOnion(
        JNIEnv *env, jobject thiz,
        jlong handle,
        jbyteArray onion) {

    CryptoContext *ctx = getCryptoContext(handle);

    if(not ctx)
    {
        return nullptr;
    }

    int len;
    const unsigned char *data = asUnsignedCharArray(env, onion, len);
    const unsigned char *plaintext = UnsealOnion(ctx, data, len);
    delete[] data;

    if(not plaintext or len < 0)
    {
        return nullptr;
    }

    return toJByteArray(env, plaintext, len);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_sealOnion(
        JNIEnv *env,
        jobject thiz,
        jbyteArray plaintext,
        jobjectArray keys,
        jobjectArray addresses) {

    int keysCount;
    int addressesCount;

    const char **cKeys = toArrayOfStrings(env, keys, keysCount);
    const char **cAddresses = toArrayOfStrings(env, addresses, addressesCount);

    if(keysCount != addressesCount)
    {
        releaseStrings(cKeys, keysCount);
        releaseStrings(cAddresses, addressesCount);

        return nullptr;
    }

    int plaintextLen;
    const unsigned char *cPlaintext = asUnsignedCharArray(env, plaintext, plaintextLen);

    int outLen;
    const unsigned char *onion = SealOnion(cPlaintext, plaintextLen, cKeys, cAddresses, keysCount, outLen);

    delete[] cPlaintext;
    releaseStrings(cKeys, keysCount);
    releaseStrings(cAddresses, addressesCount);

    if(onion == nullptr || outLen < 0)
    {
        return nullptr;
    }

    jbyteArray out = toJByteArray(env, onion, outLen);
    delete[] onion;

    return out;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_getPKeySize(JNIEnv *env,
                                                                         jobject thiz,
                                                                         jstring publicKey) {
    const char *key = env->GetStringUTFChars(publicKey, nullptr);
    int result = GetPKeySize(key);
    env->ReleaseStringUTFChars(publicKey, key);
    return result;
}