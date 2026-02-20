# CarrierConfig Override Manager (CCO)

Toolkit for Samsung CarrierConfig override workflows, Magisk module deployment, diagnostics, and runtime instrumentation research (Frida/LSPosed).

## Repository Status

This repository currently contains multiple code paths from parallel development tracks:
- `app/src/main/...` (`com.svtt.*`) and `app/app/src/main/...` (`com.supermarx.*`, `dev.mars.*`)
- root-level docs with both current and historical milestone/session notes

Treat this as an active integration state, not a finalized release branch.

## Repository Layout

```text
.
├── app/                  Android application sources and scripts
├── module/               Magisk module scripts, profiles, and tests
├── instrumentation/      Frida + LSPosed tooling and profiles
├── cli/                  CLI utility (`ccoctl`)
├── docs/                 Project docs, guides, and specs
├── QUICKREF.md           User/developer quick reference
├── MODULE_DEV_QUICKREF.md
└── DOCS_MAP.md           Documentation entrypoint
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

- `DOCS_MAP.md`
- `docs/README.md`
- `docs/INSTALL.md`
- `docs/TROUBLESHOOTING.md`
- `docs/spec-design.md`
- `docs/spec-1.md`

## Contribution

Use `CONTRIBUTING.md` for branch, testing, and review expectations.
