SCRIPT_PATH=$(dirname "$(realpath "$0")")
OPENSSL_DIR="openssl"
CPU_CORES=$(nproc)

cd "$SCRIPT_PATH/$OPENSSL_DIR" || exit

for architecture in 'android-arm' 'android-arm64' 'android-x86' 'android-x86_64'
do
  ./Configure $architecture -D__ANDROID_API__=26 no-ssl shared no-tests
  make clean
  make -j"$CPU_CORES"

  if [ $architecture = 'android-arm' ]
    then
      mkdir -p ../../../../libs/armeabi-v7a
      cp ./libcrypto.a ../../../../libs/armeabi-v7a
    elif [ $architecture = 'android-arm64' ]
    then
      mkdir -p ../../../../libs/arm64-v8a
      cp ./libcrypto.a ../../../../libs/arm64-v8a
    elif [ $architecture = 'android-x86' ]
    then
      mkdir -p ../../../../libs/x86
      cp ./libcrypto.a ../../../../libs/x86
    elif [ $architecture = 'android-x86_64' ]
    then
      mkdir -p ../../../../libs/x86_64
      cp ./libcrypto.a ../../../../libs/x86_64
    fi
done
