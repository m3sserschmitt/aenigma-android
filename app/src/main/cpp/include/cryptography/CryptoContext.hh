#ifndef CRYPTO_CONTEXT_HH
#define CRYPTO_CONTEXT_HH

#include "EncryptionMachine.hh"
#include "DecryptionMachine.hh"
#include "SymmetricKey.hh"
#include "AsymmetricKey.hh"
#include "enums/CryptoOp.hh"
#include "enums/CryptoType.hh"
#include "exceptions/InvalidOperation.hh"

class CryptoContext
{
    CryptoType cryptoType;
    CryptoOp cryptoOp;

    Key *key;
    EvpContext *cipher;
    CryptoMachine *cryptoMachine;

    CryptoContext(const CryptoContext &);
    const CryptoContext &operator=(const CryptoContext &);

    bool notNullCryptoMachine() const { return this->cryptoMachine != nullptr; }

    bool notNullKey() const { return this->key != nullptr; }

    bool notNullCipher() const { return this->cipher != nullptr; }

    void freeKey()
    {
        delete this->key;
        this->key = nullptr;
    }

    bool allocateKey();

    bool initKey()
    {
        this->freeKey();
        return this->allocateKey();
    }

    void freeCipher()
    {
        delete this->cipher;
        this->cipher = nullptr;
    }

    bool allocateCipher();

    bool initCipher()
    {
        this->freeCipher();
        return this->allocateCipher();
    }

    void freeCryptoMachine()
    {
        delete this->cryptoMachine;
        this->cryptoMachine = nullptr;
    }

    bool allocateCryptoMachine();

    bool initCryptoMachine()
    {
        this->freeCryptoMachine();
        return this->allocateCryptoMachine();
    }

    CryptoContext(CryptoType cryptoType, CryptoOp cryptoOp)
    {
        this->cryptoMachine = nullptr;
        this->key = nullptr;
        this->cipher = nullptr;
        this->setCryptoType(cryptoType);
        this->setCryptoOp(cryptoOp);
        this->allocateMemory();
    }

    CryptoContext()
    {
        this->cryptoMachine = nullptr;
        this->key = nullptr;
        this->cipher = nullptr;
    }

public:
    ~CryptoContext() { this->cleanup(); }

    CryptoOp getCryptoOp() const { return this->cryptoOp; }

    CryptoType getCryptoType() const { return this->cryptoType; }

    void setCryptoType(CryptoType cryptoType) { this->cryptoType = cryptoType; }

    void setCryptoOp(CryptoOp cryptoOp) { this->cryptoOp = cryptoOp; }

    bool allocateMemory()
    {
        return this->initKey() and
               this->initCipher() and
               this->initCryptoMachine();
    }

    bool setKey256(const unsigned char *key)
    {
        if (this->notNullKey() and !this->key->isSymmetricKey())
        {
            throw InvalidKey(INVALID_KEY_MATERIAL);
        }

        return this->notNullKey() and this->key->setKeyData(key, SYMMETRIC_KEY_SIZE);
    }

    bool setKeyData(const char *key, const char *passphrase = nullptr)
    {
        return this->notNullKey() and this->key->setKeyData((const unsigned char *)key, strlen(key), passphrase);
    }

    bool readKeyFile(const char *path, const char *passphrase = nullptr)
    {
        if (this->notNullKey() and this->key->isSymmetricKey())
        {
            throw InvalidKey(INVALID_KEY_MATERIAL);
        }

        return this->notNullKey() and this->key->readKeyFile(path, passphrase);
    }

    bool isSetForEncryption() const
    {
        return this->notNullCryptoMachine() and this->getCryptoOp() == Encrypt;
    }

    bool isSetForSigning() const
    {
        return this->notNullCryptoMachine() and this->getCryptoOp() == Sign;
    }

    bool setPlaintext(const unsigned char *data, unsigned int datalen)
    {
        if (!(this->isSetForEncryption() or this->isSetForSigning()))
        {
            throw InvalidOperation(COULD_NOT_SET_PLAINTEXT_IN_CONTEXT);
        }

        return this->cryptoMachine->setInput(data, datalen);
    }

    bool isSetForDecryption() const
    {
        return this->notNullCryptoMachine() and this->getCryptoOp() == Decrypt;
    }

    bool isSetForVerifying() const
    {
        return this->notNullCryptoMachine() and this->getCryptoOp() == SignVerify;
    }

    const EncrypterResult *getPlaintext() const
    {
        return this->isSetForDecryption() or this->isSetForVerifying() ? this->cryptoMachine->getOutput() : nullptr;
    }

    bool setCiphertext(const unsigned char *data, unsigned int datalen)
    {
        if (!(this->isSetForDecryption() or this->isSetForVerifying()))
        {
            throw InvalidOperation(COULD_NOT_SET_CIPHERTEXT_IN_CONTEXT);
        }

        return this->cryptoMachine->setInput(data, datalen);
    }

    const EncrypterResult *getCiphertext() const
    {
        return this->isSetForEncryption() or this->isSetForSigning() ? this->cryptoMachine->getOutput() : nullptr;
    }

    bool run() { return this->notNullCryptoMachine() and this->cryptoMachine->run(); }

    void cleanup()
    {
        this->freeCryptoMachine();
        this->freeKey();
        this->freeCipher();
    }

    class Factory
    {
    public:
        static CryptoContext *createAesEncryptionContext()
        {
            return new CryptoContext(SymmetricCryptography, Encrypt);
        }

        static CryptoContext *CreateAesDecryptionContext()
        {
            return new CryptoContext(SymmetricCryptography, Decrypt);
        }

        static CryptoContext *createRsaEncryptionContext()
        {
            return new CryptoContext(AsymmetricCryptography, Encrypt);
        }

        static CryptoContext *createRsaDecryptionContext()
        {
            return new CryptoContext(AsymmetricCryptography, Decrypt);
        }

        static CryptoContext *createRsaSignatureContext()
        {
            return new CryptoContext(AsymmetricCryptography, Sign);
        }

        static CryptoContext *createRsaSignatureVerificationContext()
        {
            return new CryptoContext(AsymmetricCryptography, SignVerify);
        }

        static CryptoContext *CreateCryptoContext()
        {
            return new CryptoContext();
        }
    };
};

#endif