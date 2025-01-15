#ifndef INVALID_OPERATION_HH
#define INVALID_OPERATION_HH

#include <exception>
#include <string>

#include "ErrorMessages.hh"

class InvalidOperation : public std::exception
{
    std::string message;

public:
    InvalidOperation(const std::string &message) : message(message) {}

    const char *what() const noexcept { return message.c_str(); };
};

#endif
