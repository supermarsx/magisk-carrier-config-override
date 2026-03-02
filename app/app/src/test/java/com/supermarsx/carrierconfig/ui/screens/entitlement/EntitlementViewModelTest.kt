package com.supermarsx.carrierconfig.ui.screens.entitlement

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for EntitlementViewModel data models and state.
 *
 * The ViewModel itself depends on FridaManager and ProfileManager which require
 * root/Context. Here we fully test the pure-Kotlin state models and enums.
 */
class EntitlementViewModelTest {

    // =========================================================================
    // EntitlementTab enum
    // =========================================================================

    @Test
    fun `EntitlementTab has 3 values`() {
        assertThat(EntitlementTab.values()).hasLength(3)
    }

    @Test
    fun `EntitlementTab display names`() {
        assertThat(EntitlementTab.PROFILES.displayName).isEqualTo("Profiles")
        assertThat(EntitlementTab.HOOKS.displayName).isEqualTo("Hooks")
        assertThat(EntitlementTab.SESSION.displayName).isEqualTo("Session")
    }

    // =========================================================================
    // HookBackend enum
    // =========================================================================

    @Test
    fun `HookBackend has 2 values`() {
        assertThat(HookBackend.values()).hasLength(2)
    }

    @Test
    fun `HookBackend display names`() {
        assertThat(HookBackend.FRIDA.displayName).isEqualTo("Frida")
        assertThat(HookBackend.LSPOSED.displayName).isEqualTo("LSPosed")
    }

    // =========================================================================
    // EntitlementState defaults
    // =========================================================================

    @Test
    fun `EntitlementState default values`() {
        val state = EntitlementState()
        assertThat(state.profiles).isEmpty()
        assertThat(state.profilesMetadata).isNull()
        assertThat(state.selectedProfileId).isNull()
        assertThat(state.isLoadingProfiles).isFalse()
        assertThat(state.selectedBackend).isEqualTo(HookBackend.FRIDA)
        assertThat(state.entitlementPackages).isEmpty()
        assertThat(state.isScanningPackages).isFalse()
        assertThat(state.sessionActive).isFalse()
        assertThat(state.fridaInstalled).isFalse()
        assertThat(state.fridaRunning).isFalse()
        assertThat(state.fridaVersion).isNull()
        assertThat(state.fridaPid).isNull()
        assertThat(state.isInstallingFrida).isFalse()
        assertThat(state.selectedTab).isEqualTo(EntitlementTab.PROFILES)
        assertThat(state.error).isNull()
    }

    // =========================================================================
    // EntitlementState copy transitions
    // =========================================================================

    @Test
    fun `copy with profile selection`() {
        val state = EntitlementState().copy(selectedProfileId = "oneui6_generic")
        assertThat(state.selectedProfileId).isEqualTo("oneui6_generic")
    }

    @Test
    fun `copy with backend switch`() {
        val state = EntitlementState().copy(selectedBackend = HookBackend.LSPOSED)
        assertThat(state.selectedBackend).isEqualTo(HookBackend.LSPOSED)
    }

    @Test
    fun `copy with session active`() {
        val state = EntitlementState().copy(sessionActive = true)
        assertThat(state.sessionActive).isTrue()
    }

    @Test
    fun `copy with frida status`() {
        val state = EntitlementState().copy(
            fridaInstalled = true,
            fridaRunning = true,
            fridaVersion = "16.0.0",
            fridaPid = 12345
        )
        assertThat(state.fridaInstalled).isTrue()
        assertThat(state.fridaRunning).isTrue()
        assertThat(state.fridaVersion).isEqualTo("16.0.0")
        assertThat(state.fridaPid).isEqualTo(12345)
    }

    @Test
    fun `copy with loading profiles`() {
        val state = EntitlementState().copy(isLoadingProfiles = true)
        assertThat(state.isLoadingProfiles).isTrue()
    }

    @Test
    fun `copy with error`() {
        val state = EntitlementState().copy(error = "Frida not found")
        assertThat(state.error).isEqualTo("Frida not found")
    }

    @Test
    fun `copy with scanning packages`() {
        val state = EntitlementState().copy(isScanningPackages = true)
        assertThat(state.isScanningPackages).isTrue()
    }

    @Test
    fun `copy with installing frida`() {
        val state = EntitlementState().copy(isInstallingFrida = true)
        assertThat(state.isInstallingFrida).isTrue()
    }

    // =========================================================================
    // EntitlementPackage
    // =========================================================================

    @Test
    fun `EntitlementPackage data class`() {
        val pkg = EntitlementPackage(
            packageName = "com.sec.imsservice",
            versionName = "12.0.00.20",
            isInstalled = true,
            isHookTarget = true
        )
        assertThat(pkg.packageName).isEqualTo("com.sec.imsservice")
        assertThat(pkg.versionName).isEqualTo("12.0.00.20")
        assertThat(pkg.isInstalled).isTrue()
        assertThat(pkg.isHookTarget).isTrue()
    }

    @Test
    fun `EntitlementPackage equality`() {
        val a = EntitlementPackage("com.sec.ims", "1.0", true, true)
        val b = EntitlementPackage("com.sec.ims", "1.0", true, true)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `EntitlementPackage copy toggles hook target`() {
        val pkg = EntitlementPackage("com.sec.ims", "1.0", true, true)
        val toggled = pkg.copy(isHookTarget = !pkg.isHookTarget)
        assertThat(toggled.isHookTarget).isFalse()
    }

    @Test
    fun `EntitlementPackage list manipulation`() {
        val packages = listOf(
            EntitlementPackage("com.sec.imsservice", "12.0", true, true),
            EntitlementPackage("com.sec.ims", "11.0", true, false),
            EntitlementPackage("com.sec.epdg", "5.0", true, true)
        )
        val hookTargets = packages.filter { it.isHookTarget }
        assertThat(hookTargets).hasSize(2)
        assertThat(hookTargets.map { it.packageName }).containsExactly(
            "com.sec.imsservice", "com.sec.epdg"
        )
    }

    // =========================================================================
    // SessionEvent / SessionEventType
    // =========================================================================

    @Test
    fun `SessionEventType has 3 values`() {
        assertThat(SessionEventType.values()).hasLength(3)
        assertThat(SessionEventType.values().map { it.name }).containsExactly("INFO", "HOOK", "ERROR")
    }

    @Test
    fun `SessionEvent data class`() {
        val event = SessionEvent(
            timestamp = System.currentTimeMillis(),
            type = SessionEventType.INFO,
            message = "Session started"
        )
        assertThat(event.type).isEqualTo(SessionEventType.INFO)
        assertThat(event.message).isEqualTo("Session started")
        assertThat(event.timestamp).isGreaterThan(0L)
    }

    @Test
    fun `SessionEvent equality`() {
        val ts = 100L
        val a = SessionEvent(ts, SessionEventType.HOOK, "intercepted")
        val b = SessionEvent(ts, SessionEventType.HOOK, "intercepted")
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `SessionEvent list takeLast simulates buffer limit`() {
        val events = (1..600).map {
            SessionEvent(it.toLong(), SessionEventType.INFO, "event $it")
        }.takeLast(500)
        assertThat(events).hasSize(500)
        assertThat(events.first().timestamp).isEqualTo(101L)
    }

    // =========================================================================
    // EntitlementState with packages
    // =========================================================================

    @Test
    fun `EntitlementState with packages populated`() {
        val packages = listOf(
            EntitlementPackage("com.sec.imsservice", "12.0", true, true),
            EntitlementPackage("com.google.android.ims", "1.0", true, false)
        )
        val state = EntitlementState(entitlementPackages = packages)
        assertThat(state.entitlementPackages).hasSize(2)
    }

    @Test
    fun `EntitlementState toggle package hook in list`() {
        val packages = listOf(
            EntitlementPackage("com.sec.imsservice", "12.0", true, true),
            EntitlementPackage("com.sec.ims", "11.0", true, false)
        )
        val updated = packages.map { pkg ->
            if (pkg.packageName == "com.sec.ims") pkg.copy(isHookTarget = !pkg.isHookTarget) else pkg
        }
        assertThat(updated[1].isHookTarget).isTrue()
    }

    // =========================================================================
    // EntitlementState tab
    // =========================================================================

    @Test
    fun `EntitlementState tab navigation`() {
        val state = EntitlementState(selectedTab = EntitlementTab.HOOKS)
        assertThat(state.selectedTab).isEqualTo(EntitlementTab.HOOKS)
    }

    @Test
    fun `EntitlementState all tabs are navigable`() {
        EntitlementTab.values().forEach { tab ->
            val state = EntitlementState(selectedTab = tab)
            assertThat(state.selectedTab).isEqualTo(tab)
        }
    }

    // =========================================================================
    // EntitlementState equality
    // =========================================================================

    @Test
    fun `two default EntitlementStates are equal`() {
        assertThat(EntitlementState()).isEqualTo(EntitlementState())
    }

    @Test
    fun `modified EntitlementState not equal to default`() {
        assertThat(EntitlementState(sessionActive = true)).isNotEqualTo(EntitlementState())
    }
}
