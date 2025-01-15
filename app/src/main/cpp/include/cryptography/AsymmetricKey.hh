#ifndef ASYMMETRIC_KEY_HH
#define ASYMMETRIC_KEY_HH

#include "Key.hh"
#include "exceptions/InvalidKey.hh"

class AsymmetricKey : public Key
{
    void *key;

    AsymmetricKey(const AsymmetricKey &);

    const AsymmetricKey &operator=(const AsymmetricKey &);

    AsymmetricKey(KeyType keyType) : Key(keyType)
    {
        this->setKeyType(keyType);
        this->key = nullptr;
    }

public:
    ~AsymmetricKey() { this->freeKey(); }

    bool setKeyData(const unsigned char *keyData, unsigned int len, const char *passphrase = nullptr) override;

    bool readKeyFile(const char *path, const char *passphrase = nullptr) override;

    int getSize() const override;

    const void *getKeyData() const override { return this->key; }

    void freeKey() override;

    class Factory
    {
    public:
        /**
         * @brief Create an uninitialized Public Key object.
         *
         * @return AsymmetricKey* pointer to newly created object.
         */
        static AsymmetricKey *createPublicKey()
        {
            return new AsymmetricKey(PublicKey);
        }

        /**
         * @brief Create an uninitialized Private Key object.
         *
         * @return AsymmetricKey* pointer to newly created object
         */
        static AsymmetricKey *createPrivateKey()
        {
            return new AsymmetricKey(PrivateKey);
        }

        /**
         * @brief Create a Public Key object using a public key in PEM format.
         *
         * @param keyData public key in PEM format
         * @param keylen size of keyData string
         * @param passphrase [Optional] passphrase to unlock the key
         * @return AsymmetricKey* pointer to newly created object
         */
        static AsymmetricKey *createPublicKeyFromPem(const char *keyData, unsigned int keylen, char *passphrase = nullptr)
        {
            AsymmetricKey *key = createPublicKey();

            if (!key->setKeyData((const unsigned char *)keyData, keylen, passphrase))
            {
                delete key;
                return nullptr;
            }

            return key;
        }

        /**
         * @brief Create a Private Key object using a private key in PEM format.
         *
         * @param keyData private key in PEM format
         * @param keylen size of keyData string
         * @param passphrase [Optional] passphrase to unlock the key
         * @return AsymmetricKey* pointer to newly created object
         */
        static AsymmetricKey *createPrivateKeyFromPem(const char *keyData, unsigned int keylen, char *passphrase = nullptr)
        {
            AsymmetricKey *key = createPrivateKey();

            if (!key->setKeyData((const unsigned char *)keyData, keylen, passphrase))
            {
                delete key;
                return nullptr;
            }

            return key;
        }

        /**
         * @brief Create a Public Key object using a public key file in PEM format.
         *
         * @param keyData public key in PEM format
         * @param keylen size of keyData string
         * @param passphrase [Optional] passphrase to unlock the key
         * @return AsymmetricKey* pointer to newly created object
         */
        static AsymmetricKey *createPublicKeyFromFile(const char *path, char *passphrase = nullptr)
        {
            AsymmetricKey *key = createPublicKey();

            if (!key->readKeyFile(path, passphrase))
            {
                delete key;
                return nullptr;
            }

            return key;
        }

        /**
         * @brief Create a Private Key object using a private key file in PEM format.
         *
         * @param keyData private key in PEM format
         * @param keylen size of keyData string
         * @param passphrase [Optional] passphrase to unlock the key
         * @return AsymmetricKey* pointer to newly created object
         */
        static AsymmetricKey *createPrivateKeyFromFile(const char *path, char *passphrase = nullptr)
        {
            AsymmetricKey *key = createPrivateKey();

            if (!key->readKeyFile(path, passphrase))
            {
                delete key;
                return nullptr;
            }

            return key;
        }
    };
};

#endif
