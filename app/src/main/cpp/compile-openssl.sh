#!/bin/bash

OPENSSL_DIR=openssl
OPENSSL_VERSION=openssl-3.0.14
BIN=openssl-bin

rm -rf $OPENSSL_DIR
rm -rf $OPENSSL_VERSION
rm -rf $BIN

rm $OPENSSL_VERSION.tar.gz

wget https://www.openssl.org/source/$OPENSSL_VERSION.tar.gz
tar -xf $OPENSSL_VERSION.tar.gz
mv $OPENSSL_VERSION $OPENSSL_DIR

mkdir $BIN
mkdir $BIN/arm64-v8a
mkdir $BIN/armeabi-v7a
mkdir $BIN/x86
mkdir $BIN/x86_64

cd $OPENSSL_DIR

for architecture in 'android-arm' 'android-arm64' 'android-x86' 'android-x86_64'
do

make clean
./Configure $architecture -D__ANDROID_API__=24
make

if [ $architecture = 'android-arm' ]
    then
        cp *.a *.so *.so.* ../$BIN/armeabi-v7a
    elif [ $architecture = 'android-arm64' ]
    then
        cp *.a *.so *.so.* ../$BIN/arm64-v8a
    elif [ $architecture = 'android-x86' ]
    then
        cp *.a *.so *.so.* ../$BIN/x86
    elif [ $architecture = 'android-x86_64' ]
    then
        cp *.a *.so *.so.* ../$BIN/x86_64
    fi
done