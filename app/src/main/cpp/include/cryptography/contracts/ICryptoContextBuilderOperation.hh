#ifndef I_CRYPTO_CONTEXT_BUILDER_OPERATION
#define I_CRYPTO_CONTEXT_BUILDER_OPERATION

#include "ICryptoContextBuilderPlaintext.hh"
#include "ICryptoContextBuilderCiphertext.hh"

class ICryptoContextBuilderOperation
{
public:
    virtual ~ICryptoContextBuilderOperation() {}
    virtual ICryptoContextBuilderPlaintext *useEncryption() = 0;
    virtual ICryptoContextBuilderCiphertext *useDecryption() = 0;
};

#endif
