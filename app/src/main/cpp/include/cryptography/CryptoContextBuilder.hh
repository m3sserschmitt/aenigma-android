#ifndef CRYPTO_CONTEXT_BUILDER_HH
#define CRYPTO_CONTEXT_BUILDER_HH

#include <exception>

#include "contracts/ICryptoContextBuilderType.hh"
#include "CryptoContext.hh"

class CryptoContextBuilder
{
private:
    class Impl
        : public ICryptoContextBuilderType,
          public ICryptoContextBuilderRsaOperation,
          public ICryptoContextBuilderPlaintext,
          public ICryptoContextBuilderCiphertext,
          public ICryptoContextBuilderKeyData,
          public ICryptoContextBuilder
    {
    private:
        bool completed;
        CryptoContext *ctx;

    public:
        Impl()
        {
            completed = false;
            this->ctx = CryptoContext::Factory::CreateCryptoContext();
        }

        ~Impl()
        {
            // Important note:
            // If the user abandons the construction before calling the build() method
            // and, later on, the Impl object is destroyed, then the CryptoContext object
            // will remain behind (although partially initialized) with no chance for
            // further cleanup. Thus, that will cause a memory leak.
            // We have to release the that memory only if the build() method has not been called,
            // i.e., the object has not been returned.
            if (!completed)
            {
                delete this->ctx;
            }
        }

        ICryptoContextBuilderKeyData *noPlaintext() override
        {
            return this;
        }

        ICryptoContextBuilderKeyData *noCiphertext() override
        {
            return this;
        }

        ICryptoContextBuilder *setKey256(const unsigned char *key) override
        {
            if (!this->ctx->setKey256(key))
            {
                throw InvalidOperation(COULD_NOT_SET_KEY);
            }

            return this;
        }

        ICryptoContextBuilder *setKey(const char *key) override
        {
            if (!this->ctx->setKeyData(key))
            {
                throw InvalidOperation(COULD_NOT_SET_KEY);
            }

            return this;
        }

        ICryptoContextBuilder *setKey(const char *key, const char *passphrase) override
        {
            if (!this->ctx->setKeyData(key, passphrase))
            {
                throw InvalidOperation(COULD_NOT_SET_KEY);
            }

            return this;
        }

        ICryptoContextBuilder *readKeyData(const char *path, const char *passphrase) override
        {
            if (!this->ctx->readKeyFile(path, passphrase))
            {
                throw InvalidOperation(COULD_NOT_SET_KEY);
            }

            return this;
        }

        ICryptoContextBuilder *readKeyData(const char *path) override
        {
            if (!this->ctx->readKeyFile(path))
            {
                throw InvalidOperation(COULD_NOT_SET_KEY);
            }

            return this;
        }

        ICryptoContextBuilderKeyData *setPlaintext(const unsigned char *data, unsigned int datalen) override
        {
            if (!this->ctx->setPlaintext(data, datalen))
            {
                throw InvalidOperation(COULD_NOT_SET_PLAINTEXT);
            }

            return this;
        }

        ICryptoContextBuilderKeyData *setCiphertext(const unsigned char *data, unsigned int datalen) override
        {
            if (!this->ctx->setCiphertext(data, datalen))
            {
                throw InvalidOperation(COULD_NOT_SET_CIPHERTEXT);
            }

            return this;
        }

        ICryptoContextBuilderRsaOperation *useRsa() override
        {
            this->ctx->setCryptoType(AsymmetricCryptography);
            return this;
        }

        ICryptoContextBuilderOperation *useAes() override
        {
            this->ctx->setCryptoType(SymmetricCryptography);
            return this;
        }

        ICryptoContextBuilderPlaintext *useEncryption() override
        {
            this->ctx->setCryptoOp(Encrypt);

            if (!this->ctx->allocateMemory())
            {
                throw InvalidOperation(COULD_NOT_INITIALIZE_CONTEXT);
            }

            return this;
        }

        ICryptoContextBuilderCiphertext *useDecryption() override
        {
            this->ctx->setCryptoOp(Decrypt);

            if (!this->ctx->allocateMemory())
            {
                throw InvalidOperation(COULD_NOT_INITIALIZE_CONTEXT);
            }

            return this;
        }

        ICryptoContextBuilderPlaintext *useSignature() override
        {
            this->ctx->setCryptoOp(Sign);

            if (!this->ctx->allocateMemory())
            {
                throw InvalidOperation(COULD_NOT_INITIALIZE_CONTEXT);
            }

            return this;
        }

        ICryptoContextBuilderCiphertext *useSignatureVerification() override
        {
            this->ctx->setCryptoOp(SignVerify);

            if (!this->ctx->allocateMemory())
            {
                throw InvalidOperation(COULD_NOT_INITIALIZE_CONTEXT);
            }

            return this;
        }

        CryptoContext *build() override
        {
            completed = true;
            return this->ctx;
        }
    };

    CryptoContextBuilder() {}
    CryptoContextBuilder(const CryptoContextBuilder &);
    const CryptoContextBuilder &operator=(const CryptoContextBuilder &);

public:
    static ICryptoContextBuilderType *Create()
    {
        return new Impl();
    }
};

#endif