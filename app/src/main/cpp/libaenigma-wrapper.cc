#include <jni.h>
#include <map>

#include "cryptography/Aenigma.hh"

using namespace std;

static CryptoContext *decryptContext = nullptr;
static CryptoContext *signContext = nullptr;

static unsigned char* toUnsignedCharArray(JNIEnv *env, jbyteArray array, int &len)
{
    len = env->GetArrayLength(array);
    auto buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}

static jbyteArray toJByteArray(JNIEnv *env, const unsigned char *data, unsigned int size)
{
    auto ret = env->NewByteArray((int)size);
    env->SetByteArrayRegion(ret, 0, (int)size, (jbyte *)data);
    return ret;
}

static const char **toArrayOfStrings(JNIEnv *env, jobjectArray array, int &size)
{
    size = env->GetArrayLength(array);
    auto **strings = new const char*[size];

    for (int t = 0; t < size; t ++) {

        auto javaString = (jstring)env->GetObjectArrayElement(array, t);
        auto string = env->GetStringUTFChars(javaString, nullptr);

        auto len = env->GetStringUTFLength(javaString);
        auto stringCopy = new char[len + 1];
        memcpy(stringCopy, string, len);
        stringCopy[len] = 0;
        strings[t] = stringCopy;

        env->ReleaseStringUTFChars(javaString, string);
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

static CryptoContext *createEncryptionContext(JNIEnv *env, jstring key)
{
    auto publicKey = env->GetStringUTFChars(key, nullptr);
    auto ctx = CreateAsymmetricEncryptionContext(publicKey);
    env->ReleaseStringUTFChars(key, publicKey);
    return ctx;
}

static CryptoContext *createDecryptionContext(JNIEnv *env, jstring key, jstring passphrase)
{
    auto privateKey = env->GetStringUTFChars(key, nullptr);
    auto protectionPassphrase = env->GetStringUTFChars(passphrase, nullptr);
    auto ctx = CreateAsymmetricDecryptionContext(privateKey, protectionPassphrase);
    env->ReleaseStringUTFChars(key, privateKey);
    env->ReleaseStringUTFChars(passphrase, protectionPassphrase);
    return ctx;
}

static CryptoContext *createSignatureContext(JNIEnv *env, jstring key, jstring passphrase)
{
    auto privateKey = env->GetStringUTFChars(key, nullptr);
    auto protectionPassphrase = env->GetStringUTFChars(passphrase, nullptr);
    auto ctx = CreateSignatureContext(privateKey, protectionPassphrase);
    env->ReleaseStringUTFChars(key, privateKey);
    env->ReleaseStringUTFChars(passphrase, protectionPassphrase);
    return ctx;
}

static CryptoContext *createSignatureVerificationContext(JNIEnv *env, jstring key)
{
    auto publicKey = env->GetStringUTFChars(key, nullptr);
    auto ctx = CreateVerificationContext(publicKey);
    env->ReleaseStringUTFChars(key, publicKey);
    return ctx;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_initDecryption(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jstring privateKey,
                                                                            jstring passphrase) {
    if(decryptContext == nullptr) {
        decryptContext = createDecryptionContext(env, privateKey, passphrase);
    }
    return decryptContext != nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_initSignature(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jstring privateKey,
                                                                           jstring passphrase) {
    if(signContext == nullptr) {
        signContext = createSignatureContext(env, privateKey, passphrase);
    }
    return signContext != nullptr;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_encrypt(
        JNIEnv *env,
        jobject thiz,
        jstring key,
        jbyteArray plaintext) {

    auto ctx = createEncryptionContext(env, key);

    if(not ctx)
    {
        return nullptr;
    }

    int len;
    auto data = toUnsignedCharArray(env, plaintext, len);
    auto ciphertext = EncryptDataEx(ctx, data, len);
    delete[] data;

    if(not ciphertext or ciphertext->isError())
    {
        delete ctx;
        return nullptr;
    }

    auto result = toJByteArray(env, ciphertext->getData(), ciphertext->getDataSize());
    delete ctx;
    
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_decrypt(
        JNIEnv *env,
        jobject thiz,
        jbyteArray ciphertext) {
    
    if(not decryptContext)
    {
        return nullptr;
    }

    int len;
    auto data = toUnsignedCharArray(env, ciphertext, len);
    auto plaintext = DecryptDataEx(decryptContext, data, len);
    delete[] data;

    if(not plaintext or plaintext->isError())
    {
        return nullptr;
    }

    return toJByteArray(env, plaintext->getData(), plaintext->getDataSize());
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_sign(
        JNIEnv *env,
        jobject thiz,
        jbyteArray plaintext) {
    
    if(not signContext)
    {
        return nullptr;
    }

    int len;
    auto data = toUnsignedCharArray(env, plaintext, len);
    auto signature = SignDataEx(signContext, data, len);
    delete[] data;

    if(not signature or signature->isError())
    {
        return nullptr;
    }

    return toJByteArray(env, signature->getData(), signature->getDataSize());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_verify(
        JNIEnv *env,
        jobject thiz,
        jstring key,
        jbyteArray signature) {

    auto ctx = createSignatureVerificationContext(env, key);

    if(not ctx)
    {
        return false;
    }

    int len;
    auto data = toUnsignedCharArray(env, signature, len);
    auto result = VerifySignature(ctx, data, len);
    delete[] data;
    delete ctx;

    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_unsealOnion(
        JNIEnv *env, jobject thiz,
        jbyteArray onion) {

    if(not decryptContext)
    {
        return nullptr;
    }

    int len;
    auto data = toUnsignedCharArray(env, onion, len);
    auto plaintext = UnsealOnion(decryptContext, data, len);
    delete[] data;

    if(not plaintext or len < 0)
    {
        return nullptr;
    }

    return toJByteArray(env, plaintext, len);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_sealOnion(
        JNIEnv *env,
        jobject thiz,
        jbyteArray plaintext,
        jobjectArray keys,
        jobjectArray addresses) {

    int keysCount;
    int addressesCount;
    int plaintextLen;
    int outLen;

    unsigned char *input = nullptr;
    const unsigned char *onion = nullptr;
    jbyteArray out = nullptr;

    auto cKeys = toArrayOfStrings(env, keys, keysCount);
    auto cAddresses = toArrayOfStrings(env, addresses, addressesCount);

    if (keysCount != addressesCount) {
        goto cleanup;
    }

    input = toUnsignedCharArray(env, plaintext, plaintextLen);
    onion = SealOnion(input, plaintextLen, cKeys, cAddresses, keysCount, outLen);

    if (onion == nullptr || outLen < 0) {
        goto cleanup;
    }

    out = toJByteArray(env, onion, outLen);

    cleanup:
    delete[] input;
    delete[] onion;
    releaseStrings(cKeys, keysCount);
    releaseStrings(cAddresses, addressesCount);

    return out;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ro_aenigma_crypto_CryptoProvider_00024Companion_getPKeySize(
        JNIEnv *env,
        jobject thiz,
        jstring publicKey) {
    auto key = env->GetStringUTFChars(publicKey, nullptr);
    auto result = GetPKeySize(key);
    env->ReleaseStringUTFChars(publicKey, key);
    return result;
}
