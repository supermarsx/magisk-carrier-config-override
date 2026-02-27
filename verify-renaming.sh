#!/usr/bin/env bash
# Verification script for CCO namespace/path migration consistency.

set -u

echo "=== CCO Rebranding Verification ==="
echo ""

FAILURES=0

echo "1. Checking for legacy namespace/path references in active code..."
LEGACY_PATTERN='com\.svtt|dev\.mars|com\.supermarx|/data/adb/svtt|svtt_reports'
LEGACY_HITS=""

if command -v rg >/dev/null 2>&1; then
  LEGACY_HITS=$(rg -n -S "$LEGACY_PATTERN" app/app/src module cli instrumentation app/app/build.gradle.kts app/app/src/main/AndroidManifest.xml 2>/dev/null || true)
else
  LEGACY_HITS=$(grep -REn "com\.svtt|dev\.mars|com\.supermarx|/data/adb/svtt|svtt_reports" app/app/src module cli instrumentation app/app/build.gradle.kts app/app/src/main/AndroidManifest.xml 2>/dev/null || true)
fi

if [ -z "$LEGACY_HITS" ]; then
  echo "   ✅ No legacy refs found in active code"
else
  echo "   ❌ Legacy refs found in active code:"
  echo "$LEGACY_HITS"
  FAILURES=$((FAILURES + 1))
fi

echo ""

echo "2. Checking for files with svtt/SVTT in names (active trees)..."
SVTT_FILES=$(find app module cli instrumentation -type f \( -iname "*svtt*" -o -iname "*SVTT*" \) 2>/dev/null || true)
if [ -z "$SVTT_FILES" ]; then
  echo "   ✅ No svtt/SVTT filenames found"
else
  echo "   ❌ Legacy filenames found:"
  echo "$SVTT_FILES"
  FAILURES=$((FAILURES + 1))
fi

echo ""

echo "3. Verifying key files exist..."
FILES=(
  "cli/ccoctl"
  "app/app/src/main/java/com/supermarsx/carrierconfig/CCOApplication.kt"
  "app/app/src/main/java/com/supermarsx/carrierconfig/ui/navigation/CCONavHost.kt"
  "app/app/src/main/java/com/supermarsx/carrierconfig/ui/theme/Theme.kt"
)

for file in "${FILES[@]}"; do
  if [ -f "$file" ]; then
    echo "   ✅ $file"
  else
    echo "   ❌ Missing: $file"
    FAILURES=$((FAILURES + 1))
  fi
done

echo ""

echo "4. Verifying package structure..."
PKG_DIR="app/app/src/main/java/com/supermarsx/carrierconfig"
if [ -d "$PKG_DIR" ]; then
  PKG_FILE_COUNT=$(find "$PKG_DIR" -name "*.kt" | wc -l | tr -d ' ')
  echo "   ✅ Package com.supermarsx.carrierconfig exists"
  echo "   ✅ Kotlin files: $PKG_FILE_COUNT"
else
  echo "   ❌ Package com.supermarsx.carrierconfig not found"
  FAILURES=$((FAILURES + 1))
fi

echo ""

echo "5. Verifying Magisk module configuration..."
if [ -f "module/module.prop" ]; then
  MODULE_ID=$(grep "^id=" module/module.prop | cut -d'=' -f2)
  MODULE_NAME=$(grep "^name=" module/module.prop | cut -d'=' -f2)
  echo "   ✅ Module ID: $MODULE_ID"
  echo "   ✅ Module Name: $MODULE_NAME"
else
  echo "   ❌ module/module.prop not found"
  FAILURES=$((FAILURES + 1))
fi

echo ""

echo "6. Verifying Android app configuration..."
if [ -f "app/app/build.gradle.kts" ]; then
  NAMESPACE=$(grep 'namespace *= *"com.supermarsx.carrierconfig"' app/app/build.gradle.kts || true)
  APP_ID=$(grep 'applicationId *= *"com.supermarsx.carrierconfig"' app/app/build.gradle.kts || true)
  if [ -n "$NAMESPACE" ] && [ -n "$APP_ID" ]; then
    echo "   ✅ namespace/applicationId set to com.supermarsx.carrierconfig"
  else
    echo "   ❌ namespace/applicationId mismatch in app/app/build.gradle.kts"
    FAILURES=$((FAILURES + 1))
  fi
else
  echo "   ❌ app/app/build.gradle.kts not found"
  FAILURES=$((FAILURES + 1))
fi

echo ""

echo "7. Verifying module data paths are cco..."
if command -v rg >/dev/null 2>&1; then
  CCO_PATHS=$(rg -n -S "/data/adb/cco" module || true)
  SVTT_PATHS=$(rg -n -S "/data/adb/svtt" module cli app/app/src/main instrumentation || true)
else
  CCO_PATHS=$(grep -REn "/data/adb/cco" module 2>/dev/null || true)
  SVTT_PATHS=$(grep -REn "/data/adb/svtt" module cli app/app/src/main instrumentation 2>/dev/null || true)
fi

if [ -n "$CCO_PATHS" ] && [ -z "$SVTT_PATHS" ]; then
  echo "   ✅ Active scripts use /data/adb/cco and no /data/adb/svtt"
else
  echo "   ❌ Path mismatch in active scripts"
  if [ -z "$CCO_PATHS" ]; then
    echo "      - No /data/adb/cco references found in module scripts"
  fi
  if [ -n "$SVTT_PATHS" ]; then
    echo "      - Found legacy /data/adb/svtt references:"
    echo "$SVTT_PATHS"
  fi
  FAILURES=$((FAILURES + 1))
fi

echo ""
if [ "$FAILURES" -eq 0 ]; then
  echo "=== Verification Complete: PASS ==="
  exit 0
fi

echo "=== Verification Complete: FAIL ($FAILURES issue(s)) ==="
exit 1
