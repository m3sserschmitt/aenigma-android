#include <jni.h>
#include <map>

#include "./libcryptography7/include/cryptography/Libcryptography.hh"

using namespace std;

static int64_t i = 0;
static map<int64_t, ICryptoContext *> handles;

static long long getHandle(ICryptoContext *ctx)
{
    if(!ctx)
    {
        return -1;
    }

    i = i < 0 ? 0 : i;
    handles[i] = ctx;
    i++;

    return i - 1;
}

static bool freeHandle(int64_t i)
{
    auto it = handles.find(i);

    if(it == handles.end())
    {
        return false;
    }

    delete it->second;

    return true;
}

static ICryptoContext *getCryptoContext(int64_t handle)
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

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoContext_00024Companion_createEncryptionContext(
        JNIEnv *env,
        jobject thiz,
        jstring key) {
    const char *publicKey = env->GetStringUTFChars(key, nullptr);

    ICryptoContext *ctx = CreateAsymmetricEncryptionContext(publicKey);
    env->ReleaseStringUTFChars(key, publicKey);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoContext_00024Companion_createDecryptionContext(
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

    ICryptoContext *ctx = CreateAsymmetricDecryptionContext(privateKey, _protectionPassphrase);

    delete[] _protectionPassphrase;
    env->ReleaseStringUTFChars(key, privateKey);
    env->ReleaseStringUTFChars(passphrase, protectionPassphrase);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoContext_00024Companion_createSignatureContext(
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

    ICryptoContext *ctx = CreateSignatureContext(privateKey, _protectionPassphrase);

    delete[] _protectionPassphrase;
    env->ReleaseStringUTFChars(key, privateKey);
    env->ReleaseStringUTFChars(passphrase, protectionPassphrase);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_enigma_crypto_CryptoContext_00024Companion_createSignatureVerificationContext(
        JNIEnv *env,
        jobject thiz,
        jstring key) {

    const char *publicKey = env->GetStringUTFChars(key, nullptr);

    ICryptoContext *ctx = CreateVerificationContext(publicKey);

    env->ReleaseStringUTFChars(key, publicKey);

    return getHandle(ctx);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_enigma_crypto_CryptoContextHandle_00024Companion_freeContext(
        JNIEnv *env,
        jobject thiz,
        jlong i) {

    return freeHandle(i);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_enigma_crypto_CryptoProvider_00024Companion_encrypt
(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jbyteArray plaintext) {

    ICryptoContext *ctx = getCryptoContext(handle);

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

    ICryptoContext *ctx = getCryptoContext(handle);

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

    ICryptoContext *ctx = getCryptoContext(handle);

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

    ICryptoContext *ctx = getCryptoContext(handle);

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
