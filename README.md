## Getting Started

These instructions will get you a copy of the project up and running on your local machine.

### Prerequisites

Make sure to have Android Studio, Android SDK, Android NDK (Side by side) and Android Emulator (or
physical Android device) installed. The following instructions will guide you to setup the project
for Ubuntu Linux distribution.

### Building the source code

You need a copy of the source code. Clone the repository using git:

`git clone https://github.com/m3sserschmitt/aenigma-android.git --recursive`

This app statically links to cryptographic [OpenSSL](https://www.openssl.org/) library. First, we
need to download and compile this library. A shell script is included into this repository to
automate this process. Change the working directory to newly downloaded source code and then into
the `./app/src/main/cpp`. Run `./compile-openssl.sh` script. Make the script executable (if not
already) using command `sudo chmod +x ./compile-openssl.sh`. After completing this step you are
ready to open the project into Android Studio IDE to build the application.

## Authors

* **Romulus-Emanuel Ruja** <<romulus-emanuel.ruja@tutanota.com>>

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](./LICENSE)
file for details.
