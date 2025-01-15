#ifndef INVALID_KEY_HH
#define INVALID_KEY_HH

#include <exception>
#include <string>

#include "ErrorMessages.hh"

class InvalidKey : public std::exception
{
    std::string message;

public:
    InvalidKey(const std::string &message) : message(message) {}

    const char *what() const noexcept { return message.c_str(); };
};

#endif
