#ifndef I_CRYPTO_CONTEXT_BUILDER_KEY_DATA
#define I_CRYPTO_CONTEXT_BUILDER_KEY_DATA

#include "ICryptoContextBuilder.hh"

class ICryptoContextBuilderKeyData
{
public:
    virtual ~ICryptoContextBuilderKeyData() {}
    virtual ICryptoContextBuilder *setKey256(const unsigned char *key) = 0;
    virtual ICryptoContextBuilder *setKey(const char *key) = 0;
    virtual ICryptoContextBuilder *setKey(const char *Key, const char *passphrase) = 0;
    virtual ICryptoContextBuilder *readKeyData(const char *path, const char *passphrase) = 0;
    virtual ICryptoContextBuilder *readKeyData(const char *path) = 0;
};

#endif