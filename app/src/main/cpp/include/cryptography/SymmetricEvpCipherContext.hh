#ifndef SYMMETRIC_EVP_CIPHER_CONTEXT_HH
#define SYMMETRIC_EVP_CIPHER_CONTEXT_HH

#include "EvpCipherContext.hh"

class SymmetricEvpCipherContext : public EvpCipherContext
{
    SymmetricEvpCipherContext(const SymmetricEvpCipherContext &);
    const SymmetricEvpCipherContext &operator=(const SymmetricEvpCipherContext &);

    bool encryptionAllocateMemory(const EncrypterData *in)
    {
        return this->allocateCipherContext() and this->allocateIV() and this->allocateOutBuffer(in->getDataSize()) and this->allocateTag();
    }

    bool decryptionAllocateMemory(const EncrypterData *in)
    {
        unsigned int outBufferSize = in->getDataSize() - IV_SIZE - TAG_SIZE;
        return this->allocateCipherContext() and this->allocateIV() and this->allocateOutBuffer(outBufferSize) and this->allocateTag();
    }

    /**
     * @brief Create output buffer resulted from a symmetric encryption
     * 
     * Structure of output buffer: 
     * 1. Initialization Vector (IV);
     * 2. Ciphertext (C)
     * 3. Tag (T)
     * 
     * Total size of the output buffer: len(IV) + len(C) + len(T)
     * 
     * @return EncrypterResult* structure containing the output buffer resulted from symmetric encryption
     */
    EncrypterResult *createEncryptedData() const;

    /**
     * @brief Read the byte array created by createEncryptedData method an initializes internal structures
     * i.e. initialization vector and tag
     * 
     * @param in data to be decrypted, as it was created by createEncryptedData
     * @param cipherlen if successful it contains the size of ciphertext (C)
     * @return const unsigned char * pointer to the ciphertext
     */
    const unsigned char * readEncryptedData(const EncrypterData *in, int &cipherlen);

public:
    SymmetricEvpCipherContext(Key *key) : EvpCipherContext(key) {}

    ~SymmetricEvpCipherContext() {}

    EncrypterResult *encrypt(const EncrypterData *in) override;

    EncrypterResult *decrypt(const EncrypterData *in) override;

    class Factory
    {
    public:
        /**
         * @brief Create a new SymmetricEvpCipherContext.
         *
         * @param key Initialized SymmetricKey object
         * @return EvpContext* Newly created SymmetricEvpCipherContext
         */
        static SymmetricEvpCipherContext *create(Key *key) { return new SymmetricEvpCipherContext(key); }
    };
};

#endif
