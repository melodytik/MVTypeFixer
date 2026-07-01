#!/bin/bash
# ============================================================
#  MVTypeFixer - Build Script
#  Compiles the plugin using local Bukkit API stubs.
#  No external Maven/Gradle repository needed.
# ============================================================

set -e
cd "$(dirname "$0")"

echo "=== MVTypeFixer Build ==="

# Step 1: Compile Bukkit API stubs
echo "[1/5] Compiling API stubs..."
rm -rf build
mkdir -p build/stubs build/classes
javac --release 8 -encoding UTF-8 -d build/stubs \
    $(find stubs -name "*.java")

# Step 2: Package stubs into a jar (classpath for plugin compilation)
echo "[2/5] Creating stub jar..."
jar cf build/bukkit-stubs.jar -C build/stubs .

# Step 3: Compile the plugin
echo "[3/5] Compiling plugin..."
javac --release 8 -encoding UTF-8 -d build/classes \
    -cp build/bukkit-stubs.jar \
    src/main/java/me/mvtypefixer/MVTypeFixer.java

# Step 4: Copy resources
echo "[4/5] Copying resources..."
cp src/main/resources/plugin.yml build/classes/
cp src/main/resources/config.yml build/classes/

# Step 5: Package final jar
echo "[5/5] Creating jar..."
cd build/classes
jar cf "../../MVTypeFixer-1.0.0.jar" \
    me/mvtypefixer/MVTypeFixer.class plugin.yml config.yml
cd ../..

echo ""
echo "=== Build successful! ==="
echo "Output: MVTypeFixer-1.0.0.jar ($(wc -c < MVTypeFixer-1.0.0.jar) bytes)"
echo ""
echo "Drop this jar into your server's plugins/ folder."
