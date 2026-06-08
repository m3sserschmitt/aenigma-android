#include <jni.h>
#include <map>

#include "cryptography/Aenigma.hh"

using namespace std;

static CryptoContext *decryptContext = nullptr;
static CryptoContext *signContext = nullptr;

static unsigned char* toUnsignedCharArray(JNIEnv *env, jbyteArray array, int &len) {
    if (array == nullptr) {
        len = 0;
        return nullptr;
    }

    len = env->GetArrayLength(array);
    if (len == 0) {
        return nullptr;
    }

    jbyte *arrayElements = env->GetByteArrayElements(array, nullptr);
    if (arrayElements == nullptr) {
        len = 0;
        return nullptr;
    }

    auto *nativeArray = new unsigned char[len];
    memcpy(nativeArray, arrayElements, len);

    memset(arrayElements, 0, len);
    env->ReleaseByteArrayElements(array, arrayElements, JNI_ABORT);

    return nativeArray;
}

static char* toCharArray(JNIEnv *env, jbyteArray array, int &len) {
    if (array == nullptr) {
        len = 0;
        return nullptr;
    }

    len = env->GetArrayLength(array);
    if (len == 0) {
        return nullptr;
    }

    jbyte *arrayElements = env->GetByteArrayElements(array, nullptr);
    if (arrayElements == nullptr) {
        len = 0;
        return nullptr;
    }

    char *nativeArray = new char[len + 1];
    memcpy(nativeArray, arrayElements, len);
    nativeArray[len] = '\0';

    memset(arrayElements, 0, len);
    env->ReleaseByteArrayElements(array, arrayElements, JNI_ABORT);

    return nativeArray;
}

template<typename T>
static void freeArray(T* array, int len) {
    if (array == nullptr) { return; }
    memset(array, 0, len * sizeof(T));
    delete[] array;
}

static jbyteArray toJByteArray(JNIEnv *env, const unsigned char *data, unsigned int size) {
    if (data == nullptr || size == 0) {
        return nullptr;
    }

    auto ret = env->NewByteArray((jsize) size);
    if (ret == nullptr) {
        return nullptr;
    }

    env->SetByteArrayRegion(ret, 0, (jsize) size, reinterpret_cast<const jbyte *>(data));

    return ret;
}

static void freeArrayOfStrings(const char **strings, int size) {
    if (strings == nullptr) { return; }

    for (int t = 0; t < size; t++) {
        delete[] strings[t];
    }

    delete[] strings;
}

static const char **toArrayOfStrings(JNIEnv *env, jobjectArray array, int &size) {
    if (array == nullptr) {
        size = 0;
        return nullptr;
    }

    size = env->GetArrayLength(array);
    if (size == 0) {
        return nullptr;
    }

    auto **strings = new const char *[size]();

    for (int t = 0; t < size; t++) {

        auto byteArray = (jbyteArray) env->GetObjectArrayElement(array, t);

        if (byteArray == nullptr) {
            strings[t] = nullptr;
            continue;
        }

        int byteArraySize;
        strings[t] = toCharArray(env, byteArray, byteArraySize);
    }

    return strings;
}

static CryptoContext *createDecryptionContext(JNIEnv *env, jbyteArray key, jbyteArray passphrase) {
    int privateKeySize;
    int protectionPassphraseSize;
    auto privateKey = toCharArray(env, key, privateKeySize);
    auto protectionPassphrase = toCharArray(env, passphrase, protectionPassphraseSize);
    auto ctx = CreateAsymmetricDecryptionContext(privateKey, protectionPassphrase);
    freeArray(privateKey, privateKeySize);
    freeArray(protectionPassphrase, protectionPassphraseSize);
    return ctx;
}

static CryptoContext *createSignatureContext(JNIEnv *env, jbyteArray key, jbyteArray passphrase) {
    int privateKeySize;
    int protectionPassphraseSize;
    auto privateKey = toCharArray(env, key, privateKeySize);
    auto protectionPassphrase = toCharArray(env, passphrase, protectionPassphraseSize);
    auto ctx = CreateSignatureContext(privateKey, protectionPassphrase);
    freeArray(privateKey, privateKeySize);
    freeArray(protectionPassphrase, protectionPassphraseSize);
    return ctx;
}

static CryptoContext *createSignatureVerificationContext(JNIEnv *env, jbyteArray key) {
    int publicKeySize;
    auto publicKey = toCharArray(env, key, publicKeySize);
    auto ctx = CreateVerificationContext(publicKey);
    freeArray(publicKey, publicKeySize);
    return ctx;
}

static CryptoContext *createSymmetricEncryptionContext(JNIEnv *env, jbyteArray key) {
    int keySize;
    auto cKey = toUnsignedCharArray(env, key, keySize);
    auto ctx = CreateSymmetricEncryptionContext(cKey);
    freeArray(cKey, keySize);
    return ctx;
}

static CryptoContext *createSymmetricDecryptionContext(JNIEnv *env, jbyteArray key) {
    int keySize;
    auto cKey = toUnsignedCharArray(env, key, keySize);
    auto ctx = CreateSymmetricDecryptionContext(cKey);
    freeArray(cKey, keySize);
    return ctx;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ro_aenigma_crypto_CryptoProvider_initDecryption(JNIEnv *env,
                             jobject thiz,
                             jbyteArray privateKey,
                             jbyteArray passphrase) {
    if (decryptContext == nullptr) {
        decryptContext = createDecryptionContext(env, privateKey, passphrase);
    }
    return decryptContext != nullptr;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ro_aenigma_crypto_CryptoProvider_initSignature(JNIEnv *env,
                             jobject thiz,
                             jbyteArray privateKey,
                             jbyteArray passphrase) {
    if (signContext == nullptr) {
        signContext = createSignatureContext(env, privateKey, passphrase);
    }
    return signContext != nullptr;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_sign(
        JNIEnv *env,
        jobject thiz,
        jbyteArray plaintext) {

    if (not signContext) {
        return nullptr;
    }

    int len;
    auto data = toUnsignedCharArray(env, plaintext, len);
    auto signature = RunEx(signContext, data, len);
    freeArray(data, len);

    if (not signature or signature->isError()) {
        return nullptr;
    }

    return toJByteArray(env, signature->getData(), signature->getDataSize());
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ro_aenigma_crypto_CryptoProvider_verify(
        JNIEnv *env,
        jobject thiz,
        jbyteArray key,
        jbyteArray signature) {

    auto ctx = createSignatureVerificationContext(env, key);

    if (not ctx) {
        return false;
    }

    int len;
    auto data = toUnsignedCharArray(env, signature, len);
    auto result = RunVerification(ctx, data, len);
    freeArray(data, len);
    delete ctx;

    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_unsealOnion(
        JNIEnv *env, jobject thiz,
        jbyteArray onion) {

    if (not decryptContext) {
        return nullptr;
    }

    int len;
    auto data = toUnsignedCharArray(env, onion, len);
    auto plaintext = UnsealOnion(decryptContext, data, len);
    freeArray(data, len);

    if (not plaintext or len < 0) {
        return nullptr;
    }

    return toJByteArray(env, plaintext, len);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_sealOnion(
        JNIEnv *env,
        jobject thiz,
        jbyteArray plaintext,
        jobjectArray keys,
        jobjectArray addresses) {

    int keysCount = 0;
    int addressesCount = 0;
    int plaintextSize = 0;
    int onionSize = 0;

    unsigned char *cPlaintext = nullptr;
    const unsigned char *onion = nullptr;
    jbyteArray out = nullptr;

    auto cKeys = toArrayOfStrings(env, keys, keysCount);
    auto cAddresses = toArrayOfStrings(env, addresses, addressesCount);

    if (keysCount != addressesCount) {
        goto cleanup;
    }

    cPlaintext = toUnsignedCharArray(env, plaintext, plaintextSize);
    onion = SealOnion(cPlaintext, plaintextSize, cKeys, cAddresses, keysCount, onionSize);

    if (onion == nullptr || onionSize < 0) {
        goto cleanup;
    }

    out = toJByteArray(env, onion, onionSize);

    cleanup:
    freeArray(cPlaintext, plaintextSize);
    freeArray((unsigned char *) onion, onionSize);
    freeArrayOfStrings(cKeys, keysCount);
    freeArrayOfStrings(cAddresses, addressesCount);

    return out;
}

extern "C"
JNIEXPORT jint JNICALL
Java_ro_aenigma_crypto_CryptoProvider_getPKeySize(
        JNIEnv *env,
        jobject thiz,
        jbyteArray key) {
    int publicKeySize;
    auto publicKey = toCharArray(env, key, publicKeySize);
    auto result = GetPKeySize(publicKey);
    freeArray(publicKey, publicKeySize);
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_encryptSymmetric(
        JNIEnv *env,
        jobject thiz,
        jbyteArray key,
        jbyteArray plaintext) {
    auto ctx = createSymmetricEncryptionContext(env, key);

    if (not ctx) {
        return nullptr;
    }

    int plaintextSize;
    auto cPlaintext = toUnsignedCharArray(env, plaintext, plaintextSize);

    auto ciphertext = RunEx(ctx, cPlaintext, plaintextSize);
    freeArray(cPlaintext, plaintextSize);

    if (not ciphertext or ciphertext->isError()) {
        delete ctx;
        return nullptr;
    }

    auto out = toJByteArray(env, ciphertext->getData(), ciphertext->getDataSize());
    delete ctx;

    return out;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_ro_aenigma_crypto_CryptoProvider_decryptSymmetric(
        JNIEnv *env,
        jobject thiz,
        jbyteArray key,
        jbyteArray ciphertext) {
    auto ctx = createSymmetricDecryptionContext(env, key);

    if (not ctx) {
        return nullptr;
    }

    int ciphertextSize;
    auto cCiphertext = toUnsignedCharArray(env, ciphertext, ciphertextSize);

    auto plaintext = RunEx(ctx, cCiphertext, ciphertextSize);
    freeArray(cCiphertext, ciphertextSize);

    if (not plaintext or plaintext->isError()) {
        delete ctx;
        return nullptr;
    }

    auto out = toJByteArray(env, plaintext->getData(), plaintext->getDataSize());
    delete ctx;

    return out;
}
