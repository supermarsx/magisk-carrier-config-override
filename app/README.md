# Android App

This repository's Android app module is `app/app`.

## Canonical Module

- Gradle module: `:app`
- Module directory: `app/app`
- Current app id / namespace: `com.supermarsx.carrierconfig`
- Primary UI entrypoint: `app/app/src/main/java/dev/mars/carrierconfig/ui/MainActivity.kt`

## Source Layout

```text
app/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/dev/mars/carrierconfig/           # canonical entrypoints + nav + instrumentation
│       └── java/com/supermarsx/carrierconfig/      # shared repositories/components under migration
└── scripts/
    ├── dev.sh
    ├── build.sh
    ├── test.sh
    └── lint.sh
```

## Notes

- The app codebase is mid-migration from `com.supermarx` to `dev.mars`.
- `dev.mars` is the active direction; `com.supermarx` contains shared internals still referenced by current screens.
- Legacy `com.svtt` app sources were removed.

## Diagnostics Export Path

`/sdcard/Android/data/com.supermarsx.carrierconfig/files/cco_reports/`
