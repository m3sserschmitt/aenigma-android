#ifndef I_CRYPTO_CONTEXT_BUILDER_PLAINTEXT
#define I_CRYPTO_CONTEXT_BUILDER_PLAINTEXT

#include "ICryptoContextBuilderKeyData.hh"

class ICryptoContextBuilderPlaintext
{
public:
    virtual ~ICryptoContextBuilderPlaintext() {}
    virtual ICryptoContextBuilderKeyData *setPlaintext(const unsigned char *data, unsigned int datalen) = 0;
    virtual ICryptoContextBuilderKeyData *noPlaintext() = 0;
};

#endif
