#ifndef I_CRYPTO_CONTEXT_BUILDER_RSA_OPERATION
#define I_CRYPTO_CONTEXT_BUILDER_RSA_OPERATION

#include "ICryptoContextBuilderKeyData.hh"
#include "ICryptoContextBuilderOperation.hh"

class ICryptoContextBuilderRsaOperation : public ICryptoContextBuilderOperation
{
public:
    virtual ~ICryptoContextBuilderRsaOperation() {}
    virtual ICryptoContextBuilderPlaintext *useSignature() = 0;
    virtual ICryptoContextBuilderCiphertext *useSignatureVerification() = 0;
};

#endif
