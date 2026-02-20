#!/bin/bash
# Quick build script for CCO app

set -e

echo "🔨 Building CCO App..."

cd "$(dirname "$0")/.."

if [ -f "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
elif command -v gradle >/dev/null 2>&1; then
    GRADLE_CMD="gradle"
else
    echo "❌ Gradle not found. Add ./gradlew to app/ or install 'gradle'."
    exit 1
fi

# Clean build (optional, comment out for faster builds)
# ./gradlew clean

# Build debug APK
$GRADLE_CMD assembleDebug

# Find the APK
APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)

if [ -n "$APK_PATH" ]; then
    echo "✅ Build successful!"
    echo "📦 APK: $APK_PATH"
    
    # Show APK size
    SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo "📊 Size: $SIZE"
    
    # Offer to install if device connected
    if command -v adb >/dev/null 2>&1 && adb devices | grep -q "device$"; then
        echo ""
        read -p "📱 Install to device? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            adb install -r "$APK_PATH"
            echo "✅ Installed!"
        fi
    fi
else
    echo "❌ APK not found!"
    exit 1
fi
