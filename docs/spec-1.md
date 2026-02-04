# magisk-carrier-config-override

A toolkit to **control Wi‑Fi Calling (VoWiFi), VoLTE, IMS and related CarrierConfig behaviour** on Samsung devices by (1) applying **CarrierConfig overrides** and (2) performing **runtime entitlement simulation** via instrumentation.

---

## 0) Goals

### Primary goals

- Make Samsung **Wi‑Fi Calling settings UI populate** when it is otherwise gated.
- Control app/OS integration paths:
  - CarrierConfig‑controlled feature exposure
  - IMS registration state handling
  - Entitlement‑dependent toggles
- Provide repeatable, reversible configuration profiles for:
  - SIM1 / SIM2
  - roaming vs non‑roaming
  - Wi‑Fi preferred / Cellular preferred / Wi‑Fi only modes

### Out of scope

- Emergency calling guarantees
- Carrier billing or provisioning systems

---

## 1) Supported Approaches

### Method 1 — CarrierConfig Override (Magisk)

Applies an override config (boolean/int/string keys) used by `CarrierConfigManager` so the OS **exposes WFC controls** and default states.

### Method 2 — Runtime Entitlement Simulation

Uses a runtime instrumentation layer to simulate entitlement responses so Samsung Settings and IMS service **believe the line is entitled** for VoWiFi.

---

## 2) Target Platforms

- Samsung Galaxy devices running One UI 5/6/6.1/7 (Android 13–15) (best-effort)
- Rooted device for Method 1.
- Rooted device recommended for Method 2 instrumentation (Frida server or LSPosed). Some flows can run on userdebug/test builds.

---

## 3) Product Components

### A) Android app: `cco-app`

A user-facing control panel with presets, current state readouts, and test runner.

### B) Magisk module: `cco-carrierconfig`

Boot-time bind-mount and file deploy of carrier config override files (Method 1).

### C) Instrumentation bundle: `cco-entitlement`

Runtime hooks for entitlement decisions, with profiles per firmware/package (Method 2).

### D) CLI utility (optional): `ccoctl`

A small companion CLI (adb-friendly) that reads status and triggers app actions via intents.

---

## 4) UX / Screens

### 4.1 Home Dashboard

**Purpose:** show whether WFC should appear and why it’s blocked.

Widgets:

- Device info: model, One UI, build fingerprint
- SIM info: slot, MCC/MNC, carrier name
- IMS status:
  - IMS registered: yes/no
  - VoLTE avail: yes/no
  - VoWiFi avail: yes/no
- WFC UI status:
  - Settings activity exists: yes/no
  - Page populates: yes/no
  - Toggle present: yes/no
- “Likely blocker” indicator:
  - CSC gate suspected
  - Entitlement gate suspected
  - IMS not registered

Actions:

- **Run Diagnostic Scan**
- **Open Wi‑Fi Calling Settings**
- **Export Report** (JSON + text)

### 4.2 Method 1 — CarrierConfig Overrides

Tabs:

- **Presets**
- **Keys**
- **Deploy**

**Presets**

- “Expose WFC UI”
- “WFC Default Enabled”
- “Editable WFC Mode”
- “Wi‑Fi Preferred”
- “Wi‑Fi Only”

**Keys** (editable table) Columns: `key`, `type`, `value`, `notes`

**Deploy**

- Validate prerequisites (root, Magisk, paths)
- Install/Update Magisk module
- Apply overrides per SIM slot (if supported)
- Reboot prompt

### 4.3 Method 2 — Entitlement Simulation

Tabs:

- **Profiles**
- **Hooks**
- **Session**

**Profiles**

- “Generic Samsung IMS entitlement”
- “Carrier plugin: ”
- “Custom (record & replay)”

**Hooks**

- Enable/disable hooks
- Scope by package
  - `com.sec.imsservice`
  - `com.android.settings`
  - carrier entitlement packages (detected)

**Session**

- Start instrumentation session
- Show live events (entitlement requests, responses)
- Stop session
- Export trace

### 4.4 Diagnostics & Logs

- Logcat snapshot buttons:
  - Radio buffer
  - Main buffer
- `dumpsys` snapshot buttons:
  - `dumpsys ims`
  - `dumpsys carrier_config`
  - `getprop` filtered
- Export in a single ZIP

---

## 5) Method 1 Technical Spec — CarrierConfig Override

### 5.1 Architecture

- App builds an `override.xml` from selected keys.
- Magisk module deploys and bind-mounts the file at boot.
- Optionally restarts relevant services (where safe) or requests reboot.

### 5.2 File Locations (variant-aware)

Samsung differs across devices. The module supports a **path matrix**:

- Candidate override paths:
  - `/data/vendor/carrierconfig/override.xml`
  - `/data/vendor/carrierconfig/override_carrier.xml`
  - `/data/misc/carrierconfig/override.xml`
  - `/data/user_de/0/com.android.phone/files/carrierconfig_override.xml` (rare)

Module strategy:

1. Detect which paths are read by system (heuristic + logs).
2. Bind-mount into the detected canonical path.
3. Keep a copy at `/data/adb/cco/overrides/<profile>.xml`.

### 5.3 Key Set (initial)

The app maintains a key catalog (extensible by JSON updates):

**Booleans**

- `carrier_wfc_ims_available_bool`
- `carrier_default_wfc_ims_enabled_bool`
- `editable_wfc_mode_bool`
- `editable_wfc_roaming_mode_bool`
- `carrier_wfc_supports_wifi_only_bool`

**Integers**

- `carrier_default_wfc_ims_mode_int`
- `carrier_default_wfc_ims_roaming_mode_int`

**Strings**

- `wfc_operator_error_codes_string_array` (rare)

Value semantics (best-effort):

- Mode ints commonly map:
  - 0 = cellular preferred
  - 1 = wifi preferred
  - 2 = wifi only

### 5.4 Deployment Flow

1. App collects desired keys.
2. App writes `override.xml` into app-private storage.
3. App asks module installer to copy into `/data/adb/cco/active/override.xml`.
4. Module’s `service.sh` bind-mounts at boot.
5. App prompts reboot.

### 5.5 Magisk Module Layout

```
cco-carrierconfig/
  module.prop
  service.sh
  post-fs-data.sh
  uninstall.sh
  system/ (empty, bind-mount only)
  common/
    busybox (optional)
```

**post-fs-data.sh**

- Ensure `/data/adb/cco` structure exists
- Set SELinux contexts when possible (`restorecon`) on target paths

**service.sh**

- Wait for `/data` mounted
- Resolve target path
- Bind-mount override file
- Emit logs to `/data/adb/cco/logs/module.log`

### 5.6 Safety / Revert

- “Disable overrides” button toggles module state by:
  - swapping bind source to a blank file or
  - setting a `disable` flag read by `service.sh`
- Full uninstall removes `/data/adb/cco` and module.

---

## 6) Method 2 Technical Spec — Runtime Entitlement Simulation

### 6.1 Architecture Options

Provide two backends:

**Backend A: Frida**

- App controls a local instrumentation session.
- A small Frida script hooks entitlement-related calls and forces results.

**Backend B: LSPosed module** (more persistent)

- An Xposed module implements the same hooks.

magisk-carrier-config-override supports both; user chooses backend.

### 6.2 Detection

On install, the app runs:

- Package scan for known entitlement components:
  - `com.sec.imsservice`
  - `com.google.android.ims` (if present)
  - `com.samsung.android.ims.*`
  - carrier-specific entitlement packages
- Method signature detection (mapping database):
  - method name and parameter types per One UI version

The app stores a selected “hook profile”:

- `oneui6_generic`
- `oneui6.1_imsservice_vX`
- `carrier_plugin_<mccmnc>`

### 6.3 Hook Targets (conceptual)

Hook categories:

1. **Entitlement query**
   - functions that return “isWfcEntitled / isVoWiFiEnabled / entitlementStatus”
2. **CarrierConfig gating**
   - optional: force config key values at runtime
3. **Settings rendering gating**
   - optional: ensure Settings reads as “available”

Hook outcomes (per profile):

- Force “entitled=true”
- Optionally force “editable=true”
- Optionally force default mode

### 6.4 Session Control

- Start session: deploy scripts, attach to target process(es)
- Live event stream:
  - entitlement requested
  - returned value
  - caller stack (optional)
- Stop session: detach and cleanup

### 6.5 Record & Replay Mode

- **Record** entitlement interactions and configs into a trace.
- **Replay** same responses deterministically.

Trace schema:

```json
{
  "device": {"model":"...","build":"..."},
  "sim": {"mcc":"...","mnc":"..."},
  "events": [
    {"t":0,"type":"entitlement_query","name":"isWfcEntitled","args":{},"result":false}
  ]
}
```

### 6.6 Safety / Revert

- Hooks are runtime-only (Frida): stopping session returns system to normal.
- LSPosed: toggle module off and reboot.

---

## 7) Diagnostics Spec

### 7.1 Commands used (read-only)

- `dumpsys ims`
- `dumpsys carrier_config`
- `getprop | grep -i ims`
- `logcat -b radio`

### 7.2 Heuristic “Blocker” Logic

Rules (examples):

- If `dumpsys ims` shows not registered → blocker = IMS
- If carrier config says WFC unavailable → blocker = CarrierConfig
- If Settings activity exists but page empty AND IMS registered → blocker = CSC/Entitlement
- If entitlement calls return false → blocker = Entitlement

### 7.3 Report Output

- `report.json`
- `report.txt` (human readable)
- `logs/radio.log`
- `dumpsys/ims.txt`
- `dumpsys/carrier_config.txt`

---

## 8) Security / Permissions

### 8.1 App permissions

- No special runtime permissions required for read-only diagnostics.
- For rooted operations:
  - Uses `su` shell access with user approval.

### 8.2 Threat model

- The toolkit touches telephony/IMS surfaces; must protect logs:
  - redact phone numbers
  - redact IMSI/ICCID by default

### 8.3 Privacy

- No network calls required.
- Optional: export zip locally only.

---

## 9) Implementation Plan

### Milestone 1 — Diagnostics Core

- Dashboard
- Snapshot exports
- Open WFC settings shortcut

### Milestone 2 — Method 1 (Magisk)

- Module installer flow
- Override builder + deploy
- Revert/uninstall

### Milestone 3 — Method 2 (Frida)

- Session controller
- Basic generic hook profile
- Live event stream + export

### Milestone 4 — Profiles & Record/Replay

- Profile DB
- Record/replay traces

---

## 10) Test Plan

### Device matrix

- Single SIM vs dual SIM
- OXM multi-CSC vs carrier firmware
- Wi‑Fi only networks vs mixed

### Scenarios

1. Baseline: empty WFC menu
2. Apply Method 1 preset “Expose WFC UI” → verify menu populates
3. Remove Method 1 → menu returns empty
4. Start Method 2 session “Entitled=true” → verify menu populates without Method 1
5. Stop session → menu returns empty

Acceptance criteria

- 1-click export of a repro bundle
- Overrides applied and reverted cleanly
- Instrumentation session stable for 5 minutes

---

## 11) Deliverables

- `cco-app` (Android)
- `cco-carrierconfig` (Magisk module)
- `cco-entitlement` (Frida scripts + optional LSPosed)
- Documentation:
  - install
  - safety
  - known issues
  - per-One UI profile notes

