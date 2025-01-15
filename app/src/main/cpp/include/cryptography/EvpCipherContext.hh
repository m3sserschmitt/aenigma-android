#ifndef EVP_CIPHER_CONTEXT_HH
#define EVP_CIPHER_CONTEXT_HH

#include "EvpContext.hh"

class EvpCipherContext : public EvpContext
{
    void *cipherContext;

    unsigned char * iv;
    unsigned char * tag;

protected:
    void *getCipherContext() { return this->cipherContext; }

    void freeCipherContext();

    bool allocateCipherContext();

    unsigned char * getTag() { return this->tag; }

    const unsigned char * getTag() const { return this->tag; }

    bool writeTag(const unsigned char * tag)
    {
        unsigned char * localTag = this->getTag();
        if (tag and localTag)
        {
            memcpy(localTag, tag, TAG_SIZE);
            return true;
        }

        return false;
    }

    unsigned char * getIV() { return this->iv; }

    const unsigned char * getIV() const { return this->iv; }

    bool writeIV(const unsigned char * iv)
    {
        unsigned char * localIV = this->getIV();

        if (localIV and iv)
        {
            memcpy(localIV, iv, IV_SIZE);
            return true;
        }

        return false;
    }

    void freeIV()
    {
        unsigned char * iv = this->getIV();

        if (iv)
        {
            memset(iv, 0, IV_SIZE);
            delete[] iv;
            this->iv = nullptr;
        }
    }

    bool allocateIV()
    {
        if (not this->getIV())
        {
            this->iv = new unsigned char[IV_SIZE + 1];
            return this->getIV() != nullptr;
        }

        return true;
    }

    bool generateIV()
    {
        const unsigned char * randomData = RandomDataGenerator::generate(IV_SIZE);
        bool ok = this->writeIV(randomData);
        delete[] randomData;

        return ok;
    }

    void freeTag()
    {
        unsigned char * tag = this->getTag();

        if (tag)
        {
            memset(tag, 0, TAG_SIZE);
            delete[] tag;
            this->tag = nullptr;
        }
    }

    bool allocateTag()
    {
        if (not this->getTag())
        {
            this->tag = new unsigned char[TAG_SIZE + 1];

            return this->getTag() != nullptr;
        }

        return true;
    }

public:

    EvpCipherContext(Key *key) : EvpContext(key)
    {
        this->cipherContext = nullptr;
        this->iv = nullptr;
        this->tag = nullptr;
    }

    ~EvpCipherContext()
    {
        this->freeCipherContext();
        this->freeIV();
        this->freeTag();
    }

    void cleanup() override
    {
        EvpContext::cleanup();

        this->freeCipherContext();
        this->freeIV();
        this->freeTag();
    }
};

#endif
