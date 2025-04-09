cmake_minimum_required(VERSION 3.10.2)
project("cryptography-wrapper" VERSION 1.0.0)

set(LIBS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs)

add_library(crypto STATIC IMPORTED)
set_target_properties(crypto PROPERTIES IMPORTED_LOCATION ${LIBS_DIR}/${ANDROID_ABI}/libcrypto.a)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/openssl/include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/aenigma7/include)

add_subdirectory(aenigma7)

add_library(aenigma-wrapper SHARED libaenigma-wrapper.cc)
target_link_libraries(aenigma-wrapper aenigma)
