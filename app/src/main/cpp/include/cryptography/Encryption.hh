#ifndef ENCRYPTION_HH
#define ENCRYPTION_HH

#include "CryptoContext.hh"
#include "EncrypterResult.hh"

extern "C"
{
    const unsigned char *EncryptData(CryptoContext *ctx, const unsigned char *plaintext, unsigned int plaintextLen, int &cipherLen);

    const EncrypterResult *EncryptDataEx(CryptoContext *ctx, const unsigned char *plaintext, unsigned int plaintextLen);

    const unsigned char *DecryptData(CryptoContext *ctx, const unsigned char *ciphertext, unsigned int cipherLen, int &plaintextLen);

    const EncrypterResult *DecryptDataEx(CryptoContext *ctx, const unsigned char *ciphertext, unsigned int cipherLen);

    const unsigned char *SignData(CryptoContext *ctx, const unsigned char *plaintext, unsigned int plaintextLen, int &signatureLen);

    const EncrypterResult *SignDataEx(CryptoContext *ctx, const unsigned char *plaintext, unsigned int plaintextLen);

    bool VerifySignature(CryptoContext *ctx, const unsigned char *ciphertext, unsigned int cipherLen);
}

#endif
