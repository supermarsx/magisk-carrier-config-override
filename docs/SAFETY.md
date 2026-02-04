# Safety Guidelines

**READ THIS BEFORE USING CCO**

## ⚠️ Important Disclaimers

### Use at Your Own Risk
- This toolkit modifies system telephony behavior
- Improper use may affect device stability
- Emergency calling functionality is NOT guaranteed
- You are solely responsible for any consequences

### No Warranty
- Software provided "AS-IS" without any warranty
- No guarantee of functionality or safety
- Authors not liable for device damage or service issues

### Legal Considerations
- May violate carrier terms of service
- Could affect device warranty
- Ensure you understand local regulations
- Use on test/non-critical devices first

## 🛡️ Safety Practices

### Before You Start

1. **Backup Everything**
   ```bash
   # Full device backup recommended
   # At minimum, backup:
   - Important data
   - Photos/contacts
   - App data
   ```

2. **Test Device Recommended**
   - First-time users should use a non-primary device
   - Verify functionality before daily driver use
   - Keep stock firmware available for recovery

3. **Understand the Risks**
   - System modifications can brick devices
   - Carrier detection may trigger blocks
   - Emergency services may not work reliably
   - OTA updates may fail or break functionality

### During Use

1. **Emergency Calling**
   - **NEVER rely on this toolkit for emergency calling**
   - Test regular calls before emergencies
   - Keep alternative communication method available
   - Know fallback procedures

2. **Monitor Behavior**
   - Check IMS registration status regularly
   - Monitor call quality
   - Watch for unusual battery drain
   - Be alert to carrier notifications

3. **Incremental Testing**
   - Apply one change at a time
   - Test after each modification
   - Document what works
   - Revert if issues appear

### After Modifications

1. **Verify Functionality**
   - Test regular calls (non-emergency)
   - Test SMS/MMS
   - Test mobile data
   - Test Wi-Fi Calling (if enabled)

2. **Monitor Stability**
   - Watch for crashes in Phone/Settings apps
   - Check for IMS service restarts
   - Monitor network connectivity
   - Review logs for errors

## 🚨 Emergency Procedures

### If Calls Don't Work

1. **Immediate Actions**
   - Disable Wi-Fi Calling in settings
   - Toggle Airplane mode off/on
   - Restart device
   - Try different network mode (LTE/3G)

2. **Revert Changes**
   ```bash
   # Disable Magisk module
   adb shell "su -c 'touch /data/adb/cco/disable'"
   adb reboot
   
   # Or remove module entirely
   # Magisk Manager → Modules → CCO → Uninstall → Reboot
   ```

3. **Safe Mode Boot**
   - Magisk modules disabled in safe mode
   - Use to diagnose if module is cause
   - Volume Down during boot

### If Device Unstable

1. **Stop Frida Sessions**
   - CCO app → Entitlement → Stop Session
   - Or: `adb shell "pkill frida"`

2. **Disable Module**
   ```bash
   adb shell "su -c 'touch /data/adb/modules/cco-carrierconfig/disable'"
   adb reboot
   ```

3. **Factory Reset (Last Resort)**
   - Backup first if possible
   - Settings → General → Reset → Factory Reset
   - Reinstall from stock firmware if needed

## 📋 Known Risks

### High Risk Operations
- Modifying CarrierConfig on production device without backup
- Enabling Wi-Fi Only mode on device without tested fallback
- Using untested hook profiles
- Deploying configurations without understanding keys

### Medium Risk Operations
- Standard CarrierConfig overrides with tested presets
- Frida instrumentation with record/replay mode
- Using generic profiles on supported devices

### Low Risk Operations
- Read-only diagnostics
- Exporting reports
- Viewing IMS status
- Testing with module disabled

## 🔒 Privacy & Security

### Data Handling
- CCO does not transmit data over network
- Logs may contain sensitive info (IMSI, ICCID, phone numbers)
- Redact sensitive data before sharing logs/reports
- Keep exported reports secure

### Root Access
- CCO requires root for full functionality
- Root access exposes device to security risks
- Keep Magisk up to date
- Review apps granted root access regularly

### Permissions
- Grant only necessary permissions
- Review permission requests carefully
- Revoke unused permissions

## 📝 Best Practices

### Recommended Workflow

1. **Research Phase**
   - Read full documentation
   - Check device compatibility
   - Review similar user experiences
   - Understand your device's carrier setup

2. **Preparation Phase**
   - Full device backup
   - Document current working state
   - Prepare recovery method
   - Gather necessary tools (ADB, stock firmware)

3. **Testing Phase**
   - Install on test device first
   - Use diagnostics mode only initially
   - Try presets before custom configs
   - Document results

4. **Deployment Phase**
   - Apply one preset at a time
   - Verify after each change
   - Monitor for 24-48 hours
   - Keep notes of what works

5. **Maintenance Phase**
   - Regularly check status
   - Review logs periodically
   - Keep software updated
   - Re-test after Android updates

### Documentation

Keep records of:
- Device info (model, firmware version)
- Working configurations
- Failed attempts
- Logs of successful setups
- Recovery procedures used

## ⛔ What NOT To Do

### Never
- Use in life-critical situations
- Assume emergency calling works
- Deploy untested configs to production device
- Share logs with sensitive data
- Modify system partition directly
- Use outdated profiles on new firmware
- Ignore stability issues

### Avoid
- Testing multiple changes simultaneously
- Skipping backups
- Using without understanding
- Ignoring carrier notifications
- Dismissing error messages

## 🆘 Getting Help

### Before Asking for Help

1. Check [Troubleshooting Guide](TROUBLESHOOTING.md)
2. Search GitHub Issues
3. Review logs at `/data/adb/cco/logs/`
4. Document exact steps to reproduce
5. Note device/firmware details

### When Reporting Issues

Include:
- Device model and firmware version
- CCO version
- Steps to reproduce
- Relevant logs (redacted)
- Expected vs actual behavior

### Do NOT Share
- Full IMSI/ICCID numbers
- Phone numbers
- Personal identifiable information
- Carrier-specific authentication tokens

## 📞 Emergency Contacts

Always have alternative methods to contact emergency services:
- Landline phone
- Neighbor's phone
- Public phone
- Alternative mobile device

**Remember: Wi-Fi Calling enabled by this toolkit may not support emergency calls reliably.**

## ✅ Safety Checklist

Before deploying to daily driver:

- [ ] Full device backup completed
- [ ] Tested on non-critical device
- [ ] Emergency contact method available
- [ ] Regular calls tested and working
- [ ] SMS/Data connectivity verified
- [ ] No stability issues observed
- [ ] Logs reviewed for errors
- [ ] Recovery procedure understood
- [ ] All documentation read
- [ ] Risks fully understood

## Legal Notice

By using this toolkit, you acknowledge that:
- You have read and understood these safety guidelines
- You accept all risks associated with use
- You will not hold authors liable for any issues
- You understand emergency calling may not work
- You are responsible for compliance with local laws and carrier policies

---

**When in doubt, don't proceed. Safety first.**
