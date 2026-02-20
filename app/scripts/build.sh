#!/bin/bash
# Quick build script for CCO app

set -e

echo "🔨 Building CCO App..."

cd "$(dirname "$0")/.."

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Run from app directory."
    exit 1
fi

# Clean build (optional, comment out for faster builds)
# ./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Find the APK
APK_PATH=$(find app/app/build/outputs/apk/debug -name "*.apk" 2>/dev/null | head -1)

if [ -n "$APK_PATH" ]; then
    echo "✅ Build successful!"
    echo "📦 APK: $APK_PATH"
    
    # Show APK size
    SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo "📊 Size: $SIZE"
    
    # Offer to install if device connected
    if adb devices | grep -q "device$"; then
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
