#ifndef I_CRYPTO_CONTEXT_BUILDER
#define I_CRYPTO_CONTEXT_BUILDER

#include "cryptography/CryptoContext.hh"

class ICryptoContextBuilder
{
public:
    virtual ~ICryptoContextBuilder() {}
    virtual CryptoContext *build() = 0;
};

#endif
