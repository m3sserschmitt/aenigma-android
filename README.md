# Aenigma – Secure and Anonymous Communication

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/platform-android-green)
![Status](https://img.shields.io/badge/status-active-brightgreen)
![OpenSSL](https://img.shields.io/badge/openssl-%3E%3D%203.0-blue)

## 📝 Overview

**Aenigma** is a free and open-source Android application focused on secure, private communication 
through **end-to-end encryption** using **public key cryptography**.

- Anonymous sign-up with **no personal information required**
- No data collection or processing
- Built with transparency, privacy, and user autonomy in mind


## 🚀 Getting Started

Follow the instructions below to clone, configure, and build the Aenigma project on your local machine.

### ✅ Prerequisites

Before building the application, ensure you have the following tools installed:

- [Android Studio](https://developer.android.com/studio)
- Android SDK
- Android NDK (Side by side)
- Android Emulator _or_ a physical Android device

> These instructions are tailored for **Ubuntu Linux** distributions but can be adapted for other platforms.

### 📥 Clone the Repository

Clone the project using Git:

```bash
git clone https://github.com/m3sserschmitt/aenigma-android.git --recursive
```

## 🔐 OpenSSL Integration & NDK Setup - [OPTIONAL]

This application statically links against the [OpenSSL](https://www.openssl.org/) cryptographic 
library. A **pre-built version of OpenSSL (≥ 3.0)** is included directly in the project repository 
for convenience.

If you prefer to build the OpenSSL library yourself, an **automation script** is available:

```bash
cd ./app/src/main/cpp
export ANDROID_NDK_ROOT=<sdk-path>/ndk/<ndk-version>
export PATH=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/<system>/bin:$PATH
./make-openssl.sh
```

>In the previous commands `<ndk-version>` is your installed NDK version,
>`<sdk-path>` is the path of your installed Android SDK and
>`<system>` is your local machine OS: `linux-x86_64`, `darwin-x86_64` `windows-x86_64` or `windows`.

After completing this step you are
ready to open the project into Android Studio IDE to build the application.

## 👤 Authors

* [Romulus-Emanuel Ruja](mailto:romulus-emanuel.ruja@tutanota.com)

## 📜 License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](./LICENSE)
file for details.
