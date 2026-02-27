# Android App

This repository's Android app module is `app/app`.

## Canonical Module

- Gradle module: `:app`
- Module directory: `app/app`
- Current app id / namespace: `com.supermarsx.carrierconfig`
- Primary UI entrypoint: `app/app/src/main/java/com/supermarsx/carrierconfig/ui/MainActivity.kt`

## Source Layout

```text
app/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/supermarsx/carrierconfig/      # canonical app sources
└── scripts/
    ├── dev.sh
    ├── build.sh
    ├── test.sh
    └── lint.sh
```

## Notes

- The app codebase is now consolidated under `com.supermarsx.carrierconfig`.
- Legacy `dev.mars` and `com.svtt` trees were removed.

## Diagnostics Export Path

`/sdcard/Android/data/com.supermarsx.carrierconfig/files/cco_reports/`
