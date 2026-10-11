# Vibe Coder - Release Setup Guide

## Quick Start

### 1. Push to GitHub
```bash
cd /root/VibeCoder
git init
git add .
git commit -m "feat: initial Vibe Coder implementation"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/VibeCoder.git
git push -u origin main
```

### 2. Configure Secrets for Release Builds
Go to **GitHub Repo → Settings → Secrets and variables → Actions → New repository secret**:

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_PATH` | `keystore/release.keystore` (relative path in repo) |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |

**To generate a keystore:**
```bash
keytool -genkeypair \
  -alias vibecoder \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -keystore keystore/release.keystore \
  -storepass YOUR_STORE_PASSWORD \
  -keypass YOUR_KEY_PASSWORD \
  -dname "CN=Vibe Coder, OU=Development, O=VibeCoder, L=City, ST=State, C=US"
```

Then commit the keystore (or store it securely and reference in secrets):
```bash
mkdir -p keystore
# Copy your release.keystore to keystore/
git add keystore/release.keystore
git commit -m "chore: add release keystore"
git push
```

### 3. Create a Release
```bash
# Tag a version
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

This triggers the GitHub Action which:
1. Builds debug APK (always)
2. Builds release APK (if keystore secrets configured)
3. Creates a GitHub Release with both APKs attached

### 4. Download APKs
- **Debug**: Available on every push (Actions → Artifacts)
- **Release**: Available on tagged releases (Releases page)

---

## Build Locally

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (requires keystore)
```bash
# Set env vars or use local.properties
export KEYSTORE_PATH=keystore/release.keystore
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=vibecoder
export KEY_PASSWORD=your_password

./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Install on Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Requirements for Full Functionality

### PRoot Runtime (ARM64 only)
The app needs these binaries in `runtime/src/main/jniLibs/arm64-v8a/`:
- `libproot.so` - PRoot binary
- Alpine rootfs tarball (or implement downloader)

### Fonts
Add to `design-system/src/main/res/font/`:
- `inter_*.ttf` (Thin, ExtraLight, Light, Regular, Medium, SemiBold, Bold, ExtraBold, Black)
- `jetbrains_mono_*.ttf` (Thin, ExtraLight, Light, Regular, Medium, SemiBold, Bold, ExtraBold)

### Minimum Device Requirements
- Android 9+ (API 28)
- ARM64 architecture
- 2GB+ RAM recommended
- 1GB+ free storage

---

## F-Droid Distribution

For F-Droid, you'll need:
1. Reproducible builds (fixed timestamps, no secrets in repo)
2. All dependencies from F-Droid repos
3. No proprietary blobs (PRoot binary must be built from source)
4. Submit metadata to `fdroiddata` repo

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `NDK not found` | Install NDK r27c via SDK Manager |
| `Keystore not found` | Check `KEYSTORE_PATH` secret matches repo path |
| `PRoot fails` | Ensure device is ARM64, check SELinux policies |
| `Out of memory` | Increase Gradle heap: `org.gradle.jvmargs=-Xmx4g` |