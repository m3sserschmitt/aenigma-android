#ifndef UTILS_HH
#define UTILS_HH

extern "C"
{
    int GetAesGcmCiphertextSize(unsigned int plaintext);

    int GetAesGcmPlaintextSize(unsigned int ciphertext);

    int GetEnvelopeSize(unsigned int plaintextLen, const char *publicKey);

    int GetOpenEnvelopeSize(unsigned int envelopeSize, const char *publicKey);

    int GetSignedDataSize(unsigned int dataSize, const char *publicKey);

    int GetPKeySize(const char *publicKey);
}

#endif
