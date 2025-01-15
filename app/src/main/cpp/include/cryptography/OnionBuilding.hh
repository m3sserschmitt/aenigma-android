#ifndef ONION_BUILDING_HH
#define ONION_BUILDING_HH

extern "C" const unsigned char *SealOnion(const unsigned char *plaintext, unsigned int plaintextLen, const char **keys, const char **addresses, unsigned int count, int &outLen);

#endif
