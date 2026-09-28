#!/bin/bash
# ==============================================================================
# Rewardly - Release APK Build Script
# This script generates a signed release APK ready for installation on Android.
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

KEYSTORE_FILE="${KEYSTORE_PATH:-$SCRIPT_DIR/my-upload-key.jks}"
KEY_ALIAS="upload"
DEFAULT_PASSWORD="rewardlypass123"

STORE_PASS="${STORE_PASSWORD:-$DEFAULT_PASSWORD}"
KEY_PASS="${KEY_PASSWORD:-$DEFAULT_PASSWORD}"

echo "=========================================================="
echo "  Rewardly - Building Release APK for Android Devices    "
echo "=========================================================="

# 1. Check or generate release keystore
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo ">> Release keystore not found. Generating new keystore: $KEYSTORE_FILE ..."
    keytool -genkeypair -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$STORE_PASS" \
        -keypass "$KEY_PASS" \
        -dname "CN=Rewardly, OU=Mobile, O=Rewardly, L=San Francisco, ST=CA, C=US"
    echo ">> Keystore successfully generated!"
else
    echo ">> Using existing keystore: $KEYSTORE_FILE"
fi

# 2. Determine Gradle executable
if [ -f "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
elif command -v gradle >/dev/null 2>&1; then
    GRADLE_CMD="gradle"
else
    echo "Error: Neither ./gradlew nor system gradle was found."
    exit 1
fi

echo ">> Running Gradle build (:app:assembleRelease)..."
export KEYSTORE_PATH="$KEYSTORE_FILE"
export STORE_PASSWORD="$STORE_PASS"
export KEY_PASSWORD="$KEY_PASS"

$GRADLE_CMD :app:assembleRelease

RELEASE_APK="$SCRIPT_DIR/app/build/outputs/apk/release/app-release.apk"

if [ -f "$RELEASE_APK" ]; then
    echo "=========================================================="
    echo "  SUCCESS! Release APK generated successfully:           "
    echo "  $RELEASE_APK"
    echo "=========================================================="
    ls -lh "$RELEASE_APK"
    echo ""
    echo "----------------------------------------------------------"
    echo "Installation Instructions for Android Device:"
    echo "----------------------------------------------------------"
    echo "Option 1 (Via USB & ADB):"
    echo "  1. Connect your Android device via USB and enable USB Debugging."
    echo "  2. Run: adb install -r \"$RELEASE_APK\""
    echo ""
    echo "Option 2 (Direct File Transfer to Phone):"
    echo "  1. Copy 'app-release.apk' to your phone (via USB, Google Drive, or messaging)."
    echo "  2. Open the Files / Downloads app on your Android device."
    echo "  3. Tap 'app-release.apk' and select 'Install'."
    echo "  (If prompted, allow 'Install unknown apps' for your file manager)."
    echo "=========================================================="
else
    echo "Error: Release APK was not found at expected path: $RELEASE_APK"
    exit 1
fi
