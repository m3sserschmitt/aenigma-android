#ifndef KEY_HH
#define KEY_HH

#include "enums/KeyType.hh"

class Key
{
    KeyType keyType;

protected:

    void setKeyType(KeyType keyType)
    {
        this->keyType = keyType;
    }

    Key(KeyType keyType) { this->setKeyType(keyType); }

    bool notNullKeyData() const { return this->getKeyData() != nullptr; }

public:

    virtual ~Key() { }

    KeyType getKeyType() const
    {
        return this->keyType;
    }

    bool isPublicKey() const
    {
        return this->getKeyType() == PublicKey;
    }

    bool isPrivateKey() const
    {
        return this->getKeyType() == PrivateKey;
    }

    bool isSymmetricKey() const
    {
        return this->getKeyType() == KeySymmetric;
    }

    /**
     * @brief Initialize encryption / decryption key from buffer. This method should be overriden into any derived class
     * to achieve desired behavior.
     *
     * @param keyData Key material for initialization
     * @param len Size initialization buffer
     * @param passphrase Passphrase for key file decryption (usually for reading private keys)
     * @return true If initialization successful
     * @return false If initialization failed
     */
    virtual bool setKeyData(const unsigned char *keyData, unsigned int len, const char *passphrase = nullptr) = 0;

    /**
     * @brief Read encryption / decryption key material from file (especially useful for public/private key pairs). Override
     * this method into any derived class to achieve desired behavior
     *
     * @param path Path to file which contains key material for initialization
     * @param passphrase Passphrase for key file decryption (usually for reading private keys)
     * @return true If initialization successful
     * @return false If initialization failed
     */
    virtual bool readKeyFile(const char *path, const char *passphrase = nullptr) = 0;

    /**
     * @brief release all memory related to the key
     * 
     */
    virtual void freeKey() = 0;

    /**
     * @brief Get the Size of key. For symmetric key, it shall return SYMMETRIC_KEY_SIZE.
     * For Asymmetric keys the returned size depends on the key material used for initialization.
     * 
     * @return Size Size of the key in bytes
     */
    virtual int getSize() const = 0;

    /**
     * @brief Returns a pointer to the underlying key structure: for symmetric keys it will be an array of
     * unsigned chars; for asymmetric keys an EVP_PKEY structure.
     * 
     * @return const void* pointer to underlying key structure.
     */
    virtual const void *getKeyData() const = 0;
};

#endif
