#ifndef ENCRYPTION_MACHINE_HH
#define ENCRYPTION_MACHINE_HH

#include "CryptoMachine.hh"

class EncryptionMachine : public CryptoMachine
{
    EncryptionMachine(const EncryptionMachine &);
    const EncryptionMachine &operator=(const EncryptionMachine &);
    
public:
    EncryptionMachine(EvpContext *cipher) : CryptoMachine(cipher) {}

    bool run() override
    {
        EncrypterResult *result = this->getCipher()->encrypt(this->getIn());
        this->freeOut();
        this->setOut(result);
        return not result->isError();
    }

    static CryptoMachine *create(EvpContext *cipher)
    {
        return new EncryptionMachine(cipher);
    }
};

#endif
