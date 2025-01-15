#ifndef ENCRYPTER_DATA_HH
#define ENCRYPTER_DATA_HH

#include <cstring>

class EncrypterData
{
    unsigned char *data;
    unsigned int datalen;

    EncrypterData(const EncrypterData &);
    const EncrypterData &operator=(const EncrypterData &);

public:
    EncrypterData(const unsigned char *data, unsigned int datalen)
    {
        this->datalen = datalen;
        this->data = new unsigned char[datalen + 1];
        if (data)
        {
            memcpy(this->data, data, datalen);
        }
    }

    virtual ~EncrypterData()
    {
        if (this->data)
        {
            memset(this->data, 0, datalen);
        }

        delete[] this->data;
        this->data = nullptr;
    }

    unsigned int getDataSize() const
    {
        return this->datalen;
    }

    const unsigned char *getData() const
    {
        return this->data;
    }

    virtual bool isError() const { return false; }
};

#endif
