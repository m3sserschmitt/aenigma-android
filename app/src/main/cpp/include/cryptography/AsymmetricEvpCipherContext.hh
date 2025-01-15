#ifndef ASYMMETRIC_CIPHER_HH
#define ASYMMETRIC_CIPHER_HH

#include "EvpCipherContext.hh"

class AsymmetricEvpCipherContext : public EvpCipherContext
{
    unsigned char *encryptedKey;
    int encryptedKeyLength;

    AsymmetricEvpCipherContext(const AsymmetricEvpCipherContext &);
    const AsymmetricEvpCipherContext *operator=(const AsymmetricEvpCipherContext &);

    void freeEncryptedKey()
    {
        if (this->encryptedKey)
        {
            memset(encryptedKey, 0, SYMMETRIC_KEY_SIZE);
            delete[] encryptedKey;
            this->encryptedKey = nullptr;
            this->encryptedKeyLength = 0;
        }
    }

    bool allocateEncryptedKey()
    {
        int pkeySize = this->getKeySize();

        if (pkeySize <= 0)
        {
            return false;
        }

        this->freeEncryptedKey();
        this->encryptedKey = new unsigned char[pkeySize + 1];
        this->encryptedKeyLength = 0;

        return this->encryptedKey != nullptr;
    }

    bool writeEncryptedKey(const unsigned char *encryptedKey)
    {
        if (encryptedKey and this->encryptedKey)
        {
            unsigned int encryptedKeySize = this->getKeySize();
            memcpy(this->encryptedKey, encryptedKey, encryptedKeySize);
            this->encryptedKeyLength = encryptedKeySize;
            return true;
        }

        return false;
    }

    bool sealEnvelopeAllocateMemory(const EncrypterData *in)
    {
        return this->allocateCipherContext() and this->allocateEncryptedKey() and this->allocateIV() and this->allocateOutBuffer(in->getDataSize()) and this->allocateTag();
    }

    bool openEnvelopeAllocateMemory(const EncrypterData *in)
    {
        unsigned int outBufferSize = in->getDataSize() - this->getKeySize() - IV_SIZE - TAG_SIZE;
        return this->allocateCipherContext() and this->allocateEncryptedKey() and this->allocateIV() and this->allocateOutBuffer(outBufferSize) and this->allocateTag();
    }

    unsigned int calculateEnvelopeSize() const
    {
        return this->encryptedKeyLength + IV_SIZE + this->getOutBufferSize() + TAG_SIZE;
    }

    /**
     * @brief Create an Envelope;
     *
     * Envelope structure:
     * N = size of public key in bytes (e.g. 2048 bits key length => N = 256 bytes) = len(EK);
     *
     * Structure of envelope:
     * 1. Encrypted Key (EK);
     * 2. Initialization Vector (IV); AES GCM default IV length is 12 bytes;
     * 3. Ciphertext (C); note: length of ciphertext is equal to length of plaintext when using GCM;
     * 4. Tag (T); AES GCM default tag size is 16 bytes;
     *
     * Envelope total size: N + len(IV) + len(C) + len(T)
     *
     * @return EncrypterResult* Structure containing envelope data and size;
     */
    EncrypterResult *createEnvelope() const;

    /**
     * @brief Read a byte array created by createEnvelope method and initializes internal structures
     * i.e. initialization vector (IV), encrypted key (EK) and tag (T)
     *
     * @param in Structure containing envelope data and size
     * @param cipherlen if successful it contains the calculated size of ciphertext (C)
     * @return const unsigned char * pointer to the ciphertext (C)
     */
    const unsigned char *readEnvelope(const EncrypterData *in, int &cipherlen);

public:
    AsymmetricEvpCipherContext(Key *key) : EvpCipherContext(key)
    {
        this->encryptedKey = nullptr;
        this->encryptedKeyLength = 0;
    }

    ~AsymmetricEvpCipherContext() { this->freeEncryptedKey(); }

    EncrypterResult *encrypt(const EncrypterData *in) override;

    EncrypterResult *decrypt(const EncrypterData *in) override;

    void cleanup() override
    {
        EvpCipherContext::cleanup();
        this->freeEncryptedKey();
    }

    class Factory
    {
    public:
        /**
         * @brief Creates a new AsymmetricEvpCipherContext.
         *
         * @param key Initialized AsymmetricKey object
         * @return EvpContext* Newly created AsymmetricEvpCipherContext
         */
        static AsymmetricEvpCipherContext *create(Key *key) { return new AsymmetricEvpCipherContext(key); }
    };
};

#endif
