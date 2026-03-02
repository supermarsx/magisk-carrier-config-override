# CCO Documentation

## Getting Started

- **[Installation Guide](INSTALL.md)** — Complete setup for app, module, Frida, LSPosed, CLI
- **[Safety Guidelines](SAFETY.md)** — Critical safety information before use
- **[Troubleshooting](TROUBLESHOOTING.md)** — Common issues and solutions

## Guides

- **[Export/Import Guide](EXPORT_IMPORT_GUIDE.md)** — Settings, presets, hook profiles, diagnostic reports
- **[Testing Guide](TESTING.md)** — Test suite, running tests, coverage targets

## Technical Reference

- **[Technical Specification](spec-1.md)** — Requirements and implementation spec
- **[Design Specification](spec-design.md)** — Glassmorphism theme, colors, typography, components
- **[Spec Compliance & Gaps](SPEC_COMPLIANCE.md)** — Implementation status and remaining work
- **[Changelog](CHANGELOG.md)** — Version history

## Contributing

- **[Contributing Guide](../CONTRIBUTING.md)** — Dev setup, coding standards, PR process

## Component Documentation

| Component | Location |
| --- | --- |
| Android App | [app/README.md](../app/README.md), [app/scripts/README.md](../app/scripts/README.md) |
| Magisk Module | [module/README.md](../module/README.md), [module/docs/](../module/docs/) |
| CLI Tools | [cli/README.md](../cli/README.md) |
| Instrumentation | [instrumentation/README.md](../instrumentation/README.md) |
| LSPosed Module | [instrumentation/lsposed/README.md](../instrumentation/lsposed/README.md) |

## Quick References

- [Module Dev Scripts](../module/docs/SCRIPTS.md) — Module build, test, lint commands
- [Module Profiles](../module/docs/PROFILES.md) — Override profile documentation

## Documentation Structure

```text
docs/
├── README.md              # This file
├── INSTALL.md             # Installation instructions
├── SAFETY.md              # Safety guidelines
├── TROUBLESHOOTING.md     # Problem solving
├── TESTING.md             # Testing guide + quick reference
├── EXPORT_IMPORT_GUIDE.md # Export/import workflows
├── SPEC_COMPLIANCE.md     # Spec compliance & gap analysis
├── CHANGELOG.md           # Version history
├── CONTRIBUTING.md        # → Redirects to ../CONTRIBUTING.md
├── spec-1.md              # Technical specification
└── spec-design.md         # Design specification
```
