#!/bin/bash
# Verification script for CCO rebranding

echo "=== CCO Rebranding Verification ==="
echo ""

# Check for remaining SVTT references
echo "1. Checking for remaining SVTT/svtt references in code..."
SVTT_COUNT=$(grep -r "SVTT\|svtt" \
  --include="*.kt" --include="*.xml" --include="*.gradle*" \
  --include="*.md" --include="*.py" --include="*.sh" \
  --include="*.js" --include="*.json" --include="*.yml" \
  --include="*.yaml" . 2>/dev/null | \
  grep -v ".git" | grep -v "Binary" | wc -l)

if [ "$SVTT_COUNT" -eq 0 ]; then
  echo "   ✅ No SVTT/svtt references found in code"
else
  echo "   ❌ Found $SVTT_COUNT SVTT/svtt references:"
  grep -r "SVTT\|svtt" \
    --include="*.kt" --include="*.xml" --include="*.gradle*" \
    --include="*.md" --include="*.py" --include="*.sh" \
    --include="*.js" --include="*.json" --include="*.yml" \
    --include="*.yaml" . 2>/dev/null | \
    grep -v ".git" | grep -v "Binary"
fi

echo ""

# Check for files with svtt in names
echo "2. Checking for files with svtt/SVTT in names..."
SVTT_FILES=$(find . -name "*svtt*" -o -name "*SVTT*" 2>/dev/null | grep -v ".git" | wc -l)

if [ "$SVTT_FILES" -eq 0 ]; then
  echo "   ✅ No files with svtt/SVTT in names"
else
  echo "   ❌ Found $SVTT_FILES files with svtt/SVTT in names:"
  find . -name "*svtt*" -o -name "*SVTT*" 2>/dev/null | grep -v ".git"
fi

echo ""

# Verify key files exist
echo "3. Verifying renamed files exist..."
FILES=(
  "cli/ccoctl"
  "app/app/src/main/java/dev/mars/carrierconfig/CCOApplication.kt"
  "app/app/src/main/java/dev/mars/carrierconfig/ui/navigation/CCONavHost.kt"
  "app/app/src/main/java/dev/mars/carrierconfig/ui/theme/Theme.kt"
)

for file in "${FILES[@]}"; do
  if [ -f "$file" ]; then
    echo "   ✅ $file"
  else
    echo "   ❌ Missing: $file"
  fi
done

echo ""

# Check package structure
echo "4. Verifying package structure..."
if [ -d "app/app/src/main/java/dev/mars/carrierconfig" ]; then
  echo "   ✅ Package com.supermarsx.carrierconfig exists"
  echo "   Files in package:"
  find app/app/src/main/java/dev/mars/carrierconfig -name "*.kt" | wc -l | xargs echo "     " "Kotlin files"
else
  echo "   ❌ Package com.supermarsx.carrierconfig not found"
fi

echo ""

# Check module configuration
echo "5. Verifying Magisk module configuration..."
if [ -f "module/module.prop" ]; then
  MODULE_ID=$(grep "^id=" module/module.prop | cut -d'=' -f2)
  MODULE_NAME=$(grep "^name=" module/module.prop | cut -d'=' -f2)
  echo "   ✅ Module ID: $MODULE_ID"
  echo "   ✅ Module Name: $MODULE_NAME"
else
  echo "   ❌ module/module.prop not found"
fi

echo ""

# Check Android app configuration
echo "6. Verifying Android app configuration..."
if [ -f "app/app/build.gradle.kts" ]; then
  NAMESPACE=$(grep "namespace" app/app/build.gradle.kts | head -1)
  APP_ID=$(grep "applicationId" app/app/build.gradle.kts | head -1)
  echo "   ✅ $NAMESPACE"
  echo "   ✅ $APP_ID"
else
  echo "   ❌ app/app/build.gradle.kts not found"
fi

echo ""
echo "=== Verification Complete ==="
