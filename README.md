## Aenigma - Private Messaging

![Status](https://img.shields.io/badge/status-active-brightgreen)
![Platform](https://img.shields.io/badge/platform-android-green)
![OpenSSL](https://img.shields.io/badge/openssl-%3E%3D%203.5-blue)

### Overview

**Aenigma** is a free and open-source Android application focused on secure,
private communication through end-to-end encryption based on
public key cryptography.

- Anonymous sign-up with no personal information required
- No data collection or processing
- Built with transparency, privacy, and user autonomy in mind

<a href="https://play.google.com/store/apps/details?id=ro.aenigma">
  <img alt="Get it on Google Play"
  src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
  width="150">
</a>

### Prerequisites

> __*Note*__: These instructions are intended for Debian/Ubuntu distributions.

> __*Note*__: Relative paths in the following instructions are relative to local
> repository root path.

Before building the application please ensure you have
[Android Studio](https://developer.android.com/studio) installed alongside Android
Emulator or a physical Android device to test. Check `app/build.gradle` for current
`targetSdk` and `ndkVersion` then make sure to install required SDK Platform and SDK
Tools via `Sdk Manager`: `Android Sdk Build-Tools`, `NDK (Side by side)`, `CMake`,
`Android Emulator`, `Android Sdk Platform-Tools`.

### Build

#### Step 1: Get the source code

Clone the repository using `git`:

```bash
git clone https://github.com/m3sserschmitt/aenigma-android.git --recursive
```

#### Step 2 (Optional): build OpenSSL

This application relies on a prebuilt version of [OpenSSL](https://www.openssl.org/)
library. A pre-built version of OpenSSL is included directly in the project
repository for convenience. However, if you prefer to build the OpenSSL library
yourself the entire process is automated by `build-install-libaenigma7.sh` script
located in `app/src/main/cpp` directory.

```bash
export ANDROID_NDK_ROOT=<ndk-path>
export PATH=<ndk-path>/toolchains/llvm/prebuilt/<system>/bin:$PATH
./app/src/main/cpp/build-install-libaenigma7.sh
```

> __*Note*__: In the previous commands `<ndk-path>` is the path of your installed
> Android NDK and `<system>` is your local machine OS: `linux-x86_64`, `darwin-x86_64`,
> `windows-x86_64`, `windows` etc.

#### Step 3: Build the app

The simplest way to build is to trigger the build directly from Android Studio.
Alternatively, `gradle` can be invoked using the following:

```bash
export ANDROID_HOME=<sdk-path>
./gradlew :app:assembleDebug        # generate debug build or
./gradlew :app:assembleRelease      # to generate release (unsigned) build
```

> __*Note*__: In the previous commands `<sdk-path>` represents the the path of your
> installed Android SDK.

> __*Note*__: Alternatively, you can set the `sdk.dir=/path/to/installed/android/sdk`
> in your project's local properties file at `local.properties`.
### License

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](./LICENSE)

This project is licensed under the GNU General Public License v3.0. See the
[LICENSE](./LICENSE) file for details.

### Contact

You can report errors or suggest improvements at [contact@aenigma.ro](mailto:contact@aenigma.ro)
