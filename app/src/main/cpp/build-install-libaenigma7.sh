#!/bin/bash

# Aenigma - Federal messaging system
# Copyright © 2024-2025 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

# This file is part of Aenigma project.

# Aenigma is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# Aenigma is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.

set -Eeuo pipefail

SCRIPT_PATH=$(dirname "$(realpath "$0")")

$SCRIPT_PATH/aenigma7/build-openssl-android.sh
$SCRIPT_PATH/aenigma7/build-android.sh

cp -v $SCRIPT_PATH/aenigma7/build-android-arm64-v8a/libaenigma.so $SCRIPT_PATH/../../../libs/arm64-v8a
cp -v $SCRIPT_PATH/aenigma7/build-android-armeabi-v7a/libaenigma.so $SCRIPT_PATH/../../../libs/armeabi-v7a
cp -v $SCRIPT_PATH/aenigma7/build-android-x86/libaenigma.so $SCRIPT_PATH/../../../libs/x86
cp -v $SCRIPT_PATH/aenigma7/build-android-x86_64/libaenigma.so $SCRIPT_PATH/../../../libs/x86_64

cp -v $SCRIPT_PATH/aenigma7/openssl-android/arm64-v8a/lib/libcrypto.so $SCRIPT_PATH/../../../libs/arm64-v8a
cp -v $SCRIPT_PATH/aenigma7/openssl-android/armeabi-v7a/lib/libcrypto.so $SCRIPT_PATH/../../../libs/armeabi-v7a
cp -v $SCRIPT_PATH/aenigma7/openssl-android/x86/lib/libcrypto.so $SCRIPT_PATH/../../../libs/x86
cp -v $SCRIPT_PATH/aenigma7/openssl-android/x86_64/lib/libcrypto.so $SCRIPT_PATH/../../../libs/x86_64
