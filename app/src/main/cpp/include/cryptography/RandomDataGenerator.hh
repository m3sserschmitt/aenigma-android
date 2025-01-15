#ifndef RANDOM_DATA_GENERATOR_HH
#define RANDOM_DATA_GENERATOR_HH

#include "Constants.hh"
#include <random>

class RandomDataGenerator
{
public:
    static unsigned char * generate(unsigned int len)
    {
        std::random_device dev;
        std::mt19937 rng(dev());
        std::uniform_int_distribution<int> dist(0, 0xff);

        unsigned char * data = new unsigned char[len + 1];

        for (unsigned int i = 0; i < len; i++)
        {
            data[i] = dist(rng);
        }

        return data;
    }

    static unsigned char * generateKey()
    {
        return generate(SYMMETRIC_KEY_SIZE);
    }
};

#endif
