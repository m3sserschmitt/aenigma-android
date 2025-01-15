#ifndef ONION_PARSING_HH
#define ONION_PARSING_HH

#include "CryptoContext.hh"

extern "C"
{
    unsigned int DecodeOnionSize(const unsigned char *onion);

    const unsigned char *UnsealOnion(CryptoContext *ctx, const unsigned char *onion, int &plaintextLen);
}

#endif
