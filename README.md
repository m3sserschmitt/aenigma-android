## Getting Started

These instructions will get you a copy of the project up and running on your local machine.

### Prerequisites

Make sure to have Android Studio, Android SDK, Android NDK (Side by side) and Android Emulator (or
physical Android device) installed. The following instructions will guide you to setup the project
for Ubuntu Linux distribution.

### Building the source code

You need a copy of the source code. Clone the repository using git:

`git clone https://github.com/m3sserschmitt/aenigma-android.git --recursive`

This app statically links to cryptographic [OpenSSL](https://www.openssl.org/) library. We
need to download and compile this library.

Before that configure `ANDROID_NDK_ROOT` and `PATH`:

`export ANDROID_NDK_ROOT=<sdk-path>/ndk/<ndk-version>`

`PATH=PATH=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/<system>/bin:$PATH`

where,

`<ndk-version>` is your installed NDK version,

`<sdk-path>` is the path of your installed Android SDK and

`<system>` is your local machine OS: `linux-x86_64`, `darwin-x86_64`, `windows-x86_64` or `windows`.

Change the working directory to newly downloaded source code and then into
the `./app/src/main/cpp`. Run `./compile-openssl.sh` script. Make the script executable (if not
already) using command `sudo chmod +x ./compile-openssl.sh`. This script will download, extract and 
compile the source code for OpenSSL library for `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64`.
Resulted binaries are located into `openssl-<version>-bin` directory.

After completing this step you are
ready to open the project into Android Studio IDE to build the application.

## Authors

* **Romulus-Emanuel Ruja** <<romulus-emanuel.ruja@tutanota.com>>

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](./LICENSE)
file for details.
