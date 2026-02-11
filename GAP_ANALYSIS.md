# Gap Analysis: Spec vs Implementation

**Project**: CarrierConfig Override Manager (CCO)
**Date**: February 11, 2026
**Spec Documents**: spec-1.md, spec-design.md

---

## Source Tree Status

Legacy source trees (SVTT at `app/src/`, dev.mars at `app/app/src/main/java/dev/mars/`) have been **deleted**. All code now lives in the single active tree:

- **supermarsx** at `app/app/src/main/java/com/supermarsx/carrierconfig/` — 37 files, compiled by Gradle

The supermarsx tree contains equivalent or superior implementations for all functionality that previously existed in the legacy trees, including consolidated repositories (DeviceRepository, CarrierConfigRepository) that replace the per-domain repositories (ImsStatusRepository, WfcUiStatusRepository, BlockerDetectionService, PresetRepository, OverrideXmlBuilder, MagiskRepository, etc.).

---

## Component-by-Component Gaps

### 1. Magisk Module — 98% Complete

| Requirement | Status | Detail |
|---|---|---|
| module.prop | ✅ | Correct metadata |
| service.sh bind-mount (4 paths) | ✅ | All 4 Samsung paths |
| post-fs-data.sh + SELinux | ✅ | restorecon implemented |
| uninstall.sh restoration | ✅ | Unmounts + restores backups |
| install.sh validation | ✅ | Magisk/device/path checks |
| common/functions.sh | ✅ | Logging, path detection, safe_bind_mount |
| 3 Profile XMLs | ✅ | generic, aggressive, wifi_only |
| Disable flag | ✅ | `/data/adb/cco/disable` |
| Logging | ✅ | Timestamped to module.log |
| **Backup to `/data/adb/cco/overrides/<profile>.xml`** | ⚠️ Gap | Directory created but not populated by module — relies on app layer |

### 2. Instrumentation (Frida + LSPosed) — ~95% Complete

| Requirement | Status | Detail |
|---|---|---|
| Frida backend (hooks, agent, RPC) | ✅ | 8 JS files, ~2,400 LOC |
| LSPosed/Xposed backend | ✅ | 5 hook classes, ~1,155 LOC |
| Entitlement hooks (isWfcEntitled, isVoWiFiEnabled, entitlementStatus) | ✅ | Both backends |
| CarrierConfig gating hooks | ✅ | Both backends |
| Settings rendering hooks | ✅ | Both backends |
| Session control (start/stop/live) | ✅ | EventRecorder + IPC |
| Record & replay with JSON schema | ✅ | recording.js state machine |
| 8 hook profiles (oneui4/5/6 + carriers) | ✅ | profiles.json |
| **Version-specific IMS profiles** (e.g. `oneui6.1_imsservice_vX`) | ❌ Gap | Spec calls for this naming; not implemented |
| **Runtime package enumeration** for IMS components | ❌ Gap | Hardcoded targets only — no scanning of `/system/app/`, `/system/priv-app/` |
| **Method signature discovery/validation** | ❌ Gap | No reflection check before hook install — hooks silently fail if signatures change |
| **Dynamic carrier plugin detection** | ⚠️ Gap | T-Mobile/AT&T/Verizon hardcoded — no wildcard/dynamic discovery |

### 3. Android App — Key Gaps

#### Screens (supermarsx at `app/app/`)

| Screen | Files Exist? | Spec Section |
|---|---|---|
| Dashboard | ✅ DashboardScreen.kt + ViewModel | 4.1 |
| CarrierConfig (Method 1) | ✅ CarrierConfigScreen.kt + ViewModel | 4.2 |
| Entitlement (Method 2) | ✅ EntitlementScreen.kt (no ViewModel) | 4.3 |
| Diagnostics | ✅ DiagnosticsScreen.kt + ViewModel | 4.4 |
| Settings | ✅ SettingsScreen.kt + ViewModel + LicensesScreen | — |
| About | ✅ AboutScreen.kt | — |

#### Data layer gaps

| Requirement | Status | Detail |
|---|---|---|
| 6 presets | ✅ | All named presets in CarrierConfigRepository |
| All CarrierConfig keys | ✅ | Boolean, int, string keys present |
| **STRING_ARRAY XML generation** | ⚠️ Gap | Array values not fully implemented in XML generation |
| **IMSI/ICCID redaction** | ⚠️ Partial | ICCID redacted (last 4 digits) in DeviceRepository; IMSI not explicitly handled |
| Phone number privacy | ✅ | Intentionally never retrieved |
| JSON + text export | ✅ | ExportRepository with JSON + text formats |
| **Per-SIM-slot override** | ⚠️ Gap | Spec mentions it; not visible in Method 1 UI |

### 4. Security & Privacy — Gaps

| Requirement | Status | Detail |
|---|---|---|
| No network calls | ✅ | Offline-only (except UpdateChecker) |
| Phone number redaction | ✅ | Never collected |
| ICCID redaction | ✅ | DeviceRepository shows last 4 digits only |
| **IMSI redaction** | ⚠️ Gap | Not explicitly handled in app exports |
| **Build fingerprint redaction** | ❌ Gap | Exported verbatim |
| **Logcat PII filtering** | ❌ Gap | No scrubbing before export |
| Frida event sanitization | ✅ | utils.js has sanitize() for IMSI/IMEI |

---

## Priority Gaps

### High Priority (spec violations)

1. ~~**Reconcile/consolidate two source trees**~~ — RESOLVED: legacy trees deleted, single supermarsx tree is authoritative
2. **PII redaction** (IMSI) in app exports
3. **STRING_ARRAY XML generation** incomplete
4. **Logcat PII filtering** before export

### Medium Priority (spec gaps)

5. **Runtime IMS package detection** (currently hardcoded)
6. **Version-specific hook profiles** (`oneui6.1_imsservice_vX`)
7. **Per-SIM-slot override** in Method 1 UI
8. **Method signature validation** before hook installation

### Low Priority (polish)

9. Profile backup to `/data/adb/cco/overrides/<profile>.xml` from module
10. Dynamic carrier plugin detection
11. Build fingerprint redaction in exports
