# Troubleshooting Guide

Common issues and solutions for CCO.

## Table of Contents
- [Installation Issues](#installation-issues)
- [Root Access Problems](#root-access-problems)
- [Magisk Module Issues](#magisk-module-issues)
- [Wi-Fi Calling Not Appearing](#wi-fi-calling-not-appearing)
- [IMS Registration Issues](#ims-registration-issues)
- [Frida/Instrumentation Issues](#fridainstrumentation-issues)
- [App Crashes](#app-crashes)
- [Logs and Diagnostics](#logs-and-diagnostics)

---

## Installation Issues

### "App not installed" error

**Symptoms**: APK installation fails

**Solutions**:
```bash
# Check available storage
adb shell "df -h /data"

# Uninstall old version
adb uninstall com.supermarsx.carrierconfig

# Try installing again
adb install -r cco-app.apk

# If still fails, check logcat
adb logcat | grep -i "package"
```

### Unable to grant permissions

**Symptoms**: Permission dialogs don't appear or can't be granted

**Solutions**:
1. Settings → Apps → CCO → Permissions → Grant manually
2. For phone permission on Android 13+:
   ```bash
   adb shell "pm grant com.supermarsx.carrierconfig android.permission.READ_PHONE_STATE"
   ```

---

## Root Access Problems

### "Root access denied"

**Symptoms**: App shows "Root not available" or requests fail

**Diagnosis**:
```bash
# Test root via ADB
adb shell "su -c id"
# Should show: uid=0(root)

# Check Magisk status
adb shell "su -c magisk -v"
```

**Solutions**:
1. Open Magisk Manager
2. Check Magisk is properly installed
3. Grant root to CCO in Magisk settings
4. Try:
   ```bash
   adb shell "su -c setenforce 0"  # Temporary, testing only
   ```

### Root works in other apps but not CCO

**Symptoms**: Other root apps work, CCO doesn't detect root

**Solutions**:
1. Clear CCO app data: Settings → Apps → CCO → Storage → Clear Data
2. Grant root again
3. Check if Magisk hide is enabled for CCO (should be OFF)
4. Reinstall app

---

## Magisk Module Issues

### Module not appearing in Magisk Manager

**Symptoms**: Installed module not showing up

**Diagnosis**:
```bash
# Check module directory
adb shell "ls -la /data/adb/modules/"

# Check module.prop
adb shell "cat /data/adb/modules/cco-carrierconfig/module.prop"
```

**Solutions**:
1. Verify module.prop exists and is correct
2. Reinstall module
3. Check Magisk version (need 24.0+)

### Module installed but not working

**Symptoms**: Module shows as installed but override not applied

**Diagnosis**:
```bash
# Check service.sh executed
adb shell "cat /data/adb/cco/logs/module.log"

# Check if bind mount succeeded
adb shell "mount | grep override"

# Check active override exists
adb shell "cat /data/adb/cco/active/override.xml"
```

**Solutions**:

1. **No active override file**:
   ```bash
   # Deploy a configuration via app first
   # Or create manually at /data/adb/cco/active/override.xml
   ```

2. **Bind mount failed**:
   ```bash
   # Check target directory exists
   adb shell "ls -la /data/vendor/carrierconfig/"
   
   # Try manual mount (testing)
   adb shell "su -c 'mount --bind /data/adb/cco/active/override.xml /data/vendor/carrierconfig/override.xml'"
   ```

3. **SELinux blocking**:
   ```bash
   # Check denials
   adb shell "su -c 'dmesg | grep denied | grep cco'"
   
   # Temporary test (revert after)
   adb shell "su -c setenforce 0"
   adb reboot
   ```

4. **Wrong target path**:
   - Check module log for detected path
   - Your device may use different path
   - Edit `service.sh` to add your device's path

---

## Wi-Fi Calling Not Appearing

### Settings menu empty/missing

**Symptoms**: Wi-Fi Calling settings exist but page is blank or toggles missing

**Diagnosis**:
```bash
# Check CarrierConfig
adb shell "dumpsys carrier_config | grep -i wfc"

# Check IMS status
adb shell "dumpsys ims | grep -i wfc"

# Check if settings activity exists
adb shell "pm list packages | grep settings"
```

**Solutions**:

1. **CarrierConfig not applied**:
   - Verify Magisk module is active
   - Check override.xml is deployed
   - Reboot device
   - Verify mount:
     ```bash
     adb shell "mount | grep override"
     ```

2. **CSC/Country lock**:
   - This may require entitlement simulation (Method 2)
   - Try changing CSC (advanced, risky)
   - Use Frida hooks to bypass CSC checks

3. **IMS not registered**:
   ```bash
   # Toggle airplane mode
   adb shell "settings put global airplane_mode_on 1"
   sleep 2
   adb shell "settings put global airplane_mode_on 0"
   
   # Or via UI: Toggle airplane mode off/on
   ```

4. **Wrong SIM slot**:
   - Check if settings show for SIM 1 vs SIM 2
   - Some configs are per-slot
   - Try switching SIM slots

### Settings appear but can't enable

**Symptoms**: Toggle present but can't be turned on or reverts to off

**Causes**:
- Entitlement check failing
- Carrier server returning false
- IMS not properly registered

**Solutions**:
1. Use Method 2 (Frida instrumentation)
2. Start entitlement session before enabling
3. Check real entitlement status with carrier

---

## IMS Registration Issues

### IMS not registering

**Symptoms**: `dumpsys ims` shows "NOT_REGISTERED"

**Diagnosis**:
```bash
# Check IMS state
adb shell "dumpsys ims"

# Check if VoLTE is enabled
adb shell "settings get global volte_vt_enabled"

# Check network registration
adb shell "dumpsys telephony.registry | grep -i service"
```

**Solutions**:

1. **Enable VoLTE first**:
   - Settings → Connections → Mobile Networks → VoLTE Calls
   - Or: `adb shell "settings put global volte_vt_enabled 1"`

2. **Network issues**:
   - Toggle airplane mode
   - Switch network mode (4G/5G/3G)
   - Reboot

3. **SIM issues**:
   - Check SIM is active
   - Check carrier supports VoLTE
   - Try SIM in different device

4. **Carrier restrictions**:
   - Some carriers have provisioning requirements
   - May need carrier unlock code
   - Contact carrier support

---

## Frida/Instrumentation Issues

### Frida server not starting

**Symptoms**: Can't connect to Frida, `frida-ps -U` fails

**Diagnosis**:
```bash
# Check if server is running
adb shell "ps | grep frida"

# Check architecture
adb shell "getprop ro.product.cpu.abi"
```

**Solutions**:

1. **Wrong architecture**:
   - Download correct frida-server (arm64 for modern devices)
   - Verify: `file frida-server-*-android-arm64`

2. **SELinux blocking**:
   ```bash
   adb shell "su -c setenforce 0"  # Temporary
   adb shell "su -c /data/local/tmp/frida-server &"
   ```

3. **Port conflict**:
   ```bash
   # Check if port 27042 is in use
   adb shell "netstat -tlnp | grep 27042"
   
   # Kill conflicting process
   adb shell "su -c 'pkill frida'"
   ```

4. **Start frida-server properly**:
   ```bash
   adb shell "su -c '/data/local/tmp/frida-server -D'"
   # -D = daemonize (background)
   ```

### Hooks not applying

**Symptoms**: Frida session active but hooks don't trigger

**Diagnosis**:
```bash
# Check target process is running
adb shell "ps | grep imsservice"

# Check Frida can attach
frida-ps -U | grep imsservice

# Test basic hook
frida -U -n com.sec.imsservice -e 'console.log("test")'
```

**Solutions**:

1. **Wrong package name**:
   - Verify target package exists
   - Check actual package name: `adb shell "pm list packages | grep ims"`

2. **Class/method not found**:
   - Target may be obfuscated or different in your firmware
   - Use record mode to discover correct signatures
   - Check One UI version compatibility

3. **Timing issues**:
   - Hook may load after target method already called
   - Try restarting target app after starting session

---

## App Crashes

### CCO app crashes on launch

**Diagnosis**:
```bash
# Get crash logs
adb logcat -b crash | grep cco

# Or full logcat
adb logcat | grep -i "crash\|exception" | grep cco
```

**Solutions**:
1. Clear app data and cache
2. Check Android version compatibility (need 13+)
3. Reinstall app
4. Report crash with logs to GitHub Issues

### Phone app crashes after modifications

**Symptoms**: com.android.phone crashes or restarts

**Solutions**:
```bash
# Disable CCO module immediately
adb shell "su -c 'touch /data/adb/modules/cco-carrierconfig/disable'"
adb reboot

# Check what caused it
adb logcat -b crash | grep "com.android.phone"

# May need to:
# - Revert override.xml
# - Use less aggressive configuration
# - Report issue with device details
```

---

## Logs and Diagnostics

### Collecting logs for bug reports

```bash
# 1. CCO app logs
adb shell "cat /data/adb/cco/logs/module.log" > cco-module.log

# 2. Magisk logs
adb shell "cat /data/adb/magisk.log" > magisk.log

# 3. Logcat
adb logcat -d > logcat.txt

# 4. IMS status
adb shell "dumpsys ims" > dumpsys-ims.txt

# 5. CarrierConfig
adb shell "dumpsys carrier_config" > dumpsys-carrier.txt

# 6. Device info
adb shell "getprop" > device-props.txt

# Package as report
tar -czf cco-debug-$(date +%Y%m%d).tar.gz *.log *.txt

# IMPORTANT: Review and redact sensitive info before sharing
```

### Reading CCO module logs

```bash
# View real-time
adb shell "su -c 'tail -f /data/adb/cco/logs/module.log'"

# Check for errors
adb shell "cat /data/adb/cco/logs/module.log | grep -i error"

# Check bind mount status
adb shell "cat /data/adb/cco/logs/module.log | grep -i mount"
```

---

## Still Having Issues?

1. **Search existing issues**: [GitHub Issues](https://github.com/YOUR_USERNAME/magisk-carrier-config-override/issues)
2. **Check discussions**: [GitHub Discussions](https://github.com/YOUR_USERNAME/magisk-carrier-config-override/discussions)
3. **Create new issue**:
   - Use appropriate template
   - Include device/firmware info
   - Attach relevant logs (redacted)
   - Describe exact steps to reproduce

4. **Emergency revert**:
   ```bash
   # Boot to safe mode (Magisk modules disabled)
   # Or remove module:
   adb shell "su -c 'rm -rf /data/adb/modules/cco-carrierconfig'"
   adb reboot
   ```

Remember: Always review [Safety Guidelines](SAFETY.md) when troubleshooting.
