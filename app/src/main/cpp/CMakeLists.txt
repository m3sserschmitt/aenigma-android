cmake_minimum_required(VERSION 3.10.2)
project("libaenigma-wrapper" VERSION 1.0.0)

set(LIBS_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../libs)
add_library(aenigma SHARED IMPORTED)
set_target_properties(aenigma PROPERTIES IMPORTED_LOCATION ${LIBS_DIR}/${ANDROID_ABI}/libaenigma.so)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

add_library(aenigma-wrapper SHARED libaenigma-wrapper.cc)
target_link_libraries(aenigma-wrapper aenigma)
