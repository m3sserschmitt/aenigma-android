#ifndef SYMMETRIC_KEY_HH
#define SYMMETRIC_KEY_HH

#include "Key.hh"

class SymmetricKey : public Key
{
    unsigned char * keyData;

    SymmetricKey(const SymmetricKey &);

    const SymmetricKey &operator=(const SymmetricKey &);

    SymmetricKey() : Key(KeySymmetric),
                     keyData(new unsigned char[SYMMETRIC_KEY_SIZE + 1]) {}

    SymmetricKey(const unsigned char *keyData) : Key(KeySymmetric),
                                                 keyData(new unsigned char[SYMMETRIC_KEY_SIZE + 1])
    {
        this->writeKeyData(keyData);
    }

    bool writeKeyData(const unsigned char *keyData)
    {
        if (keyData and this->keyData)
        {
            memcpy(this->keyData, keyData, SYMMETRIC_KEY_SIZE);
            return true;
        }

        return false;
    }

    void cleanKeyData()
    {
        if (this->notNullKeyData())
        {
            memset(this->keyData, 0, SYMMETRIC_KEY_SIZE);
        }
    }

public:
    ~SymmetricKey() { this->freeKey(); }

    bool setKeyData(const unsigned char *keyData, unsigned int keylen, const char *passphrase = nullptr) override
    {
        return this->writeKeyData(keyData);
    }

    bool readKeyFile(const char *path, const char *passphrase = nullptr) override
    {
        return false;
    }

    const void *getKeyData() const override { return this->keyData; }

    int getSize() const override { return this->notNullKeyData() ? SYMMETRIC_KEY_SIZE : -1; }

    void freeKey() override
    {
        this->cleanKeyData();
        delete[] this->keyData;
        this->keyData = nullptr;
    }

    class Factory
    {
    public:
        static Key *create(const unsigned char *keyData) { return new SymmetricKey(keyData); }

        static Key *create() { return new SymmetricKey(); }
    };
};

#endif
