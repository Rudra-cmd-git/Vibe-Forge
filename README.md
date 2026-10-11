# VibeForge

> Vibe code anything, right from your phone.

VibeForge is an Android app where anyone, even people who can't code, can
describe what they want and watch an AI agent build it on-device. No root,
no terminal, no setup headaches.

## Features
- **One-tap setup:** pick a model, paste your API key, start building
- **On-device Linux runtime:** Node, Python, git and a full web stack
  (HTML, CSS, JS, Tailwind, Vite, React) via PRoot
- **Agent built for big projects:** auto-maintained project memory,
  task lists and plan-first milestones
- **Safety net:** git auto-commit after every change, with one-tap undo
  and a version timeline
- **Self-healing:** the agent runs your app, reads errors and fixes them
- **Live preview:** see your app and the chat side by side
- **Bring your own model:** Anthropic, OpenRouter, DeepSeek or any
  OpenAI/Anthropic-compatible endpoint
- **Classy dark UI:** calm, minimal and built for phones

## Download
Grab the latest APK from [Releases](../../releases):
- **Online:** small download, fetches runtime bundles on first launch
- **Offline:** everything bundled, works without a connection (larger)

Requires Android 8.0+ and an ARM64 device.

## Getting started
1. Install the APK and open the app
2. Choose a provider and add your API key
3. Type what you want to build, for example "a portfolio website with a
   dark theme"
4. Watch it build, preview it live, and undo anything with one tap

## Build from source
```bash
git clone https://github.com/<your-username>/vibeforge.git
cd vibeforge
./gradlew assembleDebug
```

## Project structure
| Module | Purpose |
|---|---|
| `ui` | Compose screens, theme and components |
| `agent` | Agent loop, tools, project memory |
| `runtime` | PRoot and rootfs manager |
| `providers` | Model API integrations |
| `storage` | Projects, settings, key storage |
| `versioning` | Git auto-commit, undo timeline |

## Roadmap
- [ ] Design system and home screen
- [ ] Runtime setup
- [ ] Chat and first provider
- [ ] File tools and bash
- [ ] Live preview
- [ ] Undo timeline
- [ ] Self-healing loop
- [ ] Templates and Advanced tab

## Contributing
Issues and PRs are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Disclaimer
AI-generated code can be wrong. Review anything important, and keep
backups of projects you care about. Your API keys stay on your device.

## License
MIT. See [LICENSE](LICENSE).

## Credits
Inspired by [Mobile-Harness](https://github.com/techjarves/Mobile-Harness).
