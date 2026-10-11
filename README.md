# Vibe Coder

A premium vibe-coding Android app where anyone can describe what they want and have an AI agent build it on their phone — including very large projects. No root required.

## Features

- **One-Tap Setup**: Pick a model/provider, paste API key, start building
- **PRoot Runtime**: Full Alpine Linux with Node, Python, Git, and web stack
- **AI Agent**: Tools for bash, file ops, search, web fetch with safety checks
- **Project Memory**: Auto-maintained PROJECT.md and TODO.md
- **Git Versioning**: Auto-commit, undo, version timeline
- **Self-Healing**: Agent fixes its own errors
- **Live Preview**: Draggable bottom sheet with device toggles
- **Multi-Provider**: Anthropic, OpenRouter, DeepSeek, Custom endpoints

## Architecture

```
VibeCoder/
├── app/                    # Main application module
├── design-system/          # Theme, components, design tokens
├── agent/                  # Agent loop, tools, memory
├── runtime/                # PRoot + Alpine rootfs
├── providers/              # AI model providers
├── storage/                # Preferences, Git, file ops
├── versioning/             # Git-based version timeline
└── preview/                # Live preview with WebView
```

## Design System

- **Colors**: Dark-first (#0B0B0F bg), one accent (#8B7CFF)
- **Typography**: Inter (UI) + JetBrains Mono (code)
- **Shapes**: 16-20dp radius, pill inputs, 1dp hairline borders
- **Motion**: Spring animations (200-300ms), shared transitions

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires keystore env vars)
./gradlew assembleRelease
```

## Requirements

- Android Studio Koala or later
- JDK 17
- Android SDK 34
- Device/emulator with ARM64 (for PRoot)

## License

MIT