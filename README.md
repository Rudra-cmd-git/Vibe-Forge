# VibeCoder

An AI-powered coding app for Android where anyone, even non-coders, can describe what they want and an AI agent builds it on their phone, including very large projects.

## Features

### GitHub Integration (Top Priority)
- **OAuth Device Flow**: Connect GitHub without a backend server - show a code, open github.com/login/device, poll until approved
- **Personal Access Token Fallback**: Paste a classic PAT with repo scope
- **Secure Token Storage**: Tokens stored only in Android Keystore / EncryptedSharedPreferences, never logged or displayed
- **Repository Actions**: Create repos, clone existing, connect local folders, list user repositories
- **Git Operations**: Commit with AI-written messages, push, pull, fetch, branch management, merge, commit history
- **Auto-commit**: Automatic commits after every successful agent change with optional auto-push

### Safety & Clarity
- **Secret Scanning**: Before every push, scans for API keys, tokens, .env files, keystores, private keys - blocks push with plain explanation
- **Auto .gitignore**: Generates proper .gitignore for project type
- **Large File Warning**: Warns on files over 100 MB, offers to exclude or use Git LFS
- **Human-readable Errors**: Translates git errors into plain language

### AI Agent
- **Natural Language**: Describe what you want in plain English
- **Project Generation**: Creates complete project structure with all files
- **Iterative Development**: Add features, fix bugs, refactor through conversation
- **Multiple Languages**: Supports Kotlin, Java, JavaScript, TypeScript, Python, Go, Rust, Swift, Dart, C#, C++, and more

### Editor & Experience
- **Jetpack Compose UI**: Modern, responsive Material 3 design
- **Code Editor**: Syntax highlighting, line numbers, themes
- **Project Management**: Multiple projects, favorites, recent projects
- **Settings**: Theme, biometric lock, auto-commit/push, editor preferences, AI model config

## Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Hilt dependency injection
- **Database**: Room for local storage
- **Git**: JGit (pure Java, no native binary)
- **Networking**: Retrofit + OkHttp + Moshi
- **Security**: EncryptedSharedPreferences, Android Keystore
- **Background**: WorkManager for long-running tasks
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: ARM64 optimized

## Project Structure

```
VibeCoder/
├── app/
│   ├── src/main/
│   │   ├── java/com/vibecoder/
│   │   │   ├── agent/           # AI Agent implementation
│   │   │   ├── data/
│   │   │   │   ├── github/      # GitHub API & OAuth
│   │   │   │   ├── git/         # JGit operations
│   │   │   │   ├── secrets/     # Secret scanning
│   │   │   │   └── storage/     # Room DB & Preferences
│   │   │   ├── di/              # Hilt modules
│   │   │   ├── ui/
│   │   │   │   ├── editor/      # Code editor & AI chat
│   │   │   │   ├── github/      # GitHub screens
│   │   │   │   ├── main/        # Main activity & home
│   │   │   │   ├── projects/    # Project list
│   │   │   │   └── settings/    # Settings screen
│   │   │   └── util/            # Utilities
│   │   └── res/                 # Resources (layouts, values, drawables)
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/wrapper/
├── gradlew
└── gradlew.bat
```

## Building

### Prerequisites
- Android Studio Ladybug (2024.2.1) or later
- JDK 17
- Android SDK 34

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Run tests
./gradlew test

# Install debug APK
./gradlew installDebug
```

## Configuration

### GitHub OAuth App
Create a GitHub OAuth App at https://github.com/settings/developers with:
- **Homepage URL**: `https://vibecoder.app`
- **Authorization callback URL**: `vibecoder://callback`

Add the Client ID to `GitHubRepository.kt`:
```kotlin
private val clientId = "YOUR_CLIENT_ID"
```

### Signing Config (Release)
Set environment variables:
```bash
export KEYSTORE_PATH=/path/to/keystore.jks
export KEYSTORE_PASSWORD=your_keystore_password
export KEY_ALIAS=your_key_alias
export KEY_PASSWORD=your_key_password
```

## Security

- All sensitive data (GitHub tokens, API keys) encrypted with Android Keystore
- No data sent to third parties except GitHub API
- Local-first architecture - projects stored on device
- Biometric authentication option for app access

## License

MIT License - see LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and linting
5. Submit a pull request

## Roadmap

- [ ] Full LLM integration (Anthropic, OpenAI, Google)
- [ ] Proot-based Linux environment for running code
- [ ] Real-time collaboration
- [ ] Plugin system for custom tools
- [ ] Web version
- [ ] iOS version