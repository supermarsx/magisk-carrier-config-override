# CarrierConfig Override Manager (CCO)

Toolkit for Samsung CarrierConfig override workflows, Magisk module deployment, diagnostics, and runtime instrumentation research (Frida/LSPosed).

## Repository Status

This repository is consolidated on the `com.supermarsx` namespace:

- canonical app module at `app/app/src/main/...` under `com.supermarsx.carrierconfig`
- module/CLI/instrumentation paths standardized on `cco`

## Repository Layout

```text
.
├── app/                  Android application sources and scripts
├── module/               Magisk module scripts, profiles, and tests
├── instrumentation/      Frida + LSPosed tooling and profiles
├── cli/                  CLI utility (ccoctl)
├── docs/                 Project docs, guides, and specs
├── CONTRIBUTING.md       Contribution guide
└── license.md            License
```

## Quick Start

### Magisk Module Validation

```bash
cd module
./scripts/lint.sh
./scripts/test.sh
```

### Android App Work

There is currently no Gradle wrapper committed in the repo root. Use Android Studio/Gradle with the intended app project path for your branch.

## Key Documentation

- [docs/README.md](docs/README.md) — Documentation index
- [docs/INSTALL.md](docs/INSTALL.md) — Installation guide
- [docs/SAFETY.md](docs/SAFETY.md) — Safety guidelines
- [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) — Problem solving
- [docs/spec-1.md](docs/spec-1.md) — Technical specification
- [docs/spec-design.md](docs/spec-design.md) — Design specification
- [docs/SPEC_COMPLIANCE.md](docs/SPEC_COMPLIANCE.md) — Compliance & gap analysis

## Contribution

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch, testing, and review expectations.
