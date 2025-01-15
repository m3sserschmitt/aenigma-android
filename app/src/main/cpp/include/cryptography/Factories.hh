#ifndef FACTORIES_HH
#define FACTORIES_HH

#include "CryptoContext.hh"

extern "C"
{
    CryptoContext *CreateSymmetricEncryptionContext(const unsigned char *key);

    CryptoContext *CreateSymmetricDecryptionContext(const unsigned char *key);

    CryptoContext *CreateAsymmetricEncryptionContext(const char *key);

    CryptoContext *CreateAsymmetricDecryptionContext(const char *key, const char *passphrase = nullptr);

    CryptoContext *CreateAsymmetricEncryptionContextFromFile(const char *path);

    CryptoContext *CreateAsymmetricDecryptionContextFromFile(const char *key, const char *passphrase = nullptr);

    CryptoContext *CreateSignatureContext(const char *key, const char *passphrase = nullptr);

    CryptoContext *CreateVerificationContext(const char *key);

    CryptoContext *CreateSignatureContextFromFile(const char *path, const char *passphrase = nullptr);

    CryptoContext *CreateVerificationContextFromFile(const char *path);

    void FreeContext(CryptoContext *context);
}

#endif
