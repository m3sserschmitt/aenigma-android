# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.10.2)
project("cryptography-wrapper" VERSION 1.0.0)

set(OPENSSL_VERSION 3.0.15)

add_library(crypto STATIC IMPORTED)
set_target_properties(crypto PROPERTIES
        IMPORTED_LOCATION "${CMAKE_SOURCE_DIR}/openssl-${OPENSSL_VERSION}-bin/${ANDROID_ABI}/lib/libcrypto.a")

add_library(ssl STATIC IMPORTED)
set_target_properties(ssl PROPERTIES
        IMPORTED_LOCATION "${CMAKE_SOURCE_DIR}/openssl-${OPENSSL_VERSION}-bin/${ANDROID_ABI}/lib/libssl.a")

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/openssl-${OPENSSL_VERSION}/include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/libaenigma7/include)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_subdirectory(libaenigma7)

add_library( # Sets the name of the library.
        libaenigma-wrapper
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        libaenigma-wrapper.cc)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.
        libaenigma-wrapper
        # Links the target library to the log library
        # included in the NDK.
        aenigma
        )