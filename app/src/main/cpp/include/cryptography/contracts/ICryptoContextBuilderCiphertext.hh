#ifndef I_CRYPTO_CONTEXT_BUILDER_CIPHERTEXT
#define I_CRYPTO_CONTEXT_BUILDER_CIPHERTEXT

#include "ICryptoContextBuilderKeyData.hh"

class ICryptoContextBuilderCiphertext
{
public:
    virtual ~ICryptoContextBuilderCiphertext() {}
    virtual ICryptoContextBuilderKeyData *setCiphertext(const unsigned char *data, unsigned int datalen) = 0;
    virtual ICryptoContextBuilderKeyData *noCiphertext() = 0;
};

#endif
