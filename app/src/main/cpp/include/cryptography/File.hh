#ifndef FILE_HH
#define FILE_HH

#include <fstream>
#include <string>
#include <cstring>

class File
{
public:
    static const char *readFile(const char *path, unsigned int &len);
};

#endif