#!/bin/bash

SCRIPT_PATH=$(dirname "$(realpath "$0")")
OPENSSL_VERSION="3.0.15"
OPENSSL_DIR="openssl-$OPENSSL_VERSION"
OPENSSL_SOURCE="$OPENSSL_DIR.tar.gz"
BIN="openssl-$OPENSSL_VERSION-bin"

if [ -e $OPENSSL_SOURCE ]; then
    echo "$OPENSSL_SOURCE already exists."
else
    echo "$OPENSSL_SOURCE does not exists; Starting download..."
    wget https://www.openssl.org/source/$OPENSSL_SOURCE
fi

tar -xf $OPENSSL_SOURCE

rm -rf $BIN
mkdir $BIN
mkdir $BIN/arm64-v8a
mkdir $BIN/armeabi-v7a
mkdir $BIN/x86
mkdir $BIN/x86_64

cd $OPENSSL_DIR

for architecture in 'android-arm' 'android-arm64' 'android-x86' 'android-x86_64'
do

if [ $architecture = 'android-arm' ]
    then
      ./Configure $architecture -D__ANDROID_API__=26 --prefix=$SCRIPT_PATH/$BIN/armeabi-v7a
    elif [ $architecture = 'android-arm64' ]
    then
        ./Configure $architecture -D__ANDROID_API__=26 --prefix=$SCRIPT_PATH/$BIN/arm64-v8a
    elif [ $architecture = 'android-x86' ]
    then
        ./Configure $architecture -D__ANDROID_API__=26 --prefix=$SCRIPT_PATH/$BIN/x86
    elif [ $architecture = 'android-x86_64' ]
    then
        ./Configure $architecture -D__ANDROID_API__=26 --prefix=$SCRIPT_PATH/$BIN/x86_64
    fi

    make clean
    make -j4
    make install_sw
done
